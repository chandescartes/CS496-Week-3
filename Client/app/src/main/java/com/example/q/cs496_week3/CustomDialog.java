package com.example.q.cs496_week3;

import android.app.Dialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kjy on 2016-04-11.
 */
public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        super(context);
    }

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
                Calendar c = Calendar.getInstance();
                String created_at = String.valueOf(c.get(Calendar.MONTH))+String.valueOf(c.get(Calendar.DATE))+
                        String.valueOf(c.get(Calendar.HOUR_OF_DAY))+String.valueOf(c.get(Calendar.MINUTE))+
                        String.valueOf(c.get(Calendar.SECOND));

                String roomTitle = title.getText().toString().trim();
                if (TextUtils.isEmpty(roomTitle)) {
                    Toast.makeText(MainActivity.context, "Please enter a valid title", Toast.LENGTH_SHORT).show();
                    return;
                }
                String roomFounder = UserInfo.getIdStr();
                String roomId = roomFounder+created_at;
                String roomFood = food.getSelectedItem().toString();
                String roomNum = max_num.getSelectedItem().toString();
                JSONArray memberArr = new JSONArray();
                memberArr.put(roomFounder);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("id", roomId)
                            .put("created_at", created_at)
                            .put("founder", roomFounder)
                            .put("title", roomTitle)
                            .put("food", roomFood)
                            .put("members", memberArr)
                            .put("lat", UserInfo.getLatv())
                            .put("lng", UserInfo.getLngv())
                            .put("max_num", Integer.parseInt(roomNum));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String body = obj.toString();
                HttpCall.setMethodtext("POST");
                HttpCall.setUrltext("/api/addroom");
                HttpCall.setBodytext(body);
                HttpCall.getResponse();

                dismiss();
            }
        });
    }
}
