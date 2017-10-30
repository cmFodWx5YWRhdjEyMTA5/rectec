package com.ym.traegergill.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.tuya.smart.android.device.api.IGetDataPointStatCallback;
import com.tuya.smart.android.device.bean.DataPointStatBean;
import com.tuya.smart.android.device.enums.DataPointTypeEnum;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.api.IDevListener;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.db.bean.UserData;
import com.ym.traegergill.service.TraegerGillService;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.SnackbarUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/10.
 */

public class RemoteControlActivity extends BaseActivity {
    public static final String INTENT_DEVID = "intent_devid";
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.setting)
    ImageView setting;
    @BindView(R.id.left_temp)
    TextView leftTemp;
    @BindView(R.id.left_temp_unit)
    TextView leftTempUnit;
    @BindView(R.id.mid_temp)
    TextView midTemp;
    @BindView(R.id.mid_temp_unit)
    TextView midTempUnit;
    @BindView(R.id.right_top_temp)
    TextView rightTopTemp;
    @BindView(R.id.right_top_temp_unit)
    TextView rightTopTempUnit;
    @BindView(R.id.right_bottom_temp)
    TextView rightBottomTemp;
    @BindView(R.id.right_bottom_temp_unit)
    TextView rightBottomTempUnit;
    @BindView(R.id.top_main)
    LinearLayout topMain;
    @BindView(R.id.bottom_temp)
    TextView bottomTemp;
    @BindView(R.id.bottom_temp_unit)
    TextView bottomTempUnit;
    @BindView(R.id.iv_less)
    ImageView ivLess;
    @BindView(R.id.iv_add)
    ImageView ivAdd;
    @BindView(R.id.btn_on_off)
    CheckBox btnOnOff;
    @BindView(R.id.tv_warn)
    TextView tvWarn;
    @BindView(R.id.cover)
    RelativeLayout cover;
    @BindView(R.id.ll_temp_chart)
    LinearLayout llTempChart;
    private TuyaDevice mTuyaDevice;
    private String mDevId;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_LESS)) {
                OUtil.TLog("ACTION_ALARMS_ACTUAL_LESS");
                SnackbarUtil.IndefiniteSnackbarCancel(title, "Actual Temp Low", SnackbarUtil.Warning).show();
            } else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_MORE)) {
                OUtil.TLog("ACTION_ALARMS_ACTUAL_MORE");
                SnackbarUtil.IndefiniteSnackbarCancel(title, "Actual Temp High", SnackbarUtil.Warning).show();
            } else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_MORE)) {
                OUtil.TLog("ACTION_ALARMS_TEMPA_MORE");
                SnackbarUtil.IndefiniteSnackbarCancel(title, "Probe A Temp High", SnackbarUtil.Warning).show();
            } else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_MORE)) {
                OUtil.TLog("ACTION_ALARMS_TEMPB_MORE");
                SnackbarUtil.IndefiniteSnackbarCancel(title, "Probe B Temp High", SnackbarUtil.Warning).show();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                btnOnOff.setText("ON");
                btnOnOff.setTextColor(getResources().getColor(R.color.orange_light));
            } else {
                btnOnOff.setText("OFF");
                btnOnOff.setTextColor(getResources().getColor(R.color.color9999));
            }

            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put("1", b);
            String commandStr = JSON.toJSONString(stringObjectHashMap);
            mTuyaDevice.publishDps(commandStr, new IControlCallback() {
                @Override
                public void onError(String code, String error) {
                    enableViews(false);
                    boolean b = btnOnOff.isChecked();
                    btnOnOff.setChecked(!b);
                    if (!b) {
                        btnOnOff.setText("ON");
                        btnOnOff.setTextColor(getResources().getColor(R.color.orange_light));
                    } else {
                        btnOnOff.setText("OFF");
                        btnOnOff.setTextColor(getResources().getColor(R.color.color9999));
                    }
                    enableViews(true);
                    DialogUtil.simpleSmartDialog(getActivity(), code + " : " + error, null);

                }

                @Override
                public void onSuccess() {

                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar(this);
        setContentView(R.layout.activity_remote_control);
        ButterKnife.bind(this);
        init();

    }

    private void init() {
        title.setText("GRILL ONE");
        mDevId = getIntent().getStringExtra(RemoteControlActivity.INTENT_DEVID);
        mTuyaDevice = new TuyaDevice(mDevId);
        initBroadcast();
        initService();
        initView();
        initListener();
    }

    private void initBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_LESS);
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_MORE);
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_MORE);
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_MORE);
        registerReceiver(receiver, intentFilter);
    }

    Intent serviceIntent;

    private void initService() {
        if (!isServiceExisted(this, TraegerGillService.class.getName())) {
            serviceIntent = new Intent(this, TraegerGillService.class);
            serviceIntent.putExtra(INTENT_DEVID, getIntent().getStringExtra(INTENT_DEVID));
            startService(serviceIntent);
        }

    }

    List<TextView> unitTexts;

    private void initView() {
        unitTexts = new ArrayList<>();
        unitTexts.add(leftTempUnit);
        unitTexts.add(midTempUnit);
        unitTexts.add(rightBottomTempUnit);
        unitTexts.add(bottomTempUnit);
        unitTexts.add(rightTopTempUnit);
        btnOnOff.setTag(R.id.schemaId, "1");
        for (TextView text : unitTexts) {
            text.setTag(R.id.schemaId, "108");
        }
        bottomTemp.setTag(R.id.schemaId, "102");
        midTemp.setTag(R.id.schemaId, "102");
        leftTemp.setTag(R.id.schemaId, "103");
        rightTopTemp.setTag(R.id.schemaId, "105");
        rightBottomTemp.setTag(R.id.schemaId, "106");
    }

    private ScheduledExecutorService scheduledExecutor;

    private void updateAddOrSubtract(int viewId) {
        final int vid = viewId;
        if (scheduledExecutor == null) {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = vid;
                    handler.sendMessage(msg);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);    //每间隔100ms发送Message
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!btnOnOff.isChecked()) {
                return;
            }
            int viewId = msg.what;
            int now;
            switch (viewId) {
                case R.id.iv_less:
                    now = Integer.parseInt(bottomTemp.getText().toString()) - 5;
                    if (now <= 179) {
                        return;
                    }
                    bottomTemp.setText(now + "");
                    break;
                case R.id.iv_add:
                    now = Integer.parseInt(bottomTemp.getText().toString()) + 5;
                    if (now >= 601) {
                        return;
                    }
                    bottomTemp.setText(now + "");
                    break;
            }
        }
    };

    private void stopAddOrSubtract() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdownNow();
            scheduledExecutor = null;
            if (btnOnOff.isChecked()) {
                sendCommandSetpoint(Integer.parseInt(bottomTemp.getText().toString()));
            }
        }
    }

    private void initListener() {
        if (mTuyaDevice == null) {
            this.finish();
            OUtil.TLog("device is already deleted!");
        }
        Map<String, Object> list = TuyaUser.getDeviceInstance().getDps(mDevId);
        for (Map.Entry<String, Object> entry : list.entrySet()) {
            updateData(entry);
        }
        ivLess.setOnTouchListener(longClick);
        ivAdd.setOnTouchListener(longClick);
        btnOnOff.setOnCheckedChangeListener(checkListener);
        mTuyaDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                OUtil.TLog(" =====onDpUpdate =====");
                // enableViews(false);
                JSONObject jsonObject = JSONObject.parseObject(dpStr);
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    updateData(entry);
                }
                // enableViews(true);
            }

            @Override
            public void onRemoved(String devId) {
                cover.setVisibility(View.VISIBLE);
                tvWarn.setText(getResources().getString(R.string.device_has_unbinded));
                DialogUtil.simpleSmartDialog(getActivity(), "设备被移除了", null);
                SQLiteDbUtil.getSQLiteDbUtil().delete(UserData.class, "dev_id = " + mDevId);
            }

            @Override
            public void onStatusChanged(String devId, boolean b) {
                OUtil.TLog("===onStatusChanged=== : " + b);
                if (!b) {
                    cover.setVisibility(View.VISIBLE);
                    tvWarn.setText(getResources().getString(R.string.device_offLine));
                } else {
                    cover.setVisibility(View.GONE);
                }
                //DialogUtil.simpleSmartDialog(getActivity(), "设备离线了", null);
            }

            @Override
            public void onNetworkStatusChanged(String s, boolean b) {
                OUtil.TLog("===onNetworkStatusChanged=== : " + b);
                if (!b) {
                    cover.setVisibility(View.VISIBLE);
                    tvWarn.setText(getResources().getString(R.string.network_error));
                    //DialogUtil.simpleSmartDialog(getActivity(), "网络异常", null);
                } else {
                    cover.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDevInfoUpdate(String s) {
                OUtil.TLog("===onDevInfoUpdate=== : " + s);
            }
        });
    }

    private void updateData(Map.Entry<String, Object> entry) {
        OUtil.TLog("key : " + entry.getKey() + " value : " + entry.getValue());
        if (entry.getKey().equals(btnOnOff.getTag(R.id.schemaId))) {
            //电源开关	Power
            enableViews(false);
            btnOnOff.setChecked((boolean) entry.getValue());
            if ((boolean) entry.getValue()) {
                btnOnOff.setText("ON");
                btnOnOff.setTextColor(getResources().getColor(R.color.orange_light));
            } else {
                btnOnOff.setText("OFF");
                btnOnOff.setTextColor(getResources().getColor(R.color.color9999));
            }
            enableViews(true);
        } else if (entry.getKey().equals(bottomTempUnit.getTag(R.id.schemaId))) {
            //	温度单位切换	Temp_unit
            boolean isC = (boolean) entry.getValue();
            for (TextView text : unitTexts) {
                if (isC) {
                    text.setText(getResources().getString(R.string.CCCC));
                } else {
                    text.setText(getResources().getString(R.string.FFFF));
                }
            }
        } else if (entry.getKey().equals(midTemp.getTag(R.id.schemaId))) {
            //设定温度	Set_temp
            midTemp.setText(entry.getValue().toString());
            bottomTemp.setText(entry.getValue().toString());
        } else if (entry.getKey().equals(leftTemp.getTag(R.id.schemaId))) {
            //实际温度	Actual_temp
            leftTemp.setText(entry.getValue().toString());
        } else if (entry.getKey().equals(rightTopTemp.getTag(R.id.schemaId))) {
            //食物温度1	Food_temp1
            rightTopTemp.setText(entry.getValue().toString());
        } else if (entry.getKey().equals(rightBottomTemp.getTag(R.id.schemaId))) {
            //食物温度2	Food_temp2
            rightBottomTemp.setText(entry.getValue().toString());
        }
        //btnOnOff.isEnabled() 说明 没有在修改setpoint
        if (btnOnOff.isEnabled() && !midTemp.getText().equals(bottomTemp.getText())) {
            bottomTemp.setText(midTemp.getText());
        }

    }


    @OnClick({R.id.setting,R.id.ll_temp_chart})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.setting:
                getActivity().startActivity(new Intent(getActivity(), SettingGrillActivity.class).putExtra(RemoteControl2Activity.INTENT_DEVID, mDevId));
                break;
            case R.id.ll_temp_chart:
                long startTime = System.currentTimeMillis(); //startTime起始时间
                int number = 12;//往前获取历史数据结果值的个数 ，最大是50
                String dpId = "103";
                mTuyaDevice.getDataPointStat(DataPointTypeEnum.DAY, startTime, number, dpId, new IGetDataPointStatCallback() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        OUtil.TLog(errorCode + " : "  +errorMsg);
                    }
                    @Override
                    public void onSuccess(DataPointStatBean bean) {
                        OUtil.TLog("DataPointStatBean : " + new Gson().toJson(bean));
                    }
                });
                getActivity().startActivity(new Intent(getActivity(), TempChartActivity.class).putExtra(RemoteControl2Activity.INTENT_DEVID, mDevId));
                break;
        }
    }

    void sendCommandSetpoint(int temp) {
        enableViews(false);
        mHandler.postDelayed(mRunnable, 300);
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("102", temp);
        String commandStr = JSON.toJSONString(stringObjectHashMap);
        mTuyaDevice.publishDps(commandStr, new IControlCallback() {
            @Override
            public void onError(String code, String error) {
                DialogUtil.simpleSmartDialog(getActivity(), code + " : " + error, null);
            }

            @Override
            public void onSuccess() {
            }
        });
    }

    private void enableViews(boolean isAble) {
        if (isAble) {
            ivLess.setEnabled(true);
            ivAdd.setEnabled(true);
            btnOnOff.setEnabled(true);
            btnOnOff.setOnCheckedChangeListener(checkListener);
        } else {
            ivLess.setEnabled(false);
            ivAdd.setEnabled(false);
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdownNow();
                scheduledExecutor = null;
            }
            btnOnOff.setEnabled(false);
            btnOnOff.setOnCheckedChangeListener(null);
        }

    }

    @Override
    public boolean needLogin() {
        return true;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(1);
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            enableViews(true);
        }
    };
    View.OnTouchListener longClick = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                btnOnOff.setEnabled(false);
                updateAddOrSubtract(view.getId());    //手指按下时触发不停的发送消息
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                btnOnOff.setEnabled(true);
                stopAddOrSubtract();    //手指抬起时停止发送
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                btnOnOff.setEnabled(true);
                stopAddOrSubtract();    //手指抬起时停止发送
            }
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTuyaDevice != null) {
            mTuyaDevice.unRegisterDevListener();
            mTuyaDevice.onDestroy();
        }
        if (serviceIntent != null)
            stopService(serviceIntent);
        unregisterReceiver(receiver);
    }

    public static boolean isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}
