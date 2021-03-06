package fr.feepin.go4lunch.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.databinding.ItemRestaurantBinding;

public class ListItemAdapter extends ListAdapter<ListViewState.ListItemState, ListItemAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<ListViewState.ListItemState> DIFF_CALLBACK = new DiffUtil.ItemCallback<ListViewState.ListItemState>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListViewState.ListItemState oldItem, @NonNull ListViewState.ListItemState newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListViewState.ListItemState oldItem, @NonNull ListViewState.ListItemState newItem) {
            return oldItem.equals(newItem);
        }
    };

    private final OnRestaurantClickListener listener;

    public ListItemAdapter(OnRestaurantClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemRestaurantBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = ItemRestaurantBinding.bind(itemView);
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(getItem(getAdapterPosition()));
                }
            });
        }

        public void bind(ListViewState.ListItemState listItemState) {

            LayoutInflater layoutInflater = LayoutInflater.from(itemView.getContext());

            Glide.with(itemView.getContext())
                    .load(listItemState.getPhoto())
                    .error(R.color.gray)
                    .transform(new CenterCrop(), new RoundedCorners(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.item_restaurant_photo_radius)))
                    .into(binding.ivRestaurantPhoto);

            if (listItemState.isRestaurantOpened() != null) {
                binding.tvClosingInfo.setText(listItemState.isRestaurantOpened() ? R.string.text_restaurant_opened : R.string.text_restaurant_closed);
            } else {
                binding.tvClosingInfo.setText(R.string.text_restaurant_opening_unknown);
            }

            binding.tvRestaurantName.setText(listItemState.getRestaurantName());
            binding.tvPersonCount.setText(String.valueOf(listItemState.getWorkmatesJoining()));
            binding.tvTypeAddress.setText(listItemState.getRestaurantAddress());
            binding.tvRestaurantDistance.setText(itemView.getContext().getString(R.string.distance_meters, listItemState.getDistance()));

            binding.linearLayoutRating.removeAllViews();
            for (int i = 0; i < listItemState.getRating(); i++) {
                layoutInflater.inflate(R.layout.item_restaurant_rating_star, binding.linearLayoutRating, true);
            }
        }
    }

    public interface OnRestaurantClickListener {
        void onClick(ListViewState.ListItemState listItemState);
    }
}
