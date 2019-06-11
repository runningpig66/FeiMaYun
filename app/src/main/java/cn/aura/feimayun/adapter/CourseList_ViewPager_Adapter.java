package cn.aura.feimayun.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;
import java.util.Map;

public class CourseList_ViewPager_Adapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private List<Map<String, String>> lmList_List;

    public CourseList_ViewPager_Adapter(FragmentManager fm, List<Fragment> fragments, List<Map<String, String>> lmList_List) {
        super(fm);
        this.fragments = fragments;
        this.lmList_List = lmList_List;
    }

    public void setData(List<Fragment> fragments, List<Map<String, String>> lmList_List) {
        this.fragments = fragments;
        this.lmList_List = lmList_List;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments == null ? 0 : fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return lmList_List.get(position).get("name");
    }
}
