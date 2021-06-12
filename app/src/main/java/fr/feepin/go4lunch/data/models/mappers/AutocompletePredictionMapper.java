package fr.feepin.go4lunch.data.models.mappers;

import com.google.android.libraries.places.api.model.AutocompletePrediction;

import fr.feepin.go4lunch.data.models.domain.PlacePrediction;

public class AutocompletePredictionMapper implements Mapper<AutocompletePrediction, PlacePrediction> {

    @Override
    public PlacePrediction toEntity(AutocompletePrediction autocompletePrediction) {
        return new PlacePrediction(autocompletePrediction.getPlaceId(), autocompletePrediction.getDistanceMeters());
    }

}
