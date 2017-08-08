package kr.co.lumylumy.linekeeper.main;

import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.main.*;


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
    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {

    }
    @Override
    public boolean touchEvent(float x, float y, int id, int action, MotionEvent rawEvent) {
        return false;
    }
}
