package fr.feepin.go4lunch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.feepin.go4lunch.data.PositionSharedRepository;

@Singleton
public class DefaultPositionSharedRepository implements PositionSharedRepository {

    private MutableLiveData<LatLng> position = new MutableLiveData<>();

    @Inject
    public DefaultPositionSharedRepository() { }

    @Override
    public LiveData<LatLng> getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(LatLng position) {
        this.position.setValue(position);
    }

}
