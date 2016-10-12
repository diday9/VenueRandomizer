package com.dids.venuerandomizer.view.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dids.venuerandomizer.R;

public class ImageViewFragment extends Fragment {
    private static final String URL = "url";

    public static ImageViewFragment getInstance(String url) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(URL, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_view, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
//        ImageLoader loader = ImageLoader.getInstance();
//        loader.displayImage(getArguments().getString(URL), imageView);

        ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress_indicator);
        progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(),
                R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        return view;
    }
}
