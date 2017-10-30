package com.ym.traegergill.tools;

import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/5/15.
 */

public class MyCountDownTimer extends CountDownTimer {
    private TextView btn;

    public MyCountDownTimer(long millisInFuture, long countDownInterval, TextView btn) {
        super(millisInFuture, countDownInterval);
        this.btn = btn;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        btn.setText(millisUntilFinished/1000 + "s");
    }

    @Override
    public void onFinish() {
        btn.setEnabled(true);
        btn.setText("get code");
    }
}
