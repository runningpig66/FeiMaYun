package cn.aura.feimayun.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.sdk.QbSdk;
import com.vhall.business.VhallSDK;
import com.vhall.vhalllive.pushlive.CameraFilterView;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.FlashActivity;
import cn.aura.feimayun.util.NetStateChangeReceiver;
import cn.aura.feimayun.vhall.Param;

/**
 * 描述：程序Application
 */
public class MyApplication extends Application {
    public final static int APP_STATUS_KILLED = 0;//表示应用是被杀死后再启动的
    public final static int APP_STATUS_NORMAL = 1;//表示应用是正常启动流程
    public static int APP_STATUS = APP_STATUS_KILLED;//记录App的启动状态
    public static Param param;
    public static Context context;

    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @NonNull
            @Override
            public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
                layout.setPrimaryColorsId(android.R.color.white);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @NonNull
            @Override
            public RefreshFooter createRefreshFooter(@NonNull Context context, @NonNull RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    public static void reInitApp() {
        Intent intent = new Intent(context, FlashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    //程序创建时调用
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        getParam();
//        VhallSDK.init(this, getResources().getString(R.string.vhall_app_key1), getResources().getString(R.string.vhall_app_secret_key1));
//        VhallSDK.setLogEnable(true);

        Album.initialize(AlbumConfig.newBuilder(this)
                .setAlbumLoader(new MediaLoader())
                .setLocale(Locale.getDefault())
                .build()
        );

        //注册BroadcastReceiver
        NetStateChangeReceiver.registerReceiver(this);

        //腾讯BUGLY
        String packageName = context.getPackageName();
        String processName = getProcessName(android.os.Process.myPid());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        CrashReport.initCrashReport(getApplicationContext(), "1694b47e8e", false, strategy);

        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }

    //程序终止时调用
    @Override
    public void onTerminate() {
        super.onTerminate();
        //取消BroadcastReceiver注册
        NetStateChangeReceiver.unregisterReceiver(this);
    }

    public Param getParam() {
        if (param == null) {
            param = new Param();
            SharedPreferences sp = this.getSharedPreferences("set", MODE_PRIVATE);

            param.broId = sp.getString("broid", "");
            param.broToken = sp.getString("brotoken", "");
            param.pixel_type = sp.getInt("pixeltype", CameraFilterView.TYPE_HDPI);
            param.videoBitrate = sp.getInt("videobitrate", 500);
            param.videoFrameRate = sp.getInt("videoframerate", 15);

            param.watchId = sp.getString("watchid", ""); //
            param.key = sp.getString("key", "");
            param.bufferSecond = sp.getInt("buffersecond", 6);
        }
        return param;
    }

    public static void setParam(Param mParam) {
        if (param == null)
            return;
        param = mParam;
        SharedPreferences sp = context.getSharedPreferences("set", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("broid", param.broId);
        editor.putString("brotoken", param.broToken);
        editor.putInt("pixeltype", param.pixel_type);
        editor.putInt("videobitrate", param.videoBitrate);
        editor.putInt("videoframerate", param.videoFrameRate);

        editor.putString("watchid", param.watchId);
        editor.putString("key", param.key);
        editor.putInt("buffersecond", param.bufferSecond);

        editor.commit();

    }

    class MediaLoader implements AlbumLoader {
        @Override
        public void load(ImageView imageView, AlbumFile albumFile) {
            load(imageView, albumFile.getPath());
        }

        @Override
        public void load(ImageView imageView, String url) {
            Glide.with(MyApplication.context)
                    .load(url)
                    .into(imageView);
        }
    }
}
