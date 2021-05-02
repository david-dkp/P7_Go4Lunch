package fr.feepin.go4lunch.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.databinding.ItemRestaurantBinding;

public class ListItemAdapter extends ListAdapter<ListItemState, ListItemAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<ListItemState> DIFF_CALLBACK = new DiffUtil.ItemCallback<ListItemState>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItemState oldItem, @NonNull ListItemState newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItemState oldItem, @NonNull ListItemState newItem) {
            return oldItem.equals(newItem);
        }
    };

    public ListItemAdapter() {
        super(DIFF_CALLBACK);
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

        private ItemRestaurantBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = ItemRestaurantBinding.bind(itemView);
        }

        public void bind(ListItemState listItemState) {

            LayoutInflater layoutInflater = LayoutInflater.from(itemView.getContext());

            Glide.with(itemView.getContext())
                    .load(listItemState.getPhoto())
                    .centerCrop()
                    .into(binding.ivRestaurantPhoto);

            if (listItemState.isRestaurantOpened() != null) {
                binding.tvClosingInfo.setText(listItemState.isRestaurantOpened() ? R.string.text_restaurant_opened : R.string.text_restaurant_closed);
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
}