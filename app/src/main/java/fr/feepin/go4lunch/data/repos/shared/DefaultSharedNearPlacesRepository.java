package fr.feepin.go4lunch.data.repos.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.models.domain.NearPlace;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

@Singleton
public class DefaultSharedNearPlacesRepository implements SharedNearPlacesRepository {

    private final MutableLiveData<List<NearPlace>> nearPlaces = new MutableLiveData<>(Collections.EMPTY_LIST);

    @Inject
    public DefaultSharedNearPlacesRepository() { }

    @Override
    public LiveData<List<NearPlace>> getNearPlaces() {
        return nearPlaces;
    }

    @Override
    public void addNearPlace(NearPlace nearPlace) {
        ArrayList<NearPlace> nearPlaces = new ArrayList<>(this.nearPlaces.getValue());

        boolean added = addNearPlaceIfNotExists(nearPlace, nearPlaces);

        if (added) {
            this.nearPlaces.setValue(nearPlaces);
        }
    }

    @Override
    public void addNearPlaces(List<NearPlace> nearPlaces) {

        ArrayList<NearPlace> newPlaces = new ArrayList<>(this.nearPlaces.getValue());

        boolean hasAdded = false;

        for (NearPlace nearPlace : nearPlaces) {
            boolean added = addNearPlaceIfNotExists(nearPlace, newPlaces);

            if (added) {
                hasAdded = true;
            }

        }

        if (hasAdded) {
            this.nearPlaces.setValue(newPlaces);
        }
    }

    private boolean addNearPlaceIfNotExists(NearPlace nearPlace, ArrayList<NearPlace> nearPlaces) {
        if (!nearPlaces.contains(nearPlace)) {
            nearPlaces.add(nearPlace);
            return true;
        }

        return false;
    }

}
