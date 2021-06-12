package fr.feepin.go4lunch.data.models.mappers;

import com.google.android.libraries.places.api.model.AutocompletePrediction;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.models.domain.PlacePrediction;

@Singleton
public class AutocompletePredictionMapper implements Mapper<AutocompletePrediction, PlacePrediction> {

    @Inject
    public AutocompletePredictionMapper() {}

    @Override
    public PlacePrediction toEntity(AutocompletePrediction autocompletePrediction) {
        return new PlacePrediction(autocompletePrediction.getPlaceId(), autocompletePrediction.getDistanceMeters());
    }

}
