package com.ym.traegergill.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/9.
 */

public class AddDevicesGuidActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.other_status)
    TextView otherStatus;
    @BindView(R.id.next_step)
    TextView nextStep;
    @BindView(R.id.top_step)
    ImageView topStep;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_devices_guid);
        ButterKnife.bind(this);
        SharedPreferencesUtils.getSharedPreferencesUtil(getActivity()).setValue(Constants.ISFIRSTTIME,false);
        init();
    }

    private void init() {
        title.setText(getString(R.string.ty_ec_find_add_device_title));
        otherStatus.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        otherStatus.getPaint().setAntiAlias(true);//抗锯齿
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }

    @OnClick(R.id.next_step)
    public void onViewClicked(View view) {
        getActivity().startActivity(new Intent(getActivity(),SetUpWifiDeviceActivity.class));
        this.finish();
        overridePendingTransition(this,ANIMATE_FORWARD);

    }
    @Override
    public boolean needLogin() {
        return true;
    }
}
