package com.ym.traegergill.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;

import com.ym.traegergill.R;
import com.ym.traegergill.view.chart.LineCardOne;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/10/30.
 */

public class TempChartActivity extends BaseActivity {
    @BindView(R.id.chart_card)
    CardView chartCard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_chart);
        ButterKnife.bind(this);
        (new LineCardOne(chartCard, this)).init();
    }
}
