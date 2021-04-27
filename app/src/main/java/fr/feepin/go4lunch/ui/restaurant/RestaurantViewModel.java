package fr.feepin.go4lunch.ui.restaurant;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.feepin.go4lunch.data.maps.MapsRepository;
import fr.feepin.go4lunch.data.user.UserRepository;
import fr.feepin.go4lunch.data.user.models.UserInfo;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class RestaurantViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private UserRepository userRepository;
    private MapsRepository mapsRepository;

    private MutableLiveData<Bitmap> restaurantPhoto = new MutableLiveData<>();
    private MutableLiveData<Place> place = new MutableLiveData<>();
    private MutableLiveData<List<UserInfo>> usersInfo = new MutableLiveData<>();
    private MutableLiveData<Integer> rating = new MutableLiveData<>();

    private String placeId;

    @Inject
    public RestaurantViewModel(UserRepository userRepository, MapsRepository mapsRepository) {
        this.userRepository = userRepository;
        this.mapsRepository = mapsRepository;
    }

    public void setup(String placeId) {
        this.placeId = placeId;
        setupPlace();
        setupRating();
        setupUsersInfo();
    }

    private void setupPlace() {
        mapsRepository.getRestaurantDetails(placeId, Arrays.asList(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.WEBSITE_URI,
                Place.Field.PHOTO_METADATAS,
                Place.Field.OPENING_HOURS
        ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Place>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Place place) {
                        RestaurantViewModel.this.place.setValue(place);
                        if (place.getPhotoMetadatas() != null) {
                            if (!place.getPhotoMetadatas().isEmpty()) {
                                setupPhoto(place.getPhotoMetadatas().get(0));
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("debug", "error: " + e.getMessage());
                    }
                });
    }

    private void setupPhoto(PhotoMetadata photoMetadata) {
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
                        Log.d("debug", "Error: " + e.getMessage());
                    }
                });
    }

    private void setupRating() {

    }

    private void setupUsersInfo() {
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
                        Log.d("debug", e.getMessage());
                    }

                    @Override
                    public void onComplete() {

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

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
