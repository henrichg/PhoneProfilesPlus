package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class EventsPrefsCallAnswerCallParameters extends EventsPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.event_prefs_call_sensor_answer_call, rootKey);
    }

}
