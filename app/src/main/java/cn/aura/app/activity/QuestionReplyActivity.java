package cn.aura.app.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.classichu.lineseditview.LinesEditView;
import com.timmy.tdialog.TDialog;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.widget.divider.Api21ItemDivider;
import com.yanzhenjie.album.widget.divider.Divider;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.aura.app.R;
import cn.aura.app.adapter.QuestionReplyRvAdapter;
import cn.aura.app.application.MyApplication;
import cn.aura.app.util.RequestURL;
import cn.aura.app.util.Util;

public class QuestionReplyActivity extends BaseActivity implements View.OnClickListener {
    private static Handler handleImages;
    private static Handler handleAnswer;
    String uid = "";
    String leve = "";
    String call_uid = "";
    String pid = "";
    String qaid = "";
    private LinesEditView questionreply_edittext1;//大的输入框
    private QuestionReplyRvAdapter adapter;//recyclerview网格的适配器
    private ArrayList<AlbumFile> mAlbumFiles;//选择的照片
    private ArrayList<String> mList = new ArrayList<>();//存储选择照片的路径
    //当回复成功，页面会在1秒后关闭。
    //但是如果这1秒内点击了返回键，也需要更新回复详情页的数据。用这个变量来记录当前是否回复成功
    private boolean requestSuccessful = false;
    //判断当前是否正在上传，控制回复按钮是否可以点击。虽然点击后弹出dialog看似屏蔽了点击，
    //但不能解决短时间内双击的问题，所以需要这个变量代码上进行控制
    private boolean isUploadNow = false;
    private TDialog tDialog;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleImages = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(QuestionReplyActivity.this, "请检查网络连接_Error14", Toast.LENGTH_LONG).show();
//                    if (progressDialog != null) {
//                        progressDialog.dismiss();
//                    }
                    isUploadNow = false;
                    tDialog.dismiss();
                } else {
                    parseImages(msg.obj.toString());
                }
            }
        };
        handleAnswer = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(QuestionReplyActivity.this, "请检查网络连接_Error15", Toast.LENGTH_LONG).show();
//                    if (progressDialog != null) {
//                        progressDialog.dismiss();
//                    }
                    isUploadNow = false;
                    tDialog.dismiss();
                } else {
                    parseAnswer(msg.obj.toString());
                }
            }
        };
    }

    private void parseAnswer(String s) {
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                Toast.makeText(this, "回复成功", Toast.LENGTH_SHORT).show();
                tDialog.dismiss();
                requestSuccessful = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        finish();
                        isUploadNow = false;
                    }
                }, 1000);
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
                String data = jsonObject.getString("data");
                //拿到服务器返回的图片存放地址，然后再次上传到内容服务器

                String content = questionreply_edittext1.getContentText();//回复内容
                Map<String, String> map = new HashMap<>();
                map.put("qaid", qaid);
                map.put("pid", pid);
                map.put("content", content);
                map.put("uid", uid);
                map.put("leve", leve);
                if (data != null && !data.equals("")) {
                    map.put("images", data);
                }
                if (leve.equals("3")) {
                    if (call_uid != null && !call_uid.equals("")) {
                        map.put("call_uid", call_uid);
                    }
                }
                RequestURL.sendPOST("https://app.feimayun.com/Qa/answer", handleAnswer, map);
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
        setContentView(R.layout.activity_question_reply);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            Intent intent = getIntent();
            qaid = intent.getStringExtra("qaid");
            pid = intent.getStringExtra("pid");
            leve = intent.getStringExtra("leve");
            call_uid = intent.getStringExtra("call_uid");
            if (leve.equals("3")) {
                call_uid = intent.getStringExtra("call_uid");
                pid = intent.getStringExtra("pid");
            }
            uid = Util.getUid();

            handle();

            //取消
            TextView questionreply_textview1 = findViewById(R.id.questionreply_textview1);
            questionreply_textview1.setOnClickListener(this);
            //回复
            TextView questionreply_textview2 = findViewById(R.id.questionreply_textview2);
            questionreply_textview2.setOnClickListener(this);
            questionreply_edittext1 = findViewById(R.id.questionreply_edittext1);
            //显示照片的网格
            RecyclerView questionreply_recyclerview1 = findViewById(R.id.questionreply_recyclerview1);

            adapter = new QuestionReplyRvAdapter(this, mList);
            questionreply_recyclerview1.setLayoutManager(new GridLayoutManager(this, 4));
            Divider divider = new Api21ItemDivider(Color.TRANSPARENT, 10, 10);
            questionreply_recyclerview1.addItemDecoration(divider);
            questionreply_recyclerview1.setAdapter(adapter);
            adapter.setOnItemClickListener(new QuestionReplyRvAdapter.OnItemClickListener() {
                @Override
                public void onPictureClick(View view, int position) {
                    if (position == adapter.getItemCount() - 1) {//如果点击了最后一张图，判断最后一张图是加号 还是 图片
                        if (mList.size() == 4) {//如果图片已经有4张了，说明加号已经消失，最后一张是图片
//                        Toast.makeText(QuestionReplyActivity.this, "查看大图", Toast.LENGTH_SHORT).show();
                            previewImage(position);
                        } else {//图片不满4张，最后一张是加号，进入相册选择
                            selectImage();
                        }
                    } else {//如果点击的不是最后一张，说明点击的一定是图片
//                    Toast.makeText(QuestionReplyActivity.this, "查看大图", Toast.LENGTH_SHORT).show();
                        previewImage(position);
                    }
                }

                @Override
                public void onRedButtonClick(View view, int position) {
                    mList.remove(position);
                    mAlbumFiles.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });
        }

    }

    public void selectImage() {
        Album.image(this)
                .multipleChoice()
                .camera(true)
                .columnCount(3)
                .selectCount(4)
                .checkedList(mAlbumFiles)
                .onResult(new Action<ArrayList<AlbumFile>>() {
                    @Override
                    public void onAction(@NonNull ArrayList<AlbumFile> result) {
                        mAlbumFiles = result;
                        mList.clear();
                        for (int i = 0; i < mAlbumFiles.size(); i++) {
                            mList.add(mAlbumFiles.get(i).getPath());
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .onCancel(new Action<String>() {
                    @Override
                    public void onAction(@NonNull String result) {
//                        Toast.makeText(QuestionReplyActivity.this, "Canceled.", Toast.LENGTH_LONG).show();
                    }
                })
                .start();
    }

    private void previewImage(int position) {
        if (mAlbumFiles == null || mAlbumFiles.size() == 0) {
            Toast.makeText(this, "Please select, first.", Toast.LENGTH_LONG).show();
        } else {
            Album.galleryAlbum(this)
                    .checkable(true)
                    .checkedList(mAlbumFiles)
                    .currentPosition(position)
                    .onResult(new Action<ArrayList<AlbumFile>>() {
                        @Override
                        public void onAction(@NonNull ArrayList<AlbumFile> result) {
                            mAlbumFiles = result;
                            mList.clear();
                            for (int i = 0; i < mAlbumFiles.size(); i++) {
                                mList.add(mAlbumFiles.get(i).getPath());
                            }
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .start();
        }
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.questionreply_textview1) {//取消
            finish();
        } else if (vId == R.id.questionreply_textview2) {//回复
            if (isUploadNow) {//如果正在上传中，禁止用户再次点击
                Toast.makeText(this, "正在上传中，请勿重复点击", Toast.LENGTH_SHORT).show();
            } else {
                String content = questionreply_edittext1.getContentText();//回复内容
                if (content.equals("")) {//回复文字是不允许为空的
                    Toast.makeText(this, "请输入回复内容", Toast.LENGTH_SHORT).show();
                } else {
                    isUploadNow = true;
                    tDialog = new TDialog.Builder(getSupportFragmentManager())
                            .setLayoutRes(R.layout.dialog_loading)
                            .setHeight(300)
                            .setWidth(300)
//                            .setCancelable(false)//TODO
                            .setCancelableOutside(false)
                            .create()
                            .show();
                    if (mList.isEmpty()) {//用户没有添加回复图片，只有回复的文字
                        Map<String, String> map = new HashMap<>();
                        map.put("qaid", qaid);
                        map.put("pid", pid);
                        map.put("content", content);
                        map.put("uid", uid);
                        map.put("leve", leve);
                        if (leve.equals("3")) {
                            if (call_uid != null && !call_uid.equals("")) {
                                map.put("call_uid", call_uid);
                            }
                        }
                        RequestURL.sendPOST("https://app.feimayun.com/Qa/answer", handleAnswer, map);
                    } else {//用户添加了回复图片，同时还有回复的文字
                        //构建图片File数组
//                        List<File> files = new ArrayList<>();
//                        for (int i = 0; i < mList.size(); i++) {
//                            File file = new File(mList.get(i));
//                            files.add(file);
//                        }
                        //首先上传图片到图片服务器，等待返回图片保存的服务器地址
                        RequestURL.uploadFile(mList, "https://app.feimayun.com/Upload/upImages", handleImages);
                    }
                }
            }
        }

    }
}
