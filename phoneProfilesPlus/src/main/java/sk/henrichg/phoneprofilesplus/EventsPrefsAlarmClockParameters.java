package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsAlarmClockParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_alarm_clock_sensor, rootKey);
    }

}
