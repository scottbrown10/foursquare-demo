package com.example.scott.foursquare.Models;

public class Tip {

    public String locationName;
    public String body;
    public float latitude, longitude;

    public Tip (String locationName, String body, float lat, float lng) {
        this.locationName = locationName;
        this.body = body;
        this.latitude = lat;
        this.longitude = lng;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

