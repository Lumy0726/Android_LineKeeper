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

import static android.R.attr.id;
import static kr.co.lumylumy.linekeeper.log.LogSystem.androidLog;

/**
 * Created by LMJ on 2017-08-12.
 */

public class GameBoard implements TimerAble, TouchEvent {
    //
    static final int boardW = 6, boardH= 8;
    //size.
    int xPos, yPos, width, height, tileSize;
    //tiles
    Tile[][] tile_S = new Tile[boardH][boardW];
    //Gameboard Bitmap.
    Bitmap b_Board;
    Canvas c_Board;
    static final int RECT_TOP1 = 0, RECT_TOP2 = 1, RECT_BOTTOM1 = 2, RECT_BOTTOM2 = 3, RECT_MIDDLE = 4;
    Rect[] rect_S = new Rect[5];
    //touchInput.
    ArrayList<TouchInfo> touchInput = new ArrayList<>();
    static final int TOUCHNUM_MAX = 3;
    int touchNum;
    int[] touchId = new int[TOUCHNUM_MAX];
    float[] touchX = new float[TOUCHNUM_MAX];
    float[] touchY = new float[TOUCHNUM_MAX];
    //
    Coord tilePos;

    //constructor.
    GameBoard(int width){
        tileSize = width / boardW;
        this.width = tileSize * boardW;
        this.height = tileSize * (boardH + 1);
        xPos = 0; yPos = 0;
        init();
    }
    GameBoard(int width, int height){
        tileSize = width / boardW;
        this.width = tileSize * boardW;
        this.height = tileSize * (boardH + 1);
        xPos = 0; yPos = height - this.height;
        init();
    }
    GameBoard(int xPos, int yPos, int width){
        this.xPos = xPos; this.yPos = yPos;
        tileSize = width / boardW;
        this.width = tileSize * boardW;
        height = tileSize * (boardH + 1);
        init();
    }
    void setPosition(int xPos, int yPos){ this.xPos = xPos; this.yPos = yPos; }
    void init(){
        //Tile initialize.
        Tile.makeTileBitmap(tileSize);
        //BitmapBoard.
        b_Board = Bitmap.createBitmap(width, height + tileSize, Bitmap.Config.ARGB_8888);
        c_Board = Tools.newCanvas(b_Board);
        rect_S[RECT_TOP1] = new Rect(0, 0, width, tileSize);
        (rect_S[RECT_TOP2] = new Rect(rect_S[RECT_TOP1])).offsetTo(0, tileSize);
        (rect_S[RECT_BOTTOM1] = new Rect(rect_S[RECT_TOP1])).offsetTo(0, height - tileSize);
        (rect_S[RECT_BOTTOM2] = new Rect(rect_S[RECT_TOP1])).offsetTo(0, height);
        rect_S[RECT_MIDDLE] = new Rect(0, 0, width, height);
        //Tile allocate.
        Coord coord = new Coord(), coord1;
        coord.setMode(Coord.MODE_CONSTRAINT, Coord.MODE_CYCLE);
        coord.setBorder(0, width, tileSize, height);
        for (int x = 0; x < boardW; x++){
            for (int y = 0; y < boardH; y++){
                coord1 = new Coord(coord).setPos(x * tileSize + tileSize / 2, (y + 1) * tileSize + tileSize / 2);
                Direction direction = null;
                switch((int)(Math.random() * 4)){
                    case 0: direction = new Direction(Direction.R); break;
                    case 1: direction = new Direction(Direction.U); break;
                    case 2: direction = new Direction(Direction.L); break;
                    case 3: direction = new Direction(Direction.D); break;
                }
                switch((int)(Math.random() * 3)){
                    case 0:
                    case 1:
                        tile_S[y][x] = new TileA(direction, coord1);
                        break;
                    case 2:
                        tile_S[y][x] = new TileB(direction, coord1);
                        break;
                }
            }
        }
        //TilePos Coord setting.
        tilePos = new Coord();
        tilePos.setMode(Coord.MODE_CONSTRAINT, Coord.MODE_CONSTRAINT);
        tilePos.setBorder(0, boardW, 0, boardH + 1);
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
        c_Board.drawRect(rect_S[RECT_TOP1], paint);
        c_Board.drawRect(rect_S[RECT_BOTTOM2], paint);
        //androidLog(String.format("GameBoard-draw-initialize: %5.2f", time_I.getTimeAv()));
        //time_D.reset();
        for (int x = 0; x < boardW; x++){
            for (int y = 0; y < boardH; y++){
                tile_S[y][x].draw(c_Board);
            }
        }
        //androidLog(String.format("GameBoard-draw-tileDraw: %5.2f", time_D.getTimeAv()));
        //time_C.reset();
        c_Board.drawBitmap(b_Board, rect_S[RECT_TOP1], rect_S[RECT_BOTTOM1], null);
        c_Board.drawBitmap(b_Board, rect_S[RECT_BOTTOM2], rect_S[RECT_TOP2], null);
        c_Board.drawBitmap(b_Board, rect_S[RECT_BOTTOM1], rect_S[RECT_TOP1], null);
        c_Board.drawRect(rect_S[RECT_TOP1], Tools.colorPaint(MyColor.aColor(0x7f, MyColor.hsvColor(0, 80, 50))));
        //androidLog(String.format("GameBoard-draw-timeCycle: %5.2f", time_C.getTimeAv()));
        //time_M.reset();
        canvas.drawBitmap(b_Board, rect_S[RECT_MIDDLE], Tools.rectWH(xPos, yPos, width, height), null);
        //androidLog(String.format("GameBoard-draw-main: %5.2f", time_M.getTimeAv()));
    }
    //
    int getTileY(Coord input){ return Tools.remainder(input.getY() - 1, boardH); }
    Tile getTile(Coord input){ return tile_S[getTileY(input)][input.getX()]; }
    void tileSwap(Coord input1, Coord input2){
        Tile temp = getTile(input1);
        tile_S[getTileY(input1)][input1.getX()] = getTile(input2);
        tile_S[getTileY(input2)][input2.getX()] = temp;
    }
    //timer, touch.
    @Override
    public void onTimer(int id, int sendNum) {
        for (int x = 0; x < boardW; x++){
            for (int y = 0; y < boardH; y++){
                tile_S[y][x].onTimer(id, sendNum);
            }
        }
    }

    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        int loop1;
        boolean flag;
        TouchInfo t_Info = null;
        Iterator<TouchInfo> it;
        switch(touchInfo.action){
            case TouchInfo.DOWN:
                touchInput.add(touchInfo);
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
                    //save position.
                    tilePos.setPos((int) t_Info.x / tileSize, (int) t_Info.y / tileSize);
                    //process touch.
                    int dx = Tools.floorByDiv((int)touchInfo.x, tileSize) / tileSize - tilePos.getX();
                    int dy = Tools.floorByDiv((int)touchInfo.y, tileSize) / tileSize - tilePos.getY();
                    Tile tile = getTile(tilePos);
                    if (dx == 0 && dy == 0) {//rotate process.
                        if (tile.processAble(Tile.P_ROTATE_R)) tile.startProcess(Tile.P_ROTATE_L);
                    } else if (dx == 0 || dy == 0) {//move process.
                        //get direction.
                        Direction di = new Direction(), diM;
                        if (dx > 0) {
                            di.set(Direction.R);
                        } else if (dx < 0) {
                            di.set(Direction.L);
                        } else if (dy > 0) {
                            di.set(Direction.D);
                        } else {
                            di.set(Direction.U);
                        }
                        diM = new Direction(di).mirror();
                        //get movePos.
                        Coord movePos = new Coord(tilePos);
                        movePos.move(di);
                        //process.
                        if (!movePos.isOut()) {
                            Tile moveTile = getTile(movePos);
                            if (tile.processAble(di) && moveTile.processAble(diM)) {
                                //tile can move.
                                tile.startProcess(di);
                                moveTile.startProcess(diM);
                                tileSwap(tilePos, movePos);
                            }
                        }
                    }
                }
                break;
            case TouchInfo.CANCEL:
                touchInput.clear();
                break;
        }
        return true;
    }
}

abstract class Tile implements TimerAble{
    //direction.
    Direction direction = new Direction(Direction.R);
    //tilesize.
    static int tileSize = 0;
    //
    boolean moveAble = true;
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
        Coord movePos = new Coord(pos);
        return !movePos.move(direction).isOut();
    }
    void startProcess(int content) {
        switch (content) {
            case P_ROTATE_R:
            case P_ROTATE_L:
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
    void draw(Canvas canvas){ canvas.drawBitmap(outBitmap, pos.getX() - outBitmap.getWidth() / 2, pos.getY() - outBitmap.getHeight() / 2, null); }
    void directionToBitmap(){
        int di = direction.get();
        int rotateValue = 0, angle = P_LEVEL * 4;
        if (isProcessing){
            if (p_Content == P_ROTATE_L) rotateValue = p_Level;
            if (p_Content == P_ROTATE_R) rotateValue = -p_Level;
        }
        switch(di){
            case Direction.R: outBitmap = rotateBitmap[Tools.remainder(rotateValue, angle)];
                break;
            case Direction.U: outBitmap = rotateBitmap[Tools.remainder(P_LEVEL + rotateValue, angle)];
                break;
            case Direction.L: outBitmap = rotateBitmap[Tools.remainder(P_LEVEL * 2 + rotateValue, angle)];
                break;
            case Direction.D: outBitmap = rotateBitmap[Tools.remainder(P_LEVEL * 3 + rotateValue, angle)];
                break;
        }
    }

    //
    abstract Bitmap[] getRotateBitmap();
    //isConnect.
    abstract boolean isConnect(int input, int output);

    //initialize Tile Bitmap.
    static void makeTileBitmap(int tileSize){
        Tile.tileSize = tileSize;
        moveSpeed = tileSize / P_LEVEL;
        TileA.makeTileBitmap();
        TileB.makeTileBitmap();
    }
}

class TileA extends Tile{ //Straight Line.
    //Bitmap.
    static Bitmap[] bitmap_S = new Bitmap[Tile.P_LEVEL * 4];
    TileA(Direction direction, Coord pos){ super(direction, pos); }
    @Override
    boolean isConnect(int input, int output) {
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

class TileB extends Tile{ //Straight Line.
    //Bitmap.
    static Bitmap[] bitmap_S = new Bitmap[Tile.P_LEVEL * 4];
    TileB(Direction direction, Coord pos){ super(direction, pos); }
    @Override
    boolean isConnect(int input, int output) {
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
    boolean isOut(){
        if (modeX == MODE_CONSTRAINT && x == bx2){ return true; }
        if (modeY == MODE_CONSTRAINT && y == by2){ return true; }
        return false;
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