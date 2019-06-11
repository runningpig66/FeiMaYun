package cn.aura.feimayun.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.PaperListActivity;
import cn.aura.feimayun.util.Util;

public class ExamListAcitivty_ListView1_Adapter extends BaseAdapter {
    Activity activity;
    private List<Map<String, String>> dataList;

    public ExamListAcitivty_ListView1_Adapter(Activity activity, List<Map<String, String>> dataList) {
        this.activity = activity;
        this.dataList = dataList;
    }

    public void setData(List<Map<String, String>> dataList) {
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            convertView = inflater.inflate(R.layout.activity_exam_list_listview1_item, null);
        }
        TextView activity_exam_list_listview1_item_textview1 = convertView.findViewById(R.id.activity_exam_list_listview1_item_textview1);
//        ImageView activity_exam_list_listview1_item_imageview1 = convertView.findViewById(R.id.activity_exam_list_listview1_item_imageview1);
        TextView activity_exam_list_listview1_item_textview2 = convertView.findViewById(R.id.activity_exam_list_listview1_item_textview2);
        TextView activity_exam_list_listview1_item_textview3 = convertView.findViewById(R.id.activity_exam_list_listview1_item_textview3);
        activity_exam_list_listview1_item_textview1.setText(dataList.get(position).get("name"));

//        RequestOptions options = new RequestOptions().centerCrop();
//        Glide.with(activity).load(dataList.get(position).get("bg_img")).apply(options).into(activity_exam_list_listview1_item_imageview1);
        activity_exam_list_listview1_item_textview2.setText(
                "试卷数量:" + dataList.get(position).get("tp_total") + "个  " +
                        "试题数量:" + dataList.get(position).get("test_total") + "道  " +
                        "参与人数:" + dataList.get(position).get("total") + "人");
        activity_exam_list_listview1_item_textview3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = Util.getUid();
                if (uid.equals("")) {//如果uid还是0，就返回之前的页面
                    Toast.makeText(activity, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
                } else {
                    String id = dataList.get(position).get("id");
                    Intent intentPaperListActivity = new Intent(activity, PaperListActivity.class);
                    intentPaperListActivity.putExtra("sid", id);
                    activity.startActivity(intentPaperListActivity);
                }
            }
        });
        return convertView;
    }
}
