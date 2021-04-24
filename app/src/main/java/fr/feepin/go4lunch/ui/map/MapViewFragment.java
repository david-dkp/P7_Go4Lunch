package fr.feepin.go4lunch.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.model.Place;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.databinding.FragmentMapViewBinding;
import fr.feepin.go4lunch.utils.PermissionUtils;

@AndroidEntryPoint
public class MapViewFragment extends Fragment {

    public static final String TAG = "MAP_VIEW_TAG";

    private FragmentMapViewBinding binding;

    private GoogleMap googleMap;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationManager locationManager;

    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d("debug", "called");
            fusedLocationProviderClient.removeLocationUpdates(this);
            animateCameraToLocation(locationResult.getLastLocation());
        }
    };

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    moveCameraToMyPosition();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        PermissionUtils.showRationalDialog(getActivity(), R.string.rational_location_permission, (dialog, which) -> {
                            requestLocationPermission();
                        });
                    }
                }
            }
    );

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapViewBinding.inflate(inflater);
        binding.mapView.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        binding.mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            configGoogleMap();
        });

        binding.fabMyLocation.setOnClickListener(v -> {
            if (PermissionUtils.isPermissionGranted(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                moveCameraToMyPosition();
            } else {
                requestLocationPermission();
            }
        });

        return binding.getRoot();
    }


    private void configGoogleMap() {
        if (PermissionUtils.isPermissionGranted(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            moveCameraToMyPosition();
        }
        setMapStyle();
    }

    private void animateCameraToLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.MAPS_START_ZOOM_LEVEL));
    }

    @SuppressLint("MissingPermission")
    private void moveCameraToMyPosition() {
        if (LocationManagerCompat.isLocationEnabled(locationManager)) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setNumUpdates(1);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } else {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.getResult() != null) {
                     animateCameraToLocation(task.getResult());
                }
                //TODO: show location not enabled error
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
