package fr.feepin.go4lunch.data.remote.caches;

import android.graphics.Bitmap;
import android.util.LruCache;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlacesPhotoCache {

    private final LruCache<String, Bitmap> lruCache;

    @Inject
    public PlacesPhotoCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        lruCache = new LruCache<>(cacheSize);
    }

    @Nullable
    public Bitmap getPlacePhoto(String placeId) {
        return lruCache.get(placeId);
    }

    public void storePhoto(String placeId, Bitmap bitmap) {
        lruCache.put(placeId, bitmap);
    }
}
