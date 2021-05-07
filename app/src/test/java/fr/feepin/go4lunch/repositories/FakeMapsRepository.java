package fr.feepin.go4lunch.repositories;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;

import java.util.Collections;
import java.util.List;

import fr.feepin.go4lunch.data.maps.MapsRepository;
import fr.feepin.go4lunch.data.maps.models.NearbySearchResponse;
import io.reactivex.rxjava3.core.Single;

public class FakeMapsRepository implements MapsRepository {
    @Override
    public Single<NearbySearchResponse> getNearbySearch(String apiKey, String location, int radius) {
        return Single.just(new NearbySearchResponse("400", Collections.emptyList(), ""));
    }

    @Override
    public Single<FetchPhotoResponse> getRestaurantPhoto(String placeId, PhotoMetadata photoMetadata) {
        return Single.just(FetchPhotoResponse.newInstance(null));
    }

    @Override
    public Single<FindAutocompletePredictionsResponse> getRestaurantsFromQuery(AutocompleteSessionToken token, String query, LatLng origin, LocationRestriction locationRestriction) {
        return Single.just(FindAutocompletePredictionsResponse.newInstance(Collections.emptyList()));
    }

    @Override
    public Single<Place> getRestaurantDetails(String placeId, List<Place.Field> fields, AutocompleteSessionToken token) {
        return Single.just(Place.builder().build());
    }

    @Override
    public Location getLastKnownLocation() {
        return new Location("me");
    }

    @Override
    public Single<Location> getCurrentLocation() {
        return Single.just(new Location("me"));
    }

    @Override
    public Single<LatLng> getLatestPositionFromPrefs() {
        return Single.just(new LatLng(0, 0));
    }
}
