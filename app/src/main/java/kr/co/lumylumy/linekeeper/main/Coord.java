package kr.co.lumylumy.linekeeper.main;

import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-08-26.
 */

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

class Direction{
    static final int R = 0, UR = 1, U = 2, UL = 3, L = 4, DL = 5, D = 6, DR = 7;
    int direction;
    Direction(){ set(R); }
    Direction(int direction){ set(direction); }
    Direction(Direction input){ this(input.get()); }
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Direction) return direction == ((Direction)obj).direction;
        return false;
    }
    @Override
    public int hashCode() { return direction; }
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