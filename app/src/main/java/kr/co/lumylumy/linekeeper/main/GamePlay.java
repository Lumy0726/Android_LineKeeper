package kr.co.lumylumy.linekeeper.main;

import android.graphics.Canvas;
import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-08-08.
 */

public class GamePlay implements GameBase{
    //gameMain.
    GameMain gameMain;
    //size value.
    int dv_Width;
    int dv_Height;
    int gameBoardMargin;
    //GameBoard
    GameBoard gameBoard;

    //constructer
    public GamePlay(GameMain gameMain){
        this.gameMain = gameMain;
        init();
    }
    void init(){
        dv_Width = gameMain.dv_CanvasWidth;
        dv_Height = gameMain.dv_CanvasHeight;
        gameBoard = new GameBoard(dv_Width, dv_Height);
        gameBoardMargin = dv_Height - gameBoard.height;
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
        gameBoard.onTimer(id, sendNum);
        gameBoard.draw(gameMain.dv_Canvas);
        gameMain.drawView.update();
    }
    @Override
    public boolean touchEvent(float x, float y, int id, int action, MotionEvent rawEvent) {
        if (gameBoardMargin < y){
            return gameBoard.touchEvent(x, y - gameBoardMargin, id, action, rawEvent);
        }
        return true;
    }
}