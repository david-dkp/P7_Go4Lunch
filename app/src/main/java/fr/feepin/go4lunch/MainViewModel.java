package fr.feepin.go4lunch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private MutableLiveData<FirebaseUser> _currentUser;
    private FirebaseAuth firebaseAuth;

    @Inject
    public MainViewModel(FirebaseAuth firebaseAuth) {
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
