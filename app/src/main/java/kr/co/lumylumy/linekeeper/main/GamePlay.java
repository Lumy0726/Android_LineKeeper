package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.ListIterator;

import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.TouchInfo;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;

/**
 * Created by LMJ on 2017-08-08.
 */

class GamePlay implements GameBase{
    //gameMain.
    GameMain gameMain;
    //size value.
    int dv_Width;
    int dv_Height;
    int gameBoardMargin;
    //GameBoard
    GameBoard gameBoard;
    int gameBoardW, gameBoardH;
    //ScoreBoard
    ScoreBoard scoreBoard;
    //TouchEvent.
    ArrayList<TouchInfo> touchInfo_S = new ArrayList<>();

    //constructer
    public GamePlay(GameMain gameMain){
        this.gameMain = gameMain;
        init();
    }
    void init(){
        dv_Width = gameMain.dv_CanvasWidth;
        dv_Height = gameMain.dv_CanvasHeight;
        gameBoard = new GameBoard(dv_Width, dv_Height);
        gameBoardW = gameBoard.outputWidth; gameBoardH = gameBoard.outputHeight;
        gameBoardMargin = dv_Height - gameBoardH;
        scoreBoard = new ScoreBoard(dv_Width * 8 / 10, gameBoardMargin);
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
        Tools.resetBitmap(gameMain.dv_Canvas, MyColor.WHITE);
        gameBoard.onTimer(id, sendNum);
        gameBoard.draw(gameMain.dv_Canvas);
        scoreBoard.draw(gameMain.dv_Canvas, gameBoard.gameScore, gameBoard.gameScore / 300 + 1);
        /*
        Bitmap scoreBitmap = Tools.textBitmap(
                gameBoard.gameScore + "",
                gameBoardMargin,
                Tools.textPaint(MyColor.BLACK, gameBoardMargin, Paint.Align.LEFT)
        );
        gameMain.dv_Canvas.drawBitmap(scoreBitmap, 0, 0, null);
        */
        gameMain.drawView.update();
    }

    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        TouchInfo t_Info;
        switch(touchInfo.action){
            case TouchInfo.DOWN:
                touchInfo_S.add(touchInfo);
                if (inGameBoard(touchInfo)){
                    return gameBoard.touchEvent(gameBoardTouch(touchInfo), rawEvent);
                }
                return true;
            case TouchInfo.MOVE:
            case TouchInfo.UP:
                ListIterator<TouchInfo> it = touchInfo_S.listIterator();
                while(it.hasNext()){
                    t_Info = it.next();
                    if (t_Info.id == touchInfo.id){
                        //t_Info has the first position of touchInfo.
                        if (touchInfo.action == TouchInfo.UP) it.remove();
                        if (inGameBoard(t_Info)){
                            return gameBoard.touchEvent(gameBoardTouch(touchInfo), rawEvent);
                        }
                        return true;
                    }
                }
                return false;
            case TouchInfo.CANCEL:
                touchInfo_S.clear();
                return gameBoard.touchEvent(touchInfo, rawEvent);
            default: return false;
        }
    }
    boolean inGameBoard(TouchInfo touchInfo){
         return 0 <= touchInfo.x &&
                 touchInfo.x < gameBoardW &&
                 gameBoardMargin <= touchInfo.y &&
                 touchInfo.y < gameBoardMargin + gameBoardH;
    }
    TouchInfo gameBoardTouch(TouchInfo touchInfo){ return new TouchInfo(touchInfo.x, touchInfo.y - gameBoardMargin, touchInfo.id, touchInfo.action); }
}