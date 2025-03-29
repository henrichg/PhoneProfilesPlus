package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsCallEndCallParameters extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_call_sensor_end_call, rootKey);
    }

}
