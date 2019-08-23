package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.ExamCardAdapter;
import cn.aura.feimayun.adapter.ExamDeatilActivity_ViewPager1_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.fragment.ExamDetailActivity_ViewPager1_Fragment;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.ScreenUtils;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.view.GridItemDecoration;
import cn.aura.feimayun.view.MyGuideView;
import cn.aura.feimayun.view.ProgressDialog;
import cn.aura.feimayun.view.SelfDialog;
import cn.aura.feimayun.view.SelfDialog2;

import static cn.aura.feimayun.activity.PaperListActivity.refreshPaper;

/**
 * 描述：试题页面
 */
public class ExamDetailActivity extends BaseActivity implements View.OnClickListener {
    //请求答题页面详情
    private static Handler handleNetwork;
    //发送保存不提交答案
    private static Handler handleSaveTest1;
    //提交答案&强制提交答案
    private static Handler handleSaveTest2;
    public long countDownMillis = 0;//全局记录剩余的秒数
    public String fileName;
    //保存不提交 & 提交
    TextView activity_exam_detail_textview2;
    TextView activity_exam_detail_textview3;
    private CountDownTimer countDownTimer;
    private ProgressDialog progressDialog;
    private boolean haveData = true;//根据返回status==0判断是否购买试卷
    private int lastAnswerPage = 0;//记录未答题最小的page
    private TreeMap<Integer, String> answerMap = new TreeMap<>();
    private List<Boolean> mList = new LinkedList<>();//记录是否选择
    private String sid;
    private String tid;
    private Map<String, String> dataMap;
    private List<Map<String, String>> listsList = new ArrayList<>();
    private TextView activity_exam_detail_textview1;//倒计时文本框
    private ImageView activity_exam_detail_imageview1;//左箭头
    private ImageView activity_exam_detail_imageview2;//右箭头
    private ViewPager activity_exam_detail_viewpager1;
    private boolean loadLocalFile = false;
    private SelfDialog mSelfDialog;//提交时没有做题，提示是否重新做题
    private SelfDialog2 mSelfDialog2;//未联网时提示本地保存
    private boolean isConnected = true;//记录当前网络是否连接
    private LinearLayout activity_exam_detail_imageview3;//答题卡
    private ViewGroup root;
    private boolean isVisible = false;

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(ExamDetailActivity.this, "请检查网络连接_Error26", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parseJSON(msg.obj.toString());
                }
            }
        };
        handleSaveTest1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(ExamDetailActivity.this, "请检查网络连接_Error27", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    if (mSelfDialog2 == null) {
                        mSelfDialog2 = new SelfDialog2(ExamDetailActivity.this);
                        mSelfDialog2.setTitle("温馨提示");
                        mSelfDialog2.setMessage("当前无网络！答题记录将保存到本地");
                        mSelfDialog2.setYesOnclickListener("确认", new SelfDialog2.onYesOnclickListener() {
                            @Override
                            public void onYesClick() {
                                finish();
                            }
                        });
                        WindowManager.LayoutParams params = mSelfDialog2.getWindow().getAttributes();
                        params.width = (int) (ScreenUtils.getWidth(ExamDetailActivity.this) * 0.7);
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        mSelfDialog2.getWindow().setAttributes(params);
                    }
                    mSelfDialog2.show();
                    if (countDownTimer != null) {
                        countDownTimer.cancel();//暂停计时
                    }
                } else {
                    parseJSONSave1Test(msg.obj.toString());
                }
            }
        };
        handleSaveTest2 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(ExamDetailActivity.this, "请检查网络连接_Error28", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    if (mSelfDialog2 == null) {
                        mSelfDialog2 = new SelfDialog2(ExamDetailActivity.this);
                        mSelfDialog2.setTitle("温馨提示");
                        mSelfDialog2.setMessage("当前无网络！答题记录将保存到本地");
                        mSelfDialog2.setYesOnclickListener("确认", new SelfDialog2.onYesOnclickListener() {
                            @Override
                            public void onYesClick() {
                                finish();
                            }
                        });
                        WindowManager.LayoutParams params = mSelfDialog2.getWindow().getAttributes();
                        params.width = (int) (ScreenUtils.getWidth(ExamDetailActivity.this) * 0.7);
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        mSelfDialog2.getWindow().setAttributes(params);
                    }
                    mSelfDialog2.show();
                    if (countDownTimer != null) {
                        countDownTimer.cancel();//暂停计时
                    }
                } else {
                    parseJSONSave2Test(msg.obj.toString());
                }
            }
        };
    }

    //提交答案
    private void parseJSONSave2Test(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//提交成功
                //保存不提交成功，删除本地的异常记录文件
                File file = new File("/data/data/" + getPackageName() + "/shared_prefs", fileName + ".xml");
                if (file.exists()) {
                    file.delete();
                }
                JSONObject dataObject = jsonObject.getJSONObject("data");
                String id = dataObject.getString("id");
//                String sid = dataObject.getString("sid");
                String msg = jsonObject.getString("msg");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                //开启解析页面
                final Intent intentExamResultActivity = new Intent(this, ExamResultActivity.class);
                intentExamResultActivity.putExtra("id", id);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //提交成功，关闭试题页面
                        startActivity(intentExamResultActivity);
                        setResult(RESULT_OK);
                        finish();
                    }
                }, 1000);
            } else {
                String msg = jsonObject.getString("msg");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                if (mSelfDialog2 == null) {
                    mSelfDialog2 = new SelfDialog2(ExamDetailActivity.this);
                    mSelfDialog2.setTitle("温馨提示");
                    mSelfDialog2.setMessage("提交异常！答题记录将保存到本地");
                    mSelfDialog2.setYesOnclickListener("确认", new SelfDialog2.onYesOnclickListener() {
                        @Override
                        public void onYesClick() {
                            finish();
                        }
                    });
                    WindowManager.LayoutParams params = mSelfDialog2.getWindow().getAttributes();
                    params.width = (int) (ScreenUtils.getWidth(ExamDetailActivity.this) * 0.7);
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mSelfDialog2.getWindow().setAttributes(params);
                }
                mSelfDialog2.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (countDownTimer != null) {
                countDownTimer.cancel();//暂停计时
            }
        }
    }

    //发送保存不提交答案
    private void parseJSONSave1Test(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            String msg = jsonObject.optString("msg");
            if (status == 1) {
                //保存不提交成功，删除本地的异常记录文件
                File file = new File("/data/data/" + getPackageName() + "/shared_prefs", fileName + ".xml");
                if (file.exists()) {
                    file.delete();
                }
                //提交成功，延时关闭试题页面
                Toast.makeText(this, "保存做题记录成功", Toast.LENGTH_SHORT).show();
                if (countDownTimer != null) {
                    countDownTimer.cancel();//暂停计时
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK);
                        finish();
                    }
                }, 1000);
            } else {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                //如果保存不提交的时候，uid为空，那么提交异常（可能是因为账号被顶下线）
                if (Util.getUid().equals("")) {
                    if (mSelfDialog2 == null) {
                        mSelfDialog2 = new SelfDialog2(ExamDetailActivity.this);
                        mSelfDialog2.setTitle("温馨提示");
                        mSelfDialog2.setMessage("保存异常！答题记录将保存到本地");
                        mSelfDialog2.setYesOnclickListener("确认", new SelfDialog2.onYesOnclickListener() {
                            @Override
                            public void onYesClick() {
                                finish();
                            }
                        });
                        WindowManager.LayoutParams params = mSelfDialog2.getWindow().getAttributes();
                        params.width = (int) (ScreenUtils.getWidth(ExamDetailActivity.this) * 0.7);
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        mSelfDialog2.getWindow().setAttributes(params);
                    }
                    mSelfDialog2.show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseJSON(final String s) {
//        Util.d("021103", s);
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {//解析成功
                //解析data
                JSONObject dataObject = jsonObject.getJSONObject("data");
                dataMap = new TreeMap<>();
                dataMap.put("id", dataObject.getString("id"));
                dataMap.put("name", dataObject.getString("name"));
                dataMap.put("pass", dataObject.getString("pass"));
                dataMap.put("inc_type", dataObject.getString("inc_type"));
                dataMap.put("answer_time", dataObject.getString("answer_time"));
                dataMap.put("time", dataObject.getString("time"));
                dataMap.put("total", dataObject.getString("total"));
                dataMap.put("scores", dataObject.getString("scores"));
                if (dataObject.has("radio")) {//如果有单选题
                    JSONObject radioObject = dataObject.getJSONObject("radio");
//                    Map<String, String> radioMap = new HashMap<>();
//                    radioMap.put("name", radioObject.getString("name"));
//                    radioMap.put("total", radioObject.getString("total"));
//                    radioMap.put("score", radioObject.getString("score"));
                    if (radioObject.has("lists")) {
                        JSONArray listsArray = radioObject.getJSONArray("lists");
                        listsList = new ArrayList<>();
                        int write = 0;//临时变量，记录服务器返回的已经答题的个数
                        SharedPreferences.Editor editor = getSharedPreferences(fileName, MODE_PRIVATE).edit();
//                        editor.clear().apply();//使用前清空本地缓存
                        for (int i = 0; i < listsArray.length(); i++) {
                            JSONObject listsObject = listsArray.getJSONObject(i);
                            Map<String, String> listsMap = new HashMap<>();
                            listsMap.put("id", listsObject.getString("id"));
                            listsMap.put("no", listsObject.getString("no"));
                            listsMap.put("no_id", listsObject.getString("no_id"));
                            listsMap.put("subject", listsObject.getString("subject"));

                            //题干内容转换br为\n
//                            String subjectString = listsObject.getString("subject");
//                            String goingToReplace = "<br/>";
//                            String newSubjectString = subjectString.replaceAll(goingToReplace, "\n");
//                            listsMap.put("subject", newSubjectString);

                            //切换为大写
                            if (loadLocalFile) {//从异常文件中读取选项状态
                                SharedPreferences sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
                                String checked = sharedPreferences.getString(listsObject.getString("no_id"), "");
                                if (!checked.equals("")) {
                                    ++write;
                                }
                                listsMap.put("checked", checked.toUpperCase());
                            } else {//异常文件不存在，从服务器返回的数据读取选项状态，同时写入本地文件做备份
                                listsMap.put("checked", listsObject.getString("checked").toUpperCase());
                                //TODO 如果是服务器继续答题，保存服务器的记录
                                String checked = listsObject.getString("checked").toUpperCase();
                                editor.putString(listsObject.getString("no_id"), checked);
                                if (!checked.equals("")) {
                                    ++write;
                                }
                            }
                            editor.apply();
                            //如果有题干图片的话
                            if (listsObject.has("sub_img")) {
                                JSONArray sub_imgArray = listsObject.getJSONArray("sub_img");
                                listsMap.put("sub_img", sub_imgArray.toString());
                            }
                            JSONArray option_listArray = listsObject.getJSONArray("option_list");
                            listsMap.put("option_list", option_listArray.toString());
                            listsList.add(listsMap);
                        }
                        editor.putInt("write", write);
                        editor.apply();
                    }

                    //初始化计时相关
                    String answer_time = dataMap.get("answer_time");

                    if (loadLocalFile) {//从异常文件中读取选项状态
                        SharedPreferences sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
                        answer_time = sharedPreferences.getString("time", "");
                    } else {
                        SharedPreferences.Editor editor = getSharedPreferences(fileName, MODE_PRIVATE).edit();
                        editor.putString("time", answer_time);
                        editor.apply();
                    }

                    int mCount = 0;
                    if (answer_time != null) {
                        mCount = Integer.valueOf(answer_time);//需要计时的总秒数
                    }
                    int totalMinutes = mCount / 60;//需要计时的总分钟数
                    int second = mCount % 60;//秒数
                    int hour = totalMinutes / 60;//小时数
                    int minute = totalMinutes % 60;//分钟数

                    String hourString = String.valueOf(hour);
                    String minuteString = String.valueOf(minute);
                    String secondString = String.valueOf(second);

                    //处理显示2位数字的文字
                    if (hour / 10 == 0) {
                        hourString = "0" + hour;
                    }
                    if (minute / 10 == 0) {
                        minuteString = "0" + minute;
                    }
                    if (second / 10 == 0) {
                        secondString = "0" + second;
                    }

                    //初始化ViewPager
                    final List<Fragment> fragmentList = new ArrayList<>();

                    for (int i = 0; i < listsList.size(); i++) {
                        ExamDetailActivity_ViewPager1_Fragment fragment = new ExamDetailActivity_ViewPager1_Fragment();

                        List_Bean bean = new List_Bean();
                        bean.setMap(listsList.get(i));

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("bean", bean);
                        fragment.setArguments(bundle);
                        fragmentList.add(fragment);

                        //计算最小答题页面
                        if (!listsList.get(i).get("checked").equals("")) {
                            lastAnswerPage = i;
                        }

                        //初始化服务器返回的答题数据
                        String key = listsList.get(i).get("no_id");
                        if (!listsList.get(i).get("checked").equals("")) {
                            String val = listsList.get(i).get("checked");
                            answerMap.put(Integer.parseInt(key), val);
                        }

                    }

                    ExamDeatilActivity_ViewPager1_Adapter adapter = new ExamDeatilActivity_ViewPager1_Adapter(getSupportFragmentManager(), fragmentList);
                    activity_exam_detail_viewpager1.setAdapter(adapter);
                    activity_exam_detail_viewpager1.setOffscreenPageLimit(2);
                    //跳转到最后一道做的题的页面，方便继续做题
                    activity_exam_detail_viewpager1.setCurrentItem(lastAnswerPage);
                    activity_exam_detail_viewpager1.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            if (position == 0) {
                                if (listsList.size() == 1) {
                                    activity_exam_detail_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                                    activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                                } else {
                                    activity_exam_detail_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                                    activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                                }
                            } else {//进入考试页面的position不为0说明是继续答题模式，跳转到了上次做的最后一题的位置
                                if (position < listsList.size() - 1) {//如果不是最后一题
                                    activity_exam_detail_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                                    activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                                } else {//上次做到了最后一题
                                    activity_exam_detail_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                                    activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                                }
                            }
                        }

                        @Override
                        public void onPageSelected(int position) {//根据页面变化，设置下方左右箭头颜色
                            if (listsList.size() > 1) {
                                if (position == 0) {
                                    activity_exam_detail_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                                    activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                                } else if (position == listsList.size() - 1) {
                                    activity_exam_detail_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                                    activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                                } else {
                                    activity_exam_detail_imageview1.setImageResource(R.drawable.activity_exam_detail_left_orange);
                                    activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_orange);
                                }
                            } else {
                                activity_exam_detail_imageview1.setImageResource(R.drawable.activity_exam_detail_left_gray);
                                activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                            }
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });

                    //初始倒计时时间
                    activity_exam_detail_textview1.setText(hourString + ":" + minuteString + ":" + secondString);
                    countDownTimer = new CountDownTimer(mCount * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            //剩余的秒数
                            countDownMillis = millisUntilFinished;
                            //根据剩余秒数算出总分钟数
                            long totalMinute = (millisUntilFinished / 1000) / 60;//剩余总分钟数
                            long second = (millisUntilFinished / 1000) % 60;//剩余秒数
                            long totalHour = totalMinute / 60;//剩余总小时数
                            long minute = totalMinute % 60;//剩余分钟数

                            String totalHourString = String.valueOf(totalHour);
                            String minuteString = String.valueOf(minute);
                            String secondString = String.valueOf(second);

                            //处理显示2位数字的文字
                            if (totalHour / 10 == 0) {
                                totalHourString = "0" + totalHour;
                            }
                            if (minute / 10 == 0) {
                                minuteString = "0" + minute;
                            }
                            if (second / 10 == 0) {
                                secondString = "0" + second;
                            }
                            activity_exam_detail_textview1.setText(totalHourString + ":" + minuteString + ":" + secondString);
                        }

                        @Override
                        public void onFinish() {
                            if (isVisible) {
                                activity_exam_detail_textview1.setText("考试时间到");
                                //强制提交
                                //构建答案JSON
                                Set<Integer> answerKeySet1 = answerMap.keySet();
                                JSONArray answer1 = new JSONArray();
                                for (Object anAnswerKeySet : answerKeySet1) {
                                    int key = (int) anAnswerKeySet;
                                    String val = answerMap.get(key).toLowerCase();
                                    String answerString = "{\"key\":\"" + key + "\",\"val\":\"" + val + "\"}";
                                    try {
                                        JSONObject jsonObject = new JSONObject(answerString);
                                        answer1.put(jsonObject);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (answerKeySet1.isEmpty()) {
                                    mSelfDialog = new SelfDialog(ExamDetailActivity.this);
                                    mSelfDialog.setTitle("考试时间到，您未答题");
                                    mSelfDialog.setMessage("是否重新答题");
                                    mSelfDialog.setYesOnclickListener("确认", new SelfDialog.onYesOnclickListener() {
                                        @Override
                                        public void onYesClick() {
                                            countDownTimer.start();
                                            mSelfDialog.dismiss();
                                        }
                                    });
                                    mSelfDialog.setNoOnclickListener("取消", new SelfDialog.onNoOnclickListener() {
                                        @Override
                                        public void onNoClick() {
                                            finish();
                                            mSelfDialog.dismiss();
                                        }
                                    });
                                    WindowManager.LayoutParams params = mSelfDialog.getWindow().getAttributes();
                                    params.width = (int) (ScreenUtils.getWidth(ExamDetailActivity.this) * 0.7);
                                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                    mSelfDialog.getWindow().setAttributes(params);
                                    mSelfDialog.show();
                                } else {
                                    if (isConnected) {
                                        String uid = Util.getUid();
                                        //发送答案
                                        Map<String, String> paramsMap2 = new HashMap<>();
                                        paramsMap2.put("sid", sid);
                                        paramsMap2.put("uid", uid);
                                        paramsMap2.put("tid", tid);
                                        paramsMap2.put("state", "1");

                                        long timelest1 = countDownMillis / 1000;//剩余时间
                                        long examtime1 = Long.parseLong(dataMap.get("answer_time"));//考试要求时间/秒
                                        long result1 = examtime1 - timelest1;

                                        paramsMap2.put("times", result1 + "");
                                        paramsMap2.put("answers", answer1.toString());
                                        RequestURL.sendPOST("https://app.feimayun.com/Tiku/saveTest", handleSaveTest2, paramsMap2, ExamDetailActivity.this);//提交
                                    } else {
                                        if (mSelfDialog2 == null) {
                                            mSelfDialog2 = new SelfDialog2(ExamDetailActivity.this);
                                            mSelfDialog2.setTitle("温馨提示");
                                            mSelfDialog2.setMessage("当前无网络！答题记录将保存到本地");
                                            mSelfDialog2.setYesOnclickListener("确认", new SelfDialog2.onYesOnclickListener() {
                                                @Override
                                                public void onYesClick() {
                                                    finish();
                                                }
                                            });
                                            WindowManager.LayoutParams params = mSelfDialog2.getWindow().getAttributes();
                                            params.width = (int) (ScreenUtils.getWidth(ExamDetailActivity.this) * 0.7);
                                            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                            mSelfDialog2.getWindow().setAttributes(params);
                                        }
                                        mSelfDialog2.show();
                                    }
                                }
                            } else {
                                finish();
                                //计时器在后台时间到了以后，不提交。回到paperlist页面刷新一下
                                if (refreshPaper != null) {
                                    refreshPaper.sendEmptyMessage(0);
                                }
                            }
                        }
                    }.start();//开始计时
                } else if (dataObject.has("short")) {//简答题临时对策
                    Toast.makeText(this, "请到PC端答题~", Toast.LENGTH_SHORT).show();
//                    activity_exam_detail_textview2
//                            activity_exam_detail_textview3
                    if (activity_exam_detail_textview2 != null) {
                        activity_exam_detail_textview2.setClickable(false);
                    }
                    if (activity_exam_detail_textview3 != null) {
                        activity_exam_detail_textview3.setClickable(false);
                    }
                    if (activity_exam_detail_imageview2 != null) {
                        activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                    }
                } else { //论文题没有标签，临时处理
                    Toast.makeText(this, "请到PC端答题~", Toast.LENGTH_SHORT).show();
//                    activity_exam_detail_textview2
//                            activity_exam_detail_textview3
                    if (activity_exam_detail_textview2 != null) {
                        activity_exam_detail_textview2.setClickable(false);
                    }
                    if (activity_exam_detail_textview3 != null) {
                        activity_exam_detail_textview3.setClickable(false);
                    }
                    if (activity_exam_detail_imageview2 != null) {
                        activity_exam_detail_imageview2.setImageResource(R.drawable.activity_exam_detail_right_gray);
                    }
                }
            } else {
                haveData = false;
                Toast.makeText(this, jsonObject.get("msg").toString(), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_exam_detail);
        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            SharedPreferences spf = getSharedPreferences("my_guide_record", Context.MODE_PRIVATE);
            boolean hasShown = spf.getBoolean("has_shown_exam", false);
            root = getWindow().getDecorView().findViewById(R.id.root);
            if (!hasShown) {
                MyGuideView myGuideView = new MyGuideView(this);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                root.addView(myGuideView, params);
            }
            //记录下来
            SharedPreferences.Editor editor = spf.edit();
            editor.putBoolean("has_shown_exam", true);
            editor.apply();

            handler();
            Intent intent = getIntent();
            sid = intent.getStringExtra("sid");
            tid = intent.getStringExtra("tid");
            loadLocalFile = intent.getBooleanExtra("loadLocalFile", false);
            String uid = Util.getUid();
            fileName = "paper" + sid + tid + uid;//由题库id+试卷id共同构建的唯一文件名
            initView();
            initData();
        }
    }

    private void initData() {
        String uid = Util.getUid();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("sid", sid);
        paramsMap.put("uid", uid);
        paramsMap.put("tid", tid);
        Log.i("190308", "sid:" + sid + ",uid:" + uid + ",tid:" + tid);
        RequestURL.sendPOST("https://app.feimayun.com/Tiku/tpDetail", handleNetwork, paramsMap, ExamDetailActivity.this);
    }

    private void initView() {
        //左上角返回按钮
        //返回按钮布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);
        //标题
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("考试中心");

        activity_exam_detail_textview1 = findViewById(R.id.activity_exam_detail_textview1);//倒计时
        activity_exam_detail_imageview1 = findViewById(R.id.activity_exam_detail_imageview1);
        activity_exam_detail_imageview2 = findViewById(R.id.activity_exam_detail_imageview2);
        //保存不提交
        activity_exam_detail_textview2 = findViewById(R.id.activity_exam_detail_textview2);
        activity_exam_detail_textview2.setOnClickListener(this);
        //提交
        activity_exam_detail_textview3 = findViewById(R.id.activity_exam_detail_textview3);
        activity_exam_detail_textview3.setOnClickListener(this);
        activity_exam_detail_viewpager1 = findViewById(R.id.activity_exam_detail_viewpager1);
        //左箭头的布局
        RelativeLayout activity_exam_detail_layout3 = findViewById(R.id.activity_exam_detail_layout3);
        activity_exam_detail_layout3.setOnClickListener(this);
        //右箭头的布局
        RelativeLayout activity_exam_detail_layout4 = findViewById(R.id.activity_exam_detail_layout4);
        activity_exam_detail_layout4.setOnClickListener(this);

        activity_exam_detail_imageview3 = findViewById(R.id.activity_exam_detail_imageview3);
        activity_exam_detail_imageview3.setOnClickListener(this);

        progressDialog = new ProgressDialog(ExamDetailActivity.this);
        progressDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_exam_detail_layout3://左箭头布局，上一页
                int position1 = activity_exam_detail_viewpager1.getCurrentItem();
                if (position1 - 1 == -1) {
                    Toast.makeText(this, "已经是第一题了", Toast.LENGTH_SHORT).show();
                } else {
                    activity_exam_detail_viewpager1.setCurrentItem(position1 - 1);
                }
                break;
            case R.id.activity_exam_detail_layout4://右箭头布局，下一页
                int position2 = activity_exam_detail_viewpager1.getCurrentItem();
                if (position2 == listsList.size() - 1) {
                    Toast.makeText(this, "已经是最后一题了", Toast.LENGTH_SHORT).show();
                } else {
                    activity_exam_detail_viewpager1.setCurrentItem(position2 + 1);
                }
                break;
            case R.id.activity_exam_detail_textview2://保存不提交
                if (haveData) {//如果购买了试卷，才能提交，否则只提示
                    if (isConnected) {
                        //构建答案JSON
                        Set<Integer> answerKeySet = answerMap.keySet();
                        JSONArray answer = new JSONArray();
                        for (Object anAnswerKeySet : answerKeySet) {
                            int key = (int) anAnswerKeySet;
                            String val = answerMap.get(key).toLowerCase();
                            String answerString = "{\"key\":\"" + key + "\",\"val\":\"" + val + "\"}";
                            try {
                                JSONObject jsonObject = new JSONObject(answerString);
                                answer.put(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        String uid = Util.getUid();
                        //TODO 发送答案
                        Map<String, String> paramsMap = new HashMap<>();
                        paramsMap.put("sid", sid);
                        paramsMap.put("uid", uid);
                        paramsMap.put("tid", tid);
                        paramsMap.put("state", "0");

                        long timelest = countDownMillis / 1000;//剩余时间
                        long examtime = Long.parseLong(Objects.requireNonNull(dataMap.get("answer_time")));//考试要求时间/秒
                        long result = examtime - timelest;

                        paramsMap.put("times", result + "");
                        paramsMap.put("answers", answer.toString());
                        Util.d("052801", "times:" + result);
                        RequestURL.sendPOST("https://app.feimayun.com/Tiku/saveTest", handleSaveTest1, paramsMap, ExamDetailActivity.this);//保存不提交
                    } else {
                        if (mSelfDialog2 == null) {
                            mSelfDialog2 = new SelfDialog2(ExamDetailActivity.this);
                            mSelfDialog2.setTitle("温馨提示");
                            mSelfDialog2.setMessage("当前无网络！答题记录将保存到本地");
                            mSelfDialog2.setYesOnclickListener("确认", new SelfDialog2.onYesOnclickListener() {
                                @Override
                                public void onYesClick() {
                                    finish();
                                }
                            });
                            WindowManager.LayoutParams params = mSelfDialog2.getWindow().getAttributes();
                            params.width = (int) (ScreenUtils.getWidth(ExamDetailActivity.this) * 0.7);
                            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            mSelfDialog2.getWindow().setAttributes(params);
                        }
                        mSelfDialog2.show();
                        if (countDownTimer != null) {
                            countDownTimer.cancel();//暂停计时
                        }
                    }
                } else {
                    Toast.makeText(this, "您未购买该试卷所在的课程或题库~", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.activity_exam_detail_textview3://提交按钮
                onTijiao(v);
                break;
            case R.id.headtitle_layout://左上角返回按钮，共用提交按钮的方法
//                onTijiao(v);
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.activity_exam_detail_imageview3://答题卡
                mList.clear();
                for (int i = 0; i < listsList.size(); i++) {
                    int no_id = Integer.parseInt(listsList.get(i).get("no_id"));
                    if (answerMap.get(no_id) == null) {
                        mList.add(false);
                    } else {
                        mList.add(true);
                    }
                }
                View view = LayoutInflater.from(this).inflate(R.layout.answer_card, root, false);
                RecyclerView answercard_recyclerview = view.findViewById(R.id.answercard_recyclerview);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5, LinearLayoutManager.VERTICAL, false);
                GridItemDecoration divider = new GridItemDecoration.Builder(this)
                        .setVerticalSpan(R.dimen.dp30)
                        .setHorizontalSpan(R.dimen.dp12)
                        .setColorResource(R.color.white)
                        .setShowLastLine(true)
                        .build();
                answercard_recyclerview.addItemDecoration(divider);
                answercard_recyclerview.setLayoutManager(gridLayoutManager);
                ExamCardAdapter adapter = new ExamCardAdapter(this, mList);
                answercard_recyclerview.setAdapter(adapter);
                adapter.setOnItemClickListener(new ExamCardAdapter.OnItemClickListener() {
                    @Override
                    public void onItemCLick(View view, int position) {
                        activity_exam_detail_viewpager1.setCurrentItem(position);
                    }
                });

                new TDialog.Builder(getSupportFragmentManager())
                        .setLayoutRes(R.layout.answer_card)
                        .setDialogView(view)
                        .setScreenHeightAspect(this, 0.6f)
                        .setScreenWidthAspect(this, 1)
                        .setGravity(Gravity.BOTTOM)
                        .setDialogAnimationRes(R.style.animate_dialog)
                        .create()
                        .show();

                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    //提交按钮的点击事件，上下返回键、提交按钮共用
    public void onTijiao(View view1) {
        if (haveData) {//如果购买了试卷，才能提交，否则只提示
            if (isConnected) {
                //构建提交Dialog
                View v = LayoutInflater.from(this).inflate(R.layout.dialog_call, root, false);//复用call布局
                TextView dialog_call_textview1 = v.findViewById(R.id.dialog_call_textview1);
                TextView dialog_call_textview2 = v.findViewById(R.id.dialog_call_textview2);

                //已做题数
                int doneTotal = answerMap.size();
                //总题数
                int Alltotal = listsList.size();

                if (doneTotal == 0) {//没做题直接提示
                    Toast.makeText(this, "请答题后交卷~", Toast.LENGTH_SHORT).show();
                    return;
                } else if (doneTotal == Alltotal) {
                    dialog_call_textview1.setText("您已完成所有题目~");
                } else {
                    dialog_call_textview1.setText("已做" + doneTotal + "题，还有" + (Alltotal - doneTotal) + "题未做");
                }
                dialog_call_textview2.setText("是否要交卷？");

                new TDialog.Builder(getSupportFragmentManager())
                        .setDialogView(v)
                        .setScreenWidthAspect(this, 0.7f)
                        .addOnClickListener(R.id.dialog_call_confirm, R.id.dialog_call_cancel)
                        .setOnViewClickListener(new OnViewClickListener() {
                            @Override
                            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                switch (view.getId()) {
                                    case R.id.dialog_call_cancel:
                                        tDialog.dismiss();
                                        break;
                                    case R.id.dialog_call_confirm://确定提交
                                        //构建答案JSON
                                        Set<Integer> answerKeySet1 = answerMap.keySet();
                                        JSONArray answer1 = new JSONArray();
                                        for (Object anAnswerKeySet : answerKeySet1) {
                                            int key = (int) anAnswerKeySet;
                                            String val = answerMap.get(key).toLowerCase();
                                            String answerString = "{\"key\":\"" + key + "\",\"val\":\"" + val + "\"}";
                                            try {
                                                JSONObject jsonObject = new JSONObject(answerString);
                                                answer1.put(jsonObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        String uid = Util.getUid();
                                        //TODO 发送答案
                                        Map<String, String> paramsMap2 = new HashMap<>();
                                        paramsMap2.put("sid", sid);
                                        paramsMap2.put("uid", uid);
                                        paramsMap2.put("tid", tid);
                                        paramsMap2.put("state", "1");

                                        long timelest1 = countDownMillis / 1000;//剩余时间
                                        long examtime1 = Long.parseLong(dataMap.get("answer_time"));//考试要求时间/秒
                                        long result1 = examtime1 - timelest1;

                                        paramsMap2.put("times", result1 + "");
                                        paramsMap2.put("answers", answer1.toString());
                                        RequestURL.sendPOST("https://app.feimayun.com/Tiku/saveTest", handleSaveTest2, paramsMap2, ExamDetailActivity.this);//提交
                                        tDialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
            } else {
                if (mSelfDialog2 == null) {
                    mSelfDialog2 = new SelfDialog2(ExamDetailActivity.this);
                    mSelfDialog2.setTitle("温馨提示");
                    mSelfDialog2.setMessage("当前无网络！答题记录将保存到本地");
                    mSelfDialog2.setYesOnclickListener("确认", new SelfDialog2.onYesOnclickListener() {
                        @Override
                        public void onYesClick() {
                            finish();
                        }
                    });
                    WindowManager.LayoutParams params = mSelfDialog2.getWindow().getAttributes();
                    params.width = (int) (ScreenUtils.getWidth(ExamDetailActivity.this) * 0.7);
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mSelfDialog2.getWindow().setAttributes(params);
                }
                mSelfDialog2.show();
                if (countDownTimer != null) {
                    countDownTimer.cancel();//暂停计时
                }
            }
        } else {
            Toast.makeText(this, "您未购买该试卷所在的课程或题库~", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        //新题点进去啥也不干，走的时候清空一下
        SharedPreferences sharedPreferences = getSharedPreferences(fileName, MODE_PRIVATE);
        int write = sharedPreferences.getInt("write", 0);
        if (write == 0) {
            File file = new File("/data/data/" + getPackageName() + "/shared_prefs", fileName + ".xml");
            if (file.exists()) {
                file.delete();
            }
            setResult(RESULT_OK);
        }
        isVisible = false;
    }

    public Map<Integer, String> getAnswerMap() {
        return answerMap;
    }

    @Override
    protected boolean needRegisterNetworkChangeObserver() {
        return true;
    }

    @Override
    public void onNetDisconnected() {
        isConnected = false;
    }

    @Override
    public void onNetConnected(NetworkInfo networkInfo) {
        isConnected = true;
    }
}
