package com.ym.traegergill.bean;


/**
 * Created by Administrator on 2017/9/14.
 */

public class PhotoBean extends BaseBean{
    String imageUrl;
    public PhotoBean(){

    }
    public PhotoBean(String imageUrl){
        this.imageUrl = imageUrl;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
