package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsMobileCellsParameters  extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_mobile_cells_sensor, rootKey);
    }

}
