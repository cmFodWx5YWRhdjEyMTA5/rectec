package com.ym.traegergill.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.activity.FilterActivity;
import com.ym.traegergill.activity.MainActivity;
import com.ym.traegergill.adapter.MyFragmentPagerAdapter;
import com.ym.traegergill.tools.OUtil;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.TriangularPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * Created by Administrator on 2017/9/18.
 */

public class TabRecFragment extends BaseFragment {


    @BindView(R.id.tabLayout)
    MagicIndicator tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    Unbinder unbinder;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.searchFor)
    ImageView searchFor;
    @BindView(R.id.filter)
    TextView filter;
    @BindView(R.id.et_search)
    EditText etSearch;
    List<String> titles;
    List<Fragment> fragments;

    private MyFragmentPagerAdapter myPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tab_rec, container, false);
        unbinder = ButterKnife.bind(this, view);
        initViewPager();
        initListener();
        return view;
    }

    private void initListener() {
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    search();
                }
                return false;
            }
        });

    }

    private void search() {

        String searchContext = etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(searchContext)) {
            showToastError("不能为空");
            etSearch.setFocusable(true);
            etSearch.setFocusableInTouchMode(true);
            searchFor.requestFocus();

        } else {
            showToastSuccess("搜索内容 ： "+searchContext);
            showInput(false);
            // 调用搜索的API方法
            //searchUser(searchContext);
        }
    }

    private void showEtSearch() {
        if (title.getVisibility() == View.INVISIBLE) {
            showInput(false);
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.search_anim_hide);
            etSearch.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    searchFor.setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    etSearch.setVisibility(View.INVISIBLE);
                    title.setVisibility(View.VISIBLE);
                    searchFor.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {

            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.search_anim_show);
            etSearch.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    etSearch.setVisibility(View.VISIBLE);
                    title.setVisibility(View.INVISIBLE);
                    searchFor.setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    searchFor.setEnabled(true);
                    showInput(true);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

    }

    private void initViewPager() {
        String[] resS = getResources().getStringArray(R.array.top_tab_list2);
        titles = Arrays.asList(resS);
        fragments = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            if (i == 0) {
                fragments.add(new ShowRecipesFragment());
            } else {
                fragments.add(new ShowRecipesFragment());
            }
        }
        viewPager.setOffscreenPageLimit(fragments.size());
        myPagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager(), fragments, titles);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //title.setText(titles.get(position).replaceAll("@",""));
                MainActivity.mainActivity.showBottom();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // title.setText(titles.get(0).replaceAll("@",""));
        title.setText("RECIPES");
        initMagicIndicator();

    }

    private void initMagicIndicator() {
        tabLayout.setBackgroundColor(Color.parseColor("#080404"));
        CommonNavigator commonNavigator = new CommonNavigator(getActivity());
        commonNavigator.setScrollPivotX(0.2f);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return titles == null ? 0 : titles.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new SimplePagerTitleView(context);
                simplePagerTitleView.setText(titles.get(index));
                simplePagerTitleView.setNormalColor(Color.parseColor("#999999"));
                simplePagerTitleView.setSelectedColor(Color.parseColor("#dc5f27"));
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewPager.setCurrentItem(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                TriangularPagerIndicator indicator = new TriangularPagerIndicator(context);
                indicator.setLineColor(Color.parseColor("#dc5f27"));
                return indicator;
            }
        });
        tabLayout.setNavigator(commonNavigator);
        ViewPagerHelper.bind(tabLayout, viewPager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    void showInput(boolean show) {
        if (show) {
            etSearch.setFocusable(true);
            etSearch.setFocusableInTouchMode(true);
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etSearch, 0);
        } else {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    @OnClick({R.id.searchFor, R.id.filter})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.searchFor:
                showEtSearch();
                break;
            case R.id.filter:
                showInput(false);
                getActivity().startActivity(
                        new Intent(getActivity(), FilterActivity.class),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity) getActivity(),
                                Pair.create(view, getActivity().getString(R.string.iv_img_transitionName))
                        ).toBundle()
                );
                break;
        }
    }
}
