package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;

/**
 * Created by LMJ on 2017-08-29.
 */

class ScoreBoard {
    //
    static final int SCORE_LENGTH = 9;
    static final int COLOR = MyColor.BLACK;
    static final int BASENUMBER = 10;
    static final int[] divNum = new int[SCORE_LENGTH + 1];
    static {
        for (int loop1 = 0; loop1 < divNum.length; loop1++){
            int temp = 1;
            for (int loop2 = 0; loop2 < loop1; loop2++) temp *= BASENUMBER;
            divNum[loop1] = temp;
        }
    }
    static int getNumberOfPos(int value, int pos){
        if (0 <= pos && pos < SCORE_LENGTH){
            return (value % divNum[pos + 1]) / divNum[pos];
        }
        return 0;
    }
    //size.
    int width, height;
    int l_Width, l_Height;
    //Bitmap.
    Bitmap[] b_Number = new Bitmap[BASENUMBER];
    Bitmap b_LevelStr;

    //constructor
    ScoreBoard(int width, int height){
        this.width = width; this.height = height;
        l_Height = height / 2;
        l_Width = Tools.textBitmap("0", l_Height, Tools.colorPaint(COLOR)).getWidth() * 4 / 3;
        init();
    }
    void init(){
        Bitmap bitmap;
        for (int loop1 = 0; loop1 < BASENUMBER; loop1++){
            bitmap = Tools.textBitmap(loop1 + "", l_Height, Tools.colorPaint(COLOR));
            b_Number[loop1] = Bitmap.createBitmap(l_Width, l_Height, Bitmap.Config.ARGB_8888);
            Tools.newCanvas(b_Number[loop1]).drawBitmap(bitmap, (l_Width - bitmap.getWidth()) / 2, (l_Height - bitmap.getHeight()) / 2, null);
        }
        bitmap = Tools.textBitmap("LEVEL:", l_Height, Tools.colorPaint(COLOR));
        b_LevelStr = Bitmap.createBitmap(bitmap.getWidth() + l_Width / 8, l_Height, Bitmap.Config.ARGB_8888);
        Tools.newCanvas(b_LevelStr).drawBitmap(bitmap, l_Width / 8, (l_Height - bitmap.getHeight()) / 2, null);
    }
    void draw(Canvas canvas, int score, int level){ draw(canvas, score, level, 0, 0); }
    void draw(Canvas canvas, int score, int level, int xPos, int yPos){
        int x = xPos, y = yPos;
        boolean flag = false;
        //Score draw.
        for (int numPos = SCORE_LENGTH - 1; numPos >= 0; numPos--){
            canvas.drawBitmap(b_Number[getNumberOfPos(score, numPos)], x, y, null);
            x += l_Width;
        }
        //level draw.
        x = xPos + width - l_Width * SCORE_LENGTH;
        y = yPos + l_Height;
        canvas.drawBitmap(b_LevelStr, xPos, y, null);
        for (int numPos = SCORE_LENGTH - 1; numPos >= 0; numPos--){
            int number = getNumberOfPos(level, numPos);
            if (number != 0) flag = true;
            if (flag){
                canvas.drawBitmap(b_Number[number], x, y, null);
            }
            x += l_Width;
        }
    }
}
