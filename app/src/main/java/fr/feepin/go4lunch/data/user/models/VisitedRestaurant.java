package fr.feepin.go4lunch.data.user.models;

import java.util.Objects;

public class VisitedRestaurant {

    private String restaurantId;

    private boolean liked;

    public VisitedRestaurant(String restaurantId, boolean liked) {
        this.restaurantId = restaurantId;
        this.liked = liked;
    }

    public VisitedRestaurant() {
    }

    ;

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisitedRestaurant that = (VisitedRestaurant) o;
        return liked == that.liked &&
                Objects.equals(restaurantId, that.restaurantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, liked);
    }
}
