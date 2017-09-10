package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import kr.co.lumylumy.linekeeper.MainActivity;
import kr.co.lumylumy.linekeeper.log.LogSystem;
import kr.co.lumylumy.linekeeper.timer.Timer;
import kr.co.lumylumy.linekeeper.timer.TimerAble;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.TouchInfo;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView.TouchEvent;

/**
 * Created by LMJ on 2017-08-07.
 */

/*
---Job List---
DEV.
    make many type of tile (extends Tile Class).
    메인메뉴에 설정 관련 추가하기.

    make image of tile.
        이미지를 resource폴더에 넣어두고 로드하기.
        --->이미지가 apk에서 바로 보이지 않도록 간단한 변환을 한 뒤 저장, 불러올때 변환 제거하고 Bitmap으로 변환.
    put backgroundMusic and sound effect.
    GameBoard - 이후 계속 개발.
DEBUG.
    ?
*/

public class GameMain implements TimerAble, TouchEvent, MainActivity.BackKeyReceiver{
    //file
    static final String FILE_SCORE = "Score.dat";
    //resolution.
    static final int WIDTH = 600, HEIGHT = 1066;
    //activity
    AppCompatActivity activity;
    //Timer.
    static final int TIMERID_MAIN = 0;
    static final int TIMERPERIOD_MAIN = 16;
    Timer timer;
    //SurfaceDrawView.
    SurfaceDrawView drawView;
    Canvas dv_Canvas;
    int dv_CanvasWidth, dv_CanvasHeight;
    float dv_Ratio;
    //Bitmap.
    Bitmap bitmapMain;
    //gameState.
    static final int GSTATE_MENU = 0, GSTATE_PLAY = 1, GSTATE_PAUSE = 2;
    GameBase gameClass;
    GameMenu gameMenu;
    GamePlay gamePlay;
    GamePause gamePause;
    //test value.
    class Coord {
        int id;
        float x, y;
        Coord(int id){ this(id, 0, 0); }
        Coord(int id, float x, float y){ this.id = id; this.x = x; this.y = y; }
    }
    ArrayList<Coord> circleCoord_S = new ArrayList<Coord>();

    //constructer
    public GameMain(AppCompatActivity activity, SurfaceDrawView drawView){
        this.activity = activity; this.drawView = drawView;
        init();
    }

    //init, reset,exit
    void init(){
        //Timer.
        timer = new Timer(this);
        timer.add(TIMERID_MAIN, TIMERPERIOD_MAIN);
        //DrawView.
        drawView.setTouchEventClass(this);
        drawView.setFpsOutput(true);//test code.
        dv_CanvasWidth = WIDTH; dv_CanvasHeight = HEIGHT;
        dv_Canvas = drawView.setBitmap(Bitmap.createBitmap(dv_CanvasWidth, dv_CanvasHeight, Bitmap.Config.ARGB_8888));
        dv_Ratio = drawView.getRatio();
        //reset.
        reset();
    }
    void reset(){
        //Clear drawView.
        Tools.resetBitmap(dv_Canvas, 0xffffffff);
        drawView.update();
        //GameClass.
        gameMenu = new GameMenu(this);
        gamePlay = new GamePlay(this);
        gamePause = new GamePause(this, gamePlay);
        //gameState
        setGameState(GSTATE_MENU);
    }
    void setGameState(int state){
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
    void exit(){
        timer.stop();
        activity.finish();
    }

    //
    int loadScore(){
        int score = -1;
        FileInputStream fInS = Tools.getFileInternal(FILE_SCORE);
        if (fInS != null){
            DataInputStream dInS = new DataInputStream(fInS);
            try{
                score = dInS.readInt();
                dInS.close();
            }
            catch (IOException e){}
        }
        return score;
    }
    void saveScore(int score){
        FileOutputStream fOutS = Tools.makeFileInternal(FILE_SCORE);
        if (fOutS != null){
            DataOutputStream dOutS = new DataOutputStream(fOutS);
            try{
                dOutS.writeInt(score);
                fOutS.close();
            }
            catch (IOException e){}
        }
    }


    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {
        if (id == TIMERID_MAIN) gameClass.onTimer(id, sendNum);
    }
    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        return gameClass.touchEvent(touchInfo, rawEvent);
    }
    @Override
    public boolean onBackKeyDown() {
        return gameClass.onBackKeyDown();
    }

    //activity state
    public void activityPause(){ timer.stop(); }
    public void activityResume(){
        drawView.viewUpdate();
        timer.start();
    }
    public void activityDestroy(){
        timer.stop();
        gamePlay.saveScore();
    }
}
