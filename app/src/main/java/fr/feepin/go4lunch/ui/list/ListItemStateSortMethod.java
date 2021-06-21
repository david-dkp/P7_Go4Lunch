package fr.feepin.go4lunch.ui.list;

import java.util.Comparator;

public enum ListItemStateSortMethod {
    DISTANCE(0, ListViewState.ListItemState.DISTANCE_COMPARATOR),
    RATING(1, ListViewState.ListItemState.RATING_COMPARATOR),
    WORKMATES(2, ListViewState.ListItemState.WORKMATES_COMPARATOR);

    private final int position;
    private final Comparator<ListViewState.ListItemState> comparator;

    ListItemStateSortMethod(int position, Comparator<ListViewState.ListItemState> comparator) {
        this.position = position;
        this.comparator = comparator;
    }

    public int getPosition() {
        return position;
    }

    public Comparator<ListViewState.ListItemState> getComparator() {
        return comparator;
    }
}
