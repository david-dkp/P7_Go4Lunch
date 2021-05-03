package fr.feepin.go4lunch.data.maps.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NearbySearchResponse {

    @Expose
    private String status;

    @SerializedName("results")
    @Expose
    private List<PlaceResponse> results;

    @SerializedName("error_message")
    @Expose
    private String errorMessage;

    public NearbySearchResponse(String status, List<PlaceResponse> results, String errorMessage) {
        this.status = status;
        this.results = results;
        this.errorMessage = errorMessage;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
