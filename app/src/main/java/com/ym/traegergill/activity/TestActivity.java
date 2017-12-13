package com.ym.traegergill.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ym.traegergill.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/12/12.
 */

public class TestActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.et_value)
    EditText etValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        title.setText("TEST");
    }

    public void testChart(View view) {
        String devId = "002003825ccf7fff0e81";
        if(!TextUtils.isEmpty(etValue.getText())){
            devId = etValue.getText().toString();
        }
        showToastSuccess("devId : " + devId);
        getActivity().startActivity(new Intent(getActivity(), TempChartActivity.class).putExtra(RemoteControlActivity.INTENT_DEVID, devId));
    }

    public void testTalk(View view) {
        getActivity().startActivity(new Intent(getActivity(), TalkActivity.class));
    }
}
