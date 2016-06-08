package com.example.scott.foursquare;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class FoursquareAPIClient {
    private static FoursquareAPIClient mInstance;
    private static Context mContext;
    private RequestQueue mRequestQueue;

    private FoursquareAPIClient(Context context) {
        /* associate singleton with Application rather than Activity
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

    public void getNearby(String location) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constants.VENUES_SEARCH, null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        ((FoursquareAPIListener)mContext).onSearchResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        mRequestQueue.add(jsonArrayRequest);
    }

    public void getTip(final String location, final int index) {
        String url = Constants.VENUES_PATH + location + Constants.VENUES_TIPS;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ((FoursquareAPIListener)mContext).onTipResponse(location, response, index);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        mRequestQueue.add(jsonObjectRequest);
    }

    public interface FoursquareAPIListener {
        void onSearchResponse(JSONArray locations);
        void onTipResponse(String locationName, JSONObject tip, int index); // index is position of location if local array
    }
}
