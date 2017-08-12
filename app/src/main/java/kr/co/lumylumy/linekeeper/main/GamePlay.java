package kr.co.lumylumy.linekeeper.main;

import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-08-08.
 */

public class GamePlay implements GameBase{
    //gameMain.
    GameMain gameMain;

    //constructer
    public GamePlay(GameMain gameMain){
        this.gameMain = gameMain;
    }

    @Override
    public void onStart() {

    }

    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {

    }
    @Override
    public boolean touchEvent(float x, float y, int id, int action, MotionEvent rawEvent) {
        return false;
    }
}

class GameBoard{

    //constructor.
    GameBoard(){

    }
}

class Tile{
    //constructor.
    Tile(){

    }
}

class Coord{
    static final int R = 0, UR = 1, U = 2, UL = 3, L = 4, DL = 5, D = 6, DR = 7;
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
    Coord move(int direction){ return move(direction, 1); }
    Coord move(int direction, int num){
        int moveX = 0, moveY = 0;
        switch(direction){
            case R: moveX = num; moveY = 0;
                break;
            case UR: moveX = num; moveY = -num;
                break;
            case U: moveX = 0; moveY = -num;
                break;
            case UL: moveX = -num; moveY = -num;
                break;
            case L: moveX = -num; moveY = 0;
                break;
            case DL: moveX = -num; moveY = num;
                break;
            case D: moveX = 0; moveY = num;
                break;
            case DR:moveX = num; moveY = num;
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