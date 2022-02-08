package com.inf8405.tp1;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    int[][] gridArray = new int[8][8]; // Grid that is used to keep track of block placement. Even if grid is 6x6 some padding is necessary (for the exit bloc)
    int[][] previousGrid;
    int moveCount = 0;
    int currentPuzzleNum = 0;
    int gridElementSize;
    Stack<Object[]> stateStack = new Stack<>();
    TextView _moveCount;
    TextView _puzzleNumber;
    Pair<ViewGroup, ImageView[]>[] puzzleList = new Pair[3];
    private int _dx;
    private int _dy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_page);
        hideToolBr();
        _moveCount = findViewById(R.id.txt_moves_num); // Obtain reference to the xml object containing the amount of moves played
        _puzzleNumber = findViewById(R.id.txt_puzzle_num); // Obtain reference to the xml object containing the current puzzle level
        ViewGroup board1 = findViewById(R.id.layout_middle_gameboard1);
        ViewGroup board2 = findViewById(R.id.layout_middle_gameboard2);
        ViewGroup board3 = findViewById(R.id.layout_middle_gameboard3);
        puzzleList[0] = new Pair<>(board1, generatePuzzleImages(board1)); // Add pair containing the board and reference to the blocks/children of that board
        puzzleList[1] = new Pair<>(board2, generatePuzzleImages(board2));
        puzzleList[2] = new Pair<>(board3, generatePuzzleImages(board3));
        defineGridElementSize(puzzleList[0].second[0]);
        activatePuzzle(puzzleList[0].second, puzzleList[0].first);
    }


    ////////////////////////////////////////////////////////
    //   Function that obtains the children (blocks)      //
    //   of a board and return it as a puzzle. Also adds  //
    //   listeners to the blocks.                         //
    ////////////////////////////////////////////////////////
    public ImageView[] generatePuzzleImages(ViewGroup board){
        ImageView[] puzzle = new ImageView[board.getChildCount()];
        for(int i = 0; i < board.getChildCount(); i++){
            puzzle[i] = (ImageView) board.getChildAt(i);
        }
        for (ImageView bloc : puzzle) {
            bloc.setOnTouchListener(this);
        }
        return puzzle;
    }

    /////////////////////////////////////////////////////////
    //   Function that initializes the playing board       //
    //   of the puzzle. Gets called when switching levels. //
    /////////////////////////////////////////////////////////
    public void activatePuzzle(ImageView[] puzzle, ViewGroup board) {
        onButtonReset(board); // Clears the board of all blocks
        gridFill(); // Sets the initial state of the grid
        for (Pair<ViewGroup, ImageView[]> groupPuzzle : puzzleList) {
            groupPuzzle.first.setVisibility(View.INVISIBLE); // Set all boards to invisible
        }
        board.setVisibility(View.VISIBLE); // Show the current puzzle board
        _puzzleNumber.setText(Integer.toString(currentPuzzleNum + 1));
        for (ImageView bloc : puzzle) {
            updateBlocGrid(bloc, true);
        }
    }

    // Hide the view of top tool bar with TP1 name on it
    public void hideToolBr() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    /////////////////////////////////////////////////////////
    //   Gets the grid size based on the specific phone    //
    //   resolution used.                                  //
    /////////////////////////////////////////////////////////
    public void defineGridElementSize(ImageView bloc){
        ViewGroup.LayoutParams lParams = bloc.getLayoutParams();
        gridElementSize = Math.min(lParams.height, lParams.width);
    }

    /////////////////////////////////////////////////////////
    //   Initializes a clean grid ready for puzzle         //
    //   population.                                       //
    /////////////////////////////////////////////////////////
    public void gridFill() {
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if(i == 6 && j == 2){ //  Sets the exit space to 0 in order for main bloc to exit
                    gridArray[i][j] = 0;
                } else if(i >= 6 || j >= 6){ // Set the padding to 1 in order for blocks not to pass
                    gridArray[i][j] = 1;
                } else{
                    gridArray[i][j] = 0;
                }
            }
        }
    }

    //////////////////////////////////////////////////////////
    //   Function that is called when creating or moving    //
    //   blocks. Updates the bloc position in the gridArray //
    //   by setting value to 1.                             //
    //////////////////////////////////////////////////////////
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

    //////////////////////////////////////////////////////////
    //   Function that is called everytime a block is       //
    //   touched, moved, let go, etc. Updates and handles   //
    //   the view, the grid and all other logic elements.   //
    //////////////////////////////////////////////////////////
    public boolean onTouch(View view, MotionEvent event) {
        ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        boolean isBlocVertical = lParams.height > lParams.width;
        int blocSize = lParams.height > lParams.width ? lParams.height / lParams.width : lParams.width / lParams.height;
        final int x = (int) event.getRawX();
        final int y = (int) event.getRawY();

        switch (event.getAction()) { // Switch depending on action performed
            case MotionEvent.ACTION_DOWN:
                previousGrid = Arrays.stream(gridArray).map(int[]::clone).toArray(int[][]::new); // Clones the grid
                stateStack.push(new Object[]{previousGrid, view, new ConstraintLayout.LayoutParams(lParams)});
                _dx = x - lParams.leftMargin; // Get position in the puzzle view
                _dy = y - lParams.topMargin;
                updateBlocGrid(view, false);
                break;
            case MotionEvent.ACTION_MOVE:
                if(isBlocVertical){ // Updates the position of the block
                    lParams.topMargin = Math.max(y - _dy, 0);
                } else {
                    lParams.leftMargin = Math.max(x - _dx, 0);
                }
                int horizontalFloor = Math.max(lParams.leftMargin / gridElementSize, 0);
                int verticalFloor = Math.max(lParams.topMargin / gridElementSize, 0);
                int horizontalCeil = (int) Math.max(Math.ceil(lParams.leftMargin / gridElementSize), 0);
                int verticalCeil = (int) Math.max(Math.ceil(lParams.topMargin / gridElementSize), 0);
                if(isBlocVertical){
                    if(gridArray[horizontalFloor][verticalFloor] == 1) { // Check if base of block is trying to go over another block, update lparams accordingly
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
                view.setLayoutParams(lParams); // Update the view
                break;
            case MotionEvent.ACTION_UP: // Checks where to drop the block depending on closest grid position
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
                view.setLayoutParams(lParams); // Update the view
                updateBlocGrid(view, true);
                if(Arrays.deepEquals(gridArray, previousGrid)){ // Check if block has gone back to original position. In this case, take out the move from the stack
                    stateStack.pop();
                } else {
                    moveCount++;
                    _moveCount.setText(Integer.toString(moveCount)); // Update move tracker on view
                }
                if(gridArray[6][2] == 1){ // Checks if player has solved the puzzle
                    onPuzzlePassed(view); // Calls to open the victory pop up
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                updateBlocGrid(view, true);
                break;
            default:
                break;
        }
        return true;
    }

    /////////////////////////////////////////////////////////
    //   Activates correct puzzle when previous button     //
    //   is called.                                        //
    /////////////////////////////////////////////////////////
    public void onPreviousPuzzle(View view){
        currentPuzzleNum = Math.max(currentPuzzleNum - 1, 0); // Checks if we are trying previous on puzzle 1
        activatePuzzle(puzzleList[currentPuzzleNum].second, puzzleList[currentPuzzleNum].first);
    }

    /////////////////////////////////////////////////////////
    //   Activates correct puzzle when next button         //
    //   is called.                                        //
    /////////////////////////////////////////////////////////
    public void onNextPuzzle(View view){
        currentPuzzleNum = Math.min(currentPuzzleNum + 1, 2); // Checks if we are trying next on puzzle 3
        activatePuzzle(puzzleList[currentPuzzleNum].second, puzzleList[currentPuzzleNum].first);
    }

    /////////////////////////////////////////////////////////
    //   Function that allows going to a previous state    //
    //   when the undo button is called. Updates the view  //
    //   as well.                                          //
    /////////////////////////////////////////////////////////
    public void onButtonUndo(View view) {
        if (stateStack.empty()) return; // Checks if no previous action has been done
        Object[] undo = stateStack.pop();
        gridArray = (int[][]) undo[0];
        ImageView bloc = findViewById(((View) undo[1]).getId());
        bloc.setLayoutParams((ConstraintLayout.LayoutParams) undo[2]); // Resets blocs to previous location
        moveCount--;
        _moveCount.setText(Integer.toString(moveCount));  // Takes out 1 from the view count on the puzzle page
    }

    ///////////////////////////////////////////////
    //   Reset the puzzle to the initial view.   //
    ///////////////////////////////////////////////
    public void onButtonReset(View view) {
        if(stateStack.empty()) return;
        do{
            onButtonUndo(view);
        }while(!stateStack.empty());
    }

    ////////////////////////////////////////////////////////////
    //   Creates the pop up window when the puzzle if solved. //
    ////////////////////////////////////////////////////////////
    public void onPuzzlePassed(View view) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.winnerwindow, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height);

        // show the popup window
        popupWindow.setAnimationStyle(R.style.Animation); // Set the animation to fade out when the pop up is closed
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Play the media sound
        MediaPlayer song = MediaPlayer.create(popupView.getContext(), R.raw.ring);
        song.start();

        new CountDownTimer(3000, 1000) { // Create a time for 3 seconds for the pop up to automatically disapear
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }
            public void onFinish() {
                popupWindow.dismiss();
                currentPuzzleNum = Math.min(currentPuzzleNum + 1, 2); // Starts the next puzzle after the delay
                activatePuzzle(puzzleList[currentPuzzleNum].second, puzzleList[currentPuzzleNum].first);
            }

        }.start();
    }

    public void onButtonPauseClick(View view) {
        finish();
    }
}