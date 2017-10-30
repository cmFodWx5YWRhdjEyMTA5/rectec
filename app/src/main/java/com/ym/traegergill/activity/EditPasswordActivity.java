package com.ym.traegergill.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.ym.traegergill.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/9/28.
 */

public class EditPasswordActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.save)
    TextView save;
    @BindView(R.id.et_cur_password)
    EditText etCurPassword;
    @BindView(R.id.et_new_password)
    EditText etNewPassword;
    TextWatcher textWatcher;
    @BindView(R.id.et_confirm_password)
    EditText etConfirmPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        ButterKnife.bind(this);
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
        etCurPassword.addTextChangedListener(textWatcher);
        etNewPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);
    }

    private void checkIsInput() {
        if (TextUtils.isEmpty(etCurPassword.getText()) || TextUtils.isEmpty(etNewPassword.getText()) || TextUtils.isEmpty(etConfirmPassword.getText())) {
            save.setEnabled(false);
            save.setTextColor(getResources().getColor(R.color.color9999));
        }else{
            save.setEnabled(true);
            save.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void initData() {
        title.setText("PASSWORD");
       /* curPassword = getIntent().getStringExtra("curPassword");
        newPassword = getIntent().getStringExtra("newPassword");
        etCurPassword.setText(curPassword);
        etCurPassword.setSelection(curPassword.length());
        etNewPassword.setText(newPassword);
        etNewPassword.setSelection(newPassword.length());*/

    }

    @OnClick(R.id.save)
    public void onViewClicked() {
        if(!etCurPassword.getText().toString().equals("123456")){
            showToastError("密码错误");
        }else if (!etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            showToastError("两次密码输入不一样");
        }else{
            showToastSuccess("成功!");
        }
    }
    @Override
    public boolean needLogin() {
        return true;
    }
}
