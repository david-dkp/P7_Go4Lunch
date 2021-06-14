package fr.feepin.go4lunch.data.repos.data;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import javax.inject.Singleton;

import fr.feepin.go4lunch.data.models.domain.VisitedRestaurant;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class DefaultRestaurantRepository implements RestaurantRepository {

    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    public Single<List<VisitedRestaurant>> getVisitedRestaurantsByRestaurantId(String restaurantId) {
        return Single.create(emitter -> {
            List<VisitedRestaurant> visitedRestaurants = Tasks.await(firebaseFirestore
                    .collectionGroup("visited_restaurants")
                    .whereEqualTo("restaurant_id", restaurantId)
                    .get()
            ).toObjects(VisitedRestaurant.class);

            emitter.onSuccess(visitedRestaurants);
        });
    }

    @Override
    public Single<List<VisitedRestaurant>> getUserVisitedRestaurants(String userId) {
        return Single.create(emitter -> {
            List<VisitedRestaurant> visitedRestaurants = Tasks.await(firebaseFirestore
                    .collection("users")
                    .document(userId)
                    .collection("visited_restaurants")
                    .get()
            ).toObjects(VisitedRestaurant.class);

            emitter.onSuccess(visitedRestaurants);
        });
    }

    @Override
    public Completable setRestaurantRating(String id, boolean liked) {
        return Completable.create(emitter -> {
            Tasks.await(
                    firebaseFirestore.collection("users")
                            .document(firebaseUser.getUid())
                            .collection("visited_restaurants")
                            .document(id)
                            .update("liked", liked)
            );
            emitter.onComplete();
        });
    }

    @Override
    public Completable setRestaurantChoice(String restaurantId) {
        return Completable.create(emitter -> {
            Tasks.await(
                    firebaseFirestore
                    .collection("users")
                    .document(firebaseUser.getUid())
                    .update("restaurant_choice_id", restaurantId)
            );
            emitter.onComplete();
        });
    }

    @Override
    public Completable leaveRestaurant() {
        return Completable.create(emitter -> {
            Tasks.await(
                    firebaseFirestore
                    .collection("users")
                    .document(firebaseUser.getUid())
                    .update("restaurant_choice_id", "")
            );
            emitter.onComplete();
        });
    }

    @Override
    public Completable addVisitedRestaurant(String restaurantId) {
        return Completable.create(emitter -> {
            Tasks.await(
                    firebaseFirestore
                    .collection("users")
                    .document(firebaseUser.getUid())
                    .collection("visited_restaurants")
                    .add(new VisitedRestaurant(restaurantId, false))
            );
            emitter.onComplete();
        });
    }

}