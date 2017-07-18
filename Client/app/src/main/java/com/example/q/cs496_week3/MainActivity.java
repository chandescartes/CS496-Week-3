package com.example.q.cs496_week3;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;

import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public final String S_GET_ROOMS = "get-rooms";
    public final String S_JOIN_ROOM = "join-room";
    public final String S_USER_JOINED = "user-joined";

    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    InputMethodManager imm;
    EditText mainsearchtitle;
    ImageButton mainsearchbtn;
    Spinner mainsearchfood;
    CustomAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ListView roomListView;

    boolean doubleBack = false;
    public static String nickname;
    double lat;
    double lng;

    public static Socket mSocket;
    static Context context;

    ArrayList<Room> items;
    ArrayList<Room> RoomArrList = new ArrayList<>();
    ArrayList<Room> displayitems = new ArrayList<>();

    public class Room {
        String id;
        String title;
        String created_at;
        String founder;
        String food;
        double lat;
        double lng;
        int max_members;
        int current_members;

        public double getDistance() {
            return getDistanceFromLatLonInm(lat, lng, UserInfo.getLatv(), UserInfo.getLngv());
        }
    }

    public class Ascending implements Comparator<Room> {
        public int compare(Room room, Room t1) {
            return (int) ((room.getDistance() - t1.getDistance())*10000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        nickname = UserInfo.getNickname();
        lat = UserInfo.getLatv();
        lng = UserInfo.getLngv();
        roomListView = (ListView) findViewById(R.id.roomListView);
        adapter = new CustomAdapter(this, R.layout.room_row, RoomArrList);
        adapter.filter("", "All");
        roomListView.setAdapter(adapter);
        roomListView.setOnItemClickListener(roomListViewListener);

        ChatApplication app = (ChatApplication) this.getApplication();
        mSocket = app.getSocket();
        mSocket.on(S_GET_ROOMS, onGetRooms);
        mSocket.on(S_JOIN_ROOM, onJoinRoom);
        mSocket.connect();
        mSocket.emit(S_GET_ROOMS, "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab.setOnClickListener(myListener);
        fab1.setOnClickListener(myListener);
        fab2.setOnClickListener(myListener);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_red_light
        );

        mainsearchtitle = (EditText) findViewById(R.id.search_room_main_title);
        mainsearchfood = (Spinner) findViewById(R.id.search_room_main_food);
        mainsearchbtn = (ImageButton) findViewById(R.id.searchBtn);
        mainsearchtitle.addTextChangedListener(textWatcher);
        mainsearchbtn.setOnClickListener(myListener);
    }

    @Override
    public void onPause () {
        super.onPause();
//        mSwipeRefreshLayout.setOnRefreshListener(null);
//        mainsearchtitle.addTextChangedListener(null);
//        mainsearchbtn.setOnClickListener(null);
    }

    @Override
    public void onResume () {
        super.onResume();
        Log.d("RESUME", "RESUME");

//        ChatApplication app = (ChatApplication) this.getApplication();
//        mSocket = app.getSocket();
//
//        mSocket.on(S_GET_ROOMS, onGetRooms);
//        mSocket.on(S_JOIN_ROOM, onJoinRoom);
//        mSocket.connect();
        mSocket.emit(S_GET_ROOMS, "");

        mSwipeRefreshLayout.setOnRefreshListener(swipeRefresh);
        mainsearchtitle.addTextChangedListener(textWatcher);
        mainsearchbtn.setOnClickListener(myListener);
    }

    SwipeRefreshLayout.OnRefreshListener swipeRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mSocket.emit(S_GET_ROOMS, "");
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    TextWatcher textWatcher = new TextWatcher() {
        String previousString = "";
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            previousString= charSequence.toString();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (mainsearchtitle.getLineCount() >= 2)
            {
                mainsearchtitle.setText(previousString);
                mainsearchtitle.setSelection(mainsearchtitle.length());
            }
        }
    };

    View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            imm.hideSoftInputFromWindow(mainsearchtitle.getWindowToken(), 0);
            switch (view.getId()) {
                case R.id.searchBtn :
                    String search_title = mainsearchtitle.getText().toString().trim();
                    String search_food = mainsearchfood.getSelectedItem().toString();
                    adapter.filter(search_title, search_food);
                    break;

                case R.id.fab :
                    animateFAB();
                    break;

                case R.id.fab1 :
                    animateFAB();
                    CustomDialog customDialog = new CustomDialog(MainActivity.this);
                    customDialog.show();
                    break;

                case R.id.fab2 :
                    animateFAB();
                    MatchDialog matchDialogDialog = new MatchDialog(MainActivity.this);
                    matchDialogDialog.show();
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener roomListViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Room room = RoomArrList.get(i);
            JSONObject data = new JSONObject();
            try {
                data.put("room", room.id);
                data.put("nickname", nickname);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mSocket.emit(S_JOIN_ROOM, data);
        }
    };

    private Emitter.Listener onJoinRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject resultData = (JSONObject) args[0];

            try {
                if (resultData.getBoolean("result")) {
                    JSONObject data = new JSONObject();
                    data.put("room", resultData.getString("room"));
                    data.put("nickname", nickname);
                    mSocket.emit(S_USER_JOINED, data);

                    Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                    intent.putExtra("room", resultData.getString("room"));
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onGetRooms = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Iterator<?> keys = data.keys();
            Log.d("onGetRooms", data.toString());

            RoomArrList.clear();

            while (keys.hasNext()) {
                Room room = new Room();
                String key = (String) keys.next();
                try {
                    JSONObject item = (JSONObject) data.get(key);
                    room.id = key;
                    room.created_at = (String) item.get("created_at");
                    room.title = (String) item.get("title");
                    room.founder = (String) item.get("founder");
                    room.food = (String) item.get("food");
                    room.max_members = (Integer) item.get("limit");
                    room.current_members = ((JSONObject) item.get("sockets")).length();
                    room.lat = Double.parseDouble((String) item.get("lat"));
                    room.lng = Double.parseDouble((String) item.get("long"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RoomArrList.add(room);
            }
            Ascending ascending = new Ascending();
            Collections.sort(RoomArrList, ascending);
            Log.d("RoomArrList", RoomArrList.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };

    private class CustomAdapter extends ArrayAdapter<Room> {
        public void filter(String searchText, String searchFood) {
            searchText = searchText.toLowerCase(Locale.getDefault());
            displayitems.clear();
            if (searchText.length() == 0) {
                displayitems.addAll(items);
            } else {
                for (Room item : items) {
                    if (item.title.contains(searchText)) {
                        if (item.food.equals(searchFood) || searchFood.equals("All"))
                            displayitems.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<Room> objects) {
            super(context, textViewResourceId, objects);
            displayitems = objects;
            items = new ArrayList<>();
            items.addAll(displayitems);
        }

        @Override
        public int getCount() {
            return displayitems.size();
        }

        @Override
        public Room getItem(int position) {
            return displayitems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.room_row, null);
            }

            TextView titleview = (TextView) v.findViewById(R.id.list_title);
            TextView foodview = (TextView) v.findViewById(R.id.list_food);
            TextView distanceview = (TextView) v.findViewById(R.id.list_distance);
            TextView numberview = (TextView) v.findViewById(R.id.list_number);
            ImageView kindoffood = (ImageView) v.findViewById(R.id.kind_of_food);

            if (displayitems.get(position).food != null) {
                if (displayitems.get(position).food.equals("치킨"))
                    kindoffood.setImageResource(R.drawable.chickenleg);
                if (displayitems.get(position).food.equals("피자"))
                    kindoffood.setImageResource(R.drawable.pizza);
                if (displayitems.get(position).food.equals("족발/보쌈"))
                    kindoffood.setImageResource(R.drawable.jokbal);
                if (displayitems.get(position).food.equals("샌드위치/햄버거"))
                    kindoffood.setImageResource(R.drawable.sandwich);
                if (displayitems.get(position).food.equals("중국집"))
                    kindoffood.setImageResource(R.drawable.noodles);
                if (displayitems.get(position).food.equals("한식/분식"))
                    kindoffood.setImageResource(R.drawable.rice);
                if (displayitems.get(position).food.equals("일식"))
                    kindoffood.setImageResource(R.drawable.sushi);
            }

            titleview.setText(displayitems.get(position).title);
            foodview.setText(displayitems.get(position).food);
            Log.d("lat1",String.valueOf(displayitems.get(position).lat));
            Log.d("lng1",String.valueOf(displayitems.get(position).lng));
            Log.d("lat2",String.valueOf(UserInfo.getLatv()));
            Log.d("lng2",String.valueOf(UserInfo.getLngv()));
            double distance = getDistanceFromLatLonInm(displayitems.get(position).lat,
                    displayitems.get(position).lng,
                    UserInfo.getLatv(), UserInfo.getLngv());
            if (distance > 1000000) distanceview.setText(String.valueOf(Math.round(distance/10000d)/100d)+"Mm");
            else if (distance > 1000) distanceview.setText(String.valueOf(Math.round(distance/10d)/100d)+"km");
            else distanceview.setText(String.valueOf(Math.round(distance))+"m");
            String numbers = String.valueOf(displayitems.get(position).current_members)+" / "
                    +String.valueOf(displayitems.get(position).max_members);
            numberview.setText(numbers);

            return v;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBack) {
                super.onBackPressed();
                return;
            }

            doubleBack = true;
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBack = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        askForPermissions();
        getMenuInflater().inflate(R.menu.main, menu);
        TextView nicknameText = (TextView) findViewById(R.id.nicknameHeader);
        nicknameText.setText(nickname);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.usermap);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng currloc = new LatLng(lat, lng);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currloc, 15);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(currloc);
                markerOptions.title("현재 위치");
                googleMap.addMarker(markerOptions);
                googleMap.animateCamera(cameraUpdate);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.make_room) {
            CustomDialog customDialog = new CustomDialog(MainActivity.this);
            customDialog.show();
        } else if (id == R.id.quick_match) {
            MatchDialog matchDialog = new MatchDialog(MainActivity.this);
            matchDialog.show();
        } else if (id == R.id.my_profile) {
            Intent intent = new Intent(MainActivity.this, EditNickname.class);
            startActivity(intent);
        } else if (id == R.id.my_location) {
            Intent intent = new Intent(MainActivity.this, EditLocation.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    double getDistanceFromLatLonInm(double lat1,double lon1,double lat2,double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        Log.d("distanceis", String.valueOf(d));
        return d*1000;
    }

    double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }

    public void askForPermissions() {
        Log.d("permissioncheck", "AAAA");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }
}
