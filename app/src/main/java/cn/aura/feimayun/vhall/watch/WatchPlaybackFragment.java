package cn.aura.feimayun.vhall.watch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.WatchLive;
import com.vhall.business.widget.ContainerLayout;

import java.util.List;

import cn.aura.feimayun.R;


/**
 * 观看回放的Fragment HAVE DONE
 */
public class WatchPlaybackFragment extends Fragment implements WatchContract.PlaybackView, View.OnClickListener {

    WatchContract.PlaybackPresenter mPresenter;
    ContainerLayout rl_video_container;//视频区容器
    ImageView clickStart, btn_changescaletype;
    SeekBar seekbar;
    TextView tv_current_time, tv_end_time;
    ProgressBar pb;
    //    ImageView iv_dlna_playback;
//    RadioGroup rg_quality;
    WatchActivity mContext;
    ImageView clickOrientation;
    ImageView click_ppt_live;
    //用AudioManager获取音频焦点避免音视频声音并发问题
    AudioManager mAudioManager;
    AudioFocusRequest mFocusRequest;
    AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private ImageView image_action_back;
    private LinearLayout playback_layout1;
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

    public static WatchPlaybackFragment newInstance() {
//        WatchPlaybackFragment articleFragment = new WatchPlaybackFragment();
        return new WatchPlaybackFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (WatchActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vhall_watch_playback_fragment, container, false);
        rl_video_container = view.findViewById(R.id.rl_video_container);
        btn_changescaletype = view.findViewById(R.id.btn_change_scale_type);
        pb = view.findViewById(R.id.pb);
        playback_layout1 = view.findViewById(R.id.playback_layout1);
        clickStart = view.findViewById(R.id.click_rtmp_watch);
        seekbar = view.findViewById(R.id.seekbar);
        tv_current_time = view.findViewById(R.id.tv_current_time);
        tv_end_time = view.findViewById(R.id.tv_end_time);
        image_action_back = view.findViewById(R.id.image_action_back);//左上角返回键
        //全屏按钮
        clickOrientation = view.findViewById(R.id.click_rtmp_orientation);
        //切换PPT和视频的按钮
        click_ppt_live = view.findViewById(R.id.click_ppt_live);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        iv_dlna_playback = (ImageView) getView().findViewById(R.id.iv_dlna_playback);
//        rg_quality = getView().findViewById(R.id.rg_quality);
        btn_changescaletype.setOnClickListener(this);
//        iv_dlna_playback.setOnClickListener(this);
        clickStart.setOnClickListener(this);
        clickOrientation.setOnClickListener(this);
        image_action_back.setOnClickListener(this);
        click_ppt_live.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPresenter.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.onStopTrackingTouch(seekBar);
//                Log.e(TAG, "onStopTrackingTouch == " + seekBar.getProgress());
            }
        });
    }

    @Override
    public void setPresenter(WatchContract.PlaybackPresenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    @Override
    public void setPlayIcon(boolean isStop) {
        if (isStop) {
            clickStart.setImageResource(R.drawable.vhall_icon_live_play);
        } else {
            clickStart.setImageResource(R.drawable.vhall_icon_live_pause);
        }
    }

    @Override
    public void setProgressLabel(String currentTime, String max) {
        tv_current_time.setText(currentTime);
        tv_end_time.setText(max);
    }

    @Override
    public void setSeekbarMax(int max) {
        seekbar.setMax(max);
    }

    @Override
    public void setSeekbarCurrentPosition(int position) {
        seekbar.setProgress(position);
    }

    @Override
    public void showProgressbar(boolean show) {
        if (show)
            pb.setVisibility(View.VISIBLE);
        else
            pb.setVisibility(View.GONE);
    }

    @Override
    public ContainerLayout getContainer() {
        return rl_video_container;
    }

    @Override
    public void setScaleTypeText(int text) {
        switch (text) {
            case WatchLive.FIT_DEFAULT:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_default));
                break;
            case WatchLive.FIT_CENTER_INSIDE:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_center));
                break;
            case WatchLive.FIT_X:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_x));
                break;
            case WatchLive.FIT_Y:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_y));
                break;
            case WatchLive.FIT_XY:
                btn_changescaletype.setBackground(getResources().getDrawable(R.drawable.fit_xy));
                break;
        }
    }

    @Override
    public void setQuality(List<String> qualities) {
        if (qualities != null && qualities.size() > 0) {
            for (int i = 0; i < qualities.size(); i++) {
                RadioButton button = new RadioButton(mContext);
                button.setText(qualities.get(i));
//                rg_quality.addView(button);
            }
//            rg_quality.setOnCheckedChangeListener((group, checkedId) -> {
//                RadioButton rb = group.findViewById(checkedId);
//                String text = rb.getText().toString();
//                mPresenter.onSwitchPixel(text);
//            });

//            rg_quality.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(RadioGroup group, int checkedId) {
//                    RadioButton rb = group.findViewById(checkedId);
//                    String text = rb.getText().toString();
//                    mPresenter.onSwitchPixel(text);
//                }
//            });

        }
    }

    @Override
    public void setQualityChecked(String dpi) {
//        int count = rg_quality.getChildCount();
//        if (TextUtils.isEmpty(dpi) || count <= 0)
//            return;
//        for (int i = 0; i < count; i++) {
//            RadioButton rb = (RadioButton) rg_quality.getChildAt(i);
//            if (rb.getText().equals(dpi))
//                rb.setChecked(true);
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isVisible = false;
//        mPresenter.onFragmentStop();
        //mPresenter.startPlay();
    }

    @Override
    public void onStart() {
        super.onStart();
        isVisible = true;
        if (requestTheAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mPresenter.startPlay();
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
            mPresenter.start();
            mPresenter.onResume();
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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.image_action_back) {//返回
            mContext.onBackPressed();
//            mPresenter.onFragmentDestory();
//            ((Activity) mContext).finish();
        } else if (i == R.id.click_rtmp_watch) {//播放
            mPresenter.onPlayClick();
        } else if (i == R.id.click_rtmp_orientation) {
            mPresenter.changeScreenOri();
        } else if (i == R.id.btn_change_scale_type) {
            mPresenter.changeScaleType();
        } else if (i == R.id.click_ppt_live) {
            WatchActivity watchActivity = (WatchActivity) getActivity();
            if (watchActivity != null) {
                watchActivity.setPlace();
            } else {
                Toast.makeText(getContext(), "程序异常,请重新打开界面", Toast.LENGTH_SHORT).show();
            }
        }

//        else if (i == R.id.iv_dlna_playback) {
//            // Todo 投屏相关
//            // mPresenter.showDevices();
//        }

    }

    public void setVisiable(boolean canSee) {
        if (canSee) {
            image_action_back.setVisibility(View.VISIBLE);
            playback_layout1.setVisibility(View.VISIBLE);
        } else {
            image_action_back.setVisibility(View.GONE);
            playback_layout1.setVisibility(View.GONE);
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