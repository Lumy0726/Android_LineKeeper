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

import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;


/**
 * Created by LMJ on 2017-08-08.
 */

public class GameMenu implements GameBase {
    //gameMain.
    GameMain gameMain;

    //Menu.
    static final int MENU_NUM = 2;
    static final int MENU_START = 0;
    static final int MENU_EXIT = 1;
    Menu[] menu_S = new Menu[MENU_NUM];

    //
    Bitmap background;

    //constructer
    public GameMenu(GameMain gameMain) {
        this.gameMain = gameMain;
        init();
    }

    void init() {
        int width = gameMain.dv_CanvasWidth;
        int height = gameMain.dv_CanvasHeight;
        int menuWidth = width * 4 / 5;
        int menuHeight = height / 10;
        int marginHeight = menuHeight / 4;

        //Menu.
        int menuX = (width - menuWidth) / 2, menuY = (height - (menuHeight + marginHeight) * MENU_NUM) / 2;
        menu_S[MENU_START] = new Menu("시작하기", menuWidth, menuHeight, menuWidth / 8);
        menu_S[MENU_EXIT] = new Menu("종료하기", menuWidth, menuHeight, menuWidth / 8);
        for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
            menu_S[loop1].setPos(menuX, menuY);
            menuY += (menuHeight + marginHeight);
        }
        //Background.
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            //Background - tileBitmap;
        int tileSize = width / 20;
        Paint paint = Tools.aaPaint(Tools.colorPaint(MyColor.hsvColor(0, 0, 93)));
        paint.setStyle(Paint.Style.FILL);
        Path path = new Path();
        {
            float size = (float)tileSize, halfSize = tileSize / (float)2;
            path.moveTo(halfSize, 0);
            path.lineTo(size, halfSize);
            path.lineTo(halfSize, size);
            path.lineTo(0, halfSize);
            path.close();
        }
        Bitmap tBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(tBitmap);
        canvas.drawPath(path, paint);
            //Background - draw.
        canvas.setBitmap(background);
        canvas.drawColor(MyColor.WHITE);
        for (int loop1 = 0; loop1 < width; loop1 += tileSize){
            for (int loop2 = 0; loop2 < height; loop2 += tileSize){
                canvas.drawBitmap(tBitmap, loop1, loop2, null);
            }
        }
    }
    @Override
    public void onStart() {
        Canvas dv_Canvas = gameMain.dv_Canvas;
        dv_Canvas.drawBitmap(background, 0, 0, null);
        for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
            menu_S[loop1].draw(dv_Canvas);
        }
        gameMain.drawView.update();
    }
    void exit() { gameMain.exit(); }

    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {
        //NONE.
    }
    @Override
    public boolean touchEvent(float x, float y, int id, int action, MotionEvent rawEvent) {
        switch (action) {
            case SurfaceDrawView.TouchEvent.DOWN:
                for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
                    if (menu_S[loop1].inMenu(x, y)) {
                        menu_S[loop1].touchId = id;
                        menu_S[loop1].touchState = true;
                        menu_S[loop1].draw(gameMain.dv_Canvas);
                        gameMain.drawView.update();
                        break;
                    }
                }
                break;
            case SurfaceDrawView.TouchEvent.UP:
                for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
                    if (menu_S[loop1].touchState && menu_S[loop1].touchId == id){
                        menu_S[loop1].touchState = false;
                        menu_S[loop1].draw(gameMain.dv_Canvas);
                        gameMain.drawView.update();
                        if (menu_S[loop1].inMenu(x, y)) {
                            switch(loop1){
                                case MENU_START:
                                    gameMain.setGameState(GameMain.GSTATE_PLAY);
                                    break;
                                case MENU_EXIT:
                                    exit();
                                    break;
                            }
                        }
                        break;
                    }
                }
                break;
            case SurfaceDrawView.TouchEvent.CANCEL:
                for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
                    menu_S[loop1].touchState = false;
                    menu_S[loop1].draw(gameMain.dv_Canvas);
                }
                gameMain.drawView.update();
                break;
        }
        return true;
    }
}

class Menu {
    Rect rect;
    Bitmap bitmap, bitmapTouch;
    int width, height, edgeWidth;
    int touchId;
    boolean touchState = false;

    Menu(String str, int width, int height, int edgeWidth) {
        this(str, 0, 0, width, height, edgeWidth);
    }

    Menu(String str, int left, int top, int width, int height, int edgeWidth) {
        bitmap = Bitmap.createBitmap(this.width = width, this.height = height, Bitmap.Config.ARGB_8888);
        rect = Tools.rectWH(left, top, width, height);
        Canvas canvas = Tools.newCanvas(bitmap);
        Path path = new Path();
        path.moveTo(edgeWidth, 0);
        path.lineTo(width - edgeWidth, 0);
        path.lineTo(width, height / (float) 2);
        path.lineTo(width - edgeWidth, height);
        path.lineTo(edgeWidth, height);
        path.lineTo(0, height / (float) 2);
        path.close();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, 0, 0, height, MyColor.hsvColor(68, 100, 100), MyColor.hsvColor(68, 80, 50), Shader.TileMode.CLAMP));
        canvas.drawPath(path, paint);
        Bitmap tBitmap = Tools.textBitmap(str, width, height, Tools.aaPaint(Tools.colorPaint(MyColor.BLUE)));
        canvas.drawBitmap(tBitmap, (width - tBitmap.getWidth()) / (float) 2, (height - tBitmap.getHeight()) / (float) 2, null);
        bitmapTouch = Bitmap.createBitmap(bitmap);
        canvas.setBitmap(bitmapTouch);
        canvas.drawColor(MyColor.aColor(0x7f, MyColor.hsvColor(0, 0, 50)), PorterDuff.Mode.SRC_ATOP);
        this.edgeWidth = edgeWidth;
    }

    void setPos(int x, int y) { rect = Tools.rectWH(x, y, width, height); }

    boolean inMenu(float x, float y) {
        if (rect.top <= (int) y && (int) y < rect.bottom) {
            if (rect.left <= (int) x && (int) x < rect.right) {
                if (rect.left + edgeWidth <= (int) x && (int) x < rect.right - edgeWidth)
                    return true;
                if ((int) x < rect.left + edgeWidth) {
                    if (Tools.dotIsRight(x, y, rect.left, rect.exactCenterY(), rect.left + edgeWidth, rect.top) &&
                            Tools.dotIsRight(x, y, rect.left + edgeWidth, rect.bottom, rect.left, rect.exactCenterY())
                            ) return true;
                } else if (Tools.dotIsRight(x, y, rect.right - edgeWidth, rect.top, rect.right, rect.exactCenterY()) &&
                        Tools.dotIsRight(x, y, rect.right, rect.exactCenterY(), rect.right - edgeWidth, rect.bottom)
                        ) return true;
            }
        }
        return false;
    }

    void draw(Canvas canvas) {
        if (touchState) {
            canvas.drawBitmap(bitmapTouch, null, rect, null);
        } else {
            canvas.drawBitmap(bitmap, null, rect, null);
        }
    }
}