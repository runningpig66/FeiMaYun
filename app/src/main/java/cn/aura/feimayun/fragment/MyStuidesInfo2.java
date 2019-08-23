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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Objects;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.MyStudiesItemActivity;
import cn.aura.feimayun.activity.QuestionAddActivity;
import cn.aura.feimayun.activity.QuestionDetailActivity;
import cn.aura.feimayun.adapter.MyStudiesInfo2_RecyclerView_Adapter;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

/**
 * 描述：个人中心 我的问答
 */
public class MyStuidesInfo2 extends Fragment {
    private Handler handleNetwork1;
    private Handler handleNetwork3;//按钮中多请求一次下个页面，判断能否进去
    private MyStudiesItemActivity context;
    private String lid;
    private String uid;
    private String lesson_type;
    private String leimu_1;
    private String leimu_2;
    private int clickPosition;

    private boolean isFirstIn = true;

    private SmartRefreshLayout info2_refreshLayout;
    private int p = 1;//需要传的页号

    private List<Map<String, String>> dataList = new ArrayList<>();
    private RecyclerView fragment_mystudiesinfo2_recyclerview;
    private MyStudiesInfo2_RecyclerView_Adapter adapter;
    private View view;
    private RelativeLayout activity_paper_list_layout2;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleNetwork1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(context, "请检查网络连接_Error36", Toast.LENGTH_LONG).show();
                    info2_refreshLayout.finishRefresh(false);
                    info2_refreshLayout.finishLoadMore(false);
                } else {
                    parseJson2(msg.obj.toString());
                }
            }
        };
        handleNetwork3 = new Handler() {//为了判断权限专门请求一次下个页面的数据，下个页面也请求了一次，将来根据后台改动再调整
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(context, "请检查网络连接_Error38", Toast.LENGTH_LONG).show();
                } else {
                    String msgString = msg.obj.toString();
                    JSONTokener jsonTokener = new JSONTokener(msgString);
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        int status = jsonObject.getInt("status");
                        if (status == 1) {
                            String tid = dataList.get(clickPosition).get("id");
                            Intent intent = new Intent(context, QuestionDetailActivity.class);
                            intent.putExtra("tid", tid);
                            startActivity(intent);
                        } else {
                            Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void parseJson2(String s) {
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                if (isFirstIn) {
                    dataList = new ArrayList<>();
                }
                JSONArray dataArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    Map<String, String> map = new HashMap<>();
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    map.put("id", dataObject.getString("id"));
                    map.put("uid", dataObject.getString("uid"));
                    map.put("title", dataObject.getString("title"));
                    if (dataObject.has("solve")) {
                        map.put("solve", dataObject.getString("solve"));
                    }
                    map.put("create_time", dataObject.getString("create_time"));
                    map.put("smaller", dataObject.getString("smaller"));
                    map.put("name", dataObject.getString("name"));
                    if (!dataObject.isNull("content_img")) {
                        map.put("content_img", dataObject.getString("content_img"));
                    }
                    if (!dataObject.isNull("content_font")) {
                        map.put("content_font", dataObject.getString("content_font"));
                    }
                    dataList.add(map);
                }

                //如果问答item数量大于0，隐藏显示一些布局，理论上status == 1问答item数量一定大于0
                if (dataList.size() > 0) {
                    activity_paper_list_layout2.setVisibility(View.GONE);
                    fragment_mystudiesinfo2_recyclerview.setVisibility(View.VISIBLE);
                }

                info2_refreshLayout.finishRefresh(true);
                info2_refreshLayout.finishLoadMore(0, true, false);
            } else {
                //出现status == 0有两种原因
                //一是在第一次“进入页面”时，题库中没有试卷
                //二是在上拉加载更多时，发现没有更多数据
//                if (jsonObject.getString("errno").equals("E2000")) {
                if (isFirstIn) {
                    //如果在第一次加载的时候没有试卷，隐藏并显示相应的布局
                    activity_paper_list_layout2.setVisibility(View.VISIBLE);
                    fragment_mystudiesinfo2_recyclerview.setVisibility(View.GONE);
                }
                //第二次加载失败的情况也是分两种
//                        if (listList.isEmpty()) {//说明在第一次加载的时候没有试卷
//
//                        } else {//说明在第一次加载的时候有试卷，上拉加载后无更多试卷的情况
//
//                        }
                //其实只要是刷新和上拉加载，都一定不是firstIn，firstIn在onCreate的时候执行过了
                info2_refreshLayout.finishRefresh(true);
                info2_refreshLayout.finishLoadMore(0, true, true);
            }
            initRecyclerView();
//            }
        } catch (JSONException e) {
            info2_refreshLayout.finishRefresh(false);
            info2_refreshLayout.finishLoadMore(false);
            e.printStackTrace();
        }
    }

    private void initRecyclerView() {
        if (isFirstIn) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            fragment_mystudiesinfo2_recyclerview.setLayoutManager(layoutManager);
            adapter = new MyStudiesInfo2_RecyclerView_Adapter(context, dataList);
            fragment_mystudiesinfo2_recyclerview.setAdapter(adapter);
            adapter.setOnItemClickListener(new MyStudiesInfo2_RecyclerView_Adapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    clickPosition = position;
                    String tid = dataList.get(position).get("id");
                    Map<String, String> map = new HashMap<>();
                    map.put("tid", tid);
                    map.put("uid", uid);
                    RequestURL.sendPOST("https://app.feimayun.com/Qa/detail", handleNetwork3, map, context);
                }
            });
            isFirstIn = false;
        } else {
            adapter.setData(dataList);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (MyStudiesItemActivity) context;
        uid = Util.getUid();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            lid = getArguments().getString("lid", "");
            lesson_type = bundle.getString("lesson_type");
            leimu_1 = bundle.getString("series_1");
            leimu_2 = bundle.getString("series_2");
        }
        handle();
        p = 1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //为了防止第二次加载的时候重复调用了这个方法onCreateView(),重新new了一个pageadapter导致fragment不显示，显示空白
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
            return view;
        }
        view = inflater.inflate(R.layout.fragment_mystudiesinfo2, container, false);
        fragment_mystudiesinfo2_recyclerview = view.findViewById(R.id.fragment_mystudiesinfo2_recyclerview);
        //添加recyclerview的分割线，在这里添加可以保证只添加一次，不能在initData中添加，否则下拉刷新会不停的添加分割线
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.recyclerview_decoration)));
        fragment_mystudiesinfo2_recyclerview.addItemDecoration(divider);
        activity_paper_list_layout2 = view.findViewById(R.id.activity_paper_list_layout2);
        info2_refreshLayout = view.findViewById(R.id.info2_refreshLayout);
        TextView fragment_mystudiesinfo2_textview1 = view.findViewById(R.id.fragment_mystudiesinfo2_textview1);
        fragment_mystudiesinfo2_textview1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QuestionAddActivity.class);
                intent.putExtra("leimu_1", leimu_1);
                intent.putExtra("leimu_2", leimu_2);
                context.startActivityForResult(intent, 0x8765);
            }
        });

        initData();

        info2_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isFirstIn = true;
                p = 1;
                initData();
            }
        });
        info2_refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                p++;
                initData();
            }
        });
        return view;
    }

    private void initData() {
        //有lid走的是个人中心我的问答打开的
        Map<String, String> map = new HashMap<>();
        map.put("uid", uid);
        map.put("lid", lid);
        map.put("p", String.valueOf(p));
        map.put("lesson_type", lesson_type);
        map.put("series_2", leimu_2);
        RequestURL.sendPOST("https://app.feimayun.com/User/myQuest", handleNetwork1, map, context);
    }

    public void requestNetwork2() {
//        if (dataList != null) {
//            dataList.clear();
//        }
//        p = 1;
//        initData();
        info2_refreshLayout.autoRefresh();
    }

}
