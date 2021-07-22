package com.sq.mobile.hook_fix_plu_second;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

/**
 * 提供getText, getImage和getLayout三个方法，根据R.java中的资源值，得到相应的字符串、图片和布局
 */
public class UIUtil {
    public static String getTextString(Context ctx) {
        return ctx.getResources().getString(R.string.app_name);
    }

    public static Drawable getImageDrawable(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_launcher_background);
    }

    public static View getLayout(Context ctx) {
        return LayoutInflater.from(ctx).inflate(R.layout.activity_main, null);
    }
}
