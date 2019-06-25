package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

/**
 * 软件flash页面
 */
public class FlashActivity extends AppCompatActivity {
    public static Handler handleIndexStart;

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleIndexStart = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.obj.toString().equals("网络异常")) {
                } else {
//                    Util.d("062002", msg.obj.toString());
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.APP_STATUS = MyApplication.APP_STATUS_NORMAL;//App正常启动状态设置
        super.onCreate(savedInstanceState);
        handler();
        String uid = Util.getUid();
        if (!uid.isEmpty()) {
            //登录成功后开始请求个人课程列表
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("uid", uid);
            RequestURL.sendPOST("https://app.feimayun.com/Index/start", handleIndexStart, paramsMap);
        }

//        setContentView(R.layout.activity_flash);
//        TextView version_text = findViewById(R.id.version_text);
//
//        PackageManager manager = getPackageManager();
//        PackageInfo info = null;

//        try {
//            info = manager.getPackageInfo(getPackageName(), 0);
//            String versionCodeLocal = info.versionName;
//            version_text.setText("Version: " + versionCodeLocal);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }

        //延迟2s跳转
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(FlashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 900);
    }

}
