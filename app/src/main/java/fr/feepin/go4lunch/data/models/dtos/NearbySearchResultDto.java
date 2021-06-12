package fr.feepin.go4lunch.data.models.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NearbySearchResultDto {

    @SerializedName("place_id")
    @Expose
    private String placeId;

    @Expose
    private Geometry geometry;

    @Expose
    private String name;

    @Expose
    private List<Photo> photos;

    @Expose
    private String vicinity;

    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;

    public NearbySearchResultDto(String placeId, Geometry geometry, String name, List<Photo> photos, String vicinity, OpeningHours openingHours) {
        this.placeId = placeId;
        this.geometry = geometry;
        this.name = name;
        this.photos = photos;
        this.vicinity = vicinity;
        this.openingHours = openingHours;
    }

    public String getPlaceId() {
        return placeId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getName() {
        return name;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public String getVicinity() {
        return vicinity;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public static class OpeningHours {

        @SerializedName("open_now")
        @Expose
        private boolean openNow;

        public OpeningHours(boolean openNow) {
            this.openNow = openNow;
        }

        public boolean isOpenNow() {
            return openNow;
        }

        @Override
        public String toString() {
            return "OpeningHours{" +
                    "openNow=" + openNow +
                    '}';
        }
    }

    public static class Geometry {

        @Expose
        private LatLng location;

        public Geometry(LatLng location) {
            this.location = location;
        }

        public LatLng getLocation() {
            return location;
        }

    }

    public static class LatLng {

        @Expose
        private double lat;

        @Expose
        private double lng;

        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public com.google.android.gms.maps.model.LatLng toMapsLatLng() {
            return new com.google.android.gms.maps.model.LatLng(getLat(), getLng());
        }

        public static LatLng fromMapsLatLng(com.google.android.gms.maps.model.LatLng mapsLatLng) {
            return new LatLng(mapsLatLng.latitude, mapsLatLng.longitude);
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

        public int getWidth() {
            return width;
        }

        public String getReference() {
            return reference;
        }
    }

    @Override
    public String toString() {
        return "PlaceResponse{" +
                "placeId='" + placeId + '\'' +
                ", geometry=" + geometry +
                ", name='" + name + '\'' +
                ", photos=" + photos +
                ", vicinity='" + vicinity + '\'' +
                ", openingHours=" + openingHours +
                '}';
    }
}
