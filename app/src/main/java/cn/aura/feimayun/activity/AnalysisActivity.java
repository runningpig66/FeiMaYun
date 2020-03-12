package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.ExamDeatilActivity_ViewPager1_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.fragment.AnalysisActivity_ViewPager1_Fragment;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.view.JZExoPlayer;
import cn.aura.feimayun.view.MyGuideView;
import cn.aura.feimayun.view.ProgressDialog;
import cn.jzvd.Jzvd;

/**
 * 描述：答案解析
 */
public class AnalysisActivity extends BaseActivity implements View.OnClickListener {
    //请求按题型解析
    private static Handler handleNetwork1;
    //请求按标签解析
    private static Handler handleNetwork2;
    private ProgressDialog progressDialog;
    private ViewPager activity_analysis_viewpager1;//显示内容的viewpager
    private ImageView activity_analysis_imageview1;//上一页
    private ImageView activity_analysis_imageview2;//下一页
    private List<Fragment> fragmentList;
    private List<Map<String, String>> listsList = new ArrayList<>();
    private String tid;
    private String typer;
    private String type;
    private String tids;

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleNetwork1 = new Handler() {//请求网络，按题型分析
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(AnalysisActivity.this, "请检查网络连接_Error17", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parseJSON1(msg.obj.toString());
                }
            }
        };
        handleNetwork2 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(AnalysisActivity.this, "请检查网络连接_Error18", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parseJSON2(msg.obj.toString());
                }
            }
        };
    }

    //按标签解析
    private void parseJSON2(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                listsList = new ArrayList<>();
                if (jsonObject.has("data")) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");

                    if (dataObject.has("radio")) {
                        JSONObject radioObject = dataObject.getJSONObject("radio");
                        if (radioObject.has("lists")) {
                            JSONArray listsArray = radioObject.getJSONArray("lists");
                            for (int i = 0; i < listsArray.length(); i++) {
                                JSONObject listsObject = listsArray.getJSONObject(i);
                                Map<String, String> listsMap = new HashMap<>();
                                listsMap.put("id", listsObject.getString("id"));
                                listsMap.put("company_id", listsObject.getString("company_id"));
                                listsMap.put("subject", listsObject.getString("subject"));
                                listsMap.put("options", listsObject.getString("options"));
                                listsMap.put("right", listsObject.getString("right"));
                                listsMap.put("analysis", listsObject.getString("analysis"));
                                listsMap.put("my_ans", listsObject.getString("my_ans"));
                                listsMap.put("no", listsObject.getString("no"));
                                if (listsObject.has("video")) {
                                    listsMap.put("video", listsObject.getString("video"));
                                }
//                                listsMap.put("tpid", listsObject.getString("tpid"));
                                if (listsObject.has("sub_img")) {//题干图片
                                    JSONArray sub_imgArray = listsObject.getJSONArray("sub_img");
                                    listsMap.put("sub_img", sub_imgArray.toString());
                                }
                                if (listsObject.has("option_list")) {//选项
                                    JSONArray option_listArray = listsObject.getJSONArray("option_list");
                                    listsMap.put("option_list", option_listArray.toString());
                                }
                                if (listsObject.has("ana_img")) {//解析图片
                                    JSONArray ana_imgArray = listsObject.getJSONArray("ana_img");
                                    listsMap.put("ana_img", ana_imgArray.toString());
                                }
                                listsList.add(listsMap);
                            }
                        }
                    } else if (dataObject.has("short")) {
                        Toast.makeText(this, "请到PC端查看~", Toast.LENGTH_SHORT).show();
                        if (activity_analysis_imageview2 != null) {
                            activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                        }
                    }
                }

                //初始化ViewPager
                fragmentList = new ArrayList<>();
                for (int i = 0; i < listsList.size(); i++) {
                    AnalysisActivity_ViewPager1_Fragment fragment = new AnalysisActivity_ViewPager1_Fragment();

                    List_Bean bean = new List_Bean();
                    bean.setMap(listsList.get(i));

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("bean", bean);
                    fragment.setArguments(bundle);
                    fragmentList.add(fragment);
                }

                ExamDeatilActivity_ViewPager1_Adapter adapter = new ExamDeatilActivity_ViewPager1_Adapter(getSupportFragmentManager(), fragmentList);
                activity_analysis_viewpager1.setAdapter(adapter);
                activity_analysis_viewpager1.setOffscreenPageLimit(2);
                activity_analysis_viewpager1.setCurrentItem(0);
                activity_analysis_viewpager1.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == 0) {
                            if (listsList.size() == 1) {
                                activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                                activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                            }
                        }
                    }

                    @Override
                    public void onPageSelected(int position) {
                        if (listsList.size() > 1) {
                            if (position == 0) {
                                activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                                activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                            } else if (position == listsList.size() - 1) {
                                activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                                activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                            } else {
                                activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                                activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                            }
                        } else {
                            activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                            activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }

    }

    //按题型解析
    private void parseJSON1(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                listsList = new ArrayList<>();
                if (jsonObject.has("data")) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    String name = "";
                    if (dataObject.has("name")) {
                        name = dataObject.getString("name");
                    }
                    if (name.equals("简答题")) {
                        Toast.makeText(this, "请到PC端查看~", Toast.LENGTH_SHORT).show();
                        if (activity_analysis_imageview2 != null) {
                            activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                        }
                    } else if (name.equals("单选题")) {
                        if (dataObject.has("lists")) {
                            JSONArray listsArray = dataObject.getJSONArray("lists");
                            for (int i = 0; i < listsArray.length(); i++) {
                                JSONObject listsObject = listsArray.getJSONObject(i);
                                Map<String, String> listsMap = new HashMap<>();
                                listsMap.put("id", listsObject.getString("id"));
                                listsMap.put("company_id", listsObject.getString("company_id"));
                                listsMap.put("subject", listsObject.getString("subject"));
                                listsMap.put("options", listsObject.getString("options"));
                                listsMap.put("right", listsObject.getString("right"));
                                listsMap.put("analysis", listsObject.getString("analysis"));
                                listsMap.put("my_ans", listsObject.getString("my_ans"));
                                listsMap.put("no", listsObject.getString("no"));
                                listsMap.put("tpid", listsObject.getString("tpid"));
                                if (listsObject.has("video")) {
                                    listsMap.put("video", listsObject.getString("video"));
                                }
                                if (listsObject.has("sub_img")) {//题干图片
                                    JSONArray sub_imgArray = listsObject.getJSONArray("sub_img");
                                    listsMap.put("sub_img", sub_imgArray.toString());
                                }
                                if (listsObject.has("option_list")) {//选项
                                    JSONArray option_listArray = listsObject.getJSONArray("option_list");
                                    listsMap.put("option_list", option_listArray.toString());
                                }
                                if (listsObject.has("ana_img")) {//解析图片
                                    JSONArray ana_imgArray = listsObject.getJSONArray("ana_img");
                                    listsMap.put("ana_img", ana_imgArray.toString());
                                }
                                listsList.add(listsMap);
                            }
                        }
                    }

                }

                //初始化ViewPager
                fragmentList = new ArrayList<>();
                for (int i = 0; i < listsList.size(); i++) {
                    AnalysisActivity_ViewPager1_Fragment fragment = new AnalysisActivity_ViewPager1_Fragment();

                    List_Bean bean = new List_Bean();
                    bean.setMap(listsList.get(i));

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("bean", bean);
                    fragment.setArguments(bundle);
                    fragmentList.add(fragment);
                }

                ExamDeatilActivity_ViewPager1_Adapter adapter = new ExamDeatilActivity_ViewPager1_Adapter(getSupportFragmentManager(), fragmentList);
                activity_analysis_viewpager1.setAdapter(adapter);
                activity_analysis_viewpager1.setOffscreenPageLimit(4);
                activity_analysis_viewpager1.setCurrentItem(0);
                activity_analysis_viewpager1.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == 0) {
                            if (listsList.size() == 1) {
                                activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                                activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                            }
                        }
                    }

                    @Override
                    public void onPageSelected(int position) {
                        if (listsList.size() > 1) {
                            if (position == 0) {
                                activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                                activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                            } else if (position == listsList.size() - 1) {
                                activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                                activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                            } else {
                                activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                                activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                            }
                        } else {
                            activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                            activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {//正常启动

            SharedPreferences spf = getSharedPreferences("my_guide_record", Context.MODE_PRIVATE);
            boolean hasShown = spf.getBoolean("has_shown_analysis", false);

            if (!hasShown) {
                ViewGroup root = getWindow().getDecorView().findViewById(R.id.root1);
                MyGuideView myGuideView = new MyGuideView(this);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                root.addView(myGuideView, params);
            }
            //记录下来
            SharedPreferences.Editor editor = spf.edit();
            editor.putBoolean("has_shown_analysis", true);
            editor.apply();

            Jzvd.setMediaInterface(new JZExoPlayer());

            handler();

            Intent intent = getIntent();
            tid = intent.getStringExtra("tid");
            typer = intent.getStringExtra("typer");
            type = intent.getStringExtra("type");//1正题2错题
            tids = intent.getStringExtra("tids");
            Bundle bundle = intent.getExtras();

            initView();

            int position = -1;
            if (bundle != null) {
                List_Bean list_bean = (List_Bean) bundle.getSerializable("list_bean");
                if (list_bean != null) {
                    listsList = list_bean.getList();
                }
                position = bundle.getInt("position", -1);
            }

            if (position != -1) {
                initData3(position);
            } else {
                progressDialog = new ProgressDialog(this);
                progressDialog.show();

                if (typer == null) {//如果是按题型分析
                    initData2();
                } else {//如果是按标签分析
                    //网络请求、初始化数据
                    initData1();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    private void initView() {
        //左上角返回按钮
        //返回按钮布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);
        //标题栏
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("考试中心");

        activity_analysis_viewpager1 = findViewById(R.id.activity_analysis_viewpager1);
        //上一页布局
        RelativeLayout activity_analysis_layout3 = findViewById(R.id.activity_analysis_layout3);
        activity_analysis_layout3.setOnClickListener(this);
        //下一页布局
        RelativeLayout activity_analysis_layout4 = findViewById(R.id.activity_analysis_layout4);
        activity_analysis_layout4.setOnClickListener(this);
        activity_analysis_imageview1 = findViewById(R.id.activity_analysis_imageview1);
        activity_analysis_imageview2 = findViewById(R.id.activity_analysis_imageview2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.headtitle_layout://左上角返回按钮
                finish();
                break;
            case R.id.activity_analysis_layout3://上一页
                int position1 = activity_analysis_viewpager1.getCurrentItem();
                if (position1 - 1 == -1) {
                    Toast.makeText(this, "已经是第一题了", Toast.LENGTH_SHORT).show();
                } else {
                    activity_analysis_viewpager1.setCurrentItem(position1 - 1);
                }
                break;
            case R.id.activity_analysis_layout4://下一页
                int position2 = activity_analysis_viewpager1.getCurrentItem();
                if (position2 + 1 == listsList.size()) {
                    Toast.makeText(this, "已经是最后一题了", Toast.LENGTH_SHORT).show();
                } else {
                    activity_analysis_viewpager1.setCurrentItem(position2 + 1);
                }
                break;
        }
    }

    //请求网络，按题型分析
    private void initData1() {
        String uid = Util.getUid();

        //请求考试结果
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("tid", tid);
        paramsMap.put("uid", Util.getUid());
        paramsMap.put("typer", typer);
        paramsMap.put("type", type);
        RequestURL.sendPOST("https://app.feimayun.com/Test/alsTyper", handleNetwork1, paramsMap, AnalysisActivity.this);
    }

    //请求网络，按标签分析
    private void initData2() {
        String uid = Util.getUid();

        //请求考试结果
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("id", tid);
        paramsMap.put("uid", Util.getUid());
        paramsMap.put("tids", tids);
        paramsMap.put("type", type);
        RequestURL.sendPOST("https://app.feimayun.com/Test/alsSign", handleNetwork2, paramsMap, AnalysisActivity.this);
    }

    //请求所有题目
    private void initData3(int position) {
        //初始化ViewPager
        fragmentList = new ArrayList<>();
        for (int i = 0; i < listsList.size(); i++) {
            AnalysisActivity_ViewPager1_Fragment fragment = new AnalysisActivity_ViewPager1_Fragment();

            List_Bean bean = new List_Bean();
            bean.setMap(listsList.get(i));

            Bundle bundle = new Bundle();
            bundle.putSerializable("bean", bean);
            fragment.setArguments(bundle);
            fragmentList.add(fragment);
        }

        ExamDeatilActivity_ViewPager1_Adapter adapter = new ExamDeatilActivity_ViewPager1_Adapter(getSupportFragmentManager(), fragmentList);
        activity_analysis_viewpager1.setAdapter(adapter);
        activity_analysis_viewpager1.setOffscreenPageLimit(2);
        activity_analysis_viewpager1.setCurrentItem(position);
        activity_analysis_viewpager1.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (listsList.size() > 1) {
                    if (position == 0) {
                        activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                        activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                    } else if (position == listsList.size() - 1) {
                        activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                        activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                    } else {
                        activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                        activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                    }
                } else {
                    activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                    activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (listsList.size() > 1) {
                    if (position == 0) {
                        activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                        activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                    } else if (position == listsList.size() - 1) {
                        activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                        activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                    } else {
                        activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                        activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                    }
                } else {
                    activity_analysis_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                    activity_analysis_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}