package com.dids.venuerandomizer.view.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import com.dids.venuerandomizer.R;

public class DetailActionFragment extends Fragment {
    private static final String VERTICAL_POSITION_PROPERTY = "Y";
    private static final int VERTICAL_POSITION_BOUNCE = 120;
    private static final int ANIMATION_DURATION = 700;
    private static final int ANIMATION_DURATION_OFFSET = 300;
    private static final String ADDRESS = "address";

    public static DetailActionFragment newInstance(String address) {
        DetailActionFragment fragment = new DetailActionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ADDRESS, address);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_actions, container, false);
        /** Location */
        FloatingActionButton actionButton = (FloatingActionButton) view.findViewById(R.id.location);
        int animationOffset = animateView(actionButton, 0);

        /** Telephone */
        actionButton = (FloatingActionButton) view.findViewById(R.id.telephone);
        animationOffset = animateView(actionButton, animationOffset);

        /** Web */
        actionButton = (FloatingActionButton) view.findViewById(R.id.url);
        animationOffset = animateView(actionButton, animationOffset);

        /** Facebook */
        actionButton = (FloatingActionButton) view.findViewById(R.id.facebook);
        animationOffset = animateView(actionButton, animationOffset);

        /** Twitter */
        actionButton = (FloatingActionButton) view.findViewById(R.id.twitter);
        animationOffset = animateView(actionButton, animationOffset);

        return view;
    }

    private int animateView(View view, int offset) {
        view.setVisibility(View.VISIBLE);
        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(view, VERTICAL_POSITION_PROPERTY,
                VERTICAL_POSITION_BOUNCE);
        moveAnim.setDuration(ANIMATION_DURATION);
        moveAnim.setStartDelay(1000 + offset);
        moveAnim.setInterpolator(new BounceInterpolator());
        moveAnim.start();
        return offset + ANIMATION_DURATION_OFFSET;
    }
}
