package fr.feepin.go4lunch.data.models.mappers;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.models.domain.NearPlace;
import fr.feepin.go4lunch.data.models.dtos.NearbySearchResultDto;

@Singleton
public class NearbyPlaceMapper implements Mapper<NearbySearchResultDto, NearPlace> {

    @Inject
    public NearbyPlaceMapper() {}

    @Override
    public NearPlace toEntity(NearbySearchResultDto nearbySearchResultDto) {

        ArrayList<NearPlace.Photo> domainPhotos = new ArrayList<>();

        for (NearbySearchResultDto.Photo photo : nearbySearchResultDto.getPhotos()) {
            domainPhotos.add(new NearPlace.Photo(photo.getWidth(), photo.getHeight(), photo.getReference()));
        }

        return new NearPlace(
                nearbySearchResultDto.getPlaceId(),
                new LatLng(
                        nearbySearchResultDto.getGeometry().getLocation().getLat(),
                        nearbySearchResultDto.getGeometry().getLocation().getLat()
                ),
                nearbySearchResultDto.getName(),
                domainPhotos,
                nearbySearchResultDto.getVicinity(),
                nearbySearchResultDto.getOpeningHours().isOpenNow()
        );
    }

}
