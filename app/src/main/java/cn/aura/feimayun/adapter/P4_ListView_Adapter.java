package cn.aura.feimayun.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
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
import cn.aura.feimayun.activity.CourseListActivity;
import cn.aura.feimayun.activity.MainActivity;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.SetHeightUtil;
import cn.aura.feimayun.util.Util;

/**
 * 描述：首页P4页ListView适配器，下方有一个内部类是ListView子项GridView的适配器
 */
public class P4_ListView_Adapter extends BaseAdapter {
    private MainActivity activity;
    private List<Map<String, String>> data;

    public P4_ListView_Adapter(Activity activity, List<Map<String, String>> data) {
        this.activity = (MainActivity) activity;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
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
        TextView title_left = convertView.findViewById(R.id.title_left);
        title_left.setText(data.get(position).get("name"));
        ImageView top_img = convertView.findViewById(R.id.top_img);

        RequestOptions options = new RequestOptions()
                .fitCenter();
        if (Util.isOnMainThread()) {
            Glide.with(MyApplication.context).load(data.get(position).get("bg_img")).apply(options).into(top_img);
        }

        GridView gridView = convertView.findViewById(R.id.p_4_item_gridview);
        //记录解析的各1级children信息
        List<Map<String, String>> gridViewList = new ArrayList<>();
        String children_str = data.get(position).get("children");

        //记录childern个数
        int children_count = 0;
        if (children_str.equals("")) {
            children_count = 0;
        } else {
            //解析二级目录，也就是children1
            JSONTokener jsonTokener = new JSONTokener(children_str);
            try {
                JSONArray jsonArray_children = (JSONArray) jsonTokener.nextValue();
                children_count = jsonArray_children.length();
                for (int i = 0; i < children_count; i++) {
                    Map<String, String> map = new HashMap<>();
                    JSONObject children_object = jsonArray_children.getJSONObject(i);
                    map.put("id", children_object.getString("id"));
                    map.put("name", children_object.getString("name"));
                    map.put("bg_img", children_object.getString("bg_img"));
                    map.put("icon_img", children_object.getString("icon_img"));
                    if (children_object.has("children")) {
                        JSONArray jsonArray_children2 = children_object.getJSONArray("children");
                        map.put("children", jsonArray_children2.toString());
                    } else {
                        map.put("children", "");
                    }
                    gridViewList.add(map);
                }

                P4_ListViewItem_GridView_Adapter gridView_adapter = new P4_ListViewItem_GridView_Adapter(gridViewList);
                gridView.setAdapter(gridView_adapter);
                //禁止gridView获取焦点
                gridView.setFocusable(false);
                //固定GridView的高度和数量
                SetHeightUtil.setGridViewHeight(gridView, gridViewList.size(), 2);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position_inner, long id) {
                        //TODO GridView点击事件
                        String childrenJSON = data.get(position).get("children");
                        if (!childrenJSON.equals("")) {
                            //拿到二级JSON，变量名后的s代表二级second
                            try {
                                JSONTokener jsonTokener_s = new JSONTokener(childrenJSON);
                                JSONArray jsonArray_s = (JSONArray) jsonTokener_s.nextValue();
                                JSONObject jsonObject_s = jsonArray_s.getJSONObject(position_inner);

                                String id_ = jsonObject_s.getString("id");
                                Intent intent = new Intent(activity, CourseListActivity.class);
                                intent.putExtra("series", "series_2");
                                intent.putExtra("id", id_);
                                activity.startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    //ListView子项GridView适配器
    static class P4_ListViewItem_GridView_Adapter extends BaseAdapter {
        private List<Map<String, String>> data;

        P4_ListViewItem_GridView_Adapter(List<Map<String, String>> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
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
            LayoutInflater inflater = LayoutInflater.from(MyApplication.context);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.p4_listviewitem_gridviewitem, parent, false);
            }
            ImageView gridview1_imageview = convertView.findViewById(R.id.gridview1_imageview);
            TextView gridview1_textview = convertView.findViewById(R.id.gridview1_textview);
            gridview1_textview.setText(data.get(position).get("name"));

            RequestOptions options = new RequestOptions().fitCenter();
            if (Util.isOnMainThread()) {
                Glide.with(MyApplication.context).load(data.get(position).get("bg_img")).apply(options).into(gridview1_imageview);
            }

            return convertView;
        }
    }
}
