package fr.feepin.go4lunch.data.repos.data;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.models.domain.UserInfo;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class DefaultUserRepository implements UserRepository {

    private final FirebaseFirestore firebaseFirestore;
    private final FirebaseAuth firebaseAuth;

    private final Observable<UserInfo> userInfoObservable;

    @Inject
    public DefaultUserRepository(FirebaseAuth firebaseAuth) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = firebaseAuth;

        userInfoObservable = Observable.create(emitter -> {
            ListenerRegistration listenerRegistration =
                    firebaseFirestore
                            .collection("users")
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .addSnapshotListener((value, error) -> {
                                if (error != null) {
                                    emitter.tryOnError(error.getCause());
                                } else {
                                    emitter.onNext(value.toObject(UserInfo.class));
                                }
                            });

            emitter.setCancellable(listenerRegistration::remove);
        });
    }


    @Override
    public Observable<UserInfo> getUserInfoObservable() {
        return userInfoObservable;
    }

    @Override
    public Single<List<UserInfo>> getUsersInfo() {
        return Single.create(emitter -> {
            List<UserInfo> userInfos = Tasks.await(
                    firebaseFirestore
                            .collection("users")
                            .get()
            ).toObjects(UserInfo.class);

            emitter.onSuccess(userInfos);
        });
    }

    @Override
    public Completable addUserInfo(UserInfo userInfo) {
        return Completable.create(emitter -> {
            Tasks.await(
                    firebaseFirestore
                    .collection("users")
                    .add(userInfo)
            );
        });
    }
}
