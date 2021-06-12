package fr.feepin.go4lunch.data;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;

import java.util.List;

import fr.feepin.go4lunch.data.models.dtos.NearbySearchDto;
import io.reactivex.rxjava3.core.Single;

public interface MapsRepository {

    Single<NearbySearchDto> getNearbySearch(String apiKey, String location, int radius);

    Single<FetchPhotoResponse> getRestaurantPhoto(String placeId, PhotoMetadata photoMetadata);

    Single<FindAutocompletePredictionsResponse> getRestaurantsFromQuery(AutocompleteSessionToken token, String query, LatLng origin, LocationRestriction locationRestriction);

    Single<Place> getRestaurantDetails(String placeId, List<Place.Field> fields, AutocompleteSessionToken token);

    Location getLastKnownLocation();

    Single<Location> getCurrentLocation();

    Single<LatLng> getLatestPositionFromPrefs();
}
