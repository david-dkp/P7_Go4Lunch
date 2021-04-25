package fr.feepin.go4lunch.data.user.models;

public class VisitedRestaurant {

    private String placeId;

    private boolean liked;

    public VisitedRestaurant(String placeId, boolean liked) {
        this.placeId = placeId;
        this.liked = liked;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
