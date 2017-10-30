package com.ym.traegergill.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.api.IDevListener;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.RemoteControlActivity;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.db.bean.UserData;
import com.ym.traegergill.tools.OUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/29.
 */

public class TraegerGillService extends Service {
    private final static String TAG = TraegerGillService.class.getSimpleName();
    private String mDevId;
    private String uid;
    private TuyaDevice mTuyaDevice;
    private SQLiteDbUtil dbUtil;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        OUtil.TLog("===TraegerGillService=== onCreate ====");
        Actual_temp = -1000;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OUtil.TLog("=== TraegerGillService === onStartCommand ====");
        mDevId = intent.getStringExtra(RemoteControlActivity.INTENT_DEVID);
        uid = TuyaUser.getUserInstance().getUser().getUid();
        Map<String, Object> list = TuyaUser.getDeviceInstance().getDps(mDevId);
        initParams();
        for (Map.Entry<String, Object> entry : list.entrySet()) {
            OUtil.TLog("=== TraegerGillService === init ====");
            updateData(entry);
        }
        mTuyaDevice = new TuyaDevice(mDevId);
        mTuyaDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                JSONObject jsonObject = JSONObject.parseObject(dpStr);
                initParams();
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    updateData(entry);
                }
            }

            @Override
            public void onRemoved(String s) {

            }

            @Override
            public void onStatusChanged(String s, boolean b) {

            }

            @Override
            public void onNetworkStatusChanged(String s, boolean b) {

            }

            @Override
            public void onDevInfoUpdate(String s) {

            }
        });
        initParams();
        return super.onStartCommand(intent, flags, startId);
    }


    private void initParams() {
        dbUtil = SQLiteDbUtil.getSQLiteDbUtil();
        List<UserData> datas = dbUtil.query(UserData.class, "dev_id = ? and user = ?", new String[]{mDevId, uid});
        if (datas == null || datas.size() == 0) {
            return;
        } else {
            data = datas.get(0);
        }
    }

    private void send(String schema) {
        OUtil.TLog(TAG + " send dpId : " + schema);
        //grill range alarms
        switch (schema) {
            case "102":
            case "103":
                if (data.isFlag()) {
                    int maxTemp = Set_temp + data.getGill_range();
                    int minTemp = Set_temp - data.getGill_range();
                    maxTemp = (maxTemp <= 600) ? maxTemp : 600;
                    minTemp = (minTemp >= 180) ? minTemp : 180;
                    OUtil.TLog(TAG+" SetTempMax : " + maxTemp + " /// SetTempMin : " + minTemp + "/// Actual_temp : " + Actual_temp);
                    if (Actual_temp >= minTemp && Actual_temp <= maxTemp) {
                        OUtil.TLog(TAG + "  temp ok");
                    } else {
                        OUtil.TLog(TAG + "  temp bad ");
                        if (Actual_temp < minTemp) {
                            sendBroadcast(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_LESS);
                        } else {
                            sendBroadcast(TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_MORE);
                        }
                    }
                }
                break;
            case "105":
                //Food_temp1 alarms
                if (data.isA_temp_open()) {
                    int maxTemp = data.getProbe_a_temp() + data.getGill_range();
                    int minTemp = data.getProbe_a_temp() - data.getGill_range();
                    maxTemp = (maxTemp <= 600) ? maxTemp : 600;
                    minTemp = (minTemp >= 0) ? minTemp : 0;
                    OUtil.TLog(TAG+" maxTempA : " + maxTemp + " /// minTempA : " + minTemp);
                    if (Food_temp1 >= minTemp && Food_temp1 <= maxTemp) {
                        OUtil.TLog(TAG + "  tempA ok");
                    } else {
                        OUtil.TLog(TAG + "  tempA bad");
                        sendBroadcast(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_MORE);
                    }
                }
                break;
            case "106":
                //Food_temp2 alarms
                if (data.isB_temp_open()) {
                    int maxTemp = data.getProbe_b_temp() + data.getGill_range();
                    int minTemp =  data.getProbe_b_temp() - data.getGill_range();
                    maxTemp = (maxTemp <= 600) ? maxTemp : 600;
                    minTemp = (minTemp >= 0) ? minTemp : 0;
                    OUtil.TLog(TAG+" maxTempB : " + maxTemp + " /// minTempB : " + minTemp);
                    if (Food_temp2 >= minTemp && Food_temp2 <= maxTemp) {
                        OUtil.TLog(TAG + "  tempB ok");
                    } else {
                        OUtil.TLog(TAG + "  tempB bad");
                        sendBroadcast(TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_MORE);
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OUtil.TLog(TAG + "  =====onDestroy====");
        if (mTuyaDevice != null) {
            mTuyaDevice.unRegisterDevListener();
            mTuyaDevice.onDestroy();
        }

      /*  if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            // 要释放资源，不然会打开很多个MediaPlayer
            mMediaPlayer.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }*/
    }

    private int Set_temp;
    private int Actual_temp;
    private int Food_temp1, Food_temp2;
    private UserData data;
    private boolean isOpen = false;
    private void updateData(Map.Entry<String, Object> entry) {
        if (data == null)
            return;
        OUtil.TLog(TAG + " key : " + entry.getKey() + " value : " + entry.getValue());
        if (entry.getKey().equals("1")) {
            isOpen = (boolean) entry.getValue();
        }else if (entry.getKey().equals("102")) {
            //设定温度	Set_temp
            Set_temp = Integer.parseInt(entry.getValue().toString());
        } else if (entry.getKey().equals("103")) {
            //实际温度	Actual_temp
            Actual_temp = Integer.parseInt(entry.getValue().toString());
        } else if (entry.getKey().equals("105")) {
            //食物温度1	Food_temp1
            Food_temp1 = Integer.parseInt(entry.getValue().toString());
        } else if (entry.getKey().equals("106")) {
            //食物温度2	Food_temp2
            Food_temp2 = Integer.parseInt(entry.getValue().toString());
        }

        if(Actual_temp!=-1000){
            send(entry.getKey());
        }

    }

    private void sendBroadcast(String action) {
       /* vibrator();
        PlaySound();*/
        OUtil.TLog(TAG + " isOpen : " + isOpen);
        if(isOpen){
            notifyWarn(action);
            Intent intent = new Intent(action);
            sendBroadcast(intent);
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
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
        Intent i = new Intent(this, RemoteControlActivity.class);
        i.putExtra(RemoteControlActivity.INTENT_DEVID, mDevId);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 1000, i, PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentIntent(pi);
        notifyBuilder.setAutoCancel(true);
        notifyBuilder.setContentTitle("Device Warning");
        switch (action){
            case TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_LESS:
                notifyBuilder.setContentText("Actual Temp Low");
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_ACTUAL_MORE:
                notifyBuilder.setContentText("Actual Temp High");
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPA_MORE:
                notifyBuilder.setContentText("Probe A Temp High");
                break;
            case TraegerGillBroadcastHelper.ACTION_ALARMS_TEMPB_MORE:
                notifyBuilder.setContentText("Probe B Temp High");
                break;
        }
        handler.sendEmptyMessage(1);

    }
   /* Vibrator vibrator;
    private void vibrator() {
        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{50, 300}, -1);
    }

    MediaPlayer mMediaPlayer;

    public void PlaySound() {
        // 使用来电铃声的铃声路径
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // 如果为空，才构造，不为空，说明之前有构造过
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(getApplicationContext(), uri);
                mMediaPlayer.setLooping(false); //循环播放
                mMediaPlayer.prepare();
            }
            mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }*/

}
