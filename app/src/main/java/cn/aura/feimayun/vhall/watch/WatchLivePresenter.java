package cn.aura.feimayun.vhall.watch;

import android.os.Build;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.Watch;
import com.vhall.business.WatchLive;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.Survey;
import com.vhall.business.data.UserInfo;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.SurveyDataSource;
import com.vhall.business.data.source.UserInfoDataSource;
import com.vhall.vhalllive.common.Constants;
import com.vhall.vhalllive.playlive.GLPlayInterface;

import org.json.JSONObject;

import java.util.List;

import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.vhall.Param;
import cn.aura.feimayun.vhall.chat.ChatContract;
import cn.aura.feimayun.vhall.chat.ChatFragment;
import cn.aura.feimayun.vhall.util.emoji.InputUser;

/**
 * 观看直播的Presenter HAVE DONE
 */
public class WatchLivePresenter implements WatchContract.LivePresenter, ChatContract.ChatPresenter {
    private static final String TAG = "WatchLivePresenter";
    public boolean isWatching = false;
    DocumentFragment documentFragment;
    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
    int[] scaleTypes = new int[]{Constants.DrawMode.kVHallDrawModeAspectFit.getValue(), Constants.DrawMode.kVHallDrawModeAspectFill.getValue(), Constants.DrawMode.kVHallDrawModeNone.getValue()};
    int currentPos = 0;
    //    ChatContract.ChatView questionView;
    CountDownTimer onHandDownTimer;
    boolean force = false;
    private Param params;
    private WatchContract.LiveView liveView;
    private WatchLive watchLive;
    private int scaleType = Constants.DrawMode.kVHallDrawModeAspectFit.getValue();
    private GLPlayInterface mPlayView;
    private boolean isHand = false;
    private int isHandStatus = 1;
    private int durationSec = 30; // 举手上麦倒计时

    WatchLivePresenter(WatchContract.LiveView liveView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, WatchContract.WatchView watchView, Param param, DocumentFragment fragment) {
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
    }

    public void setParams(Param params) {
        this.params = params;
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
                Log.e(TAG, " reason == " + reason);
//                chatView.showToast(reason);
            }
        });
    }

    @Override
    public void sendCustom(JSONObject text) {//自定义聊天信息
        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        getWatchLive().sendCustom(text, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String reason) {
//                chatView.showToast(reason);
            }
        });
    }

    @Override
    public void sendQuestion(String content) {
        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        getWatchLive().sendQuestion(content, new RequestCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String reason) {
//                questionView.showToast(reason);
            }
        });
    }

    @Override
    public void onLoginReturn() {
        initWatch();
    }

    @Override
    public void onFreshData() {
    }

    @Override
    public void showSurvey(String surveyid) {
        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        VhallSDK.getSurveyInfo(surveyid, new SurveyDataSource.SurveyInfoCallback() {
            @Override
            public void onSuccess(Survey survey) {
                watchView.showSurvey(survey);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
//                chatView.showToast(errorMsg);
            }
        });
    }

    @Override
    public void onSwitchPixel(int level) {
        if (getWatchLive().getDefinition() == level && !force) {
            return;
        }
        force = false;
        getWatchLive().setPCSwitchDefinition();
        if (watchView.getActivity().isFinishing()) {
            return;
        }
    }

    @Override
    public void onMobileSwitchRes(int res) {
        if (getWatchLive().getDefinition() == res && !force) {
            return;
        }
        if (isWatching) {
            stopWatch();
        }
        force = false;
        getWatchLive().setDefinition(res);
    }


    @Override
    public int setScaleType() {
        int scaleType = scaleTypes[(++currentPos) % scaleTypes.length];
        getWatchLive().setScaleType(scaleType);
        liveView.setScaleButtonText(scaleType);
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
    public int getCurrentPixel() {
        return getWatchLive().getDefinition();
    }

    @Override
    public int getScaleType() {
        if (getWatchLive() != null) {
            return getWatchLive().getScaleType();
        }
        return -1;
    }

    @Override
    public void setHeadTracker() {
        if (!getWatchLive().isVR()) {
//            watchView.showToast("当前活动为非VR活动，不可使用陀螺仪");
            return;
        }
        getWatchLive().setVRHeadTracker(!getWatchLive().isVRHeadTracker());
        liveView.reFreshView();
    }

    @Override
    public boolean isHeadTracker() {
        return getWatchLive().isVRHeadTracker();
    }

    @Override
    public void initWatch() {
        if (!VhallSDK.isLogin()) {
            //登录聊天账号
            String uid = Util.getUid();
            //登录微吼账号，用于聊天
            String username = "wxh" + uid;
            String userpass = "1q2w3e4r5t6y7u8i9o";

            VhallSDK.login(username, userpass, new UserInfoDataSource.UserInfoCallback() {

                @Override
                public void onSuccess(UserInfo userInfo) {
//                    Toast.makeText(MyApplication.context, "登录聊天服务器成功watch", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int errorCode, String reason) {
//                    Toast.makeText(MyApplication.context, "登录聊天服务器失败watch", Toast.LENGTH_SHORT).show();
                }

            });
        }

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
                liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
                chatView.clearChatData();
                getChatHistory();
//                getAnswerList();
                startWatch();//initWatch成功，直接开始播放直播
            }

            @Override
            public void onError(int errorCode, String msg) {
//                watchView.showToast(msg);
            }

        });

    }

    private void getAnswerList() {
        VhallSDK.getAnswerList(params.watchId, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
//                questionView.notifyDataChanged(ChatFragment.CHAT_EVENT_QUESTION, list);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
//                Toast.makeText(watchView.getActivity(), messaage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void startWatch() {
        getWatchLive().start();
    }

    @Override
    public void stopWatch() {
        if (isWatching) {
            getWatchLive().stop();
            isWatching = false;
            liveView.setPlayPicture(isWatching);
            documentFragment.setPlayIcon(!isWatching);
        }
    }

    private WatchLive getWatchLive() {
        if (watchLive == null) {
            WatchLive.Builder builder = new WatchLive.Builder()
                    .context(watchView.getActivity().getApplicationContext())
                    .containerLayout(liveView.getWatchLayout())
                    .bufferDelay(params.bufferSecond)
                    .callback(new WatchCallback())
                    .messageCallback(new MessageEventCallback())
                    .connectTimeoutMils(5000)
                    .chatCallback(new ChatCallback());
            watchLive = builder.build();
        }
        return watchLive;
    }

    //签到
    @Override
    public void signIn(String signId) {
        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        VhallSDK.performSignIn(params.watchId, signId, new RequestCallback() {
            @Override
            public void onSuccess() {
//                watchView.showToast("签到成功");
                watchView.dismissSignIn();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
//                watchView.showToast(errorMsg);
            }
        });
    }

    //提交问卷 需要先登录且watch已初始化完成
    @Override
    public void submitSurvey(Survey survey, String result) {
        if (survey == null)
            return;
        if (!VhallSDK.isLogin()) {
//            watchView.showToast(R.string.vhall_login_first);
            return;
        }
        VhallSDK.submitSurveyInfo(getWatchLive(), survey.surveyid, result, new RequestCallback() {
            @Override
            public void onSuccess() {
//                watchView.showToast("提交成功！");
                watchView.dismissSurvey();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
//                watchView.showToast(errorMsg);
                if (errorCode == 10821)
                    watchView.dismissSurvey();
            }
        });
    }

    @Override
    public void onRaiseHand() {
        getWatchLive().onRaiseHand(params.watchId, isHand ? 0 : 1, new RequestCallback() {
            @Override
            public void onSuccess() {
                if (isHand) {
                    isHand = false;
                    watchView.refreshHand(0);
                    if (onHandDownTimer != null) {
                        onHandDownTimer.cancel();
                    }
                } else {
//                    Log.e(TAG, "举手成功");
                    startDownTimer(durationSec);
                    isHand = true;
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
//                watchView.showToast("举手失败，errorMsg:" + errorMsg);
            }
        });
    }

    private void startDownTimer(int secondTimer) {
        onHandDownTimer = new CountDownTimer(secondTimer * 1000 + 1080, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                watchView.refreshHand((int) millisUntilFinished / 1000 - 1);
            }

            @Override
            public void onFinish() {
                onHandDownTimer.cancel();
                onRaiseHand();
            }
        }.start();
    }

    private void getChatHistory() {
        getWatchLive().acquireChatRecord(true, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, list);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
//                Log.e(TAG, "onFailed->" + errorcode + ":" + messaage);
            }
        });
    }

    /**
     * 观看过程中事件监听
     */
    private class WatchCallback implements Watch.WatchEventCallback {
        @Override
        public void onError(int errorCode, String errorMsg) {
            switch (errorCode) {
                case WatchLive.ERROR_CONNECT:
                    Log.e(TAG, "连接失败");
                    isWatching = false;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
                    documentFragment.setPlayIcon(!isWatching);
//                    watchView.showToast(errorMsg);
                    break;
                default:
//                    watchView.showToast(errorMsg);
            }
        }

        @Override
        public void onStateChanged(int stateCode) {
            switch (stateCode) {
                case WatchLive.STATE_CONNECTED:
                    Log.e(TAG, "STATE_CONNECTED  ");
                    isWatching = true;
                    liveView.setPlayPicture(isWatching);
                    documentFragment.setPlayIcon(!isWatching);
                    break;
                case WatchLive.STATE_BUFFER_START:
                    Log.e(TAG, "STATE_BUFFER_START  ");
                    if (isWatching)
                        liveView.showLoading(true);
                    break;
                case WatchLive.STATE_BUFFER_STOP:
                    Log.e(TAG, "STATE_BUFFER_STOP  ");
                    liveView.showLoading(false);
                    break;
                case WatchLive.STATE_STOP:
                    Log.e(TAG, "STATE_STOP  ");
                    isWatching = false;
                    liveView.showLoading(false);
                    liveView.setPlayPicture(isWatching);
                    documentFragment.setPlayIcon(!isWatching);
                    break;
            }
        }

        @Override
        public void onVhallPlayerStatue(boolean playWhenReady, int playbackState) {
            // 播放器状态回调  只在看回放时使用
        }

        @Override
        public void uploadSpeed(String kbps) {
            liveView.setDownSpeed("速率" + kbps + "/kbps");
        }

        @Override
        public void videoInfo(int width, int height) {
            if (mPlayView != null) {
                mPlayView.init(width, height);
                // INIT STUF
            }
        }
    }

    /**
     * 观看过程消息监听
     */
    private class MessageEventCallback implements MessageServer.Callback {
        @Override
        public void onEvent(MessageServer.MsgInfo messageInfo) {
//            Log.e(TAG, "messageInfo " + messageInfo.event);
            switch (messageInfo.event) {
                case MessageServer.EVENT_DISABLE_CHAT://禁言
                    break;
                case MessageServer.EVENT_KICKOUT://踢出
                    break;
                case MessageServer.EVENT_OVER://直播结束
//                    watchView.showToast("直播已结束");
                    Toast.makeText(watchView.getActivity(), "直播已结束", Toast.LENGTH_SHORT).show();
                    stopWatch();
                    break;
                case MessageServer.EVENT_PERMIT_CHAT://解除禁言
                    break;
                case MessageServer.EVENT_DIFINITION_CHANGED:
//                    Log.e(TAG, "EVENT_DIFINITION_CHANGED PC 端切换分辨率");
                    liveView.showRadioButton(getWatchLive().getDefinitionAvailable());
                    onSwitchPixel(WatchLive.DPI_DEFAULT);
//                    if (!getWatchLive().isDifinitionAvailable(getWatchLive().getDefinition())) {
//                        onSwitchPixel(WatchLive.DPI_DEFAULT);
//                    }
                    break;
                case MessageServer.EVENT_START_LOTTERY://抽奖开始
                    watchView.showLottery(messageInfo);
                    break;
                case MessageServer.EVENT_END_LOTTERY://抽奖结束
                    watchView.showLottery(messageInfo);
                    break;
                case MessageServer.EVENT_NOTICE:
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
                    ChatServer.ChatInfo chatInfo = new ChatServer.ChatInfo();
                    chatInfo.event = "survey";
                    chatInfo.id = messageInfo.id;
                    chatView.notifyDataChanged(chatInfo);
                    break;
                case MessageServer.EVENT_CLEARBOARD:
                case MessageServer.EVENT_DELETEBOARD:
                case MessageServer.EVENT_INITBOARD:
                case MessageServer.EVENT_PAINTBOARD:
                case MessageServer.EVENT_SHOWBOARD:
                    documentView.paintBoard(messageInfo);
                    break;
                case MessageServer.EVENT_CHANGEDOC://PPT翻页消息
                case MessageServer.EVENT_CLEARDOC:
                case MessageServer.EVENT_PAINTDOC:
                case MessageServer.EVENT_DELETEDOC:
                    Log.e(TAG, " event " + messageInfo.event);
                    documentView.paintPPT(messageInfo);
                    break;
                case MessageServer.EVENT_RESTART:
                    force = true;
                    //onSwitchPixel(WatchLive.DPI_DEFAULT);
                    break;
                case MessageServer.EVENT_INTERACTIVE_HAND:
                    Log.e(TAG, " status " + messageInfo.status);
                    /** 互动举手消息 status = 1  允许上麦  */
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_MIC:
//                    getWatchLive().disconnectMsgServer(); // 关闭watchLive中的消息
                    watchView.enterInteractive();
                    if (onHandDownTimer != null) {
                        isHand = false; //重置是否举手标识
                        onHandDownTimer.cancel();
                        watchView.refreshHand(0);
                    }
                    break;
                case MessageServer.EVENT_INTERACTIVE_ALLOW_HAND:
//                    watchView.showToast(messageInfo.status == 0 ? "举手按钮关闭" : "举手按钮开启");
                    break;
            }
        }

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

    private class ChatCallback implements ChatServer.Callback {
        @Override
        public void onChatServerConnected() {
            Log.i("tianyanyuzhibo", "连接成功：");
        }

        @Override
        public void onConnectFailed() {
//            getWatchLive().connectChatServer();
            Log.i("tianyanyuzhibo", "连接失败：");
        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
            Log.i("tianyanyuzhibo", "收到新消息啦：" + chatInfo.msgData);
            switch (chatInfo.event) {
                case ChatServer.eventMsgKey:
                    chatView.notifyDataChanged(chatInfo);
//                    liveView.addDanmu(chatInfo.msgData.text);
                    break;
                case ChatServer.eventCustomKey:
//                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventOnlineKey:
//                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventOfflineKey:
//                    chatView.notifyDataChanged(chatInfo);
                    break;
                case ChatServer.eventQuestion:
//                    questionView.notifyDataChanged(chatInfo);
                    break;
            }
        }

        @Override
        public void onChatServerClosed() {
        }
    }
}

