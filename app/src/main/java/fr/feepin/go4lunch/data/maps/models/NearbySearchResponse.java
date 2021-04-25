package fr.feepin.go4lunch.data.maps.models;

import com.google.android.libraries.places.api.Places;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NearbySearchResponse {

    @Expose
    private String status;

    @SerializedName("results")
    @Expose
    private List<PlaceResponse> results;

    public NearbySearchResponse(String status, List<PlaceResponse> results) {
        this.status = status;
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PlaceResponse> getResults() {
        return results;
    }

    public void setResults(List<PlaceResponse> results) {
        this.results = results;
    }
}
