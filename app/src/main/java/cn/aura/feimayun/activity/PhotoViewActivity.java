package cn.aura.feimayun.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.MyImageAdapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.view.PhotoViewPager;

public class PhotoViewActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = PhotoViewActivity.class.getSimpleName();
    private PhotoViewPager mViewPager;
    private int currentPosition;
    private MyImageAdapter adapter;
    private TextView mTvImageCount;
    private TextView mTvSaveImage;
    private List<String> Urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            initView();
            initData();
        }

    }

    private void initData() {
        Intent intent = getIntent();
        currentPosition = intent.getIntExtra("currentPosition", 0);
        List_Bean bean = (List_Bean) intent.getSerializableExtra("questionlistdataBean");
        Urls = bean.getStringList();

        adapter = new MyImageAdapter(Urls, this);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(currentPosition, false);
        mTvImageCount.setText(currentPosition + 1 + "/" + Urls.size());
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                mTvImageCount.setText(currentPosition + 1 + "/" + Urls.size());
            }
        });
    }

    private void initView() {
        //设置CutoutMode
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(params);
        }
        if (Build.VERSION.SDK_INT >= 28) {
            findViewById(R.id.root0).setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets windowInsets) {
                    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                    if (displayCutout != null) {
                        int left = displayCutout.getSafeInsetLeft();
                        int top = displayCutout.getSafeInsetTop();
                        int right = displayCutout.getSafeInsetRight();
                        int bottom = displayCutout.getSafeInsetBottom();
                        findViewById(R.id.view1).getLayoutParams().height = top;
                    }
                    return windowInsets.consumeSystemWindowInsets();
                }
            });
        }

        mViewPager = findViewById(R.id.view_pager_photo);
        mTvImageCount = findViewById(R.id.tv_image_count);
        mTvSaveImage = findViewById(R.id.tv_save_image_photo);
        mTvSaveImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_save_image_photo:
                //save image
                break;
        }
    }
}
