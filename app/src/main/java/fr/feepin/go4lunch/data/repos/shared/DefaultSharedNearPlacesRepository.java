package fr.feepin.go4lunch.data.repos.shared;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.models.domain.NearPlace;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

@Singleton
public class DefaultSharedNearPlacesRepository implements SharedNearPlacesRepository {

    private final PublishSubject<List<NearPlace>> nearPlacesPublishSubject = PublishSubject.create();
    private final ArrayList<NearPlace> nearPlaces = new ArrayList<>();

    @Inject
    public DefaultSharedNearPlacesRepository() {
        nearPlacesPublishSubject.onNext(nearPlaces);
    }

    @Override
    public Observable<List<NearPlace>> getNearPlacesObservable() {
        return nearPlacesPublishSubject;
    }

    @Override
    public void addNearPlace(NearPlace nearPlace) {
        boolean added = addNearPlaceIfNotExists(nearPlace);

        if (added) {
            nearPlacesPublishSubject.onNext(nearPlaces);
        }
    }

    @Override
    public void addNearPlaces(List<NearPlace> nearPlaces) {
        for (NearPlace nearPlace : nearPlaces) {
            addNearPlaceIfNotExists(nearPlace);
        }

        nearPlacesPublishSubject.onNext(nearPlaces);
    }

    private boolean addNearPlaceIfNotExists(NearPlace nearPlace) {
        if (!nearPlaces.contains(nearPlace)) {
            nearPlaces.add(nearPlace);
            return true;
        }

        return false;
    }

}
