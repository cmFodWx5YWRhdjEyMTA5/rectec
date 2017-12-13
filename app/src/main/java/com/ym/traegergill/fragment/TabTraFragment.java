package com.ym.traegergill.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.MainActivity;
import com.ym.traegergill.adapter.PlatformFragmentPagerAdapter;
import com.ym.traegergill.modelBean.SharePlatform;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.TriangularPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Administrator on 2017/9/18.
 */

public class TabTraFragment extends BaseFragment {


    @BindView(R.id.tabLayout)
    MagicIndicator tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.title)
    TextView title;

    List<SharePlatform> titles;
    List<Fragment> fragments;
    private PlatformFragmentPagerAdapter myPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tab_tra, container, false);
        unbinder = ButterKnife.bind(this, view);
        initTitle();
        return view;
    }

    private void initTitle() {
        title.setText("RECTECGRILLS");
        titles = new ArrayList<>();
        netTitle();

    }
    private void netTitle() {
        ProgressUtil.showLoading(getActivity(),getString(R.string.loading));
        HttpParams params = new HttpParams();
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("isFromCache : "+isFromCache+" json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if(obj.optInt("code")==200){
                        JSONArray array = obj.optJSONArray("content");
                        for(int i = 0;i<array.length();i++){
                            SharePlatform share = new SharePlatform();
                            share.setSharePlatformid(array.optJSONObject(i).optInt("sharePlatformid"));
                            share.setName(array.optJSONObject(i).optString("name").toUpperCase());
                            titles.add(share);
                        }
                        setViewPager();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAfter(boolean isFromCache, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                super.onAfter(isFromCache, s, call, response, e);
                ProgressUtil.hideLoading();
            }
        };

        if(!MyNetTool.netHttpParams(getActivity(),URLs.findSharePlatformAll,callback,params)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netTitle();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            System.exit(0);
                            break;
                    }
                }
            });
        }

    }
    private void setViewPager(){
        fragments = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            fragments.add(new ShowItemsFragment(titles.get(i).getSharePlatformid()));
        }
        viewPager.setOffscreenPageLimit(fragments.size());
        myPagerAdapter = new PlatformFragmentPagerAdapter(getChildFragmentManager(), fragments, titles);
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
        //title.setText(titles.get(0).replaceAll("@",""));
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
                simplePagerTitleView.setText(titles.get(index).getName());
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
}
