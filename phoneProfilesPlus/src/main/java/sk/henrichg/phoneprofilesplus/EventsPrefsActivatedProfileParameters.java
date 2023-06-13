package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsActivatedProfileParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_activated_profile_sensor, rootKey);
    }

}
