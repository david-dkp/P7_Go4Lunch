 package fr.feepin.go4lunch.data.maps;

import android.graphics.Bitmap;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.maps.caches.PlacesPhotoCache;
import fr.feepin.go4lunch.data.maps.models.NearbySearchResponse;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class MapsRepositoryCaching implements MapsRepository{

    private DefaultMapsRepository defaultMapsRepository;
    private PlacesPhotoCache placesPhotoCache;

    @Inject
    public MapsRepositoryCaching(DefaultMapsRepository defaultMapsRepository, PlacesPhotoCache placesPhotoCache) {
        this.defaultMapsRepository = defaultMapsRepository;
        this.placesPhotoCache = placesPhotoCache;
    }

    @Override
    public Single<NearbySearchResponse> getNearbySearch(String apiKey, String location, int radius) {
        return defaultMapsRepository.getNearbySearch(apiKey, location, radius);
    }

    @Override
    public Single<FetchPhotoResponse> getRestaurantPhoto(String placeId, PhotoMetadata photoMetadata) {

        Bitmap cachedPhoto = placesPhotoCache.getPlacePhoto(placeId);

        if (cachedPhoto != null) {
            return Single.just(FetchPhotoResponse.newInstance(cachedPhoto));
        } else {
            return defaultMapsRepository.getRestaurantPhoto(placeId, photoMetadata)
                    .doOnSuccess(fetchPhotoResponse -> {
                        placesPhotoCache.storePhoto(placeId, fetchPhotoResponse.getBitmap());
                    });
        }
    }

    @Override
    public Single<FindAutocompletePredictionsResponse> getRestaurantsFromQuery(AutocompleteSessionToken token, String query, LatLng origin, LocationRestriction locationRestriction) {
        return defaultMapsRepository.getRestaurantsFromQuery(token, query, origin, locationRestriction);
    }

    @Override
    public Single<Place> getRestaurantDetails(String placeId, List<Place.Field> fields, AutocompleteSessionToken token) {
        return defaultMapsRepository.getRestaurantDetails(placeId, fields, token);
    }

    @Override
    public Location getLastKnownLocation() {
        return defaultMapsRepository.getLastKnownLocation();
    }

    @Override
    public Single<Location> getCurrentLocation() {
        return defaultMapsRepository.getCurrentLocation();
    }

    @Override
    public Single<LatLng> getLatestPositionFromPrefs() {
        return defaultMapsRepository.getLatestPositionFromPrefs();
    }
}
