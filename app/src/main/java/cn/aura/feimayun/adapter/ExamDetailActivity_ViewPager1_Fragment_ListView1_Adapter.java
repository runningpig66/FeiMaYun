package cn.aura.feimayun.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aura.feimayun.R;
import cn.aura.feimayun.activity.ExamDetailActivity;
import cn.aura.feimayun.activity.PhotoViewActivity;
import cn.aura.feimayun.bean.List_Bean;
import cn.aura.feimayun.util.ScreenUtils;

import static android.content.Context.MODE_PRIVATE;

public class ExamDetailActivity_ViewPager1_Fragment_ListView1_Adapter extends BaseAdapter {
    private int selected = -1;

    private ExamDetailActivity activity;
    private Map<String, String> listsMap;
    private List<Map<String, String>> option_listList;//存放了 key:A  val:西瓜是圆的吗？  img:存放图片  的Map
    private List<String> sub_imgList;//题干图片
    private List<List<String>> itemImgList;//选项图片
    private LayoutInflater inflater;
    private int itemHeight;
    private OnItemClickListener mOnItemClickListener;

    public ExamDetailActivity_ViewPager1_Fragment_ListView1_Adapter(Activity activity, Map<String, String> listsMap) {
        this.activity = (ExamDetailActivity) activity;
        this.listsMap = listsMap;
        inflater = LayoutInflater.from(activity);
        itemHeight = ScreenUtils.dp2px(activity, 120);

        //解析lists标签下的某个Object里的option_list，也就是选项信息
        String option_listString = listsMap.get("option_list");
        if (!option_listString.equals("")) {
            try {
                JSONTokener jsonTokener = new JSONTokener(option_listString);
                JSONArray option_listArray = (JSONArray) jsonTokener.nextValue();
                option_listList = new ArrayList<>();
                for (int i = 0; i < option_listArray.length(); i++) {
                    JSONObject option_listObject = option_listArray.getJSONObject(i);
                    Map<String, String> option_listMap = new HashMap<>();
                    //转为大写
                    option_listMap.put("key", option_listObject.getString("key").toUpperCase());
                    option_listMap.put("val", option_listObject.getString("val"));
                    if (option_listObject.has("img")) {
                        JSONArray imgArray = option_listObject.getJSONArray("img");
                        option_listMap.put("img", imgArray.toString());
                    } else {
                        option_listMap.put("img", "");
                    }
                    option_listList.add(option_listMap);
                }

                //最后解析题目图片
                itemImgList = new ArrayList<>();
                for (int i = 0; i < option_listList.size(); i++) {
                    String imgString = option_listList.get(i).get("img");
                    List<String> list = new ArrayList<>();
                    if (!imgString.equals("")) {
                        JSONTokener tokener = new JSONTokener(imgString);
                        JSONArray array = (JSONArray) tokener.nextValue();
                        for (int j = 0; j < array.length(); j++) {
                            String s = (String) array.get(j);
                            list.add(s);
                        }
                    } else {
                        list.add("");
                    }
                    itemImgList.add(list);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String sub_imgString = listsMap.get("sub_img");
        if (!sub_imgString.equals("")) {
            try {
                JSONTokener jsonTokener = new JSONTokener(sub_imgString);
                JSONArray sub_imgArray = (JSONArray) jsonTokener.nextValue();
                sub_imgList = new ArrayList<>();
                for (int i = 0; i < sub_imgArray.length(); i++) {
                    String sub_imgStringItem = (String) sub_imgArray.get(i);
                    sub_imgList.add(sub_imgStringItem);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String checked = listsMap.get("checked");
        char[] c = checked.toCharArray();
        if (!checked.equals("")) {
            //将A B C D等选项转换为对应的position
            selected = (c[0] + 1) % 65;
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    private List<Map<String, String>> getOption_listList() {
        return option_listList;
    }

    @Override
    public int getCount() {
        return option_listList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.examdetail_rv_item, parent, false);
            holder.rvitem_layout1 = convertView.findViewById(R.id.rvitem_layout1);
            holder.rvitem_textview1 = convertView.findViewById(R.id.rvitem_textview1);
            holder.rvitem_textview2 = convertView.findViewById(R.id.rvitem_textview2);
            holder.rvitem_layout2 = convertView.findViewById(R.id.rvitem_layout2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == selected) {
            holder.rvitem_textview1.setBackgroundResource(R.drawable.circle_orange);
            holder.rvitem_textview1.setTextColor(activity.getResources().getColor(R.color.white));
            holder.rvitem_textview2.setTextColor(activity.getResources().getColor(R.color.orange));
        } else {
            holder.rvitem_textview1.setBackgroundResource(R.drawable.circle_gray);
            holder.rvitem_textview1.setTextColor(activity.getResources().getColor(R.color.black));
            holder.rvitem_textview2.setTextColor(activity.getResources().getColor(R.color.black));
        }

        //加文字
        if (position == 0) {//0是题干
            holder.rvitem_textview1.setVisibility(View.GONE);//隐藏ABCD选项布局，只留下题干
            holder.rvitem_textview2.setText(listsMap.get("no") + "." + listsMap.get("subject"));
            if (sub_imgList.isEmpty()) {
                holder.rvitem_layout2.setVisibility(View.GONE);
            } else {
                holder.rvitem_layout2.setVisibility(View.VISIBLE);
                holder.rvitem_layout2.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(activity);
                for (int i = 0; i < sub_imgList.size(); i++) {
                    @SuppressLint("InflateParams") ImageView listview_imageview_item_imageview1 = (ImageView) inflater.inflate(R.layout.info2_recyclerview_imageview, null);
                    Glide.with(activity).load(sub_imgList.get(i)).into(listview_imageview_item_imageview1);
                    LinearLayout linearLayout = new LinearLayout(activity);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, itemHeight);
                    linearLayout.addView(listview_imageview_item_imageview1, params);
                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params1.topMargin = ScreenUtils.dp2px(activity, 10);
                    final int finalI = i;
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(activity, PhotoViewActivity.class);
                            Bundle bundle = new Bundle();
                            List_Bean bean = new List_Bean();
                            bean.setStringList(sub_imgList);
                            bundle.putSerializable("questionlistdataBean", bean);
                            intent.putExtras(bundle);
                            intent.putExtra("currentPosition", finalI);
                            activity.startActivity(intent);
                        }
                    });
                    holder.rvitem_layout2.addView(linearLayout, params1);
                }
            }
        } else {
            holder.rvitem_textview1.setVisibility(View.VISIBLE);
            holder.rvitem_textview1.setText(option_listList.get(position - 1).get("key"));
            holder.rvitem_textview2.setText(option_listList.get(position - 1).get("val"));

            final List<String> imgList = itemImgList.get(position - 1);
            if (imgList.isEmpty()) {
                holder.rvitem_layout2.setVisibility(View.GONE);
            } else {
                holder.rvitem_layout2.setVisibility(View.VISIBLE);
                holder.rvitem_layout2.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(activity);
                for (int i = 0; i < imgList.size(); i++) {
                    @SuppressLint("InflateParams") ImageView listview_imageview_item_imageview1 = (ImageView) inflater.inflate(R.layout.info2_recyclerview_imageview, null);
                    Glide.with(activity).load(imgList.get(i)).into(listview_imageview_item_imageview1);
                    LinearLayout linearLayout = new LinearLayout(activity);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, itemHeight);
                    linearLayout.addView(listview_imageview_item_imageview1, params);
                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params1.topMargin = ScreenUtils.dp2px(activity, 10);
                    final int finalI = i;
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(activity, PhotoViewActivity.class);
                            Bundle bundle = new Bundle();
                            List_Bean bean = new List_Bean();
                            bean.setStringList(imgList);
                            bundle.putSerializable("questionlistdataBean", bean);
                            intent.putExtras(bundle);
                            intent.putExtra("currentPosition", finalI);
                            activity.startActivity(intent);
                        }
                    });
                    holder.rvitem_layout2.addView(linearLayout, params1);
                }
            }

            holder.rvitem_layout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String key = listsMap.get("no_id");
                    String val = getOption_listList().get(position - 1).get("key");
                    activity.getAnswerMap().put(key, val);//保存答案

                    //同时保存到本地文件中
                    SharedPreferences.Editor editor = activity.getSharedPreferences(activity.fileName, MODE_PRIVATE).edit();
                    editor.putString(key, val.toUpperCase());
                    editor.putInt("write", activity.getAnswerMap().size());
                    editor.putString("time", String.valueOf(activity.countDownMillis / 1000));
                    editor.commit();

                    selected = position;//这里设置selected是为了在notify的时候，改变年背景色
                    mOnItemClickListener.onTextViewClick(v, position);
                }
            });
        }
        return convertView;
    }

    //    获取被点击的item的position
    public void setPosition(int selected) {
        this.selected = selected;
    }

    public interface OnItemClickListener {
        void onTextViewClick(View view, int position);
    }

    //重写，更改item文字背景时不刷新图片
//    public void notifyDataSetChanged(ListView listView) {
//        int firstVisiblePosition = 1;
//        int lastVisiblePosition = listView.getLastVisiblePosition();
//        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
//            View item = listView.getChildAt(i);
//            getView(i, item, listView);
//        }
//    }

    static class ViewHolder {
        LinearLayout rvitem_layout1;
        TextView rvitem_textview1;
        TextView rvitem_textview2;
        LinearLayout rvitem_layout2;
    }

}
