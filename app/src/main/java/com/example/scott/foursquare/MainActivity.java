package com.example.scott.foursquare;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.example.scott.foursquare.Adapters.TipAdapter;
import com.example.scott.foursquare.Models.Tip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

        // TODO: 6/8/16 use gps for location if available
        mFoursquareClient.getNearby(Constants.location);

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

}
