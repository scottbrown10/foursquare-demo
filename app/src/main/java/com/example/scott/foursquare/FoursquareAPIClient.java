package com.example.scott.foursquare;

/**
 * Client to interact with Foursquare api.
 * The user of this should implement its listener interface to obtain the responses.
 * Responses are returned exactly as they come from the api.
 */

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class FoursquareAPIClient {
    public static final String TAG = "4SquareClient";
    private static FoursquareAPIClient mInstance;
    private static Context mContext;
    private RequestQueue mRequestQueue;

    private FoursquareAPIClient(Context context) {
        /*
         * associate singleton with Application rather than Activity
         * so it lasts the lifetime of the application
         */
        assert (context instanceof FoursquareAPIListener);

        this.mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mContext = context;
    }

    public static FoursquareAPIClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FoursquareAPIClient(context);
        }
        return mInstance;
    }

    public void getNearby(final String location) {
        String searchURL = Constants.VENUES_SEARCH +
                Constants.PARAM_LIMIT + Constants.LOCATION_LIMIT +
                Constants.LAT_LONG_PARAM + location;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, searchURL, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        ((FoursquareAPIListener) mContext).onSearchResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: " + error.toString());
                    }
                });
        mRequestQueue.add(jsonObjectRequest);
    }

    public void getTip(final String name, final String locationID, final int index) {
        String url = Constants.VENUES_PATH + "/" + locationID + Constants.VENUES_TIPS +
                Constants.PARAM_LIMIT + Constants.TIP_LIMIT;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ((FoursquareAPIListener)mContext).onTipResponse(name, response, index);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: " + error.toString());
                    }
                }
        );
        mRequestQueue.add(jsonObjectRequest);
    }

    public interface FoursquareAPIListener {
        void onSearchResponse(JSONObject searchResponse);
        void onTipResponse(String locationName, JSONObject tipResponse, int index); // index is position of location if local array
    }
}
