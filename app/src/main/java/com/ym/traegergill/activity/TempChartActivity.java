package com.ym.traegergill.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.google.gson.Gson;
import com.tuya.smart.sdk.TuyaSmartRequest;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.ym.traegergill.R;
import com.ym.traegergill.bean.ChartPointBean;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;
import com.ym.traegergill.view.ChartUnitPickerDialog;
import com.ym.traegergill.view.StringPickerDialog;
import com.ym.traegergill.view.chart.MyMarkerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/30.
 */

public class TempChartActivity extends BaseActivity implements OnChartGestureListener, OnChartValueSelectedListener {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.chart1)
    LineChart mChart;
    String mDevId;
    List<ChartPointBean> mData;
    /*    @BindView(R.id.radioGroup)
        RadioGroup radioGroup;*/
    @BindView(R.id.tv_start_time)
    TextView tvStartTime;
    @BindView(R.id.tv_end_time)
    TextView tvEndTime;
    @BindView(R.id.tv_unit)
    TextView tvUint;
    ChartUnitPickerDialog uintPickerDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_chart);
        initStatusBar(this);
        unbinder = ButterKnife.bind(this);
        title.setText(getString(R.string.Temperature_Chart));
        initData();
        initChart();
    }

    int type = -1;
    int firstType;
    int offset;
    int limit;
    boolean isOpened;
    boolean isOver;
    long startTime;
    long endTime;
    int realType;

    private void initData() {
        mData = new ArrayList<>();
        mDevId = getIntent().getStringExtra(RemoteControlActivity.INTENT_DEVID);
        offset = 0;
        firstType = 0;
        startTime = 0;
        endTime = 0;
        isOpened = false;
        isOver = false;
        limit = 30;
        //test();
        ProgressUtil.showLoading(getActivity(), getString(R.string.loading));
        getData(offset, limit);
    }

    private void getData(int page, final int limit) {
        if(isFinishing() || isDestroyed()){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("devId", mDevId);
        map.put("dpIds", "1,102");
        map.put("offset", page);
        map.put("limit", limit);
        map.put("sortType", "DESC");
        TuyaSmartRequest.getInstance().requestWithApiName("m.smart.operate.log", "2.0", map, new IRequestCallback() {
            @Override
            public void onSuccess(Object result) {
                OUtil.TLog(new Gson().toJson(result));
                try {
                    JSONObject jsonObj = new JSONObject(new Gson().toJson(result));
                    boolean hasNext = jsonObj.optBoolean("hasNext");//是否有下一页
                    JSONArray dps = jsonObj.optJSONArray("dps");//数据
                    for (int i = 0; i < dps.length(); i++) {
                        JSONObject dp = dps.optJSONObject(i);
                        int dpId = dp.optInt("dpId");
                        String value = dp.optString("value");
                        long timeStamp = dp.optLong("timeStamp");
                        if (i == 0 && offset == 0) {//记录下第一个出现的dp类型
                            if (dpId == 1 && value.equals("true")) {//on
                                firstType = 1;
                            } else if (dpId == 1 && value.equals("false")) {//off
                                endTime = timeStamp;
                                firstType = 2;
                            } else if (dpId == 102) {//value
                                firstType = 3;
                                //直接算是开始了 加入数据列表
                                endTime = timeStamp;
                                mData.add(new ChartPointBean(timeStamp, Integer.parseInt(value)));
                            }
                        } else {
                            if (firstType == 1) {
                                isOver = true;
                                break;
                            } else if (firstType == 2) {//off
                                if (dpId == 102) {//value
                                    mData.add(new ChartPointBean(timeStamp, Integer.parseInt(value)));
                                } else if (dpId == 1) {
                                    isOver = true;
                                    startTime = timeStamp;
                                    break;
                                }
                            } else if (firstType == 3) {
                                if (dpId == 102) {//value
                                    mData.add(new ChartPointBean(timeStamp, Integer.parseInt(value)));
                                } else if (dpId == 1) {
                                    isOver = true;
                                    startTime = timeStamp;
                                    break;
                                }
                            }
                        }
                    }
                    if (!isOver && hasNext) {
                        getData(++TempChartActivity.this.offset, limit);
                    } else {
                        OUtil.TLog("mData : " + new Gson().toJson(mData));
                        ProgressUtil.hideLoading();
                        if (mData.size() > 0){
                            setData2(mData, true);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(String errorCode, String errorMsg) {
                ProgressUtil.hideLoading();
                if(isFinishing() || isDestroyed()){
                    showToastError("errorMsg :" + errorMsg);
                }
                OUtil.TLog("errorCode" + errorCode + "  errorMsg : " + errorMsg);
            }
        });
    }

    void test() {
        String result = "{\"hasNext\":true,\"total\":556,\"dps\":[{\"timeStr\":\"2017-12-08 15:34:44\",\"value\":\"285\",\"dpId\":102,\"timeStamp\":1512718484},{\"timeStr\":\"2017-12-08 15:34:42\",\"value\":\"290\",\"dpId\":102,\"timeStamp\":1512718482},{\"timeStr\":\"2017-12-08 15:34:41\",\"value\":\"295\",\"dpId\":102,\"timeStamp\":1512718481},{\"timeStr\":\"2017-12-08 15:34:39\",\"value\":\"300\",\"dpId\":102,\"timeStamp\":1512718479},{\"timeStr\":\"2017-12-08 15:34:39\",\"value\":\"305\",\"dpId\":102,\"timeStamp\":1512718479},{\"timeStr\":\"2017-12-08 15:34:38\",\"value\":\"310\",\"dpId\":102,\"timeStamp\":1512718478},{\"timeStr\":\"2017-12-08 15:34:37\",\"value\":\"315\",\"dpId\":102,\"timeStamp\":1512718477},{\"timeStr\":\"2017-12-08 15:32:14\",\"value\":\"350\",\"dpId\":102,\"timeStamp\":1512718334},{\"timeStr\":\"2017-12-08 15:32:05\",\"value\":\"290\",\"dpId\":102,\"timeStamp\":1512718325},{\"timeStr\":\"2017-12-08 15:20:29\",\"value\":\"285\",\"dpId\":102,\"timeStamp\":1512717629},{\"timeStr\":\"2017-12-08 15:20:27\",\"value\":\"290\",\"dpId\":102,\"timeStamp\":1512717627},{\"timeStr\":\"2017-12-08 15:20:22\",\"value\":\"300\",\"dpId\":102,\"timeStamp\":1512717622},{\"timeStr\":\"2017-12-08 15:20:19\",\"value\":\"295\",\"dpId\":102,\"timeStamp\":1512717619},{\"timeStr\":\"2017-12-08 15:20:17\",\"value\":\"290\",\"dpId\":102,\"timeStamp\":1512717617},{\"timeStr\":\"2017-12-08 15:16:37\",\"value\":\"285\",\"dpId\":102,\"timeStamp\":1512717397},{\"timeStr\":\"2017-12-08 15:16:35\",\"value\":\"345\",\"dpId\":102,\"timeStamp\":1512717395},{\"timeStr\":\"2017-12-08 15:12:16\",\"value\":\"true\",\"dpId\":1,\"timeStamp\":1512717136},{\"timeStr\":\"2017-12-08 15:10:53\",\"value\":\"false\",\"dpId\":1,\"timeStamp\":1512717053},{\"timeStr\":\"2017-12-08 15:10:53\",\"value\":\"350\",\"dpId\":102,\"timeStamp\":1512717053},{\"timeStr\":\"2017-12-08 14:59:10\",\"value\":\"false\",\"dpId\":1,\"timeStamp\":1512716350},{\"timeStr\":\"2017-12-08 14:31:11\",\"value\":\"true\",\"dpId\":1,\"timeStamp\":1512714671},{\"timeStr\":\"2017-12-08 14:31:11\",\"value\":\"350\",\"dpId\":102,\"timeStamp\":1512714671},{\"timeStr\":\"2017-12-08 14:30:43\",\"value\":\"false\",\"dpId\":1,\"timeStamp\":1512714643},{\"timeStr\":\"2017-12-08 14:30:43\",\"value\":\"350\",\"dpId\":102,\"timeStamp\":1512714643},{\"timeStr\":\"2017-12-08 14:25:30\",\"value\":\"false\",\"dpId\":1,\"timeStamp\":1512714330},{\"timeStr\":\"2017-12-08 14:25:12\",\"value\":\"true\",\"dpId\":1,\"timeStamp\":1512714312},{\"timeStr\":\"2017-12-08 14:18:34\",\"value\":\"false\",\"dpId\":1,\"timeStamp\":1512713914},{\"timeStr\":\"2017-12-08 14:17:28\",\"value\":\"350\",\"dpId\":102,\"timeStamp\":1512713848},{\"timeStr\":\"2017-12-08 14:17:26\",\"value\":\"true\",\"dpId\":1,\"timeStamp\":1512713846},{\"timeStr\":\"2017-12-08 14:14:42\",\"value\":\"false\",\"dpId\":1,\"timeStamp\":1512713682}]}\n";
        try {
            JSONObject jsonObj = new JSONObject(result);
            boolean hasNext = jsonObj.optBoolean("hasNext");//是否有下一页
            JSONArray dps = jsonObj.optJSONArray("dps");//数据
            for (int i = 0; i < dps.length(); i++) {
                JSONObject dp = dps.optJSONObject(i);
                int dpId = dp.optInt("dpId");
                String value = dp.optString("value");
                long timeStamp = dp.optLong("timeStamp");
                if (i == 0 && offset == 0) {//记录下第一个出现的dp类型
                    if (dpId == 1 && value.equals("true")) {//on
                        firstType = 1;
                    } else if (dpId == 1 && value.equals("false")) {//off
                        endTime = timeStamp;
                        firstType = 2;
                    } else if (dpId == 102) {//value
                        firstType = 3;
                        //直接算是开始了 加入数据列表
                        endTime = timeStamp;
                        mData.add(new ChartPointBean(timeStamp, Integer.parseInt(value)));
                    }
                } else {
                    if (firstType == 1) {
                        isOver = true;
                        break;
                    } else if (firstType == 2) {//off
                        if (dpId == 102) {//value

                            mData.add(new ChartPointBean(timeStamp, Integer.parseInt(value)));
                        } else if (dpId == 1) {
                            isOver = true;
                            startTime = timeStamp;
                            break;
                        }
                    } else if (firstType == 3) {
                        if (dpId == 102) {//value
                            mData.add(new ChartPointBean(timeStamp, Integer.parseInt(value)));
                        } else if (dpId == 1) {
                            isOver = true;
                            startTime = timeStamp;
                            break;
                        }
                    }
                }
            }
            if (!isOver && hasNext) {
                getData(++TempChartActivity.this.offset, limit);
                //showToastSuccess("json not ok");
            } else {
                //setData(65, 600);
                OUtil.TLog("mData : " + new Gson().toJson(mData));
                ProgressUtil.hideLoading();
                if (mData.size() > 0)
                    setData2(mData, true);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initChart() {
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.setDoubleTapToZoomEnabled(false);//双击放大失效
        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color
        mChart.setBackgroundColor(ContextCompat.getColor(this, R.color.tab_bg));

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(1f, 1f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);// 设置X轴的位置
        xAxis.setAxisLineColor(ContextCompat.getColor(this, R.color.color9999));
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.color9999));
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String inner = "/s";
                float valueTemp = value;
                int timeOffSet = 1;
                switch (type) {
                    case Constants.CHART_TYPE_SECONDS:
                        //seconds
                        inner = "/s";
                        return (int) value * timeOffSet + inner;
                    //break;
                    case Constants.CHART_TYPE_10MINS:
                        //10mins 以内 10s为单位
                        valueTemp = value * 10;
                        inner = "/s";
                        break;
                    case Constants.CHART_TYPE_30MINS:
                        //30mins 以内 30s为单位
                        valueTemp = value * 30 / 60.0f;
                        inner = "/min";
                        break;
                    case Constants.CHART_TYPE_HOURS:
                        //60mins 以内 60s为单位
                        valueTemp = value * 60 / 60.0f;
                        inner = "/min";
                        break;
                    case Constants.CHART_TYPE_3HOURS:
                        //180mins 以内 300s为单位
                        valueTemp = value * 300 / 60.0f;
                        inner = "/min";
                        break;
                    case Constants.CHART_TYPE_ELSE:
                        //超出180mins  暂时600s为单位
                        valueTemp = value * 600 / 60.0f;
                        inner = "/min";
                        break;
                }
               if((valueTemp-((int)valueTemp))<=0){
                   return (int)valueTemp + inner;
                }
                return valueTemp + inner;
            }
        });
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line


        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        /*LimitLine ll1 = new LimitLine(620f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(120f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setTypeface(tf);*/

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
      /*  leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);*/
        leftAxis.setAxisMaximum(650f);
        leftAxis.setAxisMinimum(100f);
        leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setAxisLineColor(ContextCompat.getColor(this, R.color.color9999));
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.color9999));
        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        mChart.getAxisRight().setEnabled(false);
        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
        //setData(65, 600);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(1000);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.WHITE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        // // dont forget to refresh the drawing
        // mChart.invalidate();

    }
    //相差 118S mData json
    //[{"timeStamp":1510308040,"value":395},{"timeStamp":1510307994,"value":415},{"timeStamp":1510307994,"value":420},{"timeStamp":1510307993,"value":425},{"timeStamp":1510307973,"value":430},{"timeStamp":1510307926,"value":390}]
    //704 s
    //[{"timeStamp":1510309559,"value":375},{"timeStamp":1510309266,"value":445},{"timeStamp":1510309138,"value":345},{"timeStamp":1510308949,"value":290},{"timeStamp":1510308894,"value":425},{"timeStamp":1510308858,"value":400}]

    private void setData2(List<ChartPointBean> mData, boolean isFirst) {
        if(isFinishing() || isDestroyed()){
            return;
        }
        if (endTime == 0) {
            endTime = mData.get(0).getTimeStamp();
        }
        if (startTime == 0) {
            startTime = mData.get(mData.size() - 1).getTimeStamp();
        }
        //OUtil.stampToDate(startTime*1000,"");
        tvStartTime.setText(OUtil.stampToDate(startTime * 1000, ""));
        tvEndTime.setText(OUtil.stampToDate(endTime * 1000, ""));
        long diff = (endTime - startTime);//前后相差的秒数
        if(diff == 0){
            return;
        }
        OUtil.TLog("开始结束时间相差 : " + diff + " s");
        //showToastSuccess("本次烤肉时间 : " + diff + " s");
        if (isFirst) {
            if (diff < 60) {
                this.type = Constants.CHART_TYPE_SECONDS;
            } else if (diff < 60 * 10) {
                this.type = Constants.CHART_TYPE_10MINS;
            } else if (diff < 60 * 30) {
                this.type = Constants.CHART_TYPE_30MINS;
            } else if (diff < 60 * 60) {
                this.type = Constants.CHART_TYPE_HOURS;
            } else if (diff < 60 * 60 * 3) {
                this.type = Constants.CHART_TYPE_3HOURS;
            } else {
                this.type = Constants.CHART_TYPE_ELSE;
            }
            realType = this.type;
        }
        int timeOffSet = 1;
        switch (this.type) {
            case Constants.CHART_TYPE_SECONDS:
                //1mins 以内 1s为单位
                timeOffSet = 1;
                break;
            case Constants.CHART_TYPE_10MINS:
                //10mins 以内 10s为单位
                timeOffSet = 10;
                break;
            case Constants.CHART_TYPE_30MINS:
                //30mins 以内 30s为单位
                timeOffSet = 30;
                break;
            case Constants.CHART_TYPE_HOURS:
                //60mins 以内 60s为单位
                timeOffSet = 60;
                break;
            case Constants.CHART_TYPE_3HOURS:
                //180mins 以内 300s为单位
                timeOffSet = 300;
                break;
            case Constants.CHART_TYPE_ELSE:
                //超出180mins  暂时600s为单位
                timeOffSet = 600;
                break;
        }
        //setSeconds(diff);
        setByOffset(diff, timeOffSet);
        //mChart.invalidate();
    }

    private void setByOffset(long diff, int timeOffSet) {
        diff = diff / timeOffSet + 1;
        boolean nextStar = true;
        ArrayList<Entry> values = new ArrayList<Entry>();
        int index = mData.size() - 1;
        for (long lastTime = 0; lastTime <= diff; lastTime++) {
            if(index > 0){
                ChartPointBean indexBean = mData.get(index);
                float val = (float) indexBean.getValue();
                ChartPointBean nextIndexBean = mData.get(index-1);
                double indexTime = (indexBean.getTimeStamp() - startTime) * 1.0 / timeOffSet;
                double nextIndexTime = (nextIndexBean.getTimeStamp() - startTime) * 1.0 / timeOffSet;
                if(indexTime<lastTime){
                    if(nextIndexTime>=lastTime){

                    }else{
                        index--;
                        lastTime -= 1;
                        nextStar = true;
                        continue;
                    }

                }else{

                }
                if (nextStar) {
                    values.add(new Entry(lastTime, val, ContextCompat.getDrawable(this, R.mipmap.star)));
                    nextStar = false;
                } else {
                    values.add(new Entry(lastTime, val));
                }
            }else{
                ChartPointBean indexBean = mData.get(index);
                float val = (float) indexBean.getValue();

                if (nextStar) {
                    values.add(new Entry(lastTime, val, ContextCompat.getDrawable(this, R.mipmap.star)));
                    nextStar = false;
                } else {
                    values.add(new Entry(lastTime, val));
                }
            }


          /*  if (index < 0) {
                index = 0;
            }
            ChartPointBean bean = mData.get(index);
            float val = (float) bean.getValue();
            double time = (bean.getTimeStamp() - startTime) * 1.0 / timeOffSet;
            if (time < lastTime && index != 0) {
                index--;
                lastTime -= 1;
                nextStar = true;
                continue;
            }
            if (nextStar) {
                values.add(new Entry(lastTime, val, ContextCompat.getDrawable(this, R.mipmap.star)));
                nextStar = false;
            } else {
                values.add(new Entry(lastTime, val));
            }
*/
        }
        LineDataSet set1;
       /* if(diff == 0){
            diff = 7;
        }*/
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.zoom(diff / 7f, 1, 0, values.get(0).getY() - 100);
            mChart.getViewPortHandler().setMaximumScaleX(diff / 7f);
        } else {
            // create a dataset and give it a type

            set1 = new LineDataSet(values, "The star means Change Temperature");
            set1.setDrawIcons(true);
            // set the line to be drawn like this "- - - - - -"
/*            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);*/
            set1.setColor(ContextCompat.getColor(this, R.color.chart_pink));
            set1.setCircleColor(ContextCompat.getColor(this, R.color.chart_pink));
            set1.setLineWidth(1.5f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setValueTextColor(ContextCompat.getColor(this, R.color.chart_pink));
            set1.setDrawCircleHole(false);
            set1.setDrawValues(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setHighLightColor(ContextCompat.getColor(this, R.color.chart_pink));
            //set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(R.color.chart_pink);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);
            data.setValueTextColor(Color.WHITE);
            // set data
            mChart.setData(data);
            mChart.zoom(diff / 7f, 1, 0, values.get(0).getY() - 100);
            mChart.getViewPortHandler().setMaximumScaleX(diff / 7f);
        }

    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOver = true;
        //TuyaSmartRequest.getInstance().onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.tv_unit)
    public void onViewClicked() {
        if(uintPickerDialog==null){
            List<String> datas = new ArrayList<>();
            datas.add("1s");
            datas.add("10s");
            datas.add("30s");
            datas.add("1min");
            datas.add("5mins");
            datas.add("10mins");
            uintPickerDialog = new ChartUnitPickerDialog(getActivity(), datas, tvUint.getText().toString());
            uintPickerDialog.create();
            uintPickerDialog.setTitle("Unit Select");
            uintPickerDialog.setCallBackListen(new ChartUnitPickerDialog.callBackListen() {
                @Override
                public void callback(String text) {
                    switch (text) {
                        case "1s":
                            type = Constants.CHART_TYPE_SECONDS;
                            break;
                        case "10s":
                            type = Constants.CHART_TYPE_10MINS;
                            break;
                        case "30s":
                            type = Constants.CHART_TYPE_30MINS;
                            break;
                        case "1min":
                            type = Constants.CHART_TYPE_HOURS;
                            break;
                        case "5mins":
                            type = Constants.CHART_TYPE_3HOURS;
                            break;
                        case "10mins":
                            type = Constants.CHART_TYPE_ELSE;
                    }
                    if(realType<type){
                        type = realType;
                        showToastSuccess("Unit too long..");
                    }
                    if (mData.size() > 0)
                        setData2(mData, false);
                }
            });
            uintPickerDialog.setSelect("1s");
        }
        uintPickerDialog.setSelect(uintPickerDialog.getmDatasByIndex(type));
        uintPickerDialog.show();
    }


}
