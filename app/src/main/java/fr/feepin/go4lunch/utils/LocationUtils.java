package fr.feepin.go4lunch.utils;

import android.content.Context;
import android.location.LocationManager;

import androidx.core.location.LocationManagerCompat;

public class LocationUtils {

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

}
