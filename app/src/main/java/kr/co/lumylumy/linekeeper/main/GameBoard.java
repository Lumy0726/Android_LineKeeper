package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
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

class GameBoard implements TimerAble, TouchEvent, Tile.TileUpdateReceiver {
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
    //rect - for Board Cycle.
    static final int RECT_CYCLETOP1 = 0, RECT_CYCLETOP2 = 1, RECT_CYCLEBOTTOM1 = 2, RECT_CYCLEBOTTOM2 = 3;
    Rect[] rect_Cycle = new Rect[4];
    //Control Bitmap, rect.
    Bitmap b_Control;
    static final int CONTROL_NUM = 6, CONTROL_ROTATEL = 0, CONTROL_ROTATER = 1, CONTROL_R = 2, CONTROL_U = 3, CONTROL_L = 4, CONTROL_D = 5;
    Rect[] rect_Control = new Rect[CONTROL_NUM];
    //other Bitmap.
    Bitmap b_Cursor;
    //touchInput.
    ArrayList<TouchInfo> touchInput = new ArrayList<>();
    //static final int TOUCHNUM_MAX = 3;
    //cursor.
    Coord cursorTilePos;
    //
    Coord tileInternalPosEx;
    //sweepLine.
    SweepLine sweepLine;
    boolean needLineUpdate;

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
    void setPosition(int xPos, int yPos){ this.xPos = xPos; this.yPos = yPos; }
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
        Canvas canvas = new Canvas();
        Matrix matrix = new Matrix();
        b_Control = Bitmap.createBitmap(outputWidth, controlPanelHeight, Bitmap.Config.ARGB_8888);
        Bitmap bitmap_Arrow = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Bitmap bitmap_Rotate = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Bitmap frameBitmap = frameBitmap(tileSize, tileSize, MyColor.hsvColor(30, 100, 100));
        canvas.setBitmap(bitmap_Arrow);
        Tools.resetBitmap(canvas, MyColor.hsvColor(0, 0, 80));
        canvas.drawBitmap(frameBitmap, 0, 0, null);
        canvas.drawBitmap(arrowBitmap(tileSize, tileSize, MyColor.RED), 0, 0, null);
        canvas.setBitmap(bitmap_Rotate);
        Tools.resetBitmap(canvas, MyColor.hsvColor(0, 0, 80));
        canvas.drawBitmap(frameBitmap, 0, 0, null);
        canvas.drawBitmap(rotateArrowBitmap(tileSize, MyColor.RED), 0, 0, null);
        canvas.setBitmap(b_Control);
        Tools.resetBitmap(canvas, MyColor.WHITE);
        canvas.drawBitmap(bitmap_Arrow, null, rect_Control[CONTROL_U], null);
        matrix.setRotate(-90);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Arrow, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_L], null);
        matrix.setRotate(-180);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Arrow, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_D], null);
        matrix.setRotate(90);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Arrow, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_R], null);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Arrow, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_R], null);
        canvas.drawBitmap(bitmap_Rotate, null, rect_Control[CONTROL_ROTATEL], null);
        matrix.setScale(-1, 1);
        canvas.drawBitmap(Bitmap.createBitmap(bitmap_Rotate, 0, 0, tileSize, tileSize, matrix, false), null, rect_Control[CONTROL_ROTATER], null);
    }
    void init(){
        //Tile initialize.
        Tile.makeTileBitmap(tileSize);
        //sweepLine.
        sweepLine = new SweepLine(this);
        needLineUpdate = true;
        //
        setTileCyclePanel();
        setControlPanel();
        //Cursor Bitmap.
        b_Cursor = frameBitmap(tileSize, tileSize, MyColor.RED);
        //BitmapBoard.
        b_Board = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
        c_Board = Tools.newCanvas(b_Board);
        //Tile allocate.
        tileInternalPosEx = new Coord(0, 0, Coord.MODE_CONSTRAINT, Coord.MODE_CYCLE);
        tileInternalPosEx.setBorder(0, outputWidth, tileSize, tileSize * (BOARDH + 1));
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
        //cursorTilePos Coord setting.
        cursorTilePos = new Coord();
        cursorTilePos.setMode(Coord.MODE_CONSTRAINT, Coord.MODE_CONSTRAINT);
        cursorTilePos.setBorder(0, BOARDW, 0, BOARDH + 1);
        cursorTilePos.forceOut();
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
            switch((int)(Math.random() * 3)){
                case 0:
                    tile_S[y][x] = new Tile_STRAIGHT(this, direction, coord);
                    break;
                case 1:
                    tile_S[y][x] = new TileA(this, direction, coord);
                    break;
                case 2:
                    tile_S[y][x] = new TileB(this, direction, coord);
                    break;
            }
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

    //some Bitmap drawing method.
    Bitmap arrowBitmap(int width, int height, int color){
        int arrowWidth = width / 10, width_2 = width / 2;
        Bitmap rValue = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Path path = new Path();
        path.moveTo(width_2, arrowWidth);
        path.rLineTo(arrowWidth * 3 / 2, arrowWidth * 2);
        path.lineTo(width_2 + arrowWidth / 2, arrowWidth * 3);
        path.lineTo(width_2 + arrowWidth / 2, height - arrowWidth);
        path.rLineTo(-arrowWidth, 0);
        path.lineTo(width_2 - arrowWidth / 2, arrowWidth * 3);
        path.rLineTo(-arrowWidth, 0);
        path.close();
        Paint paint = Tools.colorPaint(color);
        paint.setStyle(Paint.Style.FILL);
        Tools.newCanvas(rValue).drawPath(path, paint);
        return rValue;
    }
    Bitmap frameBitmap(int width, int height, int color){
        Bitmap rValue;
        Canvas canvas = new Canvas();
        rValue = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(rValue);
        Tools.resetBitmap(canvas, color);
        int marginX = width / 15;
        int marginY = height / 15;
        canvas.drawRect(marginX, marginY, width - marginX, height - marginY, Tools.colorPaint(0, true));
        return rValue;
    }
    Bitmap rotateArrowBitmap(int size, int color){
        float middle = size / (float)2, quarter = size / (float)4, width_2 = size / 20;
        Bitmap rValue;
        Paint paint;
        Path path = new Path();
        Canvas canvas = new Canvas();
        rValue = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(rValue);
        canvas.drawCircle(middle, middle, quarter + width_2, Tools.colorPaint(color));
        canvas.drawCircle(middle, middle, quarter - width_2, paint = Tools.colorPaint(0, true));
        paint.setStyle(Paint.Style.FILL);
        path.moveTo(0, quarter);
        path.lineTo(middle, 0);
        path.lineTo(middle, middle);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(color);
        path.reset();
        path.moveTo(middle, quarter - width_2 * 3);
        path.lineTo(middle - width_2 * 3, quarter);
        path.lineTo(middle, quarter + width_2 * 3);
        path.close();
        canvas.drawPath(path, paint);
        return rValue;
    }

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
        //androidLog(String.format("GameBoard-draw-tileDraw: %5.2f", time_D.getTimeAv()));
        sweepLine.draw(c_Board, tileSize);
        //time_C.reset();
        c_Board.drawBitmap(b_Board, rect_Cycle[RECT_CYCLETOP1], rect_Cycle[RECT_CYCLEBOTTOM1], null);
        c_Board.drawBitmap(b_Board, rect_Cycle[RECT_CYCLEBOTTOM2], rect_Cycle[RECT_CYCLETOP2], null);
        c_Board.drawBitmap(b_Board, rect_Cycle[RECT_CYCLEBOTTOM1], rect_Cycle[RECT_CYCLETOP1], null);
        c_Board.drawRect(rect_Cycle[RECT_CYCLETOP1], Tools.colorPaint(MyColor.aColor(0x7f, MyColor.hsvColor(0, 80, 50))));
        //androidLog(String.format("GameBoard-draw-tileCycle: %5.2f", time_C.getTimeAv()));
        if (!cursorTilePos.isOut()){ c_Board.drawBitmap(b_Cursor, cursorTilePos.getX() * tileSize, cursorTilePos.getY() * tileSize, null); }
        c_Board.drawBitmap(b_Control, 0, tileOutputHeight, null);
        //time_M.reset();
        canvas.drawBitmap(b_Board, r_Output, Tools.rectWH(xPos, yPos, outputWidth, outputHeight), null);
        //androidLog(String.format("GameBoard-draw-main: %5.2f", time_M.getTimeAv()));
    }
    //
    int getTileY(Coord input){ return Tools.remainder(input.getY() - 1, BOARDH); }
    Tile getTile(Coord input){ return tile_S[getTileY(input)][input.getX()]; }
    void tileSwap(Coord input1, Coord input2){
        Tile temp = getTile(input1);
        tile_S[getTileY(input1)][input1.getX()] = getTile(input2);
        tile_S[getTileY(input2)][input2.getX()] = temp;
    }
    //timer, touch.
    @Override
    public void onTimer(int id, int sendNum) {
        if (sweepLine.processMain(sendNum)){
            //game over.
            //test revival code.
            sweepLine.newTile();
            sweepLine.tilePosition.setPosY((int)sweepLine.position / tileSize);
            sweepLine.restrictTilePosition.setPosY((int)sweepLine.position / tileSize - 1);
            sweepLine.newTile();
            startTileAllocate(0, sweepLine.restrictTilePosition.getY());
            for (int tileY = sweepLine.restrictTilePosition.getY(), tileX = 0; tileX < BOARDW; tileX++){
                tile_S[tileY][tileX].moveAble = false;
            }
            Tools.simpleToast("부활하였습니다.");
            needLineUpdate = true;
        }
        if (needLineUpdate){ sweepLine.needLineUpdate(); }
        for (int x = 0; x < BOARDW; x++){
            for (int y = 0; y < BOARDH; y++){
                tile_S[y][x].onTimer(id, sendNum);
            }
        }
    }
    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        TouchInfo t_Info = null;
        Iterator<TouchInfo> it;
        boolean flag;
        Coord touchTilePos = new Coord(cursorTilePos);
        Tile tile, tile2;
        switch(touchInfo.action){
            case TouchInfo.DOWN:
                touchInput.add(touchInfo);
                touchTilePos.setPos((int) touchInfo.x / tileSize, (int) touchInfo.y / tileSize);
                if (touchTilePos.isOut()){//touch control button
                    if (!cursorTilePos.isOut()){
                        Direction di = null, diM;
                        Coord obPos;
                        tile = getTile(cursorTilePos);
                        int x = (int) touchInfo.x, y = (int) touchInfo.y - tileOutputHeight;
                        if (rect_Control[CONTROL_ROTATEL].contains(x, y)){
                            if (tile.processAble(Tile.P_ROTATE_L)) tile.startProcess(Tile.P_ROTATE_L);
                        }
                        else if (rect_Control[CONTROL_ROTATER].contains(x, y)){
                            if (tile.processAble(Tile.P_ROTATE_R)) tile.startProcess(Tile.P_ROTATE_R);
                        }
                        else if (rect_Control[CONTROL_R].contains(x, y)){ di = new Direction(Direction.R); }
                        else if (rect_Control[CONTROL_U].contains(x, y)){ di = new Direction(Direction.U); }
                        else if (rect_Control[CONTROL_L].contains(x, y)){ di = new Direction(Direction.L); }
                        else if (rect_Control[CONTROL_D].contains(x, y)){ di = new Direction(Direction.D); }
                        if (di != null) {
                            obPos = new Coord(cursorTilePos);
                            obPos.move(di);
                            if (!obPos.isOut()) {
                                diM = new Direction(di);
                                diM.mirror();
                                tile2 = getTile(obPos);
                                if (tile.processAble(di) && tile2.processAble(diM)) {
                                    tileSwap(cursorTilePos, obPos);
                                    tile.startProcess(di);
                                    tile2.startProcess(diM);
                                    cursorTilePos = obPos;
                                }
                            }
                        }
                    }
                }
                else {//select tile
                    if (cursorTilePos.samePos(touchTilePos)){
                        tile = getTile(cursorTilePos);
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
                }
                break;
            case TouchInfo.CANCEL:
                touchInput.clear();
                break;
        }
        return true;
    }
    @Override
    public void tileUpdate() { needLineUpdate = true; }
}

class SweepLine {
    //gameBoard.
    GameBoard gameBoard;
    int tileSize;
    //
    int alphaValue = 0xdd;
    //sweepLine property.
    double position, speed;
    Coord tilePosition, restrictTilePosition;
    int height, cycleHeight;

    //constructor.
    SweepLine(GameBoard input){
        gameBoard = input;
        tileSize = gameBoard.tileSize;
        tilePosition = new Coord(0, 1, Coord.MODE_CONSTRAINT, Coord.MODE_CYCLE);
        tilePosition.setBorder(0, GameBoard.BOARDW, 0, GameBoard.BOARDH);
        restrictTilePosition = new Coord(tilePosition);
        restrictTilePosition.setPos(0, 0);
        cycleHeight = tileSize * GameBoard.BOARDH;
        height = tileSize / 5;
        speed = (double)tileSize / 120;
        position = (double)(height + 1);
    }
    //drawing.
    void draw(Canvas canvas, int margin){
        canvas.drawRect(0, margin + (int)position - height,
                gameBoard.outputWidth, margin + (int)position,
                Tools.colorPaint(MyColor.aColor(alphaValue, MyColor.BLUE)));
    }
    //entry of process.
    boolean processMain(int sendNum){ return moveLine(sendNum); }
    //
    boolean moveLine(int processNum){
        int tileProcessHeight;//how many game tile's row have to processing.
        position += speed * processNum;
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
                if (!processLineRow()) return true;//Die, game over.
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
        int tempX = 0;
        Direction directionU = new Direction(Direction.U);
        Direction directionR = new Direction(Direction.R);
        Direction directionL = new Direction(Direction.L);
        Direction directionD = new Direction(Direction.D);
        //position of Y.
        int tileX, tileY = tilePosition.getY(), restrictTileY = restrictTilePosition.getY();
        //line connecting Info.
        class ConnectInfo{
            int x;
            Direction direction;
            ConnectInfo(Direction di, int x){ direction = di; this.x = x; }
        }
        LinkedList<ConnectInfo> connectInfo_S = new LinkedList<>();
        ConnectInfo connectInfo = null;
        //reset connect state.
        for (tileX = 0; tileX < GameBoard.BOARDW; tileX++){
            gameBoard.tile_S[tileY][tileX].deleteConnect();
        }
        //line connecting.
        for (tileX = 0; tileX < GameBoard.BOARDW; tileX++){
            if (gameBoard.tile_S[restrictTileY][tileX].lineD){//whether upper tile's lineFlow exist.
                if (gameBoard.tile_S[tileY][tileX].isLine(directionU)){//check lineflow is able.
                    newFlowDirection = gameBoard.tile_S[tileY][tileX].lineFlow(directionU);//lineflow.
                    for (Direction di: newFlowDirection){ connectInfo_S.add(new ConnectInfo(di, tileX)); }//push to connectInfo_S stack.
                    while(!connectInfo_S.isEmpty()){
                        connectInfo = connectInfo_S.getLast();
                        newFlowDirection = null;
                        switch(connectInfo.direction.get()){
                            case Direction.R:
                                if (connectInfo.x < GameBoard.BOARDW - 1){//check out of board.
                                    if (gameBoard.tile_S[tileY][connectInfo.x + 1].isLine(directionL)){//check lineflow is able.
                                        newFlowDirection = gameBoard.tile_S[tileY][connectInfo.x + 1].lineFlow(directionL);
                                        tempX = connectInfo.x + 1;
                                    }
                                    else { isConnectSuccess = false; }//lineflow fail.
                                }
                                else { isConnectSuccess = false; }//lineflow fail(out of board).
                                break;
                            case Direction.L:
                                if (0 < connectInfo.x){//check out of board.
                                    if (gameBoard.tile_S[tileY][connectInfo.x - 1].isLine(directionR)){//check lineflow is able.
                                        newFlowDirection = gameBoard.tile_S[tileY][connectInfo.x - 1].lineFlow(directionR);
                                        tempX = connectInfo.x - 1;
                                    }
                                    else { isConnectSuccess = false; }//lineflow fail.
                                }
                                else { isConnectSuccess = false; }//lineflow fail(out of board).
                                break;
                            case Direction.U:
                                if (!gameBoard.tile_S[restrictTileY][connectInfo.x].lineD) isConnectSuccess = false;
                                break;
                            case Direction.D:
                                break;
                        }
                        connectInfo_S.removeLast();
                        if (newFlowDirection != null){
                            for (Direction di : newFlowDirection){
                                connectInfo_S.add(new ConnectInfo(di, tempX));
                            }
                        }
                    }
                }
                else isConnectSuccess = false;//lineflow fail.
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
            else {
                //mustConnect check.
                for (tileX = 0; tileX < GameBoard.BOARDW; tileX++) {
                    if (gameBoard.tile_S[tileY][tileX].mustConnect) {
                        if (!gameBoard.tile_S[tileY][tileX].isConnectAll()) {
                            isConnectSuccess = false;
                            break;
                        }
                    }
                }
            }
        }
        return isConnectSuccess;
    }
    void needLineUpdate(){ processLineRow(); }
    void newTile(){
        int tileY = restrictTilePosition.getY();
        for (int tileX = 0; tileX < GameBoard.BOARDW; tileX++){
            gameBoard.tileAllocate(tileX, tileY);
        }
    }
}