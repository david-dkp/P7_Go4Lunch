package fr.feepin.go4lunch.data.models.domain;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PhotoMetadata;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

public class NearPlace {

    private final String placeId;

    private final LatLng latLng;

    private final String name;

    private final List<PhotoMetadata> photoMetadatas;

    private final String address;

    @Nullable
    private final Boolean isOpen;

    public NearPlace(String placeId, LatLng latLng, String name, List<PhotoMetadata> photoMetadatas, String address, @Nullable Boolean isOpen) {
        this.placeId = placeId;
        this.latLng = latLng;
        this.name = name;
        this.photoMetadatas = photoMetadatas;
        this.address = address;
        this.isOpen = isOpen;
    }

    public String getPlaceId() {
        return placeId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getName() {
        return name;
    }

    public List<PhotoMetadata> getPhotoMetadatas() {
        return photoMetadatas;
    }

    public String getAddress() {
        return address;
    }

    public Boolean isOpen() {
        return isOpen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NearPlace nearPlace = (NearPlace) o;
        return Objects.equals(placeId, nearPlace.placeId) &&
                Objects.equals(latLng, nearPlace.latLng) &&
                Objects.equals(name, nearPlace.name) &&
                Objects.equals(address, nearPlace.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeId, latLng, name, photoMetadatas, address, isOpen);
    }
}
