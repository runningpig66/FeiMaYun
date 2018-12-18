package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.QuestionDetailRvAdapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.QuestionDetailBean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

public class QuestionDetailActivity extends BaseActivity implements View.OnClickListener {
    private static Handler handleMessage;
    private RecyclerView qutiondetail_recyclerview;
    private QuestionDetailRvAdapter adapter;
    private SmartRefreshLayout qutiondetail_refreshlayout;
    private boolean isFirstIn = true;
    private String tid;
    private String uid;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleMessage = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(QuestionDetailActivity.this, "请检查网络连接_Error09", Toast.LENGTH_LONG).show();
//                    if (progressDialog != null) {
//                        progressDialog.dismiss();
//                    }
                    qutiondetail_refreshlayout.finishRefresh(false);
                } else {
                    parseJsonWithGson(msg.obj.toString());
                }
            }
        };
    }

    private void parseJsonWithGson(String s) {
        Gson gson = new Gson();
        QuestionDetailBean questionDetailBean = gson.fromJson(s, QuestionDetailBean.class);
        int status = questionDetailBean.getStatus();
        if (status == 1) {
            if (isFirstIn) {
                //初始化RecyclerView
                adapter = new QuestionDetailRvAdapter(this, questionDetailBean.getData());
                LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                qutiondetail_recyclerview.setLayoutManager(layoutManager);
                qutiondetail_recyclerview.setAdapter(adapter);
                DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
                divider.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.recyclerview_decoration)));
                qutiondetail_recyclerview.addItemDecoration(divider);
                isFirstIn = false;
            } else {
                adapter.setData(questionDetailBean.getData());
                adapter.notifyDataSetChanged();
            }
            qutiondetail_refreshlayout.finishRefresh(true);
        } else {
            qutiondetail_refreshlayout.finishRefresh(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            handle();

            //返回布局
            RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
            headtitle_layout.setOnClickListener(this);
            //标题
            TextView headtitle_textview = findViewById(R.id.headtitle_textview);
            headtitle_textview.setText("问答详情");
            qutiondetail_recyclerview = findViewById(R.id.qutiondetail_recyclerview);
            qutiondetail_refreshlayout = findViewById(R.id.qutiondetail_refreshlayout);
            qutiondetail_refreshlayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    initData();
                }
            });

            Intent intent = getIntent();
            tid = intent.getStringExtra("tid");
            uid = Util.getUid();

            initData();
        }

    }

    private void initData() {
        if (uid.equals("")) {
            Toast.makeText(this, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
        } else {
            Map<String, String> map = new HashMap<>();
            map.put("tid", tid);
            map.put("uid", uid);
            RequestURL.sendPOST("https://app.feimayun.com/Qa/detail", handleMessage, map);
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
            case 9876:
                if (resultCode == RESULT_OK) {
                    initData();
                }
                break;
        }
    }
}
