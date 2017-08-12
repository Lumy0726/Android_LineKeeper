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
    //
    Bitmap b_DownArrow;

    //constructor.
    GameBoard(int xPos, int yPos, int width){
        this.xPos = xPos; this.yPos = yPos;
        tileSize = width / (boardW + 1);
        Tile.makeTileBitmap(tileSize);
        this.width = tileSize * (boardW + 1);
        height = tileSize * (boardH + 1);
        init();
    }
    void init(){
        for (int x = 0; x < boardW; x++){
            for (int y = 0; y < boardH; y++){
                tile_S[y][x] = new TileA(new Direction(Direction.U), xPos + x * tileSize + tileSize / 2, yPos + y * tileSize + tileSize / 2);
            }
        }
        //DownArrow Bitmap.
        Paint paint = Tools.colorPaint(MyColor.RED);
        Path path = new Path();
        b_DownArrow = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        int arrowWidth = tileSize / 8;
        int margin = tileSize / 6;
            //path
        path.moveTo(tileSize / (float)2, margin);
        path.rQuadTo(arrowWidth / (float)2, 0, arrowWidth / (float)2, arrowWidth / (float)2);
        path.lineTo((tileSize + arrowWidth) / (float)2, tileSize - margin - arrowWidth);
        float dx = (tileSize - arrowWidth) / (float)2 - margin;
        float dy = margin + arrowWidth - tileSize / (float)2;
        path.rLineTo(dx, dy);
        Tools.smoothQuad(path,
                tileSize - margin, tileSize / (float)2,
                (tileSize + arrowWidth) / (float)2, tileSize - margin,
                0, -1, (int)dx, (int)dy
                );
        Tools.smoothQuad(path,
                (tileSize + arrowWidth) / (float)2, tileSize - margin,
                (tileSize - arrowWidth) / (float)2, tileSize - margin,
                (int)dx, (int)dy, -(int)dx, (int)dy
                );
        Tools.smoothQuad(path,
                (tileSize - arrowWidth) / (float)2, tileSize - margin,
                margin, tileSize / (float)2,
                -(int)dx, (int)dy, 0, -1
        );
        path.lineTo((tileSize - arrowWidth) / (float)2, tileSize - margin - arrowWidth);
        path.lineTo((tileSize - arrowWidth) / (float)2, margin + arrowWidth / (float)2);
        path.rQuadTo(0, -arrowWidth / (float)2, arrowWidth / (float)2, -arrowWidth / (float)2);
        path.close();
        Tools.newCanvas(b_DownArrow).drawPath(path, paint);
        //END: DownArrow Bitmap.
    }
    void draw(Canvas canvas){
        for (int x = 0; x < boardW; x++){
            for (int y = 0; y < boardH; y++){
                tile_S[y][x].draw(canvas);
            }
        }
        int arrowX = xPos + tileSize * boardW;
        for (int y = 0; y < boardH; y++){
            canvas.drawBitmap(b_DownArrow, arrowX, yPos + tileSize * y, null);
        }
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