package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsStartOfEventsOthers  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_start_of_event_others, rootKey);
    }

}
