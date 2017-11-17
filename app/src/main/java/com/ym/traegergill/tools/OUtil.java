package com.ym.traegergill.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ym.traegergill.R;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/5/9.
 */

public class OUtil {
    static final String preTestString = "oyxTest";
    public static void TLog(String msg){
        if(msg==null){
            msg = "";
        }
        Log.i(preTestString,msg);
    }
    public static void toastSuccess(final Context context,
                                       final String message){
        int duration = Toast.LENGTH_SHORT;
        int drawable = R.drawable.success;
        Toast toast = Toast.makeText(context,
                message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastView = (LinearLayout) toast.getView();
        toastView.setGravity(Gravity.CENTER);
        toastView.setOrientation(LinearLayout.VERTICAL);
        toastView.setBackground(context.getResources().getDrawable(R.drawable.toast_backgroud));
        TextView messageTextView = (TextView) toastView.getChildAt(0);
        messageTextView.setTextSize(12);
        messageTextView.setTextColor(Color.WHITE);
        ImageView imageCodeProject = new ImageView(
                context);
        imageCodeProject.setPadding(0, 0, 0, 20);
        imageCodeProject.setImageResource(drawable);
        toastView.addView(imageCodeProject, 0);
        toast.show();
    }
    public static void toastError(final Context context,
                                    final String message){
        int duration = Toast.LENGTH_SHORT;
        int drawable = R.drawable.error;
        Toast toast = Toast.makeText(context,
                message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastView = (LinearLayout) toast.getView();
        toastView.setGravity(Gravity.CENTER);
        toastView.setOrientation(LinearLayout.VERTICAL);
        toastView.setBackground(context.getResources().getDrawable(R.drawable.toast_backgroud));
        TextView messageTextView = (TextView) toastView.getChildAt(0);
        messageTextView.setTextSize(12);
        messageTextView.setTextColor(Color.WHITE);
        ImageView imageCodeProject = new ImageView(
                context);
        imageCodeProject.setPadding(0, 0, 0, 20);
        imageCodeProject.setImageResource(drawable);
        toastView.addView(imageCodeProject, 0);
        toast.show();
    }

    private static int screenWidth = 0;
    private static int screenHeight = 0;
    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public static boolean isNavigationBarShow(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y!=size.y;
        }else {
            boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if(menu || back) {
                return false;
            }else {
                return true;
            }
        }
    }

    public static int getNavigationBarHeight(Activity activity) {
        if (!isNavigationBarShow(activity)){
            return 0;
        }
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }


    public static int getSceenHeight(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getHeight()+getNavigationBarHeight(activity);
    }




    public static long getFileSize(File file) throws Exception
    {
        long size = 0;
        if (file.exists()){
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        else{
            file.createNewFile();
            Log.e("获取文件大小","文件不存在!");
        }
        return size;
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /*
    * 将时间戳转换为时间
    */
    public static String stampToDate(long lt,String format){
        String res;
        if(TextUtils.isEmpty(format)){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

}
