package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.android.user.api.IResetPasswordCallback;
import com.tuya.smart.android.user.api.IValidateCallback;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.MyCountDownTimer;
import com.ym.traegergill.tools.MyNetTool;
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
 * Created by Administrator on 2017/9/28.
 */

public class EditPasswordActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.save)
    TextView save;
   /* @BindView(R.id.et_cur_password)
    EditText etCurPassword;*/
    @BindView(R.id.et_new_password)
    EditText etNewPassword;

    @BindView(R.id.et_confirm_password)
    EditText etConfirmPassword;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.get_code)
    TextView getCode;
    @BindView(R.id.et_verification_code)
    EditText etVerificationCode;
    private MyCountDownTimer timer;
    private TextWatcher textWatcher;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        unbinder = ButterKnife.bind(this);
        initData();
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
        if (/*TextUtils.isEmpty(etCurPassword.getText()) || */TextUtils.isEmpty(etNewPassword.getText()) || TextUtils.isEmpty(etConfirmPassword.getText())|| TextUtils.isEmpty(etVerificationCode.getText())) {
            save.setEnabled(false);
            save.setTextColor(getResources().getColor(R.color.color9999));
        }else if(etNewPassword.getText().toString().length()<Constants.password_min_len && etNewPassword.getText().toString().length()>Constants.password_max_len ){
            save.setEnabled(false);
            save.setTextColor(getResources().getColor(R.color.color9999));
        } else{
            save.setEnabled(true);
            save.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void initData() {
        title.setText("PASSWORD");
        etEmail.setText(TuyaUser.getUserInstance().getUser().getEmail());

        timer = new MyCountDownTimer(60000, 1000, getCode);

       /* curPassword = getIntent().getStringExtra("curPassword");
        newPassword = getIntent().getStringExtra("newPassword");
        etCurPassword.setText(curPassword);
        etCurPassword.setSelection(curPassword.length());
        etNewPassword.setText(newPassword);
        etNewPassword.setSelection(newPassword.length());*/

    }

    @OnClick({R.id.save,R.id.get_code})
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.save:
                if (!etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
                    showToastError("两次密码输入不一样");
                } else {
                    TuyaUser.getUserInstance().resetEmailPassword(TuyaUser.getUserInstance().getUser().getPhoneCode(),
                            TuyaUser.getUserInstance().getUser().getEmail(),
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
                            TLog("code: " + code + " error:" + error);
                            showToastError("code: " + code + " error:" + error);
                        }
                    });
                }
                break;
            case R.id.get_code:
                //获取邮箱验证码
                TuyaUser.getUserInstance().getEmailValidateCode(TuyaUser.getUserInstance().getUser().getPhoneCode(), TuyaUser.getUserInstance().getUser().getEmail(), new IValidateCallback() {
                    @Override
                    public void onSuccess() {
                        getCode.setEnabled(false);
                        timer.start();
                        showToastSuccess("send success");
                    }
                    @Override
                    public void onError(String code, String error) {
                        TLog("code: " + code + " error:" + error);
                        showToastError("code: " + code + " error:" + error);
                    }
                });

                break;

        }


    }

    private void netUpdatePassword() {
       // String params = "account="+etEmail.getText().toString()+"&password="+etNewPassword.getText().toString();
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
                        Intent intent = new Intent(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
                        getApplicationContext().sendBroadcast(intent);
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
        //MyNetTool.netCrossWithParams(getActivity(), TuyaUser.getUserInstance().getUser().getUid(), URLs.updatePwdWithEmail, params, callback);
    }

    @Override
    public boolean needLogin() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
