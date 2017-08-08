package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Iterator;

import kr.co.lumylumy.linekeeper.log.LogSystem;
import kr.co.lumylumy.linekeeper.timer.Timer;
import kr.co.lumylumy.linekeeper.timer.TimerAble;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView.TouchEvent;
/**
 * Created by LMJ on 2017-08-07.
 */

public class GameMain implements TimerAble, TouchEvent{
    //activity
    AppCompatActivity activity;
    //Timer.
    static final int TIMERID_MAIN = 0;
    Timer timer;
    //SurfaceDrawView.
    SurfaceDrawView drawView;
    Canvas dv_Canvas;
    int dv_CanvasWidth, dv_CanvasHeight;
    float dv_Ratio;
    //Bitmap.
    Bitmap bitmapMain;
    Bitmap bufB_Circle;
    int bufB_CircleR;
    //tempCanvas.
    Canvas tempCanvas = new Canvas();
    protected Canvas setTempCanvas(Bitmap bitmap){
        tempCanvas.setBitmap(bitmap);
        return tempCanvas;
    }
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

    //init, reset
    protected void init(){
        //Timer.
        timer = new Timer(this);
        timer.add(TIMERID_MAIN, 16);
        //DrawView.
        drawView.setTouchEventClass(this);
        drawView.setFpsOutput(true);//test code.
        dv_CanvasWidth = 600; dv_CanvasHeight = 900;
        dv_Canvas = drawView.setBitmap(Bitmap.createBitmap(dv_CanvasWidth, dv_CanvasHeight, Bitmap.Config.ARGB_8888));
        dv_Ratio = drawView.getRatio();
        //Bitmap.
        bitmapMain = Bitmap.createBitmap(dv_CanvasWidth, dv_CanvasHeight, Bitmap.Config.ARGB_8888);
        bufB_CircleR = (int) Tools.dipToPix(25);
        bufB_Circle = Bitmap.createBitmap(2 * bufB_CircleR, 2 * bufB_CircleR, Bitmap.Config.ARGB_8888);
        setTempCanvas(bufB_Circle).drawCircle(bufB_CircleR, bufB_CircleR, bufB_CircleR, Tools.colorPaint(0xff000000, true));
        //reset.
        reset();
    }
    protected void reset(){
        //Clear drawView.
        Tools.resetBitmap(dv_Canvas, 0xffffffff);
        drawView.update();
    }

    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {
        setTempCanvas(bitmapMain);
        Tools.resetBitmap(tempCanvas, 0xffffffff);
        Iterator<Coord> it = circleCoord_S.iterator();
        while(it.hasNext()){
            Coord coord = it.next();
            tempCanvas.drawBitmap(bufB_Circle, coord.x - bufB_CircleR, coord.y - bufB_CircleR, null);
        }
        dv_Canvas.drawBitmap(bitmapMain, 0, 0, Tools.forcePaint());
        drawView.update();
    }
    @Override
    public boolean touchEvent(float x, float y, int id, int action, MotionEvent rawEvent) {
        Iterator<Coord> it;
        switch(action){
            case TouchEvent.DOWN:
                LogSystem.androidLog("DOWN:" + id);
                circleCoord_S.add(new Coord(id, x, y));
                break;
            case TouchEvent.MOVE:
                it = circleCoord_S.iterator();
                while(it.hasNext()){
                    Coord coord = it.next();
                    if (coord.id == id){
                        coord.x = x; coord.y = y;
                        break;
                    }
                }
                break;
            case TouchEvent.UP:
                LogSystem.androidLog("--UP:" + id);
                it = circleCoord_S.iterator();
                while(it.hasNext()){
                    if (it.next().id == id){
                        it.remove();
                        break;
                    }
                }
                break;
            case TouchEvent.CANCEL:
                circleCoord_S.clear();
                break;
        }
        return true;
    }

    //activity state
    public void activityPause(){
        timer.stop();
    }
    public void activityResume(){
        timer.start();
    }
    public void activityDestroy(){
        timer.stop();
    }
}
