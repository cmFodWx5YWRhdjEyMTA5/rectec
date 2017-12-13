package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.tuya.presenter.ECBindPresenter;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ToastUtil;
import com.ym.traegergill.tuya.view.IECBindView;
import com.ym.traegergill.view.circleprogress.CircleProgressView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/10/9.
 */

public class TryConnectDeviceActivity extends BaseActivity implements IECBindView {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.progressNum)
    TextView progressNum;
    @BindView(R.id.step1)
    LinearLayout step1;
    @BindView(R.id.step2)
    LinearLayout step2;
    @BindView(R.id.step3)
    LinearLayout step3;
    String ssid, password;
    @BindView(R.id.circleView)
    CircleProgressView circleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_connect_device);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText(getString(R.string.ty_ez_connecting_device_title));
        ssid = getIntent().getStringExtra(SetUpWifiDeviceActivity.CONFIG_SSID);
        password = getIntent().getStringExtra(SetUpWifiDeviceActivity.CONFIG_PASSWORD);
        initPresenter();
        initC();
    }

    private void initC() {
        circleView.setValueInterpolator(new LinearInterpolator());
        circleView.setOnProgressChangedListener(new CircleProgressView.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float value) {
                progressNum.setText((int)value + "%");
            }
        });

    }

    ECBindPresenter mECBindPresenter;

    private void initPresenter() {
        mECBindPresenter = new ECBindPresenter(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mECBindPresenter.onDestroy();
    }


    @Override
    public void showSuccessPage() {
        OUtil.TLog("====showSuccessPage====");
        getActivity().startActivity(new Intent(getActivity(),AddDeviceSuccessActivity.class).putExtra(AddDeviceSuccessActivity.SUCCESS_DEVICE_NAME,devicesName).putExtra(AddDeviceSuccessActivity.SUCCESS_DEVICE_ID,devicesId));
        isFinished = true;
        this.finish();
        overridePendingTransition(this,ANIMATE_FORWARD);


    }

    @Override
    public void showFailurePage() {
        OUtil.TLog("====showFailurePage====");
        getActivity().startActivity(new Intent(getActivity(),AddDeviceErrorActivity.class).putExtra(AddDeviceErrorActivity.ERRORTEXT,getString(R.string.ty_ap_error_description)));
        isFinished = true;
        this.finish();
        overridePendingTransition(this,ANIMATE_FORWARD);


    }

    @Override
    public void showConnectPage() {
        OUtil.TLog("====showConnectPage====");
    }

    @Override
    public void setConnectProgress(float progress, int animationDuration) {
        OUtil.TLog(progress + " // " + animationDuration);
        circleView.setValueAnimated(progress, animationDuration);

    }
    private boolean isFinished = false;
    @Override
    public void showNetWorkFailurePage() {
        OUtil.TLog("====showNetWorkFailurePage====");
        getActivity().startActivity(new Intent(getActivity(),AddDeviceErrorActivity.class).putExtra(AddDeviceErrorActivity.ERRORTEXT,getString(R.string.network_time_out)));
        isFinished = true;
        this.finish();
        overridePendingTransition(this,ANIMATE_FORWARD);

    }

    @Override
    public void showBindDeviceSuccessTip() {
        OUtil.TLog("====showBindDeviceSuccessTip====");
        step2.getChildAt(0).setBackground(getResources().getDrawable(R.drawable.chense_yuanxing));
        TextView textview = (TextView) step2.getChildAt(1);
        textview.setTextColor(getResources().getColor(R.color.orange_light));
    }

    @Override
    public void showDeviceFindTip(String gwId) {
        OUtil.TLog("====showDeviceFindTip====");
        step1.getChildAt(0).setBackground(getResources().getDrawable(R.drawable.chense_yuanxing));
        TextView textview = (TextView) step1.getChildAt(1);
        textview.setTextColor(getResources().getColor(R.color.orange_light));
        devicesId = gwId;
    }

    @Override
    public void showConfigSuccessTip() {
        OUtil.TLog("====showConfigSuccessTip====");
        step3.getChildAt(0).setBackground(getResources().getDrawable(R.drawable.chense_yuanxing));
        TextView textview = (TextView) step3.getChildAt(1);
        textview.setTextColor(getResources().getColor(R.color.orange_light));
    }

    @Override
    public void showBindDeviceSuccessFinalTip() {
        OUtil.TLog("====showBindDeviceSuccessFinalTip====");

    }
    private String devicesName;
    private String devicesId;
    @Override
    public void setAddDeviceName(String name) {
        OUtil.TLog("====setAddDeviceName====");
        devicesName = name;
        OUtil.TLog("name  : " + name );
    }

    @Override
    public void showMainPage() {
        OUtil.TLog("==showMainPage==");
    }

    @Override
    public void hideMainPage() {
        OUtil.TLog("==hideMainPage==");
    }

    @Override
    public void showSubPage() {
        OUtil.TLog("==showSubPage==");
    }

    @Override
    public void hideSubPage() {
        OUtil.TLog("==hideSubPage==");
    }

    @Override
    public boolean needLogin() {
        return true;
    }

    @Override
    public void finish() {
        if(isFinished){
            super.finish();
            return;
        }
        DialogUtil.customDialog(getActivity(), null, getString(R.string.In_connection_confirm_exit)
                 , getString(R.string.Confirm), getString(R.string.cancel), null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                finish(true);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:

                                break;
                        }
                    }
                }).show();

    }
    public void finish(boolean sure) {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }
}
