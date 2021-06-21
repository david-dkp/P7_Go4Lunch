package fr.feepin.go4lunch.sorts;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.feepin.go4lunch.data.models.domain.UserInfo;
import fr.feepin.go4lunch.ui.workmates.WorkmateState;

@RunWith(JUnit4.class)
public class UserInfoSortTest {

    @Test
    public void sortWorkmatesNotJoining() {
        List<UserInfo> userInfosToSort = Arrays.asList(
                new UserInfo("Michelle", "", "1541"),
                new UserInfo("Cristelle", "", "4527"),
                new UserInfo("David", "", ""),
                new UserInfo("Valentin", "", "4554"),
                new UserInfo("Antoine", "", ""),
                new UserInfo("Rose", "", "1541"),
                new UserInfo("Saphia", "", "")
        );

        List<UserInfo> expectedUserInfos = Arrays.asList(
                new UserInfo("Michelle", "", "1541"),
                new UserInfo("Cristelle", "", "4527"),
                new UserInfo("Valentin", "", "4554"),
                new UserInfo("Rose", "", "1541"),
                new UserInfo("David", "", ""),
                new UserInfo("Antoine", "", ""),
                new UserInfo("Saphia", "", "")
        );

        Collections.sort(userInfosToSort, UserInfo.RESTAURANT_CHOSEN_FIRST_COMPARATOR);

        Assert.assertEquals(expectedUserInfos, userInfosToSort);
    }

}
