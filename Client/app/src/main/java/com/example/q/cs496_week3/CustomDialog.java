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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class CustomDialog extends Dialog {

    public final String S_CREATE_ROOM = "create-room";
    Activity mActivity;

    public CustomDialog(Activity activity) {
        super(activity);
        mActivity = activity;
    }

    Socket mSocket = MainActivity.mSocket;
    EditText title;
    Spinner food;
    Spinner max_num;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        setContentView(R.layout.dialog_layout);

        title = (EditText) findViewById(R.id.txt_modify_edit);
        food = (Spinner) findViewById(R.id.txt_modify_food);
        submit = (Button) findViewById(R.id.btn_modify_done);
        max_num = (Spinner) findViewById(R.id.txt_modify_num);

        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                InputMethodManager imm;
                imm = (InputMethodManager) mActivity.getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
                Calendar c = Calendar.getInstance();
                String created_at = String.valueOf(c.get(Calendar.MONTH))+String.valueOf(c.get(Calendar.DATE))+
                        String.valueOf(c.get(Calendar.HOUR_OF_DAY))+String.valueOf(c.get(Calendar.MINUTE))+
                        String.valueOf(c.get(Calendar.SECOND));

                String roomTitle = title.getText().toString().trim();

                if (roomTitle.isEmpty()) {
                    return;
                }
                
                String roomFounder = UserInfo.getIdStr();
                String roomId = roomFounder + created_at;
                String roomFood = food.getSelectedItem().toString();
                int roomLimit = Integer.parseInt((String) max_num.getSelectedItem());

                JSONObject data = new JSONObject();
                try {
                    data.put("id", roomId);
                    data.put("created_at", created_at);
                    data.put("title", roomTitle);
                    data.put("founder", roomFounder);
                    data.put("food", roomFood);
                    data.put("limit", roomLimit);
                    data.put("lat", String.valueOf(UserInfo.getLatv()));
                    data.put("long", String.valueOf(UserInfo.getLngv()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mSocket.emit(S_CREATE_ROOM, data);
                dismiss();

                Intent intent = new Intent(mActivity, RoomActivity.class);
                intent.putExtra("room", roomId);
                intent.putExtra("title", roomTitle);
                mActivity.startActivity(intent);
            }
        });

        title.addTextChangedListener(new TextWatcher() {
            String previousString = "";
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                previousString= charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (title.getLineCount() >= 2)
                {
                    title.setText(previousString);
                    title.setSelection(title.length());
                }
            }
        });
    }
}
