package kr.co.lumylumy.linekeeper.main;

import kr.co.lumylumy.linekeeper.MainActivity;
import kr.co.lumylumy.linekeeper.main.*;

import kr.co.lumylumy.linekeeper.timer.TimerAble;
import kr.co.lumylumy.linekeeper.view.SurfaceDrawView.TouchEvent;


/**
 * Created by LMJ on 2017-08-08.
 */

interface GameBase extends TimerAble, TouchEvent, MainActivity.BackKeyReceiver {
    void onStart();
}