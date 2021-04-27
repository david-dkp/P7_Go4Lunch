package fr.feepin.go4lunch.data.maps;


import fr.feepin.go4lunch.data.maps.models.NearbySearchResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesApi {

    @GET("maps/api/place/nearbysearch/json")
    Single<NearbySearchResponse> getNearbySearch(
            @Query("key") String apiKey,
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("type") String type
    );
}
