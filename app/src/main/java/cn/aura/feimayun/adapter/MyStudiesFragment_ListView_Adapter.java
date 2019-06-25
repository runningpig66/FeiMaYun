package cn.aura.feimayun.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.view.CornerLabelView;

/**
 * 描述：我的学习页面下方的ListView的适配器
 */
public class MyStudiesFragment_ListView_Adapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Map<String, String>> userList;
    private OnButtonClickListener mOnButtonClickListener;

    public MyStudiesFragment_ListView_Adapter(Activity activity, List<Map<String, String>> userList) {
        this.userList = userList;
        inflater = LayoutInflater.from(activity);
    }

    public void setData(List<Map<String, String>> userList) {
        this.userList = userList;
    }

    public void setmOnButtonClickListener(OnButtonClickListener listener) {
        mOnButtonClickListener = listener;
    }

    @Override
    public int getCount() {
        return userList == null ? 0 : userList.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        Map<String, String> userListItem = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = inflater.inflate(R.layout.fragment_my_studies_listview_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.cornerView = view.findViewById(R.id.cornerView);
            viewHolder.mystudies_textview1 = view.findViewById(R.id.mystudies_textview1);
            viewHolder.mystudies_textview2 = view.findViewById(R.id.mystudies_textview2);
            viewHolder.mystudies_textview3 = view.findViewById(R.id.mystudies_textview3);
            viewHolder.mystudies_textview4 = view.findViewById(R.id.mystudies_textview4);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        String teach_type = userListItem.get("teach_type");
        if (teach_type != null) {
            if (teach_type.equals("1")) {//直播
                viewHolder.cornerView.setText("直播");
                viewHolder.cornerView.setBgColor(Color.RED);
            } else {
                viewHolder.cornerView.setText("课程");
                viewHolder.cornerView.setBgColor(Color.parseColor("#00a63b"));
            }
        } else {
            viewHolder.cornerView.setText("课程");
            viewHolder.cornerView.setBgColor(Color.parseColor("#00a63b"));
        }

//        else if (teach_type.equals("2")) {//录播
//            viewHolder.cornerView.setText("录播");
//            viewHolder.cornerView.setBgColor(Color.parseColor("#ee7708"));
//        } else if (teach_type.equals("3")) {//混合
//            viewHolder.cornerView.setText("混合");
//            viewHolder.cornerView.setBgColor(Color.parseColor("#00a63b"));
//        }

        viewHolder.mystudies_textview1.setText(userListItem.get("name"));
        viewHolder.mystudies_textview2.setText("学习进度：" + userListItem.get("stat"));
        viewHolder.mystudies_textview3.setText("学习有效期：" + userListItem.get("expire"));
        viewHolder.mystudies_textview4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnButtonClickListener != null) {
                    mOnButtonClickListener.onItemClick(v, position);
                }
            }
        });
        return view;
    }

    public interface OnButtonClickListener {
        void onItemClick(View view, int position);
    }

    class ViewHolder {
        CornerLabelView cornerView;
        TextView mystudies_textview1;
        TextView mystudies_textview2;
        TextView mystudies_textview3;
        TextView mystudies_textview4;
    }
}
