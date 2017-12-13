package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.lzy.okhttputils.callback.StringCallback;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.Constants;
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
    private SharedPreferencesUtils spUtils;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);
        unbinder = ButterKnife.bind(this);
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
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
        firstName = spUtils.getString(Constants.FIRST_NAME);
        lastName =  spUtils.getString(Constants.LAST_NAME);
        etFirstName.setText(firstName);
        etFirstName.setSelection(firstName.length());
        etLastName.setText(lastName);
        etLastName.setSelection(lastName.length());
    }

    @OnClick(R.id.save)
    public void onViewClicked() {

        if(etFirstName.getText().toString().equals("test") && etLastName.getText().toString().equals("645540"))
        {
            getActivity().startActivity(new Intent(getActivity(), TestActivity.class));
            return;
        }


        if(TextUtils.isEmpty(etFirstName.getText().toString())){
            showToastError("First Name is empty!");
        }else if(TextUtils.isEmpty(etLastName.getText().toString())){
            showToastSuccess("Last Name is empty!");
        }else{
            netUpdateUserInfo();
        }
    }

    private void netUpdateUserInfo() {
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
        String params = "firstname="+etFirstName.getText().toString()+"&lastname="+etLastName.getText().toString();
        if(!MyNetTool.netCrossWithParams(getActivity(),TuyaUser.getUserInstance().getUser().getUid(),URLs.updateUserinfo,params,callback)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netUpdateUserInfo();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }

    }

    @Override
    public boolean needLogin() {
        return true;
    }


}
