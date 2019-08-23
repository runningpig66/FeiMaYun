package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.MyStudiesInfo4_RecyclerView_Adapter;
import cn.aura.feimayun.bean.MyStuidesInfo4Bean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

public class MyStuidesInfo4 extends Fragment {
    private static Handler handleNetwork;
    private static Handler handleNetwork2;
    private boolean isFirstIn = true;
    private boolean isFirstInitRecyclerView = true;
    private AppCompatActivity context;
    private String lid;
    private String uid;
    private int p = 1;
    //    private List<Map<String, String>> dataList = new ArrayList<>();
    private List<MyStuidesInfo4Bean.DataBean> dataBeanList;
    private MyStudiesInfo4_RecyclerView_Adapter adapter;

    private RecyclerView mystudiesinfo4_recyclerView;
    private SmartRefreshLayout mystudiesinfo4_refreshLayout;//下拉刷新
    private RelativeLayout mystudiesinfo4_layout1;//没有试卷的页面

    private int clickPosition = -1;

    public int getClickPosition() {
        return clickPosition;
    }

    public void setClickPosition(int clickPosition) {
        this.clickPosition = clickPosition;
    }

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(context, "请检查网络连接_Error30", Toast.LENGTH_LONG).show();
                } else {
                    parseJson(msg.obj.toString());
                }
            }
        };
        handleNetwork2 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(context, "请检查网络连接_Error31", Toast.LENGTH_LONG).show();
                } else {
                    parseJson2(msg.obj.toString());
                }
            }
        };
    }

    private void parseJson2(String s) {
        Gson gson = new Gson();
        MyStuidesInfo4Bean myStuidesInfo4Bean = gson.fromJson(s, MyStuidesInfo4Bean.class);
//        if (isFirstIn) {
        int status = myStuidesInfo4Bean.getStatus();
        if (status == 1) {//理论上status==1的话，一定是有课程的，因为没课程status==0，所以下方标注1执行不到
            List<MyStuidesInfo4Bean.DataBean> dataBeanListTmp = myStuidesInfo4Bean.getData();
            int innerClickPosition = clickPosition % 15;//当页内的position
            MyStuidesInfo4Bean.DataBean dataBean = dataBeanListTmp.get(innerClickPosition);
            dataBeanList.set(clickPosition, dataBean);
            adapter.setData(dataBeanList);
            adapter.notifyDataSetChanged();
        } else {
//                "status":0,
//                "msg":"暂无更多数据~"
            Toast.makeText(context, myStuidesInfo4Bean.getMsg(), Toast.LENGTH_SHORT).show();
        }
    }

    private void parseJson(String s) {
//        Util.d("021101", s);
        Gson gson = new Gson();
        MyStuidesInfo4Bean myStuidesInfo4Bean = gson.fromJson(s, MyStuidesInfo4Bean.class);
//        if (isFirstIn) {
        int status = myStuidesInfo4Bean.getStatus();
        if (status == 1) {//理论上status==1的话，一定是有课程的，因为没课程status==0，所以下方标注1执行不到
            List<MyStuidesInfo4Bean.DataBean> dataBeanListTmp = myStuidesInfo4Bean.getData();
            if (dataBeanListTmp != null && !dataBeanListTmp.isEmpty()) {
                if (isFirstIn) {
                    dataBeanList = dataBeanListTmp;
                    mystudiesinfo4_recyclerView.setVisibility(View.VISIBLE);
                    mystudiesinfo4_layout1.setVisibility(View.GONE);
                    isFirstIn = false;
                } else {
                    dataBeanList.addAll(dataBeanListTmp);
                }
                initRecyclerView();
                mystudiesinfo4_refreshLayout.finishLoadMore(0, true, false);
            } else {// 标注1
                if (isFirstIn) {
                    mystudiesinfo4_recyclerView.setVisibility(View.GONE);
                    mystudiesinfo4_layout1.setVisibility(View.VISIBLE);
                    isFirstIn = false;
                }
                mystudiesinfo4_refreshLayout.finishLoadMore(0, true, true);
            }
            mystudiesinfo4_refreshLayout.finishRefresh(true);
        } else {
//                "status":0,
//                "msg":"暂无更多数据~"
            if (isFirstIn) {
                mystudiesinfo4_recyclerView.setVisibility(View.GONE);
                mystudiesinfo4_layout1.setVisibility(View.VISIBLE);
                isFirstIn = false;
            }
            mystudiesinfo4_refreshLayout.finishRefresh(true);
            mystudiesinfo4_refreshLayout.finishLoadMore(0, true, true);
        }
//        }

//        JSONTokener jsonTokener = new JSONTokener(s);
//        try {
//            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//            int status = jsonObject.getInt("status");
//            if (status == 1) {
//                JSONArray dataArray = jsonObject.getJSONArray("data");
//                dataList.clear();
//                for (int i = 0; i < dataArray.length(); i++) {
//                    JSONObject dataObject = dataArray.getJSONObject(i);
//                    Map<String, String> map = new HashMap<>();
//                    map.put("id", dataObject.getString("id"));
//                    map.put("store_id", dataObject.getString("store_id"));
//                    map.put("tpaper_id", dataObject.getString("tpaper_id"));
//                    map.put("tname", dataObject.getString("tname"));
//                    map.put("sname", dataObject.getString("sname"));
//                    map.put("total", dataObject.getString("total"));
//                    map.put("type", dataObject.getString("type"));
//                    map.put("write", dataObject.getString("write"));
//                    map.put("type_name", dataObject.getString("type_name"));
//                    if (!dataObject.getString("type").equals("1")) {
//                        map.put("last_id", dataObject.getString("last_id"));
//                    }
//                    if (dataObject.has("test")) {
//                        JSONArray testArray = dataObject.getJSONArray("test");
//                        map.put("test", testArray.toString());
//                    }
//                    dataList.add(map);
//                }
//                if (isFirstIn) {
//                    initRecyclerView();
//                    isFirstIn = false;
//                } else {
//                    mystudiesinfo4_recyclerView.getAdapter().notifyDataSetChanged();
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            if (dataList.isEmpty()) {
//                Toast.makeText(context, "暂无考试", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void initRecyclerView() {
        if (isFirstInitRecyclerView) {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            mystudiesinfo4_recyclerView.setLayoutManager(layoutManager);
            adapter = new MyStudiesInfo4_RecyclerView_Adapter(context, dataBeanList);
            mystudiesinfo4_recyclerView.setAdapter(adapter);
            DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
            divider.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.recyclerview_decoration)));
            mystudiesinfo4_recyclerView.addItemDecoration(divider);
            isFirstInitRecyclerView = false;
        } else {
            adapter.setData(dataBeanList);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (AppCompatActivity) context;
        uid = Util.getUid();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handle();
        if (getArguments() != null) {
            lid = getArguments().getString("lid");
        }
    }

    public void requestNetwork() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", uid);
        map.put("lid", lid);
        map.put("p", String.valueOf(p));
        RequestURL.sendPOST("https://app.feimayun.com/User/myTest", handleNetwork, map, context);
    }

    public void requestNetwork2() {
        if (clickPosition != -1) {
            int clickP = (clickPosition / 15) + 1;
            Map<String, String> map = new HashMap<>();
            map.put("uid", uid);
            map.put("lid", lid);
            map.put("p", String.valueOf(clickP));
            RequestURL.sendPOST("https://app.feimayun.com/User/myTest", handleNetwork2, map, context);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requestNetwork();
        View view = inflater.inflate(R.layout.fragment_mystudiesinfo4, container, false);
        mystudiesinfo4_recyclerView = view.findViewById(R.id.mystudiesinfo4_recyclerView);
        mystudiesinfo4_layout1 = view.findViewById(R.id.mystudiesinfo4_layout1);
        mystudiesinfo4_refreshLayout = view.findViewById(R.id.mystudiesinfo4_refreshLayout);
        mystudiesinfo4_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isFirstIn = true;
                p = 1;
                requestNetwork();
            }
        });
        mystudiesinfo4_refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                p++;
                requestNetwork();
            }
        });
        return view;
    }

}
