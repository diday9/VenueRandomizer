package com.dids.venuerandomizer.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.dids.venuerandomizer.controller.network.VolleySingleton;
import com.dids.venuerandomizer.view.fragment.ImageViewFragment;

import java.util.List;

public class SlidingImagePagerAdapter extends FragmentPagerAdapter {
    private final List<String> mUrlList;

    public SlidingImagePagerAdapter(FragmentManager fm, List<String> urlList) {
        super(fm);
        mUrlList = urlList;
    }

    @Override
    public int getCount() {
        return mUrlList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return ImageViewFragment.getInstance(mUrlList.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
