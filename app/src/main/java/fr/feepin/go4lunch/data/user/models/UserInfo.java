package fr.feepin.go4lunch.data.user.models;

public class UserInfo {

    private String name;

    private String photoUrl;

    private String restaurantChoiceId;

    public UserInfo(String name, String photoUrl, String restaurantChoiceId) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.restaurantChoiceId = restaurantChoiceId;
    }

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
}
