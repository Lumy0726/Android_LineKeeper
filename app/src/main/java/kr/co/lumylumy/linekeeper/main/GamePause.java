package kr.co.lumylumy.linekeeper.main;

import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.TouchInfo;

/**
 * Created by LMJ on 2017-08-08.
 */

class GamePause implements GameBase{
    //gameMain.
    GameMain gameMain;
    //gamePlay.
    GamePlay gamePlay;
    //test
    int touchNum;

    //constructer
    public GamePause(GameMain gameMain, GamePlay gamePlay){
        this.gameMain = gameMain;
        this.gamePlay = gamePlay;
    }

    @Override
    public void onStart() {
        //test code - save score and return to menu(when 4 touch input).
        touchNum = 4;
        int bestScore = gameMain.loadScore();
        if (gamePlay.gameBoard.gameScore > bestScore){
            gameMain.saveScore(gamePlay.gameBoard.gameScore);
        }
    }

    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {

    }
    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        //test code - return to menu.
        if (touchInfo.action == TouchInfo.DOWN){
            touchNum--;
            if (touchNum == 0) gameMain.setGameState(GameMain.GSTATE_MENU);
            else Tools.simpleToast("TouchDown" + touchNum);
        }
        return true;
    }
    @Override
    public boolean onBackKeyDown() { return false; }
}
