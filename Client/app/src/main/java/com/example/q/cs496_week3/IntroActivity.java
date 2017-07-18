package com.example.q.cs496_week3;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.hanks.htextview.fade.FadeTextView;

/**
 * Created by q on 2017-07-18.
 */

public class IntroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_intro);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        AnimateIntro();
        FadeTextView b = (FadeTextView) findViewById(R.id.introtext1);
        b.animateText(" Dine Together");
        FadeTextView a = (FadeTextView) findViewById(R.id.introtext2);
        a.animateText(" TOGEDINE");

        ImageView c = (ImageView) findViewById(R.id.introimg);
        YoYo.with(Techniques.FadeIn).duration(1500).playOn(c);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntroActivity.this, StartActivity.class);
                startActivity(intent);

                finish();
            }
        }, 4000);
    }

    private void AnimateIntro() {

    }
}
