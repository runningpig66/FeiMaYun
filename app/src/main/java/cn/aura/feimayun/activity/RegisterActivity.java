package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

/**
 * 描述：注册界面
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    //接收验证码的handler
    private static Handler handleMsg;
    //请求注册的handler
    private static Handler handleRegister;
    //请输入手机号
    private EditText activity_register_editText1;
    //请输入验证码
    private EditText activity_register_editText2;
    //获取验证码
    private TextView activity_register_textView1;
    //请输入您的真实姓名
    private EditText activity_register_editText3;
    //请输入密码，6 - 16位密码
    private EditText activity_register_editText4;
    private CountDownTimer countDownTimer;

    {
        //获取验证码按钮计数60秒
        int mCount = 60;
        countDownTimer = new CountDownTimer(mCount * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                activity_register_textView1.setText("请稍候\n(" + millisUntilFinished / 1000 + "秒)");
            }

            @Override
            public void onFinish() {
                activity_register_textView1.setText("获取验证码");
                activity_register_textView1.setBackgroundResource(R.drawable.button_orange);
                activity_register_textView1.setClickable(true);
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            //返回按钮布局
            RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
            headtitle_layout.setOnClickListener(this);
            //标题
            TextView headtitle_textview = findViewById(R.id.headtitle_textview);
            headtitle_textview.setText("注册");

            activity_register_editText1 = findViewById(R.id.activity_register_editText1);
            activity_register_editText2 = findViewById(R.id.activity_register_editText2);
            activity_register_textView1 = findViewById(R.id.activity_register_textView1);
            activity_register_editText3 = findViewById(R.id.activity_register_editText3);
            activity_register_editText4 = findViewById(R.id.activity_register_editText4);
            //注册按钮布局
            LinearLayout activity_register_layout2 = findViewById(R.id.activity_register_layout2);
            //已有账号立即登录
            TextView activity_register_textView2 = findViewById(R.id.activity_register_textView2);

            activity_register_editText1.setOnClickListener(this);
            activity_register_editText2.setOnClickListener(this);
            activity_register_textView1.setOnClickListener(this);
            activity_register_editText3.setOnClickListener(this);
            activity_register_editText4.setOnClickListener(this);
            activity_register_layout2.setOnClickListener(this);
            activity_register_textView2.setOnClickListener(this);

            handler();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleMsg = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(RegisterActivity.this, "请检查网络连接_Error04", Toast.LENGTH_LONG).show();
//                    if (progressDialog != null) {
//                        progressDialog.dismiss();
//                    }
//                    activity_paper_list_refreshLayout.finishRefresh(false);
//                    activity_paper_list_refreshLayout.finishLoadMore(false);
                } else {
                    parseMsg(msg.obj.toString());
                }
            }
        };
        handleRegister = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(RegisterActivity.this, "请检查网络连接_Error05", Toast.LENGTH_LONG).show();
//                    if (progressDialog != null) {
//                        progressDialog.dismiss();
//                    }
//                    activity_paper_list_refreshLayout.finishRefresh(false);
//                    activity_paper_list_refreshLayout.finishLoadMore(false);
                } else {
                    parseRegister(msg.obj.toString());
                }

            }
        };
    }

    //解析请求注册返回的JSON
    private void parseRegister(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            int status = jsonObject.getInt("status");
            if (status == 1) {//注册成功
                //保存用户的apud和aptk，本地文件名user_info.xml
                //TODO 下面的代码辅助实现注册后直接登录，暂时注释
//                String apud = jsonObject.getString("apud");
//                String aptk = jsonObject.getString("aptk");
//                SharedPreferences.Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
//                editor.putString("apud", apud);
//                editor.putString("aptk", aptk);
//                editor.apply();
                finish();
            }
            String msgReturn = jsonObject.getString("msg");
            Toast.makeText(this, msgReturn, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //解析发送验证码返回的JSON
    private void parseMsg(String s) {
//        try {
//            JSONTokener jsonTokener = new JSONTokener(s);
//            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//            int status = jsonObject.getInt("status");
        //TODO 暂时不判断验证码status
//            if (status == 0) {
        activity_register_textView1.setClickable(false);
        activity_register_textView1.setBackgroundResource(R.drawable.button_gray);
        countDownTimer.start();
//            }
//            String msgReturn = jsonObject.getString("msg");
//            Toast.makeText(this, msgReturn, Toast.LENGTH_SHORT).show();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_register_textView2://已有账号立即登录
            case R.id.headtitle_layout://左上角的返回图标布局
                finish();
                break;
            case R.id.activity_register_editText1://请输入手机号

                break;
            case R.id.activity_register_editText2://请输入验证码

                break;
            case R.id.activity_register_textView1://获取验证码
                //首先获取输入框的手机号
                String phone0 = activity_register_editText1.getText().toString();
                if (phone0.equals("")) {
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                } else {
                    //判断手机号是否正确
                    if (Util.isMobile(phone0)) {
                        //发送验证码
                        Map<String, String> paramsMap = new HashMap<>();
                        paramsMap.put("phone", phone0);
                        RequestURL.sendPOST("https://app.feimayun.com/Login/msgSend", handleMsg, paramsMap, RegisterActivity.this);
                    }
                }
                break;
            case R.id.activity_register_editText3://请输入您的真实姓名

                break;
            case R.id.activity_register_editText4://请输入密码，6 - 16位密码

                break;
            case R.id.activity_register_layout2://注册按钮布局
                //获取各个输入框的内容
                String phone1 = activity_register_editText1.getText().toString();
                String msg1 = activity_register_editText2.getText().toString();
                String nickname1 = activity_register_editText3.getText().toString();
                String password1 = activity_register_editText4.getText().toString();
                if (phone1.equals("") || msg1.equals("") || nickname1.equals("") || password1.equals("")) {
                    Toast.makeText(this, "请完成输入注册信息", Toast.LENGTH_SHORT).show();
                } else {
                    if (Util.isMobile(phone1)) {
                        if (Util.isPassword(password1)) {
                            //发送注册请求
                            Map<String, String> paramsMap = new HashMap<>();
                            paramsMap.put("phone", phone1);
                            paramsMap.put("nick_name", nickname1);
                            paramsMap.put("code", msg1);
                            paramsMap.put("passwd", password1);
                            RequestURL.sendPOST("https://app.feimayun.com/Login/register", handleRegister, paramsMap, RegisterActivity.this);
                        }
                    }
                }
                break;
        }
    }
}
