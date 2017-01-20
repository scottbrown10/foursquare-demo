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
import android.net.Uri;
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
import android.widget.TextView;
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

// TODO: 10/1/16 add button for users to retry location request
public class MainActivity extends AppCompatActivity implements
        FoursquareAPIClient.FoursquareAPIListener,
        TipAdapter.TipListener,
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

    private RecyclerView mRecyclerView;
    private RelativeLayout mListLayout;
    private Button mButton;
    private TextView mGetNearbyTV;

    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;

    private FoursquareAPIClient mFoursquareClient;

    private String mCoordinates;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTips = new ArrayList<>();
        initViews();

        mFoursquareClient = FoursquareAPIClient.getInstance(this);

        requestLocationPermission();
        setupLocationServices();
    }

    private void requestLocationPermission() {
        // check for permission at runtime on Android >= M
        // on Android < M, permission was granted at install time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions
                        (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    /**
     * Build Google API Client and LocationRequest object
     */
    private void setupLocationServices() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(1000 * 5)
                .setFastestInterval(1000 * 3);
    }

    /**
     * Check if location settings are enabled, then if user is connected to internet.
     * If both are true, then invoke 4Square, else show prompt user to turn them on.
     */
    private void attemptGetNearbyLocations() {
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

                    // location settings satisfied
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (isConnected()) {
                            getNearbyLocations();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.prompt_for_internet, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    // location settings not satisfied, but satisfiable
                    // show dialog to enable location settings
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "onResult: " + e.getMessage());
                        }
                        break;

                    // location settings not satisfied, but also unsatisfiable
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(MainActivity.this, R.string.location_not_satisfiable, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    /**
     * Get the nearby locations and update the screen accordingly
     */
    private void getNearbyLocations() {
        // TODO: 10/1/16 show a progress bar
        mButton.setVisibility(View.GONE);
//        mListLayout.setVisibility(View.VISIBLE);
        // try getting coordinates from location services. upon failure, use default
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLastLocation != null) {
            mCoordinates = (String.valueOf(mLastLocation.getLatitude() + "," + String.valueOf(mLastLocation.getLongitude())));
            Log.d(TAG, "Got location: " + mCoordinates);
        }
        if (mCoordinates == null) {
            mCoordinates = Constants.COORDINATES;
            Log.d(TAG, "Use default location");
        }
        mFoursquareClient.getNearby(mCoordinates); // response is handled asynchronously

    }

    private void getTips() {
        mTips.clear();
        int length = mLocations.length();
        for (int i = 0; i < length; i++) {
            try {
                // extract needed values from location objects
                JSONObject location = mLocations.getJSONObject(i);
                String name = location.getString("name");
                String id = location.getString("id");
                JSONObject j = location.getJSONObject("location").getJSONArray("labeledLatLngs").getJSONObject(0);
                float lat = Float.parseFloat(j.getString("lat"));
                float lng = Float.parseFloat(j.getString("lng"));

                Tip tip = new Tip(name, null, lat, lng);
                mTips.add(tip);
                mFoursquareClient.getTip(id, i); // response handled asynchronously
            } catch (JSONException e) {
                Log.e(TAG, "getTips: " + e.getMessage());
            }
        }
        tipAdapter.notifyDataSetChanged();
    }

    /* Foursquare listener overridden methods */

    @Override
    public void onSearchResponse(JSONObject searchResponse) {
        JSONArray locations = null;
        try {
            // extract needed attributes from response
            locations = searchResponse.getJSONObject("response").getJSONArray("venues");
        } catch (JSONException e) {
            Log.e(TAG, "onSearchResponse: " + e.getMessage());
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
            Log.e(TAG, "onTipResponse: " + e.getMessage());
        }
    }

    /* Google api overridden methods */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // don't request updates periodically. only do it on demand
        //startLocationUpdates();
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
            attemptGetNearbyLocations();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            Toast.makeText(MainActivity.this,  R.string.location_enabled,Toast.LENGTH_SHORT).show();
            attemptGetNearbyLocations();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_locations_button:
            case R.id.get_nearby_TV:
                attemptGetNearbyLocations();
                break;
        }
    }

    /* Tip listener overridden methods */

    @Override
    public void onTipClicked(int pos) {
        Tip tip = mTips.get(pos);

        // setup intent to show this location on map with pin and label
        String latLong = "" + tip.latitude + "," + tip.longitude;
        String uriString = "geo:0,0?q=" + latLong + "(" + tip.locationName + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uriString));

        // verify the user has an app capable of displaying the location on a map
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_map_app, Toast.LENGTH_SHORT).show();
        }
    }

    /* Utility methods */

    /**
     * Initialize everything related to views, including adapters and click listeners
     */
    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.location_list);
        mButton = (Button) findViewById(R.id.get_locations_button);
        mListLayout = (RelativeLayout) findViewById(R.id.location_list_layout);
        mGetNearbyTV = (TextView) findViewById(R.id.get_nearby_TV);

        mRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mButton.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.VISIBLE);

        mButton.setOnClickListener(this);
        mGetNearbyTV.setOnClickListener(this);

        tipAdapter = new TipAdapter(mTips);
        mRecyclerView.setAdapter(tipAdapter);
    }

    /**
     * Check if user is currently online
     * @return True if user is connected to internet, else false
     */
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

/*    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }*/

}
