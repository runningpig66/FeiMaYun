package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.view.ProgressDialog;

public class FaceToFaceActivity extends BaseActivity implements View.OnClickListener {
    //请求面授详情
    private static Handler handleNetwork;
    private ProgressDialog progressDialog;
    private TextView activity_face_to_face_textview1;
    private TextView activity_face_to_face_textview2;
    private TextView activity_face_to_face_textview3;
    private TextView activity_face_to_face_textview4;
    private TextView activity_face_to_face_textview5;
    private TextView activity_face_to_face_textview6;
    private com.tencent.smtt.sdk.WebView activity_face_to_face_webview;
    private ImageView activity_face_to_face_imageview1;
    private ImageView activity_face_to_face_imageview2;
    //存放intent中获取到的数据
    private String data_id;
    private String data_teach_type;

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(FaceToFaceActivity.this, "请检查网络连接_Error16", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
//                    activity_paper_list_refreshLayout.finishRefresh(false);
//                    activity_paper_list_refreshLayout.finishLoadMore(false);
                } else {
                    parseJSON(msg.obj.toString());
                }
            }
        };
    }

    private void parseJSON(String s) {

        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                //解析data
                JSONObject dataObject = jsonObject.getJSONObject("data");
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("id", dataObject.getString("id"));
                dataMap.put("name", dataObject.getString("name"));
                dataMap.put("bg_url", dataObject.getString("bg_url"));
                dataMap.put("expire", dataObject.getString("expire"));
                dataMap.put("lesson_time", dataObject.getString("lesson_time"));
                dataMap.put("address", dataObject.getString("address"));
                dataMap.put("about", dataObject.getString("about"));

                //解析teacher
                JSONObject teacherObject = jsonObject.getJSONObject("teacher");
                Map<String, String> teacherMap = new HashMap<>();
                teacherMap.put("id", teacherObject.getString("id"));
                teacherMap.put("biger", teacherObject.getString("biger"));
                teacherMap.put("name", teacherObject.getString("name"));
                teacherMap.put("lessons", teacherObject.getString("lessons"));

                //得到数据后，开始设置界面数据
                activity_face_to_face_textview1.setText(dataMap.get("name"));
                if (Util.isOnMainThread()) {
                    RequestOptions options = new RequestOptions().fitCenter();
                    Glide.with(MyApplication.context).load(dataMap.get("bg_url")).apply(options).into(activity_face_to_face_imageview1);
                }
                activity_face_to_face_textview2.setText("学习周期:" + dataMap.get("expire"));
                activity_face_to_face_textview3.setText("上课地点:" + dataMap.get("address"));
                activity_face_to_face_textview4.setText("开课时间:" + dataMap.get("lesson_time"));
                if (Util.isOnMainThread()) {
                    RequestOptions options = new RequestOptions().fitCenter();
                    Glide.with(MyApplication.context).load(teacherMap.get("biger")).apply(options).into(activity_face_to_face_imageview2);
                }
                activity_face_to_face_textview5.setText("主讲教师:" + teacherMap.get("name"));
                activity_face_to_face_textview6.setText("课程数量:" + teacherMap.get("lessons"));

                String aboutString = dataMap.get("about");
                String des2 = Util.getNewContent(aboutString);
                activity_face_to_face_webview.loadData(des2, "text/html; charset=UTF-8", null);
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_to_face);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            handler();

            Intent intent = getIntent();
            data_id = intent.getStringExtra("data_id");
            data_teach_type = intent.getStringExtra("data_teach_type");

            initView();

            //网络请求、初始化数据
            initData();
        }

    }

    private void initView() {
        //返回按钮布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);
        //标题
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("课程详情");

        activity_face_to_face_textview1 = findViewById(R.id.activity_face_to_face_textview1);
        activity_face_to_face_textview2 = findViewById(R.id.activity_face_to_face_textview2);
        activity_face_to_face_textview3 = findViewById(R.id.activity_face_to_face_textview3);
        activity_face_to_face_textview4 = findViewById(R.id.activity_face_to_face_textview4);
        activity_face_to_face_textview5 = findViewById(R.id.activity_face_to_face_textview5);
        activity_face_to_face_textview6 = findViewById(R.id.activity_face_to_face_textview6);
        activity_face_to_face_webview = findViewById(R.id.activity_face_to_face_webview);
        TextView activity_face_to_face_textview8 = findViewById(R.id.activity_face_to_face_textview8);
        activity_face_to_face_imageview1 = findViewById(R.id.activity_face_to_face_imageview1);
        activity_face_to_face_imageview2 = findViewById(R.id.activity_face_to_face_imageview2);
        activity_face_to_face_textview8.setOnClickListener(this);
    }

    private void initData() {
        String uid = Util.getUid();

        //请求视频播放
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("id", data_id);
        paramsMap.put("teach_type", data_teach_type);
        paramsMap.put("uid", uid);

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        RequestURL.sendPOST("https://app.feimayun.com/Lesson/detail", handleNetwork, paramsMap);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_face_to_face_textview8:
                //TODO 打开电话dialog
                break;
            case R.id.headtitle_layout:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activity_face_to_face_webview != null) {
            activity_face_to_face_webview.destroy();
        }
    }
}
