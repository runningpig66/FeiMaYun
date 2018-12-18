package cn.aura.feimayun.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.ExamDetailActivity;
import cn.aura.feimayun.activity.PlayDetailActivity;
import cn.aura.feimayun.bean.PlayDetailBean;
import cn.aura.feimayun.util.OnClickListener;

/**
 * 描述：PlayDeatilRightFragment中ListView的适配器
 */
//一级listview适配器
public class PlayDetailRightFragment_ListView_Adapter2 extends BaseAdapter {
    private static final int ITEM_TYPE1 = 0;
    private static final int ITEM_TYPE2 = 1;
    private static final int ITEM_TYPE3 = 2;
    private static final int ITEM_COUNT = 3;

    private PlayDetailActivity activity;
    private List<PlayDetailBean> detailBeans;
    private String sid;
    private String uid;
    private String isBuy;

    public PlayDetailRightFragment_ListView_Adapter2(Context context, List<PlayDetailBean> detailBeans, String sid, String uid, String isBuy) {
        this.activity = (PlayDetailActivity) context;
        this.detailBeans = detailBeans;
        printList(detailBeans);
        this.sid = sid;
        this.uid = uid;
        this.isBuy = isBuy;
    }

    public void setIsBuy(String isBuy) {
        this.isBuy = isBuy;
    }

    @Override
    public int getCount() {
        if (detailBeans == null) {
            return 0;
        } else {
            return detailBeans.size();
        }
    }

    @Override
    public PlayDetailBean getItem(int position) {
        if (detailBeans == null) {
            return null;
        } else {
            return detailBeans.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final PlayDetailBean playDetailBean = getItem(position);
        View view = null;
        ViewHolder1 viewHolder1 = null;
        ViewHolder2 viewHolder2 = null;
        ViewHolder3 viewHolder3 = null;
        int item_type = getItemViewType(position);

        if (convertView == null) {
            if (item_type == ITEM_TYPE1) {
                view = LayoutInflater.from(activity).inflate(R.layout.fragment_playdeatil_right_listview1_item1, parent, false);
                viewHolder1 = new ViewHolder1();
                viewHolder1.fragment_playdeatil_right_listview1_item1_imageview1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item1_imageview1);//一级播放图标
                viewHolder1.fragment_playdeatil_right_listview1_item1_textView1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item1_textView1);//一级标题
                viewHolder1.fragment_playdeatil_right_listview1_item1_imageview2 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item1_imageview2);//一级小三角
                viewHolder1.fragment_playdeatil_right_listview1_item1_layout1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item1_layout1);//一级布局
                view.setTag(R.id.fragment_playdeatil_right_listview1_item1_layout1, viewHolder1);
            } else if (item_type == ITEM_TYPE2) {
                view = LayoutInflater.from(activity).inflate(R.layout.fragment_playdeatil_right_listview1_item2, parent, false);
                viewHolder2 = new ViewHolder2();
                viewHolder2.fragment_playdeatil_right_listview1_item2_imageview1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item2_imageview1);
                viewHolder2.fragment_playdeatil_right_listview1_item2_imageview2 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item2_imageview2);
                viewHolder2.fragment_playdeatil_right_listview1_item2_textview1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item2_textview1);
                viewHolder2.fragment_playdeatil_right_listview1_item2_layout1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item2_layout1);
                view.setTag(R.id.fragment_playdeatil_right_listview1_item2_layout1, viewHolder2);
            } else if (item_type == ITEM_TYPE3) {
                view = LayoutInflater.from(activity).inflate(R.layout.fragment_playdeatil_right_listview1_item3, parent, false);
                viewHolder3 = new ViewHolder3();
                viewHolder3.fragment_playdeatil_right_listview1_item3_imageview1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item3_imageview1);
                viewHolder3.fragment_playdeatil_right_listview1_item3_textview1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item3_textview1);
                viewHolder3.fragment_playdeatil_right_listview1_item3_layout1 = view.findViewById(R.id.fragment_playdeatil_right_listview1_item3_layout1);
                view.setTag(R.id.fragment_playdeatil_right_listview1_item3_layout1, viewHolder3);
            }
        } else {
            view = convertView;
            if (item_type == ITEM_TYPE1) {
                viewHolder1 = (ViewHolder1) view.getTag(R.id.fragment_playdeatil_right_listview1_item1_layout1);
            } else if (item_type == ITEM_TYPE2) {
                viewHolder2 = (ViewHolder2) view.getTag(R.id.fragment_playdeatil_right_listview1_item2_layout1);
            } else if (item_type == ITEM_TYPE3) {
                viewHolder3 = (ViewHolder3) view.getTag(R.id.fragment_playdeatil_right_listview1_item3_layout1);
            }
        }

        if (item_type == ITEM_TYPE1) {
            //章级别的内容布局，有可能是章，有可能是顶级课件
            viewHolder1.fragment_playdeatil_right_listview1_item1_textView1.setText(playDetailBean.getName());
            viewHolder1.fragment_playdeatil_right_listview1_item1_layout1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.fragment_playdeatil_right_listview1_item1_layout1:
                            if (playDetailBean.getFtype().equals("video")) {//顶级课件视频
                                if (isDoubleClick()) {
//                                    Toast.makeText(activity, "isDoubleClick", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (uid.equals("")) {
                                    Toast.makeText(activity, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
                                } else {
                                    if (sid == null && isBuy.equals("0")) {//未登录的、未购买的都拿不到sid
                                        Toast.makeText(activity, "您还未购买课程~", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String sid = playDetailBean.getSort();
                                        activity.setSid(sid);
                                    }
                                }
                            } else if (playDetailBean.getFtype().equals("test")) {//顶级课件题目
                                String sid2 = playDetailBean.getStore_id();
                                String tid = playDetailBean.getTid();

                                Intent intent = new Intent(activity, ExamDetailActivity.class);
                                intent.putExtra("sid", sid2);
                                intent.putExtra("tid", tid);
                                activity.startActivity(intent);
                            } else if (playDetailBean.getType().equals("zhang")) {
                                List<PlayDetailBean> children2List = playDetailBean.getChildrenList();//拿到章下的小节列表，拿到一级下的二级list
                                if (children2List != null) {//181016 屏蔽章下没有条目的情况
                                    if (playDetailBean.isOpen()) {//如果当前一级是打开状态，则关闭二级和三级
                                        //先关闭三级，再关闭二级
                                        for (int i = 0; i < children2List.size(); i++) {//遍历小节，遍历二级
                                            //判断二级item是否展开，展开则关闭
                                            List<PlayDetailBean> children3List = children2List.get(i).getChildrenList();//拿到小节下的课程列表，拿到二级下的三级list
                                            if (children2List.get(i).isOpen()) {//如果当前二级item是打开状态，则关闭三级
                                                for (int j = 0; j < children3List.size(); j++) {//遍历3级
                                                    detailBeans.remove(position + 1 + 1);//关闭三级，+1选中小节，再次+1选中课程
                                                }
                                                children2List.get(i).setOpen(false);//删除完三级，将二级设置为关闭状态
                                            }
                                            detailBeans.remove(position + 1);//关闭二级
                                        }
                                        playDetailBean.setOpen(false);//删完二级，将一级设置为关闭状态
                                        printList(detailBeans);
                                    } else {//如果当前一级是关闭状态，则打开二级
                                        for (int i = 0; i < children2List.size(); i++) {
                                            detailBeans.add(position + i + 1, children2List.get(i));
                                        }
                                        playDetailBean.setOpen(true);
//                                        printList(detailBeans);
                                    }
                                    notifyDataSetChanged();

                                    ListView listView = (ListView) parent;
                                    listView.setSelection(position - 3);

                                }
                            }
                            break;
                    }
                }
            });

            //判断对应position章下是否有小节，没有小节的话就隐藏小三角图形
            if (playDetailBean.getType().equals("zhang")) {//只有章才需要设置小三角和背景，课件不需要
                //章隐藏播放图标，顶级课件不需要隐藏
                viewHolder1.fragment_playdeatil_right_listview1_item1_imageview1.setVisibility(View.GONE);
                //首先为章布局设置灰色背景
                viewHolder1.fragment_playdeatil_right_listview1_item1_layout1.setBackground(activity.getResources().getDrawable(R.drawable.linearlayout_corner));
                //判断章下面是否有小节，以便显示或隐藏三角形
                List<PlayDetailBean> children = playDetailBean.getChildrenList();
                if (children == null) {//一级下没有二级
                    viewHolder1.fragment_playdeatil_right_listview1_item1_imageview2.setVisibility(View.GONE);//隐藏小三角
                } else {//一级下有二级
                    viewHolder1.fragment_playdeatil_right_listview1_item1_imageview2.setVisibility(View.VISIBLE);//显示小三角
                    if (playDetailBean.isOpen()) {//然后判断一级是否展开状态，显示上下两种三角
                        viewHolder1.fragment_playdeatil_right_listview1_item1_imageview2.setImageResource(R.drawable.previous3x);
                    } else {
                        viewHolder1.fragment_playdeatil_right_listview1_item1_imageview2.setImageResource(R.drawable.next);
                    }
                }
                //文字设置灰色
                viewHolder1.fragment_playdeatil_right_listview1_item1_textView1.setTextColor(Color.parseColor("#999999"));
            } else if (playDetailBean.getType().equals("kejian")) {
                viewHolder1.fragment_playdeatil_right_listview1_item1_imageview1.setVisibility(View.VISIBLE);//显示课件图标
                viewHolder1.fragment_playdeatil_right_listview1_item1_layout1.setBackgroundColor(Color.parseColor("#ffffff"));
                viewHolder1.fragment_playdeatil_right_listview1_item1_imageview2.setVisibility(View.GONE);//隐藏小三角

                if (playDetailBean.getSort().equals(sid)) {//如果是上次播放的视频,子项sort和play的data下的sid比较
                    switch (playDetailBean.getFtype()) {//视频图标设置为橘色
                        case "video":

                            viewHolder1.fragment_playdeatil_right_listview1_item1_imageview1.setImageResource(R.drawable.course_arrow_o);
                            break;
                        case "test":
                            viewHolder1.fragment_playdeatil_right_listview1_item1_imageview1.setImageResource(R.drawable.pen_gray);
                            break;
                    }
                    //文字设置橘色
                    viewHolder1.fragment_playdeatil_right_listview1_item1_textView1.setTextColor(Color.parseColor("#ee7708"));
                } else {
                    switch (playDetailBean.getFtype()) {
                        case "video":
                            viewHolder1.fragment_playdeatil_right_listview1_item1_imageview1.setImageResource(R.drawable.course_arrow_gray);
                            if (playDetailBean.getLearn().equals("0")) {//没学过的是黑色字体
                                viewHolder1.fragment_playdeatil_right_listview1_item1_textView1.setTextColor(Color.parseColor("#000000"));
                            } else {//学过的是灰色字体
                                viewHolder1.fragment_playdeatil_right_listview1_item1_textView1.setTextColor(Color.parseColor("#999999"));
                            }
                            break;
                        case "test":
                            viewHolder1.fragment_playdeatil_right_listview1_item1_imageview1.setImageResource(R.drawable.pen_gray);
                            //试题的颜色是黑色，不设置已学
                            viewHolder1.fragment_playdeatil_right_listview1_item1_textView1.setTextColor(Color.parseColor("#000000"));
                            break;
                    }
                }
            }
        } else if (item_type == ITEM_TYPE2) {//二级
            viewHolder2.fragment_playdeatil_right_listview1_item2_textview1.setText(playDetailBean.getName());//设置二级标题
            if (playDetailBean.getIsAlive()) {
                viewHolder2.fragment_playdeatil_right_listview1_item2_imageview1.setImageResource(R.drawable.course_icon_orange);
                viewHolder2.fragment_playdeatil_right_listview1_item2_textview1.setTextColor(Color.parseColor("#ee7708"));
            } else {
                viewHolder2.fragment_playdeatil_right_listview1_item2_imageview1.setImageResource(R.drawable.course_icon_gray3x);
                viewHolder2.fragment_playdeatil_right_listview1_item2_textview1.setTextColor(Color.parseColor("#666666"));
            }
            viewHolder2.fragment_playdeatil_right_listview1_item2_layout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.fragment_playdeatil_right_listview1_item2_layout1:
                            if (playDetailBean.getType().equals("xjie")) {
                                List<PlayDetailBean> children3List = playDetailBean.getChildrenList();
                                if (children3List != null) {//小节下有课件，二级下有三级
                                    if (playDetailBean.isOpen()) {//如果当前二级是打开状态，则关闭三级
                                        for (int i = 0; i < children3List.size(); i++) {
                                            detailBeans.remove(position + 1);
                                        }
                                        playDetailBean.setOpen(false);
                                    } else {//如果当前二级是关闭状态，则打开三级
                                        for (int i = 0; i < children3List.size(); i++) {
                                            detailBeans.add(position + i + 1, children3List.get(i));
                                        }
                                        playDetailBean.setOpen(true);
                                    }
                                }
                                notifyDataSetChanged();

                                ListView listView = (ListView) parent;
                                listView.setSelection(position - 3);
                            }
                            break;
                    }
                }
            });

            //判断对应position小节下是否有课程/试题，没有小节的话就隐藏小三角图形
            List<PlayDetailBean> children = playDetailBean.getChildrenList();
            if (children == null) {//没有三级
                viewHolder2.fragment_playdeatil_right_listview1_item2_imageview2.setVisibility(View.GONE);//隐藏小三角
            } else {//二级下有三级
                viewHolder2.fragment_playdeatil_right_listview1_item2_imageview2.setVisibility(View.VISIBLE);//显示小三角
                if (playDetailBean.isOpen()) {//然后判断二级是否展开状态，显示上下两种三角
                    viewHolder2.fragment_playdeatil_right_listview1_item2_imageview2.setImageResource(R.drawable.previous3x);
                } else {
                    viewHolder2.fragment_playdeatil_right_listview1_item2_imageview2.setImageResource(R.drawable.next);
                }
            }
        } else if (item_type == ITEM_TYPE3) {
            viewHolder3.fragment_playdeatil_right_listview1_item3_textview1.setText(playDetailBean.getName());//设置三级标题
            //根据视频/试题设置不同的图片
            if (playDetailBean.getSort().equals(sid)) {//如果是上次播放的视频,子项sort和play的data下的sid比较
                switch (playDetailBean.getFtype()) {
                    case "video":
                        viewHolder3.fragment_playdeatil_right_listview1_item3_imageview1.setImageResource(R.drawable.course_arrow_o);
                        break;
                    case "test":
                        viewHolder3.fragment_playdeatil_right_listview1_item3_imageview1.setImageResource(R.drawable.pen_gray);
                        break;
                }
                viewHolder3.fragment_playdeatil_right_listview1_item3_textview1.setTextColor(Color.parseColor("#ee7708"));
            } else {
                switch (playDetailBean.getFtype()) {
                    case "video":
                        viewHolder3.fragment_playdeatil_right_listview1_item3_imageview1.setImageResource(R.drawable.course_arrow_gray);
                        if (playDetailBean.getLearn().equals("0")) {//没学过的是黑色字体
                            viewHolder3.fragment_playdeatil_right_listview1_item3_textview1.setTextColor(Color.parseColor("#000000"));
                        } else {//学过的是灰色字体
                            viewHolder3.fragment_playdeatil_right_listview1_item3_textview1.setTextColor(Color.parseColor("#999999"));
                        }
                        break;
                    case "test":
                        viewHolder3.fragment_playdeatil_right_listview1_item3_imageview1.setImageResource(R.drawable.pen_gray);
                        viewHolder3.fragment_playdeatil_right_listview1_item3_textview1.setTextColor(Color.parseColor("#000000"));
                        break;
                }
            }
            viewHolder3.fragment_playdeatil_right_listview1_item3_layout1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.fragment_playdeatil_right_listview1_item3_layout1://视频或者题目的点击事件
                            switch (playDetailBean.getFtype()) {
                                case "video"://播放视频
                                    if (isDoubleClick()) {
//                                        Toast.makeText(activity, "isDoubleClick", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (uid.equals("")) {
                                        Toast.makeText(activity, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (sid == null && isBuy.equals("0")) {//未登录的、未购买的都拿不到sid
                                            Toast.makeText(activity, "您还未购买课程~", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String sid = playDetailBean.getSort();
                                            activity.setSid(sid);
                                        }
                                    }
                                    break;
                                case "test":
                                    if (uid.equals("")) {
                                        Toast.makeText(activity, R.string.vhall_login_first, Toast.LENGTH_SHORT).show();
                                    } else {
                                        String sid2 = playDetailBean.getStore_id();
                                        String tid = playDetailBean.getTid();
                                        Intent intent = new Intent(activity, ExamDetailActivity.class);
                                        intent.putExtra("sid", sid2);
                                        intent.putExtra("tid", tid);
                                        activity.startActivity(intent);
                                    }
                                    break;
                            }
                            break;
                    }
                }
            });
        }
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        String level = detailBeans.get(position).getLevel();
        switch (level) {
            case "1":
                return ITEM_TYPE1;
            case "2":
                return ITEM_TYPE2;
            case "3":
                return ITEM_TYPE3;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_COUNT;
    }

    //打印数据源
    private void printList(List<PlayDetailBean> detailBeans) {
        for (int i = 0; i < detailBeans.size(); i++) {
            String name = detailBeans.get(i).getName();
            System.out.print(name + "， ");
        }
        System.out.println();
    }

    class ViewHolder1 {//一级
        ImageView fragment_playdeatil_right_listview1_item1_imageview1;//一级播放图标
        TextView fragment_playdeatil_right_listview1_item1_textView1;//一级标题
        ImageView fragment_playdeatil_right_listview1_item1_imageview2;//一级小三角
        LinearLayout fragment_playdeatil_right_listview1_item1_layout1;//一级布局
    }

    class ViewHolder2 {//二级
        ImageView fragment_playdeatil_right_listview1_item2_imageview1;//二级图标
        TextView fragment_playdeatil_right_listview1_item2_textview1;//二级标题
        ImageView fragment_playdeatil_right_listview1_item2_imageview2;//二级小三角
        LinearLayout fragment_playdeatil_right_listview1_item2_layout1;//二级布局
    }

    class ViewHolder3 {//三级
        ImageView fragment_playdeatil_right_listview1_item3_imageview1;//三级播放图标
        TextView fragment_playdeatil_right_listview1_item3_textview1;//三级标题
        LinearLayout fragment_playdeatil_right_listview1_item3_layout1;//三级布局
    }

}
