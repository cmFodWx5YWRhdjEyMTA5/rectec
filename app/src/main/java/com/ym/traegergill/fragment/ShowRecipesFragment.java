package com.ym.traegergill.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.sdk.TuyaUser;
import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.CreateAccountActivity;
import com.ym.traegergill.activity.RecipesDetailActivity;
import com.ym.traegergill.activity.SignInActivity;
import com.ym.traegergill.adapter.ShowRecipesRvAdapter;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.modelBean.Recipe;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.SharedPreferencesUtils;
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
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/9/18.
 */

@SuppressLint("ValidFragment")
public class ShowRecipesFragment extends BaseFragment {
    @BindView(R.id.rv_load_more)
    RecyclerViewWithFooter rvLoadMore;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    Unbinder unbinder;
    List<Recipe> mData;
    ShowRecipesRvAdapter adapter;
    int page = 0;
    int maxPage = 1;
    Gson gson;
    SharedPreferencesUtils spUtils;
    public int filterid;
    @BindView(R.id.sign_in)
    TextView signIn;
    @BindView(R.id.create_account)
    TextView createAccount;
    @BindView(R.id.cover)
    LinearLayout cover;

    public ShowRecipesFragment(int filterid) {
        this.filterid = filterid;
    }

    private UpdateUserStatusReceiver updateUserStatusReceiver;

    @OnClick({R.id.sign_in, R.id.create_account})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sign_in:
                startActivity(new Intent(getActivity(), SignInActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_bottom_to_top, R.anim.slide_none_medium_time);
                break;
            case R.id.create_account:
                startActivity(new Intent(getActivity(), CreateAccountActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_bottom_to_top, R.anim.slide_none_medium_time);
                break;
        }
    }

    class UpdateUserStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS)) {
                if (filterid == Constants.MAIN_INGREDIENT_FAVORITES) {
                    showCollection(spUtils.getBoolean(Constants.ISLOGIN, false));
                }
            }
        }
    }

    private void showCollection(boolean isLogin) {
        if (isLogin) {
            cover.setVisibility(View.GONE);
        } else {
            cover.setVisibility(View.VISIBLE);
        }
        showData(isLogin);
    }

    private void showData(boolean flag) {
        if (flag) {
            page = 0;
            initData(false);
            rvLoadMore.setLoading();
        } else {
            mData.clear();
            rvLoadMore.setEnd("");
            adapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_recipes, container, false);
        unbinder = ButterKnife.bind(this, view);
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
        return view;
    }


    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        TLog("filterid :" + filterid);
        gson = new Gson();
        initRecycler();
        //注册广播
        if (filterid == Constants.MAIN_INGREDIENT_FAVORITES) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
            updateUserStatusReceiver = new UpdateUserStatusReceiver();
            getActivity().registerReceiver(updateUserStatusReceiver, intentFilter);
        }

        if (!spUtils.getBoolean(Constants.ISLOGIN, false) && filterid == Constants.MAIN_INGREDIENT_FAVORITES) {
            showCollection(false);
        } else {
            showCollection(true);
            ProgressUtil.showLoading(getActivity(), getString(R.string.loading));
            new Handler().postDelayed(new Runnable() {//定义延时任务模仿网络请求
                @Override
                public void run() {
                    //loading.dismiss();//3秒后调用关闭加载的方法
                    ProgressUtil.hideLoading();
                }
            }, 1000);
        }

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
                        initData(true);
                    }
                }, 1000);
            }
        });
    }

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
                TLog("isFromCache : " + isFromCache + " json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONObject content = obj.optJSONObject("content");
                        page = content.optInt("page");
                        maxPage = content.optInt("maxPage");
                        JSONArray array = content.optJSONArray("data");
                        List<Recipe> beans = new ArrayList<Recipe>();
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                Recipe recipe = gson.fromJson(array.optJSONObject(i).toString(), Recipe.class);
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
                        if (rvLoadMore != null && rvLoadMore.getAdapter() != null)
                            rvLoadMore.getAdapter().notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(boolean isFromCache, Call call, @Nullable Response response, @Nullable Exception e) {
                super.onError(isFromCache, call, response, e);
                OUtil.TLog("isFromCache : " + isFromCache + " Exception : " + gson.toJson(e));
            }
        };
        HttpParams params = new HttpParams();
        params.put("page", (page + 1) + "");
        String url = "";
        if (filterid == Constants.MAIN_INGREDIENT_ALL) {
            url = URLs.findRecipeAll;
        } else if (filterid == Constants.MAIN_INGREDIENT_FAVORITES) {
            url = URLs.findUserRecipeCollection;
        } else {
            url = URLs.findRecipesInCondition;
            List<HashMap<String, Integer>> filteridList = new ArrayList<>();
            HashMap map = new HashMap<String, Integer>();
            map.put("filterid", filterid);
            filteridList.add(map);
            String recipeFilterListStr = gson.toJson(filteridList);
            params.put("recipeFilterListStr", recipeFilterListStr);
        }
        if (filterid != Constants.MAIN_INGREDIENT_FAVORITES && !MyNetTool.netHttpParams(getActivity(), url, callback, params)) {
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
        } else if (filterid == Constants.MAIN_INGREDIENT_FAVORITES) {
            if (!MyNetTool.netCross(getActivity(), TuyaUser.getUserInstance().getUser().getUid(), url, callback)) {
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
}
