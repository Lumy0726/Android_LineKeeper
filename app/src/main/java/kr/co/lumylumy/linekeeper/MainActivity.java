package kr.co.lumylumy.linekeeper;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import kr.co.lumylumy.linekeeper.log.LogSystem;
import kr.co.lumylumy.linekeeper.main.GameMain;
import kr.co.lumylumy.linekeeper.tools.MyColor;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;

public class MainActivity extends AppCompatActivity {
    //System value.
    boolean onCreateFlag;
    long backKeyTime;
    //
    GameMain gameMain;

    //onBackKeyDown.
    public interface BackKeyReceiver{
        boolean onBackKeyDown();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateFlag = true;
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (onCreateFlag){
            onCreateFlag = false;
            Tools.tools_initial(this);
            SurfaceDrawView drawView = (SurfaceDrawView) findViewById(R.id.drawView);
            drawView.setBackground(MyColor.hsvColor(0, 0, 30));
            gameMain = new GameMain(this, drawView);
            gameMain.activityResume();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            long pressTime = SystemClock.elapsedRealtime();
            if (gameMain.onBackKeyDown()){ return true; }
            else {
                int timeDiff = (int)(pressTime - backKeyTime);
                if (!(0 < timeDiff && timeDiff < 1500)){
                    backKeyTime = pressTime;
                    Tools.simpleToast(getApplicationContext(), "강제 종료하려면 한번더 누르십시오", 1500);
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onPause() {
        if (gameMain != null) gameMain.activityPause();
        super.onPause();
    }
    @Override
    protected void onResume() {
        if (gameMain != null) gameMain.activityResume();
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        if (gameMain != null) gameMain.activityDestroy();
        super.onDestroy();
    }
}
