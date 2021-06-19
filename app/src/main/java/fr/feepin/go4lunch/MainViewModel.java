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
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.data.repos.data.MapsRepository;
import fr.feepin.go4lunch.data.models.dtos.NearbySearchDto;
import fr.feepin.go4lunch.data.models.dtos.NearbySearchResultDto;
import fr.feepin.go4lunch.data.repos.data.UserRepository;
import fr.feepin.go4lunch.data.models.domain.UserInfo;
import fr.feepin.go4lunch.others.SchedulerProvider;
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
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final SchedulerProvider schedulerProvider;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<FirebaseUser> currentUser;
    private final MutableLiveData<UserInfo> currentUserInfo = new MutableLiveData<>();

    @Inject
    public MainViewModel(@ApplicationContext Context context,
                         FirebaseAuth firebaseAuth,
                         UserRepository userRepository,
                         SchedulerProvider schedulerProvider
    ) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
        this.schedulerProvider = schedulerProvider;

        setup();
    }

    public void setup() {
        setupFirebaseUser();
    }

    private void setupFirebaseUser() {
        currentUser = new MutableLiveData<>(firebaseAuth.getCurrentUser());

        firebaseAuth.addAuthStateListener(newAuth -> {
            currentUser.postValue(newAuth.getCurrentUser());
        });

        Disposable disposable = userRepository.getUserInfoObservable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnError(Throwable::printStackTrace)
                .subscribe(currentUserInfo::setValue);

        compositeDisposable.add(disposable);
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<UserInfo> getCurrentUserInfo() {
        return currentUserInfo;
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
