package com.example.q.cs496_week3;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by q on 2017-07-16.
 */

public class EditNickname extends AppCompatActivity {

    boolean doubleBack = false;

    EditText nicknameEditText;
    Button startButton;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        nicknameEditText = (EditText) findViewById(R.id.nicknameEditText);
        startButton = (Button) findViewById(R.id.startButton);

        startButton.setOnClickListener(clickListener);
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

            HttpCall.setMethodtext("userPUT");
            HttpCall.setUrltext("/api/user/"+deviceId+"/nickname");
            HttpCall.setNicknametext(nickname);
            HttpCall.getResponse();

            UserInfo.setNickname(nickname);

//            Intent intent = new Intent(EditNickname.this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
            finish();
        }
    };
}