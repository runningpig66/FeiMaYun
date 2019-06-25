package cn.aura.feimayun.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

import java.io.File;
import java.util.List;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.ExamDetailActivity;
import cn.aura.feimayun.activity.ExamResultActivity;
import cn.aura.feimayun.activity.MyStudiesItemActivity;
import cn.aura.feimayun.bean.MyStuidesInfo4Bean;
import cn.aura.feimayun.util.Util;

import static android.content.Context.MODE_PRIVATE;

public class MyStudiesInfo4_RecyclerView_Adapter extends RecyclerView.Adapter<MyStudiesInfo4_RecyclerView_Adapter.ViewHolder> {
    private MyStudiesItemActivity context;
    //    private List<Map<String, String>> mapList;
    private List<MyStuidesInfo4Bean.DataBean> dataBeanList;

    public MyStudiesInfo4_RecyclerView_Adapter(Context context, List<MyStuidesInfo4Bean.DataBean> dataBeanList) {
        this.context = (MyStudiesItemActivity) context;
        this.dataBeanList = dataBeanList;
//        this.mapList = mapList;
    }

    public void setData(List<MyStuidesInfo4Bean.DataBean> dataBeanList) {
        this.dataBeanList = dataBeanList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mystudiesinfo4_recyclerview_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.info4_recycler_textview3.setOnClickListener(new View.OnClickListener() {//再次挑战/开始答题/继续答题等
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                MyStuidesInfo4Bean.DataBean dataBean = dataBeanList.get(position);
                String uid = Util.getUid();
                final String sid = dataBean.getStore_id();//题库id
                final String tid = dataBean.getTpaper_id();//试卷id
                String fileName = "paper" + sid + tid + uid;
                File file = new File("/data/data/" + context.getPackageName() + "/shared_prefs", fileName + ".xml");
                boolean fileExists = file.exists();
                SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, MODE_PRIVATE);
                int write = sharedPreferences.getInt("write", 0);//默认为0，说明用户清空了这张表中的内容，这时应该走服务器端的字段
                //文件完整性检查，后期可以更加完善
                boolean fileOk = write > 0;
                if (fileExists && fileOk) {
                    //异常文件存在，准备dialog的文字信息
                    View view = LayoutInflater.from(context).inflate(R.layout.dialog_call, null);
                    TextView dialog_call_textview1 = view.findViewById(R.id.dialog_call_textview1);//大标题
                    TextView dialog_call_textview2 = view.findViewById(R.id.dialog_call_textview2);//小标题
                    //根据异常文件中的error判断出错的类型，设置提示dialog的文字
                    dialog_call_textview1.setText("温馨提示");
                    dialog_call_textview2.setText("本地有该试卷答题记录，\n是否继续答题？");
                    new TDialog.Builder(context.getSupportFragmentManager())
                            .setDialogView(view)
//                            .setLayoutRes(R.layout.dialog_call)
                            .setScreenWidthAspect(context, 0.7f)
                            .addOnClickListener(R.id.dialog_call_confirm, R.id.dialog_call_cancel)
                            .setOnViewClickListener(new OnViewClickListener() {
                                @Override
                                public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                    switch (view.getId()) {
                                        case R.id.dialog_call_cancel:
                                            //执行服务器返回的流程
                                            context.myStuidesInfo4.setClickPosition(position);
                                            Intent intentExamDetailActivity = new Intent(context, ExamDetailActivity.class);
                                            intentExamDetailActivity.putExtra("sid", sid);
                                            intentExamDetailActivity.putExtra("tid", tid);
                                            intentExamDetailActivity.putExtra("loadLocalFile", false);
                                            context.startActivityForResult(intentExamDetailActivity, 0X1231);
                                            tDialog.dismiss();
                                            break;
                                        case R.id.dialog_call_confirm:
                                            //执行本地答题记录
                                            context.myStuidesInfo4.setClickPosition(position);
                                            Intent intentExamDetailActivity2 = new Intent(context, ExamDetailActivity.class);
                                            intentExamDetailActivity2.putExtra("sid", sid);
                                            intentExamDetailActivity2.putExtra("tid", tid);
                                            intentExamDetailActivity2.putExtra("loadLocalFile", true);
                                            context.startActivityForResult(intentExamDetailActivity2, 0X1231);
                                            tDialog.dismiss();
                                            break;
                                    }
                                }
                            })
                            .create()
                            .show();
                } else {
                    //TODO 未购买的不进答题页面
                    context.myStuidesInfo4.setClickPosition(position);
//                Map<String, String> map = mapList.get(position);
                    Intent intentExamDetailActivity = new Intent(context, ExamDetailActivity.class);
                    intentExamDetailActivity.putExtra("sid", sid);
                    intentExamDetailActivity.putExtra("tid", tid);
                    context.startActivityForResult(intentExamDetailActivity, 0X1231);
                }

            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Map<String, String> map = mapList.get(position);
        MyStuidesInfo4Bean.DataBean dataBean = dataBeanList.get(position);
        if (position == 0) {
            holder.info4_recycler_view0.setVisibility(View.VISIBLE);
        } else {
            holder.info4_recycler_view0.setVisibility(View.GONE);
        }
        holder.info4_recycler_textview1.setText(dataBean.getTname());

        String uid = Util.getUid();
        String sid = dataBean.getStore_id();//题库id
        String tid = dataBean.getTpaper_id();//试卷id
        String fileName = "paper" + sid + tid + uid;
        File file = new File("/data/data/" + context.getPackageName() + "/shared_prefs", fileName + ".xml");
        boolean fileExists = file.exists();
        if (fileExists) {//如果文件存在，用本地的记录继续答题，再次之前需要判断下文件是否完整fileOk
            SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, MODE_PRIVATE);
            int write = sharedPreferences.getInt("write", 0);//默认为0，说明用户清空了这张表中的内容，这时应该走服务器端的字段
            //文件完整性检查，后期可以更加完善
            boolean fileOk = write > 0;
            if (fileOk) {
                holder.info4_recycler_textview3.setText("继续答题");
                holder.info4_recycler_view1.setVisibility(View.GONE);
                holder.info4_recycler_layout1.setVisibility(View.GONE);
            } else {
//        holder.info4_recycler_textview2.setText("共" + map.get("total") + "道题/已做" + map.get("write") + "道题");
                holder.info4_recycler_textview3.setText(dataBean.getType_name());

                if (dataBean.getTest() == null || dataBean.getTest().isEmpty()) {
                    holder.info4_recycler_view1.setVisibility(View.GONE);
                    holder.info4_recycler_layout1.setVisibility(View.GONE);
                } else {
                    holder.info4_recycler_view1.setVisibility(View.VISIBLE);
                    holder.info4_recycler_layout1.setVisibility(View.VISIBLE);

                    List<MyStuidesInfo4Bean.DataBean.TestBean> testBeanList = dataBean.getTest();
                    holder.info4_recycler_layout1.removeAllViews();
                    for (int i = 0; i < testBeanList.size(); i++) {
                        MyStuidesInfo4Bean.DataBean.TestBean testBean = testBeanList.get(i);
                        View view = LayoutInflater.from(context).inflate(R.layout.mystudiesinfo4_textviewsimple, null);
                        TextView textviewsimple_textview1 = view.findViewById(R.id.textviewsimple_textview1);
                        TextView textviewsimple_textview2 = view.findViewById(R.id.textviewsimple_textview2);
                        TextView textviewsimple_textview3 = view.findViewById(R.id.textviewsimple_textview3);
                        textviewsimple_textview1.setText(testBean.getSub_time());
                        textviewsimple_textview2.setText(testBean.getScore() + "分");

                        final String id = testBean.getId();
                        textviewsimple_textview3.setText("查看结果");
                        textviewsimple_textview3.setOnClickListener(new View.OnClickListener() {//查看结果
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ExamResultActivity.class);
                                intent.putExtra("id", id);
                                context.startActivity(intent);
                            }
                        });
                        holder.info4_recycler_layout1.addView(view);
                    }

                }
            }
        } else {//文件不存在，走服务器的状态
//        holder.info4_recycler_textview2.setText("共" + map.get("total") + "道题/已做" + map.get("write") + "道题");
            holder.info4_recycler_textview3.setText(dataBean.getType_name());

            if (dataBean.getTest() == null || dataBean.getTest().isEmpty()) {
                holder.info4_recycler_view1.setVisibility(View.GONE);
                holder.info4_recycler_layout1.setVisibility(View.GONE);
            } else {
                holder.info4_recycler_view1.setVisibility(View.VISIBLE);
                holder.info4_recycler_layout1.setVisibility(View.VISIBLE);

                List<MyStuidesInfo4Bean.DataBean.TestBean> testBeanList = dataBean.getTest();
                holder.info4_recycler_layout1.removeAllViews();
                for (int i = 0; i < testBeanList.size(); i++) {
                    MyStuidesInfo4Bean.DataBean.TestBean testBean = testBeanList.get(i);
                    View view = LayoutInflater.from(context).inflate(R.layout.mystudiesinfo4_textviewsimple, null);
                    TextView textviewsimple_textview1 = view.findViewById(R.id.textviewsimple_textview1);
                    TextView textviewsimple_textview2 = view.findViewById(R.id.textviewsimple_textview2);
                    TextView textviewsimple_textview3 = view.findViewById(R.id.textviewsimple_textview3);
                    textviewsimple_textview1.setText(testBean.getSub_time());
                    textviewsimple_textview2.setText(testBean.getScore() + "分");

                    final String id = testBean.getId();
                    textviewsimple_textview3.setText("查看结果");
                    textviewsimple_textview3.setOnClickListener(new View.OnClickListener() {//查看结果
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ExamResultActivity.class);
                            intent.putExtra("id", id);
                            context.startActivity(intent);
                        }
                    });
                    holder.info4_recycler_layout1.addView(view);
                }

            }

        }

    }

    @Override
    public int getItemCount() {
        return dataBeanList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView info4_recycler_textview1;
        //        TextView info4_recycler_textview2;
        TextView info4_recycler_textview3;
        View info4_recycler_view0;
        View info4_recycler_view1;
        LinearLayout info4_recycler_layout1;
        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            info4_recycler_textview1 = itemView.findViewById(R.id.info4_recycler_textview1);
//            info4_recycler_textview2 = itemView.findViewById(R.id.info4_recycler_textview2);
            info4_recycler_textview3 = itemView.findViewById(R.id.info4_recycler_textview3);
            info4_recycler_view0 = itemView.findViewById(R.id.info4_recycler_view0);
            info4_recycler_view1 = itemView.findViewById(R.id.info4_recycler_view1);
            info4_recycler_layout1 = itemView.findViewById(R.id.info4_recycler_layout1);
        }
    }
}
