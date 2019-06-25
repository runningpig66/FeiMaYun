package cn.aura.feimayun.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 描述：设置控件的高度
 * 首页的
 */
public class SetHeightUtil {
    /**
     * 设置GridView高度
     *
     * @param gridView   gridView
     * @param size       gridView的item个数
     * @param numColumns gridView的列数
     */
    public static void setGridViewHeight(GridView gridView, int size, int numColumns) {
        BaseAdapter adapter = (BaseAdapter) gridView.getAdapter();

        //计算griView行数，最后存储在变量row中
        if (size > 4) {//最多显示4个子项
            size = 4;
        }
        int a = size;
        int b = a / numColumns;
        int c = a % numColumns;
        if (c == 0) {
            a = b;
        } else {
            a = b + 1;
        }
        int row = a;

        View gridItem = adapter.getView(0, null, gridView);
        if (gridItem == null) {
            return;
        }
        gridItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        gridItem.measure(0, 0);
        int totalHeight = gridItem.getMeasuredHeight() * row + (gridView.getVerticalSpacing() * (row - 1));
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }

    //dp转像素
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //像素转dp
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    //设置ListView子项高度
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int len = listAdapter.getCount();
        int totalHeight = 0;
        for (int i = 0; i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight = totalHeight + listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (len - 1));
        listView.setLayoutParams(params);
    }

    //设置6个方格高度
    public static void setGridViewHeight2(GridView gridView, int size, int numColumns) {
        BaseAdapter adapter = (BaseAdapter) gridView.getAdapter();

        //计算griView行数，最后存储在变量row中
        int a = size;
        int b = a / numColumns;
        int c = a % numColumns;
        if (c == 0) {
            a = b;
        } else {
            a = b + 1;
        }
        int row = a;

        View gridItem = adapter.getView(0, null, gridView);
        if (gridItem == null) {
            return;
        }
        gridItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        gridItem.measure(0, 0);
        int totalHeight = gridItem.getMeasuredHeight() * row + (gridView.getVerticalSpacing() * (row - 1));
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }

    //首页底部ListView，包含GridView的测量方法
    public static void measureListViewHeight(final AbsListView listView) {
        final ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return;
        }
        listView.post(new Runnable() {
            @Override
            public void run() {
                int totalHeight = 0;
                int count = listAdapter.getCount();
                //这里可以去获取每一列最高的一个
                View listItem = listAdapter.getView(0, null, listView);
                listItem.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                listItem.measure(0, 0);
                if (listView instanceof GridView) {
                    int columns = ((GridView) listView).getNumColumns();
                    int rows = count % columns != 0 ? 1 : 0;
                    rows += count / columns;
                    totalHeight += listItem.getMeasuredHeight() * rows;
                } else if (listView instanceof ListView) {
                    for (int i = 0; i < count; i++) {
                        listItem = listAdapter.getView(i, null, listView);
                        listItem.measure(0, 0);
                        totalHeight += listItem.getMeasuredHeight() + ((ListView) listView).getDividerHeight() * (listAdapter.getCount() - 1);
                    }
                }
                ViewGroup.LayoutParams params = listView.getLayoutParams();

                params.height = totalHeight;

                listView.setLayoutParams(params);
            }
        });
    }

}
