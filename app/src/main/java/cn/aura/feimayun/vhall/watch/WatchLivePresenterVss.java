package cn.aura.feimayun.vhall.watch;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.common.Constants;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.UserInfo;
import com.vhall.business.data.source.UserInfoDataSource;
import com.vhall.document.DocumentView;
import com.vhall.lss.play.VHLivePlayer;
import com.vhall.message.ConnectServer;
import com.vhall.ops.VHOPS;
import com.vhall.player.VHPlayerListener;
import com.vhall.player.stream.play.impl.VHVideoPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import vhall.com.vss.CallBack;
import vhall.com.vss.VssSdk;
import vhall.com.vss.data.MessageData;
import vhall.com.vss.data.ResponseChatInfo;
import vhall.com.vss.data.ResponseRoomInfo;
import vhall.com.vss.data.ResponseUserStatus;
import vhall.com.vss.data.VssMessageAnnouncementData;
import vhall.com.vss.data.VssMessageChatData;
import vhall.com.vss.data.VssMessageQuestionData;
import vhall.com.vss.data.VssMessageSignData;
import vhall.com.vss.module.chat.VssChatManger;
import vhall.com.vss.module.room.VssRoomManger;
import vhall.com.vss.module.room.callback.IVssCallBackLister;
import vhall.com.vss.module.room.callback.IVssMessageLister;
import vhall.com.vss.module.rtc.VssRtcManger;
import vhall.com.vss.module.sign.VssSignManger;

import static com.vhall.ops.VHOPS.ERROR_CONNECT;
import static com.vhall.ops.VHOPS.ERROR_DOC_INFO;
import static com.vhall.ops.VHOPS.ERROR_SEND;
import static com.vhall.ops.VHOPS.KEY_OPERATE;
import static com.vhall.ops.VHOPS.TYPE_ACTIVE;
import static com.vhall.ops.VHOPS.TYPE_SWITCHOFF;
import static com.vhall.ops.VHOPS.TYPE_SWITCHON;
//TODO 投屏相关
//import com.vhall.business_support.Watch_Support;
//import com.vhall.business_support.dlna.DeviceDisplay;
//import com.vhall.business_support.WatchLive;
//import org.fourthline.cling.android.AndroidUpnpService;

/**
 * 观看直播的Presenter
 */
public class WatchLivePresenterVss implements WatchContract.LivePresenter,
        ChatContract.ChatPresenter {
    private static final String TAG = "WatchLivePresenterVss";
    private Context context;
    private WatchContract.LiveView liveView;

    WatchContract.DocumentViewVss documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
//    ChatContract.ChatView questionView;

    public boolean isWatching = false;
    //    private WatchLive watchLive;
    private VHLivePlayer mPlayer;
    private VHVideoPlayerView mVideoPlayer;
    private VHOPS mDocument;
    int[] scaleTypes = new int[]{Constants.DrawMode.kVHallDrawModeAspectFit.getValue(), Constants.DrawMode.kVHallDrawModeAspectFill.getValue(), Constants.DrawMode.kVHallDrawModeNone.getValue()};
    int currentPos = 0;
    private int scaleType = Constants.DrawMode.kVHallDrawModeAspectFit.getValue();

    private boolean isHand = false;
    CountDownTimer onHandDownTimer;
    private int durationSec = 30; // 举手上麦倒计时
    private String roomId;
    private String accessToken;
    private String currentDPI = "";
    private String canSpeak = "0";
    private Param params;
    private boolean isBroadcast = false;
    //    private VHVideoPlayerView mPlayView;
//    private int isHandStatus = 1;
    DocumentFragmentVss documentFragment;

    //自定义
    private WatchLiveFragment liveFragment;
    private String vhall_account = "1";

    public WatchLivePresenterVss(WatchContract.LiveView liveView,
                                 WatchContract.DocumentViewVss documentView,
                                 ChatContract.ChatView chatView,
                                 final WatchContract.WatchView watchView,
                                 Param param,
                                 DocumentFragmentVss fragment,
                                 String vhall_account) {
        this.params = param;
        this.liveView = liveView;
        this.documentView = documentView;
        this.watchView = watchView;
//        this.questionView = questionView;
        this.chatView = chatView;
        this.watchView.setPresenter(this);
        this.liveView.setPresenter(this);
        this.chatView.setPresenter(this);
//        this.questionView.setPresenter(this);
        this.documentFragment = fragment;
        this.liveFragment = liveView.getLiveFragment();
        this.vhall_account = vhall_account;
        context = watchView.getActivity();

        VssRoomManger.getInstance().enterRoom(param.vssToken, param.vssRoomId, new CallBack<ResponseRoomInfo>() {
            @Override
            public void onSuccess(final ResponseRoomInfo result) {
                if (!VhallSDK.isLogin()) {
                    VssSdk.getInstance().setUserId(result.getThird_party_user_id());
                }
                if (VhallSDK.isLogin()) {
                    VssRtcManger vssRtcManger = new VssRtcManger();
                    vssRtcManger.getUserStatus(new CallBack<ResponseUserStatus>() {
                        @Override
                        public void onSuccess(ResponseUserStatus userStatus) {
                            if (userStatus != null && !TextUtils.isEmpty(userStatus.getIs_banned())) {
                                canSpeak = userStatus.getIs_banned();
                            }
                        }

                        @Override
                        public void onError(int eventCode, String msg) {
                            watchView.showToast("eventCode2: " + eventCode + ", msg: " + msg);
                        }
                    });
                }
                if (result.getStatus() == 1) {
                    isBroadcast = true;
                } else {
                    isBroadcast = false;
                    if (result.getStatus() == 2 && !TextUtils.isEmpty(result.getRecord_id())) {
                        watchView.showToast("当前是视频回放，还没开始直播");
                        return;
                    }
                    watchView.showToast("还没开始直播");
                }
                VssRoomManger.getInstance().setVssMessageLister(new MyLister(), IVssMessageLister.MESSAGE_SERVICE_TYPE_ALL);
                VssRoomManger.getInstance().setVssCallBackLister(new MyCallback());
                roomId = result.getRoom_id();
                accessToken = result.getPaas_access_token();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mDocument = new VHOPS(context, result.getChannel_id(), result.getRoom_id(), accessToken);
                        mDocument.setListener(opsListener);
                        mDocument.join();
                    }
                });
                initWatch();
            }

            @Override
            public void onError(int eventCode, String msg) {
                watchView.showToast("eventCode1: " + eventCode + ", msg: " + msg);
                isBroadcast = false;
            }
        });
    }

    public DocumentView getActiveView() {
        return activeView;
    }

    private DocumentView activeView;

    private VHOPS.EventListener opsListener = new VHOPS.EventListener() {
        @Override
        public void onEvent(String event, String type, String cid) {
            if (event.equals(KEY_OPERATE)) {
                if (type.equals(TYPE_ACTIVE)) {
                    activeView = mDocument.getActiveView();
                    documentView.refreshView(mDocument.getActiveView());
                } else if (type.equals(TYPE_SWITCHOFF) || type.equals(TYPE_SWITCHON)) {
                    //文档演示 开关
                    documentView.switchType(type);
                }
            }
        }

        @Override
        public void onError(int errorCode, int innerError, String errorMsg) {
            switch (errorCode) {
                case ERROR_CONNECT:
                case ERROR_SEND:
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_DOC_INFO:
                    try {
                        JSONObject obj = new JSONObject(errorMsg);
                        String msg = obj.optString("msg");
                        String cid = obj.optString("cid");
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private VHLivePlayer getPlayer() {
        if (mPlayer == null) {
            RelativeLayout watchLayout = liveView.getWatchLayout();
            mVideoPlayer = new VHVideoPlayerView(context);
            mVideoPlayer.setDrawMode(com.vhall.player.Constants.VideoMode.DRAW_MODE_ASPECTFIT);
            watchLayout.addView(mVideoPlayer);
            mPlayer = new VHLivePlayer.Builder()
                    .videoPlayer(mVideoPlayer)
                    .listener(new MyListener())
                    .build();
        }
        return mPlayer;
    }

    @Override
    public void start() {
        getPlayer();
        if (!TextUtils.isEmpty(params.noticeContent)) {
            watchView.showNotice(params.noticeContent);
        }
//        getWatchLive().setVRHeadTracker(true);
//        getWatchLive().setScaleType(Constants.DrawMode.kVHallDrawModeAspectFit.getValue());
        initWatch();
        getPlayer().start(roomId, accessToken);
    }

    @Override
    public void onWatchBtnClick() {
        if (isWatching) {
            stopWatch();
            Log.d(TAG, "onWatchBtnClick: stopWatch");
        } else {
            if (getPlayer().resumeAble()) {
                getPlayer().resume();
                Log.d(TAG, "onWatchBtnClick: resume");
            } else {
                getPlayer().start(roomId, accessToken);
                Log.d(TAG, "onWatchBtnClick: start");
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
            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        if ("1".equals(canSpeak)) {
            watchView.showToast("你被禁言了");
            return;
        }
        VssRoomManger.getInstance().sendMsg(text, "", new CallBack<ResponseRoomInfo>() {
            @Override
            public void onSuccess(ResponseRoomInfo result) {
            }

            @Override
            public void onError(int eventCode, String msg) {
                chatView.showToast(msg);
            }
        });

    }

//    @Override
//    public void sendCustom(JSONObject text) {
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
//            return;
//        }
//        if ("1".equals(canSpeak)) {
//            watchView.showToast("你被禁言了");
//            return;
//        }
//        VssRoomManger.getInstance().sendMsg(text.toString(), VHIM.TYPE_CUSTOM, new CallBack<ResponseRoomInfo>() {
//            @Override
//            public void onSuccess(ResponseRoomInfo result) {
//            }
//
//            @Override
//            public void onError(int eventCode, String msg) {
//                chatView.showToast(msg);
//            }
//        });
//    }

//    @Override
//    public void sendQuestion(final String content) {
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
//            return;
//        }
//        if ("1".equals(canSpeak)) {
//            watchView.showToast("你被禁言了");
//            return;
//        }
//        sendQuestion(VhallSDK.user.user_id, content, new RequestCallback() {
//            @Override
//            public void onSuccess() {
//                chatView.showToast("发送成功");
//            }
//
//            @Override
//            public void onError(int errorCode, String errorMsg) {
//                questionView.showToast(errorMsg);
//            }
//        });
//    }

//    private void sendQuestion(String user_id, String content, final RequestCallback callback) {
//        if (TextUtils.isEmpty(content) || content.length() > 200) {
//            VhallCallback.ErrorCallback(callback, ErrorCode.ERROR_PARAM, "聊天内容长度在0-200之间");
//            return;
//        }
//        if (TextUtils.isEmpty(user_id) || TextUtils.isEmpty(params.webinar_id)) {
//            VhallCallback.ErrorCallback(callback, ErrorCode.ERROR_INIT, "获取视频信息失败！");
//            return;
//        }
//        UserInfoRepository userInfoRepository = UserInfoRepository.getInstance(UserInfoRemoteDataSource.getInstance(), UserInfoLocalDataSource.getInstance());
//        userInfoRepository.sendQuestion(user_id, params.webinar_id, content, new RequestCallback() {
//            @Override
//            public void onSuccess() {
//                if (callback != null) {
//                    callback.onSuccess();
//                }
//            }
//
//            @Override
//            public void onError(int errorCode, String reason) {
//                VhallCallback.ErrorCallback(callback, errorCode, reason);
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
//    }

    boolean force = false;

    @Override
    public void onSwitchPixel(String dpi) {
        if (dpi.equals(currentDPI)) {
            return;
        }
        currentDPI = dpi;
    }

    @Override
    public void onMobileSwitchRes(String dpi) {
        if (dpi.equals(currentDPI)) {
            return;
        }
        currentDPI = dpi;
        getPlayer().setDPI(dpi);
    }

    @Override
    public int setScaleType() {
        scaleType = scaleTypes[(++currentPos) % scaleTypes.length];
        getPlayer().setDrawMode(scaleType);
//        liveView.setScaleButtonText(scaleType);
        return scaleType;
    }

    @Override
    public int changeOriention() {
        return watchView.changeOrientation();
    }

    @Override
    public void onDestory() {
        if (getPlayer() != null) {
            getPlayer().release();
        }
        if (mDocument != null) {
            mDocument.leave();
        }
    }

    @Override
    public void submitLotteryInfo(String id, String lottery_id, String nickname, String phone) {
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(lottery_id)) {
            VhallSDK.submitLotteryInfo(id, lottery_id, nickname, phone, new RequestCallback() {
                @Override
                public void onSuccess() {
                    watchView.showToast("信息提交成功");
                }

                @Override
                public void onError(int errorCode, String reason) {

                }
            });
        }
    }

    @Override
    public String getCurrentPixel() {
        return currentDPI;
    }

    @Override
    public int getScaleType() {
        //todo getScaleType
        return -1;
    }

//    @Override
//    public void setHeadTracker() {
//        watchView.showToast("当前活动为非VR活动，不可使用陀螺仪");
//    }

//    @Override
//    public boolean isHeadTracker() {
//        return false;
//    }

    @Override
    public void initWatch() {
        if (watchView.getActivity().isFinishing()) {
            return;
        }
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
                String customId = username;
                String customNickname = userInfo.nick_name;
                chatView.clearChatData();
                getChatHistory();
                startWatch();//initWatch成功，直接开始播放直播
            }

            @Override
            public void onError(int errorCode, String reason) {
                watchView.showToast("登录直播账号失败：" + reason);
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
        getPlayer().start(roomId, accessToken);
        liveFragment.updateViewState(MyControlView.PlayState.Playing);
        documentFragment.updateViewState(MyControlView.PlayState.Playing);
    }

    @Override
    public void stopWatch() {
        if (isWatching) {
            mPlayer.stop();
            isWatching = false;
            liveFragment.updateViewState(MyControlView.PlayState.Paused);
            documentFragment.updateViewState(MyControlView.PlayState.Paused);
        }
    }

    //签到
    @Override
    public void signIn(String signId) {
        if (!VhallSDK.isLogin()) {
            watchView.showToast("请先登录！");
            return;
        }
        VssSignManger.getInstance().signIn(signId, new CallBack() {
            @Override
            public void onSuccess(Object result) {
                watchView.showToast("签到成功");
                watchView.dismissSignIn();
            }

            @Override
            public void onError(int eventCode, String msg) {
                watchView.showToast(msg);
            }
        });
    }

    //提交问卷 需要先登录且watch已初始化完成
//    @Override
//    public void submitSurvey(String result) {
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast("请先登录！");
//            return;
//        }
//        try {
//            JSONObject obj = new JSONObject(result);
//            final String qId = obj.optString("question_id");
//            final String wId = obj.optString("data");
//            String answer = obj.getString("answer");
//            VhallSDK.submitSurveyInfo(params.webinar_id, qId, answer, new RequestCallback() {
//                @Override
//                public void onSuccess() {
//                    watchView.showToast("提交成功！");
//                    watchView.dismissSurvey();
//                    VssQuestionManger.getInstance().questionAnswer(wId, qId, "", new CallBack() {
//                        @Override
//                        public void onSuccess(Object result) {
//
//                        }
//
//                        @Override
//                        public void onError(int eventCode, String msg) {
//                            watchView.showToast(msg);
//                        }
//                    });
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
//        }
//    }

//    @Override
//    public void submitSurvey(Survey survey, String result) {
//    }

//    @Override
//    public void onRaiseHand() {
//        if (!VhallSDK.isLogin()) {
//            watchView.showToast("请先登录！");
//            return;
//        }
//        if (!isHand) {
//            VssRtcManger.getInstance(context).apply(new CallBack() {
//                @Override
//                public void onSuccess(Object result) {
//                    Log.e(TAG, "举手成功");
//                    startDownTimer(durationSec);
//                    isHand = true;
//                }
//
//                @Override
//                public void onError(int eventCode, String msg) {
//                    watchView.showToast("举手失败，errorMsg:" + msg);
//                }
//            });
//        } else {
//            VssRtcManger.getInstance(context).cancelApply(new CallBack() {
//                @Override
//                public void onSuccess(Object result) {
//                    Log.e(TAG, "取消举手成功");
//                    isHand = false;
//                    watchView.refreshHand(0);
//                    if (onHandDownTimer != null) {
//                        onHandDownTimer.cancel();
//                    }
//                }
//
//                @Override
//                public void onError(int eventCode, String msg) {
//                    watchView.showToast("取消举手失败，errorMsg:" + msg);
//                }
//            });
//        }
//    }

//    @Override
//    public void replyInvite(int type) {
//        if (type == 1) {
//            VssRtcManger.getInstance(context).agreeInvite(new CallBack() {
//                @Override
//                public void onSuccess(Object result) {
//
//                }
//
//                @Override
//                public void onError(int eventCode, String msg) {
//                    watchView.showToast("上麦状态反馈异常，errorMsg:" + msg);
//                }
//            });
//        } else {
//            VssRtcManger.getInstance(context).rejectInvite(new CallBack() {
//                @Override
//                public void onSuccess(Object result) {
//
//                }
//
//                @Override
//                public void onError(int eventCode, String msg) {
//                    watchView.showToast("上麦状态反馈异常，errorMsg:" + msg);
//                }
//            });
//        }
//    }

    /**
     * 观看过程中事件监听
     */
    private class MyListener implements VHPlayerListener {
        @Override
        public void onStateChanged(com.vhall.player.Constants.State state) {
            switch (state) {
                case START:
                    isWatching = true;
                    liveView.showLoading(false);
//                    liveView.setPlayPicture(isWatching);
                    liveFragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
                    mDocument.setDealTime(mPlayer.getRealityBufferTime() + 2500);
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
                    liveFragment.updateViewState(MyControlView.PlayState.Paused);
                    documentFragment.updateViewState(MyControlView.PlayState.Paused);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case com.vhall.player.Constants.Event.EVENT_DOWNLOAD_SPEED:
//                    liveView.setDownSpeed("速率" + msg + "/kbps");
                    break;
                case com.vhall.player.Constants.Event.EVENT_DPI_CHANGED:
                    //分辨率切换
                    Log.i(TAG, msg);
//                    onSwitchPixel(msg);
                    break;
                case com.vhall.player.Constants.Event.EVENT_DPI_LIST:
                    //支持的分辨率 msg
                    try {
                        JSONArray array = new JSONArray(msg);
                        HashMap<String, Integer> map = new HashMap<>();
                        for (int i = 0; i < array.length(); i++) {
                            String dpi = (String) array.opt(i);
                            Log.e(TAG, "DPI=" + dpi);
                            map.put(dpi, 1);
                        }
//                        liveView.showRadioButton(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case com.vhall.player.Constants.Event.EVENT_VIDEO_SIZE_CHANGED:
                    Log.i(TAG, msg);
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
                    if (!isBroadcast) {
                        watchView.showToast("还没开始直播");
                    } else {
                        watchView.showToast(msg);
                    }
                    break;
                default:
                    watchView.showToast(msg);
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

    private void getChatHistory() {
        VssChatManger.getInstance().chatLists("", "", "", new CallBack<List<ResponseChatInfo>>() {
            @Override
            public void onSuccess(List<ResponseChatInfo> result) {
                if (result == null || result.size() == 0) {
                    return;
                }
                chatView.clearChatData();
                List<MessageChatData> list1 = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    ResponseChatInfo chatInfo = result.get(i);
                    if (chatInfo == null) {
                        return;
                    }
                    list1.add(MessageChatData.getChatData(chatInfo));
                }
                Collections.reverse(list1);
                chatView.notifyDataChangedChat(ChatFragment.CHAT_EVENT_CHAT, list1);
            }

            @Override
            public void onError(int eventCode, String msg) {
                watchView.showToast(msg);
            }
        });
    }

    class MyCallback implements IVssCallBackLister {
        @Override
        public void onStateChanged(ConnectServer.State state, int i) {
            switch (state) {
                case STATE_CONNECTIONG:
                    // "连接中";
                    break;
                case STATE_DISCONNECT:
                    Log.e(TAG, "ConnectServer   i=   " + i + "   " + "连接失败");
                    break;
                case STATE_CONNECTED:
                    Log.e(TAG, "ConnectServer   i=   " + i + "   " + "连接成功");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int code, String msg) {
            Log.e(TAG, "onError    " + msg);
        }
    }

    /**
     * 观看过程消息监听
     */
    class MyLister implements IVssMessageLister {
        int parseTime(String str, int defaultTime) {
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
        public void onMessage(MessageData msg) {
            if (msg == null || TextUtils.isEmpty(msg.getType())) {
                return;
            }
            switch (msg.getType()) {
                case "live_start"://发起端开始推流
                    isBroadcast = true;
                    if (mPlayer.isPlaying()) {
                        return;
                    }
                    watchView.showToast("主播开始推流");
                    break;
                case "live_over"://发起端停止推流
                    isBroadcast = false;
                    watchView.showToast("主播停止推流");
                    if (!mPlayer.isPlaying()) {
                        return;
                    }
                    mPlayer.pause();
                    break;
                case "live_converted":
                    watchView.showToast("不允许用户上麦");
                    break;
                case "vrtc_connect_close":
                    watchView.showToast("不允许用户上麦");
                    break;
                case "vrtc_connect_open":
                    watchView.showToast("允许用户上麦");
                    break;
                case "vrtc_connect_invite":
                    //用户收到上麦邀请
//                    watchView.showInvited();
                    break;
                case "vrtc_connect_agree":
                    //用户被同意上麦
//                    watchView.enterInteractive();
//                    if (onHandDownTimer != null) {
//                        isHand = false; //重置是否举手标识
//                        onHandDownTimer.cancel();
//                        watchView.refreshHand(0);
//                    }
                    break;
                case "room_kickout":
                    watchView.showToast("您已被踢出");
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
//                    watchView.getActivity().finish();
                    break;

                case "disable":
                case "disable_all":
                    watchView.showToast("您已被禁言");
                    canSpeak = "1";
                    break;

                case "permit":
                case "permit_all":
                    watchView.showToast("您已被取消禁言");
                    canSpeak = "0";
                    break;

                case "Join":
                    JSONObject joinObject = (JSONObject) msg.getT();
                    MessageChatData join = new MessageChatData();
                    join.event = MessageChatData.eventOnlineKey;
                    join.setTime(joinObject.optString("time"));
                    join.setNickname(joinObject.optString("name"));
                    join.setAvatar(joinObject.optString("avatar"));
                    chatView.notifyDataChangedChat(join);
                    break;
                case "Leave":
                    JSONObject leaveObject = (JSONObject) msg.getT();
                    MessageChatData leave = new MessageChatData();
                    leave.setTime(leaveObject.optString("time"));
                    leave.setNickname(leaveObject.optString("name"));
                    leave.setAvatar(leaveObject.optString("avatar"));
                    leave.event = MessageChatData.eventOfflineKey;
                    chatView.notifyDataChangedChat(leave);
                    break;
                case "question_answer_open":
                    watchView.showToast("问答开始");
                    break;

                case "question_answer_close":
                    watchView.showToast("问答结束");
                    break;
                case "question_answer_create":
                case "question_answer_commit":
                    VssMessageQuestionData vssMessageQuestionData = (VssMessageQuestionData) msg.getT();
                    if (vssMessageQuestionData != null) {
                        ChatServer.ChatInfo chatInfo = new ChatServer.ChatInfo();
                        ChatServer.ChatInfo.QuestionData questionData = new ChatServer.ChatInfo.QuestionData();
                        questionData.avatar = vssMessageQuestionData.getAvatar();
                        questionData.content = vssMessageQuestionData.getContent();
                        questionData.created_at = vssMessageQuestionData.getCreated_at();
                        questionData.id = String.valueOf(vssMessageQuestionData.getId());
                        questionData.join_id = String.valueOf(vssMessageQuestionData.getJoin_id());
                        questionData.nick_name = vssMessageQuestionData.getNick_name();
                        questionData.type = vssMessageQuestionData.getType();
                        VssMessageQuestionData.AnswerBean answer = vssMessageQuestionData.getAnswer();
                        if (answer != null) {
                            ChatServer.ChatInfo.QuestionData answerData = new ChatServer.ChatInfo.QuestionData();
                            answerData.avatar = answer.getAvatar();
                            answerData.content = answer.getContent();
                            answerData.created_at = answer.getCreated_at();
                            answerData.id = String.valueOf(answer.getId());
                            answerData.join_id = String.valueOf(answer.getJoin_id());
                            answerData.nick_name = answer.getNick_name();
                            answerData.is_open = Integer.parseInt(answer.getIs_open());
                            questionData.answer = answerData;
                        }
                        chatInfo.questionData = questionData;
//                        questionView.notifyDataChangedQe(chatInfo);
                    }
                    break;

                case "sign_in_push":
                    //提交签到
                    VssMessageSignData vssMessageSignData = (VssMessageSignData) msg.getT();
                    watchView.showSignIn(vssMessageSignData.getSign_id(), parseTime(vssMessageSignData.getSign_show_time(), 30));
                    break;

                case "room_announcement":
                    //公告
                    VssMessageAnnouncementData announcementData = (VssMessageAnnouncementData) msg.getT();
                    watchView.showNotice(announcementData.getRoom_announcement_text());
                    break;

                case "lottery_push":
                    //抽奖 开始
//                    VssMessageLotteryData vssMessageLotteryData = (VssMessageLotteryData) msg.getT();
//                    if (vssMessageLotteryData != null) {
//                        MessageLotteryData data = new MessageLotteryData(
//                                vssMessageLotteryData.getType(),
//                                vssMessageLotteryData.getRoom_id(),
//                                vssMessageLotteryData.getLottery_id(),
//                                vssMessageLotteryData.getLottery_creator_id(),
//                                vssMessageLotteryData.getLottery_creator_avatar(),
//                                vssMessageLotteryData.getLottery_creator_nickname(),
//                                vssMessageLotteryData.getLottery_type(),
//                                vssMessageLotteryData.getLottery_number(),
//                                MessageLotteryData.EVENT_START_LOTTERY,
//                                vssMessageLotteryData.getLottery_status());
//                        watchView.showLottery(data);
//                    }
                    break;

                case "lottery_result_notice":
                    //抽奖 结束
//                    VssMessageLotteryData vssMessageLotteryData1 = (VssMessageLotteryData) msg.getT();
//                    if (vssMessageLotteryData1 != null) {
//                        MessageLotteryData data1 = new MessageLotteryData(
//                                vssMessageLotteryData1.getType(),
//                                vssMessageLotteryData1.getRoom_id(),
//                                vssMessageLotteryData1.getLottery_id(),
//                                vssMessageLotteryData1.getLottery_creator_id(),
//                                vssMessageLotteryData1.getLottery_creator_avatar(),
//                                vssMessageLotteryData1.getLottery_creator_nickname(),
//                                vssMessageLotteryData1.getLottery_type(),
//                                vssMessageLotteryData1.getLottery_number(),
//                                MessageLotteryData.EVENT_END_LOTTERY,
//                                vssMessageLotteryData1.getLottery_status());
//                        List<VssMessageLotteryData.LotteryWinnersBean> lottery_winners = vssMessageLotteryData1.getLottery_winners();
//                        if (lottery_winners != null && lottery_winners.size() > 0) {
//                            List<MessageLotteryData.LotteryWinnersBean> winnersBeans = new ArrayList<>();
//                            for (VssMessageLotteryData.LotteryWinnersBean lottery_winner : lottery_winners) {
//                                MessageLotteryData.LotteryWinnersBean bean = new MessageLotteryData.LotteryWinnersBean(lottery_winner.getId(),
//                                        lottery_winner.getLottery_id(),
//                                        lottery_winner.getLottery_idX(),
//                                        lottery_winner.getLottery_user_id(),
//                                        lottery_winner.getLottery_user_nickname(),
//                                        lottery_winner.getLottery_user_avatar(),
//                                        lottery_winner.getPreset(),
//                                        lottery_winner.isSelf());
//                                winnersBeans.add(bean);
//                            }
//                            data1.setLottery_winners(winnersBeans);
//                        }
//                        watchView.showLottery(data1);
//                    }
                    break;
                case "service_im":
                case "service_custom":
                    //聊天消息
                    VssMessageChatData messageChatData = (VssMessageChatData) msg.getT();
                    MessageChatData data = new MessageChatData();
                    if (messageChatData != null) {
                        data.setType(messageChatData.getType());
                        data.setAvatar(messageChatData.getAvatar());
                        data.setNickname(messageChatData.getNickname());
                        data.setMy(messageChatData.isMy());
                        data.setUserId(messageChatData.getUserId());
                        data.setTime(messageChatData.getTime());
                        data.event = messageChatData.event;
                        data.setRoom_id(messageChatData.getRoom_id());
                        data.setText_content(messageChatData.getText_content());
                        data.setImage_urls(messageChatData.getImage_urls());
                        data.setImage_url(messageChatData.getImage_url());
                        chatView.notifyDataChangedChat(data);
//                        liveView.addDanmu(messageChatData.getText_content());
                    }
                    break;
                case "questionnaire_push":
//                    Log.e(TAG, "onMessage: " + msg);
//                    JSONObject obj = (JSONObject) msg.getT();
//                    MessageChatData surveyData = new MessageChatData();
//                    surveyData.event = MessageChatData.eventSurveyKey;
//                    surveyData.setUrl(VhallSDK.getSurveyUrl(obj.optString("questionnaire_id"), params.webinar_id, ""));
//                    surveyData.setId(obj.optString("questionnaire_id"));
//                    chatView.notifyDataChangedChat(surveyData);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int code, String msg) {
            Log.e(TAG, msg);
        }
    }


    public void setParams(Param params) {
        this.params = params;
    }

}

