package fr.feepin.go4lunch.data.user.models;

import java.util.Objects;

public class UserInfo {

    private String name;

    private String photoUrl;

    private String restaurantChoiceId;

    public UserInfo(String name, String photoUrl, String restaurantChoiceId) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.restaurantChoiceId = restaurantChoiceId;
    }

    public UserInfo(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getRestaurantChoiceId() {
        return restaurantChoiceId;
    }

    public void setRestaurantChoiceId(String restaurantChoiceId) {
        this.restaurantChoiceId = restaurantChoiceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(name, userInfo.name) &&
                Objects.equals(photoUrl, userInfo.photoUrl) &&
                Objects.equals(restaurantChoiceId, userInfo.restaurantChoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, photoUrl, restaurantChoiceId);
    }
}
