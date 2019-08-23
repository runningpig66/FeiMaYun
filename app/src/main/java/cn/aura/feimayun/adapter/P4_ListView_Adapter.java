package cn.aura.feimayun.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.CoursePackageActivity;
import cn.aura.feimayun.activity.FaceToFaceActivity;
import cn.aura.feimayun.activity.MainActivity;
import cn.aura.feimayun.activity.PlayDetailActivity;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.ScreenUtils;
import cn.aura.feimayun.util.SetHeightUtil;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.vhall.watch.WatchActivity;

/**
 * 描述：首页P4页ListView适配器，下方有一个内部类是ListView子项GridView的适配器
 */
public class P4_ListView_Adapter extends BaseAdapter {
    public int mScreenWidth;
    private MainActivity activity;
    private List<Map<String, String>> data;

    public P4_ListView_Adapter(Activity activity, List<Map<String, String>> data) {
        this.activity = (MainActivity) activity;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        (activity).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
//        itemWidth = (mScreenWidth - ScreenUtils.dp2px(context, 84)) / 4;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.p4_listviewitem, parent, false);
        }
        LinearLayout main_layout = convertView.findViewById(R.id.main_layout);
        TextView title_left = convertView.findViewById(R.id.title_left);
        title_left.setText(data.get(position).get("name"));
        final ImageView top_img = convertView.findViewById(R.id.top_img);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) top_img.getLayoutParams();
        //大图是1054*456的，比例为2.3114
        params.height = (int) ((mScreenWidth - ScreenUtils.dp2px(activity, 30)) / 2.3114f);
        top_img.setLayoutParams(params);

        TextView main_title_textview = convertView.findViewById(R.id.main_title_textview);
        GridView gridView = convertView.findViewById(R.id.p_4_item_gridview);
        //记录解析的各1级children信息
        final List<Map<String, String>> gridViewList = new ArrayList<>();
        String lessonsString = data.get(position).get("lessons");
        int lessonsCount;
        if (lessonsString == null || lessonsString.equals("")) {
            lessonsCount = 0;
        } else {
            //解析二级目录，也就是lessons
            JSONTokener lessonsTokener = new JSONTokener(lessonsString);
            try {
                JSONArray lessonsArray = (JSONArray) lessonsTokener.nextValue();
                lessonsCount = lessonsArray.length();
                for (int i = 0; i < lessonsCount; i++) {
                    final Map<String, String> map = new HashMap<>();
                    final JSONObject lessonsObject = lessonsArray.optJSONObject(i);
                    map.put("id", lessonsObject.optString("id"));
                    map.put("teach_type", lessonsObject.optString("teach_type"));
                    map.put("name", lessonsObject.optString("name"));
                    map.put("bg_url", lessonsObject.optString("bg_url"));
                    map.put("title", lessonsObject.optString("title"));
                    map.put("sells", lessonsObject.optString("sells"));
                    if (lessonsObject.has("lessons")) {
                        JSONArray jsonArray_children2 = lessonsObject.getJSONArray("lessons");
                        map.put("lessons", jsonArray_children2.toString());
                    } else {
                        map.put("lessons", "");
                    }
                    if (i == 0) {
                        RequestOptions options = new RequestOptions().fitCenter();
                        if (Util.isOnMainThread()) {
                            Glide.with(MyApplication.context).load(lessonsObject.optString("bg_url")).apply(options).into(top_img);
                        }
                        main_title_textview.setText(lessonsObject.optString("name"));
                        main_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //添加课程列表页面点击事件
                                String data_id = lessonsObject.optString("id");
                                String data_teach_type = lessonsObject.optString("teach_type");
                                startActivity(data_id, data_teach_type);
                            }
                        });
                    }
                    gridViewList.add(map);
                }
                //去掉第一项
                gridViewList.remove(0);
                P4_ListViewItem_GridView_Adapter gridView_adapter = new P4_ListViewItem_GridView_Adapter(gridViewList);
                gridView.setAdapter(gridView_adapter);
                //禁止gridView获取焦点
                gridView.setFocusable(false);
                //固定GridView的高度和数量
                SetHeightUtil.setGridViewHeight(gridView, gridViewList.size(), 2);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position_inner, long id) {
                        //添加课程列表页面点击事件
                        Map<String, String> map = gridViewList.get(position_inner);
                        String data_id = map.get("id");
                        String data_teach_type = map.get("teach_type");
                        startActivity(data_id, data_teach_type);
//                        //TODO GridView点击事件
//                        String childrenJSON = data.get(position).get("lessons");
//                        assert childrenJSON != null;
//                        if (!childrenJSON.equals("")) {
//                            //拿到二级JSON，变量名后的s代表二级second
//                            try {
//                                JSONTokener jsonTokener_s = new JSONTokener(childrenJSON);
//                                JSONArray jsonArray_s = (JSONArray) jsonTokener_s.nextValue();
//                                JSONObject jsonObject_s = jsonArray_s.getJSONObject(position_inner);
//                                String id_ = jsonObject_s.getString("id");
//                        Intent intent = new Intent(activity, CourseListActivity.class);
//                                intent.putExtra("series", "series_2");
//                                intent.putExtra("id", id_);
//                                activity.startActivity(intent);
//                            } catch (JSONException e) {
////                                e.printStackTrace();
//                            }
//                        }
                    }
                });
            } catch (JSONException e) {
//                e.printStackTrace();
            }
        }
        return convertView;
    }

    private void startActivity(String data_id, String data_teach_type) {
        switch (Integer.parseInt(data_teach_type)) {
            case 1://直播
                Intent intentLiveActivity = new Intent(activity, WatchActivity.class);
                intentLiveActivity.putExtra("data_id", data_id);
                intentLiveActivity.putExtra("data_teach_type", data_teach_type);
                activity.startActivity(intentLiveActivity);
                break;
            case 2://录播
                Intent intentPlayDeatilActivity = new Intent(activity, PlayDetailActivity.class);
                intentPlayDeatilActivity.putExtra("data_id", data_id);
                intentPlayDeatilActivity.putExtra("data_teach_type", data_teach_type);
                activity.startActivity(intentPlayDeatilActivity);
                break;
            case 3://课程包
                Intent intentCoursePackageActivity = new Intent(activity, CoursePackageActivity.class);
                intentCoursePackageActivity.putExtra("data_id", data_id);
                intentCoursePackageActivity.putExtra("data_teach_type", data_teach_type);
                activity.startActivity(intentCoursePackageActivity);
                break;
            case 4://面授
                Intent intentFaceToFaceActivity = new Intent(activity, FaceToFaceActivity.class);
                intentFaceToFaceActivity.putExtra("data_id", data_id);
                intentFaceToFaceActivity.putExtra("data_teach_type", data_teach_type);
                activity.startActivity(intentFaceToFaceActivity);
                break;
        }
    }


    //ListView子项GridView适配器
    class P4_ListViewItem_GridView_Adapter extends BaseAdapter {
        private List<Map<String, String>> data;

        P4_ListViewItem_GridView_Adapter(List<Map<String, String>> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            if (data == null) {
                return 0;
            }
            if (data.size() > 4) {
                return 4;
            } else {
                return data.size();
            }
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (data.size() > 0) {
                LayoutInflater inflater = LayoutInflater.from(MyApplication.context);
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.p4_listviewitem_gridviewitem, parent, false);
                }
                ImageView gridview1_imageview = convertView.findViewById(R.id.gridview1_imageview);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) gridview1_imageview.getLayoutParams();
                //大图是1054*456的，比例为2.3114
                params.height = (int) ((mScreenWidth - ScreenUtils.dp2px(activity, 40)) / 2.0f / 1.8065f);
                gridview1_imageview.setLayoutParams(params);

                TextView gridview1_textview = convertView.findViewById(R.id.gridview1_textview);
                gridview1_textview.setText(data.get(position).get("name"));
                RequestOptions options = new RequestOptions().fitCenter();
                if (Util.isOnMainThread()) {
                    Glide.with(MyApplication.context).load(data.get(position).get("bg_url")).apply(options).into(gridview1_imageview);
                }
            }
            return convertView;
        }
    }
}
