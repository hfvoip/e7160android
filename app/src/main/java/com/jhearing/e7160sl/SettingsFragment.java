package com.jhearing.e7160sl;

import android.os.Bundle;
import android.preference.PreferenceFragment;


/**
 * Saves the Settings in the shared preferences
 */
public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).changeNavigationSelected(R.id.nav_manage);
        addPreferencesFromResource(R.xml.preferences);
    }


}
