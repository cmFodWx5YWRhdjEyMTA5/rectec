package com.ym.traegergill.tuya.view;

import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

/**
 * Created by letian on 16/7/18.
 */
public interface IDeviceListView {
    //返回当前所有设备数量 用于判断是否显示空列表提示
    void updateDeviceData(List<DeviceBean> myDevices);

    void loadStart();

    void loadFinish();

    void showNetWorkTipView(int tipRes);

    void hideNetWorkTipView();

    void showBackgroundView();

    void hideBackgroundView();
}
