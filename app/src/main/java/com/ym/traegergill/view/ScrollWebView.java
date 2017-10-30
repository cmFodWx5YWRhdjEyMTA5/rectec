package com.ym.traegergill.view;

/**
 * Created by Administrator on 2017/9/29.
 */
import android.content.Context;
import android.util.AttributeSet;

import java.util.Map;

/**
 * webview控件重写，解决setOnScrollChangeListener在api<23的时候不兼容问题
 * Created by TZG on 2016/11/17.
 */
public class ScrollWebView extends com.tencent.smtt.sdk.WebView{

    public interface ScrollViewListener {
        void onScrollChanged(com.tencent.smtt.sdk.WebView scrollWebView, int x, int y, int oldx, int oldy);

    }

    private ScrollViewListener scrollViewListener = null;

    public ScrollWebView(Context context) {
        super(context);
    }

    public ScrollWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ScrollWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public ScrollWebView(Context context, AttributeSet attributeSet, int i, boolean b) {
        super(context, attributeSet, i, b);
    }

    public ScrollWebView(Context context, AttributeSet attributeSet, int i, Map<String, Object> map, boolean b) {
        super(context, attributeSet, i, map, b);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

}