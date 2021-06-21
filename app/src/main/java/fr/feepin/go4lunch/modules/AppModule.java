package fr.feepin.go4lunch.modules;

import android.content.Context;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
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
import fr.feepin.go4lunch.data.repos.data.DefaultMapsRepository;
import fr.feepin.go4lunch.data.repos.data.DefaultRestaurantRepository;
import fr.feepin.go4lunch.data.repos.data.RestaurantRepository;
import fr.feepin.go4lunch.data.local.DefaultLocationService;
import fr.feepin.go4lunch.data.local.LocationService;
import fr.feepin.go4lunch.data.repos.data.MapsRepository;
import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.domain.PlacePrediction;
import fr.feepin.go4lunch.data.models.dtos.NearbySearchResultDto;
import fr.feepin.go4lunch.data.models.mappers.AutocompletePredictionMapper;
import fr.feepin.go4lunch.data.models.mappers.Mapper;
import fr.feepin.go4lunch.data.models.mappers.NearbyPlaceMapper;
import fr.feepin.go4lunch.data.remote.apis.PlacesApi;
import fr.feepin.go4lunch.data.repos.data.DefaultUserRepository;
import fr.feepin.go4lunch.data.repos.data.UserRepository;
import fr.feepin.go4lunch.data.repos.shared.DefaultSharedNearPlacesRepository;
import fr.feepin.go4lunch.data.repos.shared.SharedNearPlacesRepository;
import fr.feepin.go4lunch.others.DefaultSchedulerProvider;
import fr.feepin.go4lunch.others.SchedulerProvider;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static fr.feepin.go4lunch.Constants.USER_PREFS_NAME;

@Module
@InstallIn(SingletonComponent.class)
abstract public class AppModule {

    @MapsRetrofit
    @Singleton
    @Provides
    public static Retrofit providesMapsRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(Constants.MAPS_BASE_URL)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
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

    @Singleton
    @Provides
    public static RxDataStore<Preferences> providesRxDataStore(@ApplicationContext Context context) {
        return new RxPreferenceDataStoreBuilder(context, USER_PREFS_NAME).build();
    }

    @Singleton
    @Provides
    public static FirebaseAuth providesFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Binds
    public abstract Mapper<AutocompletePrediction, PlacePrediction> bindsAutocompleteMapper(AutocompletePredictionMapper autocompletePredictionMapper);

    @Binds
    public abstract Mapper<NearbySearchResultDto, NearPlace> bindsNearbySearchDtoMapper(NearbyPlaceMapper nearbyPlaceMapper);

    @Binds
    public abstract SchedulerProvider bindsSchedulerProvider(DefaultSchedulerProvider defaultSchedulerProvider);

    @Binds
    public abstract LocationService bindsLocationService(DefaultLocationService defaultLocationService);

    @Binds
    public abstract RestaurantRepository bindsRestaurantRepository(DefaultRestaurantRepository defaultRestaurantRepository);

    @Binds
    public abstract UserRepository bindsUserRepository(DefaultUserRepository defaultUserRepository);

    @Binds
    public abstract MapsRepository bindsMapsRepository(DefaultMapsRepository defaultMapsRepository);

    @Binds
    public abstract SharedNearPlacesRepository bindsSharedNearPlacesRepository(DefaultSharedNearPlacesRepository defaultSharedNearPlacesRepository);

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface MapsRetrofit {
    }
}
