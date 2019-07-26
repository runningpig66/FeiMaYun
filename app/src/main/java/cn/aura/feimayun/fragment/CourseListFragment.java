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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
import java.util.Objects;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.CoursePackageActivity;
import cn.aura.feimayun.activity.FaceToFaceActivity;
import cn.aura.feimayun.activity.PlayDetailActivity;
import cn.aura.feimayun.adapter.CouseListViewPagerRvAdapter;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.vhall.watch.WatchActivity;
import cn.aura.feimayun.view.GridItemDecoration;

/**
 * 描述：CourseListActivity中动态添加的碎片
 */
public class CourseListFragment extends Fragment {
    private Context context;
    private CouseListViewPagerRvAdapter adapter;
    private SmartRefreshLayout fragment_courselist_refreshlayou;
    private int position;
    private List<Map<String, String>> lmList_List;
    private List<Map<String, String>> data_List = new ArrayList<>();
    private RecyclerView courselist_viewpager_recyclerview1;
    private RelativeLayout fragment_courselist_layout1;
    private boolean isFirstIn = true;
    private View view;

    //接受后台data的handler
    @SuppressLint("HandlerLeak")
    private Handler handleData = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj.toString().equals("网络异常")) {
                Toast.makeText(context, "请检查网络连接_Error40", Toast.LENGTH_SHORT).show();
            } else {
                parseData(msg.obj.toString());
            }
        }
    };

    public static CourseListFragment newInstance(Bundle bundle) {
        CourseListFragment fragment = new CourseListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void parseData(String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                //解析data
                String dataString = jsonObject.getString("data");
                data_List.clear();
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
                fragment_courselist_layout1.setVisibility(View.VISIBLE);
                courselist_viewpager_recyclerview1.setVisibility(View.GONE);
            } else {
                fragment_courselist_layout1.setVisibility(View.GONE);
                courselist_viewpager_recyclerview1.setVisibility(View.VISIBLE);
                //获取到页面data数据，初始化gridview
                adapter.setData_List(data_List);
                adapter.notifyDataSetChanged();
            }
            fragment_courselist_refreshlayou.finishRefresh(true);
        } catch (JSONException e) {
            e.printStackTrace();
            fragment_courselist_refreshlayou.finishRefresh(false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            //获取到碎片在viewpager页面中的位置
            position = bundle.getInt("position");
            //获取序列化的bean对象
            List_Bean bean = (List_Bean) bundle.getSerializable("bean");
            //从bean对象中取出需要的list数据
            lmList_List = Objects.requireNonNull(bean).getList();
        }
    }

    //在界面可见时再加载网络数据
    @Override
    public void onStart() {
        super.onStart();
        if (!isFirstIn) {
            //得到lmList_List和position，开始请求页面数据
            String series;
            String id = lmList_List.get(position).get("id");
            if (position == 0) {
                series = "series_2";
            } else {
                series = "series_3";
            }
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put(series, id);
            RequestURL.sendPOST("https://app.feimayun.com/Lesson/index", handleData, paramsMap);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isFirstIn = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
            return view;
        }
        view = inflater.inflate(R.layout.fragment_courselist, container, false);
        fragment_courselist_layout1 = view.findViewById(R.id.fragment_courselist_layout1);
        fragment_courselist_refreshlayou = view.findViewById(R.id.fragment_courselist_refreshlayou);
        courselist_viewpager_recyclerview1 = view.findViewById(R.id.courselist_viewpager_recyclerview1);

        //得到lmList_List和position，开始请求页面数据
        String series;
        String id = lmList_List.get(position).get("id");
        if (position == 0) {
            series = "series_2";
        } else {
            series = "series_3";
        }
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(series, id);
        RequestURL.sendPOST("https://app.feimayun.com/Lesson/index", handleData, paramsMap);

        fragment_courselist_refreshlayou.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                String series;
                String id = lmList_List.get(position).get("id");
                if (position == 0) {
                    series = "series_2";
                } else {
                    series = "series_3";
                }
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put(series, id);
                RequestURL.sendPOST("https://app.feimayun.com/Lesson/index", handleData, paramsMap);
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        courselist_viewpager_recyclerview1.setLayoutManager(gridLayoutManager);
        adapter = new CouseListViewPagerRvAdapter(context, null);
        courselist_viewpager_recyclerview1.setAdapter(adapter);
        GridItemDecoration divider = new GridItemDecoration.Builder(context)
                .setVerticalSpan(R.dimen.dp5)
                .setColorResource(R.color.eeeeee)
                .setShowLastLine(false)
                .build();
        courselist_viewpager_recyclerview1.addItemDecoration(divider);

        adapter.setItemClickListener(new CouseListViewPagerRvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //添加课程列表页面点击事件
                String data_id = data_List.get(position).get("id");
                String data_teach_type = data_List.get(position).get("teach_type");
                switch (Integer.parseInt(data_teach_type)) {
                    case 1://直播
                        Intent intentLiveActivity = new Intent(context, WatchActivity.class);
                        intentLiveActivity.putExtra("data_id", data_id);
                        intentLiveActivity.putExtra("data_teach_type", data_teach_type);
                        startActivity(intentLiveActivity);
                        break;
                    case 2://录播
                        Intent intentPlayDeatilActivity = new Intent(context, PlayDetailActivity.class);
                        intentPlayDeatilActivity.putExtra("data_id", data_id);
                        intentPlayDeatilActivity.putExtra("data_teach_type", data_teach_type);
                        startActivity(intentPlayDeatilActivity);
                        break;
                    case 3://课程包
                        Intent intentCoursePackageActivity = new Intent(context, CoursePackageActivity.class);
                        intentCoursePackageActivity.putExtra("data_id", data_id);
                        intentCoursePackageActivity.putExtra("data_teach_type", data_teach_type);
                        startActivity(intentCoursePackageActivity);
                        break;
                    case 4://面授
                        Intent intentFaceToFaceActivity = new Intent(context, FaceToFaceActivity.class);
                        intentFaceToFaceActivity.putExtra("data_id", data_id);
                        intentFaceToFaceActivity.putExtra("data_teach_type", data_teach_type);
                        startActivity(intentFaceToFaceActivity);
                        break;
                }
            }
        });
        return view;
    }
}
