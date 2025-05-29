package com.snap.intelligence.musics.beans;


public class PageArgs {

    //user當前頁面 * local * snap
    private String currentUserPage;

    //af拉取到的user來源 * or * non-or

    private String snapAFUser;

    //af拉取到的media來源

    public String getSnapMediaSource() {
        return snapMediaSource;
    }

    public void setSnapMediaSource(String snapMediaSource) {
        this.snapMediaSource = snapMediaSource;
    }

    public String getCurrentUserPage() {
        return currentUserPage;
    }

    public void setCurrentUserPage(String currentUserPage) {
        this.currentUserPage = currentUserPage;
    }

    public String getSnapAFUser() {
        return snapAFUser;
    }

    public void setSnapAFUser(String snapAFUser) {
        this.snapAFUser = snapAFUser;
    }

    public String getSnapCampaign() {
        return snapCampaign;
    }

    public void setSnapCampaign(String snapCampaign) {
        this.snapCampaign = snapCampaign;
    }

    private String snapMediaSource;

    //af拉取到的活動

    private String snapCampaign;

    public String getGoogleRefer() {
        return googleRefer;
    }

    public void setGoogleRefer(String googleRefer) {
        this.googleRefer = googleRefer;
    }

    //google refer
    private String googleRefer;




}
