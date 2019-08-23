package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.CourseList_ViewPager_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.fragment.CourseListFragment;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.view.CourseListViewPager;
import cn.aura.feimayun.view.ProgressDialog;

/**
 * 课程列表页面
 */
public class CourseListActivity extends BaseActivity {
    //接收后台数据的handler
    private static Handler handleNetWork;
    TabLayout courselist_tabLayout;
    CourseListViewPager courselist_viewpager;
    private ProgressDialog progressDialog;
    private TextView headtitle_textview;//标题
    //记录全部课程页面传来的信息，用于页面跳转
    private String series;
    private String id;
    private List<Fragment> fragmentList;
    //lmList_List
    private List<Map<String, String>> lmList_List;
    //记录首次跳转的页面
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            Intent intent = getIntent();
            series = intent.getStringExtra("series");
            id = intent.getStringExtra("id");
            //初始化hander
            hander();
            initData();
            initView();
        }

    }

    private void initView() {
        headtitle_textview = findViewById(R.id.headtitle_textview);//标题
        courselist_tabLayout = findViewById(R.id.courselist_tabLayout);
        courselist_viewpager = findViewById(R.id.courselist_viewpager);
        //页面顶部左上角的返回按钮的布局及其点击事件
        //返回按钮布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(series, id);

        RequestURL.sendPOST("https://app.feimayun.com/Lesson/index", handleNetWork, paramsMap, CourseListActivity.this);
    }

    @SuppressLint("HandlerLeak")
    private void hander() {
        handleNetWork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(CourseListActivity.this, "请检查网络连接_Error10", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } else {
                    parseJSON(msg.obj.toString());
                }
            }
        };
    }

    //解析二级id
    private void parseJSON(String data) {
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                fragmentList = new ArrayList<>();
                //存放解析的lmList字段
                lmList_List = new ArrayList<>();

                //解析lmList，变量名后的s代表second二级
                JSONArray jsonArray = jsonObject.getJSONArray("lmList");
                //这里只可能获取到一个Object，所以我就不循环了
                JSONObject object_s = jsonArray.getJSONObject(0);

                Map<String, String> map = new HashMap<>();
                map.put("id", object_s.getString("id"));
                headtitle_textview.setText(object_s.getString("name"));
                map.put("name", "全部");
                map.put("pid", object_s.getString("pid"));
                lmList_List.add(map);

                if (object_s.has("children")) {
                    //解析3级目录的信息，变量名后的t代表third三级
                    JSONArray jsonArray_children = object_s.getJSONArray("children");

                    for (int i = 0; i < jsonArray_children.length(); i++) {
                        JSONObject jsonObject_children = jsonArray_children.getJSONObject(i);
                        Map<String, String> children_map = new HashMap<>();
                        children_map.put("id", jsonObject_children.getString("id"));
                        children_map.put("name", jsonObject_children.getString("name"));
                        children_map.put("pid", jsonObject_children.getString("pid"));
                        lmList_List.add(children_map);
                    }
                }
            }
            initViewPager();
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

    //初始化ViewPager
    private void initViewPager() {
        for (int i = 0; i < lmList_List.size(); i++) {
            //根据全部课程按钮的id判断跳转页面
            if (lmList_List.get(i).get("id").equals(id)) {
                currentPage = i;
            }

            Bundle bundle = new Bundle();
            List_Bean bean = new List_Bean();
            bean.setList(lmList_List);
            bundle.putSerializable("bean", bean);
            bundle.putInt("position", i);
            CourseListFragment fragment = CourseListFragment.newInstance(bundle);
            fragmentList.add(fragment);
        }

        CourseList_ViewPager_Adapter adapter = new CourseList_ViewPager_Adapter(getSupportFragmentManager(), fragmentList, lmList_List);
        courselist_viewpager.setAdapter(adapter);
        courselist_viewpager.setOffscreenPageLimit(2);
        courselist_tabLayout.setupWithViewPager(courselist_viewpager);
        //页面跳转
        courselist_viewpager.setCurrentItem(currentPage);
        courselist_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPage = position;
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });
    }
}
