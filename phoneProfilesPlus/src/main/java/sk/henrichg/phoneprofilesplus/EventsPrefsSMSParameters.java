package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsSMSParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_sms_mms_sensor, rootKey);
    }

}
