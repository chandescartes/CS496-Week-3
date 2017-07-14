package com.example.q.cs496_week3;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    boolean doubleBack = false;

    EditText nicknameEditText;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

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
            Log.d("invalid", "asdf");
            String nickname = nicknameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(nickname)) {
                Toast.makeText(getApplicationContext(), "Please enter a valid nickname", Toast.LENGTH_SHORT).show();
                Log.d("invalid", "nickname");
                return;
            }

            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            intent.putExtra("nickname", nickname);
            startActivity(intent);
        }
    };
}
