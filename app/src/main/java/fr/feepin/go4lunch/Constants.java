package fr.feepin.go4lunch;

public class Constants {
    public static final String MAPS_BASE_URL = "https://maps.googleapis.com/";
    public static final float MAPS_MY_LOCATION_ZOOM_LEVEL = 16;
    public static final float MAPS_RESTAURANT_ZOOM_LEVEL = 19;

    public static final String NO_LOCATION_PERMISSION_MESSAGE = "No location permission";
    public static final String LOCATION_DISABLED_MESSAGE = "Location is disabled";

    public static final String USER_PREFS_NAME = "User preferences";

    public static final int NEARBY_SEARCH_RADIUS = 300;
    public static final int PREDICTION_SEARCH_RADIUS = 600;

    public static final String EXTRA_AUTOCOMPLETE_TOKEN = "EXTRA_AUTOCOMPLETE_TOKEN";
    public static final String EXTRA_RESTAURANT_ID = "EXTRA_RESTAURANT_ID";

    //NotifyWorker
    public static final String NOTIFY_WORKER_TAG = "NOTIFY_WORKER_TAG";
    public static final String KEY_RESTAURANT_ID = "KEY_RESTAURANT_ID";
    public static final String KEY_RESTAURANT_NAME = "KEY_RESTAURANT_NAME";
    public static final String KEY_RESTAURANT_ADDRESS = "KEY_RESTAURANT_ADDRESS";
    public static final String EAT_NOTIFICATION_ID = "EAT_NOTIFICATION_ID";

    public static final String VISIT_RESTAURANT_TAG = "VISIT_RESTAURANT_TAG";
    public static final int HOUR_NOTIFICATION_FIRE = 12;
    public static final int HOUR_VISIT_RESTAURANT_DELAY = 2;

    public static final int MINUTES_VISIT_RESTAURANT_BACKOFF_DELAY = 30;
}
