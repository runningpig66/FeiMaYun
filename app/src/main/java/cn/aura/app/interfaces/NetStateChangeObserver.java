package cn.aura.app.interfaces;

import android.net.NetworkInfo;

public interface NetStateChangeObserver {
    void onNetDisconnected();

    void onNetConnected(NetworkInfo networkInfo);
}
