package com.dids.venuerandomizer.model;

public class DatabaseVenue {
    private final String mId;
    private final String mName;
    private final String mCategory;
    private final String mAddress;

    public DatabaseVenue(String id, String name, String category, String address) {
        mId = id;
        mName = name;
        mCategory = category;
        mAddress = address;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getAddress() {
        return mAddress;
    }
}
