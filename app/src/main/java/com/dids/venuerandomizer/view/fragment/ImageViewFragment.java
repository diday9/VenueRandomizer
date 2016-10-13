package com.dids.venuerandomizer.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ViewSwitcher;

import com.android.volley.toolbox.ImageLoader;
import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.VenueRandomizerApplication;
import com.dids.venuerandomizer.controller.network.VolleySingleton;
import com.dids.venuerandomizer.view.custom.EventNetworkImageView;

public class ImageViewFragment extends Fragment {
    private static final String URL = "url";
    private EventNetworkImageView mImageView;

    public static ImageViewFragment getInstance(String url) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(URL, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewSwitcher view = (ViewSwitcher) inflater.inflate(R.layout.fragment_image_view,
                container, false);
        mImageView = (EventNetworkImageView) view.findViewById(R.id.image);
        mImageView.setImageLoaderListener(new EventNetworkImageView.ImageLoaderListener() {
            @Override
            public void onImageLoaded() {
                view.showNext();
            }
        });
        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setResources(getArguments().getString(URL));
            }
        });
        return view;
    }

    private void setResources(String url) {
        VenueRandomizerApplication app = VenueRandomizerApplication.getInstance();
        ImageLoader loader = VolleySingleton.getInstance(getContext()).getImageLoader();
        mImageView.setImageUrl(url, loader);
    }
}
