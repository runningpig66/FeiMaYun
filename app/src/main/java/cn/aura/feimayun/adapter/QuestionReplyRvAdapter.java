package cn.aura.feimayun.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cn.aura.feimayun.R;
import cn.aura.feimayun.application.MyApplication;
import cn.aura.feimayun.util.Util;

public class QuestionReplyRvAdapter extends RecyclerView.Adapter<QuestionReplyRvAdapter.ImageViewHolder> {
    private Context mContext;
    private ArrayList<String> mList;
    private LayoutInflater mInflater;
    private OnItemClickListener mOnItemClickListener;

    public QuestionReplyRvAdapter(Context context, ArrayList<String> list) {
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_content_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {
        if (position < mList.size()) {
            //代表+号之前的需要正常显示图片
            holder.adbutton.setVisibility(View.VISIBLE);
            String picUrl = mList.get(position);//图片路径
            if (Util.isOnMainThread()) {
                Glide.with(MyApplication.context).load(picUrl).into(holder.mIvImage);
            }

        } else {
            holder.adbutton.setVisibility(View.INVISIBLE);
            holder.mIvImage.setImageResource(R.drawable.item_add_border);
        }
        if (mOnItemClickListener != null) {
            holder.mIvImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onPictureClick(v, position);
                }
            });
            holder.adbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onRedButtonClick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = mList == null ? 1 : mList.size() + 1;
        if (count > 4) {
            return mList.size();
        } else {
            return count;
        }
    }

    public interface OnItemClickListener {
        void onPictureClick(View view, int position);

        void onRedButtonClick(View view, int position);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView mIvImage;
        ImageView adbutton;

        ImageViewHolder(View itemView) {
            super(itemView);
            mIvImage = itemView.findViewById(R.id.iv_album_content_image);
            adbutton = itemView.findViewById(R.id.adbutton);
        }
    }

}
