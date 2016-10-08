package com.dids.venuerandomizer.view.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.view.fragment.RandomizerFragment;

public class MainViewPagerAdapter extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 3;
    private final RandomizerFragment mFoodFragment;
    private final RandomizerFragment mDrinksFragment;
    private final RandomizerFragment mCoffeeFragment;
    private final Context mContext;

    public MainViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mFoodFragment = RandomizerFragment.newInstance(RandomizerFragment.FOOD);
        mDrinksFragment = RandomizerFragment.newInstance(RandomizerFragment.DRINKS);
        mCoffeeFragment = RandomizerFragment.newInstance(RandomizerFragment.COFFEE);
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
}
