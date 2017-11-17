package com.ym.traegergill.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ym.traegergill.R;

import java.util.List;

/**
 * Created by Administrator on 2017/9/18.
 */

public class MyDyFragmentPagerAdapter extends FragmentPagerAdapter {
    private FragmentManager fm;
    private List<Fragment> fragments;
    private List<String> titiles;
    private Context context;
    private boolean isFirst = true;
    public MyDyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titiles) {
        super(fm);
        this.fm = fm;
        this.fragments = fragments;
        this.titiles = titiles;
    }

    public MyDyFragmentPagerAdapter(Context context, FragmentManager fm, List<Fragment> fragments, List<String> titiles) {
        super(fm);
        this.fm = fm;
        this.fragments = fragments;
        this.titiles = titiles;
        this.context = context;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //得到缓存的fragment
        Fragment fragment = (Fragment) super.instantiateItem(container,
                position);
//得到tag，这点很重要
        String fragmentTag = fragment.getTag();


        if (position == 1 &&  !isFirst) {
//如果这个fragment需要更新

            FragmentTransaction ft = fm.beginTransaction();
//移除旧的fragment
            ft.remove(fragment);
//换成新的fragment
            fragment = fragments.get(position % fragments.size());
//添加新fragment时必须用前面获得的tag，这点很重要
            ft.add(container.getId(), fragment, fragmentTag);
            ft.attach(fragment);
            ft.commit();
            isFirst = false;
        }
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return fragments.get(position).hashCode();
    }


    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titiles.get(position);
    }

}
