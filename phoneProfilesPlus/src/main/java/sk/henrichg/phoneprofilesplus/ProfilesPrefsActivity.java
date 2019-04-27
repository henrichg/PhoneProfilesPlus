package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class ProfilesPrefsActivity extends AppCompatActivity {

    long profile_id = 0;
    int newProfileMode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;
    int predefinedProfileIndex = 0;
    //int startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY;

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
        /*if (profile_id == Profile.SHARED_PROFILE_ID)
            resultCode = RESULT_OK;*/
        newProfileMode = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
        predefinedProfileIndex = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);


        ProfilesPrefsFragment preferenceFragment = new ProfilesPrefsActivity.ProfilesPrefsRoot();

        /*if (profile_id == Profile.SHARED_PROFILE_ID)
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE;
        else
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY;*/

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_preferences_settings, preferenceFragment)
                    .commit();
        }
        else {
            profile_id = savedInstanceState.getLong("profile_id", 0);
            /*if (profile_id == Profile.SHARED_PROFILE_ID)
                resultCode = RESULT_OK;*/
            newProfileMode = savedInstanceState.getInt("newProfileMode", EditorProfileListFragment.EDIT_MODE_UNDEFINED);
            predefinedProfileIndex = savedInstanceState.getInt("predefinedProfileIndex", 0);
            //startupSource = savedInstanceState.getInt("startupSource", PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);

            showSaveMenu = savedInstanceState.getBoolean("showSaveMenu", false);
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


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putLong("profile_id", profile_id);
        savedInstanceState.putInt("newProfileMode", newProfileMode);
        savedInstanceState.putInt("predefinedProfileIndex", predefinedProfileIndex);
        //savedInstanceState.putInt("startupSource", startupSource);

        savedInstanceState.putBoolean("showSaveMenu", showSaveMenu);
    }


//--------------------------------------------------------------------------------------------------

    static public class ProfilesPrefsRoot extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_root, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_root, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_root, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            //if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
                editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
                editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
                editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
            //}
        }

    }

    static public class ProfilesPrefsActivationDuration extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_activation_duration, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_activation_duration, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            //if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                editor.putString(Profile.PREF_PROFILE_DURATION, fromPreference.getString(Profile.PREF_PROFILE_DURATION, "0"));
                editor.putString(Profile.PREF_PROFILE_AFTER_DURATION_DO, fromPreference.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO, "0"));
                editor.putBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, fromPreference.getBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, false));
                editor.putString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, fromPreference.getString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, ""));
                editor.putBoolean(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, fromPreference.getBoolean(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, false));
            //}
        }

    }

    static public class ProfilesPrefsSoundProfiles extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_sound_profile, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_sound_profile, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_sound_profile, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsVolumes extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_volumes, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_volumes, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_volumes, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsSounds extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_sounds, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_sounds, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_sounds, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsTouchEffects extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_touch_effects, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_touch_effects, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_touch_effects, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsRadios extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_radios, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_radios, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_radios, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsScreen extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_screen, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_screen, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_screen, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsApplication extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_application, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_application, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_application, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsOthers extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_others, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_others, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_others, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsForceStopApplications extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_force_stop, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_force_stop, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_force_stop, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

    static public class ProfilesPrefsLockDevice extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*if (getActivity() != null) {
                startupSource = ((ProfilesPrefsActivity)getActivity()).startupSource;
                String prefsName = getPreferenceName(startupSource);
                if (prefsName.equals(PREFS_NAME_ACTIVITY))
                    setPreferencesFromResource(R.xml.profile_prefs_lock_device, rootKey);
                else
                    setPreferencesFromResource(R.xml.default_profile_prefs_lock_device, rootKey);
            }*/
            setPreferencesFromResource(R.xml.profile_prefs_lock_device, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(Profile.PREF_PROFILE_NAME, fromPreference.getString(Profile.PREF_PROFILE_NAME, "Profile"));
            editor.putString(Profile.PREF_PROFILE_ICON, fromPreference.getString(Profile.PREF_PROFILE_ICON, "ic_profile_default|1|0|0"));
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, fromPreference.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true));
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, fromPreference.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false));
        }

    }

}
