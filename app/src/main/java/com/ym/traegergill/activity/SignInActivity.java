package com.ym.traegergill.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.gson.Gson;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.tools.BlurBitmapUtil;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.CountryUtils;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.RegularUtils;
import com.ym.traegergill.tools.SharedPreferencesUtils;
import com.ym.traegergill.tuya.utils.ProgressUtil;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/9/27.
 */

public class SignInActivity extends BaseActivity {
    private static final int REQUEST_COUNTRY_CODE = 998;
    @BindView(R.id.backg)
    ImageView backg;
    @BindView(R.id.main_login_view)
    LinearLayout mainLoginView;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.sign_in)
    TextView signIn;
    @BindView(R.id.forget)
    TextView forget;
    @BindView(R.id.tv_country)
    TextView tvCountry;
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.rl_select_country)
    RelativeLayout rlSelectCountry;
    private SharedPreferencesUtils spUtils;
    String mCountryName,mCountryCode;
    TextWatcher textWatcher = new TextWatcher() {
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
        initAnim();
        initInfo();
        initListener();
    }

    private void initInfo() {
        etEmail.setText(spUtils.getString(Constants.EMAIL,""));
        etEmail.setSelection(etEmail.getText().toString().length());
        if(!TextUtils.isEmpty(etEmail.getText().toString())){
            etPassword.requestFocus();
        }
        // 初始化国家/地区信息
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

    private void initAnim() {
        Bitmap initBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg);
        Bitmap blurBitmap = BlurBitmapUtil.blurBitmap(this, initBitmap, 15f);
        backg.setImageBitmap(blurBitmap);
        startIntroAnimation(back, 1, -1);
        startIntroAnimation(mainLoginView, 2, 1);
    }

    private void initListener() {
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
    }

    void checkInPutAndUpDateUI() {
        String emailVal = etEmail.getText().toString();
        String passwordVal = etPassword.getText().toString();
        if (!TextUtils.isEmpty(emailVal) && !TextUtils.isEmpty(passwordVal)) {
            signIn.setEnabled(true);
            return;
        }
        signIn.setEnabled(false);
    }

    private void startIntroAnimation(View temp, int index, int up) {
        int dis = OUtil.getSceenHeight(getActivity());
        temp.setTranslationY(up * dis);
        temp.animate()
                .translationY(0)
                .setDuration(500)
                .setStartDelay(100 * index);
    }

    @OnClick({R.id.sign_in, R.id.forget,R.id.rl_select_country})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sign_in:
                login();
                break;
            case R.id.forget:
                getActivity().startActivity(new Intent(getActivity(), ForgetPswActivity.class));
                overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
            case R.id.rl_select_country:
                getActivity().startActivityForResult(new Intent(getActivity(), CountryListActivity.class),REQUEST_COUNTRY_CODE);
                overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
        }
    }

    private void login() {
        //邮箱密码登陆
        ProgressUtil.showLoading(getActivity(), R.string.logining);

        if(RegularUtils.isEmail(etEmail.getText().toString())){
            TuyaUser.getUserInstance().loginWithEmail(mCountryCode,
                    etEmail.getText().toString(),
                    etPassword.getText().toString(),
                    new ILoginCallback() {
                        @Override
                        public void onSuccess(User user) {
                            OUtil.TLog("login user   : " + new Gson().toJson(user));
                            ProgressUtil.hideLoading();
                            ProgressUtil.hideLoading();
                            spUtils.setValue(Constants.ISLOGIN, true);
                            spUtils.setValue(Constants.EMAIL,user.getEmail());
                            Intent intent = new Intent(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
                            getApplicationContext().sendBroadcast(intent);
                            OUtil.TLog("login success");
                            //OUtil.toastSuccess(getActivity(),"success");
                            getActivity().finish();
                        }

                        @Override
                        public void onError(String s, String s1) {
                            ProgressUtil.hideLoading();
                            OUtil.TLog(s1);
                            OUtil.toastError(getActivity(),s1);
                        }
                    });
        }else{
            showToastError("email error!");

        }
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
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
    @Override
    public boolean needLogin() {
        return false;
    }
}
