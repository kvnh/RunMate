package com.khackett.runmate.adapters;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.parse.ParseUser;

/**
 * Created by KHackett on 09/08/15.
 */
public class RecipientsAdapter extends RecyclerView.Adapter<RecipientsAdapter.ViewHolder> {

    private List<ParseUser>  mFriends;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtHeader;
        public TextView txtFooter;

        public ViewHolder(View v) {
            super(v);
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);
        }
    }

    public void add(int position, ParseUser item) {
        mFriends.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(ParseUser item) {
        int position = mFriends.indexOf(item);
        mFriends.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecipientsAdapter(List<ParseUser> friends) {
        mFriends = friends;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecipientsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_route_recipient_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final ParseUser name = mFriends.get(position);
        holder.txtHeader.setText("" + mFriends.get(position));
        holder.txtHeader.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(name);
            }
        });
        holder.txtFooter.setText("Footer: " + mFriends.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFriends.size();
        // return 0;
    }

}
