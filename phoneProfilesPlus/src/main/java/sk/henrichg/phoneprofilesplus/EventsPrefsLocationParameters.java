package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsLocationParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_location_sensor, rootKey);
    }

}
