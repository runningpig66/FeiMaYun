package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
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
import cn.aura.feimayun.fragment.MessageCenterFragment;
import cn.aura.feimayun.fragment.MyStudiesFragment;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;

/**
 * 描述：登录页面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    //接收验证码的handler
    private static Handler handleMsg;
    //左上角的关闭图标布局
    RelativeLayout activity_login_layout0;
    private CountDownTimer countDownTimer;
    //铭文显示密码切换，默认不显示密码
    private boolean showPassword = false;
    //验证码/密码登录切换，默认是密码登录
    private boolean isPassword = true;
    //请输入您的手机号
    private EditText activity_login_editText1;
    //请输入您的密码
    private EditText activity_login_editText2;
    //明文显示密码眼睛图标
    private ImageView activity_login_imageView2;
    //验证码、密码登录切换按钮
    private TextView activity_login_textView1;
    //验证码登录之获取验证码
    private TextView activity_login_textview3;
    //需要上移的输入布局
    private RelativeLayout activity_login_layout4;
    //根布局
    private RelativeLayout root;
    //处理登录信息的handler
    private Handler handleLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            activity_login_layout0 = findViewById(R.id.activity_login_layout0);
            activity_login_editText1 = findViewById(R.id.activity_login_editText1);
            activity_login_editText2 = findViewById(R.id.activity_login_editText2);
            activity_login_imageView2 = findViewById(R.id.activity_login_imageView2);
            //登录按钮布局
            LinearLayout activity_login_layout3 = findViewById(R.id.activity_login_layout3);
            activity_login_textView1 = findViewById(R.id.activity_login_textView1);
            //立即注册
            TextView activity_login_textView2 = findViewById(R.id.activity_login_textView2);
            activity_login_textview3 = findViewById(R.id.activity_login_textview3);
            activity_login_layout4 = findViewById(R.id.activity_login_layout4);
            root = findViewById(R.id.root);

            activity_login_layout0.setOnClickListener(this);
            activity_login_editText1.setOnClickListener(this);
            activity_login_editText2.setOnClickListener(this);
            activity_login_imageView2.setOnClickListener(this);
            activity_login_layout3.setOnClickListener(this);
            activity_login_textView1.setOnClickListener(this);
            activity_login_textView2.setOnClickListener(this);
            activity_login_textview3.setOnClickListener(this);

            //获取验证码按钮计数60秒
            int mCount = 60;
            countDownTimer = new CountDownTimer(mCount * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    activity_login_textview3.setText("请稍候(" + millisUntilFinished / 1000 + "秒)");
                }

                @Override
                public void onFinish() {
                    activity_login_textview3.setText("获取验证码");
                    activity_login_textview3.setBackgroundResource(R.drawable.loginbutton_orange);
                    activity_login_textview3.setClickable(true);
                }
            };
            handler();
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            controlKeyboardLayout(root, activity_login_layout4);
        }
    }

    private void controlKeyboardLayout(final View root, final View scrollToView) {
        final int[] location = new int[2];
        // 获取scrollToView在窗体的坐标
        scrollToView.getLocationInWindow(location);

        //注册一个回调函数，当在一个视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变时调用这个回调函数
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                // 获取root在窗体的可视区域
                root.getWindowVisibleDisplayFrame(rect);
                // 当前视图最外层的高度减去现在所看到的视图的最底部的y坐标
                int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;//不可见高度

                if (rootInvisibleHeight > 300) {
                    //软键盘弹出来的时候
                    // 计算root滚动高度，使scrollToView在可见区域的底部
                    int target1 = location[1] + scrollToView.getHeight() - rect.bottom;//上移目标1
                    int target2 = (location[1] - target1) / 4;//上衣目标2，为了更加居中

                    int scrollHeight = target1 + target2;
                    root.scrollTo(0, scrollHeight);
                } else {
                    root.scrollTo(0, 0);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        root.scrollTo(0, 0);
    }

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleLogin = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(LoginActivity.this, "请检查网络连接_Error12", Toast.LENGTH_LONG).show();
//                    if (progressDialog != null) {
//                        progressDialog.dismiss();
//                    }
//                    activity_paper_list_refreshLayout.finishRefresh(false);
//                    activity_paper_list_refreshLayout.finishLoadMore(false);
                } else {
                    parseLogin(msg.obj.toString());
                }
            }
        };
        handleMsg = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(LoginActivity.this, "请检查网络连接_Error13", Toast.LENGTH_LONG).show();
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
    }

    //处理验证码
    private void parseMsg(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//            int status = jsonObject.getInt("status");
            //TODO 暂时不判断验证码status
//            if (status == 0) {
            activity_login_textview3.setClickable(false);
            activity_login_textview3.setBackgroundResource(R.drawable.msgsend_gray);
            countDownTimer.start();
//            }
//            String msgReturn = jsonObject.getString("msg");
//            Toast.makeText(this, msgReturn, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseLogin(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//登录成功
                //保存用户的uid等信息
                String apud = jsonObject.getString("apud");
                String aptk = jsonObject.getString("aptk");
                SharedPreferences.Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
                editor.putString("apud", apud);
                editor.putString("aptk", aptk);
                editor.apply();
                //由于用户、流量2种vhall_account的类型只能在play和detail中获取到，这里只能取消登录vhall
//                //登录聊天账号
//                String uid = Util.getUid();
//                //登录微吼账号，用于聊天
//                String username = "wxh" + uid;
//                String userpass = "1q2w3e4r5t6y7u8i9o";
//
//                VhallSDK.login(username, userpass, new UserInfoDataSource.UserInfoCallback() {
//                    @Override
//                    public void onSuccess(UserInfo userInfo) {
////                        Toast.makeText(MyApplication.context, "登录聊天服务器成功login", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(int errorCode, String reason) {
////                        Toast.makeText(MyApplication.context, "登录聊天服务器失败login", Toast.LENGTH_SHORT).show();
//                    }
//                });

                //向消息中心发送登录成功的信息，刷新消息界面
                MessageCenterFragment.handleLogin.obtainMessage().sendToTarget();

                //登录成功后，关闭登录页面，并向我的学习返回成功成功的消息
                MyStudiesFragment.handleLogin.obtainMessage().sendToTarget();

                finish();
            }
            String msgReturn = jsonObject.getString("msg");
            Toast.makeText(this, msgReturn, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_login_layout0://左上角关闭按钮布局
                this.finish();
                break;
            case R.id.activity_login_editText1://请输入您的手机号

                break;
            case R.id.activity_login_editText2://请输入您的密码

                break;
            case R.id.activity_login_imageView2://明文显示密码图标布局
                if (showPassword) {//如果当前是显示密码的状态，点击后改为不显示密码明文
                    activity_login_imageView2.setImageResource(R.drawable.icon_close_password);
                    activity_login_editText2.setTransformationMethod(PasswordTransformationMethod.getInstance());//密码隐藏
                    showPassword = false;
                } else {//如果当前是密码状态，点击后显示密码
                    activity_login_imageView2.setImageResource(R.drawable.icon_open_password);
                    activity_login_editText2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());//密码显示
                    showPassword = true;
                }
                activity_login_editText2.setSelection(activity_login_editText2.getText().toString().length());//将光标移至文字末尾
                break;
            case R.id.activity_login_layout3://登录按钮布局
                if (isPassword) {//密码登录
                    String phone = activity_login_editText1.getText().toString();
                    String pwd = activity_login_editText2.getText().toString();
                    if (phone.equals("") || pwd.equals("")) {
                        Toast.makeText(this, "请输入登录信息", Toast.LENGTH_SHORT).show();
                    } else {
                        if (Util.isMobile(phone)) {
                            if (Util.isPassword(pwd)) {
                                //发送登录请求
                                Map<String, String> paramsMap = new HashMap<>();
                                paramsMap.put("phone", phone);
                                paramsMap.put("pwd", pwd);
                                paramsMap.put("type", "1");
                                RequestURL.sendPOST("https://app.feimayun.com/Login/login", handleLogin, paramsMap);
                            }
                        }
                    }
                } else {//验证码登录
                    String phone1 = activity_login_editText1.getText().toString();
                    String code = activity_login_editText2.getText().toString();
                    if (phone1.equals("") || code.equals("")) {
                        Toast.makeText(this, "请输入登录信息", Toast.LENGTH_SHORT).show();
                    } else {
                        if (Util.isMobile(phone1)) {
                            //发送登录请求
                            Map<String, String> paramsMap = new HashMap<>();
                            paramsMap.put("phone", phone1);
                            paramsMap.put("vcode", code);
                            paramsMap.put("type", "2");
                            RequestURL.sendPOST("https://app.feimayun.com/Login/login", handleLogin, paramsMap);
                        }
                    }
                }
                break;
            case R.id.activity_login_textView1://验证码/密码登录切换按钮
                //清空输入框
                if (activity_login_editText2.length() > 0) {
                    activity_login_editText2.getText().clear();
                }
                if (isPassword) {//如果当前是密码登录，切换成验证码登录
                    activity_login_editText2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());//显示文本
                    activity_login_editText2.setInputType(TYPE_CLASS_NUMBER);
                    activity_login_editText2.setHint("请输入验证码");
                    activity_login_imageView2.setVisibility(View.GONE);
                    activity_login_textview3.setVisibility(View.VISIBLE);
                    activity_login_textView1.setText("密码登录");
                    isPassword = false;
                } else {//如果当前是验证码登录，切换成密码登录
                    activity_login_editText2.setInputType(TYPE_TEXT_VARIATION_PASSWORD);//为了打开英文键盘
                    if (showPassword) {
                        activity_login_imageView2.setImageResource(R.drawable.icon_open_password);
                        activity_login_editText2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());//密码显示
                    } else {
                        activity_login_imageView2.setImageResource(R.drawable.icon_close_password);
                        activity_login_editText2.setTransformationMethod(PasswordTransformationMethod.getInstance());//密码隐藏
                    }
                    activity_login_editText2.setHint("请输入您的密码");
                    activity_login_imageView2.setVisibility(View.VISIBLE);
                    activity_login_textview3.setVisibility(View.GONE);
                    activity_login_textView1.setText("验证码登录");
                    isPassword = true;
                }
                break;
            case R.id.activity_login_textView2://立即注册
                Intent intentRegisterActivity = new Intent(this, RegisterActivity.class);
                startActivity(intentRegisterActivity);
                break;
            case R.id.activity_login_textview3://获取验证码
                //首先获取输入框的手机号
                String phone0 = activity_login_editText1.getText().toString();
                if (phone0.equals("")) {
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                } else {
                    //发送验证码
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("phone", phone0);
                    RequestURL.sendPOST("https://app.feimayun.com/Login/msgSend", handleMsg, paramsMap);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}
