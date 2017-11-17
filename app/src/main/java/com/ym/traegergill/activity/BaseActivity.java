package com.ym.traegergill.activity;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.lzy.okhttputils.OkHttpUtils;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.StatusBarTool;
import com.ym.traegergill.tools.SystemTool;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2017/9/20.
 */

public class BaseActivity extends AppCompatActivity {

    /** 日志输出标志 **/
    protected final String TAG =
            this.getClass().getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置不能横屏，防止生命周期的改变
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        checkLogin();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

    }


    protected Activity getActivity(){
        return this;
    }
    public void initStatusBar(Activity mainActivity) {
        if (SystemTool.isFlyme()) {
            StatusBarTool.setMeizuStatusBarDarkIcon(mainActivity, true);
        } else if (SystemTool.isMIUI()) {
            StatusBarTool.setMiuiStatusBarDarkMode(mainActivity, true);
        } else {
            StatusBarTool.setNormalStatusBarDarkIcon(mainActivity);
        }
    }
    public void exit(View view){
        //super.finish();
        this.finishAfterTransition();
    }

    @Override
    public void finish() {
        super.finish();
    }

    public void showToastSuccess(String msg)
    {
        OUtil.toastSuccess(this,msg);
    }
    public void showToastError(String msg)
    {
        OUtil.toastError(this,msg);
    }


    public void TLog(String msg){
        if(msg==null){
            msg = "";
        }
        Log.i(Constants.preTestString  + TAG,msg);
    }

    private void checkLogin() {
        if (needLogin() && !TuyaUser.getUserInstance().isLogin()) {
            startActivity(new Intent(this, SignInActivity.class));
            this.finish();
            overridePendingTransition(R.anim.slide_bottom_to_top,R.anim.slide_none_medium_time);
        }
    }
    /**
     * 是否需要登录，子类根据业务需要决定.
     * 默认所有界面都需要判断是否登录状态。
     */
    public boolean needLogin() {
        return false;
    }

    //动画标识
    public static final int ANIMATE_FORWARD = 0;
    public static final int ANIMATE_BACK = 1;
    public static final int ANIMATE_SLIDE_TOP_FROM_BOTTOM = 3;
    public static final int ANIMATE_SLIDE_BOTTOM_FROM_TOP = 4;
    public static void overridePendingTransition(Activity activity, int direction) {
        if (direction == ANIMATE_FORWARD) {
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (direction == ANIMATE_BACK) {
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else if (direction == ANIMATE_SLIDE_TOP_FROM_BOTTOM) {
            activity.overridePendingTransition(R.anim.slide_bottom_to_top, R.anim.slide_none_medium_time);
        } else if (direction == ANIMATE_SLIDE_BOTTOM_FROM_TOP) {
            activity.overridePendingTransition(R.anim.slide_none_medium_time, R.anim.slide_top_to_bottom);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(getActivity());
    }
}
