package kr.co.lumylumy.linekeeper.tools;

/**
 * Created by LMJ on 2017-08-15.
 */

public class TouchInfo{
    public static final int CANCEL = 0, DOWN = 1, MOVE = 2, UP = 3;
    public float x, y;
    public int id;
    public int action;
    public TouchInfo(float x, float y, int id, int action){ this.x = x; this.y = y; this.id = id; this.action = action;}
    public TouchInfo(TouchInfo input){ x = input.x; y = input.y; id = input.id; action = input.action; }
}