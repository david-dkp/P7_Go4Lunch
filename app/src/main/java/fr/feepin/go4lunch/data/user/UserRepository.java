package fr.feepin.go4lunch.data.user;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import fr.feepin.go4lunch.data.user.models.UserInfo;
import fr.feepin.go4lunch.data.user.models.VisitedRestaurant;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface UserRepository {

    Single<List<VisitedRestaurant>> getVisitedRestaurants(String restaurantId);

    Observable<List<UserInfo>> getUsersInfo();

    Completable setRestaurantRating(String restaurantId, boolean liked);

    Completable joinRestaurant(String restaurantId);

    Completable leaveRestaurant();

    Completable registerUserInfo(UserInfo userInfo);
}
