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
import kr.co.lumylumy.linekeeper.tools.TouchInfo;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;


/**
 * Created by LMJ on 2017-08-08.
 */

public class GameMenu implements GameBase {
    //gameMain.
    GameMain gameMain;

    //DisplayObject.
    static final int MENU_NUM = 5;
    static final String MENU_START = "Menu_Start";
    static final String MENU_EXIT = "Menu_Exit";
    static final String MENU_START50 = "Menu_Start50";
    static final String MENU_START100 = "Menu_Start100";
    static final String MENU_SETTING = "Menu_Setting";
    static final int SCOREBAR_NUM = 1;
    static final String DO_SCOREBAR = "DO_ScoreBar";
    static final int DO_NUM = MENU_NUM + SCOREBAR_NUM;
    DisplayObject[] displayObjects = new DisplayObject[DO_NUM];
    int do_Num = 0;

    //
    Bitmap background;

    //
    int bestScore;

    //constructer
    GameMenu(GameMain gameMain) {
        this.gameMain = gameMain;
        init();
    }

    void init() {
        int width = gameMain.dv_CanvasWidth;
        int height = gameMain.dv_CanvasHeight;
        int menuWidth = width * 4 / 5;
        int menuHeight = height / 10;
        int marginHeight = menuHeight / 4;
        int scoreBarHeight = height / 10;

        //Menu.
        int menuX = (width - menuWidth) / 2, menuY = (height + scoreBarHeight - (menuHeight + marginHeight) * MENU_NUM) / 2;
        addDisplayObject(new Menu(MENU_START, "시작하기", menuWidth, menuHeight, menuWidth / 8));
        addDisplayObject(new Menu(MENU_EXIT, "종료하기", menuWidth, menuHeight, menuWidth / 8));
        addDisplayObject(new Menu(MENU_START50, "50L시작(test)", menuWidth, menuHeight, menuWidth / 8));
        addDisplayObject(new Menu(MENU_START100, "100L시작(test)", menuWidth, menuHeight, menuWidth / 8));
        addDisplayObject(new Menu(MENU_SETTING, "설정", menuWidth, menuHeight, menuWidth / 8));
        for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
            displayObjects[loop1].setPos(menuX, menuY);
            menuY += (menuHeight + marginHeight);
        }
        //scoreBar.
        bestScore = 0;
        addDisplayObject(new ScoreBar(DO_SCOREBAR, (width - menuWidth) / 2, 0, menuWidth, scoreBarHeight, bestScore));
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
    void addDisplayObject(DisplayObject input){
        if (input != null && do_Num < DO_NUM){
            displayObjects[do_Num++] = input;
        }
    }
    @Override
    public void onStart() {
        int lScore = gameMain.loadScore();
        if (lScore != -1) bestScore = lScore;//load complete.
        if (displayObjects[MENU_NUM] instanceof ScoreBar){
            ((ScoreBar)displayObjects[MENU_NUM]).changeScore(bestScore);
        }
        Canvas dv_Canvas = gameMain.dv_Canvas;
        dv_Canvas.drawBitmap(background, 0, 0, null);
        for (int loop1 = 0; loop1 < DO_NUM; loop1++) {
            displayObjects[loop1].draw(dv_Canvas);
        }
        gameMain.drawView.update();
    }
    void exit() { gameMain.exit(); }

    //Timer/Touch input.

    @Override
    public boolean onBackKeyDown() { return false; }
    @Override
    public void onTimer(int id, int sendNum) {
        //NONE.
    }

    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        DisplayObject obj = null;
        switch (touchInfo.action) {
            case TouchInfo.DOWN:
                for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
                    obj = displayObjects[loop1];
                    if (obj.inObject(touchInfo.x, touchInfo.y)) {
                        obj.onTouch(touchInfo.id);
                        obj.draw(gameMain.dv_Canvas);
                        gameMain.drawView.update();
                        break;
                    }
                }
                break;
            case TouchInfo.UP:
                for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
                    obj = displayObjects[loop1];
                    if (obj.isTouch() && obj.touchId == touchInfo.id){
                        obj.offTouch();
                        obj.draw(gameMain.dv_Canvas);
                        gameMain.drawView.update();
                        if (obj.inObject(touchInfo.x, touchInfo.y)) {
                            String str = obj.name();
                            if (str.equals(MENU_START)){
                                gameMain.setGameState(GameMain.GSTATE_PLAY);
                            }
                            else if (str.equals(MENU_EXIT)){
                                exit();
                            }
                            else if (str.equals(MENU_START50)){
                                gameMain.gamePlay.gameBoard.changeLevel(50);
                                gameMain.setGameState(GameMain.GSTATE_PLAY);
                            }
                            else if (str.equals(MENU_START100)){
                                gameMain.gamePlay.gameBoard.changeLevel(100);
                                gameMain.setGameState(GameMain.GSTATE_PLAY);
                            }
                        }
                        break;
                    }
                }
                break;
            case TouchInfo.CANCEL:
                for (int loop1 = 0; loop1 < MENU_NUM; loop1++) {
                    displayObjects[loop1].offTouch();
                    displayObjects[loop1].draw(gameMain.dv_Canvas);
                }
                gameMain.drawView.update();
                break;
        }
        return true;
    }
}

class Menu extends DO_HexView{
    Menu(String name, String str, int width, int height, int edgeWidth) { this(name, str, 0, 0, width, height, edgeWidth); }
    Menu(String name, String str, int xPos, int yPos, int width, int height, int edgeWidth) {
        super(
                name, str,
                xPos, yPos,
                width, height, edgeWidth,
                new Paint(){
                    Paint f(int height){
                        setShader(new LinearGradient(0, 0, 0, height, MyColor.hsvColor(68, 100, 100), MyColor.hsvColor(68, 80, 50), Shader.TileMode.CLAMP));
                        return this;
                    }
                }.f(height),
                MyColor.BLUE
        );
    }
}

class ScoreBar extends DisplayObject{
    int width, height;
    int height_2;
    Bitmap scoreBitmap;
    ScoreBar(String name, int xPos, int yPos, int width, int height, int score){
        super(name);
        this.width = width; this.height = height;
        setPos(xPos, yPos);
        scoreBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(scoreBitmap);
        float radius = height / (float)6;
        canvas.drawBitmap(Tools.roundRectBitmap(width, height, radius, MyColor.hsvColor(0x90, 120, 60, 100)), 0, 0, null);
        height_2 = height / 2;
        Bitmap tBitmap = Tools.textBitmap("BEST SCORE:", height_2, Tools.colorPaint(MyColor.BLACK));
        canvas.drawBitmap(tBitmap, (width - tBitmap.getWidth()) / 2, (height_2 - tBitmap.getHeight()) / 2, null);
        changeScore(score);
    }

    void changeScore(int score){
        Bitmap tempBitmap = Tools.textBitmap(score + "", height_2, Tools.colorPaint(MyColor.BLACK));
        displayBitmap = Bitmap.createBitmap(scoreBitmap);
        Tools.newCanvas(displayBitmap).drawBitmap(tempBitmap, (width - tempBitmap.getWidth()) / 2, height_2 + (height_2 - tempBitmap.getHeight()) / 2, null);
    }

    @Override
    void draw(Canvas canvas) {
        //Do not draw displayBitmapTouch.
        canvas.drawBitmap(displayBitmap, null, outputRect, null);
    }
    @Override
    void setPos(int x, int y) {
        xPos = x; yPos = y;
        outputRect = Tools.rectWH(xPos, yPos, width, height);
    }
    @Override
    boolean inObject(float x, float y) { return false; }
}