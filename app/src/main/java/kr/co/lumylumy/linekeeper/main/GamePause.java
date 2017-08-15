package kr.co.lumylumy.linekeeper.main;

import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.tools.TouchInfo;

/**
 * Created by LMJ on 2017-08-08.
 */

public class GamePause implements GameBase{
    //gameMain.
    GameMain gameMain;

    //constructer
    public GamePause(GameMain gameMain){
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
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        return false;
    }
}
