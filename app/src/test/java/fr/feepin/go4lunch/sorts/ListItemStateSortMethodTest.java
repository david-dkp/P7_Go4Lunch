package fr.feepin.go4lunch.sorts;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import fr.feepin.go4lunch.ui.list.ListViewState;

@RunWith(JUnit4.class)
public class ListItemStateSortMethodTest {

    @Test
    public void sortByDistance() {
        List<ListViewState.ListItemState> listToSort = Arrays.asList(
                new ListViewState.ListItemState(null, null, null, 324, 0, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 145, 0, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 678, 0, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 432, 0, 0, null, null)
        );

        List<ListViewState.ListItemState> expectedSortedList = Arrays.asList(
                new ListViewState.ListItemState(null, null, null, 145, 0, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 324, 0, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 432, 0, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 678, 0, 0, null, null)
        );

        listToSort.sort(ListViewState.ListItemState.DISTANCE_COMPARATOR);

        Assert.assertArrayEquals(listToSort.toArray(), expectedSortedList.toArray());
    }

    @Test
    public void sortByRating() {
        List<ListViewState.ListItemState> listToSort = Arrays.asList(
                new ListViewState.ListItemState(null, null, null, 0, 0, 2, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 0, 3, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 0, 1, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 0, 3, null, null)
        );

        List<ListViewState.ListItemState> expectedSortedList = Arrays.asList(
                new ListViewState.ListItemState(null, null, null, 0, 0, 3, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 0, 3, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 0, 2, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 0, 1, null, null)
        );

        listToSort.sort(ListViewState.ListItemState.RATING_COMPARATOR);

        Assert.assertArrayEquals(listToSort.toArray(), expectedSortedList.toArray());
    }

    @Test
    public void sortByWorkmates() {
        List<ListViewState.ListItemState> listToSort = Arrays.asList(
                new ListViewState.ListItemState(null, null, null, 0, 15, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 7, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 25, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 17, 0, null, null)
        );

        List<ListViewState.ListItemState> expectedSortedList = Arrays.asList(
                new ListViewState.ListItemState(null, null, null, 0, 25, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 17, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 15, 0, null, null),
                new ListViewState.ListItemState(null, null, null, 0, 7, 0, null, null)
        );

        listToSort.sort(ListViewState.ListItemState.WORKMATES_COMPARATOR);

        Assert.assertArrayEquals(listToSort.toArray(), expectedSortedList.toArray());
    }

}
