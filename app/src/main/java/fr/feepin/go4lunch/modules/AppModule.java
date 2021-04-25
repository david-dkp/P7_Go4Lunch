package fr.feepin.go4lunch.modules;

import android.content.Context;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.GsonBuilder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.data.maps.FusedLocationService;
import fr.feepin.go4lunch.data.maps.LocationService;
import fr.feepin.go4lunch.data.maps.PlacesApi;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class) abstract public class AppModule {

    @MapsRetrofit
    @Singleton
    @Provides
    public static Retrofit providesMapsRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(Constants.MAPS_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(
                        GsonConverterFactory.create(
                                new GsonBuilder()
                                        .excludeFieldsWithoutExposeAnnotation()
                                        .create()
                        )
                ).build();
    }

    @Singleton
    @Provides
    public static PlacesApi providesPlacesApi(
            @MapsRetrofit Retrofit mapsRetrofitInstance
    ) {
        return mapsRetrofitInstance.create(PlacesApi.class);
    }

    @Singleton
    @Provides
    public static PlacesClient providesPlacesClient(@ApplicationContext Context context) {
        return Places.createClient(context);
    }

    @Binds
    public abstract LocationService bindsFusedLocationService(FusedLocationService fusedLocationService);

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface MapsRetrofit {}
}
