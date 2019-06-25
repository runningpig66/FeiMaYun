package cn.aura.feimayun.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;

/**
 * 描述：FullCourseFragment中左侧的ListView的适配器
 */
public class FullCourse_ListView_Adapter extends BaseAdapter {
    //在FullCourseFragment的listView的item点击事件里获取到点击的position，保存到下面变量position中
    private int selected = 0;

    private List<? extends Map<String, ?>> data;
    private int resource;
    private LayoutInflater inflater;

    public FullCourse_ListView_Adapter(Activity activity, List<? extends Map<String, ?>> data, int resource) {
        this.data = data;
        this.resource = resource;
        inflater = LayoutInflater.from(activity);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(resource, null);
        TextView textView = convertView.findViewById(R.id.simple_button);
        textView.setText(data.get(position).get("name").toString());

        //如果textView被点击，改变样式
        if (position == selected) {
            textView.setBackgroundColor(0xffffffff);
            textView.setTextColor(0xffEE7708);
        } else {
            textView.setBackgroundColor(0xffeeeeee);
            textView.setTextColor(0xff666666);
        }
        return convertView;
    }

    //获取被点击的item的position
    public void setPosition(int selected) {
        this.selected = selected;
    }
}
