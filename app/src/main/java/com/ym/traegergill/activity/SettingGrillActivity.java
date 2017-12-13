package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.ym.traegergill.R;
import com.ym.traegergill.bean.TagBean;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;
import com.ym.traegergill.view.ChartUnitPickerDialog;
import com.ym.traegergill.view.EditPickerDialog;
import com.ym.traegergill.view.StringPickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

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
    @BindView(R.id.ll_update_devices_model)
    LinearLayout llUpdateDevicesModel;
    StringPickerDialog uintPickerDialog;
    private TuyaDevice mTuyaDevice;
    private String mDevId;
    private DeviceBean deviceBean;
    List<TagBean> tempModels;
    List<String> datas;
    int modelIndex = -1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_grill);
        unbinder = ButterKnife.bind(this);
        init();
        netFindDeviceInfo();
    }

    private void netModelData(int index) {
        modelIndex = index;
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONArray content = obj.optJSONArray("content");
                        createDialog(content);
                    }else{
                        TLog(obj.optString("msg"));
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
        HttpParams httpParams = new HttpParams();
        if(!MyNetTool.netHttpParams(getActivity(), URLs.findDeviceTypeAll,callback,httpParams)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netModelData(modelIndex);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }

    }
    private void createDialog(JSONArray content) {
        if(uintPickerDialog==null){
            tempModels = new ArrayList<>();
            datas = new ArrayList<>();
            for(int i = 0 ;i<content.length();i++){
                JSONObject obj = content.optJSONObject(i);
                datas.add(obj.optString("name"));
                tempModels.add(new TagBean(obj.optInt("deviceTypeid"),obj.optString("name")));
                if(obj.optInt("deviceTypeid")==modelIndex){
                    modelIndex = i;
                }
            }
            uintPickerDialog = new StringPickerDialog(getActivity(), datas,datas.get(modelIndex));
            uintPickerDialog.setTitle("Model Select");
            //uintPickerDialog.setBtnColor(R.drawable.orange_btn_bg);
            uintPickerDialog.setCallBackListen(new StringPickerDialog.callBackListen() {
                @Override
                public void callback(String text) {
                    //showToastSuccess(text);
                    modelIndex = datas.indexOf(text);
                    netUpdateModel();
                }
            });
        }
    }

    private void netUpdateModel() {
        if(modelIndex==-1){
            showToastError("please select the device model..");
            return;
        }
        ProgressUtil.showLoading(getActivity(),getString(R.string.loading));
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
                        showToastSuccess(obj.optString("msg"));
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
        String params = "deviceInfo="+mTuyaDevice.getDevId()+"&deviceTypeid="+tempModels.get(modelIndex).getId();
        if(!MyNetTool.netCrossWithParams(getActivity(),TuyaUser.getUserInstance().getUser().getUid(),URLs.updateDeviceType,params,callback)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netUpdateModel();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }
    }
    private void init() {
        title.setText("SETTINGS GRILL ONE");

        mDevId = getIntent().getStringExtra(RemoteControlActivity.INTENT_DEVID);
        mTuyaDevice = new TuyaDevice(mDevId);
        deviceBean = TuyaUser.getDeviceInstance().getDev(mDevId);
        title.setText("SETTINGS " + deviceBean.getName());
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

    @OnClick({R.id.ll_alerms, R.id.ll_temp_unit, R.id.ll_temp_cali, R.id.ll_mini_feed_rate, R.id.ll_update_devices_model,R.id.ll_set_name})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_alerms:
                getActivity().startActivity(new Intent(getActivity(), AlarmsActivity.class).putExtra(RemoteControlActivity.INTENT_DEVID, mDevId));
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
                            showToastSuccess("Temperature Unit can't change now!");
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
                getActivity().startActivity(new Intent(getActivity(), TempCalibrationActivity.class).putExtra(RemoteControlActivity.INTENT_DEVID, mDevId));
                break;
            case R.id.ll_mini_feed_rate:
                getActivity().startActivity(new Intent(getActivity(), MiniFeedRateActivity.class).putExtra(RemoteControlActivity.INTENT_DEVID, mDevId));
                break;
            case R.id.ll_update_devices_model:
                if(uintPickerDialog==null){
                    netFindDeviceInfo();
                }else{
                    uintPickerDialog.show();
                }
                break;
            case R.id.ll_set_name:
                if (namePickerDialog == null) {
                    namePickerDialog = new EditPickerDialog(getActivity(), deviceBean.getName());
                    namePickerDialog.setTitle("Set The Device Name");
                    namePickerDialog.setCallBackListen(new EditPickerDialog.callBackListen() {
                        @Override
                        public void callback(String text) {
                            llSetName.setEnabled(false);

                            mTuyaDevice.renameDevice(text, new IControlCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    //重命名失败
                                    OUtil.toastSuccess(getActivity(), error);
                                    llSetName.setEnabled(true);
                                }

                                @Override
                                public void onSuccess() {
                                    OUtil.toastSuccess(getActivity(), "success");
                                    deviceBean = TuyaUser.getDeviceInstance().getDev(mDevId);
                                    title.setText("SETTINGS " + deviceBean.getName());
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

    private void netFindDeviceInfo() {
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
                        JSONObject content = obj.optJSONObject("content");
                        netModelData(content.optInt("deviceTypeid"));
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
            }
        };
        String params = "deviceInfo="+mTuyaDevice.getDevId();
        if(!MyNetTool.netCrossWithParams(getActivity(),TuyaUser.getUserInstance().getUser().getUid(),URLs.addUserDevice,params,callback)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netFindDeviceInfo();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }
    }


    @Override
    public boolean needLogin() {
        return true;
    }



}
