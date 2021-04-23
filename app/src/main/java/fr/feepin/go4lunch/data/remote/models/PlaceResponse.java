package fr.feepin.go4lunch.data.remote.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlaceResponse {

    @SerializedName("place_id")
    @Expose
    private String placeId;

    @Expose
    private Geometry geometry;

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
        private String lat;
        private String lng;

        public LatLng(String lat, String lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }
    }

}
