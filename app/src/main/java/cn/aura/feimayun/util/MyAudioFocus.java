package cn.aura.feimayun.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import cn.aura.feimayun.application.MyApplication;

/**
 * 为了不让XIAOMI手机在android7.0由于找不到AudioFocusRequest类闪退
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class MyAudioFocus {
    private static AudioFocusRequest mFocusRequest;

    //请求音频焦点 设置监听
    @TargetApi(Build.VERSION_CODES.O)
    public static int requestTheAudioFocus(AudioManager mAudioManager, AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) MyApplication.context.getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioManager != null) {
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
            return 0;
        }
    }

    //sdk26再使用时做判断
    @TargetApi(Build.VERSION_CODES.O)
    public static void releaseTheAudioFocusSDK26(AudioManager mAudioManager) {
        if (mAudioManager != null && mFocusRequest != null) {
            mAudioManager.abandonAudioFocusRequest(mFocusRequest);
        }
    }
}
