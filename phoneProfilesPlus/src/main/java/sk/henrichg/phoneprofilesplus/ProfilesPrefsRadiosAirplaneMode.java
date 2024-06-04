package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class ProfilesPrefsRadiosAirplaneMode extends ProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.profile_prefs_radios_airplane_mode, rootKey);
    }

}
