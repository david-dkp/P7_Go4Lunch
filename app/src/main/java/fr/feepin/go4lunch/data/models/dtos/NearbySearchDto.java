package fr.feepin.go4lunch.data.models.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NearbySearchDto {

    @Expose
    private String status;

    @SerializedName("results")
    @Expose
    private List<NearbySearchResultDto> results;

    @SerializedName("error_message")
    @Expose
    private String errorMessage;

    public NearbySearchDto(String status, List<NearbySearchResultDto> results, String errorMessage) {
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

    public List<NearbySearchResultDto> getResults() {
        return results;
    }

    public void setResults(List<NearbySearchResultDto> results) {
        this.results = results;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
