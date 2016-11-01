package com.dids.venuerandomizer.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.model.DatabaseVenue;
import com.dids.venuerandomizer.model.FavoriteListHolder;
import com.dids.venuerandomizer.view.custom.FoldingCell;

import java.util.HashSet;
import java.util.List;

public class FavoriteListAdapter extends ArrayAdapter<DatabaseVenue> {
    private final String mType;
    private HashSet<Integer> mUnfoldedIndexes = new HashSet<>();

    public FavoriteListAdapter(Context context, int resource, List<DatabaseVenue> list, String type) {
        super(context, resource, list);
        mType = type;
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
        return cell;
    }

    public void registerToggle(int position) {
        if (mUnfoldedIndexes.contains(position))
            registerFold(position);
        else {
            registerUnfold(position);
        }
    }

    public void registerFold(int position) {
        mUnfoldedIndexes.remove(position);
    }

    public void registerUnfold(int position) {
        mUnfoldedIndexes.add(position);
    }
}
