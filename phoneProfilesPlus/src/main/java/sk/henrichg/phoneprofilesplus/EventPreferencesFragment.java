package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
 
public class EventPreferencesFragment extends EventPreferencesNestedFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private Context context;

    //public static LocationGeofencePreference changedLocationGeofencePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);
        // this is really important in order to save the state across screen
        // configuration changes for example
        //setRetainInstance(true);

        context = getActivity().getBaseContext();

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(EventPreferencesNestedFragment.PREFS_NAME_ACTIVITY);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        /*Bundle bundle = this.getArguments();
        if (bundle != null)
            startupSource = bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0);*/

        return R.xml.event_preferences;
    }

    private void updateSharedPreference()
    {
        // updating activity with selected event preferences

        Event event = new Event();
        event.setAllSummary(prefMng, preferences, context);
    }

    /*
    static public void setChangedLocationGeofencePreference(LocationGeofencePreference changedLocationGeofencePref)
    {
        changedLocationGeofencePreference = changedLocationGeofencePref;
    }
    */

}
