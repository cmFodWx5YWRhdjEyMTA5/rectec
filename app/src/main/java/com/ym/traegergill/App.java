package com.ym.traegergill;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.lzy.okhttputils.OkHttpUtils;
import com.tencent.smtt.sdk.QbSdk;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.ym.traegergill.activity.SignInActivity;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.tools.ApplicationInfoUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/9/20.
 */

public class App extends Application{

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        Log.i("oyxTest App","=== onCreate ===");
        String processName = getProcessName(this, android.os.Process.myPid());
        Log.i("oyxTest App","=== processName === " + processName);
        if(!processName.endsWith("traegergill")){
            return;
        }
        //OkHttpUtils初始化
        OkHttpUtils.init(this);
        //初始化X5内核
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                //x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。
                Log.i("oyxTest App","x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.i("oyxTest App","加载内核是否成功:"+b);
            }
        });

        if (isInitAppkey()) {
            initSdk();
        }
        //http://blog.csdn.net/linglongxin24/article/details/53385868
        SQLiteDbUtil.getSQLiteDbUtil().openOrCreateDataBase(this);
    }
    private void initSdk() {
        Log.i("oyxTest App","==== initSdk ====");
        TuyaSdk.init(this);
        TuyaSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                Intent intent = new Intent(context, SignInActivity.class);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        });
        TuyaSdk.setDebugMode(true);
    }

    private boolean isInitAppkey() {
        String appkey = ApplicationInfoUtil.getInfo("TUYA_SMART_APPKEY", this);
        String appSecret = ApplicationInfoUtil.getInfo("TUYA_SMART_SECRET", this);
        Log.i("oyxTest App","appkey : " + appkey + " //// appSecret : " + appSecret);
        if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(appSecret)) {
            return false;
        }
        return true;
    }


}
