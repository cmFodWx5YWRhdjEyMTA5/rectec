package com.ym.traegergill.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuya.smart.sdk.TuyaSdk;
import com.ym.traegergill.R;
import com.ym.traegergill.tools.CountryUtils;
import com.ym.traegergill.tools.MyCountDownTimer;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.RegularUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/9/27.
 */

public class CreateAccountActivity extends BaseActivity {
    private static final int REQUEST_COUNTRY_CODE = 998;
    @BindView(R.id.main_login_view)
    LinearLayout mainLoginView;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_first_name)
    EditText etFirstName;
    @BindView(R.id.et_last_name)
    EditText etLastName;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.create)
    TextView create;
    @BindView(R.id.et_password2)
    EditText etPassword2;
    @BindView(R.id.et_verification_code)
    EditText etVerificationCode;

    TextWatcher textWatcher;
    @BindView(R.id.get_code)
    TextView getCode;
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.rl_select_country)
    RelativeLayout rlSelectCountry;
    private MyCountDownTimer timer;
    String mCountryName,mCountryCode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        ButterKnife.bind(this);
        initStatusBar(this);
        init();
        initListener();
    }


    private void initListener() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkInPutAndUpDateUI();
            }
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etPassword2.addTextChangedListener(textWatcher);
        etVerificationCode.addTextChangedListener(textWatcher);
        etFirstName.addTextChangedListener(textWatcher);
        etLastName.addTextChangedListener(textWatcher);
    }

    void checkInPutAndUpDateUI() {
        String emailVal = etEmail.getText().toString();
        String passwordVal = etPassword.getText().toString();
        String password2Val = etPassword2.getText().toString();
        String firstNameVal = etFirstName.getText().toString();
        String lastNameVal = etLastName.getText().toString();
        String verificationCodeVal = etVerificationCode.getText().toString();
        /*if (RegularUtils.isEmail(emailVal) && passwordVal.length() > 3) {
            signIn.setEnabled(true);
            return;
        }*/
        //判断是否为空
        if (TextUtils.isEmpty(emailVal)
                || TextUtils.isEmpty(passwordVal)
                || TextUtils.isEmpty(password2Val)
                || TextUtils.isEmpty(firstNameVal)
                || TextUtils.isEmpty(lastNameVal)
                || TextUtils.isEmpty(verificationCodeVal)) {
            create.setEnabled(false);
            return;
        }
        create.setEnabled(true);
    }

    private void init() {
        timer = new MyCountDownTimer(60000, 1000, getCode);
        startIntroAnimation(back, 1, -1);
        startIntroAnimation(mainLoginView, 3, 1);
        initCountryInfo();
    }

    // 初始化国家/地区信息
    private void initCountryInfo() {
        String countryKey = CountryUtils.getCountryKey(TuyaSdk.getApplication());
        if (!TextUtils.isEmpty(countryKey)) {
            mCountryName = CountryUtils.getCountryTitle(countryKey);
            mCountryCode = CountryUtils.getCountryNum(countryKey);
        } else {
            countryKey = CountryUtils.getCountryDefault(TuyaSdk.getApplication());
            mCountryName = CountryUtils.getCountryTitle(countryKey);
            mCountryCode = CountryUtils.getCountryNum(countryKey);
        }
        tvContent.setText(mCountryName + " +" + mCountryCode);
    }

    private void startIntroAnimation(View temp, int index, int up) {
        int dis = OUtil.getSceenHeight(getActivity());
        temp.setTranslationY(up * dis);
        temp.animate()
                .translationY(0)
                .setDuration(500)
                .setStartDelay(100 * index);
    }

    @OnClick({R.id.create, R.id.get_code,R.id.rl_select_country})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.create:
                String emailVal = etEmail.getText().toString();
                String passwordVal = etPassword.getText().toString();
                String password2Val = etPassword2.getText().toString();
                String firstNameVal = etFirstName.getText().toString();
                String lastNameVal = etLastName.getText().toString();
                String verificationCodeVal = etVerificationCode.getText().toString();
                if (!passwordVal.equals(password2Val)) {
                    showToastError("两次密码输入不一致");
                } else if (!RegularUtils.isEmail(emailVal)) {
                    showToastError("邮箱格式有误");
                } else {
                    showToastSuccess("成功!");
                }


                break;
            case R.id.get_code:
                if (RegularUtils.isEmail(etEmail.getText().toString())) {
                    getCode.setEnabled(false);
                    timer.start();
                    // todo something
                } else {
                    showToastError("邮箱格式有误");
                }

                break;
            case R.id.rl_select_country:
                getActivity().startActivityForResult(new Intent(getActivity(), CountryListActivity.class),REQUEST_COUNTRY_CODE);
                overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;

        }


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
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
