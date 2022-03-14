package com.example.tp2_inf8405;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        waitForTimer();
        hideToolBr();
    }

    public void waitForTimer(){
        new CountDownTimer(3000, 1000) { // Create a time for 3 seconds for the pop up to automatically disappear
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }

            public void onFinish() {
                startActivity(new Intent(SplashActivity.this, MapsActivity.class)); // Start the maps activity page
            }
        }.start();
    }
    // Hide the view of top tool bar with TP1 name on it
    public void hideToolBr() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }
}