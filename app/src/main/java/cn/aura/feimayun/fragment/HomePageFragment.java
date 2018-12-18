package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.base.TBaseAdapter;
import com.timmy.tdialog.list.TListDialog;
import com.timmy.tdialog.listener.OnViewClickListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.ExamListActivity;
import cn.aura.feimayun.activity.LiveListActivity;
import cn.aura.feimayun.activity.MainActivity;
import cn.aura.feimayun.adapter.Fragment_home_page_viewpager2_Adapter;
import cn.aura.feimayun.adapter.P4_ListView_Adapter;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.util.RequestURL;
import cn.aura.feimayun.util.ScreenUtils;
import cn.aura.feimayun.util.SetHeightUtil;
import cn.aura.feimayun.util.StaticUtil;
import cn.aura.feimayun.view.GlideImageLoader;
import cn.aura.feimayun.view.ProgressDialog;

/**
 * 描述：首页碎片
 */
public class HomePageFragment extends Fragment implements View.OnClickListener {
    //处理网络请求，在initView()方法中请求网络，返回结果会传递到handleNetwork中
    public static Handler handleNetwork;
    public boolean isFirstIn = true;
    //判断下载是否成功
    public boolean isRequestSuccess = false;
    private SmartRefreshLayout homepage_refreshLayout;
    private List<String> dataName;//存放一级name
    private ProgressDialog progressDialog;
    private MainActivity mainActivity;
    //存放后台解析后的data标签的JSON数据
    private List<Map<String, String>> data_mapList;
    private List<String> banner_mapList;
    //顶部轮播图的ViewPager的相关变量

    //存储顶部轮播图片item视图
    private List<String[]> lmListName;//存储1级和2级的name
    private List<String[]> lmListId;//存储1级和2级id
    //碎片视图
    private View view;
    //顶部6个格子
    private ViewPager fragment_home_page_viewpager2;
    //轮播图切换图片的handler
    //用来标识ViewPager页数的小圆点布局
    private LinearLayout fragment_home_page_layout1;
    //存放圆点图片
    private ImageView[] pointImageviews;
    private Banner banner;

    @SuppressLint("HandlerLeak")
    public void handler() {
        //处理网络请求，在initView()方法中请求网络，返回结果会传递到handleNetwork中
        handleNetwork = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("网络异常")) {
                    Toast.makeText(mainActivity, "请检查网络连接_Error41", Toast.LENGTH_LONG).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    homepage_refreshLayout.finishRefresh(false);
                    isRequestSuccess = false;
                } else {
                    isRequestSuccess = true;
                    //解析后台返回的JSON数据
                    parseJson(msg.obj.toString());
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_page, container, false);
        homepage_refreshLayout = view.findViewById(R.id.homepage_refreshLayout);//刷新
        homepage_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initData();
                mainActivity.getRequestSuccess();
            }
        });
        //初始化布局
        initView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //开始轮播
        banner.startAutoPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        banner.stopAutoPlay();
    }

    //初始化布局和handler
    public void initView() {
        banner = view.findViewById(R.id.banner);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) banner.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = (int) (ScreenUtils.getWidth(mainActivity) * 0.5);
        banner.setLayoutParams(params);

        LinearLayout live = view.findViewById(R.id.live);
        live.setOnClickListener(this);
        LinearLayout exam = view.findViewById(R.id.exam);
        exam.setOnClickListener(this);
        LinearLayout answer = view.findViewById(R.id.answer);
        answer.setOnClickListener(this);
        LinearLayout more = view.findViewById(R.id.more);
        more.setOnClickListener(this);

        fragment_home_page_viewpager2 = view.findViewById(R.id.fragment_home_page_viewpager2);
        fragment_home_page_layout1 = view.findViewById(R.id.fragment_home_page_layout1);

        if (isFirstIn) {
            progressDialog = new ProgressDialog(mainActivity);
            progressDialog.show();
            isFirstIn = false;
        }

        initData();
    }

    public void initData() {
        //请求后台网络数据，数据会在handleNetwork接收并处理
        RequestURL.sendGET("https://app.feimayun.com/Index/index", handleNetwork);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live://直播中心
                new TListDialog.Builder(mainActivity.getSupportFragmentManager())
                        .setScreenWidthAspect(mainActivity, 0.8f)
                        .setScreenHeightAspect(mainActivity, 0.6f)
                        .setListLayoutRes(R.layout.dialog_live, LinearLayoutManager.VERTICAL)
                        .setGravity(Gravity.CENTER)
                        .setCancelOutside(true)
                        .setAdapter(new TBaseAdapter<String>(R.layout.dialog_live_textviewitem, dataName) {
                            @Override
                            protected void onBind(BindViewHolder holder, int position, String s) {
                                holder.setText(R.id.tv, s);
                            }
                        })
                        .setOnAdapterItemClickListener(new TBaseAdapter.OnAdapterItemClickListener<String>() {
                            @Override
                            public void onItemClick(BindViewHolder holder, int position, String item, TDialog tDialog) {
                                //TODO 直播列表跳转
                                String series_1 = data_mapList.get(position).get("id");
                                Intent intent = new Intent(mainActivity, LiveListActivity.class);
                                intent.putExtra("series_1", series_1);
                                intent.putExtra("is_live", "1");
                                startActivity(intent);
//                                Toast.makeText(mainActivity, position + "", Toast.LENGTH_SHORT).show();
                                tDialog.dismiss();
                            }
                        })
                        .addOnClickListener(R.id.dialog_live_imageview1)
                        .setOnViewClickListener(new OnViewClickListener() {
                            @Override
                            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                switch (view.getId()) {
                                    case R.id.dialog_live_imageview1:
                                        tDialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
                break;
            case R.id.exam://考试中心
                Intent intentExamListActivity = new Intent(mainActivity, ExamListActivity.class);
                startActivity(intentExamListActivity);
                break;
            case R.id.answer://答疑中心
                Toast.makeText(mainActivity, "敬请期待", Toast.LENGTH_SHORT).show();

//                new TListDialog.Builder(mainActivity.getSupportFragmentManager())
//                        .setScreenWidthAspect(mainActivity, 0.8f)
//                        .setScreenHeightAspect(mainActivity, 0.6f)
//                        .setListLayoutRes(R.layout.dialog_live, LinearLayoutManager.VERTICAL)
//                        .setGravity(Gravity.CENTER)
//                        .setCancelOutside(true)
//                        .setAdapter(new TBaseAdapter<String>(R.layout.dialog_live_textviewitem, dataName) {
//                            @Override
//                            protected void onBind(BindViewHolder holder, int position, String s) {
//                                holder.setText(R.id.tv, s);
//                            }
//                        })
//                        .setOnAdapterItemClickListener(new TBaseAdapter.OnAdapterItemClickListener<String>() {
//                            @Override
//                            public void onItemClick(BindViewHolder holder, int position, String item, TDialog tDialog) {
//                                Intent intent = new Intent(mainActivity, QuestionListActivity.class);
//                                List_Bean list_beanId = new List_Bean();
//                                list_beanId.setListString(lmListId);
//                                List_Bean list_beanName = new List_Bean();
//                                list_beanName.setListString(lmListName);
//                                intent.putExtra("list_beanId", list_beanId);
//                                intent.putExtra("list_beanName", list_beanName);
//                                intent.putExtra("position", position);
//                                startActivity(intent);
//                                tDialog.dismiss();
//                            }
//                        })
//                        .addOnClickListener(R.id.dialog_live_imageview1)
//                        .setOnViewClickListener(new OnViewClickListener() {
//                            @Override
//                            public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
//                                switch (view.getId()) {
//                                    case R.id.dialog_live_imageview1:
//                                        tDialog.dismiss();
//                                        break;
//                                }
//                            }
//                        })
//                        .create()
//                        .show();
                break;
            case R.id.more://敬请期待
                Toast.makeText(mainActivity, "敬请期待", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //初始化底部ListView
    private void initListView() {
        //底部ListView的相关变量
        ListView p_4_listView = view.findViewById(R.id.p_4_listView);
        P4_ListView_Adapter adapter = new P4_ListView_Adapter(mainActivity, data_mapList);
        p_4_listView.setAdapter(adapter);
        //固定ListView的高度和数量
        SetHeightUtil.measureListViewHeight(p_4_listView);
        p_4_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //实现碎片页面跳转
                mainActivity.onCheckedChanged(mainActivity.rg_bt, R.id.selector2_rb);

                //实现碎片页面数据传递
                String data_position = String.valueOf(position);
                String data_id = data_mapList.get(position).get("id");
                Bundle bundle = new Bundle();
                bundle.putString("data_position", data_position);
                bundle.putString("data_id", data_id);
                Message message = new Message();
                message.what = StaticUtil.FROM_HOMEPAGE_TO_FULLCOURSE;
                message.obj = bundle;
                FullCourseFragment.handleJump.sendMessage(message);
            }
        });
    }

    private String getResourcesUri(@DrawableRes int id) {
        Resources resources = getResources();
        String uriPath = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(id) + "/" +
                resources.getResourceTypeName(id) + "/" +
                resources.getResourceEntryName(id);
        return uriPath;
    }

    //初始化顶部ViewPager轮播图
    private void initViewPager() {
        if (banner_mapList.size() > 0) {
            banner.setImages(banner_mapList);
        } else {
            ArrayList<String> bannerDefaultList = new ArrayList<>();
            bannerDefaultList.add(getResourcesUri(R.drawable.banner1));
            banner.setImages(bannerDefaultList);
        }
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        banner.setImageLoader(new GlideImageLoader());
        banner.setBannerAnimation(Transformer.Default);
        banner.isAutoPlay(true);
        banner.setDelayTime(3000);
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.start();
    }

    //初始化顶部ViewPager2，也就是6个方格布局
    private void initViewPager2() {
        //每页存放6个，a表示存放的页数
        int a = data_mapList.size() / 6;
        int b = data_mapList.size() % 6;
        if (b != 0) {
            a = a + 1;
        }
        if (a <= 1) {
            fragment_home_page_layout1.setVisibility(View.INVISIBLE);
        } else {
            fragment_home_page_layout1.setVisibility(View.VISIBLE);
        }
        //存放fragment的列表
        //6个方格布局的GridView相关变量
        //每页显示GridView的ViewPager
        List<Fragment> fragmentArrayList = new ArrayList<>();
        for (int i = 0; i < a; i++) {
            HomePageFragment_ViewPager2_Fragment fragment = new HomePageFragment_ViewPager2_Fragment();
            Bundle bundle = new Bundle();
            bundle.putInt("cur_page", (i + 1));
            bundle.putInt("max_page", a);
            List_Bean bean = new List_Bean();
            bean.setList(data_mapList);
            bundle.putSerializable("data_Bean", bean);
            fragment.setArguments(bundle);
            fragmentArrayList.add(fragment);
        }

        Fragment_home_page_viewpager2_Adapter adapter2 = new Fragment_home_page_viewpager2_Adapter(
                getChildFragmentManager(), fragmentArrayList);
        fragment_home_page_viewpager2.setAdapter(adapter2);
        //让ViewPager缓存2个页面
        fragment_home_page_viewpager2.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < pointImageviews.length; i++) {
                    if (i == position) {
                        pointImageviews[i].setImageResource(R.drawable.point_1);
                    } else {
                        pointImageviews[i].setImageResource(R.drawable.point_0);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        fragment_home_page_layout1.removeAllViews();
        pointImageviews = new ImageView[a];
        for (int i = 0; i < a; i++) {
            View point_panel = LayoutInflater.from(mainActivity).inflate(R.layout.fragment_homepage_viewpager2_point, null);
            ImageView imageView = point_panel.findViewById(R.id.point);
            if (i == 0) {
                imageView.setImageResource(R.drawable.point_1);
            } else {
                imageView.setImageResource(R.drawable.point_0);
            }
            pointImageviews[i] = imageView;
            fragment_home_page_layout1.addView(point_panel);
        }
    }

    //解析后台返回的JSON数据,同时调用本碎片中各个页面加载方法，传入页面所需的数据
    private void parseJson(String jsonData) {
        try {
            JSONTokener jsonTokener = new JSONTokener(jsonData);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            String status = jsonObject.getString("status");

            if (status.equals("1")) {
                //解析banner
                String bannerString = jsonObject.getString("banner");
                banner_mapList = new ArrayList<>();
                if (!bannerString.equals("null")) {
                    JSONArray banner = jsonObject.getJSONArray("banner");
                    for (int i = 0; i < banner.length(); i++) {
                        banner_mapList.add(banner.getJSONObject(i).getString("bg_url"));
                    }
                }

                //解析data
                JSONArray data = jsonObject.getJSONArray("data");
                data_mapList = new ArrayList<>();
                lmListId = new ArrayList<>();
                lmListName = new ArrayList<>();
                dataName = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    Map<String, String> map = new HashMap<>();

                    map.put("id", item.getString("id"));
                    map.put("company_id", item.getString("company_id"));
                    map.put("name", item.getString("name"));

                    dataName.add(item.getString("name"));

                    map.put("pid", item.getString("pid"));
                    map.put("level", item.getString("level"));
                    map.put("path", item.getString("path"));
                    map.put("sort", item.getString("sort"));
                    map.put("bg_img", item.getString("bg_img"));
                    map.put("icon_img", item.getString("icon_img"));
                    map.put("about", item.getString("about"));
                    map.put("create_time", item.getString("create_time"));
                    map.put("create_uid", item.getString("create_uid"));
                    map.put("update_uid", item.getString("update_uid"));
                    map.put("update_time", item.getString("update_time"));
                    map.put("status", item.getString("status"));
                    map.put("is_del", item.getString("is_del"));
                    if (item.has("children")) {
                        JSONArray jsonArray_children = item.getJSONArray("children");
                        map.put("children", jsonArray_children.toString());
                        String[] lmListIdString = new String[jsonArray_children.length() + 1];
                        String[] lmListNameString = new String[jsonArray_children.length() + 1];
                        lmListIdString[0] = item.getString("id");
//                        lmListNameString[0] = item.getString("name");
                        lmListNameString[0] = "全部";
                        for (int j = 0; j < jsonArray_children.length(); j++) {
                            JSONObject jsonArray_childrenObject = jsonArray_children.getJSONObject(j);
                            lmListIdString[j + 1] = jsonArray_childrenObject.getString("id");
                            lmListNameString[j + 1] = jsonArray_childrenObject.getString("name");
                        }
                        lmListId.add(lmListIdString);
                        lmListName.add(lmListNameString);
                    } else {
                        String[] lmListIdString = new String[1];
                        String[] lmListNameString = new String[1];
                        lmListIdString[0] = item.getString("id");
//                        lmListNameString[0] = item.getString("name");
                        lmListNameString[0] = "全部";
                        lmListId.add(lmListIdString);
                        lmListName.add(lmListNameString);
                        map.put("children", "");
                    }
                    data_mapList.add(map);
                }

                //初始化顶部ViewPager轮播图
                initViewPager();
                //初始化GridView
                initViewPager2();
                //初始化ListView
                initListView();
                homepage_refreshLayout.finishRefresh(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            homepage_refreshLayout.finishRefresh(false);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } finally {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    }
}
