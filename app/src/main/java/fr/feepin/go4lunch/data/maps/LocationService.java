package fr.feepin.go4lunch.data.maps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import io.reactivex.rxjava3.core.Single;

public interface LocationService {

    Single<Location> getCurrentPosition();

    Location getLastKnownPosition();

    Single<LatLng> getLatestPositionFromPrefs();
}
