package fr.feepin.go4lunch;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;

import androidx.core.location.LocationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.SphericalUtil;

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
import fr.feepin.go4lunch.others.SchedulerProvider;
import fr.feepin.go4lunch.ui.list.ListItemState;
import fr.feepin.go4lunch.ui.list.ListItemStateSortMethod;
import fr.feepin.go4lunch.ui.list.ListViewState;
import fr.feepin.go4lunch.ui.map.RestaurantState;
import fr.feepin.go4lunch.ui.workmates.WorkmateState;
import fr.feepin.go4lunch.utils.LatLngUtils;
import fr.feepin.go4lunch.utils.PermissionUtils;
import fr.feepin.go4lunch.utils.UserInfoUtils;
import fr.feepin.go4lunch.utils.VisitedRestaurantUtils;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final Context context;
    private final Handler handler = new Handler();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AutocompleteSessionToken sessionToken;

    private LocationManager locationManager;

    private final FirebaseAuth firebaseAuth;
    private final MapsRepository mapsRepository;
    private final UserRepository userRepository;
    private final SchedulerProvider schedulerProvider;

    private MutableLiveData<FirebaseUser> currentUser;

    //States
    private final MutableLiveData<Resource<LatLng>> position = new MutableLiveData<>();
    private final MutableLiveData<UserInfo> currentUserInfo = new MutableLiveData<>();
    private final MediatorLiveData<Resource<List<RestaurantState>>> restaurantStates = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<ListViewState>> listViewState = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<WorkmateState>>> workmateStates = new MediatorLiveData<>();

    //Datas
    private final MutableLiveData<FindAutocompletePredictionsResponse> autocompletePredictions = new MutableLiveData<>();
    private final MutableLiveData<List<PlaceResponse>> placesResponse = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<UserInfo>> userInfos = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<ListItemStateSortMethod> listItemStateSortMethod = new MutableLiveData<>(ListItemStateSortMethod.DISTANCE);

    private Disposable runningListViewObservable;
    private Disposable runningPredictionsQueryObservable;
    private Disposable runningMyLocationObservable;

    @Inject
    public MainViewModel(@ApplicationContext Context context,
                         FirebaseAuth firebaseAuth,
                         MapsRepository mapsRepository,
                         UserRepository userRepository,
                         SchedulerProvider schedulerProvider
    ) {
        this.context = context;
        this.firebaseAuth = firebaseAuth;
        this.mapsRepository = mapsRepository;
        this.userRepository = userRepository;
        this.schedulerProvider = schedulerProvider;
        setup();
    }

    public void setup() {
        setupRestaurantStates();
        setupListViewState();
        setupWorkmateStates();
        setupLocationManager();
        setupFirebaseUser();

        listenToUserInfos();
        askLocation();
    }

    private void setupListViewState() {

        listViewState.setValue(new Resource.Success<>(new ListViewState(Collections.emptyList(), true, false), null));

        listViewState.addSource(placesResponse, placeResponses -> {
            updateListViewState(placeResponses, autocompletePredictions.getValue(), listItemStateSortMethod.getValue());
        });

        listViewState.addSource(autocompletePredictions, autocompletePredictions -> {
            updateListViewState(placesResponse.getValue(), autocompletePredictions, listItemStateSortMethod.getValue());
        });

        listViewState.addSource(listItemStateSortMethod, listItemStateSortMethod -> {
            Collections.sort(listViewState.getValue().getData().getListItemStates(), listItemStateSortMethod.getComparator());
            listViewState.getValue().getData().setScrollToFirst(true);
            listViewState.setValue(listViewState.getValue());
        });
    }

    private void setupRestaurantStates() {
        restaurantStates.setValue(new Resource.Success<>(Collections.emptyList(), null));

        restaurantStates.addSource(placesResponse, places -> updateRestaurantsState(places, userInfos.getValue()));

        restaurantStates.addSource(userInfos, infos -> {
            updateRestaurantsState(placesResponse.getValue(), infos);
        });
    }

    private void setupWorkmateStates() {
        workmateStates.addSource(userInfos, this::updateWorkmateStates);
    }

    private void setupLocationManager() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private void setupFirebaseUser() {
        currentUser = new MutableLiveData<>(firebaseAuth.getCurrentUser());
        firebaseAuth.addAuthStateListener(newAuth -> {
            currentUser.postValue(newAuth.getCurrentUser());
        });

        userRepository.getCurrentUserInfoObservable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Observer<UserInfo>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull UserInfo userInfo) {
                        currentUserInfo.setValue(userInfo);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void listenToUserInfos() {
        userRepository.getUsersInfoObservable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Observer<List<UserInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull List<UserInfo> infos) {
                        userInfos.setValue(infos);
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

    private void updateListViewState(List<PlaceResponse> placeResponses, FindAutocompletePredictionsResponse autocompletePredictionsResponse, ListItemStateSortMethod listItemStateSortMethod) {
        listViewState.setValue(new Resource.Loading(new ListViewState(listViewState.getValue().getData().getListItemStates(), false, false), null));

        if (autocompletePredictionsResponse == null) {
            //If we don't use autocomplete, we use NearbySearch data "placeResponses"
            runningListViewObservable = Single.just(userInfos.getValue())
                    //We load all the text required first
                    .flatMapObservable(userInfos -> Observable.fromIterable(placeResponses)
                            .flatMap(placeResponse -> userRepository.getVisitedRestaurants(placeResponse.getPlaceId())
                                    .map(visitedRestaurants -> {
                                        int rating = VisitedRestaurantUtils.calculateRating(visitedRestaurants);
                                        int usersJoining = UserInfoUtils.calculateUsersJoiningByRestaurantId(userInfos, placeResponse.getPlaceId());
                                        int distance = (int) SphericalUtil.computeDistanceBetween(
                                                placeResponse.getGeometry().getLocation().toMapsLatLng(), getPosition().getValue().getData()
                                        );

                                        PlaceResponse.OpeningHours openingHours = placeResponse.getOpeningHours();

                                        List<PlaceResponse.Photo> photos = placeResponse.getPhotos();

                                        //Return the ListItemState and the photos to keep them on track
                                        return Pair.create(new ListItemState(
                                                placeResponse.getName(),
                                                placeResponse.getVicinity(),
                                                openingHours != null ? openingHours.isOpenNow() : null,
                                                distance,
                                                usersJoining,
                                                rating,
                                                null,
                                                placeResponse.getPlaceId()
                                        ), photos);
                                    }).toObservable()))
                    .toList() //Waiting for all data to be loaded
                    //Post the List<ListItemState> in the adapter then we load photos.
                    .flatMapObservable(pairs -> {

                        ArrayList<ListItemState> itemStates = new ArrayList<>();

                        for (Pair<ListItemState, List<PlaceResponse.Photo>> pair : pairs) {
                            itemStates.add(pair.first);
                        }

                        Collections.sort(itemStates, listItemStateSortMethod.getComparator());
                        listViewState.postValue(new Resource.Success<>(new ListViewState(itemStates, true, true), null));

                        return Observable
                                .fromIterable(pairs)
                                .filter(listItemStateListPair -> listItemStateListPair.second != null)
                                .flatMap(listItemStateListPair -> {
                                    PlaceResponse.Photo photo = listItemStateListPair.second.get(0);
                                    return mapsRepository.getRestaurantPhoto(
                                            listItemStateListPair.first.getId(),
                                            PhotoMetadata.builder(photo.getReference())
                                                    .setWidth(photo.getWidth())
                                                    .setHeight(photo.getHeight())
                                                    .build()
                                    )
                                            .doAfterSuccess(photoResponse -> {
                                                listItemStateListPair.first.setPhoto(photoResponse.getBitmap());
                                                listViewState.getValue().getData().setScrollToFirst(false);
                                                listViewState.postValue(listViewState.getValue());
                                            })
                                            .toObservable();
                                });
                    })
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe();
        } else {
            //Else if we use autocomplete, request the Places because AutocompletePrediction has not all the fields we want
            runningListViewObservable = Single.just(userInfos.getValue())
                    .flatMapObservable(userInfos -> Observable
                            .fromIterable(autocompletePredictionsResponse.getAutocompletePredictions())
                            //Getting all required fields for Place
                            .flatMap(autocompletePrediction -> mapsRepository.getRestaurantDetails(
                                    autocompletePrediction.getPlaceId(),
                                    Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.UTC_OFFSET, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS),
                                    sessionToken
                            )
                                    //Getting VisitedRestaurants to calculate rating
                                    .flatMapObservable(place -> userRepository.getVisitedRestaurants(place.getId())
                                            .map(visitedRestaurants -> Pair.create(new ListItemState(
                                                    place.getName(),
                                                    place.getAddress(),
                                                    place.isOpen(),
                                                    (int) SphericalUtil.computeDistanceBetween(position.getValue().getData(), place.getLatLng()),
                                                    UserInfoUtils.calculateUsersJoiningByRestaurantId(userInfos, place.getId()),
                                                    VisitedRestaurantUtils.calculateRating(visitedRestaurants),
                                                    null,
                                                    place.getId()
                                            ), place.getPhotoMetadatas())).toObservable()
                                    )))
                    .toList() //Waiting for the data to be loaded
                    //Post the List<ListItemState> in the adapter then we load photos.
                    .flatMapObservable(pairs -> {

                        ArrayList<ListItemState> itemStates = new ArrayList<>();

                        for (Pair<ListItemState, List<PhotoMetadata>> pair : pairs) {
                            itemStates.add(pair.first);
                        }

                        Collections.sort(itemStates, listItemStateSortMethod.getComparator());
                        listViewState.postValue(new Resource.Success<>(new ListViewState(itemStates, false, true), null));

                        return Observable
                                .fromIterable(pairs)
                                .filter(listItemStateListPair -> listItemStateListPair.second != null)
                                .flatMap(listItemStateListPair -> {
                                    PhotoMetadata photo = listItemStateListPair.second.get(0);
                                    return mapsRepository.getRestaurantPhoto(
                                            listItemStateListPair.first.getId(),
                                            photo
                                    )
                                            .doAfterSuccess(photoResponse -> {
                                                listItemStateListPair.first.setPhoto(photoResponse.getBitmap());
                                                listViewState.getValue().getData().setScrollToFirst(false);
                                                listViewState.postValue(listViewState.getValue());
                                            })
                                            .toObservable();
                                });
                    })
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe();
        }
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

        this.restaurantStates.setValue(new Resource.Success<>(restaurantsState, null));
    }

    private void updateWorkmateStates(List<UserInfo> userInfos) {

        Observable.fromIterable(userInfos)
                .flatMap(userInfo -> {

                    if (userInfo.getRestaurantChoiceId().equals("")) {
                        return Observable.just(new WorkmateState("", "", userInfo.getPhotoUrl(), userInfo.getName()));
                    } else {
                        PlaceResponse placeResponse = getPlaceFromId(userInfo.getRestaurantChoiceId());
                        if (placeResponse != null) {
                            return Observable.just(new WorkmateState(placeResponse.getPlaceId(), placeResponse.getName(), userInfo.getPhotoUrl(), userInfo.getName()));
                        } else {
                            return mapsRepository.getRestaurantDetails(userInfo.getRestaurantChoiceId(), Collections.singletonList(Place.Field.NAME), null)
                                    .map(place -> {
                                        return new WorkmateState(userInfo.getRestaurantChoiceId(), place.getName(), userInfo.getPhotoUrl(), userInfo.getName());
                                    })
                                    .toObservable();
                        }
                    }
                })
                .toList()
                .map(workmateStates -> {
                    if (!workmateStates.isEmpty()) {
                        Collections.sort(workmateStates, WorkmateState.RESTAURANT_NOT_CHOSEN_COMPARATOR);
                    }
                    return workmateStates;
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe((workmateStates, throwable) -> {
                    if (throwable != null) {
                        Log.d("debug", throwable.getMessage());
                    }
                    MainViewModel.this.workmateStates.setValue(new Resource.Success<>(workmateStates, null));
                });
    }

    private void sortWorkmateStates(List<WorkmateState> workmateStates) {
        Collections.sort(workmateStates, WorkmateState.RESTAURANT_NOT_CHOSEN_COMPARATOR);
    }

    private PlaceResponse getPlaceFromId(String id) {
        for (PlaceResponse placeResponse : placesResponse.getValue()) {
            if (placeResponse.getPlaceId().equals(id)) {
                return placeResponse;
            }
        }

        return null;
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

        if (runningMyLocationObservable != null) runningMyLocationObservable.dispose();

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            Location latestKnown = mapsRepository.getLastKnownLocation();

            if (latestKnown != null) {
                LatLng latLng = new LatLng(latestKnown.getLatitude(), latestKnown.getLongitude());
                position.setValue(new Resource.Error<>(latLng, Constants.LOCATION_DISABLED_MESSAGE));
                getNearbyPlaces(latLng);
            } else {
                mapsRepository.getLatestPositionFromPrefs()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(new SingleObserver<LatLng>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                runningMyLocationObservable = d;
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
            position.setValue(new Resource.Loading<>(null, null));
            mapsRepository.getCurrentLocation()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(new SingleObserver<Location>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            runningMyLocationObservable = d;
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

    private void getNearbyPlaces(LatLng latLng) {

        mapsRepository.getNearbySearch(BuildConfig.MAPS_API_KEY, latLng.latitude + "," + latLng.longitude, Constants.NEARBY_SEARCH_RADIUS)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
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
                        Log.d("debug", "nearby error: " + e.getMessage());
                    }
                });
    }

    private void getPredictionsForQuery(String query) {
        mapsRepository.getRestaurantsFromQuery(
                sessionToken,
                query,
                position.getValue().getData(),
                RectangularBounds.newInstance(LatLngUtils.toBounds(position.getValue().getData(), Constants.PREDICTION_SEARCH_RADIUS))
        )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new SingleObserver<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        runningPredictionsQueryObservable = d;
                    }

                    @Override
                    public void onSuccess(@NonNull FindAutocompletePredictionsResponse findAutocompletePredictionsResponse) {
                        autocompletePredictions.setValue(findAutocompletePredictionsResponse);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("debug", e.getMessage());
                    }
                });
    }

    public void addRestaurant(Place place) {
        PlaceResponse placeResponse = new PlaceResponse(
                place.getId(),
                new PlaceResponse.Geometry(PlaceResponse.LatLng.fromMapsLatLng(place.getLatLng())),
                null,
                null,
                null,
                null
        );

        ArrayList<PlaceResponse> newPlaces = new ArrayList<>(placesResponse.getValue());
        newPlaces.add(placeResponse);

        placesResponse.setValue(newPlaces);
    }

    public AutocompleteSessionToken getSessionToken() {
        return sessionToken;
    }

    public void destroyAutocompleteSession() {
        sessionToken = null;
    }

    public void autoCompleteQuery(String query) {
        String cleanQuery = query.trim();

        if (sessionToken == null) {
            sessionToken = AutocompleteSessionToken.newInstance();
        }

        if (runningListViewObservable != null) {
            runningListViewObservable.dispose();
        }

        if (runningPredictionsQueryObservable != null) {
            runningPredictionsQueryObservable.dispose();
        }

        if (cleanQuery.equals("")) {
            handler.removeCallbacksAndMessages(null);
            if (autocompletePredictions.getValue() != null)
                autocompletePredictions.setValue(null);
            return;
        }

        //Prevent excessive requests
        handler.postDelayed(() -> {
            handler.removeCallbacksAndMessages(null);
            getPredictionsForQuery(cleanQuery);
        }, 400);

    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Resource<LatLng>> getPosition() {
        return position;
    }

    public LiveData<Resource<List<RestaurantState>>> getRestaurantStates() {
        return restaurantStates;
    }

    public LiveData<Resource<ListViewState>> getListViewState() {
        return listViewState;
    }

    public void setListItemStateSortMethod(ListItemStateSortMethod listItemStateSortMethod) {
        this.listItemStateSortMethod.setValue(listItemStateSortMethod);
    }

    public LiveData<ListItemStateSortMethod> getListItemStateSortMethod() {
        return listItemStateSortMethod;
    }

    public LiveData<Resource<List<WorkmateState>>> getWorkmateStates() {
        return workmateStates;
    }

    public LiveData<UserInfo> getCurrentUserInfo() {
        return currentUserInfo;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
