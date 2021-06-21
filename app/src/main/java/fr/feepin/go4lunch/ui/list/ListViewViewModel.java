package fr.feepin.go4lunch.ui.list;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.maps.android.SphericalUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import io.reactivex.rxjava3.subjects.PublishSubject;

@HiltViewModel
public class ListViewViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MapsRepository mapsRepository;
    private final SharedNearPlacesRepository sharedNearPlacesRepository;

    private final SchedulerProvider schedulerProvider;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final PublishSubject<String> publishSubject = PublishSubject.create();

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
        setupPublishSubject();
    }

    private void wireListViewState() {
        listViewState.addSource(sharedNearPlacesRepository.getNearPlaces(), nearPlaces -> {
            publishSubject.onNext(query.getValue());
        });

        listViewState.addSource(query, s -> {

            if (autocompleteSessionToken == null) {
                autocompleteSessionToken = AutocompleteSessionToken.newInstance();
            }
            publishSubject.onNext(s);
        });

        listViewState.addSource(sortMethod, sortMethod -> {
            ListViewState listViewState = this.listViewState.getValue().getData();
            this.listViewState.setValue(new Resource.Loading<>(listViewState, null));
            Collections.sort(listViewState.getListItemStates(), sortMethod.getComparator());
            this.listViewState.setValue(new Resource.Success<>(listViewState, null));
        });
    }

    private void setupPublishSubject() {
        Disposable disposable = publishSubject
                .doOnNext(s -> {
                    this.listViewState.setValue(
                            new Resource.Loading<>(this.listViewState.getValue().getData(), null)
                    );
                })
                .debounce(400, TimeUnit.MILLISECONDS)
                .switchMap(s -> {
                    if (s.equals("")) {
                        return getListViewStateFromNearPlaces(this.sharedNearPlacesRepository.getNearPlaces().getValue(), this.getSortMethod().getValue());
                    } else {
                        return getListViewStateFromQuery(s, this.sharedNearPlacesRepository.getNearPlaces().getValue(), this.getSortMethod().getValue());
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .subscribe();

        compositeDisposable.add(disposable);

    }

    private Observable getListViewStateFromNearPlaces(List<NearPlace> nearPlaces, ListItemStateSortMethod sortMethod) {
        HashMap<ListViewState.ListItemState, PhotoMetadata> listItemWithPhotoMetadatas = new HashMap<>();

        return getPositionAndUserInfos()
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
                .doOnNext(listItemState -> {
                    ListViewState listViewState = new ListViewState(
                            this.listViewState.getValue().getData().getListItemStates(),
                            true,
                            false
                    );

                    this.listViewState.setValue(new Resource.Success<>(listViewState, null));
                });
    }

    private Observable getListViewStateFromQuery(String query, List<NearPlace> nearPlaces, ListItemStateSortMethod sortMethod) {
        HashMap<ListViewState.ListItemState, PhotoMetadata> listItemWithPhotoMetadatas = new HashMap<>();

        return getPositionAndUserInfos()
                .flatMapObservable(positionAndUserInfos ->
                        mapsRepository
                                .getPlacePredictionsFromQuery(autocompleteSessionToken, query, positionAndUserInfos.first)
                                .flatMapObservable(Observable::fromIterable)
                                .flatMap(placePrediction -> {
                                    NearPlace nearPlace = findNearPlaceById(placePrediction.getPlaceId(), nearPlaces);
                                    if (nearPlace != null) {
                                        return Observable.just(nearPlace);
                                    }

                                    return mapsRepository.getPlace(
                                            placePrediction.getPlaceId(),
                                            Arrays.asList(
                                                    Place.Field.ID,
                                                    Place.Field.LAT_LNG,
                                                    Place.Field.NAME,
                                                    Place.Field.ADDRESS,
                                                    Place.Field.OPENING_HOURS,
                                                    Place.Field.PHOTO_METADATAS
                                            ),
                                            autocompleteSessionToken
                                    )
                                            .map(place -> new NearPlace(
                                                    place.getId(),
                                                    place.getLatLng(),
                                                    place.getName(),
                                                    place.getPhotoMetadatas(),
                                                    place.getAddress(),
                                                    place.isOpen()
                                            ))
                                            .subscribeOn(schedulerProvider.io())
                                            .toObservable();
                                })
                                .toList()
                                .flatMap(nearPlaceList -> getListItemStates(nearPlaceList, positionAndUserInfos.first, positionAndUserInfos.second, listItemWithPhotoMetadatas))
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
                                .doOnNext(listItemState -> {
                                    ListViewState listViewState = new ListViewState(
                                            this.listViewState.getValue().getData().getListItemStates(),
                                            true,
                                            false
                                    );

                                    this.listViewState.setValue(new Resource.Success<>(listViewState, null));
                                }));
    }

    private NearPlace findNearPlaceById(String id, List<NearPlace> nearPlaces) {
        for (NearPlace nearPlace : nearPlaces) {
            if (nearPlace.getPlaceId() == id) {
                return nearPlace;
            }
        }

        return null;
    }

    private Single<Pair<LatLng, List<UserInfo>>> getPositionAndUserInfos() {
        return Single
                .zip(
                        mapsRepository.getLocation(),
                        userRepository.getUsersInfo(),
                        Pair::new
                );
    }

    private Single<List<ListViewState.ListItemState>> getListItemStates(
            List<NearPlace> nearPlaces,
            LatLng position, List<UserInfo> userInfos,
            HashMap<ListViewState.ListItemState, PhotoMetadata> listItemWithPhotoMetadatas
    ) {


        return Observable
                .fromIterable(nearPlaces)
                .flatMap(nearPlace -> getListItemState(nearPlace, userInfos, position, listItemWithPhotoMetadatas)
                        .subscribeOn(schedulerProvider.io()))
                .toList();
    }

    private Observable<ListViewState.ListItemState> getListItemState(
            NearPlace nearPlace,
            List<UserInfo> userInfos,
            LatLng latLng,
            HashMap<ListViewState.ListItemState, PhotoMetadata> mapToSavePhotoMetadataTo
    ) {
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

                            if (nearPlace.getPhotoMetadatas() != null) {
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

    public void onRefresh() {
        publishSubject.onNext(this.query.getValue());
    }

    public void onQuery(String query) {
        this.query.setValue(query);
    }

    public void destroyAutocompleteSession() {
        autocompleteSessionToken = null;
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

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
