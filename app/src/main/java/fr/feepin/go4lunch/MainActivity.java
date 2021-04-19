package fr.feepin.go4lunch;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import fr.feepin.go4lunch.databinding.ActivityMainBinding;
import fr.feepin.go4lunch.ui.list.ListViewFragment;
import fr.feepin.go4lunch.ui.map.MapViewFragment;
import fr.feepin.go4lunch.ui.restaurant.RestaurantActivity;
import fr.feepin.go4lunch.ui.settings.SettingsActivity;
import fr.feepin.go4lunch.ui.workmates.WorkmatesFragment;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;

    private MapViewFragment mapViewFragment;
    private ListViewFragment listViewFragment;
    private WorkmatesFragment workmatesFragment;

    private Fragment currentBotNavFragment;
    private Fragment currentShowingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapViewFragment = new MapViewFragment();
        listViewFragment = new ListViewFragment();
        workmatesFragment = new WorkmatesFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, listViewFragment).hide(listViewFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, workmatesFragment).hide(workmatesFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, mapViewFragment).commit();

        currentBotNavFragment = mapViewFragment;

        setSupportActionBar(binding.toolbar);
        setupBottomNavigation();
        setupDrawerLayout();
    }

    private void setupBottomNavigation() {
        binding.botNav.setOnNavigationItemSelectedListener((item -> {

            switch(item.getItemId()) {
                case R.id.itemMapViewFragment:
                    getSupportFragmentManager().beginTransaction().hide(currentBotNavFragment).show(mapViewFragment).commit();
                    currentBotNavFragment = mapViewFragment;
                    break;
                case R.id.itemListViewFragment:
                    getSupportFragmentManager().beginTransaction().hide(currentBotNavFragment).show(listViewFragment).commit();
                    currentBotNavFragment = listViewFragment;
                    break;
                case R.id.itemWorkmatesFragment:
                    getSupportFragmentManager().beginTransaction().hide(currentBotNavFragment).show(workmatesFragment).commit();
                    currentBotNavFragment = workmatesFragment;
                    break;
            }

            currentShowingFragment = currentBotNavFragment;

            return true;
        }));
    }

    private void setupDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.getRoot(),
                binding.toolbar,
                R.string.desc_open_drawer,
                R.string.desc_close_drawer
        );
        binding.getRoot().addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(item -> {

            switch(item.getItemId()) {
                case R.id.itemYourLunch:
                    RestaurantActivity.navigate(this, 0);
                    break;
                case R.id.itemSettings:
                    SettingsActivity.navigate(this);
                    break;
                case R.id.itemLogout:
                    break;
            }

            return true;
        });
    }

    @Override
    public void onBackPressed() {

        if (binding.getRoot().isDrawerOpen(GravityCompat.START)) {
            binding.getRoot().closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}