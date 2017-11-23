package com.ym.traegergill.activity;

import android.app.Activity;
import android.content.DialogInterface;
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

import com.google.gson.Gson;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cache.CacheMode;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.android.user.api.IRegisterCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.CountryUtils;
import com.ym.traegergill.tools.MyCountDownTimer;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.RegularUtils;
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
        unbinder = ButterKnife.bind(this);
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
       /* etEmail.setText("574999723@qq.com");
        etPassword.setText("123456");
        etPassword2.setText("123456");
        etFirstName.setText("ouyang");
        etLastName.setText("xiao111");*/

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
                    netMyCreateAccount();
                    //showToastSuccess("成功!");
                }
                break;
            case R.id.get_code:
                if (RegularUtils.isEmail(etEmail.getText().toString())) {


                    // todo something
                    netCode(etEmail.getText().toString());
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

    private void netMyCreateAccount() {
        String emailVal = etEmail.getText().toString();
        String passwordVal = etPassword.getText().toString();
        String firstNameVal = etFirstName.getText().toString();
        String lastNameVal = etLastName.getText().toString();
        String verificationCodeVal = etVerificationCode.getText().toString();
        HttpParams params = new HttpParams();
        params.put("account", emailVal);
        params.put("password", passwordVal);
        params.put("firstname", firstNameVal);
        params.put("lastname", lastNameVal);
        params.put("code", verificationCodeVal);
        TLog("params : " + new Gson().toJson(params));

        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        TLog("注册自己的服务器 SUCCESS");
                        createAccountInTuya(obj.optJSONObject("content").optString("token"));
                    }else{
                        TLog("注册自己的服务器 FAIL");
                        showToastSuccess("create fail");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToastError("create fail : " + e.getMessage());
                }
            }

            @Override
            public void onAfter(boolean isFromCache, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                super.onAfter(isFromCache, s, call, response, e);
            }
        };

        if(!MyNetTool.netHttpParams(getActivity(),URLs.signEmailUser,callback,params)){
            DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                    , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    System.exit(0);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    netMyCreateAccount();
                                    break;
                            }
                        }
                    }).show();
        }
    }

    private void createAccountInTuya(final String token) {
        //涂鸦 邮箱密码注册
        String emailVal = etEmail.getText().toString();
        String passwordVal = etPassword.getText().toString();
        TuyaUser.getUserInstance().registerAccountWithEmail(mCountryCode+"", emailVal,passwordVal, new IRegisterCallback() {
            @Override
            public void onSuccess(User user) {
                updateToMyAccount(user.getUid(),token);
                TLog("create account  : " + new Gson().toJson(user));
            }
            @Override
            public void onError(String code, String error) {
                showToastError("create fail");
                TLog(code + " : " + error);
            }
        });
    }

    private void updateToMyAccount(final String uid ,final String token) {
        HttpParams params = new HttpParams();
        params.put("tuyaUid", uid);
        params.put("token", token);
        TLog("params : " + new Gson().toJson(params));
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        Intent intent = new Intent(getActivity(),SignInActivity.class);
                        startActivity(intent);
                        finish();
                        showToastSuccess("success");
                    }else{
                        showToastSuccess("error");
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

        if(!MyNetTool.netHttpParams(getActivity(),URLs.updateTuya,callback,params)){
            DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                    , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    System.exit(0);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    updateToMyAccount(uid,token);
                                    break;
                            }
                        }
                    }).show();
        }

    }

    private void netCode(final String email) {
        HttpParams params = new HttpParams();
        params.put("email", email);
        TLog("params : " + new Gson().toJson(params));
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        timer.start();
                        getCode.setEnabled(false);
                        showToastSuccess(obj.optString("msg"));
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

        if(!MyNetTool.netHttpParams(getActivity(),URLs.sendEmailCode,callback,params)){
            DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                    , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    System.exit(0);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    netCode(email);
                                    break;
                            }
                        }
                    }).show();
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
