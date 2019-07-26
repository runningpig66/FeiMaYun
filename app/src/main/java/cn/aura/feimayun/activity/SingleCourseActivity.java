package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import cn.aura.feimayun.adapter.CouseListViewPagerRvAdapter;
import cn.aura.feimayun.util.OnClickListener;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.vhall.watch.WatchActivity;
import cn.aura.feimayun.view.GridItemDecoration;
import cn.aura.feimayun.view.ProgressDialog;

public class SingleCourseActivity extends BaseActivity {
    public static Handler handleSearch;
    private ProgressDialog progressDialog;
    //下拉上拉控件
    private SmartRefreshLayout single_course_refreshLayout;
    //暂无课程layout
    private RelativeLayout single_course_relativeLayout;
    //未找到您搜索的课程，给您推荐以下课程~
    private TextView none_message_textView;
    private RecyclerView none_message_recyclerView;
    //title
    private TextView headtitle_textview;
    //back button
    private RelativeLayout headtitle_layout;
    private CouseListViewPagerRvAdapter adapter;
    private int page = 1;
    private List<Map<String, String>> data_List = new ArrayList<>();
    private boolean isFirstIn = true;
    //intent传递的搜索数据
    private String searchMessage;

    @SuppressLint("HandlerLeak")
    public void handler() {
        //处理网络请求，在initView()方法中请求网络，返回结果会传递到handleNetwork中
        handleSearch = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(SingleCourseActivity.this, "请检查网络连接_Error99", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    single_course_refreshLayout.finishRefresh(300, false);
                    single_course_refreshLayout.finishLoadMore(800, false, false);
                } else {
                    parseSearch(msg.obj.toString());
                }
            }
        };
    }

    private void parseSearch(String result) {
        try {
            JSONTokener jsonTokener = new JSONTokener(result);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.optInt("status");
            String msg = jsonObject.optString("msg");
            if (status == 1 || status == 2) {
                //解析data
                String dataString = jsonObject.getString("data");
                if (isFirstIn) {
                    data_List.clear();
                    isFirstIn = false;
                }
                if (!dataString.equals("null")) {
                    JSONArray data_array = jsonObject.getJSONArray("data");
                    for (int i = 0; i < data_array.length(); i++) {
                        JSONObject data_object = data_array.getJSONObject(i);
                        Map<String, String> data_map = new HashMap<>();
                        String teach_type = data_object.getString("teach_type");
                        //判断teach_type，有4种类型：1直播课程，2录播课程，3课程包，4面授课程
                        switch (teach_type) {
                            case "1":
                                data_map.clear();
                                data_map.put("id", data_object.getString("id"));
                                data_map.put("teach_type", data_object.getString("teach_type"));
                                data_map.put("bg_url", data_object.getString("bg_url"));
                                data_map.put("name", data_object.getString("name"));
                                data_map.put("rprice", data_object.getString("rprice"));
                                data_map.put("price", data_object.getString("price"));
                                data_map.put("browse", data_object.getString("browse"));
                                data_map.put("title", data_object.getString("title"));
                                data_map.put("start_ts", data_object.getString("start_ts"));
                                data_map.put("stat", data_object.getString("stat"));
                                data_map.put("liveStatus", data_object.getString("liveStatus"));
                                data_List.add(data_map);
                                break;
                            case "2":
                                data_map.clear();
                                data_map.put("id", data_object.getString("id"));
                                data_map.put("teach_type", data_object.getString("teach_type"));
                                data_map.put("bg_url", data_object.getString("bg_url"));
                                data_map.put("name", data_object.getString("name"));
                                data_map.put("rprice", data_object.getString("rprice"));
                                data_map.put("price", data_object.getString("price"));
                                data_map.put("browse", data_object.getString("browse"));
                                data_map.put("title", data_object.getString("title"));
                                data_map.put("hours", data_object.getString("hours"));
                                data_List.add(data_map);
                                break;
                            case "3":
                                data_map.clear();
                                data_map.put("id", data_object.getString("id"));
                                data_map.put("teach_type", data_object.getString("teach_type"));
                                data_map.put("bg_url", data_object.getString("bg_url"));
                                data_map.put("name", data_object.getString("name"));
                                data_map.put("rprice", data_object.getString("rprice"));
                                data_map.put("price", data_object.getString("price"));
                                data_map.put("browse", data_object.getString("browse"));
                                data_map.put("mediaTotal", data_object.getString("mediaTotal"));
                                data_map.put("tikuTotal", data_object.getString("tikuTotal"));
                                data_map.put("paperTotal", data_object.getString("paperTotal"));
                                data_map.put("dyTotal", data_object.getString("dyTotal"));
                                data_List.add(data_map);
                                break;
                            case "4":
                                data_map.clear();
                                data_map.put("id", data_object.getString("id"));
                                data_map.put("teach_type", data_object.getString("teach_type"));
                                data_map.put("bg_url", data_object.getString("bg_url"));
                                data_map.put("name", data_object.getString("name"));
                                data_map.put("rprice", data_object.getString("rprice"));
                                data_map.put("price", data_object.getString("price"));
                                data_map.put("browse", data_object.getString("browse"));
                                data_map.put("address", data_object.getString("address"));
                                data_map.put("lesson_time", data_object.getString("lesson_time"));
                                data_List.add(data_map);
                                break;
                        }
                    }
                }
            }
            //如果没有课程，不加载gridview，并且显示没课程的提示图
            if (data_List.isEmpty()) {
                single_course_relativeLayout.setVisibility(View.VISIBLE);
                none_message_recyclerView.setVisibility(View.GONE);
            } else {
                if (status == 1) {//正常数据
                    single_course_relativeLayout.setVisibility(View.GONE);
                    none_message_recyclerView.setVisibility(View.VISIBLE);
                    single_course_refreshLayout.finishLoadMore(0, true, false);
                } else if (status == 2) {//推荐数据
                    single_course_relativeLayout.setVisibility(View.VISIBLE);
                    none_message_recyclerView.setVisibility(View.VISIBLE);
                    single_course_refreshLayout.finishLoadMore(0, true, true);
                    //暂无更多提示
                    none_message_textView.setText(msg);
                } else {
                    single_course_refreshLayout.finishLoadMore(0, true, true);
                }
                //获取到页面data数据，初始化gridview
                adapter.setData_List(data_List);
                adapter.notifyDataSetChanged();
            }
            //设置title
            headtitle_textview.setText(searchMessage);
        } catch (JSONException e) {
            e.printStackTrace();
            single_course_refreshLayout.finishRefresh(false);
        } finally {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            single_course_refreshLayout.finishRefresh(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_course);
        handler();
        initView();
        searchMessage = getIntent().getStringExtra("searchMessage");
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        initData();
    }

    private void initData() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("keywd", searchMessage);
        paramsMap.put("p", String.valueOf(page));
        RequestURL.sendGetPath("https://app.feimayun.com/Index/search", handleSearch, paramsMap);
    }

    private void initView() {
        headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        single_course_refreshLayout = findViewById(R.id.single_course_refreshLayout);
        single_course_relativeLayout = findViewById(R.id.single_course_relativeLayout);
        none_message_textView = findViewById(R.id.none_message_textView);
        none_message_recyclerView = findViewById(R.id.none_message_recyclerView);
        single_course_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isFirstIn = true;
                page = 1;
                initData();
            }
        });
        single_course_refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                page++;
                initData();
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        none_message_recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new CouseListViewPagerRvAdapter(this, null);
        none_message_recyclerView.setAdapter(adapter);
        GridItemDecoration divider = new GridItemDecoration.Builder(this)
                .setVerticalSpan(R.dimen.dp5)
                .setColorResource(R.color.eeeeee)
                .setShowLastLine(false)
                .build();
        none_message_recyclerView.addItemDecoration(divider);
        adapter.setItemClickListener(new CouseListViewPagerRvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //添加课程列表页面点击事件
                String data_id = data_List.get(position).get("id");
                String data_teach_type = data_List.get(position).get("teach_type");
                switch (Integer.parseInt(data_teach_type)) {
                    case 1://直播
                        Intent intentLiveActivity = new Intent(SingleCourseActivity.this, WatchActivity.class);
                        intentLiveActivity.putExtra("data_id", data_id);
                        intentLiveActivity.putExtra("data_teach_type", data_teach_type);
                        startActivity(intentLiveActivity);
                        break;
                    case 2://录播
                        Intent intentPlayDeatilActivity = new Intent(SingleCourseActivity.this, PlayDetailActivity.class);
                        intentPlayDeatilActivity.putExtra("data_id", data_id);
                        intentPlayDeatilActivity.putExtra("data_teach_type", data_teach_type);
                        startActivity(intentPlayDeatilActivity);
                        break;
                    case 3://课程包
                        Intent intentCoursePackageActivity = new Intent(SingleCourseActivity.this, CoursePackageActivity.class);
                        intentCoursePackageActivity.putExtra("data_id", data_id);
                        intentCoursePackageActivity.putExtra("data_teach_type", data_teach_type);
                        startActivity(intentCoursePackageActivity);
                        break;
                    case 4://面授
                        Intent intentFaceToFaceActivity = new Intent(SingleCourseActivity.this, FaceToFaceActivity.class);
                        intentFaceToFaceActivity.putExtra("data_id", data_id);
                        intentFaceToFaceActivity.putExtra("data_teach_type", data_teach_type);
                        startActivity(intentFaceToFaceActivity);
                        break;
                }
            }
        });
    }


}
