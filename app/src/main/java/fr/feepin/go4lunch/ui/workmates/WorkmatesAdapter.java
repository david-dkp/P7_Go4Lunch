package fr.feepin.go4lunch.ui.workmates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import fr.feepin.go4lunch.R;

public class WorkmatesAdapter extends ListAdapter<WorkmateState, WorkmatesAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<WorkmateState> DIFF_CALLBACK = new DiffUtil.ItemCallback<WorkmateState>() {
        @Override
        public boolean areItemsTheSame(@NonNull WorkmateState oldItem, @NonNull WorkmateState newItem) {
            return oldItem.getRestaurantId().equals(newItem.getRestaurantId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull WorkmateState oldItem, @NonNull WorkmateState newItem) {
            return oldItem.equals(newItem);
        }
    };

    protected WorkmatesAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public int getItemViewType(int position) {
        WorkmateState workmateState = getItem(position);
        if (workmateState.getRestaurantName() == null) {
            return NotDecidedViewHolder.VIEW_TYPE;
        } else {
            return DecidedViewHolder.VIEW_TYPE;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == DecidedViewHolder.VIEW_TYPE) {
            return new DecidedViewHolder(layoutInflater.inflate(R.layout.item_workmate, parent, false));
        } else {
            return new NotDecidedViewHolder(layoutInflater.inflate(R.layout.item_workmate_not_decided, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        WorkmateState workmateState = getItem(position);

        View view = holder.itemView;
        TextView tvUserInfo = view.findViewById(R.id.tvWorkmateInfo);
        ImageView ivPhoto = view.findViewById(R.id.ivWorkmatePhoto);

        Glide.with(context)
                .load(workmateState.getPhotoUrl())
                .transform(new CircleCrop())
                .into(ivPhoto);

        if (holder instanceof DecidedViewHolder) {
            tvUserInfo.setText(context.getString(R.string.text_workmate_decided, workmateState.getName(), workmateState.getRestaurantName()));
        } else if (holder instanceof NotDecidedViewHolder) {
            tvUserInfo.setText(context.getString(R.string.text_workmate_not_decided, workmateState.getName()));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class DecidedViewHolder extends ViewHolder{

        public final static int VIEW_TYPE = 1;

        public DecidedViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class NotDecidedViewHolder extends ViewHolder{

        public final static int VIEW_TYPE = 2;

        public NotDecidedViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
