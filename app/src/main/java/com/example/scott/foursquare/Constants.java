package com.example.scott.foursquare;

public class Constants {
    private static final String CLIENT_ID = "BZZKEQXCI54CEPD4BSRQFE4TD1DUWYVEBCAC3RNULGLHJNUN";
    private static final String CLIENT_SECRET = "CLPY1BXZOOGTJ4MPNMJAMQNKBZEP3NJUKMPWX50ITOUJLXKC";

    public static final  String API_ROOT = "https://api.foursquare.com/v2";
    public static final  String VENUES_PATH = API_ROOT + "/venues";
    public static final  String VENUES_SEARCH = VENUES_PATH + "/search";
    public static final  String VENUES_TIPS = "/tips";

    public static final  int LOCATION_LIMIT = 10;
    public static final  int TIP_LIMIT = 1;

    public static final  String CREDENTIALS = "&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;

    public static final String location = "38.858679, -77.384027";
}
