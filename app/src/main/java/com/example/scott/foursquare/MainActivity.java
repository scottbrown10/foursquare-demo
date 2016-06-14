package com.example.scott.foursquare;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

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
    private static final int LOCATION_REQUEST_CODE = 7000;
    private static final int REQUEST_CHECK_SETTINGS = 7001;

    private JSONArray mLocations;
    private TipAdapter tipAdapter;
    private ArrayList<Tip> mTips;

    private FoursquareAPIClient mFoursquareClient;
    private RelativeLayout mListLayout;
    private Button mButton;

    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;

    private String mCoordinates;
    private boolean mPermissionGranted = false;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestLocationPermission();
        if (mPermissionGranted) {
            setupLocationServices();
        }

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

        mButton.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.GONE);
        mButton.setOnClickListener(this);
    }

    private void setupLocationServices() {
        // create google client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        // create location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(1000 * 5)
                .setFastestInterval(1000 * 3);

        // create builder to check location settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                builder.build());

        // check result of location settings check
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // locations settings satisfied
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // location settings not satisfied, but satisfiable
                        // show dialog to enable location settings
                        try {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // location settings not satisfied, but also unsatisfiable
                        break;
                }
            }
        });
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

    private void getNearbyLocations() {
        mTips.clear();
        // try getting coordinates from location services. if fails, use default
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLastLocation != null) {
            mCoordinates = (String.valueOf(mLastLocation.getLatitude() + "," + String.valueOf(mLastLocation.getLongitude())));
            Log.d(TAG, "Got location: " + mCoordinates);
        }
        if (mCoordinates == null) {
            mCoordinates = Constants.COORDINATES;
            Log.d(TAG, "Use default location");
        }
        mFoursquareClient.getNearby(mCoordinates);

        mButton.setVisibility(View.GONE);
        mListLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions
                        (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        } else {
            mPermissionGranted = true;
        }
    }


    // google api overridden methods
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /* don't request updates periodically. only do it on demand */
        //startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }

    @Override
    public void onLocationChanged(Location location) {
        mCoordinates = (String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
        Log.d(TAG, "Got location: " + mCoordinates);
        //getNearbyLocations();
    }

    @Override
    protected void onStart() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mPermissionGranted = true;
        } else { // user denied location access permission
            mCoordinates = Constants.COORDINATES;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            // location settings have been turned on
        }
    }

    @Override
    public void onClick(View v) {
        if (isConnected()) {
            getNearbyLocations();
        } else {
            Toast.makeText(MainActivity.this, "No active internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    // foursquare listener overridden methods
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
}
