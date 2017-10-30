package com.ym.traegergill.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ym.traegergill.R;

import java.util.List;

/**
 * Created by Administrator on 2017/9/18.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private FragmentManager fm;
    private List<Fragment> fragments;
    private List<String> titiles;
    private Context context;
    public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titiles) {
        super(fm);
        this.fm = fm;
        this.fragments = fragments;
        this.titiles = titiles;
    }
    public MyFragmentPagerAdapter(Context context,FragmentManager fm, List<Fragment> fragments, List<String> titiles) {
        super(fm);
        this.fm = fm;
        this.fragments = fragments;
        this.titiles = titiles;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
       return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titiles.get(position);
    }

    public View getTabView(int position) {
        View v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        TextView tv = (TextView) v.findViewById(R.id.news_title);
        tv.setText(titiles.get(position));
        ImageView img = (ImageView) v.findViewById(R.id.imageView);
        switch (position){
            case 0:
                img.setImageResource(R.drawable.tab_traeger_icon);
                break;
            case 1:
                img.setImageResource(R.drawable.tab_recipes_icon);
                break;
            case 2:
                img.setImageResource(R.drawable.tab_icon_remote);
                break;
            case 3:
                img.setImageResource(R.drawable.tab_shop_icon);
                break;
            case 4:
                img.setImageResource(R.drawable.tab_my_icon);
                break;
            default:
                img.setImageResource(R.drawable.tab_my_icon);
        }
        //img.setImageResource(imageResId[position]);
        return v;
    }
}
