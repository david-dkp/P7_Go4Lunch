package fr.feepin.go4lunch.data.remote.apis;


import fr.feepin.go4lunch.data.models.dtos.NearbySearchDto;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesApi {

    @GET("maps/api/place/nearbysearch/json")
    Single<NearbySearchDto> getNearbySearch(
            @Query("key") String apiKey,
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("type") String type
    );
}
