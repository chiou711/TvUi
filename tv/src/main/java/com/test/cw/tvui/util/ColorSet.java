package com.test.cw.tvui.util;
/**
 * Created by CW on 2016/7/16.
 */
import android.app.Activity;
import android.graphics.Color;

import com.test.cw.tvui.R;

public class ColorSet
{
    // style
    // 0,2,4,6,8: dark background, 1,3,5,7,9: light background
    public static int[] mBG_ColorArray = new int[]{Color.rgb(34,34,34), //#222222
            Color.rgb(255,255,255),
            Color.rgb(38,87,51), //#265733
            Color.rgb(186,249,142),
            Color.rgb(87,38,51),//#572633
            Color.rgb(249,186,142),
            Color.rgb(38,51,87),//#263357
            Color.rgb(142,186,249),
            Color.rgb(87,87,51),//#575733
            Color.rgb(249,249,140)};
    public static int[] mText_ColorArray = new int[]{Color.rgb(255,255,255),
            Color.rgb(0,0,0),
            Color.rgb(255,255,255),
            Color.rgb(0,0,0),
            Color.rgb(255,255,255),
            Color.rgb(0,0,0),
            Color.rgb(255,255,255),
            Color.rgb(0,0,0),
            Color.rgb(255,255,255),
            Color.rgb(0,0,0)};

    public static int color_white = Color.rgb(255,255,255);
    public static int color_black = Color.rgb(0,0,0);

    public static int getBarColor(Activity act)
    {
        return act.getResources().getColor(R.color.bar_color);
    }

    public static int getHighlightColor(Activity act)
    {
        return act.getResources().getColor(R.color.highlight_color);
    }

    public static int getPauseColor(Activity act)
    {
        return act.getResources().getColor(R.color.pause_color);
    }
}
