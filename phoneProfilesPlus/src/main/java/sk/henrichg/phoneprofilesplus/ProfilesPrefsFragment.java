package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class ProfilesPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private SharedPreferences profilesPreferences;

    private boolean nestedFragment = false;

    static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    //static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    //private static final String PREFS_NAME_SHARED_PROFILE = PPApplication.SHARED_PROFILE_PREFS_NAME;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PPApplication.logE("ProfilesPrefsFragment.onCreate", "xxx");

        // is requred for to not call onCreate and onDestroy on orientation change
        setRetainInstance(true);

        nestedFragment = !(this instanceof ProfilesPrefsActivity.ProfilesPrefsRoot);
        PPApplication.logE("ProfilesPrefsFragment.onCreate", "nestedFragment="+nestedFragment);

        initPreferenceFragment(savedInstanceState);
        //prefMng = getPreferenceManager();
        //preferences = prefMng.getSharedPreferences();

        updateAllSummary();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference)
    {
        PPApplication.logE("ProfilesPrefsFragment.onDisplayPreferenceDialog", "xxx");

        PreferenceDialogFragmentCompat dialogFragment = null;

        if (preference instanceof DurationDialogPreferenceX)
        {
            ((DurationDialogPreferenceX)preference).fragment = new DurationDialogPreferenceFragmentX();
            dialogFragment = ((DurationDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof RingtonePreferenceX)
        {
            ((RingtonePreferenceX)preference).fragment = new RingtonePreferenceFragmentX();
            dialogFragment = ((RingtonePreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (preference instanceof InfoDialogPreferenceX)
        {
            ((InfoDialogPreferenceX)preference).fragment = new InfoDialogPreferenceFragmentX();
            dialogFragment = ((InfoDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ProfileIconPreferenceX)
        {
            ((ProfileIconPreferenceX)preference).fragment = new ProfileIconPreferenceFragmentX();
            dialogFragment = ((ProfileIconPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof VolumeDialogPreferenceX)
        {
            ((VolumeDialogPreferenceX)preference).fragment = new VolumeDialogPreferenceFragmentX();
            dialogFragment = ((VolumeDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof NotificationVolume0DialogPreferenceX)
        {
            ((NotificationVolume0DialogPreferenceX)preference).fragment = new NotificationVolume0DialogPreferenceFragmentX();
            dialogFragment = ((NotificationVolume0DialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ConnectToSSIDDialogPreferenceX)
        {
            ((ConnectToSSIDDialogPreferenceX)preference).fragment = new ConnectToSSIDDialogPreferenceFragmentX();
            dialogFragment = ((ConnectToSSIDDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof BrightnessDialogPreferenceX)
        {
            ((BrightnessDialogPreferenceX)preference).fragment = new BrightnessDialogPreferenceFragmentX();
            dialogFragment = ((BrightnessDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ApplicationsDialogPreferenceX)
        {
            ((ApplicationsDialogPreferenceX)preference).fragment = new ApplicationsDialogPreferenceFragmentX();
            dialogFragment = ((ApplicationsDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ApplicationsMultiSelectDialogPreferenceX)
        {
            ((ApplicationsMultiSelectDialogPreferenceX)preference).fragment = new ApplicationsMultiSelectDialogPreferenceFragmentX();
            dialogFragment = ((ApplicationsMultiSelectDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, "sk.henrichg.phoneprofilesplus.ProfilesPrefsActivity.DIALOG");
            }
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PPApplication.logE("ProfilesPrefsFragment.onActivityCreated", "xxx");

        if (getActivity() == null)
            return;

        if (savedInstanceState != null) {
            //startupSource = savedInstanceState.getInt("startupSource", PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        }


    }

    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            SharedPreferences.Editor editor = profilesPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();

            PPApplication.logE("ProfilesPrefsFragment.onDestroy", "xxx");

        } catch (Exception ignored) {}

        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PPApplication.logE("ProfilesPrefsFragment.onSharedPreferenceChanged", "xxx");
        //setSummary(key);
    }

    void doOnActivityResult(int requestCode, int resultCode/*, Intent data*/) {
        PPApplication.logE("ProfilesPrefsFragment.doOnActivityResult", "xxx");
        PPApplication.logE("ProfilesPrefsFragment.doOnActivityResult", "requestCode=" + requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //outState.putInt("startupSource", startupSource);
    }

    private void initPreferenceFragment(Bundle savedInstanceState) {
        prefMng = getPreferenceManager();

        //prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        //prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();

        PPApplication.logE("ProfilesPrefsFragment.initPreferenceFragment", "getContext()="+getContext());

        if (savedInstanceState == null) {
            if (getContext() != null) {
                profilesPreferences = getContext().getSharedPreferences(PREFS_NAME_ACTIVITY, Activity.MODE_PRIVATE);

                SharedPreferences.Editor editor = preferences.edit();
                updateSharedPreferences(editor, profilesPreferences);
                editor.apply();
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }

    private void updateAllSummary() {
        if (getActivity() == null)
            return;

    }

}
