package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuya.smart.android.common.utils.NetworkUtil;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.adapter.DevicesRvAdapter;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.presenter.DeviceListPresenter;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;
import com.ym.traegergill.tuya.view.IDeviceListView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/10.
 */

public class DevicesActivity extends BaseActivity implements IDeviceListView {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.add)
    ImageView add;
    @BindView(R.id.device_recycler)
    RecyclerView deviceRecycler;
    DevicesRvAdapter adapter;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    DeviceListPresenter deviceListPresenter;
    @BindView(R.id.list_background_tip)
    RelativeLayout listBackgroundTip;
    @BindView(R.id.tv_remark)
    TextView tvRemark;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        unbinder = ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pullToRefresh != null) {
            pullToRefresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (NetworkUtil.isNetworkAvailable(getActivity())) {
                        deviceListPresenter.getDataFromServer();
                    } else {
                        loadFinish();
                    }
                }
            }, 1000);
        }


    }

    private void init() {
        title.setText(getString(R.string.home_my_device).toUpperCase());
        ProgressUtil.showLoading(this, "loading..");
        new Handler().postDelayed(new Runnable() {//定义延时任务模仿网络请求
            @Override
            public void run() {
                ProgressUtil.hideLoading();
            }
        }, 1000);
        deviceListPresenter = new DeviceListPresenter(this, this);
        initRecycler();
    }

    private void initRecycler() {
        pullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (NetworkUtil.isNetworkAvailable(getActivity())) {
                            deviceListPresenter.getDataFromServer();
                        } else {
                            loadFinish();
                        }
                    }
                }, 1000);
            }
        });

        adapter = new DevicesRvAdapter(getActivity(), TuyaUser.getDeviceInstance().getDevList());
        adapter.setOnMyItemClickListener(new DevicesRvAdapter.OnMyItemClickListener() {
            @Override
            public void onNormalClick(View v, int position) {
                deviceListPresenter.onDeviceClick(adapter.getItem(position));
            }
        });
        adapter.setOnMyItemLongClickListener(new DevicesRvAdapter.OnMyItemLongClickListener() {
            @Override
            public void onNormalLongClick(View v, final int position) {
                DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.device_confirm_remove)
                        , getActivity().getString(R.string.Yes), getActivity().getString(R.string.No), null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes
                                        String devId = adapter.getItem(position).getDevId();
                                        TuyaDevice mDevice = new TuyaDevice(devId);
                                        mDevice.removeDevice(new IControlCallback() {
                                            @Override
                                            public void onError(String s, String s1) {
                                                OUtil.TLog(s + " : " + s1);
                                            }

                                            @Override
                                            public void onSuccess() {
                                                showToastSuccess("Success");
                                                deviceListPresenter.getDataFromServer();
                                            }
                                        });
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No
                                        TLog("devId : " + adapter.getItem(position).getDevId());
                                        //ToastUtil.shortToast(getActivity(),"BUTTON_NEGATIVE");
                                        break;
                                }
                            }
                        }).show();
            }
        });
        deviceRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        deviceRecycler.setAdapter(adapter);
    }

    @OnClick({R.id.add, R.id.list_background_tip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add:
                getActivity().startActivity(new Intent(getActivity(), AddDevicesGuidActivity.class));
                overridePendingTransition(this, ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;

        }

    }

    @Override
    public void updateDeviceData(List<DeviceBean> myDevices) {
        OUtil.TLog("====updateDeviceData====");
        if (adapter != null) {
            adapter.setData(myDevices);
        }
    }

    @Override
    public void loadStart() {
        pullToRefresh.setRefreshing(true);
    }

    @Override
    public void loadFinish() {
        pullToRefresh.setRefreshing(false);
    }

    @Override
    public void showNetWorkTipView(int tipRes) {

    }

    @Override
    public void hideNetWorkTipView() {

    }

    @Override
    public void showBackgroundView() {
        tvRemark.setVisibility(View.GONE);
        listBackgroundTip.setVisibility(View.VISIBLE);
        deviceRecycler.setVisibility(View.GONE);
    }

    @Override
    public void hideBackgroundView() {
        tvRemark.setVisibility(View.VISIBLE);
        listBackgroundTip.setVisibility(View.GONE);
        deviceRecycler.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceListPresenter != null)
            deviceListPresenter.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this, ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }

    @Override
    public boolean needLogin() {
        return true;
    }

}
