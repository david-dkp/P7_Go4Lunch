package fr.feepin.go4lunch.data.maps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.maps.models.NearbySearchResponse;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class DefaultMapsRepository implements MapsRepository {

    private LocationService locationService;

    private PlacesClient placesClient;

    private PlacesApi placesApi;

    @Inject
    public DefaultMapsRepository(LocationService locationService, PlacesClient placesClient, PlacesApi placesApi) {
        this.locationService = locationService;
        this.placesClient = placesClient;
        this.placesApi = placesApi;
    }

    @Override
    public Single<NearbySearchResponse> getNearbySearch(String apiKey, String location, String radius) {

        return placesApi.getNearbySearch(apiKey, location, radius, "restaurant");
    }

    @Override
    public Single<FetchPhotoResponse> getRestaurantPhoto(String ref, int width, int height) {
        PhotoMetadata photoMetadata = PhotoMetadata.builder(ref)
                .setWidth(width)
                .setHeight(height)
                .build();

        FetchPhotoRequest fetchPhotoRequest = FetchPhotoRequest.builder(photoMetadata).build();

        return Single.create(emitter -> {
            FetchPhotoResponse fetchPhotoResponse = Tasks.await(placesClient.fetchPhoto(fetchPhotoRequest));
            emitter.onSuccess(fetchPhotoResponse);
        });
    }

    @Override
    public Single<FindAutocompletePredictionsResponse> getRestaurantsFromQuery(AutocompleteSessionToken token, String query, LatLng origin, LocationRestriction locationRestriction) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setCountry("FR")
                .setOrigin(origin)
                .setLocationRestriction(locationRestriction)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery(query)
                .build();

        return Single.create(e -> {
            FindAutocompletePredictionsResponse findAutocompletePredictionsResponse = Tasks.await(placesClient.findAutocompletePredictions(request));
            e.onSuccess(findAutocompletePredictionsResponse);
        });
    }

    @Override
    public Single<Place> getRestaurantDetails(String placeId) {
        return Single.create( e -> {

            FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(
                    placeId,
                    Arrays.asList(Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.ADDRESS)
                    ).build();

            Place place = Tasks.await(placesClient.fetchPlace(fetchPlaceRequest)).getPlace();

            e.onSuccess(place);
        });
    }

    @Override
    public Single<Location> getLastKnownLocation() {
        return locationService.getLastKnownPosition();
    }

    @Override
    public Single<Location> getCurrentLocation() {
        return locationService.getCurrentPosition();
    }
}