package com.ym.traegergill.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.ym.traegergill.R;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.view.EditPickerDialog;
import com.ym.traegergill.view.StringPickerDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/10.
 */

public class SettingGrillActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.ll_alerms)
    LinearLayout llAlerms;
    @BindView(R.id.ll_temp_unit)
    LinearLayout llTempUnit;
    @BindView(R.id.ll_temp_cali)
    LinearLayout llTempCali;
    @BindView(R.id.ll_mini_feed_rate)
    LinearLayout llMiniFeedRate;
    @BindView(R.id.tv_unit)
    TextView tvUnit;
    @BindView(R.id.ll_set_name)
    LinearLayout llSetName;
    StringPickerDialog UnitPickerDialog;
    EditPickerDialog namePickerDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_grill);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private TuyaDevice mTuyaDevice;
    private String mDevId;
    private DeviceBean deviceBean;
    private void init() {
        title.setText("SETTINGS GRILL ONE");
        mDevId = getIntent().getStringExtra(RemoteControlActivity.INTENT_DEVID);
        mTuyaDevice = new TuyaDevice(mDevId);
        deviceBean = TuyaUser.getDeviceInstance().getDev(mDevId);
        Map<String, Object> list = TuyaUser.getDeviceInstance().getDps(mDevId);
        for (Map.Entry<String, Object> entry : list.entrySet()) {
            if (entry.getKey().equals("108")) {
                boolean isC = (boolean) entry.getValue();
                if (isC) {
                    tvUnit.setText(getResources().getString(R.string.CCCC));
                } else {
                    tvUnit.setText(getResources().getString(R.string.FFFF));
                }
                break;
            }
        }
    }

    @OnClick({R.id.ll_alerms, R.id.ll_temp_unit, R.id.ll_temp_cali, R.id.ll_mini_feed_rate,R.id.ll_set_name})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_alerms:
                getActivity().startActivity(new Intent(getActivity(), AlarmsActivity.class).putExtra(RemoteControlActivity.INTENT_DEVID,mDevId));
                break;
            case R.id.ll_temp_unit:
                if (UnitPickerDialog == null) {
                    List<String> datas = new ArrayList<>();
                    datas.add("℃");
                    datas.add("℉");
                    UnitPickerDialog = new StringPickerDialog(getActivity(), datas, tvUnit.getText().toString());
                    UnitPickerDialog.setTitle("Unit Select");
                    UnitPickerDialog.setCallBackListen(new StringPickerDialog.callBackListen() {
                        @Override
                        public void callback(String text) {
                            showToastSuccess("温度不可切换");
                            return;
                            /*if(!text.equals(tvUnit.getText().toString())){
                                tvUnit.setText(text);
                                HashMap<String, Object> stringObjectHashMap = new HashMap<>();
                                if(text.equals(getResources().getString(R.string.CCCC))){
                                    stringObjectHashMap.put("108",true);
                                }else{
                                    stringObjectHashMap.put("108",false);
                                }

                                String commandStr = JSON.toJSONString(stringObjectHashMap);
                                OUtil.TLog("commandStr : " + commandStr);
                                mTuyaDevice.publishDps(commandStr, new IControlCallback() {
                                    @Override
                                    public void onError(String code, String error) {
                                    }
                                    @Override
                                    public void onSuccess() {
                                        showToastSuccess("切换成功");
                                    }
                                });
                            }*/
                        }
                    });
                    UnitPickerDialog.setSelect(tvUnit.getText().toString());
                }
                UnitPickerDialog.show();

                break;
            case R.id.ll_temp_cali:
                getActivity().startActivity(new Intent(getActivity(), TempCalibrationActivity.class).putExtra(RemoteControlActivity.INTENT_DEVID,mDevId));
                break;
            case R.id.ll_mini_feed_rate:
                getActivity().startActivity(new Intent(getActivity(), MiniFeedRateActivity.class).putExtra(RemoteControlActivity.INTENT_DEVID,mDevId));
                break;
            case R.id.ll_set_name:
                if(namePickerDialog == null){
                    namePickerDialog = new EditPickerDialog(getActivity(),deviceBean.getName());
                    namePickerDialog.setTitle("Set The Device Name");
                    namePickerDialog.setCallBackListen(new EditPickerDialog.callBackListen() {
                        @Override
                        public void callback(String text) {
                            llSetName.setEnabled(false);

                            mTuyaDevice.renameDevice(text, new IControlCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    //重命名失败
                                    OUtil.toastSuccess(getActivity(),error);
                                    llSetName.setEnabled(true);
                                }
                                @Override
                                public void onSuccess() {
                                    OUtil.toastSuccess(getActivity(),"success");
                                    deviceBean = TuyaUser.getDeviceInstance().getDev(mDevId);
                                    llSetName.setEnabled(true);
                                }
                            });
                        }
                    });
                }
                namePickerDialog.setText(deviceBean.getName());
                namePickerDialog.show();
                break;
        }
    }

    @Override
    public boolean needLogin() {
        return true;
    }
}
