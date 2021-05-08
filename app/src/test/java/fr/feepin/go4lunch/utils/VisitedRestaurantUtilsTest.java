package fr.feepin.go4lunch.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import fr.feepin.go4lunch.data.user.models.VisitedRestaurant;

@RunWith(JUnit4.class)
public class VisitedRestaurantUtilsTest {

    @Test
    public void calculateRating() {
        List<VisitedRestaurant> visitedRestaurantList = Arrays.asList(
                new VisitedRestaurant("1", true),
                new VisitedRestaurant("1", true),
                new VisitedRestaurant("1", true),
                new VisitedRestaurant("1", false),
                new VisitedRestaurant("1", true),
                new VisitedRestaurant("1", false),
                new VisitedRestaurant("1", false),
                new VisitedRestaurant("1", true),
                new VisitedRestaurant("1", false)
                );

        int rating = VisitedRestaurantUtils.calculateRating(visitedRestaurantList);

        Assert.assertEquals(rating, 2);
    }
}
