package com.dids.venuerandomizer.model;

public class Assets {
    private final String mCopyright;
    private final String mLink;
    private final String mUrl;

    public Assets(String copyright, String link, String url) {
        mCopyright = copyright;
        mLink = link;
        mUrl = url;
    }

    public String getCopyright() {
        return mCopyright;
    }

    public String getLink() {
        return mLink;
    }

    public String getUrl() {
        return mUrl;
    }
}
