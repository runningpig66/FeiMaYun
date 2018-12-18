package cn.aura.feimayun.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.PhotoViewActivity;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.util.ScreenUtils;

public class MyStudiesInfo2_RecyclerView_Adapter extends RecyclerView.Adapter<MyStudiesInfo2_RecyclerView_Adapter.ViewHolder> {
    private Context context;
    private List<Map<String, String>> mapList;
    private int itemWidth;
    private OnItemClickListener mOnItemClickListener;

    public MyStudiesInfo2_RecyclerView_Adapter(Context context, List<Map<String, String>> mapList) {
        this.context = context;
        this.mapList = mapList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setData(List<Map<String, String>> mapList) {
        this.mapList = mapList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mystudiesinfo2_recyclerview_item, parent, false);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenWidth = displayMetrics.widthPixels;
        itemWidth = (mScreenWidth - ScreenUtils.dp2px(context, 84)) / 4;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (position == 0) {
            holder.info2_recycler_view0.setVisibility(View.VISIBLE);
        } else {
            holder.info2_recycler_view0.setVisibility(View.GONE);
        }

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });
        }

        Map<String, String> map = mapList.get(position);
        holder.info2_item_textview1.setText(map.get("title"));
        if (map.get("content_font") == null || map.get("content_font").equals("")) {
            holder.info2_item_textview2.setVisibility(View.GONE);
        } else {
            holder.info2_item_textview2.setVisibility(View.VISIBLE);
            holder.info2_item_textview2.setText(map.get("content_font"));
        }
        Glide.with(context).load(map.get("smaller")).into(holder.info2_item_imageview1);
        holder.info2_item_textview3.setText(map.get("name"));
        holder.info2_item_textview4.setText(map.get("create_time"));
        holder.info2_item_layout1.removeAllViews();

        //开始获取提问图片，存入String[]
        String qa_imgString = map.get("content_img");
        if (qa_imgString != null) {
            JSONTokener jsonTokener = new JSONTokener(qa_imgString);
            try {
                JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                if (jsonArray.length() > 0) {
                    holder.info2_item_layout1.setVisibility(View.VISIBLE);
                    holder.info2_item_layout1.removeAllViews();
                    LayoutInflater inflater = LayoutInflater.from(context);
                    final List<String> stringList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String imgUrl = jsonArray.getString(i);
                        stringList.add(imgUrl);
                        ImageView info2_imageview = (ImageView) inflater.inflate(R.layout.info2_recyclerview_imageview, null);
                        Glide.with(context).load(imgUrl).into(info2_imageview);
                        LinearLayout linearLayout = new LinearLayout(context);
                        linearLayout.addView(info2_imageview, itemWidth, itemWidth);
                        final int finalI = i;
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, PhotoViewActivity.class);
                                Bundle bundle = new Bundle();
                                List_Bean bean = new List_Bean();
                                bean.setStringList(stringList);
                                bundle.putSerializable("questionlistdataBean", bean);
                                intent.putExtras(bundle);
                                intent.putExtra("currentPosition", finalI);
                                context.startActivity(intent);
                            }
                        });

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        if (i > 0) {
                            params.leftMargin = ScreenUtils.dp2px(context, 10);
                        }
                        holder.info2_item_layout1.addView(linearLayout, params);
                    }
                } else {
                    holder.info2_item_layout1.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView info2_item_textview1;
        TextView info2_item_textview2;
        TextView info2_item_textview3;//学员姓名
        ImageView info2_item_imageview1;//学员头像
        TextView info2_item_textview4;//时间
        LinearLayout info2_item_layout0;
        LinearLayout info2_item_layout1;//图片布局
        View info2_recycler_view0;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            info2_recycler_view0 = itemView.findViewById(R.id.info2_recycler_view0);
            info2_item_textview1 = itemView.findViewById(R.id.info2_item_textview1);
            info2_item_textview2 = itemView.findViewById(R.id.info2_item_textview2);
            info2_item_textview3 = itemView.findViewById(R.id.info2_item_textview3);
            info2_item_textview4 = itemView.findViewById(R.id.info2_item_textview4);
            info2_item_imageview1 = itemView.findViewById(R.id.info2_item_imageview1);
            info2_item_layout0 = itemView.findViewById(R.id.info2_item_layout0);
            info2_item_layout1 = itemView.findViewById(R.id.info2_item_layout1);
        }
    }

}
