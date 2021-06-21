package fr.feepin.go4lunch.data.models.domain;

public class PlacePrediction {

    private final String placeId;

    private final int getDistanceMeters;

    public PlacePrediction(String placeId, int getDistanceMeters) {
        this.placeId = placeId;
        this.getDistanceMeters = getDistanceMeters;
    }

    public String getPlaceId() {
        return placeId;
    }

    public int getGetDistanceMeters() {
        return getDistanceMeters;
    }
}
