package cn.aura.feimayun.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.vodplayerview.theme.ITheme;
import com.aliyun.vodplayerview.utils.TimeFormater;
import com.aliyun.vodplayerview.view.interfaces.ViewAction;
import com.aliyun.vodplayerview.widget.AliyunScreenMode;
import com.aliyun.vodplayerview.widget.AliyunVodPlayerView;

import java.lang.ref.WeakReference;

import cn.aura.feimayun.R;

public class MyControlView extends RelativeLayout implements ViewAction, ITheme {
    private static final int WHAT_HIDE = 0;
    private static final int DELAY_TIME = 5 * 1000; //5秒后隐藏
    // 标题，控制条单独控制是否可显示
    private boolean mTitleBarCanShow = true;
    private boolean mControlBarCanShow = true;
    private View mTitleBar;
    private View mControlBar;
    //这些是大小屏都有的==========START========
    //返回按钮
    private ImageView mTitlebarBackBtn;
    //标题
    private TextView mTitlebarText;
    //视频播放状态
    private PlayState mPlayState = PlayState.Idle;
    //PPT位置状态
    private PPTState mPPTState = PPTState.Top;
    //播放按钮
    private ImageView mPlayStateBtn;
    //锁定屏幕方向相关
    //屏幕方向是否锁定
    private boolean mScreenLocked = false;
    //锁屏按钮
    private ImageView mScreenLockBtn;
    //切换大小屏相关
    private AliyunScreenMode mAliyunScreenMode = AliyunScreenMode.Small;
    //全屏/小屏按钮
    private ImageView mScreenModeBtn;
    //PPT切换按钮
    private ImageView change_ppt_video;
    //大小屏公用的信息
    //播放的进度
    private int mVideoPosition = 0;
    //这些是大小屏都有的==========END========
    //seekbar拖动状态
    private boolean isSeekbarTouching = false;
    //这些是大屏时显示的
    //大屏的底部控制栏
    private View mLargeInfoBar;
    //当前位置文字
    private TextView mLargePositionText;
    //时长文字
    private TextView mLargeDurationText;
    //进度条
    private SeekBar mLargeSeekbar;
    //这些是小屏时显示的
    //底部控制栏
    private View mSmallInfoBar;
    //当前位置文字
    private TextView mSmallPositionText;
    //时长文字
    private TextView mSmallDurationText;
    //seek进度条
    private SeekBar mSmallSeekbar;
    //整个view的显示控制：
    //不显示的原因。如果是错误的，那么view就都不显示了。
    private HideType mHideType = null;
    private FrameLayout seekbarLayout;
    //各种监听
    //进度拖动监听
    private OnSeekListener mOnSeekListener;
    //标题返回按钮监听
    private OnBackClickListener mOnBackClickListener;
    //播放按钮点击监听
    private OnPlayStateClickListener mOnPlayStateClickListener;
    //锁屏按钮点击监听
    private OnScreenLockClickListener mOnScreenLockClickListener;
    //大小屏按钮点击监听
    private OnScreenModeClickListener mOnScreenModeClickListener;
    //PPT切换按钮点击监听
    private OnPPTClickListener mOnPPTClickListener;
    private HideHandler mHideHandler = new HideHandler(this);
    private String titleString = "";
    private int duration = -1;

    public MyControlView(Context context) {
        super(context);
        init();
    }

    public MyControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setmPlayType(PlayType mPlayType) {
        //直播还是回放
        //直播状态下不显示进度条
        if (mPlayType == PlayType.Play) {
            seekbarLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void setTitleString(String titleString) {
        this.titleString = titleString;
        updateTitleView();
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setmOnPPTClickListener(OnPPTClickListener mOnPPTClickListener) {
        this.mOnPPTClickListener = mOnPPTClickListener;
    }

    public void setOnPlayStateClickListener(OnPlayStateClickListener onPlayStateClickListener) {
        mOnPlayStateClickListener = onPlayStateClickListener;
    }

    public void setOnSeekListener(OnSeekListener onSeekListener) {
        mOnSeekListener = onSeekListener;
    }

    public void setOnScreenLockClickListener(OnScreenLockClickListener l) {
        mOnScreenLockClickListener = l;
    }

    public void setOnScreenModeClickListener(OnScreenModeClickListener l) {
        mOnScreenModeClickListener = l;
    }

    public void setOnBackClickListener(OnBackClickListener l) {
        mOnBackClickListener = l;
    }

    private void init() {
        //Inflate布局
        LayoutInflater.from(getContext()).inflate(R.layout.my_view_control, this, true);
        findAllViews();//找到所有的view
        setViewListener();//设置view的监听事件
        updateAllViews(); //更新view的显示
    }

    private void findAllViews() {
        mTitleBar = findViewById(R.id.titlebar);
        mControlBar = findViewById(R.id.controlbar);
        mTitlebarBackBtn = findViewById(R.id.alivc_title_back);
        mTitlebarText = findViewById(R.id.alivc_title_title);
        mScreenModeBtn = findViewById(R.id.alivc_screen_mode);
        change_ppt_video = findViewById(R.id.change_ppt_video);
        mScreenLockBtn = findViewById(R.id.alivc_screen_lock);
        mPlayStateBtn = findViewById(R.id.alivc_player_state);

        mLargeInfoBar = findViewById(R.id.alivc_info_large_bar);
        mLargePositionText = findViewById(R.id.alivc_info_large_position);
        mLargeDurationText = findViewById(R.id.alivc_info_large_duration);
        mLargeSeekbar = findViewById(R.id.alivc_info_large_seekbar);

        mSmallInfoBar = findViewById(R.id.alivc_info_small_bar);
        mSmallPositionText = findViewById(R.id.alivc_info_small_position);
        mSmallDurationText = findViewById(R.id.alivc_info_small_duration);
        mSmallSeekbar = findViewById(R.id.alivc_info_small_seekbar);
        seekbarLayout = findViewById(R.id.seekbarLayout);
    }

    private void setViewListener() {
        //PPT切换
        change_ppt_video.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPPTClickListener != null) {
                    mOnPPTClickListener.onClick();
                }
            }
        });
        //标题的返回按钮监听
        mTitlebarBackBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBackClickListener != null) {
                    mOnBackClickListener.onClick();
                }
            }
        });
        //控制栏的播放按钮监听
        mPlayStateBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPlayStateClickListener != null) {
                    mOnPlayStateClickListener.onPlayStateClick();
                }
            }
        });
        //锁屏按钮监听
        mScreenLockBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnScreenLockClickListener != null) {
                    mOnScreenLockClickListener.onClick();
                }
            }
        });
        //大小屏按钮监听
        mScreenModeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnScreenModeClickListener != null) {
                    mOnScreenModeClickListener.onClick();
                }
            }
        });
        //seekbar的滑动监听
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //这里是用户拖动，直接设置文字进度就行，
                    // 无需去updateAllViews() ， 因为不影响其他的界面。
                    if (mAliyunScreenMode == AliyunScreenMode.Full) {
                        //全屏状态.
                        mLargePositionText.setText(TimeFormater.formatMs(progress));
                    } else if (mAliyunScreenMode == AliyunScreenMode.Small) {
                        //小屏状态
                        mSmallPositionText.setText(TimeFormater.formatMs(progress));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarTouching = true;
                if (mOnSeekListener != null) {
                    mOnSeekListener.onSeekStart();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (mOnSeekListener != null) {
                    mOnSeekListener.onSeekEnd(seekBar.getProgress());
                }

                isSeekbarTouching = false;
            }
        };
        //seekbar的滑动监听
        mLargeSeekbar.setOnSeekBarChangeListener(seekBarChangeListener);
        mSmallSeekbar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    /**
     * 设置是否显示标题栏。
     *
     * @param show false:不显示
     */
    public void setTitleBarCanShow(boolean show) {
        mTitleBarCanShow = show;
        updateAllTitleBar();
    }

    /**
     * 设置是否显示控制栏
     *
     * @param show fase：不显示
     */
    public void setControlBarCanShow(boolean show) {
        mControlBarCanShow = show;
        updateAllControlBar();
    }

    /**
     * 设置当前屏幕模式：全屏还是小屏
     */
    @Override
    public void setScreenModeStatus(AliyunScreenMode mode) {
        mAliyunScreenMode = mode;
        updateLargeInfoBar();
        updateSmallInfoBar();
        updateScreenLockBtn();
        updateScreenModeBtn();
    }

    /**
     * 设置当前的播放状态
     *
     * @param playState 播放状态
     */
    public void setPlayState(PlayState playState) {
        mPlayState = playState;
        updatePlayStateBtn();
        updateSmallInfoBar();
        updateLargeInfoBar();
    }

    public void setPPTState(PPTState pptState) {
        mPPTState = pptState;
        updatePPTBtn();
        updateSmallInfoBar();
        updateLargeInfoBar();
    }

//    /**
//     * 设置视频信息
//     *
//     * @param aliyunMediaInfo 媒体信息
//     * @param currentQuality  当前清晰度
//     */
//    public void setMediaInfo(AliyunMediaInfo aliyunMediaInfo, String currentQuality) {
//        mAliyunMediaInfo = aliyunMediaInfo;
//        mCurrentQuality = currentQuality;
//        updateLargeInfoBar();
//    }

    /**
     * 是否锁屏。锁住的话，其他的操作界面将不会显示。
     *
     * @param screenLocked true：锁屏
     */
    public void setScreenLockStatus(boolean screenLocked) {
        mScreenLocked = screenLocked;
        updateScreenLockBtn();
        updateAllTitleBar();
        updateAllControlBar();
    }

    /**
     * 获取视频进度
     *
     * @return 视频进度
     */
    public int getVideoPosition() {
        return mVideoPosition;
    }

    /**
     * 更新视频进度
     *
     * @param position 位置，ms
     */
    public void setVideoPosition(int position) {
        mVideoPosition = position;
        updateSmallInfoBar();
        updateLargeInfoBar();
    }

    private void updateAllViews() {
        updateTitleView();//更新标题信息，文字
        updateScreenLockBtn();//更新锁屏状态
        updatePlayStateBtn();//更新播放状态
        updateLargeInfoBar();//更新大屏的显示信息
        updateSmallInfoBar();//更新小屏的显示信息
        updateScreenModeBtn();//更新大小屏信息
        updateAllTitleBar(); //更新标题显示
        updateAllControlBar();//更新控制栏显示
    }

    /**
     * 更新标题栏的标题文字
     */
    private void updateTitleView() {
        if (TextUtils.isEmpty(titleString)) {
            mTitlebarText.setText("");
        } else {
            mTitlebarText.setText(titleString);
        }
    }

    /**
     * 更新锁屏按钮的信息
     */
    private void updateScreenLockBtn() {
        if (mScreenLocked) {
            mScreenLockBtn.setImageResource(R.drawable.alivc_screen_lock);
        } else {
            mScreenLockBtn.setImageResource(R.drawable.alivc_screen_unlock);
        }

        if (mAliyunScreenMode == AliyunScreenMode.Full) {
            mScreenLockBtn.setVisibility(VISIBLE);
        } else {
            mScreenLockBtn.setVisibility(GONE);
        }
    }

    /**
     * 更新播放按钮的状态
     */
    private void updatePlayStateBtn() {
        if (mPlayState == PlayState.Paused || mPlayState == PlayState.Idle) {
            mPlayStateBtn.setImageResource(R.drawable.vhall_icon_live_play);
        } else if (mPlayState == PlayState.Playing) {
            mPlayStateBtn.setImageResource(R.drawable.vhall_icon_live_pause);
        }
    }

    /**
     * 监听view是否可见。从而实现5秒隐藏的功能
     *
     * @param changedView
     * @param visibility
     */
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            //如果变为可见了。启动五秒隐藏。
            hideDelayed();
        }
    }

    private void hideDelayed() {
        mHideHandler.removeMessages(WHAT_HIDE);
        mHideHandler.sendEmptyMessageDelayed(WHAT_HIDE, DELAY_TIME);
    }

    /**
     * 更新大屏下的控制条信息
     */
    private void updateLargeInfoBar() {
        if (mAliyunScreenMode == AliyunScreenMode.Small) {
            //里面包含了很多按钮，比如切换清晰度的按钮之类的
            mLargeInfoBar.setVisibility(INVISIBLE);
        } else if (mAliyunScreenMode == AliyunScreenMode.Full) {

            //先更新大屏的info数据
            if (duration != -1) {
                mLargeDurationText.setText("/" + TimeFormater.formatMs(duration));
                mLargeSeekbar.setMax((int) duration);
            } else {
                mLargeDurationText.setText("/" + TimeFormater.formatMs(0));
                mLargeSeekbar.setMax(0);
            }

            if (isSeekbarTouching) {
                //用户拖动的时候，不去更新进度值，防止跳动。
            } else {
//                mLargeSeekbar.setSecondaryProgress(mVideoBufferPosition);
                mLargeSeekbar.setProgress(mVideoPosition);
                mLargePositionText.setText(TimeFormater.formatMs(mVideoPosition));
            }
//            mLargeChangeQualityBtn.setText(QualityItem.getItem(getContext(), mCurrentQuality, isMtsSource).getName());
//            if (TextUtils.isEmpty(mCurrentQuality)) {
//                return;
//            } else {
//                mLargeChangeQualityBtn.setText(MAPVALUE.get(mCurrentQuality).toString());
//            }
//            Log.i(TAG, "mCurrentQuality = " + mCurrentQuality + " , isMts Source = " + isMtsSource + " , mForceQuality = " + mForceQuality);

            //然后再显示出来。
            mLargeInfoBar.setVisibility(VISIBLE);
        }

        if (mPlayState == PlayState.Idle) {
            mLargeSeekbar.setEnabled(false);
        } else {
            mLargeSeekbar.setEnabled(true);
        }
    }

    /**
     * 更新小屏下的控制条信息
     */
    private void updateSmallInfoBar() {
        if (mAliyunScreenMode == AliyunScreenMode.Full) {
            mSmallInfoBar.setVisibility(INVISIBLE);
        } else if (mAliyunScreenMode == AliyunScreenMode.Small) {
            //先设置小屏的info数据
            if (duration != -1) {
                mSmallDurationText.setText(TimeFormater.formatMs(duration));
                mSmallSeekbar.setMax((int) duration);
            } else {
                mSmallDurationText.setText(TimeFormater.formatMs(0));
                mSmallSeekbar.setMax(0);
            }

            if (isSeekbarTouching) {
                //用户拖动的时候，不去更新进度值，防止跳动。
            } else {
//                mSmallSeekbar.setSecondaryProgress(mVideoBufferPosition);
                mSmallSeekbar.setProgress(mVideoPosition);
                mSmallPositionText.setText(TimeFormater.formatMs(mVideoPosition));
            }
            //然后再显示出来。
            mSmallInfoBar.setVisibility(VISIBLE);
        }

        if (mPlayState == PlayState.Idle) {
            mSmallSeekbar.setEnabled(false);
        } else {
            mSmallSeekbar.setEnabled(true);
        }
    }

    /**
     * 更新切换大小屏按钮的信息
     */
    private void updateScreenModeBtn() {
        if (mAliyunScreenMode == AliyunScreenMode.Full) {
            mScreenModeBtn.setImageResource(R.drawable.icon_round_smallscreen);
        } else {
            mScreenModeBtn.setImageResource(R.drawable.icon_round_fullscreen);
        }
    }

    private void updatePPTBtn() {
        if (mPPTState == PPTState.Top) {
            change_ppt_video.setImageResource(R.drawable.live_ppt);
        } else if (mPPTState == PPTState.Bottom) {
            change_ppt_video.setImageResource(R.drawable.live_live);
        }
//        if (mPlayState == PlayState.Paused || mPlayState == PlayState.Idle) {
//            mPlayStateBtn.setImageResource(R.drawable.vhall_icon_live_play);
//        } else if (mPlayState == PlayState.Playing) {
//            mPlayStateBtn.setImageResource(R.drawable.vhall_icon_live_pause);
//        }
    }

    /**
     * 更新标题栏的显示
     */
    private void updateAllTitleBar() {
        //单独设置可以显示，并且没有锁屏的时候才可以显示
        boolean canShow = mTitleBarCanShow && !mScreenLocked;
        if (mTitleBar != null) {
            mTitleBar.setVisibility(canShow ? VISIBLE : INVISIBLE);
        }
    }

    /**
     * 更新控制条的显示
     */
    private void updateAllControlBar() {
        //单独设置可以显示，并且没有锁屏的时候才可以显示
        boolean canShow = mControlBarCanShow && !mScreenLocked;
        if (mControlBar != null) {
            mControlBar.setVisibility(canShow ? VISIBLE : INVISIBLE);
        }
    }

    @Override
    public void reset() {
        mHideType = null;
        mVideoPosition = 0;
        duration = -1;
        mPlayState = PlayState.Idle;
        isSeekbarTouching = false;
        updateAllViews();
    }

    @Override
    public void show() {
        if (mHideType == HideType.End) {
            //如果是由于错误引起的隐藏，那就不能再展现了
            setVisibility(GONE);
        } else {
            updateAllViews();
            setVisibility(VISIBLE);
        }
    }

    @Override
    public void hide(ViewAction.HideType hideType) {
        if (mHideType != HideType.End) {
            mHideType = hideType;
        }
        setVisibility(GONE);
    }

    /**
     * 设置主题色
     *
     * @param theme 支持的主题
     */
    @Override
    public void setTheme(AliyunVodPlayerView.Theme theme) {
        updateSeekBarTheme(theme);
    }

    /**
     * 更新当前主题色
     *
     * @param theme 设置的主题色
     */
    private void updateSeekBarTheme(AliyunVodPlayerView.Theme theme) {
        //获取不同主题的图片
        int progressDrawableResId = R.drawable.alivc_info_seekbar_bg_blue;
        int thumbResId = R.drawable.alivc_info_seekbar_thumb_blue;
        if (theme == AliyunVodPlayerView.Theme.Blue) {
            progressDrawableResId = (R.drawable.alivc_info_seekbar_bg_blue);
            thumbResId = (R.drawable.alivc_info_seekbar_thumb_blue);
        } else if (theme == AliyunVodPlayerView.Theme.Green) {
            progressDrawableResId = (R.drawable.alivc_info_seekbar_bg_green);
            thumbResId = (R.drawable.alivc_info_seekbar_thumb_green);
        } else if (theme == AliyunVodPlayerView.Theme.Orange) {
            progressDrawableResId = (R.drawable.alivc_info_seekbar_bg_orange);
            thumbResId = (R.drawable.alivc_info_seekbar_thumb_orange);
        } else if (theme == AliyunVodPlayerView.Theme.Red) {
            progressDrawableResId = (R.drawable.alivc_info_seekbar_bg_red);
            thumbResId = (R.drawable.alivc_info_seekbar_thumb_red);
        }

        //这个很有意思。。哈哈。不同的seekbar不能用同一个drawable，不然会出问题。
        // https://stackoverflow.com/questions/12579910/seekbar-thumb-position-not-equals-progress

        //设置到对应控件中
        Resources resources = getResources();
        Drawable smallProgressDrawable = resources.getDrawable(progressDrawableResId);
        Drawable smallThumb = resources.getDrawable(thumbResId);
        mSmallSeekbar.setProgressDrawable(smallProgressDrawable);
        mSmallSeekbar.setThumb(smallThumb);

        Drawable largeProgressDrawable = resources.getDrawable(progressDrawableResId);
        Drawable largeThumb = resources.getDrawable(thumbResId);
        mLargeSeekbar.setProgressDrawable(largeProgressDrawable);
        mLargeSeekbar.setThumb(largeThumb);
    }

    public static enum PlayState {
        Playing, Paused, Idle
    }

    public static enum PPTState {
        Top, Bottom
    }

    public static enum PlayType {
        PlayBack, Play
    }

    public interface OnScreenLockClickListener {
        /**
         * 锁屏按钮点击事件
         */
        void onClick();
    }

    public interface OnScreenModeClickListener {
        /**
         * 大小屏按钮点击事件
         */
        void onClick();
    }

    public interface OnPPTClickListener {
        /**
         * PPT切换按钮点击事件
         */
        void onClick();
    }

    public interface OnBackClickListener {
        /**
         * 返回按钮点击事件
         */
        void onClick();
    }

    public interface OnSeekListener {
        /**
         * seek结束事件
         */
        void onSeekEnd(int position);

        /**
         * seek开始事件
         */
        void onSeekStart();
    }

    public interface OnPlayStateClickListener {
        /**
         * 播放按钮点击事件
         */
        void onPlayStateClick();
    }

    /**
     * 隐藏类
     */
    private static class HideHandler extends Handler {
        private WeakReference<MyControlView> controlViewWeakReference;

        public HideHandler(MyControlView myControlView) {
            controlViewWeakReference = new WeakReference<>(myControlView);
        }

        @Override
        public void handleMessage(Message msg) {
            MyControlView controlView = controlViewWeakReference.get();
            if (controlView != null) {
                controlView.hide(HideType.Normal);
            }
            super.handleMessage(msg);
        }
    }
}
