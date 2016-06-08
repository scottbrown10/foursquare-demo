package com.example.scott.foursquare;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FoursquareAPIClient.FoursquareAPIListener {
    private JSONArray mLocations;
    private ArrayList<Tip> mTips;
    private TipAdapter tipAdapter;
    FoursquareAPIClient mFoursquareClient;
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFoursquareClient = FoursquareAPIClient.getInstance(this);
        mTips = new ArrayList<>();

        mFoursquareClient.getLocations();

        mListView = (ListView) findViewById(R.id.location_list);
        tipAdapter = new TipAdapter(this, R.layout.location_cell_layout, mTips);
        mListView.setAdapter(tipAdapter);
    }

    private void getTips() {
        int length = mLocations.length();
        for (int i = 0; i < length; i++) {
            try {
                JSONObject location = mLocations.getJSONObject(i);
                String name = location.getString("name");
                mFoursquareClient.getTip(name, i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSearchResponse(JSONArray locations) {
        mLocations = locations;
        getTips();
    }

    @Override
    public void onTipResponse(String locationName, JSONObject jsonTip, int index) {
        try {
            Tip tip = new Tip(locationName, jsonTip.getString("text"));
            mTips.add(tip);
            tipAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class TipAdapter extends ArrayAdapter<Tip> {
        public TextView nameTV;
        public TextView tipTV;
        public TipAdapter(Context context, int resource, List<Tip> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            nameTV = (TextView) convertView.findViewById(R.id.location_name);
            tipTV = (TextView) convertView.findViewById(R.id.location_tip);
            nameTV.setText(mTips.get(position).locationName);
            tipTV.setText(mTips.get(position).body);
            return convertView;
        }
    }

    public class Tip {
        public String locationName;
        public String body;
        public Tip(String locationName, String body) {
            this.locationName = locationName;
            this.body = body;
        }
    }

}
