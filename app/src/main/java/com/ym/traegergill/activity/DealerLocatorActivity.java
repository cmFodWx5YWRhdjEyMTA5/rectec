package com.ym.traegergill.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.ym.traegergill.R;
import com.ym.traegergill.adapter.MyFragmentPagerAdapter;
import com.ym.traegergill.bean.TagBean;
import com.ym.traegergill.fragment.ShowDealerFragment;
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
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/11/24.
 */

public class DealerLocatorActivity extends BaseActivity {
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.tabLayout)
    MagicIndicator tabLayout;
    List<TagBean> titles;
    List<Fragment> fragments;
    MyFragmentPagerAdapter myPagerAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dealer_locator);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText("DEALER LOCATOR");

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
                            TagBean bean = new TagBean();
                            bean.setName(array.optJSONObject(i).optString("name"));
                            bean.setId(array.optJSONObject(i).optInt("distributorTypeid"));
                            titles.add(bean);
                        }
                    initViewPager();
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

        if(!MyNetTool.netHttpParams(getActivity(), URLs.findDistributorTypeAll,callback,params)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netTitle();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            getActivity().finish();
                            break;
                    }
                }
            });
        }
    }

    private void initViewPager() {
        fragments = new ArrayList<>();
        List<String> temps = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            fragments.add(new ShowDealerFragment(titles.get(i).getId()));
            temps.add(titles.get(i).getName());

        }
        viewPager.setOffscreenPageLimit(fragments.size());
        myPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments, temps);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //title.setText(titles.get(position).replaceAll("@",""));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
}
