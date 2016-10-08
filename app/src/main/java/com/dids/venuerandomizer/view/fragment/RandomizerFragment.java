package com.dids.venuerandomizer.view.fragment;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dids.venuerandomizer.R;

public class RandomizerFragment extends Fragment implements View.OnClickListener {
    public static final int FOOD = 0;
    public static final int DRINKS = 1;
    public static final int COFFEE = 2;
    private static final String TYPE = "type";
    private ProgressBar mProgress;
    private FloatingActionButton mSearchButton;

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
        mProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(),
                R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        mProgress.setVisibility(View.VISIBLE);
        mSearchButton.setEnabled(false);
        switch (getArguments().getInt(TYPE, FOOD)) {
            case FOOD:
                break;
            case DRINKS:
            case COFFEE:
        }
    }
}
