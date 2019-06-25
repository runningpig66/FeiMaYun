package cn.aura.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * 描述：屏幕开屏/锁屏监听工具类
 */

public class ScreenStatusController {

    private Context mContext;
    private IntentFilter mScreenStatusFilter;
    private ScreenStatusListener mScreenStatusListener = null;
    private BroadcastReceiver mScreenStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
//                Log.d(TAG, "ACTION_SCREEN_ON");
                if (mScreenStatusListener != null) {
                    mScreenStatusListener.onScreenOn();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
//                Log.d(TAG, "ACTION_SCREEN_OFF");
                if (mScreenStatusListener != null) {
                    mScreenStatusListener.onScreenOff();
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁

            }
        }
    };

    public ScreenStatusController(Context context) {
        mContext = context;

        mScreenStatusFilter = new IntentFilter();
        mScreenStatusFilter.addAction(Intent.ACTION_SCREEN_ON);
        mScreenStatusFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenStatusFilter.addAction(Intent.ACTION_USER_PRESENT);
    }

    //设置监听
    public void setScreenStatusListener(ScreenStatusListener screenStatusListener) {
        mScreenStatusListener = screenStatusListener;
    }

    //开始监听
    public void startListen() {
        if (mContext != null) {
            mContext.registerReceiver(mScreenStatusReceiver, mScreenStatusFilter);
        }
    }

    //结束监听
    public void stopListen() {
        if (mContext != null) {
            mContext.unregisterReceiver(mScreenStatusReceiver);
        }
    }

    //监听事件
    public interface ScreenStatusListener {
        //开屏时调用
        void onScreenOn();

        //锁屏时调用
        void onScreenOff();
    }
}