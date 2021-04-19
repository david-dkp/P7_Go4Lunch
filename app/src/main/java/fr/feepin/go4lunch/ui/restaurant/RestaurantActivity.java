package fr.feepin.go4lunch.ui.restaurant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import fr.feepin.go4lunch.databinding.ActivityRestaurantBinding;

public class RestaurantActivity extends AppCompatActivity {

    public final static String EXTRA_RESTAURANT_ID = "EXTRA_RESTAURANT_ID";

    private ActivityRestaurantBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static void navigate(Context context, int restaurantId) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_ID, restaurantId);
        context.startActivity(intent);
    }

}
