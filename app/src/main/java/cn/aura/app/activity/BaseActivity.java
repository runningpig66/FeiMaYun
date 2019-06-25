package cn.aura.app.activity;

import android.annotation.SuppressLint;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import cn.aura.app.R;
import cn.aura.app.application.MyApplication;
import cn.aura.app.interfaces.NetStateChangeObserver;
import cn.aura.app.util.NetStateChangeReceiver;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements NetStateChangeObserver {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MyApplication.APP_STATUS != MyApplication.APP_STATUS_NORMAL) {//非正常启动，直接重新初始化应用界面
            MyApplication.reInitApp();
            finish();
        } else {
            //知晓当前页面属于哪个活动
            Log.d("BaseActivity", getClass().getSimpleName());

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);

            //安卓4.4
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {//安卓4.4以上状态栏25%透明度黑色
                tintManager.setStatusBarTintResource(R.color.status_color_alpha);//状态背景色
            } else {//安卓4.4以上
                tintManager.setStatusBarAlpha(0);
                tintManager.setStatusBarTintColor(0x000000);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (needRegisterNetworkChangeObserver()) {
            NetStateChangeReceiver.registerObserver(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (needRegisterNetworkChangeObserver()) {
            NetStateChangeReceiver.unregisterObserver(this);
        }
    }

    /**
     * 是否需要注册网络变化的Observer
     *
     * @return true：需要监听，false：不需要监听
     */
    protected boolean needRegisterNetworkChangeObserver() {
        return false;
    }

    @Override
    public void onNetDisconnected() {

    }

    @Override
    public void onNetConnected(NetworkInfo networkInfo) {

    }
}
