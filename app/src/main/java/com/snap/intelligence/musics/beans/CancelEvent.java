package com.snap.intelligence.musics.beans;


public class CancelEvent {
    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    private boolean isShow;

    public CancelEvent(boolean isShow) {
        this.isShow = isShow;
    }
}
