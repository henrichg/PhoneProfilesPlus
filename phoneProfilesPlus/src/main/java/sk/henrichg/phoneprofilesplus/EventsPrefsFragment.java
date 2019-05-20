package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class EventsPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;

    private boolean nestedFragment = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PPApplication.logE("EventsPrefsFragment.onCreate", "xxx");

        // is required for to not call onCreate and onDestroy on orientation change
        setRetainInstance(true);

        nestedFragment = !(this instanceof EventsPrefsActivity.EventsPrefsRoot);
        PPApplication.logE("EventsPrefsFragment.onCreate", "nestedFragment=" + nestedFragment);

        initPreferenceFragment(savedInstanceState);

        updateAllSummary();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        PPApplication.logE("EventsPrefsFragment.onDisplayPreferenceDialog", "xxx");

        PreferenceDialogFragmentCompat dialogFragment = null;

        if (preference instanceof ProfilePreferenceX) {
            ((ProfilePreferenceX) preference).fragment = new ProfilePreferenceFragmentX();
            dialogFragment = ((ProfilePreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof InfoDialogPreferenceX) {
            ((InfoDialogPreferenceX) preference).fragment = new InfoDialogPreferenceFragmentX();
            dialogFragment = ((InfoDialogPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof DurationDialogPreferenceX) {
            ((DurationDialogPreferenceX) preference).fragment = new DurationDialogPreferenceFragmentX();
            dialogFragment = ((DurationDialogPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ApplicationsMultiSelectDialogPreferenceX) {
            ((ApplicationsMultiSelectDialogPreferenceX) preference).fragment = new ApplicationsMultiSelectDialogPreferenceFragmentX();
            dialogFragment = ((ApplicationsMultiSelectDialogPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof BetterNumberPickerPreferenceX) {
            ((BetterNumberPickerPreferenceX) preference).fragment = new BetterNumberPickerPreferenceFragmentX();
            dialogFragment = ((BetterNumberPickerPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof RingtonePreferenceX) {
            ((RingtonePreferenceX) preference).fragment = new RingtonePreferenceFragmentX();
            dialogFragment = ((RingtonePreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof LocationGeofencePreferenceX) {
            ((LocationGeofencePreferenceX) preference).fragment = new LocationGeofencePreferenceFragmentX();
            dialogFragment = ((LocationGeofencePreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (preference instanceof ProfileMultiSelectPreferenceX) {
            ((ProfileMultiSelectPreferenceX) preference).fragment = new ProfileMultiSelectPreferenceFragmentX();
            dialogFragment = ((ProfileMultiSelectPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof DaysOfWeekPreferenceX) {
            ((DaysOfWeekPreferenceX) preference).fragment = new DaysOfWeekPreferenceFragmentX();
            dialogFragment = ((DaysOfWeekPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof TimePreferenceX) {
            ((TimePreferenceX) preference).fragment = new TimePreferenceFragmentX();
            dialogFragment = ((TimePreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof CalendarsMultiSelectDialogPreferenceX) {
            ((CalendarsMultiSelectDialogPreferenceX) preference).fragment = new CalendarsMultiSelectDialogPreferenceFragmentX();
            dialogFragment = ((CalendarsMultiSelectDialogPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof CalendarSearchStringPreferenceX) {
            ((CalendarSearchStringPreferenceX) preference).fragment = new CalendarSearchStringPreferenceFragmentX();
            dialogFragment = ((CalendarSearchStringPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ContactGroupsMultiSelectDialogPreferenceX) {
            ((ContactGroupsMultiSelectDialogPreferenceX) preference).fragment = new ContactGroupsMultiSelectDialogPreferenceFragmentX();
            dialogFragment = ((ContactGroupsMultiSelectDialogPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ContactsMultiSelectDialogPreferenceX) {
            ((ContactsMultiSelectDialogPreferenceX) preference).fragment = new ContactsMultiSelectDialogPreferenceFragmentX();
            dialogFragment = ((ContactsMultiSelectDialogPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof WifiSSIDPreferenceX) {
            ((WifiSSIDPreferenceX) preference).fragment = new WifiSSIDPreferenceFragmentX();
            dialogFragment = ((WifiSSIDPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof BluetoothNamePreferenceX) {
            ((BluetoothNamePreferenceX) preference).fragment = new BluetoothNamePreferenceFragmentX();
            dialogFragment = ((BluetoothNamePreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof MobileCellsRegistrationDialogPreferenceX) {
            ((MobileCellsRegistrationDialogPreferenceX) preference).fragment = new MobileCellsRegistrationDialogPreferenceFragmentX();
            dialogFragment = ((MobileCellsRegistrationDialogPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof MobileCellsPreferenceX) {
            ((MobileCellsPreferenceX) preference).fragment = new MobileCellsPreferenceFragmentX();
            dialogFragment = ((MobileCellsPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof NFCTagPreferenceX) {
            ((NFCTagPreferenceX) preference).fragment = new NFCTagPreferenceFragmentX();
            dialogFragment = ((NFCTagPreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".EventsPrefsActivity.DIALOG");
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PPApplication.logE("EventsPrefsFragment.onActivityCreated", "xxx");

    }

    @Override
    public void onDestroy() {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            /*
            SharedPreferences.Editor editor = profilesPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();
            */

            PPApplication.logE("EventsPrefsFragment.onDestroy", "xxx");

        } catch (Exception ignored) {
        }

        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PPApplication.logE("EventsPrefsFragment.onSharedPreferenceChanged", "key=" + key);

    }

    void doOnActivityResult(int requestCode, int resultCode, Intent data) {
        PPApplication.logE("EventsPrefsFragment.doOnActivityResult", "xxx");
        PPApplication.logE("EventsPrefsFragment.doOnActivityResult", "requestCode=" + requestCode);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //outState.putBoolean("nestedFragment", nestedFragment);
    }

    private void initPreferenceFragment(@SuppressWarnings("unused") Bundle savedInstanceState) {
        prefMng = getPreferenceManager();

        preferences = prefMng.getSharedPreferences();

        /*
        PPApplication.logE("ProfilesPrefsFragment.initPreferenceFragment", "getContext()="+getContext());

        if (savedInstanceState == null) {
            if (getContext() != null) {
                profilesPreferences = getContext().getSharedPreferences(PREFS_NAME_ACTIVITY, Activity.MODE_PRIVATE);

                SharedPreferences.Editor editor = preferences.edit();
                updateSharedPreferences(editor, profilesPreferences);
                editor.apply();
            }
        }
        */

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /*
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }
    */

}