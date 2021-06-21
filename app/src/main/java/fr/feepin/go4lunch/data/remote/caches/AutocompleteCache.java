package fr.feepin.go4lunch.data.remote.caches;

import android.util.LruCache;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AutocompleteCache {

    private final LruCache<Pair<String, LatLng>, FindAutocompletePredictionsResponse> cache;

    @Inject
    public AutocompleteCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        cache = new LruCache<>(cacheSize);
    }

    public void cacheAutocompleteResponse(Pair<String, LatLng> query, FindAutocompletePredictionsResponse findAutocompletePredictionsResponse) {
        cache.put(query, findAutocompletePredictionsResponse);
    }

    @Nullable
    public FindAutocompletePredictionsResponse getAutocompleteResponse(Pair<String, LatLng> query) {
        return cache.get(query);
    }
}
