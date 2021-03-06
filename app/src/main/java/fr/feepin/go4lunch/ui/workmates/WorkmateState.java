package fr.feepin.go4lunch.ui.workmates;

import java.util.Comparator;
import java.util.Objects;

public class WorkmateState {

    private String photoUrl;

    private String name;

    private String restaurantName;

    private final String restaurantId;

    public WorkmateState(String restaurantId, String restaurantName, String photoUrl, String name) {
        this.restaurantId = restaurantId;
        this.photoUrl = photoUrl;
        this.name = name;
        this.restaurantName = restaurantName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkmateState that = (WorkmateState) o;
        return photoUrl.equals(that.photoUrl) &&
                name.equals(that.name) &&
                restaurantName.equals(that.restaurantName) &&
                restaurantId.equals(that.restaurantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(photoUrl, name, restaurantName, restaurantId);
    }

    @Override
    public String toString() {
        return "WorkmateState{" +
                "photoUrl='" + photoUrl + '\'' +
                ", name='" + name + '\'' +
                ", restaurantName='" + restaurantName + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                '}';
    }

}
