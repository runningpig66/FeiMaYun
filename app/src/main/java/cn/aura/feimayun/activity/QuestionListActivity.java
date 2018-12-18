package cn.aura.feimayun.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.QuestionList_ViewPager_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.fragment.MyStuidesInfo22;
import cn.aura.feimayun.view.CourseListViewPager;

public class QuestionListActivity extends BaseActivity {

    //    private ProgressDialog progressDialog;
    private int position;
    private List<String[]> lmListId;//存储1级和2级id
    private List<String[]> lmListName;//存储1级和2级name
    private TabLayout questionlist_tabLayout;
    private CourseListViewPager questionlist_viewpager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        if (MyApplication.APP_STATUS == MyApplication.APP_STATUS_NORMAL) {
            //初始化hander
            initViews();
        }

    }


    private void initViews() {
        //标题
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("答疑中心");
        questionlist_tabLayout = findViewById(R.id.questionlist_tabLayout);
        questionlist_viewpager = findViewById(R.id.questionlist_viewpager);
        //页面顶部左上角的返回按钮的布局及其点击事件
        //返回按钮布局
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        List_Bean list_beanId = (List_Bean) intent.getSerializableExtra("list_beanId");
        lmListId = list_beanId.getListString();
        List_Bean list_beanName = (List_Bean) intent.getSerializableExtra("list_beanName");
        lmListName = list_beanName.getListString();
        position = intent.getIntExtra("position", 0);

        //开始初始化ViewPager和TabLayout
        initViewPager();
    }

    private void initViewPager() {
        String[] lmListIdString = lmListId.get(position);
        List<Fragment> fragmentList = new ArrayList<>();
        for (int i = 0; i < lmListIdString.length; i++) {
            String leimu_1 = lmListIdString[0];
            String leimu_2 = "";
            if (i > 0) {
                leimu_2 = lmListIdString[i];
            }
            Bundle bundle = new Bundle();
            bundle.putString("leimu_1", leimu_1);
            bundle.putString("leimu_2", leimu_2);
            MyStuidesInfo22 fragment = new MyStuidesInfo22();
            fragment.setArguments(bundle);
            fragmentList.add(fragment);
        }
        String[] lmListNameString = lmListName.get(position);
        QuestionList_ViewPager_Adapter adapter = new QuestionList_ViewPager_Adapter(getSupportFragmentManager(), fragmentList, lmListNameString);
        questionlist_viewpager.setAdapter(adapter);
        questionlist_viewpager.setOffscreenPageLimit(fragmentList.size() - 1);
        questionlist_tabLayout.setupWithViewPager(questionlist_viewpager);
        questionlist_viewpager.setCurrentItem(0);
    }

}
