package fr.feepin.go4lunch.data.maps.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaceResponse {

    @SerializedName("place_id")
    @Expose
    private String placeId;

    @Expose
    private Geometry geometry;

    @Expose
    private List<Photo> photos;

    @Expose
    private String vicinity;

    public PlaceResponse(String placeId, Geometry geometry) {
        this.placeId = placeId;
        this.geometry = geometry;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public class Geometry {

        @Expose
        private LatLng location;

        public Geometry(LatLng location) {
            this.location = location;
        }

        public LatLng getLocation() {
            return location;
        }

        public void setLocation(LatLng location) {
            this.location = location;
        }

    }

    public class LatLng {

        @Expose
        private float lat;

        @Expose
        private float lng;

        public LatLng(float lat, float lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public float getLat() {
            return lat;
        }

        public void setLat(float lat) {
            this.lat = lat;
        }

        public float getLng() {
            return lng;
        }

        public void setLng(float lng) {
            this.lng = lng;
        }

        public com.google.android.gms.maps.model.LatLng toMapsLatLng() {
            return new com.google.android.gms.maps.model.LatLng(getLat(), getLng());
        }
    }

    public class Photo {

        @Expose
        private int height;

        @Expose
        private int width;

        @SerializedName("photo_reference")
        @Expose
        private String reference;

        public Photo(int height, int width, String reference) {
            this.height = height;
            this.width = width;
            this.reference = reference;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }
    }
}
