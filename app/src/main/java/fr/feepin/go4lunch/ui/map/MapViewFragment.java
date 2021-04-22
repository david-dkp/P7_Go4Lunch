package fr.feepin.go4lunch.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;

import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.databinding.FragmentMapViewBinding;
import fr.feepin.go4lunch.utils.PermissionUtils;

public class MapViewFragment extends Fragment {

    public static final String TAG = "MAP_VIEW_TAG";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FragmentMapViewBinding binding;

    private GoogleMap googleMap;
    private View locationButton;

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return;

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
            locationButton.callOnClick();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionUtils.showRationalDialog(getActivity(), R.string.rational_location_permission, (dialog, which) -> {
                    requestLocationPermission();
                });
            }
        }

    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
