package com.example.q.cs496_week3;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

public class CustomDialog extends Dialog {

    public final String S_CREATE_ROOM = "create-room";
    Activity mActivity;

    public CustomDialog(Activity activity) {
        super(activity);
        mActivity = activity;
    }

    static Socket mSocket = MainActivity.mSocket;
    EditText title;
    Spinner food;
    ImageButton submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_layout);

        title = (EditText) findViewById(R.id.txt_modify_edit);
        food = (Spinner) findViewById(R.id.txt_modify_food);
        submit = (ImageButton) findViewById(R.id.btn_modify_done);

        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                String created_at = String.valueOf(c.get(Calendar.MONTH))+String.valueOf(c.get(Calendar.DATE))+
                        String.valueOf(c.get(Calendar.HOUR_OF_DAY))+String.valueOf(c.get(Calendar.MINUTE))+
                        String.valueOf(c.get(Calendar.SECOND));

                String roomTitle = title.getText().toString().trim();
                String roomFounder = UserInfo.getIdStr();
                String roomId = roomFounder + created_at;
                String roomFood = food.getSelectedItem().toString();
//                JSONArray memberArr = new JSONArray();
//                memberArr.put(roomFounder);
//                JSONObject obj = new JSONObject();
//                try {
//                    obj.put("id", roomId)
//                            .put("created_at", created_at)
//                            .put("founder", roomFounder)
//                            .put("title", roomTitle)
//                            .put("food", roomFood)
//                            .put("members", memberArr)
//                            .put("lat", UserInfo.getLatv())
//                            .put("lng", UserInfo.getLngv())
//                            .put("max_num", 8);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                String body = obj.toString();
//                HttpCall.setMethodtext("POST");
//                HttpCall.setUrltext("/api/addroom");
//                HttpCall.setBodytext(body);
//                HttpCall.getResponse();
                JSONObject data = new JSONObject();
                try {
                    data.put("id", roomId);
                    data.put("title", roomTitle);
                    data.put("founder", roomFounder);
                    data.put("food", roomFood);
                    data.put("limit", 8); // change later
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mSocket.emit(S_CREATE_ROOM, data);
                dismiss();

                Intent intent = new Intent(mActivity, RoomActivity.class);
                intent.putExtra("room", roomId);
                mActivity.startActivity(intent);
            }
        });
    }
}
