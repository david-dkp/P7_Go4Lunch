package fr.feepin.go4lunch.data.repos.shared;

import androidx.lifecycle.LiveData;

import java.util.List;

import fr.feepin.go4lunch.data.models.domain.NearPlace;
import io.reactivex.rxjava3.core.Observable;

public interface SharedNearPlacesRepository {

    LiveData<List<NearPlace>> getNearPlaces();

    void addNearPlace(NearPlace nearPlace);

    void addNearPlaces(List<NearPlace> nearPlaces);

}
