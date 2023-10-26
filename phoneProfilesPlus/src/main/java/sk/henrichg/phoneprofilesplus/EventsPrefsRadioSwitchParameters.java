package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsRadioSwitchParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_radio_switch_sensor, rootKey);
    }

}
