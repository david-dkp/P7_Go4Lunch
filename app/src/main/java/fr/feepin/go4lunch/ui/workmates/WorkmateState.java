package fr.feepin.go4lunch.ui.workmates;

import java.util.Objects;

public class WorkmateState {

    private String photoUrl;

    private String name;

    private String restaurantName;

    private String restaurantId;

    public WorkmateState(String restaurantId, String photoUrl, String name, String restaurantName) {
        this.restaurantId = restaurantId;
        this.photoUrl = photoUrl;
        this.name = name;
        this.restaurantName = restaurantName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkmateState that = (WorkmateState) o;
        return Objects.equals(photoUrl, that.photoUrl) &&
                Objects.equals(name, that.name) &&
                Objects.equals(restaurantName, that.restaurantName) &&
                Objects.equals(restaurantId, that.restaurantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(photoUrl, name, restaurantName, restaurantId);
    }
}
