package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

import kr.co.lumylumy.linekeeper.timer.TimerAble;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-08-26.
 */

abstract class Tile implements TimerAble {
    //tileUpdate Class.
    interface TileUpdateReceiver{ void tileUpdate(); }
    TileUpdateReceiver tileUpdateClass;
    //direction.
    Direction tileDirection = new Direction(Direction.R);
    //tilesize.
    static int tileSize = 0;
    static int LineWidth = 0;
    //if moveAble is false, draw this.
    static Bitmap b_UnAbleMove;
    //connect
    boolean lineR = false, lineU = false, lineL = false, lineD = false;
    boolean mustConnect = false;
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
    Tile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){
        this.tileUpdateClass = tileUpdateClass;
        this.pos = new Coord(pos);
        int di = direction.get();
        //this.drection's default value is R.
        if (di == Direction.U || di == Direction.L || di == Direction.D) tileDirection.set(di);
        rotateBitmap = getRotateBitmap();
        directionToBitmap();
        mustConnect = false;
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
        if (!moveAble || isProcessing) return;
        switch (content) {
            case P_ROTATE_R:
            case P_ROTATE_L:
                isProcessing = true;
                p_Content = content;
                p_Level = 0;
                deleteConnect();
                tileUpdateClass.tileUpdate();
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
            deleteConnect();
            tileUpdateClass.tileUpdate();
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
                    tileDirection.rotate((p_Content == P_ROTATE_L)?true:false, 2);
                    directionToBitmap();
                    tileUpdateClass.tileUpdate();
                }
            }
            else {//moveing process.
                if (isProcessing){
                    pos.move(p_Direction, moveSpeed);
                }
                else {
                    pos = backupPos.move(p_Direction, tileSize);
                    tileUpdateClass.tileUpdate();
                }
            }
        }
    }
    void draw(Canvas canvas){
        drawLine(canvas);
        canvas.drawBitmap(outBitmap, pos.getX() - outBitmap.getWidth() / 2, pos.getY() - outBitmap.getHeight() / 2, null);
        if (!moveAble) canvas.drawBitmap(b_UnAbleMove, pos.getX() - tileSize / 2, pos.getY() - tileSize / 2, null);
    }
    abstract void drawLine(Canvas canvas);
    void directionToBitmap(){
        int di = tileDirection.get();
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
    //Connect.
    void deleteConnect(){ lineR = lineU = lineL = lineD = false; }
    abstract Direction[] lineFlow(Direction di);
    abstract boolean isLine(Direction di);
    abstract boolean isConnectAll();

    //initialize Tile Bitmap.
    static void makeTileBitmap(int tileSize){
        //size, speed.
        Tile.tileSize = tileSize;
        LineWidth = tileSize / 5;
        moveSpeed = tileSize / P_LEVEL;
        //b_UnAbleMove.
        {
            float gradientWidth = tileSize / (float)8;
            int[] color = new int[]{0, 0, MyColor.aColor(0xdd, MyColor.BLACK)};
            float[] gradientPos = new float[]{0f, 0.7f, 1f};
            b_UnAbleMove = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            paint.setShader(
                    new LinearGradient(0, 0,
                            gradientWidth, gradientWidth,
                            color, gradientPos,
                            Shader.TileMode.MIRROR)
            );
            Tools.newCanvas(b_UnAbleMove).drawRect(0, 0, tileSize, tileSize, paint);
        }
        //Tile's Bitmap.
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
    Tile_EX(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){//constructor.
        super(tileUpdateClass, direction, pos);
        mustConnect = true;//set whether require connecting(optional).
    }
    @Override
    void drawLine(Canvas canvas) {//if the line is flow, draw the line (it will be covered by tile's bitmap).
        if (!isProcessing){

        }
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    Direction[] lineFlow(Direction di) {//line flow process. if line is start to flow by this, it will return direction.
        if (!isProcessing){
            ;
        }
        return new Direction[0];
    }
    @Override
    boolean isLine(Direction di) {//check whether line exist tile's direction.
        //overriding tile's line.
        return false;
    }
    @Override
    boolean isConnectAll() {//check whether tile's every line is connected.
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
    Tile_STRAIGHT(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){ super(tileUpdateClass, direction, pos); }
    @Override
    void drawLine(Canvas canvas) {//if the line is flow, draw the line (it will be covered by tile's bitmap).
        if (!isProcessing && isConnectAll()){
            int tileSize_2 = tileSize / 2;
            canvas.drawRect(
                    pos.getX() - tileSize_2, pos.getY() - tileSize_2,
                    pos.getX() + tileSize_2, pos.getY() + tileSize_2, Tools.colorPaint(MyColor.BLUE, true)
            );
        }
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    Direction[] lineFlow(Direction di) {//line flow process. if line is start to flow by this, it will return direction.
        if (isLine(di)){
            switch(di.get()){
                case Direction.R:
                    if (!lineL){
                        lineR = lineL = true;
                        return new Direction[]{new Direction(Direction.L)};
                    }
                    break;
                case Direction.U:
                    if (!lineD){
                        lineU = lineD = true;
                        return new Direction[]{new Direction(Direction.D)};
                    }
                    break;
                case Direction.L:
                    if (!lineR){
                        lineR = lineL = true;
                        return new Direction[]{new Direction(Direction.R)};
                    }
                    break;
                case Direction.D:
                    if (!lineU){
                        lineU = lineD = true;
                        return new Direction[]{new Direction(Direction.U)};
                    }
                    break;
            }
        }
        return new Direction[0];
    }
    @Override
    boolean isLine(Direction di) {//check whether line exist tile's direction.
        Direction diMirror = new Direction(di).mirror();
        return tileDirection.equals(di) || tileDirection.equals(diMirror);
    }
    @Override
    boolean isConnectAll() {//check whether tile's every line is connected.
        return lineR || lineU;
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
    TileA(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){ super(tileUpdateClass, direction, pos); }
    @Override
    void drawLine(Canvas canvas) { ; }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    Direction[] lineFlow(Direction di) { return new Direction[0]; }
    @Override
    boolean isLine(Direction di) { return false; }
    @Override
    boolean isConnectAll() { return true; }
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
    TileB(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){ super(tileUpdateClass, direction, pos); }
    @Override
    void drawLine(Canvas canvas) { ; }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    Direction[] lineFlow(Direction di) { return new Direction[0]; }
    @Override
    boolean isLine(Direction di) { return false; }
    @Override
    boolean isConnectAll() { return true; }
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