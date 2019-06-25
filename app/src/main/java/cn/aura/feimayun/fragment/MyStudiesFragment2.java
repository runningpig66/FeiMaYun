package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

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
import cn.aura.feimayun.activity.MainActivity;
import cn.aura.feimayun.adapter.MyStudiesFragment_ListView_Adapter;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.vhall.watch.WatchActivity;

public class MyStudiesFragment2 extends Fragment {
    //判断下载是否成功
    public static boolean isRequestSuccess = false;
    public static Handler handleMyLessons;
    public static Handler handleRefresh;
    private MainActivity mainActivity;
    private int p = 1;//需要传的页号
    private boolean isFirstIn = true;
    private SmartRefreshLayout msf2_refreshLayout;
    private ListView msf2_listView1;
    private LinearLayout msf2_layout1;
    //存放用户视频的list
    private List<Map<String, String>> dataList;
    private MyStudiesFragment_ListView_Adapter adapter;

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleRefresh = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //隐藏listview，避免切换用户时可以看到前一个用户的课程列表
                msf2_listView1.setVisibility(View.GONE);
                msf2_layout1.setVisibility(View.GONE);
                msf2_refreshLayout.autoRefresh();
            }
        };
        handleMyLessons = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(mainActivity, "请检查网络连接_Error39", Toast.LENGTH_LONG).show();
                    msf2_refreshLayout.finishRefresh(false);
                    msf2_refreshLayout.finishLoadMore(false);
                    isRequestSuccess = false;
                } else {
                    isRequestSuccess = true;
                    parseMyLessons(msg.obj.toString());
                }
            }
        };
    }

    private void parseMyLessons(String s) {
        Util.d("061803", s);
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                //解析data--------个人课程列表
                if (isFirstIn) {
                    dataList = new ArrayList<>();
                }
                if (jsonObject.has("data") && !(jsonObject.get("data").equals(JSONObject.NULL))) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject listObject = jsonArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        if (listObject.has("id")) {
                            map.put("id", listObject.getString("id"));
                        }
                        if (listObject.has("name")) {
                            map.put("name", listObject.getString("name"));
                        }
                        if (listObject.has("uid")) {
                            map.put("uid", listObject.getString("uid"));
                        }
                        if (listObject.has("lesson_id")) {
                            map.put("lesson_id", listObject.getString("lesson_id"));
                        }
                        if (listObject.has("lesson_type")) {
                            map.put("lesson_type", listObject.getString("lesson_type"));
                        }
                        if (listObject.has("hours")) {
                            map.put("hours", listObject.getString("hours"));
                        }
                        if (listObject.has("teach_type")) {
                            map.put("teach_type", listObject.getString("teach_type"));
                        }
                        if (listObject.has("title")) {
                            map.put("title", listObject.getString("title"));
                        }
                        if (listObject.has("expire")) {
                            map.put("expire", listObject.getString("expire"));
                        }
                        if (listObject.has("series_1")) {
                            map.put("series_1", listObject.getString("series_1"));
                        }
                        if (listObject.has("series_2")) {
                            map.put("series_2", listObject.getString("series_2"));
                        }
                        if (listObject.has("stat")) {
                            map.put("stat", listObject.getString("stat"));
                        }
                        dataList.add(map);
                    }
                    if (dataList.size() > 0) {//登录有有课程
                        msf2_listView1.setVisibility(View.VISIBLE);
                        msf2_layout1.setVisibility(View.GONE);
                        if (isFirstIn) {
                            //初始化listview
                            adapter = new MyStudiesFragment_ListView_Adapter(mainActivity, dataList);
                            msf2_listView1.setAdapter(adapter);
                            adapter.setmOnButtonClickListener(new MyStudiesFragment_ListView_Adapter.OnButtonClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    Map<String, String> itemMap = dataList.get(position);
                                    if (Objects.equals(itemMap.get("teach_type"), "1")) {
                                        Intent intent1 = new Intent(mainActivity, WatchActivity.class);
                                        intent1.putExtra("data_id", itemMap.get("id"));
                                        intent1.putExtra("data_teach_type", itemMap.get("teach_type"));
                                        intent1.putExtra("pkid", "0");
                                        mainActivity.startActivity(intent1);
                                    } else {
                                        Toast.makeText(mainActivity, "非直播课程，数据异常！", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            isFirstIn = false;
                        } else {
                            adapter.setData(dataList);
                            adapter.notifyDataSetChanged();
                        }
                        msf2_refreshLayout.finishLoadMore(0, true, false);
                    } else {//登录后没有课程
                        msf2_listView1.setVisibility(View.GONE);
                        msf2_layout1.setVisibility(View.VISIBLE);
                        msf2_refreshLayout.finishLoadMore(0, true, true);
                    }
                } else { // "data": null,
                    //出现data为null这种情况，一是初次加载就没有课程，二是上拉加载更多的时候，没有课程的情况。
                    //这里可能会出现一种情况：用户一个课程都没有，所以登陆后返回"data": null，然后直接显示暂无课程的图标
                    if (isFirstIn) {
                        msf2_listView1.setVisibility(View.GONE);
                        msf2_layout1.setVisibility(View.VISIBLE);
                    }
                    msf2_refreshLayout.finishLoadMore(0, true, true);
                }
            } else {//status = 0
                msf2_listView1.setVisibility(View.GONE);
                msf2_layout1.setVisibility(View.VISIBLE);
                msf2_refreshLayout.finishLoadMore(0, true, true);
            }
            msf2_refreshLayout.finishRefresh(true);
        } catch (JSONException e) {
            e.printStackTrace();
            msf2_refreshLayout.finishRefresh(false);
            msf2_refreshLayout.finishLoadMore(false);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initData();
        View view = inflater.inflate(R.layout.mystudiesfragment2, container, false);
        msf2_layout1 = view.findViewById(R.id.msf2_layout1);
        msf2_refreshLayout = view.findViewById(R.id.msf2_refreshLayout);
        msf2_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isFirstIn = true;
                p = 1;
                initData();
                mainActivity.getRequestSuccess();
            }
        });
        msf2_refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                p++;
                initData();
            }
        });
        msf2_listView1 = view.findViewById(R.id.msf2_listView1);
        msf2_listView1.addHeaderView(new LinearLayout(mainActivity));
        msf2_listView1.addFooterView(new LinearLayout(mainActivity));
        TextView msf2_textView1 = view.findViewById(R.id.msf2_textView1);
        msf2_textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_call, null);
                TextView dialog_call_textview1 = view.findViewById(R.id.dialog_call_textview1);
                TextView dialog_call_textview2 = view.findViewById(R.id.dialog_call_textview2);
                dialog_call_textview1.setText("课程咨询电话：");
                dialog_call_textview2.setText("400-0893-521");
                new TDialog.Builder(mainActivity.getSupportFragmentManager())
                        .setDialogView(view)
                        .setScreenWidthAspect(mainActivity, 0.7f)
                        .addOnClickListener(R.id.dialog_call_confirm, R.id.dialog_call_cancel)
                        .setOnViewClickListener(new OnViewClickListener() {
                            @Override
                            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                switch (view.getId()) {
                                    case R.id.dialog_call_cancel:
                                        tDialog.dismiss();
                                        break;
                                    case R.id.dialog_call_confirm:
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:4000892521"));
                                        startActivity(intent);
                                        tDialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });
        return view;
    }

    public void initData() {
        String uid = Util.getUid();
        if (!uid.equals("")) {
            //登录成功后开始请求个人课程列表
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("uid", uid);
            paramsMap.put("p", String.valueOf(p));
            RequestURL.sendPOST("https://app.feimayun.com/Lesson/myClassLessons", handleMyLessons, paramsMap);
        } else {
            isRequestSuccess = true;
            if (msf2_refreshLayout != null) {
                msf2_refreshLayout.finishRefresh(500, true);
                msf2_refreshLayout.finishLoadMore(500, true, true);
            }
        }
    }

}
