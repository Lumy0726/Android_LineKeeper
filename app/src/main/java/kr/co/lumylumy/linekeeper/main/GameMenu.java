package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.main.*;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;


/**
 * Created by LMJ on 2017-08-08.
 */

public class GameMenu implements GameBase{
    //gameMain.
    GameMain gameMain;
    //initFlag
    boolean dv_UpdateNeed;

    //Menu.
    Rect r_Start;
    Bitmap b_Start;
    int b_StartW, b_StartH;
    Rect r_Exit;

    Bitmap b_Exit;
    int b_ExitW, b_ExitH;
    int exitTouchId;
    boolean exitTouchFlag;

    //constructer
    public GameMenu(GameMain gameMain){
        this.gameMain = gameMain;
        init();
    }
    protected void init(){
        int width = gameMain.dv_CanvasWidth;
        int height = gameMain.dv_CanvasHeight;
        int sHeight = height / 8;
        int marginHeight = sHeight / 4;

        //Start menu.
        b_Start = Tools.textBitmap("시작하기", width, sHeight, MyColor.temp, Tools.textPaint(MyColor.BLUE, sHeight, Paint.Align.CENTER));
        b_StartW = b_Start.getWidth(); b_StartH = b_Start.getHeight();
        r_Start = Tools.rectWH((width - b_StartW) / 2, height / 2 - b_StartH, b_StartW, b_StartH);
        //Exit menu.
        b_Exit = Tools.textBitmap("종료하기", width, sHeight, MyColor.temp, Tools.textPaint(MyColor.BLUE, sHeight, Paint.Align.CENTER));
        b_ExitW = b_Exit.getWidth(); b_ExitH = b_Exit.getHeight();
        r_Exit = Tools.rectWH((width - b_ExitW) / 2, height / 2 + marginHeight, b_ExitW, b_ExitH);
        //
        dv_UpdateNeed = true;
    }

    protected void exit(){
        gameMain.exit();
    }
    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {
        if (dv_UpdateNeed){
            Canvas dv_Canvas = gameMain.dv_Canvas;
            dv_Canvas.drawColor(MyColor.WHITE);
            dv_Canvas.drawBitmap(b_Start, null, r_Start, null);
            dv_Canvas.drawBitmap(b_Exit, null, r_Exit, null);
            gameMain.drawView.update();
            dv_UpdateNeed = false;
        }
    }
    @Override
    public boolean touchEvent(float x, float y, int id, int action, MotionEvent rawEvent) {
        switch(action){
            case SurfaceDrawView.TouchEvent.DOWN:
                if (r_Exit.contains((int)x, (int)y)){
                    exitTouchFlag = true;
                    exitTouchId = id;
                }
                break;
            case SurfaceDrawView.TouchEvent.UP:
                if (exitTouchFlag && exitTouchId == id){
                    if (r_Exit.contains((int)x, (int)y)){
                        exit();
                    }
                    else exitTouchFlag = false;
                }
                break;
            case SurfaceDrawView.TouchEvent.CANCEL:
                exitTouchFlag = false;
                break;
        }
        return true;
    }
}
