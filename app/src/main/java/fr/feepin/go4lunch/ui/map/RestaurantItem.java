package fr.feepin.go4lunch.ui.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class RestaurantItem implements ClusterItem {

    private final LatLng position;
    private final String restaurantId;
    private boolean joined;

    public RestaurantItem(LatLng position, String restaurantId, boolean joined) {
        this.position = position;
        this.restaurantId = restaurantId;
        this.joined = joined;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }
}
