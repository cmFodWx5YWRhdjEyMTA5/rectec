package com.ym.traegergill.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
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

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.ItemsDetailActivity;
import com.ym.traegergill.adapter.ShowItemsRvAdapter;
import com.ym.traegergill.modelBean.RecipeShare;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.other.SpaceItemDecoration;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;
import com.ym.traegergill.view.LoadingView;
import com.ym.traegergill.view.loadmore.DefaultFootItem;
import com.ym.traegergill.view.loadmore.OnLoadMoreListener;
import com.ym.traegergill.view.loadmore.RecyclerViewWithFooter;

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

@SuppressLint("ValidFragment")
public class ShowItemsFragment extends BaseFragment {
    @BindView(R.id.rv_load_more)
    RecyclerViewWithFooter rvLoadMore;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    Unbinder unbinder;
    private LoadingView loading;
    String[] imgs;
    List<RecipeShare> mData;
    ShowItemsRvAdapter adapter;
    private int sharePlatformid;

    @SuppressLint("ValidFragment")
    public ShowItemsFragment(int sharePlatformid) {
        this.sharePlatformid = sharePlatformid;
    }

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
     /*   loading = new LoadingView(getActivity(), R.style.CustomDialog);
        loading.show();*/
        initRecycler();
    }

    private void initRecycler() {
        pullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        page = 0;
                        rvLoadMore.setLoading();
                        initData(false);
                        pullToRefresh.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mData = new ArrayList<>();
        rvLoadMore.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new ShowItemsRvAdapter(getActivity(), mData);
        adapter.setOnMyItemClickListener(new ShowItemsRvAdapter.OnMyItemClickListener() {
            @Override
            public void onNormalClick(View v, int position) {
                getActivity().startActivity(
                        new Intent(getActivity(), ItemsDetailActivity.class).putExtra("recipeShareid", mData.get(position).getRecipeShareid()),
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
                        initData(true);
                    }
                }, 1000);
            }
        });
        page = 0;
        initData(false);
    }

    int page = 0;
    int maxPage = 1;

    private void initData(final boolean isMore) {
        if (!isMore) {
            ProgressUtil.showLoading(getActivity(), getString(R.string.loading));
            new Handler().postDelayed(new Runnable() {//定义延时任务模仿网络请求
                @Override
                public void run() {
                    ProgressUtil.hideLoading();
                }
            }, 1000);
        }
        HttpParams params = new HttpParams();
        params.put("sharePlatformid", sharePlatformid + "");
        params.put("page", (page + 1) + "");
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("isFromCache : "+isFromCache+" json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONObject content = obj.optJSONObject("content");
                        page = content.optInt("page");
                        maxPage = content.optInt("maxPage");
                        JSONArray array = content.optJSONArray("data");
                        List<RecipeShare> beans = new ArrayList<RecipeShare>();
                        if(array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                RecipeShare share = new RecipeShare();
                                share.setShareMainPic(array.optJSONObject(i).optString("shareMainPic"));
                                share.setRecipeShareid(array.optJSONObject(i).optInt("recipeShareid"));
                                beans.add(share);
                            }
                        }
                        if (!isMore) {
                            mData.clear();
                        }
                        mData.addAll(beans);
                        if (array == null || array.length() == 0 || maxPage == page) {
                            rvLoadMore.setEnd(getResources().getString(R.string.rv_with_footer_empty));
                        }
                        rvLoadMore.getAdapter().notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAfter(boolean isFromCache, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                super.onAfter(isFromCache, s, call, response, e);
            }
        };
        if(! MyNetTool.netHttpParams(getActivity(),URLs.findRecipeShareBySharePlatformid,callback,params)){
            DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                    , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    System.exit(0);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    initData(isMore);
                                    break;
                            }
                        }
                    }).show();
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
