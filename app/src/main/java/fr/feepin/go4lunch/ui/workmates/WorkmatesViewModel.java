package fr.feepin.go4lunch.ui.workmates;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.domain.UserInfo;
import fr.feepin.go4lunch.data.repos.data.MapsRepository;
import fr.feepin.go4lunch.data.repos.data.UserRepository;
import fr.feepin.go4lunch.data.repos.shared.SharedNearPlacesRepository;
import fr.feepin.go4lunch.others.SchedulerProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class WorkmatesViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MapsRepository mapsRepository;
    private final SharedNearPlacesRepository sharedNearPlacesRepository;

    private final SchedulerProvider schedulerProvider;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<Resource<List<WorkmateState>>> workmateStates = new MutableLiveData<>();

    @Inject
    public WorkmatesViewModel(UserRepository userRepository, MapsRepository mapsRepository, SharedNearPlacesRepository sharedNearPlacesRepository, SchedulerProvider schedulerProvider) {
        this.userRepository = userRepository;
        this.mapsRepository = mapsRepository;
        this.sharedNearPlacesRepository = sharedNearPlacesRepository;
        this.schedulerProvider = schedulerProvider;
        askUserInfos();
    }

    public void askUserInfos() {
        workmateStates.setValue(new Resource.Loading<>(null, null));

        Disposable disposable = userRepository
                .getUsersInfo()
                .toObservable()
                .flatMapIterable(userInfos -> {
                    Collections.sort(userInfos, UserInfo.RESTAURANT_CHOSEN_FIRST_COMPARATOR);
                    return userInfos;
                })
                .concatMapEager(userInfo -> getRestaurantNameObservable(userInfo)
                        .map(name -> new WorkmateState(
                                userInfo.getRestaurantChoiceId(),
                                name,
                                userInfo.getPhotoUrl(),
                                userInfo.getName()
                        )))
                .toList()
                .map(workmateStates -> new Resource.Success<>(workmateStates, null))
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(this.workmateStates::setValue);

        compositeDisposable.add(disposable);
    }

    private Observable<String> getRestaurantNameObservable(UserInfo userInfo) {

        if (userInfo.getRestaurantChoiceId().equals("")) {
            return Observable.just("");
        }

        for (NearPlace nearPlace : sharedNearPlacesRepository.getNearPlaces().getValue()) {
            if (nearPlace.getPlaceId().equals(userInfo.getRestaurantChoiceId())) {
                return Observable.just(nearPlace.getName());
            }
        }

        return mapsRepository
                .getPlace(userInfo.getRestaurantChoiceId(), new ArrayList<>(Collections.singleton(Place.Field.NAME)), null)
                .map(Place::getName)
                .toObservable()
                ;
    }

    public LiveData<Resource<List<WorkmateState>>> getWorkmateStates() {
        return workmateStates;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
