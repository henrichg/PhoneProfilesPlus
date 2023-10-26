package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsDeviceBootParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_device_boot_sensor, rootKey);
    }

}
