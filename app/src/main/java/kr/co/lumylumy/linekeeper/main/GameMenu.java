package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
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
    //classStart
    boolean menuStart;

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
        int width_8 = width / 8;
        int height_8 = height / 8;
        int marginHeight = height_8 / 4;

        Paint tPaint;
        Bitmap tBitmap;
        Canvas tCanvas;
        Path tPath;
        float[] tX = new float[6], tY = new float[6];

        //Start menu.
        tBitmap = Tools.textBitmap("시작하기", width, height_8, Tools.aaPaint(Tools.colorPaint(MyColor.BLUE)));
        b_StartW = tBitmap.getWidth() + width_8 * 2; b_StartH = tBitmap.getHeight();
        b_Start = Bitmap.createBitmap(b_StartW, b_StartH, Bitmap.Config.ARGB_8888);
        tCanvas = Tools.newCanvas(b_Start);
        tX[0] = width_8;                tY[0] = 0;
        tX[1] = b_StartW - width_8;     tY[1] = 0;
        tX[2] = b_StartW - 1;           tY[2] = b_StartH / 2;
        tX[3] = b_StartW - width_8;     tY[3] = b_StartH - 1;
        tX[4] = width_8;                tY[4] = b_StartH - 1;
        tX[5] = 0;                      tY[5] = b_StartH / 2;
        tPath = Tools.polyPath(tX, tY);
        tPaint = new Paint();
        tPaint.setStyle(Paint.Style.FILL);
        tPaint.setShader(new LinearGradient(0, 0, 0, b_StartH - 1, 0xffddff00, 0xff6e7f00, Shader.TileMode.CLAMP));
        tCanvas.drawPath(tPath, tPaint);
        tCanvas.drawBitmap(tBitmap, width_8, 0, null);
        r_Start = Tools.rectWH((width - b_StartW) / 2, height / 2 - b_StartH, b_StartW, b_StartH);
        //Exit menu.
        tBitmap = Tools.textBitmap("종료하기", width, height_8, Tools.aaPaint(Tools.colorPaint(MyColor.BLUE)));
        b_ExitW = tBitmap.getWidth() + width_8 * 2; b_ExitH = tBitmap.getHeight();
        b_Exit = Bitmap.createBitmap(b_ExitW, b_ExitH, Bitmap.Config.ARGB_8888);
        tCanvas = Tools.newCanvas(b_Exit);
        tX[0] = width_8;                tY[0] = 0;
        tX[1] = b_ExitW - width_8;      tY[1] = 0;
        tX[2] = b_ExitW - 1;            tY[2] = b_ExitH / 2;
        tX[3] = b_ExitW - width_8;      tY[3] = b_ExitH - 1;
        tX[4] = width_8;                tY[4] = b_ExitH - 1;
        tX[5] = 0;                      tY[5] = b_ExitH / 2;
        tPath = Tools.polyPath(tX, tY);
        tPaint = new Paint();
        tPaint.setStyle(Paint.Style.FILL);
        tPaint.setShader(new LinearGradient(0, 0, 0, b_ExitH - 1, 0xffddff00, 0xff6e7f00, Shader.TileMode.CLAMP));
        tCanvas.drawPath(tPath, tPaint);
        tCanvas.drawBitmap(tBitmap, width_8, 0, null);
        r_Exit = Tools.rectWH((width - b_ExitW) / 2, height / 2 + marginHeight, b_ExitW, b_ExitH);
        //
        menuStart = true;
    }

    protected void exit(){
        gameMain.exit();
    }
    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {
        if (menuStart){
            Canvas dv_Canvas = gameMain.dv_Canvas;
            dv_Canvas.drawColor(MyColor.WHITE);
            dv_Canvas.drawBitmap(b_Start, null, r_Start, null);
            dv_Canvas.drawBitmap(b_Exit, null, r_Exit, null);
            gameMain.drawView.update();
            menuStart = false;
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
