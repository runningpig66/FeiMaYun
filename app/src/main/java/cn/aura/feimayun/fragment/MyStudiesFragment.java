package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;
import com.vhall.business.VhallSDK;

import java.util.ArrayList;
import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.InformationActivity;
import cn.aura.feimayun.activity.LoginActivity;
import cn.aura.feimayun.activity.MainActivity;
import cn.aura.feimayun.adapter.MyStudies_ViewPager_Adapter;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.Util;

import static android.content.Context.MODE_PRIVATE;

/**
 * 描述：我的学习页面
 */
public class MyStudiesFragment extends Fragment implements View.OnClickListener {
    //登录成功后用户课程的处理
    public static Handler handleLogin;
    public static Handler handleLogout;
    public static Handler handleUserInfo;
    //判断下载是否成功
    public boolean isRequestSuccess = true;
    //todo    private MyStudiesFragment_ListView_Adapter adapter;
    private MainActivity mainActivity;

    //头像
    private ImageView fragment_my_studies_imageView1;
    //立即登录
    private TextView fragment_my_studies_textView1;
    //登录后没有课程的展示图片
//    private RelativeLayout fragment_my_studies_layout1;
    //未登录的下方界面
    private LinearLayout fragment_my_studies_level2;
    //右上角的设置按钮
    private ImageView fragment_my_studies_imageView0;
    private TabLayout my_studies_tablayout;
    private ViewPager my_studies_viewpager;
    private List<Fragment> fragments = new ArrayList<>();
    private MyStudiesFragment1 myStudiesFragment1 = null;
    private MyStudiesFragment2 myStudiesFragment2 = null;

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleLogin = new Handler() {//接收登录成功
            @Override
            public void handleMessage(Message msg) {
                my_studies_tablayout.setVisibility(View.VISIBLE);
                my_studies_viewpager.setVisibility(View.VISIBLE);
                fragment_my_studies_level2.setVisibility(View.GONE);

                //通知进行刷新
                if (myStudiesFragment1 != null) {
                    if (myStudiesFragment1.handleRefresh != null) {
                        myStudiesFragment1.handleRefresh.obtainMessage().sendToTarget();
                    }
                }
                if (myStudiesFragment2 != null) {
                    if (myStudiesFragment2.handleRefresh != null) {
                        myStudiesFragment2.handleRefresh.obtainMessage().sendToTarget();
                    }
                }
            }
        };
        handleLogout = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //执行退出登录逻辑,恢复界面
                SharedPreferences.Editor editor = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE).edit();
                editor.clear().apply();
                if (VhallSDK.isLogin()) {
                    VhallSDK.logout();
                }
                //退出登录隐藏设置按钮
                fragment_my_studies_imageView0.setVisibility(View.GONE);
                if (Util.isOnMainThread()) {
                    RequestOptions options = new RequestOptions().fitCenter();
                    Glide.with(MyApplication.context).load(R.drawable.data_head).apply(options).into(fragment_my_studies_imageView1);
                }
                fragment_my_studies_textView1.setText("立即登录");
                fragment_my_studies_level2.setVisibility(View.VISIBLE);
                my_studies_tablayout.setVisibility(View.INVISIBLE);
                my_studies_viewpager.setVisibility(View.INVISIBLE);
                fragment_my_studies_textView1.setClickable(true);
            }
        };
        handleUserInfo = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE);
                String real_name = sharedPreferences.getString("real_name", "");
                String avater = sharedPreferences.getString("avater", "");
                //初始化用户界面和个人课程listview
                fragment_my_studies_imageView0.setVisibility(View.VISIBLE);
                //把原来的立即登录按钮设置为不可点击，并将字符设置为用户名，设置用户头像
//                fragment_my_studies_imageView1.setClickable(false);
                fragment_my_studies_textView1.setClickable(false);
                fragment_my_studies_textView1.setText(real_name);
                if (Util.isOnMainThread()) {
                    Glide.with(MyApplication.context).load(avater).into(fragment_my_studies_imageView1);
                }
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_studies, container, false);
        fragment_my_studies_imageView1 = view.findViewById(R.id.fragment_my_studies_imageView1);
        fragment_my_studies_textView1 = view.findViewById(R.id.fragment_my_studies_textView1);
        //去发现感兴趣的内容
        TextView fragment_my_studies_textView2 = view.findViewById(R.id.fragment_my_studies_textView2);
        fragment_my_studies_level2 = view.findViewById(R.id.fragment_my_studies_level2);
        fragment_my_studies_imageView0 = view.findViewById(R.id.fragment_my_studies_imageView0);
        fragment_my_studies_imageView1.setOnClickListener(this);
        fragment_my_studies_textView1.setOnClickListener(this);
        fragment_my_studies_textView2.setOnClickListener(this);
        fragment_my_studies_imageView0.setOnClickListener(this);
        my_studies_tablayout = view.findViewById(R.id.my_studies_tablayout);
        my_studies_viewpager = view.findViewById(R.id.my_studies_viewpager);
        initFragment();
        if (!Util.getUid().equals("")) {
            SharedPreferences sp = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE);
            String real_name = sp.getString("real_name", "");
            String avater = sp.getString("avater", "");
            fragment_my_studies_textView1.setText(real_name);
            fragment_my_studies_textView1.setClickable(false);
            if (Util.isOnMainThread()) {
                RequestOptions options = new RequestOptions().fitCenter();
                Glide.with(MyApplication.context).load(avater).apply(options).into(fragment_my_studies_imageView1);
            }
            //uid不为空显示fragment
            my_studies_tablayout.setVisibility(View.VISIBLE);
            my_studies_viewpager.setVisibility(View.VISIBLE);
            fragment_my_studies_level2.setVisibility(View.GONE);
        }
        return view;
    }

    private void initFragment() {
        myStudiesFragment1 = new MyStudiesFragment1();
        myStudiesFragment2 = new MyStudiesFragment2();
        fragments.clear();
        fragments.add(myStudiesFragment1);
        fragments.add(myStudiesFragment2);
        MyStudies_ViewPager_Adapter viewPager_adapter = new MyStudies_ViewPager_Adapter(getChildFragmentManager(), fragments);
        my_studies_viewpager.setAdapter(viewPager_adapter);
        my_studies_viewpager.setOffscreenPageLimit(2);
        my_studies_tablayout.setupWithViewPager(my_studies_viewpager);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_my_studies_imageView1://头像
                String uid = Util.getUid();
                if (uid.equals("")) {//uid为空则登录
                    Intent intentLoginActivity = new Intent(mainActivity, LoginActivity.class);
                    startActivity(intentLoginActivity);
                } else {//uid不为空则跳转到个人资料页面，个人资料页面再次点头像为更换头像
                    Intent intentInformationActivity = new Intent(mainActivity, InformationActivity.class);
                    SharedPreferences sp = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE);
                    String real_name = sp.getString("real_name", "");
                    String phone = sp.getString("phone", "");
                    String avater = sp.getString("avater", "");
                    intentInformationActivity.putExtra("real_name", real_name);
                    intentInformationActivity.putExtra("phone", phone);
                    intentInformationActivity.putExtra("avater", avater);
                    startActivity(intentInformationActivity);
                }
                break;
            case R.id.fragment_my_studies_textView1://立即登录
                Intent intentLoginActivity = new Intent(mainActivity, LoginActivity.class);
                startActivity(intentLoginActivity);
                break;
            case R.id.fragment_my_studies_textView2://去发现感兴趣的内容
                View view = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_call, null);
                TextView dialog_call_textview1 = view.findViewById(R.id.dialog_call_textview1);
                TextView dialog_call_textview2 = view.findViewById(R.id.dialog_call_textview2);
                dialog_call_textview1.setText("课程咨询电话：");
                dialog_call_textview2.setText("400-0893-521");
                new TDialog.Builder(mainActivity.getSupportFragmentManager())
                        .setDialogView(view)
                        .setScreenWidthAspect(mainActivity, 0.7f)
                        .addOnClickListener(R.id.dialog_call_confirm, R.id.dialog_call_cancel)
                        .setOnViewClickListener(new OnViewClickListener() {
                            @Override
                            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                switch (view.getId()) {
                                    case R.id.dialog_call_cancel:
                                        tDialog.dismiss();
                                        break;
                                    case R.id.dialog_call_confirm:
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:4000892521"));
                                        startActivity(intent);
                                        tDialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
                break;
            case R.id.fragment_my_studies_imageView0://右上角的设置按钮
                Intent intentInformationActivity = new Intent(mainActivity, InformationActivity.class);

                SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("user_info", MODE_PRIVATE);
                String real_name = sharedPreferences.getString("real_name", "");
                String phone = sharedPreferences.getString("phone", "");
                String avater = sharedPreferences.getString("avater", "");
                intentInformationActivity.putExtra("real_name", real_name);
                intentInformationActivity.putExtra("phone", phone);
                intentInformationActivity.putExtra("avater", avater);
                startActivity(intentInformationActivity);
                break;
        }
    }

}
