package com.example.scott.foursquare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    FoursquareAPIClient mClient;
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = FoursquareAPIClient.getInstance(this);
        mClient.getLocations();
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.location_list);
    }
}
