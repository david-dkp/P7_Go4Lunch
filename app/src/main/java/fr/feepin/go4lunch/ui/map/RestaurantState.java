package fr.feepin.go4lunch.ui.map;

import com.google.android.gms.maps.model.LatLng;

public class RestaurantState {

    private String id;

    private LatLng position;

    private boolean joined;

    public RestaurantState(String id,LatLng position, boolean joined) {
        this.id = id;
        this.position = position;
        this.joined = joined;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RestaurantState{" +
                "id='" + id + '\'' +
                ", position=" + position +
                ", joined=" + joined +
                '}';
    }
}
