package com.ym.traegergill.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
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
import com.tuya.smart.android.device.bean.DataPointBean;
import com.tuya.smart.android.device.bean.DataPointStatBean;
import com.tuya.smart.android.device.enums.DataPointTypeEnum;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaSmartRequest;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.db.bean.UserData;
import com.ym.traegergill.service.TraegerGillService;
import com.ym.traegergill.tools.Constants;
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

    private static final String FOOD_TEMP_EMPTY = "- -";
    private TuyaDevice mTuyaDevice;
    private DeviceBean mDevBean;
    private String mDevId;
    private String mUid;
    private SQLiteDbUtil dbUtil;
    private boolean isRunning = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case TraegerGillBroadcastHelper.ACTION_DEVICE_onDpUpdate:
                    syncData();
                    break;
                case TraegerGillBroadcastHelper.ACTION_DEVICE_onRemoved:
                    cover.setVisibility(View.VISIBLE);
                    tvWarn.setText(getResources().getString(R.string.device_has_unbinded));
                    DialogUtil.simpleSmartDialog(getActivity(), getString(R.string.device_has_unbinded), null);
                    SQLiteDbUtil.getSQLiteDbUtil().delete(UserData.class, "dev_id = " + mDevId);
                    break;
                case TraegerGillBroadcastHelper.ACTION_DEVICE_onDevInfoUpdate:
                    mDevBean = TuyaUser.getDeviceInstance().getDev(mDevId);
                    title.setText(mDevBean.getName());
                    break;
                case TraegerGillBroadcastHelper.ACTION_DEVICE_onStatusChanged:
                    boolean isOnLine = intent.getBooleanExtra("data",false);
                    OUtil.TLog("===onStatusChanged=== : " + isOnLine);
                    if (!isOnLine) {
                        cover.setVisibility(View.VISIBLE);
                        tvWarn.setText(getResources().getString(R.string.device_offLine));
                    } else {
                        cover.setVisibility(View.GONE);
                    }
                    break;
                case TraegerGillBroadcastHelper.ACTION_DEVICE_onNetworkStatusChanged:
                    boolean isNetOnLine = intent.getBooleanExtra("data",false);
                    OUtil.TLog("===onNetworkStatusChanged=== : " + isNetOnLine);
                    if (!isNetOnLine) {
                        cover.setVisibility(View.VISIBLE);
                        tvWarn.setText(getResources().getString(R.string.network_error));
                        //DialogUtil.simpleSmartDialog(getActivity(), "网络异常", null);
                    } else {
                        cover.setVisibility(View.GONE);
                    }

                    break;
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
                    //syncData();
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar(this);
        setContentView(R.layout.activity_remote_control);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText("GRILL ONE");
        mUid = TuyaUser.getUserInstance().getUser().getUid();
        mDevId = getIntent().getStringExtra(RemoteControlActivity.INTENT_DEVID);
        mTuyaDevice = new TuyaDevice(mDevId);
        mDevBean = TuyaUser.getDeviceInstance().getDev(mDevId);
        title.setText(mDevBean.getName());
        initBroadcast();
        initService();
        initView();
        initListener();
    }

    private void initBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_DEVICE_onDevInfoUpdate);
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_DEVICE_onDpUpdate);
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_DEVICE_onRemoved);
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_DEVICE_onStatusChanged);
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_DEVICE_onNetworkStatusChanged);
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
            }, 0, 150, TimeUnit.MILLISECONDS);    //每间隔150ms发送Message
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
            }else{
                syncData();
            }
        }
    }

    private void initListener() {
        if (mTuyaDevice == null) {
            this.finish();
            OUtil.TLog("device is already deleted!");
        }
        syncData();
        ivLess.setOnTouchListener(longClick);
        ivAdd.setOnTouchListener(longClick);
        btnOnOff.setOnCheckedChangeListener(checkListener);
    }
    private UserData alarmsData;
    private void initAlarmsDB() {
        dbUtil = SQLiteDbUtil.getSQLiteDbUtil();
        List<UserData> datas = dbUtil.query(UserData.class, "dev_id = ? and user = ?", new String[]{mDevId, mUid});
        if (datas == null || datas.size() == 0) {
            return;
        } else {
            alarmsData = datas.get(0);
        }
    }
    private void syncData() {
        initAlarmsDB();
        Map<String, Object> list = TuyaUser.getDeviceInstance().getDps(mDevId);
        for (Map.Entry<String, Object> entry : list.entrySet()) {
            updateData(entry);
        }
    }

    private void updateData(Map.Entry<String, Object> entry) {
        OUtil.TLog("key : " + entry.getKey() + " value : " + entry.getValue());
        if (entry.getKey().equals(btnOnOff.getTag(R.id.schemaId)) && (boolean) entry.getValue() != btnOnOff.isChecked()) {
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
            if(!isRunning)
                bottomTemp.setText(entry.getValue().toString());
        } else if (entry.getKey().equals(leftTemp.getTag(R.id.schemaId))) {
            //实际温度	Actual_temp
            leftTemp.setText(entry.getValue().toString());
        } else if (entry.getKey().equals(rightTopTemp.getTag(R.id.schemaId))) {
            //食物温度1	Food_temp1
            if((int)entry.getValue() == 32){
                //没有插入探测头A
                String inner = FOOD_TEMP_EMPTY;
                rightTopTemp.setText(inner);
                rightTopTempUnit.setVisibility(View.GONE);
            }else{
                rightTopTemp.setText(entry.getValue().toString());
                rightTopTempUnit.setVisibility(View.VISIBLE);
            }
        } else if (entry.getKey().equals(rightBottomTemp.getTag(R.id.schemaId))) {
            //食物温度2	Food_temp2
            if((int)entry.getValue() == 32){
                //没有插入探测头B
                String inner = FOOD_TEMP_EMPTY;
                rightBottomTemp.setText(inner);
                rightBottomTempUnit.setVisibility(View.GONE);
            }else{
                rightBottomTemp.setText(entry.getValue().toString());
                rightBottomTempUnit.setVisibility(View.VISIBLE);
            }
        }
        //btnOnOff.isEnabled() 说明 没有在修改setpoint
        if (btnOnOff.isEnabled() && !midTemp.getText().equals(bottomTemp.getText())) {
            bottomTemp.setText(midTemp.getText());
        }
        sendAlarms(entry);
    }


    @OnClick({R.id.setting,R.id.ll_temp_chart})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.setting:
                getActivity().startActivity(new Intent(getActivity(), SettingGrillActivity.class).putExtra(RemoteControl2Activity.INTENT_DEVID, mDevId));
                break;
            case R.id.ll_temp_chart:
                //修改昵称接口示例
                getActivity().startActivity(new Intent(getActivity(), TempChartActivity.class).putExtra(RemoteControl2Activity.INTENT_DEVID, mDevId));
                break;
        }
    }

    void sendCommandSetpoint(int temp) {
        enableViews(false);
        mHandler.postDelayed(mRunnable, 400);
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("102", temp);
        String commandStr = JSON.toJSONString(stringObjectHashMap);
        mTuyaDevice.publishDps(commandStr, new IControlCallback() {
            @Override
            public void onError(String code, String error) {
                DialogUtil.simpleSmartDialog(getActivity(), code + " : " + error, null);
                syncData();
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
                isRunning = true;
                btnOnOff.setEnabled(false);
                updateAddOrSubtract(view.getId());    //手指按下时触发不停的发送消息
                switch (view.getId()){
                    case R.id.iv_add:
                        ivAdd.setImageResource(R.drawable.icon_add_pre);
                        break;
                    case R.id.iv_less:
                        ivLess.setImageResource(R.drawable.icon_less_pre);
                        break;

                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                isRunning = false;
                switch (view.getId()){
                    case R.id.iv_add:
                        ivAdd.setImageResource(R.drawable.icon_add);
                        break;
                    case R.id.iv_less:
                        ivLess.setImageResource(R.drawable.icon_less);
                        break;
                }
                btnOnOff.setEnabled(true);
                stopAddOrSubtract();    //手指抬起时停止发送
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                isRunning = false;
                switch (view.getId()){
                    case R.id.iv_add:
                        ivAdd.setImageResource(R.drawable.icon_add);
                        break;
                    case R.id.iv_less:
                        ivLess.setImageResource(R.drawable.icon_less);
                        break;

                }
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

        if(notifyManager!=null){
            notifyManager.cancelAll();
        }

    }

    int max_actual_temp = 0;//实际温度当前最大的点
    int last_actual_temp = 0;//实际温度上一个点的温度
    private void sendAlarms(Map.Entry<String, Object> entry) {
        if (alarmsData == null){
            return;
        }
        String dpId = entry.getKey();
        OUtil.TLog(TAG + " send dpId : " + dpId);
        //grill range alarms
        switch (dpId) {
           // case "102":
            case "103":
                if (alarmsData.isFlag()) {
                    int set_temp= Integer.parseInt(midTemp.getText().toString());
                    int actual_temp = Integer.parseInt(leftTemp.getText().toString());
                    int maxTemp = set_temp + alarmsData.getGill_range();
                    int minTemp = set_temp - alarmsData.getGill_range();
                    maxTemp = (maxTemp <= 600) ? maxTemp : 600;
                    minTemp = (minTemp >= 180) ? minTemp : 180;
                    OUtil.TLog(TAG+" SetTempMax : " + maxTemp + " /// SetTempMin : " + minTemp + "/// Actual_temp : " + actual_temp);

                    if (actual_temp < minTemp) {
                        OUtil.TLog(TAG + "  temp bad ");
                        //小于设定最小值
                        leftTemp.setTextColor(getResources().getColor(R.color.blue_temp_less));
                        leftTempUnit.setTextColor(getResources().getColor(R.color.blue_temp_less));
                        if(actual_temp>=last_actual_temp){
                            //上升趋势 不警告
                            last_actual_temp = actual_temp;
                            if(actual_temp>=max_actual_temp){
                                //大于最大的点 说明之前温度没有下降过 更新 max_actual_temp
                                max_actual_temp = actual_temp;
                            }else{
                                //小于最大的点 说明之前温度下降过 不更新 max_actual_temp
                            }
                            setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_OK);
                        }else{
                            //下降趋势
                            last_actual_temp = actual_temp;
                            if((max_actual_temp - actual_temp) <= Constants.temp_down_range){
                                //下降值小于20
                                //不警告
                                setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_OK);
                            }else{
                                //下降值大于20
                                //警告 复原 max_actual_temp
                                //max_actual_temp = 0;
                                setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_LESS);
                            }
                        }
                    }else if (actual_temp >= minTemp && actual_temp <= maxTemp) {
                        //温度已经到正常范围 不警告
                        max_actual_temp = minTemp;
                        leftTemp.setTextColor(getResources().getColor(R.color.color444));
                        leftTempUnit.setTextColor(getResources().getColor(R.color.color444));
                        OUtil.TLog(TAG + "  temp ok");
                        setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_OK);
                    } else {
                        // 大于正常值
                        leftTemp.setTextColor(getResources().getColor(R.color.orange_text));
                        leftTempUnit.setTextColor(getResources().getColor(R.color.orange_text));
                        OUtil.TLog(TAG + "  temp bad ");
                        setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_MORE);
                    }
                }else {
                    // 未开启警告 不警告
                    leftTemp.setTextColor(getResources().getColor(R.color.color444));
                    leftTempUnit.setTextColor(getResources().getColor(R.color.color444));
                    setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_OK);
                }
                break;
            case "105":
                //Food_temp1 alarms
                if (alarmsData.isA_temp_open() && (!rightTopTemp.getText().toString().equals(FOOD_TEMP_EMPTY))) {
                    int food_temp1 = Integer.parseInt(rightTopTemp.getText().toString());
                    int maxTemp = alarmsData.getProbe_a_temp();
                    if (food_temp1 < maxTemp) {
                        //没有达到设定温度
                        setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_LESS);
                    } else {
                        //达到设定温度及以上
                        setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_OK);
                    }
                }else{
                    setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_OK);
                }

                break;
            case "106":
                //Food_temp2 alarms
                if (alarmsData.isB_temp_open() && (!rightBottomTemp.getText().toString().equals(FOOD_TEMP_EMPTY))) {
                    int food_temp2 = Integer.parseInt(rightBottomTemp.getText().toString());
                    int maxTemp = alarmsData.getProbe_b_temp();
                    if (food_temp2 < maxTemp) {
                        //没有达到设定温度
                        setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_LESS);
                    } else {
                        //达到设定温度及以上
                        setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_OK);
                    }
                }else {
                    setAlarms(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_OK);
                }
                break;
        }

    }

    private void setAlarms(String action) {
        notifyWarn(action);
        if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_LESS)) {
            OUtil.TLog("ACTION_ALARMS_ACTUAL_LESS");
            SnackbarUtil.ShortSnackbar(title, "Actual Temp Low", SnackbarUtil.MyWarning).show();
        } else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_MORE)) {
            OUtil.TLog("ACTION_ALARMS_ACTUAL_MORE");
            SnackbarUtil.ShortSnackbar(title, "Actual Temp High", SnackbarUtil.MyWarning).show();
        } else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_MORE)) {
            OUtil.TLog("ACTION_ALARMS_TEMPA_MORE");
            rightTopTemp.setTextColor(getResources().getColor(R.color.orange_text));
            rightTopTempUnit.setTextColor(getResources().getColor(R.color.orange_text));
            SnackbarUtil.ShortSnackbar(title, "Probe A Temp High", SnackbarUtil.MyWarning).show();
        } else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_MORE)) {
            OUtil.TLog("ACTION_ALARMS_TEMPB_MORE");
            rightBottomTemp.setTextColor(getResources().getColor(R.color.orange_text));
            rightBottomTempUnit.setTextColor(getResources().getColor(R.color.orange_text));
            SnackbarUtil.ShortSnackbar(title, "Probe B Temp High", SnackbarUtil.MyWarning).show();
        } else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_LESS)) {
            OUtil.TLog("ACTION_ALARMS_TEMPA_LESS");
            rightTopTemp.setTextColor(getResources().getColor(R.color.blue_temp_less));
            rightTopTempUnit.setTextColor(getResources().getColor(R.color.blue_temp_less));
            SnackbarUtil.ShortSnackbar(title, "Probe A Temp Less", SnackbarUtil.MyWarning).show();
        } else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_LESS)) {
            OUtil.TLog("ACTION_ALARMS_TEMPB_LESS");
            rightBottomTemp.setTextColor(getResources().getColor(R.color.blue_temp_less));
            rightBottomTempUnit.setTextColor(getResources().getColor(R.color.blue_temp_less));
            SnackbarUtil.ShortSnackbar(title, "Probe B Temp Less", SnackbarUtil.MyWarning).show();
        }else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_OK)) {
            OUtil.TLog("ACTION_ALARMS_TEMPB_OK");
            rightBottomTemp.setTextColor(getResources().getColor(R.color.color444));
            rightBottomTempUnit.setTextColor(getResources().getColor(R.color.color444));
        }else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_OK)) {
            OUtil.TLog("ACTION_ALARMS_TEMPA_OK");
            rightTopTemp.setTextColor(getResources().getColor(R.color.color444));
            rightTopTempUnit.setTextColor(getResources().getColor(R.color.color444));
        }else if (action.equals(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_OK)) {
            OUtil.TLog("ACTION_ALARMS_ACTUAL_OK");
        }
    }
    @SuppressLint("HandlerLeak")
    private Handler warnHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(bitmap!=null){
                notifyBuilder.setLargeIcon(bitmap);
                notifyBuilder.setSmallIcon(R.drawable.icon_photo);
            }else{
                notifyBuilder.setSmallIcon(R.drawable.icon_photo);
            }
            notification = notifyBuilder.build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notifyManager.notify(1000, notification);
        }
    };

    private Bitmap bitmap = null;
    private NotificationManager notifyManager = null;
    private NotificationCompat.Builder notifyBuilder = null;
    private Notification notification = null;
    private void notifyWarn(String action){
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyBuilder = new NotificationCompat.Builder(this);
        Intent  appIntent = new Intent(getApplicationContext(),RemoteControlActivity.class);
        appIntent.putExtra(RemoteControlActivity.INTENT_DEVID,mDevId);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//关键的一步，设置启动模式
        PendingIntent pi = PendingIntent.getActivity(this, 1000, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentIntent(pi);
        notifyBuilder.setAutoCancel(true);
        notifyBuilder.setContentTitle("Device Warning");
        switch (action){
            case TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_OK:
                notifyBuilder.setContentText("Actual Temp OK");
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_LESS:
                notifyBuilder.setContentText("Actual Temp Low");
                warnHandler.sendEmptyMessage(1);
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_MORE:
                notifyBuilder.setContentText("Actual Temp High");
                warnHandler.sendEmptyMessage(1);
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_OK:
                notifyBuilder.setContentText("Probe A Temp OK");
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_MORE:
                notifyBuilder.setContentText("Probe A Temp High");
                warnHandler.sendEmptyMessage(1);
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_LESS:
                notifyBuilder.setContentText("Probe A Temp Less");
                warnHandler.sendEmptyMessage(1);
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_OK:
                notifyBuilder.setContentText("Probe B Temp OK");
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_MORE:
                notifyBuilder.setContentText("Probe B Temp High");
                warnHandler.sendEmptyMessage(1);
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_LESS:
                notifyBuilder.setContentText("Probe B Temp Less");
                break;
        }
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);

    }
}
