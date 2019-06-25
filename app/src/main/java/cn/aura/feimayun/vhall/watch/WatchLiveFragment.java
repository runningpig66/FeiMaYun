package cn.aura.feimayun.vhall.watch;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.vhall.business.widget.ContainerLayout;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.view.MyControlView;

import static android.view.View.VISIBLE;


/**
 * 观看直播的Fragment
 */
public class WatchLiveFragment extends Fragment implements WatchContract.LiveView {
    ProgressBar progressbar;
    //用AudioManager获取音频焦点避免音视频声音并发问题
    AudioManager mAudioManager;
    AudioFocusRequest mFocusRequest;
    AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private WatchContract.LivePresenter mPresenter;
    private ContainerLayout mContainerLayout;
    private WatchActivity context;
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
    //皮肤view
    private MyControlView myControlView;
    //手势操作view
    private GestureView mGestureView;
    private RelativeLayout root;

    public static WatchLiveFragment newInstance() {
        return new WatchLiveFragment();
    }

    public boolean ismIsFullScreenLocked() {
        return mIsFullScreenLocked;
    }

    public void setTitleString(String titleString) {
        this.titleString = titleString;
    }

    public void setmCurrentScreenMode(AliyunScreenMode mCurrentScreenMode) {
        this.mCurrentScreenMode = mCurrentScreenMode;
    }

    @Override
    public void setPresenter(WatchContract.LivePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (WatchActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        if (context != null) {
            VcPlayerLog.d("Player", "setScreenBrightness mContext instanceof Activity brightness = " + brightness);
            if (brightness > 0) {
                Window localWindow = ((Activity) context).getWindow();
                WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
                localLayoutParams.screenBrightness = (float) brightness / 100.0F;
                localWindow.setAttributes(localLayoutParams);
            }
        } else {
            try {
                boolean suc = Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", 0);
                Settings.System.putInt(context.getContentResolver(), "screen_brightness", (int) ((float) brightness * 2.55F));
                VcPlayerLog.d("Player", "setScreenBrightness suc " + suc);
            } catch (Exception var4) {
                VcPlayerLog.e("Player", "cannot set brightness cause of no write_setting permission e = " + var4.getMessage());
            }

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vhall_watch_live_fragment, container, false);
        mContainerLayout = view.findViewById(R.id.rl_container);
        progressbar = view.findViewById(R.id.progressbar);
        root = view.findViewById(R.id.root);
//        if (mPresenter != null) {
//            mPresenter.start();
//        }
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

    private void initGestureView() {
        mGestureView = new GestureView(context);
        //设置手势监听
        mGestureView.setOnGestureListener(new GestureView.GestureListener() {
            @Override
            public void onHorizontalDistance(float downX, float nowX) {
                //直播没有进度手势
            }

            @Override
            public void onLeftVerticalDistance(float downY, float nowY) {
                //左侧上下滑动调节亮度
                int changePercent = (int) ((nowY - downY) * 100 / mContainerLayout.getHeight());
                if (mGestureDialogManager != null) {
                    mGestureDialogManager.showBrightnessDialog(root);
                    int brightness = mGestureDialogManager.updateBrightnessDialog(changePercent);
                    setScreenBrightness(brightness);
                }
            }

            @Override
            public void onRightVerticalDistance(float downY, float nowY) {
                //右侧上下滑动调节音量
                int changePercent = (int) ((nowY - downY) * 100 / mContainerLayout.getHeight());
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
                //TODO 添加播放暂停
            }

        });
    }

    private void initControlView() {
        myControlView = new MyControlView(context);
        myControlView.setmPlayType(MyControlView.PlayType.Play);
        myControlView.setTheme(AliyunVodPlayerView.Theme.Orange);
        //设置PPT
        myControlView.setmOnPPTClickListener(new MyControlView.OnPPTClickListener() {
            @Override
            public void onClick() {
                context.setPlace();
            }
        });
        //设置播放按钮点击
        myControlView.setOnPlayStateClickListener(new MyControlView.OnPlayStateClickListener() {
            @Override
            public void onPlayStateClick() {
                mPresenter.onWatchBtnClick();
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
                mPresenter.changeOriention();
            }
        });
        //点击了标题栏的返回按钮
        myControlView.setOnBackClickListener(new MyControlView.OnBackClickListener() {
            @Override
            public void onClick() {
                context.onBackPressed();
                //屏幕由竖屏转为横屏
                if (mCurrentScreenMode == AliyunScreenMode.Small) {
                    myControlView.setScreenModeStatus(AliyunScreenMode.Small);
                }
            }
        });
        myControlView.setTitleString(titleString);
        updateViewState(MyControlView.PlayState.Idle);
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

    public void setScreenModeStatus() {
        if (mCurrentScreenMode == AliyunScreenMode.Full) {
            myControlView.setScreenModeStatus(AliyunScreenMode.Full);
        } else if (mCurrentScreenMode == AliyunScreenMode.Small) {
            myControlView.setScreenModeStatus(AliyunScreenMode.Small);
        }
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
    public ContainerLayout getWatchLayout() {
        return mContainerLayout;
    }

    @Override
    public void showLoading(boolean isShow) {
        if (isShow)
            progressbar.setVisibility(View.VISIBLE);
        else
            progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 26) {
            releaseTheAudioFocusSDK26(mFocusRequest);
        } else {
            releaseTheAudioFocusSDK19(mAudioFocusChangeListener);
        }
        mPresenter.stopWatch();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPresenter != null) {
            if (requestTheAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mPresenter.start();
            } else {
                Toast.makeText(context, "请关闭其他音频再开始播放", Toast.LENGTH_SHORT).show();
            }
        } else {
            WatchLivePresenter watchLivePresenter = context.getmPresenter();
            watchLivePresenter.start();
            mPresenter = watchLivePresenter;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestory();
        }
    }

    public void setVisiable(boolean canSee) {
        if (canSee) {
            ViewGroup.LayoutParams params =
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            root.addView(mGestureView, params);
            root.addView(myControlView, params);
        } else {
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
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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

    @Override
    public WatchLiveFragment getLiveFragment() {
        return this;
    }

}