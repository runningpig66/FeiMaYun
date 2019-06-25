package cn.aura.app.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Map;

import cn.aura.app.R;
import cn.aura.app.activity.ExamDetailActivity;
import cn.aura.app.adapter.ExamDetailActivity_ViewPager1_Fragment_ListView1_Adapter;
import cn.aura.app.bean.List_Bean;

public class ExamDetailActivity_ViewPager1_Fragment extends Fragment {
    private ExamDetailActivity_ViewPager1_Fragment_ListView1_Adapter adapter;
    private Map<String, String> listsMap;
    private int selected = -1;
    private ExamDetailActivity activity;

    private ListView fragment_examdetailactivity_viewpager1_listview1;

    private void initListView() {
        adapter = new ExamDetailActivity_ViewPager1_Fragment_ListView1_Adapter(activity, listsMap);
        fragment_examdetailactivity_viewpager1_listview1.setAdapter(adapter);
        fragment_examdetailactivity_viewpager1_listview1.addFooterView(new FrameLayout(activity));
//        fragment_examdetailactivity_viewpager1_listview1.addHeaderView(view);
        adapter.setOnItemClickListener(new ExamDetailActivity_ViewPager1_Fragment_ListView1_Adapter.OnItemClickListener() {
            @Override
            public void onTextViewClick(View view, int position) {
                TextView rvitem_textview1 = view.findViewById(R.id.rvitem_textview1);
                TextView rvitem_textview2 = view.findViewById(R.id.rvitem_textview2);
                rvitem_textview1.setBackgroundResource(R.drawable.circle_orange);
                rvitem_textview1.setTextColor(activity.getResources().getColor(R.color.white));
                rvitem_textview2.setTextColor(activity.getResources().getColor(R.color.orange));
                selected = position;//这里设置selected是为了在碎片resume的时候恢复选项
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examdetailactivity_viewpager1, null);
        fragment_examdetailactivity_viewpager1_listview1 = view.findViewById(R.id.fragment_examdetailactivity_viewpager1_listview1);
        activity = (ExamDetailActivity) getActivity();

        Bundle bundle = getArguments();
        List_Bean bean = (List_Bean) bundle.getSerializable("bean");
        listsMap = bean.getMap();

        //初始化数据，开始加载listview控件信息
        initListView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selected != -1) {
            adapter.setPosition(selected);
        }
    }
}
