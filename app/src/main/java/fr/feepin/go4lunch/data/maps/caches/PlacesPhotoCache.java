package fr.feepin.go4lunch.data.maps.caches;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.google.common.cache.Cache;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlacesPhotoCache {

    private LruCache<String, Bitmap> lruCache;

    @Inject
    public PlacesPhotoCache(){
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        lruCache = new LruCache<>(cacheSize);
    }

    public Bitmap getPlacePhoto(String placeId) {
        return lruCache.get(placeId);
    }

    public void storePhoto(String placeId, Bitmap bitmap) {
        lruCache.put(placeId, bitmap);
    }
}
