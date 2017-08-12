package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import java.util.ArrayList;

import kr.co.lumylumy.linekeeper.log.LogSystem;
import kr.co.lumylumy.linekeeper.timer.Timer;
import kr.co.lumylumy.linekeeper.timer.TimerAble;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView.TouchEvent;

/**
 * Created by LMJ on 2017-08-07.
 */

public class GameMain implements TimerAble, TouchEvent{
    //resolution.
    static final int WIDTH = 600, HEIGHT = 900;
    //activity
    protected AppCompatActivity activity;
    //Timer.
    static final int TIMERID_MAIN = 0;
    protected Timer timer;
    //SurfaceDrawView.
    protected SurfaceDrawView drawView;
    protected Canvas dv_Canvas;
    protected int dv_CanvasWidth, dv_CanvasHeight;
    protected float dv_Ratio;
    //Bitmap.
    protected Bitmap bitmapMain;
    //gameState.
    static final int GSTATE_MENU = 0, GSTATE_PLAY = 1, GSTATE_PAUSE = 2;
    protected GameBase gameClass;
    protected GameMenu gameMenu;
    protected GamePlay gamePlay;
    protected GamePause gamePause;
    //test value.
    protected class Coord {
        int id;
        float x, y;
        Coord(int id){ this(id, 0, 0); }
        Coord(int id, float x, float y){ this.id = id; this.x = x; this.y = y; }
    }
    protected ArrayList<Coord> circleCoord_S = new ArrayList<Coord>();

    //constructer
    public GameMain(AppCompatActivity activity, SurfaceDrawView drawView){
        this.activity = activity; this.drawView = drawView;
        init();
    }

    //init, reset,exit
    protected void init(){
        //Timer.
        timer = new Timer(this);
        timer.add(TIMERID_MAIN, 16);
        //DrawView.
        drawView.setTouchEventClass(this);
        drawView.setFpsOutput(true);//test code.
        dv_CanvasWidth = WIDTH; dv_CanvasHeight = HEIGHT;
        dv_Canvas = drawView.setBitmap(Bitmap.createBitmap(dv_CanvasWidth, dv_CanvasHeight, Bitmap.Config.ARGB_8888));
        dv_Ratio = drawView.getRatio();
        //reset.
        reset();
    }
    protected void reset(){
        //Clear drawView.
        Tools.resetBitmap(dv_Canvas, 0xffffffff);
        drawView.update();
        //GameClass.
        gameMenu = new GameMenu(this);
        gamePlay = new GamePlay(this);
        gamePause = new GamePause(this);
        //gameState
        setGameState(GSTATE_MENU);
    }
    protected void setGameState(int state){
        switch(state){
            case GSTATE_MENU:
                (gameClass = gameMenu).onStart();
                break;
            case GSTATE_PLAY:
                (gameClass = gamePlay).onStart();
                break;
            case GSTATE_PAUSE:
                (gameClass = gamePause).onStart();
                break;
        }
    }
    protected void exit(){
        timer.stop();
        activity.finish();
    }

    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {
        if (id == TIMERID_MAIN) gameClass.onTimer(id, sendNum);
    }
    @Override
    public boolean touchEvent(float x, float y, int id, int action, MotionEvent rawEvent) {
        return gameClass.touchEvent(x, y, id, action, rawEvent);
    }

    //activity state
    public void activityPause(){ timer.stop(); }
    public void activityResume(){
        drawView.viewUpdate();
        timer.start();
    }
    public void activityDestroy(){ timer.stop(); }
}
