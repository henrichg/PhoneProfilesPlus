package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsPeriodicParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_periodic_sensor, rootKey);
    }

}
