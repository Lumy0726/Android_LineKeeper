package kr.co.lumylumy.linekeeper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import kr.co.lumylumy.linekeeper.MainActivity;
import kr.co.lumylumy.linekeeper.tools.Tools;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView;

/**
 * Created by LMJ on 2017-08-07.
 */

public class GameMain {

    public GameMain(MainActivity activity, SurfaceDrawView drawView){
        Canvas canvas = drawView.setBitmap(Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888));
        Tools.resetBitmap(canvas, 0xffffffff);
        canvas.drawCircle(300, 400, 80, Tools.colorPaint(0xffaaff77, true));
        drawView.update();
    }

    //activity state
    public void activityPause(){

    }
    public void activityResume(){

    }
    public void activityDestroy(){

    }
}
