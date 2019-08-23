package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import cn.aura.feimayun.adapter.PaperListActivity_ListView_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.SetHeightUtil;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.view.ProgressDialog;

public class PaperListActivity extends BaseActivity implements View.OnClickListener {
    public static Handler refreshPaper;//计时器后台结束后直接finish不提交，再次进入paper刷新
    private LinearLayout activity_paper_list_layout1;//有试卷的布局
    private RelativeLayout activity_paper_list_layout2;//空白布局
    private RefreshLayout activity_paper_list_refreshLayout;
    private boolean isFirstIn = true;
    private int p = 1;//需要传的页号
    private int currentPosition = 0;//记录当前listview的位置
    private ProgressDialog progressDialog;
    private List<Map<String, String>> listList;//解析list标签的列表
    private PaperListActivity_ListView_Adapter adapter;
    private TextView activity_paper_list_textview1;
    private TextView activity_paper_list_textview2;
    private ListView activity_paper_list_listview1;
    private TextView activity_paper_list_textview3;//单选题标题，需要在没题时隐藏
    private View activity_paper_list_line1;//单选题下划线，需要在没题时隐藏
    private String sid;
    private int clickPosition = -1;
    private int clickP = 0;
    //请求面授详情
    private Handler handleNetwork;
    private Handler handleNetwork2;

    public int getClickPosition() {
        return clickPosition;
    }

    public void setClickPosition(int clickPosition) {
        this.clickPosition = clickPosition;
    }

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(PaperListActivity.this, "请检查网络连接_Error06", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    activity_paper_list_refreshLayout.finishRefresh(false);
                    activity_paper_list_refreshLayout.finishLoadMore(false);
                } else {
                    parseJSON(msg.obj.toString());
                }
            }
        };
        handleNetwork2 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(PaperListActivity.this, "请检查网络连接_Error07", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    activity_paper_list_refreshLayout.finishRefresh(false);
                    activity_paper_list_refreshLayout.finishLoadMore(false);
                } else {
                    parseJSON2(msg.obj.toString());
                }
            }
        };
        refreshPaper = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (clickPosition != -1) {
                    clickP = (clickPosition / 15) + 1;//确定点击的页号，只请求更新当页的数据
                    initData2();
                }
            }
        };
    }

    private void parseJSON(String s) {
//        Util.d("021102", s);
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                JSONObject dataObject = jsonObject.getJSONObject("data");
                JSONObject storeObject = dataObject.getJSONObject("store");
                String name = storeObject.getString("name");
                String about = storeObject.getString("about");
                activity_paper_list_textview1.setText(name);
                activity_paper_list_textview2.setText(about);

                if (isFirstIn) {
                    listList = new ArrayList<>();
                }
                JSONArray listArray = dataObject.getJSONArray("list");
                for (int i = 0; i < listArray.length(); i++) {
                    JSONObject listObject = listArray.getJSONObject(i);
                    Map<String, String> listMap = new HashMap<>();
                    if (listObject.has("id")) {
                        listMap.put("id", listObject.getString("id"));
                    }
                    if (listObject.has("name")) {
                        listMap.put("name", listObject.getString("name"));
                    }
                    if (listObject.has("total")) {
                        listMap.put("total", listObject.getString("total"));
                    }
                    if (listObject.has("type")) {
                        listMap.put("type", listObject.getString("type"));
                        int type = listObject.getInt("type");
                        if (type == 2) {
                            listMap.put("last_id", listObject.getString("last_id"));
                        }
                    }
                    if (listObject.has("type_name")) {
                        listMap.put("type_name", listObject.getString("type_name"));
                    }
                    if (listObject.has("write")) {
                        listMap.put("write", listObject.getString("write"));
                    }
                    if (listObject.has("tp_type")) {
                        listMap.put("tp_type", listObject.getString("tp_type"));
                    }
                    listList.add(listMap);
                }

                //如果单选题试卷数量大于0，显示单选题标题和下划线，理论上status == 1单选题试卷数量一定大于0
                if (listList.size() > 0) {//但保险起见，这里判断一下
                    activity_paper_list_textview3.setVisibility(View.VISIBLE);
                    activity_paper_list_line1.setVisibility(View.VISIBLE);
                }

                if (isFirstIn) {
                    //执行到这一步，由于status == 1，所以说明第一次加载肯定是有单选题试卷的，进行listview初始化
                    adapter = new PaperListActivity_ListView_Adapter(this, listList, sid);
                    activity_paper_list_listview1.setAdapter(adapter);
                    SetHeightUtil.setListViewHeightBasedOnChildren(activity_paper_list_listview1);
                    isFirstIn = false;
                } else {
                    //如果第二次又执行到这里，同样由于status == 1，说明第二次加载还是有单选题试卷的
                    //出现第二次“进入页面”的原因是：下拉刷新和上拉加载更多
                    //刷新会清空数据源，下拉加载会追加数据源，所以这里不用考虑数据源的处理问题
                    adapter.setData(listList);
                    adapter.notifyDataSetChanged();
                    SetHeightUtil.setListViewHeightBasedOnChildren(activity_paper_list_listview1);
                    activity_paper_list_listview1.setSelection(currentPosition);//位置也在刷新和加载中设置了
                }

                activity_paper_list_refreshLayout.finishRefresh(true);
                activity_paper_list_refreshLayout.finishLoadMore(0, true, false);
            } else if (status == 0) {
                //出现status == 0有两种原因
                //一是在第一次“进入页面”时，题库中没有试卷
                //二是在上拉加载更多时，发现没有更多数据
                if (jsonObject.getString("errno").equals("E2000")) {//暂无更多数据的错误码
                    if (isFirstIn) {
                        //如果在第一次加载的时候没有试卷，隐藏并显示相应的布局
                        activity_paper_list_layout1.setVisibility(View.GONE);
                        activity_paper_list_layout2.setVisibility(View.VISIBLE);
                        isFirstIn = false;
                    } else {
//                        //第二次加载失败的情况也是分两种
//                        if (listList.isEmpty()) {//说明在第一次加载的时候没有试卷
//                        } else {//说明在第一次加载的时候有试卷，上拉加载后无更多试卷的情况
//                        }
                        activity_paper_list_refreshLayout.finishRefresh(true);
                    }
                    activity_paper_list_refreshLayout.finishLoadMore(0, true, true);
                } else {
                    activity_paper_list_refreshLayout.finishRefresh(false);
                    activity_paper_list_refreshLayout.finishLoadMore(false);
                }
            }
        } catch (JSONException e) {
            activity_paper_list_refreshLayout.finishRefresh(false);
            activity_paper_list_refreshLayout.finishLoadMore(false);
            e.printStackTrace();
        } finally {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    private void parseJSON2(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                JSONObject dataObject = jsonObject.getJSONObject("data");
                List<Map<String, String>> listList2 = new ArrayList<>();
                JSONArray listArray = dataObject.getJSONArray("list");
                for (int i = 0; i < listArray.length(); i++) {
                    JSONObject listObject = listArray.getJSONObject(i);
                    Map<String, String> listMap = new HashMap<>();
                    if (listObject.has("id")) {
                        listMap.put("id", listObject.getString("id"));
                    }
                    if (listObject.has("name")) {
                        listMap.put("name", listObject.getString("name"));
                    }
                    if (listObject.has("total")) {
                        listMap.put("total", listObject.getString("total"));
                    }
                    if (listObject.has("type")) {
                        listMap.put("type", listObject.getString("type"));
                        int type = listObject.getInt("type");
                        if (type == 2) {
                            listMap.put("last_id", listObject.getString("last_id"));
                        }
                    }
                    if (listObject.has("type_name")) {
                        listMap.put("type_name", listObject.getString("type_name"));
                    }
                    if (listObject.has("write")) {
                        listMap.put("write", listObject.getString("write"));
                    }
                    if (listObject.has("tp_type")) {
                        listMap.put("tp_type", listObject.getString("tp_type"));
                    }
                    listList2.add(listMap);
                }
                Log.i("TAG666", "parseJSON2: clickPosition: " + clickPosition);
                int innerClickPosition = clickPosition % 15;//当页内的position
                Log.i("TAG666", "parseJSON2: innerClickPosition: " + innerClickPosition);
                Map<String, String> mapItem = listList2.get(innerClickPosition);
                listList.set(clickPosition, mapItem);

//                int startShownIndex = activity_paper_list_listview1.getFirstVisiblePosition();
//                int endShownIndex = activity_paper_list_listview1.getLastVisiblePosition();
//                if (clickPosition >= startShownIndex && clickPosition <= endShownIndex) {
//                    View view = activity_paper_list_listview1.getChildAt(clickPosition - startShownIndex);
//                    Log.i("tianyanyuewquywetr", "clickPosition:" + clickPosition);
//                    adapter.getView(clickPosition, view, activity_paper_list_listview1);
//                }
                adapter.notifyDataSetChanged();
                clickPosition = -1;
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (JSONException e) {
            activity_paper_list_refreshLayout.finishRefresh(false);
            activity_paper_list_refreshLayout.finishLoadMore(false);
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_list);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {//正常启动
                handler();

                Intent intent = getIntent();
                sid = intent.getStringExtra("sid");

                initView();
            }
        }

    }

    private void initData() {
        //请求试卷课表
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("sid", sid);
        paramsMap.put("uid", Util.getUid());
        paramsMap.put("p", String.valueOf(p));
        if (isFirstIn) {
            progressDialog = new ProgressDialog(this);
            progressDialog.show();
        }
        RequestURL.sendPOST("https://app.feimayun.com/Tiku/tpList", handleNetwork, paramsMap, PaperListActivity.this);
    }

    private void initData2() {//用来更新单页的数据
        String uid = Util.getUid();

        //请求试卷课表
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("sid", sid);
        paramsMap.put("uid", uid);
        paramsMap.put("p", String.valueOf(clickP));
        if (isFirstIn) {
            progressDialog = new ProgressDialog(this);
            progressDialog.show();
        }
        RequestURL.sendPOST("https://app.feimayun.com/Tiku/tpList", handleNetwork2, paramsMap, PaperListActivity.this);
    }


    private void initView() {
        activity_paper_list_layout1 = findViewById(R.id.activity_paper_list_layout1);
        activity_paper_list_layout2 = findViewById(R.id.activity_paper_list_layout2);
        activity_paper_list_refreshLayout = findViewById(R.id.activity_paper_list_refreshLayout);
        activity_paper_list_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isFirstIn = true;
                currentPosition = 0;
                p = 1;
                initData();
//                activity_paper_list_refreshLayout.finishRefresh(2000, true);
            }
        });
        activity_paper_list_refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {//记录listview当前最后一个元素的位置
                currentPosition = activity_paper_list_listview1.getFirstVisiblePosition();//下拉刷新前记住当前最后一个item的位置
                p++;
                initData();
//                activity_paper_list_refreshLayout.finishLoadMore(2000, true, true);
            }
        });

        //返回按钮布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);
        //标题
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("试卷中心");

        activity_paper_list_textview1 = findViewById(R.id.activity_paper_list_textview1);
        activity_paper_list_textview2 = findViewById(R.id.activity_paper_list_textview2);
        activity_paper_list_listview1 = findViewById(R.id.activity_paper_list_listview1);

        View headerView = LayoutInflater.from(this).inflate(R.layout.danxuanti_title, null);
        activity_paper_list_listview1.addHeaderView(headerView);//添加头
        activity_paper_list_listview1.addFooterView(new LinearLayout(this));//添加尾分割线

        activity_paper_list_textview3 = findViewById(R.id.activity_paper_list_textview3);//单选题标题
        activity_paper_list_line1 = findViewById(R.id.activity_paper_list_line1);//单选题下划线

        initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.headtitle_layout://左上角返回按钮
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0x7777:
                if (resultCode == RESULT_OK) {
                    if (clickPosition != -1) {
                        clickP = (clickPosition / 15) + 1;//确定点击的页号，只请求更新当页的数据
                        initData2();
                    }
                }
                break;
        }
    }
}
