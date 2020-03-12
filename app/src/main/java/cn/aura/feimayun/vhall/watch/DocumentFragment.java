package cn.aura.feimayun.vhall.watch;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alivc.player.VcPlayerLog;
import com.aliyun.vodplayerview.view.GestureDialogManager;
import com.aliyun.vodplayerview.view.gesture.GestureView;
import com.aliyun.vodplayerview.view.interfaces.ViewAction;
import com.aliyun.vodplayerview.widget.AliyunScreenMode;
import com.aliyun.vodplayerview.widget.AliyunVodPlayerView;
import com.vhall.business.MessageServer;
import com.vhall.business.widget.PPTView;
import com.vhall.business.widget.WhiteBoardView;

import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.vhall.BasePresenter;
import cn.aura.feimayun.vhall.util.VhallUtil;
import cn.aura.feimayun.view.MyControlView;

import static android.view.View.VISIBLE;

/**
 * 文档页的Fragment HAVE DONE
 */
public class DocumentFragment extends Fragment
        implements WatchContract.DocumentView {
    public long playerCurrentPosition = 0L; // 当前的进度
    public long playerDuration;
    public String playerDurationTimeStr = "00:00:00";
    //    private ProgressBar pb;
    private PPTView iv_doc;
    private WhiteBoardView board;
    private WatchActivity activity;
    private WatchPlaybackPresenter watchPlaybackPresenter;
    //手势对话框控制
    private GestureDialogManager mGestureDialogManager;
    //手势操作view
    private GestureView mGestureView;
    private AudioManager mAudioManage;
    private int maxVolume = 0;
    private int currentVolume = 0;
    private String titleString = "";
    //当前屏幕模式
    private AliyunScreenMode mCurrentScreenMode = AliyunScreenMode.Small;
    //皮肤view
    private MyControlView myControlView;
    private RelativeLayout root;
    //是否锁定全屏
    private boolean mIsFullScreenLocked = false;
    private TextView document_textview_marquee;

    public DocumentFragment() {
    }

    public static DocumentFragment newInstance() {
        return new DocumentFragment();
    }

    public void setTitleString(String titleString) {
        this.titleString = titleString;
    }

    public void setmCurrentScreenMode(AliyunScreenMode mCurrentScreenMode) {
        this.mCurrentScreenMode = mCurrentScreenMode;
        if (mCurrentScreenMode == AliyunScreenMode.Full) {
            myControlView.setScreenModeStatus(AliyunScreenMode.Full);
        } else if (mCurrentScreenMode == AliyunScreenMode.Small) {
            myControlView.setScreenModeStatus(AliyunScreenMode.Small);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("20200311", "onCreate: ");
        super.onCreate(savedInstanceState);
        mAudioManage = (AudioManager) MyApplication.context.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManage != null) {
            maxVolume = mAudioManage.getStreamMaxVolume(3);
            currentVolume = mAudioManage.getStreamVolume(3);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("20200311", "onCreateView: ");
        View view = inflater.inflate(R.layout.vhall_document_fragment, container, false);
        root = view.findViewById(R.id.root1);
        iv_doc = view.findViewById(R.id.iv_doc);
        board = view.findViewById(R.id.board);
        document_textview_marquee = view.findViewById(R.id.document_textview_marquee);
        document_textview_marquee.setSelected(true);
        initVideoView();
        return view;
    }

    /**
     * 初始化view
     */
    private void initVideoView() {
        //初始化手势view
        initGestureView();
        //初始化控制栏
        initControlView();
        //初始化手势对话框控制
        initGestureDialogManager();
    }

    public void setScreenBrightness(int brightness) {
        if (this.activity != null) {
            VcPlayerLog.d("Player", "setScreenBrightness mContext instanceof Activity brightness = " + brightness);
            if (brightness > 0) {
                Window localWindow = this.activity.getWindow();
                WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
                localLayoutParams.screenBrightness = (float) brightness / 100.0F;
                localWindow.setAttributes(localLayoutParams);
            }
        } else {
            try {
                boolean suc = Settings.System.putInt(this.activity.getContentResolver(), "screen_brightness_mode", 0);
                Settings.System.putInt(this.activity.getContentResolver(), "screen_brightness", (int) ((float) brightness * 2.55F));
                VcPlayerLog.d("Player", "setScreenBrightness suc " + suc);
            } catch (Exception var4) {
                VcPlayerLog.e("Player", "cannot set brightness cause of no write_setting permission e = " + var4.getMessage());
            }
        }
    }

    public void SetVolumn(int fVol) {
        float volume = (float) fVol * 1.0F / 100.0F;
        this.mAudioManage.setStreamVolume(3, (int) (volume * (float) this.maxVolume), 0);
    }

    public int getVolume() {
        this.currentVolume = this.mAudioManage.getStreamVolume(3);
        return (int) ((float) this.currentVolume * 100.0F / (float) this.maxVolume);
    }

    private void initGestureView() {
        final int type = activity.getType();//确定当前是录播还是直播
        mGestureView = new GestureView(activity);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(mGestureView, params);

        //设置手势监听
        mGestureView.setOnGestureListener(new GestureView.GestureListener() {
            @Override
            public void onHorizontalDistance(float downX, float nowX) {
                // 只有在回放状态下有播放进度等
                if (type == VhallUtil.WATCH_PLAYBACK) {
                    if (myControlView.getVisibility() != VISIBLE) {
                        myControlView.show();
                    }

                    //水平滑动调节seek。
                    // seek需要在手势结束时操作。
                    long duration = watchPlaybackPresenter.getWatchPlayback().getDuration();
                    long position = watchPlaybackPresenter.getWatchPlayback().getCurrentPosition();
                    long deltaPosition;

//                if () {
                    //在播放时才能调整大小
                    deltaPosition = (long) (nowX - downX) * duration / root.getWidth();
//                }

                    if (mGestureDialogManager != null) {
                        mGestureDialogManager.showSeekDialog(root, (int) position);
                        mGestureDialogManager.updateSeekDialog(duration, position, deltaPosition);
                    }
                }

            }

            @Override
            public void onLeftVerticalDistance(float downY, float nowY) {
                //左侧上下滑动调节亮度
                int changePercent = (int) ((nowY - downY) * 100 / root.getHeight());
                if (mGestureDialogManager != null) {
                    mGestureDialogManager.showBrightnessDialog(root);
                    int brightness = mGestureDialogManager.updateBrightnessDialog(changePercent);
                    setScreenBrightness(brightness);
                }
            }

            @Override
            public void onRightVerticalDistance(float downY, float nowY) {
                //右侧上下滑动调节音量
                int changePercent = (int) ((nowY - downY) * 100 / root.getHeight());
                int volume = getVolume();

                if (mGestureDialogManager != null) {
                    mGestureDialogManager.showVolumeDialog(root, volume);
                    int targetVolume = mGestureDialogManager.updateVolumeDialog(changePercent);
                    SetVolumn(targetVolume);//通过返回值改变音量
                }
            }

            @Override
            public void onGestureEnd() {
                //手势结束。
                //seek需要在结束时操作。
                if (mGestureDialogManager != null) {
                    mGestureDialogManager.dismissBrightnessDialog();
                    mGestureDialogManager.dismissVolumeDialog();

                    if (type == VhallUtil.WATCH_PLAYBACK) {
                        int seekPosition = mGestureDialogManager.dismissSeekDialog();
                        if (seekPosition >= watchPlaybackPresenter.getWatchPlayback().getDuration()) {
                            seekPosition = (int) (watchPlaybackPresenter.getWatchPlayback().getDuration() - 1000);
                        }

                        if (seekPosition >= 0) {
                            watchPlaybackPresenter.onStopTrackingTouch(seekPosition);
                            myControlView.setVideoPosition(seekPosition);
//                        seekTo(seekPosition);
                        }
                    }

                }
            }

            @Override
            public void onSingleTap() {
                //单击事件，显示控制栏
                if (myControlView != null) {
                    if (myControlView.getVisibility() != VISIBLE) {
                        myControlView.show();
                    } else {
                        myControlView.hide(ViewAction.HideType.Normal);
                    }
                }
            }

            @Override
            public void onDoubleTap() {
                //直播时不需要双击事件
                if (type == VhallUtil.WATCH_PLAYBACK) {
                    //双击事件，控制暂停播放
                    watchPlaybackPresenter.onPlayClick();
                    myControlView.show();
                }

            }

        });
    }

    /**
     * 初始化手势的控制类
     */
    private void initGestureDialogManager() {
        Context context = getContext();
        if (context instanceof Activity) {
            mGestureDialogManager = new GestureDialogManager((Activity) context);
        }
    }

    private void initControlView() {
        myControlView = new MyControlView(activity);
        final int type = activity.getType();
        if (type == VhallUtil.WATCH_LIVE) {
            myControlView.setmPlayType(MyControlView.PlayType.Play);
        }
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(myControlView, params);
        myControlView.setTheme(AliyunVodPlayerView.Theme.Orange);
        //设置PPT
        myControlView.setmOnPPTClickListener(new MyControlView.OnPPTClickListener() {
            @Override
            public void onClick() {
                activity.setPlace();
            }
        });
        //设置播放按钮点击
        myControlView.setOnPlayStateClickListener(new MyControlView.OnPlayStateClickListener() {
            @Override
            public void onPlayStateClick() {
                if (type == VhallUtil.WATCH_LIVE) {
                    WatchLivePresenter presenter = activity.getWatchLivePresenter();
                    presenter.onWatchBtnClick();
                } else if (type == VhallUtil.WATCH_PLAYBACK) {
                    watchPlaybackPresenter.onPlayClick();
                }
            }
        });
        //设置进度条的seek监听
        myControlView.setOnSeekListener(new MyControlView.OnSeekListener() {
            @Override
            public void onSeekEnd(int position) {
                Log.d("12341234", "onSeekEnd: " + position);
                watchPlaybackPresenter.onStopTrackingTouch(position);
                myControlView.setVideoPosition(position);
            }

            @Override
            public void onSeekStart() {
            }
        });
        //点击锁屏的按钮
        myControlView.setOnScreenLockClickListener(new MyControlView.OnScreenLockClickListener() {
            @Override
            public void onClick() {
                lockScreen(!mIsFullScreenLocked);
            }
        });
        //点击全屏/小屏按钮
        myControlView.setOnScreenModeClickListener(new MyControlView.OnScreenModeClickListener() {
            @Override
            public void onClick() {
                if (mIsFullScreenLocked) {
                    return;
                }
                if (type == VhallUtil.WATCH_LIVE) {
                    activity.getWatchLivePresenter().changeOriention();
                } else if (type == VhallUtil.WATCH_PLAYBACK) {
                    activity.getPlaybackPresenter().changeScreenOri();
                }

            }
        });
        //点击了标题栏的返回按钮
        myControlView.setOnBackClickListener(new MyControlView.OnBackClickListener() {
            @Override
            public void onClick() {
                activity.onBackPressed();
                //屏幕由竖屏转为横屏
//                if (mCurrentScreenMode == AliyunScreenMode.Full) {
                //全屏情况转到了横屏
//                } else
                if (mCurrentScreenMode == AliyunScreenMode.Small) {
                    myControlView.setScreenModeStatus(AliyunScreenMode.Small);
                }
            }
        });
        myControlView.setTitleString(titleString);
        updateViewState(MyControlView.PlayState.Idle);
    }

    protected void updateViewState(MyControlView.PlayState playState) {
        if (myControlView != null) {
            myControlView.setPlayState(playState);
        }
        if (playState == MyControlView.PlayState.Idle) {
//            mGestureView.hide(ViewAction.HideType.Normal);
        } else {
            if (mIsFullScreenLocked) {
                mGestureView.hide(ViewAction.HideType.Normal);
            } else {
                mGestureView.show();
            }
        }
    }

    protected void updatePPTState(MyControlView.PPTState pptState) {
        myControlView.setPPTState(pptState);
    }

    @Override
    public void paintBoard(MessageServer.MsgInfo msgInfo) {
        board.setStep(msgInfo);
    }

    @Override
    public void paintBoard(String key, List<MessageServer.MsgInfo> msgInfos) {
        board.setSteps(key, msgInfos);
    }

    @Override
    public void paintPPT(MessageServer.MsgInfo msgInfo) {
        iv_doc.setStep(msgInfo);
    }

    @Override
    public void paintPPT(String key, List<MessageServer.MsgInfo> msgInfos) {
        iv_doc.setSteps(key, msgInfos);
    }

    @Override
    public void showType(int type) {
        switch (type) {
            case 0://文档
                iv_doc.setVisibility(View.VISIBLE);
                board.setVisibility(View.GONE);
                board.setShowDoc(true);
                break;
            case 1://白板
                iv_doc.setVisibility(View.VISIBLE);
                board.setVisibility(View.VISIBLE);
                board.setShowDoc(true);
                break;
            case 2://关闭文档
                iv_doc.setVisibility(View.GONE);
                board.setVisibility(View.GONE);
                board.setShowDoc(false);
                break;
        }
    }

    @Override
    public void setPresenter(BasePresenter presenter) {

    }

    private boolean mCanSee = true;//PPT在上方默认可见

    public void setVisiable(boolean canSee) {
        mCanSee = canSee;
        Log.d("asdfasdf", "setVisiable: " + canSee);
        int type = activity.getType();
        if (canSee) {
            ViewGroup.LayoutParams params =
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            root.addView(mGestureView, params);
            root.addView(myControlView, params);
//            pb.setVisibility(View.VISIBLE);
            //公告信息不为空才显示
            if (mNoticeString != null && !TextUtils.isEmpty(mNoticeString) && !isTimeCountDonw) {
                document_textview_marquee.setVisibility(View.VISIBLE);
            }
            if (type == VhallUtil.WATCH_PLAYBACK) {
                myControlView.show();
                document_textview_marquee.setVisibility(View.INVISIBLE);//回访隐藏公告信息栏
            }
        } else {
//            myControlView = new MyControlView(activity);
//            ViewGroup.LayoutParams params =
//                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            root.addView(myControlView, params);
//            pb.setVisibility(View.INVISIBLE);
            root.removeView(myControlView);
            root.removeView(mGestureView);
            document_textview_marquee.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (WatchActivity) context;
        int type = activity.getType();
        if (type == VhallUtil.WATCH_LIVE) {

        } else if (type == VhallUtil.WATCH_PLAYBACK) {
            watchPlaybackPresenter = activity.getPlaybackPresenter();
        }
    }

    public void setSeekbarMax(int max) {
        myControlView.setDuration(max);
    }

    public void setSeekbarCurrentPosition(int position) {
        myControlView.setVideoPosition(position);
    }

    public boolean ismIsFullScreenLocked() {
        return mIsFullScreenLocked;
    }

    /**
     * 锁定屏幕。锁定屏幕后，只有锁会显示，其他都不会显示。手势也不可用
     *
     * @param lockScreen 是否锁住
     */
    public void lockScreen(boolean lockScreen) {
        mIsFullScreenLocked = lockScreen;
        myControlView.setScreenLockStatus(mIsFullScreenLocked);
        mGestureView.setScreenLockStatus(mIsFullScreenLocked);
    }

    private String mNoticeString = "";
    private CountDownTimer countDownTimer;
    //公告只显示60秒
    int mCount = 10;
    private boolean isTimeCountDonw = false;

    //设置公告信息
    public void setNotice(String noticeString) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimeCountDonw = false;
        if (noticeString == null | TextUtils.isEmpty(noticeString)) {
            document_textview_marquee.setVisibility(View.INVISIBLE);
            document_textview_marquee.setText("");
        } else {
            mNoticeString = noticeString;
            if (mCanSee) {
                document_textview_marquee.setVisibility(VISIBLE);
            }
            document_textview_marquee.setText("公告：" + noticeString);
        }
        countDownTimer = new CountDownTimer(mCount * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                isTimeCountDonw = true;
                document_textview_marquee.setVisibility(View.INVISIBLE);
            }
        }.start();
    }
}
