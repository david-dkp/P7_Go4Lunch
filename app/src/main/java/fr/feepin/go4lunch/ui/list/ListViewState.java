package fr.feepin.go4lunch.ui.list;

import java.util.List;

public class ListViewState {

    private List<ListItemState> listItemStates;

    private boolean isSortable;

    private boolean scrollToFirst;

    public ListViewState(List<ListItemState> listItemStates, boolean isSortable, boolean scrollToFirst) {
        this.listItemStates = listItemStates;
        this.isSortable = isSortable;
        this.scrollToFirst = scrollToFirst;
    }

    public List<ListItemState> getListItemStates() {
        return listItemStates;
    }

    public void setListItemStates(List<ListItemState> listItemStates) {
        this.listItemStates = listItemStates;
    }

    public boolean isSortable() {
        return isSortable;
    }

    public void setSortable(boolean sortable) {
        isSortable = sortable;
    }

    public boolean isScrollToFirst() {
        return scrollToFirst;
    }

    public void setScrollToFirst(boolean scrollToFirst) {
        this.scrollToFirst = scrollToFirst;
    }
}
