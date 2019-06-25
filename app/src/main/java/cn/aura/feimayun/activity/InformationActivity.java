package cn.aura.feimayun.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnBindViewListener;
import com.timmy.tdialog.listener.OnViewClickListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.fragment.MessageCenterFragment;
import cn.aura.feimayun.fragment.MyStudiesFragment;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class InformationActivity extends BaseActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    public final static String[] PERMS_CAMERA = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public final static String[] PERMS_PHOTO = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static Handler handleImages;
    private static Handler handleUpAvater;//上传存储图片的路径
    private static Handler handleMsg;//接收验证码的handler
    private static Handler handleResetPwd;//修改登录密码
    private static Handler handleEditPhone;//修改手机号
    String phone;//记录需要修改的手机号，当手机号修改成功后，取这个新号码刷新一下界面
    private Uri imageUri;
    private ImageView activity_information_imageView2;
    private ArrayList<String> mList = new ArrayList<>();//存储选择照片的路径
    private boolean requestSuccessful = false;
    private boolean isUploadNow = false;
    private TDialog tDialog;
    private Uri uriTempFile;
    private LayoutInflater inflater;
    private TextView activity_register_textView1;//修改密码发送验证码按钮
    private EditText activity_information_editText2;//展示手机号
    private CountDownTimer countDownTimer;
    private TDialog tDialog1;//修改密码
    private LinearLayout root;//root

    {
        //获取验证码按钮计数60秒
        int mCount = 60;
        countDownTimer = new CountDownTimer(mCount * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                activity_register_textView1.setClickable(false);
                activity_register_textView1.setBackgroundResource(R.drawable.button_gray);
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

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleImages = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(InformationActivity.this, "请检查网络连接_Error46", Toast.LENGTH_LONG).show();
                    isUploadNow = false;
                    tDialog.dismiss();
                } else {
                    parseImages(msg.obj.toString());
                }
            }
        };
        handleUpAvater = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(InformationActivity.this, "请检查网络连接_Error47", Toast.LENGTH_LONG).show();
                    isUploadNow = false;
                    tDialog.dismiss();
                } else {
                    parseUpAvater(msg.obj.toString());
                }
            }
        };
        handleMsg = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(InformationActivity.this, "请检查网络连接_Error000", Toast.LENGTH_LONG).show();
                } else {
                    parseMsg(msg.obj.toString());
                }
            }
        };
        handleResetPwd = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(InformationActivity.this, "请检查网络连接_Error001", Toast.LENGTH_LONG).show();
                } else {
                    parseResetPwd(msg.obj.toString());
                }
            }
        };
        handleEditPhone = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(InformationActivity.this, "请检查网络连接_Error002", Toast.LENGTH_LONG).show();
                } else {
                    parseEditPhone(msg.obj.toString());
                }
            }
        };
    }

    private void parseEditPhone(String s) {
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            String msg = jsonObject.getString("msg");
            if (status == 1) {
                tDialog1.dismiss();
                View view = inflater.inflate(R.layout.setpwd_success, root, false);
                TextView t_1 = view.findViewById(R.id.t_1);
                t_1.setText(msg);
                new TDialog.Builder(getSupportFragmentManager())
                        .setLayoutRes(R.layout.setpwd_success)
                        .setDialogView(view)
                        .setScreenWidthAspect(InformationActivity.this, 0.8f)
//                        .setScreenHeightAspect(InformationActivity.this, 0.6f)
                        .setCancelableOutside(true)
                        .addOnClickListener(R.id.submit)
                        .setOnViewClickListener(new OnViewClickListener() {
                            @Override
                            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                switch (view.getId()) {
                                    case R.id.submit:
                                        tDialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
                activity_information_editText2.setText(phone);
                SharedPreferences.Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
                editor.putString("phone", phone);
                editor.apply();
            } else {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseResetPwd(String s) {
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            String msg = jsonObject.getString("msg");
            if (status == 1) {
                tDialog1.dismiss();
                View view = inflater.inflate(R.layout.setpwd_success, root, false);
                TextView t_1 = view.findViewById(R.id.t_1);
                t_1.setText(msg);
                new TDialog.Builder(getSupportFragmentManager())
                        .setLayoutRes(R.layout.setpwd_success)
                        .setDialogView(view)
                        .setScreenWidthAspect(InformationActivity.this, 0.8f)
//                        .setScreenHeightAspect(InformationActivity.this, 0.6f)
                        .setCancelableOutside(true)
                        .addOnClickListener(R.id.submit)
                        .setOnViewClickListener(new OnViewClickListener() {
                            @Override
                            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                switch (view.getId()) {
                                    case R.id.submit:
                                        tDialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
            } else {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
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

    private void parseUpAvater(String s) {
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                Toast.makeText(this, "头像更改成功", Toast.LENGTH_SHORT).show();
                tDialog.dismiss();
                requestSuccessful = true;
                isUploadNow = false;
                MyStudiesFragment.handleLogin.obtainMessage().sendToTarget();

                Bitmap bitmap = BitmapFactory.decodeFile(mList.get(0));
                if (Util.isOnMainThread()) {
                    Glide.with(MyApplication.context).load(bitmap).into(activity_information_imageView2);
                }
            } else {
                String msg = jsonObject.getString("msg");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                isUploadNow = false;
                tDialog.dismiss();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            isUploadNow = false;
            tDialog.dismiss();
        }

    }

    @Override
    public void onBackPressed() {
        if (requestSuccessful) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    private void parseImages(String s) {

        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                JSONObject dataObject = jsonObject.getJSONObject("data");
                String big_img = dataObject.getString("big_img");
                String small_img = dataObject.getString("small_img");
                String uid = Util.getUid();

                // 上传成功头像图片以后，获取到图片路径，将路径上传到文字服务器
                Map<String, String> map = new HashMap<>();
                map.put("uid", uid);
                map.put("bgImg", big_img);
                map.put("smallImg", small_img);
                RequestURL.sendPOST("https://app.feimayun.com/User/upAvater", handleUpAvater, map);
            } else {
                Toast.makeText(this, "图片上传失败", Toast.LENGTH_SHORT).show();
                isUploadNow = false;
                tDialog.dismiss();
            }
        } catch (JSONException e) {
            isUploadNow = false;
            tDialog.dismiss();
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        handle();

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            Intent intent = getIntent();
            String real_name = intent.getStringExtra("real_name");
            String phone = intent.getStringExtra("phone");
            String avater = intent.getStringExtra("avater");

            inflater = LayoutInflater.from(this);

            root = findViewById(R.id.root);

            //左上角的返回按钮的布局
            RelativeLayout activity_information_layout2 = findViewById(R.id.activity_information_layout2);
            //圆形头像
            activity_information_imageView2 = findViewById(R.id.activity_information_imageView2);
            //用户名
            TextView activity_information_textView3 = findViewById(R.id.activity_information_textView3);
            //昵称
            EditText activity_information_editText1 = findViewById(R.id.activity_information_editText1);
            //手机号
            activity_information_editText2 = findViewById(R.id.activity_information_editText2);
            //密码
            EditText activity_information_editText3 = findViewById(R.id.activity_information_editText3);
            //退出按钮的布局
            LinearLayout activity_information_layout4 = findViewById(R.id.activity_information_layout4);
            //3个修改资料图标按钮
            LinearLayout activity_information_imageView3 = findViewById(R.id.activity_information_imageView3);
            LinearLayout activity_information_imageView4 = findViewById(R.id.activity_information_imageView4);
            LinearLayout activity_information_imageView5 = findViewById(R.id.activity_information_imageView5);
            activity_information_imageView3.setOnClickListener(this);
            activity_information_imageView4.setOnClickListener(this);
            activity_information_imageView5.setOnClickListener(this);

            activity_information_layout2.setOnClickListener(this);
            activity_information_imageView2.setOnClickListener(this);
            activity_information_textView3.setOnClickListener(this);
            activity_information_editText1.setOnClickListener(this);
            activity_information_editText2.setOnClickListener(this);
            activity_information_editText3.setOnClickListener(this);
            activity_information_layout4.setOnClickListener(this);

            //显示用户数据&输入框不可编辑
            activity_information_textView3.setText(real_name);
            activity_information_editText1.setText(real_name);
            activity_information_editText2.setText(phone);
            if (Util.isOnMainThread()) {
                Glide.with(MyApplication.context).load(avater).into(activity_information_imageView2);
            }
            activity_information_editText1.setFocusable(false);
            activity_information_editText1.setFocusableInTouchMode(false);
            activity_information_editText2.setFocusable(false);
            activity_information_editText2.setFocusableInTouchMode(false);
            activity_information_editText3.setFocusable(false);
            activity_information_editText3.setFocusableInTouchMode(false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.activity_information_layout2) {//左上角的返回按钮
            finish();
        } else if (vId == R.id.activity_information_imageView2) {//圆形头像
            if (isUploadNow) {
                Toast.makeText(this, "正在上传中，请勿重复点击", Toast.LENGTH_SHORT).show();
            } else {
                new TDialog.Builder(getSupportFragmentManager())
                        .setLayoutRes(R.layout.dialog_change_avatar)
                        .setScreenWidthAspect(this, 1.0f)
                        .setGravity(Gravity.BOTTOM)
                        .setDialogAnimationRes(R.style.animate_dialog)
                        .addOnClickListener(R.id.tv_open_camera, R.id.tv_open_album, R.id.tv_cancel)
                        .setOnViewClickListener(new OnViewClickListener() {
                            @Override
                            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                switch (view.getId()) {
                                    case R.id.tv_open_camera:
                                        // 获取拍照权限
                                        if (EasyPermissions.hasPermissions(InformationActivity.this, PERMS_CAMERA)) {
                                            toCamera();
                                        } else {
                                            EasyPermissions.requestPermissions(InformationActivity.this, "上传头像需要开启部分权限", 0x2001, PERMS_CAMERA);
                                        }
                                        tDialog.dismiss();
                                        break;
                                    case R.id.tv_open_album:
                                        if (EasyPermissions.hasPermissions(InformationActivity.this, PERMS_PHOTO)) {
                                            toPicture();
                                        } else {
                                            EasyPermissions.requestPermissions(InformationActivity.this, "上传头像需要开启部分权限", 0x2002, PERMS_PHOTO);
                                        }
                                        tDialog.dismiss();
                                        break;
                                    case R.id.tv_cancel:
                                        tDialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
            }
        }
//        else if (vId == R.id.activity_information_textView3) {//用户名
//
//        } else if (vId == R.id.activity_information_editText1) {//昵称
//
//        } else if (vId == R.id.activity_information_editText2) {//手机号
//
//        } else if (vId == R.id.activity_information_editText3) {//密码
//
//        }
        else if (vId == R.id.activity_information_layout4) {//退出按钮的布局
            //给我的学习返回退出登录的信息
            MyStudiesFragment.handleLogout.obtainMessage().sendToTarget();
            //给消息中心返回退出登录的信息
            MessageCenterFragment.handleLogout.obtainMessage().sendToTarget();
            finish();
        }
//        else if (vId == R.id.activity_information_imageView3) {//显示昵称
//
//        }
        else if (vId == R.id.activity_information_imageView4) {//修改手机号
            View view = inflater.inflate(R.layout.modify_phone, root, false);
            final EditText e_0 = view.findViewById(R.id.e_0);
            final EditText e_1 = view.findViewById(R.id.e_1);
            final EditText e_2 = view.findViewById(R.id.e_2);
            if (activity_register_textView1 != null) {
                activity_register_textView1 = null;
            }
            activity_register_textView1 = view.findViewById(R.id.activity_register_textView1);//获取验证码
            if (tDialog1 != null) {
                tDialog1 = null;
            }
            tDialog1 = new TDialog.Builder(getSupportFragmentManager())
                    .setLayoutRes(R.layout.modify_phone)
                    .setDialogView(view)
                    .setScreenWidthAspect(InformationActivity.this, 0.8f)
//                        .setScreenHeightAspect(InformationActivity.this, 0.6f)
                    .setCancelableOutside(false)
                    .addOnClickListener(R.id.page_close, R.id.activity_register_textView1, R.id.submit)
                    .setOnBindViewListener(new OnBindViewListener() {
                        @Override
                        public void bindView(BindViewHolder viewHolder) {
                            final EditText editText = viewHolder.getView(R.id.e_0);
                            editText.post(new Runnable() {
                                @Override
                                public void run() {
                                    InputMethodManager imm = (InputMethodManager) InformationActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    Objects.requireNonNull(imm).showSoftInput(editText, 0);
                                }
                            });
                        }
                    })
                    .setOnViewClickListener(new OnViewClickListener() {
                        @Override
                        public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                            switch (view.getId()) {
                                case R.id.page_close:
                                    tDialog.dismiss();
                                    break;
                                case R.id.activity_register_textView1:
                                    String phone0 = e_0.getText().toString();
                                    if (TextUtils.isEmpty(phone0)) {
                                        Toast.makeText(InformationActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //判断手机号是否正确
                                        if (Util.isMobile(phone0)) {
                                            //发送验证码
                                            Map<String, String> paramsMap = new HashMap<>();
                                            paramsMap.put("phone", phone0);
                                            RequestURL.sendPOST("https://app.feimayun.com/Login/msgSend", handleMsg, paramsMap);
                                        }
                                    }
                                    break;
                                case R.id.submit:
                                    phone = e_0.getText().toString();
                                    String code = e_1.getText().toString();
                                    String passwd = e_2.getText().toString();
                                    if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(code) || TextUtils.isEmpty(passwd)) {
                                        Toast.makeText(InformationActivity.this, "请完成输入修改信息", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (Util.isMobile(phone)) {
                                            if (Util.isPassword(passwd)) {
                                                HashMap<String, String> paramsMap = new HashMap<>();
                                                paramsMap.put("uid", Util.getUid());
                                                paramsMap.put("phone", phone);
                                                paramsMap.put("code", code);
                                                paramsMap.put("passwd", passwd);
                                                //申请修改手机号
                                                RequestURL.sendPOST("https://app.feimayun.com/User/editPhone", handleEditPhone, paramsMap);
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    })
                    .create()
                    .show();
        } else if (vId == R.id.activity_information_imageView5) {//修改密码
            View view = inflater.inflate(R.layout.modify_pwd, root, false);
            final EditText e_0 = view.findViewById(R.id.e_0);//input phone number
            final EditText e_1 = view.findViewById(R.id.e_1);//authority code
            final EditText e_2 = view.findViewById(R.id.e_2);//new password
            final EditText e_3 = view.findViewById(R.id.e_3);//repeat pwd
            if (activity_register_textView1 != null) {
                activity_register_textView1 = null;
            }
            activity_register_textView1 = view.findViewById(R.id.activity_register_textView1);//request code
            if (tDialog1 != null) {
                tDialog1 = null;
            }
            tDialog1 = new TDialog.Builder(getSupportFragmentManager())
                    .setLayoutRes(R.layout.modify_pwd)
                    .setDialogView(view)
                    .setScreenWidthAspect(InformationActivity.this, 0.8f)
//                        .setScreenHeightAspect(InformationActivity.this, 0.6f)
                    .setCancelableOutside(false)
                    .addOnClickListener(R.id.page_close, R.id.activity_register_textView1, R.id.submit)
                    .setOnBindViewListener(new OnBindViewListener() {
                        @Override
                        public void bindView(BindViewHolder viewHolder) {
                            final EditText editText = viewHolder.getView(R.id.e_0);
                            editText.post(new Runnable() {
                                @Override
                                public void run() {
                                    InputMethodManager imm = (InputMethodManager) InformationActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    Objects.requireNonNull(imm).showSoftInput(editText, 0);
                                }
                            });
                        }
                    })
                    .setOnViewClickListener(new OnViewClickListener() {
                        @Override
                        public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                            switch (view.getId()) {
                                case R.id.page_close:
                                    tDialog.dismiss();
                                    break;
                                case R.id.activity_register_textView1:
                                    String phone0 = e_0.getText().toString();
                                    if (TextUtils.isEmpty(phone0)) {
                                        Toast.makeText(InformationActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //判断手机号是否正确
                                        if (Util.isMobile(phone0)) {
                                            //发送验证码
                                            Map<String, String> paramsMap = new HashMap<>();
                                            paramsMap.put("phone", phone0);
                                            RequestURL.sendPOST("https://app.feimayun.com/Login/msgSend", handleMsg, paramsMap);
                                        }
                                    }
                                    break;
                                case R.id.submit:
                                    String phone = e_0.getText().toString();
                                    String code = e_1.getText().toString();
                                    String passwd = e_2.getText().toString();
                                    String repasswd = e_3.getText().toString();
                                    if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(code) || TextUtils.isEmpty(passwd) || TextUtils.isEmpty(repasswd)) {
                                        Toast.makeText(InformationActivity.this, "请完成输入修改信息", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (Util.isMobile(phone)) {
                                            if (Util.isPassword(passwd)) {
                                                if (passwd.equals(repasswd)) {
                                                    HashMap<String, String> paramsMap = new HashMap<>();
                                                    paramsMap.put("phone", phone);
                                                    paramsMap.put("code", code);
                                                    paramsMap.put("passwd", passwd);
                                                    paramsMap.put("repasswd", repasswd);
                                                    //申请修改密码
                                                    RequestURL.sendPOST("https://app.feimayun.com/User/resetPwd", handleResetPwd, paramsMap);
                                                } else {
                                                    Toast.makeText(InformationActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case 0x2001://从拍照获取照片
                if (EasyPermissions.hasPermissions(this, PERMS_CAMERA)) {
                    toCamera();
                }
                break;
            case 0x2002:
                if (EasyPermissions.hasPermissions(this, PERMS_PHOTO)) {
                    toPicture();
                }
                break;
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
//        Toast.makeText(this, "onPermissionsDenied:" + requestCode + ":" + perms.size(), Toast.LENGTH_SHORT).show();

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        switch (requestCode) {
            case 0x2001:
                if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    new AppSettingsDialog.Builder(this)
                            .setRequestCode(0x2011)
                            .setTitle("拍照需要开启打开相机权限")
                            .setRationale("您已拒绝开启部分权限，这将导致无法打开相机，是否打开设置界面开启权限？")
                            .setNegativeButton("取消")
                            .setPositiveButton("确认")
                            .build()
                            .show();
                } else {
                    Toast.makeText(this, "权限拒绝无法打开相机", Toast.LENGTH_SHORT).show();
                }
                break;
            case 0x2002:
                if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    new AppSettingsDialog.Builder(this)
                            .setRequestCode(0x2022)
                            .setTitle("需要开启修改图片权限")
                            .setRationale("您已拒绝开启部分权限，这将导致无法打开相册，是否打开设置界面开启权限？")
                            .setNegativeButton("取消")
                            .setPositiveButton("确认")
                            .build()
                            .show();
                } else {
                    Toast.makeText(this, "权限拒绝无法打开相册", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x2011) {
            // Do something after user returned from app settings screen, like showing a Toast.
//            Toast.makeText(this, "returned_from_app_settings_to_activity", Toast.LENGTH_SHORT).show();
            if (EasyPermissions.hasPermissions(this, PERMS_CAMERA)) {
                toCamera();
            } else {
                Toast.makeText(this, "权限拒绝无法打开相机", Toast.LENGTH_SHORT).show();
            }
            return;
        } else if (requestCode == 0x2022) {
            if (EasyPermissions.hasPermissions(this, PERMS_PHOTO)) {
                toPicture();
            } else {
                Toast.makeText(this, "权限拒绝无法打开相册", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                startPhotoZoom(imageUri);
            } else if (requestCode == 2) {
                if (data != null) {
                    startPhotoZoom(data.getData());
                }
            } else if (requestCode == 3) {
                if (data != null) {
                    mList.clear();
                    mList.add(handleImage(uriTempFile));

                    isUploadNow = true;
                    tDialog = new TDialog.Builder(getSupportFragmentManager())
                            .setLayoutRes(R.layout.dialog_loading)
                            .setHeight(300)
                            .setWidth(300)
//                            .setCancelable(false)//TODO
                            .setCancelableOutside(false)
                            .create()
                            .show();
                    RequestURL.uploadFile2(mList, "https://app.feimayun.com/Upload/upImage", handleImages);
                }
            }
        }
    }

    /**
     * 跳转相机
     */
    public void toCamera() {
        File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");

        if (outputImage.exists()) {
            outputImage.delete();
        }
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            //通过FileProvider.getUriForFile获取URL，参数2应该与Provider在AndroidManifest.xml中定义的authorities标签一致
            imageUri = FileProvider.getUriForFile(InformationActivity.this, "cn.aura.app.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //MediaStore.ACTION_IMAGE_CAPTURE对应android.media.action.IMAGE_CAPTURE，用于打开系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, 1);//请求码1代表：请求系统相机
    }

    /**
     * 跳转相册
     */
    private void toPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 2);
    }

    /**
     * 裁剪相片
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= 24) {
            //添加这一句表示对目标应用临时
            // 授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        //输出的宽高
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //裁剪图片的质量
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        // 图片剪裁不足黑边解决
        intent.putExtra("scaleUpIfNeeded", true);

        uriTempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "temp_image.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//        intent.putExtra("return-data", true);
//        intent.putExtra("noFaceDetection", false);
        startActivityForResult(intent, 3);//请求码3代表：请求系统裁剪
    }

    @TargetApi(19)
    private String handleImage(Uri uri) {
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document 类型的 Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri 和 selection 来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

}
