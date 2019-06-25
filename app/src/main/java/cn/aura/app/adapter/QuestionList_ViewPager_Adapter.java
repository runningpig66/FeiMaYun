package cn.aura.app.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class QuestionList_ViewPager_Adapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private String[] lmListString;

    public QuestionList_ViewPager_Adapter(FragmentManager fm, List<Fragment> fragments, String[] lmListString) {
        super(fm);
        this.fragments = fragments;
        this.lmListString = lmListString;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public String getPageTitle(int position) {
        return lmListString[position];
    }

}
