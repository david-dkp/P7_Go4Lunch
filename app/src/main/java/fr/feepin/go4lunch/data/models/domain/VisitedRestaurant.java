package fr.feepin.go4lunch.data.models.domain;

import com.google.firebase.firestore.DocumentId;

import java.util.Objects;

public class VisitedRestaurant {

    @DocumentId
    private String id;

    private String restaurantId;

    private boolean liked;

    public VisitedRestaurant(String restaurantId, boolean liked) {
        this.restaurantId = restaurantId;
        this.liked = liked;
    }

    public VisitedRestaurant() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public boolean isLiked() {
        return liked;
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
