package com.ym.traegergill.bean;

/**
 * Created by Administrator on 2017/8/1.
 */

public class TagBean {
    private String name;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TagBean(String name){
        this.name = name;
    }
    public TagBean(int id,String name){
        this.name = name;
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
