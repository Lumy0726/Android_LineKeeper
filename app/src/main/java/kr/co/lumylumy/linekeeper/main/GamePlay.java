package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.ListIterator;

import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.TouchInfo;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;

/**
 * Created by LMJ on 2017-08-08.
 */

class GamePlay implements GameBase{
    //gameMain.
    GameMain gameMain;
    //size value.
    int dv_Width;
    int dv_Height;
    int gameBoardMargin;
    //GameBoard
    GameBoard gameBoard;
    int gameBoardW, gameBoardH;
    //ScoreBoard
    ScoreBoard scoreBoard;
    int scoreBoardWidth;
    //PauseButton.
    PauseButton pauseButton;
    static final String PAUSEBUTTON_ID = "PauseButton";
    //TouchEvent.
    ArrayList<TouchInfo> touchInfo_S = new ArrayList<>();
    //state.
    static final int STATE_PLAY = 0, STATE_PAUSE = 1, STATE_DIE = 2;
    int state;

    //constructer
    public GamePlay(GameMain gameMain){
        this.gameMain = gameMain;
        init();
    }
    void init(){
        dv_Width = gameMain.dv_CanvasWidth;
        dv_Height = gameMain.dv_CanvasHeight;
        gameBoard = new GameBoard(dv_Width, dv_Height);
        gameBoardW = gameBoard.outputWidth; gameBoardH = gameBoard.outputHeight;
        gameBoardMargin = dv_Height - gameBoardH;
        scoreBoardWidth = dv_Width * 8 / 10;
        scoreBoard = new ScoreBoard(scoreBoardWidth, gameBoardMargin);
        int pauseButtonMargin = (dv_Width - scoreBoardWidth) / 10;
        pauseButton = new PauseButton(PAUSEBUTTON_ID, dv_Width - scoreBoardWidth - pauseButtonMargin * 2, dv_Width - scoreBoardWidth - pauseButtonMargin * 2);
        pauseButton.setPos(scoreBoardWidth + pauseButtonMargin, (gameBoardMargin - pauseButton.height) / 2);
        state = STATE_PLAY;
    }
    @Override
    public void onStart() {
        Canvas canvas = gameMain.dv_Canvas;
        canvas.drawColor(MyColor.WHITE);
        if (state == STATE_DIE) gameBoard.reset();
        else if (state == STATE_PAUSE){
            //code - resume game countdown.
        }
        gameBoard.draw(gameMain.dv_Canvas);
        gameMain.drawView.update();
        state = STATE_PLAY;
    }

    //Timer/Touch input.
    @Override
    public boolean onBackKeyDown() {
        //need - return to menu.
        return false;
    }
    @Override
    public void onTimer(int id, int sendNum) {
        Tools.resetBitmap(gameMain.dv_Canvas, MyColor.WHITE);
        gameBoard.onTimer(id, sendNum);
        gameBoard.draw(gameMain.dv_Canvas);
        scoreBoard.draw(gameMain.dv_Canvas, gameBoard.gameScore, gameBoard.gameLevel);
        pauseButton.draw(gameMain.dv_Canvas);
        gameMain.drawView.update();
        if (gameBoard.isDie){
            state = STATE_DIE;
            Tools.simpleToast("Game Over");
            gameMain.setGameState(GameMain.GSTATE_PAUSE);
        }
    }

    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        TouchInfo t_Info;
        switch(touchInfo.action){
            case TouchInfo.DOWN:
                touchInfo_S.add(touchInfo);
                if (inGameBoard(touchInfo)){
                    return gameBoard.touchEvent(gameBoardTouch(touchInfo), rawEvent);
                }
                return true;
            case TouchInfo.MOVE:
            case TouchInfo.UP:
                ListIterator<TouchInfo> it = touchInfo_S.listIterator();
                while(it.hasNext()){
                    t_Info = it.next();
                    if (t_Info.id == touchInfo.id){
                        //t_Info has the first position of touchInfo.
                        if (touchInfo.action == TouchInfo.UP){
                            it.remove();
                            if (pauseButton.inObject(t_Info.x, t_Info.y)){
                                if (pauseButton.inObject(touchInfo.x, touchInfo.y)){
                                    state = STATE_PAUSE;
                                    gameMain.setGameState(GameMain.GSTATE_PAUSE);
                                }
                                return true;
                            }
                        }
                        if (inGameBoard(t_Info)){
                            return gameBoard.touchEvent(gameBoardTouch(touchInfo), rawEvent);
                        }
                        return true;
                    }
                }
                return false;
            case TouchInfo.CANCEL:
                touchInfo_S.clear();
                return gameBoard.touchEvent(touchInfo, rawEvent);
            default: return false;
        }
    }
    boolean inGameBoard(TouchInfo touchInfo){
         return 0 <= touchInfo.x &&
                 touchInfo.x < gameBoardW &&
                 gameBoardMargin <= touchInfo.y &&
                 touchInfo.y < gameBoardMargin + gameBoardH;
    }
    TouchInfo gameBoardTouch(TouchInfo touchInfo){ return new TouchInfo(touchInfo.x, touchInfo.y - gameBoardMargin, touchInfo.id, touchInfo.action); }
}

class PauseButton extends DisplayObject{
    int width, height;
    PauseButton(String name, int width, int height){
        super(name);
        this.width = width; this.height = height;
        setPos(0, 0);
        displayBitmap = Tools.roundRectBitmap(width, height, width / 10, MyColor.CYAN);
        Canvas canvas = Tools.newCanvas(displayBitmap);
        float width_2 = width / (float)2;
        float height_2 = height / (float)2;
        float pHeight = height / (float)2;
        float pWidth = pHeight / (float)5;
        float pHeight_2 = pHeight / (float)2;
        float pWidth_2 = pWidth / (float)2;
        Paint paint = Tools.colorPaint(MyColor.WHITE);
        canvas.drawRect(width_2 - pWidth_2 * 3, height_2 - pHeight_2, width_2 - pWidth_2, height_2 + pHeight_2, paint);
        canvas.drawRect(width_2 + pWidth_2, height_2 - pHeight_2, width_2 + pWidth_2 * 3, height_2 + pHeight_2, paint);
        displayBitmapTouch = Bitmap.createBitmap(displayBitmap);
    }
    @Override
    void setPos(int x, int y) {
        xPos = x; yPos = y;
        outputRect = Tools.rectWH(xPos, yPos, width, height);
    }
    @Override
    boolean inObject(float x, float y) { return outputRect.contains((int)x, (int)y); }
}

/*
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
}
*/