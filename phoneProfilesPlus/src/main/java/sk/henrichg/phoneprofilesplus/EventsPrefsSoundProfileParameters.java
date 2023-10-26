package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsSoundProfileParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_sound_profile_sensor, rootKey);
    }

}
