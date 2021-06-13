package fr.feepin.go4lunch.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.feepin.go4lunch.BuildConfig;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.data.MapsRepository;
import fr.feepin.go4lunch.data.UserRepository;
import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.domain.UserInfo;
import fr.feepin.go4lunch.others.SchedulerProvider;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class MapViewViewModel extends ViewModel {

    private final MapsRepository mapsRepository;
    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private final SchedulerProvider schedulerProvider;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<List<RestaurantState>> restaurantStates = new MutableLiveData<>();
    private final MutableLiveData<LatLng> position = new MutableLiveData<>();

    @Inject
    public MapViewViewModel(
            MapsRepository mapsRepository,
            UserRepository userRepository,
            SchedulerProvider schedulerProvider
    ) {
        this.mapsRepository = mapsRepository;
        this.userRepository = userRepository;
        this.schedulerProvider = schedulerProvider;
        wireRestaurantStates();
        askPosition();
    }

    private void wireRestaurantStates() {
        Disposable disposable = mapsRepository
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
                        position.setValue(latLng);
                        mapsRepository.refreshNearPlaces(BuildConfig.MAPS_API_KEY, latLng, Constants.NEARBY_SEARCH_RADIUS);
                    }
                });

        compositeDisposable.add(disposable);
    }

    public LiveData<List<RestaurantState>> getRestaurantStates() {
        return restaurantStates;
    }

    public LiveData<LatLng> getPosition() {
        return position;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
