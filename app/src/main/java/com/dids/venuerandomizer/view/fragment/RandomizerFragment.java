package com.dids.venuerandomizer.view.fragment;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;
import com.dids.venuerandomizer.controller.task.GetVenueListTask;
import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RandomizerFragment extends Fragment implements View.OnClickListener,
        GetVenueListTask.GetVenueListListener {
    public static final int FOOD = 0;
    public static final int DRINKS = 1;
    public static final int COFFEE = 2;
    private static final String VERTICAL_TRANSLATION_PROPERTY = "translationY";
    private static final int VERTICAL_OFFSET = 200;
    private static final int ANIMATION_DURATION = 1000;
    private static final String TYPE = "type";

    /* Food constants */
    private static final int MAX_FOOD = 2;
    private static final String FOOD_RESOURCE_ID = "bg_food%d";

    private ProgressBar mProgress;
    private FloatingActionButton mSearchButton;
    private GetVenueListTask mGetVenueListTask;
    private View mButtonGroup;
    private View mResultView;
    private TextView mVenueName;
    private TextView mCategoryName;
    private TextView mAddress;
    private TextView mTelephone;

    public static RandomizerFragment newInstance(int type) {
        RandomizerFragment fragment = new RandomizerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_randomizer, container, false);
        mSearchButton = (FloatingActionButton) view.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(this);

        ImageView background = (ImageView) view.findViewById(R.id.background);
        mProgress = (ProgressBar) view.findViewById(R.id.progress_indicator);
        mSearchButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                getColor(getContext(), R.color.colorPrimaryAlpha)));
        mButtonGroup = view.findViewById(R.id.button_group);
        mResultView = view.findViewById(R.id.result);
        mVenueName = (TextView) view.findViewById(R.id.venue_name);
        mCategoryName = (TextView) view.findViewById(R.id.category_name);
        mAddress = (TextView) view.findViewById(R.id.address);
        mTelephone = (TextView) view.findViewById(R.id.telephone);
        Random rng = new Random();
        TextView copyright = (TextView) view.findViewById(R.id.copyright);
        TextView link = (TextView) view.findViewById(R.id.link);
        switch (getArguments().getInt(TYPE, FOOD)) {
            case DRINKS:
                background.setImageResource(R.drawable.bg_drinks);
                break;
            case COFFEE:
                background.setImageResource(R.drawable.bg_coffee);
                break;
            default:
                setResources(String.format(Locale.getDefault(), FOOD_RESOURCE_ID,
                        rng.nextInt(MAX_FOOD) + 1), copyright, link, background);
                break;
        }
        return view;
    }

    private void setResources(String resource, TextView copyright, TextView link,
                              ImageView background) {
        Resources res = getContext().getResources();
        int drawableId = res.getIdentifier(resource, "drawable", getContext().getPackageName());
        background.setImageResource(drawableId);

        int arrayId = res.getIdentifier(resource, "array", getContext().getPackageName());
        TypedArray attributions = res.obtainTypedArray(arrayId);
        copyright.setText(attributions.getString(0));
        link.setText(attributions.getString(1));
        attributions.recycle();
    }

    @Override
    public void onClick(View v) {
        if (mGetVenueListTask == null) {
            mProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(),
                    R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            mProgress.setVisibility(View.VISIBLE);
            mSearchButton.setEnabled(false);
            String section;
            switch (getArguments().getInt(TYPE, FOOD)) {
                case DRINKS:
                    section = FourSquareWrapper.SECTION_DRINKS;
                    break;
                case COFFEE:
                    section = FourSquareWrapper.SECTION_COFFEE;
                    break;
                default:
                    section = FourSquareWrapper.SECTION_FOOD;
                    break;
            }
            mGetVenueListTask = new GetVenueListTask(getContext(), this);
            mGetVenueListTask.execute(section);
        }
    }

    private void animateButtonGroup() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mButtonGroup, VERTICAL_TRANSLATION_PROPERTY,
                VERTICAL_OFFSET);
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
        mResultView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCompleted(Venue venue) {
        mProgress.setVisibility(View.GONE);
        mSearchButton.setEnabled(true);
        mGetVenueListTask = null;
        if (venue != null) {
            setVenue(venue);
            animateButtonGroup();
        }
    }

    private void setVenue(Venue venue) {
        mVenueName.setText(venue.getName());
        if (venue.getCategories() != null && !venue.getCategories().isEmpty()) {
            for (Category category : venue.getCategories()) {
                if (category.isPrimary()) {
                    mCategoryName.setText(category.getName());
                    break;
                }
            }
        } else {
            mCategoryName.setVisibility(View.GONE);
        }
        mAddress.setText(venue.getCity() + ", " + venue.getState());
        if (venue.getFormattedPhone() != null && !venue.getFormattedPhone().isEmpty()) {
            mTelephone.setText(venue.getFormattedPhone());
        } else {
            mTelephone.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLocationDisabled() {
        mProgress.setVisibility(View.GONE);
        mSearchButton.setEnabled(true);
        mGetVenueListTask = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.random_gps_disabled);
        builder.setMessage(R.string.random_gps_disabled_msg);
        builder.setPositiveButton(R.string.control_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.cast_tracks_chooser_dialog_cancel, null);
        builder.create().show();
    }

    @Override
    public void onConnectionError() {
        mProgress.setVisibility(View.GONE);
        mSearchButton.setEnabled(true);
        mGetVenueListTask = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.random_no_internet);
        builder.setMessage(R.string.random_no_internet_msg);
        builder.setPositiveButton(R.string.control_ok, null);
        builder.create().show();
    }
}
