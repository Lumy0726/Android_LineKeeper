package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

import java.util.Random;

import kr.co.lumylumy.linekeeper.timer.TimerAble;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-08-26.
 */

//some helper Interface, Class.
interface TileUpdateReceiver{ void tileUpdate(); }
interface TileAllocator{

    //probability.
    int PROBABILITY_DEFAULT = 100;

    int P_NOFUNCTION_TILE = 5;
    int P_CURVE_RESERVED = 5;

    int P_STRAIGHT = PROBABILITY_DEFAULT;
    int P_STRAIGHT_MUST_LOW = PROBABILITY_DEFAULT / 3;
    int P_STRAIGHT_MUST_HIGH = PROBABILITY_DEFAULT;

    int P_CURVE_START = PROBABILITY_DEFAULT * 4 / 3;
    int P_CURVE_MUST_LOW = PROBABILITY_DEFAULT / 3;
    int P_CURVE_LAST = PROBABILITY_DEFAULT * 4 / 3;
    int P_CURVE_MUST_HIGH = PROBABILITY_DEFAULT * 3 / 2;

    int P_PLUS = PROBABILITY_DEFAULT / 3;
    int P_PLUS_MUST_LOW = PROBABILITY_DEFAULT / 10;
    int P_PLUS_MUST_HIGH = PROBABILITY_DEFAULT / 5;

    int P_TRIPLE = PROBABILITY_DEFAULT;
    int P_TRIPLE_MUST_LOW = PROBABILITY_DEFAULT / 8;
    int P_TRIPLE_MUST_HIGH = PROBABILITY_DEFAULT / 3;

    //
    int getProbability(int level);
    Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos);
}
abstract class TA_LinearProbability implements TileAllocator{
    static int linear(int level, int low, int addProbability, int addLevel){ return low + (level - 1) * addProbability / addLevel; }
    static int linearMaxLevel(int level, int low, int high, int maxLevel){
        if (level < maxLevel){
            return low + (high - low) * (level - 1) / (maxLevel - 1);
        }
        else return high;
    }
}
//Tile class
abstract class Tile implements TimerAble {
    //
    TileUpdateReceiver tileUpdateClass;
    //direction.
    Direction tileDirection = new Direction(Direction.R);
    //tilesize.
    static int tileSize = 0, tileSize_2 = 0;
    static int lineWidth = 0;
    static int tilePos1 = 0, tilePos2 = 0;
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
    //some color.
    static final int COLOR_LINE = MyColor.BLUE;
    static final int COLOR1 = MyColor.hsvColor(40, 100, 80);
    static final int COLOR2 = MyColor.hsvColor(24, 100, 50);

    //score
    interface ScoreValue{
        int SCORE_DEFAULT = 10;

        int STRAIGHT = SCORE_DEFAULT;
        int STRAIGHT_ROTATE = SCORE_DEFAULT * 2;

        int CURVE = SCORE_DEFAULT;

        int PLUS = SCORE_DEFAULT * 4;
        int PLUS_MUST = SCORE_DEFAULT * 8;

        int TRIPLE = SCORE_DEFAULT * 2;
        int TRIPLE_MUST = SCORE_DEFAULT * 4;
    }

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
    abstract int connectScore();

    //initialize Tile Bitmap.
    static void tileInitialze(int tileSize){
        //size, speed.
        Tile.tileSize = tileSize;
        tileSize_2 = tileSize / 2;
        lineWidth = tileSize / 5;
        tilePos1 = (tileSize - lineWidth) / 2;
        tilePos2 = (tileSize + lineWidth) / 2;
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
        Tile_STRAIGHT.makeTileBitmap();
        Tile_STRAIGHT_MUST.makeTileBitmap();
        Tile_CURVE.makeTileBitmap();
        Tile_CURVE_MUST.makeTileBitmap();
        Tile_PLUS.makeTileBitmap();
        Tile_PLUS_MUST.makeTileBitmap();
        Tile_TRIPLE.makeTileBitmap();
        Tile_TRIPLE_MUST.makeTileBitmap();
        TileA.makeTileBitmap();
        TileB.makeTileBitmap();
        //TileAllocatorTable.
        tileAllocatorTable = new TileAllocator[]{
                Tile_STRAIGHT.getTileAllocator(),
                Tile_STRAIGHT_MUST.getTileAllocator(),
                Tile_CURVE.getTileAllocator(),
                Tile_CURVE_MUST.getTileAllocator(),
                Tile_PLUS.getTileAllocator(),
                Tile_PLUS_MUST.getTileAllocator(),
                Tile_TRIPLE.getTileAllocator(),
                Tile_TRIPLE_MUST.getTileAllocator(),
        };
    }

    //tileAllocator.
    /*
    interface Probability { int get(int level); }
    interface TileAllocator {
        int probabilityDefault = 100;
        int getProbability(int level);
        Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos);
    }
    */
    static TileAllocator[] tileAllocatorTable;
    static Random random = new Random();
    static void tileAllocSeedReset(){ random = new Random(); }
    static void tileAllocSeedReset(long seed){ random.setSeed(seed); }
    static Tile newTile(TileUpdateReceiver tileUpdateClass, Direction tileDirection, Coord tileOutputPos, int level){
        //allocate non-functionTile block.
        if (random.nextInt(TileAllocator.P_NOFUNCTION_TILE) == 0){
            if (random.nextInt(2) == 0) return new TileA(tileUpdateClass, tileDirection, tileOutputPos);
            return new TileB(tileUpdateClass, tileDirection, tileOutputPos);
        }
        //allocate reserved curve tile.
        if (random.nextInt(TileAllocator.P_CURVE_RESERVED) == 0){ return new Tile_CURVE(tileUpdateClass, tileDirection, tileOutputPos); }
        //allocate.
        int tileIndex = 0, probabilitySum = 0;
        int[] probabilityTable = new int[tileAllocatorTable.length];
        for (int loop1 = 0; loop1 < tileAllocatorTable.length; loop1++){ probabilitySum += (probabilityTable[loop1] = tileAllocatorTable[loop1].getProbability(level)); }
        for (
                int tempSum = 0, value = random.nextInt(probabilitySum);
                tileIndex < tileAllocatorTable.length;
                tileIndex++
                ){
            if (tempSum <= value && value < tempSum + probabilityTable[tileIndex]){
                return tileAllocatorTable[tileIndex].newTile(tileUpdateClass, tileDirection, tileOutputPos);
            }
            tempSum += probabilityTable[tileIndex];
        }
        return null;
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
        if (!isProcessing && isConnectAll()){//for dual line, need more checking about isConnect.
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
    @Override
    int connectScore() {//return score.
        return 0;
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        //draw tile bitmap (Direction U)
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TileAllocator(){
            @Override
            public int getProbability(int level) { return PROBABILITY_DEFAULT; }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_EX(tileUpdateClass, direction, pos); }
        };
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
            canvas.drawRect(
                    pos.getX() - tileSize_2, pos.getY() - tileSize_2,
                    pos.getX() + tileSize_2, pos.getY() + tileSize_2, Tools.colorPaint(COLOR_LINE, true)
            );
        }
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    Direction[] lineFlow(Direction di) {//line flow process. if line is start to flow by this, it will return direction.
        if (!isProcessing){
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
    @Override
    int connectScore() {//return score.
        if (isConnectAll()){
            switch(tileDirection.get()){
                case Direction.R:
                case Direction.L:
                    return ScoreValue.STRAIGHT_ROTATE;
                default:
                    return ScoreValue.STRAIGHT;
            }
        }
        return 0;
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tileBitmap);
        //draw tile bitmap (Direction U)
        Tools.resetBitmap(canvas, COLOR1);
        canvas.drawRect((tileSize - lineWidth) / (float)2, 0, (tileSize + lineWidth) / (float)2, tileSize ,Tools.colorPaint(0, true));
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TileAllocator(){
            @Override
            public int getProbability(int level) { return P_STRAIGHT; }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_STRAIGHT(tileUpdateClass, direction, pos); }
        };
    }
}
class Tile_STRAIGHT_MUST extends Tile_STRAIGHT{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_STRAIGHT_MUST(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){//constructor.
        super(tileUpdateClass, direction, pos);
        mustConnect = true;
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tileBitmap);
        //draw tile bitmap (Direction U)
        Tools.resetBitmap(canvas, COLOR2);
        canvas.drawRect((tileSize - lineWidth) / (float)2, 0, (tileSize + lineWidth) / (float)2, tileSize ,Tools.colorPaint(0, true));
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TA_LinearProbability(){
            @Override
            public int getProbability(int level) { return linearMaxLevel(level, P_STRAIGHT_MUST_LOW, P_STRAIGHT_MUST_HIGH, GameBoard.MAXLEVEL_DEFAULT); }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_STRAIGHT_MUST(tileUpdateClass, direction, pos); }
        };
    }
}
class Tile_CURVE extends Tile{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_CURVE(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){ super(tileUpdateClass, direction, pos); }
    @Override
    void drawLine(Canvas canvas) {//if the line is flow, draw the line (it will be covered by tile's bitmap).
        if (!isProcessing && isConnectAll()){
            canvas.drawRect(
                    pos.getX() - tileSize_2, pos.getY() - tileSize_2,
                    pos.getX() + tileSize_2, pos.getY() + tileSize_2, Tools.colorPaint(COLOR_LINE, true)
            );
        }
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    Direction[] lineFlow(Direction di) {//line flow process. if line is start to flow by this, it will return direction.
        if (!isProcessing){
            if (isLine(di)){
                if (tileDirection.equals(di)){
                    switch(di.get()){
                        case Direction.R:
                            if (!lineU){
                                lineU = lineR = true;
                                return new Direction[]{new Direction(Direction.U)};
                            }
                            break;
                        case Direction.U:
                            if (!lineL){
                                lineL = lineU = true;
                                return new Direction[]{new Direction(Direction.L)};
                            }
                            break;
                        case Direction.L:
                            if (!lineD){
                                lineD = lineL = true;
                                return new Direction[]{new Direction(Direction.D)};
                            }
                            break;
                        case Direction.D:
                            if (!lineR){
                                lineR = lineD = true;
                                return new Direction[]{new Direction(Direction.R)};
                            }
                            break;
                    }
                }
                else {
                    switch(di.get()){
                        case Direction.R:
                            if (!lineD){
                                lineD = lineR = true;
                                return new Direction[]{new Direction(Direction.D)};
                            }
                            break;
                        case Direction.U:
                            if (!lineR){
                                lineR = lineU = true;
                                return new Direction[]{new Direction(Direction.R)};
                            }
                            break;
                        case Direction.L:
                            if (!lineU){
                                lineU = lineL = true;
                                return new Direction[]{new Direction(Direction.U)};
                            }
                            break;
                        case Direction.D:
                            if (!lineL){
                                lineL = lineD = true;
                                return new Direction[]{new Direction(Direction.L)};
                            }
                            break;
                    }
                }
            }
        }
        return new Direction[0];
    }
    @Override
    boolean isLine(Direction di) {//check whether line exist tile's direction.
        Direction di2 = new Direction(di).rotate(false, 2);
        return tileDirection.equals(di) || tileDirection.equals(di2);
    }
    @Override
    boolean isConnectAll() {//check whether tile's every line is connected.
        return lineR || lineL;
    }
    @Override
    int connectScore() {//return score.
        if (isConnectAll()) return ScoreValue.CURVE;
        return 0;
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tileBitmap);
        //draw tile bitmap (Direction U)
        Tools.resetBitmap(canvas, COLOR1);
        canvas.drawCircle(0, 0, tilePos2, Tools.colorPaint(0, true));
        canvas.drawCircle(0, 0, tilePos1, Tools.colorPaint(COLOR1));
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TA_LinearProbability(){
            @Override
            public int getProbability(int level) { return linearMaxLevel(level, P_CURVE_START, P_CURVE_LAST, GameBoard.MAXLEVEL_DEFAULT); }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_CURVE(tileUpdateClass, direction, pos); }
        };
    }
}
class Tile_CURVE_MUST extends Tile_CURVE{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_CURVE_MUST(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){
        super(tileUpdateClass, direction, pos);
        mustConnect = true;
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tileBitmap);
        //draw tile bitmap (Direction U)
        Tools.resetBitmap(canvas, COLOR2);
        canvas.drawCircle(0, 0, tilePos2, Tools.colorPaint(0, true));
        canvas.drawCircle(0, 0, tilePos1, Tools.colorPaint(COLOR2));
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TA_LinearProbability(){
            @Override
            public int getProbability(int level) { return linearMaxLevel(level, P_CURVE_MUST_LOW, P_CURVE_MUST_HIGH, GameBoard.MAXLEVEL_DEFAULT); }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_CURVE_MUST(tileUpdateClass, direction, pos); }
        };
    }
}
class Tile_PLUS extends Tile{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_PLUS(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){ super(tileUpdateClass, direction, pos); }
    @Override
    void drawLine(Canvas canvas) {//if the line is flow, draw the line (it will be covered by tile's bitmap).
        if (!isProcessing && isConnectAll()){//for dual line, need more checking about isConnect.
            canvas.drawRect(
                    pos.getX() - tileSize_2, pos.getY() - tileSize_2,
                    pos.getX() + tileSize_2, pos.getY() + tileSize_2, Tools.colorPaint(COLOR_LINE, true)
            );
        }
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    Direction[] lineFlow(Direction di) {//line flow process. if line is start to flow by this, it will return direction.
        if (!(isProcessing || isConnectAll())){
            lineR = lineU = lineL = lineD = true;
            Direction[] returnValue = new Direction[3];
            Direction tempDi = new Direction(Direction.R);
            for (int loop1 = 0, index = 0; loop1 < 4; loop1++){
                if (!di.equals(tempDi)) returnValue[index++] = new Direction(tempDi);
                tempDi.rotate(true, 2);
            }
            return returnValue;
        }
        return new Direction[0];
    }
    @Override
    boolean isLine(Direction di) {//check whether line exist tile's direction.
        return true;
    }
    @Override
    boolean isConnectAll() {//check whether tile's every line is connected.
        return lineR;
    }
    @Override
    int connectScore() {//return score.
        return ScoreValue.PLUS;
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tileBitmap);
        Paint paint = Tools.colorPaint(COLOR1);
        canvas.drawRect(0, 0, tilePos1, tilePos1, paint);
        canvas.drawRect(tilePos2, 0, tileSize, tilePos1, paint);
        canvas.drawRect(0, tilePos2, tilePos1, tileSize, paint);
        canvas.drawRect(tilePos2, tilePos2, tileSize, tileSize, paint);
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TileAllocator(){
            @Override
            public int getProbability(int level) { return P_PLUS; }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_PLUS(tileUpdateClass, direction, pos); }
        };
    }
}
class Tile_PLUS_MUST extends Tile_PLUS{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_PLUS_MUST(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){
        super(tileUpdateClass, direction, pos);
        mustConnect = true;
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    int connectScore() {//return score.
        return ScoreValue.PLUS_MUST;
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        int pos1 = (tileSize - lineWidth) / 2, pos2 = (tileSize + lineWidth) / 2;
        Canvas canvas = Tools.newCanvas(tileBitmap);
        Paint paint = Tools.colorPaint(COLOR2);
        canvas.drawRect(0, 0, tilePos1, tilePos1, paint);
        canvas.drawRect(tilePos2, 0, tileSize, tilePos1, paint);
        canvas.drawRect(0, tilePos2, tilePos1, tileSize, paint);
        canvas.drawRect(tilePos2, tilePos2, tileSize, tileSize, paint);
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TA_LinearProbability(){
            @Override
            public int getProbability(int level) { return linearMaxLevel(level, P_PLUS_MUST_LOW, P_PLUS_MUST_HIGH, GameBoard.MAXLEVEL_DEFAULT); }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_PLUS_MUST(tileUpdateClass, direction, pos); }
        };
    }
}
class Tile_TRIPLE extends Tile{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_TRIPLE(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){ super(tileUpdateClass, direction, pos); }
    @Override
    void drawLine(Canvas canvas) {//if the line is flow, draw the line (it will be covered by tile's bitmap).
        if (!isProcessing && isConnectAll()){
            canvas.drawRect(
                    pos.getX() - tileSize_2, pos.getY() - tileSize_2,
                    pos.getX() + tileSize_2, pos.getY() + tileSize_2, Tools.colorPaint(COLOR_LINE, true)
            );
        }
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    Direction[] lineFlow(Direction di) {//line flow process. if line is start to flow by this, it will return direction.
        if (!isProcessing){
            if (isLine(di) && !isConnectAll()){
                switch(tileDirection.get()){
                    case Direction.R:
                        lineU = lineL = lineD = true;
                        break;
                    case Direction.U:
                        lineR = lineL = lineD = true;
                        break;
                    case Direction.L:
                        lineR = lineU = lineD = true;
                        break;
                    case Direction.D:
                        lineR = lineU = lineL = true;
                        break;
                }
                Direction[] returnValue = new Direction[2];
                Direction tempDi = new Direction(Direction.R);
                for (int loop1 = 0, index = 0; loop1 < 4; loop1++){
                    if (!(di.equals(tempDi) || tileDirection.equals(tempDi))) returnValue[index++] = new Direction(tempDi);
                    tempDi.rotate(true, 2);
                }
                return returnValue;
            }
        }
        return new Direction[0];
    }
    @Override
    boolean isLine(Direction di) {//check whether line exist tile's direction.
        return !tileDirection.equals(di);
    }
    @Override
    boolean isConnectAll() {//check whether tile's every line is connected.
        return lineR || lineU;
    }
    @Override
    int connectScore() {//return score.
        if (isConnectAll()){
            return ScoreValue.TRIPLE;
        }
        return 0;
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tileBitmap);
        Paint paint = Tools.colorPaint(COLOR1);
        canvas.drawRect(0, 0, tileSize, tilePos1, paint);
        canvas.drawRect(0, tilePos2, tilePos1, tileSize, paint);
        canvas.drawRect(tilePos2, tilePos2, tileSize, tileSize, paint);
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TileAllocator(){
            @Override
            public int getProbability(int level) { return P_TRIPLE; }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_TRIPLE(tileUpdateClass, direction, pos); }
        };
    }
}
class Tile_TRIPLE_MUST extends Tile_TRIPLE{
    //Bitmap.
    static Bitmap[] bitmap_S;//save tile's bitmap with rotation.
    Tile_TRIPLE_MUST(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos){
        super(tileUpdateClass, direction, pos);
        mustConnect = true;
    }
    @Override
    Bitmap[] getRotateBitmap() { return bitmap_S; }//give rotate bitmap to Tile's instance value.
    @Override
    int connectScore() {//return score.
        if (isConnectAll()){
            return ScoreValue.TRIPLE_MUST;
        }
        return 0;
    }
    static void makeTileBitmap(){
        Bitmap tileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tileBitmap);
        Paint paint = Tools.colorPaint(COLOR2);
        canvas.drawRect(0, 0, tileSize, tilePos1, paint);
        canvas.drawRect(0, tilePos2, tilePos1, tileSize, paint);
        canvas.drawRect(tilePos2, tilePos2, tileSize, tileSize, paint);
        bitmap_S = makeRotateBitmap(tileBitmap);//save it to bitmap_S.
    }
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TA_LinearProbability(){
            @Override
            public int getProbability(int level) { return linearMaxLevel(level, P_TRIPLE_MUST_LOW, P_TRIPLE_MUST_HIGH, GameBoard.MAXLEVEL_DEFAULT); }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new Tile_TRIPLE_MUST(tileUpdateClass, direction, pos); }
        };
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
    int connectScore() { return 0; }
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
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TileAllocator(){
            @Override
            public int getProbability(int level) { return PROBABILITY_DEFAULT; }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new TileA(tileUpdateClass, direction, pos); }
        };
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
    @Override
    int connectScore() { return 0; }
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
    static TileAllocator getTileAllocator(){//return TileAllocator interface.
        return new TileAllocator(){
            @Override
            public int getProbability(int level) { return PROBABILITY_DEFAULT; }
            @Override
            public Tile newTile(TileUpdateReceiver tileUpdateClass, Direction direction, Coord pos) { return new TileB(tileUpdateClass, direction, pos); }
        };
    }
}