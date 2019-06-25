package cn.aura.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import cn.aura.app.R;
import cn.aura.app.activity.PhotoViewActivity;
import cn.aura.app.activity.QuestionReplyActivity;
import cn.aura.app.application.MyApplication;
import cn.aura.app.bean.List_Bean;
import cn.aura.app.bean.QuestionDetailBean;
import cn.aura.app.util.ScreenUtils;
import cn.aura.app.util.Util;

public class QuestionDetailRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ONE = 1;
    private static final int TYPE_TWO = 2;

    private int mScreenWidth;

    private LayoutInflater mInflater;
    private Activity mContext;
    private QuestionDetailBean.DataBean dataBean;

    public QuestionDetailRvAdapter(Context mContext, QuestionDetailBean.DataBean dataBean) {
        this.mContext = (Activity) mContext;
        this.dataBean = dataBean;
        this.mInflater = LayoutInflater.from(mContext);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
    }

    public void setData(QuestionDetailBean.DataBean dataBean) {
        this.dataBean = dataBean;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ONE:
                return new TypeOneViewHolder(mInflater.inflate(R.layout.questiondetail_rvitem_0ji, parent, false));
            case TYPE_TWO:
                return new TypeTwoViewHolder(mInflater.inflate(R.layout.questiondetail_rvitem_1ji, parent, false));
        }
        return new TypeTwoViewHolder(mInflater.inflate(R.layout.questiondetail_rvitem_1ji, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TypeAbstractViewHolder) holder).bindHolder(dataBean, position);
    }

    @Override
    public int getItemCount() {
        return dataBean.getReply() == null ? 1 : dataBean.getReply().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_ONE;
        } else {
            return TYPE_TWO;
        }
    }

    abstract class TypeAbstractViewHolder extends RecyclerView.ViewHolder {

        TypeAbstractViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bindHolder(QuestionDetailBean.DataBean dataBean, int position);
    }

    class TypeOneViewHolder extends TypeAbstractViewHolder {
        TextView rvitem0ji_textview1;//问题标题
        TextView rvitem0ji_textview2;//问题描述
        ImageView rvitem0ji_imageview1;//头像
        TextView rvitem0ji_textview3;//姓名
        TextView rvitem0ji_textview4;//日期
        TextView rvitem0ji_textview5;//回复按钮
        LinearLayout rvitem0ji_layout2;//回复按钮布局
        LinearLayout rvitem0ji_layout1;//回复图片0~4张

        TypeOneViewHolder(View itemView) {
            super(itemView);
            rvitem0ji_textview1 = itemView.findViewById(R.id.rvitem0ji_textview1);
            rvitem0ji_textview2 = itemView.findViewById(R.id.rvitem0ji_textview2);
            rvitem0ji_imageview1 = itemView.findViewById(R.id.rvitem0ji_imageview1);
            rvitem0ji_textview3 = itemView.findViewById(R.id.rvitem0ji_textview3);
            rvitem0ji_textview4 = itemView.findViewById(R.id.rvitem0ji_textview4);
            rvitem0ji_textview5 = itemView.findViewById(R.id.rvitem0ji_textview5);
            rvitem0ji_layout2 = itemView.findViewById(R.id.rvitem0ji_layout2);
            rvitem0ji_layout1 = itemView.findViewById(R.id.rvitem0ji_layout1);
        }

        @Override
        public void bindHolder(final QuestionDetailBean.DataBean dataBean, int position) {//TODO 添加0级
            rvitem0ji_textview1.setText(dataBean.getTitle());
            if (dataBean.getContent_font() == null || dataBean.getContent_font().equals("")) {
                rvitem0ji_textview2.setVisibility(View.GONE);
            } else {
                rvitem0ji_textview2.setVisibility(View.VISIBLE);
                rvitem0ji_textview2.setText(dataBean.getContent_font());
            }
            if (Util.isOnMainThread()) {
                RequestOptions options = new RequestOptions().fitCenter();
                Glide.with(MyApplication.context).load(dataBean.getAvater()).apply(options).into(rvitem0ji_imageview1);
            }

            rvitem0ji_textview3.setText(dataBean.getNick_name());
            rvitem0ji_textview4.setText(dataBean.getCreate_time());
            rvitem0ji_textview5.setText("回复");
            rvitem0ji_layout2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//问题的回复按钮被点击，表示添加一级回复
                    Intent intent = new Intent(mContext, QuestionReplyActivity.class);
                    intent.putExtra("qaid", dataBean.getId());
                    intent.putExtra("pid", dataBean.getId());
                    intent.putExtra("leve", "1");
                    mContext.startActivityForResult(intent, 9876);
                }
            });
            if (dataBean.getContent_img() != null) {
                final List<String> content_img = dataBean.getContent_img();
                if (!content_img.isEmpty()) {
                    rvitem0ji_layout1.setVisibility(View.VISIBLE);
                    rvitem0ji_layout1.removeAllViews();
                    for (int i = 0; i < content_img.size(); i++) {
                        ImageView imageView = (ImageView) mInflater.inflate(R.layout.info2_recyclerview_imageview, null);
                        Glide.with(MyApplication.context).load(content_img.get(i)).into(imageView);
                        LinearLayout linearLayout = new LinearLayout(mContext);
                        int itemWidth = (mScreenWidth - ScreenUtils.dp2px(mContext, 84)) / 4;
                        linearLayout.addView(imageView, itemWidth, itemWidth);
                        final int finalI = i;
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, PhotoViewActivity.class);
                                Bundle bundle = new Bundle();
                                List_Bean bean = new List_Bean();
                                bean.setStringList(content_img);
                                bundle.putSerializable("questionlistdataBean", bean);
                                intent.putExtras(bundle);
                                intent.putExtra("currentPosition", finalI);
                                mContext.startActivity(intent);
                            }
                        });
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        if (i > 0) {
                            params.leftMargin = ScreenUtils.dp2px(mContext, 10);
                        }
                        rvitem0ji_layout1.addView(linearLayout, params);
                    }
                } else {
                    rvitem0ji_layout1.setVisibility(View.GONE);
                }
            }

        }
    }

    class TypeTwoViewHolder extends TypeAbstractViewHolder {
        LinearLayout rvitem1ji_layout0;//总体布局
        TextView rvitem1ji_textview1;//姓名
        TextView rvitem1ji_textview2;//日期
        TextView rvitem1ji_textview3;//回复内容
        LinearLayout rvitem1ji_layout2;//回复按钮布局
        TextView rvitem1ji_textview4;//回复按钮
        LinearLayout rvitem1ji_layout1;//动态加图
        LinearLayout rvitem1ji_layout4;//添加二级回复的布局
        View rvitem1ji_view1;//分割线

        TypeTwoViewHolder(View itemView) {
            super(itemView);
            rvitem1ji_layout0 = itemView.findViewById(R.id.rvitem1ji_layout0);
            rvitem1ji_textview1 = itemView.findViewById(R.id.rvitem1ji_textview1);
            rvitem1ji_textview2 = itemView.findViewById(R.id.rvitem1ji_textview2);
            rvitem1ji_textview3 = itemView.findViewById(R.id.rvitem1ji_textview3);
            rvitem1ji_layout2 = itemView.findViewById(R.id.rvitem1ji_layout2);
            rvitem1ji_textview4 = itemView.findViewById(R.id.rvitem1ji_textview4);
            rvitem1ji_layout1 = itemView.findViewById(R.id.rvitem1ji_layout1);
            rvitem1ji_layout4 = itemView.findViewById(R.id.rvitem1ji_layout4);
            rvitem1ji_view1 = itemView.findViewById(R.id.rvitem1ji_view1);
        }

        @Override
        public void bindHolder(final QuestionDetailBean.DataBean dataBean, int position) {//TODO 添加1级回复
            final QuestionDetailBean.DataBean.ReplyBean replyBean = dataBean.getReply().get(position - 1);
            rvitem1ji_view1.setVisibility(View.GONE);
            rvitem1ji_textview1.setText(replyBean.getNick_name());
            rvitem1ji_textview2.setText(replyBean.getCreate_time());
            if (replyBean.getContent_font() == null || replyBean.getContent_font().equals("")) {
                rvitem1ji_textview3.setVisibility(View.GONE);
            } else {
                rvitem1ji_textview3.setVisibility(View.VISIBLE);
                rvitem1ji_textview3.setText(replyBean.getContent_font());
            }
            rvitem1ji_textview4.setText("回复");
            rvitem1ji_layout2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//点击了一级回复的回复按钮，表示添加二级回复
                    Intent intent = new Intent(mContext, QuestionReplyActivity.class);
                    intent.putExtra("qaid", dataBean.getId());
                    intent.putExtra("leve", "2");
                    intent.putExtra("pid", replyBean.getId());
                    mContext.startActivityForResult(intent, 9876);
                }
            });
            if (replyBean.getContent_img() != null) {
                final List<String> content_img = replyBean.getContent_img();
                if (!content_img.isEmpty()) {
                    rvitem1ji_layout1.setVisibility(View.VISIBLE);
                    rvitem1ji_layout1.removeAllViews();
                    for (int i = 0; i < content_img.size(); i++) {
                        ImageView imageView = (ImageView) mInflater.inflate(R.layout.info2_recyclerview_imageview, null);
                        Glide.with(MyApplication.context).load(content_img.get(i)).into(imageView);
                        LinearLayout linearLayout = new LinearLayout(mContext);
                        int itemWidth = (mScreenWidth - ScreenUtils.dp2px(mContext, 65)) / 4;
                        linearLayout.addView(imageView, itemWidth, itemWidth);
                        final int finalI = i;
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, PhotoViewActivity.class);
                                Bundle bundle = new Bundle();
                                List_Bean bean = new List_Bean();
                                bean.setStringList(content_img);
                                bundle.putSerializable("questionlistdataBean", bean);
                                intent.putExtras(bundle);
                                intent.putExtra("currentPosition", finalI);
                                mContext.startActivity(intent);
                            }
                        });
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        if (i > 0) {
                            params.leftMargin = ScreenUtils.dp2px(mContext, 10);
                        }
                        rvitem1ji_layout1.addView(linearLayout, params);
                    }
                } else {
                    rvitem1ji_layout1.setVisibility(View.GONE);
                }
            }

            if (replyBean.getComment() != null) {//TODO 添加二级
                rvitem1ji_layout4.setVisibility(View.VISIBLE);
                rvitem1ji_layout4.removeAllViews();
                List<QuestionDetailBean.DataBean.ReplyBean.CommentBean> commentBeanList = replyBean.getComment();
                for (int i = 0; i < commentBeanList.size(); i++) {
                    final QuestionDetailBean.DataBean.ReplyBean.CommentBean commentBean = commentBeanList.get(i);
                    View view = mInflater.inflate(R.layout.questiondetail_rvitem_1ji, null);
                    LinearLayout rvitem2ji_layout0;//总体布局
                    TextView rvitem2ji_textview1;//姓名
                    TextView rvitem2ji_textview2;//日期
                    TextView rvitem2ji_textview3;//回复内容
                    LinearLayout rvitem2ji_layout2;//回复按钮布局
                    TextView rvitem2ji_textview4;//回复按钮
                    LinearLayout rvitem2ji_layout1;//动态加图
                    LinearLayout rvitem2ji_layout4;//添加三级回复的布局
                    View rvitem2ji_view1;//分割线

                    rvitem2ji_layout0 = view.findViewById(R.id.rvitem1ji_layout0);
                    rvitem2ji_textview1 = view.findViewById(R.id.rvitem1ji_textview1);
                    rvitem2ji_textview2 = view.findViewById(R.id.rvitem1ji_textview2);
                    rvitem2ji_textview3 = view.findViewById(R.id.rvitem1ji_textview3);
                    rvitem2ji_layout2 = view.findViewById(R.id.rvitem1ji_layout2);
                    rvitem2ji_textview4 = view.findViewById(R.id.rvitem1ji_textview4);
                    rvitem2ji_layout1 = view.findViewById(R.id.rvitem1ji_layout1);
                    rvitem2ji_layout4 = view.findViewById(R.id.rvitem1ji_layout4);
                    rvitem2ji_view1 = view.findViewById(R.id.rvitem1ji_view1);

                    rvitem2ji_layout0.setBackgroundColor(Color.parseColor("#F0F0F0"));
                    rvitem2ji_textview1.setText(commentBean.getNick_name());
                    rvitem2ji_textview2.setText(commentBean.getCreate_time());
                    if (commentBean.getContent_font() == null || commentBean.getContent_font().equals("")) {
                        rvitem2ji_textview3.setVisibility(View.GONE);
                    } else {
                        rvitem2ji_textview3.setVisibility(View.VISIBLE);
                        rvitem2ji_textview3.setText(commentBean.getContent_font());
                    }
                    rvitem2ji_textview4.setText("回复@Ta");
                    rvitem2ji_layout2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {//点击了二级回复的回复按钮，表示添加三级回复
                            Intent intent = new Intent(mContext, QuestionReplyActivity.class);
                            intent.putExtra("qaid", dataBean.getId());
                            intent.putExtra("leve", "3");
                            intent.putExtra("pid", commentBean.getId());
                            intent.putExtra("call_uid", commentBean.getUid());
                            mContext.startActivityForResult(intent, 9876);
                        }
                    });

                    if (commentBean.getContent_img() != null) {
                        final List<String> content_img = commentBean.getContent_img();
                        if (!content_img.isEmpty()) {
                            rvitem2ji_layout1.setVisibility(View.VISIBLE);
                            rvitem2ji_layout1.removeAllViews();
                            for (int j = 0; j < content_img.size(); j++) {
                                ImageView imageView = (ImageView) mInflater.inflate(R.layout.info2_recyclerview_imageview, null);
                                Glide.with(MyApplication.context).load(content_img.get(j)).into(imageView);
                                LinearLayout linearLayout = new LinearLayout(mContext);
                                int itemWidth = (mScreenWidth - ScreenUtils.dp2px(mContext, 100)) / 4;
                                linearLayout.addView(imageView, itemWidth, itemWidth);
                                final int finalI = j;
                                linearLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(mContext, PhotoViewActivity.class);
                                        Bundle bundle = new Bundle();
                                        List_Bean bean = new List_Bean();
                                        bean.setStringList(content_img);
                                        bundle.putSerializable("questionlistdataBean", bean);
                                        intent.putExtras(bundle);
                                        intent.putExtra("currentPosition", finalI);
                                        mContext.startActivity(intent);
                                    }
                                });
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                if (j > 0) {
                                    params.leftMargin = ScreenUtils.dp2px(mContext, 10);
                                }
                                rvitem2ji_layout1.addView(linearLayout, params);
                            }
                        } else {
                            rvitem2ji_layout1.setVisibility(View.GONE);
                        }
                    }
                    if (i == commentBeanList.size() - 1) {
                        rvitem2ji_view1.setVisibility(View.GONE);
                    } else {
                        rvitem2ji_view1.setVisibility(View.VISIBLE);
                    }
                    rvitem1ji_layout4.addView(view);

                    if (commentBean.getCall() != null) {//TODO 添加三级
                        rvitem2ji_layout4.setVisibility(View.VISIBLE);
                        rvitem2ji_layout4.removeAllViews();
                        List<QuestionDetailBean.DataBean.ReplyBean.CallBean> callBeanList = commentBean.getCall();
                        for (int j = 0; j < callBeanList.size(); j++) {
                            final QuestionDetailBean.DataBean.ReplyBean.CallBean callBean = callBeanList.get(j);
                            View view1 = mInflater.inflate(R.layout.questiondetail_rvitem_3ji, null);
                            LinearLayout rvitem3ji_layout0;//总体布局
                            TextView rvitem3ji_textview1;//姓名
                            TextView rvitem3ji_textview2;//日期
                            TextView rvitem3ji_textview3;//回复内容
                            LinearLayout rvitem3ji_layout2;//回复按钮布局
                            TextView rvitem3ji_textview4;//回复按钮
                            LinearLayout rvitem3ji_layout1;//动态加图
                            LinearLayout rvitem3ji_layout4;//添加三级回复的布局
                            View rvitem1ji_view0;//顶部分割线
                            View rvitem3ji_view1;//底部分割线
                            View rvitem3ji_view2;//底部粗分割线

                            rvitem3ji_layout0 = view1.findViewById(R.id.rvitem1ji_layout0);
                            rvitem3ji_textview1 = view1.findViewById(R.id.rvitem1ji_textview1);
                            rvitem3ji_textview2 = view1.findViewById(R.id.rvitem1ji_textview2);
                            rvitem3ji_textview3 = view1.findViewById(R.id.rvitem1ji_textview3);
                            rvitem3ji_layout2 = view1.findViewById(R.id.rvitem1ji_layout2);
                            rvitem3ji_textview4 = view1.findViewById(R.id.rvitem1ji_textview4);
                            rvitem3ji_layout1 = view1.findViewById(R.id.rvitem1ji_layout1);
                            rvitem3ji_layout4 = view1.findViewById(R.id.rvitem1ji_layout4);
                            rvitem3ji_view1 = view1.findViewById(R.id.rvitem1ji_view1);
                            rvitem1ji_view0 = view1.findViewById(R.id.rvitem1ji_view0);
                            rvitem3ji_view2 = view1.findViewById(R.id.rvitem1ji_view2);

                            rvitem3ji_layout0.setBackgroundColor(Color.parseColor("#F0F0F0"));
                            rvitem3ji_textview1.setText(callBean.getNick_name() + "  指定回复  " + callBean.getCall_name());
                            rvitem3ji_textview2.setText(callBean.getCreate_time());
                            if (callBean.getContent_font() == null || callBean.getContent_font().equals("")) {
                                rvitem3ji_textview3.setVisibility(View.GONE);
                            } else {
                                rvitem3ji_textview3.setVisibility(View.VISIBLE);
                                rvitem3ji_textview3.setText(callBean.getContent_font());
                            }
                            rvitem3ji_textview4.setText("回复@Ta");
                            rvitem3ji_layout2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, QuestionReplyActivity.class);
                                    intent.putExtra("qaid", dataBean.getId());
                                    intent.putExtra("leve", "3");
                                    intent.putExtra("pid", callBean.getPid());
                                    intent.putExtra("call_uid", callBean.getUid());
                                    mContext.startActivityForResult(intent, 9876);
                                }
                            });
                            if (callBean.getContent_img() != null) {
                                final List<String> content_img = callBean.getContent_img();
                                if (!content_img.isEmpty()) {
                                    rvitem3ji_layout1.setVisibility(View.VISIBLE);
                                    rvitem3ji_layout1.removeAllViews();
                                    for (int k = 0; k < content_img.size(); k++) {
                                        ImageView imageView = (ImageView) mInflater.inflate(R.layout.info2_recyclerview_imageview, null);
                                        Glide.with(MyApplication.context).load(content_img.get(k)).into(imageView);
                                        LinearLayout linearLayout = new LinearLayout(mContext);
                                        int itemWidth = (mScreenWidth - ScreenUtils.dp2px(mContext, 125)) / 4;
                                        linearLayout.addView(imageView, itemWidth, itemWidth);
                                        final int finalI = k;
                                        linearLayout.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(mContext, PhotoViewActivity.class);
                                                Bundle bundle = new Bundle();
                                                List_Bean bean = new List_Bean();
                                                bean.setStringList(content_img);
                                                bundle.putSerializable("questionlistdataBean", bean);
                                                intent.putExtras(bundle);
                                                intent.putExtra("currentPosition", finalI);
                                                mContext.startActivity(intent);
                                            }
                                        });
                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        if (k > 0) {
                                            params.leftMargin = ScreenUtils.dp2px(mContext, 10);
                                        }
                                        rvitem3ji_layout1.addView(linearLayout, params);
                                    }
                                } else {
                                    rvitem3ji_layout1.setVisibility(View.GONE);
                                }
                            }
                            if (j == 0) {
                                rvitem1ji_view0.setVisibility(View.VISIBLE);
                            } else {
                                rvitem1ji_view0.setVisibility(View.GONE);
                            }
                            if (j == callBeanList.size() - 1) {
                                rvitem3ji_view2.setVisibility(View.GONE);
                                rvitem3ji_view1.setVisibility(View.GONE);
                            } else {
                                rvitem3ji_view2.setVisibility(View.VISIBLE);
                                rvitem3ji_view1.setVisibility(View.VISIBLE);
                            }
                            rvitem2ji_layout4.addView(view1);
                        }
                    } else {
                        rvitem2ji_layout4.setVisibility(View.GONE);
                    }
                }
            } else {
                rvitem1ji_layout4.setVisibility(View.GONE);
            }

        }

    }

}
