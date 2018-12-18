package cn.aura.feimayun.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.AnalysisActivity;
import cn.aura.feimayun.activity.PhotoViewActivity;
import cn.aura.feimayun.adapter.AnalysisActivity_ViewPager1_Fragment_ListView1_Adapter;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.util.ScreenUtils;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class AnalysisActivity_ViewPager1_Fragment extends Fragment {
    private List<String> ana_imgList;//解析图片

    private ListView fragment_analysisactivity_viewpager1_listview1;//题目listview
    private LinearLayout videoLayout;
    private int itemHeight;

    private Map<String, String> listsMap;

    private AnalysisActivity activity;

    private JzvdStd mJzvdStd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysisactivity_viewpager1, null);
        fragment_analysisactivity_viewpager1_listview1 = view.findViewById(R.id.fragment_analysisactivity_viewpager1_listview1);

        itemHeight = ScreenUtils.dp2px(activity, 120);

        Bundle bundle = getArguments();
        List_Bean bean = (List_Bean) bundle.getSerializable("bean");
        listsMap = bean.getMap();

        //解析一下获取到的list，其中包含JSONArray的字符串，需要解析成list或者map进行使用
        initParse();

        //初始化数据，开始加载listview控件信息
        initView(view);
        return view;
    }

    //解析一下获取到的list，其中包含JSONArray的字符串，需要解析成list或者map进行使用
    private void initParse() {
        if (listsMap.get("ana_img") != null) {
            String ana_imgString = "";
            ana_imgString = listsMap.get("ana_img");
            if (!ana_imgString.equals("")) {
                ana_imgList = new ArrayList<>();
                try {
                    JSONTokener jsonTokener = new JSONTokener(ana_imgString);
                    JSONArray ana_imgArray = (JSONArray) jsonTokener.nextValue();
                    for (int i = 0; i < ana_imgArray.length(); i++) {
                        String ana_imgItem = (String) ana_imgArray.get(i);
                        ana_imgList.add(ana_imgItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void initView(View view) {

        videoLayout = view.findViewById(R.id.videoLayout);


        if (listsMap.get("video") == null || listsMap.get("video").equals("")) {
            videoLayout.setVisibility(View.GONE);
        } else {
            videoLayout.setVisibility(View.VISIBLE);
            mJzvdStd = view.findViewById(R.id.jz_video);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mJzvdStd.getLayoutParams();
            layoutParams.height = (int) ((ScreenUtils.getWidth(activity) - ScreenUtils.dp2px(activity, 20)) * 9.0f / 16.0);
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mJzvdStd.setLayoutParams(layoutParams);
            mJzvdStd.setBackgroundColor(Color.WHITE);

            String videoUrl = listsMap.get("video");
            mJzvdStd.setUp(videoUrl, "视频解析", JzvdStd.SCREEN_WINDOW_NORMAL);
//        Glide.with(this).load("http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png").into(mJzvdStd.thumbImageView);
            Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        //正确答案
        TextView fragment_analysisactivity_viewpager1_textview1 = view.findViewById(R.id.fragment_analysisactivity_viewpager1_textview1);
        //错误答案
        TextView fragment_analysisactivity_viewpager1_textview2 = view.findViewById(R.id.fragment_analysisactivity_viewpager1_textview2);
        //解析详情
        TextView fragment_analysisactivity_viewpager1_textview3 = view.findViewById(R.id.fragment_analysisactivity_viewpager1_textview3);
        LinearLayout fragment_analysisactivity_viewpager1_layout1 = view.findViewById(R.id.fragment_analysisactivity_viewpager1_layout1);

        //初始化listview1
        AnalysisActivity_ViewPager1_Fragment_ListView1_Adapter adapter = new AnalysisActivity_ViewPager1_Fragment_ListView1_Adapter(activity, listsMap);
        fragment_analysisactivity_viewpager1_listview1.setAdapter(adapter);
        fragment_analysisactivity_viewpager1_listview1.addFooterView(new FrameLayout(activity));
        fragment_analysisactivity_viewpager1_listview1.addHeaderView(new FrameLayout(activity));
        ViewGroup.LayoutParams params = fragment_analysisactivity_viewpager1_listview1.getLayoutParams();
        params.height = getListviewHeight(fragment_analysisactivity_viewpager1_listview1);
        fragment_analysisactivity_viewpager1_listview1.setLayoutParams(params);
        fragment_analysisactivity_viewpager1_textview1.setText("正确答案:" + listsMap.get("right"));
        fragment_analysisactivity_viewpager1_textview2.setText("您的答案:" + listsMap.get("my_ans"));
        fragment_analysisactivity_viewpager1_textview3.setText(listsMap.get("analysis"));

        //初始化解析图片listview2
        if (ana_imgList == null) {
            fragment_analysisactivity_viewpager1_layout1.setVisibility(View.GONE);
        } else {
            fragment_analysisactivity_viewpager1_layout1.setVisibility(View.VISIBLE);
            fragment_analysisactivity_viewpager1_layout1.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(activity);
            for (int i = 0; i < ana_imgList.size(); i++) {
                ImageView listview_imageview_item_imageview1 = (ImageView) inflater.inflate(R.layout.info2_recyclerview_imageview, null);
                Glide.with(activity).load(ana_imgList.get(i)).into(listview_imageview_item_imageview1);
                LinearLayout linearLayout = new LinearLayout(activity);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, itemHeight);
                linearLayout.addView(listview_imageview_item_imageview1, params2);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params1.topMargin = ScreenUtils.dp2px(activity, 10);
                final int finalI = i;
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, PhotoViewActivity.class);
                        Bundle bundle = new Bundle();
                        List_Bean bean = new List_Bean();
                        bean.setStringList(ana_imgList);
                        bundle.putSerializable("questionlistdataBean", bean);
                        intent.putExtras(bundle);
                        intent.putExtra("currentPosition", finalI);
                        activity.startActivity(intent);
                    }
                });
                fragment_analysisactivity_viewpager1_layout1.addView(linearLayout, params1);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AnalysisActivity) context;
    }

    @Override
    public void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if ((isVisibleToUser && isResumed())) {

        } else if (!isVisibleToUser) {
            Jzvd.goOnPlayOnPause();
        }
    }

    //ListView子项多行文本的情况的测量
    private int getListviewHeight(ListView pull) {
        ListAdapter listAdapter = pull.getAdapter();
        if (listAdapter == null) {
            return 0;
        }

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;

        int totalHeight = 0;
        int listViewWidth = w_screen - ScreenUtils.dp2px(activity, 20);                                         //listView在布局时的宽度
        int widthSpec = View.MeasureSpec.makeMeasureSpec(listViewWidth, View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, pull);
            listItem.measure(widthSpec, 0);

            int itemHeight = listItem.getMeasuredHeight();
            totalHeight += itemHeight;
        }
        // 减掉底部分割线的高度

        return totalHeight + (pull.getDividerHeight() * listAdapter.getCount() - 1);
    }

}
