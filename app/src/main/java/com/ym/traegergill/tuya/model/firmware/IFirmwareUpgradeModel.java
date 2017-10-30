package com.ym.traegergill.tuya.model.firmware;

/**
 * Created by letian on 16/4/18.
 */

import com.tuya.smart.sdk.api.IFirmwareUpgradeListener;
import com.ym.traegergill.tuya.presenter.firmware.IFirmwareUpgrade;

/**
 * Created by letian on 15/7/3.
 */
public interface IFirmwareUpgradeModel extends IFirmwareUpgrade {

    /**
     * 立即对设备升级
     */
    void upgradeDevice();

    /**
     * 立即对网关升级
     */
    void upgradeGW();

    /**
     *
     */
    void setUpgradeDeviceUpdateAction(IFirmwareUpgradeListener listener);
}
