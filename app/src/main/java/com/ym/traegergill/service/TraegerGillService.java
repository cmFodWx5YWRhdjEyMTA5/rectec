package com.ym.traegergill.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.lzy.okhttputils.callback.StringCallback;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaSmartRequest;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.RemoteControlActivity;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.db.bean.UserData;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Administrator on 2017/9/29.
 */

public class TraegerGillService extends Service {
    private final static String TAG = TraegerGillService.class.getSimpleName();
    private String mDevId;
    private TuyaDevice mTuyaDevice;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        OUtil.TLog("===TraegerGillService=== onCreate ====");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OUtil.TLog("=== TraegerGillService === onStartCommand ====");
        mDevId = intent.getStringExtra(RemoteControlActivity.INTENT_DEVID);
        mTuyaDevice = new TuyaDevice(mDevId);
        //syncToServer();
        mTuyaDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                OUtil.TLog("devId : " + devId + " dpStr : " + dpStr);
                JSONObject jsonObject = JSONObject.parseObject(dpStr);
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    String dpId = entry.getKey();
                    if(dpId == "1"){
                        boolean flag = (boolean)entry.getValue();
                        setDevice(flag);
                        break;
                    }
                }
                sendDeviceBroadcast(TraegerGillBroadcastHelper.ACTION_DEVICE_onDpUpdate);
            }
            @Override
            public void onRemoved(String s) {
                sendDeviceBroadcast(TraegerGillBroadcastHelper.ACTION_DEVICE_onRemoved);
            }

            @Override
            public void onStatusChanged(String s, boolean b) {
                sendDeviceBroadcastWithBoolean(TraegerGillBroadcastHelper.ACTION_DEVICE_onStatusChanged,b);
            }

            @Override
            public void onNetworkStatusChanged(String s, boolean b) {
                sendDeviceBroadcastWithBoolean(TraegerGillBroadcastHelper.ACTION_DEVICE_onNetworkStatusChanged,b);
            }
            @Override
            public void onDevInfoUpdate(String s) {
                sendDeviceBroadcast(TraegerGillBroadcastHelper.ACTION_DEVICE_onDevInfoUpdate);
            }
        });


        return super.onStartCommand(intent, flags, startId);
    }

    private void setDevice(boolean flag) {
        String url = "";
        if(flag){
            url = URLs.onUserDevice;
        }else{
            url = URLs.offUserDevice;
        }
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                OUtil.TLog("isFromCache : " + isFromCache+"  json : " + s);
                if(isFromCache){
                    return;
                }
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(s);
                    OUtil.TLog(obj.optString("msg"));
                    if (obj.optInt("code") == 200) {

                    }else{

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
        String params = "deviceInfo="+mDevId;
        if(!MyNetTool.netCrossWithParams(this,TuyaUser.getUserInstance().getUser().getUid(),url,params,callback)){
            OUtil.TLog(TAG + "===network error===");
        }
    }

    private void syncToServer() {
        Map<String, Object> map = new HashMap<>();
        map.put("devId", mDevId);
        map.put("dpIds", "1");
        map.put("offset", 0);
        map.put("limit", 1);
        map.put("sortType", "DESC");
        TuyaSmartRequest.getInstance().requestWithApiName("m.smart.operate.log", "2.0", map, new IRequestCallback() {

            @Override
            public void onSuccess(Object result) {
                OUtil.TLog(new Gson().toJson(result));
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        OUtil.TLog(TAG + "  =====onDestroy====");
        if (mTuyaDevice != null) {
            mTuyaDevice.unRegisterDevListener();
            mTuyaDevice.onDestroy();
        }
    }

    private void sendDeviceBroadcast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void sendDeviceBroadcastWithBoolean(String action,boolean b) {
        Intent intent = new Intent(action);
        intent.putExtra("data",b);
        sendBroadcast(intent);
    }



}
