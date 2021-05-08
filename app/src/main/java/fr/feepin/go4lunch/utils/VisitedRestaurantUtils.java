package fr.feepin.go4lunch.utils;

import java.util.List;

import fr.feepin.go4lunch.data.user.models.VisitedRestaurant;

public class VisitedRestaurantUtils {

    public static int calculateRating(List<VisitedRestaurant> visitedRestaurantList) {
        int likes = 0;

        for (VisitedRestaurant visitedRestaurant : visitedRestaurantList) {
            if (visitedRestaurant.isLiked()) likes++;
        }

        int rating = Math.round(((float) likes / (float) visitedRestaurantList.size()) * 3f);

        return rating;
    }

}
