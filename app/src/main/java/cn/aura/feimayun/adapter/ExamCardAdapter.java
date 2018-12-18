package cn.aura.feimayun.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.aura.feimayun.R;

public class ExamCardAdapter extends RecyclerView.Adapter<ExamCardAdapter.MyViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<Boolean> mList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public ExamCardAdapter(Context context, List<Boolean> list) {
        this.mContext = context;
        this.mList = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.orange_square_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        boolean itemBoolean = mList.get(position);
        if (itemBoolean) {// 选择了：图片橘色，文字白色
            holder.textView.setTextColor(mContext.getResources().getColor(R.color.colorfefefe));
            holder.textView.setBackgroundResource(R.drawable.orange_square);
        } else {// 未选择：图片白色，文字橘色
            holder.textView.setTextColor(mContext.getResources().getColor(R.color.color666666));
            holder.textView.setBackgroundResource(R.drawable.gray_square);
        }
        holder.textView.setText(String.valueOf(position + 1));
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemCLick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setData(List<Boolean> mList) {
        this.mList = mList;
    }

    public interface OnItemClickListener {
        void onItemCLick(View view, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.squareTextView);
        }
    }

}
