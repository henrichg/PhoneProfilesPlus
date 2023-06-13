package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsBatteryParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_battery_sensor, rootKey);
    }

}
