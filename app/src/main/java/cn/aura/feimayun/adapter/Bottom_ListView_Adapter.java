package cn.aura.feimayun.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.vhall.watch.WatchActivity;

/**
 * 描述：首页P4页ListView适配器，下方有一个内部类是ListView子项GridView的适配器
 */
public class Bottom_ListView_Adapter extends BaseAdapter {
    private MainActivity activity;
    private List<Map<String, String>> data;

    public Bottom_ListView_Adapter(Activity activity, List<Map<String, String>> data) {
        this.activity = (MainActivity) activity;
        //记录解析的各1级children信息
        final List<Map<String, String>> lessonsList = new ArrayList<>();
        String lessonsString = data.get(0).get("lessons");
        int lessonsCount;
        if (lessonsString.equals("")) {
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
                    lessonsList.add(map);
                }
                this.data = lessonsList;
            } catch (JSONException e) {
//                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Map<String, String> getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Map<String, String> mapItem = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(activity);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.bottom_listviewitem, parent, false);
        }
        View bottom_root = convertView.findViewById(R.id.bottom_root);
        ImageView bottom_imageview1 = convertView.findViewById(R.id.bottom_imageview1);
        TextView bottom_textview1 = convertView.findViewById(R.id.bottom_textview1);
        TextView bottom_textview2 = convertView.findViewById(R.id.bottom_textview2);
        TextView bottom_textview3 = convertView.findViewById(R.id.bottom_textview3);
        TextView bottom_textview4 = convertView.findViewById(R.id.bottom_textview4);
        RequestOptions options = new RequestOptions().fitCenter();
        if (Util.isOnMainThread()) {
            Glide.with(MyApplication.context).load(mapItem.get("bg_url")).apply(options).into(bottom_imageview1);
        }
        bottom_textview1.setText(mapItem.get("name"));
        bottom_textview2.setText(mapItem.get("title"));
        bottom_textview3.setText(mapItem.get("sells") + "人报名");
        bottom_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data_id = mapItem.get("id");
                String data_teach_type = mapItem.get("teach_type");
                startActivity(data_id, data_teach_type);
            }
        });
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

}
