package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.ExamListAcitivty_ListView1_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.view.ProgressDialog;

/**
 * 描述：
 */
public class ExamListActivity extends BaseActivity implements View.OnClickListener {
    private SmartRefreshLayout activityExamList_refreshLayout;

    private ExamListAcitivty_ListView1_Adapter adapter;
    private int currentPosition = 0;//记录当前listview的位置
    private int p = 1;//记录当前的页号

    private ProgressDialog progressDialog;

    private ListView activity_exam_list_listview1;
    private Handler handlerTiku;
    private boolean isFirstIn = true;
    private List<Map<String, String>> dataList;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handlerTiku = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(ExamListActivity.this, "请检查网络连接_Error23", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    activityExamList_refreshLayout.finishRefresh(false);
                    activityExamList_refreshLayout.finishLoadMore(false);
                } else {
                    parseTiku(msg.obj.toString());
                }
            }
        };
    }

    //解析题库列表
    private void parseTiku(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                if (isFirstIn) {
                    dataList = new ArrayList<>();
                }
                //解析data
                //刷新默认只显示第一页数据
                JSONArray dataArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    Map<String, String> map = new HashMap<>();
                    map.put("id", dataObject.getString("id"));
                    map.put("name", dataObject.getString("name"));
                    map.put("about", dataObject.getString("about"));
//                    map.put("bg_img", dataObject.getString("bg_img"));
                    map.put("is_sell", dataObject.getString("is_sell"));
                    map.put("tp_total", dataObject.getString("tp_total"));
                    map.put("test_total", dataObject.getString("test_total"));
                    map.put("total", dataObject.getString("total"));
                    if (dataObject.has("tpaper_name")) {
                        JSONArray tpaperNameArray = dataObject.getJSONArray("tpaper_name");
                        map.put("tpaper_name", tpaperNameArray.toString());
                    } else {
                        map.put("tpaper_name", "");
                    }
                    dataList.add(map);
                }

                if (isFirstIn) {
                    //拿到数据，初始化控件内容
                    adapter = new ExamListAcitivty_ListView1_Adapter(this, dataList);
                    activity_exam_list_listview1.setAdapter(adapter);
                    isFirstIn = false;
                } else {
                    adapter.setData(dataList);
                    adapter.notifyDataSetChanged();
                }
                activityExamList_refreshLayout.finishRefresh(true);
                activityExamList_refreshLayout.finishLoadMore(0, true, false);
                activity_exam_list_listview1.setSelection(currentPosition);//这里的位置可以共用，因为刷新和加载更多的时候都处理过
            } else if (status == 0) {
                String errno = jsonObject.getString("errno");
                if (errno.equals("E2000")) {//暂无更多数据
                    activityExamList_refreshLayout.finishLoadMore(0, true, true);
                }
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            activityExamList_refreshLayout.finishRefresh(false);
            activityExamList_refreshLayout.finishLoadMore(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_list);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            handle();
            initView();

            progressDialog = new ProgressDialog(this);
            progressDialog.show();

            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("p", String.valueOf(p));
            RequestURL.sendPOST("https://app.feimayun.com/Tiku/index", handlerTiku, paramsMap);
        }

    }

    private void initView() {
        activityExamList_refreshLayout = findViewById(R.id.activityExamList_refreshLayout);
        activityExamList_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isFirstIn = true;
                currentPosition = 0;
                p = 1;
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("p", String.valueOf(p));
                RequestURL.sendPOST("https://app.feimayun.com/Tiku/index", handlerTiku, paramsMap);
            }
        });
        activityExamList_refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                currentPosition = activity_exam_list_listview1.getFirstVisiblePosition();//下拉刷新前记住当前最后一个item的位置
                p++;
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("p", String.valueOf(p));
                RequestURL.sendPOST("https://app.feimayun.com/Tiku/index", handlerTiku, paramsMap);
            }
        });
        //返回按钮布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);
        //标题
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("考试中心");
        activity_exam_list_listview1 = findViewById(R.id.activity_exam_list_listview1);
        //添加头和尾的分割线，这里不设置会不显示
        activity_exam_list_listview1.addFooterView(new FrameLayout(this));
        activity_exam_list_listview1.addHeaderView(new FrameLayout(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.headtitle_layout://左上角返回按钮
                finish();
                break;
        }
    }
}
