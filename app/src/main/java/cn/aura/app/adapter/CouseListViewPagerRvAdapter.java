package cn.aura.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Map;

import cn.aura.app.R;
import cn.aura.app.application.MyApplication;
import cn.aura.app.util.Util;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;

public class CouseListViewPagerRvAdapter extends RecyclerView.Adapter<CouseListViewPagerRvAdapter.MyViewHolder> {
    private Context context;
    private List<Map<String, String>> data_List;
    private LayoutInflater mInflater;
    private OnItemClickListener mOnItemClickListener;

    public CouseListViewPagerRvAdapter(Context context, List<Map<String, String>> data_List) {
        this.context = context;
        this.data_List = data_List;
        mInflater = LayoutInflater.from(context);
    }

    public void setItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setData_List(List<Map<String, String>> data_List) {
        this.data_List = data_List;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.courselist_viewpager_gridview_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Map<String, String> map = data_List.get(position);

        RequestOptions options = new RequestOptions().fitCenter();
        if (Util.isOnMainThread()) {
            Glide.with(MyApplication.context).load(map.get("bg_url")).apply(options).into(holder.courselist_viewpager_gridview_bg_url);
        }

        holder.courselist_viewpager_gridview_name.setText(map.get("name"));
        holder.courselist_viewpager_gridview_price.setPaintFlags(ANTI_ALIAS_FLAG);
        holder.courselist_viewpager_gridview_price.setPaintFlags(STRIKE_THRU_TEXT_FLAG);
        holder.courselist_viewpager_gridview_price.setText(map.get("price"));
        holder.courselist_viewpager_gridview_rprice.setText(map.get("rprice"));
        holder.courselist_viewpager_gridview_browse.setText(map.get("browse"));

        //根据teach_type的不同设置不同的布局，其实这里最多隐藏一个TextView
        int teach_type = Integer.parseInt(map.get("teach_type"));
        switch (teach_type) {
            case 1://直播课程
                holder.courselist_viewpager_gridview_text1.setText("直播时间:" + map.get("start_ts"));
                holder.courselist_viewpager_gridview_text2.setText("简介:" + map.get("title"));
                holder.courselist_viewpager_gridview_textview1.setText(map.get("stat"));

                switch (map.get("liveStatus")) {
                    case "1"://正在直播红色
                        holder.courselist_viewpager_gridview_textview1.setTextColor(Color.parseColor("#f25051"));
                        break;
                    case "2"://即将开始绿色，课程预约中
                        holder.courselist_viewpager_gridview_textview1.setTextColor(Color.parseColor("#00a63b"));
                        break;
                    case "5"://正在回放灰色
                        holder.courselist_viewpager_gridview_textview1.setTextColor(Color.parseColor("#000000"));
                        break;
                    default://其他状态黑色，包括：回放过期、点播
                        holder.courselist_viewpager_gridview_textview1.setTextColor(Color.parseColor("#999999"));
                        break;
                }

                holder.courselist_viewpager_gridview_text3.setVisibility(View.INVISIBLE);
                holder.courselist_viewpager_gridview_textview1.setVisibility(View.VISIBLE);//当前正在直播
                break;
            case 2://录播课程
                holder.courselist_viewpager_gridview_text1.setText("课程时长:" + map.get("hours"));
                holder.courselist_viewpager_gridview_text2.setText("简介:" + map.get("title"));
                holder.courselist_viewpager_gridview_text3.setVisibility(View.INVISIBLE);
                holder.courselist_viewpager_gridview_textview1.setVisibility(View.GONE);
                break;
            case 3://课程包
                holder.courselist_viewpager_gridview_text1.setText("课程视频共" + map.get("mediaTotal") + "个");
                holder.courselist_viewpager_gridview_text2.setText("模拟题库共" + map.get("paperTotal") + "套试卷");
                holder.courselist_viewpager_gridview_text3.setVisibility(View.VISIBLE);
                holder.courselist_viewpager_gridview_text3.setText("专项答疑共" + map.get("dyTotal") + "条");
                holder.courselist_viewpager_gridview_textview1.setVisibility(View.GONE);
                break;
            case 4://面授课程
                holder.courselist_viewpager_gridview_text1.setText("面授地点:" + map.get("address"));
                holder.courselist_viewpager_gridview_text2.setText("面授时间:" + map.get("lesson_time"));
                holder.courselist_viewpager_gridview_text3.setVisibility(View.INVISIBLE);
                holder.courselist_viewpager_gridview_textview1.setVisibility(View.GONE);
                break;
        }

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(v, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data_List == null ? 0 : data_List.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView courselist_viewpager_gridview_bg_url;
        TextView courselist_viewpager_gridview_name;
        TextView courselist_viewpager_gridview_rprice;
        TextView courselist_viewpager_gridview_price;
        TextView courselist_viewpager_gridview_text1;
        TextView courselist_viewpager_gridview_text2;
        TextView courselist_viewpager_gridview_text3;
        TextView courselist_viewpager_gridview_browse;
        TextView courselist_viewpager_gridview_textview1;//正在直播提示

        MyViewHolder(View itemView) {
            super(itemView);
            courselist_viewpager_gridview_bg_url = itemView.findViewById(R.id.courselist_viewpager_gridview_bg_url);
            courselist_viewpager_gridview_name = itemView.findViewById(R.id.courselist_viewpager_gridview_name);
            courselist_viewpager_gridview_rprice = itemView.findViewById(R.id.courselist_viewpager_gridview_rprice);
            courselist_viewpager_gridview_price = itemView.findViewById(R.id.courselist_viewpager_gridview_price);
            courselist_viewpager_gridview_text1 = itemView.findViewById(R.id.courselist_viewpager_gridview_text1);
            courselist_viewpager_gridview_text2 = itemView.findViewById(R.id.courselist_viewpager_gridview_text2);
            courselist_viewpager_gridview_text3 = itemView.findViewById(R.id.courselist_viewpager_gridview_text3);
            courselist_viewpager_gridview_browse = itemView.findViewById(R.id.courselist_viewpager_gridview_browse);
            courselist_viewpager_gridview_textview1 = itemView.findViewById(R.id.courselist_viewpager_gridview_textview1);
        }
    }

}
