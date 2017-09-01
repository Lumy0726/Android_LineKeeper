package kr.co.lumylumy.linekeeper.main;

import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.tools.TouchInfo;

/**
 * Created by LMJ on 2017-08-08.
 */

class GamePause implements GameBase{
    //gameMain.
    GameMain gameMain;
    //gamePlay.
    GamePlay gamePlay;

    //constructer
    public GamePause(GameMain gameMain, GamePlay gamePlay){
        this.gameMain = gameMain;
        this.gamePlay = gamePlay;
    }

    @Override
    public void onStart() {
        //test code - save score and return to menu.
        int bestScore = gameMain.loadScore();
        if (gamePlay.gameBoard.gameScore > bestScore){
            gameMain.saveScore(gamePlay.gameBoard.gameScore);
        }
        gameMain.setGameState(GameMain.GSTATE_MENU);
    }

    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {

    }
    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        return false;
    }
    @Override
    public boolean onBackKeyDown() { return false; }
}
