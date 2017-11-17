package com.ym.traegergill.tools;

import android.content.Context;


import com.google.gson.Gson;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cache.CacheMode;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.ym.traegergill.net.URLs;

/**
 * Created by Administrator on 2017/11/7.
 */

public class MyNetTool {
    public static boolean netCross(Context context, String uid, String doubleLink, StringCallback callback) {
        String url = URLs.testCross + "?tuyaUid=" + uid + "&doubleLink=" + doubleLink;
        OUtil.TLog("net url : " + url);
        if (OUtil.isNetworkConnected(context)) {
            OkHttpUtils.post(url)//
                    .tag(context)//
                    .cacheKey(doubleLink)//
                    .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)//
                    .execute(callback);
            return true;
        } else {
            return false;
        }
    }

    public static boolean netCrossWithParams(Context context, String uid, String doubleLink, String params, StringCallback callback) {
        HttpParams httpParams = new HttpParams();
        httpParams.put("param", params);
        String url = URLs.testCross + "?tuyaUid=" + uid + "&doubleLink=" + doubleLink;
        OUtil.TLog("net url : " + url + " params : " + params);
        if (OUtil.isNetworkConnected(context)) {
            OkHttpUtils.post(url)//
                    .params(httpParams)
                    .tag(context)//
                    .cacheKey(doubleLink)//
                    .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)//
                    .execute(callback);
            return true;
        } else {
            return false;
        }


    }

    public static boolean netHttpParams(Context context, String url, StringCallback callback, HttpParams httpParams) {
        OUtil.TLog("net url : " + url + " params : " + new Gson().toJson(httpParams));
        if (OUtil.isNetworkConnected(context)) {
            OkHttpUtils.post(url)//
                    .params(httpParams)
                    .tag(context)//
                    .cacheKey(url)//
                    .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)//
                    .execute(callback);
            return true;
        } else {
            return false;
        }

    }
}
