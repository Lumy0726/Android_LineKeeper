package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;

import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.tools.TouchInfo;

/**
 * Created by LMJ on 2017-08-08.
 */

class GamePause implements GameBase{
    //gameMain.
    GameMain gameMain;
    //gamePlay.
    GamePlay gamePlay;
    //Bitmap
    Bitmap gamePlayBg;
    Bitmap b_PauseMenu;
    //PauseMenu
    static final int BUTTON_NUM = 4;
    static final String BUTTON_PLAY = "Button_Play";
    static final String BUTTON_RETRY = "Button_Retry";
    static final String BUTTON_SETTING = "Button_Setting";
    static final String BUTTON_GOTOMENU = "Button_GotoMenu";
    PauseMenu[] pauseMenus = new PauseMenu[BUTTON_NUM];
    int pauseMenuNum;
    int buttonsWidth, buttonsHeight;
    int buttonPositionMargin;
    //size value.
    int dv_Width, dv_Height, gp_BoardMargin;
    //
    boolean hideTileState;
    //
    String clickButton;
    //animation.
    static final int STATE_STOP = 0, STATE_INPUT = 1, STATE_OUTPUT = 2;
    int state;
    double speed, speedMinimum;
    double position;

    //constructer
    GamePause(GameMain gameMain, GamePlay gamePlay){
        this.gameMain = gameMain;
        this.gamePlay = gamePlay;
        init();
    }
    void init(){
        //size.
        dv_Width = gameMain.dv_CanvasWidth;
        dv_Height = gameMain.dv_CanvasHeight;
        gp_BoardMargin = gamePlay.gameBoardMargin;
        //
        speedMinimum = (dv_Height - gp_BoardMargin) / (double)(20000 / GameMain.TIMERPERIOD_MAIN);
        //button size.
        int buttonWidth = dv_Width * 2 / 3;
        int buttonHeight = buttonWidth / 4;
        int buttonMargin = buttonHeight / 3;
        buttonsWidth = buttonWidth;
        buttonsHeight = (buttonHeight + buttonMargin) * BUTTON_NUM - buttonMargin;
        buttonPositionMargin = (gamePlay.gameBoard.getTileOutputHeight() - buttonsHeight) / 2;
        //add pauseMenu.
        addPauseMenu(new PauseMenu(BUTTON_PLAY, "계속하기", buttonWidth, buttonHeight, buttonWidth / 14));
        addPauseMenu(new PauseMenu(BUTTON_RETRY, "다시하기", buttonWidth, buttonHeight, buttonWidth / 14));
        addPauseMenu(new PauseMenu(BUTTON_SETTING, "설정", buttonWidth, buttonHeight, buttonWidth / 14));
        addPauseMenu(new PauseMenu(BUTTON_GOTOMENU, "메인메뉴", buttonWidth, buttonHeight, buttonWidth / 14));
        //draw b_PauseMenu.
        b_PauseMenu = Bitmap.createBitmap(buttonsWidth, buttonsHeight + buttonPositionMargin, Bitmap.Config.ARGB_8888);
        Canvas canvas = Tools.newCanvas(b_PauseMenu);
        int xPos = 0;
        int yPos = buttonPositionMargin;
        for (int loop1 = 0; loop1 < BUTTON_NUM; loop1++){
            pauseMenus[loop1].setPos(xPos, yPos);
            pauseMenus[loop1].draw(canvas);
            yPos += buttonHeight + buttonMargin;
        }
        //pauseMenu setPos.
        xPos = (dv_Width - buttonWidth) / 2;
        yPos = gp_BoardMargin + buttonPositionMargin;
        for (int loop1 = 0; loop1 < BUTTON_NUM; loop1++){
            pauseMenus[loop1].setPos(xPos, yPos);
            yPos += buttonHeight + buttonMargin;
        }
    }
    void addPauseMenu(PauseMenu input){
        if (input != null && pauseMenuNum < BUTTON_NUM){
            pauseMenus[pauseMenuNum++] = input;
        }
    }

    @Override
    public void onStart() {
        gamePlayBg = gameMain.drawView.copyCurBitmap();
        switch(gamePlay.state){
            case GamePlay.STATE_PAUSE:
                hideTileState = true;
                break;
            case GamePlay.STATE_DIE:
                hideTileState = false;
                break;
            case GamePlay.STATE_PLAY:
                gameMain.setGameState(GameMain.GSTATE_PLAY);
                return;
        }
        state = STATE_INPUT;
        position = (double)dv_Height;
    }

    //Timer/Touch input.
    @Override
    public void onTimer(int id, int sendNum) {
        switch(state){
            case STATE_INPUT:
                double moveDelta = (position - gp_BoardMargin) / 3;
                if (moveDelta < speedMinimum) moveDelta = speedMinimum;
                position -= moveDelta;
                if ((int)position <= gp_BoardMargin){
                    state = STATE_STOP;
                    position = (double)gp_BoardMargin;
                }
                draw(gameMain.dv_Canvas);
                break;
            case STATE_OUTPUT:
                position += speed;
                if ((int)position > dv_Height){
                    if (clickButton.equals(BUTTON_PLAY)){
                        gameMain.setGameState(GameMain.GSTATE_PLAY);
                    }
                    else if (clickButton.equals(BUTTON_RETRY)){
                        gamePlay.state = GamePlay.STATE_DIE;
                        gameMain.setGameState(GameMain.GSTATE_PLAY);
                    }
                    else if (clickButton.equals(BUTTON_SETTING)){
                        //test code.
                        state = STATE_INPUT;
                    }
                    else if (clickButton.equals(BUTTON_GOTOMENU)){
                        gamePlay.state = GamePlay.STATE_DIE;
                        gameMain.setGameState(GameMain.GSTATE_MENU);
                    }
                }
                else {
                    draw(gameMain.dv_Canvas);
                }
                break;
        }
    }
    void draw(Canvas canvas){
        canvas.drawBitmap(gamePlayBg, 0, 0, null);
        if (hideTileState){
            canvas.drawBitmap(gameMain.gameMenu.background, 0, (int)position, null);
            canvas.drawBitmap(b_PauseMenu, (dv_Width - buttonsWidth) / 2, (int)position, null);
        }
        else {
            canvas.drawBitmap(b_PauseMenu, (dv_Width - buttonsWidth) / 2, (int)position, null);
        }
        gameMain.drawView.update();
    }
    void draw_ButtonUpdate(Canvas canvas){
        canvas.drawBitmap(gamePlayBg, 0, 0, null);
        if (hideTileState){
            canvas.drawBitmap(gameMain.gameMenu.background, 0, (int)position, null);
            for (PauseMenu pauseMenu : pauseMenus) pauseMenu.draw(canvas);
        }
        else {
            for (PauseMenu pauseMenu : pauseMenus) pauseMenu.draw(canvas);
        }
        gameMain.drawView.update();
    }
    @Override
    public boolean touchEvent(TouchInfo touchInfo, MotionEvent rawEvent) {
        if (state == STATE_STOP){
            PauseMenu pauseMenu = null;
            switch (touchInfo.action) {
                case TouchInfo.DOWN:
                    for (int loop1 = 0; loop1 < BUTTON_NUM; loop1++) {
                        pauseMenu = pauseMenus[loop1];
                        if (pauseMenu.inObject(touchInfo.x, touchInfo.y)) {
                            pauseMenu.onTouch(touchInfo.id);
                            draw_ButtonUpdate(gameMain.dv_Canvas);
                            break;
                        }
                    }
                    break;
                case TouchInfo.UP:
                    for (int loop1 = 0; loop1 < BUTTON_NUM; loop1++) {
                        pauseMenu = pauseMenus[loop1];
                        if (pauseMenu.isTouch() && pauseMenu.touchId == touchInfo.id){
                            pauseMenu.offTouch();
                            draw_ButtonUpdate(gameMain.dv_Canvas);
                            if (pauseMenu.inObject(touchInfo.x, touchInfo.y)) {
                                clickButton = pauseMenu.name();
                                if (gamePlay.state == GamePlay.STATE_DIE && clickButton.equals(BUTTON_PLAY)){//impossible click.
                                    return true;
                                }
                                state = STATE_OUTPUT;
                                speed = (dv_Height - gp_BoardMargin) / (double)(500 / GameMain.TIMERPERIOD_MAIN);
                            }
                            break;
                        }
                    }
                    break;
                case TouchInfo.CANCEL:
                    for (int loop1 = 0; loop1 < BUTTON_NUM; loop1++) {
                        pauseMenus[loop1].offTouch();
                        pauseMenus[loop1].draw(gameMain.dv_Canvas);
                    }
                    gameMain.drawView.update();
                    break;
            }
            return true;
        }
        return true;
    }
    @Override
    public boolean onBackKeyDown() { return false; }
}

class PauseMenu extends DO_HexView{
    PauseMenu(String name, String str, int width, int height, int edgeWidth) { this(name, str, 0, 0, width, height, edgeWidth); }
    PauseMenu(String name, String str, int xPos, int yPos, int width, int height, int edgeWidth) {
        super(
                name, str,
                xPos, yPos,
                width, height, edgeWidth,
                new Paint(){
                    Paint f(int height){
                        setShader(new LinearGradient(0, 0, 0, height, MyColor.hsvColor(0xa0, 150, 80, 100), MyColor.hsvColor(0xa0, 180, 80, 80), Shader.TileMode.CLAMP));
                        return this;
                    }
                }.f(height),
                MyColor.BLACK
        );
    }
}