package com.inf8405.tp1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MotionEventCompat;
import java.lang.String;
import java.util.Arrays;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PuzzleActivity extends AppCompatActivity implements View.OnTouchListener{
    int[][] stackedGrid;
    int[][] gridArray = new int[8][8];
    int moveCount = 0;
    int gridElementSize;
    Stack<Object[]> stateStack = new Stack<>();
    ImageView _bloc1;
    ImageView _bloc2;
    ImageView _bloc3;
    ImageView _bloc4;
    ImageView _bloc5;
    ImageView _bloc6;
    ImageView _bloc7;
    ImageView _bloc8;
    TextView _moveCount;
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
        _bloc3 = findViewById(R.id.bloc3);
        _bloc4 = findViewById((R.id.bloc4));
        _bloc5 = findViewById(R.id.bloc5);
        _bloc6 = findViewById((R.id.bloc6));
        _bloc7 = findViewById(R.id.bloc7);
        _bloc8 = findViewById((R.id.bloc8));
        _moveCount = findViewById(R.id.txt_moves_num);
        _bloc1.setOnTouchListener(this);
        _bloc2.setOnTouchListener(this);
        _bloc3.setOnTouchListener(this);
        _bloc4.setOnTouchListener(this);
        _bloc5.setOnTouchListener(this);
        _bloc6.setOnTouchListener(this);
        _bloc7.setOnTouchListener(this);
        _bloc8.setOnTouchListener(this);


        defineGridElementSize();
        gridFill();
        updateBlocGrid(_bloc1, true);
        updateBlocGrid(_bloc2, true);
        updateBlocGrid(_bloc3, true);
        updateBlocGrid(_bloc4, true);
        updateBlocGrid(_bloc5, true);
        updateBlocGrid(_bloc6, true);
        updateBlocGrid(_bloc7, true);
        updateBlocGrid(_bloc8, true);
    }

    public void hideToolBr() {

        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();

        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    public void defineGridElementSize(){
        ViewGroup.LayoutParams lParams = (ConstraintLayout.LayoutParams) _bloc1.getLayoutParams();
        gridElementSize = lParams.height > lParams.width ? lParams.width : lParams.height;
    }

    public void gridFill() {
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if(i == 6 && j == 2){
                    gridArray[i][j] = 0;
                } else if(i >= 6 || j >= 6){
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
        int horizontalPosition = Math.max(lParams.leftMargin / gridElementSize, 0);
        int verticalPosition = Math.max(lParams.topMargin / gridElementSize, 0);
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
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                System.out.print(gridArray[i][j] + ",");
            }
            System.out.println();
        }
    }

    public void victory(){
        _moveCount.setText(Integer.toString(99));
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
                int horizontalFloor = Math.max(lParams.leftMargin / gridElementSize, 0);
                int verticalFloor = Math.max(lParams.topMargin / gridElementSize, 0);
                int horizontalCeil = (int) Math.max(Math.ceil(lParams.leftMargin / gridElementSize), 0);
                int verticalCeil = (int) Math.max(Math.ceil(lParams.topMargin / gridElementSize), 0);
                if(isBlocVertical){
                    if(gridArray[horizontalFloor][verticalFloor] == 1) {
                        lParams.topMargin = (verticalFloor + 1) * gridElementSize;
                        _dx = x - lParams.leftMargin;
                        _dy = y - lParams.topMargin;
                        break;
                    }
                    if(gridArray[horizontalCeil][verticalCeil + blocSize] == 1) {
                        lParams.topMargin = (verticalCeil) * gridElementSize;
                        _dx = x - lParams.leftMargin;
                        _dy = y - lParams.topMargin;
                        break;
                    }

                } else {
                    if(gridArray[horizontalFloor][verticalFloor] == 1) {
                        lParams.leftMargin = (horizontalFloor + 1) * gridElementSize;
                        _dx = x - lParams.leftMargin;
                        _dy = y - lParams.topMargin;
                        break;
                    }
                    if(gridArray[horizontalCeil + blocSize][verticalCeil] == 1) {
                        lParams.leftMargin = (horizontalCeil) * gridElementSize;
                        _dx = x - lParams.leftMargin;
                        _dy = y - lParams.topMargin;
                        break;
                    }
                }
                view.setLayoutParams(lParams);
                break;
            case MotionEvent.ACTION_UP:
                if(isBlocVertical){
                    if(lParams.topMargin % gridElementSize > gridElementSize / 2){
                        lParams.topMargin += gridElementSize - lParams.topMargin % gridElementSize;
                    } else {
                        lParams.topMargin -= lParams.topMargin % gridElementSize;
                    }
                } else {
                    if(lParams.leftMargin % gridElementSize > gridElementSize / 2){
                        lParams.leftMargin += gridElementSize - lParams.leftMargin % gridElementSize;
                    } else {
                        lParams.leftMargin -= lParams.leftMargin % gridElementSize;
                    }
                }
                view.setLayoutParams(lParams);
                updateBlocGrid(view, true);
                if(Arrays.deepEquals(gridArray, stackedGrid)){
                    stateStack.pop();
                } else {
                    moveCount++;
                    _moveCount.setText(Integer.toString(moveCount));
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                updateBlocGrid(view, true);
            default:
                break;
        }
        if(gridArray[6][2] == 1){
            victory();
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
        _moveCount.setText(Integer.toString(moveCount));
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