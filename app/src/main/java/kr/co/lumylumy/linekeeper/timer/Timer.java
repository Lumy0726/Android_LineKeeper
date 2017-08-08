package kr.co.lumylumy.linekeeper.timer;

/**
 * Created by LMJ on 2017-08-07.
 */

import android.os.SystemClock;
import android.os.Handler;
import android.os.Message;

public class Timer {
    public static final int TIMER_MAX = 10;
    int timerNum = 0;
    long[] timeSave = new long[TIMER_MAX];
    int[] timerPeriod = new int[TIMER_MAX];
    int[] timerID = new int[TIMER_MAX];
    int idQueueNum = 0;
    int[] idQueue = new int[TIMER_MAX];
    int[] sendNumQueue = new int[TIMER_MAX];
    TimerHandle timerHandle;
    TimerAble timerClass;

    public Timer(){
        this(null);
    }
    public Timer(TimerAble input){
        timerHandle = new TimerHandle(this);
        timerClass = input;
    }
    public void change(TimerAble input){
        timerClass = input;
    }
    public boolean add(int id, int period){
        int loop1;
        for (loop1 = 0; loop1 < timerNum; loop1++){
            if (timerID[loop1] == id){
                timerPeriod[loop1] = period;
                return true;
            }
        }
        if (loop1 == timerNum && timerNum < TIMER_MAX){
            timerPeriod[timerNum] = period;
            timerID[timerNum] = id;
            timeSave[timerNum++] = SystemClock.elapsedRealtime();
            return true;
        }
        return false;
    }
    public boolean delete(int id){
        for (int loop1=0; loop1 < timerNum; loop1++){
            if (timerID[loop1] == id){
                if (loop1 != timerNum - 1){
                    timerPeriod[loop1] = timerPeriod[timerNum - 1];
                    timerID[loop1] = timerID[timerNum - 1];
                    timeSave[loop1] = timeSave[timerNum - 1];
                }
                timerNum--;
                return true;
            }
        }
        return false;
    }
    public void deleteAll(){ timerNum = 0; }
    public void start(){
        long time= SystemClock.elapsedRealtime();
        for (int loop1 = 0; loop1 < timerNum; loop1++){ timeSave[loop1] = time; }
        timerHandle.start();
    }
    public void stop(){
        timerHandle.stop();
    }
    void timeCheck(){
        if (timerClass != null){
            idQueueNum = 0;
            long time = SystemClock.elapsedRealtime();
            for (int loop1 = 0; loop1 < timerNum; loop1++){
                if (time - timeSave[loop1] >= (long)timerPeriod[loop1]){
                    idQueue[idQueueNum] = timerID[loop1];
                    sendNumQueue[idQueueNum] = 0;
                    do {
                        sendNumQueue[idQueueNum]++;
                        timeSave[loop1]+=(long)timerPeriod[loop1];
                    }while (time - timeSave[loop1] >= (long)timerPeriod[loop1]);
                    idQueueNum++;
                }
            }
            for (int loop1 = 0; loop1 < idQueueNum; loop1++){
                timerClass.onTimer(idQueue[loop1], sendNumQueue[loop1]);
            }
        }
    }
    public static void sendOneTimer(TimerAble input, int id, int time){
        new OneTimerHandle().send(input, id, time);
    }
}

class TimerHandle extends Handler {
    int state = 0;
    Timer timerClass;
    TimerHandle(Timer input){super(); timerClass = input;}
    void stop(){
        state = 0;
    }
    void start(){
        state = 1;
        sendEmptyMessage(0);
    }
    @Override
    public void handleMessage(Message msg){
        if (state == 1){
            timerClass.timeCheck();
            sendEmptyMessageDelayed(0, 1);
        }
    }
}

class OneTimerHandle extends Handler {
    TimerAble timerAble;
    int id;
    void send(TimerAble timerAble, int id, int time){
        if (timerAble != null){
            this.timerAble = timerAble;
            this.id = id;
            sendEmptyMessageDelayed(0, time);
        }
    }
    @Override
    public void handleMessage(Message msg) {
        timerAble.onTimer(id, 1);
    }
}