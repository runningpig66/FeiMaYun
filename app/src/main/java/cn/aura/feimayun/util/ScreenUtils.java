package cn.aura.feimayun.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 测量屏幕高度工具类
 */
public class ScreenUtils {
    private static final int ORIENTATION_PORTRAIT = 1;
    private static final int ORIENTATION_LANDSCAPE = 2;

    //返回屏幕的宽度
    public static int getWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    //返回屏幕的高度
    public static int getHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    //判断是否在屏幕右侧滑动
    public static boolean isInRight(Activity activity, int xWeight) {
        return xWeight > getWidth(activity) / 2;
    }

    //判断是否在屏幕左侧滑动
    public static boolean isInLeft(Activity activity, int xWeight) {
        return xWeight < getWidth(activity) / 2;
    }

    //判断是否横屏显示
    public static boolean screenIsLandscape(Context context) {
        boolean isLandscape = false;
        switch (context.getResources().getConfiguration().orientation) {
            case ORIENTATION_PORTRAIT:
                isLandscape = false;
                break;
            case ORIENTATION_LANDSCAPE:
                isLandscape = true;
                break;
        }
        return isLandscape;
    }

    //获取当前屏幕方向
    public static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    //获取屏幕密度
    public static float getDensity(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.density;
    }

    /**
     * 将dp值转换为px值
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
