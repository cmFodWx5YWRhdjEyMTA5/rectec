package com.ym.traegergill.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.view.ScrollWebView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2017/9/27.
 */

public class H5ShopFragment extends BaseFragment {

    Unbinder unbinder;
    @BindView(R.id.tbsContent)
    ScrollWebView tbsContent;
    String url = "http://www.rectecgrills.com/";
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_h5_shop, container, false);
        unbinder = ButterKnife.bind(this, view);
        title.setText("SHOP");
        initRefresh();
        initWebView();
        return view;
    }

    private void initRefresh() {
        pullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tbsContent.clearCache(true);
                        tbsContent.loadUrl(url);
                        pullToRefresh.setRefreshing(false);
                    }
                }, 1000);
            }
        });
    }

    private void initWebView() {
        tbsContent.loadUrl(url);
        WebSettings webSettings = tbsContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        tbsContent.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        tbsContent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (progressBar.getVisibility() == View.GONE)
                        progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }

            }

        });

        pullToRefresh.setTag(true);
        tbsContent.setScrollViewListener(new ScrollWebView.ScrollViewListener() {
            @Override
            public void onScrollChanged(WebView scrollWebView, int x, int y, int oldx, int oldy) {
                if (y == 0) {//滑动到顶部
                    if (Boolean.valueOf(pullToRefresh.getTag().toString()) == false) {
                        pullToRefresh.setEnabled(true);
                        pullToRefresh.setTag(true);
                    }
                } else {
                    if (Boolean.valueOf(pullToRefresh.getTag().toString()) == true) {
                        pullToRefresh.setEnabled(false);
                        pullToRefresh.setTag(false);
                    }
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.back)
    public void onViewClicked() {
        if (tbsContent.canGoBack()) {
            tbsContent.goBack();
        } else {
            showToastError("已经是首页了!无法回退");
        }

    }
}
