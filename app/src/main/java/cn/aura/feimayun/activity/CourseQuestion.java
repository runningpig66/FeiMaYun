package cn.aura.feimayun.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.fragment.MyStuidesInfo22;

public class CourseQuestion extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            setContentView(R.layout.activity_course_question);
            //设置CutoutMode
            if (Build.VERSION.SDK_INT >= 28) {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getWindow().setAttributes(params);
            }
            if (Build.VERSION.SDK_INT >= 28) {
                findViewById(R.id.root).setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets windowInsets) {
                        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                        if (displayCutout != null) {
                            int left = displayCutout.getSafeInsetLeft();
                            int top = displayCutout.getSafeInsetTop();
                            int right = displayCutout.getSafeInsetRight();
                            int bottom = displayCutout.getSafeInsetBottom();
                            findViewById(R.id.view).getLayoutParams().height = top;
                        }
                        return windowInsets.consumeSystemWindowInsets();
                    }
                });
            }

            //返回按钮
            RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
            //标题
            TextView headtitle_textview = findViewById(R.id.headtitle_textview);
            headtitle_textview.setText("课程问答");
            headtitle_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            Intent intent = getIntent();
            String lid = intent.getStringExtra("lid");
            String series_1 = intent.getStringExtra("series_1");
            String series_2 = intent.getStringExtra("series_2");
            MyStuidesInfo22 myStuidesInfo22 = new MyStuidesInfo22();
            Bundle bundle = new Bundle();
            bundle.putString("lid", lid);
            bundle.putString("leimu_1", series_1);
            bundle.putString("leimu_2", series_2);
            myStuidesInfo22.setArguments(bundle);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_layout, myStuidesInfo22);
            transaction.commit();
        }

    }

}
