package com.ym.traegergill.iview;

import android.content.DialogInterface;

import com.ym.traegergill.bean.DealerBean;

import java.util.List;

/**
 * Created by Administrator on 2017/11/28.
 */

public interface IShowDealerView {
    void updateDeviceData(List<DealerBean> datas);

    void updateDeviceDataEnd();

    void showRenetDialog(DialogInterface.OnClickListener listener);
}
