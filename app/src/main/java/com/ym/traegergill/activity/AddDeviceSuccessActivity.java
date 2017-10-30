package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.db.bean.UserData;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/19.
 */

public class AddDeviceSuccessActivity extends BaseActivity {
    public static String SUCCESS_DEVICE_NAME = "success_device_name";
    public static String SUCCESS_DEVICE_ID = "success_device_id";
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.skip)
    TextView skip;
    @BindView(R.id.et_value)
    EditText etValue;
    private TuyaDevice mTuyaDevice;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_success);
        ButterKnife.bind(this);
        initDb();
        init();
    }

    private void initDb() {
        SQLiteDbUtil dbUtil = SQLiteDbUtil.getSQLiteDbUtil();
        dbUtil.openOrCreateDataBase(this);
        dbUtil.createTable(UserData.class);
        String devId = getIntent().getStringExtra(SUCCESS_DEVICE_ID);
        String uid = TuyaUser.getUserInstance().getUser().getUid();
        List<UserData> datas = dbUtil.query(UserData.class,"dev_id = ? and user = ?",new String[]{devId,uid});
        //数据库中没有该用户下 该设备的数据
        if(datas == null || datas.size() == 0){

        }else{
            dbUtil.delete(UserData.class,"dev_id = " + devId + " and user = " + uid);
        }
        UserData data = new UserData();
        data.setUser(uid);
        data.setDev_id(devId);
        long ret = dbUtil.insert(data);
        if(ret == -1){
            DialogUtil.simpleSmartDialog(getActivity(), "db something error", null);
        }else{
            //DialogUtil.simpleSmartDialog(getActivity(), "新数据插入成功", null);
            TLog("新数据插入成功");
        }
    }

    private void init() {
        title.setText("Add device successfully");
        etValue.setText(getIntent().getStringExtra(SUCCESS_DEVICE_NAME));
        etValue.setSelection(etValue.getText().length());
        mTuyaDevice = new TuyaDevice(getIntent().getStringExtra(SUCCESS_DEVICE_ID));
    }
    @OnClick(R.id.skip)
    public void onViewClicked() {
        if(TextUtils.isEmpty(etValue.getText().toString())){
            DialogUtil.simpleSmartDialog(getActivity(), getString(R.string.cannot_input_empty_string), null);
            return;
        }
        mTuyaDevice.renameDevice(etValue.getText().toString(), new IControlCallback() {
            @Override
            public void onError(String code, String error) {
                //重命名失败
                DialogUtil.simpleSmartDialog(getActivity(), code+" : " + error, null);
            }
            @Override
            public void onSuccess() {
                //重命名成功
                DialogUtil.simpleSmartDialog(getActivity(), getString(R.string.save_success), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                        overridePendingTransition(getActivity(),ANIMATE_SLIDE_BOTTOM_FROM_TOP);
                    }
                });
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this, ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }
}
