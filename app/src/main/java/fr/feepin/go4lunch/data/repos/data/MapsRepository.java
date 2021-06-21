package fr.feepin.go4lunch.data.repos.data;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

import javax.annotation.Nullable;

import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.domain.PlacePrediction;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface MapsRepository {

    Single<List<NearPlace>> getNearPlaces(String apiKey, LatLng location, int radius);

    Single<Bitmap> getPlacePhoto(String placeId, PhotoMetadata photoMetadata);

    Single<List<PlacePrediction>> getPlacePredictionsFromQuery(AutocompleteSessionToken token, String query, LatLng origin);

    Single<Place> getPlace(String placeId, List<Place.Field> fields, @Nullable AutocompleteSessionToken token);

    Single<LatLng> getLocation();
}
