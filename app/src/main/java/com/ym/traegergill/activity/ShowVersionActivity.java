package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.ym.traegergill.R;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/11/30.
 */

public class ShowVersionActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.ll_check)
    LinearLayout llCheck;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_version);
        unbinder = ButterKnife.bind(this);
        init();

    }

    private void init() {
        title.setText("VERSION");
        String nowVer = getVersion();
        tvVersion.setText("V "+nowVer);
    }

    @OnClick(R.id.ll_check)
    public void onViewClicked() {
        netAppVersion();
    }


    private void netAppVersion() {
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("isFromCache : " + isFromCache+"  json : " + s);
                if(isFromCache){
                    showToastError(getString(R.string.network_error));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(s);
                    TLog(obj.optString("msg"));
                    if (obj.optInt("code") == 200) {
                        String content = obj.optString("content");
                        String nowVer = getVersion();
                        if(content.equals(nowVer)){
                            //=====test=====
                            DialogUtil.simpleConfirmDialog(getActivity(), getString(R.string.update_no_new_version), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            //=====test=====
                        }else{
                            DialogUtil.simpleConfirmDialog(getActivity(), getString(R.string.update_ready_download_title)+ " V " + content, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                           /*DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.update_ready_download_title)
                                   , getActivity().getString(R.string.Confirm), getActivity().getString(R.string.action_close), null, new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           switch (which) {
                                               case DialogInterface.BUTTON_POSITIVE:
                                                   break;
                                               case DialogInterface.BUTTON_NEGATIVE:
                                                   break;
                                           }
                                       }
                                   }).show();*/
                        }
                    }else{
                        showToastError(obj.optString("msg"));
                        TLog(obj.optString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onAfter(boolean isFromCache, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                super.onAfter(isFromCache, s, call, response, e);
                ProgressUtil.hideLoading();
            }
        };
        HttpParams httpParams = new HttpParams();
        if(!MyNetTool.netHttpParams(getActivity(), URLs.getAppVersion,callback,httpParams)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netAppVersion();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }

    }

    public String getVersion() {
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }
}
