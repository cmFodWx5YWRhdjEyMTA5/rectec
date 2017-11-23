package com.ym.traegergill.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.db.bean.UserData;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.RegularUtils;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.view.EditPickerDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/10.
 */

public class AlarmsActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.grill_check)
    CheckBox grillCheck;
    @BindView(R.id.probe_A_check)
    CheckBox probeACheck;
    @BindView(R.id.probe_B_check)
    CheckBox probeBCheck;
    @BindView(R.id.grill_Range)
    TextView grillRange;
    @BindView(R.id.probe_temp)
    TextView probeTemp;
    @BindView(R.id.set_a_temp)
    LinearLayout setATemp;
    @BindView(R.id.set_b_temp)
    LinearLayout setBTemp;
    @BindView(R.id.set_grill_range)
    LinearLayout setGrillRange;
    EditPickerDialog numPickerDialog;
    @BindView(R.id.tempRange)
    TextView tempRange;
    @BindView(R.id.unitRange)
    TextView unitRange;
    @BindView(R.id.tempA)
    TextView tempA;
    @BindView(R.id.unitA)
    TextView unitA;
    @BindView(R.id.tempB)
    TextView tempB;
    @BindView(R.id.unitB)
    TextView unitB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText("Alarms");
        String g_title = "Grill Range: ";
        String g_content = "Alarm will sound if grill temp deviation from the set point exceed the value you select.";
        setupText(g_title, g_content, grillRange);
        String p_title = "Probe Temp: ";
        String p_content = "Alarm will sound when the temperature you select is reached.";
        setupText(p_title, p_content, probeTemp);
        initListener();
        numPickerDialog = new EditPickerDialog(getActivity(),"",true);
        numPickerDialog.setTitle("Set Temp");
        numPickerDialog.setCallBackListen(new EditPickerDialog.callBackListen() {
            @Override
            public void callback(String text) {
                TextView tv = ((TextView)numPickerDialog.getTag());
                tv.setText(text);
                switch (tv.getId()){
                    case R.id.tempRange:
                        data.setGill_range(Integer.parseInt(text));
                        break;
                    case R.id.tempA:
                        data.setProbe_a_temp(Integer.parseInt(text));
                        break;
                    case R.id.tempB:
                        data.setProbe_b_temp(Integer.parseInt(text));
                        break;
                }
                dbUtil.update(data, data.getId());
                sendBroadcast(TraegerGillBroadcastHelper.ACTION_UPDATE_ALARMS_SETTING);
            }
        });
        numPickerDialog.create();
        setupSetting();
    }
    String mDevId;
    String uid;
    SQLiteDbUtil dbUtil;
    UserData data;
    private void setupSetting() {
        if(dbUtil == null){
            dbUtil = SQLiteDbUtil.getSQLiteDbUtil();
            dbUtil.createTable(UserData.class);
            mDevId = getIntent().getStringExtra(RemoteControlActivity.INTENT_DEVID);
            uid = TuyaUser.getUserInstance().getUser().getUid();
        }
        List<UserData> datas = dbUtil.query(UserData.class,"dev_id = ? and user = ?",new String[]{mDevId,uid});
        //数据库中没有该用户下 该设备的数据
        if(datas == null || datas.size() == 0){
            UserData data = new UserData();
            data.setUser(uid);
            data.setDev_id(mDevId);
            long ret = dbUtil.insert(data);
            if(ret == -1){
                TLog("新数据插入失败");
                DialogUtil.simpleSmartDialog(getActivity(), "db something error", null);
            }else{
               // DialogUtil.simpleSmartDialog(getActivity(), "新数据插入成功", null);
                TLog("新数据插入成功");

                setupSetting();
                return;
            }
        }else{
            data = datas.get(0);
            if(data!=null){
                tempA.setText(data.getProbe_a_temp()+"");
                tempB.setText(data.getProbe_b_temp()+"");
                tempRange.setText(data.getGill_range()+"");
                grillCheck.setChecked(data.isFlag());
                probeACheck.setChecked(data.isA_temp_open());
                probeBCheck.setChecked(data.isB_temp_open());
            }
        }

    }

    private void initListener() {
        grillCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    setGrillRange.setVisibility(View.VISIBLE);
                } else {
                    setGrillRange.setVisibility(View.GONE);
                }
                data.setFlag(b);
                dbUtil.update(data, data.getId());
                sendBroadcast(TraegerGillBroadcastHelper.ACTION_UPDATE_ALARMS_SETTING);
            }
        });
        probeACheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    setATemp.setVisibility(View.VISIBLE);
                } else {
                    setATemp.setVisibility(View.GONE);
                }
                data.setA_temp_open(b);
                dbUtil.update(data, data.getId());
                sendBroadcast(TraegerGillBroadcastHelper.ACTION_UPDATE_ALARMS_SETTING);
            }
        });

        probeBCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    setBTemp.setVisibility(View.VISIBLE);
                } else {
                    setBTemp.setVisibility(View.GONE);
                }
                data.setB_temp_open(b);
                dbUtil.update(data, data.getId());
                sendBroadcast(TraegerGillBroadcastHelper.ACTION_UPDATE_ALARMS_SETTING);
            }
        });

    }

    private void sendBroadcast(String actionUpdateAlarmsSetting) {
        Intent intent = new Intent(actionUpdateAlarmsSetting);
        sendBroadcast(intent);
    }

    private void setupText(String title, String content, TextView text) {
        SpannableStringBuilder spannableString = new SpannableStringBuilder();
        spannableString.append(title);
        spannableString.append(content);
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD_ITALIC);
        spannableString.setSpan(styleSpan, 0, title.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        text.setText(spannableString);
    }

    @Override
    public boolean needLogin() {
        return true;
    }

    @OnClick({R.id.set_a_temp, R.id.set_b_temp, R.id.set_grill_range})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.set_a_temp:
                numPickerDialog.setText(tempA.getText().toString());
                numPickerDialog.setTag(tempA);
                numPickerDialog.show();
                break;
            case R.id.set_b_temp:
                numPickerDialog.setText(tempB.getText().toString());
                numPickerDialog.setTag(tempB);
                numPickerDialog.show();
                break;
            case R.id.set_grill_range:
                numPickerDialog.setText(tempRange.getText().toString());
                numPickerDialog.setTag(tempRange);
                numPickerDialog.show();
                break;
        }
    }
}
