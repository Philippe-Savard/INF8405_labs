package com.inf8405.tp1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Arrays;
import java.util.Stack;

public class PuzzleActivity extends AppCompatActivity implements View.OnTouchListener{
    int[][] stackedGrid;
    int[][] gridArray = new int[8][8];
    int moveCount = 0;
    int currentPuzzleNum = 0;
    int gridElementSize;
    Stack<Object[]> stateStack = new Stack<>();
    TextView _moveCount;
    TextView _puzzleNumber;
    ViewGroup _board1;
    ViewGroup _board2;
    ViewGroup _board3;
    ImageView[] puzzle1;
    ImageView[] puzzle2;
    ImageView[] puzzle3;
    Pair<ViewGroup, ImageView[]>[] puzzleList = new Pair[3];
    private int _dx;
    private int _dy;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_page);
        hideToolBr();
        _moveCount = findViewById(R.id.txt_moves_num);
        _puzzleNumber = findViewById(R.id.txt_puzzle_num);
        _board1 = findViewById(R.id.layout_middle_gameboard1);
        _board2 = findViewById(R.id.layout_middle_gameboard2);
        _board3 = findViewById(R.id.layout_middle_gameboard3);
        puzzle1 = generatePuzzleImages(puzzle1, _board1);
        puzzle2 = generatePuzzleImages(puzzle2, _board2);
        puzzle3 = generatePuzzleImages(puzzle3, _board3);
        puzzleList[0] = new Pair<>(_board1, puzzle1);
        puzzleList[1] = new Pair<>(_board2, puzzle2);
        puzzleList[2] = new Pair<>(_board3, puzzle3);
        defineGridElementSize(puzzle1[0]);
        activatePuzzle(puzzleList[0].second, puzzleList[0].first);
    }

    @SuppressLint("ClickableViewAccessibility")
    public ImageView[] generatePuzzleImages(ImageView[] puzzle, ViewGroup board){
        puzzle = new ImageView[board.getChildCount()];
        for(int i = 0; i < board.getChildCount(); i++){
            System.out.println(board.getChildAt(i));
            puzzle[i] = (ImageView) board.getChildAt(i);
        }
        for (ImageView bloc : puzzle) {
            bloc.setOnTouchListener(this);
        }
        return puzzle;
    }

    public void activatePuzzle(ImageView[] puzzle, ViewGroup board) {
        onButtonReset(board);
        gridFill();
        for (Pair<ViewGroup, ImageView[]> groupPuzzle : puzzleList) {
            groupPuzzle.first.setVisibility(View.INVISIBLE);
        }
        board.setVisibility(View.VISIBLE);
        _puzzleNumber.setText(Integer.toString(currentPuzzleNum + 1));
        for (ImageView bloc : puzzle) {
            updateBlocGrid(bloc, true);
        }
    }

    public void hideToolBr() {

        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();

        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    public void defineGridElementSize(ImageView bloc){
        ViewGroup.LayoutParams lParams = bloc.getLayoutParams();
        gridElementSize = Math.min(lParams.height, lParams.width);
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

    public void victory(View view){
        this.onPuzzlePassed(view);
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
                if(gridArray[6][2] == 1){
                    victory(view);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                updateBlocGrid(view, true);
            default:
                break;
        }
        return true;
    }

    public void onPreviousPuzzle(View view){
        currentPuzzleNum = Math.max(currentPuzzleNum - 1, 0);
        activatePuzzle(puzzleList[currentPuzzleNum].second, puzzleList[currentPuzzleNum].first);
    }

    public void onNextPuzzle(View view){
        currentPuzzleNum = Math.min(currentPuzzleNum + 1, 2);
        activatePuzzle(puzzleList[currentPuzzleNum].second, puzzleList[currentPuzzleNum].first);
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

    // Create a pop up window when puzzle is solved
    public void onPuzzlePassed(View view) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.winnerwindow, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height);

        // show the popup window
        popupWindow.setAnimationStyle(R.style.Animation);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Play the media sound
        MediaPlayer song = MediaPlayer.create(popupView.getContext(), R.raw.ring);
        song.start();

        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }
            public void onFinish() {
                popupWindow.dismiss();
                currentPuzzleNum = Math.min(currentPuzzleNum + 1, 2);
                activatePuzzle(puzzleList[currentPuzzleNum].second, puzzleList[currentPuzzleNum].first);
            }

        }.start();
    }


    public void onButtonPauseClick(View view) {
        finish();
    }
}