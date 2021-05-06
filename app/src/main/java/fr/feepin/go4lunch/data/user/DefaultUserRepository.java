package fr.feepin.go4lunch.data.user;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.rxjava3.RxDataStore;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.user.models.UserInfo;
import fr.feepin.go4lunch.data.user.models.VisitedRestaurant;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class DefaultUserRepository implements UserRepository {

    private final FirebaseFirestore firebaseFirestore;
    private final RxDataStore<Preferences> rxDataStore;
    private final FirebaseAuth firebaseAuth;

    @Inject
    public DefaultUserRepository(RxDataStore<Preferences> rxDataStore, FirebaseAuth firebaseAuth) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = firebaseAuth;
        this.rxDataStore = rxDataStore;
    }

    @Override
    public Single<List<VisitedRestaurant>> getVisitedRestaurants(String restaurantId) {
        return Single.create(e -> {

            QuerySnapshot querySnapshot = Tasks.await(
                    firebaseFirestore
                            .collectionGroup("visited_restaurants")
                            .whereEqualTo("restaurantId", restaurantId).get()
            );

            e.onSuccess(
                    querySnapshot.toObjects(VisitedRestaurant.class)
            );
        });
    }

    @Override
    public Single<UserInfo> getCurrentUserInfo() {
        return Single.create(emitter -> {
            DocumentSnapshot documentSnapshot = Tasks.await(
                    firebaseFirestore
                            .collection("users")
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .get()
            );

            emitter.onSuccess(documentSnapshot.toObject(UserInfo.class));
        });
    }

    @Override
    public Observable<UserInfo> getCurrentUserInfoObservable() {
        return Observable.create(emitter -> {
            firebaseFirestore
                    .collection("users")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null) {
                            emitter.tryOnError(error);
                        } else {
                            emitter.onNext(snapshot.toObject(UserInfo.class));
                        }
                    });
        });
    }

    @Override
    public Observable<List<UserInfo>> getUsersInfoObservable() {
        return Observable.create(e -> {

            firebaseFirestore.collection("users")
                    .whereNotEqualTo(FieldPath.documentId(), firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener((command, error) -> {
                        if (error != null) {
                            e.onError(error);
                        } else {
                            e.onNext(command.toObjects(UserInfo.class));
                        }
                    });

        });
    }

    @Override
    public Single<List<UserInfo>> getUsersInfo() {
        return Single.create(emitter -> {
            List<UserInfo> usersInfo = Tasks.await(
                    firebaseFirestore
                            .collection("users")
                            .whereNotEqualTo(FieldPath.documentId(), firebaseAuth.getCurrentUser().getUid())
                            .get()
            ).toObjects(UserInfo.class);
            emitter.onSuccess(usersInfo);
        });
    }

    @Override
    public Completable setRestaurantRating(String restaurantId, boolean liked) {
        return Completable.create(e -> {
            QuerySnapshot querySnapshot = Tasks.await(
                    firebaseFirestore
                            .collection("users")
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .collection("visited_restaurants")
                            .limit(1)
                            .whereEqualTo("restaurantId", restaurantId)
                            .get()

            );

            Tasks.await(
                    querySnapshot
                            .getDocuments()
                            .get(0)
                            .getReference()
                            .update("liked", liked)
            );
            e.onComplete();
        });
    }

    @Override
    public Completable joinRestaurant(String restaurantId) {
        return Completable.create(e -> {
            Tasks.await(firebaseFirestore
                    .collection("users")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .update("restaurantChoiceId", restaurantId)
            );
            e.onComplete();
        });
    }

    @Override
    public Completable leaveRestaurant() {
        return Completable.create(e -> {
            Tasks.await(firebaseFirestore
                    .collection("users")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .update("restaurantChoiceId", "")
            );
            e.onComplete();
        });
    }

    @Override
    public Completable registerUserInfo(UserInfo userInfo) {
        return Completable.create(emitter -> {
            Tasks.await(
                    firebaseFirestore
                            .collection("users")
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .set(userInfo)
            );

            emitter.onComplete();
        });
    }

    @Override
    public Completable addRestaurantToVisited(String restaurantId) {
        return Completable.create(emitter -> {

            QuerySnapshot snapshots = Tasks.await(
                    firebaseFirestore
                            .collection("users")
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .collection("visited_restaurants")
                            .whereEqualTo("restauranntId", restaurantId)
                            .limit(1)
                            .get()
            );

            CollectionReference visitedRestaurantsCollection = firebaseFirestore.collection("users")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .collection("visited_restaurants");

            if (snapshots.getDocuments().isEmpty()) {
                Tasks.await(
                        visitedRestaurantsCollection.add(new VisitedRestaurant(restaurantId, false))
                );
            } else {
                Tasks.await(visitedRestaurantsCollection.document(snapshots.getDocuments().get(0).getId()).set(new VisitedRestaurant(restaurantId, false)));
            }

            emitter.onComplete();
        });
    }

    @Override
    public Single<List<VisitedRestaurant>> getCurrentUserVisitedRestaurants() {
        return Single.create(e -> {
            List<VisitedRestaurant> visitedRestaurants = Tasks.await(
                    firebaseFirestore
                            .collection("users")
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .collection("visited_restaurants")
                            .get()
            ).toObjects(VisitedRestaurant.class);

            e.onSuccess(visitedRestaurants);
        });
    }
}
