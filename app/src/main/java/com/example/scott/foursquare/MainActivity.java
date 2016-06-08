package com.example.scott.foursquare;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
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
    private TipAdapter tipAdapter;
    private ArrayList<Tip> mTips;

    private FoursquareAPIClient mFoursquareClient;
    private RelativeLayout mListLayout;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.location_list);
        mButton = (Button) findViewById(R.id.get_locations_button);
        mListLayout = (RelativeLayout) findViewById(R.id.location_list_layout);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mTips = new ArrayList<>();
        tipAdapter = new TipAdapter(mTips);
        mRecyclerView.setAdapter(tipAdapter);

        mFoursquareClient = FoursquareAPIClient.getInstance(this);

        if (isConnected()) {
            getNearbyLocations();
        } else {
            mButton.setVisibility(View.VISIBLE);
            mListLayout.setVisibility(View.GONE);
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
    public void onTipResponse(JSONObject tipResponse, int index) {
        try {
            // extract needed attributes from response
            JSONArray tips = tipResponse.getJSONObject("response").getJSONObject("tips").getJSONArray("items");
            String body;
            // TODO: 6/8/16 Perhaps get another location if no tips for this one
            if (tips.length() != 0) {
                JSONObject jsonTip = tips.getJSONObject(0);
                body = jsonTip.getString("text");
                mTips.get(index).setBody(body);
                tipAdapter.notifyDataSetChanged();
            }

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

                Tip tip = new Tip(name, null);
                mTips.add(tip);
                tipAdapter.notifyDataSetChanged();
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
        mTips.clear();
        mFoursquareClient.getNearby(Constants.LOCATION);

        mButton.setVisibility(View.GONE);
        mListLayout.setVisibility(View.VISIBLE);
    }
}
