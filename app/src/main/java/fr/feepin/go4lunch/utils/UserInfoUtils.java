package fr.feepin.go4lunch.utils;

import java.util.List;

import fr.feepin.go4lunch.data.user.models.UserInfo;

public class UserInfoUtils {

    public static int calculateUsersJoiningByRestaurantId(List<UserInfo> userInfoList, String restaurantId) {
        int usersJoining = 0;

        for (UserInfo userInfo : userInfoList) {
            if (userInfo.getRestaurantChoiceId().equals(restaurantId)) {
                usersJoining++;
            }
        }

        return usersJoining;
    }

}
