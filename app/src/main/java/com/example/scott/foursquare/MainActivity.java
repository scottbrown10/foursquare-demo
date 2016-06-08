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
        mFoursquareClient.getNearby(Constants.LOCATION);

        mListView = (ListView) findViewById(R.id.location_list);
        tipAdapter = new TipAdapter(this, R.layout.location_cell_layout, mTips);
        mListView.setAdapter(tipAdapter);
    }

    @Override
    public void onSearchResponse(JSONObject searchResponse) {
        JSONArray locations = null;
        try {
            // extract needed attributes from response
            locations = searchResponse.getJSONObject("response").getJSONArray("venues");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mLocations = locations;
        getTips();
    }

    @Override
    public void onTipResponse(String locationName, JSONObject tipResponse, int index) {
        try {
            // extract needed attributes from response
            JSONArray tips = tipResponse.getJSONObject("response").getJSONObject("tips").getJSONArray("items");
            String body = null;
            if (tips.length() == 0) {
                body = getResources().getString(R.string.no_tip);
            } else {
                JSONObject jsonTip = tips.getJSONObject(0);
                body = jsonTip.getString("text");
            }

            Tip tip = new Tip(locationName, body);
            mTips.add(tip);
            tipAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getTips() {
        int length = mLocations.length();
        for (int i = 0; i < length; i++) {
            try {
                // extract needed values from location objects
                JSONObject location = mLocations.getJSONObject(i);
                String name = location.getString("name");
                String id = location.getString("id");
                mFoursquareClient.getTip(name, id, i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
