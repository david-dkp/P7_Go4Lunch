package fr.feepin.go4lunch.ui.restaurant;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.data.models.domain.UserInfo;
import fr.feepin.go4lunch.data.models.domain.VisitedRestaurant;
import fr.feepin.go4lunch.data.repos.data.MapsRepository;
import fr.feepin.go4lunch.data.repos.data.RestaurantRepository;
import fr.feepin.go4lunch.data.repos.data.UserRepository;
import fr.feepin.go4lunch.others.SchedulerProvider;
import fr.feepin.go4lunch.utils.SingleEventData;
import fr.feepin.go4lunch.utils.VisitedRestaurantUtils;
import fr.feepin.go4lunch.workers.NotifyWorker;
import fr.feepin.go4lunch.workers.VisitRestaurantWorker;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class RestaurantViewModel extends ViewModel {

    private static final List<Place.Field> REQUIRED_PLACE_FIELDS = Arrays.asList(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.WEBSITE_URI,
            Place.Field.PHOTO_METADATAS,
            Place.Field.OPENING_HOURS,
            Place.Field.UTC_OFFSET
    );

    private final WorkManager workManager;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MapsRepository mapsRepository;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    //States
    private final MutableLiveData<Resource<Bitmap>> restaurantPhoto = new MutableLiveData<>();
    private final MutableLiveData<Place> place = new MutableLiveData<>();
    private final MutableLiveData<List<UserInfo>> usersInfo = new MutableLiveData<>();
    private final MediatorLiveData<Integer> rating = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> liked = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> joined = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> alreadyVisited = new MediatorLiveData<>();
    private final MutableLiveData<SingleEventData<Resource.Error<Void>>> notOpenError = new MutableLiveData<>();

    //Datas
    private final MutableLiveData<UserInfo> currentUserInfo = new MutableLiveData<>();
    private final MutableLiveData<List<VisitedRestaurant>> usersVisitedRestaurants = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<VisitedRestaurant>> currentUserVisitedRestaurants = new MutableLiveData<>(Collections.emptyList());

    private SchedulerProvider schedulerProvider;

    private String placeId;
    private AutocompleteSessionToken sessionToken;

    @Inject
    public RestaurantViewModel(@ApplicationContext Context context, UserRepository userRepository, RestaurantRepository restaurantRepository, MapsRepository mapsRepository, SchedulerProvider schedulerProvider) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.mapsRepository = mapsRepository;
        this.schedulerProvider = schedulerProvider;

        workManager = WorkManager.getInstance(context);

        setupRating();
        setupLiked();
        setupJoined();
        setupAlreadyVisited();
    }

    public void setup(String placeId, AutocompleteSessionToken sessionToken) {
        this.placeId = placeId;
        this.sessionToken = sessionToken;

        askPlace();
        askRating();
        askUsersInfo();
        askCurrentUserInfo();
        askCurrentUserVisitedRestaurants();
    }

    private void setupRating() {
        rating.addSource(usersVisitedRestaurants, visitedRestaurants -> {
            this.rating.setValue(VisitedRestaurantUtils.calculateRating(visitedRestaurants));
        });
    }

    private void setupLiked() {
        liked.addSource(currentUserVisitedRestaurants, visitedRestaurants -> {
            liked.setValue(visitedRestaurants.contains(new VisitedRestaurant(placeId, true)));
        });
    }

    private void setupJoined() {
        joined.addSource(currentUserInfo, userInfo -> {
            joined.setValue(userInfo.getRestaurantChoiceId().equals(placeId));
        });
    }

    private void setupAlreadyVisited() {
        alreadyVisited.addSource(currentUserVisitedRestaurants, visitedRestaurants -> {

            for (VisitedRestaurant visitedRestaurant : visitedRestaurants) {
                if (visitedRestaurant.getRestaurantId().equals(placeId)) {
                    alreadyVisited.setValue(true);
                    return;
                }
            }

            alreadyVisited.setValue(false);
        });
    }

    private void askPlace() {
        Disposable disposable = mapsRepository.getPlace(placeId, REQUIRED_PLACE_FIELDS, this.sessionToken)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe((place, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                    } else {
                        RestaurantViewModel.this.sessionToken = null;
                        RestaurantViewModel.this.place.setValue(place);
                        if (place.getPhotoMetadatas() != null) {
                            if (!place.getPhotoMetadatas().isEmpty()) {
                                askPhoto(place.getPhotoMetadatas().get(0));
                            } else {
                                restaurantPhoto.setValue(new Resource.Error<>(null, null));
                            }
                        } else {
                            restaurantPhoto.setValue(new Resource.Error<>(null, null));
                        }
                    }
                });

        compositeDisposable.add(disposable);
    }

    private void askPhoto(PhotoMetadata photoMetadata) {
        restaurantPhoto.setValue(new Resource.Loading<>(null, null));

        Disposable disposable = mapsRepository.getPlacePhoto(placeId, photoMetadata)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe((bitmap, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        restaurantPhoto.setValue(new Resource.Error<>(bitmap, null));
                    } else {
                        restaurantPhoto.setValue(new Resource.Success<>(bitmap, null));
                    }
                });

        compositeDisposable.add(disposable);
    }

    private void askRating() {
        Disposable disposable = restaurantRepository.getVisitedRestaurantsByRestaurantId(placeId)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe((visitedRestaurants, throwable) -> {

                    if (throwable != null) {
                        throwable.printStackTrace();
                    } else {
                        usersVisitedRestaurants.setValue(visitedRestaurants);
                    }

                });

        compositeDisposable.add(disposable);
    }

    private void askUsersInfo() {
        Disposable disposable = userRepository.getUsersInfo()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe((userInfos, throwable) -> {

                    if (throwable != null) throwable.printStackTrace();

                    ArrayList<UserInfo> users = new ArrayList<>();

                    for (UserInfo userInfo : userInfos) {
                        if (userInfo.getRestaurantChoiceId().equals(placeId)) {
                            users.add(userInfo);
                        }
                    }

                    usersInfo.setValue(users);
                });

        compositeDisposable.add(disposable);
    }

    private void askCurrentUserInfo() {
        Disposable disposable = userRepository.getUserInfoObservable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(currentUserInfo::setValue);

        compositeDisposable.add(disposable);
    }

    private void askCurrentUserVisitedRestaurants() {
        Disposable disposable = restaurantRepository.getUserVisitedRestaurants(firebaseAuth.getCurrentUser().getUid())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe((visitedRestaurants, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                    } else {
                        currentUserVisitedRestaurants.setValue(visitedRestaurants);
                    }
                });

        compositeDisposable.add(disposable);
    }

    public void rateRestaurant() {
        Disposable disposable = restaurantRepository.setRestaurantRating(placeId, !liked.getValue())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnError(Throwable::printStackTrace)
                .subscribe(() -> liked.setValue(!liked.getValue()));

        compositeDisposable.add(disposable);
    }

    public void joinOrLeaveRestaurant() {

        boolean isJoining = !isJoined().getValue();

        if (getPlace().getValue().getOpeningHours() != null && !getPlace().getValue().isOpen() && isJoining) {
            notOpenError.setValue(new SingleEventData<>(new Resource.Error<>(null, null)));
            return;
        }

        Completable completable;

        if (!isJoining) {
            completable = restaurantRepository.leaveRestaurant();
        } else {
            completable = restaurantRepository.setRestaurantChoice(placeId);
        }

        Disposable disposable = completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(Throwable::printStackTrace)
                .subscribe(() -> {
                    if (isJoining) {
                        addNotifyWorker();
                        addVisitRestaurantWorker();
                    } else {
                        workManager.cancelUniqueWork(Constants.NOTIFY_WORKER_TAG);
                        workManager.cancelUniqueWork(Constants.VISIT_RESTAURANT_TAG);
                    }

                    joined.setValue(isJoining);
                });

        compositeDisposable.add(disposable);
    }

    private void addNotifyWorker() {
        long timeMillisFromNextMidday;

        Calendar currentCalendar = Calendar.getInstance();
        Calendar nextMiddayCalendar = Calendar.getInstance();

        if (currentCalendar.get(Calendar.HOUR_OF_DAY) >= Constants.HOUR_NOTIFICATION_FIRE) {
            return;
        }
        nextMiddayCalendar.set(Calendar.HOUR_OF_DAY, Constants.HOUR_NOTIFICATION_FIRE);

        timeMillisFromNextMidday = nextMiddayCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();

        Data data = new Data.Builder()
                .putString(Constants.KEY_RESTAURANT_ID, placeId)
                .putString(Constants.KEY_RESTAURANT_ADDRESS, place.getValue().getAddress())
                .putString(Constants.KEY_RESTAURANT_NAME, place.getValue().getName())
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                .setInputData(data)
                .setInitialDelay(timeMillisFromNextMidday, TimeUnit.MILLISECONDS)
                .build();
        workManager.enqueueUniqueWork(Constants.NOTIFY_WORKER_TAG, ExistingWorkPolicy.REPLACE, request);
    }

    private void addVisitRestaurantWorker() {

        Data data = new Data.Builder()
                .putString(Constants.KEY_RESTAURANT_ID, placeId)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(VisitRestaurantWorker.class)
                .setInputData(data)
                .setInitialDelay(Constants.HOUR_VISIT_RESTAURANT_DELAY, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, Constants.MINUTES_VISIT_RESTAURANT_BACKOFF_DELAY, TimeUnit.MINUTES)
                .build();

        workManager.enqueueUniqueWork(Constants.VISIT_RESTAURANT_TAG, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);
    }

    public LiveData<Place> getPlace() {
        return place;
    }

    public LiveData<List<UserInfo>> getUsersInfo() {
        return usersInfo;
    }

    public LiveData<Integer> getRating() {
        return rating;
    }

    public LiveData<Resource<Bitmap>> getRestaurantPhoto() {
        return restaurantPhoto;
    }

    public LiveData<Boolean> isLiked() {
        return liked;
    }

    public LiveData<Boolean> isJoined() {
        return joined;
    }

    public LiveData<Boolean> hasAlreadyVisited() {
        return alreadyVisited;
    }

    public LiveData<SingleEventData<Resource.Error<Void>>> getNotOpenError() {
        return notOpenError;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
