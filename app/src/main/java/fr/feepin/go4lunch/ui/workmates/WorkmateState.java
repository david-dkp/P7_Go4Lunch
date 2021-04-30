package fr.feepin.go4lunch.ui.workmates;

import java.util.Comparator;
import java.util.Objects;

public class WorkmateState {

    private String photoUrl;

    private String name;

    private String restaurantName;

    private String restaurantId;

    public WorkmateState(String restaurantId, String restaurantName, String photoUrl, String name) {
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

    @Override
    public String toString() {
        return "WorkmateState{" +
                "photoUrl='" + photoUrl + '\'' +
                ", name='" + name + '\'' +
                ", restaurantName='" + restaurantName + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                '}';
    }

    public static final Comparator<WorkmateState> RESTAURANT_NOT_CHOSEN_COMPARATOR = new Comparator<WorkmateState>() {
        @Override
        public int compare(WorkmateState a, WorkmateState b) {
            if (a.getRestaurantId().equals("") && !b.getRestaurantId().equals("")) {
                return 1;
            } else if (!a.getRestaurantId().equals("") && b.getRestaurantId().equals("")){
                return -1;
            } else {
                return 0;
            }
        }
    };
}
