package fr.feepin.go4lunch.data.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.core.location.LocationManagerCompat;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import fr.feepin.go4lunch.utils.PermissionUtils;
import io.reactivex.rxjava3.core.Single;

import static fr.feepin.go4lunch.Constants.NO_LOCATION_PERMISSION_MESSAGE;

@Singleton
public class FusedLocationService implements LocationService {

    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private RxDataStore<Preferences> rxDatastore;

    private Preferences.Key<Double> LATEST_LATITUDE = PreferencesKeys.doubleKey("latest_latitude");
    private Preferences.Key<Double> LATEST_LONGITUDE = PreferencesKeys.doubleKey("latest_longitude");

    @Inject
    public FusedLocationService(@ApplicationContext Context context, RxDataStore<Preferences> rxDataStore) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        fusedLocationProviderClient = new FusedLocationProviderClient(context);;

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
                    new LocationCallback(){
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
