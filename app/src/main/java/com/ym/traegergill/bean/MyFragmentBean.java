package com.ym.traegergill.bean;

/**
 * Created by Administrator on 2017/8/1.
 */

public class MyFragmentBean {
    private String name;
    private boolean flag;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public MyFragmentBean(String name,boolean flag ) {
        this.name = name;
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
