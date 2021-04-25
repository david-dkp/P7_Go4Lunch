package fr.feepin.go4lunch.data.maps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;

import fr.feepin.go4lunch.data.maps.models.NearbySearchResponse;
import io.reactivex.rxjava3.core.Single;

interface MapsRepository {

    Single<NearbySearchResponse> getNearbySearch(String apiKey, String location, String radius);

    Single<FetchPhotoResponse> getRestaurantPhoto(String ref, int width, int height);

    Single<FindAutocompletePredictionsResponse> getRestaurantsFromQuery(AutocompleteSessionToken token, String query, LatLng origin, LocationRestriction locationRestriction);

    Single<Place> getRestaurantDetails(String placeId);

    Single<Location> getLastKnownLocation();

    Single<Location> getCurrentLocation();
}
