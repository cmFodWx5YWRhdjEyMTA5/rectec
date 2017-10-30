package com.ym.traegergill.tuya.event;

import com.ym.traegergill.tuya.model.DeviceListUpdateModel;

/**
 * Created by letian on 16/7/20.
 */
public interface DeviceUpdateEvent {
    void onEventMainThread(DeviceListUpdateModel model);
}
