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
import cn.aura.feimayun.activity.AnalysisActivity;

public class ExamReusltActivity_DoubleListView_Adapter extends BaseAdapter implements View.OnClickListener {
    private Activity activity;
    private List<Map<String, String>> itemList;
    private String tid;

    public ExamReusltActivity_DoubleListView_Adapter(Activity activity, List<Map<String, String>> itemList, String tid) {
        this.activity = activity;
        this.itemList = itemList;
        this.tid = tid;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            convertView = inflater.inflate(R.layout.activity_exam_result_double_listview_item, null);
        }
        TextView double_listview_textview1 = convertView.findViewById(R.id.double_listview_textview1);
        TextView double_listview_textview2 = convertView.findViewById(R.id.double_listview_textview2);
        TextView double_listview_textview4 = convertView.findViewById(R.id.double_listview_textview4);
        TextView double_listview_textview7 = convertView.findViewById(R.id.double_listview_textview7);
        TextView double_listview_textview8 = convertView.findViewById(R.id.double_listview_textview8);

        TextView double_listview_textview5 = convertView.findViewById(R.id.double_listview_textview5);
        TextView double_listview_textview6 = convertView.findViewById(R.id.double_listview_textview6);
        double_listview_textview5.setOnClickListener(this);
        double_listview_textview6.setOnClickListener(this);
        double_listview_textview5.setTag(position);
        double_listview_textview6.setTag(position);

        double_listview_textview1.setText(itemList.get(position).get("name"));
        double_listview_textview2.setText(itemList.get(position).get("rate") + "%");

        double_listview_textview4.setText("总题:" + itemList.get(position).get("total"));
        double_listview_textview7.setText("错题:" + itemList.get(position).get("wrong"));
        double_listview_textview8.setText("正题:" + itemList.get(position).get("right"));

        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.double_listview_textview5://错题解析
                int position2 = (int) v.getTag();
                if (!itemList.get(position2).get("wrong").equals("0")) {
                    Intent intent2 = new Intent(activity, AnalysisActivity.class);
                    intent2.putExtra("tid", tid);
                    if (!(itemList.get(position2).get("typer") == null)) {//按题型分析
                        intent2.putExtra("typer", itemList.get(position2).get("typer"));
                    } else {//按标签分析
                        intent2.putExtra("tids", itemList.get(position2).get("wrong_tids"));
                    }
                    intent2.putExtra("type", "2");
                    activity.startActivity(intent2);
                } else {
                    Toast.makeText(activity, "没有错题", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.double_listview_textview6://正题解析
                int position1 = (int) v.getTag();
                if (!itemList.get(position1).get("right").equals("0")) {
                    Intent intent1 = new Intent(activity, AnalysisActivity.class);
                    intent1.putExtra("tid", tid);
                    if (!(itemList.get(position1).get("typer") == null)) {
                        intent1.putExtra("typer", itemList.get(position1).get("typer"));
                    } else {
                        intent1.putExtra("tids", itemList.get(position1).get("right_tids"));
                    }
                    intent1.putExtra("type", "1");
                    activity.startActivity(intent1);
                } else {
                    Toast.makeText(activity, "没有正题", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
