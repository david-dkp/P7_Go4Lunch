package fr.feepin.go4lunch.data.models.mappers;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PhotoMetadata;

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

        ArrayList<PhotoMetadata> domainPhotos = new ArrayList<>();

        if (nearbySearchResultDto.getPhotos() != null) {
            for (NearbySearchResultDto.Photo photo : nearbySearchResultDto.getPhotos()) {
                PhotoMetadata photoMetadata = PhotoMetadata.builder(photo.getReference())
                        .setWidth(photo.getWidth())
                        .setHeight(photo.getHeight())
                        .build();
                domainPhotos.add(photoMetadata);
            }
        }

        Boolean openNow = null;

        if (nearbySearchResultDto.getOpeningHours() != null) {
            openNow = nearbySearchResultDto.getOpeningHours().isOpenNow();
        }

        return new NearPlace(
                nearbySearchResultDto.getPlaceId(),
                new LatLng(
                        nearbySearchResultDto.getGeometry().getLocation().getLat(),
                        nearbySearchResultDto.getGeometry().getLocation().getLng()
                ),
                nearbySearchResultDto.getName(),
                domainPhotos,
                nearbySearchResultDto.getVicinity(),
                openNow
        );
    }

}
