package fr.feepin.go4lunch.ui.list;

import android.graphics.Bitmap;

import java.util.Comparator;
import java.util.Objects;

public class ListItemState {

    private String restaurantName;

    private String restaurantAddress;

    private boolean restaurantOpened;

    private int distance;

    private int workmatesJoining;

    private int rating;

    private Bitmap photo;

    private String id;

    public ListItemState(String restaurantName, String restaurantAddress, boolean restaurantOpened, int distance, int workmatesJoining, int rating, Bitmap photo, String id) {
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.restaurantOpened = restaurantOpened;
        this.distance = distance;
        this.workmatesJoining = workmatesJoining;
        this.rating = rating;
        this.photo = photo;
        this.id = id;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public boolean isRestaurantOpened() {
        return restaurantOpened;
    }

    public void setRestaurantOpened(boolean restaurantOpened) {
        this.restaurantOpened = restaurantOpened;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getWorkmatesJoining() {
        return workmatesJoining;
    }

    public void setWorkmatesJoining(int workmatesJoining) {
        this.workmatesJoining = workmatesJoining;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static Comparator<ListItemState> getDistanceComparator() {
        return DISTANCE_COMPARATOR;
    }

    public static Comparator<ListItemState> getRatingComparator() {
        return RATING_COMPARATOR;
    }

    public static Comparator<ListItemState> getWorkmatesComparator() {
        return WORKMATES_COMPARATOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListItemState that = (ListItemState) o;
        return restaurantOpened == that.restaurantOpened &&
                distance == that.distance &&
                workmatesJoining == that.workmatesJoining &&
                rating == that.rating &&
                Objects.equals(restaurantName, that.restaurantName) &&
                Objects.equals(restaurantAddress, that.restaurantAddress) &&
                Objects.equals(photo, that.photo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantName, restaurantAddress, restaurantOpened, distance, workmatesJoining, rating, photo);
    }

    public static final Comparator<ListItemState> DISTANCE_COMPARATOR = (a, b) -> {
        return Integer.compare(a.getDistance(), b.getDistance());
    };

    public static final Comparator<ListItemState> RATING_COMPARATOR = (a, b) -> {
        return Integer.compare(b.getRating(), a.getRating());
    };

    public static final Comparator<ListItemState> WORKMATES_COMPARATOR = (a, b) -> {
        return Integer.compare(b.getWorkmatesJoining(), a.getWorkmatesJoining());
    };
}
