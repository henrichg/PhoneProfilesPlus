package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class ProfilesPrefsTouchEffects  extends ProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.profile_prefs_touch_effects, rootKey);
    }

}
