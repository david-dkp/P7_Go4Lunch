package fr.feepin.go4lunch.ui.restaurant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.Place;

import dagger.hilt.android.AndroidEntryPoint;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.databinding.ActivityRestaurantBinding;

@AndroidEntryPoint
public class RestaurantActivity extends AppCompatActivity {

    private ActivityRestaurantBinding binding;

    private RestaurantViewModel restaurantViewModel;

    private UsersJoiningAdapter usersJoiningAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);

        String restaurantId = getIntent().getStringExtra(Constants.EXTRA_RESTAURANT_ID);

        restaurantViewModel.setup(restaurantId);

        setupWorkmatesList();

        setupObservers();
    }

    private void setupWorkmatesList() {
        usersJoiningAdapter = new UsersJoiningAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.rvWorkmates.setAdapter(usersJoiningAdapter);
        binding.rvWorkmates.setLayoutManager(linearLayoutManager);
    }

    private void setupObservers() {
        restaurantViewModel.getPlace().observe(this, place -> {
            binding.tvRestaurantName.setText(place.getName());
            binding.tvRestaurantAddress.setText(place.getAddress());
        });

        restaurantViewModel.getRestaurantPhoto().observe(this, photo -> {
            Glide.with(this)
                    .load(photo)
                    .centerCrop()
                    .into(binding.ivRestaurantPhoto);
        });

        restaurantViewModel.getUsersInfo().observe(this, usersInfo -> {
            usersJoiningAdapter.submitList(usersInfo);
        });

        restaurantViewModel.getRating().observe(this, rating -> {
            binding.linearLayoutRating.removeAllViews();
            Log.d("debug", rating.toString());
            for (int i = 0; i < rating; i++) {
                getLayoutInflater().inflate(R.layout.item_restaurant_rating_star, binding.linearLayoutRating, true);
            }

        });
    }

    public static void navigate(Context context, String restaurantId) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(Constants.EXTRA_RESTAURANT_ID, restaurantId);
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
