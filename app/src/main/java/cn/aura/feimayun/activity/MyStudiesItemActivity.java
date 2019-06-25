package cn.aura.feimayun.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.ExamDeatilActivity_ViewPager1_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.fragment.MyStuidesInfo1;
import cn.aura.feimayun.fragment.MyStuidesInfo2;
import cn.aura.feimayun.fragment.MyStuidesInfo3;
import cn.aura.feimayun.fragment.MyStuidesInfo4;
import cn.aura.feimayun.util.ScreenUtils;

public class MyStudiesItemActivity extends BaseActivity implements View.OnClickListener {
    public MyStuidesInfo4 myStuidesInfo4;
    public MyStuidesInfo2 myStuidesInfo2;
    private LinearLayout mystudies_item_layout1;//tab标签布局
    private ViewPager mystudies_item_viewpager;
    private String[] top_str = new String[]{"我的视频", "我的问答", "我的班级", "我的考试"};
    private int[] top_img_o = new int[]{R.drawable.icon_sp_o_3x, R.drawable.icon_wd_o_3x, R.drawable.icon_bj_o_3x, R.drawable.icon_ks_o_3x};
    private int[] top_img_g = new int[]{R.drawable.icon_sp_g_3x, R.drawable.icon_wd_g_3x, R.drawable.icon_bj_g_3x, R.drawable.icon_ks_g_3x};
    private TextView[] top_textviews = new TextView[4];
    private ImageView[] top_imageviews = new ImageView[4];
    private LinearLayout[] top_linearLayout = new LinearLayout[4];
    private int item_width;
    private String lid, lesson_type, name, series_1, series_2;
    private boolean isFirstIn = true;//控制viewpager第一次进入的标签显示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_studies_item);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            Intent intent = getIntent();
            lid = intent.getStringExtra("lid");
            lesson_type = intent.getStringExtra("lesson_type");
            name = intent.getStringExtra("name");
            series_1 = intent.getStringExtra("series_1");
            series_2 = intent.getStringExtra("series_2");
            initView();
        }

    }

    private void initView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenWidth = displayMetrics.widthPixels;
        item_width = (int) (mScreenWidth / 4.0 + 0.5f);

        //返回布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);
        //标题
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("我的课程");
        //课程名称
        TextView mystudies_item_textview2 = findViewById(R.id.mystudies_item_textview2);
        mystudies_item_textview2.setText(name);
        mystudies_item_layout1 = findViewById(R.id.mystudies_item_layout1);
        mystudies_item_viewpager = findViewById(R.id.mystudies_item_viewpager);
        initNav();
        initViewPager();
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        MyStuidesInfo1 myStuidesInfo1 = new MyStuidesInfo1();//我的视频碎片页
        Bundle bundle1 = new Bundle();
        bundle1.putString("lid", lid);
        myStuidesInfo1.setArguments(bundle1);
        fragments.add(myStuidesInfo1);

        myStuidesInfo2 = new MyStuidesInfo2();
        Bundle bundle2 = new Bundle();
        bundle2.putString("lid", lid);
        bundle2.putString("lesson_type", lesson_type);
        bundle2.putString("series_1", series_1);
        bundle2.putString("series_2", series_2);
        myStuidesInfo2.setArguments(bundle2);
        fragments.add(myStuidesInfo2);

        MyStuidesInfo3 myStuidesInfo3 = new MyStuidesInfo3();
        Bundle bundle3 = new Bundle();
        bundle3.putString("lid", lid);
        myStuidesInfo3.setArguments(bundle3);
        fragments.add(myStuidesInfo3);

        myStuidesInfo4 = new MyStuidesInfo4();
        Bundle bundle4 = new Bundle();
        bundle4.putString("lid", lid);
        myStuidesInfo4.setArguments(bundle4);
        fragments.add(myStuidesInfo4);

        FragmentStatePagerAdapter adapter = new ExamDeatilActivity_ViewPager1_Adapter(getSupportFragmentManager(), fragments);
        mystudies_item_viewpager.setAdapter(adapter);
        mystudies_item_viewpager.setOffscreenPageLimit(3);
        mystudies_item_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (isFirstIn) {
                    if (position == 0) {
                        top_textviews[0].setTextColor(0xffee7708);
                        top_imageviews[0].setImageResource(top_img_o[0]);
                        top_linearLayout[0].setVisibility(View.VISIBLE);
                    }
                    isFirstIn = false;
                }
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < top_textviews.length; i++) {
                    if (position == i) {
                        top_textviews[i].setTextColor(0xffee7708);
                        top_imageviews[i].setImageResource(top_img_o[i]);
                        top_linearLayout[i].setVisibility(View.VISIBLE);
                    } else {
                        top_textviews[i].setTextColor(0xff333333);
                        top_imageviews[i].setImageResource(top_img_g[i]);
                        top_linearLayout[i].setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void initNav() {
        for (int i = 0; i < top_str.length; i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.mystudies_item_tab, null);
            ImageView item_tab_imageview1 = view.findViewById(R.id.item_tab_imageview1);
            TextView item_tab_textview1 = view.findViewById(R.id.item_tab_textview1);
            item_tab_textview1.setText(top_str[i]);
            LinearLayout linearLayout = view.findViewById(R.id.item_tab_layout1);

            //默认选中第一个
            if (i == 0) {
                item_tab_textview1.setTextColor(0xffee7708);
                item_tab_imageview1.setImageResource(top_img_o[i]);
                linearLayout.setVisibility(View.VISIBLE);//灰色圆角显示
            } else {
                item_tab_imageview1.setImageResource(top_img_g[i]);
                linearLayout.setVisibility(View.GONE);
            }

            top_textviews[i] = item_tab_textview1;
            top_imageviews[i] = item_tab_imageview1;
            top_linearLayout[i] = linearLayout;

            RelativeLayout nav_rel = new RelativeLayout(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            nav_rel.addView(view, params);
            nav_rel.setTag(i);
            mystudies_item_layout1.addView(nav_rel, item_width, ScreenUtils.dp2px(this, 70));

            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mystudies_item_viewpager.setCurrentItem(finalI);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.headtitle_layout) {//返回布局
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0x8765:
                if (resultCode == RESULT_OK) {
                    myStuidesInfo2.requestNetwork2();
                }
                break;
            case 0X1231:
                myStuidesInfo4.requestNetwork2();
                break;

        }

    }
}
