package fr.feepin.go4lunch.data;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.domain.PlacePrediction;
import io.reactivex.rxjava3.core.Single;

public interface MapsRepository {

    Single<List<NearPlace>> getNearPlaces(String apiKey, String location, int radius);

    Single<Bitmap> getPlacePhoto(String placeId, NearPlace.Photo photo);

    Single<List<PlacePrediction>> getPlacePredictionsFromQuery(AutocompleteSessionToken token, String query, LatLng origin);

    Single<Place> getPlace(String placeId, List<Place.Field> fields, AutocompleteSessionToken token);

    Single<LatLng> getLocation();
}
