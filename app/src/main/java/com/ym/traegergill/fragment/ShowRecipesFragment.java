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
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.ItemsDetailActivity;
import com.ym.traegergill.activity.RecipesDetailActivity;
import com.ym.traegergill.adapter.ShowRecipesRvAdapter;
import com.ym.traegergill.bean.PhotoBean;
import com.ym.traegergill.bean.RecipesBean;
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

public class ShowRecipesFragment extends BaseFragment {
    @BindView(R.id.rv_load_more)
    RecyclerViewWithFooter rvLoadMore;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    Unbinder unbinder;
    private LoadingView loading;
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    String[] imgs;
    List<RecipesBean> mData;
    ShowRecipesRvAdapter adapter;
    int count = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_recipes, container, false);
        imgs = getResources().getStringArray(R.array.user_photos);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    /**
     * 在fragment首次可见时回调，可在这里进行加载数据，保证只在第一次打开Fragment时才会加载数据，
     * 这样就可以防止每次进入都重复加载数据
     * 该方法会在 onFragmentVisibleChange() 之前调用，所以第一次打开时，可以用一个全局变量表示数据下载状态，
     * 然后在该方法内将状态设置为下载状态，接着去执行下载的任务
     * 最后在 onFragmentVisibleChange() 里根据数据下载状态来控制下载进度ui控件的显示与隐藏
     */
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
        for (int i = 0; i < 10; i++) {
            RecipesBean bean = new RecipesBean();
            bean.setImageUrl(getURL());
            bean.setIngredientNum(i+10);
            bean.setTime(i*0.5+1);
            bean.setDesInImg("\"It was delicious,\" the players said.");
            bean.setDesc("To express gratitude, the farmer recently made a sausage from the pig, which was raised, and gave the firefighters a BBQ. ");
            mData.add(bean);
        }
        rvLoadMore.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ShowRecipesRvAdapter(getActivity(), mData);

        adapter.setOnMyItemClickListener(new ShowRecipesRvAdapter.OnMyItemClickListener() {
            @Override
            public void onNormalClick(View v, int position) {
                getActivity().startActivity(
                        new Intent(getActivity(), RecipesDetailActivity.class).putExtra("data",mData.get(position).getImageUrl()),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity)getActivity(),
                                Pair.create(v.findViewById(R.id.img), getActivity().getString(R.string.iv_img_transitionName))
                        ).toBundle()
                );
            }
        });
        rvLoadMore.setAdapter(adapter);
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
        if(count == 4){
            rvLoadMore.setEnd("没有更多数据了");
        }
        for (int i = 0; i < 10; i++) {
            RecipesBean bean = new RecipesBean();
            bean.setImageUrl(getURL());
            bean.setIngredientNum(i+10);
            bean.setTime(i*0.5+1);
            bean.setDesInImg("\"It was delicious,\" the players said.");
            bean.setDesc("To express gratitude, the farmer recently made a sausage from the pig, which was raised, and gave the firefighters a BBQ. ");
            mData.add(bean);
        }
        rvLoadMore.getAdapter().notifyDataSetChanged();
    }

    protected void refreshData() {
        count = 0;
        mData.clear();
        for (int i = 0; i < 10; i++) {
            RecipesBean bean = new RecipesBean();
            bean.setImageUrl(getURL());
            bean.setIngredientNum(i+10);
            bean.setTime(i*0.5+1);
            bean.setDesInImg("\"It was delicious,\" the players said.");
            bean.setDesc("To express gratitude, the farmer recently made a sausage from the pig, which was raised, and gave the firefighters a BBQ. ");
            mData.add(bean);
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
