package fr.feepin.go4lunch.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import fr.feepin.go4lunch.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

    }

}
