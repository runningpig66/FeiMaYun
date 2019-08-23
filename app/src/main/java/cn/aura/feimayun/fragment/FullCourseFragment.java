package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import cn.aura.feimayun.activity.CourseListActivity;
import cn.aura.feimayun.activity.MainActivity;
import cn.aura.feimayun.adapter.FullCourse_ListView_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.StaticUtil;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.view.FlowLayout;

/**
 * 描述：全部课程 碎片页
 */
public class FullCourseFragment extends Fragment implements View.OnClickListener {
    //处理网络请求，在initView()方法中请求网络，返回结果会传递到handleNetwork中
    public static Handler handleNetwork;
    //处理跳转信息
    public static Handler handleJump;
    //存储HomePageFragment解析好的"data"后台数据
    private static List<Map<String, String>> data_mapList;
    //判断下载是否成功
    public boolean isRequestSuccess = false;
    //碎片依赖的活动
    MainActivity mainActivity;
    FullCourse_ListView_Adapter adapter;
    private SmartRefreshLayout fullcourse_refreshLayout;
    //碎片视图
    private View view;
    private ListView full_course_list;
    private LinearLayout data_panel;
    //右上方的banner图片
    private ImageView banner_img;
    private LayoutInflater inflater;
    private int data_position = -1;
    //记录data课程的id数组，通过首页传来的id和data数组中的id作比较，得出首页跳转过来的position
    private int[] id_array;

    @SuppressLint("HandlerLeak")
    public void handler() {
        //处理网络请求，在initView()方法中请求网络，返回结果会传递到handleNetwork中
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(mainActivity, "请检查网络连接_Error29", Toast.LENGTH_LONG).show();
                    fullcourse_refreshLayout.finishRefresh(false);
                    isRequestSuccess = false;
                } else {
                    isRequestSuccess = true;
                    //解析后台返回的JSON数据
                    parseJson(msg.obj.toString());
                }

            }
        };

        //处理跳转信息
        handleJump = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case StaticUtil.FROM_HOMEPAGE_TO_FULLCOURSE:
                        Bundle bundle = (Bundle) msg.obj;
                        int data2_id = Integer.parseInt(bundle.getString("data2_id", "0"));
                        for (int i = 0; i < id_array.length; i++) {
                            if (id_array[i] == data2_id) {
                                data_position = i;
                                break;
                            }
                        }
                        if (data_position == -1) {
                            data_position = 0;
                        }
                        //从其他页面跳转过来，要判断本页面是否网络加载成功
                        if (isRequestSuccess) {
                            //根据id的不同，设置不同的按钮背景和对应的页面
                            adapter.setPosition(data_position);
                            adapter.notifyDataSetChanged();
                            //listView定位到对应项
                            full_course_list.setSelection(data_position);
                            //设置对应item的右侧视图
                            setRightData(data_mapList, data_position);
                        } else {
                            initData();
                        }
                        break;
                }
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_full_course, container, false);
        initView();
        initData();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        inflater = LayoutInflater.from(mainActivity);
    }

    public void initView() {
        fullcourse_refreshLayout = view.findViewById(R.id.fullcourse_refreshLayout);
        fullcourse_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initData();
                data_position = 0;
                mainActivity.getRequestSuccess();
            }
        });

        full_course_list = view.findViewById(R.id.full_course_list);
        data_panel = view.findViewById(R.id.data_panel);
        banner_img = view.findViewById(R.id.banner_img);
        //返回按钮布局
        RelativeLayout headtitle_layout = view.findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);
        //标题
        TextView headtitle_textview = view.findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("全部分类");
    }

    public void initData() {
        //请求后台网络数据，数据会在handleNetwork接收并处理
        RequestURL.sendGET("https://app.feimayun.com/Index/index", handleNetwork, mainActivity);
    }

    //解析后台返回的JSON数据,同时调用本碎片中各个页面加载方法，传入页面所需的数据
    private void parseJson(String jsonData) {
        try {
            JSONTokener jsonTokener = new JSONTokener(jsonData);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            String status = jsonObject.getString("status");
            if (status.equals("1")) {
                //解析data
                JSONArray data = jsonObject.getJSONArray("data");
                data_mapList = new ArrayList<>();
                id_array = new int[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    Map<String, String> map = new HashMap<>();
                    map.put("id", item.getString("id"));
                    id_array[i] = Integer.valueOf(item.getString("id"));
                    map.put("name", item.getString("name"));
                    map.put("bg_img", item.getString("bg_img"));
                    map.put("icon_img", item.getString("icon_img"));
                    map.put("about", item.getString("about"));
                    map.put("status", item.getString("status"));
                    if (item.has("children")) {
                        JSONArray jsonArray_children = item.getJSONArray("children");
                        map.put("children", jsonArray_children.toString());
                    } else {
                        map.put("children", "");
                    }
                    data_mapList.add(map);
                }
                //得到解析的后台数据后，初始化ListView
                initListView(data_mapList);
                fullcourse_refreshLayout.finishRefresh(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            fullcourse_refreshLayout.finishRefresh(false);
        }
    }

    //初始化ListView
    private void initListView(final List<Map<String, String>> data_mapList) {
        //得到后台的数据以后，初始化ListView
        adapter = new FullCourse_ListView_Adapter(mainActivity, data_mapList, R.layout.fullcourse_listview_item);
        full_course_list.post(new Runnable() {
            @Override
            public void run() {
                full_course_list.setAdapter(adapter);
                full_course_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        adapter.setPosition(position);
                        adapter.notifyDataSetChanged();
                        //设置对应item的右侧视图
                        setRightData(data_mapList, position);
                    }
                });
                if (data_position != -1) {
                    //根据id的不同，设置不同的按钮背景和对应的页面
                    adapter.setPosition(data_position);
                    adapter.notifyDataSetChanged();
                    //listView定位到对应项（以防止列表条目数量过多时）
                    full_course_list.setSelection(data_position);
                    //设置对应item的右侧视图
                    setRightData(data_mapList, data_position);
                } else {
                    //初始化没有点击设置item0
                    setRightData(data_mapList, 0);
                }
            }
        });
    }

    private void setRightData(List<Map<String, String>> data_mapList, int position) {
        data_panel.removeAllViews();
        if (Util.isOnMainThread()) {
            Glide.with(MyApplication.context).load(data_mapList.get(position).get("bg_img")).into(banner_img);
        }
        String childrenJSON = data_mapList.get(position).get("children");

        if (!childrenJSON.equals("")) {
            try {
                //拿到二级JSON，变量名后的s代表二级second
                JSONTokener jsonTokener_s = new JSONTokener(childrenJSON);
                JSONArray jsonArray_s = (JSONArray) jsonTokener_s.nextValue();
                for (int i = 0; i < jsonArray_s.length(); i++) {
                    JSONObject jsonObject_s = jsonArray_s.getJSONObject(i);
                    View view_s = inflater.inflate(R.layout.fullcourse_rightdown_item, null);
                    TextView textView_s = view_s.findViewById(R.id.s_title);
                    textView_s.setText(jsonObject_s.getString("name"));
                    view_s.setTag(jsonObject_s.getString("id"));
                    //TODO 添加二级标题的点击事件
                    view_s.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String id = v.getTag().toString();
                            Intent intent = new Intent(mainActivity, CourseListActivity.class);
                            intent.putExtra("series", "series_2");
                            intent.putExtra("id", id);
                            startActivity(intent);
                        }
                    });

                    FlowLayout flowLayout = view_s.findViewById(R.id.flowLayout);
                    if (jsonObject_s.has("children")) {
                        //拿到三级JSON，变量名后的t代表third
                        JSONArray jsonArray_t = jsonObject_s.getJSONArray("children");

                        for (int j = 0; j < jsonArray_t.length(); j++) {
                            JSONObject jsonObject_t = jsonArray_t.getJSONObject(j);
                            View view_t = inflater.inflate(R.layout.fullcourse_flowlayout_item, null);
                            TextView flowlayout_item = view_t.findViewById(R.id.flowlayout_item);
                            flowlayout_item.setText(jsonObject_t.getString("name"));
                            flowlayout_item.setTag(jsonObject_t.getString("id"));
                            //TODO 添加三级标题的点击事件
                            flowlayout_item.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String id = v.getTag().toString();
                                    Intent intent = new Intent(mainActivity, CourseListActivity.class);
                                    intent.putExtra("series", "series_3");
                                    intent.putExtra("id", id);
                                    startActivity(intent);
                                }
                            });
                            flowLayout.addView(view_t);
                        }
                    }
                    data_panel.addView(view_s);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.headtitle_layout://返回键
                mainActivity.onCheckedChanged(mainActivity.rg_bt, R.id.selector1_rb);
                break;
        }
    }
}

