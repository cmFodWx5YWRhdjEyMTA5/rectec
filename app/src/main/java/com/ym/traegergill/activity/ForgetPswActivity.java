package com.ym.traegergill.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.tools.BlurBitmapUtil;
import com.ym.traegergill.tools.MyCountDownTimer;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.RegularUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/11.
 */

public class ForgetPswActivity extends BaseActivity {
    private static final int REQUEST_COUNTRY_CODE = 998;
    @BindView(R.id.backg)
    ImageView backg;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_verification_code)
    EditText etVerificationCode;
    @BindView(R.id.et_new_password)
    EditText etNewPassword;
    @BindView(R.id.et_confirm_password)
    EditText etConfirmPassword;

    @BindView(R.id.main_login_view)
    LinearLayout mainLoginView;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.get_code)
    TextView getCode;
    @BindView(R.id.complete)
    TextView complete;
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.rl_select_country)
    RelativeLayout rlSelectCountry;
    @BindView(R.id.scroll_main_view)
    ScrollView scrollMainView;
    private MyCountDownTimer timer;
    String mCountryName, mCountryCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_psw);
        ButterKnife.bind(this);
        init();
    }


    private void init() {
        timer = new MyCountDownTimer(60000, 1000, getCode);

        Bitmap initBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg);
        Bitmap blurBitmap = BlurBitmapUtil.blurBitmap(this, initBitmap, 15f);
        backg.setImageBitmap(blurBitmap);
        startIntroAnimation(back, 1, -1);
        startIntroAnimation(scrollMainView, 2, 1);
    }

    private void startIntroAnimation(View temp, int index, int up) {
        int dis = OUtil.getSceenHeight(getActivity());
        temp.setTranslationY(up * dis);
        temp.animate()
                .translationY(0)
                .setDuration(500)
                .setStartDelay(100 * index);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }


    @OnClick({R.id.get_code, R.id.complete, R.id.rl_select_country})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.get_code:
                if (RegularUtils.isEmail(etEmail.getText().toString())) {
                    getCode.setEnabled(false);
                    timer.start();
                    // todo something
                } else {
                    showToastError("邮箱格式有误");
                }
                break;
            case R.id.complete:
                break;
            case R.id.rl_select_country:
                getActivity().startActivityForResult(new Intent(getActivity(), CountryListActivity.class), REQUEST_COUNTRY_CODE);
                overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_COUNTRY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    mCountryName = data.getStringExtra(CountryListActivity.COUNTRY_NAME);
                    mCountryCode = data.getStringExtra(CountryListActivity.PHONE_CODE);
                    tvContent.setText(mCountryName + " +" + mCountryCode);
                }
                break;

        }
    }
}
