package fr.feepin.go4lunch.data.repos.shared;

import java.util.List;

import fr.feepin.go4lunch.data.models.domain.NearPlace;
import io.reactivex.rxjava3.core.Observable;

public interface SharedNearPlacesRepository {

    Observable<List<NearPlace>> getNearPlacesObservable();

    void addNearPlace(NearPlace nearPlace);

    void addNearPlaces(List<NearPlace> nearPlaces);

}
