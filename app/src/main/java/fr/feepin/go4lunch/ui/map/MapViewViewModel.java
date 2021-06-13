package fr.feepin.go4lunch.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

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

    private final MutableLiveData<List<RestaurantState>> restaurantStates = new MutableLiveData<>();
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
        Disposable disposable = sharedNearPlacesRepository
                .getNearPlacesObservable()
                .distinct()
                .subscribeOn(schedulerProvider.io())
                .subscribe(this::updateRestaurantStates);

        compositeDisposable.add(disposable);
    }

    private void updateRestaurantStates(List<NearPlace> nearPlaces) {
        Disposable disposable = userRepository
                .getUsersInfo()
                .map(userInfos -> {
                    ArrayList<RestaurantState> restaurantStates = new ArrayList<>();
                    for (NearPlace nearPlace : nearPlaces) {

                        RestaurantState restaurantState = new RestaurantState(
                                nearPlace.getPlaceId(),
                                nearPlace.getLatLng(),
                                hasOneUserJoiningRestaurant(nearPlace.getPlaceId(), userInfos)
                        );

                        restaurantStates.add(restaurantState);
                    }

                    return (List<RestaurantState>) restaurantStates;
                })
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
        ArrayList<NearPlace.Photo> photos = new ArrayList<>();

        if (place.getPhotoMetadatas() != null) {
            for (PhotoMetadata photoMetadata : place.getPhotoMetadatas()) {
                NearPlace.Photo photo = new NearPlace.Photo(
                        photoMetadata.getWidth(),
                        photoMetadata.getHeight(),
                        photoMetadata.zza()
                );

                photos.add(photo);
            }
        }

        NearPlace nearPlace = new NearPlace(
                place.getId(),
                place.getLatLng(),
                place.getName(),
                photos,
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
