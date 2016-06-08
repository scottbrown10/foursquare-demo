package com.example.scott.foursquare;

public class Constants {
    private static String CLIENT_ID = "BZZKEQXCI54CEPD4BSRQFE4TD1DUWYVEBCAC3RNULGLHJNUN";
    private static String CLIENT_SECRET = "CLPY1BXZOOGTJ4MPNMJAMQNKBZEP3NJUKMPWX50ITOUJLXKC";

    private static String API_ROOT = "https://api.foursquare.com/v2";
    public static String VENUES_PATH = API_ROOT + "/venues";
    public static String VENUES_SEARCH = VENUES_PATH + "/search";
    public static String VENUES_TIPS = "/tips";

    public static int LOCATION_LIMIT = 10;
    public static int TIP_LIMIT = 1;

    public static String CREDENTIALS = "&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;
}
