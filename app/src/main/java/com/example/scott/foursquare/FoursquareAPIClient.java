package com.example.scott.foursquare;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.scott.foursquare.APIResponses.Locations;
import com.example.scott.foursquare.APIResponses.Tips;

import java.util.ArrayList;

public class FoursquareAPIClient {
    private static FoursquareAPIClient mInstance;
    private static Context mContext;
    private RequestQueue mRequestQueue;

    private FoursquareAPIClient(Context context) {
        /* associate singleton with Application rather than Activity
         * so it lasts the lifetime of the application
         */
        this.mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mContext = context;
    }

    public static FoursquareAPIClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FoursquareAPIClient(context);
        }
        return mInstance;
    }

    public ArrayList<Locations> getLocations() {
        // TODO: 6/8/16
        return null;
    }

    public ArrayList<Tips> getTips(ArrayList<Locations> locations) {
        // TODO: 6/8/16
        return null;
    }
}
