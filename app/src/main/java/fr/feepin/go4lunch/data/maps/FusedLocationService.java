package fr.feepin.go4lunch.data.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

    @Inject
    public FusedLocationService(@ApplicationContext Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        fusedLocationProviderClient = new FusedLocationProviderClient(context);;
        this.context = context;
    }
    
    @SuppressLint("MissingPermission")
    @Override
    public Single<Location> getCurrentPosition() {
        return Single.create(e -> {

            if (!PermissionUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                e.onError(new Throwable(NO_LOCATION_PERMISSION_MESSAGE));
                return;
            }

            LocationRequest locationRequest = new LocationRequest();
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
                            e.onSuccess(locationResult.getLastLocation());
                        }
                    },
                    Looper.getMainLooper()
            );
        });
    }

    @Override
    public Single<Location> getLastKnownPosition() {
        return Single.just(lastKnownLocation);
    }
}
