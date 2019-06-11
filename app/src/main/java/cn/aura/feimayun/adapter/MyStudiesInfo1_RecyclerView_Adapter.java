package cn.aura.feimayun.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.PlayDetailActivity;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.bean.MyStuidesInfo1Bean;
import cn.aura.feimayun.vhall.watch.WatchActivity;

public class MyStudiesInfo1_RecyclerView_Adapter extends RecyclerView.Adapter<MyStudiesInfo1_RecyclerView_Adapter.ViewHolder> {
    private Context context;
    //    private List<Map<String, String>> mapList;
    private List<MyStuidesInfo1Bean.DataBeanX.DataBean> dataBeanList;
    private String pkid;

    public MyStudiesInfo1_RecyclerView_Adapter(Context context, List<MyStuidesInfo1Bean.DataBeanX.DataBean> dataBeanList, String pkid) {
        this.context = context;
        this.dataBeanList = dataBeanList;
        this.pkid = pkid;
//        this.mapList = mapList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mystudiesinfo1_recyclerview_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
//                Map<String, String> map = mapList.get(position);
                MyStuidesInfo1Bean.DataBeanX.DataBean dataBean = dataBeanList.get(position);
                int type = dataBean.getType();
                if (type == 1) {
                    Intent intent1 = new Intent(context, WatchActivity.class);
                    intent1.putExtra("data_id", dataBean.getLid());
                    intent1.putExtra("data_teach_type", "1");
                    intent1.putExtra("pkid", pkid);
                    context.startActivity(intent1);
                } else if (type == 2) {
                    Intent intent2 = new Intent(context, PlayDetailActivity.class);
                    intent2.putExtra("data_id", dataBean.getLid());
                    intent2.putExtra("data_teach_type", "2");
                    intent2.putExtra("pkid", pkid);
                    context.startActivity(intent2);
                } else {
                    Toast.makeText(context, "敬请期待", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Map<String, String> map = mapList.get(position);
        MyStuidesInfo1Bean.DataBeanX.DataBean dataBean = dataBeanList.get(position);
        int type = dataBean.getType();
        if (type == 1) {
            holder.recyclerview_item_imageview2.setVisibility(View.GONE);
            Glide.with(MyApplication.context).load(dataBean.getBg_url()).into(holder.recyclerview_item_imageview1);
            holder.recyclerview_item_textview1.setText(dataBean.getName());
            holder.recyclerview_item_textview2.setText("开始时间：" + dataBean.getStart_ts() + "\n结束时间：" + dataBean.getEnd_ts());
            holder.recyclerview_item_textview4.setText(dataBean.getStat());

            int liveStatus = dataBean.getLiveStatus();
            if (liveStatus == 1) {//正在直播
                holder.recyclerview_item_textview3.setText("正在直播");
            } else if (liveStatus == 2) {//即将开始，课程预约中
                holder.recyclerview_item_textview3.setText("即将开始");
            } else if (liveStatus == 3) {
                holder.recyclerview_item_textview3.setText("直播已结束");
            } else if (liveStatus == 5) {//正在回放
                holder.recyclerview_item_textview3.setText("直播已结束");
            } else {
                holder.recyclerview_item_textview3.setText("直播已结束");
            }
        } else if (type == 2) {
            holder.recyclerview_item_imageview2.setVisibility(View.VISIBLE);
            Glide.with(MyApplication.context).load(dataBean.getBg_url()).into(holder.recyclerview_item_imageview1);
            holder.recyclerview_item_textview1.setText(dataBean.getName());
            holder.recyclerview_item_textview2.setText("共" + dataBean.getTotal() + "章，已学习到" + dataBean.getLearned() + "章");
            holder.recyclerview_item_textview3.setText("学习进度：" + dataBean.getRate());
            holder.recyclerview_item_textview4.setText(dataBean.getTyper());
        } else {
            //面授
            holder.recyclerview_item_imageview2.setVisibility(View.GONE);
            Glide.with(MyApplication.context).load(dataBean.getBg_url()).into(holder.recyclerview_item_imageview1);
            holder.recyclerview_item_textview1.setText(dataBean.getName());
            holder.recyclerview_item_textview2.setText("面授地点：" + dataBean.getAddress());
            holder.recyclerview_item_textview3.setText("面授时间：" + dataBean.getLesson_time());
        }
    }

    @Override
    public int getItemCount() {
        return dataBeanList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView recyclerview_item_imageview1;//左图
        ImageView recyclerview_item_imageview2;//书图标，直播隐藏
        TextView recyclerview_item_textview1;
        TextView recyclerview_item_textview2;
        TextView recyclerview_item_textview3;
        TextView recyclerview_item_textview4;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            recyclerview_item_imageview1 = itemView.findViewById(R.id.recyclerview_item_imageview1);
            recyclerview_item_imageview2 = itemView.findViewById(R.id.recyclerview_item_imageview2);
            recyclerview_item_textview1 = itemView.findViewById(R.id.recyclerview_item_textview1);
            recyclerview_item_textview2 = itemView.findViewById(R.id.recyclerview_item_textview2);
            recyclerview_item_textview3 = itemView.findViewById(R.id.recyclerview_item_textview3);
            recyclerview_item_textview4 = itemView.findViewById(R.id.recyclerview_item_textview4);
        }
    }

}
