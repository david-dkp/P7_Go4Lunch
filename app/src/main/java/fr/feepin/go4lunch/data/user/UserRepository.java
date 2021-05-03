package fr.feepin.go4lunch.data.user;

import java.util.List;

import fr.feepin.go4lunch.data.user.models.UserInfo;
import fr.feepin.go4lunch.data.user.models.VisitedRestaurant;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface UserRepository {

    Single<List<VisitedRestaurant>> getVisitedRestaurants(String restaurantId);

    Observable<UserInfo> getCurrentUserInfoObservable();

    Single<UserInfo> getCurrentUserInfo();

    Single<List<VisitedRestaurant>> getCurrentUserVisitedRestaurants();

    Observable<List<UserInfo>> getUsersInfoObservable();

    Single<List<UserInfo>> getUsersInfo();

    Completable setRestaurantRating(String restaurantId, boolean liked);

    Completable joinRestaurant(String restaurantId);

    Completable leaveRestaurant();

    Completable registerUserInfo(UserInfo userInfo);

    Completable addRestaurantToVisited(String restaurantId);
}
