package fr.feepin.go4lunch.data.user.models;

public class VisitedRestaurant {

    private String restaurantId;

    private boolean liked;

    public VisitedRestaurant(String restaurantId, boolean liked) {
        this.restaurantId = restaurantId;
        this.liked = liked;
    }

    public VisitedRestaurant(){};

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
}
