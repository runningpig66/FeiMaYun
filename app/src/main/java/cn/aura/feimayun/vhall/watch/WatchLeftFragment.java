package cn.aura.feimayun.vhall.watch;

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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.fragment.MyStudiesFragment;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.vhall.Param;
import cn.aura.feimayun.vhall.util.VhallUtil;

/**
 * 录播详情界面：简介
 */
public class WatchLeftFragment extends Fragment implements View.OnClickListener {
    private Map<String, String> detailDataMap;
    private Map<String, String> detailTeacherMap;

    private View view;
    private Handler handleBuyLessons;
    private WatchActivity activity;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleBuyLessons = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(activity, "请检查网络连接_Error03", Toast.LENGTH_LONG).show();
                } else {
                    parseBuyLessons(msg.obj.toString());
                }
            }
        };
    }

    //解析购买课程
    private void parseBuyLessons(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                Toast.makeText(activity, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                //购买成功开始播放直播
                hadBuy();
                //购买成功，更新我的学习已购买课程列表
                MyStudiesFragment.handleLogin.obtainMessage().sendToTarget();

                //开始自动播放
                int type = activity.getType();
                Param param = new Param();
                param.watchId = activity.getWebinar_id();
                if (type == VhallUtil.WATCH_LIVE) {
                    WatchLivePresenter mPresenter = activity.getWatchLivePresenter();
                    mPresenter.setParams(param);
                    mPresenter.initWatch();
                } else if (type == VhallUtil.WATCH_PLAYBACK) {
                    WatchPlaybackPresenter presenterPlackback = activity.getPlaybackPresenter();
                    presenterPlackback.setParam(param);
                    presenterPlackback.initWatch();
                }
            } else {
                Toast.makeText(activity, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handle();
        Bundle bundle = getArguments();
        if (bundle != null) {
            detailDataMap = new HashMap<>();
            detailTeacherMap = new HashMap<>();

            //获取序列化的bean对象
            List_Bean bean1 = (List_Bean) bundle.getSerializable("detailDataMap");
            if (bean1 != null) {
                detailDataMap = bean1.getMap();
            }

            List_Bean bean2 = (List_Bean) bundle.getSerializable("detailTeacherMap");
            if (bean1 != null) {
                detailTeacherMap = bean2.getMap();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_watch_left, container, false);
        initView();
        return view;
    }

    private void initView() {
        //标题
        TextView watch_left_textview1 = view.findViewById(R.id.watch_left_textview1);
        //直播时间
        TextView watch_left_textview2 = view.findViewById(R.id.watch_left_textview2);
        //观看人数
        TextView watch_left_textview3 = view.findViewById(R.id.watch_left_textview3);
        //教师姓名
        TextView watch_left_textview4 = view.findViewById(R.id.watch_left_textview4);
        //讲师简介
        TextView watch_left_textview5 = view.findViewById(R.id.watch_left_textview5);
        //课程详情
        WebView watch_left_webview = view.findViewById(R.id.watch_left_webview);
        //立即学习
        TextView watch_left_textview7 = view.findViewById(R.id.watch_left_textview7);
        watch_left_textview7.setOnClickListener(this);
        //教师头像
        ImageView watch_left_imageview2 = view.findViewById(R.id.watch_left_imageview2);

        RequestOptions options = new RequestOptions().fitCenter();
        Glide.with(activity).load(detailTeacherMap.get("biger")).apply(options).into(watch_left_imageview2);
        watch_left_textview1.setText(detailDataMap.get("name"));
        watch_left_textview2.setText("直播开始:" + detailDataMap.get("start_ts"));
        watch_left_textview3.setText(detailDataMap.get("browse"));
        watch_left_textview4.setText("主讲教师:" + detailTeacherMap.get("name"));
        watch_left_textview5.setText("讲师简介:" + detailTeacherMap.get("title"));

        String aboutString = detailDataMap.get("about");
        String des2 = Util.getNewContent(aboutString);
        WebSettings webSettings = watch_left_webview.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        watch_left_webview.loadData(des2, "text/html; charset=UTF-8", null);

        String uid = Util.getUid();
        if (uid.equals("")) {//未登录直接显示立即咨询，无论课程是否免费
            watch_left_textview7.setVisibility(View.VISIBLE);
            watch_left_textview7.setText("立即咨询");
        } else {//登录以后，再判断是否免费
            if (detailDataMap.get("isBuy").equals("0")) {//如果是没有购买
                Double rprice = Double.parseDouble(detailDataMap.get("rprice"));
                if (rprice == 0) {//如果是免费课程，隐藏立即咨询按钮，直接购买并开始播放
                    watch_left_textview7.setVisibility(View.GONE);
                    String ids = detailDataMap.get("id");
                    String order_price = detailDataMap.get("price");
                    String pay_price = detailDataMap.get("rprice");

                    //免费的直接购买
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("uid", uid);
                    paramsMap.put("ids", ids);
                    paramsMap.put("order_price", order_price);
                    paramsMap.put("pay_price", pay_price);
                    RequestURL.sendPOST("https://app.feimayun.com/Lesson/buyLessons", handleBuyLessons, paramsMap);
                } else {//收费课程显示立即咨询
                    watch_left_textview7.setVisibility(View.VISIBLE);
                    watch_left_textview7.setText("立即咨询");
                }
            } else {
                watch_left_textview7.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {//只可能在没有购买的情况下显示出来
        if (v.getId() == R.id.watch_left_textview7) {//立即学习&立即咨询
            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_call, null);
            TextView dialog_call_textview1 = view.findViewById(R.id.dialog_call_textview1);
            TextView dialog_call_textview2 = view.findViewById(R.id.dialog_call_textview2);
            dialog_call_textview1.setText("课程咨询电话：");
            dialog_call_textview2.setText("400-0893-521");
            new TDialog.Builder(activity.getSupportFragmentManager())
                    .setDialogView(view)
                    .setScreenWidthAspect(activity, 0.7f)
                    .addOnClickListener(R.id.dialog_call_confirm, R.id.dialog_call_cancel)
                    .setOnViewClickListener(new OnViewClickListener() {
                        @Override
                        public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                            if (view.getId() == R.id.dialog_call_cancel) {
                                tDialog.dismiss();
                            } else {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:4000892521"));
                                startActivity(intent);
                                tDialog.dismiss();
                            }
                        }
                    })
                    .create()
                    .show();
        }
    }

    //已购买
    public void hadBuy() {
        //初始化播放器并开始播放
        int type = activity.getType();
        if (type == VhallUtil.WATCH_LIVE) {
            WatchLivePresenter mPresenter = activity.getWatchLivePresenter();
            mPresenter.initWatch();
        } else if (type == VhallUtil.WATCH_PLAYBACK) {
            WatchPlaybackPresenter presenterPlackback = activity.getPlaybackPresenter();
            presenterPlackback.initWatch();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (WatchActivity) context;
    }
}
