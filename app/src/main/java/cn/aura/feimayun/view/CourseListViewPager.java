package cn.aura.feimayun.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class CourseListViewPager extends ViewPager {

    public CourseListViewPager(@NonNull Context context) {
        super(context);
    }

    public CourseListViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //取消碎片切换的过渡动画
    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }
}
