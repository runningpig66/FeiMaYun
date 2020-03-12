package cn.aura.feimayun.vhall.watch;

import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchLive;
import com.vhall.business.common.Constants;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.UserInfo;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.UserInfoDataSource;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.aura.feimayun.R;
import cn.aura.feimayun.util.ScreenUtils;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.vhall.Param;
import cn.aura.feimayun.vhall.chat.ChatContract;
import cn.aura.feimayun.vhall.chat.ChatFragment;
import cn.aura.feimayun.vhall.chat.MessageChatData;
import cn.aura.feimayun.vhall.util.emoji.InputUser;
import cn.aura.feimayun.view.MyControlView;
import cn.aura.feimayun.view.SelfDialog2;
//TODO 投屏相关
//import com.vhall.business_support.Watch_Support;
//import com.vhall.business_support.dlna.DeviceDisplay;
//import com.vhall.business_support.WatchLive;
//import org.fourthline.cling.android.AndroidUpnpService;

/**
 * 观看直播的Presenter
 */
public class WatchLivePresenter implements WatchContract.LivePresenter,
        ChatContract.ChatPresenter {
    private static final String TAG = "WatchLivePresenter";
    private Param params;
    private WatchContract.LiveView liveView;

    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
//    ChatContract.ChatView questionView;

    public boolean isWatching = false;
    private WatchLive watchLive;

    int[] scaleTypes = new int[]{Constants.DrawMode.kVHallDrawModeAspectFit.getValue(), Constants.DrawMode.kVHallDrawModeAspectFill.getValue(), Constants.DrawMode.kVHallDrawModeNone.getValue()};
    int currentPos = 0;
    private int scaleType = Constants.DrawMode.kVHallDrawModeAspectFit.getValue();

    private VHVideoPlayerView mPlayView;
    private boolean isHand = false;
    private int isHandStatus = 1;

    CountDownTimer onHandDownTimer;
    private int durationSec = 30; // 举手上麦倒计时

    DocumentFragment documentFragment;

    //自定义
    private WatchLiveFragment liveFragment;
    private String vhall_account = "1";

    public WatchLivePresenter(WatchContract.LiveView liveView,
                              WatchContract.DocumentView documentView,
                              ChatContract.ChatView chatView,
                              WatchContract.WatchView watchView,
                              Param param,
                              DocumentFragment fragment,
                              String vhall_account) {
        this.params = param;
        this.liveView = liveView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.chatView = chatView;
        this.watchView.setPresenter(this);
        this.liveView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.documentFragment = fragment;
        this.liveFragment = liveView.getLiveFragment();
        this.vhall_account = vhall_account;
    }

    @Override
    public void start() {
        getWatchLive().setVRHeadTracker(true);
        getWatchLive().setScaleType(Constants.DrawMode.kVHallDrawModeAspectFit.getValue());
        initWatch();
    }

    @Override
    public void onWatchBtnClick() {
        if (isWatching) {
            stopWatch();
        } else {
            if (getWatchLive().isAvaliable()) {
                startWatch();
            } else {
                initWatch();
            }
        }
    }

    @Override
    public void showChatView(boolean emoji, InputUser user, int limit) {
        watchView.showChatView(emoji, user, limit);
    }

    @Override
    public void sendChat(String text) {
        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        getWatchLive().sendChat(text, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String reason) {
                chatView.showToast(reason);
            }
        });
    }

//    @Override
//    public void sendCustom(JSONObject text) {
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
//            return;
//        }
//        getWatchLive().sendCustom(text, new RequestCallback() {
//            @Override
//            public void onSuccess() {
//            }
//
//            @Override
//            public void onError(int errorCode, String reason) {
//                chatView.showToast(reason);
//            }
//        });
//    }

//    @Override
//    public void sendQuestion(String content) {
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
//            return;
//        }
//        getWatchLive().sendQuestion(content, new RequestCallback() {
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onError(int errorCode, String reason) {
//                questionView.showToast(reason);
//            }
//        });
//    }

    @Override
    public void onLoginReturn() {
        initWatch();
    }

//    @Override
//    public void showSurvey(String url, String title) {
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
//            return;
//        }
//        watchView.showSurvey(url, title);
//    }

//    @Override
//    public void showSurvey(String surveyid) {
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
//            return;
//        }
//        VhallSDK.getSurveyInfo(surveyid, new SurveyDataSource.SurveyInfoCallback() {
//            @Override
//            public void onSuccess(Survey survey) {
//                watchView.showSurvey(survey);
//            }
//
//            @Override
//            public void onError(int errorCode, String errorMsg) {
//                chatView.showToast(errorMsg);
//            }
//        });
//    }

    boolean force = false;

    @Override
    public void onSwitchPixel(String dpi) {
        if (getWatchLive().getDefinition().equals(dpi) && !force) {
            return;
        }
        force = false;
        getWatchLive().setPCSwitchDefinition();
        if (watchView.getActivity().isFinishing()) {
            return;
        }
    }

    @Override
    public void onMobileSwitchRes(String dpi) {
        if (getWatchLive().getDefinition().equals(dpi) && !force) {
            return;
        }
        //TODO CHANGE
//        if (isWatching) {
//            stopWatch();
//        }
        force = false;
        getWatchLive().setDefinition(dpi);
    }

    @Override
    public int setScaleType() {
        scaleType = scaleTypes[(++currentPos) % scaleTypes.length];
        getWatchLive().setScaleType(scaleType);
//        liveView.setScaleButtonText(scaleType);
        return scaleType;
    }

    @Override
    public int changeOriention() {
        return watchView.changeOrientation();
    }

    @Override
    public void onDestory() {
        getWatchLive().destory();
    }

    @Override
    public void submitLotteryInfo(String id, String lottery_id, String nickname, String phone) {
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(lottery_id)) {
            VhallSDK.submitLotteryInfo(id, lottery_id, nickname, phone, new RequestCallback() {
                @Override
                public void onSuccess() {
//                    watchView.showToast("信息提交成功");
                }

                @Override
                public void onError(int errorCode, String reason) {
                }
            });
        }
    }

    @Override
    public String getCurrentPixel() {
        return getWatchLive().getDefinition();
    }

    @Override
    public int getScaleType() {
        if (getWatchLive() != null) {
            return getWatchLive().getScaleType();
        }
        return -1;
    }

//    @Override
//    public void setHeadTracker() {
//        if (!getWatchLive().isVR()) {
//            watchView.showToast("当前活动为非VR活动，不可使用陀螺仪");
//            return;
//        }
//        getWatchLive().setVRHeadTracker(!getWatchLive().isVRHeadTracker());
//        liveView.reFreshView();
//    }

//    @Override
//    public boolean isHeadTracker() {
//        return getWatchLive().isVRHeadTracker();
//    }

    @Override
    public void initWatch() {
        //游客ID及昵称 已登录用户可传空
        TelephonyManager telephonyMgr = (TelephonyManager) watchView.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//        String customeId = telephonyMgr.getDeviceId();

        //登录聊天账号
        String uid = Util.getUid();
        //登录微吼账号，用于聊天
        String username = vhall_account.equals("1") ? "wxh" + uid : "sch" + uid;
        String userpass = "1q2w3e4r5t6y7u8i9o";
//        Log.i("0610051", username + "      " + userpass);
        VhallSDK.login(username, userpass, new UserInfoDataSource.UserInfoCallback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                String customId = "";
                String customNickname = userInfo.nick_name;
                VhallSDK.initWatch(params.watchId, customId, customNickname, params.key, getWatchLive(), WebinarInfo.LIVE, new RequestCallback() {
                    @Override
                    public void onSuccess() {
                        if (watchView.getActivity().isFinishing())
                            return;
                        chatView.clearChatData();
                        getChatHistory();
                        startWatch();//initWatch成功，直接开始播放直播
                        //根据房间信息设置，是否展示文档
// TODO                       operationDocument();
                    }

                    @Override
                    public void onError(int errorCode, String msg) {
                        if (errorCode == 20003) {//error param!
                        } else {
                            Toast.makeText(watchView.getActivity(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode, String reason) {
                //游客ID及昵称 已登录用户可传空
                String customeId;
                String customNickname;
                if (VhallSDK.isLogin()) {
                    customeId = "";
                    customNickname = VhallSDK.getUserNickname();
                } else {
                    customeId = Build.BOARD + Build.DEVICE + Build.SERIAL;//SERIAL  串口序列号 保证唯一值
                    customNickname = Build.BRAND + "手机用户";
                }
                VhallSDK.initWatch(params.watchId, customeId, customNickname, params.key, getWatchLive(), WebinarInfo.LIVE, new RequestCallback() {
                    @Override
                    public void onSuccess() {
                        if (watchView.getActivity().isFinishing())
                            return;
                        chatView.clearChatData();
                        getChatHistory();
//                getAnswerList();
                        startWatch();//initWatch成功，直接开始播放直播
                        //根据房间信息设置，是否展示文档
// TODO                       operationDocument();
                    }

                    @Override
                    public void onError(int errorCode, String msg) {
                        if (errorCode == 20003) {//error param!
                        } else {
                            Toast.makeText(watchView.getActivity(), msg, Toast.LENGTH_SHORT).show();
                        }
//                watchView.showToast(msg);
                    }

                });
            }

        });

    }

//    private void getAnswerList() {
//        VhallSDK.getAnswerList(params.watchId, new ChatServer.ChatRecordCallback() {
//            @Override
//            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
//                questionView.notifyDataChangedQe(ChatFragment.CHAT_EVENT_QUESTION, list);
//            }
//
//            @Override
//            public void onFailed(int errorcode, String messaage) {
////                Toast.makeText(watchView.getActivity(), messaage, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    @Override
    public void startWatch() {
        getWatchLive().start();
        liveFragment.updateViewState(MyControlView.PlayState.Playing);
        documentFragment.updateViewState(MyControlView.PlayState.Playing);
    }

    @Override
    public void stopWatch() {
        if (isWatching) {
            getWatchLive().stop();
            isWatching = false;
            liveFragment.updateViewState(MyControlView.PlayState.Paused);
            documentFragment.updateViewState(MyControlView.PlayState.Paused);
        }
    }

    private WatchLive getWatchLive() {
        if (watchLive == null) {
            RelativeLayout watchLayout = liveView.getWatchLayout();
            WatchLive.Builder builder = new WatchLive.Builder()
                    .context(watchView.getActivity().getApplicationContext())
                    .containerLayout(watchLayout)
                    .bufferDelay(params.bufferSecond)
                    .callback(new WatchCallback())
                    .messageCallback(new MessageEventCallback())
                    .connectTimeoutMils(5000)
                    .chatCallback(new ChatCallback());
            watchLive = builder.build();
        }
        //狄拍builder
//        if (watchLive == null) {
//            WatchLive.Builder builder = new WatchLive.Builder()
//                    .context(watchView.getActivity().getApplicationContext())
//                    .bufferDelay(params.bufferSecond)
//                    .callback(new WatchCallback())
//                    .messageCallback(new MessageEventCallback())
//                    .connectTimeoutMils(5000)
//                    .playView(mPlayView = new VRPlayView(watchView.getActivity().getApplicationContext()))//todo 添加到自定义布局中，非new
//                    .chatCallback(new ChatCallback());
//            watchLive = builder.build();
//            liveView.getWatchLayout().addView((VRPlayView) mPlayView, 640, 480);
//            ((VRPlayView) mPlayView).getHolder().setFixedSize(640, 480);
//        }
        return watchLive;
    }

    //签到
    @Override
    public void signIn(String signId) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        VhallSDK.performSignIn(params.watchId, signId, new RequestCallback() {
            @Override
            public void onSuccess() {
                watchView.showToast("签到成功");
                watchView.dismissSignIn();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                watchView.showToast(errorMsg);
            }
        });
    }

    //提交问卷 需要先登录且watch已初始化完成
//    @Override
//    public void submitSurvey(String result) {
//        /*if (!VhallSDK.isLogin()) {
//            watchView.showToast("请先登录！");
//            return;
//        }
//        JSONObject obj = null;
//        try {
//            obj = new JSONObject(result);
//            String qId = obj.optString("question_id");
//            VhallSDK.submitSurveyInfo(getWatchLive(), qId, result, new RequestCallback() {
//                @Override
//                public void onSuccess() {
//                    watchView.showToast("提交成功！");
//                    watchView.dismissSurvey();
//                }
//
//                @Override
//                public void onError(int errorCode, String errorMsg) {
//                    watchView.showToast(errorMsg);
//                    if (errorCode == 10821) {
//                        watchView.dismissSurvey();
//                    }
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }*/
//    }

//    @Override
//    public void submitSurvey(Survey survey, String result) {
//        if (survey == null)
//            return;
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast("请先登录！");
//            return;
//        }
//        VhallSDK.submitSurveyInfo(getWatchLive(), survey.surveyid, result, new RequestCallback() {
//            @Override
//            public void onSuccess() {
//                watchView.showToast("提交成功！");
//                watchView.dismissSurvey();
//            }
//
//            @Override
//            public void onError(int errorCode, String errorMsg) {
//                watchView.showToast(errorMsg);
//                if (errorCode == 10821)
//                    watchView.dismissSurvey();
//            }
//        });
//    }

//    @Override
//    public void onRaiseHand() {
//        getWatchLive().onRaiseHand(params.watchId, isHand ? 0 : 1, new RequestCallback() {
//            @Override
//            public void onSuccess() {
//                if (isHand) {
//                    isHand = false;
//                    watchView.refreshHand(0);
//                    if (onHandDownTimer != null) {
//                        onHandDownTimer.cancel();
//                    }
//                } else {
//                    Log.e(TAG, "举手成功");
//                    startDownTimer(durationSec);
//                    isHand = true;
//                }
//            }
//
//            @Override
//            public void onError(int errorCode, String errorMsg) {
//                watchView.showToast("举手失败，errorMsg:" + errorMsg);
//            }
//        });
//    }

//    @Override
//    public void replyInvite(int type) {
//        getWatchLive().replyInvitation(params.watchId, type, new RequestCallback() {
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onError(int errorCode, String errorMsg) {
//                watchView.showToast("上麦状态反馈异常，errorMsg:" + errorMsg);
//            }
//        });
//    }

    //TODO 投屏相关
//
//    @Override
//    public void dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
//        getWatchLive().dlnaPost(deviceDisplay, service, new Watch_Support.DLNACallback() {
//
//            @Override
//            public void onError(int errorCode) {
//                watchView.showToast("投屏失败，errorCode:" + errorCode);
////            }
//
//            @Override
//            public void onSuccess() {
//                watchView.showToast("投屏成功!");
//                stopWatch();
//            }
//
//        });
//    }
//
//    @Override
//    public void showDevices() {
//        watchView.showDevices();
//    }
//
//    @Override
//    public void dismissDevices() {
//        watchView.dismissDevices();
//    }

    /**
     * 观看过程中事件监听
     */
    private class WatchCallback implements VHPlayerListener {
        @Override
        public void onStateChanged(com.vhall.player.Constants.State state) {
            switch (state) {
                case START:
                    isWatching = true;
                    liveView.showLoading(false);
//                    liveView.setPlayPicture(isWatching);
                    liveFragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
                    ((WatchActivity) liveFragment.getActivity()).setPlace();
                    ((WatchActivity) liveFragment.getActivity()).setPlace();
                    ((WatchActivity) liveFragment.getActivity()).setFirstIntMoveModeSize();
                    break;
                case BUFFER:
                    if (isWatching) {
                        liveView.showLoading(true);
                    }
                    break;
                case STOP:
                    isWatching = false;
                    liveView.showLoading(false);
//                    liveView.setPlayPicture(isWatching);
                    liveFragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
                    break;
            }
        }

        public void onEvent(int event, String msg) {
            switch (event) {
                case com.vhall.player.Constants.Event.EVENT_DOWNLOAD_SPEED:
//                    liveView.setDownSpeed("速率" + msg + "/kbps");
                    break;
                case com.vhall.player.Constants.Event.EVENT_DPI_CHANGED:
                    //分辨率切换
                    Log.i(TAG, msg);
                    break;
                case com.vhall.player.Constants.Event.EVENT_DPI_LIST:
                    //支持的分辨率 msg
                    try {
                        JSONArray array = new JSONArray(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case com.vhall.player.Constants.Event.EVENT_VIDEO_SIZE_CHANGED:
                    Log.i(TAG, msg);
                    break;
                case com.vhall.player.Constants.Event.EVENT_STREAM_START://发起端开始推流
                    break;
                case com.vhall.player.Constants.Event.EVENT_STREAM_STOP://发起端停止推流
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int errorCode, int innerCode, String msg) {
            switch (errorCode) {
                case com.vhall.player.Constants.ErrorCode.ERROR_CONNECT:
                    Log.e(TAG, "ERROR_CONNECT  ");
                    isWatching = false;
                    liveView.showLoading(false);
//                    liveView.setPlayPicture(isWatching);
                    liveFragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
                    Toast.makeText(watchView.getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
//                    documentFragment.setPlayIcon(!isWatching);
//                    watchView.showToast(errorMsg);
                    break;
                default:
//                    watchView.showToast(errorMsg);
            }
        }
    }

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback implements MessageServer.Callback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
            Log.e(TAG, "messageInfo " + messageInfo.event);
            switch (messageInfo.event) {
                case MessageServer.EVENT_DISABLE_CHAT://禁言
//                    watchView.showToast("您已被禁言");
                    Toast.makeText(watchView.getActivity(), "您已被禁言", Toast.LENGTH_SHORT).show();
                    break;
                case MessageServer.EVENT_KICKOUT://踢出
//                    watchView.showToast("您已被踢出");
                    final SelfDialog2 mSelfDialog2 = new SelfDialog2(watchView.getActivity());
                    mSelfDialog2.setTitle("温馨提示");
                    mSelfDialog2.setMessage("您已被踢出，请联系活动组织者");
                    mSelfDialog2.setYesOnclickListener("确认", new SelfDialog2.onYesOnclickListener() {
                        @Override
                        public void onYesClick() {
                            watchView.getActivity().finish();
                            mSelfDialog2.dismiss();
                        }
                    });
                    WindowManager.LayoutParams params = Objects.requireNonNull(mSelfDialog2.getWindow()).getAttributes();
                    params.width = (int) (ScreenUtils.getWidth(watchView.getActivity()) * 0.7);
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mSelfDialog2.getWindow().setAttributes(params);
                    mSelfDialog2.show();
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
//                    watchView.showToast("您已被解除禁言");
                    Toast.makeText(watchView.getActivity(), "您已被解除禁言", Toast.LENGTH_SHORT).show();
                    break;
                case MessageServer.EVENT_CHAT_FORBID_ALL://全员禁言
                    if (messageInfo.status == 0) {
                        //取消全员禁言
//                        watchView.showToast("解除全员禁言");
                        Toast.makeText(watchView.getActivity(), "解除全员禁言", Toast.LENGTH_SHORT).show();
                    } else {
                        //全员禁言
//                        watchView.showToast("全员禁言");
                        Toast.makeText(watchView.getActivity(), "全员禁言中", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MessageServer.EVENT_OVER://直播结束
//                    watchView.showToast("直播已结束");
                    Toast.makeText(watchView.getActivity(), "直播已结束", Toast.LENGTH_SHORT).show();

//                    final SelfDialog mSelfDialog = new SelfDialog(watchView.getActivity());
//                    mSelfDialog.setCancelable(true);
//                    mSelfDialog.setCanceledOnTouchOutside(true);
//                    mSelfDialog.setTitle("温馨提示");
//                    mSelfDialog.setMessage("直播已结束，是否退出直播间？");
//                    mSelfDialog.setYesOnclickListener("确认", new SelfDialog.onYesOnclickListener() {
//                        @Override
//                        public void onYesClick() {
//                            watchView.getActivity().finish();
//                            mSelfDialog.dismiss();
//                        }
//                    });
//                    mSelfDialog.setNoOnclickListener("取消", new SelfDialog.onNoOnclickListener() {
//                        @Override
//                        public void onNoClick() {
//                            mSelfDialog.dismiss();
//                        }
//                    });
//                    WindowManager.LayoutParams params2 = Objects.requireNonNull(mSelfDialog.getWindow()).getAttributes();
//                    params2.width = (int) (ScreenUtils.getWidth(watchView.getActivity()) * 0.7);
//                    params2.height = WindowManager.LayoutParams.WRAP_CONTENT;
//                    mSelfDialog.getWindow().setAttributes(params2);
//                    mSelfDialog.show();

                    stopWatch();
                    break;
                case MessageServer.EVENT_DIFINITION_CHANGED:
//                    Log.e(TAG, "EVENT_DIFINITION_CHANGED PC 端切换分辨率");
//                    liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
                    onSwitchPixel(com.vhall.player.Constants.Rate.DPI_SAME);
//                    if (!getWatchLive().isDifinitionAvailable(getWatchLive().getDefinition())) {
//                        onSwitchPixel(WatchLive.DPI_DEFAULT);
//                    }
                    break;
                case MessageServer.EVENT_START_LOTTERY://抽奖开始
//                    watchView.showLottery(messageInfo);
                    break;
                case MessageServer.EVENT_END_LOTTERY://抽奖结束
//                    watchView.showLottery(messageInfo);
                    break;
                case MessageServer.EVENT_NOTICE://公告
                    watchView.showNotice(messageInfo.content);
                    break;
                case MessageServer.EVENT_SIGNIN: //签到消息
                    if (!TextUtils.isEmpty(messageInfo.id) && !TextUtils.isEmpty(messageInfo.sign_show_time)) {
                        watchView.showSignIn(messageInfo.id, parseTime(messageInfo.sign_show_time, 30));
                    }
                    break;
                case MessageServer.EVENT_QUESTION: // 问答开关
//                    watchView.showToast("问答功能已" + (messageInfo.status == 0 ? "关闭" : "开启"));
                    break;
                case MessageServer.EVENT_SURVEY://问卷

                    /**
                     * 获取msg内容
                     */

                    MessageChatData surveyData = new MessageChatData();
                    surveyData.event = MessageChatData.eventSurveyKey;
                    surveyData.setUrl(VhallSDK.getSurveyUrl(messageInfo.id, messageInfo.webinar_id, messageInfo.user_id));
                    surveyData.setId(messageInfo.id);
                    chatView.notifyDataChangedChat(surveyData);
                    break;
                case MessageServer.EVENT_SHOWDOC://文档开关指令 1 使用文档 0 关闭文档
                    Log.e(TAG, "onEvent:show_docType:watchType= " + messageInfo.watchType);
                    getWatchLive().setIsUseDoc(messageInfo.watchType);
// TODO                   operationDocument();
                    break;
                case MessageServer.EVENT_CLEARBOARD:
                case MessageServer.EVENT_DELETEBOARD:
                case MessageServer.EVENT_INITBOARD:
                case MessageServer.EVENT_PAINTBOARD:
                    if (getWatchLive().isUseDoc()) {
                        documentView.paintBoard(messageInfo);
                    }
                    break;
                case MessageServer.EVENT_SHOWBOARD:
                    getWatchLive().setIsUseBoard(messageInfo.showType);
                    if (getWatchLive().isUseDoc()) {
                        documentView.paintBoard(messageInfo);
                    }
                    break;
                case MessageServer.EVENT_CHANGEDOC://PPT翻页消息
                case MessageServer.EVENT_CLEARDOC:
                case MessageServer.EVENT_PAINTDOC:
                case MessageServer.EVENT_DELETEDOC:
//                    Log.e(TAG, " event " + messageInfo.event);
                    documentView.paintPPT(messageInfo);
                    break;
                case MessageServer.EVENT_RESTART:
                    force = true;
                    //onSwitchPixel(WatchLive.DPI_DEFAULT);
                    break;
                case MessageServer.EVENT_INTERACTIVE_HAND:
//                    Log.e(TAG, " status " + messageInfo.status);
                    /** 互动举手消息 status = 1  允许上麦  */
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_MIC:
//                    getWatchLive().disconnectMsgServer(); // 关闭watchLive中的消息
//                    watchView.enterInteractive();
//                    if (onHandDownTimer != null) {
////                        isHand = false; //重置是否举手标识
////                        onHandDownTimer.cancel();
////                        watchView.refreshHand(0);
////                    }
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_HAND:
//                    watchView.showToast(messageInfo.status == 0 ? "举手按钮关闭" : "举手按钮开启");
                    break;
                case MessageServer.EVENT_INVITED_MIC://被邀请上麦
//                    watchView.showInvited();
                    break;
            }
        }

        public int parseTime(String str, int defaultTime) {
            int currentTime = 0;
            try {
                currentTime = Integer.parseInt(str);
                if (currentTime == 0) {
                    return defaultTime;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return currentTime;
        }

        @Override
        public void onMsgServerConnected() {
        }

        @Override
        public void onConnectFailed() {
//            getWatchLive().connectMsgServer();
        }

        @Override
        public void onMsgServerClosed() {
        }
    }

    /**
     * 根据文档状态选择展示
     */
    private void operationDocument() {
        if (!getWatchLive().isUseDoc()) {
            documentView.showType(2);//关闭文档
        } else {
            //展示文档
            if (getWatchLive().isUseBoard()) {
                //当前为白板
                documentView.showType(1);
            } else {
                documentView.showType(0);
            }
        }
    }

//    public void startDownTimer(int secondTimer) {
//        onHandDownTimer = new CountDownTimer(secondTimer * 1000 + 1080, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                watchView.refreshHand((int) millisUntilFinished / 1000 - 1);
//            }
//
//            @Override
//            public void onFinish() {
//                onHandDownTimer.cancel();
//                onRaiseHand();
//            }
//        }.start();
//    }

    private class ChatCallback implements ChatServer.Callback {
        @Override
        public void onChatServerConnected() {
            Log.e(TAG, "CHAT CONNECTED ");
        }

        @Override
        public void onConnectFailed() {
            Log.e(TAG, "CHAT CONNECT FAILED");
            getWatchLive().connectChatServer();
        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    Log.d(TAG, "eventMsgKey: ");
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
//                    liveView.addDanmu(chatInfo.msgData.text);
                    break;
                case ChatServer.eventCustomKey:
                    Log.d(TAG, "eventCustomKey: ");
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                case ChatServer.eventOnlineKey:
                    Log.d(TAG, "eventOnlineKey: ");
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                case ChatServer.eventOfflineKey:
                    Log.d(TAG, "eventOfflineKey: ");
                    chatView.notifyDataChangedChat(MessageChatData.getChatData(chatInfo));
                    break;
                case ChatServer.eventQuestion:
                    Log.d(TAG, "eventQuestion: ");
//                    questionView.notifyDataChangedQe(chatInfo);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onChatServerClosed() {
        }
    }

    private void getChatHistory() {
        getWatchLive().acquireChatRecord(true, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                List<MessageChatData> list1 = new ArrayList<>();
                for (ChatServer.ChatInfo chatInfo : list) {
                    list1.add(MessageChatData.getChatData(chatInfo));
                }
                chatView.notifyDataChangedChat(ChatFragment.CHAT_EVENT_CHAT, list1);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
//                Log.e(TAG, "onFailed->" + errorcode + ":" + messaage);
            }
        });
    }

    public void setParams(Param params) {
        this.params = params;
    }

        /*
    //核心模块中已经实现VR渲染器，可直接使用
    //狄拍自定义渲染
    public class VRPlayView extends GL_Preview_YUV implements IVHVideoPlayer {
        AtomicBoolean mIsReady = new AtomicBoolean(false);
        public VRPlayView(Context var1) {
            super(var1);
        }

        public VRPlayView(Context var1, AttributeSet var2) {
            super(var1, var2);
        }

        public void setDrawMode(int model) {
            super.setDrawMode(model);
        }

        public void setIsHeadTracker(boolean head) {
            super.setIsHeadTracker(head);
        }

        public boolean init(int width, int height) {
            super.setPreviewW(width);
            super.setPreviewH(height);
            super.setIsFlip(true);
            super.setColorFormat(19);
            mIsReady.set(true);
            return false;
        }

        @Override
        public void play(byte[] bytes, int i, int i1) {

        }

        public void playView(byte[] YUV) {
            if (this.isReady()) {
                this.setdata(YUV);
            }
        }

        public boolean isReady() {
            return mIsReady.get();
        }

        public void release() {
            this.setRelease();
        }
    }*/
}

