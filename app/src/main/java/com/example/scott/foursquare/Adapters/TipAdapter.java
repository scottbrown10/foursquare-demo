package com.example.scott.foursquare.Adapters;

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

    public TipAdapter(ArrayList<Tip> tips) {
        this.mTips = tips;
    }

    @Override
    public TipAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_cell_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(TipAdapter.ViewHolder holder, int position) {
        holder.mLocationName.setText(mTips.get(position).locationName);
        holder.mLocationTip.setText(mTips.get(position).body);
    }

    @Override
    public int getItemCount() {
        return mTips.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mLocationName;
        public TextView mLocationTip;

        public ViewHolder(View itemView) {
            super(itemView);
            mLocationName = (TextView) itemView.findViewById(R.id.location_name);
            mLocationTip = (TextView) itemView.findViewById(R.id.location_tip);
        }
    }
}

