package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PhoneProfilesPrefsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, true, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.preferences_activity);

        Toolbar toolbar = findViewById(R.id.preferences_activity_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_activity_settings, new PhoneProfilesPrefsRoot())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    static public class PhoneProfilesPrefsRoot extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_root, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsInterface extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_interface, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsApplicationStart extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_application_start, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsSystem extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_system, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsPermissions extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_permissions, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsNotifications extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_notifications, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsProfileActivation extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_profile_activation, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsEventRun extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_event_run, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsLocationScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_location_scanning, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsWifiScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_wifi_scanning, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsBluetoothScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_bluetooth_scanning, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsMobileCellsScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_mobile_cells_scanning, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsOrientationScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_orientation_scanning, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsActivator extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_activator, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsEditor extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_editor, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsWidgetList extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_list, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsWidgetOneRow extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_one_row, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsWidgetIcon extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_icon, rootKey);
            initPreferenceFragment();
        }

    }

    static public class PhoneProfilesPrefsSamsungEdgePanel extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.phone_profiles_prefs_samsung_edge_panel, rootKey);
            initPreferenceFragment();
        }

    }

}
