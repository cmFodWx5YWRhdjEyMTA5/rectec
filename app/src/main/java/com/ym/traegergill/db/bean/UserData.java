package com.ym.traegergill.db.bean;

/**
 * Created by Administrator on 2017/10/26.
 */

public class UserData {
    private int id;
    private String user;
    private int gill_range = 10;
    private boolean flag = false;
    private int probe_a_temp = 200;
    private boolean a_temp_open = false;
    private int probe_b_temp = 200;
    private boolean b_temp_open = false;
    private String dev_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getGill_range() {
        return gill_range;
    }

    public void setGill_range(int gill_range) {
        this.gill_range = gill_range;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public int getProbe_a_temp() {
        return probe_a_temp;
    }

    public void setProbe_a_temp(int probe_a_temp) {
        this.probe_a_temp = probe_a_temp;
    }

    public boolean isA_temp_open() {
        return a_temp_open;
    }

    public void setA_temp_open(boolean a_temp_open) {
        this.a_temp_open = a_temp_open;
    }

    public int getProbe_b_temp() {
        return probe_b_temp;
    }

    public void setProbe_b_temp(int probe_b_temp) {
        this.probe_b_temp = probe_b_temp;
    }

    public boolean isB_temp_open() {
        return b_temp_open;
    }

    public void setB_temp_open(boolean b_temp_open) {
        this.b_temp_open = b_temp_open;
    }

    public String getDev_id() {
        return dev_id;
    }

    public void setDev_id(String dev_id) {
        this.dev_id = dev_id;
    }
}
