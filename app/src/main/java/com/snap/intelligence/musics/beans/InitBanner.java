package com.snap.intelligence.musics.beans;


public class InitBanner {


    public InitBanner(boolean bannerType) {
        this.bannerType = bannerType;
    }

    public Boolean getBannerType() {
        return bannerType;
    }

    public void setBannerType(boolean bannerType) {
        this.bannerType = bannerType;
    }

    private boolean bannerType;
}
