package cn.aura.feimayun.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;

/**
 * 描述：我的学习页面下方的ListView的适配器
 */
public class MyStudiesFragment_ListView_Adapter extends BaseAdapter {
    private Activity activity;
    private List<Map<String, String>> userList;

    public MyStudiesFragment_ListView_Adapter(Activity activity, List<Map<String, String>> userList) {
        this.activity = activity;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        if (userList == null) {
            return 0;
        } else {
            return userList.size();
        }
    }

    @Override
    public Map<String, String> getItem(int position) {
        if (userList == null) {
            return null;
        } else {
            return userList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> map = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.fragment_my_studies_listview_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.fragment_my_studies_listview_item_textView1 =
                    view.findViewById(R.id.fragment_my_studies_listview_item_textView1);
            viewHolder.fragment_my_studies_listview_item_imageview3 =
                    view.findViewById(R.id.fragment_my_studies_listview_item_imageview3);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        //TODO 临时改动
        if ((map.get("name") == null)) {
            viewHolder.fragment_my_studies_listview_item_textView1.setText("");
        } else {
            viewHolder.fragment_my_studies_listview_item_textView1.setText(map.get("name"));
        }

        RequestOptions options = new RequestOptions()
                .centerCrop();
//                .placeholder(R.drawable.live_userimg);
        if ((map.get("bg_url") == null)) {
            Glide.with(activity).load(R.drawable.guanghuanguoji_auto).apply(options).into(viewHolder.fragment_my_studies_listview_item_imageview3);
        } else {
            Glide.with(activity).load(map.get("bg_url")).apply(options).into(viewHolder.fragment_my_studies_listview_item_imageview3);
        }
        return view;
    }

    class ViewHolder {
        TextView fragment_my_studies_listview_item_textView1;
        ImageView fragment_my_studies_listview_item_imageview3;
    }
}
