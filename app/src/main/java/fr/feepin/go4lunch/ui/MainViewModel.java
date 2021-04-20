package fr.feepin.go4lunch.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {

    private MutableLiveData<FirebaseUser> _currentUser;
    private FirebaseAuth firebaseAuth = firebaseAuth = FirebaseAuth.getInstance();;

    public MainViewModel() {
        _currentUser = new MutableLiveData<>(firebaseAuth.getCurrentUser());
        firebaseAuth.addAuthStateListener(auth -> {
            _currentUser.postValue(auth.getCurrentUser());
        });
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return _currentUser;
    }

    public void signOut() {
        firebaseAuth.signOut();
    }
}
