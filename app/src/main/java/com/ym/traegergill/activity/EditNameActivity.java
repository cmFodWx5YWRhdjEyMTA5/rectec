package com.ym.traegergill.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
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

public class EditNameActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.save)
    TextView save;
    @BindView(R.id.et_first_name)
    EditText etFirstName;
    @BindView(R.id.et_last_name)
    EditText etLastName;
    String firstName, lastName;
    TextWatcher textWatcher;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);
        ButterKnife.bind(this);
        initData();
        initListener();
    }

    private void initListener() {
        textWatcher =  new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkIsChange();

            }
        };
        etFirstName.addTextChangedListener(textWatcher);
        etLastName.addTextChangedListener(textWatcher);
    }

    private void checkIsChange() {
        if(firstName.equals(etFirstName.getText().toString()) && lastName.equals(etLastName.getText().toString())){
            save.setTextColor(getResources().getColor(R.color.color9999));
            save.setEnabled(false);
        }else{
            save.setTextColor(getResources().getColor(R.color.white));
            save.setEnabled(true);
        }

    }

    private void initData() {
        title.setText("NAME");
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        etFirstName.setText(firstName);
        etFirstName.setSelection(firstName.length());
        etLastName.setText(lastName);
        etLastName.setSelection(lastName.length());


    }

    @OnClick(R.id.save)
    public void onViewClicked() {
        showToastSuccess("保存成功!");
    }
    @Override
    public boolean needLogin() {
        return true;
    }
}
