package cn.aura.feimayun.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * 描述：系统主界面的ViewPager
 */
public class MainActivityViewPager extends ViewPager {

    public MainActivityViewPager(@NonNull Context context) {
        super(context);
    }

    public MainActivityViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //去除页面切换时的滑动翻页效果
    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }
}
