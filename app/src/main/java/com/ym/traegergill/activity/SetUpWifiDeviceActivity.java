package com.ym.traegergill.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.WifiUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/9.
 */

public class SetUpWifiDeviceActivity extends BaseActivity {

    public static final String CONFIG_MODE = "config_mode";
    public static final String CONFIG_PASSWORD = "config_password";
    public static final String CONFIG_SSID = "config_ssid";
    public static final int AP_MODE = 0;
    public static final int EC_MODE = 1;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.wifi_name)
    TextView wifiName;
    @BindView(R.id.wifi_password)
    EditText wifiPassword;
    @BindView(R.id.show_password)
    CheckBox showPassword;
    @BindView(R.id.next_step)
    TextView nextStep;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.ll_wifi_name)
    LinearLayout llWifiName;
    private UpdateWIFIStatusReceiver updateWIFIStatusReceiver;
    private WifiInfo wifiInfo;


    class UpdateWIFIStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TraegerGillBroadcastHelper.ACTION_TEST_WIFI)) {
                updateWifiInfo();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_wifi_device);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText(getString(R.string.ty_ez_wifi_title));
        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_TEST_WIFI);
        updateWIFIStatusReceiver = new UpdateWIFIStatusReceiver();
        getActivity().registerReceiver(updateWIFIStatusReceiver, intentFilter);
        updateWifiInfo();
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    //如果选中，显示密码
                    wifiPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    wifiPassword.setSelection(wifiPassword.getText().length());
                } else {
                    //否则隐藏密码
                    wifiPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    wifiPassword.setSelection(wifiPassword.getText().length());

                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销广播
        try {
            getActivity().unregisterReceiver(updateWIFIStatusReceiver);
        } catch (Exception e) {

        }
    }

    private void updateWifiInfo() {
        if (WifiUtil.isWifiConnected(getActivity())) {
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(getActivity().WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();
            wifiName.setText(wifiInfo.getSSID().replaceAll("\"", ""));
        } else {
            wifiInfo = null;
            wifiName.setText(getString(R.string.ty_ez_current_no_wifi));
        }
    }

    @OnClick({R.id.next_step,R.id.ll_wifi_name})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.next_step:
                if (wifiInfo == null) {
                    OUtil.toastError(getActivity(), getString(R.string.please_connect_to_wifi));
                } else if (TextUtils.isEmpty(wifiPassword.getText())) {
                    DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.wifi_psw_is_empty)
                            , getActivity().getString(R.string.ez_notSupport_5G_change), getActivity().getString(R.string.ez_notSupport_5G_continue), null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            //startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            goNext();
                                            break;
                                    }
                                }
                            }).show();

                    //OUtil.toastError(getActivity(), getString(R.string.please_input_wifi_password));
                } else if (wifiInfo.getFrequency() / 1000 == 2) {
                    goNext();
                } else {
                    DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.ez_notSupport_5G_tip)
                            , getActivity().getString(R.string.ez_notSupport_5G_change), getActivity().getString(R.string.ez_notSupport_5G_continue), null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            goNext();
                                            break;
                                    }
                                }
                            }).show();
                }


                break;
            case R.id.ll_wifi_name:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
        }
    }


    private void goNext() {
        Intent intent = new Intent(getActivity(), TryConnectDeviceActivity.class);
        intent.putExtra(SetUpWifiDeviceActivity.CONFIG_PASSWORD, wifiPassword.getText().toString());
        intent.putExtra(SetUpWifiDeviceActivity.CONFIG_SSID, wifiName.getText().toString());
        intent.putExtra(SetUpWifiDeviceActivity.CONFIG_MODE, EC_MODE);
        startActivity(intent);
        this.finish();
        overridePendingTransition(this,ANIMATE_FORWARD);

    }
    @Override
    public boolean needLogin() {
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }
}
