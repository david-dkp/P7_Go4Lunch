package fr.feepin.go4lunch.data.models.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class NearPlace {

    private final String placeId;

    private final LatLng latLng;

    private final String name;

    private final List<Photo> photos;

    private final String address;

    private final boolean isOpen;

    public NearPlace(String placeId, LatLng latLng, String name, List<Photo> photos, String address, boolean isOpen) {
        this.placeId = placeId;
        this.latLng = latLng;
        this.name = name;
        this.photos = photos;
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

    public List<Photo> getPhotos() {
        return photos;
    }

    public String getAddress() {
        return address;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public static class Photo {

        private final int width, height;

        private final String reference;

        public Photo(int width, int height, String reference) {
            this.width = width;
            this.height = height;
            this.reference = reference;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getReference() {
            return reference;
        }
    }

}
