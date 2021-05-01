package fr.feepin.go4lunch.ui.restaurant;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.data.maps.MapsRepository;
import fr.feepin.go4lunch.data.user.UserRepository;
import fr.feepin.go4lunch.data.user.models.UserInfo;
import fr.feepin.go4lunch.data.user.models.VisitedRestaurant;
import fr.feepin.go4lunch.workers.NotifyWorker;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
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
            Place.Field.OPENING_HOURS
    );

    private WorkManager workManager;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private UserRepository userRepository;
    private MapsRepository mapsRepository;

    //States
    private MutableLiveData<Bitmap> restaurantPhoto = new MutableLiveData<>();
    private MutableLiveData<Place> place = new MutableLiveData<>();
    private MutableLiveData<List<UserInfo>> usersInfo = new MutableLiveData<>();
    private MediatorLiveData<Integer> rating = new MediatorLiveData<>();
    private MediatorLiveData<Boolean> liked = new MediatorLiveData<>();
    private MediatorLiveData<Boolean> joined = new MediatorLiveData<>();
    private MediatorLiveData<Boolean> alreadyVisited = new MediatorLiveData<>();

    //Datas
    private MutableLiveData<UserInfo> currentUserInfo = new MutableLiveData<>();
    private MutableLiveData<List<VisitedRestaurant>> usersVisitedRestaurants = new MutableLiveData<>(Collections.emptyList());
    private MutableLiveData<List<VisitedRestaurant>> currentUserVisitedRestaurants = new MutableLiveData<>(Collections.emptyList());

    private String placeId;
    private AutocompleteSessionToken sessionToken;

    @Inject
    public RestaurantViewModel(@ApplicationContext Context context, UserRepository userRepository, MapsRepository mapsRepository) {
        this.userRepository = userRepository;
        this.mapsRepository = mapsRepository;
        workManager = WorkManager.getInstance(context);
    }

    public void setup(String placeId, AutocompleteSessionToken sessionToken) {
        this.placeId = placeId;
        this.sessionToken = sessionToken;

        setupRating();
        setupLiked();
        setupJoined();
        setupAlreadyVisited();

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
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    private void askPhoto(PhotoMetadata photoMetadata) {
        mapsRepository.getRestaurantPhoto(photoMetadata)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<FetchPhotoResponse>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull FetchPhotoResponse fetchPhotoResponse) {
                        restaurantPhoto.setValue(fetchPhotoResponse.getBitmap());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

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
        userRepository.getUsersInfo()
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
                            long timeMillisFromNextMidday;

                            Calendar currentCalendar = Calendar.getInstance();
                            Calendar nextMiddayCalendar = Calendar.getInstance();

                            if (currentCalendar.get(Calendar.HOUR_OF_DAY) >= 12) {
                                nextMiddayCalendar.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            nextMiddayCalendar.set(Calendar.HOUR_OF_DAY, 12);

                            timeMillisFromNextMidday = nextMiddayCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();

                            Data data = new Data.Builder()
                                    .putString(Constants.KEY_RESTAURANT_ID, placeId)
                                    .putString(Constants.KEY_RESTAURANT_ADDRESS, place.getValue().getAddress())
                                    .putString(Constants.KEY_RESTAURANT_NAME, place.getValue().getName())
                                    .build();

                            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                                    NotifyWorker.class,
                                    12,
                                    TimeUnit.HOURS
                            )
                                    .setInputData(data)
                                    .setInitialDelay(5, TimeUnit.SECONDS)
                                    .build();
                            workManager.enqueueUniquePeriodicWork(Constants.NOTIFY_WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, request);

                        } else {
                            workManager.cancelUniqueWork(Constants.NOTIFY_WORKER_TAG);
                        }

                        joined.setValue(isJoining);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("debug", e.getMessage());
                    }
                });
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

    public LiveData<Bitmap> getRestaurantPhoto() {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
