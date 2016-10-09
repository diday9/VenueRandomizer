package com.dids.venuerandomizer.view.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;
import com.dids.venuerandomizer.controller.task.GetVenueListTask;

public class RandomizerFragment extends Fragment implements View.OnClickListener,
        GetVenueListTask.GetVenueListListener {
    public static final int FOOD = 0;
    public static final int DRINKS = 1;
    public static final int COFFEE = 2;
    private static final String TYPE = "type";
    private ProgressBar mProgress;
    private FloatingActionButton mSearchButton;
    private GetVenueListTask mGetVenueListTask;

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
                getColor(getContext(), R.color.colorPrimary)));
        switch (getArguments().getInt(TYPE, FOOD)) {
            case FOOD:
                background.setImageResource(R.drawable.bg_food);
                break;
            case DRINKS:
                background.setImageResource(R.drawable.bg_drinks);
                break;
            case COFFEE:
                background.setImageResource(R.drawable.bg_coffee);
                break;
        }
        return view;
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

    @Override
    public void onCompleted() {
        mProgress.setVisibility(View.GONE);
        mSearchButton.setEnabled(true);
        mGetVenueListTask = null;
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
