package fr.feepin.go4lunch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;

import fr.feepin.go4lunch.databinding.ActivityMainBinding;
import fr.feepin.go4lunch.databinding.HeaderNavBinding;
import fr.feepin.go4lunch.ui.list.ListViewFragment;
import fr.feepin.go4lunch.ui.login.LoginActivity;
import fr.feepin.go4lunch.ui.map.MapViewFragment;
import fr.feepin.go4lunch.ui.restaurant.RestaurantActivity;
import fr.feepin.go4lunch.ui.settings.SettingsActivity;
import fr.feepin.go4lunch.ui.workmates.WorkmatesFragment;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainActivity extends AppCompatActivity {

    private final static String LATEST_FRAGMENT_TAG_KEY = "LATEST_FRAGMENT_TAG_KEY";

    private ActivityMainBinding binding;
    private HeaderNavBinding headerNavBinding;

    private MapViewFragment mapViewFragment;
    private ListViewFragment listViewFragment;
    private WorkmatesFragment workmatesFragment;

    private Fragment currentBotNavFragment;

    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(this, BuildConfig.MAPS_API_KEY);

        //Bindings
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        headerNavBinding = HeaderNavBinding.bind(binding.navView.getHeaderView(0));
        setContentView(binding.getRoot());

        //Load header image
        Glide.with(this)
                .load(R.raw.dinner)
                .transform(new BlurTransformation(getResources().getInteger(R.integer.blur_radius)))
                .into(headerNavBinding.ivHeader);

        mainViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(MainViewModel.class);

        setupObservers();
        setupToolbar();

        setupBottomNavigation();

        if (savedInstanceState == null) {
            setupFragments();
        }
        setupDrawerLayout();
    }

    private void setupObservers() {
        mainViewModel.getCurrentUser().observe(this, firebaseUser -> {
            if (firebaseUser == null) {
                LoginActivity.navigate(this);
                finish();
            } else {
                Glide.with(this)
                        .load(firebaseUser.getPhotoUrl())
                        .circleCrop()
                        .into(headerNavBinding.ivUserPhoto);

                headerNavBinding.tvUserName.setText(firebaseUser.getDisplayName());
                headerNavBinding.tvUserEmail.setText(firebaseUser.getEmail());
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void setupFragments() {
        mapViewFragment = new MapViewFragment();
        listViewFragment = new ListViewFragment();
        workmatesFragment = new WorkmatesFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, listViewFragment, ListViewFragment.TAG).hide(listViewFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, workmatesFragment, WorkmatesFragment.TAG).hide(workmatesFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, mapViewFragment, MapViewFragment.TAG).commit();

        getSupportActionBar().setTitle(R.string.title_map_view);
        currentBotNavFragment = mapViewFragment;
    }

    private void setupBottomNavigation() {
        binding.botNav.setOnNavigationItemSelectedListener((item -> {

            if (item.getItemId() == binding.botNav.getSelectedItemId()) return true;

            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.itemMapViewFragment:
                    selectedFragment = mapViewFragment;
                    break;
                case R.id.itemListViewFragment:
                    selectedFragment = listViewFragment;
                    break;
                case R.id.itemWorkmatesFragment:
                    selectedFragment = workmatesFragment;
                    break;
            }

            getSupportActionBar().setTitle(item.getTitle());
            getSupportFragmentManager().beginTransaction().hide(currentBotNavFragment).show(selectedFragment).commit();
            currentBotNavFragment = selectedFragment;
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

            switch (item.getItemId()) {
                case R.id.itemYourLunch:
                    RestaurantActivity.navigate(this, 0);
                    break;
                case R.id.itemSettings:
                    SettingsActivity.navigate(this);
                    break;
                case R.id.itemLogout:
                    mainViewModel.signOut();
                    break;
            }

            binding.getRoot().closeDrawer(GravityCompat.START);

            return true;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LATEST_FRAGMENT_TAG_KEY, currentBotNavFragment.getTag());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mapViewFragment = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
        listViewFragment = (ListViewFragment) getSupportFragmentManager().findFragmentByTag(ListViewFragment.TAG);
        workmatesFragment = (WorkmatesFragment) getSupportFragmentManager().findFragmentByTag(WorkmatesFragment.TAG);

        currentBotNavFragment = getSupportFragmentManager().findFragmentByTag(savedInstanceState.getString(LATEST_FRAGMENT_TAG_KEY));

        int titleId;

        if (currentBotNavFragment == mapViewFragment) {
            titleId = R.string.title_map_view;
        } else if (currentBotNavFragment == listViewFragment) {
            titleId = R.string.title_list_view;
        } else {
            titleId = R.string.title_workmates;
        }

        getSupportActionBar().setTitle(titleId);

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