package kr.co.lumylumy.linekeeper.main;

import android.graphics.Canvas;
import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.tools.MyColor;

/**
 * Created by LMJ on 2017-08-08.
 */

public class GamePlay implements GameBase{
    //gameMain.
    GameMain gameMain;
    //GameBoard
    GameBoard gameBoard;

    //constructer
    public GamePlay(GameMain gameMain){
        this.gameMain = gameMain;
        init();
    }
    void init(){
        int width = gameMain.dv_CanvasWidth;
        int height = gameMain.dv_CanvasHeight;
        gameBoard = new GameBoard(0, height / 10, width);
    }
    @Override
    public void onStart() {
        Canvas canvas = gameMain.dv_Canvas;
        canvas.drawColor(MyColor.WHITE);
        gameBoard.draw(gameMain.dv_Canvas);
        gameMain.drawView.update();
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