package fr.feepin.go4lunch.sorts;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.feepin.go4lunch.ui.workmates.WorkmateState;

@RunWith(JUnit4.class)
public class WorkmateStateSortTest {

    @Test
    public void sortWorkmatesNotJoining() {
        List<WorkmateState> workmatesToSort = Arrays.asList(
                new WorkmateState("", "", "", ""),
                new WorkmateState("123", "", "", ""),
                new WorkmateState("455", "", "", ""),
                new WorkmateState("", "", "", ""),
                new WorkmateState("", "", "", ""),
                new WorkmateState("", "", "", ""),
                new WorkmateState("4543", "", "", "")
        );

        List<WorkmateState> expectedWorkmates = Arrays.asList(
                new WorkmateState("123", "", "", ""),
                new WorkmateState("455", "", "", ""),
                new WorkmateState("4543", "", "", ""),
                new WorkmateState("", "", "", ""),
                new WorkmateState("", "", "", ""),
                new WorkmateState("", "", "", ""),
                new WorkmateState("", "", "", "")
        );

        Collections.sort(workmatesToSort, WorkmateState.RESTAURANT_NOT_CHOSEN_COMPARATOR);

        Assert.assertEquals(expectedWorkmates, workmatesToSort);
    }

}
