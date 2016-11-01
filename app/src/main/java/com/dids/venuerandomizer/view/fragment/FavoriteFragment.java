package com.dids.venuerandomizer.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.database.DatabaseHelper;
import com.dids.venuerandomizer.model.DatabaseVenue;
import com.dids.venuerandomizer.view.adapter.FavoriteListAdapter;
import com.dids.venuerandomizer.view.custom.FoldingCell;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String VARIANT = "variant";
    private FavoriteListAdapter mAdapter;
    private ListView mListView;
    private FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Query query = DatabaseHelper.getInstance().createAllFavoriteQuery(mAuth.
                getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DatabaseVenue> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    list.add(snapshot.getValue(DatabaseVenue.class));
                }
                String type;
                switch (getArguments().getInt(VARIANT)) {
                    case MainFragment.FOOD:
                        type = getString(R.string.random_venue_food);
                        break;
                    case MainFragment.DRINKS:
                        type = getString(R.string.random_venue_drinks);
                        break;
                    default:
                        type = getString(R.string.random_venue_coffee);
                        break;
                }
                mAdapter = new FavoriteListAdapter(getContext(), R.layout.list_favorite, list, type);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ((FoldingCell) view).toggle(false);
        mAdapter.registerToggle(i);
    }
}
