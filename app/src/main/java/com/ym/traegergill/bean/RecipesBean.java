package com.ym.traegergill.bean;


/**
 * Created by Administrator on 2017/9/14.
 */

public class RecipesBean extends BaseBean{
    String imageUrl;
    String desInImg;
    String desc;
    double time;
    int ingredientNum;

    public String getDesInImg() {
        return desInImg;
    }

    public void setDesInImg(String desInImg) {
        this.desInImg = desInImg;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getIngredientNum() {
        return ingredientNum;
    }

    public void setIngredientNum(int ingredientNum) {
        this.ingredientNum = ingredientNum;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
