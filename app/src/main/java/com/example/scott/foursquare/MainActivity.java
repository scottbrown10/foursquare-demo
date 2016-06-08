package com.example.scott.foursquare;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.scott.foursquare.Adapters.TipAdapter;
import com.example.scott.foursquare.Models.Tip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FoursquareAPIClient.FoursquareAPIListener, View.OnClickListener {
    private JSONArray mLocations;
    private ArrayList<Tip> mTips;
    private TipAdapter tipAdapter;

    private FoursquareAPIClient mFoursquareClient;
    private ListView mListView;
    private RelativeLayout mListLayout;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.location_list);
        mButton = (Button) findViewById(R.id.get_locations_button);
        mListLayout = (RelativeLayout) findViewById(R.id.location_list_layout);

        mTips = new ArrayList<>();
        tipAdapter = new TipAdapter(this, R.layout.location_cell_layout, mTips);
        mListView.setAdapter(tipAdapter);
        mFoursquareClient = FoursquareAPIClient.getInstance(this);

        if (isConnected()) {
            getNearbyLocations();
        } else {
            mButton.setOnClickListener(this);
        }
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
            if (tips.length() == 0) { // TODO: 6/8/16 Perhaps get another location? 
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


    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onClick(View v) {
        // TODO: 6/8/16 use gps for location if available
        if (isConnected()) {
            getNearbyLocations();
        } else {
            Toast.makeText(MainActivity.this, "No active internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getNearbyLocations() {
        mFoursquareClient.getNearby(Constants.LOCATION);
        mButton.setVisibility(View.GONE);
        mListLayout.setVisibility(View.VISIBLE);
    }
}
