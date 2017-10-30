package com.ym.traegergill.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.adapter.MyFragmentPagerAdapter;
import com.ym.traegergill.behavior.ByeBurgerBehavior;
import com.ym.traegergill.fragment.EmptyFragment;
import com.ym.traegergill.fragment.H5ShopFragment;
import com.ym.traegergill.fragment.MyFragment;
import com.ym.traegergill.fragment.TabRecFragment;
import com.ym.traegergill.fragment.TabTraFragment;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.SharedPreferencesUtils;
import com.ym.traegergill.view.NoScrollViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    @BindView(R.id.viewPager)
    NoScrollViewPager viewPager;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    public static MainActivity mainActivity;
    @BindView(R.id.click)
    TextView click;
    private MyFragmentPagerAdapter adapter;
    private List<String> titles;
    private List<Fragment> fragments;
    private ByeBurgerBehavior mBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initViewPager();
    }

    public void showBottom() {
        mBehavior.show();
    }

    private void initViewPager() {
        mBehavior = ByeBurgerBehavior.from(tabLayout);
        String[] resS = getResources().getStringArray(R.array.bottom_tab_list);
        titles = Arrays.asList(resS);
        fragments = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            if (i == 0) {
                fragments.add(new TabTraFragment());
            } else if (i == 1) {
                fragments.add(new TabRecFragment());
            } else if (i == 4) {
                fragments.add(new MyFragment());
            } else if (i == 3) {
                fragments.add(new H5ShopFragment());
            } else {
                fragments.add(new EmptyFragment());
            }

        }
        viewPager.setOffscreenPageLimit(fragments.size());
        // viewPager.addOnPageChangeListener(mOnPageChangeListener);
        adapter = new MyFragmentPagerAdapter(this, getSupportFragmentManager(), fragments, titles);
        viewPager.setAdapter(adapter);
        //tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < adapter.getCount(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            if (tab != null) {
                tab.setCustomView(adapter.getTabView(i));
                if (tab.getCustomView() != null) {
                    View tabView = (View) tab.getCustomView().getParent(); //重点是这一句
                    tabView.setTag(i);
                    if (i == 2) {
                        tabView.setClickable(false);
                    } else {
                        tabView.setOnClickListener(mTabOnClickListener);
                    }

                }
            }
            tabLayout.addTab(tab, i);
        }
//        viewPager.setCurrentItem(1);
        tabLayout.getTabAt(0).getCustomView().setSelected(true);

    }

    private boolean mIsExit;

    @Override
    /**
     * 双击返回键退出
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIsExit) {
                //this.finish();
                System.exit(0);
            } else {
                showToastSuccess(getString(R.string.action_tips_exit_hint));
                mIsExit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsExit = false;
                    }
                }, 2000);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private View.OnClickListener mTabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int pos = (int) view.getTag();
            if (pos != 2) {
                TabLayout.Tab tab = tabLayout.getTabAt(pos);
                if (tab != null) {
                    tab.select();
                }
                viewPager.setCurrentItem(pos);
            }

        }
    };

    @OnClick(R.id.click)
    public void onViewClicked(View view) {
        if(!SharedPreferencesUtils.getSharedPreferencesUtil(getActivity()).getBoolean(Constants.ISLOGIN, false)
                ||
                (SharedPreferencesUtils.getSharedPreferencesUtil(getActivity()).getBoolean(Constants.ISFIRSTTIME, true)
                        && TuyaUser.getDeviceInstance().getDevList().size()==0)){
            //(第一次 && 设备个数等于0) || 非登录状态
            startActivity(new Intent(getActivity(), ConnectWifiGuidActivity.class));
            overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
        }else{
            startActivity(new Intent(getActivity(),DevicesActivity.class));
            overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean needLogin() {
        return false;
    }
}
