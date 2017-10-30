package com.ym.traegergill.tuya.presenter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import com.tuya.smart.android.base.event.NetWorkStatusEvent;
import com.tuya.smart.android.base.event.NetWorkStatusEventModel;

import com.tuya.smart.android.device.event.GwRelationEvent;
import com.tuya.smart.android.device.event.GwRelationUpdateEventModel;
import com.tuya.smart.android.device.event.GwUpdateEvent;
import com.tuya.smart.android.device.event.GwUpdateEventModel;
import com.tuya.smart.android.hardware.model.IControlCallback;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.sdk.TuyaDevice;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.TuyaSmartRequest;
import com.tuya.smart.sdk.TuyaUser;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.RemoteControlActivity;
import com.ym.traegergill.tuya.event.DeviceUpdateEvent;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.model.DeviceListUpdateModel;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;
import com.ym.traegergill.tuya.utils.ToastUtil;
import com.ym.traegergill.tuya.view.IDeviceListView;

import java.util.List;

/**
 * Created by letian on 15/6/1.
 */
public class DeviceListPresenter extends BasePresenter implements GwRelationEvent, GwUpdateEvent, NetWorkStatusEvent, DeviceUpdateEvent {

    private static final String TAG = "DeviceListFragmentPresenter";
    private static final int WHAT_JUMP_GROUP_PAGE = 10212;
    protected Activity mActivity;
    protected IDeviceListView mView;

    public DeviceListPresenter(Activity activity, IDeviceListView view) {
        mActivity = activity;
        mView = view;
        initEventBus();
    }

    public void getData() {
        mView.loadStart();
        getDataFromServer();
    }


    private void showDevIsNotOnlineTip(final DeviceBean deviceBean) {
        final boolean isShared = deviceBean.isShare;
        DialogUtil.customDialog(mActivity, mActivity.getString(R.string.title_device_offline), mActivity.getString(R.string.content_device_offline),
                mActivity.getString(isShared ? R.string.ty_offline_delete_share : R.string.cancel_connect),
                mActivity.getString(R.string.right_button_device_offline), mActivity.getString(R.string.left_button_device_offline), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (isShared) {
                                    //跳转到删除共享
                                  /*  Intent intent = new Intent(mActivity, SharedActivity.class);
                                    intent.putExtra(SharedActivity.CURRENT_TAB, SharedActivity.TAB_RECEIVED);
                                    mActivity.startActivity(intent);*/
                                } else {
                                    DialogUtil.simpleConfirmDialog(mActivity, mActivity.getString(R.string.device_confirm_remove), new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                                unBindDevice(deviceBean);
                                            }
                                        }
                                    });
                                }
                                break;
                            case DialogInterface.BUTTON_NEUTRAL:
//                                //重置说明
                             /*   Intent intent = new Intent(mActivity, BrowserActivity.class);
                                intent.putExtra(BrowserActivity.EXTRA_LOGIN, false);
                                intent.putExtra(BrowserActivity.EXTRA_REFRESH, true);
                                intent.putExtra(BrowserActivity.EXTRA_TOOLBAR, true);
                                intent.putExtra(BrowserActivity.EXTRA_TITLE, mActivity.getString(R.string.left_button_device_offline));
                                intent.putExtra(BrowserActivity.EXTRA_URI, CommonConfig.RESET_URL);
                                mActivity.startActivity(intent);*/
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                }).show();

    }

    protected void onItemClick(DeviceBean devBean) {
        if (devBean == null) {
            ToastUtil.showToast(mActivity, R.string.no_device_found);
            return;
        }
        if (devBean.getProductId().equals("4eAeY1i5sUPJ8m8d")) {
            ToastUtil.showToast(mActivity, R.string.no_device_found);
            OUtil.TLog("4eAeY1i5sUPJ8m8d");

          /*  Intent intent = new Intent(mActivity, RemoteControlActivity.class);
            intent.putExtra(RemoteControlActivity.INTENT_DEVID, devBean.getDevId());
            mActivity.startActivity(intent);*/
        } else {
            gotoDeviceCommonActivity(devBean);
        }

    }

    private void gotoDeviceCommonActivity(DeviceBean devBean) {
//        Intent intent = new Intent(mActivity, DeviceCommonActivity.class);
//        intent.putExtra(DeviceCommonPresenter.INTENT_DEVID, devBean.getDevId());
//        mActivity.startActivity(intent);
        Intent intent = new Intent(mActivity, RemoteControlActivity.class);
        intent.putExtra(RemoteControlActivity.INTENT_DEVID, devBean.getDevId());
        mActivity.startActivity(intent);
    }

    public void getDataFromServer() {
        TuyaUser.getDeviceInstance().queryDevList();
    }

    public void onDeviceClick(DeviceBean deviceBean) {
        if (!deviceBean.getIsOnline()) {
            showDevIsNotOnlineTip(deviceBean);
            return;
        }
        onItemClick(deviceBean);
    }

    public boolean onDeviceLongClick(final DeviceBean deviceBean) {
        if (deviceBean.getIsShare()) {
            return false;
        }
        DialogUtil.simpleConfirmDialog(mActivity, mActivity.getString(R.string.device_confirm_remove), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            unBindDevice(deviceBean);
                        }
            }
        });
        return true;
    }

    /**
     * 移除网关
     */
    private void unBindDevice(final DeviceBean deviceBean) {
        ProgressUtil.showLoading(mActivity, R.string.loading);
        new TuyaDevice(deviceBean.getDevId()).removeDevice(new IControlCallback() {
            @Override
            public void onError(String s, String s1) {
                ProgressUtil.hideLoading();
                ToastUtil.showToast(mActivity, s1);
            }

            @Override
            public void onSuccess() {
                ProgressUtil.hideLoading();
            }
        });

    }

    private void updateDeviceData(List<DeviceBean> list) {
        if (list.size() == 0) {
            mView.showBackgroundView();
            mView.loadFinish();
        } else {
            mView.hideBackgroundView();
            mView.updateDeviceData(list);
            mView.loadFinish();
        }
    }

    @Override
    public void onEventMainThread(GwRelationUpdateEventModel event) {
        updateLocalData();
    }

    @Override
    public void onEventMainThread(GwUpdateEventModel event) {
        updateLocalData();
    }

    private void updateLocalData() {
        updateDeviceData(TuyaUser.getDeviceInstance().getDevList());
    }

    @Override
    public void onEventMainThread(DeviceListUpdateModel event) {
        getData();
    }

    @Override
    public void onEvent(NetWorkStatusEventModel eventModel) {
        netStatusCheck(eventModel.isAvailable());
    }

    public boolean netStatusCheck(boolean isNetOk) {
        networkTip(isNetOk, R.string.ty_no_net_info);
        return true;
    }

    private void networkTip(boolean networkok, int tipRes) {
        if (networkok) {
            mView.hideNetWorkTipView();
        } else {
            mView.showNetWorkTipView(tipRes);
        }
    }

    public void onDestroy() {
        TuyaSdk.getEventBus().unregister(this);
    }

    public void addDemoDevice() {
        ProgressUtil.showLoading(mActivity, null);
        TuyaSmartRequest.getInstance().requestWithApiName("s.m.dev.sdk.dem.list", "1.0", null, new IRequestCallback() {
            @Override
            public void onSuccess(Object result) {
                ProgressUtil.hideLoading();
                getDataFromServer();
            }

            @Override
            public void onFailure(String errorCode, String errorMsg) {
                ProgressUtil.hideLoading();
                ToastUtil.showToast(mActivity, errorMsg);
            }
        });
    }
}
