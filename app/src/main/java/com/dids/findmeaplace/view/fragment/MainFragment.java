package com.dids.findmeaplace.view.fragment;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.volley.toolbox.ImageLoader;
import com.dids.findmeaplace.R;
import com.dids.findmeaplace.controller.network.FourSquareWrapper;
import com.dids.findmeaplace.controller.network.VolleySingleton;
import com.dids.findmeaplace.controller.task.RefreshImageTask;
import com.dids.findmeaplace.controller.utility.AssetUtility;
import com.dids.findmeaplace.controller.utility.Utilities;
import com.dids.findmeaplace.model.Assets;
import com.dids.findmeaplace.view.custom.NetworkImageView;

import java.io.IOException;
import java.io.InputStream;

public class MainFragment extends Fragment implements RefreshImageTask.RefreshImageListener,
        ViewTreeObserver.OnGlobalLayoutListener {
    public static final int FOOD = 0;
    public static final int DRINKS = 1;
    public static final int COFFEE = 2;
    private static final String VARIANT = "variant";

    private View mRootView;

    public static MainFragment newInstance(int variant) {
        MainFragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(VARIANT, variant);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        setFragment(true);
        loadDefaultAssets(mRootView);
        return mRootView;
    }

    public void setFragment(boolean isRandomizer) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.container, isRandomizer ? RandomizerFragment
                .newInstance(getArguments().getInt(VARIANT)) :
                FavoriteFragment.newInstance(getArguments().getInt(VARIANT)));
        transaction.commit();
    }

    private void setupForDynamicView(final Assets asset) {
        final ViewSwitcher switcher = (ViewSwitcher) mRootView.findViewById(R.id.image_switcher);
        NetworkImageView.ImageLoaderListener listener = new NetworkImageView.ImageLoaderListener() {
            @Override
            public void onImageLoaded() {
                if (switcher.getDisplayedChild() == 0) {
                    switcher.showNext();
                } else {
                    switcher.showPrevious();
                }
                TextView textView = (TextView) mRootView.findViewById(R.id.copyright);
                textView.setText(Utilities.createSpannedLink(asset.getCopyright(), asset.getLink()));
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView = (TextView) mRootView.findViewById(R.id.link);
                textView.setText(asset.getLink());
            }
        };
        NetworkImageView imageView;
        if (switcher.getDisplayedChild() == 0) {
            imageView = (NetworkImageView) mRootView.findViewById(R.id.image2);
        } else {
            imageView = (NetworkImageView) mRootView.findViewById(R.id.image1);
        }
        imageView.setImageLoaderListener(listener);
        ImageLoader loader = VolleySingleton.getInstance(getContext()).getImageLoader();
        imageView.setImageUrl(asset.getUrl(), loader);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && getHost() != null && getChildFragmentManager() != null) {
            Fragment fragment = getChildFragmentManager().findFragmentById(R.id.container);
            if (fragment instanceof RandomizerFragment) {
                ((RandomizerFragment) fragment).resetView();
            } else {
                ((FavoriteFragment) fragment).resetView();
            }
        }
    }

    private void loadDefaultAssets(View view) {
        String type;
        switch (getArguments().getInt(VARIANT, FOOD)) {
            case DRINKS:
                type = FourSquareWrapper.SECTION_DRINKS;
                break;
            case COFFEE:
                type = FourSquareWrapper.SECTION_COFFEE;
                break;
            default:
                type = FourSquareWrapper.SECTION_FOOD;
        }

        int arrayId = getContext().getResources().getIdentifier("default_" + type, "array",
                getContext().getPackageName());
        TypedArray array = getContext().getResources().obtainTypedArray(arrayId);
        TextView textView = (TextView) view.findViewById(R.id.copyright);
        //noinspection ResourceType
        textView.setText(Utilities.createSpannedLink(array.getString(0), array.getString(1)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView = (TextView) view.findViewById(R.id.link);
        //noinspection ResourceType
        textView.setText(array.getString(1));
        array.recycle();

        try {
            InputStream ims = getContext().getAssets().open(String.format("default_%s.jpg", type));
            Drawable d = Drawable.createFromStream(ims, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.image1);
            imageView.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewImageSelected(Assets food, Assets drinks, Assets coffee) {
        Assets asset;
        switch (getArguments().getInt(VARIANT, FOOD)) {
            case DRINKS:
                asset = drinks;
                break;
            case COFFEE:
                asset = coffee;
                break;
            default:
                asset = food;
                break;
        }
        setupForDynamicView(asset);
    }

    @Override
    public void onGlobalLayout() {
        mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        Assets asset;
        switch (getArguments().getInt(VARIANT, FOOD)) {
            case DRINKS:
                asset = AssetUtility.getInstance().getDrinksAsset();
                break;
            case COFFEE:
                asset = AssetUtility.getInstance().getCoffeeAsset();
                break;
            default:
                asset = AssetUtility.getInstance().getFoodAsset();
                break;
        }
        setupForDynamicView(asset);
        RefreshImageTask.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefreshImageTask.getInstance().cancel(true);
    }
}
