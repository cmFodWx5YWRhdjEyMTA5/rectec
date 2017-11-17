package com.ym.traegergill;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.lzy.okhttputils.OkHttpUtils;
import com.tencent.smtt.sdk.QbSdk;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.uuch.adlibrary.utils.DisplayUtil;
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

        if (isInitAppkey()) {
            initSdk();
        }
        //http://blog.csdn.net/linglongxin24/article/details/53385868
        SQLiteDbUtil.getSQLiteDbUtil().openOrCreateDataBase(this);

        initDisplayOpinion();

        Fresco.initialize(this);
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
    private void initDisplayOpinion() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(getApplicationContext(), dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(getApplicationContext(), dm.heightPixels);
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
