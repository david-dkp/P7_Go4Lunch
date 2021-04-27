package fr.feepin.go4lunch.data.maps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;

import java.util.List;

import fr.feepin.go4lunch.data.maps.models.NearbySearchResponse;
import io.reactivex.rxjava3.core.Single;

public interface MapsRepository {

    Single<NearbySearchResponse> getNearbySearch(String apiKey, String location, int radius);

    Single<FetchPhotoResponse> getRestaurantPhoto(PhotoMetadata photoMetadata);

    Single<FindAutocompletePredictionsResponse> getRestaurantsFromQuery(AutocompleteSessionToken token, String query, LatLng origin, LocationRestriction locationRestriction);

    Single<Place> getRestaurantDetails(String placeId, List<Place.Field> fields);

    Location getLastKnownLocation();

    Single<Location> getCurrentLocation();

    Single<LatLng> getLatestPositionFromPrefs();
}
