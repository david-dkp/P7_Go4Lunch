package fr.feepin.go4lunch.ui.list;

import java.util.List;

public class ListViewState {

    private List<ListItemState> listItemStates;

    private boolean isSortable;

    public ListViewState(List<ListItemState> listItemStates, boolean isSortable) {
        this.listItemStates = listItemStates;
        this.isSortable = isSortable;
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
}
