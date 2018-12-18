package cn.aura.feimayun.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import cn.aura.feimayun.application.MyApplication;

/**
 * 软件flash页面
 */
public class FlashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.APP_STATUS = MyApplication.APP_STATUS_NORMAL;//App正常启动状态设置
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_flash);

//        TextView version_text = findViewById(R.id.version_text);
//
//        PackageManager manager = getPackageManager();
//        PackageInfo info = null;
//
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
