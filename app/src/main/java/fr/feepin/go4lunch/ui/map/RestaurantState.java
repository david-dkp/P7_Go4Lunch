package fr.feepin.go4lunch.ui.map;

import fr.feepin.go4lunch.data.maps.models.PlaceResponse;

public class RestaurantState {

    private PlaceResponse.LatLng position;

    private boolean joined;

    public RestaurantState(PlaceResponse.LatLng position, boolean joined) {
        this.position = position;
        this.joined = joined;
    }

    public PlaceResponse.LatLng getPosition() {
        return position;
    }

    public void setPosition(PlaceResponse.LatLng position) {
        this.position = position;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }
}
