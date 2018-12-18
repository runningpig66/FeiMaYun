package cn.aura.feimayun.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.aura.feimayun.R;
import cn.aura.feimayun.adapter.PlayDetailRightFragment_ListView_Adapter2;
import cn.aura.feimayun.bean.PlayDetailBean;
import cn.aura.feimayun.util.Util;

public class PlayDetailRightFragment extends Fragment {
    public static Handler handleBuy;

    private Context context;
    private Set<String> sidSet = new LinkedHashSet<>();
    private boolean isFirstInit = true;
    private ListView fragment_playdeatil_right_listView1;
    private String catalogueString;
    private String sid;
    private String isBuy;
    private int children1Position = -1;
    private int children2Position = -1;
    private int children3Position = -1;
    private PlayDetailRightFragment_ListView_Adapter2 adapter;

    public static View getChildAtPosition(final AdapterView view, final int position) {
        final int index = position - view.getFirstVisiblePosition();
        if ((index >= 0) && (index < view.getChildCount())) {
            return view.getChildAt(index);
        } else {
            return null;
        }
    }

    public Set<String> getSidSet() {
        return sidSet;
    }

    @SuppressLint("HandlerLeak")
    private void handle() {
        handleBuy = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                adapter.setIsBuy("1");
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle catalogueBundle = getArguments();

        handle();

        //setArguments传递进来的参数只会在碎片创建的时候调用，导致无法更新SID
        catalogueString = catalogueBundle.getString("catalogueString");
        sid = catalogueBundle.getString("sid");
        isBuy = catalogueBundle.getString("isBuy");
    }

    public void setSid(String sid, String catalogueString, String isBuy) {
        this.sid = sid;
        this.catalogueString = catalogueString;
        this.isBuy = isBuy;
        parseJSON();
    }

    //设置listview点击后进行滑动
//    public void scrollToPosition(int position) {
//        fragment_playdeatil_right_listView1.setSelection(position - 3);
//    }

    //解析JSON
    private void parseJSON() {
        JSONTokener jsonTokener = new JSONTokener(catalogueString);
        try {
            JSONArray catalogueArray = (JSONArray) jsonTokener.nextValue();

            //listview的数据源
            List<PlayDetailBean> detailBeans = new ArrayList<>();
            sidSet = new LinkedHashSet<>();
            for (int i = 0; i < catalogueArray.length(); i++) {
                JSONObject catalogueObject = catalogueArray.getJSONObject(i);
                PlayDetailBean detailBean = new PlayDetailBean();

                detailBean.setId(catalogueObject.getString("id"));
                detailBean.setLid(catalogueObject.getString("lid"));
                detailBean.setLevel(catalogueObject.getString("level"));
                detailBean.setPid(catalogueObject.getString("pid"));
                detailBean.setType(catalogueObject.getString("type"));
                detailBean.setChapter(catalogueObject.getString("chapter"));
                detailBean.setFtype(catalogueObject.getString("ftype"));
                detailBean.setSort(catalogueObject.getString("sort"));
                detailBean.setName(catalogueObject.getString("name"));
                detailBean.setTid(catalogueObject.getString("tid"));
                detailBean.setLearn(catalogueObject.getString("learn"));
                detailBean.setStore_id(catalogueObject.getString("store_id"));//用来传试题sid

                if (detailBean.getFtype().equals("video")) {//添加sid
                    sidSet.add(detailBean.getSort());
                }

                //传入小节信息——2级
                if (catalogueObject.has("children")) {
                    JSONArray children1Array = catalogueObject.getJSONArray("children");
                    List<PlayDetailBean> children1List = new ArrayList<>();
                    for (int j = 0; j < children1Array.length(); j++) {
                        JSONObject children1Object = children1Array.getJSONObject(j);
                        PlayDetailBean children1Bean = new PlayDetailBean();

                        children1Bean.setId(children1Object.getString("id"));
                        children1Bean.setLid(children1Object.getString("lid"));
                        children1Bean.setLevel(children1Object.getString("level"));
                        children1Bean.setPid(children1Object.getString("pid"));
                        children1Bean.setType(children1Object.getString("type"));
                        children1Bean.setChapter(children1Object.getString("chapter"));
                        children1Bean.setFtype(children1Object.getString("ftype"));
                        children1Bean.setSort(children1Object.getString("sort"));
                        children1Bean.setName(children1Object.getString("name"));
                        children1Bean.setTid(children1Object.getString("tid"));
                        children1Bean.setLearn(children1Object.getString("learn"));


                        //传入3级
                        if (children1Object.has("children")) {
                            JSONArray children2Array = children1Object.getJSONArray("children");
                            List<PlayDetailBean> children2List = new ArrayList<>();
                            for (int k = 0; k < children2Array.length(); k++) {
                                JSONObject children2Object = children2Array.getJSONObject(k);
                                PlayDetailBean children2Bean = new PlayDetailBean();

                                children2Bean.setId(children2Object.getString("id"));
                                children2Bean.setLid(children2Object.getString("lid"));
                                children2Bean.setLevel(children2Object.getString("level"));
                                children2Bean.setPid(children2Object.getString("pid"));
                                children2Bean.setType(children2Object.getString("type"));
                                children2Bean.setChapter(children2Object.getString("chapter"));
                                children2Bean.setFtype(children2Object.getString("ftype"));
                                children2Bean.setSort(children2Object.getString("sort"));
                                children2Bean.setName(children2Object.getString("name"));
                                children2Bean.setTid(children2Object.getString("tid"));
                                children2Bean.setLearn(children2Object.getString("learn"));
                                children2Bean.setStore_id(children2Object.getString("store_id"));//用来传试题sid

                                if (children2Bean.getFtype().equals("video")) {
                                    sidSet.add(children2Bean.getSort());
                                }

                                //判断展开项
                                if (children2Object.getString("sort").equals(sid)) {
                                    children2Position = j;//记录下应该展开的三级在二级中的位置
                                    children1Position = i;//记录下应该展开的二级在一级中的位置
                                    children3Position = k;//记录下橘色的三级在三级中的具体位置
                                }

                                children2List.add(children2Bean);
                            }
                            children1Bean.setChildrenList(children2List);
                        } else {
                            children1Bean.setChildrenList(null);
                        }
                        children1List.add(children1Bean);
                    }
                    detailBean.setChildrenList(children1List);

                } else {
                    detailBean.setChildrenList(null);
                }
                detailBeans.add(detailBean);
            }

            //开始添加需要展开的数据
            if (children1Position != -1 && children2Position != -1) {
                PlayDetailBean level1 = detailBeans.get(children1Position);//找到需要展开的一级的item项
                level1.setOpen(true);
                level1.setIsAlive(true);
                List<PlayDetailBean> level1ChildrenList = level1.getChildrenList();//找到需要展开的一级的item项的二级list
                int tempPosition = 0;
                for (int i = 0; i < level1ChildrenList.size(); i++) {//遍历该一级下的二级item，开始添加二级
                    tempPosition++;//记录下一个二级存放的相对位置
                    detailBeans.add(children1Position + tempPosition, level1.getChildrenList().get(i));//添加二级
                    if (i == children2Position) {//找到需要添加三级的二级位置，这个方法只可能执行一次
                        PlayDetailBean level2 = level1ChildrenList.get(i);//找到需要展开的二级的item项
                        level2.setOpen(true);
                        level2.setIsAlive(true);
                        List<PlayDetailBean> level2ChildrenList = level2.getChildrenList();//找到需要展开的二级的item项的三级list
                        int tempPosition2 = 0;
                        children3Position += children1Position + tempPosition + 1;//记录下橘色三级在整个list中的实际位置，用于滚动到该位置
                        for (int j = 0; j < level2ChildrenList.size(); j++) {//遍历该二级下的三级item，开始添加三级
                            tempPosition2++;//记录下一个三级存放的相对位置
                            //children1Position + tempPosition是“最后一个”二级的位置
                            detailBeans.add(children1Position + tempPosition + tempPosition2, level2ChildrenList.get(j));//添加三级，
                        }
                        tempPosition += level2ChildrenList.size();//下一个二级的位置在tempPosition+1添加
                    }
                }
                printList(detailBeans);
            }

            String uid = Util.getUid();

            adapter = new PlayDetailRightFragment_ListView_Adapter2(context, detailBeans, sid, uid, isBuy);
            fragment_playdeatil_right_listView1.setAdapter(adapter);

            if (children3Position > -1 && isFirstInit) {//如果不是第一次加载
                if (children3Position - 8 > 0) {//为了让滑动更平滑，最大滑动距离为8-3=5
                    fragment_playdeatil_right_listView1.setSelection(children3Position - 8);
                }
                smoothScrollToPositionFromTop(fragment_playdeatil_right_listView1, children3Position - 3);
                isFirstInit = false;
            } else {
                fragment_playdeatil_right_listView1.setSelection(children3Position - 3);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void smoothScrollToPositionFromTop(final AbsListView view, final int position) {
        View child = getChildAtPosition(view, position);
        // There's no need to scroll if child is already at top or view is already scrolled to its end
        if ((child != null) && ((child.getTop() == 0) || ((child.getTop() > 0) && !view.canScrollVertically(1)))) {
            return;
        }

        view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    view.setOnScrollListener(null);

                    // Fix for scrolling bug
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            view.setSelection(position);
                        }
                    });
                }
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
            }
        });

        // Perform scrolling to position
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                view.smoothScrollToPositionFromTop(position, 0);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playdeatil_right, container, false);
        fragment_playdeatil_right_listView1 = view.findViewById(R.id.fragment_playdeatil_right_listView1);
        View view1 = LayoutInflater.from(context).inflate(R.layout.playdetail_rightfragment_listview_title, null);
        fragment_playdeatil_right_listView1.addHeaderView(view1);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parseJSON();
    }

    //打印数据源
    private void printList(List<PlayDetailBean> detailBeans) {
        for (int i = 0; i < detailBeans.size(); i++) {
            String name = detailBeans.get(i).getName();
            System.out.print(name + "， ");
        }
        System.out.println("detailBeansSize:" + detailBeans.size());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
