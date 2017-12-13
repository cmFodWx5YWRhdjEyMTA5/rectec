package com.ym.traegergill.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.tools.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2017/9/27.
 */

public class H5RecipesFragment extends BaseFragment {

    Unbinder unbinder;

    String url = Constants.RECIPES_URL;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    @BindView(R.id.webView)
    WebView webview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_h5_shop, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        title.setText("RECIPES");
        initRefresh();
        initWebView();
    }

    private void initRefresh() {
        pullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        webview.clearCache(true);
                        webview.loadUrl(url);
                        pullToRefresh.setRefreshing(false);
                    }
                }, 1000);
            }
        });
    }

    private void initWebView() {
        webview.loadUrl(url);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
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
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
      /*  webview.setScrollViewListener(new ScrollWebView.ScrollViewListener() {
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
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != webview) {
            webview.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != webview) {
            webview.onPause();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.back)
    public void onViewClicked() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            showToastError("It's first page,can't back.");
        }

    }
}
