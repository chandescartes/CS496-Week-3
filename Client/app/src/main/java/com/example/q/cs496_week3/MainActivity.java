package com.example.q.cs496_week3;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    CustomAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    boolean doubleBack = false;
    public static String nickname;
    double lat;
    double lng;
    private Socket mSocket;
    Context context;

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
        int max_member;
        ArrayList<String> member;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        askForPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nickname = UserInfo.getNickname();
        lat = UserInfo.getLatv();
        lng = UserInfo.getLngv();

        ChatApplication app = (ChatApplication) this.getApplication();
        mSocket = app.getSocket();
        mSocket.connect();

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFAB();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog customDialog = new CustomDialog(context);
                customDialog.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // remove later
        Button button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject data = new JSONObject();
                try {
                    data.put("nickname", nickname);
                    data.put("room", "room1");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                mSocket.emit("user-joined", data);
                Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                intent.putExtra("room", "room1");
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        Refresh();
    }

    private class CustomAdapter extends ArrayAdapter<Room> {
        public void filter(String searchText) {
            searchText = searchText.toLowerCase(Locale.getDefault());
            displayitems.clear();
            if (searchText.length() == 0) {
                displayitems.addAll(items);
            } else {
                for (Room item : items) {
                    if (item.title.contains(searchText)) {
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

            if (displayitems.get(position).food.equals("치킨")) kindoffood.setImageResource(R.drawable.chickenleg);
            if (displayitems.get(position).food.equals("피자")) kindoffood.setImageResource(R.drawable.pizza);
            if (displayitems.get(position).food.equals("족발/보쌈")) kindoffood.setImageResource(R.drawable.jokbal);
            if (displayitems.get(position).food.equals("샌드위치/햄버거")) kindoffood.setImageResource(R.drawable.sandwich);
            if (displayitems.get(position).food.equals("중국집")) kindoffood.setImageResource(R.drawable.noodles);
            if (displayitems.get(position).food.equals("한식/분식")) kindoffood.setImageResource(R.drawable.rice);
            if (displayitems.get(position).food.equals("일식")) kindoffood.setImageResource(R.drawable.sushi);

            titleview.setText(displayitems.get(position).title);
            foodview.setText(displayitems.get(position).food);
            Log.d("lat1",String.valueOf(displayitems.get(position).lat));
            Log.d("lng1",String.valueOf(displayitems.get(position).lng));
            Log.d("lat2",String.valueOf(UserInfo.getLatv()));
            Log.d("lng2",String.valueOf(UserInfo.getLngv()));
            double distance = getDistanceFromLatLonInm(displayitems.get(position).lat,
                    displayitems.get(position).lng,
                    UserInfo.getLatv(), UserInfo.getLngv());
            distanceview.setText(String.valueOf(Math.round(distance))+"m");
            String numbers = String.valueOf(displayitems.get(position).member.size())+" / "
                    +String.valueOf(displayitems.get(position).max_member);
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

        } else if (id == R.id.join_room) {

        } else if (id == R.id.quick_match) {

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

    private void Refresh() {
        RoomArrList = new ArrayList<Room>();
        // listview
        ListView listview = (ListView) findViewById(R.id.roomListView);
        // sample test

        HttpCall.setMethodtext("GET");
        HttpCall.setUrltext("/api/room");
        JSONArray roomlist = new JSONArray();
        try {
            roomlist = new JSONArray(HttpCall.getResponse());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i=0;i<roomlist.length();i++) {
            Room a = new Room();
            try {
                a.id = roomlist.getJSONObject(i).getString("id");
                a.title = roomlist.getJSONObject(i).getString("title");
                a.food = roomlist.getJSONObject(i).getString("food");
                a.created_at = roomlist.getJSONObject(i).getString("created_at");
                a.founder = roomlist.getJSONObject(i).getString("founder");
                a.lat = roomlist.getJSONObject(i).getDouble("lat");
                a.lng = roomlist.getJSONObject(i).getDouble("lng");
                a.max_member = roomlist.getJSONObject(i).getInt("max_num");
                Log.d("maxmemberis",String.valueOf(a.max_member));
                a.member = new ArrayList<>();
                for (int j=0;j<roomlist.getJSONObject(i).getJSONArray("members").length();j++) {
                    a.member.add(roomlist.getJSONObject(i).getJSONArray("members").getString(j));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RoomArrList.add(a);
        }

        adapter = new CustomAdapter(context, R.layout.room_row, RoomArrList);
        adapter.filter("");
        if (listview != null)
            listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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

    public void askForPermissions() {
        Log.d("permissioncheck", "AAAA");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1
                );
            }
        }
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
}
