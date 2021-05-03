package fr.feepin.go4lunch.ui.workmates;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class WorkmatesItemDecoration extends DividerItemDecoration {

    private final int left;

    public WorkmatesItemDecoration(Context context, int orientation, int left) {
        super(context, orientation);
        this.left = left;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
    }
}
