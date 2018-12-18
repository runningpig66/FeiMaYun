package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.timmy.tdialog.TDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.ExamCardAdapter;
import cn.aura.feimayun.adapter.ExamReusltActivity_DoubleListView_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.SetHeightUtil;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.view.GridItemDecoration;
import cn.aura.feimayun.view.ProgressDialog;

/**
 * 描述：考试结果页
 */
public class ExamResultActivity extends BaseActivity implements View.OnClickListener {
    //请求考试结果页
    private static Handler handleNetwork;
    private static Handler handleAnalysis;
    TextView activity_exam_result_textview2;
    //    private String sid;
    TextView activity_exam_result_textview3;
    private ProgressDialog progressDialog;
    private String tid;
    private TextView activity_exam_result_textview1;//您本次成绩为xx分
    private ListView activity_exam_result_listview1;
    private ListView activity_exam_result_listview2;
    private LinearLayout activity_exam_result_layout2;//按题型解析
    private LinearLayout activity_exam_result_layout3;//按标签解析
    private LinearLayout activity_exam_result_linearLayout1;//答题卡
    private List<Boolean> mList = new LinkedList<>();//记录是否选择
    private List<Map<String, String>> listsList = new ArrayList<>();
    private ExamCardAdapter adapter;
    private RecyclerView answercard_recyclerview;

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(ExamResultActivity.this, "请检查网络连接_Error24", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
//                    activity_paper_list_refreshLayout.finishRefresh(false);
//                    activity_paper_list_refreshLayout.finishLoadMore(false);
                } else {
                    parseJSON(msg.obj.toString());
                }
            }
        };
        handleAnalysis = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(ExamResultActivity.this, "请检查网络连接_Error000", Toast.LENGTH_LONG).show();
//                    activity_paper_list_refreshLayout.finishRefresh(false);
//                    activity_paper_list_refreshLayout.finishLoadMore(false);
                } else {
                    parseAnalysis(msg.obj.toString());
                }
            }
        };
    }

    private void parseAnalysis(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            int status = jsonObject.getInt("status");
            if (status == 1) {
                listsList = new ArrayList<>();
                if (jsonObject.has("data")) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    //TODO 记录正确和错误数量
                    int right = dataObject.getInt("right");
                    int wrong = dataObject.getInt("wrong");
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
                                listsMap.put("is_write", listsObject.getString("is_write"));
                                if (listsMap.get("is_write").equals("1")) {
                                    mList.add(true);
                                } else {
                                    mList.add(false);
                                }
                                listsMap.put("no", listsObject.getString("no"));
                                if (listsObject.has("ana_video")) {
                                    listsMap.put("video", listsObject.getString("ana_video"));
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
                    }
                }
                //获取完后台返回的数据，设置要UI中
                answercard_recyclerview.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setData(mList);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseJSON(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                JSONObject dataObject = jsonObject.getJSONObject("data");
                String score = dataObject.getString("score");
                int tid = dataObject.getInt("tid");

                activity_exam_result_textview2.setText("您本次成绩为");
                //设置显示的总分
                activity_exam_result_textview1.setText(score);
                activity_exam_result_textview3.setText("分");

                //解析typer 按题型统计
                if (dataObject.has("typer")) {
                    JSONArray typerArray = dataObject.getJSONArray("typer");

                    if (typerArray.toString().equals("[]")) {//如果没有数据
                        activity_exam_result_layout2.setVisibility(View.GONE);
                    } else {//有数据
                        //按题型统计list
                        List<Map<String, String>> typerList = new ArrayList<>();
                        for (int i = 0; i < typerArray.length(); i++) {
                            JSONObject typerObject = typerArray.getJSONObject(i);
                            Map<String, String> typerMap = new HashMap<>();
                            typerMap.put("name", typerObject.getString("name"));
                            typerMap.put("total", typerObject.getString("total"));
                            typerMap.put("write", typerObject.getString("write"));
                            typerMap.put("right", typerObject.getString("right"));
                            typerMap.put("wrong", typerObject.getString("wrong"));
                            typerMap.put("rate", typerObject.getString("rate"));
                            typerMap.put("typer", typerObject.getString("typer"));
                            typerList.add(typerMap);

                            activity_exam_result_layout2.setVisibility(View.VISIBLE);
                            ExamReusltActivity_DoubleListView_Adapter adapter = new ExamReusltActivity_DoubleListView_Adapter(this, typerList, String.valueOf(tid));
                            activity_exam_result_listview1.setAdapter(adapter);
                            SetHeightUtil.setListViewHeightBasedOnChildren(activity_exam_result_listview1);
                        }
                    }
                }
                if (dataObject.has("sign")) {
                    //解析sign 按标签统计
                    JSONArray signArray = dataObject.getJSONArray("sign");

                    if (signArray.toString().equals("[]")) {//如果没有数据
                        activity_exam_result_layout3.setVisibility(View.GONE);
                    } else {
                        //按标签统计list
                        List<Map<String, String>> signList = new ArrayList<>();
                        for (int i = 0; i < signArray.length(); i++) {
                            JSONObject signObject = signArray.getJSONObject(i);
                            Map<String, String> signMap = new HashMap<>();
                            signMap.put("name", signObject.getString("name"));
                            signMap.put("wrong", signObject.getString("wrong"));
                            if (!signMap.get("wrong").equals("0")) {
                                signMap.put("wrong_tids", signObject.getString("wrong_tids"));
                            }
                            signMap.put("right", signObject.getString("right"));
                            if (!signMap.get("right").equals("0")) {
                                signMap.put("right_tids", signObject.getString("right_tids"));
                            }
                            signMap.put("write", signObject.getString("write"));
                            signMap.put("tids", signObject.getString("tids"));
                            signMap.put("total", signObject.getString("total"));
                            signMap.put("rate", signObject.getString("rate"));
                            signList.add(signMap);

                            activity_exam_result_layout3.setVisibility(View.VISIBLE);
                            ExamReusltActivity_DoubleListView_Adapter adapter = new ExamReusltActivity_DoubleListView_Adapter(this, signList, String.valueOf(tid));
                            activity_exam_result_listview2.setAdapter(adapter);
                            SetHeightUtil.setListViewHeightBasedOnChildren(activity_exam_result_listview2);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } finally {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_result);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            handler();

            Intent intent = getIntent();
            tid = intent.getStringExtra("id");
//        sid = intent.getStringExtra("sid");

            initView();

            //网络请求、初始化数据
            initData();
        }

    }

    //请求网络
    private void initData() {
        String uid = Util.getUid();
        //请求考试结果
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("tid", tid);
        paramsMap.put("uid", uid);

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        RequestURL.sendPOST("https://app.feimayun.com/Test/index", handleNetwork, paramsMap);
    }

    private void initView() {
        //返回按钮布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);
        //标题
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("我的成绩单");

        activity_exam_result_textview1 = findViewById(R.id.activity_exam_result_textview1);
        activity_exam_result_textview2 = findViewById(R.id.activity_exam_result_textview2);
        activity_exam_result_textview3 = findViewById(R.id.activity_exam_result_textview3);

        activity_exam_result_listview1 = findViewById(R.id.activity_exam_result_listview1);
        activity_exam_result_listview2 = findViewById(R.id.activity_exam_result_listview2);
        activity_exam_result_layout2 = findViewById(R.id.activity_exam_result_layout2);
        activity_exam_result_layout3 = findViewById(R.id.activity_exam_result_layout3);
        activity_exam_result_linearLayout1 = findViewById(R.id.activity_exam_result_linearLayout1);
        activity_exam_result_linearLayout1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.headtitle_layout://左上角返回布局
                finish();
                break;
            case R.id.activity_exam_result_linearLayout1://答题卡
                //请求网络，生成解析数据
                String uid = Util.getUid();
                //请求考试结果
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("tid", tid);
                paramsMap.put("uid", uid);
                RequestURL.sendPOST("https://app.feimayun.com/Test/analysis", handleAnalysis, paramsMap);//异步请求网络
                mList.clear();

                View view = LayoutInflater.from(this).inflate(R.layout.answer_card, null);
                answercard_recyclerview = view.findViewById(R.id.answercard_recyclerview);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5, LinearLayoutManager.VERTICAL, false);
                GridItemDecoration divider = new GridItemDecoration.Builder(this)
                        .setVerticalSpan(R.dimen.dp30)
                        .setHorizontalSpan(R.dimen.dp12)
                        .setColorResource(R.color.white)
                        .setShowLastLine(true)
                        .build();
                answercard_recyclerview.addItemDecoration(divider);
                answercard_recyclerview.setLayoutManager(gridLayoutManager);
                adapter = new ExamCardAdapter(this, mList);
                answercard_recyclerview.setAdapter(adapter);
                adapter.setOnItemClickListener(new ExamCardAdapter.OnItemClickListener() {
                    @Override
                    public void onItemCLick(View view, int position) {
                        //跳转到解析页面
                        Intent intent = new Intent(ExamResultActivity.this, AnalysisActivity.class);
                        Bundle bundle = new Bundle();
                        List_Bean list_bean = new List_Bean();
                        list_bean.setList(listsList);
                        bundle.putSerializable("list_bean", list_bean);
                        bundle.putInt("position", position);//传递viewpager的页号
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });

                new TDialog.Builder(getSupportFragmentManager())
                        .setLayoutRes(R.layout.answer_card)
                        .setDialogView(view)
                        .setScreenHeightAspect(this, 0.6f)
                        .setScreenWidthAspect(this, 1)
                        .setGravity(Gravity.BOTTOM)
                        .setDialogAnimationRes(R.style.animate_dialog)
                        .create()
                        .show();
                break;
        }
    }
}
