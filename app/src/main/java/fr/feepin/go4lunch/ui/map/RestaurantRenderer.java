package fr.feepin.go4lunch.ui.map;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class RestaurantRenderer extends DefaultClusterRenderer<RestaurantItem> {

    private Bitmap notJoinedBitmap, joinedBitmap;

    public RestaurantRenderer(Context context, GoogleMap map, ClusterManager<RestaurantItem> clusterManager, Bitmap notJoinedBitmap, Bitmap joinedBitmap) {
        super(context, map, clusterManager);
        this.notJoinedBitmap = notJoinedBitmap;
        this.joinedBitmap = joinedBitmap;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull RestaurantItem item, @NonNull MarkerOptions markerOptions) {
        markerOptions.icon(getIcon(item));
    }

    @Override
    protected void onClusterItemUpdated(@NonNull RestaurantItem item, @NonNull Marker marker) {
        marker.setIcon(getIcon(item));
    }

    private BitmapDescriptor getIcon(RestaurantItem restaurantItem) {
        return BitmapDescriptorFactory.fromBitmap(restaurantItem.isJoined() ? joinedBitmap : notJoinedBitmap);
    }
}
