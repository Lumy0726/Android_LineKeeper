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
import kr.co.lumylumy.linekeeper.timer.TimeCheck;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.TouchInfo;

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

    int defaultBackground = MyColor.BLACK;

    String debugOutput = null;

    TouchEvent touchEventClass;
    public interface TouchEvent{
        boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent);
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
    public void setBackground(int color){ defaultBackground = MyColor.aColor(0xff, color); }
    public Canvas setBitmap(Bitmap input, boolean left){
        if (left){
            return setBitmap(input, -1);
        }
        return setBitmap(input, 1);
    }
    public Canvas setBitmap(Bitmap input){ return setBitmap(input, 0); }
    protected Canvas setBitmap(Bitmap input, int bias) {
        if (input != null){
            synchronized(this){
                bitmap = input;
                int w = getWidth();
                int h = getHeight();
                int bitW = bitmap.getWidth();
                int bitH = bitmap.getHeight();
                int[] marginValue = new int[2];
                Tools.fitRect(w, h, bitW, bitH, marginValue);
                if (marginValue[0] >= 0){
                    switch(bias){
                        case -1:
                            marginX = 0;
                            rect = new Rect(0, 0, w - marginValue[0], h);
                            break;
                        case 0:
                            marginX = marginValue[0] / 2;
                            rect = new Rect(marginX, 0, w - marginX, h);
                            break;
                        case 1:
                            marginX = marginValue[0];
                            rect = new Rect(marginX, 0, w, h);
                            break;
                    }
                    ratio = bitH / (float)h;
                }
                else {
                    marginY = marginValue[1];
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
    public void setDebugOutput(String str){ debugOutput = str; }
    public Canvas getCanvas(){ return canvas; }
    public float getRatio(){ return ratio; }
    public int getMarginX(){ return marginX; }
    public int getMarginY(){ return marginY; }
    public Bitmap copyCurBitmap(){
        if (bitmap == null) return null;
        return Bitmap.createBitmap(bitmap);
    }
    public void update() {
        if (bitmap != null) {
            //long time = SystemClock.elapsedRealtime();
            saveBitmap = Bitmap.createBitmap(bitmap);
            //androidLog("bitmapCopyTime: " + (int)(SystemClock.elapsedRealtime() - time));
            updateState = true;
        }
    }
    public void viewUpdate(){ if (saveBitmap != null) updateState = true; }
    public void setFpsOutput(boolean input){ fpsOutput = input; }
    public void setTouchEventClass(TouchEvent input){ touchEventClass = input; }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchEventClass != null){
            boolean returnValue;
            int action, id, index, indexMax;
            float x, y;
            TouchInfo touchInfo;
            action = event.getActionMasked();
            switch(action){
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    id = event.getPointerId(index = event.getActionIndex());
                    x = (event.getX(index) - marginX) * ratio; y = (event.getY(index) - marginY) * ratio;
                    touchInfo = new TouchInfo(x, y, id, TouchInfo.DOWN);
                    return touchEventClass.touchEvent(touchInfo, event);
                case MotionEvent.ACTION_MOVE:
                    returnValue = true;
                    indexMax = event.getPointerCount();
                    for (index = 0; index < indexMax; index++){
                        id = event.getPointerId(index);
                        x = (event.getX(index) - marginX) * ratio; y = (event.getY(index) - marginY) * ratio;
                        touchInfo = new TouchInfo(x, y, id, TouchInfo.MOVE);
                        if (!touchEventClass.touchEvent(touchInfo, event)) returnValue = false;
                    }
                    return returnValue;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    id = event.getPointerId(index = event.getActionIndex());
                    x = (event.getX(index) - marginX) * ratio; y = (event.getY(index) - marginY) * ratio;
                    touchInfo = new TouchInfo(x, y, id, TouchInfo.UP);
                    return touchEventClass.touchEvent(touchInfo, event);
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    return touchEventClass.touchEvent(new TouchInfo(0, 0, 0, TouchInfo.CANCEL), event);
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
    //TimeCheck time_Test = new TimeCheck();
    protected void reDraw(){
        Canvas viewCanvas = null;
        if (updateState){
            updateState = false;
            frameNum++;
            try{
                viewCanvas  = mHolder.lockCanvas();
                if (viewCanvas != null){
                    synchronized(mHolder){
                        //time_Test.reset();
                        viewCanvas.drawColor(defaultBackground);
                        if (saveBitmap != null){
                            viewCanvas.drawBitmap(saveBitmap, null, rect, null);
                        }
                        //androidLog(String.format("DrawView: %5.2f", time_Test.getTimeAv()));
                        if (fpsOutput){
                            viewCanvas.drawText(String.format("FPS:%5.2f", framePerSec), 0, viewCanvas.getHeight(), fpsPaint);
                        }
                        if (debugOutput != null){
                            viewCanvas.drawText(debugOutput, 0, fpsPaint.getTextSize(), fpsPaint);
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
