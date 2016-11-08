package com.dids.venuerandomizer.model;

@SuppressWarnings("unused")
public class Category {
    private String mId;
    private String mName;
    private String mPluralName;
    private String mShortName;
    private String mIconPrefix;
    private String mIconSuffix;
    private boolean mIsPrimary;

    public Category() {
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPluralName() {
        return mPluralName;
    }

    public void setPluralName(String pluralName) {
        mPluralName = pluralName;
    }

    public String getShortName() {
        return mShortName;
    }

    public void setShortName(String shortName) {
        mShortName = shortName;
    }

    public String getIconPrefix() {
        return mIconPrefix;
    }

    public void setIconPrefix(String iconPrefix) {
        mIconPrefix = iconPrefix;
    }

    public String getIconSuffix() {
        return mIconSuffix;
    }

    public void setIconSuffix(String iconSuffix) {
        mIconSuffix = iconSuffix;
    }

    public boolean isPrimary() {
        return mIsPrimary;
    }

    public void setPrimary(boolean isPrimary) {
        mIsPrimary = isPrimary;
    }
}