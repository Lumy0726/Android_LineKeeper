package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;

import kr.co.lumylumy.linekeeper.timer.TimerAble;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-08-12.
 */

public class GameBoard implements TimerAble{
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

    //constructor.
    GameBoard(int width){
        tileSize = width / boardW;
        Tile.makeTileBitmap(tileSize);
        this.width = tileSize * boardW;
        this.height = tileSize * (boardH + 1);
        xPos = 0; yPos = 0;
        init();
    }
    GameBoard(int width, int height){
        tileSize = width / boardW;
        Tile.makeTileBitmap(tileSize);
        this.width = tileSize * boardW;
        this.height = tileSize * (boardH + 1);
        xPos = 0; yPos = height - this.height;
        init();
    }
    GameBoard(int xPos, int yPos, int width){
        this.xPos = xPos; this.yPos = yPos;
        tileSize = width / boardW;
        Tile.makeTileBitmap(tileSize);
        this.width = tileSize * boardW;
        height = tileSize * (boardH + 1);
        init();
    }
    void setPosition(int xPos, int yPos){ this.xPos = xPos; this.yPos = yPos; }
    void init(){
        //BitmapBoard.
        b_Board = Bitmap.createBitmap(width, height + tileSize, Bitmap.Config.ARGB_8888);
        c_Board = Tools.newCanvas(b_Board);
        rect_S[RECT_TOP1] = new Rect(0, 0, width, tileSize);
        (rect_S[RECT_TOP2] = new Rect(rect_S[RECT_TOP1])).offsetTo(0, tileSize);
        (rect_S[RECT_BOTTOM1] = new Rect(rect_S[RECT_TOP1])).offsetTo(0, height - tileSize);
        (rect_S[RECT_BOTTOM2] = new Rect(rect_S[RECT_TOP1])).offsetTo(0, height);
        rect_S[RECT_MIDDLE] = new Rect(0, 0, width, height);
        //Tile allocate.
        for (int x = 0; x < boardW; x++){
            for (int y = 0; y < boardH; y++){
                switch((int)(Math.random() * 4)){
                    case 0: tile_S[y][x] = new TileA(new Direction(Direction.R), x * tileSize + tileSize / 2, (y + 1) * tileSize + tileSize / 2);
                        break;
                    case 1: tile_S[y][x] = new TileA(new Direction(Direction.U), x * tileSize + tileSize / 2, (y + 1) * tileSize + tileSize / 2);
                        break;
                    case 2: tile_S[y][x] = new TileA(new Direction(Direction.L), x * tileSize + tileSize / 2, (y + 1) * tileSize + tileSize / 2);
                        break;
                    case 3: tile_S[y][x] = new TileA(new Direction(Direction.D), x * tileSize + tileSize / 2, (y + 1) * tileSize + tileSize / 2);
                        break;
                }
            }
        }
    }
    void draw(Canvas canvas){
        Paint paint = Tools.colorPaint(0, true);
        Tools.resetBitmap(c_Board ,MyColor.WHITE);
        c_Board.drawRect(rect_S[RECT_TOP1], paint);
        c_Board.drawRect(rect_S[RECT_BOTTOM2], paint);
        for (int x = 0; x < boardW; x++){
            for (int y = 0; y < boardH; y++){
                tile_S[y][x].draw(c_Board);
            }
        }
        c_Board.drawBitmap(b_Board, rect_S[RECT_TOP1], rect_S[RECT_BOTTOM1], null);
        c_Board.drawBitmap(b_Board, rect_S[RECT_BOTTOM2], rect_S[RECT_TOP2], null);
        c_Board.drawBitmap(b_Board, rect_S[RECT_BOTTOM1], rect_S[RECT_TOP1], null);
        c_Board.drawRect(rect_S[RECT_TOP1], Tools.colorPaint(MyColor.aColor(0x7f, MyColor.hsvColor(0, 80, 50))));
        canvas.drawBitmap(b_Board, rect_S[RECT_MIDDLE], Tools.rectWH(xPos, yPos, width, height), null);
    }
    @Override
    public void onTimer(int id, int sendNum) {
        for (int x = 0; x < boardW; x++){
            for (int y = 0; y < boardH; y++){
                tile_S[y][x].onTimer(id, sendNum);
            }
        }
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
    int p_State;
    //Canvas's position(tile's middle).
    int xPos, yPos;

    //graphic process.
    @Override
    public void onTimer(int id, int sendNum) {
        if (isProcessing) {

        }
    }
    void draw(Canvas canvas){ canvas.drawBitmap(outBitmap, xPos - outBitmap.getWidth() / 2, yPos - outBitmap.getHeight() / 2, null); }
    void directionToBitmap(){
        int di = direction.get();
        switch(di){
            case Direction.R: outBitmap = rotateBitmap[0];
                break;
            case Direction.U: outBitmap = rotateBitmap[P_LEVEL];
                break;
            case Direction.L: outBitmap = rotateBitmap[P_LEVEL * 2];
                break;
            case Direction.D: outBitmap = rotateBitmap[P_LEVEL * 3];
                break;
        }
    }

    //isConnect.
    abstract boolean isConnect(int input, int output);

    //initialize Tile Bitmap.
    static void makeTileBitmap(int tileSize){
        Tile.tileSize = tileSize;
        TileA.makeTileBitmap();
    }
}

class TileA extends Tile{ //Straight Line.
    //Bitmap.
    static Bitmap[] bitmap_S = new Bitmap[Tile.P_LEVEL * 4];
    TileA(Direction direction, int xPos, int yPos){
        this.xPos = xPos; this.yPos = yPos;
        int di = direction.get();
        //this.drection's default value is R.
        if (di == Direction.U || di == Direction.L || di == Direction.D) this.direction.set(di);
        rotateBitmap = bitmap_S;
        directionToBitmap();
    }
    @Override
    boolean isConnect(int input, int output) {
        return false;
    }
    static void makeTileBitmap(){
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        paint.setShader(new LinearGradient(0, 0, tileSize, tileSize, 0xff785496, 0xffddff00, Shader.TileMode.CLAMP));
        Rect rect = new Rect(0, 0, tileSize, tileSize);
        for (int loop1 = 0; loop1 < bitmap_S.length; loop1++){
            bitmap_S[loop1] = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap_S[loop1]);
            canvas.drawRect(rect, paint);
        }
    }
}

class Direction{
    static final int R = 0, UR = 1, U = 2, UL = 3, L = 4, DL = 5, D = 6, DR = 7;
    int direction;
    Direction(){ set(R); }
    Direction(int direction){ set(direction); }
    void set(int direction){
        if (0 <= direction && direction < 8) this.direction = direction;
    }
    int get(){ return direction; }
}

class Coord{
    static final int MODE_NONE = 0, MODE_CYCLE = 1, MODE_CONSTRAINT = 2;
    private int x, y;
    private int cx1 = 0xf0_00_00_00, cy1 = 0xf0_00_00_00, cx2 = 0x0f_ff_ff_ff, cy2 = 0x0f_ff_ff_ff;
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
        cx1 = input.cx1; cx2 = input.cx2;
        cy1 = input.cy1; cy2 = input.cy2;
    }
    void setMode(int mode){ setMode(mode, mode); }
    void setMode(int modeX, int modeY){
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
    }
    void setConstraint(int cx1, int cx2, int cy1, int cy2){
        if (cx1 < cx2){ this.cx1 = cx1; this.cx2 = cx2; }
        if (cy1 < cy2){ this.cy1 = cy1; this.cy2 = cy2; }
        alignPos();
    }
    void setPos(int x, int y){
        this.x = x; this.y = y;
        alignPos();
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
        if (modeX == MODE_CONSTRAINT && x == cx2){ return true; }
        if (modeY == MODE_CONSTRAINT && y == cy2){ return true; }
        return false;
    }
    private void alignPos(){
        if (modeX == MODE_CYCLE){
            x = cx1 + Tools.remainder(x - cx1, cx2 - cx1);
        }
        else if(modeX == MODE_CONSTRAINT){
            if (x < cx1 || cx2 <= x) x = cx2;
        }
        if (modeY == MODE_CYCLE){
            y = cy1 + Tools.remainder(y - cy1, cy2 - cy1);
        }
        else if(modeY == MODE_CONSTRAINT){
            if (y < cy1 || cy2 <= y) y = cy2;
        }
    }
}