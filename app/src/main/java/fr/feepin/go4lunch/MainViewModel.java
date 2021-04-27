package fr.feepin.go4lunch;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.location.LocationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.data.maps.MapsRepository;
import fr.feepin.go4lunch.data.maps.models.NearbySearchResponse;
import fr.feepin.go4lunch.data.maps.models.PlaceResponse;
import fr.feepin.go4lunch.data.user.UserRepository;
import fr.feepin.go4lunch.data.user.models.UserInfo;
import fr.feepin.go4lunch.ui.map.RestaurantState;
import fr.feepin.go4lunch.utils.PermissionUtils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private Context context;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private LocationManager locationManager;

    private FirebaseAuth firebaseAuth;
    private MapsRepository mapsRepository;
    private UserRepository userRepository;

    private MutableLiveData<FirebaseUser> currentUser;
    private MutableLiveData<Resource<LatLng>> position = new MutableLiveData<>();

    private MediatorLiveData<Resource<List<RestaurantState>>> restaurantsState = new MediatorLiveData();

    private MutableLiveData<List<PlaceResponse>> placesResponse = new MutableLiveData<>(Collections.emptyList());
    private MutableLiveData<List<UserInfo>> usersInfo = new MutableLiveData<>(Collections.emptyList());

    @Inject
    public MainViewModel(@ApplicationContext Context context, FirebaseAuth firebaseAuth, MapsRepository mapsRepository, UserRepository userRepository) {
        this.context = context;
        this.firebaseAuth = firebaseAuth;
        this.mapsRepository = mapsRepository;
        this.userRepository = userRepository;

        setupRestaurantsState();
        setupLocationManager();
        setupFirebaseUser();
    }

    private void setupRestaurantsState() {
        restaurantsState.setValue(new Resource.Success<>(Collections.emptyList(), null));

        restaurantsState.addSource(placesResponse, places -> updateRestaurantsState(places, usersInfo.getValue()));

        restaurantsState.addSource(usersInfo, infos -> {
            updateRestaurantsState(placesResponse.getValue(), infos);
        });

        userRepository.getUsersInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<UserInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull List<UserInfo> infos) {
                        usersInfo.setValue(infos);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("debug", "Error: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void setupLocationManager() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private void setupFirebaseUser() {
        currentUser = new MutableLiveData<>(firebaseAuth.getCurrentUser());
        firebaseAuth.addAuthStateListener(newAuth -> {
            currentUser.postValue(newAuth.getCurrentUser());
        });
    }

    private void updateRestaurantsState(List<PlaceResponse> places, List<UserInfo> usersInfo) {
        ArrayList<RestaurantState> restaurantsState = new ArrayList<>();

        for (PlaceResponse placeResponse : places) {
            RestaurantState restaurantState = new RestaurantState(
                    placeResponse.getPlaceId(),
                    placeResponse.getGeometry().getLocation().toMapsLatLng(),
                    restaurantJoined(placeResponse.getPlaceId(), usersInfo)
            );
            restaurantsState.add(restaurantState);
        }

        this.restaurantsState.setValue(new Resource.Success<>(restaurantsState, null));
    }

    private boolean restaurantJoined(String id, List<UserInfo> usersInfo) {
        for (UserInfo userInfo : usersInfo) {
            if (userInfo.getRestaurantChoiceId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public void askLocation() {

        if (!PermissionUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            position.setValue(new Resource.Error<>(null, Constants.NO_LOCATION_PERMISSION_MESSAGE));
            return;
        }

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {

            Location latestKnown = mapsRepository.getLastKnownLocation();

            if (latestKnown != null) {
                LatLng latLng = new LatLng(latestKnown.getLatitude(), latestKnown.getLongitude());
                position.setValue(new Resource.Error<>(latLng, Constants.LOCATION_DISABLED_MESSAGE));
                getNearbyPlaces(latLng);
            } else {
                mapsRepository.getLatestPositionFromPrefs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<LatLng>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                compositeDisposable.add(d);
                            }

                            @Override
                            public void onSuccess(@NonNull LatLng latLng) {
                                position.setValue(new Resource.Error<>(latLng, Constants.LOCATION_DISABLED_MESSAGE));
                                getNearbyPlaces(latLng);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                position.setValue(new Resource.Error<>(null, Constants.LOCATION_DISABLED_MESSAGE));
                            }
                        });
            }
        } else {
            mapsRepository.getCurrentLocation()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Location>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(@NonNull Location location) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            position.setValue(new Resource.Success<>(latLng, null));
                            getNearbyPlaces(latLng);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            position.setValue(new Resource.Success<>(null, e.getMessage()));
                        }
                    });
        }
    }

    public void getNearbyPlaces(LatLng latLng) {
        mapsRepository.getNearbySearch(BuildConfig.MAPS_API_KEY, latLng.latitude + "," + latLng.longitude, Constants.NEARBY_SEARCH_RADIUS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<NearbySearchResponse>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull NearbySearchResponse nearbySearchResponse) {
                        placesResponse.setValue(nearbySearchResponse.getResults());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("debug", "nearby error: "+e.getMessage());
                    }
                });
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Resource<LatLng>> getPosition() {
        return position;
    }

    public LiveData<Resource<List<RestaurantState>>> getRestaurantsState() {
        return restaurantsState;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
