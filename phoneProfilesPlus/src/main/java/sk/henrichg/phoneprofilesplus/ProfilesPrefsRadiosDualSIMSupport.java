package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class ProfilesPrefsRadiosDualSIMSupport  extends ProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.profile_prefs_radios_dual_sim_support, rootKey);
    }

}
