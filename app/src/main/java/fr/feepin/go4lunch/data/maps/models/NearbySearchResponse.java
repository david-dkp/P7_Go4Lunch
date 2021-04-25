package fr.feepin.go4lunch.data.remote.models;

import com.google.gson.annotations.Expose;

import java.util.List;

public class NearbySearchResponse {

    @Expose
    private String status;

    @Expose
    private List<PlaceResponse> results;

}
