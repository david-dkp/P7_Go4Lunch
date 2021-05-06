package fr.feepin.go4lunch.ui.restaurant;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;

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
import fr.feepin.go4lunch.data.maps.MapsRepository;
import fr.feepin.go4lunch.data.user.UserRepository;
import fr.feepin.go4lunch.data.user.models.UserInfo;
import fr.feepin.go4lunch.data.user.models.VisitedRestaurant;
import fr.feepin.go4lunch.utils.SingleEventData;
import fr.feepin.go4lunch.workers.NotifyWorker;
import fr.feepin.go4lunch.workers.VisitRestaurantWorker;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
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
    private final MapsRepository mapsRepository;

    //States
    private final MutableLiveData<Resource<Bitmap>> restaurantPhoto = new MutableLiveData<>();
    private final MutableLiveData<Place> place = new MutableLiveData<>();
    private final MutableLiveData<List<UserInfo>> usersInfo = new MutableLiveData<>();
    private final MediatorLiveData<Integer> rating = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> liked = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> joined = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> alreadyVisited = new MediatorLiveData<>();
    private final MutableLiveData<SingleEventData<Resource.Error<Void>>> notOpenError = new MutableLiveData();

    //Datas
    private final MutableLiveData<UserInfo> currentUserInfo = new MutableLiveData<>();
    private final MutableLiveData<List<VisitedRestaurant>> usersVisitedRestaurants = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<VisitedRestaurant>> currentUserVisitedRestaurants = new MutableLiveData<>(Collections.emptyList());

    private String placeId;
    private AutocompleteSessionToken sessionToken;

    @Inject
    public RestaurantViewModel(@ApplicationContext Context context, UserRepository userRepository, MapsRepository mapsRepository) {
        this.userRepository = userRepository;
        this.mapsRepository = mapsRepository;
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
            int likes = 0;

            for (VisitedRestaurant visitedRestaurant : visitedRestaurants) {
                if (visitedRestaurant.isLiked()) likes++;
            }

            int rating = Math.round(((float) likes / (float) visitedRestaurants.size()) * 3f);
            this.rating.setValue(rating);
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
        mapsRepository.getRestaurantDetails(placeId, REQUIRED_PLACE_FIELDS, this.sessionToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Place>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Place place) {
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

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    private void askPhoto(PhotoMetadata photoMetadata) {
        restaurantPhoto.setValue(new Resource.Loading(null, null));
        mapsRepository.getRestaurantPhoto(placeId, photoMetadata)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<FetchPhotoResponse>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull FetchPhotoResponse fetchPhotoResponse) {
                        restaurantPhoto.setValue(new Resource.Success<>(fetchPhotoResponse.getBitmap(), null));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        restaurantPhoto.setValue(new Resource.Error<>(null, null));
                    }
                });
    }

    private void askRating() {
        userRepository.getVisitedRestaurants(placeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<VisitedRestaurant>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull List<VisitedRestaurant> visitedRestaurants) {
                        usersVisitedRestaurants.setValue(visitedRestaurants);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    private void askUsersInfo() {
        userRepository.getUsersInfoObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<UserInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull List<UserInfo> usersInfo) {
                        ArrayList<UserInfo> users = new ArrayList();

                        for (UserInfo userInfo : usersInfo) {
                            if (userInfo.getRestaurantChoiceId().equals(placeId)) {
                                users.add(userInfo);
                            }
                        }

                        RestaurantViewModel.this.usersInfo.setValue(users);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void askCurrentUserInfo() {
        userRepository.getCurrentUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<UserInfo>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull UserInfo userInfo) {
                        RestaurantViewModel.this.currentUserInfo.setValue(userInfo);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    private void askCurrentUserVisitedRestaurants() {
        userRepository.getCurrentUserVisitedRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<VisitedRestaurant>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull List<VisitedRestaurant> visitedRestaurants) {
                        currentUserVisitedRestaurants.setValue(visitedRestaurants);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("debug", e.getMessage());
                    }
                });
    }

    public void rateRestaurant() {
        userRepository.setRestaurantRating(placeId, !liked.getValue())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        liked.setValue(!liked.getValue());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("debug", "error: " + e.getMessage());
                    }
                });
    }

    public void joinOrLeaveRestaurant() {

        if (!getPlace().getValue().isOpen()) {
            notOpenError.setValue(new SingleEventData<>(new Resource.Error<>(null, null)));
            return;
        }

        Completable completable;
        boolean isJoining = !isJoined().getValue();

        if (!isJoining) {
            completable = userRepository.leaveRestaurant();
        } else {
            completable = userRepository.joinRestaurant(placeId);
        }

        completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {

                        if (isJoining) {
                            addNotifyWorker();
                            addVisitRestaurantWorker();
                        } else {
                            workManager.cancelUniqueWork(Constants.NOTIFY_WORKER_TAG);
                            workManager.cancelUniqueWork(Constants.VISIT_RESTAURANT_TAG);
                        }

                        joined.setValue(isJoining);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void addNotifyWorker() {
        long timeMillisFromNextMidday;

        Calendar currentCalendar = Calendar.getInstance();
        Calendar nextMiddayCalendar = Calendar.getInstance();

        if (currentCalendar.get(Calendar.HOUR_OF_DAY) >= Constants.HOUR_NOTIFICATION_FIRE_DELAY) {
            nextMiddayCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        nextMiddayCalendar.set(Calendar.HOUR_OF_DAY, Constants.HOUR_NOTIFICATION_FIRE_DELAY);

        timeMillisFromNextMidday = nextMiddayCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();

        Data data = new Data.Builder()
                .putString(Constants.KEY_RESTAURANT_ID, placeId)
                .putString(Constants.KEY_RESTAURANT_ADDRESS, place.getValue().getAddress())
                .putString(Constants.KEY_RESTAURANT_NAME, place.getValue().getName())
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(NotifyWorker.class, 24, TimeUnit.HOURS)
                .setInputData(data)
                .setInitialDelay(timeMillisFromNextMidday, TimeUnit.MILLISECONDS)
                .build();
        workManager.enqueueUniquePeriodicWork(Constants.NOTIFY_WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, request);
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
