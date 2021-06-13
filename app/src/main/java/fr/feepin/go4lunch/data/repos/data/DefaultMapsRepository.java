package fr.feepin.go4lunch.data.repos.data;

import android.graphics.Bitmap;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.data.local.LocationService;
import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.domain.PlacePrediction;
import fr.feepin.go4lunch.data.models.dtos.NearbySearchDto;
import fr.feepin.go4lunch.data.models.dtos.NearbySearchResultDto;
import fr.feepin.go4lunch.data.models.mappers.Mapper;
import fr.feepin.go4lunch.data.remote.apis.PlacesApi;
import fr.feepin.go4lunch.data.remote.caches.AutocompleteCache;
import fr.feepin.go4lunch.data.remote.caches.NearbySearchCache;
import fr.feepin.go4lunch.data.remote.caches.PlacesPhotoCache;
import fr.feepin.go4lunch.utils.LatLngUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class DefaultMapsRepository implements MapsRepository {

    private final LocationService locationService;
    private final PlacesClient placesClient;
    private final PlacesApi placesApi;

    private final AutocompleteCache autocompleteCache;
    private final NearbySearchCache nearbySearchCache;
    private final PlacesPhotoCache placesPhotoCache;

    private final Mapper<NearbySearchResultDto, NearPlace> nearbySearchMapper;
    private final Mapper<AutocompletePrediction, PlacePrediction> autocompleteMapper;

    @Inject
    public DefaultMapsRepository(
            Mapper<NearbySearchResultDto, NearPlace> nearbySearchMapper,
            Mapper<AutocompletePrediction, PlacePrediction> autocompleteMapper,
            LocationService locationService,
            PlacesClient placesClient,
            PlacesApi placesApi,
            AutocompleteCache autocompleteCache,
            NearbySearchCache nearbySearchCache,
            PlacesPhotoCache placesPhotoCache) {
        this.locationService = locationService;
        this.placesClient = placesClient;
        this.placesApi = placesApi;
        this.placesPhotoCache = placesPhotoCache;
        this.nearbySearchMapper = nearbySearchMapper;
        this.autocompleteMapper = autocompleteMapper;
        this.autocompleteCache = autocompleteCache;
        this.nearbySearchCache = nearbySearchCache;
    }

    @Override
    public Single<List<NearPlace>> getNearPlaces(String apiKey, LatLng location, int radius) {

        Single<NearbySearchDto> nearbySearchSingle;

        NearbySearchDto cachedNearbySearch = nearbySearchCache.getNearbySearch(location);

        if (cachedNearbySearch != null) {
            nearbySearchSingle = Single.just(cachedNearbySearch);
        } else {
            nearbySearchSingle = placesApi.getNearbySearch(apiKey, location.latitude + "," + location.longitude, radius, "restaurant")
                    .doOnSuccess(nearbySearch -> nearbySearchCache.cacheNearbySearch(location, nearbySearch));
        }

        return nearbySearchSingle
                .flatMapObservable(nearbySearchDto -> Observable
                        .fromIterable(nearbySearchDto.getResults())
                        .map(nearbySearchMapper::toEntity))
                .toList();
    }

    @Override
    public Single<Bitmap> getPlacePhoto(String placeId, NearPlace.Photo photo) {

        Bitmap cachedBitmap = placesPhotoCache.getPlacePhoto(placeId);

        if (cachedBitmap != null) return Single.just(cachedBitmap);

        return Single.create(emitter -> {
            PhotoMetadata photoMetadata = PhotoMetadata.builder(photo.getReference())
                    .setWidth(photo.getWidth())
                    .setHeight(photo.getHeight())
                    .build();

            FetchPhotoRequest fetchPhotoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .build();

            FetchPhotoResponse fetchPhotoResponse = Tasks.await(placesClient.fetchPhoto(fetchPhotoRequest));

            Bitmap bitmap = fetchPhotoResponse.getBitmap();

            placesPhotoCache.storePhoto(placeId, bitmap);

            emitter.onSuccess(bitmap);
        });
    }

    @Override
    public Single<List<PlacePrediction>> getPlacePredictionsFromQuery(AutocompleteSessionToken token, String query, LatLng origin) {
        Single<FindAutocompletePredictionsResponse> autocompleteSingle;

        FindAutocompletePredictionsResponse cachedAutocomplete = autocompleteCache.getAutocompleteResponse(Pair.create(query, origin));

        if (cachedAutocomplete != null) {
            autocompleteSingle = Single.just(cachedAutocomplete);
        } else {
            LatLngBounds latLngBounds = LatLngUtils.toBounds(origin, Constants.PREDICTION_SEARCH_RADIUS);
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setCountry("FR")
                    .setOrigin(origin)
                    .setLocationRestriction(RectangularBounds.newInstance(latLngBounds))
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setQuery(query)
                    .build();

            autocompleteSingle = Single.create(e -> {
                FindAutocompletePredictionsResponse findAutocompletePredictionsResponse = Tasks.await(placesClient.findAutocompletePredictions(request));
                e.onSuccess(findAutocompletePredictionsResponse);
            });
        }

        return autocompleteSingle
                .flatMapObservable(findAutocompletePredictionsResponse -> Observable.fromIterable(findAutocompletePredictionsResponse.getAutocompletePredictions()))
                .map(autocompleteMapper::toEntity)
                .toList()
                ;
    }

    @Override
    public Single<Place> getPlace(String placeId, List<Place.Field> fields, @Nullable AutocompleteSessionToken token) {
        return Single.create(e -> {
            FetchPlaceRequest.Builder builder = FetchPlaceRequest.builder(
                    placeId,
                    fields
            );

            if (token != null) {
                builder.setSessionToken(token);
            }

            FetchPlaceRequest fetchPlaceRequest = builder.build();

            Place place = Tasks.await(placesClient.fetchPlace(fetchPlaceRequest)).getPlace();

            e.onSuccess(place);
        });
    }

    @Override
    public Single<LatLng> getLocation() {
        return locationService.getLocation();
    }
}
