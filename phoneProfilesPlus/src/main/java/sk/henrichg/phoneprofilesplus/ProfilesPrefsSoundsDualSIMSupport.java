package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class ProfilesPrefsSoundsDualSIMSupport  extends ProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.profile_prefs_sounds_dual_sim_support, rootKey);
    }

}
