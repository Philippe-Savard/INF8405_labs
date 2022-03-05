package com.example.tp2_inf8405;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        waitForTimer();

    }

    public void waitForTimer(){
        new CountDownTimer(1000, 1000) { // Create a time for 3 seconds for the pop up to automatically disappear
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }

            public void onFinish() {
                startActivity(new Intent(SplashActivity.this, MapsActivity.class)); // Start the maps activity page
            }
        }.start();
    }
}