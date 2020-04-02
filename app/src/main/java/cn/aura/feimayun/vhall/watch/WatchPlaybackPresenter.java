package cn.aura.feimayun.vhall.watch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.WatchPlayback;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.player.Constants;
import com.vhall.player.VHPlayerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.aura.feimayun.R;
import cn.aura.feimayun.vhall.Param;
import cn.aura.feimayun.vhall.chat.ChatContract;
import cn.aura.feimayun.vhall.chat.ChatFragment;
import cn.aura.feimayun.vhall.chat.MessageChatData;
import cn.aura.feimayun.vhall.util.VhallUtil;
import cn.aura.feimayun.vhall.util.emoji.InputUser;
import cn.aura.feimayun.view.MyControlView;

import static com.vhall.business.MessageServer.EVENT_SHOWBOARD;
import static com.vhall.business.MessageServer.EVENT_SHOWDOC;
import static com.vhall.business.WatchPlayback.SHOW_DOC_KEY;

//TODO  投屏相关
//import com.vhall.business_support.Watch_Support;
//import com.vhall.business_support.dlna.DeviceDisplay;
//import com.vhall.business_support.WatchLive;
//import com.vhall.business_support.WatchPlayback;
//import org.fourthline.cling.android.AndroidUpnpService;

/**
 * 观看回放的Presenter
 */
public class WatchPlaybackPresenter implements
        WatchContract.PlaybackPresenter,
        ChatContract.ChatPresenter {
    private static final String TAG = "PlaybackPresenter";
    private Param param;
    WatchContract.PlaybackView playbackView;
    WatchContract.DocumentView documentView;
    WatchContract.WatchView watchView;
    ChatContract.ChatView chatView;
    private WatchPlayback watchPlayback;

    //FIT_XY = 0;FIT = 1;FILL= 2;
    int[] scaleTypeList = new int[]{0, 1, 2};
    int currentPos = 0;
    private int scaleType = 1;//FIT_XY

    String[] speedStrs = new String[]{"0.25", "0.50", "1.00", "1.25", "1.50", "2.00"};
    int currentSpeed = 2;

    private int limit = 5;
    private int pos = 0;

    private long playerCurrentPosition = 0L; // 当前的进度
    private long playerDuration;
    private String playerDurationTimeStr = "00:00:00";

    private boolean loadingVideo = false;
    private boolean loadingComment = false;

    private Timer timer;

    WatchPlaybackFragment fragment;
    DocumentFragment documentFragment;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0: // 每秒更新SeekBar
                    if (getWatchPlayback().isPlaying()) {
                        documentFragment.playerCurrentPosition = getWatchPlayback().getCurrentPosition();
                        playerCurrentPosition = getWatchPlayback().getCurrentPosition();
                        documentFragment.setSeekbarCurrentPosition((int) documentFragment.playerCurrentPosition);
                        playbackView.setSeekbarCurrentPosition((int) playerCurrentPosition);
                        //                String playerCurrentPositionStr = VhallUtil.converLongTimeToStr(playerCurrentPosition);
                        //                //playbackView.setProgressLabel(playerCurrentPositionStr + "/" + playerDurationTimeStr);
                        //                playbackView.setProgressLabel(playerCurrentPositionStr, playerDurationTimeStr);
                    }
                    break;
            }
        }
    };

    WatchPlaybackPresenter(WatchContract.PlaybackView playbackView,
                           WatchContract.DocumentView documentView,
                           ChatContract.ChatView chatView,
                           WatchContract.WatchView watchView,
                           Param param,
                           DocumentFragment documentFragment) {
        this.playbackView = playbackView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.chatView = chatView;
        this.param = param;
        this.playbackView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.watchView.setPresenter(this);
        this.documentFragment = documentFragment;
        fragment = (WatchPlaybackFragment) playbackView;
    }

    public void setParam(Param param) {
        this.param = param;
    }

    @Override
    public void start() {
//        playbackView.setScaleTypeText(scaleType);
        getWatchPlayback().setScaleType(scaleType);
        initWatch();
    }

    private void initCommentData(int pos) {
        if (loadingComment)
            return;
        loadingComment = true;
        watchPlayback.requestCommentHistory(param.watchId, limit, pos, new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                chatView.clearChatData();
                loadingComment = false;
                List<MessageChatData> list1 = new ArrayList<>();
                for (ChatServer.ChatInfo chatInfo : list) {
                    list1.add(MessageChatData.getChatData(chatInfo));
                }
                chatView.notifyDataChangedChat(ChatFragment.CHAT_EVENT_CHAT, list1);
            }

            @Override
            public void onFailed(int errorcode, String message) {
                loadingComment = false;
                Log.d(TAG, "onFailed: errorcode: " + errorcode + ", message: " + message);
//                watchView.showToast(message);
            }
        });
    }

    void initWatch() {
        if (loadingVideo) return;
        loadingVideo = true;
        //游客ID及昵称 已登录用户可传空
        TelephonyManager telephonyMgr = (TelephonyManager) watchView.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String customeId = telephonyMgr.getDeviceId();
        if (customeId == null) {
            customeId = String.valueOf(System.currentTimeMillis());
        }
        String customNickname = Build.BRAND + "手机用户";
        VhallSDK.initWatch(param.watchId, customeId, customNickname, param.key, getWatchPlayback(), WebinarInfo.VIDEO, new RequestCallback() {
            @Override
            public void onSuccess() {
                loadingVideo = false;
                handlePosition();
                pos = 0;
                initCommentData(pos);
//                watchView.showNotice(getWatchPlayback().getNotice()); //显示公告
//                playbackView.setQuality(getWatchPlayback().getQualities());
                fragment.handleAutoPlay.obtainMessage().sendToTarget();
                operationDocument();
            }

            @Override
            public void onError(int errorCode, String reason) {
                loadingVideo = false;
                if (errorCode == 20003) {//error param!

                } else {
                    Toast.makeText(watchView.getActivity(), reason, Toast.LENGTH_SHORT).show();
                }
//                watchView.showToast(reason);
            }
        });
    }

    @Override
    public long getDurationCustom() {
        return getWatchPlayback().getDuration();
    }

    @Override
    public long getCurrentPositionCustom() {
        return getWatchPlayback().getCurrentPosition();
    }

    @Override
    public void onFragmentDestory() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        getWatchPlayback().destory();
    }

    @Override
    public void startPlay() {
        if (!getWatchPlayback().isAvaliable())
            return;
        fragment.updateViewState(MyControlView.PlayState.Playing);
        documentFragment.updateViewState(MyControlView.PlayState.Playing);
//        playbackView.setPlayIcon(false);
        getWatchPlayback().start();
    }

    @Override
    public void onPlayClick() {
        if (getWatchPlayback().isPlaying()) {
            onStop();
        } else {
            if (!getWatchPlayback().isAvaliable()) {
                initWatch();
            } else {
                if (getWatchPlayback().getPlayerState() == Constants.State.END) {
                    getWatchPlayback().seekTo(0);
                }
                startPlay();
            }
        }
    }

//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        playbackView.setProgressLabel(VhallUtil.converLongTimeToStr(progress), playerDurationTimeStr);
//    }

    @Override
    public void onStopTrackingTouch(int playerCurrentPosition) {
        if (!getWatchPlayback().isPlaying()) {
            startPlay();
        }
        getWatchPlayback().seekTo(playerCurrentPosition);
    }

    @Override
    public int changeScaleType() {
        scaleType = scaleTypeList[(++currentPos) % scaleTypeList.length];
        getWatchPlayback().setScaleType(scaleType);
//        playbackView.setScaleTypeText(scaleType);
        return scaleType;
    }

    @Override
    public int changeScreenOri() {
        return watchView.changeOrientation();
    }

    @Override
    public void onResume() {
        getWatchPlayback().onResume();
        if (getWatchPlayback().isAvaliable()) {
            fragment.updateViewState(MyControlView.PlayState.Playing);
            documentFragment.updateViewState(MyControlView.PlayState.Playing);
        } else {
            fragment.updateViewState(MyControlView.PlayState.Paused);
            documentFragment.updateViewState(MyControlView.PlayState.Paused);
        }
//        if (getWatchPlayback().isAvaliable()) {
//            playbackView.setPlayIcon(false);
//        } else {
//            playbackView.setPlayIcon(true);
//        }
    }


    @Override
    public void onPause() {
        //onPause只需要根据Activity的生命周期调用即可,暂停可以使用stop方法
        getWatchPlayback().onPause();
//        playbackView.setPlayIcon(true);
        fragment.updateViewState(MyControlView.PlayState.Paused);
        documentFragment.updateViewState(MyControlView.PlayState.Paused);
    }

    @Override
    public void onStop() {
        getWatchPlayback().stop();
//        playbackView.setPlayIcon(true);
        fragment.updateViewState(MyControlView.PlayState.Paused);
        documentFragment.updateViewState(MyControlView.PlayState.Paused);
    }

    @Override
    public void onSwitchPixel(String pix) {
        getWatchPlayback().setDefinition(pix);
    }

    @Override
    public void setSpeed() {
//        String speed = speedStrs[(++currentSpeed) % speedStrs.length];
//        if (getWatchPlayback().setSpeed(Float.parseFloat(speed)) == 0) {
//            speed = speedStrs[(--currentSpeed) % speedStrs.length];
//        }
//        playbackView.setPlaySpeedText(speed);
    }

    public WatchPlayback getWatchPlayback() {
        if (watchPlayback == null) {
            WatchPlayback.Builder builder = new WatchPlayback.Builder()
                    .context(watchView.getActivity())
//                    .containerLayout(playbackView.getContainer())
//                    .surfaceView(playbackView.getVideoView())
                    .vodPlayView(playbackView.getVideoView())
                    .callback(new WatchCallback())
                    .docCallback(new DocCallback());
            watchPlayback = builder.build();
        }
        return watchPlayback;
    }

    @Override
    public void signIn(String signId) {

    }

//    @Override
//    public void submitSurvey(String result) {

//    }

//    @Override
//    public void submitSurvey(Survey survey, String result) {

//    }

//    @Override
//    public void onRaiseHand() {

//    }

//    @Override
//    public void replyInvite(int type) {

//    }

    //TODO 投屏相关
//    @Override
//    public void dlnaPost(DeviceDisplay deviceDisplay, AndroidUpnpService service) {
//        getWatchPlayback().dlnaPost(deviceDisplay, service, new Watch_Support.DLNACallback() {
//            @Override
//            public void onError(int errorCode) {
//                watchView.showToast("投屏失败，errorCode:" + errorCode);
//            }
//
//            @Override
//            public void onSuccess() {
//                watchView.showToast("投屏成功!");
//            }
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
     * 根据文档状态选择展示
     */
    private void operationDocument() {
        if (!getWatchPlayback().isUseDoc()) {
            documentView.showType(2);//关闭文档
        } else {
            //展示文档
            if (getWatchPlayback().isUseBoard()) {
                //当前为白板
                documentView.showType(1);
            } else {
                documentView.showType(0);
            }
        }
    }

    private class DocCallback implements WatchPlayback.DocumentEventCallback {

        @Override
        public void onEvent(String key, List<MessageServer.MsgInfo> msgInfos) {
            if (SHOW_DOC_KEY.equals(key)) {
                if (msgInfos.size() > 0) {
                    getWatchPlayback().setIsUseDoc(msgInfos.get(msgInfos.size() - 1).watchType);
                    operationDocument();
                }
            } else {
                if (msgInfos != null && msgInfos.size() > 0) {
                    documentView.paintBoard(key, msgInfos);
                    documentView.paintPPT(key, msgInfos);
                }
            }
        }

        @Override
        public void onEvent(MessageServer.MsgInfo msgInfo) {
            if (msgInfo.event == EVENT_SHOWDOC) {
                getWatchPlayback().setIsUseDoc(msgInfo.watchType);
                operationDocument();
            } else {
                if (msgInfo.event == EVENT_SHOWBOARD) {
                    getWatchPlayback().setIsUseBoard(msgInfo.showType);
                }
                if (getWatchPlayback().isUseDoc()) {
                    documentView.paintBoard(msgInfo);
                }
                documentView.paintPPT(msgInfo);
            }
        }
    }

    private class WatchCallback implements VHPlayerListener {

        @Override
        public void onStateChanged(Constants.State state) {
            switch (state) {
                case IDLE:
//                    Log.e(TAG, "STATE_IDLE");
                    break;
                case START:
                    playbackView.showProgressbar(false);
//                    playbackView.setPlayIcon(false);
                    fragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
                    playerDuration = getWatchPlayback().getDuration();
                    playerDurationTimeStr = VhallUtil.converLongTimeToStr(playerDuration);
                    playbackView.setSeekbarMax((int) playerDuration);
                    documentFragment.setSeekbarMax((int) playerDuration);
                    break;
                case BUFFER:
                    Log.e(TAG, "STATE_BUFFERING");
                    playbackView.showProgressbar(true);
                    break;
                case STOP:
                    playbackView.showProgressbar(false);
                    Log.e(TAG, "STATE_STOP");
//                    getWatchPlayback().stop();
//                    playbackView.setPlayIcon(true);
                    fragment.updateViewState(MyControlView.PlayState.Paused);
                    documentFragment.updateViewState(MyControlView.PlayState.Paused);
                    break;
                case END:
                    playbackView.showProgressbar(false);
                    Log.e(TAG, "STATE_ENDED");
                    playerCurrentPosition = 0;
//                    getWatchPlayback().stop();
//                    playbackView.setPlayIcon(true);
                    fragment.updateViewState(MyControlView.PlayState.Paused);
                    documentFragment.updateViewState(MyControlView.PlayState.Paused);
                    break;
            }
        }

        @Override
        public void onEvent(int event, String msg) {
            switch (event) {
                case Constants.Event.EVENT_DPI_LIST:
                    break;
                case Constants.Event.EVENT_DPI_CHANGED:
//                    playbackView.setQualityChecked(msg);
                    break;
            }
        }

        @Override
        public void onError(int errorCode, int innerErrorCode, String msg) {
            switch (errorCode) {
                case Constants.ErrorCode.ERROR_INIT:
                    break;
                case Constants.ErrorCode.ERROR_INIT_FIRST:
                    break;
            }
            playbackView.showProgressbar(false);
//            playbackView.setPlayIcon(true);
            watchView.showToast("播放出错：" + msg);
            fragment.updateViewState(MyControlView.PlayState.Paused);
            documentFragment.updateViewState(MyControlView.PlayState.Paused);
        }
    }

    //每秒获取一下进度
    private void handlePosition() {
        if (timer != null)
            return;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    @Override
    public void showChatView(boolean emoji, InputUser user, int limit) {
        watchView.showChatView(emoji, user, limit);
    }

    @Override
    public void sendChat(final String text) {
        if (!VhallSDK.isLogin()) {
            Toast.makeText(watchView.getActivity(), R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
            return;
        }
        getWatchPlayback().sendComment(text, new RequestCallback() {
            @Override
            public void onSuccess() {
                initCommentData(pos = 0);
            }

            @Override
            public void onError(int errorCode, String reason) {
//                watchView.showToast(reason);
            }
        });
    }

//    @Override
//    public void sendCustom(JSONObject text) {

//    }

//    @Override
//    public void sendQuestion(String content) {

//    }

    @Override
    public void onLoginReturn() {

    }

//    @Override
//    public void showSurvey(String url,String title) {

//    }

//    @Override
//    public void showSurvey(String surveyid) {

//    }
}
