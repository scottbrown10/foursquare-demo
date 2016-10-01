package com.example.scott.foursquare.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.scott.foursquare.Models.Tip;
import com.example.scott.foursquare.R;

import java.util.ArrayList;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.ViewHolder> {
    public ArrayList<Tip> mTips;
    public TipListener mListener;

    public TipAdapter(ArrayList<Tip> tips) {
        this.mTips = tips;
    }

    @Override
    public TipAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        // implementing the listener interface isn't required by the context
        if (context instanceof TipListener) {
            mListener = (TipListener) context;
        }

        View v = LayoutInflater.from(context).inflate(R.layout.location_cell_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TipAdapter.ViewHolder holder, int position) {
        holder.mLocationName.setText(mTips.get(position).locationName);
        holder.mLocationTip.setText(mTips.get(position).body);

        // set a listener to invoke mListener with this tip's index when its clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onTipClicked(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTips.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mLocationName;
        private TextView mLocationTip;

        public ViewHolder(View itemView) {
            super(itemView);
            mLocationName = (TextView) itemView.findViewById(R.id.location_name);
            mLocationTip = (TextView) itemView.findViewById(R.id.location_tip);
        }
    }

    // interface to listen for clicks on tips
    public interface TipListener {
        void onTipClicked(int pos);
    }
}

