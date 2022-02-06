package com.inf8405.tp1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MotionEventCompat;
import java.lang.String;
import java.util.Arrays;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PuzzleActivity extends AppCompatActivity implements View.OnTouchListener{
    int[][] stackedGrid;
    int[][] gridArray = new int[7][7];
    int moveCount = 0;
    Stack<Object[]> stateStack = new Stack<>();
    ImageView _bloc1;
    ImageView _bloc2;
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
        _bloc1 = findViewById(R.id.bloc1);
        _bloc2 = findViewById((R.id.bloc2));
        _bloc1.setOnTouchListener(this);
        _bloc2.setOnTouchListener(this);
        gridFill();
        updateBlocGrid(_bloc1, true);
        updateBlocGrid(_bloc2, true);
    }

    public void hideToolBr() {

        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();

        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }
    public void gridFill() {
        for(int i = 0; i < 7; i++){
            for(int j = 0; j < 7; j++){
                if(i == 6 && j == 2){
                    gridArray[i][j] = 0;
                } else if(i == 6 || j == 6){
                    gridArray[i][j] = 1;
                } else{
                    gridArray[i][j] = 0;
                }
            }
        }
    }
    public void updateBlocGrid(View view, boolean isAdd){
        ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        boolean isBlocVertical = lParams.height > lParams.width;
        int blocSize = lParams.height > lParams.width ? Math.round(lParams.height / lParams.width) : Math.round(lParams.width / lParams.height);
        int horizontalPosition = Math.max(lParams.leftMargin / 165, 0);
        int verticalPosition = Math.max(lParams.topMargin / 165, 0);
        if(isBlocVertical){
            for(int i = 0; i < blocSize; i++){
                gridArray[horizontalPosition][verticalPosition + i] = isAdd ? 1 : 0;
            }
        } else {
            for(int i = 0; i < blocSize; i++){
                gridArray[horizontalPosition + i][verticalPosition] = isAdd ? 1 : 0;
            }
        }
    }


    public void checkGrid(){
        for(int i = 0; i < 7; i++){
            for(int j = 0; j < 7; j++){
                System.out.print(gridArray[i][j] + ",");
            }
            System.out.println();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent event) {
        ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        boolean isBlocVertical = lParams.height > lParams.width;
        int blocSize = lParams.height > lParams.width ? lParams.height / lParams.width : lParams.width / lParams.height;
        final int x = (int) event.getRawX();
        final int y = (int) event.getRawY();


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // The player touches the view on screen
                stackedGrid = Arrays.stream(gridArray).map(int[]::clone).toArray(int[][]::new);
                stateStack.push(new Object[]{stackedGrid, view, new ConstraintLayout.LayoutParams(lParams)});
                _dx = x - lParams.leftMargin;
                _dy = y - lParams.topMargin;
                updateBlocGrid(view, false);
                break;
            case MotionEvent.ACTION_MOVE:
                // The player start moving the view on screen
                if(isBlocVertical){
                    lParams.topMargin = Math.max(y - _dy, 0);
                } else {
                    lParams.leftMargin = Math.max(x - _dx, 0);
                }
                int horizontalFloor = Math.max(lParams.leftMargin / 165, 0);
                int verticalFloor = Math.max(lParams.topMargin / 165, 0);
                int horizontalCeil = (int) Math.max(Math.ceil(lParams.leftMargin / 165), 0);
                int verticalCeil = (int) Math.max(Math.ceil(lParams.topMargin / 165), 0);
                if(isBlocVertical){
                    if(gridArray[horizontalFloor][verticalFloor] == 1) {
                        lParams.topMargin = (verticalFloor + 1) * 165;
                        _dx = x - lParams.leftMargin;
                        _dy = y - lParams.topMargin;
                        break;
                    }
                    if(gridArray[horizontalCeil][verticalCeil + blocSize] == 1) {
                        lParams.topMargin = (verticalCeil) * 165;
                        _dx = x - lParams.leftMargin;
                        _dy = y - lParams.topMargin;
                        break;
                    }

                } else {
                    if(gridArray[horizontalFloor][verticalFloor] == 1) {
                        lParams.leftMargin = (horizontalFloor + 1) * 165;
                        _dx = x - lParams.leftMargin;
                        _dy = y - lParams.topMargin;
                        break;
                    }
                    if(gridArray[horizontalCeil + blocSize][verticalCeil] == 1) {
                        lParams.leftMargin = (horizontalCeil) * 165;
                        _dx = x - lParams.leftMargin;
                        _dy = y - lParams.topMargin;
                        break;
                    }
                }
                view.setLayoutParams(lParams);
                break;
            case MotionEvent.ACTION_UP:
                if(isBlocVertical){
                    if(lParams.topMargin % 165 > 82){
                        lParams.topMargin += 165 - lParams.topMargin % 165;
                    } else {
                        lParams.topMargin -= lParams.topMargin % 165;
                    }
                } else {
                    if(lParams.leftMargin % 165 > 82){
                        lParams.leftMargin += 165 - lParams.leftMargin % 165;
                    } else {
                        lParams.leftMargin -= lParams.leftMargin % 165;
                    }
                }
                view.setLayoutParams(lParams);
                updateBlocGrid(view, true);
                if(Arrays.deepEquals(gridArray, stackedGrid)){
                    stateStack.pop();
                } else {
                    moveCount++;
                    System.out.println(moveCount);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                updateBlocGrid(view, true);
            default:
                break;
        }
        _board.invalidate();
        return true;
    }

    public void onButtonUndo(View view) {
        if (stateStack.empty()) return;
        Object[] undo = stateStack.pop();
        gridArray = (int[][]) undo[0];
        ImageView bloc = findViewById(((View) undo[1]).getId());
        bloc.setLayoutParams((ConstraintLayout.LayoutParams) undo[2]);
        moveCount--;
        System.out.println(moveCount);
    }

    public void onButtonReset(View view) {
        if(stateStack.empty()) return;
        do{
            onButtonUndo(view);
        }while(!stateStack.empty());
    }

    public void onButtonPauseClick(View view) {
        finish();
    }
}