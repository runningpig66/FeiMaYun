package cn.aura.feimayun.vhall.watch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alivc.player.VcPlayerLog;
import com.aliyun.vodplayerview.view.GestureDialogManager;
import com.aliyun.vodplayerview.view.gesture.GestureView;
import com.aliyun.vodplayerview.view.interfaces.ViewAction;
import com.aliyun.vodplayerview.widget.AliyunScreenMode;
import com.aliyun.vodplayerview.widget.AliyunVodPlayerView;
import com.vhall.player.vod.VodPlayerView;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.view.MyControlView;

import static android.view.View.VISIBLE;

/**
 * 观看回放的Fragment
 */
public class WatchPlaybackFragment extends Fragment
        implements WatchContract.PlaybackView {
    WatchContract.PlaybackPresenter mPresenter;
    //    ContainerLayout rl_video_container;//视频区容器
    VodPlayerView vodplayer_view;//视频区容器
    ProgressBar pb;
    WatchActivity mContext;
    //用AudioManager获取音频焦点避免音视频声音并发问题
    AudioManager mAudioManager;
    AudioFocusRequest mFocusRequest;
    AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private boolean isVisible = false;
    //在界面可见时在自动开始播放，这个消息是回放初始化完成后发出的
    @SuppressLint("HandlerLeak")
    public Handler handleAutoPlay = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            Toast.makeText(mContext, "我执行自动播放啦,isVisible:" + isVisible, Toast.LENGTH_SHORT).show();
            if (isVisible) {
                mPresenter.startPlay();
            }
        }
    };
    //皮肤view
    private MyControlView myControlView;
    //手势操作view
    private GestureView mGestureView;
    private RelativeLayout root;
    //是否锁定全屏
    private boolean mIsFullScreenLocked = false;
    //手势对话框控制
    private GestureDialogManager mGestureDialogManager;
    private AudioManager mAudioManage;
    private int maxVolume = 0;
    private int currentVolume = 0;
    private String titleString = "";
    //当前屏幕模式
    private AliyunScreenMode mCurrentScreenMode = AliyunScreenMode.Small;

    public static WatchPlaybackFragment newInstance() {
        return new WatchPlaybackFragment();
    }

    public boolean ismIsFullScreenLocked() {
        return mIsFullScreenLocked;
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
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (WatchActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManage = (AudioManager) MyApplication.context.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManage != null) {
            maxVolume = mAudioManage.getStreamMaxVolume(3);
            currentVolume = mAudioManage.getStreamVolume(3);
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

    public void setScreenBrightness(int brightness) {
        if (this.mContext != null) {
            VcPlayerLog.d("Player", "setScreenBrightness mContext instanceof Activity brightness = " + brightness);
            if (brightness > 0) {
                Window localWindow = this.mContext.getWindow();
                WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
                localLayoutParams.screenBrightness = (float) brightness / 100.0F;
                localWindow.setAttributes(localLayoutParams);
            }
        } else {
            try {
                boolean suc = System.putInt(this.mContext.getContentResolver(), "screen_brightness_mode", 0);
                System.putInt(this.mContext.getContentResolver(), "screen_brightness", (int) ((float) brightness * 2.55F));
                VcPlayerLog.d("Player", "setScreenBrightness suc " + suc);
            } catch (Exception var4) {
                VcPlayerLog.e("Player", "cannot set brightness cause of no write_setting permission e = " + var4.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vhall_watch_playback_fragment, container, false);
//        rl_video_container = view.findViewById(R.id.rl_video_container);
        vodplayer_view = view.findViewById(R.id.surface_view);
        //把输送给surfaceView的视频画面，直接显示到屏幕上,不要维持它自身的缓冲区
        pb = view.findViewById(R.id.pb);
        root = view.findViewById(R.id.root1);
        initVideoView();
        myControlView.hide(ViewAction.HideType.Normal);
        return view;
    }

    @Override
    public VodPlayerView getVideoView() {
        return vodplayer_view;
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

    /**
     * 初始化手势的控制类
     */
    private void initGestureDialogManager() {
        Context context = getContext();
        if (context instanceof Activity) {
            mGestureDialogManager = new GestureDialogManager((Activity) context);
        }
    }

    private void initGestureView() {
        mGestureView = new GestureView(mContext);

        //设置手势监听
        mGestureView.setOnGestureListener(new GestureView.GestureListener() {
            @Override
            public void onHorizontalDistance(float downX, float nowX) {
                if (myControlView.getVisibility() != VISIBLE) {
                    myControlView.show();
                }

                //水平滑动调节seek。
                // seek需要在手势结束时操作。
                long duration = mPresenter.getDurationCustom();
                long position = mPresenter.getCurrentPositionCustom();
                long deltaPosition = 0;

//                if () {
                //在播放时才能调整大小
                deltaPosition = (long) (nowX - downX) * duration / vodplayer_view.getWidth();
//                }

                if (mGestureDialogManager != null) {
                    mGestureDialogManager.showSeekDialog(root, (int) position);
                    mGestureDialogManager.updateSeekDialog(duration, position, deltaPosition);
                }
            }

            @Override
            public void onLeftVerticalDistance(float downY, float nowY) {
                //左侧上下滑动调节亮度
                int changePercent = (int) ((nowY - downY) * 100 / vodplayer_view.getHeight());

                if (mGestureDialogManager != null) {
                    mGestureDialogManager.showBrightnessDialog(root);
                    int brightness = mGestureDialogManager.updateBrightnessDialog(changePercent);
                    setScreenBrightness(brightness);
                }
            }

            @Override
            public void onRightVerticalDistance(float downY, float nowY) {
                //右侧上下滑动调节音量
                int changePercent = (int) ((nowY - downY) * 100 / vodplayer_view.getHeight());
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

                    int seekPosition = mGestureDialogManager.dismissSeekDialog();
                    if (seekPosition >= mPresenter.getDurationCustom()) {
                        seekPosition = (int) (mPresenter.getDurationCustom() - 1000);
                    }

                    if (seekPosition >= 0) {
                        mPresenter.onStopTrackingTouch(seekPosition);
                        myControlView.setVideoPosition(seekPosition);
//                        seekTo(seekPosition);
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
                //双击事件，控制暂停播放
                mPresenter.onPlayClick();
                myControlView.show();
//                switchPlayerState();
            }
        });
    }

    private void initControlView() {
        myControlView = new MyControlView(mContext);

        myControlView.setTheme(AliyunVodPlayerView.Theme.Orange);
        //设置PPT
        myControlView.setmOnPPTClickListener(new MyControlView.OnPPTClickListener() {
            @Override
            public void onClick() {
                mContext.setPlace();
            }
        });
        //设置播放按钮点击
        myControlView.setOnPlayStateClickListener(new MyControlView.OnPlayStateClickListener() {
            @Override
            public void onPlayStateClick() {
                mPresenter.onPlayClick();
            }
        });

        //设置进度条的seek监听
        myControlView.setOnSeekListener(new MyControlView.OnSeekListener() {
            @Override
            public void onSeekEnd(int position) {
                mPresenter.onStopTrackingTouch(position);
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
                mPresenter.changeScreenOri();
            }
        });
        //点击了标题栏的返回按钮
        myControlView.setOnBackClickListener(new MyControlView.OnBackClickListener() {
            @Override
            public void onClick() {
                mContext.onBackPressed();
                //屏幕由竖屏转为横屏
                if (mCurrentScreenMode == AliyunScreenMode.Full) {
                    //全屏情况转到了横屏
                } else if (mCurrentScreenMode == AliyunScreenMode.Small) {
                    myControlView.setScreenModeStatus(AliyunScreenMode.Small);
                }
            }
        });
        myControlView.setTitleString(titleString);
        updateViewState(MyControlView.PlayState.Idle);
    }

    protected void updateViewState(MyControlView.PlayState playState) {
        myControlView.setPlayState(playState);
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

    protected void updatePPTState(MyControlView.PPTState pptState) {
        myControlView.setPPTState(pptState);
    }

    @Override
    public void setPresenter(WatchContract.PlaybackPresenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    @Override
    public void setSeekbarMax(int max) {
        myControlView.setDuration(max);
    }

    @Override
    public void setSeekbarCurrentPosition(int position) {
        myControlView.setVideoPosition(position);
//        seekbar.setProgress(position);
    }

    @Override
    public void showProgressbar(boolean show) {
        if (show)
            pb.setVisibility(VISIBLE);
        else
            pb.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        isVisible = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        isVisible = true;
        if (requestTheAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (mPresenter != null) {
                mPresenter.startPlay();
            }
        } else {
            Toast.makeText(mContext, "请关闭其他音频再开始播放", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onFragmentDestory();
            isVisible = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter != null) {
            mPresenter.start();
            mPresenter.onResume();
        } else {
            mPresenter = mContext.getPlaybackPresenter();
            if (mPresenter != null) {
                mPresenter.start();
                mPresenter.onResume();
            }
        }
        isVisible = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= 26) {
            releaseTheAudioFocusSDK26(mFocusRequest);
        } else {
            releaseTheAudioFocusSDK19(mAudioFocusChangeListener);
        }
        if (mPresenter != null) {
            mPresenter.onPause();
        }
        isVisible = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void setVisiable(boolean canSee) {
        if (canSee) {
            ViewGroup.LayoutParams params =
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            root.addView(mGestureView, params);
            root.addView(myControlView, params);
            myControlView.show();
        } else {
            myControlView.hide(ViewAction.HideType.Normal);
            root.removeView(mGestureView);
            root.removeView(myControlView);
        }
    }

    //请求音频焦点 设置监听
    private int requestTheAudioFocus() {
        if (Build.VERSION.SDK_INT < 8) {//Android 2.2开始(API8)才有音频焦点机制
            return 0;
        }
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
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
            AudioAttributes mPlaybackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                    .build();
            return mAudioManager.requestAudioFocus(mFocusRequest);
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

    //sdk26再使用时做判断
    private void releaseTheAudioFocusSDK26(AudioFocusRequest mAudioFocusRequest) {
        if (mAudioManager != null && mAudioFocusRequest != null) {
            mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        }
    }

}