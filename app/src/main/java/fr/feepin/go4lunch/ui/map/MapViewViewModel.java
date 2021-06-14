package fr.feepin.go4lunch.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import fr.feepin.go4lunch.BuildConfig;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.data.repos.data.MapsRepository;
import fr.feepin.go4lunch.data.repos.data.UserRepository;
import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.domain.UserInfo;
import fr.feepin.go4lunch.data.repos.shared.SharedNearPlacesRepository;
import fr.feepin.go4lunch.others.SchedulerProvider;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class MapViewViewModel extends ViewModel {

    private final MapsRepository mapsRepository;
    private final UserRepository userRepository;
    private final SharedNearPlacesRepository sharedNearPlacesRepository;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private final SchedulerProvider schedulerProvider;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MediatorLiveData<List<RestaurantState>> restaurantStates = new MediatorLiveData<>();
    private final MutableLiveData<Resource<LatLng>> position = new MutableLiveData<>();

    @Inject
    public MapViewViewModel(
            MapsRepository mapsRepository,
            UserRepository userRepository,
            SharedNearPlacesRepository sharedNearPlacesRepository,
            SchedulerProvider schedulerProvider
    ) {
        this.mapsRepository = mapsRepository;
        this.userRepository = userRepository;
        this.sharedNearPlacesRepository = sharedNearPlacesRepository;
        this.schedulerProvider = schedulerProvider;
        wireRestaurantStates();
        askPosition();
    }

    private void wireRestaurantStates() {
        restaurantStates.addSource(sharedNearPlacesRepository.getNearPlaces(), this::updateRestaurantStates);
    }

    private void updateRestaurantStates(List<NearPlace> nearPlaces) {
        Disposable disposable = userRepository
                .getUsersInfo()
                .map(userInfos -> nearPlaces
                        .stream()
                        .map(nearPlace -> new RestaurantState(
                                nearPlace.getPlaceId(),
                                nearPlace.getLatLng(),
                                hasOneUserJoiningRestaurant(nearPlace.getPlaceId(), userInfos)
                        ))
                .collect(Collectors.toList()))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe((restaurantStates, throwable) -> {
                    if (throwable != null){
                        throwable.printStackTrace();
                    } else {
                        this.restaurantStates.setValue(restaurantStates);
                    }
                });

        compositeDisposable.add(disposable);
    }

    private boolean hasOneUserJoiningRestaurant(String restaurantId, List<UserInfo> userInfos) {
        for (UserInfo userInfo : userInfos) {
            if (userInfo.getId().equals(firebaseAuth.getCurrentUser().getUid())) {
                continue;
            }

            if (userInfo.getRestaurantChoiceId().equals(restaurantId)) return true;
        }

        return false;
    }

    public void askPosition() {
        Disposable disposable = mapsRepository
                .getLocation()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe((latLng, throwable) -> {
                    if (throwable != null) {
                        //TODO: handle error
                    } else {
                        position.setValue(new Resource<>(latLng, null));
                        askNearPlaces(latLng);
                    }
                });

        compositeDisposable.add(disposable);
    }

    private void askNearPlaces(LatLng position) {
        Disposable disposable = mapsRepository
                .getNearPlaces(BuildConfig.MAPS_API_KEY, position, Constants.NEARBY_SEARCH_RADIUS)
                .subscribeOn(schedulerProvider.io())
                .subscribe((nearPlaces, throwable) -> {
                    if (throwable != null) throwable.printStackTrace();

                    sharedNearPlacesRepository.addNearPlaces(nearPlaces);
                });

        compositeDisposable.add(disposable);
    }

    public void onPlaceReceive(Place place) {

        NearPlace nearPlace = new NearPlace(
                place.getId(),
                place.getLatLng(),
                place.getName(),
                place.getPhotoMetadatas(),
                place.getAddress(),
                place.isOpen()
        );

        sharedNearPlacesRepository.addNearPlace(nearPlace);
    }

    public LiveData<List<RestaurantState>> getRestaurantStates() {
        return restaurantStates;
    }

    public LiveData<Resource<LatLng>> getPosition() {
        return position;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
