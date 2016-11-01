package com.dids.venuerandomizer.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.VenueRandomizerApplication;
import com.dids.venuerandomizer.controller.database.DatabaseHelper;
import com.dids.venuerandomizer.controller.task.GetVenueTask;
import com.dids.venuerandomizer.model.DatabaseVenue;
import com.dids.venuerandomizer.model.Venue;
import com.dids.venuerandomizer.view.VenueDetailActivity;
import com.dids.venuerandomizer.view.adapter.FavoriteListAdapter;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.dids.venuerandomizer.view.custom.FoldingCell;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment implements AdapterView.OnItemClickListener,
        GetVenueTask.GetVenueListener, SwipeRefreshLayout.OnRefreshListener, ValueEventListener {
    private static final String VARIANT = "variant";
    private FavoriteListAdapter mAdapter;
    private ListView mListView;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout mSwipeRefresh;
    private Query mQuery;

    public static FavoriteFragment newInstance(int variant) {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(VARIANT, variant);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        mAuth = FirebaseAuth.getInstance();
        //noinspection ConstantConditions
        mQuery = DatabaseHelper.getInstance().createAllFavoriteQuery(mAuth.getCurrentUser().getUid());
        mQuery.addValueEventListener(this);
        mSwipeRefresh.setOnRefreshListener(this);
        return view;
    }

    private void refresh() {
        mQuery.removeEventListener(this);
        mQuery.addValueEventListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ((FoldingCell) view).toggle(false);
        mAdapter.registerToggle(i);
    }

    @Override
    public void onStarted() {
        ((BaseActivity) getActivity()).interceptTouchEvents(true);
    }

    @Override
    public void onCompleted(Venue venue) {
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
        VenueRandomizerApplication.getInstance().setVenue(venue);
        Intent intent = new Intent(getContext(), VenueDetailActivity.class);
        intent.putExtra(VenueDetailActivity.VARIANT, getArguments().getInt(VARIANT));
        startActivity(intent);
    }

    @Override
    public void onConnectionError() {
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.random_no_internet);
        builder.setMessage(R.string.random_no_internet_msg);
        builder.setPositiveButton(R.string.control_ok, null);
        builder.create().show();
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        List<DatabaseVenue> list = new ArrayList<>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            DatabaseVenue venue = snapshot.getValue(DatabaseVenue.class);
            if (venue.getVariant() == getArguments().getInt(VARIANT)) {
                list.add(venue);
            }
        }
        mAdapter = new FavoriteListAdapter(getContext(), R.layout.list_favorite, list,
                FavoriteFragment.this);
        mListView.setAdapter(mAdapter);
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mQuery.removeEventListener(this);
    }
}
