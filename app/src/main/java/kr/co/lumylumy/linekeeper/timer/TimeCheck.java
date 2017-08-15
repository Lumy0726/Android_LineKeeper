package kr.co.lumylumy.linekeeper.timer;

import android.os.SystemClock;

/**
 * Created by LMJ on 2017-08-15.
 */

public class TimeCheck {
    long startTime;
    float timeAv;
    int num;
    public TimeCheck(){ reset(); }
    public void reset(){ startTime = SystemClock.elapsedRealtime(); }
    public int get(){ return (int)(SystemClock.elapsedRealtime() - startTime); }
    public int getReset(){
        long time = SystemClock.elapsedRealtime();
        int rValue = (int)(time - startTime);
        startTime = time;
        return rValue;
    }
    public float getTimeAv(){
        int time = getReset();
        timeAv = (timeAv * num + time) / (num + 1);
        num++;
        return timeAv;
    }
    public int getSec(){ return get() / 1000; }
    public int getSecReset(){ return getReset() / 1000; }
    public long startTime(){ return startTime; }
}
