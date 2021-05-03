package fr.feepin.go4lunch.ui.list;

import java.util.Comparator;

public enum SortMethod {
    DISTANCE(0, ListItemState.DISTANCE_COMPARATOR),
    RATING(1, ListItemState.RATING_COMPARATOR),
    WORKMATES(2, ListItemState.WORKMATES_COMPARATOR);

    private final int position;
    private final Comparator<ListItemState> comparator;

    SortMethod(int position, Comparator<ListItemState> comparator) {
        this.position = position;
        this.comparator = comparator;
    }

    public int getPosition() {
        return position;
    }

    public Comparator<ListItemState> getComparator() {
        return comparator;
    }
}
