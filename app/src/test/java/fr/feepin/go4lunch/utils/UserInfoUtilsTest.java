package fr.feepin.go4lunch.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import fr.feepin.go4lunch.data.user.models.UserInfo;

@RunWith(JUnit4.class)
public class UserInfoUtilsTest {

    @Test
    public void getUsersJoiningByRestaurantId() {
        List<UserInfo> userInfoList = Arrays.asList(
                new UserInfo("Daviod", "", "8"),
                new UserInfo("Daviod", "", "2"),
                new UserInfo("Daviod", "", "4"),
                new UserInfo("Daviod", "", "8"),
                new UserInfo("Daviod", "", "1"),
                new UserInfo("Daviod", "", "24"),
                new UserInfo("Daviod", "", "8")
        );

        int usersJoining = UserInfoUtils.calculateUsersJoiningByRestaurantId(userInfoList, "8");

        Assert.assertEquals(usersJoining, 3);
    }
}
