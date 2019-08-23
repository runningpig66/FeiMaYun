package cn.aura.feimayun.activity;

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
import android.widget.EditText;
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

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.QuestionReplyRvAdapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;

public class QuestionAddActivity extends BaseActivity implements View.OnClickListener {
    private static Handler handleImages;
    private static Handler handleAdd;
    String leimu_1;
    String leimu_2;
    String uid;
    private EditText questionadd_edittext1;//标题输入框
    private LinesEditView questionadd_edittext2;//回复内容输入框
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
                    Toast.makeText(QuestionAddActivity.this, "请检查网络连接_Error19", Toast.LENGTH_LONG).show();
                    isUploadNow = false;
                    tDialog.dismiss();
                } else {
                    parseImages(msg.obj.toString());
                }
            }
        };
        handleAdd = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(QuestionAddActivity.this, "请检查网络连接_Error20", Toast.LENGTH_LONG).show();
                    isUploadNow = false;
                    tDialog.dismiss();
                } else {
                    parseAdd(msg.obj.toString());
                }
            }
        };
    }

    private void parseAdd(String s) {
        JSONTokener jsonTokener = new JSONTokener(s);
        try {
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                requestSuccessful = true;
                Toast.makeText(this, "发布成功，待审核", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        finish();
                        isUploadNow = false;
                    }
                }, 1000);
            } else {
                requestSuccessful = false;
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
                String title = questionadd_edittext1.getText().toString();//问题标题
                String content = questionadd_edittext2.getContentText();//问题的描述，可选

                Map<String, String> map = new HashMap<>();
                map.put("title", title);
                map.put("content", content);
                map.put("leimu_1", leimu_1);
                map.put("leimu_2", leimu_2);
                map.put("uid", uid);
                if (data != null && !data.equals("")) {
                    map.put("images", data);
                }
                RequestURL.sendPOST("https://app.feimayun.com/Qa/add", handleAdd, map, QuestionAddActivity.this);
            } else {
                Toast.makeText(this, "图片上传失败", Toast.LENGTH_SHORT).show();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_add);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            Intent intent = getIntent();
            leimu_1 = intent.getStringExtra("leimu_1");
            leimu_2 = intent.getStringExtra("leimu_2");
            uid = Util.getUid();


            handle();

            //取消按钮
            TextView questionadd_textview1 = findViewById(R.id.questionadd_textview1);
            questionadd_textview1.setOnClickListener(this);
            //发布按钮
            TextView questionadd_textview2 = findViewById(R.id.questionadd_textview2);
            questionadd_textview2.setOnClickListener(this);
            questionadd_edittext1 = findViewById(R.id.questionadd_edittext1);
            questionadd_edittext2 = findViewById(R.id.questionadd_edittext2);
            //添加图片
            RecyclerView questionadd_recyclerview1 = findViewById(R.id.questionadd_recyclerview1);

            adapter = new QuestionReplyRvAdapter(this, mList);
            questionadd_recyclerview1.setLayoutManager(new GridLayoutManager(this, 4));
            Divider divider = new Api21ItemDivider(Color.TRANSPARENT, 10, 10);
            questionadd_recyclerview1.addItemDecoration(divider);
            questionadd_recyclerview1.setAdapter(adapter);
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
//                        Toast.makeText(QuestionAddActivity.this, "Canceled.", Toast.LENGTH_LONG).show();
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
        if (vId == R.id.questionadd_textview1) {//取消按钮
            finish();
        } else if (vId == R.id.questionadd_textview2) {//发布按钮
            if (isUploadNow) {//如果正在上传中，禁止用户再次点击
                Toast.makeText(this, "正在上传中，请勿重复点击", Toast.LENGTH_SHORT).show();
            } else {
                String title = questionadd_edittext1.getText().toString();//问题标题
                String content = questionadd_edittext2.getContentText();//问题的描述，可选
                if (title.equals("") || content.equals("")) {//问题标题和描述不能为空
                    Toast.makeText(this, "请完善提问标题及描述", Toast.LENGTH_SHORT).show();
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
                    if (mList.isEmpty()) {//用户没有添加回复图片，只有回复的文字信息
                        Map<String, String> map = new HashMap<>();
                        map.put("title", title);
                        map.put("content", content);
                        map.put("leimu_1", leimu_1);
                        map.put("leimu_2", leimu_2);
                        map.put("uid", uid);
                        RequestURL.sendPOST("https://app.feimayun.com/Qa/add", handleAdd, map, QuestionAddActivity.this);
                    } else {//用户添加了回复图片，同时还有回复的文字信息
                        //构建图片File数组
//                        List<File> files = new ArrayList<>();
//                        for (int i = 0; i < mList.size(); i++) {
//                            File file = new File(mList.get(i));
//                            files.add(file);
//                        }
                        //首先上传图片到图片服务器，等待返回图片保存的服务器地址

                        RequestURL.uploadFile(mList, "https://app.feimayun.com/Upload/upImages", handleImages, QuestionAddActivity.this);
                    }
                }
            }

        }

    }


}
