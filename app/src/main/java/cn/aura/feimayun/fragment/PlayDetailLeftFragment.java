package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.PlayDetailActivity;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.Util;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 录播详情界面：简介
 */
public class PlayDetailLeftFragment extends Fragment implements View.OnClickListener {
    private PlayDetailActivity context;
    private Map<String, String> detailDataMap;
    private Map<String, String> detailTeacherMap;
    private List<String> imgList;

    private View view;
    private TextView fragment_playdeatil_left_textView6;

    private Handler handleBuyLessons;

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleBuyLessons = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(context, "请检查网络连接_Error45", Toast.LENGTH_LONG).show();
                } else {
                    parseBuyLessons(msg.obj.toString());
                }
            }
        };
    }

    //解析购买课程
    private void parseBuyLessons(String s) {
        try {
            JSONTokener jsonTokener = new JSONTokener(s);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            int status = jsonObject.getInt("status");
            if (status == 1) {
                Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                //隐藏立即咨询按钮
                fragment_playdeatil_left_textView6.setVisibility(View.GONE);

                //购买成功，更新我的学习已购买课程列表
                MyStudiesFragment.handleLogin.obtainMessage().sendToTarget();//更新个人中心，添加新买的课程
                PlayDetailRightFragment.handleBuy.obtainMessage().sendToTarget();//展开列表
                PlayDetailActivity.handleBuy.obtainMessage().sendToTarget();//开启自动播放
//                context.initData();
            } else {
                Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handle();

        Bundle bundle = getArguments();
        if (bundle != null) {
            detailDataMap = new HashMap<>();
            detailTeacherMap = new HashMap<>();

            //获取序列化的bean对象
            List_Bean bean1 = (List_Bean) bundle.getSerializable("detailDataMap");
            if (bean1 != null) {
                detailDataMap = bean1.getMap();
            }

            List_Bean bean2 = (List_Bean) bundle.getSerializable("detailTeacherMap");
            if (bean2 != null) {
                detailTeacherMap = bean2.getMap();
            }

        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playdeatil_left, container, false);
        initView();
        return view;
    }

    private void initView() {
        CircleImageView fragment_playdeatil_left_circleImageView = view.findViewById(R.id.fragment_playdeatil_left_circleImageView);
        TextView fragment_playdeatil_left_textView1 = view.findViewById(R.id.fragment_playdeatil_left_textView1);
        TextView fragment_playdeatil_left_textView2 = view.findViewById(R.id.fragment_playdeatil_left_textView2);
        TextView fragment_playdeatil_left_textView3 = view.findViewById(R.id.fragment_playdeatil_left_textView3);
        TextView fragment_playdeatil_left_textView4 = view.findViewById(R.id.fragment_playdeatil_left_textView4);
        WebView fragment_playdeatil_left_webview = view.findViewById(R.id.fragment_playdeatil_left_textView5);
        fragment_playdeatil_left_textView6 = view.findViewById(R.id.fragment_playdeatil_left_textView6);
        fragment_playdeatil_left_textView6.setOnClickListener(this);

        Glide.with(context).load(detailTeacherMap.get("biger")).into(fragment_playdeatil_left_circleImageView);
        fragment_playdeatil_left_textView1.setText(detailDataMap.get("name"));
        fragment_playdeatil_left_textView2.setText((int) Double.parseDouble(detailDataMap.get("hours")) + "课时");
        fragment_playdeatil_left_textView3.setText("主讲教师：" + detailTeacherMap.get("name"));
        fragment_playdeatil_left_textView4.setText("讲师简介：" + detailTeacherMap.get("title"));

        String aboutString = detailDataMap.get("about");
        String des2 = Util.getNewContent(aboutString);
        WebSettings webSettings = fragment_playdeatil_left_webview.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        fragment_playdeatil_left_webview.loadData(des2, "text/html; charset=UTF-8", null);

        if (detailDataMap.get("isBuy").equals("0")) {//如果是没有购买
            Double rprice = Double.parseDouble(detailDataMap.get("rprice"));
            if (rprice == 0) {//如果是免费课程，直接购买并播放，并隐藏立即咨询按钮
                String uid = Util.getUid();
                if (uid.equals("")) {
//                    Toast.makeText(context, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
                } else {
                    String ids = detailDataMap.get("id");
                    String order_price = detailDataMap.get("price");
                    String pay_price = detailDataMap.get("rprice");

                    //免费的直接购买
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("uid", uid);
                    paramsMap.put("ids", ids);
                    paramsMap.put("order_price", order_price);
                    paramsMap.put("pay_price", pay_price);
                    RequestURL.sendPOST("https://app.feimayun.com/Lesson/buyLessons", handleBuyLessons, paramsMap);
                }
            } else {//收费课程显示立即咨询按钮
                fragment_playdeatil_left_textView6.setVisibility(View.VISIBLE);
            }
        } else {//购买课程隐藏立即咨询按钮
            fragment_playdeatil_left_textView6.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_playdeatil_left_textView6://立即咨询按钮
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_call, null);
                TextView dialog_call_textview1 = view.findViewById(R.id.dialog_call_textview1);
                TextView dialog_call_textview2 = view.findViewById(R.id.dialog_call_textview2);
                dialog_call_textview1.setText("课程咨询电话：");
                dialog_call_textview2.setText("400-0893-521");
                new TDialog.Builder(getFragmentManager())
                        .setDialogView(view)
                        .setScreenWidthAspect(context, 0.7f)
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
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (PlayDetailActivity) context;
    }
}
