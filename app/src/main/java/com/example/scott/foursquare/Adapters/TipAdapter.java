package com.example.scott.foursquare.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.scott.foursquare.Models.Tip;
import com.example.scott.foursquare.R;

import java.util.List;

public class TipAdapter extends ArrayAdapter<Tip> {
    public List<Tip> mTips;
    public TipAdapter(Context context, int resource, List<Tip> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView nameTV = null, tipTV = null;
        nameTV = (TextView) convertView.findViewById(R.id.location_name);
        tipTV = (TextView) convertView.findViewById(R.id.location_tip);
        nameTV.setText(mTips.get(position).locationName);
        tipTV.setText(mTips.get(position).body);
        return convertView;
    }
}

