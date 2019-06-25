package cn.aura.app.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.aura.app.R;

/**
 * 手势指导页面。
 * 主要在答题和查看解析使用
 */

public class MyGuideView extends LinearLayout {

    //三个文字显示
    private TextView mBrightText, mProgressText, mVolumeText;

    public MyGuideView(Context context) {
        super(context);
        init();
    }

    public MyGuideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyGuideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //设置页面布局
        setBackgroundColor(Color.parseColor("#88000000"));
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.my_view_guide, this, true);

        //这几个文字有颜色的变化
        mBrightText = (TextView) findViewById(R.id.bright_text);
        mProgressText = (TextView) findViewById(R.id.progress_text);
        mVolumeText = (TextView) findViewById(R.id.volume_text);

        //默认是隐藏的
//        hide();

//        setScreenMode();
        setVisibility(VISIBLE);
    }

    public void setScreenMode() {
//只有第一次进入全屏的时候显示。通过SharedPreferences记录这个值。
        SharedPreferences spf = getContext().getSharedPreferences("my_guide_record", Context.MODE_PRIVATE);
        boolean hasShown = spf.getBoolean("has_shown", false);
        //如果已经显示过了，就不接着走了
        if (hasShown) {
            return;
        } else {
            setVisibility(VISIBLE);
        }
        //记录下来
        SharedPreferences.Editor editor = spf.edit();
        editor.putBoolean("has_shown", true);
        editor.apply();
    }

    /**
     * 隐藏不显示
     */
    public void hide() {
        setVisibility(GONE);
    }

    /**
     * 手势点击到了就隐藏
     *
     * @param event 触摸事件
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hide();
        return true;
    }

}
