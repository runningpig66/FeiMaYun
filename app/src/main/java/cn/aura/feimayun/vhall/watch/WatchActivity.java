package cn.aura.feimayun.vhall.watch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aliyun.vodplayerview.widget.AliyunScreenMode;
import com.vhall.business.VhallSDK;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.Watch_ViewPager_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.ScreenUtils;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.vhall.Param;
import cn.aura.feimayun.vhall.chat.ChatFragment;
import cn.aura.feimayun.vhall.util.VhallUtil;
import cn.aura.feimayun.vhall.util.emoji.InputUser;
import cn.aura.feimayun.vhall.util.emoji.InputView;
import cn.aura.feimayun.vhall.util.emoji.KeyBoardManager;
import cn.aura.feimayun.view.MoveFrameLayout;
import cn.aura.feimayun.view.MyControlView;
import cn.aura.feimayun.view.ProgressDialog;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 描述：直播活动界面
 */
public class WatchActivity extends FragmentActivity implements WatchContract.WatchView,
        EasyPermissions.PermissionCallbacks {
    //TODO EasyPermissions相关
    public final static String[] PERMS_WRITE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};
    static WatchContract.WatchPresenter mPresenter;
    private static WatchLivePresenter watchLivePresenter;
    private static Handler handleDetail;
    private static Handler handlePlay;
    public WatchPlaybackFragment playbackFragment;
    public WatchLiveFragment liveFragment;
    public ChatFragment chatFragment;
    public DocumentFragment docFragment;
    //    public View activity_live_view;//顶部预留状态栏
    public int chatEvent = ChatFragment.CHAT_EVENT_CHAT;
    InputView inputView;
    private int type = VhallUtil.WATCH_PLAYBACK;//默认是回放
    private String uid;
    private String pkid = null;
    private WatchPlaybackPresenter playbackPresenter;
    //判断两个模块是否一上一下- -用于点击PPT按钮置换碎片
    private boolean onTop = true;
    private MoveFrameLayout contentVideo;//播放器
    private MoveFrameLayout moveMode;//PPT
    private TabLayout activity_live_tabLayout;
    private ViewPager activity_live_viewpager;
    private View activity_live_line;
    private View activity_live_line0;
    //存放data的Map
    private Map<String, String> playDataMap;
    private Map<String, String> detailDataMap;
    private Map<String, String> detailTeacherMap;

    //存放intent中获取到的数据
    private String data_id;
    private String data_teach_type;
    private ProgressDialog progressDialog;
    private String webinar_id;
    private String errno;
    private String vhall_account = "1";

    public int getType() {
        return type;
    }

    public WatchLivePresenter getmPresenter() {
        return watchLivePresenter;
    }

    public WatchPlaybackPresenter getPlaybackPresenter() {
        return playbackPresenter;
    }

    public String getWebinar_id() {
        return webinar_id;
    }

    @SuppressLint("HandlerLeak")
    private void handler() {
        handlePlay = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(WatchActivity.this, "请检查网络连接_Error01", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parsePlay(msg.obj.toString());
                }
            }
        };
        handleDetail = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(WatchActivity.this, "请检查网络连接_Error02", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parseDeatil(msg.obj.toString());
                }
            }
        };
    }

    private void parseDeatil(String s) {
        Util.d("061001", s);
        Map<String, String> paramsMap2 = new HashMap<>();
        paramsMap2.put("id", data_id);
        paramsMap2.put("teach_type", data_teach_type);
        paramsMap2.put("uid", uid);
        if (pkid != null) {
            paramsMap2.put("pkid", pkid);
        }
        RequestURL.sendPOST("https://app.feimayun.com/Lesson/play", handlePlay, paramsMap2);

        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                //解析data
                JSONObject dataObject = jsonObject.getJSONObject("data");
                detailDataMap = new HashMap<>();
                detailDataMap.put("id", dataObject.getString("id"));
                detailDataMap.put("teach_type", dataObject.getString("teach_type"));
                detailDataMap.put("data_id", dataObject.getString("data_id"));
                detailDataMap.put("name", dataObject.getString("name"));
                detailDataMap.put("title", dataObject.getString("title"));
                detailDataMap.put("rprice", dataObject.getString("rprice"));
                detailDataMap.put("price", dataObject.getString("price"));
                detailDataMap.put("tea_id", dataObject.getString("tea_id"));
                detailDataMap.put("is_rec", dataObject.getString("is_rec"));
                detailDataMap.put("rec_time", dataObject.getString("rec_time"));
                detailDataMap.put("hours", dataObject.getString("hours"));
                detailDataMap.put("plays", dataObject.getString("plays"));
                detailDataMap.put("browse", dataObject.getString("browse"));
                detailDataMap.put("bg_url", dataObject.getString("bg_url"));
                detailDataMap.put("start_ts", dataObject.getString("start_ts"));
                detailDataMap.put("end_ts", dataObject.getString("end_ts"));
                detailDataMap.put("start_time", dataObject.getString("start_time"));
                detailDataMap.put("end_time", dataObject.getString("end_time"));
                detailDataMap.put("about", dataObject.getString("about"));
                detailDataMap.put("expire", dataObject.getString("expire"));
                detailDataMap.put("isBuy", dataObject.getString("isBuy"));
                detailDataMap.put("vhall_account", dataObject.getString("vhall_account"));

                //解析teacher
                JSONObject teacherObject = jsonObject.getJSONObject("teacher");
                detailTeacherMap = new HashMap<>();
                if (teacherObject.has("id")) {
                    detailTeacherMap.put("id", teacherObject.getString("id"));
                }
                if (teacherObject.has("title")) {
                    detailTeacherMap.put("title", teacherObject.getString("title"));
                }
                if (teacherObject.has("name")) {
                    detailTeacherMap.put("name", teacherObject.getString("name"));
                }
                if (teacherObject.has("nick_name")) {
                    detailTeacherMap.put("nick_name", teacherObject.getString("nick_name"));
                }
                if (teacherObject.has("biger")) {
                    detailTeacherMap.put("biger", teacherObject.getString("biger"));
                }
                if (teacherObject.has("learns")) {
                    detailTeacherMap.put("learns", teacherObject.getString("learns"));
                }
                if (teacherObject.has("lessons")) {
                    detailTeacherMap.put("lessons", teacherObject.getString("lessons"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getErrno() {
        return errno;
    }

    private void parsePlay(String s) {
        Util.d("061002", s);
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//请求成功
                errno = "";
                JSONObject dataObject = jsonObject.getJSONObject("data");
                playDataMap = new HashMap<>();
                playDataMap.put("id", dataObject.getString("id"));
                playDataMap.put("company_id", dataObject.getString("company_id"));
                playDataMap.put("data_id", dataObject.getString("data_id"));
                playDataMap.put("name", dataObject.getString("name"));
                playDataMap.put("title", dataObject.getString("title"));
                playDataMap.put("number", dataObject.getString("number"));
                playDataMap.put("rprice", dataObject.getString("rprice"));
                playDataMap.put("price", dataObject.getString("price"));
                playDataMap.put("tea_id", dataObject.getString("tea_id"));
                playDataMap.put("is_sell", dataObject.getString("is_sell"));
                playDataMap.put("is_rec", dataObject.getString("is_rec"));
                playDataMap.put("hours", dataObject.getString("hours"));
                playDataMap.put("plays", dataObject.getString("plays"));
                playDataMap.put("browse", dataObject.getString("browse"));
                playDataMap.put("sells", dataObject.getString("sells"));
                playDataMap.put("bg_url", dataObject.getString("bg_url"));
                playDataMap.put("status", dataObject.getString("status"));
                playDataMap.put("liveStatus", dataObject.getString("liveStatus"));
                playDataMap.put("sign", dataObject.getString("sign"));
                playDataMap.put("app_key", dataObject.getString("app_key"));
                playDataMap.put("signedat", dataObject.getString("signedat"));
                playDataMap.put("account", dataObject.getString("account"));
                playDataMap.put("username", dataObject.getString("username"));
                webinar_id = dataObject.getString("webinar_id");
                playDataMap.put("webinar_id", webinar_id);
                playDataMap.put("isBuy", dataObject.getString("isBuy"));
                playDataMap.put("vhall_account", dataObject.getString("vhall_account"));
            } else {
                Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                errno = jsonObject.getString("errno");
//                "status":0,
//                "msg":"您没有权限观看该直播~",
//                "errno":"E2001",
//                "error":"无权限。",
//                "show":1
            }
            //TODO 准备直播
            initView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            setContentView(R.layout.activity_live);

            //TODO 刘海屏测试
//            getNotchParams();

            uid = Util.getUid();
            handler();
            Intent intent = getIntent();
            data_id = intent.getStringExtra("data_id");
            data_teach_type = intent.getStringExtra("data_teach_type");
            pkid = intent.getStringExtra("pkid");
//            activity_live_view = findViewById(R.id.activity_live_view);//顶部预留状态栏
            //初始化直播界面
            contentVideo = findViewById(R.id.contentVideo);
            contentVideo.setCanMove(false);
            moveMode = findViewById(R.id.moveMode);
            activity_live_line = findViewById(R.id.activity_live_line);
            activity_live_line0 = findViewById(R.id.activity_live_line0);
            //初始化播放器位置
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
            params1.height = (int) (ScreenUtils.getWidth(this) * 9.0f / 16);
            params1.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params1.leftMargin = 0;
            params1.topMargin = 0;
            contentVideo.setLayoutParams(params1);
            contentVideo.setVisibility(View.VISIBLE);
            //初始化PPT位置
            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
            params2.width = VhallUtil.dp2px(this, 200);
            params2.height = VhallUtil.dp2px(this, 112.5f);
            params2.leftMargin = ScreenUtils.getWidth(this) - params2.width;
            params2.topMargin = 0;
            params2.addRule(RelativeLayout.BELOW, R.id.activity_live_line);
            moveMode.setLayoutParams(params2);
            moveMode.setVisibility(View.VISIBLE);
            activity_live_tabLayout = findViewById(R.id.activity_live_tabLayout);
            activity_live_viewpager = findViewById(R.id.activity_live_viewpager);
            if (EasyPermissions.hasPermissions(this, PERMS_WRITE)) {
                initData();
            } else {
                EasyPermissions.requestPermissions(this, "观看直播需要开启部分权限",
                        1, PERMS_WRITE);
            }
        }
    }


    @TargetApi(28)
    private void getNotchParams() {
//        final View decorView = getWindow().getDecorView();
//        decorView.post(new Runnable() {
//            @Override
//            public void run() {
//                DisplayCutout displayCutout = decorView.getRootWindowInsets().getDisplayCutout();
//                Log.e("TAG", "安全区域距离屏幕左边的距离 SafeInsetLeft:" + displayCutout.getSafeInsetLeft());
//                Log.e("TAG", "安全区域距离屏幕右部的距离 SafeInsetRight:" + displayCutout.getSafeInsetRight());
//                Log.e("TAG", "安全区域距离屏幕顶部的距离 SafeInsetTop:" + displayCutout.getSafeInsetTop());
//                Log.e("TAG", "安全区域距离屏幕底部的距离 SafeInsetBottom:" + displayCutout.getSafeInsetBottom());
//
//                List<Rect> rects = displayCutout.getBoundingRects();
//                if (rects == null || rects.size() == 0) {
//                    Log.e("TAG", "不是刘海屏");
//                } else {
//                    Log.e("TAG", "刘海屏数量：" + rects.size());
//                    for (Rect rect : rects) {
//                        Log.e("TAG", "刘海屏区域：" + rect);
//                    }
//                }
//
//            }
//        });

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
        getWindow().setAttributes(lp);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //如果是横屏的话，取消掉状态栏
        Configuration configuration = getResources().getConfiguration();
        int orientation = configuration.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideBottomUIMenu();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.hasPermissions(this, PERMS_WRITE)) {
            initData();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle("观看直播需要开启必要的权限")
                    .setRationale("您已拒绝开启部分权限，这将导致程序无法正确执行，是否打开设置界面开启权限？")
                    .setNegativeButton("取消")
                    .setPositiveButton("确认")
                    .build()
                    .show();
        } else {
            Toast.makeText(this, "权限拒绝无法运行1", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            if (EasyPermissions.hasPermissions(this, PERMS_WRITE)) {
                initData();
            } else {
                Toast.makeText(this, "权限拒绝无法运行", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initData() {
        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("id", data_id);
        paramsMap.put("teach_type", data_teach_type);
        paramsMap.put("uid", uid);
        if (pkid != null) {
            paramsMap.put("pkid", pkid);
        }
        RequestURL.sendPOST("https://app.feimayun.com/Lesson/detail", handleDetail, paramsMap);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (inputView != null) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN && inputView.getContentView().getVisibility() == View.VISIBLE) {
                boolean isDismiss = isShouldHideInput(inputView.getContentView(), ev);
                if (isDismiss) {
                    inputView.dismiss();
                    return false;
                } else {
                    return super.dispatchTouchEvent(ev);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View view, MotionEvent event) {
        if (view.getVisibility() == View.GONE)
            return false;
        int[] leftTop = {0, 0};
        //获取输入框当前的location位置
        inputView.getContentView().getLocationInWindow(leftTop);
        int left = leftTop[0];
        int top = leftTop[1];
        int bottom = top + inputView.getContentView().getHeight();
        int right = left + inputView.getContentView().getWidth();
        return !(event.getX() > left && event.getX() < right
                && event.getY() > top && event.getY() < bottom);
    }

    private void initView() {
        if (detailDataMap != null) {
            String vhall_accountDetail = detailDataMap.get("vhall_account");
            if (vhall_accountDetail != null && vhall_accountDetail.equals("1")) {//用户账号
                vhall_account = "1";
                VhallSDK.init(this, getResources().getString(R.string.vhall_app_key1), getResources().getString(R.string.vhall_app_secret_key1));
            } else {//流量账号
                vhall_account = "2";
                VhallSDK.init(this, getResources().getString(R.string.vhall_app_key2), getResources().getString(R.string.vhall_app_secret_key2));
            }
        }

        Param param;
        if (playDataMap != null) {
            //设置WatchId
//            String end_time = detailDataMap.get("end_time");
//            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            Date dateEnd = null;
//            try {
//                dateEnd = sDateFormat.parse(end_time);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            String s = sdf.format(new Date());
//            Date dateNow = null;
//            try {
//                dateNow = sdf.parse(s);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            if (dateNow.getTime() <= dateEnd.getTime()) {
//                param = new Param();
//                param.watchId = playDataMap.get("webinar_id");
//            } else {
//                Toast.makeText(this, "直播已过期", Toast.LENGTH_SHORT).show();
//                param = new Param();
//            }
            param = new Param();
            if (playDataMap.get("isBuy").equals("1")) {
                param.watchId = webinar_id;
            }

            switch (playDataMap.get("liveStatus")) {
                case "0":
                    break;
                case "1"://直播进行中, 参加者可以进入观看直播
                    Toast.makeText(this, "正在直播...", Toast.LENGTH_SHORT).show();
                    type = VhallUtil.WATCH_LIVE;
                    break;
                case "2"://预约中 , 直播预约中,尚未开始
                    Toast.makeText(this, "直播预约中...", Toast.LENGTH_SHORT).show();
                    break;
                case "3"://直播已结束，但是没有默认回放，要求可以播放非默认回放
                    type = VhallUtil.WATCH_PLAYBACK;
                    Toast.makeText(this, "直播回放...", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(this, "没有可观看的回放", Toast.LENGTH_SHORT).show();
                    break;
                case "5"://结束且有自动回放
                    Toast.makeText(this, "直播回放...", Toast.LENGTH_SHORT).show();
                    type = VhallUtil.WATCH_PLAYBACK;
                    break;
            }
        } else {
            param = MyApplication.param;
            type = VhallUtil.WATCH_LIVE;
        }

        List<Fragment> fragments = new ArrayList<>();
        //添加展示信息和聊天碎片，准备创建viewpager
        WatchLeftFragment fragmentLeft = new WatchLeftFragment();
        Bundle bundle = new Bundle();
        List_Bean bean1 = new List_Bean();
        bean1.setMap(detailDataMap);
        List_Bean bean2 = new List_Bean();
        bean2.setMap(detailTeacherMap);
        bundle.putSerializable("detailDataMap", bean1);
        bundle.putSerializable("detailTeacherMap", bean2);
        fragmentLeft.setArguments(bundle);
        fragments.add(fragmentLeft);

        //TODO
        //聊天碎片
        chatFragment = ChatFragment.newInstance(type, false, vhall_account);
        //PPT碎片
        docFragment = DocumentFragment.newInstance();
        docFragment.setTitleString(detailDataMap.get("name"));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //如果是看直播
        if (liveFragment == null && type == VhallUtil.WATCH_LIVE) {
            //直播播放器的布局
            liveFragment = WatchLiveFragment.newInstance();
            watchLivePresenter = new WatchLivePresenter(liveFragment, docFragment, chatFragment, this, param, docFragment, vhall_account);
            liveFragment.setTitleString(detailDataMap.get("name"));
            transaction.add(R.id.moveMode, liveFragment);

            //直播加载聊天页面
            inputView = new InputView(this, KeyBoardManager.getKeyboardHeight(this), KeyBoardManager.getKeyboardHeightLandspace(this));
            inputView.add2Window(this);
            inputView.setClickCallback(new InputView.ClickCallback() {
                @Override
                public void onEmojiClick() {
                }
            });
            inputView.setOnSendClickListener(new InputView.SendMsgClickListener() {
                @Override
                public void onSendClick(String msg, InputUser user) {
                    if (chatFragment != null && chatEvent == ChatFragment.CHAT_EVENT_CHAT) {
                        chatFragment.performSend(msg, chatEvent);
                    }
                }
            });
            inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
                @Override
                public void onHeightReceived(int screenOri, int height) {
                    if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        KeyBoardManager.setKeyboardHeight(WatchActivity.this, height);
                    } else {
                        KeyBoardManager.setKeyboardHeightLandspace(WatchActivity.this, height);
                    }
                }
            });
            fragments.add(chatFragment);
        } else if (playbackFragment == null && type == VhallUtil.WATCH_PLAYBACK) { //如果是看回放
            //回放播放器的布局
            playbackFragment = WatchPlaybackFragment.newInstance();
            playbackPresenter = new WatchPlaybackPresenter(playbackFragment, docFragment, chatFragment, this, param, docFragment);
            playbackFragment.setTitleString(detailDataMap.get("name"));
            transaction.add(R.id.moveMode, playbackFragment);
        }
        transaction.add(R.id.contentVideo, docFragment);
        transaction.commitAllowingStateLoss();

        Watch_ViewPager_Adapter adapter = new Watch_ViewPager_Adapter(getSupportFragmentManager(), fragments);
        activity_live_viewpager.setAdapter(adapter);
        activity_live_tabLayout.setupWithViewPager(activity_live_viewpager);

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public WatchLivePresenter getWatchLivePresenter() {
        return watchLivePresenter;
    }

    public void setPlace() {
        if (onTop) {//如果播放器在上，就放下
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {//竖屏
                //将PPT放上面大图
                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
                params2.removeRule(RelativeLayout.BELOW);
//                params2.addRule(RelativeLayout.BELOW, R.id.activity_live_view);
                params2.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params2.height = (int) (ScreenUtils.getWidth(this) * 9.0f / 16);
                params2.setMargins(0, 0, 0, 0);
                moveMode.setLayoutParams(params2);
                moveMode.setCanMove(false);
                docFragment.setVisiable(false);

                //调整其他依赖布局
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity_live_line0.getLayoutParams();
                params.removeRule(RelativeLayout.BELOW);
                params.addRule(RelativeLayout.BELOW, R.id.moveMode);
                activity_live_line0.setLayoutParams(params);

                //将视频放下面小图
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
                params1.addRule(RelativeLayout.BELOW, R.id.activity_live_line);
                params1.width = VhallUtil.dp2px(this, 200);
                params1.height = VhallUtil.dp2px(this, 112.5f);
                params1.leftMargin = ScreenUtils.getWidth(this) - params1.width;
                params1.topMargin = 0;
                params1.rightMargin = 0;
                contentVideo.setLayoutParams(params1);

            } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                //将PPT放上面大图
                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
                params2.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                params2.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params2.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params2.setMargins(0, 0, 0, 0);
                moveMode.setLayoutParams(params2);
                moveMode.setCanMove(false);
                docFragment.setVisiable(false);

                //调整其他依赖布局
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity_live_line0.getLayoutParams();
                params.removeRule(RelativeLayout.BELOW);
                params.addRule(RelativeLayout.BELOW, R.id.moveMode);
                activity_live_line0.setLayoutParams(params);

                //将视频放右上角
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
                params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params1.removeRule(RelativeLayout.BELOW);
                params1.width = VhallUtil.dp2px(this, 200);
                params1.height = VhallUtil.dp2px(this, 112.5f);
                params1.leftMargin = ScreenUtils.getWidth(this) - params1.width;
                params1.topMargin = 0;
                params1.rightMargin = 0;
                contentVideo.setLayoutParams(params1);
            }

            contentVideo.bringToFront();
            contentVideo.setCanMove(true);
            if (type == VhallUtil.WATCH_LIVE) {
                liveFragment.setVisiable(true);//隐藏播放按钮等图标
                liveFragment.updatePPTState(MyControlView.PPTState.Bottom);
            } else if (type == VhallUtil.WATCH_PLAYBACK) {
                playbackFragment.setVisiable(true);
                playbackFragment.updatePPTState(MyControlView.PPTState.Bottom);
            }
            docFragment.updatePPTState(MyControlView.PPTState.Bottom);
            onTop = false;
        } else {//如果播放器在下，就放上
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                //将视频放上面大图
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
                params1.removeRule(RelativeLayout.BELOW);
//                params1.addRule(RelativeLayout.BELOW, R.id.activity_live_view);
                params1.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params1.height = (int) (ScreenUtils.getWidth(this) * 9.0f / 16);
                params1.setMargins(0, 0, 0, 0);
                contentVideo.setLayoutParams(params1);

                //调整其他依赖布局
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity_live_line0.getLayoutParams();
                params.removeRule(RelativeLayout.BELOW);
                params.addRule(RelativeLayout.BELOW, R.id.contentVideo);
                activity_live_line0.setLayoutParams(params);

                //将PPT放下面小图
                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
                params2.addRule(RelativeLayout.BELOW, R.id.activity_live_line);
                params2.width = VhallUtil.dp2px(this, 200);
                params2.height = VhallUtil.dp2px(this, 112.5f);
                params2.leftMargin = ScreenUtils.getWidth(this) - params2.width;
                params2.topMargin = 0;
                params2.rightMargin = 0;
                moveMode.setLayoutParams(params2);


            } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                //将视频放上面大图
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
                params1.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                params1.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params1.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params1.setMargins(0, 0, 0, 0);
                contentVideo.setLayoutParams(params1);

                //调整其他依赖布局
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity_live_line0.getLayoutParams();
                params.removeRule(RelativeLayout.BELOW);
                params.addRule(RelativeLayout.BELOW, R.id.contentVideo);
                activity_live_line0.setLayoutParams(params);

                //将PPT放下面小图
                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
                params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params2.width = VhallUtil.dp2px(this, 200);
                params2.height = VhallUtil.dp2px(this, 112.5f);
                params2.leftMargin = ScreenUtils.getWidth(this) - params2.width;
                params2.topMargin = 0;
                params2.rightMargin = 0;
                moveMode.setLayoutParams(params2);

            }
            moveMode.setCanMove(true);
            moveMode.bringToFront();
            docFragment.setVisiable(true);
            contentVideo.setCanMove(false);
            if (type == VhallUtil.WATCH_LIVE) {
                liveFragment.setVisiable(false);//隐藏播放按钮等图标
                liveFragment.updatePPTState(MyControlView.PPTState.Top);
            } else if (type == VhallUtil.WATCH_PLAYBACK) {
                playbackFragment.setVisiable(false);
                playbackFragment.updatePPTState(MyControlView.PPTState.Top);
            }
            docFragment.updatePPTState(MyControlView.PPTState.Top);
            onTop = true;
        }
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void showChatView(boolean isShowEmoji, InputUser user, int contentLengthLimit) {
        if (contentLengthLimit > 0)
            inputView.setLimitNo(contentLengthLimit);
        inputView.show(isShowEmoji, user);
    }

    @Override
    public int changeOrientation() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (type == VhallUtil.WATCH_LIVE) {
                liveFragment.setmCurrentScreenMode(AliyunScreenMode.Full);
                liveFragment.setScreenModeStatus();
            } else if (type == VhallUtil.WATCH_PLAYBACK) {
                playbackFragment.setmCurrentScreenMode(AliyunScreenMode.Full);
            }
            docFragment.setmCurrentScreenMode(AliyunScreenMode.Full);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (type == VhallUtil.WATCH_LIVE) {
                liveFragment.setmCurrentScreenMode(AliyunScreenMode.Small);
                liveFragment.setScreenModeStatus();
            } else if (type == VhallUtil.WATCH_PLAYBACK) {
                playbackFragment.setmCurrentScreenMode(AliyunScreenMode.Small);
            }
            docFragment.setmCurrentScreenMode(AliyunScreenMode.Small);
        }
        return getRequestedOrientation();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void setPresenter(WatchContract.WatchPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            if (onTop) {//如果播放器在上方
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏

                    hideBottomUIMenu();
                    //隐藏播放器下方布局
//                    activity_live_view.setVisibility(View.GONE);//隐藏状态栏
                    activity_live_line0.setVisibility(View.GONE);
                    activity_live_tabLayout.setVisibility(View.GONE);
                    activity_live_line.setVisibility(View.GONE);
                    activity_live_viewpager.setVisibility(View.GONE);

                    //初始化播放器位置
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
                    params1.removeRule(RelativeLayout.BELOW);
                    params1.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params1.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    params1.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params1.setMargins(0, 0, 0, 0);
                    contentVideo.setLayoutParams(params1);

                    //初始化PPT位置
                    RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
                    params2.width = VhallUtil.dp2px(this, 200);
                    params2.height = VhallUtil.dp2px(this, 112.5f);
                    params2.leftMargin = ScreenUtils.getWidth(this) - params2.width;
                    params2.topMargin = 0;
                    params2.rightMargin = 0;
                    params2.bottomMargin = 0;
                    params2.removeRule(RelativeLayout.BELOW);
                    params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    moveMode.setLayoutParams(params2);
                    moveMode.bringToFront();
                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    View decorView = getWindow().getDecorView();
                    int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                    decorView.setSystemUiVisibility(uiOptions);

//                    activity_live_view.setVisibility(View.VISIBLE);
                    activity_live_line0.setVisibility(View.VISIBLE);
                    activity_live_tabLayout.setVisibility(View.VISIBLE);
                    activity_live_line.setVisibility(View.VISIBLE);
                    activity_live_viewpager.setVisibility(View.VISIBLE);
                    activity_live_viewpager.post(new Runnable() {
                        @Override
                        public void run() {
                            //初始化播放器位置
                            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
//                            params1.addRule(RelativeLayout.BELOW, R.id.activity_live_view);
                            params1.height = (int) (ScreenUtils.getWidth(getActivity()) * 9.0f / 16);
                            params1.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            params1.setMargins(0, 0, 0, 0);
                            contentVideo.setLayoutParams(params1);

                            //初始化PPT位置
                            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
                            params2.width = VhallUtil.dp2px(getActivity(), 200);
                            params2.height = VhallUtil.dp2px(getActivity(), 112.5f);
                            params2.leftMargin = ScreenUtils.getWidth(WatchActivity.this) - params2.width;
                            params2.topMargin = 0;
                            params2.rightMargin = 0;
                            params2.bottomMargin = 0;
                            params2.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                            params2.addRule(RelativeLayout.BELOW, R.id.activity_live_line);
                            moveMode.setLayoutParams(params2);
                            moveMode.bringToFront();
                        }
                    });
                }
            } else {
                //如果PPT在上方
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    hideBottomUIMenu();

                    //隐藏播放器下方布局
//                    activity_live_view.setVisibility(View.GONE);
                    activity_live_line0.setVisibility(View.GONE);
                    activity_live_tabLayout.setVisibility(View.GONE);
                    activity_live_line.setVisibility(View.GONE);
                    activity_live_viewpager.setVisibility(View.GONE);

                    //初始化PPT位置
                    RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
                    params2.removeRule(RelativeLayout.BELOW);
                    params2.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params2.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params2.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    params2.setMargins(0, 0, 0, 0);
                    moveMode.setLayoutParams(params2);

                    //初始化播放器位置
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
                    params1.height = VhallUtil.dp2px(this, 112.5f);
                    params1.width = VhallUtil.dp2px(this, 200);
                    params1.leftMargin = ScreenUtils.getWidth(WatchActivity.this) - params1.width;
                    params1.topMargin = 0;
                    params1.rightMargin = 0;
                    params1.bottomMargin = 0;
                    params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params1.removeRule(RelativeLayout.BELOW);
                    contentVideo.setLayoutParams(params1);
                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    View decorView = getWindow().getDecorView();
                    int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                    decorView.setSystemUiVisibility(uiOptions);

//                    activity_live_view.setVisibility(View.VISIBLE);
                    activity_live_line0.setVisibility(View.VISIBLE);
                    activity_live_tabLayout.setVisibility(View.VISIBLE);
                    activity_live_line.setVisibility(View.VISIBLE);
                    activity_live_viewpager.setVisibility(View.VISIBLE);

                    activity_live_viewpager.post(new Runnable() {
                        @Override
                        public void run() {
                            //初始化PPT位置
                            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) moveMode.getLayoutParams();
//                            params2.addRule(RelativeLayout.BELOW, R.id.activity_live_view);
                            params2.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            params2.height = (int) (ScreenUtils.getWidth(getActivity()) * 9.0f / 16);
                            params2.setMargins(0, 0, 0, 0);
                            moveMode.setLayoutParams(params2);

                            //初始化播放器位置
                            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) contentVideo.getLayoutParams();
                            params1.height = VhallUtil.dp2px(getActivity(), 112.5f);
                            params1.width = VhallUtil.dp2px(getActivity(), 200);
                            params1.leftMargin = ScreenUtils.getWidth(WatchActivity.this) - params1.width;
                            params1.topMargin = 0;
                            params1.rightMargin = 0;
                            params1.bottomMargin = 0;
                            params1.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                            params1.removeRule(RelativeLayout.BELOW);
                            params1.addRule(RelativeLayout.BELOW, R.id.activity_live_line);
                            contentVideo.setLayoutParams(params1);
                            contentVideo.bringToFront();
                        }
                    });
                }

            }
            if (inputView != null) {
                inputView.dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            int type = getType();
            if (type == VhallUtil.WATCH_LIVE) {
                if (onTop) {
                    if (!docFragment.ismIsFullScreenLocked()) {
                        changeOrientation();
                    }
                } else {
                    if (!liveFragment.ismIsFullScreenLocked()) {
                        changeOrientation();
                    }
                }
            } else if (type == VhallUtil.WATCH_PLAYBACK) {
                if (onTop) {
                    if (!docFragment.ismIsFullScreenLocked()) {
                        changeOrientation();
                    }
                } else {
                    if (!playbackFragment.ismIsFullScreenLocked()) {
                        changeOrientation();
                    }
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        if (null != inputView) {
            inputView.dismiss();
        }
        super.onUserLeaveHint();
    }

}
