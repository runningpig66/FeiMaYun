package cn.aura.feimayun.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;

public class LiveListActivityListViewAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, String>> dataList;

    public LiveListActivityListViewAdapter(Context context, List<Map<String, String>> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    public void setData(List<Map<String, String>> dataList) {
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();

    }

    @Override
    public Map<String, String> getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> dataMap = getItem(position);
        ViewHolder viewHolder;
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_livelist_listview_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.livelist_listview_textview1 = view.findViewById(R.id.livelist_listview_textview1);
            viewHolder.livelist_listview_imageview2 = view.findViewById(R.id.livelist_listview_imageview2);
            viewHolder.livelist_listview_textview2 = view.findViewById(R.id.livelist_listview_textview2);
            viewHolder.livelist_listview_textview3 = view.findViewById(R.id.livelist_listview_textview3);
            viewHolder.livelist_listview_textview4 = view.findViewById(R.id.livelist_listview_textview4);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.livelist_listview_textview1.setText(dataMap.get("start_ts"));
        Glide.with(context).load(dataMap.get("bg_url")).into(viewHolder.livelist_listview_imageview2);
        viewHolder.livelist_listview_textview2.setText(dataMap.get("name"));
        viewHolder.livelist_listview_textview3.setText("观看人数：" + dataMap.get("browse"));

        String liveStatus = dataMap.get("liveStatus");
        if (liveStatus.equals("1")) {//正在直播红色
            viewHolder.livelist_listview_textview4.setTextColor(Color.parseColor("#f25051"));
        } else if (liveStatus.equals("2")) {//即将开始绿色，课程预约中
            viewHolder.livelist_listview_textview4.setTextColor(Color.parseColor("#00a63b"));
        } else if (liveStatus.equals("3")) {//直播已结束
            viewHolder.livelist_listview_textview4.setTextColor(Color.parseColor("#999999"));
        } else if (liveStatus.equals("5")) {//正在回放黑色
            viewHolder.livelist_listview_textview4.setTextColor(Color.parseColor("#000000"));
        } else {//其他状态灰色，包括：回放过期、点播
            viewHolder.livelist_listview_textview4.setTextColor(Color.parseColor("#999999"));
        }
        viewHolder.livelist_listview_textview4.setText(dataMap.get("stat"));
        return view;
    }

    class ViewHolder {
        TextView livelist_listview_textview1;//start_ts
        ImageView livelist_listview_imageview2;//bg_url
        TextView livelist_listview_textview2;//name
        TextView livelist_listview_textview3;//browse
        TextView livelist_listview_textview4;//stat
    }

}
