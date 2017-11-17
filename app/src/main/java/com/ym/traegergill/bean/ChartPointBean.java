package com.ym.traegergill.bean;

/**
 * Created by Administrator on 2017/11/10.
 */

public class ChartPointBean extends BaseBean{
    private long timeStamp;
    private int value;

    public ChartPointBean(long timeStamp, int value) {
        this.timeStamp = timeStamp;
        this.value = value;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
