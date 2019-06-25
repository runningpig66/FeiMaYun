package cn.aura.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.aura.app.R;
import cn.aura.app.bean.MyStuidesInfo3Bean;

public class MyStudiesInfo3_RecyclerView_Adapter extends RecyclerView.Adapter<MyStudiesInfo3_RecyclerView_Adapter.ViewHolder> {
    private Context context;
    //    private List<Map<String, String>> mapList;
    private List<MyStuidesInfo3Bean.DataBean> dataBeanList;

    public MyStudiesInfo3_RecyclerView_Adapter(Context context, List<MyStuidesInfo3Bean.DataBean> dataBeanList) {
        this.context = context;
        this.dataBeanList = dataBeanList;
//        this.mapList = mapList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mystudiesinfo3_recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            holder.info3_recycler_view0.setVisibility(View.VISIBLE);
        } else {
            holder.info3_recycler_view0.setVisibility(View.GONE);
        }

//        Map<String, String> map = mapList.get(position);
        MyStuidesInfo3Bean.DataBean dataBean = dataBeanList.get(position);
        holder.recyclerview_item_textview1.setText(dataBean.getName());
        holder.recyclerview_item_textview2.setText("学习周期：" + dataBean.getStime() + "~" + dataBean.getEtime());
        holder.recyclerview_item_textview3.setText("结束时间" + dataBean.getTimer());
    }

    @Override
    public int getItemCount() {
        return dataBeanList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recyclerview_item_textview1;
        TextView recyclerview_item_textview2;
        TextView recyclerview_item_textview3;
        View info3_recycler_view0;

        ViewHolder(View itemView) {
            super(itemView);
            recyclerview_item_textview1 = itemView.findViewById(R.id.recyclerview_item_textview1);
            recyclerview_item_textview2 = itemView.findViewById(R.id.recyclerview_item_textview2);
            recyclerview_item_textview3 = itemView.findViewById(R.id.recyclerview_item_textview3);
            info3_recycler_view0 = itemView.findViewById(R.id.info3_recycler_view0);
        }
    }

}
