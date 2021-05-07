package fr.feepin.go4lunch.repositories;

import java.util.Collections;
import java.util.List;

import fr.feepin.go4lunch.data.user.UserRepository;
import fr.feepin.go4lunch.data.user.models.UserInfo;
import fr.feepin.go4lunch.data.user.models.VisitedRestaurant;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class FakeUserRepository implements UserRepository {
    @Override
    public Single<List<VisitedRestaurant>> getVisitedRestaurants(String restaurantId) {
        return Single.just(Collections.emptyList());
    }

    @Override
    public Observable<UserInfo> getCurrentUserInfoObservable() {
        return Observable.just(new UserInfo());
    }

    @Override
    public Single<UserInfo> getCurrentUserInfo() {
        return Single.just(new UserInfo());
    }

    @Override
    public Single<List<VisitedRestaurant>> getCurrentUserVisitedRestaurants() {
        return Single.just(Collections.emptyList());
    }

    @Override
    public Observable<List<UserInfo>> getUsersInfoObservable() {
        return Observable.just(Collections.emptyList());
    }

    @Override
    public Single<List<UserInfo>> getUsersInfo() {
        return Single.just(Collections.emptyList());
    }

    @Override
    public Completable setRestaurantRating(String restaurantId, boolean liked) {
        return Completable.complete();
    }

    @Override
    public Completable joinRestaurant(String restaurantId) {
        return Completable.complete();
    }

    @Override
    public Completable leaveRestaurant() {
        return Completable.complete();
    }

    @Override
    public Completable registerUserInfo(UserInfo userInfo) {
        return Completable.complete();
    }

    @Override
    public Completable addRestaurantToVisited(String restaurantId) {
        return Completable.complete();
    }
}
