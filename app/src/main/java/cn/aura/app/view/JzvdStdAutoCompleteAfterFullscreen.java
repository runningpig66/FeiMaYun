package cn.aura.app.view;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.WindowManager;

import cn.jzvd.JZMediaManager;
import cn.jzvd.JZUtils;
import cn.jzvd.JzvdStd;

public class JzvdStdAutoCompleteAfterFullscreen extends JzvdStd {

    public JzvdStdAutoCompleteAfterFullscreen(Context context) {
        super(context);
    }

    public JzvdStdAutoCompleteAfterFullscreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void startVideo() {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
//            Log.d(TAG, "startVideo [" + this.hashCode() + "] ");
            initTextureView();
            addTextureView();
            AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            JZUtils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            JZMediaManager.setDataSource(jzDataSource);
            JZMediaManager.instance().positionInList = positionInList;
            onStatePreparing();
        } else {
            super.startVideo();
        }
    }

    @Override
    public void onAutoCompletion() {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            onStateAutoComplete();
        } else {
            super.onAutoCompletion();
        }

    }
}
