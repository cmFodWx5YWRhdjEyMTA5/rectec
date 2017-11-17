package com.ym.traegergill.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.api.IDevListener;
import com.ym.traegergill.R;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;

import java.util.HashMap;
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

public class MiniFeedRateActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.iv_less)
    ImageView ivLess;
    @BindView(R.id.percent)
    TextView percent;
    @BindView(R.id.percent_unit)
    TextView percentUnit;
    @BindView(R.id.iv_add)
    ImageView ivAdd;
    private TuyaDevice mTuyaDevice;
    private String mDevId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_feed_rate);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText("Minimum Feed Rate");
        mDevId = getIntent().getStringExtra(RemoteControlActivity.INTENT_DEVID);
        mTuyaDevice = new TuyaDevice(mDevId);
        Map<String, Object> list = TuyaUser.getDeviceInstance().getDps(mDevId);
        for (Map.Entry<String, Object> entry : list.entrySet()) {
            if (entry.getKey().equals("104")) {
                double temp = Integer.parseInt(entry.getValue().toString()) / 10.0;
                percent.setText(temp + "");
            }
        }
        initListener();
    }

    private void initListener() {
        ivLess.setOnTouchListener(longClick);
        ivAdd.setOnTouchListener(longClick);

        mTuyaDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                OUtil.TLog(" =====onDpUpdate =====");
                enableViews(false);
                JSONObject jsonObject = JSONObject.parseObject(dpStr);
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    if (entry.getKey().equals("104")) {
                        double temp = Integer.parseInt(entry.getValue().toString()) / 10.0;
                        percent.setText(temp + "");
                    }
                }
                enableViews(true);
            }

            @Override
            public void onRemoved(String s) {
                DialogUtil.simpleSmartDialog(getActivity(), getString(R.string.device_has_unbinded), null);
            }

            @Override
            public void onStatusChanged(String s, boolean b) {
                if(!b){
                    DialogUtil.simpleSmartDialog(getActivity(), getString(R.string.device_offLine), null);
                }
            }

            @Override
            public void onNetworkStatusChanged(String s, boolean b) {
                if(b == false){
                    DialogUtil.simpleSmartDialog(getActivity(), getString(R.string.network_error), null);
                }

            }

            @Override
            public void onDevInfoUpdate(String s) {

            }
        });
    }


    @Override
    public boolean needLogin() {
        return true;
    }

    View.OnTouchListener longClick = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                updateAddOrSubtract(view.getId());    //手指按下时触发不停的发送消息
                switch (view.getId()) {
                    case R.id.iv_add:
                        ivAdd.setImageResource(R.drawable.icon_add_pre);
                        break;
                    case R.id.iv_less:
                        ivLess.setImageResource(R.drawable.icon_less_pre);
                        break;

                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                stopAddOrSubtract();    //手指抬起时停止发送
                switch (view.getId()) {
                    case R.id.iv_add:
                        ivAdd.setImageResource(R.drawable.icon_add);
                        break;
                    case R.id.iv_less:
                        ivLess.setImageResource(R.drawable.icon_less);
                        break;

                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                stopAddOrSubtract();    //手指抬起时停止发送
                switch (view.getId()) {
                    case R.id.iv_add:
                        ivAdd.setImageResource(R.drawable.icon_add);
                        break;
                    case R.id.iv_less:
                        ivLess.setImageResource(R.drawable.icon_less);
                        break;

                }
            }
            return true;
        }
    };

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
            int viewId = msg.what;
            int now;
            switch (viewId) {
                case R.id.iv_less:
                    now = (int) (Double.parseDouble(percent.getText().toString()) * 10) - 5;
                    if (now <= 29) {
                        return;
                    }
                    percent.setText(now / 10.0 + "");
                    break;
                case R.id.iv_add:
                    now = (int) (Double.parseDouble(percent.getText().toString()) * 10) + 5;
                    if (now >= 251) {
                        return;
                    }
                    percent.setText(now / 10.0 + "");
                    break;
            }
        }
    };

    private void stopAddOrSubtract() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdownNow();
            scheduledExecutor = null;
            int now = (int) (Double.parseDouble(percent.getText().toString()) * 10);
            sendCommand(now);
        }
    }

    void sendCommand(int value) {
        enableViews(false);
        mHandler.postDelayed(mRunnable, 300);
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("104", value);
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
        } else {
            ivLess.setEnabled(false);
            ivAdd.setEnabled(false);
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdownNow();
                scheduledExecutor = null;
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(1);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            enableViews(true);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTuyaDevice != null) {
            mTuyaDevice.unRegisterDevListener();
            mTuyaDevice.onDestroy();
        }
    }
}
