package com.ym.traegergill.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuya.smart.android.user.api.ILogoutCallback;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.SharedPreferencesUtils;
import com.ym.traegergill.tuya.utils.ProgressUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        ButterKnife.bind(this);
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
        title.setText("SET UP");
    }

    @OnClick({R.id.ll_name, R.id.ll_password, R.id.ll_sign})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_name:
                startActivity(new Intent(getActivity(),EditNameActivity.class).putExtra("firstName","Lou").putExtra("lastName","Charlot"));
                break;
            case R.id.ll_password:
                startActivity(new Intent(getActivity(),EditPasswordActivity.class));
                break;
            case R.id.ll_sign:
                ProgressUtil.showLoading(getActivity(),R.string.ty_logout_loading);

                TuyaUser.getUserInstance().logout(new ILogoutCallback() {
                    @Override
                    public void onSuccess() {
                        ProgressUtil.hideLoading();
                        TuyaUser.getDeviceInstance().onDestroy();
                        spUtils.setValue(Constants.ISLOGIN,false);
                        Intent intent = new Intent(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
                        getApplicationContext().sendBroadcast(intent);
                        getActivity().finish();
                    }

                    @Override
                    public void onError(String s, String s1) {
                        showToastSuccess(s1);
                        ProgressUtil.hideLoading();
                    }
                });
                break;
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
