package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
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
import cn.aura.feimayun.adapter.LiveListActivityListViewAdapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.vhall.watch.WatchActivity;
import cn.aura.feimayun.view.ProgressDialog;

public class LiveListActivity extends BaseActivity implements View.OnClickListener {
    private static Handler handleNetwork;
    private ProgressDialog progressDialog;
    private String is_live;
    private SmartRefreshLayout activityLivelist_refreshLayout;
    private LiveListActivityListViewAdapter adapter;
    private boolean isFirstIn = true;
    private ListView activity_livelist_listview;
    private String series_1;
    private List<Map<String, String>> dataList;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(LiveListActivity.this, "请检查网络连接_Error11", Toast.LENGTH_LONG).show();
                    activityLivelist_refreshLayout.finishRefresh(false);
                } else {
                    parseNetwork(msg.obj.toString());
                }
            }
        };
    }

    private void parseNetwork(String s) {
//        Util.d("081301", s);
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//成功
                dataList = new ArrayList<>();
                if (jsonObject.has("data") && !(jsonObject.get("data").equals(JSONObject.NULL))) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap.put("id", dataObject.getString("id"));
                        dataMap.put("name", dataObject.getString("name"));
                        dataMap.put("start_ts", dataObject.getString("start_ts"));
                        dataMap.put("bg_url", dataObject.getString("bg_url"));
                        dataMap.put("browse", dataObject.getString("browse"));
                        dataMap.put("teach_type", dataObject.getString("teach_type"));
                        dataMap.put("data_id", dataObject.getString("data_id"));
                        dataMap.put("liveStatus", dataObject.getString("liveStatus"));
                        dataMap.put("stat", dataObject.getString("stat"));
                        dataMap.put("tea_name", dataObject.getString("tea_name"));
                        dataList.add(dataMap);
                    }
                }

                if (isFirstIn) {
                    if (dataList.isEmpty()) {//理论status == 1列表一定大于0
                        activity_livelist_listview.setVisibility(View.GONE);
                        //空布局
                        RelativeLayout activity_livelist_layout1 = findViewById(R.id.activity_livelist_layout1);
                        activity_livelist_layout1.setVisibility(View.VISIBLE);
                    } else {
                        //拿到数据初始化listview
                        adapter = new LiveListActivityListViewAdapter(this, dataList);
                        activity_livelist_listview.setAdapter(adapter);
                        activity_livelist_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                int headerViewsCount = activity_livelist_listview.getHeaderViewsCount();
                                int newPosition = position - headerViewsCount;
                                Intent intent = new Intent(LiveListActivity.this, WatchActivity.class);
                                intent.putExtra("data_id", dataList.get(newPosition).get("id"));
                                intent.putExtra("data_teach_type", dataList.get(newPosition).get("teach_type"));
                                startActivity(intent);
                            }
                        });
                    }
                    isFirstIn = false;
                } else {
                    if (adapter != null) {
                        adapter.setData(dataList);
                        adapter.notifyDataSetChanged();
                    }
                }
            } else {
                String msg = jsonObject.getString("msg");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
            activityLivelist_refreshLayout.finishRefresh(true);
        } catch (JSONException e) {
            e.printStackTrace();
            activityLivelist_refreshLayout.finishRefresh(false);
        } finally {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livelist);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            handle();

            Intent intent = getIntent();
            series_1 = intent.getStringExtra("series_1");
            is_live = intent.getStringExtra("is_live");

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
                            findViewById(R.id.view).getLayoutParams().height = top;
                        }
                        return windowInsets.consumeSystemWindowInsets();
                    }
                });
            }

            activityLivelist_refreshLayout = findViewById(R.id.activityLivelist_refreshLayout);
            activityLivelist_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("series_1", series_1);
                    paramsMap.put("teach_type", "1");
                    if (is_live.equals("1")) {
                        paramsMap.put("is_live", "1");
                    }
                    RequestURL.sendPOST("https://app.feimayun.com/Lesson/index", handleNetwork, paramsMap, LiveListActivity.this);
                }
            });
            //返回
            RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
            headtitle_layout.setOnClickListener(this);
            //标题
            TextView headtitle_textview = findViewById(R.id.headtitle_textview);
            headtitle_textview.setText("直播列表");

            activity_livelist_listview = findViewById(R.id.activity_livelist_listview);
            activity_livelist_listview.addHeaderView(new FrameLayout(this));
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("series_1", series_1);
            paramsMap.put("teach_type", "1");
            if (is_live.equals("1")) {
                paramsMap.put("is_live", "1");
            }
            progressDialog = new ProgressDialog(this);
            progressDialog.show();
            RequestURL.sendPOST("https://app.feimayun.com/Lesson/index", handleNetwork, paramsMap, LiveListActivity.this);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        isFirstIn = true;
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.headtitle_layout) {//返回
            finish();
        }
    }
}
