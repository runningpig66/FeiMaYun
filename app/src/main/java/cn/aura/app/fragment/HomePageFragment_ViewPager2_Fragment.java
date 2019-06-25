package cn.aura.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.aura.app.R;
import cn.aura.app.activity.MainActivity;
import cn.aura.app.application.MyApplication;
import cn.aura.app.bean.List_Bean;
import cn.aura.app.util.SetHeightUtil;
import cn.aura.app.util.StaticUtil;
import cn.aura.app.util.Util;

/**
 * 描述：6个方格布局页面的ViewPager的碎片页
 */
public class HomePageFragment_ViewPager2_Fragment extends Fragment {
    private MainActivity mainActivity;
    private int cur_page;
    private int max_page;
    private List<Map<String, String>> data_mapList;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            //获取序列化的bean对象
            List_Bean data_Bean = (List_Bean) bundle.getSerializable("data_Bean");
            if (data_Bean != null) {
                data_mapList = data_Bean.getList();
            }
            cur_page = bundle.getInt("cur_page");
            max_page = bundle.getInt("max_page");
//            Gson gson = new Gson();
        }
        //为了防止第二次加载的时候重复调用了这个方法onCreateView(),重新new了一个pageadapter导致fragment不显示，显示空白
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
            return view;
        }
        view = inflater.inflate(R.layout.fragment_homepage_viewpager2, container, false);
        initGridView();
        return view;
    }

    private void initGridView() {
        GridView fragment_homepage_viewpager2_gridview = view.findViewById(R.id.fragment_homepage_viewpager2_gridview);
        List<Map<String, String>> data_mapList2 = new ArrayList<>();
        int start = (cur_page - 1) * 6;

        if (cur_page == max_page) {
            for (int i = start; i < data_mapList.size(); i++) {
                Map<String, String> map = data_mapList.get(i);
                data_mapList2.add(map);
            }
        } else {
            for (int i = start; i < start + 6; i++) {
                Map<String, String> map = data_mapList.get(i);
                data_mapList2.add(map);
            }
        }

        MySimpleAdapter mySimpleAdapter = new MySimpleAdapter(mainActivity, data_mapList2);
        fragment_homepage_viewpager2_gridview.setAdapter(mySimpleAdapter);
        SetHeightUtil.setGridViewHeight2(fragment_homepage_viewpager2_gridview, 6, 3);

        //GridView点击事件
        fragment_homepage_viewpager2_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //这里的position要根据页面增加
                int newPosition = position;
                if (cur_page > 1) {
                    newPosition = (cur_page - 1) * 6 + position;
                }

                //实现碎片页面跳转
                mainActivity.onCheckedChanged(mainActivity.rg_bt, R.id.selector2_rb);
//                mainActivity.setFragment2Fragment(new MainActivity.Fragment2Fragment() {
//                    @Override
//                    public void gotoFragment(ViewPager viewPager) {
//                        viewPager.setCurrentItem(1);
//                    }
//                });
//                mainActivity.forSkip();
                //实现碎片页面数据传递

                String data_position = String.valueOf(newPosition);
                String data_id = data_mapList.get(newPosition).get("id");
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    //ViewPager里面的GridView的适配器
    static class MySimpleAdapter extends BaseAdapter {
        private Activity activity;
        private List<Map<String, String>> data;

        MySimpleAdapter(Activity activity, List<Map<String, String>> data) {
            this.activity = activity;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Map<String, String> getItem(int position) {
            return data == null ? null : data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Map<String, String> mapItem = getItem(position);
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(activity).inflate(R.layout.fragment_homepage_viewpager2_gridview_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.fragment_homepage_viewpager2_gridview_item_imageview = view.findViewById(R.id.fragment_homepage_viewpager2_gridview_item_imageview);
                viewHolder.fragment_homepage_viewpager2_gridview_item_textview = view.findViewById(R.id.fragment_homepage_viewpager2_gridview_item_textview);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            if (Util.isOnMainThread()) {
                Glide.with(MyApplication.context).load(mapItem.get("icon_img")).into(viewHolder.fragment_homepage_viewpager2_gridview_item_imageview);
            }
            viewHolder.fragment_homepage_viewpager2_gridview_item_textview.setText(mapItem.get("name"));
            return view;
        }

        static class ViewHolder {
            ImageView fragment_homepage_viewpager2_gridview_item_imageview;
            TextView fragment_homepage_viewpager2_gridview_item_textview;
        }
    }
}
