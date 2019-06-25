package cn.aura.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cn.aura.app.interfaces.NetStateChangeObserver;

public class NetStateChangeReceiver extends BroadcastReceiver {

    private List<NetStateChangeObserver> mObservers = new ArrayList<>();

    public NetStateChangeReceiver() {

    }

    /**
     * 注册网络监听
     *
     * @param context
     */
    public static void registerReceiver(@NonNull Context context) {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(InstanceHolder.INSTANCE, intentFilter);
    }

    /**
     * 取消网络监听
     *
     * @param context
     */
    public static void unregisterReceiver(@NonNull Context context) {
        context.unregisterReceiver(InstanceHolder.INSTANCE);
    }

    /**
     * 注册网络变化Observer
     */
    public static void registerObserver(NetStateChangeObserver observer) {
        if (observer == null) {
            return;
        }
        if (!InstanceHolder.INSTANCE.mObservers.contains(observer)) {
            InstanceHolder.INSTANCE.mObservers.add(observer);
        }
    }

    /**
     * 取消网络变化Observer的注册
     */
    public static void unregisterObserver(NetStateChangeObserver observer) {
        if (observer == null) {
            return;
        }
        InstanceHolder.INSTANCE.mObservers.remove(observer);
    }

    /**
     * 注册网络监听
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        notifyObservers(networkInfo);
    }

    private void notifyObservers(NetworkInfo networkInfo) {
        for (NetStateChangeObserver observer : mObservers) {
            if (networkInfo != null && networkInfo.isConnected()) {
                observer.onNetConnected(networkInfo);
            } else {
                observer.onNetDisconnected();
            }
        }

    }

    private static class InstanceHolder {
        private static final NetStateChangeReceiver INSTANCE = new NetStateChangeReceiver();
    }

}
