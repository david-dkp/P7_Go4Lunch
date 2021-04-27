package fr.feepin.go4lunch;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.location.LocationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.data.maps.MapsRepository;
import fr.feepin.go4lunch.data.user.UserRepository;
import fr.feepin.go4lunch.utils.PermissionUtils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private Context context;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private LocationManager locationManager;

    private FirebaseAuth firebaseAuth;
    private MapsRepository mapsRepository;
    private UserRepository userRepository;

    private MutableLiveData<FirebaseUser> currentUser;
    private MutableLiveData<Resource<LatLng>> position = new MutableLiveData<>();

    @Inject
    public MainViewModel(@ApplicationContext Context context, FirebaseAuth firebaseAuth, MapsRepository mapsRepository, UserRepository userRepository) {
        this.context = context;
        this.firebaseAuth = firebaseAuth;
        this.mapsRepository = mapsRepository;
        this.userRepository = userRepository;

        setupLocationManager();
        setupFirebaseUser();
    }

    private void setupLocationManager() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private void setupFirebaseUser() {
        currentUser = new MutableLiveData<>(firebaseAuth.getCurrentUser());
        firebaseAuth.addAuthStateListener( newAuth -> {
            currentUser.postValue(newAuth.getCurrentUser());
        });
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public void askLocation() {

        if (!PermissionUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            position.setValue(new Resource.Error<>(null, Constants.NO_LOCATION_PERMISSION_MESSAGE));
            return;
        }

        if (!LocationManagerCompat.isLocationEnabled(locationManager)){

            Location latestKnown = mapsRepository.getLastKnownLocation();

            if (latestKnown != null) {
                position.setValue(new Resource.Error<>(new LatLng(latestKnown.getLatitude(), latestKnown.getLongitude()), Constants.LOCATION_DISABLED_MESSAGE));
            } else {
                mapsRepository.getLatestPositionFromPrefs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<LatLng>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                compositeDisposable.add(d);
                            }

                            @Override
                            public void onSuccess(@NonNull LatLng latLng) {
                                position.setValue(new Resource.Error<>(latLng, Constants.LOCATION_DISABLED_MESSAGE));
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                position.setValue(new Resource.Error<>(null, Constants.LOCATION_DISABLED_MESSAGE));
                            }
                        });
            }
        } else {
            mapsRepository.getCurrentLocation()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Location>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(@NonNull Location location) {
                            position.setValue(new Resource.Success<>(new LatLng(location.getLatitude(), location.getLongitude()), null));
                            Log.d("debug", "called");
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            position.setValue(new Resource.Success<>(null, e.getMessage()));
                        }
                    });
        }
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
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
