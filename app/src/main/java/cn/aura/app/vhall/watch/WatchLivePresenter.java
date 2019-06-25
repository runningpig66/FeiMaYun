package cn.aura.app.vhall.watch;

import android.os.Build;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.Watch;
import com.vhall.business.WatchLive;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.UserInfo;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.data.source.UserInfoDataSource;
import com.vhall.vhalllive.common.Constants;
import com.vhall.vhalllive.playlive.GLPlayInterface;

import java.util.List;
import java.util.Objects;

import cn.aura.app.util.ScreenUtils;
import cn.aura.app.util.Util;
import cn.aura.app.vhall.Param;
import cn.aura.app.vhall.chat.ChatContract;
import cn.aura.app.vhall.chat.ChatFragment;
import cn.aura.app.vhall.util.emoji.InputUser;
import cn.aura.app.view.MyControlView;
import cn.aura.app.view.SelfDialog2;

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
    private WatchLiveFragment liveFragment;
    private String vhall_account = "1";

    WatchLivePresenter(WatchContract.LiveView liveView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, WatchContract.WatchView watchView, Param param, DocumentFragment fragment, String vhall_account) {
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

    public void setParams(Param params) {
        this.params = params;
    }

    @Override
    public void start() {
        getWatchLive().setVRHeadTracker(true);
        getWatchLive().setScaleType(Constants.DrawMode.kVHallDrawModeAspectFit.getValue());
        Log.i("061201", "start()");
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
                Log.i("061201", "onWatchBtnClick()");
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
                chatView.showToast(reason);
            }
        });
    }

    @Override
    public void onLoginReturn() {
        Log.i("061201", "onLoginReturn()");
        initWatch();
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
    public void initWatch() {
        Log.i("061201", "initWatch()");
        //登录聊天账号
        String uid = Util.getUid();
        //登录微吼账号，用于聊天
        String username = vhall_account.equals("1") ? "wxh" + uid : "sch" + uid;
        String userpass = "1q2w3e4r5t6y7u8i9o";
//        Log.i("061005", username + "      " + userpass);
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

    private void getChatHistory() {
        getWatchLive().acquireChatRecord(true, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, list);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {
                Log.e(TAG, "onFailed->" + errorcode + ":" + messaage);
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
                    Log.e(TAG, "ERROR_CONNECT  ");
                    isWatching = false;
                    liveView.showLoading(false);
                    liveFragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
                    Toast.makeText(watchView.getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
//                    liveView.setPlayPicture(isWatching);
//                    documentFragment.setPlayIcon(!isWatching);
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
                    liveFragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
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
                    liveFragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
                    break;
            }
        }

        @Override
        public void onVhallPlayerStatue(boolean playWhenReady, int playbackState) {
            // 播放器状态回调  只在看回放时使用
        }

        @Override
        public void uploadSpeed(String kbps) {
//            liveView.setDownSpeed("速率" + kbps + "/kbps");
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
                    onSwitchPixel(WatchLive.DPI_DEFAULT);
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
                case MessageServer.EVENT_NOTICE:
//                    watchView.showNotice(messageInfo.content);
                    break;
                case MessageServer.EVENT_SIGNIN: //签到消息
//                    if (!TextUtils.isEmpty(messageInfo.id) && !TextUtils.isEmpty(messageInfo.sign_show_time)) {
//                        watchView.showSignIn(messageInfo.id, parseTime(messageInfo.sign_show_time, 30));
//                    }
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
            }
        }

//        int parseTime(String str, int defaultTime) {
//            int currentTime = 0;
//            try {
//                currentTime = Integer.parseInt(str);
//                if (currentTime == 0) {
//                    return defaultTime;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return currentTime;
//        }

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
        }

        @Override
        public void onConnectFailed() {
            getWatchLive().connectChatServer();
        }

        @Override
        public void onChatMessageReceived(ChatServer.ChatInfo chatInfo) {
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

