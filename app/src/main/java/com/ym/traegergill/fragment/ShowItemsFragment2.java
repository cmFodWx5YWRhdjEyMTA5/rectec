package com.ym.traegergill.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.ItemsDetailActivity;
import com.ym.traegergill.adapter.ShowItemsRvAdapter;
import com.ym.traegergill.bean.PhotoBean;
import com.ym.traegergill.other.SpaceItemDecoration;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.view.LoadingView;
import com.ym.traegergill.view.loadmore.DefaultFootItem;
import com.ym.traegergill.view.loadmore.OnLoadMoreListener;
import com.ym.traegergill.view.loadmore.RecyclerViewWithFooter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2017/9/18.
 */

public class ShowItemsFragment2 extends BaseFragment {


    @BindView(R.id.rv_load_more)
    RecyclerViewWithFooter rvLoadMore;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    Unbinder unbinder;
    private LoadingView loading;
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    String[] imgs;
    List<PhotoBean> mData;
    ShowItemsRvAdapter adapter;
    int count = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_items, container, false);
        imgs = getResources().getStringArray(R.array.user_photos);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        loading = new LoadingView(getActivity(), R.style.CustomDialog);
        loading.show();
        new Handler().postDelayed(new Runnable() {//定义延时任务模仿网络请求
            @Override
            public void run() {
                loading.dismiss();//3秒后调用关闭加载的方法

            }
        }, 300);
        initRecycler();
    }

    private void initRecycler() {
        pullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                        pullToRefresh.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mData = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            mData.add(new PhotoBean(getURL(i)));
        }
        rvLoadMore.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new ShowItemsRvAdapter(getActivity(), mData);
        adapter.setOnMyItemClickListener(new ShowItemsRvAdapter.OnMyItemClickListener() {
            @Override
            public void onNormalClick(View v, int position) {
                getActivity().startActivity(
                        new Intent(getActivity(), ItemsDetailActivity.class).putExtra("data", mData.get(position).getImageUrl()),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity) getActivity(),
                                Pair.create(v.findViewById(R.id.iv_image), getActivity().getString(R.string.iv_img_transitionName))
                        ).toBundle()
                );
            }
        });
        rvLoadMore.setAdapter(adapter);
        rvLoadMore.addItemDecoration(new SpaceItemDecoration(OUtil.dip2px(getActivity(), 1)));
//        mRecyclerViewWithFooter.setStaggeredGridLayoutManager(2);
        rvLoadMore.setFootItem(new DefaultFootItem());//默认是这种
//        mRecyclerViewWithFooter.setFootItem(new CustomFootItem());//自定义
        rvLoadMore.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                rvLoadMore.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addData();
                    }
                }, 1000);
            }
        });
    }

    protected void addData() {
        count++;
        if (count == 4) {
            rvLoadMore.setEnd("没有更多数据了");
        }
        for (int i = 0; i < 20; i++) {
            mData.add(new PhotoBean(getURL(i)));
        }
        rvLoadMore.getAdapter().notifyDataSetChanged();
    }

    protected void refreshData() {
        count = 0;
        mData.clear();
        for (int i = 0; i < 20; i++) {

            mData.add(new PhotoBean(getURL(i)));
        }
        rvLoadMore.getAdapter().notifyDataSetChanged();
    }

    String getURL() {
        Random random = new Random();
        String url = imgs[random.nextInt(imgs.length) % imgs.length];
        return url;
    }

    String getURL(int index) {
       /* index = (index/(20/imgs.length));
        String url = imgs[index%imgs.length];*/
        return getURL();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
