package com.example.scott.foursquare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.scott.foursquare.Adapters.TipAdapter;
import com.example.scott.foursquare.Models.Tip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        FoursquareAPIClient.FoursquareAPIListener,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "Main";
    private static final int PERMISSION_REQUEST_CODE = 7000;
    private JSONArray mLocations;
    private TipAdapter tipAdapter;
    private ArrayList<Tip> mTips;

    private FoursquareAPIClient mFoursquareClient;
    private RelativeLayout mListLayout;
    private Button mButton;

    private GoogleApiClient apiClient;
    private LocationRequest mLocationRequest;

    private String mLatLong;

    @Override
    protected void onStart() {
        apiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        apiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(60 * 1000) // 30 seconds
                .setFastestInterval(15 * 1000);

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
        if (isConnected()) {
            getNearbyLocations();
        } else {
            Toast.makeText(MainActivity.this, "No active internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getNearbyLocations() {
        mTips.clear();
        mFoursquareClient.getNearby(mLatLong == null? Constants.LOCATION : mLatLong);

        mButton.setVisibility(View.GONE);
        mListLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "No permission");
            return;
        }
        // TODO: 6/10/16 prompt user to turn on gps if it's off
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if (mLastLocation != null) {
            mLatLong = (String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude()));
            Log.d(TAG, "Got location: " + mLatLong);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLatLong = (String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
        Log.d(TAG, "Got location: " + mLatLong);
        getNearbyLocations();
    }
}
