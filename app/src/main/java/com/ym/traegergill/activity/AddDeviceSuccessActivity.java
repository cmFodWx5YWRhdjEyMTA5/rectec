package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.bean.TagBean;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.db.bean.UserData;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;
import com.ym.traegergill.view.ChartUnitPickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/19.
 */

public class AddDeviceSuccessActivity extends BaseActivity {
    public static String SUCCESS_DEVICE_NAME = "success_device_name";
    public static String SUCCESS_DEVICE_ID = "success_device_id";
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.skip)
    TextView skip;
    @BindView(R.id.et_value)
    EditText etValue;
    @BindView(R.id.et_device_model)
    TextView etDeviceModel;
    private TuyaDevice mTuyaDevice;
    ChartUnitPickerDialog uintPickerDialog;
    List<TagBean> tempModels;
    List<String> datas;
    int modelIndex = -1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_success);
        unbinder = ButterKnife.bind(this);
        init();
        netAddDevice();
        netModelData();
        initDb();

    }

    private void netAddDevice() {
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
        String params = "deviceInfo="+mTuyaDevice.getDevId();
        if(!MyNetTool.netCrossWithParams(getActivity(),TuyaUser.getUserInstance().getUser().getUid(),URLs.addUserDevice,params,callback)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netAddDevice();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });

        }
    }

    private void netModelData() {
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
                            netModelData();
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
            }
            uintPickerDialog = new ChartUnitPickerDialog(getActivity(), datas, etDeviceModel.getText().toString());
            uintPickerDialog.setTitle("Model Select");
            uintPickerDialog.setBtnColor(R.drawable.orange_btn_bg);
            uintPickerDialog.setCallBackListen(new ChartUnitPickerDialog.callBackListen() {
                @Override
                public void callback(String text) {
                    etDeviceModel.setText(text);
                    modelIndex = datas.indexOf(text);
                }
            });
        }

    }

    private void initDb() {
        SQLiteDbUtil dbUtil = SQLiteDbUtil.getSQLiteDbUtil();
        dbUtil.openOrCreateDataBase(this);
        dbUtil.createTable(UserData.class);
        String devId = getIntent().getStringExtra(SUCCESS_DEVICE_ID);
        String uid = TuyaUser.getUserInstance().getUser().getUid();
        List<UserData> datas = dbUtil.query(UserData.class, "dev_id = ? and user = ?", new String[]{devId, uid});
        //数据库中没有该用户下 该设备的数据
        if (datas == null || datas.size() == 0) {

        } else {
            dbUtil.delete(UserData.class, "dev_id = " + devId + " and user = " + uid);
        }
        UserData data = new UserData();
        data.setUser(uid);
        data.setDev_id(devId);
        long ret = dbUtil.insert(data);
        if (ret == -1) {
            DialogUtil.simpleSmartDialog(getActivity(), "db something error", null);
        } else {
            //DialogUtil.simpleSmartDialog(getActivity(), "新数据插入成功", null);
            TLog("新数据插入成功");
        }
    }

    private void init() {
        title.setText("Add device successfully");
        etValue.setText(getIntent().getStringExtra(SUCCESS_DEVICE_NAME));
        etValue.setSelection(etValue.getText().length());
        mTuyaDevice = new TuyaDevice(getIntent().getStringExtra(SUCCESS_DEVICE_ID));
        initListener();
    }

    private void initListener() {
        etDeviceModel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(TextUtils.isEmpty(editable) || TextUtils.isEmpty(etValue.getText())){
                    skip.setEnabled(false);
                }else{
                    skip.setEnabled(true);
                }
            }
        });
        etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(TextUtils.isEmpty(editable) || TextUtils.isEmpty(etDeviceModel.getText())){
                    skip.setEnabled(false);
                }else{
                    skip.setEnabled(true);
                }
            }
        });
    }

    @OnClick({R.id.skip,R.id.et_device_model})
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.skip:
                if (TextUtils.isEmpty(etValue.getText().toString())) {
                    DialogUtil.simpleSmartDialog(getActivity(), getString(R.string.cannot_input_empty_string), null);
                    return;
                }
                netUpdateModel();
                break;
            case R.id.et_device_model:
                if(uintPickerDialog==null){
                    netModelData();
                }else{
                    uintPickerDialog.show();
                }
                break;

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
                        renameByTuya();
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

    private void renameByTuya() {
        mTuyaDevice.renameDevice(etValue.getText().toString(), new IControlCallback() {
            @Override
            public void onError(String code, String error) {
                //重命名失败
                DialogUtil.simpleSmartDialog(getActivity(), code + " : " + error, null);
            }

            @Override
            public void onSuccess() {
                //重命名成功
                DialogUtil.simpleSmartDialog(getActivity(), getString(R.string.success), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                        overridePendingTransition(getActivity(), ANIMATE_SLIDE_BOTTOM_FROM_TOP);
                    }
                });
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this, ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }


}
