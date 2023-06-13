package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class ProfilesPrefsVibrationIntensity  extends ProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.profile_prefs_vibration_intensity, rootKey);
    }

}
