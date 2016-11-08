package com.dids.findmeaplace.view.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dids.findmeaplace.R;
import com.dids.findmeaplace.view.fragment.MainFragment;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {
    private static final int PAGE_COUNT = 3;
    private final MainFragment mFoodFragment;
    private final MainFragment mDrinksFragment;
    private final MainFragment mCoffeeFragment;
    private final Context mContext;

    public MainViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mFoodFragment = MainFragment.newInstance(MainFragment.FOOD);
        mDrinksFragment = MainFragment.newInstance(MainFragment.DRINKS);
        mCoffeeFragment = MainFragment.newInstance(MainFragment.COFFEE);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mFoodFragment;
            case 1:
                return mDrinksFragment;
            case 2:
                return mCoffeeFragment;
        }
        return mFoodFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String name;
        switch (position) {
            case 0:
                name = mContext.getString(R.string.random_venue_food);
                break;
            case 1:
                name = mContext.getString(R.string.random_venue_drinks);
                break;
            case 2:
                name = mContext.getString(R.string.random_venue_coffee);
                break;
            default:
                name = mContext.getString(R.string.random_venue_food);
                break;
        }
        return name;
    }

    public void switchType(boolean isRandomizer) {
        mFoodFragment.setFragment(isRandomizer);
        mDrinksFragment.setFragment(isRandomizer);
        mCoffeeFragment.setFragment(isRandomizer);
    }
}
