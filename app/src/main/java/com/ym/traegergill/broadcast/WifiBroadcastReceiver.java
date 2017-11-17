package com.ym.traegergill.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.tuya.smart.android.base.event.BaseEventSender;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.NetworkUtil;
import com.ym.traegergill.activity.BaseActivity;
import com.ym.traegergill.tools.NetUtil;

/**
 * Created by Administrator on 2017/9/29.
 */

public class WifiBroadcastReceiver  extends BroadcastReceiver {
    private static final String TAG = WifiBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {

        WifiInfo info = ((WifiManager)context.getSystemService(context.WIFI_SERVICE)).getConnectionInfo();
        //mIconWifi.setImageLevel(Math.abs(info.getRssi()));
        /*

        WifiManager.WIFI_STATE_DISABLING   正在停止

        WifiManager.WIFI_STATE_DISABLED    已停止

        WifiManager.WIFI_STATE_ENABLING    正在打开

        WifiManager.WIFI_STATE_ENABLED     已开启

        WifiManager.WIFI_STATE_UNKNOWN     未知

        */
        Intent intentBroadcast;
        switch (intent.getIntExtra("wifi_state", 0)) {

            case WifiManager.WIFI_STATE_DISABLING:

                Log.i(TAG, "WIFI STATUS : WIFI_STATE_DISABLING");
                intentBroadcast = new Intent(TraegerGillBroadcastHelper.ACTION_TEST_WIFI);
                intentBroadcast.putExtra("data","WIFI CHANGE");
                context.sendBroadcast(intentBroadcast);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                Log.i(TAG, "WIFI STATUS : WIFI_STATE_DISABLED");
                intentBroadcast = new Intent(TraegerGillBroadcastHelper.ACTION_TEST_WIFI);
                intentBroadcast.putExtra("data","WIFI OFF");
                context.sendBroadcast(intentBroadcast);
                break;

            case WifiManager.WIFI_STATE_ENABLING:

                Log.i(TAG, "WIFI STATUS : WIFI_STATE_ENABLING");

                break;

            case WifiManager.WIFI_STATE_ENABLED:

                Log.i(TAG, "WIFI STATUS : WIFI_STATE_ENABLED");
                intentBroadcast = new Intent(TraegerGillBroadcastHelper.ACTION_TEST_WIFI);
                intentBroadcast.putExtra("data","WIFI ON");
                context.sendBroadcast(intentBroadcast);

                break;

            case WifiManager.WIFI_STATE_UNKNOWN:

                Log.i(TAG, "WIFI STATUS : WIFI_STATE_UNKNOWN");

                break;
        }


    }
    // 自定义接口
    public interface NetEvevt {
        public void onNetChange(int netMobile);
    }
}

