package fr.feepin.go4lunch.data;

import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.model.LatLng;

public interface PositionSharedRepository {

    LiveData<LatLng> getPosition();

    void setPosition(LatLng position);

}
