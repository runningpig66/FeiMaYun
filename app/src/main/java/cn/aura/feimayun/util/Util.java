package cn.aura.feimayun.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.aura.feimayun.application.MyApplication;

import static android.content.Context.MODE_PRIVATE;

public class Util {

    /**
     * 正则表达式：验证密码
     */
    private static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,16}$";
    /**
     * 正则表达式：验证手机号
     */
    private static final String REGEX_TEL = "^((13[0-9])|(14[0-9])|(15[0-9])|(16[0-9])|(17[0-9])|(18[0-9])|(19[0-9]))\\d{8}$";//

    public static void getDensity(Activity activity) {
        // 获取屏幕密度（方法2）
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();

        float density = dm.density;        // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        int densityDPI = dm.densityDpi;        // 屏幕密度（每寸像素：120/160/240/320）
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;

        int screenWidth = dm.widthPixels;        // 屏幕宽（像素，如：480px）
        int screenHeight = dm.heightPixels;        // 屏幕高（像素，如：800px）
    }

    /**
     * 截断输出日志
     *
     * @param msg
     */
    public static void d(String tag, String msg) {
        if (tag == null || tag.length() == 0
                || msg == null || msg.length() == 0)
            return;

        int segmentSize = 3 * 1024;
        long length = msg.length();
        if (length <= segmentSize) {// 长度小于等于限制直接打印
            Log.e(tag, msg);
        } else {
            while (msg.length() > segmentSize) {// 循环分段打印日志
                String logContent = msg.substring(0, segmentSize);
                msg = msg.replace(logContent, "");
                Log.e(tag, logContent);
            }
            Log.e(tag, msg);// 打印剩余日志
        }
    }

    public static String getUid() {
        SharedPreferences sharedPreferences = MyApplication.context.getSharedPreferences("user_info", MODE_PRIVATE);
        return sharedPreferences.getString("apud", "");
    }

    //手机号验证
    public static boolean isMobile(String tel) {
        if (tel.matches(REGEX_TEL)) {

        } else {
            Toast.makeText(MyApplication.context, "手机号格式不正确", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //密码验证
    public static boolean isPassword(String pwd) {
        if (pwd.length() < 6 || pwd.length() > 16) {
            Toast.makeText(MyApplication.context, "密码长度为6-16位", Toast.LENGTH_SHORT).show();
            return false;
        }
//        if (pwd.matches(REGEX_PASSWORD)) {
//
//        } else {
//            Toast.makeText(MyApplication.context, "密码长度为6-16位不能包含特殊字符", Toast.LENGTH_SHORT).show();
//            return false;
//        }
        return true;
    }

    public static String getNewContent(String htmltext) {
        Document doc = Jsoup.parse(htmltext);
        Elements elements = doc.getElementsByTag("img");
        for (Element element : elements) {
            element.attr("width", "100%").attr("height", "auto");
        }
        return doc.toString();
    }

    public static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}
