package com.dids.venuerandomizer.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.task.GetVenueTask;
import com.dids.venuerandomizer.model.DatabaseVenue;
import com.dids.venuerandomizer.model.FavoriteListHolder;
import com.dids.venuerandomizer.view.custom.FoldingCell;

import java.util.HashSet;
import java.util.List;

public class FavoriteListAdapter extends ArrayAdapter<DatabaseVenue> {
    private final GetVenueTask.GetVenueListener mListener;
    private final HashSet<Integer> mUnfoldedIndexes = new HashSet<>();

    public FavoriteListAdapter(Context context, int resource, List<DatabaseVenue> list,
                               GetVenueTask.GetVenueListener listener) {
        super(context, resource, list);
        mListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FavoriteListHolder holder;
        FoldingCell cell = (FoldingCell) convertView;
        if (cell == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            cell = (FoldingCell) inflater.inflate(R.layout.list_favorite, parent, false);
            holder = new FavoriteListHolder();
            holder.setVenueNameTextView((TextView) cell.findViewById(R.id.venue_name));
            holder.setVenueNameContentTextView((TextView) cell.findViewById(R.id.venue_name_content));
            holder.setAddressTextView((TextView) cell.findViewById(R.id.address));
            holder.setCategoryTextView((TextView) cell.findViewById(R.id.category_name));
            holder.setTelephoneTextView((TextView) cell.findViewById(R.id.telephone));
            holder.setCheckoutButton((Button) cell.findViewById(R.id.checkout));
            cell.setTag(holder);
        } else {
            if (mUnfoldedIndexes.contains(position)) {
                cell.unfold(true);
            } else {
                cell.fold(true);
            }
            holder = (FavoriteListHolder) cell.getTag();
        }

        final DatabaseVenue venue = getItem(position);
        holder.getVenueNameContentTextView().setText(venue.getName());
        holder.getVenueNameTextView().setText(venue.getName());
        holder.getAddressTextView().setText(venue.getAddress());
        holder.getCategoryTextView().setText(venue.getCategory());
        holder.getTelephoneTextView().setText(venue.getTelephone());
        holder.getCheckoutButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetVenueTask(getContext(), mListener).execute(venue.getId());
            }
        });
        return cell;
    }

    public void registerToggle(int position) {
        if (mUnfoldedIndexes.contains(position))
            registerFold(position);
        else {
            registerUnfold(position);
        }
    }

    private void registerFold(int position) {
        mUnfoldedIndexes.remove(position);
    }

    private void registerUnfold(int position) {
        mUnfoldedIndexes.add(position);
    }
}
