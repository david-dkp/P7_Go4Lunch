package fr.feepin.go4lunch.data.user;

import android.util.Log;
import android.widget.TextView;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.rxjava3.RxDataStore;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private FirebaseFirestore firebaseFirestore;
    private RxDataStore<Preferences> rxDataStore;
    private FirebaseAuth firebaseAuth;

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
    public Observable<List<UserInfo>> getUsersInfo() {
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
            firebaseFirestore
                    .collection("users")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .update("restaurantChoiceId", restaurantId);
        });
    }

    @Override
    public Completable leaveRestaurant() {
        return Completable.create(e -> {
            firebaseFirestore
                    .collection("users")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .update("restaurantChoiceId", null);
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
            Tasks.await(
                    firebaseFirestore
                            .collection("users")
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .collection("visited_restaurants")
                            .add(new VisitedRestaurant(restaurantId, false))
            );

            emitter.onComplete();
        });
    }
}
