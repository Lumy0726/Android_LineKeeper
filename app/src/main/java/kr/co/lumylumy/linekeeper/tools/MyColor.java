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
}
