package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;

import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-09-02.
 */

abstract class DisplayObject{
    String name;
    Rect outputRect;
    Bitmap displayBitmap, displayBitmapTouch;
    int xPos, yPos;
    int touchId;
    boolean touchState = false;
    DisplayObject(String name){ this.name = name; }
    String name(){ return name; }
    int getXPos(){ return xPos; }
    int getYPos(){ return yPos; }
    void setXPos(int x){ setPos(x, yPos); }
    void setYPos(int y){ setPos(xPos, y); }
    abstract void setPos(int x, int y);
    void onTouch(int touchId){ touchState = true; this.touchId = touchId; }
    void offTouch(){ touchState = false; }
    boolean isTouch(){ return touchState; }
    abstract boolean inObject(float x, float y);
    void draw(Canvas canvas) {
        if (touchState) {
            canvas.drawBitmap(displayBitmapTouch, null, outputRect, null);
        } else {
            canvas.drawBitmap(displayBitmap, null, outputRect, null);
        }
    }
    static Bitmap defaultTouchBitmap(Bitmap input){
        Bitmap bitmap = Bitmap.createBitmap(input);
        Tools.newCanvas(bitmap).drawColor(MyColor.aColor(0x7f, MyColor.hsvColor(0, 0, 50)), PorterDuff.Mode.SRC_ATOP);
        return bitmap;
    }
}