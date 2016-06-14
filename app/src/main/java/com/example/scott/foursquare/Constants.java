package com.example.scott.foursquare;

public class Constants {
    private static final String API_ROOT = "https://api.foursquare.com/v2";

    private static final String CLIENT_ID = "BZZKEQXCI54CEPD4BSRQFE4TD1DUWYVEBCAC3RNULGLHJNUN";
    private static final String CLIENT_SECRET = "CLPY1BXZOOGTJ4MPNMJAMQNKBZEP3NJUKMPWX50ITOUJLXKC";
    private static final String CREDENTIALS = "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;

    private static final String PARAM_VERSION_DATE = "&v=20160608";
    private static final String PARAM_MODE = "&m=foursquare";

    public static final String BASE_PARAMS = CREDENTIALS + PARAM_VERSION_DATE + PARAM_MODE;

    public static final String VENUES_PATH = API_ROOT + "/venues";
    public static final String VENUES_SEARCH = VENUES_PATH + "/search?" + BASE_PARAMS;
    public static final String VENUES_TIPS = "/tips?" + BASE_PARAMS;

    public static final String PARAM_LIMIT = "&limit=";
    public static final int LOCATION_LIMIT = 10;
    public static final int TIP_LIMIT = 1;

    public static final String COORDINATES = "38.86,-77.38";
    public static final String LAT_LONG_PARAM = "&ll=";

    public static final String SORT_PARAM = "&sort=recent";
}
