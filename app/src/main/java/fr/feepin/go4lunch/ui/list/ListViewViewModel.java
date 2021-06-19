package fr.feepin.go4lunch.ui.list;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.maps.android.SphericalUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.domain.UserInfo;
import fr.feepin.go4lunch.data.repos.data.MapsRepository;
import fr.feepin.go4lunch.data.repos.data.RestaurantRepository;
import fr.feepin.go4lunch.data.repos.data.UserRepository;
import fr.feepin.go4lunch.data.repos.shared.SharedNearPlacesRepository;
import fr.feepin.go4lunch.others.SchedulerProvider;
import fr.feepin.go4lunch.utils.UserInfoUtils;
import fr.feepin.go4lunch.utils.VisitedRestaurantUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class ListViewViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MapsRepository mapsRepository;
    private final SharedNearPlacesRepository sharedNearPlacesRepository;

    private final SchedulerProvider schedulerProvider;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MediatorLiveData<Resource<ListViewState>> listViewState = new MediatorLiveData<>();

    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<ListItemStateSortMethod> sortMethod = new MutableLiveData<>(ListItemStateSortMethod.DISTANCE);

    private AutocompleteSessionToken autocompleteSessionToken;

    @Inject
    public ListViewViewModel(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            MapsRepository mapsRepository,
            SchedulerProvider schedulerProvider,
            SharedNearPlacesRepository sharedNearPlacesRepository
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.mapsRepository = mapsRepository;
        this.schedulerProvider = schedulerProvider;
        this.sharedNearPlacesRepository = sharedNearPlacesRepository;

        listViewState.setValue(new Resource.Success<>(
                new ListViewState(
                        Collections.emptyList(),
                        false,
                        false
                ), null
        ));

        wireListViewState();
    }

    private void wireListViewState() {
        listViewState.addSource(sharedNearPlacesRepository.getNearPlaces(), nearPlaces -> {
            updateListViewState(nearPlaces, query.getValue(), sortMethod.getValue());
        });

        listViewState.addSource(query, s -> {
            if (autocompleteSessionToken == null) {
                autocompleteSessionToken = AutocompleteSessionToken.newInstance();
            }

            updateListViewState(
                    sharedNearPlacesRepository.getNearPlaces().getValue(),
                    s,
                    sortMethod.getValue());
        });

        listViewState.addSource(sortMethod, sortMethod -> {
            ListViewState listViewState = this.listViewState.getValue().getData();
            this.listViewState.setValue(new Resource.Loading<>(listViewState, null));
            Collections.sort(listViewState.getListItemStates(), sortMethod.getComparator());
            this.listViewState.setValue(new Resource.Success<>(listViewState, null));
        });
    }

    private void updateListViewState(List<NearPlace> nearPlaces, String query, ListItemStateSortMethod sortMethod) {
        listViewState.setValue(
                new Resource.Loading<>(
                        new ListViewState(
                                this.listViewState.getValue().getData().getListItemStates(),
                                false,
                                false
                        ), null
                )
        );

        if (query == null || query.equals("")) {
            updateListViewStateFromNearPlaces(nearPlaces, sortMethod);
        } else {
            //updateLIstViewStateFromQuery(nearPlaces, query, sortMethod);
        }

    }

    private void updateListViewStateFromNearPlaces(List<NearPlace> nearPlaces, ListItemStateSortMethod sortMethod) {
        HashMap<ListViewState.ListItemState, PhotoMetadata> listItemWithPhotoMetadatas = new HashMap<>();

        Disposable disposable = Single
                .zip(
                        mapsRepository.getLocation(),
                        userRepository.getUsersInfo(),
                        Pair::new
                )
                .flatMap(pair -> getListItemStates(nearPlaces, pair.first, pair.second, listItemWithPhotoMetadatas))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .flatMapObservable(listItemStates -> {

                    Collections.sort(listItemStates, sortMethod.getComparator());

                    ListViewState listViewState = new ListViewState(
                            listItemStates,
                            true,
                            true
                    );

                    this.listViewState.setValue(new Resource.Success<>(listViewState, null));

                    return Observable.fromIterable(listItemStates);
                })
                .flatMap(listItemState -> getUpdatedListItemStatePhoto(listItemState, listItemWithPhotoMetadatas.get(listItemState)))
                .doOnError(Throwable::printStackTrace)
                .observeOn(schedulerProvider.ui())
                .subscribe(listItemState -> {
                    ListViewState listViewState = new ListViewState(
                            this.listViewState.getValue().getData().getListItemStates(),
                            true,
                            false
                    );

                    this.listViewState.setValue(new Resource.Success<>(listViewState, null));
                });

        compositeDisposable.add(disposable);

    }

    private Single<List<ListViewState.ListItemState>> getListItemStates(
            List<NearPlace> nearPlaces,
            LatLng position, List<UserInfo> userInfos,
            HashMap<ListViewState.ListItemState, PhotoMetadata> listItemWithPhotoMetadatas
    ) {


        return Observable
                .fromIterable(nearPlaces)
                .flatMap(nearPlace -> getListItemState(nearPlace, userInfos, position, listItemWithPhotoMetadatas))
                .toList();
    }

    private Observable<ListViewState.ListItemState> getListItemState(NearPlace nearPlace, List<UserInfo> userInfos, LatLng latLng, HashMap<ListViewState.ListItemState, PhotoMetadata> mapToSavePhotoMetadataTo) {
        return restaurantRepository.getVisitedRestaurantsByRestaurantId(nearPlace.getPlaceId())
                .map(visitedRestaurants -> {
                            ListViewState.ListItemState listViewState = new ListViewState.ListItemState(
                                    nearPlace.getName(),
                                    nearPlace.getAddress(),
                                    nearPlace.isOpen(),
                                    (int) SphericalUtil.computeDistanceBetween(latLng, nearPlace.getLatLng()),
                                    UserInfoUtils.calculateUsersJoiningByRestaurantId(userInfos, nearPlace.getPlaceId()),
                                    VisitedRestaurantUtils.calculateRating(visitedRestaurants),
                                    null,
                                    nearPlace.getPlaceId()
                            );

                            if (!nearPlace.getPhotoMetadatas().isEmpty()) {
                                mapToSavePhotoMetadataTo.put(listViewState, nearPlace.getPhotoMetadatas().get(0));
                            }

                            return listViewState;
                        }
                ).toObservable();
    }

    private Observable<ListViewState.ListItemState> getUpdatedListItemStatePhoto(ListViewState.ListItemState listItemState, PhotoMetadata photoMetadata) {

        if (photoMetadata == null) {
            return Observable.just(listItemState)
                    .subscribeOn(schedulerProvider.io());
        }

        return mapsRepository.getPlacePhoto(
                listItemState.getId(),
                photoMetadata
        )
                .map(bitmap -> {
                    listItemState.setPhoto(bitmap);

                    return listItemState;
                })
                .toObservable()
                .subscribeOn(schedulerProvider.io());
    }

    public AutocompleteSessionToken getSessionToken() {
        return AutocompleteSessionToken.newInstance();
    }

    public void onQuery(String query) {
        //Todo
    }

    public void askLocation() {
        //Todo
    }

    public void destroyAutocompleteSession() {
        //Todo
    }

    public LiveData<Resource<ListViewState>> getListViewState() {
        return listViewState;
    }

    public LiveData<ListItemStateSortMethod> getSortMethod() {
        return sortMethod;
    }

    public void setSortMethod(ListItemStateSortMethod sortMethod) {
        this.sortMethod.setValue(sortMethod);
    }
}
