package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.AdapterView;
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
import cn.aura.feimayun.activity.MainActivity;
import cn.aura.feimayun.adapter.MessageCenterFragment_ListView_Adapter;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

public class MessageCenterFragment extends Fragment {
    public static Handler handleLogin;//接受其他布局传来的登录成功的信息
    public static Handler handleLogout;//接受其他布局传来的退出登录消息，清空本页内容
    private static Handler hanldeNetwork;//请求消息列表
    private static Handler handleSignMessages;//发送消息已读标记
    final List<Map<String, String>> dataList = new ArrayList<>();
    //判断下载是否成功
    public boolean isRequestSuccess = false;
    MessageCenterFragment_ListView_Adapter adapter;
    private MainActivity mainActivity;
    private int notReadMessageNum = 0;//未读消息数为0
    private SmartRefreshLayout messageCenter_refreshLayout;
    private boolean isFirstIn = true;
    private boolean isFirstInit = true;//为了区分登录失败，上面的isFirstIn不能成为false
    private ListView fragment_message_center_listView;
    private RelativeLayout fragment_message_center_layout1;

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleLogin = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                isFirstIn = true;//切换用户的情况
                String uid = Util.getUid();


                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("uid", uid);
                RequestURL.sendPOST("https://app.feimayun.com/Message/index", hanldeNetwork, paramsMap, mainActivity);
            }
        };
        hanldeNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(mainActivity, "请检查网络连接_Error43", Toast.LENGTH_LONG).show();
                    messageCenter_refreshLayout.finishRefresh(false);
                    isRequestSuccess = false;
                } else {
                    isRequestSuccess = true;
                    parseJSON(msg.obj.toString());
                }

            }
        };
        handleLogout = new Handler() {//退出登录，清空消息
            @Override
            public void handleMessage(Message msg) {
                fragment_message_center_layout1.setVisibility(View.VISIBLE);
                fragment_message_center_listView.setAdapter(null);
                isFirstInit = false;
                mainActivity.setRedPointVisiable(false);//退出登录，隐藏小圆点
            }
        };
        handleSignMessages = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(mainActivity, "请检查网络连接_Error44", Toast.LENGTH_LONG).show();
                } else {
                    parseSignMessages(msg.obj.toString());
                }
            }
        };
    }

    private void parseSignMessages(String s) {
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//标记消息成功
                notReadMessageNum--;//未读消息数-1
                if (notReadMessageNum == 0) {
                    mainActivity.setRedPointVisiable(false);
                }
                String uid = Util.getUid();
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("uid", uid);
                RequestURL.sendPOST("https://app.feimayun.com/Message/index", hanldeNetwork, paramsMap, mainActivity);
            } else {
                Toast.makeText(mainActivity, "标记消息失败", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //解析后台
    private void parseJSON(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                isFirstInit = true;

                notReadMessageNum = 0;
                //判断  "data":null
                if (jsonObject.has("data") && !(jsonObject.get("data").equals(JSONObject.NULL))) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    dataList.clear();
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap.put("id", dataObject.getString("id"));
                        dataMap.put("title", dataObject.getString("title"));
                        dataMap.put("content", dataObject.getString("content"));
                        dataMap.put("create_time", dataObject.getString("create_time"));
                        dataMap.put("is_read", dataObject.getString("is_read"));
                        if (dataMap.get("is_read").equals("1")) {
                            notReadMessageNum++;//记录未读消息
                        }
                        dataList.add(dataMap);
                    }

                    //初始化listView
                    if (dataList.size() == 0) {
                        fragment_message_center_listView.setVisibility(View.GONE);
                        fragment_message_center_layout1.setVisibility(View.VISIBLE);
                        Toast.makeText(mainActivity, "暂无消息", Toast.LENGTH_SHORT).show();
                    } else {
                        if (isFirstIn) {//初次进入才需要初始化listview
                            fragment_message_center_listView.setVisibility(View.VISIBLE);
                            fragment_message_center_layout1.setVisibility(View.GONE);
                            adapter = new MessageCenterFragment_ListView_Adapter(mainActivity, dataList);
                            fragment_message_center_listView.setAdapter(adapter);
                            fragment_message_center_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if (dataList.get(position).get("is_read").equals("1")) {//只有点击未读消息的时候，才需要发送标记消息已读请求
                                        String idString = dataList.get(position).get("id");
                                        String uid = Util.getUid();

                                        Map<String, String> paramsMap = new HashMap<>();
                                        paramsMap.put("uid", uid);
                                        paramsMap.put("ids", idString);
                                        RequestURL.sendPOST("https://app.feimayun.com/Message/signMessages", handleSignMessages, paramsMap, mainActivity);
                                    }
                                }
                            });
                            isFirstIn = false;
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                //TODO 根据notReadMessageNum设置小圆点是否显示
                if (notReadMessageNum > 0) {
                    mainActivity.setRedPointVisiable(true);
                } else {
                    mainActivity.setRedPointVisiable(false);
                }
            }
            messageCenter_refreshLayout.finishRefresh(true);
        } catch (JSONException e) {
            messageCenter_refreshLayout.finishRefresh(false);
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_center, container, false);
        initView(view);
        initData();
        return view;
    }

    public void initData() {
        String uid = Util.getUid();

        if (!uid.equals("")) {//如果登录成功的状态
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("uid", uid);
            RequestURL.sendPOST("https://app.feimayun.com/Message/index", hanldeNetwork, paramsMap, mainActivity);
        } else {
            isRequestSuccess = true;
            messageCenter_refreshLayout.finishRefresh(500, true);
            if (isFirstInit) {
                isFirstInit = false;
            } else {
                Toast.makeText(mainActivity, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initView(View view) {
        //设置CutoutMode
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams params = requireActivity().getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            requireActivity().getWindow().setAttributes(params);
        }
        if (Build.VERSION.SDK_INT >= 28) {
            view.findViewById(R.id.root).setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets windowInsets) {
                    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                    if (displayCutout != null) {
                        int left = displayCutout.getSafeInsetLeft();
                        int top = displayCutout.getSafeInsetTop();
                        int right = displayCutout.getSafeInsetRight();
                        int bottom = displayCutout.getSafeInsetBottom();
                        view.findViewById(R.id.view).getLayoutParams().height = top;
                    }
                    return windowInsets.consumeSystemWindowInsets();
                }
            });
        }
        //需要隐藏返回布局
        RelativeLayout headtitle_layout = view.findViewById(R.id.headtitle_layout);
        headtitle_layout.setVisibility(View.INVISIBLE);
        //标题
        TextView headtitle_textview = view.findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("消息中心");
        messageCenter_refreshLayout = view.findViewById(R.id.messageCenter_refreshLayout);
        messageCenter_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initData();
                mainActivity.getRequestSuccess();
            }
        });
        fragment_message_center_listView = view.findViewById(R.id.fragment_message_center_listView);
        fragment_message_center_layout1 = view.findViewById(R.id.fragment_message_center_layout1);
    }

}
