package com.ym.traegergill.activity;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.android.user.api.IResetPasswordCallback;
import com.tuya.smart.android.user.api.IValidateCallback;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.BlurBitmapUtil;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.CountryUtils;
import com.ym.traegergill.tools.MyCountDownTimer;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.RegularUtils;
import com.ym.traegergill.tools.SharedPreferencesUtils;
import com.ym.traegergill.tuya.utils.DialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

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
    private String mCountryName, mCountryCode;
    private TextWatcher textWatcher;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_psw);
        ButterKnife.bind(this);
        init();
    }


    private void init() {
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

        timer = new MyCountDownTimer(60000, 1000, getCode);
        Bitmap initBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg);
        Bitmap blurBitmap = BlurBitmapUtil.blurBitmap(this, initBitmap, 15f);
        backg.setImageBitmap(blurBitmap);
        startIntroAnimation(back, 1, -1);
        startIntroAnimation(scrollMainView, 2, 1);
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
                checkIsInput();

            }
        };
        etVerificationCode.addTextChangedListener(textWatcher);
        etNewPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);
    }
    private void checkIsInput() {
        if (/*TextUtils.isEmpty(etCurPassword.getText()) || */
                TextUtils.isEmpty(etNewPassword.getText()) ||
                        TextUtils.isEmpty(etConfirmPassword.getText())||
                        TextUtils.isEmpty(etVerificationCode.getText())
                ) {
            complete.setEnabled(false);
        } else {
            complete.setEnabled(true);
        }
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
                    //获取邮箱验证码
                    TLog(mCountryCode +"  " + etEmail.getText().toString());
                    TuyaUser.getUserInstance().getEmailValidateCode(mCountryCode, etEmail.getText().toString(), new IValidateCallback() {
                        @Override
                        public void onSuccess() {
                            getCode.setEnabled(false);
                            timer.start();
                            showToastSuccess("send success");
                        }
                        @Override
                        public void onError(String code, String error) {
                            TLog("code: " + code + "error:" + error);
                            showToastError("code: " + code + "error:" + error);
                        }
                    });

                } else {
                    showToastError("email error");
                }
                break;
            case R.id.complete:
                if(etNewPassword.getText().toString().length() < Constants.password_min_len ||
                        etNewPassword.getText().toString().length() > Constants.password_max_len){
                    showToastError(getString(R.string.key_num_not_reasonable));
                }
                else if (!etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
                    showToastError(getString(R.string.key_num_not_uniform));
                } else {
                    TuyaUser.getUserInstance().resetEmailPassword(mCountryCode, etEmail.getText().toString(),
                            etVerificationCode.getText().toString(),
                            etNewPassword.getText().toString(), new IResetPasswordCallback() {
                                @Override
                                public void onSuccess() {
                                    TLog("涂鸦密码更新成功");
                                    SharedPreferencesUtils.getSharedPreferencesUtil(getActivity()).setValue(Constants.ISLOGIN,false);
                                    netUpdatePassword();
                                }
                                @Override
                                public void onError(String code, String error) {
                                    TLog("code: " + code + "error:" + error);
                                }
                            });
                }
                break;
            case R.id.rl_select_country:
                getActivity().startActivityForResult(new Intent(getActivity(), CountryListActivity.class), REQUEST_COUNTRY_CODE);
                overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
        }
    }
    private void netUpdatePassword() {
        /*String params = "account="+etEmail.getText().toString()+"&password="+etNewPassword.getText().toString();*/
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("isFromCache : " + isFromCache+"  json : " + s);
                if(isFromCache){
                    showToastError(getString(R.string.network_error));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(s);
                    TLog(obj.optString("msg"));
                    if (obj.optInt("code") == 200) {
                        showToastSuccess(obj.optString("msg"));
                       /* Intent intent = new Intent(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
                        getApplicationContext().sendBroadcast(intent);*/
                        getActivity().finish();
                    }else{
                        showToastError(obj.optString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onAfter(boolean isFromCache, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                super.onAfter(isFromCache, s, call, response, e);
            }
        };
        HttpParams params = new HttpParams();
        params.put("account",etEmail.getText().toString());
        params.put("password",etNewPassword.getText().toString());
        params.put("code",etVerificationCode.getText().toString());

        if(!MyNetTool.netHttpParams(getActivity(),URLs.BASE + URLs.updatePwdWithEmail,callback,params)){
            DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                    , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    System.exit(0);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    netUpdatePassword();
                                    break;
                            }
                        }
                    }).show();
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
