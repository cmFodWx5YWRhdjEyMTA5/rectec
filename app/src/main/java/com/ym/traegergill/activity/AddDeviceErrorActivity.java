package com.ym.traegergill.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.ym.traegergill.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/19.
 */

public class AddDeviceErrorActivity extends BaseActivity {

    public static String ERRORTEXT = "error_text";

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.error_text)
    TextView errorText;
    @BindView(R.id.retry)
    TextView retry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_error);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText(getString(R.string.ty_ap_error_title));
        errorText.setText(getIntent().getStringExtra(ERRORTEXT));
    }


    @OnClick(R.id.retry)
    public void onViewClicked() {
        startActivity(new Intent(getActivity(),AddDevicesGuidActivity.class));
        this.finish();
        overridePendingTransition(this,BaseActivity.ANIMATE_BACK);

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }
}
