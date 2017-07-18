package com.example.q.cs496_week3;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MatchDialog extends Dialog {

    public final String S_GET_ROOMS = "get-rooms";
    public final String S_JOIN_ROOM = "join-room";
    public final String S_USER_JOINED = "user-joined";

    ArrayList<Room> RoomArrList = new ArrayList<>();
    ArrayList<Room> SelectedList = new ArrayList<>();
    Activity mActivity;
    Socket mSocket;

    public class Room {
        String id;
        String created_at;
        String title;
        String founder;
        String food;
        int max_members;
        int current_members;
        double lat;
        double lng;

        public double getDistance() {
            return getDistanceFromLatLonInm(lat, lng, UserInfo.getLatv(), UserInfo.getLngv());
        }
    }

    public MatchDialog(Activity activity) {
        super(activity);
        mActivity = activity;
    }

    Spinner food;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        setContentView(R.layout.match_dialog_layout);

        ChatApplication app = (ChatApplication) mActivity.getApplication();

        mSocket = app.getSocket();
        mSocket.on(S_GET_ROOMS, onGetRooms);
        mSocket.on(S_JOIN_ROOM, onJoinRoom);
        mSocket.connect();
        mSocket.emit(S_GET_ROOMS, "");

        food = (Spinner) findViewById(R.id.txt_match_food);
        submit = (Button) findViewById(R.id.btn_match_done);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String food_kind = food.getSelectedItem().toString();
                SelectedList = new ArrayList<Room>();
                if (RoomArrList != null) {
                    for (int i=0;i<RoomArrList.size();i++) {
                        if (RoomArrList.get(i).food.equals(food_kind) || food_kind.equals("All")) {
                            SelectedList.add(RoomArrList.get(i));
                        }
                    }
                }
                if (SelectedList.size() == 0) {
                    Toast.makeText(mActivity, "Sorry! No rooms match your food.", Toast.LENGTH_SHORT).show();
                    dismiss();
                    return;
                }
                Ascending ascending = new Ascending();
                Collections.sort(SelectedList, ascending);
                Room room = SelectedList.get(0);
                JSONObject data = new JSONObject();
                try {
                    data.put("room", room.id);
                    data.put("nickname", UserInfo.getNickname());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mSocket.emit(S_JOIN_ROOM, data);
                dismiss();
            }
        });
    }

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
            Log.d("RoomArrList", RoomArrList.toString());
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
                    data.put("nickname", UserInfo.getNickname());
                    mSocket.emit(S_USER_JOINED, data);

                    Intent intent = new Intent(mActivity, RoomActivity.class);
                    intent.putExtra("room", resultData.getString("room"));
                    mActivity.startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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

    public class Ascending implements Comparator<Room> {
        public int compare(Room room, Room t1) {
            return (int) ((room.getDistance() - t1.getDistance())*10000);
        }
    }
}
