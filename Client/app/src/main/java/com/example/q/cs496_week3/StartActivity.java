package com.example.q.cs496_week3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class StartActivity extends AppCompatActivity {

    boolean doubleBack = false;

    EditText nicknameEditText;
    Button startButton;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        UserInfo.setIdStr(deviceId);

        askForPermissions();

        HttpCall.setMethodtext("GET");
        HttpCall.setUrltext("/api/user");
        String all_users = HttpCall.getResponse();

        if (all_users.contains("\"id\":\""+deviceId+"\"")) {
            HttpCall.setMethodtext("GET");
            HttpCall.setUrltext("/api/user/"+deviceId);
            try {
                JSONObject curr_user = new JSONObject(HttpCall.getResponse());
                UserInfo.setNickname(curr_user.getString("nickname"));
                UserInfo.setLatv(Double.parseDouble(curr_user.getString("lat")));
                UserInfo.setLngv(Double.parseDouble(curr_user.getString("lng")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            nicknameEditText = (EditText) findViewById(R.id.nicknameEditText);
            startButton = (Button) findViewById(R.id.startButton);

            startButton.setOnClickListener(clickListener);
        }
    }

    @Override
    public void onBackPressed() {
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

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String nickname = nicknameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(nickname)) {
                Toast.makeText(getApplicationContext(), "Please enter a valid nickname", Toast.LENGTH_SHORT).show();
                return;
            }

            HttpCall.setMethodtext("POST");
            HttpCall.setUrltext("/api/adduser");
            HttpCall.setBodytext("{\"id\":\""+deviceId+"\"}");
            HttpCall.getResponse();

            HttpCall.setMethodtext("userPUT");
            HttpCall.setUrltext("/api/user/"+deviceId+"/nickname");
            HttpCall.setNicknametext(nickname);
            HttpCall.getResponse();

            UserInfo.setNickname(nickname);

            Intent intent = new Intent(StartActivity.this, StartLocation.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    };

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
