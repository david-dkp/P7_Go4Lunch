package fr.feepin.go4lunch.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Arrays;

import dagger.hilt.android.AndroidEntryPoint;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.MainViewModel;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.databinding.FragmentMapViewBinding;
import fr.feepin.go4lunch.ui.restaurant.RestaurantActivity;
import fr.feepin.go4lunch.utils.LatLngUtils;
import fr.feepin.go4lunch.utils.PermissionUtils;

import static android.app.Activity.RESULT_OK;

@AndroidEntryPoint
public class MapViewFragment extends Fragment {

    public static final String TAG = "MapViewFragment";

    private MapViewViewModel viewModel;

    private FragmentMapViewBinding binding;

    private GoogleMap googleMap;
    private ClusterManager<RestaurantItem> clusterManager;
    private Marker userLocationMarker;

    private ActivityResultLauncher<Intent> autocompleteActivityLauncher;
    private ActivityResultLauncher<Intent> navigateToLocationSettingsLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapViewBinding.inflate(inflater);
        binding.mapView.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(this).get(MapViewViewModel.class);

        setupLaunchers();

        binding.mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            configGoogleMap();
            setupObservers();
        });

        binding.fabMyLocation.setOnClickListener(v -> {
            viewModel.askPosition();
        });

        binding.btnEnableLocation.setOnClickListener(v -> {
            navigateToLocationSettingsLauncher.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        });

        return binding.getRoot();
    }

    private void setupLaunchers() {

        autocompleteActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK) return;
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    viewModel.onPlaceReceive(place);
                    animateCameraToPosition(place.getLatLng(), Constants.MAPS_RESTAURANT_ZOOM_LEVEL);
                }
        );

        navigateToLocationSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    viewModel.askPosition();
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        viewModel.askPosition();
                    } else {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            PermissionUtils.showRationalDialog(getActivity(), R.string.rational_location_permission, (dialog, which) -> {
                                requestLocationPermission();
                            });
                        }
                    }
                }
        );
    }

    private void setupObservers() {
        setupPosition();
        setupRestaurantsState();
    }

    private void setupPosition() {
        viewModel.getPosition().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Success) {
                binding.progressBar.hide();
                toggleLocationError(true);
                userLocationMarker.setVisible(true);
                userLocationMarker.setPosition(resource.getData());
                animateCameraToPosition(resource.getData(), Constants.MAPS_MY_LOCATION_ZOOM_LEVEL);
            } else if (resource instanceof Resource.Error) {
                binding.progressBar.hide();
                if (resource.getMessage().equals(Constants.NO_LOCATION_PERMISSION_MESSAGE)) {
                    requestLocationPermission();
                } else if (resource.getMessage().equals(Constants.LOCATION_DISABLED_MESSAGE)) {
                    if (resource.getData() != null) {
                        animateCameraToPosition(resource.getData(), Constants.MAPS_MY_LOCATION_ZOOM_LEVEL);
                    }
                    toggleLocationError(false);
                }
            } else {
                toggleLocationError(true);
                binding.progressBar.show();
            }
        });
    }

    private void setupRestaurantsState() {

        viewModel.getRestaurantStates().observe(getViewLifecycleOwner(), restaurantStates -> {
            clusterManager.clearItems();

            for (RestaurantState restaurantState : restaurantStates) {
                clusterManager.addItem(new RestaurantItem(restaurantState.getPosition(), restaurantState.getId(), restaurantState.isJoined()));
            }

            clusterManager.cluster();
        });
    }

    private Bitmap getBitmapFromVectorDrawable(int vectorId) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), vectorId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void toggleLocationError(boolean isLocationEnabled) {
        TransitionManager.beginDelayedTransition(binding.getRoot());
        binding.clLocationErrorContainer.setVisibility(isLocationEnabled ? View.INVISIBLE : View.VISIBLE);
    }

    private void configGoogleMap() {

        clusterManager = new ClusterManager<>(getContext(), googleMap);
        clusterManager.setRenderer(
                new RestaurantRenderer(
                        getContext(),
                        googleMap,
                        clusterManager,
                        getBitmapFromVectorDrawable(R.drawable.ic_restaurant_pin_not_joined),
                        getBitmapFromVectorDrawable(R.drawable.ic_restaurant_pin_joined)
                )
        );

        clusterManager.setOnClusterItemClickListener(item -> {
            RestaurantActivity.navigate(getContext(), item.getRestaurantId());
            return true;
        });

        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(R.drawable.ic_user_location)));
        markerOptions.visible(false);
        markerOptions.position(new LatLng(0, 0));
        markerOptions.flat(true);
        markerOptions.anchor(0.5f, 0.5f);

        userLocationMarker = googleMap.addMarker(markerOptions);

        setMapStyle();
    }

    private void animateCameraToPosition(LatLng latLng, float zoom) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void setMapStyle() {

        int nightMode = AppCompatDelegate.getDefaultNightMode();
        boolean isNightMode;

        if (nightMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            isNightMode = AppCompatDelegate.MODE_NIGHT_YES == nightMode;
        } else {
            int uiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            isNightMode = uiMode == Configuration.UI_MODE_NIGHT_YES;
        }

        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                getContext(),
                isNightMode ? R.raw.map_style_dark : R.raw.map_style
        );
        googleMap.setMapStyle(mapStyleOptions);
    }

    private void requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.search) {
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY,
                    Arrays.asList(
                            Place.Field.ID,
                            Place.Field.LAT_LNG,
                            Place.Field.NAME,
                            Place.Field.ADDRESS,
                            Place.Field.OPENING_HOURS,
                            Place.Field.PHOTO_METADATAS
                    )
            )
                    .setCountry("FR")
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setLocationRestriction(RectangularBounds.newInstance(LatLngUtils.toBounds(
                            viewModel.getPosition().getValue().getData(),
                            Constants.PREDICTION_SEARCH_RADIUS
                    )))
                    .build(getContext());
            autocompleteActivityLauncher.launch(intent);
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }
}
