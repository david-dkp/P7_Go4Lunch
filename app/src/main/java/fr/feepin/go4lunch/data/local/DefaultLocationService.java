package fr.feepin.go4lunch.data.local;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class DefaultLocationService implements LocationService {

    private final RxDataStore<Preferences> rxDatastore;

    private final Preferences.Key<Double> LATEST_LATITUDE = PreferencesKeys.doubleKey("latest_latitude");
    private final Preferences.Key<Double> LATEST_LONGITUDE = PreferencesKeys.doubleKey("latest_longitude");

    private final LocationManager locationManager;
    private final String provider;

    @Inject
    public DefaultLocationService(@ApplicationContext Context context, RxDataStore<Preferences> rxDataStore) {
        this.rxDatastore = rxDataStore;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        provider = locationManager.getBestProvider(criteria, true);
    }

    @Override
    public Single<LatLng> getLocation() {
        return getCurrentLocation()
                .flatMap(latLng -> saveLocationInPrefs(latLng)
                        .map(prefs -> latLng))
                .onErrorResumeWith(getLatestPositionFromPrefs());
    }

    @SuppressLint("MissingPermission")
    private Single<LatLng> getCurrentLocation() {
        return Single.create(e -> {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    locationManager.removeUpdates(this);
                    e.onSuccess(latLng);
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {

                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }
            };

            e.setCancellable(() -> {
                locationManager.removeUpdates(locationListener);
            });

            locationManager.requestSingleUpdate(
                    provider,
                    locationListener,
                    Looper.getMainLooper()
            );
        });
    }

    private Single<Preferences> saveLocationInPrefs(LatLng location) {
        return rxDatastore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(LATEST_LATITUDE, location.latitude);
            mutablePreferences.set(LATEST_LONGITUDE, location.longitude);
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

}
