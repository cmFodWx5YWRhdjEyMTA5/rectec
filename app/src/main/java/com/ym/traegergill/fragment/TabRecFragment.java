package com.ym.traegergill.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.google.gson.Gson;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.FilterActivity;
import com.ym.traegergill.activity.ItemsDetailActivity;
import com.ym.traegergill.activity.MainActivity;
import com.ym.traegergill.activity.RecipesSearchActivity;
import com.ym.traegergill.adapter.MyDyFragmentPagerAdapter;
import com.ym.traegergill.adapter.MyDyFragmentPagerAdapter;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.modelBean.Filter;
import com.ym.traegergill.modelBean.FilterGroupByFilterTypeModel;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.SharedPreferencesUtils;
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
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

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
    ArrayList<String> titles;
    ArrayList<Fragment> fragments;
    private CommonNavigator commonNavigator;
    private MyDyFragmentPagerAdapter myPagerAdapter;
    private List<Filter> filterList;
    private SharedPreferencesUtils spUtils;
    private UpdateUserStatusReceiver updateUserStatusReceiver;

    class UpdateUserStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS)) {
                showCollection(spUtils.getBoolean(Constants.ISLOGIN, false));
            }
        }
    }

    private void showCollection(boolean isLogin) {
      /*  initTab();
        if(isLogin){
            if(!titles.get(1).equals("FAVORITES")){
                titles.add(1,"FAVORITES");
                fragments.add(1,new ShowRecipesFragment(Constants.MAIN_INGREDIENT_FAVORITES));
                commonNavigator.notifyDataSetChanged();
                myPagerAdapter.notifyDataSetChanged();
            }
        }else{
            if(titles.get(1).equals("FAVORITES")) {
                titles.remove(1);
                fragments.remove(1);
                commonNavigator.notifyDataSetChanged();
                myPagerAdapter.notifyDataSetChanged();
            }
        }
        TLog("f size : " + myPagerAdapter.getCount());*/
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tab_rec, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        title.setText("RECIPES");
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();

        initViewPager();
        initListener();
        //注册广播
/*        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
        updateUserStatusReceiver = new UpdateUserStatusReceiver();
        getActivity().registerReceiver(updateUserStatusReceiver, intentFilter);*/
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

        getActivity().startActivity(
                new Intent(getActivity(), RecipesSearchActivity.class),
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) getActivity(),
                        Pair.create((View)searchFor, getActivity().getString(R.string.iv_img_transitionName))
                ).toBundle()
        );

       /* if (title.getVisibility() == View.INVISIBLE) {
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
        }*/

    }
    Gson gson;
    private void initViewPager() {
        gson = new Gson();
        netTabData();
    }

    private void netTabData() {
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("isFromCache : " + isFromCache+"  json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    TLog(obj.optString("msg"));
                    if (obj.optInt("code") == 200) {
                        JSONArray data = obj.optJSONArray("content");
                        for(int i = 0 ;i<data.length();i++){
                            FilterGroupByFilterTypeModel model = gson.fromJson(data.optJSONObject(i).toString(),FilterGroupByFilterTypeModel.class);
                            if( model.getFilterTypeid()== Constants.MAIN_INGREDIENT){
                                filterList = model.getFilterList();
                                initTab();
                                break;
                            }
                        }
                    }else{
                        showToastError(obj.optString("msg"));
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
        ProgressUtil.showLoading(getContext(),getString(R.string.loading));
        HttpParams params = new HttpParams();
        if(!MyNetTool.netHttpParams(getActivity(), URLs.findFilterListGroupByFilterType,callback,params)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netTabData();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            System.exit(0);
                            break;
                    }
                }
            });

        }

    }

    private void initTab() {
        if(titles == null)
            titles = new ArrayList<>();
        else
            titles.clear();
        titles.add("ALL");
            titles.add("FAVORITES");
        int index = titles.size();
        for(Filter filter : filterList){
            titles.add(filter.getFilterName().toUpperCase());
        }

        fragments = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            if (titles.get(i).equals("ALL")) {
                fragments.add(new ShowRecipesFragment(Constants.MAIN_INGREDIENT_ALL));
            } else if(titles.get(i).equals("FAVORITES")){
                fragments.add(new ShowRecipesFragment(Constants.MAIN_INGREDIENT_FAVORITES));
            }else{
                fragments.add(new ShowRecipesFragment(filterList.get(i-index).getFilterid()));
            }
        }
        viewPager.setOffscreenPageLimit(fragments.size());
        myPagerAdapter = new MyDyFragmentPagerAdapter(getChildFragmentManager(), fragments, titles);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MainActivity.mainActivity.showBottom();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // title.setText(titles.get(0).replaceAll("@",""));

        initMagicIndicator();
    }

    private void initMagicIndicator() {
        tabLayout.setBackgroundColor(Color.parseColor("#080404"));
        commonNavigator = new CommonNavigator(getActivity());
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
        //showCollection(SharedPreferencesUtils.getSharedPreferencesUtil(getActivity()).getBoolean(Constants.ISLOGIN,false));
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        try {
            getActivity().unregisterReceiver(updateUserStatusReceiver);
        } catch (Exception e) {

        }
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
