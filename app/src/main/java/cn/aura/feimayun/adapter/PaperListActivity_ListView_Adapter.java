package cn.aura.feimayun.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.ExamDetailActivity;
import cn.aura.feimayun.activity.ExamResultActivity;
import cn.aura.feimayun.activity.PaperListActivity;
import cn.aura.feimayun.util.Util;

import static android.content.Context.MODE_PRIVATE;

public class PaperListActivity_ListView_Adapter extends BaseAdapter implements View.OnClickListener {
    private PaperListActivity activity;
    private List<Map<String, String>> listList;
    private String sid;

    public PaperListActivity_ListView_Adapter(Activity activity, List<Map<String, String>> listList, String sid) {
        this.activity = (PaperListActivity) activity;
        this.listList = listList;
        this.sid = sid;
    }

    public void setData(List<Map<String, String>> listList) {
        this.listList = listList;
    }

    @Override
    public int getCount() {
        return listList.size();
    }

    @Override
    public Object getItem(int position) {
        return listList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            convertView = inflater.inflate(R.layout.activity_paper_list_listview1_item, parent, false);
        }
        TextView activity_paper_list_listview1_item_textview1 =
                convertView.findViewById(R.id.activity_paper_list_listview1_item_textview1);
        TextView activity_paper_list_listview1_item_textview2 =
                convertView.findViewById(R.id.activity_paper_list_listview1_item_textview2);
        TextView activity_paper_list_listview1_item_textview3 =
                convertView.findViewById(R.id.activity_paper_list_listview1_item_textview3);
        TextView activity_paper_list_listview1_item_textview4 =
                convertView.findViewById(R.id.activity_paper_list_listview1_item_textview4);

        activity_paper_list_listview1_item_textview1.setText(listList.get(position).get("name"));
        activity_paper_list_listview1_item_textview3.setTag(position);
        activity_paper_list_listview1_item_textview4.setTag(position);

        //显示试卷列表时，对每一个试卷都要判断一下本地是否有存储记录的文件，如果有，说明该试卷上次答题时是非正常退出的
        //如果有本地文件，那么就改为继续答题的状态
        //如果没有本地文件，走服务器的字段进行显示

        String tp_type = listList.get(position).get("tp_type");
        if (tp_type.equals("1")) {
            activity_paper_list_listview1_item_textview3.setOnClickListener(this);
            activity_paper_list_listview1_item_textview4.setOnClickListener(this);
            String uid = Util.getUid();
            String fileName = "paper" + sid + listList.get(position).get("id") + uid;//由题库id+试卷id共同构建的唯一文件名
            File file = new File("/data/data/" + activity.getPackageName() + "/shared_prefs", fileName + ".xml");

            boolean fileExists = file.exists();

            if (fileExists) {//如果文件存在，用本地的记录继续答题，再次之前需要判断下文件是否完整fileOk
                SharedPreferences sharedPreferences = activity.getSharedPreferences(fileName, MODE_PRIVATE);
                int write = sharedPreferences.getInt("write", 0);//默认为0，说明用户清空了这张表中的内容，这时应该走服务器端的字段
                //文件完整性检查，后期可以更加完善
                boolean fileOk = write > 0;
                if (fileOk) {
                    activity_paper_list_listview1_item_textview2.setText("总题数:" + listList.get(position).get("total") + "  已做题数:" + write);
                    activity_paper_list_listview1_item_textview3.setText("继续答题");
                    activity_paper_list_listview1_item_textview4.setVisibility(View.GONE);//隐藏解析
                } else {//虽然文件存在，但文件内容被修改破坏，走服务器端的字段
                    activity_paper_list_listview1_item_textview2.setText("总题数:" + listList.get(position).get("total") + "  已做题数:" + listList.get(position).get("write"));
                    activity_paper_list_listview1_item_textview3.setText(listList.get(position).get("type_name"));

                    String type = listList.get(position).get("type");
                    if (type != null) {
                        switch (type) {
                            case "0"://继续答题
                                activity_paper_list_listview1_item_textview4.setVisibility(View.GONE);//隐藏解析
                                break;
                            case "1"://开始答题
                                activity_paper_list_listview1_item_textview4.setVisibility(View.GONE);
                                break;
                            case "2"://再次挑战
                                activity_paper_list_listview1_item_textview4.setVisibility(View.VISIBLE);
                                break;
                            default:
                                break;
                        }
                    }
                }
            } else {//文件不存在，走服务器的状态
                activity_paper_list_listview1_item_textview2.setText("总题数:" + listList.get(position).get("total") + "  已做题数:" + listList.get(position).get("write"));
                activity_paper_list_listview1_item_textview3.setText(listList.get(position).get("type_name"));
                String type = listList.get(position).get("type");
                if (type != null) {
                    switch (type) {
                        case "0"://继续答题
                            activity_paper_list_listview1_item_textview4.setVisibility(View.GONE);//隐藏解析
                            break;
                        case "1"://开始答题
                            activity_paper_list_listview1_item_textview4.setVisibility(View.GONE);
                            break;
                        case "2"://再次挑战
                            activity_paper_list_listview1_item_textview4.setVisibility(View.VISIBLE);
                            break;
                        default:
                            break;
                    }
                }
            }
        } else {
            activity_paper_list_listview1_item_textview2.setText("共1题");
            activity_paper_list_listview1_item_textview4.setVisibility(View.GONE);
            activity_paper_list_listview1_item_textview3.setText(listList.get(position).get("type_name"));
            activity_paper_list_listview1_item_textview3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity, "请到PC端答题~", Toast.LENGTH_SHORT).show();
                }
            });

        }
        return convertView;
    }

    //    String fileName = "paper" + sid + listList.get(position).get("id");
//    SharedPreferences.Editor editor = activity.getSharedPreferences(fileName, MODE_PRIVATE).edit();
//        editor.putString("avater", userInfoObject.getString("avater"));
//        editor.apply();
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_paper_list_listview1_item_textview3://开始答题/继续挑战/继续答题
                final int position = (int) v.getTag();
                String uid = Util.getUid();
                String fileName = "paper" + sid + listList.get(position).get("id") + uid;//由题库id+试卷id共同构建的唯一文件名
                File file = new File("/data/data/" + activity.getPackageName() + "/shared_prefs", fileName + ".xml");
                boolean fileExists = file.exists();
                SharedPreferences sharedPreferences = activity.getSharedPreferences(fileName, MODE_PRIVATE);
                int write = sharedPreferences.getInt("write", 0);//默认为0，说明用户清空了这张表中的内容，这时应该走服务器端的字段
                //文件完整性检查，后期可以更加完善
                boolean fileOk = write > 0;
                if (fileExists && fileOk) {//如果异常文件存在，并且文件完整
                    //异常文件存在，准备dialog的文字信息
                    View view = LayoutInflater.from(activity).inflate(R.layout.dialog_call, null);
                    TextView dialog_call_textview1 = view.findViewById(R.id.dialog_call_textview1);//大标题
                    TextView dialog_call_textview2 = view.findViewById(R.id.dialog_call_textview2);//小标题
                    //根据异常文件中的error判断出错的类型，设置提示dialog的文字
                    dialog_call_textview1.setText("温馨提示");
                    dialog_call_textview2.setText("本地有该试卷答题记录，\n是否继续答题？");
                    new TDialog.Builder(activity.getSupportFragmentManager())
                            .setDialogView(view)
//                            .setLayoutRes(R.layout.dialog_call)
                            .setScreenWidthAspect(activity, 0.7f)
                            .addOnClickListener(R.id.dialog_call_confirm, R.id.dialog_call_cancel)
                            .setOnViewClickListener(new OnViewClickListener() {
                                @Override
                                public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                                    switch (view.getId()) {
                                        case R.id.dialog_call_cancel:
                                            //执行服务器返回的流程
                                            Intent intentExamDetailActivity = new Intent(activity, ExamDetailActivity.class);
                                            intentExamDetailActivity.putExtra("sid", sid);
                                            activity.setClickPosition(position);
                                            intentExamDetailActivity.putExtra("tid", listList.get(position).get("id"));
                                            intentExamDetailActivity.putExtra("loadLocalFile", false);
                                            activity.startActivityForResult(intentExamDetailActivity, 0x7777);
                                            tDialog.dismiss();
                                            break;
                                        case R.id.dialog_call_confirm:
                                            //执行本地答题记录
                                            Intent intentExamDetailActivity2 = new Intent(activity, ExamDetailActivity.class);
                                            intentExamDetailActivity2.putExtra("sid", sid);
                                            activity.setClickPosition(position);
                                            intentExamDetailActivity2.putExtra("tid", listList.get(position).get("id"));
                                            intentExamDetailActivity2.putExtra("loadLocalFile", true);
                                            activity.startActivityForResult(intentExamDetailActivity2, 0x7777);
                                            tDialog.dismiss();
                                            break;
                                    }
                                }
                            })
                            .create()
                            .show();
                } else {
                    //TODO 未购买的不进答题页面
                    Intent intentExamDetailActivity = new Intent(activity, ExamDetailActivity.class);
                    intentExamDetailActivity.putExtra("sid", sid);
                    activity.setClickPosition(position);
                    intentExamDetailActivity.putExtra("tid", listList.get(position).get("id"));
                    intentExamDetailActivity.putExtra("loadLocalFile", false);
                    activity.startActivityForResult(intentExamDetailActivity, 0x7777);
                }
                break;
            case R.id.activity_paper_list_listview1_item_textview4://查看解析
                int position2 = (int) v.getTag();
                Intent intentExamResultActivity = new Intent(activity, ExamResultActivity.class);
                intentExamResultActivity.putExtra("id", listList.get(position2).get("last_id"));
                intentExamResultActivity.putExtra("sid", sid);
                activity.startActivity(intentExamResultActivity);
                break;
        }
    }
}
