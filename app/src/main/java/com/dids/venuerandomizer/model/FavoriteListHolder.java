package com.dids.venuerandomizer.model;

import android.widget.TextView;

public class FavoriteListHolder {
    private TextView mVenueNameTextView;
    private TextView mVenueNameContentTextView;
    private TextView mCategoryTextView;
    private TextView mAddressTextView;
    private TextView mTelephoneTextView;

    public TextView getVenueNameTextView() {
        return mVenueNameTextView;
    }

    public void setVenueNameTextView(TextView venueNameTextView) {
        mVenueNameTextView = venueNameTextView;
    }

    public TextView getCategoryTextView() {
        return mCategoryTextView;
    }

    public void setCategoryTextView(TextView category) {
        mCategoryTextView = category;
    }

    public TextView getAddressTextView() {
        return mAddressTextView;
    }

    public void setAddressTextView(TextView address) {
        mAddressTextView = address;
    }

    public TextView getVenueNameContentTextView() {
        return mVenueNameContentTextView;
    }

    public void setVenueNameContentTextView(TextView venueNameContentTextView) {
        mVenueNameContentTextView = venueNameContentTextView;
    }

    public TextView getTelephoneTextView() {
        return mTelephoneTextView;
    }

    public void setTelephoneTextView(TextView telephoneTextView) {
        mTelephoneTextView = telephoneTextView;
    }
}
