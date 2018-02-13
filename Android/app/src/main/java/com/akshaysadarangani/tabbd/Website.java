package com.akshaysadarangani.tabbd;

/**
 * Created by Akshay on 8/3/2017.
 */

public class Website {

    public String wid;
    public String url;
    public String creator;

    // Default constructor required for calls to
    // DataSnapshot.getValue(Website.class)
    public Website() {
    }

    public Website(String url) {
        this.url = url;
    }

    public Website(String creator, String url) {
        this.url = url;
        this.creator = creator;
    }

    public Website(String wid, String url, String creator) {
        this.wid = wid;
        this.url = url;
        this.creator = creator;
    }
}
