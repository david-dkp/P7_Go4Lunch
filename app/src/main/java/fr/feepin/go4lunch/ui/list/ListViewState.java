package fr.feepin.go4lunch.ui.list;

import android.graphics.Bitmap;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

public class ListViewState {

    private List<ListItemState> listItemStates;

    private boolean isSortable;

    private boolean scrollToFirst;

    public ListViewState(List<ListItemState> listItemStates, boolean isSortable, boolean scrollToFirst) {
        this.listItemStates = listItemStates;
        this.isSortable = isSortable;
        this.scrollToFirst = scrollToFirst;
    }

    public List<ListItemState> getListItemStates() {
        return listItemStates;
    }

    public boolean isSortable() {
        return isSortable;
    }

    public boolean isScrollToFirst() {
        return scrollToFirst;
    }

    public static class ListItemState {

        private String restaurantName;

        private String restaurantAddress;

        private Boolean restaurantOpened;

        private int distance;

        private int workmatesJoining;

        private int rating;

        @Nullable
        private Bitmap photo;

        private String id;

        public ListItemState(String restaurantName, String restaurantAddress, Boolean restaurantOpened, int distance, int workmatesJoining, int rating, @Nullable Bitmap photo, String id) {
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

        public String getRestaurantAddress() {
            return restaurantAddress;
        }

        public Boolean isRestaurantOpened() {
            return restaurantOpened;
        }

        public int getDistance() {
            return distance;
        }

        public int getWorkmatesJoining() {
            return workmatesJoining;
        }

        public int getRating() {
            return rating;
        }

        public Bitmap getPhoto() {
            return photo;
        }

        public void setPhoto(@Nullable Bitmap photo) {
            this.photo = photo;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListItemState that = (ListItemState) o;
            return distance == that.distance &&
                    workmatesJoining == that.workmatesJoining &&
                    rating == that.rating &&
                    Objects.equals(restaurantName, that.restaurantName) &&
                    Objects.equals(restaurantAddress, that.restaurantAddress) &&
                    Objects.equals(restaurantOpened, that.restaurantOpened) &&
                    Objects.equals(photo, that.photo) &&
                    Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(restaurantName, restaurantAddress, restaurantOpened, distance, workmatesJoining, rating, photo, id);
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
}
