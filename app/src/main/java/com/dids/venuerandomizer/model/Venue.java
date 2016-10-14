package com.dids.venuerandomizer.model;

import java.util.ArrayList;
import java.util.List;

public class Venue {
    private String mId;
    private String mName;

    /* Contacts */
    private String mPhone;
    private String mFormattedPhone;
    private String mTwitter;
    private String mFacebook;
    private String mFacebookUsername;
    private String mFacebookName;

    /* Location */
    private String mAddress;
    private double mLatitude;
    private double mLongitude;
    private int mDistance;
    private String mPostalCode;
    private String mCountryCode;
    private String mCity;
    private String mState;
    private String mCountry;

    /* Categories */
    private List<Category> mCategories;

    private String mUrl;
    private String mStatus;
    private boolean mIsOpen;
    private double mRating;
    private List<String> mPhotoUrls;

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

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getFormattedPhone() {
        return mFormattedPhone;
    }

    public void setFormattedPhone(String formattedPhone) {
        mFormattedPhone = formattedPhone;
    }

    public String getTwitter() {
        return mTwitter;
    }

    public void setTwitter(String twitter) {
        mTwitter = twitter;
    }

    public String getFacebook() {
        return mFacebook;
    }

    public void setFacebook(String facebook) {
        mFacebook = facebook;
    }

    public String getFacebookUsername() {
        return mFacebookUsername;
    }

    public void setFacebookUsername(String facebookUsername) {
        mFacebookUsername = facebookUsername;
    }

    public String getFacebookName() {
        return mFacebookName;
    }

    public void setFacebookName(String facebookName) {
        mFacebookName = facebookName;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        mDistance = distance;
    }

    public String getPostalCode() {
        return mPostalCode;
    }

    public void setPostalCode(String postalCode) {
        mPostalCode = postalCode;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public void setCountryCode(String countryCode) {
        mCountryCode = countryCode;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        mState = state;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        mCountry = country;
    }

    public List<Category> getCategories() {
        return mCategories;
    }

    public void addCategory(Category category) {
        if (mCategories == null) {
            mCategories = new ArrayList<>();
        }
        mCategories.add(category);
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void setIsOpen(boolean isOpen) {
        mIsOpen = isOpen;
    }

    public double getRating() {
        return mRating;
    }

    public void setRating(double rating) {
        mRating = rating;
    }

    public List<String> getPhotoUrls() {
        return mPhotoUrls;
    }

    public void addPhotoUrl(String url) {
        if (mPhotoUrls == null) {
            mPhotoUrls = new ArrayList<>();
        }
        mPhotoUrls.add(url);
    }
}
