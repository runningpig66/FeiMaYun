package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;
import com.vhall.business.VhallSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.InformationActivity;
import cn.aura.feimayun.activity.LoginActivity;
import cn.aura.feimayun.activity.MainActivity;
import cn.aura.feimayun.activity.MyStudiesItemActivity;
import cn.aura.feimayun.adapter.MyStudiesFragment_ListView_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.SetHeightUtil;
import cn.aura.feimayun.util.Util;

import static android.content.Context.MODE_PRIVATE;

/**
 * 描述：我的学习页面
 */
public class MyStudiesFragment extends Fragment implements View.OnClickListener {
    //登录成功后用户课程的处理
    public static Handler handleMyLessons;
    public static Handler handleLogin;
    public static Handler handleLogout;
    //判断下载是否成功
    public boolean isRequestSuccess = false;
    private SmartRefreshLayout mystudies_refreshLayout;
    private MyStudiesFragment_ListView_Adapter adapter;
    private MainActivity mainActivity;
    //存放用户视频的list
    private List<Map<String, String>> dataList;
    //存放用户信息的list
    private Map<String, String> userInfoMap;
    //头像
    private ImageView fragment_my_studies_imageView1;
    //立即登录
    private TextView fragment_my_studies_textView1;
    //登录后没有课程的展示图片
//    private RelativeLayout fragment_my_studies_layout1;
    //未登录的下方界面
    private LinearLayout fragment_my_studies_level2;
    //右上角的设置按钮
    private ImageView fragment_my_studies_imageView0;
    //登录的下方界面
    private ListView fragment_my_studies_listView1;
    private boolean isFirstIn = true;
    private int p = 1;//需要传的页号

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleMyLessons = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(mainActivity, "请检查网络连接_Error39", Toast.LENGTH_LONG).show();
                    mystudies_refreshLayout.finishRefresh(false);
                    mystudies_refreshLayout.finishLoadMore(false);
                    isRequestSuccess = false;
                } else {
                    isRequestSuccess = true;
                    parseMyLessons(msg.obj.toString());
                }
            }
        };
        handleLogin = new Handler() {//接收登录成功
            @Override
            public void handleMessage(Message msg) {
//                isFirstIn = true;
//                p = 1;
                mystudies_refreshLayout.autoRefresh();
                //如果登录成功，隐藏未登录界面，显示登录界面
//                String uid = Util.getUid();
//                if (!uid.equals("")) {
//                    //登录成功后开始请求个人课程列表
//                    Map<String, String> paramsMap = new HashMap<>();
//                    paramsMap.put("uid", uid);
//                    paramsMap.put("p", String.valueOf(p));
//                    RequestURL.sendPOST("https://app.feimayun.com/Lesson/myLessons", handleMyLessons, paramsMap);
//                }
            }
        };
        handleLogout = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //执行退出登录逻辑,恢复界面
                SharedPreferences.Editor editor = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                editor.clear().apply();
                if (VhallSDK.isLogin()) {
                    VhallSDK.logout();
                }
                fragment_my_studies_imageView0.setVisibility(View.GONE);
                if (Util.isOnMainThread()) {
                    RequestOptions options = new RequestOptions().fitCenter();
                    Glide.with(MyApplication.context).load(R.drawable.data_head).apply(options).into(fragment_my_studies_imageView1);
                }
                fragment_my_studies_textView1.setText("立即登录");
                fragment_my_studies_level2.setVisibility(View.VISIBLE);
                if (dataList != null) {
                    dataList.clear();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
//                fragment_my_studies_layout1.setVisibility(View.GONE);
                fragment_my_studies_listView1.setVisibility(View.GONE);
//                fragment_my_studies_imageView1.setClickable(true);
                fragment_my_studies_textView1.setClickable(true);
            }
        };
    }

    public void initData() {
        String uid = Util.getUid();
        if (!uid.equals("")) {
            //登录成功后开始请求个人课程列表
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("uid", uid);
            paramsMap.put("p", String.valueOf(p));
            RequestURL.sendPOST("https://app.feimayun.com/Lesson/myLessons", handleMyLessons, paramsMap);
        } else {
            isRequestSuccess = true;
            if (mystudies_refreshLayout != null) {
                mystudies_refreshLayout.finishRefresh(500, false);
                mystudies_refreshLayout.finishLoadMore(500, false, true);
            }
        }
    }

    private void parseMyLessons(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                //解析data--------个人课程列表
                if (isFirstIn) {
                    dataList = new ArrayList<>();

                    //解析userInfo--------个人资料
                    userInfoMap = new HashMap<>();
                    JSONObject userInfoObject = jsonObject.getJSONObject("userInfo");
                    userInfoMap.put("avater", userInfoObject.getString("avater"));
                    userInfoMap.put("real_name", userInfoObject.getString("real_name"));
                    userInfoMap.put("phone", userInfoObject.getString("phone"));

                    SharedPreferences.Editor editor = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                    editor.putString("avater", userInfoObject.getString("avater"));
                    editor.putString("real_name", userInfoObject.getString("real_name"));
                    editor.putString("phone", userInfoObject.getString("phone"));
                    editor.apply();

                    //初始化用户界面和个人课程listview
                    fragment_my_studies_imageView0.setVisibility(View.VISIBLE);

                    //把原来的立即登录按钮设置为不可点击，并将字符设置为用户名，设置用户头像
//                fragment_my_studies_imageView1.setClickable(false);
                    fragment_my_studies_textView1.setClickable(false);
                    fragment_my_studies_textView1.setText(userInfoMap.get("real_name"));
                    if (Util.isOnMainThread()) {
                        Glide.with(MyApplication.context).load(userInfoMap.get("avater")).into(fragment_my_studies_imageView1);
                    }
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
//                        if (listObject.has("bg_url")) {
//                            map.put("bg_url", listObject.getString("bg_url"));
//                        }
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
                        fragment_my_studies_level2.setVisibility(View.GONE);
                        if (isFirstIn) {
                            fragment_my_studies_listView1.setVisibility(View.VISIBLE);
//                        fragment_my_studies_layout1.setVisibility(View.GONE);
                            //初始化listview
                            adapter = new MyStudiesFragment_ListView_Adapter(mainActivity, dataList);
                            fragment_my_studies_listView1.setAdapter(adapter);
                            adapter.setmOnButtonClickListener(new MyStudiesFragment_ListView_Adapter.OnButtonClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    Map<String, String> item = dataList.get(position);
                                    Intent intent = new Intent(mainActivity, MyStudiesItemActivity.class);
                                    intent.putExtra("lid", item.get("lesson_id"));
                                    intent.putExtra("lesson_type", item.get("lesson_type"));
                                    intent.putExtra("name", item.get("name"));
                                    intent.putExtra("series_1", item.get("series_1"));
                                    intent.putExtra("series_2", item.get("series_2"));
                                    startActivity(intent);

//                                    String teach_type = dataList.get(position).get("teach_type");
//                                    switch (teach_type) {
//                                        case "1"://直播
//                                            String lesson_id1 = dataList.get(position).get("lesson_id");
//                                            Intent intent1 = new Intent(mainActivity, WatchActivity.class);
//                                            intent1.putExtra("data_id", lesson_id1);
//                                            intent1.putExtra("data_teach_type", teach_type);
//                                            mainActivity.startActivity(intent1);
//                                            break;
//                                        case "2"://录播
//                                            String lesson_id2 = dataList.get(position).get("lesson_id");
//                                            Intent intent2 = new Intent(mainActivity, PlayDetailActivity.class);
//                                            intent2.putExtra("data_id", lesson_id2);
//                                            intent2.putExtra("data_teach_type", teach_type);
//                                            mainActivity.startActivity(intent2);
//                                            break;
//                                        case "3":
//                                            String lesson_id3 = dataList.get(position).get("lesson_id");
//                                            Intent intent3 = new Intent(mainActivity, CoursePackageActivity.class);
//                                            intent3.putExtra("data_id", lesson_id3);
//                                            intent3.putExtra("data_teach_type", teach_type);
//                                            mainActivity.startActivity(intent3);
//                                            break;
//                                        case "4":
//                                            String lesson_id4 = dataList.get(position).get("lesson_id");
//                                            Intent intent4 = new Intent(mainActivity, FaceToFaceActivity.class);
//                                            intent4.putExtra("data_id", lesson_id4);
//                                            intent4.putExtra("data_teach_type", teach_type);
//                                            mainActivity.startActivity(intent4);
//                                            break;
//                                    }
                                }
                            });
                            isFirstIn = false;
                        } else {
                            adapter.setData(dataList);
                            adapter.notifyDataSetChanged();
                        }
                        SetHeightUtil.setListViewHeightBasedOnChildren(fragment_my_studies_listView1);
                        mystudies_refreshLayout.finishLoadMore(0, true, false);
                    } else {//登录后没有课程
                        fragment_my_studies_listView1.setVisibility(View.GONE);
                        fragment_my_studies_level2.setVisibility(View.VISIBLE);
//                        fragment_my_studies_layout1.setVisibility(View.VISIBLE);
                        mystudies_refreshLayout.finishLoadMore(0, true, true);
                    }
                } else { // "data": null,
                    //出现data为null这种情况，一是初次加载就没有课程，二是上拉加载更多的时候，没有课程的情况。
                    mystudies_refreshLayout.finishLoadMore(0, true, true);
                }
            } else {//status = 0
                fragment_my_studies_listView1.setVisibility(View.GONE);
                fragment_my_studies_level2.setVisibility(View.VISIBLE);
                mystudies_refreshLayout.finishLoadMore(0, true, true);
            }
            mystudies_refreshLayout.finishRefresh(true);
        } catch (JSONException e) {
            e.printStackTrace();
            mystudies_refreshLayout.finishRefresh(false);
            mystudies_refreshLayout.finishLoadMore(false);
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        initData();
        View view = inflater.inflate(R.layout.fragment_my_studies, container, false);
        mystudies_refreshLayout = view.findViewById(R.id.mystudies_refreshLayout);
        mystudies_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isFirstIn = true;
                p = 1;
                initData();
                mainActivity.getRequestSuccess();
            }
        });
        mystudies_refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                p++;
                initData();
            }
        });

        fragment_my_studies_imageView1 = view.findViewById(R.id.fragment_my_studies_imageView1);
        fragment_my_studies_textView1 = view.findViewById(R.id.fragment_my_studies_textView1);
        //去发现感兴趣的内容
        TextView fragment_my_studies_textView2 = view.findViewById(R.id.fragment_my_studies_textView2);
        fragment_my_studies_level2 = view.findViewById(R.id.fragment_my_studies_level2);
        fragment_my_studies_imageView0 = view.findViewById(R.id.fragment_my_studies_imageView0);
        fragment_my_studies_listView1 = view.findViewById(R.id.fragment_my_studies_listView1);
        fragment_my_studies_listView1.addHeaderView(new LinearLayout(mainActivity));
        fragment_my_studies_listView1.addFooterView(new LinearLayout(mainActivity));

        fragment_my_studies_imageView1.setOnClickListener(this);
        fragment_my_studies_textView1.setOnClickListener(this);
        fragment_my_studies_textView2.setOnClickListener(this);
        fragment_my_studies_imageView0.setOnClickListener(this);

        if (!Util.getUid().equals("")) {
            SharedPreferences sp = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE);
            String real_name = sp.getString("real_name", "");
            String avater = sp.getString("avater", "");
            fragment_my_studies_textView1.setText(real_name);
            fragment_my_studies_textView1.setClickable(false);
            if (Util.isOnMainThread()) {
                RequestOptions options = new RequestOptions().fitCenter();
                Glide.with(MyApplication.context).load(avater).apply(options).into(fragment_my_studies_imageView1);
            }
        }
//        fragment_my_studies_layout1 = view.findViewById(R.id.fragment_my_studies_layout1);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_my_studies_imageView1://头像
                String uid = Util.getUid();
                if (uid.equals("")) {//uid为空则登录
                    Intent intentLoginActivity = new Intent(mainActivity, LoginActivity.class);
                    startActivity(intentLoginActivity);
                } else {//uid不为空则跳转到个人资料页面，个人资料页面再次点头像为更换头像
                    Intent intentInformationActivity = new Intent(mainActivity, InformationActivity.class);
                    SharedPreferences sp = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE);
                    String real_name = sp.getString("real_name", "");
                    String phone = sp.getString("phone", "");
                    String avater = sp.getString("avater", "");
                    intentInformationActivity.putExtra("real_name", real_name);
                    intentInformationActivity.putExtra("phone", phone);
                    intentInformationActivity.putExtra("avater", avater);
                    startActivity(intentInformationActivity);
                }
                break;
            case R.id.fragment_my_studies_textView1://立即登录
                Intent intentLoginActivity = new Intent(mainActivity, LoginActivity.class);
                startActivity(intentLoginActivity);
                break;
            case R.id.fragment_my_studies_textView2://去发现感兴趣的内容
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
                break;
            case R.id.fragment_my_studies_imageView0://右上角的设置按钮
                Intent intentInformationActivity = new Intent(mainActivity, InformationActivity.class);
                String real_name = userInfoMap.get("real_name");
                String phone = userInfoMap.get("phone");
                String avater = userInfoMap.get("avater");
                intentInformationActivity.putExtra("real_name", real_name);
                intentInformationActivity.putExtra("phone", phone);
                intentInformationActivity.putExtra("avater", avater);
                startActivity(intentInformationActivity);
                break;
        }
    }

}
