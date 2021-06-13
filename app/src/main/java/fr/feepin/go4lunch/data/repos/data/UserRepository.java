package fr.feepin.go4lunch.data.repos.data;

import java.util.List;

import fr.feepin.go4lunch.data.models.domain.UserInfo;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface UserRepository {

    Observable<UserInfo> getUserInfoObservable();

    Single<List<UserInfo>> getUsersInfo();

    Completable addUserInfo(UserInfo userInfo);

}
