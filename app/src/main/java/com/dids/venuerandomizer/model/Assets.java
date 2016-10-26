package com.dids.venuerandomizer.model;

public class Assets {
    private final String mCopyright;
    private final String mLink;
    private final String mUrl;
    private final String mPath;

    public Assets(String copyright, String link, String url, String path) {
        mCopyright = copyright;
        mLink = link;
        mUrl = url;
        mPath = path;
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

    public String getPath() {
        return mPath;
    }
}
