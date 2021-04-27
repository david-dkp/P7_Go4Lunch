package fr.feepin.go4lunch.ui.restaurant;

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
import fr.feepin.go4lunch.data.user.models.UserInfo;

public class UsersJoiningAdapter extends ListAdapter<UserInfo, UsersJoiningAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<UserInfo> diffCallback = new DiffUtil.ItemCallback<UserInfo>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserInfo oldItem, @NonNull UserInfo newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserInfo oldItem, @NonNull UserInfo newItem) {
            return oldItem.hashCode() == newItem.hashCode();
        }
    };

    public UsersJoiningAdapter() {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(layoutInflater.inflate(R.layout.item_workmate_joining, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserInfo userInfo = getItem(position);

        TextView textView = holder.itemView.findViewById(R.id.tvUserJoining);

        Context context = textView.getContext();

        textView.setText(context.getString(R.string.text_user_joining, userInfo.getName()));

        ImageView imageView = holder.itemView.findViewById(R.id.ivWorkmatePhoto);
        Glide
                .with(context)
                .load(userInfo.getPhotoUrl())
                .transform(new CircleCrop())
                .into(imageView);

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
