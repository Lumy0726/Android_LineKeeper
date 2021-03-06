package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import kr.co.lumylumy.linekeeper.log.LogSystem;
import kr.co.lumylumy.linekeeper.timer.TimeCheck;
import kr.co.lumylumy.linekeeper.timer.TimerAble;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.TouchInfo;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView.TouchEvent;

import static kr.co.lumylumy.linekeeper.log.LogSystem.androidLog;

/**
 * Created by LMJ on 2017-08-12.
 */

class GameBoard implements TimerAble, TouchEvent, TileUpdateReceiver {
    //
    static final int BOARDW = 6, BOARDH= 6;
    //size.
    int xPos, yPos, outputWidth, outputHeight, tileSize;
    int tileOutputHeight, controlPanelHeight;
    //tiles
    Tile[][] tile_S = new Tile[BOARDH][BOARDW];
    //Gameboard Bitmap.
    Bitmap b_Board;
    Canvas c_Board;
    Rect r_Output;
    //outputCanvas's rect.
    Rect r_CanvasOutput;
    //rect - for Board Cycle.
    static final int RECT_CYCLETOP1 = 0, RECT_CYCLETOP2 = 1, RECT_CYCLEBOTTOM1 = 2, RECT_CYCLEBOTTOM2 = 3;
    Rect[] rect_Cycle = new Rect[4];
    //Control Bitmap, rect.
    Bitmap b_Control;
    static final int CONTROL_NUM = 7, CONTROL_ROTATEL = 0, CONTROL_ROTATER = 1, CONTROL_R = 2, CONTROL_U = 3, CONTROL_L = 4, CONTROL_D = 5, CONTROL_MOVECURSOR = 6;
    Rect[] rect_Control = new Rect[CONTROL_NUM];
    boolean moveCursorState = false;
    int moveCursorButtonId;
    //other Bitmap.
    Bitmap b_Cursor;
    //touchInput.
    ArrayList<TouchInfo> touchInput = new ArrayList<>();
    //static final int TOUCHNUM_MAX = 3;
    //cursor.
    Coord cursorTilePos;
    //
    Coord tileInternalPosEx;
    Coord tileExternalPosEx;
    //sweepLine.
    SweepLine sweepLine;
    boolean needLineUpdate;
    //score etc.
    static final int MAXLEVEL_DEFAULT = 100;
    int gameScore, gameLevel;
    int clearLineNum, clearTileNum;
    int levelUpLineNum, levelUpTileNum;
    //
    boolean isDie;

    //constructor.
    GameBoard(int width){ this(0, 0, width); }
    GameBoard(int width, int height){
        tileSize = width / BOARDW;
        setSize();
        setPosition(0, height - this.outputHeight);
        init();
    }
    GameBoard(int xPos, int yPos, int width){
        tileSize = width / BOARDW;
        setSize();
        setPosition(xPos, yPos);
        init();
    }
    void setPosition(int xPos, int yPos){
        this.xPos = xPos; this.yPos = yPos;
        r_CanvasOutput = Tools.rectWH(xPos, yPos, outputWidth, outputHeight);
    }
    void setSize(){
        tileOutputHeight = tileSize * (BOARDH + 1);
        controlPanelHeight = tileSize * 2;
        outputWidth = tileSize * BOARDW;
        outputHeight = tileOutputHeight + controlPanelHeight;
        r_Output = new Rect(0, 0, outputWidth, outputHeight);
    }
    void setTileCyclePanel(){
        rect_Cycle[RECT_CYCLETOP1] = new Rect(0, 0, outputWidth, tileSize);
        (rect_Cycle[RECT_CYCLETOP2] = new Rect(rect_Cycle[RECT_CYCLETOP1])).offsetTo(0, tileSize);
        (rect_Cycle[RECT_CYCLEBOTTOM1] = new Rect(rect_Cycle[RECT_CYCLETOP1])).offsetTo(0, tileOutputHeight - tileSize);
        (rect_Cycle[RECT_CYCLEBOTTOM2] = new Rect(rect_Cycle[RECT_CYCLETOP1])).offsetTo(0, tileOutputHeight);
    }
    void setControlPanel(){
        Rect rect = new Rect(0, 0, tileSize, tileSize);
        (rect_Control[CONTROL_ROTATEL] = new Rect(rect)).offsetTo(outputWidth - tileSize * 3, 0);
        (rect_Control[CONTROL_ROTATER] = new Rect(rect)).offsetTo(outputWidth - tileSize, 0);
        (rect_Control[CONTROL_U] = new Rect(rect)).offsetTo(outputWidth - tileSize * 2, 0);
        (rect_Control[CONTROL_L] = new Rect(rect)).offsetTo(outputWidth - tileSize * 3, tileSize);
        (rect_Control[CONTROL_D] = new Rect(rect)).offsetTo(outputWidth - tileSize * 2, tileSize);
        (rect_Control[CONTROL_R] = new Rect(rect)).offsetTo(outputWidth - tileSize, tileSize);
        (rect_Control[CONTROL_MOVECURSOR] = new Rect(rect)).offsetTo(outputWidth - tileSize * 5, tileSize);
        Canvas canvas = new Canvas();
        Matrix matrix = new Matrix();
        b_Control = Bitmap.createBitmap(outputWidth, controlPanelHeight, Bitmap.Config.ARGB_8888);
        Bitmap bitmap_Arrow = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Bitmap bitmap_Rotate = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Bitmap frameBitmap = Tools.frameBitmap(tileSize, tileSize, MyColor.hsvColor(30, 100, 100));
        canvas.setBitmap(bitmap_Arrow);
        Tools.resetBitmap(canvas, MyColor.hsvColor(0, 0, 80));
        canvas.drawBitmap(frameBitmap, 0, 0, null);
        canvas.drawBitmap(Tools.arrowBitmap(tileSize, tileSize, MyColor.RED), 0, 0, null);
        canvas.setBitmap(bitmap_Rotate);
        Tools.resetBitmap(canvas, MyColor.hsvColor(0, 0, 80));
        canvas.drawBitmap(frameBitmap, 0, 0, null);
        canvas.drawBitmap(Tools.rotateArrowBitmap(tileSize, MyColor.RED), 0, 0, null);
        canvas.setBitmap(b_Control);
        Tools.resetBitmap(canvas, MyColor.WHITE);
        canvas.drawBitmap(bitmap_Arrow, null, rect_Control[CONTROL_U], null);
        matrix.setRotate(-90);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Arrow, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_L], null);
        matrix.setRotate(-180);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Arrow, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_D], null);
        matrix.setRotate(90);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Arrow, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_R], null);
        canvas.drawBitmap(bitmap_Rotate, null, rect_Control[CONTROL_ROTATEL], null);
        matrix.setScale(-1, 1);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Rotate, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_ROTATER], null);
        //
        Bitmap b_MoveCursor = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        int colorRes = 100;
        int[] colors = new int[colorRes];
        float[] positions = new float[colorRes];
        for (int loop1 = 0; loop1 < colorRes; loop1++){
            colors[loop1] = MyColor.hsvColor(360 * loop1 / colorRes, 50, 100);
            positions[loop1] = loop1 / (float)colorRes;
        }
        paint.setShader(new SweepGradient(tileSize / 2, tileSize / 2, colors, positions));
        Tools.newCanvas(b_MoveCursor).drawCircle(tileSize / 2, tileSize / 2, tileSize / 2, paint);
        canvas.drawBitmap(b_MoveCursor, null, rect_Control[CONTROL_MOVECURSOR], null);
    }
    void init(){
        //Tile initialize.
        Tile.tileInitialze(tileSize);
        //test.
        Tile.viewTileProbability(0);
        Tile.viewTileProbability(MAXLEVEL_DEFAULT);
        //
        setTileCyclePanel();
        setControlPanel();
        //Cursor Bitmap.
        b_Cursor = Tools.frameBitmap(tileSize, tileSize, tileSize / 10, MyColor.RED);
        //BitmapBoard.
        b_Board = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
        c_Board = Tools.newCanvas(b_Board);
        //tilePosExample
        tileExternalPosEx = new Coord(0, 0, Coord.MODE_CONSTRAINT, Coord.MODE_CYCLE);
        tileExternalPosEx.setBorder(0, BOARDW, 0, BOARDH);
        tileInternalPosEx = new Coord(0, 0, Coord.MODE_CONSTRAINT, Coord.MODE_CYCLE);
        tileInternalPosEx.setBorder(0, outputWidth, tileSize, tileSize * (BOARDH + 1));
        //cursorTilePos Coord setting.
        cursorTilePos = new Coord();
        cursorTilePos.setMode(Coord.MODE_CONSTRAINT, Coord.MODE_CONSTRAINT);
        cursorTilePos.setBorder(0, BOARDW, 0, BOARDH + 1);
        reset();
    }

    //reset.
    void reset(){
        //score.
        scoreLevelReset();
        //
        isDie = false;
        touchInput.clear();
        //Tile allocate.
        //Tile.tileAllocSeedReset((long)0);
        int startY = 2;
        for (int x = 0; x < BOARDW; x++){
            for (int y = 0; y < startY; y++){
                if (x == 0){
                    tile_S[y][0] = new Tile_STRAIGHT(
                            this,
                            new Direction(Direction.U),
                            new Coord(tileInternalPosEx).setPos(tileSize / 2, (y + 1) * tileSize + tileSize / 2)
                    );
                }
                else {
                    tile_S[y][x] = new TileA(
                            this,
                            new Direction(Direction.U),
                            new Coord(tileInternalPosEx).setPos(x * tileSize + tileSize / 2, (y + 1) * tileSize + tileSize / 2)
                    );
                }
                tile_S[y][x].moveAble = false;
            }
        }
        tile_S[0][0].lineFlow(new Direction(Direction.U));//startTile.
        for (int x = 0; x < BOARDW; x++){
            for (int y = 2; y < BOARDH; y++){
                tileAllocate(x, y);
            }
        }
        //sweepLine.
        sweepLine = new SweepLine(this);
        needLineUpdate = true;
        cursorTilePos.forceOut();
    }
    void scoreLevelReset(){
        gameScore = 0;
        gameLevel = 1;
        clearLineNum = clearTileNum = 0;
        levelUpLineNum = levelUpTileNum = 0;
    }
    void tileAllocate(int x, int y){
        if (0 <= x && x < BOARDW && 0 <= y && y < BOARDH){
            Coord coord = new Coord(tileInternalPosEx);
            coord.setPos(x * tileSize + tileSize / 2, (y + 1) * tileSize + tileSize / 2);
            Direction direction = null;
            switch((int)(Math.random() * 4)){
                case 0: direction = new Direction(Direction.R); break;
                case 1: direction = new Direction(Direction.U); break;
                case 2: direction = new Direction(Direction.L); break;
                case 3: direction = new Direction(Direction.D); break;
            }
            tile_S[y][x] = Tile.newTile(this, direction, coord, gameLevel);
        }
    }
    void startTileAllocate(int x, int y){
        if (0 <= x && x < BOARDW && 0 <= y && y < BOARDH){
            Coord coord = new Coord(tileInternalPosEx);
            coord.setPos(x * tileSize + tileSize / 2, (y + 1) * tileSize + tileSize / 2);
            tile_S[y][x] = new Tile_STRAIGHT(this, new Direction(Direction.U), coord);
            tile_S[y][x].lineFlow(new Direction(Direction.U));
        }
    }

    //
    void changeLevel(int level){
        if (level > 0){
            gameLevel = level;
            sweepLine.setSpeed(level);
        }
    }
    int levelMultiplier(){ return gameLevel / 10 + 1; }

    //
    int getTileOutputHeight(){ return tileOutputHeight; }

    //canvas draw.
    //TimeCheck time_I = new TimeCheck();
    //TimeCheck time_D = new TimeCheck();
    //TimeCheck time_C = new TimeCheck();
    //TimeCheck time_M = new TimeCheck();
    void draw(Canvas canvas){
        //time_I.reset();
        Paint paint = Tools.colorPaint(0, true);
        Tools.resetBitmap(c_Board ,MyColor.WHITE);
        c_Board.drawRect(rect_Cycle[RECT_CYCLETOP1], paint);
        c_Board.drawRect(rect_Cycle[RECT_CYCLEBOTTOM2], paint);
        //androidLog(String.format("GameBoard-draw-initialize: %5.2f", time_I.getTimeAv()));
        //time_D.reset();
        for (int x = 0; x < BOARDW; x++){
            for (int y = 0; y < BOARDH; y++){
                if (!tile_S[y][x].isProcessing) tile_S[y][x].draw(c_Board);
            }
        }
        for (int x = 0; x < BOARDW; x++){
            for (int y = 0; y < BOARDH; y++){
                if (tile_S[y][x].isProcessing) tile_S[y][x].draw(c_Board);
            }
        }
        for (int x = 0; x < BOARDW; x++){
            for (int y = 0; y < BOARDH; y++){
                if (!sweepLine.tileConnectSuccess[y][x]) tile_S[y][x].drawDanger(c_Board);
            }
        }
        //androidLog(String.format("GameBoard-draw-tileDraw: %5.2f", time_D.getTimeAv()));
        sweepLine.draw(c_Board, tileSize);
        if (!cursorTilePos.isOut()){ c_Board.drawBitmap(b_Cursor, cursorTilePos.getX() * tileSize, cursorTilePos.getY() * tileSize, null); }
        //time_C.reset();
        c_Board.drawBitmap(b_Board, rect_Cycle[RECT_CYCLETOP1], rect_Cycle[RECT_CYCLEBOTTOM1], null);
        c_Board.drawBitmap(b_Board, rect_Cycle[RECT_CYCLEBOTTOM2], rect_Cycle[RECT_CYCLETOP2], null);
        c_Board.drawBitmap(b_Board, rect_Cycle[RECT_CYCLEBOTTOM1], rect_Cycle[RECT_CYCLETOP1], null);
        c_Board.drawRect(rect_Cycle[RECT_CYCLETOP1], Tools.colorPaint(MyColor.aColor(0x7f, MyColor.hsvColor(0, 80, 50))));
        //androidLog(String.format("GameBoard-draw-tileCycle: %5.2f", time_C.getTimeAv()));
        c_Board.drawBitmap(b_Control, 0, tileOutputHeight, null);
        //time_M.reset();
        canvas.drawBitmap(b_Board, r_Output, r_CanvasOutput, null);
        //androidLog(String.format("GameBoard-draw-main: %5.2f", time_M.getTimeAv()));
    }
    //
    int getTileYToCursor(Coord input){ return Tools.remainder(input.getY() - 1, BOARDH); }
    Tile getTileToCursor(Coord input){ return tile_S[getTileYToCursor(input)][input.getX()]; }
    void tileSwapToCursor(Coord input1, Coord input2){
        Tile temp = getTileToCursor(input1);
        tile_S[getTileYToCursor(input1)][input1.getX()] = getTileToCursor(input2);
        tile_S[getTileYToCursor(input2)][input2.getX()] = temp;
    }
    //
    void sl_ProcessResult(){
        boolean isLevelUp = false;
        int lineNum = sweepLine.getLastProcessLineNumber();
        int tileNum = sweepLine.getLastProcessTileNumber();
        clearLineNum += lineNum;
        levelUpLineNum += lineNum;
        clearTileNum += tileNum;
        tileNum -= lineNum;
        if (tileNum > 0) levelUpTileNum += tileNum;
        if (levelUpLineNum >= BOARDH * 2) {
            gameLevel += levelUpLineNum / (BOARDH * 2);
            levelUpLineNum %= BOARDH * 2;
            isLevelUp = true;
        }
        if (levelUpTileNum >= BOARDH) {
            gameLevel += levelUpTileNum / BOARDH;
            levelUpTileNum %= BOARDH;
            isLevelUp = true;
        }
        if (isLevelUp) {
            sweepLine.setSpeed(gameLevel);
        }
    }
    //timer, touch.
    @Override
    public void onTimer(int id, int sendNum) {
        if (!isDie) {
            if (sweepLine.processMain(sendNum)) {
                //game over.
                isDie = true;
                //test revival code
                /*
                sweepLine.newTile();
                scoreLevelReset();
                sweepLine.tilePosition.setPosY((int)sweepLine.position / tileSize);
                sweepLine.restrictTilePosition.setPosY((int)sweepLine.position / tileSize - 1);
                sweepLine.newTile();
                startTileAllocate(0, sweepLine.restrictTilePosition.getY());
                for (int tileY = sweepLine.restrictTilePosition.getY(), tileX = 0; tileX < BOARDW; tileX++){
                    tile_S[tileY][tileX].moveAble = false;
                }
                Tools.simpleToast("부활하였습니다.");
                needLineUpdate = true;
                */
            }
            else {//sweepLine processing complete.
                if (sweepLine.getLastProcessLineNumber() > 0) {
                    gameScore += sweepLine.getLastProcessScore() * levelMultiplier();
                    sl_ProcessResult();
                }
            }
            if (needLineUpdate) {
                sweepLine.needLineUpdate();
                needLineUpdate = false;
            }
            if (isDie){
                for (int x = 0; x < BOARDW; x++) {
                    for (int y = 0; y < BOARDH; y++) {
                        tile_S[y][x].forceCloseProcess();
                    }
                }
            }
            else {
                for (int x = 0; x < BOARDW; x++) {
                    for (int y = 0; y < BOARDH; y++) {
                        tile_S[y][x].onTimer(id, sendNum);
                    }
                }
            }
        }
    }
    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        if (isDie) return true;
        TouchInfo t_Info = null;
        Iterator<TouchInfo> it;
        boolean flag;
        Coord touchTilePos = new Coord(cursorTilePos);
        Tile tile;
        //sweepLine touch.
        float sl_TouchY = touchInfo.y;
        if (sl_TouchY > 0){
            if (touchInfo.y < tileSize){
                sl_TouchY += (BOARDH - 1) * tileSize;
            }
            else { sl_TouchY -= tileSize; }
        }
        if (sweepLine.touchInput(new TouchInfo(touchInfo.x, sl_TouchY, touchInfo.id, touchInfo.action))) {
            //game over.
            isDie = true;
        }
        else {
            if (sweepLine.getLastProcessLineNumber() > 0) {
                gameScore += sweepLine.getLastProcessScore() * levelMultiplier();
                sl_ProcessResult();
            }
        }
        //touch.
        switch(touchInfo.action){
            case TouchInfo.DOWN:
                touchInput.add(touchInfo);
                touchTilePos.setPos((int) touchInfo.x / tileSize, (int) touchInfo.y / tileSize);
                if (touchTilePos.isOut()){//not select tile.
                    //processing touch control button.
                    int x = (int)touchInfo.x, y = (int)touchInfo.y - tileOutputHeight;
                    controlButtonTouchDown(x, y);
                    if (!moveCursorState && rect_Control[CONTROL_MOVECURSOR].contains(x, y)){
                        moveCursorButtonId = touchInfo.id;
                        moveCursorState = true;
                    }
                }
                else {//select tile
                    if (cursorTilePos.samePos(touchTilePos)){
                        tile = getTileToCursor(cursorTilePos);
                        if (tile.processAble(Tile.P_ROTATE_R)) tile.startProcess(Tile.P_ROTATE_R);
                    }
                    else { cursorTilePos = touchTilePos; }
                }
                break;
            case TouchInfo.MOVE:
                //process touch move.
                break;
            case TouchInfo.UP:
                //find first TouchInfo of touchInfo.id.
                it = touchInput.iterator();
                flag = false;
                while(it.hasNext()){
                    t_Info = it.next();
                    if (t_Info.id == touchInfo.id){
                        //find first TouchInfo complete.
                        it.remove();
                        flag = true;
                        break;
                    }
                }
                if (flag){
                    //process touch out.
                    if (moveCursorState && t_Info.id == moveCursorButtonId){ moveCursorState = false; }
                }
                break;
            case TouchInfo.CANCEL:
                touchInput.clear();
                moveCursorState = false;
                break;
        }
        return true;
    }
    void controlButtonTouchDown(int x, int y){
        if (!cursorTilePos.isOut()){//able to process control button.
            Direction di = null, diM;
            Coord obPos;
            Tile tile, tile2;
            tile = getTileToCursor(cursorTilePos);
            if (rect_Control[CONTROL_ROTATEL].contains(x, y)){
                if (tile.processAble(Tile.P_ROTATE_L)) tile.startProcess(Tile.P_ROTATE_L);
            }
            else if (rect_Control[CONTROL_ROTATER].contains(x, y)){
                if (tile.processAble(Tile.P_ROTATE_R)) tile.startProcess(Tile.P_ROTATE_R);
            }
            else if (rect_Control[CONTROL_R].contains(x, y)){ di = new Direction(Direction.R); }
            else if (rect_Control[CONTROL_L].contains(x, y)){ di = new Direction(Direction.L); }
            else if (rect_Control[CONTROL_U].contains(x, y)){ di = new Direction(Direction.U); }
            else if (rect_Control[CONTROL_D].contains(x, y)){ di = new Direction(Direction.D); }
            if (di != null) {
                obPos = new Coord(cursorTilePos);
                if (di.get() == Direction.U && obPos.getY() == 0) obPos.setPosY(BOARDH);
                if (di.get() == Direction.D && obPos.getY() == BOARDH) obPos.setPosY(0);
                if (moveCursorState){
                    //cursor move.
                    obPos.setMode(Coord.MODE_CYCLE);
                    obPos.move(di);
                    cursorTilePos.setPos(obPos.getX(), obPos.getY());
                }
                else {
                    //tile move.
                    obPos.move(di);
                    if (!obPos.isOut()) {
                        diM = new Direction(di);
                        diM.mirror();
                        tile2 = getTileToCursor(obPos);
                        if (tile.processAble(di) && tile2.processAble(diM)) {
                            tileSwapToCursor(cursorTilePos, obPos);
                            tile.startProcess(di);
                            tile2.startProcess(diM);
                            cursorTilePos = obPos;
                        }
                    }
                }
            }
        }
    }
    @Override
    public void tileUpdate() { needLineUpdate = true; }
}

class SweepLine {
    //gameBoard.
    GameBoard gameBoard;
    int tileSize;
    //Score value.
    int score, p_Score;
    int lineNumber;
    int tileNumber, p_TileNumber;
    //
    int alphaValue = 0xdd;
    //sweepLine property.
    double speed, position;
    double speedDefault, speedHigh, speedIncreaseValue;
    Coord tilePosition, restrictTilePosition;
    int height, cycleHeight;
    //sweepLine moving identity value.
    double autoMoveValue, userMoveValue;
    static final int MOVE_AUTO = 0, MOVE_USER = 1;
    int lastMoveIdentity;
    //connectSuccessInfo.
    boolean[][] tileConnectSuccess = new boolean[GameBoard.BOARDH][GameBoard.BOARDW];
    //line connecting Info.
    class ConnectInfo{
        Coord pos;
        Direction direction;
        ConnectInfo(Direction di, Coord pos){ direction = di; this.pos = pos; }
    }
    //touch property.
    boolean touchFlag = false;//whether user touch/moving sweepLine.
    boolean touchMoveFlag;//whether user moving sweepLine once.
    int touchId;
    int touchMargin;//sweepLine's touch radius.
    int touchMoveTileNum = 0;



    //constructor.
    SweepLine(GameBoard input){
        //gameBoard class(for tile).
        gameBoard = input;
        tileSize = gameBoard.tileSize;
        //tilePosition.
        tilePosition = new Coord(0, 1, Coord.MODE_CONSTRAINT, Coord.MODE_CYCLE);
        tilePosition.setBorder(0, GameBoard.BOARDW, 0, GameBoard.BOARDH);
        restrictTilePosition = new Coord(tilePosition);
        restrictTilePosition.setPos(0, 0);
        //sweepLine cycle, height.
        cycleHeight = tileSize * GameBoard.BOARDH;
        height = tileSize / 5;
        //speed value.
        speed = speedDefault = (double)tileSize / (6000 / GameMain.TIMERPERIOD_MAIN);
        speedHigh = (double)tileSize / (3500 / GameMain.TIMERPERIOD_MAIN);
        speedIncreaseValue = 1.15;
        //position.
        position = 0d;
        //touchMargin.
        touchMargin = tileSize / 3;
        //
        autoMoveValue = userMoveValue = 0d;
    }
    //drawing.
    void draw(Canvas canvas, int margin){
        canvas.drawRect(0, margin + (int)position - height,
                gameBoard.outputWidth, margin + (int)position,
                Tools.colorPaint(MyColor.aColor(alphaValue, MyColor.BLUE)));
    }
    //
    int getLastProcessScore(){ return score; }
    int getLastProcessLineNumber(){ return lineNumber; }
    int getLastProcessTileNumber(){ return tileNumber; }
    //
    void setSpeed(int level){
        if (level > 0){
            speed = speedHigh - (speedHigh - speedDefault) * Math.pow(speedIncreaseValue, (double)(1 - level));
        }
    }
    //touch moving process.,
    boolean touchInput(TouchInfo touchInfo){
        lineNumber = 0; //if touchInput does not move sweepLine, getLastProcessLineNumber() should return 0.
        switch(touchInfo.action){
            case TouchInfo.DOWN:
                if (!touchFlag){
                    int intPosition = (int)this.position;
                    int touchY = (int)touchInfo.y;
                    if (intPosition - touchMargin < touchY && touchY < intPosition + touchMargin){//touch the sweepLine.
                        touchId = touchInfo.id;
                        touchFlag = true;
                        touchMoveFlag = false;
                    }
                }
                break;
            case TouchInfo.MOVE:
                if (touchFlag && touchId == touchInfo.id){ return moveLineTouch((double)touchInfo.y); }
                break;
            case TouchInfo.UP:
                if (touchFlag && touchId == touchInfo.id) touchFlag = false;
                break;
            case TouchInfo.CANCEL:
                touchFlag = false;
                break;
        }
        return false;//Not game over.
    }
    boolean moveLineTouch(double y){
        if (0 < y && y < cycleHeight){
            double moveDelta = (y + cycleHeight - position) % cycleHeight;
            if ((int)moveDelta < cycleHeight / 2){//prevent reverse teleport moving.
                if (touchMoveFlag || (int)moveDelta > touchMargin){//user move sweepLine once || user move sweepLine obviously.
                    touchMoveFlag = true;
                    userMoveValue += moveDelta;
                    lastMoveIdentity = MOVE_USER;
                    return moveLineDelta(moveDelta);
                }
            }
        }
        return false;//Not game over.
    }
    //entry of process.
    boolean processMain(int sendNum){ return moveLine(sendNum); }
    //
    boolean moveLine(int processNum){
        double moveDelta = speed * processNum;
        autoMoveValue += moveDelta;
        lastMoveIdentity = MOVE_AUTO;
        return moveLineDelta(speed * processNum);
    }
    boolean moveLineDelta(double delta){
        int tileProcessHeight;//how many game tile's row have to processing.
        position += delta;
        score = lineNumber = tileNumber = 0;
        tileProcessHeight = (int)position / tileSize - tilePosition.getY();
        if (position >= cycleHeight) position = (double)((int)position % cycleHeight);
        if (processLine(tileProcessHeight)) return true;//Die, game over.
        return false;
    }
    boolean processLine(int tileProcessHeight){
        if (tileProcessHeight == 0){
            return false;//NOT game over.
        }
        else {
            for (int processNum = 0; processNum < tileProcessHeight; processNum++) {
                int tileY = tilePosition.getY();
                //force close Tile's processing first.
                for (int tileX = 0; tileX < GameBoard.BOARDW; tileX++) {
                    gameBoard.tile_S[tileY][tileX].forceCloseProcess();
                }
                //processLine.
                if (!processLineRow()){//Die, game over.
                    return true;
                }
                else {
                    //line processing complete.
                    int moveIdentity = MOVE_AUTO;
                    switch (lastMoveIdentity){
                        case MOVE_AUTO:
                            if (userMoveValue > tileSize / (double)2){
                                moveIdentity = MOVE_USER;
                                userMoveValue = 0d;
                            }
                            break;
                        case MOVE_USER:
                            if (autoMoveValue < tileSize / (double)2){
                                moveIdentity = MOVE_USER;
                                autoMoveValue = 0d;
                            }
                            break;
                    }
                    switch(moveIdentity){
                        case MOVE_AUTO:
                            score += p_Score;
                            touchMoveTileNum = 0;
                            break;
                        case MOVE_USER:
                            score += p_Score * (++touchMoveTileNum + 1);
                            break;
                    }
                    tileNumber += p_TileNumber;
                    lineNumber++;
                }
                //generate new Tile.
                newTile();
                //set Tile restrict.
                for (int tileX = 0; tileX < GameBoard.BOARDW; tileX++) {
                    gameBoard.tile_S[tileY][tileX].moveAble = false;
                }
                //move to next line.
                restrictTilePosition = tilePosition;
                tilePosition = new Coord(tilePosition).move(new Direction(Direction.D));
            }
            switch (lastMoveIdentity){
                case MOVE_AUTO:
                    userMoveValue = 0d;
                    autoMoveValue = position % (double)tileSize;
                    break;
                case MOVE_USER:
                    autoMoveValue = 0d;
                    userMoveValue = position % (double)tileSize;
                    break;
            }
            processLineRow();
        }
        return false;//NOT game over.
    }
    boolean processLineRow(){
        //return value.
        boolean isConnectSuccess = true;
        //result of line flow.
        Direction[] newFlowDirection = null;
        //temp.
        Coord tempPos = null;
        Direction tempDirection = null;
        //position of Y.
        int tileX, tileY = tilePosition.getY(), restrictTileY = restrictTilePosition.getY();
        //line connecting Info.
        LinkedList<ConnectInfo> connectInfos = new LinkedList<>();
        LinkedList<ConnectInfo> connectInfosU = new LinkedList<>();
        ConnectInfo connectInfo = null;

        //
        p_Score = 0;
        p_TileNumber = 0;
        //reset connect state.
        tileConnectInfoReset();
        for (tileX = 0; tileX < GameBoard.BOARDW; tileX++){
            for (int y = 0; y < GameBoard.BOARDH; y++){
                if (y != restrictTileY){
                    gameBoard.tile_S[y][tileX].deleteConnect();
                }
            }
        }
        //line connecting.
        for (tileX = 0; tileX < GameBoard.BOARDW; tileX++){
            if (gameBoard.tile_S[restrictTileY][tileX].lineD){//whether upper tile's lineFlow exist.
                if (gameBoard.tile_S[tileY][tileX].isLine(new Direction(Direction.U))){//check lineflow is able.
                    newFlowDirection = gameBoard.tile_S[tileY][tileX].lineFlow(new Direction(Direction.U));//lineflow.
                    p_Score += gameBoard.tile_S[tileY][tileX].connectScore();
                    tempPos = new Coord(gameBoard.tileExternalPosEx).setPos(tileX, tileY);
                    for (Direction di: newFlowDirection){//push to connectInfos stack.
                        if (di.get() == Direction.U) connectInfosU.add(new ConnectInfo(di, tempPos));
                        else connectInfos.add(new ConnectInfo(di, tempPos));
                    }
                    while(!connectInfos.isEmpty()){
                        connectInfo = connectInfos.getLast();
                        newFlowDirection = null;
                        tempPos = new Coord(connectInfo.pos).move(connectInfo.direction);
                        switch(connectInfo.direction.get()){
                            case Direction.R:
                            case Direction.L:
                            case Direction.D:
                                if (!tempPos.isOut()){//check out of board.
                                    if (tempPos.getY() == restrictTileY){//prevent infinity lineflow.
                                        break;
                                    }
                                    tempDirection = new Direction(connectInfo.direction).mirror();
                                    if (gameBoard.tile_S[tempPos.getY()][tempPos.getX()].isLine(tempDirection)){//check lineflow is able.
                                        newFlowDirection = gameBoard.tile_S[tempPos.getY()][tempPos.getX()].lineFlow(tempDirection);
                                        if (tempPos.getY() == tileY ){
                                            p_Score += gameBoard.tile_S[tileY][tempPos.getX()].connectScore();
                                            p_TileNumber++;
                                        }
                                    }
                                    else {//lineflow fail.
                                        if (tempPos.getY() == tileY) isConnectSuccess = false;
                                        tileConnectSuccess[connectInfo.pos.getY()][connectInfo.pos.getX()] = false;
                                    }
                                }
                                else {//lineflow fail(out of board).
                                    if (tempPos.getY() == tileY) isConnectSuccess = false;
                                    tileConnectSuccess[connectInfo.pos.getY()][connectInfo.pos.getX()] = false;
                                }
                                break;
                        }
                        connectInfos.removeLast();
                        if (newFlowDirection != null){
                            for (Direction di : newFlowDirection){
                                if (di.get() == Direction.U) connectInfosU.add(new ConnectInfo(di, tempPos));
                                else connectInfos.add(new ConnectInfo(di, tempPos));
                            }
                        }
                    }
                }
                else{
                    isConnectSuccess = false;//lineflow fail.
                    tileConnectSuccess[restrictTileY][tileX] = false;
                }
            }
        }
        //ConnectU Check.
        while(!connectInfosU.isEmpty()){
            connectInfo = connectInfosU.getLast();
            tempPos = new Coord(connectInfo.pos).move(connectInfo.direction);
            if (!gameBoard.tile_S[tempPos.getY()][tempPos.getX()].lineD){//check lineflow.
                if (tempPos.getY() == restrictTileY) isConnectSuccess = false;
                tileConnectSuccess[connectInfo.pos.getY()][connectInfo.pos.getX()] = false;
            }
            connectInfosU.removeLast();
        }
        //mustConnect check.
        for (tileX = 0; tileX < GameBoard.BOARDW; tileX++) {
            if (gameBoard.tile_S[tileY][tileX].mustConnect) {
                if (!gameBoard.tile_S[tileY][tileX].isConnectAll()) {
                    tileConnectSuccess[tileY][tileX] = false;
                    isConnectSuccess = false;
                }
            }
        }
        //if Connect is success, then more check.
        if (isConnectSuccess){
            //Check line is flowing to down
            boolean lineFlowDown = false;
            for (tileX = 0; tileX < GameBoard.BOARDW; tileX++){
                if (gameBoard.tile_S[tileY][tileX].lineD){
                    lineFlowDown = true;
                    break;
                }
            }
            if (!lineFlowDown) isConnectSuccess = false;
        }
        return isConnectSuccess;
    }
    void tileConnectInfoReset(){
        for (boolean[] loop1 : tileConnectSuccess){
            for (int loop2 = 0; loop2 < loop1.length; loop2++) loop1[loop2] = true;
        }
    }
    void needLineUpdate(){ processLineRow(); }
    void newTile(){
        int tileY = restrictTilePosition.getY();
        for (int tileX = 0; tileX < GameBoard.BOARDW; tileX++){
            gameBoard.tileAllocate(tileX, tileY);
        }
    }
}