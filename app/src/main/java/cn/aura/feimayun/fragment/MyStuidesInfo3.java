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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.MyStudiesInfo3_RecyclerView_Adapter;
import cn.aura.feimayun.bean.MyStuidesInfo3Bean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

public class MyStuidesInfo3 extends Fragment {

    private static Handler handleNetwork;
    private AppCompatActivity context;
    private String uid;
    private RelativeLayout mystudiesinfo3_layout1;//没有班级的界面
    private RecyclerView fragment_mystudiesinfo3_recyclerview;
    //    private List<Map<String, String>> dataList = new ArrayList<>();
    private List<MyStuidesInfo3Bean.DataBean> dataBeanList;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(context, "请检查网络连接_Error42", Toast.LENGTH_LONG).show();
                } else {
                    parseJson(msg.obj.toString());
                }
            }
        };
    }

    private void parseJson(String s) {
        Gson gson = new Gson();
        MyStuidesInfo3Bean myStuidesInfo3Bean = gson.fromJson(s, MyStuidesInfo3Bean.class);
        int status = myStuidesInfo3Bean.getStatus();
        if (status == 1) {
            dataBeanList = myStuidesInfo3Bean.getData();
            for (int i = 0; i < dataBeanList.size(); i++) {//这里筛选掉已经过期的课程，不展示
                MyStuidesInfo3Bean.DataBean dataBean = dataBeanList.get(i);
                if (dataBean.getId() == null || dataBean.getId().equals("")) {
                    dataBeanList.remove(i);
                    i--;
                }
            }
        }

        if (dataBeanList != null && !dataBeanList.isEmpty()) {
            fragment_mystudiesinfo3_recyclerview.setVisibility(View.VISIBLE);
            mystudiesinfo3_layout1.setVisibility(View.GONE);
            initRecyclerView();
        } else {
            fragment_mystudiesinfo3_recyclerview.setVisibility(View.GONE);
            mystudiesinfo3_layout1.setVisibility(View.VISIBLE);
        }

//        JSONTokener jsonTokener = new JSONTokener(s);
//        try {
//            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//            int status = jsonObject.getInt("status");
//            if (status == 1) {
//                JSONArray dataArray = jsonObject.getJSONArray("data");
//                for (int i = 0; i < dataArray.length(); i++) {
//                    JSONObject dataObject = dataArray.getJSONObject(i);
//                    Map<String, String> map = new HashMap<>();
//                    map.put("id", dataObject.getString("id"));
//                    map.put("name", dataObject.getString("name"));
//                    map.put("stime", dataObject.getString("stime"));
//                    map.put("etime", dataObject.getString("etime"));
//                    map.put("lessons", dataObject.getString("lessons"));
//                    map.put("timer", dataObject.getString("timer"));
//                    dataList.add(map);
//                }
//                if (!dataList.isEmpty()) {
//                    initRecyclerView();
//                }
//            }
////            else {//"status":0
////                Toast.makeText(context, "暂无班级", Toast.LENGTH_SHORT).show();
////            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            if (dataBeanList == null || dataBeanList.isEmpty()) {
//                Toast.makeText(context, "暂无班级", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void initRecyclerView() {
        MyStudiesInfo3_RecyclerView_Adapter adapter = new MyStudiesInfo3_RecyclerView_Adapter(context, dataBeanList);
        fragment_mystudiesinfo3_recyclerview.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.recyclerview_decoration)));
        fragment_mystudiesinfo3_recyclerview.addItemDecoration(divider);
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String lid = "";
        if (getArguments() != null) {
            lid = getArguments().getString("lid");
        }
        Map<String, String> map = new HashMap<>();
        map.put("uid", uid);
        map.put("lid", lid);
        RequestURL.sendPOST("https://app.feimayun.com/User/myClass", handleNetwork, map, context);

        View view = inflater.inflate(R.layout.fragment_mystudiesinfo3, container, false);
        mystudiesinfo3_layout1 = view.findViewById(R.id.mystudiesinfo3_layout1);
        fragment_mystudiesinfo3_recyclerview = view.findViewById(R.id.fragment_mystudiesinfo3_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        fragment_mystudiesinfo3_recyclerview.setLayoutManager(layoutManager);
        return view;
    }

}
