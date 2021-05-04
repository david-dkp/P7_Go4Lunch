package fr.feepin.go4lunch.ui.restaurant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;

import dagger.hilt.android.AndroidEntryPoint;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.databinding.ActivityRestaurantBinding;

@AndroidEntryPoint
public class RestaurantActivity extends AppCompatActivity {

    private ActivityRestaurantBinding binding;

    private RestaurantViewModel restaurantViewModel;

    private UsersJoiningAdapter usersJoiningAdapter;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Setup toolbar
        ViewGroup.LayoutParams layoutParams = binding.vStatusZone.getLayoutParams();
        layoutParams.height = getStatusBarHeight();
        binding.vStatusZone.setLayoutParams(layoutParams);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);

        String restaurantId = getIntent().getStringExtra(Constants.EXTRA_RESTAURANT_ID);
        AutocompleteSessionToken sessionToken = getIntent().getParcelableExtra(Constants.EXTRA_AUTOCOMPLETE_TOKEN);

        restaurantViewModel.setup(restaurantId, sessionToken);

        //Trigger name and address scroll when too long
        binding.tvRestaurantName.setSelected(true);
        binding.tvRestaurantAddress.setSelected(true);

        //Setup options button listeners, call/like/website
        binding.tvCall.setOnClickListener(v -> {
            Place place = restaurantViewModel.getPlace().getValue();
            if (place == null) return;

            if (place.getPhoneNumber() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("tel:" + place.getPhoneNumber()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Yeah, no", Toast.LENGTH_LONG).show();
            }
        });

        binding.tvLike.setOnClickListener(v -> {
            restaurantViewModel.rateRestaurant();
        });

        binding.tvWebsite.setOnClickListener(v -> {
            Place place = restaurantViewModel.getPlace().getValue();
            if (place == null) return;

            if (place.getWebsiteUri() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(place.getWebsiteUri());
                startActivity(intent);
            }
        });

        //User joining the restaurant
        binding.fabJoinRestaurant.setOnClickListener(v -> {
            restaurantViewModel.joinOrLeaveRestaurant();
        });

        setupWorkmatesList();
        setupObservers();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void setupWorkmatesList() {
        usersJoiningAdapter = new UsersJoiningAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.rvWorkmates.setAdapter(usersJoiningAdapter);
        binding.rvWorkmates.setLayoutManager(linearLayoutManager);
    }

    private void setupObservers() {

        //Restaurant infos
        restaurantViewModel.getPlace().observe(this, place -> {
            binding.tvRestaurantName.setText(place.getName());
            binding.tvRestaurantAddress.setText(place.getAddress());
            if (place.getPhoneNumber() == null) {
                binding.tvCall.setEnabled(false);
            }

            if (place.getWebsiteUri() == null) {
                binding.tvWebsite.setEnabled(false);
            }

        });

        //Restaurant photo
        restaurantViewModel.getRestaurantPhoto().observe(this, photo -> {
            if (photo instanceof Resource.Loading) {
                binding.photoProgressBar.show();
            } else if (photo instanceof Resource.Success) {
                binding.photoProgressBar.hide();
                Glide
                        .with(this)
                        .load(photo.getData())
                        .error(R.color.gray)
                        .centerCrop()
                        .into(binding.ivRestaurantPhoto);

            } else {
                Glide
                        .with(this)
                        .load(R.color.gray)
                        .into(binding.ivRestaurantPhoto);
            }
        });

        //Workmates joining
        restaurantViewModel.getUsersInfo().observe(this, usersInfo -> {
            usersJoiningAdapter.submitList(usersInfo);
        });

        //Rating
        restaurantViewModel.getRating().observe(this, rating -> {
            binding.linearLayoutRating.removeAllViews();
            for (int i = 0; i < rating; i++) {
                getLayoutInflater().inflate(R.layout.item_restaurant_rating_star, binding.linearLayoutRating, true);
            }
        });

        //Liking
        restaurantViewModel.isLiked().observe(this, liked -> {
            binding.tvLike.setActivated(liked);
        });

        restaurantViewModel.hasAlreadyVisited().observe(this, hasVisited -> {
            binding.tvLike.setEnabled(hasVisited);
        });

        //Joining info
        restaurantViewModel.isJoined().observe(this, isJoined -> {
            binding.fabJoinRestaurant.setActivated(isJoined);
        });

        restaurantViewModel.canJoin().observe(this, canJoin -> {
            binding.fabJoinRestaurant.setEnabled(canJoin);
        });
    }

    public static void navigate(Context context, String restaurantId) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(Constants.EXTRA_RESTAURANT_ID, restaurantId);
        context.startActivity(intent);
    }

    public static void navigate(Context context, String restaurantId, AutocompleteSessionToken autocompleteSessionToken) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(Constants.EXTRA_RESTAURANT_ID, restaurantId);

        if (autocompleteSessionToken != null) {
            intent.putExtra(Constants.EXTRA_AUTOCOMPLETE_TOKEN, autocompleteSessionToken);
        }
        context.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
