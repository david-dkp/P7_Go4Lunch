package fr.feepin.go4lunch.data;

import java.util.List;

import fr.feepin.go4lunch.data.models.domain.VisitedRestaurant;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface RestaurantRepository {

    Single<List<VisitedRestaurant>> getVisitedRestaurantsByRestaurantId(String restaurantId);

    Single<List<VisitedRestaurant>> getUserVisitedRestaurants(String userId);

    Completable setRestaurantRating(String restaurantId, boolean liked);

    Completable setRestaurantChoice(String restaurantId);

    Completable leaveRestaurant();

    Completable addVisitedRestaurant(String restaurantId);

}
