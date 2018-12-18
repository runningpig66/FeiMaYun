package cn.aura.feimayun.interfaces;

import android.net.NetworkInfo;

public interface NetStateChangeObserver {
    void onNetDisconnected();

    void onNetConnected(NetworkInfo networkInfo);
}
