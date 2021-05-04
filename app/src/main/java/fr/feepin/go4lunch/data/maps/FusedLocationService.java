package fr.feepin.go4lunch.data.maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class FusedLocationService implements LocationService {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private final RxDataStore<Preferences> rxDatastore;

    private final Preferences.Key<Double> LATEST_LATITUDE = PreferencesKeys.doubleKey("latest_latitude");
    private final Preferences.Key<Double> LATEST_LONGITUDE = PreferencesKeys.doubleKey("latest_longitude");

    @Inject
    public FusedLocationService(@ApplicationContext Context context, RxDataStore<Preferences> rxDataStore) {
        fusedLocationProviderClient = new FusedLocationProviderClient(context);
        this.rxDatastore = rxDataStore;
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    @Override
    public Single<Location> getCurrentPosition() {
        return Single.create(e -> {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setNumUpdates(1);
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            fusedLocationProviderClient.removeLocationUpdates(this);
                            lastKnownLocation = locationResult.getLastLocation();
                            saveLocationInPrefs(lastKnownLocation);
                            e.onSuccess(locationResult.getLastLocation());
                        }
                    },
                    Looper.getMainLooper()
            );
        });
    }

    private void saveLocationInPrefs(Location location) {
        rxDatastore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(LATEST_LATITUDE, location.getLatitude());
            mutablePreferences.set(LATEST_LONGITUDE, location.getLongitude());
            return Single.just(mutablePreferences);
        });
    }

    public Single<LatLng> getLatestPositionFromPrefs() {
        return rxDatastore.data().map(preferences -> {
            Double latitude = preferences.get(LATEST_LATITUDE);
            Double longitude = preferences.get(LATEST_LONGITUDE);
            return new LatLng(latitude, longitude);
        }).firstOrError();
    }

    @Override
    public Location getLastKnownPosition() {
        return lastKnownLocation;
    }
}
