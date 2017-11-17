package com.ym.traegergill.net;

import android.os.Handler;
import android.support.annotation.Nullable;

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.request.BaseRequest;
import com.ym.traegergill.tools.OUtil;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/11/13.
 */

public class MyStringCallback extends StringCallback {

    /**
     * 请求网络开始前，UI线程
     *
     * @param request
     */
    @Override
    public void onBefore(BaseRequest request) {
        super.onBefore(request);
    }

    @Override
    public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {

    }

    /**
     * 请求网络结束后，UI线程
     *
     * @param isFromCache
     * @param s
     * @param call
     * @param response
     * @param e
     */
    @Override
    public void onAfter(boolean isFromCache, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
        super.onAfter(isFromCache, s, call, response, e);
    }

    @Override
    public void onError(boolean isFromCache, Call call, @Nullable Response response, @Nullable Exception e) {
        super.onError(isFromCache, call, response, e);
        OUtil.TLog("isFromCache : " + isFromCache + " Exception : " + e.getMessage());
    }
}
