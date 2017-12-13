package com.ym.traegergill.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.adapter.ShowRecipesRvAdapter;
import com.ym.traegergill.modelBean.Recipe;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;
import com.ym.traegergill.view.loadmore.DefaultFootItem;
import com.ym.traegergill.view.loadmore.OnLoadMoreListener;
import com.ym.traegergill.view.loadmore.RecyclerViewWithFooter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/11/9.
 */

public class FilterRecipesActivity extends BaseActivity {
    String recipeFilterListStr,searchContent;
    boolean isBySearchContent;
    Gson gson;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.rv_load_more)
    RecyclerViewWithFooter rvLoadMore;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    List<Recipe> mData;
    ShowRecipesRvAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_recipes);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText("Recipes");
        ProgressUtil.showLoading(getActivity(), getString(R.string.loading));
        new Handler().postDelayed(new Runnable() {//定义延时任务模仿网络请求
            @Override
            public void run() {
                ProgressUtil.hideLoading();
            }
        }, 1000);
        gson = new Gson();
        initRecycler();
        page = 0;
        isBySearchContent = getIntent().getBooleanExtra("isBySearchContent",false);
        if(isBySearchContent){
            searchContent = getIntent().getStringExtra("searchContent");
            initDataByStr(false);
        }else{
            recipeFilterListStr = getIntent().getStringExtra("recipeFilterListStr");
            initData(false);
        }
    }

    private void initRecycler() {
        pullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rvLoadMore.setLoading();
                        page = 0;
                        if(isBySearchContent){
                            initDataByStr(false);
                        }else{
                            initData(false);
                        }

                        pullToRefresh.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mData = new ArrayList<>();
        rvLoadMore.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ShowRecipesRvAdapter(getActivity(), mData);

        adapter.setOnMyItemClickListener(new ShowRecipesRvAdapter.OnMyItemClickListener() {
            @Override
            public void onNormalClick(View v, int position) {
                getActivity().startActivity(
                        new Intent(getActivity(), RecipesDetailActivity.class).putExtra("recipeid", mData.get(position).getRecipeid()),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity) getActivity(),
                                Pair.create(v.findViewById(R.id.img), getActivity().getString(R.string.iv_img_transitionName))
                        ).toBundle()
                );
            }
        });
        rvLoadMore.setAdapter(adapter);
        rvLoadMore.setFootItem(new DefaultFootItem());//默认是这种
        rvLoadMore.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                rvLoadMore.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isBySearchContent){
                            initDataByStr(true);
                        }else{
                            initData(true);
                        }
                    }
                }, 1000);
            }
        });
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
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONObject content = obj.optJSONObject("content");
                        page = content.optInt("page");
                        maxPage = content.optInt("maxPage");
                        JSONArray array = content.optJSONArray("data");
                        List<Recipe> beans = new ArrayList<Recipe>();
                        if(array != null){
                            for (int i = 0; i < array.length(); i++) {
                                Recipe recipe = gson.fromJson(array.optJSONObject(i).toString(),Recipe.class);
                                beans.add(recipe);
                            }
                        }
                        if (!isMore) {
                            mData.clear();
                        }
                        mData.addAll(beans);
                        if (array == null || array.length() == 0 || page == maxPage) {
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


        HttpParams params = new HttpParams();
        params.put("recipeFilterListStr", recipeFilterListStr);
        params.put("page", (page + 1) + "");
        if(!MyNetTool.netHttpParams(getActivity(),URLs.findRecipesInCondition,callback,params)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            initData(isMore);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }

    }

    private void initDataByStr(final boolean isMore) {
        if (!isMore) {
            ProgressUtil.showLoading(getActivity(), getString(R.string.loading));
            new Handler().postDelayed(new Runnable() {//定义延时任务模仿网络请求
                @Override
                public void run() {
                    ProgressUtil.hideLoading();
                }
            }, 1000);
        }
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONObject content = obj.optJSONObject("content");
                        page = content.optInt("page");
                        maxPage = content.optInt("maxPage");
                        JSONArray array = content.optJSONArray("data");
                        List<Recipe> beans = new ArrayList<Recipe>();
                        if(array != null){
                            for (int i = 0; i < array.length(); i++) {
                                Recipe recipe = gson.fromJson(array.optJSONObject(i).toString(),Recipe.class);
                                beans.add(recipe);
                            }
                        }
                        if (!isMore) {
                            mData.clear();
                        }
                        mData.addAll(beans);
                        if (array == null || array.length() == 0 || page == maxPage) {
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


        HttpParams params = new HttpParams();
        params.put("title", searchContent);
        params.put("page", (page + 1) + "");
        if(!MyNetTool.netHttpParams(getActivity(),URLs.retrieRecipeByRecipeName,callback,params)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            initData(isMore);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(getActivity(),ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }
}
