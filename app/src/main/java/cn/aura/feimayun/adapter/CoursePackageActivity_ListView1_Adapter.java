package cn.aura.feimayun.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.PaperListActivity;
import cn.aura.feimayun.activity.PlayDetailActivity;
import cn.aura.feimayun.util.SetHeightUtil;
import cn.aura.feimayun.util.Util;
import cn.aura.feimayun.vhall.watch.WatchActivity;

public class CoursePackageActivity_ListView1_Adapter extends BaseAdapter {
    Activity activity;
    private List<Map<String, String>> catalogueList;
    private String dataId;

    public CoursePackageActivity_ListView1_Adapter(Activity activity, List<Map<String, String>> catalogueList, String dataId) {
        this.activity = activity;
        this.catalogueList = catalogueList;
        this.dataId = dataId;
    }

    @Override
    public int getCount() {
        return catalogueList.size();
    }

    @Override
    public Object getItem(int position) {
        return catalogueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.activity_course_package_listview1_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.activity_course_package_listview1_item_textview1 = view.findViewById(R.id.activity_course_package_listview1_item_textview1);
            viewHolder.activity_course_package_listview1_item_listview1 = view.findViewById(R.id.activity_course_package_listview1_item_listview1);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.activity_course_package_listview1_item_textview1.setText(catalogueList.get(position).get("name"));

        if (catalogueList.get(position).get("children") != null) {
            String childrenString = catalogueList.get(position).get("children");
            try {
                JSONTokener jsonTokener = new JSONTokener(childrenString);
                JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();

                List<Map<String, String>> childrenList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    Map<String, String> childrenMap = new HashMap<>();
                    JSONObject childrenObject = jsonArray.getJSONObject(i);
                    childrenMap.put("id", childrenObject.getString("id"));
                    childrenMap.put("company_id", childrenObject.getString("company_id"));
                    childrenMap.put("lid", childrenObject.getString("lid"));
                    childrenMap.put("pid", childrenObject.getString("pid"));
                    childrenMap.put("type", childrenObject.getString("type"));
                    childrenMap.put("name", childrenObject.getString("name"));
                    childrenMap.put("tid", childrenObject.getString("tid"));
                    childrenMap.put("sort", childrenObject.getString("sort"));
                    childrenMap.put("status", childrenObject.getString("status"));
                    childrenMap.put("is_del", childrenObject.getString("is_del"));
                    //一共有4中类型的type
                    String type = childrenObject.getString("type");
                    switch (type) {
                        case "1"://直播
                            childrenMap.put("title", childrenObject.getString("title"));
                            childrenMap.put("number", childrenObject.getString("number"));
                            childrenMap.put("tea_id", childrenObject.getString("tea_id"));
                            childrenMap.put("helper_id", childrenObject.getString("helper_id"));
                            childrenMap.put("change_uid", childrenObject.getString("change_uid"));
                            childrenMap.put("price", childrenObject.getString("price"));
                            childrenMap.put("rprice", childrenObject.getString("rprice"));
                            childrenMap.put("is_sell", childrenObject.getString("is_sell"));
                            childrenMap.put("hours", childrenObject.getString("hours"));
                            childrenMap.put("open_say", childrenObject.getString("open_say"));
                            childrenMap.put("open_auth", childrenObject.getString("open_auth"));
                            childrenMap.put("user_json", childrenObject.getString("user_json"));
                            childrenMap.put("bg_url", childrenObject.getString("bg_url"));
                            childrenMap.put("about", childrenObject.getString("about"));
                            childrenMap.put("replacement", childrenObject.getString("replacement"));
                            childrenMap.put("is_rec", childrenObject.getString("is_rec"));
                            childrenMap.put("rec_time", childrenObject.getString("rec_time"));
                            childrenMap.put("plays", childrenObject.getString("plays"));
                            childrenMap.put("browse", childrenObject.getString("browse"));
                            childrenMap.put("sells", childrenObject.getString("sells"));
                            childrenMap.put("start_time", childrenObject.getString("start_time"));
                            childrenMap.put("end_time", childrenObject.getString("end_time"));
                            childrenMap.put("live_time", childrenObject.getString("live_time"));
                            childrenMap.put("is_safe", childrenObject.getString("is_safe"));
                            childrenMap.put("is_draw", childrenObject.getString("is_draw"));
                            childrenMap.put("is_stop", childrenObject.getString("is_stop"));
                            childrenMap.put("live_num", childrenObject.getString("live_num"));
                            childrenMap.put("app_img_url", childrenObject.getString("app_img_url"));
                            childrenMap.put("need_record", childrenObject.getString("need_record"));
                            childrenMap.put("model", childrenObject.getString("model"));
                            childrenMap.put("quality", childrenObject.getString("quality"));
                            childrenMap.put("format", childrenObject.getString("format"));
                            childrenMap.put("start_ts", childrenObject.getString("start_ts"));
                            childrenMap.put("end_ts", childrenObject.getString("end_ts"));
                            childrenMap.put("category", childrenObject.getString("category"));
                            childrenMap.put("play_mode", childrenObject.getString("play_mode"));
                            childrenMap.put("code_rate_types", childrenObject.getString("code_rate_types"));
                            childrenMap.put("need_time_shift", childrenObject.getString("need_time_shift"));
                            childrenMap.put("need_full_view", childrenObject.getString("need_full_view"));
                            childrenMap.put("del_txt", childrenObject.getString("del_txt"));
                            childrenMap.put("webinar_id", childrenObject.getString("webinar_id"));
                            childrenMap.put("webinar_start_url", childrenObject.getString("webinar_start_url"));
                            childrenMap.put("webinar_play_url", childrenObject.getString("webinar_play_url"));
                            childrenMap.put("wk_day", childrenObject.getString("wk_day"));
                            childrenList.add(childrenMap);
                            break;
                        case "2"://录播
                            childrenMap.put("title", childrenObject.getString("title"));
                            childrenMap.put("number", childrenObject.getString("number"));
                            childrenMap.put("tea_id", childrenObject.getString("tea_id"));
                            childrenMap.put("is_sell", childrenObject.getString("is_sell"));
                            childrenMap.put("price", childrenObject.getString("price"));
                            childrenMap.put("rprice", childrenObject.getString("rprice"));
                            childrenMap.put("hours", childrenObject.getString("hours"));
                            childrenMap.put("is_rec", childrenObject.getString("is_rec"));
                            childrenMap.put("rec_time", childrenObject.getString("rec_time"));
                            childrenMap.put("start_time", childrenObject.getString("start_time"));
                            childrenMap.put("end_time", childrenObject.getString("end_time"));
                            childrenMap.put("user_json", childrenObject.getString("user_json"));
                            childrenMap.put("learn_type", childrenObject.getString("learn_type"));
                            childrenMap.put("bg_url", childrenObject.getString("bg_url"));
                            childrenMap.put("about", childrenObject.getString("about"));
                            childrenMap.put("replacement", childrenObject.getString("replacement"));
                            childrenMap.put("plays", childrenObject.getString("plays"));
                            childrenMap.put("browse", childrenObject.getString("browse"));
                            childrenMap.put("sells", childrenObject.getString("sells"));
                            childrenMap.put("wk_day", childrenObject.getString("wk_day"));
                            childrenList.add(childrenMap);
                            break;
                        case "3"://题库
                            childrenMap.put("about", childrenObject.getString("about"));
                            childrenMap.put("bg_img", childrenObject.getString("bg_img"));
                            childrenMap.put("is_sell", childrenObject.getString("is_sell"));
                            childrenMap.put("tkTotal", childrenObject.getString("tkTotal"));
                            childrenMap.put("testTotal", childrenObject.getString("testTotal"));
                            childrenMap.put("lm2List", childrenObject.getString("lm2List"));
                            childrenList.add(childrenMap);
                            break;
                        case "4"://TODO 答疑
                            childrenMap.put("qaTotal", childrenObject.getString("qaTotal"));
                            childrenMap.put("lmtotal", childrenObject.getString("lmtotal"));
                            childrenMap.put("pkid", childrenObject.getString("pkid"));
                            childrenList.add(childrenMap);
                            break;
                        default:
                            break;
                    }
                }
                //创建二级listview
                InnerAdapter adapter = new InnerAdapter(childrenList);
                viewHolder.activity_course_package_listview1_item_listview1.setAdapter(adapter);
                SetHeightUtil.setListViewHeightBasedOnChildren(viewHolder.activity_course_package_listview1_item_listview1);
                viewHolder.activity_course_package_listview1_item_listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    class ViewHolder {
        TextView activity_course_package_listview1_item_textview1;
        ListView activity_course_package_listview1_item_listview1;
    }

    class InnerAdapter extends BaseAdapter {
        List<Map<String, String>> childrenList;

        InnerAdapter(List<Map<String, String>> childrenList) {
            this.childrenList = childrenList;
        }

        @Override
        public int getCount() {
            return childrenList.size();
        }

        @Override
        public Object getItem(int position) {
            return childrenList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder2 viewHolder2;
            if (convertView == null) {
                view = LayoutInflater.from(activity).inflate(R.layout.activity_course_package_listview1_item_listview1_item, null);
                viewHolder2 = new ViewHolder2();
                viewHolder2.activity_course_package_listview1_item_listview1_item_layout1 =
                        view.findViewById(R.id.activity_course_package_listview1_item_listview1_item_layout1);
                viewHolder2.activity_course_package_listview1_item_listview1_item_textview1 =
                        view.findViewById(R.id.activity_course_package_listview1_item_listview1_item_textview1);
                viewHolder2.activity_course_package_listview1_item_listview1_item_imageview1 =
                        view.findViewById(R.id.activity_course_package_listview1_item_listview1_item_imageview1);
                view.setTag(viewHolder2);
            } else {
                view = convertView;
                viewHolder2 = (ViewHolder2) view.getTag();
            }

            viewHolder2.activity_course_package_listview1_item_listview1_item_layout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.activity_course_package_listview1_item_listview1_item_layout1:
                            String type = childrenList.get(position).get("type");
                            String id = childrenList.get(position).get("lid");
                            String pkid = dataId;
                            if (type != null) {
                                switch (type) {
                                    case "1":
                                        Intent intent0 = new Intent(activity, WatchActivity.class);
                                        intent0.putExtra("data_id", id);
                                        intent0.putExtra("data_teach_type", type);
                                        intent0.putExtra("pkid", pkid);
                                        activity.startActivity(intent0);
                                        break;
                                    case "2":
                                        Intent intent1 = new Intent(activity, PlayDetailActivity.class);
                                        intent1.putExtra("data_id", id);
                                        intent1.putExtra("data_teach_type", type);
                                        intent1.putExtra("pkid", pkid);
                                        activity.startActivity(intent1);
                                        break;
                                    case "3":
                                        //题库跳转
                                        String uid = Util.getUid();
                                        if (uid.equals("")) {
                                            Toast.makeText(activity, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Intent intent2 = new Intent(activity, PaperListActivity.class);
                                            String id2 = childrenList.get(position).get("id");
                                            intent2.putExtra("sid", id2);
                                            activity.startActivity(intent2);
                                        }
                                        break;
                                    case "4":
                                        Toast.makeText(activity, "敬请期待", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                            break;
                    }
                }
            });

            viewHolder2.activity_course_package_listview1_item_listview1_item_textview1.setText(childrenList.get(position).get("name"));
            String type = childrenList.get(position).get("type");
            if (type != null) {
                switch (type) {
                    case "1":
                        Glide.with(activity).load(R.drawable.course_package_i1).into(viewHolder2.activity_course_package_listview1_item_listview1_item_imageview1);
                        break;
                    case "2":
                        Glide.with(activity).load(R.drawable.course_package_i2).into(viewHolder2.activity_course_package_listview1_item_listview1_item_imageview1);
                        break;
                    case "3":
                        Glide.with(activity).load(R.drawable.course_package_i3).into(viewHolder2.activity_course_package_listview1_item_listview1_item_imageview1);
                        break;
                    case "4":
                        Glide.with(activity).load(R.drawable.course_package_i4).into(viewHolder2.activity_course_package_listview1_item_listview1_item_imageview1);
                        break;
                }
            }
            return view;
        }

        class ViewHolder2 {
            RelativeLayout activity_course_package_listview1_item_listview1_item_layout1;
            ImageView activity_course_package_listview1_item_listview1_item_imageview1;
            TextView activity_course_package_listview1_item_listview1_item_textview1;
        }
    }
}
