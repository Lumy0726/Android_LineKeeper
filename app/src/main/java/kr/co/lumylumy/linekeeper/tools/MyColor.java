package kr.co.lumylumy.linekeeper.tools;

/**
 * Created by LMJ on 2017-08-09.
 */

public class MyColor {
    public static final int
            //BLACK
            BLACK = 0xff000000,
    //pure color
            RED = 0xffff0000,
            YELLOW = 0xffffff00,
            GREEN = 0xff00ff00,
            CYAN = 0xff00ffff,
            BLUE = 0xff0000ff,
            MAGENTA = 0xffff00ff,
    //color
            temp = 0xffddff00,
    //WHITE
            WHITE = 0xffffffff;
    public static final int
            ALPHA_NONE = 0,
            ALPHA_VVLOW = 0x20,
            ALPHA_VLOW = 0x40,
            ALPHA_LOW = 0x60,
            ALPHA_MIDDLE = 0x7f,
            ALPHA_HIGH = 0x9f,
            ALPHA_VHIGH = 0xbf,
            ALPHA_VVHIGH = 0xdf,
            ALPHA_FULL = 0xff;
    public static int aColor(int alpha, int color){ return (color & 0x00ffffff) | (alpha << 24); }
    public static int hsvColor(int h, int s, int v){
        if (h < 0 || 360 <= h) return 0;
        if (s < 0 || 100 < s) return 0;
        if (v < 0 || 100 < v) return 0;
        int max = 0xff * v / 100;
        int low = max * (100 - s) / 100;
        int middle = low + (max - low) * (60 - Math.abs(h % 120 - 60)) / 60;
        switch(h / 60){
            case 0:
                return 0xff_00_00_00 | max << 16 | middle << 8 | low;
            case 1:
                return 0xff_00_00_00 | middle << 16 | max << 8 | low;
            case 2:
                return 0xff_00_00_00 | low << 16 | max << 8 | middle;
            case 3:
                return 0xff_00_00_00 | low << 16 | middle << 8 | max;
            case 4:
                return 0xff_00_00_00 | middle << 16 | low << 8 | max;
            case 5:
                return 0xff_00_00_00 | max << 16 | low << 8 | middle;
        }
        return 0;
    }
}
