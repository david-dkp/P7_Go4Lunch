package fr.feepin.go4lunch.data.remote;


import fr.feepin.go4lunch.data.remote.models.NearbySearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesApi {

    @GET("maps/api/place/nearbysearch/json")
    public Call<NearbySearchResponse> getNearbySearch(
            @Query("key") String apiKey,
            @Query("location") String location,
            @Query("radius") String radius
    );
}
