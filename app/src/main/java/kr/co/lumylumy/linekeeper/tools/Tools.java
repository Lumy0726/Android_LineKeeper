package kr.co.lumylumy.linekeeper.tools;

/**
 * Created by LMJ on 2017-08-07.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import kr.co.lumylumy.linekeeper.timer.Timer;
import kr.co.lumylumy.linekeeper.timer.TimerAble;

public class Tools {
    static DisplayMetrics displayMetrics;

    //Tools_initial
    public static void tools_initial(AppCompatActivity input){
        dipToPixInit(input.getResources().getDisplayMetrics());
    }
    //dipToPix
    public static void dipToPixInit(DisplayMetrics input){
        displayMetrics = input;
    }
    public static float dipToPix(int dip){
        if (displayMetrics != null){
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
        }
        return (float)0;
    }
    //Toast.
    public static void simpleToast(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
    public static void simpleToast(Context context, String str, int time){
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
        Timer.sendOneTimer(
                new TimerAble(){
                    Toast toast;
                    TimerAble setToast(Toast input){ (toast = input).show(); return this; }
                    @Override
                    public void onTimer(int id, int sendNum) {
                        toast.cancel();
                    }
                }.setToast(toast),
                0,
                time
        );
    }
    //Path.
    public static Path polyPath(float[] x, float[] y){
        if (x.length == y.length){
            Path path = new Path();
            int num = x.length;
            path.moveTo(x[0], y[0]);
            for (int loop1 = 1; loop1 < num; loop1++){
                path.lineTo(x[loop1], y[loop1]);
            }
            path.close();
            return path;
        }
        return null;
    }
    public static boolean smoothQuad(Path path, float x1, float y1, float x2, float y2, int dx1, int dy1, int dx2, int dy2){
        int denominator = dx1 * dy2 - dx2 * dy1;
        if (denominator != 0){
            float x = (dx1 * dx2 * (y1 - y2) + dx1 * dy2 * x2 - dx2 * dy1 * x1) / denominator;
            float y = (dy1 * dy2 * (x1 - x2) + dy1 * dx2 * y2 - dy2 * dx1 * y1) / -denominator;
            path.lineTo(x1, y1);
            path.quadTo(x, y, x2, y2);
        }
        return false;
    }
    //Math.
    public static boolean dotIsRight(float x, float y, float lX1, float lY1, float lX2, float lY2){
        float value = (lY2 - lY1) * (x - lX1) + (lX1 - lX2) * (y - lY1);
        //The angle((lX2, lY2) -> (lX1, lY1) -> (x, y)) sin value's sign.
        if (value > (float)0) return false;
        return true;
    }
    public static int remainder(int value, int div){
        int rValue = value % div;
        return (rValue < 0)? rValue + div: rValue;
    }
    //Canvas, Bitmap, Paint, Rect.
    public static Rect rectWH(int x, int y, int w, int h){ return new Rect(x, y, x + w, y + h); }
    public static Paint aaPaint(Paint paint){
        paint.setAntiAlias(true);
        return paint;
    }
    public static Paint forcePaint(){
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        return paint;
    }
    public static Paint colorPaint(int color){ return colorPaint(color, false); }
    public static Paint colorPaint(int color, boolean forceSet){
        Paint colorPaint = new Paint();
        colorPaint.setColor(color);
        if (forceSet){
            colorPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
        return colorPaint;
    }
    public static Paint linePaint(int color, int width){ return linePaint(color, width, false); }
    public static Paint linePaint(int color, int width, boolean forceSet){
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(width);
        if (forceSet){
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
        return paint;
    }
    public static Paint textPaint(int color, float textSize, Paint.Align align){
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        paint.setTextAlign(align);
        return paint;
    }
    public static Paint alphaMultiplyPaint(int alpha){
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        paint.setColor(alpha << 24);
        return paint;
    }
    public static Paint setXfermode(Paint input, PorterDuff.Mode mode){
        input.setXfermode(new PorterDuffXfermode(mode));
        return input;
    }
    public static Bitmap reverseAlpha(Bitmap bitmap){
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int loop1, loop2, color;
        for (loop1 = 0; loop1 < w; loop1++){
            for (loop2 = 0; loop2 < h; loop2++){
                color = bitmap.getPixel(loop1, loop2);
                bitmap.setPixel(loop1, loop2, color ^ 0xff000000);
            }
        }
        return bitmap;
    }
    public static Bitmap trimBitmap(Bitmap bitmap){
        int x1, y1, x2, y2, w = bitmap.getWidth(), h = bitmap.getHeight();
        boolean flag;
        x1=0;x2=0;y2=0;y1=0;
        for (flag = false, x1 = 0; x1 < w; x1++){
            for (int loop1 = 0; loop1 < h; loop1++){
                if (bitmap.getPixel(x1, loop1) != 0){
                    flag = true;
                    break;
                }
            }
            if (flag) break;
        }
        for (flag = false, x2 = w - 1; 0 <= x2; x2--){
            for (int loop1 = 0; loop1 < h; loop1++){
                if (bitmap.getPixel(x1, loop1) != 0){
                    flag = true;
                    break;
                }
            }
            if (flag) break;
        }
        for (flag = false, y1 = 0; y1 < h; y1++){
            for (int loop1 = 0; loop1 < w; loop1++){
                if (bitmap.getPixel(loop1, y1) != 0){
                    flag = true;
                    break;
                }
            }
            if (flag) break;
        }
        for (flag = false, y2 = h - 1; 0 <= y2; y2--){
            for (int loop1 = 0; loop1 < w; loop1++){
                if (bitmap.getPixel(loop1, y1) != 0){
                    flag = true;
                    break;
                }
            }
            if (flag) break;
        }
        if (x1 <= x2 && y1 <= y2){
            Bitmap returnBitmap = Bitmap.createBitmap(x2 - x1 + 1, y2 - y1 + 1, Bitmap.Config.ARGB_8888);
            newCanvas(returnBitmap).drawBitmap(bitmap, new Rect(x1, y1, x2, y2), new Rect(0, 0, x2 - x1 + 1, y2 - y1 + 1), null);
            return returnBitmap;
        }
        return null;
    }
    public static Bitmap textBitmap(String str, int width, int height_TextSize, Paint textPaint){
        Rect rect = new Rect();
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(height_TextSize);
        textPaint.getTextBounds(str, 0, str.length(), rect);
        Bitmap tBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        Canvas tCanvas = Tools.newCanvas(tBitmap);
        tCanvas.drawText(str, -rect.left, -rect.top, textPaint);
        return tBitmap;
    }
    public static Canvas multiplyARGB(Canvas canvas, int multi){
        canvas.drawColor(multi << 24, PorterDuff.Mode.DST_IN);
        return canvas;
    }
    public static Canvas resetBitmap(Canvas canvas, int color){
        canvas.drawColor(color, PorterDuff.Mode.SRC);
        return canvas;
    }
    public static Canvas newCanvas(Bitmap bitmap){
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        return canvas;
    }
}
