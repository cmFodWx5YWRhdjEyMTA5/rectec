package com.ym.traegergill.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.TyCommonUtil;
import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.tools.CommonUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by mikeshou on 15/6/15.
 */
public class BrowserActivity extends BaseActivity {

    private static final String TAG = "Browser";

    public static final String EXTRA_TITLE = "Title";
    public static final String EXTRA_URI = "Uri";
    public static final String EXTRA_LOGIN = "Login";
    public static final String EXTRA_REFRESH = "Refresh";
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.webView)
    WebView webview;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    Unbinder unbinder;
    /**
     * 是否需要登录
     */
    private boolean needlogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        unbinder = ButterKnife.bind(this);
        init();

    }

    private void init() {
        title.setText(getIntent().getStringExtra(EXTRA_TITLE));
        initRefresh();
        initWebView();
    }
    String url;
    private void initWebView() {
        Intent intent = getIntent();
        url = intent.getStringExtra(EXTRA_URI);
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
        webview.clearCache(true);
        if (TextUtils.isEmpty(url) || !CommonUtil.checkUrl(url)) {
            url = "about:blank";
        }
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept-Language", TyCommonUtil.getLang(getActivity()));
        webview.loadUrl(url, headers);
        pullToRefresh.setTag(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

    }

    private void initRefresh() {
        pullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Accept-Language", TyCommonUtil.getLang(getActivity()));
                        webview.loadUrl(url, headers);
                        pullToRefresh.setRefreshing(false);
                    }
                }, 1000);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        resumeUptime = SystemClock.uptimeMillis();
        if (null != webview) {
            webview.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != webview) {
            webview.onPause();
        }
    }
    private long resumeUptime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isFinishing()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (webview.canGoBack()) {
                    webview.goBack();
                    return true;
                }
            }
        }
        if (!this.isFinishing()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                long eventtime = event.getEventTime();
                if (Math.abs(eventtime - resumeUptime) < 400) {
                    L.d(TAG, "baseactivity onKeyDown after onResume to close, do none");
                    return true;
                }
            }

            if (!(event.getRepeatCount() > 0) && !onPanelKeyDown(keyCode, event)) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    this.finish();
                    return true;
                } else {
                    return super.onKeyDown(keyCode, event);
                }
            } else {
                L.d(TAG, "baseactivity onKeyDown true");
                return true;
            }

        } else {
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        if (null != webview) {
            webview.destroy();
            webview = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            String url = intent.getStringExtra(EXTRA_URI);
            L.d(TAG, "Browser : onNewIntent 2:" + url);
            if (webview != null && url != null) {
                webview.loadUrl(url);
            }
        }
        super.onNewIntent(intent);
    }

    protected boolean onPanelKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (webview.canGoBack()) {
                return true;
            }
        }
        return false;

    }

    @Override
    public void onBackPressed() {
        if (!webview.canGoBack()) {
            super.onBackPressed();
            //ActivityUtils.back(this);
        }
    }




    @Override
    public boolean needLogin() {
        return needlogin;
    }
}
