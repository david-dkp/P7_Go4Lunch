package fr.feepin.go4lunch.data.remote.caches;

import android.util.LruCache;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.models.dtos.NearbySearchDto;

@Singleton
public class NearbySearchCache {

    private final LruCache<LatLng, NearbySearchDto> cache;

    @Inject
    public NearbySearchCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        cache = new LruCache<>(cacheSize);
    }

    public void cacheNearbySearch(LatLng location, NearbySearchDto nearbySearchDto) {
        cache.put(location, nearbySearchDto);
    }

    @Nullable
    public NearbySearchDto getNearbySearch(LatLng location) {
        return cache.get(location);
    }

}
