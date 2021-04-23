package fr.feepin.go4lunch.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.libraries.places.api.model.Place;

import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.databinding.FragmentMapViewBinding;
import fr.feepin.go4lunch.utils.PermissionUtils;

public class MapViewFragment extends Fragment {

    public static final String TAG = "MAP_VIEW_TAG";

    private FragmentMapViewBinding binding;

    private GoogleMap googleMap;
    private View locationButton;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    enableMyLocation();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        PermissionUtils.showRationalDialog(getActivity(), R.string.rational_location_permission, (dialog, which) -> {
                            requestLocationPermission();
                        });
                    }
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapViewBinding.inflate(inflater);
        setHasOptionsMenu(true);
        binding.mapView.onCreate(savedInstanceState);
        return binding.getRoot();
    }

    private void configGoogleMap() {
        enableMyLocation();
        setMapStyle();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            googleMap.setMyLocationEnabled(true);
            locationButton = binding.mapView.findViewWithTag("GoogleMapMyLocationButton");
            locationButton.callOnClick();
            locationButton.setVisibility(View.GONE);

            binding.fabMyLocation.setOnClickListener(v -> {
                locationButton.callOnClick();
            });

        } else {
            googleMap.setMyLocationEnabled(false);
            binding.fabMyLocation.setOnClickListener(v -> {
                requestLocationPermission();
            });
        }
    }

    private void setMapStyle() {
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style);
        googleMap.setMapStyle(mapStyleOptions);
    }

    private void requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            configGoogleMap();
        });

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
