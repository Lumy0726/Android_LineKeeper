package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-09-02.
 */

public class DO_HexView extends DisplayObject{
    int width, height, edgeWidth;
    int touchId;
    boolean touchState = false;
    Bitmap background;
    int textColor;
    Path path;

    DO_HexView(String name, String str, int width, int height, int edgeWidth, Bitmap bgBitmap, int textColor){ this(name, str, 0, 0, width, height, edgeWidth, bgBitmap, textColor); }
    DO_HexView(String name, String str, int width, int height, int edgeWidth, Paint bgPaint, int textColor) { this(name, str, 0, 0, width, height, edgeWidth, bgPaint, textColor); }
    DO_HexView(String name, String str, int xPos, int yPos, int width, int height, int edgeWidth, Bitmap bgBitmap, int textColor){
        super(name);
        this.edgeWidth = edgeWidth;
        this.width = width; this.height = height;
        setPos(xPos, yPos);
        this.textColor = textColor;
        backgroundInit();
        Canvas canvas = Tools.newCanvas(background);
        canvas.clipPath(path);
        canvas.drawBitmap(bgBitmap, 0, 0, null);
        setStr(str);
    }
    DO_HexView(String name, String str, int xPos, int yPos, int width, int height, int edgeWidth, Paint bgPaint, int textColor) {
        super(name);
        this.edgeWidth = edgeWidth;
        this.width = width; this.height = height;
        setPos(xPos, yPos);
        this.textColor = textColor;
        backgroundInit();
        Canvas canvas = Tools.newCanvas(background);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, bgPaint);
        setStr(str);
    }
    void backgroundInit(){
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        path = new Path();
        path.moveTo(edgeWidth, 0);
        path.lineTo(width - edgeWidth, 0);
        path.lineTo(width, height / (float) 2);
        path.lineTo(width - edgeWidth, height);
        path.lineTo(edgeWidth, height);
        path.lineTo(0, height / (float) 2);
        path.close();
    }
    void setStr(String str){
        displayBitmap = Bitmap.createBitmap(background);
        Canvas canvas = Tools.newCanvas(displayBitmap);
        Bitmap tBitmap = Tools.textBitmap(str, height, Tools.aaPaint(Tools.colorPaint(textColor)));
        int[] fitXY = new int[2];
        int heightMargin = height / 12;
        Tools.fitRect(width - edgeWidth * 2, height - heightMargin * 2, tBitmap.getWidth(), tBitmap.getHeight(), fitXY);
        canvas.drawBitmap(
                tBitmap,
                null,
                new Rect(edgeWidth + fitXY[0] / 2, heightMargin + fitXY[1] / 2, width - edgeWidth - fitXY[0] / 2, height - heightMargin - fitXY[1] / 2),
                null);
        displayBitmapTouch = defaultTouchBitmap(displayBitmap);
    }
    @Override
    void setPos(int x, int y) {
        xPos = x; yPos = y;
        outputRect = Tools.rectWH(x, y, width, height);
    }
    @Override
    boolean inObject(float x, float y) {
        if (outputRect.top <= (int) y && (int) y < outputRect.bottom) {
            if (outputRect.left <= (int) x && (int) x < outputRect.right) {
                if (outputRect.left + edgeWidth <= (int) x && (int) x < outputRect.right - edgeWidth)
                    return true;
                if ((int) x < outputRect.left + edgeWidth) {
                    if (Tools.dotIsRight(x, y, outputRect.left, outputRect.exactCenterY(), outputRect.left + edgeWidth, outputRect.top) &&
                            Tools.dotIsRight(x, y, outputRect.left + edgeWidth, outputRect.bottom, outputRect.left, outputRect.exactCenterY())
                            ) return true;
                } else if (Tools.dotIsRight(x, y, outputRect.right - edgeWidth, outputRect.top, outputRect.right, outputRect.exactCenterY()) &&
                        Tools.dotIsRight(x, y, outputRect.right, outputRect.exactCenterY(), outputRect.right - edgeWidth, outputRect.bottom)
                        ) return true;
            }
        }
        return false;
    }
}
