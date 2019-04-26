package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class ProfilesPrefsActivity extends AppCompatActivity {

    private long profile_id = 0;
    private int newProfileMode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;
    private int predefinedProfileIndex = 0;

    private int resultCode = RESULT_CANCELED;

    boolean showSaveMenu = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, true, false);
        GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }

        profile_id = getIntent().getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        if (profile_id == Profile.SHARED_PROFILE_ID)
            resultCode = RESULT_OK;
        newProfileMode = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
        predefinedProfileIndex = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);


        ProfilesPrefsFragment preferenceFragment = new ProfilesPrefsActivity.ProfilesPrefsRoot();

        //TODO - test if, bundle is filled in nested fragments!!!!
        Bundle arguments = new Bundle();
        arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
        arguments.putInt(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, newProfileMode);
        if (profile_id == Profile.SHARED_PROFILE_ID)
            arguments.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE);
        else
            arguments.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        arguments.putInt(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        //arguments.putBoolean(PreferenceFragment.EXTRA_NESTED, nested);
        preferenceFragment.setArguments(arguments);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_preferences_settings, preferenceFragment)
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
        if (fragment != null)
            ((ProfilesPrefsFragment)fragment).doOnActivityResult(requestCode, resultCode);
    }

//--------------------------------------------------------------------------------------------------

    static public class ProfilesPrefsRoot extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            //TODO - test if, bundle is filled in nested fragments!!!!
            if (bundle != null) {
                String prefsName = getPreferenceName(bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0));
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_root, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_root, rootKey);
            }
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
                editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
                editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
                editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
            }
        }

    }

    static public class ProfilesPrefsActivationDuration extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            //TODO - test if, bundle is filled in nested fragments!!!!
            if (bundle != null) {
                String prefsName = getPreferenceName(bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0));
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_activation_duration, rootKey);
            }
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                editor.putString(Profile.PREF_PROFILE_DURATION, fromPreference.getString(Profile.PREF_PROFILE_DURATION, "0"));
                editor.putString(Profile.PREF_PROFILE_AFTER_DURATION_DO, fromPreference.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO, "0"));
                editor.putBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, fromPreference.getBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, false));
                editor.putString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, fromPreference.getString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, ""));
                editor.putBoolean(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, fromPreference.getBoolean(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, false));
            }
        }

    }

}
