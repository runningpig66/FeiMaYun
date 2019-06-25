package cn.aura.app.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

import cn.aura.app.R;
import cn.aura.app.activity.PhotoViewActivity;
import cn.aura.app.application.MyApplication;
import cn.aura.app.bean.List_Bean;
import cn.aura.app.util.ScreenUtils;

public class AnalysisActivity_ViewPager1_Fragment_ListView1_Adapter extends BaseAdapter {
    //    private int selected = -1;
    private int right = -1;

    private Activity activity;
    private Map<String, String> listsMap;
    private List<Map<String, String>> option_listList;//存放了 key:A  val:西瓜是圆的吗？  img:存放图片  的Map
    private List<String> sub_imgList;//题干图片
    private List<List<String>> itemImgList;//选项图片
    private LayoutInflater inflater;
    private int itemHeight;

    public AnalysisActivity_ViewPager1_Fragment_ListView1_Adapter(Activity activity, Map<String, String> listsMap) {
        this.activity = activity;
        this.listsMap = listsMap;
        inflater = LayoutInflater.from(activity);
        itemHeight = ScreenUtils.dp2px(activity, 120);

        //解析lists标签下的某个Object里的option_list，也就是选项信息
        String option_listString = listsMap.get("option_list");
        if (option_listString != null && !option_listString.equals("")) {
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

                //TODO 最后解析题目图片
                itemImgList = new ArrayList<>();
                for (int i = 0; i < option_listList.size(); i++) {
                    String imgString = option_listList.get(i).get("img");
                    List<String> list = new ArrayList<>();
                    if (imgString != null) {
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
                    }
                    itemImgList.add(list);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String sub_imgString = listsMap.get("sub_img");
        if (sub_imgString != null && !sub_imgString.equals("")) {
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

        //初始化正确答案
        String rightString = listsMap.get("right");
        char[] rightChar = new char[0];
        if (rightString != null) {
            rightString = rightString.toUpperCase();
            rightChar = rightString.toCharArray();
        }
        //将A B C D等选项转换为对应的position
        right = (rightChar[0] + 1) % 65;
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
    public View getView(int position, View convertView, ViewGroup parent) {
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

        if (position == right) {//正确答案的选项
            holder.rvitem_textview1.setBackgroundResource(R.drawable.circle_green);
            holder.rvitem_textview1.setTextColor(Color.parseColor("#ffffff"));
            holder.rvitem_textview2.setTextColor(Color.parseColor("#00a63b"));
        } else {
            holder.rvitem_textview1.setBackgroundResource(R.drawable.circle_gray);
            holder.rvitem_textview1.setTextColor(Color.parseColor("#000000"));
            holder.rvitem_textview2.setTextColor(Color.parseColor("#000000"));
        }

        if (position == 0) {
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
                    Glide.with(MyApplication.context).load(sub_imgList.get(i)).into(listview_imageview_item_imageview1);
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
                    Glide.with(MyApplication.context).load(imgList.get(i)).into(listview_imageview_item_imageview1);
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
        }
        return convertView;
    }

    static class ViewHolder {
        LinearLayout rvitem_layout1;
        TextView rvitem_textview1;
        TextView rvitem_textview2;
        LinearLayout rvitem_layout2;
    }

//                if (selected == right) {//选对的情况
//                    if (position == selected) {//如果选择的是正确答案，就设置选择的那项为绿色，不需要设置别的选项
//                        viewHolder2.textView1.setBackgroundResource(R.drawable.circle_green);
//                        viewHolder2.textView1.setTextColor(Color.parseColor("#ffffff"));
//                        viewHolder2.textView2.setTextColor(Color.parseColor("#00a63b"));
//                    } else {
//                        viewHolder2.textView1.setBackgroundResource(R.drawable.circle_gray);
//                        viewHolder2.textView1.setTextColor(Color.parseColor("#000000"));
//                        viewHolder2.textView2.setTextColor(activity.getResources().getColor(R.color.black));
//                    }
//                }
//                else {//其他情况
//                    if (position == selected) {//如果选择的是错误答案，就设置选择的那项为红色
//                        viewHolder2.textView1.setBackgroundResource(R.drawable.circle_red);
//                        viewHolder2.textView1.setTextColor(activity.getResources().getColor(R.color.white));
//                        viewHolder2.textView2.setTextColor(Color.parseColor("#f25051"));
//                    } else if (position == right) {
//                        viewHolder2.textView1.setBackgroundResource(R.drawable.circle_green);
//                        viewHolder2.textView1.setTextColor(Color.parseColor("#ffffff"));
//                        viewHolder2.textView2.setTextColor(Color.parseColor("#00a63b"));
//                    } else {
//                        viewHolder2.textView1.setBackgroundResource(R.drawable.circle_gray);
//                        viewHolder2.textView1.setTextColor(Color.parseColor("#000000"));
//                        viewHolder2.textView2.setTextColor(Color.parseColor("#000000"));
//                    }
//                }

}
