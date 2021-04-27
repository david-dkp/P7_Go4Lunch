package fr.feepin.go4lunch.ui.map;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;


import dagger.hilt.android.AndroidEntryPoint;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.MainViewModel;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.contracts.LocationSettingsContract;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.databinding.FragmentMapViewBinding;
import fr.feepin.go4lunch.ui.restaurant.RestaurantActivity;
import fr.feepin.go4lunch.utils.PermissionUtils;

@AndroidEntryPoint
public class MapViewFragment extends Fragment {

    public static final String TAG = "MAP_VIEW_TAG";

    private FragmentMapViewBinding binding;

    private GoogleMap googleMap;

    private MainViewModel mainViewModel;

    private ClusterManager<RestaurantItem> clusterManager;

    private final ActivityResultLauncher<Void> navigateToLocationSettingsLauncher = registerForActivityResult(
            new LocationSettingsContract(),
            result -> {
                mainViewModel.askLocation();
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    mainViewModel.askLocation();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        PermissionUtils.showRationalDialog(getActivity(), R.string.rational_location_permission, (dialog, which) -> {
                            requestLocationPermission();
                        });
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapViewBinding.inflate(inflater);
        binding.mapView.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding.mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            configGoogleMap();
            setupObservers();
        });

        binding.fabMyLocation.setOnClickListener(v -> {
            mainViewModel.askLocation();
        });

        binding.btnEnableLocation.setOnClickListener(v -> {
            navigateToLocationSettingsLauncher.launch(null);
        });

        return binding.getRoot();
    }

    private void setupObservers() {
        setupPosition();
        setupRestaurantsState();
    }

    private void setupPosition() {
        mainViewModel.getPosition().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Success) {
                toggleLocationError(true);
                animateCameraToPosition(resource.getData());
            } else if (resource instanceof Resource.Error) {
                if (resource.getMessage().equals(Constants.NO_LOCATION_PERMISSION_MESSAGE)) {
                    requestLocationPermission();
                } else if (resource.getMessage().equals(Constants.LOCATION_DISABLED_MESSAGE)) {
                    if (resource.getData() != null) {
                        animateCameraToPosition(resource.getData());
                    }
                    toggleLocationError(false);
                }
            }
        });
    }

    private void setupRestaurantsState() {

        mainViewModel.getRestaurantsState().observe(getViewLifecycleOwner(), statesResource -> {
            clusterManager.clearItems();

            if (statesResource instanceof Resource.Error) {
                Log.d("debug", "Error: "+statesResource.getMessage());
                return;
            }

            for (RestaurantState restaurantState : statesResource.getData()) {
                clusterManager.addItem(new RestaurantItem(restaurantState.getPosition(), restaurantState.getId(), restaurantState.isJoined()));
            }
            clusterManager.cluster();
        } );
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

        setMapStyle();
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void animateCameraToPosition(LatLng latLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.MAPS_START_ZOOM_LEVEL));
    }

    private void setMapStyle() {
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style);
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
