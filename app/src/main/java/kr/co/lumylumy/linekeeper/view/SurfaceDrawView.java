package kr.co.lumylumy.linekeeper.view;

/**
 * Created by LMJ on 2017-08-07.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import kr.co.lumylumy.linekeeper.log.LogSystem;
import kr.co.lumylumy.linekeeper.tools.Tools;

import static kr.co.lumylumy.linekeeper.log.LogSystem.androidLog;

public class SurfaceDrawView extends SurfaceView implements SurfaceHolder.Callback {

    ViewThread thread;
    SurfaceHolder mHolder;

    Bitmap bitmap, saveBitmap;
    Canvas canvas;
    Rect rect;
    int marginX = 0, marginY = 0;
    float ratio;

    boolean updateState = false;
    int frameNum = 0;
    long time;

    float framePerSec;
    boolean fpsOutput = false;
    Paint fpsPaint;

    TouchEvent touchEventClass;
    public interface TouchEvent{
        int CANCEL = 0, DOWN = 1, MOVE = 2, UP = 3;
        boolean touchEvent(float x, float y, int id, int action, MotionEvent rawEvent);
    }

    public SurfaceDrawView(Context context){
        super(context);
        init();
    }
    public SurfaceDrawView(Context context, AttributeSet att){
        super(context, att);
        init();
    }
    protected void init(){
        (mHolder = getHolder()).addCallback(this);
        fpsPaint = Tools.textPaint(0xffff7777, 40, Paint.Align.LEFT);
    }
    public Canvas setBitmap(Bitmap input, boolean left){
        if (left){
            return setBitmap(input, -1);
        }
        return setBitmap(input, 1);
    }
    public Canvas setBitmap(Bitmap input){
        return setBitmap(input, 0);
    }
    protected Canvas setBitmap(Bitmap input, int bias) {
        if (input != null){
            synchronized(this){
                bitmap = input;
                int w = getWidth();
                int h = getHeight();
                int bitW = bitmap.getWidth();
                int bitH = bitmap.getHeight();
                float margin = w - (float)bitW * h / bitH;
                if (margin > 0){
                    switch(bias){
                        case -1:
                            marginX = 0;
                            rect = new Rect(0, 0, w - (int)margin, h);
                            break;
                        case 0:
                            marginX = (int)(margin / 2);
                            rect = new Rect(marginX, 0, w - marginX, h);
                            break;
                        case 1:
                            marginX = (int)margin;
                            rect = new Rect(marginX, 0, w, h);
                            break;
                    }
                    ratio = bitH / (float)h;
                }
                else {
                    marginY = h - (int)((float)bitH * w / bitW);
                    rect = new Rect(0, marginY, w, h);
                    ratio = bitW / (float)w;
                }
                canvas = new Canvas();
                canvas.setBitmap(bitmap);
                update();
            }
            return canvas;
        }
        return null;
    }
    public boolean changeBitmap(Bitmap input){
        if (bitmap != null){
            if (bitmap.getWidth() == input.getWidth() && bitmap.getHeight() == input.getHeight()){
                bitmap = input;
                canvas.setBitmap(bitmap);
                update();
                return true;
            }
        }
        return false;
    }
    public Canvas getCanvas(){ return canvas; }
    public float getRatio(){ return ratio; }
    public int getMarginX(){ return marginX; }
    public int getMarginY(){ return marginY; }
    public void update() {
        if (bitmap != null) {
            //long time = SystemClock.elapsedRealtime();
            saveBitmap = Bitmap.createBitmap(bitmap);
            //androidLog("bitmapCopyTime: " + (int)(SystemClock.elapsedRealtime() - time));
            updateState = true;
        }
    }
    public void setFpsOutput(boolean input){ fpsOutput = input; }
    public void setTouchEventClass(TouchEvent input){ touchEventClass = input; }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchEventClass != null){
            boolean returnValue;
            int action, id, index, indexMax;
            float x, y;
            action = event.getActionMasked();
            switch(action){
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    id = event.getPointerId(index = event.getActionIndex());
                    x = (event.getX(index) - marginX) * ratio; y = (event.getY(index) - marginY) * ratio;
                    return touchEventClass.touchEvent(x, y, id, TouchEvent.DOWN, event);
                case MotionEvent.ACTION_MOVE:
                    returnValue = true;
                    indexMax = event.getPointerCount();
                    for (index = 0; index < indexMax; index++){
                        id = event.getPointerId(index);
                        x = (event.getX(index) - marginX) * ratio; y = (event.getY(index) - marginY) * ratio;
                        if (!touchEventClass.touchEvent(x, y, id, TouchEvent.MOVE, event)) returnValue = false;
                    }
                    return returnValue;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    id = event.getPointerId(index = event.getActionIndex());
                    x = (event.getX(index) - marginX) * ratio; y = (event.getY(index) - marginY) * ratio;
                    return touchEventClass.touchEvent(x, y, id, TouchEvent.UP, event);
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    return touchEventClass.touchEvent(0, 0, 0, TouchEvent.CANCEL, event);
            }
        }
        else {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new ViewThread(this);
        time = SystemClock.elapsedRealtime();
        thread.start();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { thread.off(); }
    //Call in another thread.
    protected void reDraw(){
        Canvas viewCanvas = null;
        if (updateState){
            updateState = false;
            frameNum++;
            try{
                viewCanvas  = mHolder.lockCanvas();
                if (viewCanvas != null){
                    synchronized(mHolder){
                        viewCanvas.drawColor(0xff000000);
                        if (saveBitmap != null){
                            viewCanvas.drawBitmap(saveBitmap, null, rect, null);
                        }
                        if (fpsOutput){
                            viewCanvas.drawText(String.format("FPS:%5.2f", framePerSec), 0, viewCanvas.getHeight(), fpsPaint);
                        }
                    }
                }
            } finally {
                if (viewCanvas  != null){
                    mHolder.unlockCanvasAndPost(viewCanvas);
                }
            }
        }
        long endTime = SystemClock.elapsedRealtime();
        if (endTime - time > (long)500){
            framePerSec = (float)frameNum * 1000 / (int)(endTime - time);
            frameNum = 0;
            time = endTime;
        }
        //androidLog("bitmapDrawTime: " + (int)(SystemClock.elapsedRealtime() - time));
    }
}

class ViewThread extends Thread{
    volatile boolean state = true;
    SurfaceDrawView sdView;
    ViewThread(SurfaceDrawView input){ sdView = input; }
    protected void off() {
        state = false;
        boolean retry = true;
        while (retry) {
            try {
                join();
                retry = false;
            } catch (InterruptedException e) { }
        }
    }
    @Override
    public void run() {
        while (state){
            sdView.reDraw();
        }
    }
}
