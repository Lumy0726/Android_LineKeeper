package kr.co.lumylumy.linekeeper.tools;

/**
 * Created by LMJ on 2017-08-07.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
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
    //Canvas, Bitmap, Paint, Rect.
    public static Rect rectWH(int x, int y, int w, int h){
        return new Rect(x, y, x + w, y + h);
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
    public static Canvas multiplyARGB(Canvas canvas, int multi){
        canvas.drawColor(multi << 24, PorterDuff.Mode.DST_IN);
        return canvas;
    }
    public static Canvas resetBitmap(Canvas canvas, int color){
        canvas.drawColor(color, PorterDuff.Mode.SRC);
        return canvas;
    }
}
