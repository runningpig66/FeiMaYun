package cn.aura.app.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import cn.aura.app.R;

public class MessageCenterFragment_ListView_Adapter extends BaseAdapter {
    private Activity activity;
    private List<Map<String, String>> dataList;

    public MessageCenterFragment_ListView_Adapter(Activity activity, List<Map<String, String>> dataList) {
        this.activity = activity;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            convertView = inflater.inflate(R.layout.fragment_message_center_listview_item, parent, false);
        }
        ImageView message_center_imageview1 = convertView.findViewById(R.id.message_center_imageview1);
        TextView message_center_textview1 = convertView.findViewById(R.id.message_center_textview1);
        TextView message_center_textview2 = convertView.findViewById(R.id.message_center_textview2);
        TextView message_center_textview3 = convertView.findViewById(R.id.message_center_textview3);
        message_center_textview1.setText(dataList.get(position).get("title"));
        message_center_textview2.setText(dataList.get(position).get("create_time"));
        message_center_textview3.setText(dataList.get(position).get("content"));
        switch (dataList.get(position).get("is_read")) {
            case "1"://未读消息显示小红点
                message_center_imageview1.setVisibility(View.VISIBLE);
                break;
            case "2":
                message_center_imageview1.setVisibility(View.GONE);
                break;
            default:
                message_center_imageview1.setVisibility(View.GONE);
                break;
        }
        return convertView;
    }
}
