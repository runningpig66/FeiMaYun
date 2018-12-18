package cn.aura.feimayun.vhall.watch;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vhall.business.widget.ContainerLayout;

import java.util.HashMap;

import cn.aura.feimayun.R;


/**
 * 观看直播的Fragment
 */
public class WatchLiveFragment extends Fragment implements WatchContract.LiveView, View.OnClickListener {

    ProgressBar progressbar;
    //用AudioManager获取音频焦点避免音视频声音并发问题
    AudioManager mAudioManager;
    AudioFocusRequest mFocusRequest;
    AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private WatchContract.LivePresenter mPresenter;
    private ImageView clickOrientation, clickStart;
    private ImageView click_ppt_live;//切换PPT和视频的按钮
    private ImageView image_action_back;
    private ContainerLayout mContainerLayout;
    private WatchActivity context;

    public static WatchLiveFragment newInstance() {
        return new WatchLiveFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.vhall_watch_live_fragment, container, false);
        initView(root);
//        reFreshView();
        return root;
    }

    private void initView(View root) {
        click_ppt_live = root.findViewById(R.id.click_ppt_live);
        click_ppt_live.setOnClickListener(this);
        clickStart = root.findViewById(R.id.click_rtmp_watch);
        clickStart.setOnClickListener(this);
        clickOrientation = root.findViewById(R.id.click_rtmp_orientation);
        clickOrientation.setOnClickListener(this);
        mContainerLayout = root.findViewById(R.id.rl_container);
        progressbar = root.findViewById(R.id.progressbar);
        image_action_back = root.findViewById(R.id.image_action_back);
        image_action_back.setOnClickListener(this);
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    @Override
    public ContainerLayout getWatchLayout() {
        return mContainerLayout;
    }

    @Override
    public void setPlayPicture(boolean state) {
        if (state) {
            clickStart.setBackgroundResource(R.drawable.vhall_icon_live_pause);
        } else {
            clickStart.setBackgroundResource(R.drawable.vhall_icon_live_play);
        }
    }

    @Override
    public void setDownSpeed(String text) {
//        fragmentDownloadSpeed.setText(text);
    }

    @Override
    public void showLoading(boolean isShow) {
        if (isShow)
            progressbar.setVisibility(View.VISIBLE);
        else
            progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.click_rtmp_watch) {//开始/暂停播放按钮
            mPresenter.onWatchBtnClick();
        } else if (i == R.id.click_rtmp_orientation) {//水平切换按钮
            mPresenter.changeOriention();
        } else if (i == R.id.image_action_back) {//左上角返回按钮
            context.onBackPressed();
        } else if (i == R.id.click_ppt_live) {//切换PPT
            WatchActivity watchActivity = (WatchActivity) getActivity();
            if (watchActivity != null) {
                watchActivity.setPlace();
            } else {
                Toast.makeText(getContext(), "程序异常,请重新打开界面", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 切换分辨率
     *
     * @param map 0 : 无效不可用  1 ：有效可用
     */
    @Override
    public void showRadioButton(HashMap map) {
//        if (map == null)
//            return;
//        Iterator iter = map.entrySet().iterator();
//        while (iter.hasNext()) {
//            Map.Entry entry = (Map.Entry) iter.next();
//            String key = (String) entry.getKey();
//            Integer value = (Integer) entry.getValue();
//            switch (key) {
//                case "A":
//                    if (value == 1)
//                        btnChangePlayStatus.setVisibility(View.VISIBLE);
//                    else
//                        btnChangePlayStatus.setVisibility(View.GONE);
//                    break;
//                case "SD":
//                    if (value == 1)
//                        radioButtonShowSD.setVisibility(View.VISIBLE);
//                    else
//                        radioButtonShowSD.setVisibility(View.GONE);
//                    break;
//                case "HD":
//                    if (value == 1)
//                        radioButtonShowHD.setVisibility(View.VISIBLE);
//                    else
//                        radioButtonShowHD.setVisibility(View.GONE);
//                    break;
//                case "UHD":
//                    if (value == 1)
//                        radioButtonShowUHD.setVisibility(View.VISIBLE);
//                    else
//                        radioButtonShowUHD.setVisibility(View.GONE);
//                    break;
//            }
//        }
    }

    @Override
    public void setScaleButtonText(int type) {

    }

    @Override
    public void addDanmu(String danmu) {
    }

    @Override
    public void reFreshView() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void onResume() {
        super.onResume();
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
            image_action_back.setVisibility(View.VISIBLE);
            click_ppt_live.setVisibility(View.VISIBLE);
            clickOrientation.setVisibility(View.VISIBLE);
            clickStart.setVisibility(View.VISIBLE);
        } else {
            image_action_back.setVisibility(View.GONE);
            click_ppt_live.setVisibility(View.GONE);
            clickOrientation.setVisibility(View.GONE);
            clickStart.setVisibility(View.GONE);
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

}