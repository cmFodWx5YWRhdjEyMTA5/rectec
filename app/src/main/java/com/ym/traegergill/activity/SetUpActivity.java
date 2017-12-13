package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okhttputils.callback.StringCallback;
import com.tuya.smart.android.user.api.ILogoutCallback;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.SharedPreferencesUtils;
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
 * Created by Administrator on 2017/9/28.
 */

public class SetUpActivity extends BaseActivity {
    private SharedPreferencesUtils spUtils;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.ll_name)
    LinearLayout llName;
    @BindView(R.id.ll_password)
    LinearLayout llPassword;
    @BindView(R.id.ll_sign)
    LinearLayout llSign;

    @Override
    protected void onResume() {
        super.onResume();
        if(!SharedPreferencesUtils.getSharedPreferencesUtil(this).getBoolean(Constants.ISLOGIN,false)){
            this.finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        unbinder = ButterKnife.bind(this);
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
        title.setText("SET UP");
    }

    @OnClick({R.id.ll_name, R.id.ll_password, R.id.ll_sign})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_name:
                startActivity(new Intent(getActivity(),EditNameActivity.class));
                break;
            case R.id.ll_password:
                startActivity(new Intent(getActivity(),EditPasswordActivity.class));
                break;
            case R.id.ll_sign:
                ProgressUtil.showLoading(getActivity(),R.string.ty_logout_loading);
                netQuit();
                break;
        }
    }

    private void netQuit() {
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
                        TuyaUser.getUserInstance().logout(new ILogoutCallback() {
                            @Override
                            public void onSuccess() {
                                TuyaUser.getDeviceInstance().onDestroy();
                                spUtils.setValue(Constants.ISLOGIN,false);
                                spUtils.remove(Constants.FIRST_NAME);
                                spUtils.remove(Constants.LAST_NAME);
                                Intent intent = new Intent(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
                                getApplicationContext().sendBroadcast(intent);
                                getActivity().finish();
                            }

                            @Override
                            public void onError(String s, String s1) {
                                showToastError(s1);
                            }
                        });
                    }else{
                        showToastError(obj.optString("msg"));
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
        if(!MyNetTool.netCross(getActivity(),TuyaUser.getUserInstance().getUser().getUid(), URLs.quit,callback)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netQuit();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }
    @Override
    public boolean needLogin() {
        return true;
    }
}
