package cn.aura.app.vhall.watch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.Watch;
import com.vhall.business.WatchLive;
import com.vhall.business.WatchPlayback;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.WebinarInfo;
import com.vhall.playersdk.player.VHExoPlayer;
import com.vhall.playersdk.player.vhallplayer.VHallPlayer;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.aura.app.R;
import cn.aura.app.vhall.Param;
import cn.aura.app.vhall.chat.ChatContract;
import cn.aura.app.vhall.chat.ChatFragment;
import cn.aura.app.vhall.util.VhallUtil;
import cn.aura.app.vhall.util.emoji.InputUser;
import cn.aura.app.view.MyControlView;

/**
 * 观看回放的Presenter HAVE DONE
 */
public class WatchPlaybackPresenter implements WatchContract.PlaybackPresenter, ChatContract.ChatPresenter {

    private Param param;
    private WatchContract.PlaybackView playbackView;
    private WatchContract.DocumentView documentView;
    private WatchContract.WatchView watchView;
    private ChatContract.ChatView chatView;
    private DocumentFragment documentFragment;
    private WatchPlayback watchPlayback;
    private int[] scaleTypeList = new int[]{WatchLive.FIT_DEFAULT, WatchLive.FIT_CENTER_INSIDE, WatchLive.FIT_X, WatchLive.FIT_Y, WatchLive.FIT_XY};
    private int currentPos = 0;
    private int scaleType = WatchLive.FIT_DEFAULT;
    private int limit = 5;
    private int pos = 0;
    private long playerCurrentPosition = 0L; // 当前的进度
    private String playerDurationTimeStr = "00:00:00";
    private boolean loadingVideo = false;
    private boolean loadingComment = false;
    private Timer timer;
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
    private WatchPlaybackFragment fragment;

    WatchPlaybackPresenter(WatchContract.PlaybackView playbackView, WatchContract.DocumentView documentView, ChatContract.ChatView chatView, WatchContract.WatchView watchView, Param param, DocumentFragment documentFragment) {
        this.documentFragment = documentFragment;
        this.playbackView = playbackView;
        fragment = (WatchPlaybackFragment) playbackView;
        this.documentView = documentView;
        this.watchView = watchView;
        this.chatView = chatView;
        this.param = param;
        this.playbackView.setPresenter(this);
        this.chatView.setPresenter(this);
        this.watchView.setPresenter(this);
    }

    public void setParam(Param param) {
        this.param = param;
    }

    @Override
    public void start() {
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
                chatView.notifyDataChanged(ChatFragment.CHAT_EVENT_CHAT, list);
            }

            @Override
            public void onFailed(int errorcode, String message) {
                loadingComment = false;
//                Toast.makeText(watchView.getActivity(), message, Toast.LENGTH_SHORT).show();
//                watchView.showToast(messaage);
            }
        });
    }

    void initWatch() {
        if (loadingVideo) return;
        loadingVideo = true;
        //游客ID及昵称 已登录用户可传空
        TelephonyManager telephonyMgr = (TelephonyManager) watchView.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String customeId = telephonyMgr.getDeviceId();
        String customNickname = Build.BRAND + "手机用户";
        VhallSDK.initWatch(param.watchId, customeId, customNickname, param.key, getWatchPlayback(), WebinarInfo.VIDEO, new RequestCallback() {
            @Override
            public void onSuccess() {
                loadingVideo = false;
                handlePosition();
                pos = 0;
                initCommentData(pos);
//                watchView.showNotice(getWatchPlayback().getNotice()); //显示公告
                fragment.handleAutoPlay.obtainMessage().sendToTarget();
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
                if (getWatchPlayback().getPlayerState() == VHExoPlayer.STATE_ENDED) {
                    getWatchPlayback().seekTo(0);
                }
                startPlay();
            }
        }
    }

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
        }
    }


    @Override
    public void onPause() {
        //onPause只需要根据Activity的生命周期调用即可,暂停可以使用stop方法
        getWatchPlayback().onPause();
        fragment.updateViewState(MyControlView.PlayState.Paused);
        documentFragment.updateViewState(MyControlView.PlayState.Paused);
    }

    @Override
    public void onStop() {
        getWatchPlayback().stop();
        fragment.updateViewState(MyControlView.PlayState.Paused);
        documentFragment.updateViewState(MyControlView.PlayState.Paused);
    }

    @Override
    public void onSwitchPixel(String pix) {
        getWatchPlayback().setDefinition(pix);
    }

    @Override
    public WatchPlayback getWatchPlayback() {
        if (watchPlayback == null) {
            WatchPlayback.Builder builder = new WatchPlayback.Builder().context(watchView.getActivity()).containerLayout(playbackView.getContainer()).callback(new WatchCallback()).docCallback(new DocCallback());
            watchPlayback = builder.build();
        }
        return watchPlayback;
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
                Toast.makeText(watchView.getActivity(), reason, Toast.LENGTH_SHORT).show();
//                watchView.showToast(reason);
            }
        });
    }

    @Override
    public void onLoginReturn() {
    }

    private class DocCallback implements WatchPlayback.DocumentEventCallback {

        @Override
        public void onEvent(String key, List<MessageServer.MsgInfo> msgInfos) {
            if (msgInfos != null && msgInfos.size() > 0) {
                documentView.paintPPT(key, msgInfos);
                documentView.paintBoard(key, msgInfos);
            }
        }

        @Override
        public void onEvent(MessageServer.MsgInfo msgInfo) {
            documentView.paintPPT(msgInfo);
            documentView.paintBoard(msgInfo);
        }
    }

    private class WatchCallback implements WatchPlayback.WatchEventCallback {
        @Override
        public void onVhallPlayerStatue(boolean playWhenReady, int playbackState) {//播放过程中的状态信息
            switch (playbackState) {
                case VHallPlayer.STATE_IDLE:
//                    Log.e(TAG, "STATE_IDLE");
                    break;
                case VHallPlayer.STATE_PREPARING:
//                    Log.e(TAG, "STATE_PREPARING");
                    fragment.updateViewState(MyControlView.PlayState.Playing);
                    documentFragment.updateViewState(MyControlView.PlayState.Playing);
//                    playbackView.setPlayIcon(false);
//                    documentFragment.setPlayIcon(false);
                    playbackView.showProgressbar(true);
//                    documentFragment.showProgressbar(true);
                    break;
                case VHallPlayer.STATE_BUFFERING:
//                    Log.e(TAG, "STATE_BUFFERING");
                    playbackView.showProgressbar(true);
//                    documentFragment.showProgressbar(true);
                    break;
                case VHallPlayer.STATE_READY:
                    playbackView.showProgressbar(false);
//                    documentFragment.showProgressbar(false);
                    documentFragment.playerDuration = getWatchPlayback().getDuration();
                    long playerDuration = getWatchPlayback().getDuration();
                    documentFragment.playerDurationTimeStr = VhallUtil.converLongTimeToStr(playerDuration);
                    playerDurationTimeStr = VhallUtil.converLongTimeToStr(playerDuration);
                    documentFragment.setSeekbarMax((int) documentFragment.playerDuration);
                    playbackView.setSeekbarMax((int) playerDuration);
                    if (playWhenReady) {
//                        playbackView.setPlayIcon(false);
                        fragment.updateViewState(MyControlView.PlayState.Playing);
                        documentFragment.updateViewState(MyControlView.PlayState.Playing);
//                        documentFragment.setPlayIcon(false);
                    } else {
                        fragment.updateViewState(MyControlView.PlayState.Idle);
                        documentFragment.updateViewState(MyControlView.PlayState.Idle);
//                        playbackView.setPlayIcon(true);
//                        documentFragment.setPlayIcon(true);
                    }
//                    Log.e(TAG, "STATE_READY");
                    break;
                case VHallPlayer.STATE_ENDED:
                    playbackView.showProgressbar(false);
//                    documentFragment.showProgressbar(false);
//                    Log.e(TAG, "STATE_ENDED");
                    playerCurrentPosition = 0;
                    documentFragment.playerCurrentPosition = 0;
                    getWatchPlayback().stop();
                    fragment.updateViewState(MyControlView.PlayState.Paused);
                    documentFragment.updateViewState(MyControlView.PlayState.Paused);
//                    playbackView.setPlayIcon(true);
//                    documentFragment.setPlayIcon(true);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void uploadSpeed(String kbps) {
        }

        @Override
        public void onError(int errorCode, String errorMeg) {//播放出错
            playbackView.showProgressbar(false);
            fragment.updateViewState(MyControlView.PlayState.Paused);
            documentFragment.updateViewState(MyControlView.PlayState.Paused);
            Toast.makeText(watchView.getActivity(), "播放出错", Toast.LENGTH_SHORT).show();
//            playbackView.setPlayIcon(true);
//            documentFragment.showProgressbar(false);
//            documentFragment.setPlayIcon(true);
//            watchView.showToast("播放出错");
        }

        @Override
        public void onStateChanged(int stateCode) {
            switch (stateCode) {
                case Watch.STATE_CHANGE_DEFINITION:
//                    String dpi = watchPlayback.getCurrentDPI();
                    break;
            }
        }

        @Override
        public void videoInfo(int width, int height) {//视频宽高改变
        }

    }


}
