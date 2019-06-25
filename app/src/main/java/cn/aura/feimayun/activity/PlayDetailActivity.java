package cn.aura.feimayun.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aliyun.vodplayer.media.AliyunVidSts;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.aliyun.vodplayerview.widget.AliyunScreenMode;
import com.aliyun.vodplayerview.widget.AliyunVodPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.PlayDetail_ViewPager_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.fragment.PlayDetailLeftFragment;
import cn.aura.feimayun.fragment.PlayDetailRightFragment;
import cn.aura.feimayun.util.MyAudioFocus;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.ScreenUtils;
import cn.aura.feimayun.util.StaticUtil;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.view.ProgressDialog;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 描述：录播活动界面
 */
public class PlayDetailActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    public final static String[] PERMS_WRITE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static Handler handlePlay;
    public static Handler handleBuy;
    private static Handler handleDetail;
    private static Handler handleSaveRecord;
    public String QUALITY = "";//记录用户切换的清晰度
    //用AudioManager获取音频焦点避免音视频声音并发问题
    AudioManager mAudioManager;
    AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private ProgressDialog progressDialog;
    private View playdeatil_view;//顶部预留
    //    private List<String> logStrs = new ArrayList<>();
//    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SS");
    private boolean isFirstInit = true;
    private PlayDetail_ViewPager_Adapter adapter;
    private Map<String, String> detailDataMap = new HashMap<>();
    private String catalogueString = "";
    private Map<String, String> detailTeacherMap = new HashMap<>();
    private Map<String, String> playDataMap;
    //存放intent中获取到的数据
    private String data_id = null;
    private String data_teach_type = null;
    private String uid = null;
    private String pkid = null;
    private AliyunVodPlayerView aliyunVodPlayerView;
    //    private ScreenStatusController mScreenStatusController = null;
    private String errno = "";
    private Timer timer = null;
    private TimerTask task = null;
    private String sid = null;
    //电话管理者对象
    private TelephonyManager mTelephonyManager;
    //电话状态监听者
    private MyPhoneStateListener myPhoneStateListener;
    private boolean fromCall = false;
    private HashSet<String> sidLearned = new HashSet<>();//用来记录本地哪些sid是leared过的
    private TabLayout playdeatil_tabLayout;
    private ViewPager playdeatil_viewpager;

    private boolean isStrangePhone() {
        return Build.DEVICE.equalsIgnoreCase("mx5")
                || Build.DEVICE.equalsIgnoreCase("Redmi Note2")
                || Build.DEVICE.equalsIgnoreCase("Z00A_1")
                || Build.DEVICE.equalsIgnoreCase("hwH60-L02")
                || Build.DEVICE.equalsIgnoreCase("hermes")
                || (Build.DEVICE.equalsIgnoreCase("V4") && Build.MANUFACTURER.equalsIgnoreCase("Meitu"))
                || (Build.DEVICE.equalsIgnoreCase("m1metal") && Build.MANUFACTURER.equalsIgnoreCase("Meizu"));
    }

    @SuppressLint("HandlerLeak")
    private void handler() {
        handlePlay = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(PlayDetailActivity.this, "请检查网络连接_Error21", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parseJSONPlay(msg.obj.toString());
                }
            }
        };
        handleDetail = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(PlayDetailActivity.this, "请检查网络连接_Error22", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parseJSONDetail(msg.obj.toString());
                }
            }
        };
        handleBuy = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                setSid(sid);
            }
        };
        handleSaveRecord = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                if (!msg.obj.toString().equals("网络异常")) {
//                    parseSaveRecord(msg.obj.toString());
//                }
            }
        };
    }

//    private void parseSaveRecord(String s) {
//        //TODO 返回字段
//        JSONTokener jsonTokener = new JSONTokener(s);
//        try {
//            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//            int status = jsonObject.getInt("status");
//            String msg = jsonObject.getString("msg");
//            if (status == 1) {
//                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void parseJSONDetail(String s) {
//        Util.d("021301", s);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("id", data_id);
        paramsMap.put("teach_type", data_teach_type);
        paramsMap.put("uid", uid);
        paramsMap.put("pkid", pkid);
        paramsMap.put("sid", sid);
//        Log.i("021303", "id:" + data_id + ", teach_type:" + data_teach_type + ", uid:" + uid + ", pkid" + pkid + ", sid:" + sid);
        RequestURL.sendPOST("https://app.feimayun.com/Lesson/play", handlePlay, paramsMap);

        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                //解析data
                JSONObject dataObject = jsonObject.getJSONObject("data");
                detailDataMap.clear();
                detailDataMap.put("id", dataObject.getString("id"));
                detailDataMap.put("teach_type", dataObject.getString("teach_type"));
                detailDataMap.put("data_id", dataObject.getString("data_id"));
                detailDataMap.put("name", dataObject.getString("name"));
                detailDataMap.put("title", dataObject.getString("title"));
                detailDataMap.put("number", dataObject.getString("number"));
                detailDataMap.put("rprice", dataObject.getString("rprice"));
                detailDataMap.put("price", dataObject.getString("price"));
                detailDataMap.put("tea_id", dataObject.getString("tea_id"));
                detailDataMap.put("hours", dataObject.getString("hours"));
                detailDataMap.put("plays", dataObject.getString("plays"));
                detailDataMap.put("bg_url", dataObject.getString("bg_url"));
                detailDataMap.put("browse", dataObject.getString("browse"));
                detailDataMap.put("about", dataObject.getString("about"));
                detailDataMap.put("expire", dataObject.getString("expire"));
                detailDataMap.put("isBuy", dataObject.getString("isBuy"));
                if (dataObject.has("sid")) {
                    String sidTmp = dataObject.getString("sid");
                    if (!TextUtils.isEmpty(sidTmp)) {
                        detailDataMap.put("sid", sidTmp);
                        sid = sidTmp;
                    }
                }
//                if (dataObject.has("img") && !(dataObject.get("img").equals(JSONObject.NULL))) {
//                    JSONArray imgArray = dataObject.getJSONArray("img");
//                    detailDataMap.put("img", imgArray.toString());
//                }
                //解析teacher
                JSONObject teacherObject = jsonObject.getJSONObject("teacher");
                detailTeacherMap.clear();
                detailTeacherMap.put("id", teacherObject.getString("id"));
                detailTeacherMap.put("company_id", teacherObject.getString("company_id"));
                detailTeacherMap.put("customer_id", teacherObject.getString("customer_id"));
                detailTeacherMap.put("title", teacherObject.getString("title"));
                detailTeacherMap.put("name", teacherObject.getString("name"));
                detailTeacherMap.put("nick_name", teacherObject.getString("nick_name"));
                detailTeacherMap.put("phone", teacherObject.getString("phone"));
                detailTeacherMap.put("passwd", teacherObject.getString("passwd"));
                detailTeacherMap.put("email", teacherObject.getString("email"));
                detailTeacherMap.put("about", teacherObject.getString("about"));
                detailTeacherMap.put("address", teacherObject.getString("address"));
                detailTeacherMap.put("biger", teacherObject.getString("biger"));
                detailTeacherMap.put("learns", teacherObject.getString("learns"));
                detailTeacherMap.put("lessons", teacherObject.getString("lessons"));
                //解析catalogue
                JSONArray catalogueArray = jsonObject.getJSONArray("catalogue");
                catalogueString = catalogueArray.toString();
                initPlay();
            } else {
                String msg = jsonObject.getString("msg");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    void initPlay() {
        //这时候可以开始初始化左右碎片了
        List<Fragment> fragments = new ArrayList<>();
        double rprice = Double.parseDouble(detailDataMap.get("rprice"));
        String isBuy = detailDataMap.get("isBuy");
        PlayDetailLeftFragment fragmentLeft = new PlayDetailLeftFragment();
        Bundle bundle = new Bundle();
        List_Bean bean1 = new List_Bean();
        bean1.setMap(detailDataMap);
        List_Bean bean2 = new List_Bean();
        bean2.setMap(detailTeacherMap);
        bundle.putSerializable("detailDataMap", bean1);
        bundle.putSerializable("detailTeacherMap", bean2);
        fragmentLeft.setArguments(bundle);
        fragments.add(fragmentLeft);

        PlayDetailRightFragment fragmentRight = new PlayDetailRightFragment();
        Bundle catalogueBundle = new Bundle();
        catalogueBundle.putString("catalogueString", catalogueString);
        catalogueBundle.putString("isBuy", isBuy);
        if (rprice == 0) {//免费的直接传sid
            catalogueBundle.putString("sid", sid);
        } else {
            if (isBuy.equals("1")) {//购买的传sid
                catalogueBundle.putString("sid", sid);
            } else {
                catalogueBundle.putString("sid", null);
            }
        }
        fragmentRight.setArguments(catalogueBundle);
        fragments.add(fragmentRight);

        adapter = null;
        adapter = new PlayDetail_ViewPager_Adapter(getSupportFragmentManager(), fragments);
        playdeatil_viewpager.setAdapter(adapter);
        playdeatil_tabLayout.setupWithViewPager(playdeatil_viewpager);
        playdeatil_viewpager.setCurrentItem(1);
//        if (isBuy.equals("1")) {
//            Map<String, String> paramsMap = new HashMap<>();
//            paramsMap.put("id", data_id);
//            paramsMap.put("teach_type", data_teach_type);
//            paramsMap.put("uid", uid);
//            paramsMap.put("pkid", pkid);
//            paramsMap.put("sid", sid);
//            RequestURL.sendPOST("https://app.feimayun.com/Lesson/play", handlePlay, paramsMap);
//        }
        if (aliyunVodPlayerView != null) {
            aliyunVodPlayerView.setCoverUri(detailDataMap.get("bg_url"));//设置播放封面
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

    }

    private void parseJSONPlay(String data) {
//        Util.d("021302", data);
        JSONTokener jsonTokener = new JSONTokener(data);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                //play请求成功，说明这个sid已经learned过了
                sidLearned.add(sid);

                errno = "";//将错误码置空
                JSONObject dataObject = jsonObject.getJSONObject("data");
                playDataMap = new HashMap<>();
                playDataMap.put("id", dataObject.getString("id"));
                playDataMap.put("teach_type", dataObject.getString("teach_type"));
                playDataMap.put("data_id", dataObject.getString("data_id"));
                playDataMap.put("name", dataObject.getString("name"));
                playDataMap.put("title", dataObject.getString("title"));
                playDataMap.put("number", dataObject.getString("number"));
                playDataMap.put("rprice", dataObject.getString("rprice"));
                playDataMap.put("price", dataObject.getString("price"));
                playDataMap.put("tea_id", dataObject.getString("tea_id"));
                playDataMap.put("hours", dataObject.getString("hours"));
                playDataMap.put("plays", dataObject.getString("plays"));
                playDataMap.put("browse", dataObject.getString("browse"));
                playDataMap.put("about", dataObject.getString("about"));
                playDataMap.put("expire", dataObject.getString("expire"));
                playDataMap.put("sid", dataObject.getString("sid"));
                playDataMap.put("sort", dataObject.getString("sort"));
                playDataMap.put("alvid", dataObject.getString("alvid"));
                playDataMap.put("isBuy", dataObject.getString("isBuy"));
                playDataMap.put("kj_name", dataObject.getString("kj_name"));
                playDataMap.put("seek", dataObject.getString("seek"));

                //从后台获取到vid后，设置播放源
                if (playDataMap.get("isBuy").equals("1")) {//只有购买了初始化播放器
                    if (!playDataMap.get("alvid").equals("null")) {//只有章的情况，没有可播放的视频
                        setPlaySource(playDataMap.get("alvid"), playDataMap.get("kj_name"));
                    }
                }

                //在流量状态下，播放器不会完成[setOnPreparedListener 设置视频准备结束的监听事件]
                //这个时候fragmentDiaglog不能取消，只能在流量状态下这里进行取消，同时也不会发生播放器页面错误
                int APNType = StaticUtil.getAPNType(this);
                if (APNType == 4 || APNType == 3 || APNType == 2) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
            } else {
                Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                errno = jsonObject.getString("errno");
            }
            //初始化界面
            initView();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    //设置vid，点击三级item调用
    public void setSid(String sid) {
        this.sid = sid;

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("id", data_id);
        paramsMap.put("teach_type", data_teach_type);
        paramsMap.put("uid", uid);
        paramsMap.put("pkid", pkid);
        paramsMap.put("sid", sid);
        RequestURL.sendPOST("https://app.feimayun.com/Lesson/play", handlePlay, paramsMap);
    }

    public void initData() {
        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        uid = Util.getUid();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("id", data_id);
        paramsMap.put("teach_type", data_teach_type);
        paramsMap.put("uid", uid);
        paramsMap.put("pkid", pkid);
        RequestURL.sendPOST("https://app.feimayun.com/Lesson/detail", handleDetail, paramsMap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_deatil);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            handler();
            //初始化hander
            Intent intent = getIntent();
            data_id = intent.getStringExtra("data_id");
            data_teach_type = intent.getStringExtra("data_teach_type");
            pkid = intent.getStringExtra("pkid");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            playdeatil_view = findViewById(R.id.playdeatil_view);//顶部的预留
            playdeatil_tabLayout = findViewById(R.id.playdeatil_tabLayout);
            playdeatil_viewpager = findViewById(R.id.playdeatil_viewpager);
            //初始化aliyunVodPlayerView
            aliyunVodPlayerView = findViewById(R.id.videoView);
            aliyunVodPlayerView.setKeepScreenOn(true);//保持屏幕敞亮
            aliyunVodPlayerView.setTheme(AliyunVodPlayerView.Theme.Orange);
            //设置是否循环播放
//        aliyunVodPlayerView.setCirclePlay(false);
            //在WIFI状态下，视频播放器执行准备完成，关闭fragmentDialog
            aliyunVodPlayerView.setOnPreparedListener(new MyPrepareListener(this));
            aliyunVodPlayerView.setOnErrorListener(new MyErrorListener(this));
            //设置首帧开始播放的监听事件
            aliyunVodPlayerView.setOnFirstFrameStartListener(new MyFrameInfoListener(this));
            //设置视频切换清晰度的监听事件
            aliyunVodPlayerView.setOnChangeQualityListener(new MyChangeQualityListener(this));
            //屏幕开屏/锁屏监听
//        mScreenStatusController = new ScreenStatusController(this);
//        mScreenStatusController.setScreenStatusListener(new ScreenStatusController.ScreenStatusListener() {
//            //开屏时调用
//            @Override
//            public void onScreenOn() {
//            }
//            //锁屏时调用
//            @Override
//            public void onScreenOff() {
//            }
//        });
//        mScreenStatusController.startListen();
            //设置是否自动播放
            aliyunVodPlayerView.setAutoPlay(true);
            //设置视频播放结束的监听事件
            aliyunVodPlayerView.setOnCompletionListener(new MyCompletionListener(this));
            aliyunVodPlayerView.setOnSeekCompleteListener(new MySeekCompleteListener(this));
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            myPhoneStateListener = new MyPhoneStateListener();
            mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

            //申请权限
            if (EasyPermissions.hasPermissions(PlayDetailActivity.this, PERMS_WRITE)) {
                //网络请求、初始化数据
                initData();
                //初始化AliyunPlayerView
                initAliyunPlayerView();
            } else {
                EasyPermissions.requestPermissions(PlayDetailActivity.this, "视频播放需要开启部分权限",
                        1, PERMS_WRITE);
            }
        }
    }

    private void initView() {
        if (isFirstInit) {
            isFirstInit = false;
        } else {
//                    "status":0,
//                    "msg":"还未到指定的学习时间哦~",
//                    "errno":"E2002",
//                    "error":"还未到指定的学习时间哦。",
//                    "show":1
            if (!errno.equals("E2002")) {
                PlayDetailRightFragment fragment = (PlayDetailRightFragment) adapter.getItem(1);
                fragment.setSid(sid, detailDataMap.get("isBuy"), sidLearned);
            }
        }
    }

    private void setPlaySource(String mVid, String kj_name) {
        //准备播放
        AliyunVidSts mVidSts = new AliyunVidSts();
        mVidSts.setVid(mVid);
        //临时信息
        String akid = "JmAAR5JjLWnTprt7";
        mVidSts.setAcId(akid);
        String aks = "fe9I7LOhyWhXYzr2KSu940RkfePqZB";
        mVidSts.setAkSceret(aks);
        String token = "in-20170622141111085-3xh81i95vr";
        mVidSts.setSecurityToken(token);
        mVidSts.setTitle(kj_name);
        if (QUALITY.equals("")) {
            mVidSts.setQuality(IAliyunVodPlayer.QualityValue.QUALITY_LOW);//设置标清
        } else {
            mVidSts.setQuality(QUALITY);
        }
        if (aliyunVodPlayerView != null) {
            aliyunVodPlayerView.setVidSts(mVidSts);
        }
    }

    private void onPrepared() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        String seek = Objects.requireNonNull(playDataMap.get("seek"));
        if (seek.equals("null")) {
            seek = "0";
        }
        int seekPosition = Integer.parseInt(seek);
        if (seekPosition < 0) {
            seekPosition = 0;
        }
//                Log.i("seek1", seekPosition + "");
        aliyunVodPlayerView.seekTo(seekPosition * 1000);
    }

    private void onError() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void onFirstFrameStart() {
        //清空一下定时器
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        //重新设置定制器
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                //TODO 发送秒数
                if (playDataMap != null) {
                    if (aliyunVodPlayerView != null) {
                        String lid = playDataMap.get("id");
                        String uid = Util.getUid();
                        String sid = playDataMap.get("sid");

                        int seekPosition = aliyunVodPlayerView.getCurrentPosition() / 1000 - 5;
                        if (seekPosition < 0) {
                            seekPosition = 0;
                        }
                        String time = String.valueOf(seekPosition);

                        Map<String, String> map = new HashMap<>();
                        map.put("lid", lid);
                        map.put("uid", uid);
                        map.put("sid", sid);
                        map.put("time", time);
                        RequestURL.sendPOST("https://app.feimayun.com/Lesson/saveRecord", handleSaveRecord, map);
//                                Log.i("tianyanyurecordsecond", "我执行了一次记录:在首帧：" + playDataMap.get("sid") + ", " + time);
                    }
                }
            }
        };
        timer.schedule(task, 10000, 10000);
    }

    private void onChangeQualitySuccess(String finalQuality) {
        Toast.makeText(getApplicationContext(), getString(R.string.log_change_quality_success), Toast.LENGTH_SHORT).show();
        QUALITY = finalQuality;//记录切换的清晰度
    }

    void onChangeQualityFail(int code, String msg) {
        Toast.makeText(getApplicationContext(), getString(R.string.log_change_quality_fail), Toast.LENGTH_SHORT).show();
    }

    private void onCompletion() {
        //播放结束提交一下0秒
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                //发送秒数
                if (playDataMap != null) {
                    if (aliyunVodPlayerView != null) {
                        String lid = playDataMap.get("id");
                        String uid = Util.getUid();
                        String sid = playDataMap.get("sid");
                        String time = "0";

                        Map<String, String> map = new HashMap<>();
                        map.put("lid", lid);
                        map.put("uid", uid);
                        map.put("sid", sid);
                        map.put("time", time);
                        RequestURL.sendPOST("https://app.feimayun.com/Lesson/saveRecord", handleSaveRecord, map);
//                                Log.i("tianyanyurecordsecond", "播放结束：" + playDataMap.get("sid") + ", " + time);
                    }
                }
            }
        };
        timer.schedule(task, 0);
        //播放正常完成时触发
        PlayDetailRightFragment fragment = (PlayDetailRightFragment) adapter.getItem(1);
        String currentSid = playDataMap.get("sid");
        String nextSid = currentSid;
        Set<String> sidSet = fragment.getSidSet();
        Iterator iterator = sidSet.iterator();
        while (iterator.hasNext()) {
            if (currentSid.equals(iterator.next())) {
                if (iterator.hasNext()) {//如果有下一个视频
                    nextSid = (String) iterator.next();
                    break;
                } else {
                    aliyunVodPlayerView.setTipsViewVisivle();
                }
            }
        }
        if (!currentSid.equals(nextSid)) {//相当于：如果当前不是最后一个视频的话
            setSid(nextSid);
        }
    }

    private void onSeekComplete() {
        String lid = playDataMap.get("id");
        String uid = Util.getUid();
        String sid = playDataMap.get("sid");

        int seekPosition = aliyunVodPlayerView.getCurrentPosition() / 1000 - 5;
        if (seekPosition < 0) {
            seekPosition = 0;
        }
        String time = String.valueOf(seekPosition);

        Map<String, String> map = new HashMap<>();
        map.put("lid", lid);
        map.put("uid", uid);
        map.put("sid", sid);
        map.put("time", time);
        RequestURL.sendPOST("https://app.feimayun.com/Lesson/saveRecord", handleSaveRecord, map);
    }

    private void initAliyunPlayerView() {
        //设置边播边缓存的配置
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_save_cache";
        /*
         *  enable：是否可以边播边存。如果为true，则根据后面的几个参数决定是否能够缓存。
         *  saveDir：缓存的目录（绝对路径）
         *  maxDuration：能缓存的单个视频最大长度（单位：秒）。如果单个视频超过这个值，就不缓存。
         *  maxSize：缓存目录的所有缓存文件的总的最大大小（单位：MB）。如果超过则删除最旧文件，如果还是不够，则不缓存。
         */
        aliyunVodPlayerView.setPlayingCache(true, sdDir, 60 * 60 /*时长, s */, 300 /*大小，MB*/);
    }

    @Override
    public void onBackPressed() {
        if (aliyunVodPlayerView != null) {
            AliyunScreenMode mCurrentScreenMode = aliyunVodPlayerView.getScreenMode();
            if (mCurrentScreenMode == AliyunScreenMode.Full) {
                //设置为小屏状态
                aliyunVodPlayerView.changeScreenMode(AliyunScreenMode.Small);
            } else if (mCurrentScreenMode == AliyunScreenMode.Small) {
                //小屏状态下，就结束活动
                aliyunVodPlayerView.onStop();
                aliyunVodPlayerView.onDestroy();
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void updatePlayerViewMode() {
        if (aliyunVodPlayerView != null) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {//如果当前是竖屏
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                aliyunVodPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(uiOptions);

                playdeatil_view.setVisibility(View.VISIBLE);//显示状态栏预留
                //设置布局宽高
                RelativeLayout.LayoutParams aliyunVodPlayerViewLayoutParams = (RelativeLayout.LayoutParams) aliyunVodPlayerView.getLayoutParams();
                aliyunVodPlayerViewLayoutParams.height = (int) (ScreenUtils.getWidth(this) * 9.0f / 16);
                aliyunVodPlayerViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                aliyunVodPlayerView.setLayoutParams(aliyunVodPlayerViewLayoutParams);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {//如果当前是横屏
                //TODO 隐藏状态栏
                if (isStrangePhone()) {
                    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    aliyunVodPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                } else {
                    hideBottomUIMenu();
                }

                playdeatil_view.setVisibility(View.GONE);//隐藏状态栏预留
                //设置布局宽高
                RelativeLayout.LayoutParams aliyunVodPlayerViewLayoutParams = (RelativeLayout.LayoutParams) aliyunVodPlayerView.getLayoutParams();
                aliyunVodPlayerViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                aliyunVodPlayerViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                aliyunVodPlayerView.setLayoutParams(aliyunVodPlayerViewLayoutParams);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (aliyunVodPlayerView != null) {
            boolean handle = aliyunVodPlayerView.onKeyDown(keyCode, event);
            if (!handle) {
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updatePlayerViewMode();
    }

    @Override
    protected void onDestroy() {
        if (aliyunVodPlayerView != null) {
            aliyunVodPlayerView.onDestroy();
            aliyunVodPlayerView = null;
        }
//        if (mScreenStatusController != null) {
//            mScreenStatusController.stopListen();
//        }
        // 取消来电的电话状态监听服务
        if (mTelephonyManager != null && myPhoneStateListener != null) {
            mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.hasPermissions(this, PERMS_WRITE)) {
            initData();
            //初始化AliyunPlayerView
            initAliyunPlayerView();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
//        Toast.makeText(this, "onPermissionsDenied:" + requestCode + ":" + perms.size(), Toast.LENGTH_SHORT).show();

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle("视频播放需要开启必要的权限")
                    .setRationale("您已拒绝开启部分权限，这将导致视频无法正常播放，是否打开设置界面开启权限？")
                    .setNegativeButton("取消")
                    .setPositiveButton("确认")
                    .build()
                    .show();
        } else {
            Toast.makeText(this, "权限拒绝无法播放视频", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 1:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    initAliyunPlayerView();
//                } else {
//                    Toast.makeText(this, "没有sd卡读写权限无法缓存", Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//                break;
//        }
//    }

    @Override
    protected void onStart() {
        if (aliyunVodPlayerView != null) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (task != null) {
                task.cancel();
                task = null;
            }
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    //TODO 发送秒数
                    if (playDataMap != null) {
                        if (aliyunVodPlayerView != null) {
                            String lid = playDataMap.get("id");
                            String uid = Util.getUid();
                            String sid = playDataMap.get("sid");

                            int seekPosition = aliyunVodPlayerView.getCurrentPosition() / 1000 - 5;
                            if (seekPosition < 0) {
                                seekPosition = 0;
                            }
                            String time = String.valueOf(seekPosition);

                            Map<String, String> map = new HashMap<>();
                            map.put("lid", lid);
                            map.put("uid", uid);
                            map.put("sid", sid);
                            map.put("time", time);
                            RequestURL.sendPOST("https://app.feimayun.com/Lesson/saveRecord", handleSaveRecord, map);
//                                Log.i("tianyanyurecordsecond", "我执行了一次记录，在start：" + playDataMap.get("sid") + ", " + time);
                        }
                    }
                }
            };
            timer.schedule(task, 0, 10000);
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
//        if (Util.getUid().equals("")) {
//            Toast.makeText(this, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
//        }
        updatePlayerViewMode();
        if (!fromCall) {
            if (aliyunVodPlayerView != null) {
                if (requestTheAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    aliyunVodPlayerView.onResume();
                } else {
                    Toast.makeText(this, "请关闭其他音频再开始播放", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            fromCall = false;
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (aliyunVodPlayerView != null) {
            aliyunVodPlayerView.onStop();
            if (Build.VERSION.SDK_INT >= 26) {
                MyAudioFocus.releaseTheAudioFocusSDK26(mAudioManager);
            } else {
                releaseTheAudioFocusSDK19(mAudioFocusChangeListener);
            }
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //解决某些手机上锁屏之后会出现标题栏的问题。
        updatePlayerViewMode();
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
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

    //请求音频焦点 设置监听
    private int requestTheAudioFocus() {
//        if (Build.VERSION.SDK_INT < 8) {//Android 2.2开始(API8)才有音频焦点机制
//            return 0;
//        }
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioFocusChangeListener == null) {
            mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//监听器
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                            //播放操作
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            //暂停操作
                            break;
                        default:
                            break;
                    }
                }
            };
        }

        if (Build.VERSION.SDK_INT >= 26) {
            return MyAudioFocus.requestTheAudioFocus(mAudioManager, mAudioFocusChangeListener);
        } else {
            //下面两个常量参数试过很多 都无效，最终反编译了其他app才搞定，汗 ~
            return mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    //zxzhong 暂停、播放完成或退到后台释放音频焦点
    private void releaseTheAudioFocusSDK19(AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener) {
        if (mAudioManager != null && mAudioFocusChangeListener != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE:
                if (EasyPermissions.hasPermissions(this, PERMS_WRITE)) {
                    initData();
                    //初始化AliyunPlayerView
                    initAliyunPlayerView();
                } else {
                    Toast.makeText(this, "权限拒绝无法播放视频", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private static class MyPrepareListener implements IAliyunVodPlayer.OnPreparedListener {
        private WeakReference<PlayDetailActivity> activityWeakReference;

        MyPrepareListener(PlayDetailActivity playDetailActivity) {
            activityWeakReference = new WeakReference<>(playDetailActivity);
        }

        @Override
        public void onPrepared() {
            PlayDetailActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onPrepared();
            }
        }
    }

    private static class MyErrorListener implements IAliyunVodPlayer.OnErrorListener {

        private WeakReference<PlayDetailActivity> activityWeakReference;

        MyErrorListener(PlayDetailActivity playDetailActivity) {
            activityWeakReference = new WeakReference<>(playDetailActivity);
        }

        @Override
        public void onError(int i, int i1, String s) {
            PlayDetailActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onError();
            }
        }
    }

    private static class MyFrameInfoListener implements IAliyunVodPlayer.OnFirstFrameStartListener {

        private WeakReference<PlayDetailActivity> activityWeakReference;

        MyFrameInfoListener(PlayDetailActivity playDetailActivity) {
            activityWeakReference = new WeakReference<>(playDetailActivity);
        }

        @Override
        public void onFirstFrameStart() {
            PlayDetailActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onFirstFrameStart();
            }
        }
    }

    private static class MyChangeQualityListener implements IAliyunVodPlayer.OnChangeQualityListener {

        private WeakReference<PlayDetailActivity> activityWeakReference;

        MyChangeQualityListener(PlayDetailActivity playDetailActivity) {
            activityWeakReference = new WeakReference<>(playDetailActivity);
        }

        @Override
        public void onChangeQualitySuccess(String finalQuality) {

            PlayDetailActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onChangeQualitySuccess(finalQuality);
            }
        }

        @Override
        public void onChangeQualityFail(int code, String msg) {
            PlayDetailActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onChangeQualityFail(code, msg);
            }
        }
    }

    private static class MyCompletionListener implements IAliyunVodPlayer.OnCompletionListener {

        private WeakReference<PlayDetailActivity> activityWeakReference;

        MyCompletionListener(PlayDetailActivity playDetailActivity) {
            activityWeakReference = new WeakReference<>(playDetailActivity);
        }

        @Override
        public void onCompletion() {

            PlayDetailActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onCompletion();
            }
        }
    }

    private static class MySeekCompleteListener implements IAliyunVodPlayer.OnSeekCompleteListener {
        WeakReference<PlayDetailActivity> weakReference;

        MySeekCompleteListener(PlayDetailActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSeekComplete() {
            PlayDetailActivity activity = weakReference.get();
            if (activity != null) {
                activity.onSeekComplete();
            }
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        //重写电话状态改变时触发的方法
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    fromCall = true;
//                    Log.i("tianyasdfjklxc", "响铃:" + phoneNumber);
                    if (aliyunVodPlayerView != null && aliyunVodPlayerView.isPlaying()) {
                        aliyunVodPlayerView.onStop();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
//                    Log.i("tianyasdfjklxc", "接听");
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
//                    Log.i("tianyasdfjklxc", "挂断");
                    break;
            }
        }
    }

}
