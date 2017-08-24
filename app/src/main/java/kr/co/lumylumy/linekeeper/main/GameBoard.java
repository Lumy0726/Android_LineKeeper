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

public class GameBoard implements TimerAble, TouchEvent {
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
        for (int x = 0; x < BOARDW; x++){
            for (int y = 0; y < BOARDH; y++){
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
                    tile_S[y][x] = new Tile_STRAIGHT(direction, coord);
                    break;
                case 1:
                    tile_S[y][x] = new TileA(direction, coord);
                    break;
                case 2:
                    tile_S[y][x] = new TileB(direction, coord);
                    break;
            }
        }
    }
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
        }
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
}

class SweepLine{
    //gameBoard.
    GameBoard gameBoard;
    int tileSize;
    //
    int alphaValue = 0xdd;
    //sweepLine property.
    double position = 0d, speed;
    Coord tilePosition, restrictTilePosition;
    int height, cycleHeight;
    //restrictTile's Bitmap
    Bitmap b_RestrictTile;

    //constructor.
    SweepLine(GameBoard input){
        gameBoard = input;
        tileSize = gameBoard.tileSize;
        tilePosition = new Coord(0, 0, Coord.MODE_CONSTRAINT, Coord.MODE_CYCLE);
        tilePosition.setBorder(0, GameBoard.BOARDW, 0, GameBoard.BOARDH);
        restrictTilePosition = new Coord(tilePosition);
        restrictTilePosition.setPos(0, GameBoard.BOARDH - 1);
        cycleHeight = tileSize * GameBoard.BOARDH;
        height = tileSize / 5;
        speed = (double)tileSize / 120;
        initBitmap();
    }
    void initBitmap(){
        int gradientWidth = tileSize / 8;//not real Size.
        int[] color = new int[]{0, 0, MyColor.aColor(alphaValue, MyColor.BLACK)};
        float[] gradientPos = new float[]{0f, 0.7f, 1f};
        Paint paint = new Paint();
        paint.setShader(new LinearGradient(0, 0, gradientWidth, gradientWidth, color, gradientPos, Shader.TileMode.MIRROR));
        b_RestrictTile = Bitmap.createBitmap(gameBoard.outputWidth, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(b_RestrictTile);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
    }
    //drawing.
    void draw(Canvas canvas, int margin){
        canvas.drawBitmap(b_RestrictTile, 0, margin + restrictTilePosition.getY() * tileSize, null);
        canvas.drawRect(0, margin + (int)position - height,
                gameBoard.outputWidth, margin + (int)position,
                Tools.colorPaint(MyColor.aColor(alphaValue, MyColor.BLUE)));
    }
    //entry of process.
    boolean processMain(int sendNum){
        return moveLine(sendNum);
    }
    //
    boolean moveLine(int processNum){
        //processNum = how many game ticks have to processing.
        int tileProcessNum;//how many game tiles have to processing.
        position += speed * processNum;
        tileProcessNum = (int)position / tileSize - tilePosition.getY();
        if (position >= cycleHeight) position = (double)((int)position % cycleHeight);
        if (processTile(tileProcessNum)) return true;//Die, game over.
        processLine();
        return false;
    }
    boolean processTile(int tileProcessNum){
        for (int processNum = 0; processNum < tileProcessNum; processNum++){
            int tileY = tilePosition.getY();
            //force close Tile's processing first.
            for (int tileX = 0; tileX < GameBoard.BOARDW; tileX++){
                gameBoard.tile_S[tileY][tileX].forceCloseProcess();
            }
            //processLine.
            if (!processLine()) return true;//Die, game over.
            //generate new Tile.
            newTile();
            //set Tile restrict.
            for (int tileX = 0; tileX < GameBoard.BOARDW; tileX++){
                gameBoard.tile_S[tileY][tileX].moveAble = false;
            }
            //move to next line.
            restrictTilePosition = tilePosition;
            tilePosition = new Coord(tilePosition).move(new Direction(Direction.D));
        }
        return false;//NOT game over.
    }
    boolean processLine(){
        boolean isConnect = false;
        //return isConnect;
        return true;
    }
    void newTile(){
        int tileY = restrictTilePosition.getY();
        for (int tileX = 0; tileX < GameBoard.BOARDW; tileX++){
            gameBoard.tileAllocate(tileX, tileY);
        }
    }
}

abstract class Tile implements TimerAble{
    //direction.
    Direction direction = new Direction(Direction.R);
    //tilesize.
    static int tileSize = 0;
    static int LineWidth = 0;
    //
    boolean moveAble = true;
    //
    boolean LineOn = false;
    //bitmap.
    Bitmap outBitmap;
    Bitmap[] rotateBitmap;
    //graphic processing.
    static final int P_LEVEL = 10;
    static final int P_RIGHT = 0, P_UP = 1, P_LEFT = 2, P_DOWN = 3, P_ROTATE_R = 4, P_ROTATE_L = 5;
    boolean isProcessing = false;
    int p_Content, p_Level;
    Direction p_Direction;
    static int moveSpeed;
    //Canvas's position(tile's middle).
    Coord pos, backupPos;

    //constructor.
    Tile(Direction direction, Coord pos){
        this.pos = pos;
        int di = direction.get();
        //this.drection's default value is R.
        if (di == Direction.U || di == Direction.L || di == Direction.D) this.direction.set(di);
        rotateBitmap = getRotateBitmap();
        directionToBitmap();
    }

    //graphic process.
    boolean processAble(int content){
        if (isProcessing) return false;
        switch(content){
            case P_ROTATE_R: return moveAble;
            case P_ROTATE_L: return moveAble;
            case P_RIGHT: return processAble(new Direction(Direction.R));
            case P_UP: return processAble(new Direction(Direction.U));
            case P_LEFT: return processAble(new Direction(Direction.L));
            case P_DOWN: return processAble(new Direction(Direction.D));
            default: return false;
        }
    }
    boolean processAble(Direction direction){
        if (isProcessing) return false;
        if (direction.get() % 2 == 1) return false;
        if (!moveAble) return false;
        Coord movePos = new Coord(pos);
        return !movePos.move(direction).isOut();
    }
    void startProcess(int content) {
        switch (content) {
            case P_ROTATE_R:
            case P_ROTATE_L:
                if (!moveAble || isProcessing) return;
                isProcessing = true;
                p_Content = content;
                p_Level = 0;
                break;
            case P_RIGHT: startProcess(new Direction(Direction.R)); break;
            case P_UP: startProcess(new Direction(Direction.U)); break;
            case P_LEFT: startProcess(new Direction(Direction.L)); break;
            case P_DOWN: startProcess(new Direction(Direction.D)); break;
        }
    }
    void startProcess(Direction direction){
        boolean ableDirection = false;
        if (!moveAble || isProcessing) return;
        switch(direction.get()){
            case Direction.R: p_Content = P_RIGHT; ableDirection = true;
                break;
            case Direction.U: p_Content = P_UP; ableDirection = true;
                break;
            case Direction.L: p_Content = P_LEFT; ableDirection = true;
                break;
            case Direction.D: p_Content = P_DOWN; ableDirection = true;
                break;
        }
        if (ableDirection){
            isProcessing = true;
            p_Direction = new Direction(direction);
            backupPos = new Coord(pos);
            p_Level = 0;
        }
    }
    void forceCloseProcess(){ onTimer(0, P_LEVEL); }
    @Override
    public void onTimer(int id, int sendNum) {
        if (isProcessing) {
            p_Level += sendNum;
            if (p_Level >= P_LEVEL){//end of process.
                p_Level = P_LEVEL;
                isProcessing = false;
            }
            if (p_Content == P_ROTATE_R || p_Content == P_ROTATE_L){//rotate process.
                if (isProcessing){ directionToBitmap(); }
                else {
                    direction.rotate((p_Content == P_ROTATE_L)?true:false, 2);
                    directionToBitmap();
                }
            }
            else {//moveing process.
                if (isProcessing){
                    pos.move(p_Direction, moveSpeed);
                }
                else {
                    pos = backupPos.move(p_Direction, tileSize);
                }
            }
        }
    }
    void draw(Canvas canvas){
        if (LineOn){
            canvas.drawRect(Tools.rectWH(pos.getX() - tileSize / 2, pos.getY() - tileSize / 2, tileSize, tileSize), Tools.colorPaint(MyColor.BLUE));
        }
        canvas.drawBitmap(outBitmap, pos.getX() - outBitmap.getWidth() / 2, pos.getY() - outBitmap.getHeight() / 2, null);
    }
    void directionToBitmap(){
        int di = direction.get();
        int rotateValue = 0, angle = P_LEVEL * 4;
        if (isProcessing){
            if (p_Content == P_ROTATE_L) rotateValue = p_Level;
            if (p_Content == P_ROTATE_R) rotateValue = -p_Level;
        }
        switch(di){
            case Direction.R: outBitmap = rotateBitmap[Tools.remainder(P_LEVEL * 3 + rotateValue, angle)];
                break;
            case Direction.U: outBitmap = rotateBitmap[Tools.remainder(rotateValue, angle)];
                break;
            case Direction.L: outBitmap = rotateBitmap[Tools.remainder(P_LEVEL + rotateValue, angle)];
                break;
            case Direction.D: outBitmap = rotateBitmap[Tools.remainder(P_LEVEL * 2 + rotateValue, angle)];
                break;
        }
    }

    //
    static Bitmap[] makeRotateBitmap(Bitmap bitmap){
        Bitmap[] bitmap_S = new Bitmap[P_LEVEL * 4];
        Matrix matrix = new Matrix();
        bitmap_S[0] = Bitmap.createBitmap(bitmap);
        for (int loop1 = 1; loop1 < bitmap_S.length; loop1++){
            matrix.setRotate(-(float)360 * loop1 / bitmap_S.length);
            bitmap_S[loop1] = Bitmap.createBitmap(bitmap_S[0], 0, 0, tileSize, tileSize, matrix, false);
        }
        return bitmap_S;
    }
    abstract Bitmap[] getRotateBitmap();
    //Connect Check.
    abstract boolean isConnect(Direction input, Direction output);

    //initialize Tile Bitmap.
    static void makeTileBitmap(int tileSize){
        Tile.tileSize = tileSize;
        LineWidth = tileSize / 5;
        moveSpeed = tileSize / P_LEVEL;
        TileA.makeTileBitmap();
        TileB.makeTileBitmap();
        Tile_STRAIGHT.makeTileBitmap();
    }
}
//example of tile.
/*
class Tile_EX extends Tile{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_EX(Direction direction, Coord pos){ super(direction, pos); }//constructor.
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    boolean isConnect(Direction input, Direction output) {
        //overriding tile's Line.
        return false;
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        //draw tile bitmap (Direction U)
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
}
*/

class Tile_STRAIGHT extends Tile{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_STRAIGHT(Direction direction, Coord pos){ super(direction, pos); }//constructor.
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    boolean isConnect(Direction input, Direction output) {
        Direction temp = new Direction(output);
        return !isProcessing && input.get() == temp.mirror().get() && (direction.get() == input.get() || direction.get() == output.get());
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tileBitmap);
        //draw tile bitmap (Direction U)
        Tools.resetBitmap(canvas, MyColor.hsvColor(24, 100, 80));
        canvas.drawRect((tileSize - LineWidth) / (float)2, 0, (tileSize + LineWidth) / (float)2, tileSize ,Tools.colorPaint(0, true));
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
}

//tile for test.
class TileA extends Tile{
    //Bitmap.
    static Bitmap[] bitmap_S = new Bitmap[Tile.P_LEVEL * 4];
    TileA(Direction direction, Coord pos){ super(direction, pos); }
    @Override
    boolean isConnect(Direction input, Direction output) {
        return false;
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }
    static void makeTileBitmap(){
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Matrix matrix = new Matrix();
        paint.setShader(new LinearGradient(0, 0, tileSize, tileSize, 0xff785496, 0xffddff00, Shader.TileMode.CLAMP));
        Rect rect = new Rect(0, 0, tileSize, tileSize);
        bitmap_S[0] = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap_S[0]);
        canvas.drawRect(rect, paint);
        for (int loop1 = 1; loop1 < bitmap_S.length; loop1++){
            matrix.setRotate(-(float)360 * loop1 / bitmap_S.length);
            bitmap_S[loop1] = Bitmap.createBitmap(bitmap_S[0], 0, 0, tileSize, tileSize, matrix, false);
        }
    }
}
class TileB extends Tile{
    //Bitmap.
    static Bitmap[] bitmap_S = new Bitmap[Tile.P_LEVEL * 4];
    TileB(Direction direction, Coord pos){ super(direction, pos); }
    @Override
    boolean isConnect(Direction input, Direction output) {
        return false;
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }
    static void makeTileBitmap(){
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Matrix matrix = new Matrix();
        paint.setShader(new LinearGradient(0, 0, 0, tileSize, MyColor.GREEN, MyColor.MAGENTA, Shader.TileMode.CLAMP));
        Rect rect = new Rect(0, 0, tileSize, tileSize);
        bitmap_S[0] = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap_S[0]);
        canvas.drawRect(rect, paint);
        for (int loop1 = 1; loop1 < bitmap_S.length; loop1++){
            matrix.setRotate(-(float)360 * loop1 / bitmap_S.length);
            bitmap_S[loop1] = Bitmap.createBitmap(bitmap_S[0], 0, 0, tileSize, tileSize, matrix, false);
        }
    }
}


class Direction{
    static final int R = 0, UR = 1, U = 2, UL = 3, L = 4, DL = 5, D = 6, DR = 7;
    int direction;
    Direction(){ set(R); }
    Direction(int direction){ set(direction); }
    Direction(Direction input){ this(input.get()); }
    Direction set(int direction){
        if (0 <= direction && direction < 8) this.direction = direction;
        return this;
    }
    int get(){ return direction; }
    Direction rotate(boolean left, int num){
        direction += (left?1:-1) * num;
        direction = Tools.remainder(direction, 8);
        return this;
    }
    Direction mirror(){ return rotate(true, 4); }
}

class Coord{
    static final int MODE_NONE = 0, MODE_CYCLE = 1, MODE_CONSTRAINT = 2;
    private int x, y;
    private int bx1 = 0xf0_00_00_00, by1 = 0xf0_00_00_00, bx2 = 0x0f_ff_ff_ff, by2 = 0x0f_ff_ff_ff;
    private int modeX, modeY;
    Coord(){ this(0, 0); }
    Coord(int x, int y){ this(x, y, MODE_NONE); }
    Coord(int x, int y, int mode){ this(x, y, mode, mode); }
    Coord(int x, int y, int modeX, int modeY){
        this.x = x; this.y = y;
        setMode(modeX, modeY);
    }
    Coord(Coord input){
        x = input.x; y = input.y;
        modeX = input.modeX; modeY = input.modeY;
        bx1 = input.bx1; bx2 = input.bx2;
        by1 = input.by1; by2 = input.by2;
    }
    Coord setMode(int mode){ return setMode(mode, mode); }
    Coord setMode(int modeX, int modeY){
        switch(modeX){
            case MODE_CONSTRAINT: this.modeX = MODE_CONSTRAINT;
                break;
            case MODE_CYCLE: this.modeX = MODE_CYCLE;
                break;
            case MODE_NONE:
            default:
                this.modeX = MODE_NONE;;
                break;
        }
        switch(modeY){
            case MODE_CONSTRAINT: this.modeY = MODE_CONSTRAINT;
                break;
            case MODE_CYCLE: this.modeY = MODE_CYCLE;
                break;
            case MODE_NONE:
            default:
                this.modeY = MODE_NONE;;
                break;
        }
        alignPos();
        return this;
    }
    Coord setBorder(int bx1, int bx2, int by1, int by2){
        if (bx1 < bx2){ this.bx1 = bx1; this.bx2 = bx2; }
        if (by1 < by2){ this.by1 = by1; this.by2 = by2; }
        alignPos();
        return this;
    }
    Coord setPosX(int x){ return setPos(x, y); }
    Coord setPosY(int y){ return setPos(x, y); }
    Coord setPos(int x, int y){
        this.x = x; this.y = y;
        alignPos();
        return this;
    }
    Coord move(Direction direction){ return move(direction, 1); }
    Coord move(Direction direction, int num){
        int moveX = 0, moveY = 0;
        switch(direction.get()){
            case Direction.R: moveX = num; moveY = 0;
                break;
            case Direction.UR: moveX = num; moveY = -num;
                break;
            case Direction.U: moveX = 0; moveY = -num;
                break;
            case Direction.UL: moveX = -num; moveY = -num;
                break;
            case Direction.L: moveX = -num; moveY = 0;
                break;
            case Direction.DL: moveX = -num; moveY = num;
                break;
            case Direction.D: moveX = 0; moveY = num;
                break;
            case Direction.DR:moveX = num; moveY = num;
                break;
        }
        x += moveX; y += moveY;
        alignPos();
        return this;
    }
    int getX(){ return x; }
    int getY(){ return y; }
    boolean samePos(Coord input){
        if (this.isOut() || input.isOut()) return false;
        return x == input.x && y == input.y;
    }
    boolean isOut(){
        if (modeX == MODE_CONSTRAINT && x == bx2){ return true; }
        if (modeY == MODE_CONSTRAINT && y == by2){ return true; }
        return false;
    }
    Coord forceOut(){
        if (modeX == MODE_CONSTRAINT){ x = bx2; }
        if (modeY == MODE_CONSTRAINT){ y = by2; }
        return this;
    }
    private void alignPos(){
        if (modeX == MODE_CYCLE){
            x = bx1 + Tools.remainder(x - bx1, bx2 - bx1);
        }
        else if(modeX == MODE_CONSTRAINT){
            if (x < bx1 || bx2 <= x) x = bx2;
        }
        if (modeY == MODE_CYCLE){
            y = by1 + Tools.remainder(y - by1, by2 - by1);
        }
        else if(modeY == MODE_CONSTRAINT){
            if (y < by1 || by2 <= y) y = by2;
        }
    }
}