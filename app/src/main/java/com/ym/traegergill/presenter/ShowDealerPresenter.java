package com.ym.traegergill.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.MapsActivity;
import com.ym.traegergill.bean.DealerBean;
import com.ym.traegergill.fragment.ShowDealerFragment;
import com.ym.traegergill.iview.IShowDealerView;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/11/28.
 */

public class ShowDealerPresenter {
    Context context;
    IShowDealerView mView;
    List<DealerBean> mData;
    ShowDealerFragment fragment;
    public ShowDealerPresenter(ShowDealerFragment fragment, IShowDealerView view) {
        mView = view;
        this.fragment = fragment;
        this.context = fragment.getActivity();
        mData = new ArrayList<>();
    }
    public void getDataFromServer(boolean isMore) {
        initData(isMore);
    }

    public void callClick(DealerBean dealerBean) {
        OUtil.call(context,dealerBean.getTel());
    }
    public void locationClick(DealerBean dealerBean) {
        context.startActivity(new Intent(context, MapsActivity.class).putExtra("data",dealerBean));
    }
    private void initData(final boolean isMore) {
        ProgressUtil.showLoading(context, context.getString(R.string.loading));
        new Handler().postDelayed(new Runnable() {//定义延时任务模仿网络请求
            @Override
            public void run() {
                ProgressUtil.hideLoading();
            }
        }, 1000);

        HttpParams params = new HttpParams();
        params.put("distributorTypeid", fragment.getDistributorTypeid() + "");
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                OUtil.TLog("isFromCache : "+isFromCache+" json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONArray content = obj.optJSONArray("content");
                        setValue(content);

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
        if(! MyNetTool.netHttpParams(context, URLs.findDistributorByType,callback,params)){
            fragment.showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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

    private void setValue(JSONArray content) throws JSONException {
        mData.clear();
        for(int i = 0 ;i<content.length();i++){
            JSONObject obj = content.optJSONObject(i);
            DealerBean bean = new DealerBean();
            bean.setName(obj.optString("name"));
            bean.setTel(obj.optString("contact"));
            bean.setAddress(obj.optString("addr"));
            String tempPos = obj.optString("pos");
            JSONObject posObj = new JSONObject(tempPos);
            /*String[] sTemp = tempPos.split(",");*/
            bean.setLat(posObj.optDouble("lat"));
            bean.setLon(posObj.optDouble("lng"));
            mData.add(bean);
        }
        mView.updateDeviceDataEnd();
        mView.updateDeviceData(mData);
    }

}
