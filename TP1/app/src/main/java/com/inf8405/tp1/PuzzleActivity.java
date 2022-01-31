package com.inf8405.tp1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PuzzleActivity extends AppCompatActivity implements View.OnTouchListener{

    ImageView _star;
    ViewGroup _board;
    private int _dx;
    private int _dy;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_page);
        hideToolBr();

        _board = (ViewGroup) findViewById(R.id.layout_middle_gameboard);
        _star = findViewById(R.id.imageView);
        _star.setOnTouchListener(this);
    }

    public void hideToolBr() {

        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();

        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent event) {

        final int x = (int) event.getRawX();
        final int y = (int) event.getRawY();

        ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // The player touches the view on screen
                _dx = x - lParams.leftMargin;
                _dy = y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_MOVE:
                // The player start moving the view on screen
                lParams.leftMargin = x - _dx;
                lParams.topMargin = y - _dy;
                view.setLayoutParams(lParams);
                break;
            default:
                break;
        }
        _board.invalidate();
        return true;
    }

    public void onButtonPauseClick(View view) {
        startActivity(new Intent(PuzzleActivity.this, MainActivity.class));
    }
}