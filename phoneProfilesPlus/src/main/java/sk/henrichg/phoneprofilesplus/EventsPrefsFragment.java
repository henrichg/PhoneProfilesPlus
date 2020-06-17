package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("WeakerAccess")
public class EventsPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;

    private boolean nestedFragment = false;

    private Event event;

    private static final String PRF_GRANT_PERMISSIONS = "eventGrantPermissions";
    private static final String PRF_NOT_IS_RUNNABLE = "eventNotIsRunnable";
    private static final String PRF_NOT_ENABLED_SOME_SENSOR = "eventNotEnabledSomeSensors";
    private static final String PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE = "eventNotEnabledAccessibilityService";
    private static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1981;
    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1982;
    private static final int RESULT_LOCATION_APP_SETTINGS = 1983;
    private static final int RESULT_WIFI_SCANNING_APP_SETTINGS = 1984;
    private static final int RESULT_BLUETOOTH_SCANNING_APP_SETTINGS = 1985;
    private static final String PREF_ORIENTATION_SCANNING_APP_SETTINGS = "eventEnableOrientationScanningAppSettings";
    private static final int RESULT_ORIENTATION_SCANNING_SETTINGS = 1986;
    private static final String PREF_MOBILE_CELLS_SCANNING_APP_SETTINGS = "eventMobileCellsScanningAppSettings";
    private static final int RESULT_MOBILE_CELLS_SCANNING_SETTINGS = 1987;
    private static final String PREF_USE_PRIORITY_APP_SETTINGS = "eventUsePriorityAppSettings";
    private static final int RESULT_USE_PRIORITY_SETTINGS = 1988;
    //private static final String PREF_MOBILE_CELLS_REGISTRATION = "eventMobileCellsRegistration";
    static final int RESULT_WIFI_LOCATION_SYSTEM_SETTINGS = 1989;
    static final int RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = 1990;
    static final int RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS = 1991;
    private static final int RESULT_WIFI_KEEP_ON_SYSTEM_SETTINGS = 1992;
    static final int RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = 1993;
    private static final int RESULT_TIME_LOCATION_SYSTEM_SETTINGS = 1994;
    private static final int RESULT_TIME_SCANNING_APP_SETTINGS = 1995;
    private static final int RESULT_CALENDAR_SCANNING_APP_SETTINGS = 1995;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //PPApplication.logE("EventsPrefsFragment.onCreate", "xxx");

        // is required for to not call onCreate and onDestroy on orientation change
        setRetainInstance(true);

        nestedFragment = !(this instanceof EventsPrefsActivity.EventsPrefsRoot);
        //PPApplication.logE("EventsPrefsFragment.onCreate", "nestedFragment=" + nestedFragment);

        initPreferenceFragment(savedInstanceState);

        if (getActivity() != null) {
            event.setAllSummary(prefMng, preferences, getActivity().getBaseContext());
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @Override
    public RecyclerView onCreateRecyclerView (LayoutInflater inflater, ViewGroup parent, Bundle state) {
        final RecyclerView view = super.onCreateRecyclerView(inflater, parent, state);
        view.setItemAnimator(null);
        view.setLayoutAnimation(null);
        return view;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        //PPApplication.logE("EventsPrefsFragment.onDisplayPreferenceDialog", "xxx");

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
        /*if (preference instanceof TimePreferenceX) {
            ((TimePreferenceX) preference).fragment = new TimePreferenceFragmentX();
            dialogFragment = ((TimePreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }*/
        if (preference instanceof TimeDialogPreferenceX) {
            ((TimeDialogPreferenceX) preference).fragment = new TimeDialogPreferenceFragmentX();
            dialogFragment = ((TimeDialogPreferenceX) preference).fragment;
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
        if (preference instanceof SearchStringPreferenceX) {
            ((SearchStringPreferenceX) preference).fragment = new SearchStringPreferenceFragmentX();
            dialogFragment = ((SearchStringPreferenceX) preference).fragment;
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
            if ((getActivity() != null) && (!getActivity().isFinishing())) {
                FragmentManager fragmentManager = getParentFragmentManager();//getFragmentManager();
                //if (fragmentManager != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".EventsPrefsActivity.DIALOG");
                //}
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //PPApplication.logE("EventsPrefsFragment.onActivityCreated", "xxx");

        if (getActivity() == null)
            return;

        EventsPrefsActivity activity = (EventsPrefsActivity) getActivity();

        final Context context = activity.getBaseContext();

        // must be used handler for rewrite toolbar title/subtitle
        final EventsPrefsFragment fragment = this;
        Handler handler = new Handler(getActivity().getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                if (nestedFragment) {
                    toolbar.setTitle(fragment.getPreferenceScreen().getTitle());
                }
                else {
                    toolbar.setTitle(getString(R.string.title_activity_event_preferences));
                }

            }
        }, 200);

        setRedTextToPreferences();

        event.checkPreferences(prefMng, context);

        Preference notificationAccessPreference = prefMng.findPreference(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS);
        if (notificationAccessPreference != null) {
            //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean ok = false;
                    String activity;
                    activity = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                    if (GlobalGUIRoutines.activityActionExists(activity, context)) {
                        try {
                            Intent intent = new Intent(activity);
                            startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        Preference extenderPreference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    installExtender(getString(R.string.event_preferences_PPPExtenderInstallInfo_summary) + "\n\n" +
                            getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_2) + " " +
                            getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_3));
                    return false;
                }
            });
        }
        Preference accessibilityPreference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    enableExtender();
                    return false;
                }
            });
        }
        Preference preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "locationScanningCategoryRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_LOCATION_APP_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "wifiScanningCategoryRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_WIFI_SCANNING_APP_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_LOCATION_SYSTEM_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesTime.PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_TIME_LOCATION_SYSTEM_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        if (Build.VERSION.SDK_INT >= 27) {
            preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("eventWifiCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        else {
            preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        boolean ok = false;
                        //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_WIFI_IP_SETTINGS, context.getApplicationContext())) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_WIFI_KEEP_ON_SYSTEM_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (!ok) {
                            if (getActivity() != null) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();

//                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(DialogInterface dialog) {
//                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                        if (positive != null) positive.setAllCaps(false);
//                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                        if (negative != null) negative.setAllCaps(false);
//                                    }
//                                });

                                if (!getActivity().isFinishing())
                                    dialog.show();
                            }
                        }
                        return false;
                    }
                });
            }
        }
        preference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "bluetoothScanningCategoryRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_BLUETOOTH_SCANNING_APP_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(PREF_ORIENTATION_SCANNING_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "orientationScanningCategoryRoot");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_ORIENTATION_SCANNING_SETTINGS);
                    return false;
                }
            });
        }
        extenderPreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    installExtender(getString(R.string.event_preferences_PPPExtenderInstallInfo_summary) + "\n\n" +
                            getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_2) + " " +
                            getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_3));
                    return false;
                }
            });
        }
        Preference orientationPreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ACCESSIBILITY_SETTINGS);
        if (orientationPreference != null) {
            //orientationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            orientationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    enableExtender();
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(PREF_MOBILE_CELLS_SCANNING_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "mobileCellsScanningCategoryRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_MOBILE_CELLS_SCANNING_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesTime.PREF_EVENT_TIME_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "backgroundScanningCategoryRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_TIME_SCANNING_APP_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesCalendar.PREF_EVENT_CALENDAR_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "backgroundScanningCategoryRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_CALENDAR_SCANNING_APP_SETTINGS);
                    return false;
                }
            });
        }

        preference = prefMng.findPreference(PREF_USE_PRIORITY_APP_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "eventRunCategoryRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_USE_PRIORITY_SETTINGS);
                    return false;
                }
            });
        }
        MobileCellsRegistrationDialogPreferenceX mobileCellsRegistrationDialogPreference =
                prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_REGISTRATION);
        if (mobileCellsRegistrationDialogPreference != null) {
            mobileCellsRegistrationDialogPreference.event_id = activity.event_id;
        }
        /*
        MobileCellsPreference mobileCellsPreference =
                (MobileCellsPreference)prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
        if (mobileCellsPreference != null) {
            mobileCellsPreference.event_id = event_id;
        }
        */

        extenderPreference = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    installExtender(getString(R.string.event_preferences_PPPExtenderInstallInfo_summary) + "\n\n" +
                            getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_2) + " " +
                            getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_3));
                    return false;
                }
            });
        }
        Preference smsPreference = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_ACCESSIBILITY_SETTINGS);
        if (smsPreference != null) {
            //smsPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            smsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    enableExtender();
                    return false;
                }
            });
        }
        smsPreference = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_LAUNCH_EXTENDER);
        if (smsPreference != null) {
            //smsPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            smsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_3_0) {
                        PackageManager packageManager = context.getPackageManager();
                        Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                        if (intent != null) {
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                    }
                    else {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.event_preferences_extender_not_installed);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }

        extenderPreference = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    installExtender(getString(R.string.event_preferences_PPPExtenderInstallInfo_summary) + "\n\n" +
                            getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_2) + " " +
                            getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_3));
                    return false;
                }
            });
        }
        Preference callPreference = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_ACCESSIBILITY_SETTINGS);
        if (callPreference != null) {
            //smsPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            callPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    enableExtender();
                    return false;
                }
            });
        }
        callPreference = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_LAUNCH_EXTENDER);
        if (callPreference != null) {
            //callPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            callPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_3_0) {
                        PackageManager packageManager = context.getPackageManager();
                        Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                        if (intent != null) {
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                    }
                    else {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.event_preferences_extender_not_installed);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        accessibilityPreference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_LAUNCH_EXTENDER);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_3_0) {
                        PackageManager packageManager = context.getPackageManager();
                        Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                        if (intent != null) {
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                    }
                    else {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.event_preferences_extender_not_installed);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        accessibilityPreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_LAUNCH_EXTENDER);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_3_0) {
                        PackageManager packageManager = context.getPackageManager();
                        Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                        if (intent != null) {
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                    }
                    else {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.event_preferences_extender_not_installed);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(EventPreferencesBattery.PREF_EVENT_BATTERY_BATTERY_SAVER_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean activityExists;
                    Intent intent;
                    /*if (Build.VERSION.SDK_INT == 21) {
                        intent = new Intent();
                        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                        activityExists = GlobalGUIRoutines.activityIntentExists(intent, context);
                    } else*/ {
                        activityExists = GlobalGUIRoutines.activityActionExists(Settings.ACTION_BATTERY_SAVER_SETTINGS, context);
                        intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                    }
                    if (activityExists) {
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            //if (Build.VERSION.SDK_INT > 21) {
                                intent = new Intent();
                                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                                activityExists = GlobalGUIRoutines.activityIntentExists(intent, context);
                                if (activityExists) {
                                    try {
                                        startActivity(intent);
                                    } catch (Exception ee) {
                                        PPApplication.recordException(ee);
                                    }
                                }
                            //}
                        }
                    }
                    if (!activityExists) {
                        if (getActivity() != null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                }
            });
        }

        InfoDialogPreferenceX infoDialogPreference = prefMng.findPreference("eventSensorsInfo");
        if (infoDialogPreference != null) {
            String info = getString(R.string.event_preferences_sensorsInfo_summary);
            info = "• " + info;
            info = info.replace("\n\n", "\n\n• ");
            infoDialogPreference.setInfoText(info);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //PPApplication.logE("EventsPrefsFragment.onResume", "xxx");

        if (!nestedFragment) {
            if (getActivity() == null)
                return;

            final Context context = getActivity().getBaseContext();

            event._eventPreferencesApplication.checkPreferences(prefMng, context);
            event._eventPreferencesOrientation.checkPreferences(prefMng, context);
            event._eventPreferencesSMS.checkPreferences(prefMng, context);
            event._eventPreferencesCall.checkPreferences(prefMng, context);
            event._eventPreferencesNotification.checkPreferences(prefMng, context);
            setRedTextToPreferences();
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventsPrefsFragment.onResume");
            PPApplication.updateGUI(context.getApplicationContext(), true, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            /*
            SharedPreferences.Editor editor = profilesPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();
            */

            //PPApplication.logE("EventsPrefsFragment.onDestroy", "xxx");

        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //PPApplication.logE("EventsPrefsFragment.onSharedPreferenceChanged", "key=" + key);

        if (key.equals(Event.PREF_EVENT_NAME)) {
            String value = sharedPreferences.getString(key, "");
            if (getActivity() != null) {
                // must be used handler for rewrite toolbar title/subtitle
                final String _value = value;
                Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null)
                            return;

                        Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                        toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + _value);
                    }
                }, 200);
            }
        }

        if (getActivity() == null)
            return;

        event.setSummary(prefMng, key, sharedPreferences, getActivity());

        setRedTextToPreferences();

        EventsPrefsActivity activity = (EventsPrefsActivity)getActivity();
        //PPApplication.logE("EventsPrefsFragment.onSharedPreferenceChanged", "activity="+activity);
        if (activity != null) {
            activity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        }
    }

    void doOnActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("EventsPrefsFragment.doOnActivityResult", "xxx");
            PPApplication.logE("EventsPrefsFragment.doOnActivityResult", "requestCode=" + requestCode);
        }*/

        if (getActivity() == null)
            return;

        final Context context = getActivity().getBaseContext();

        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EVENT) {
            setRedTextToPreferences();
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            event._eventPreferencesNotification.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS) {
            event._eventPreferencesApplication.checkPreferences(prefMng, context);
            event._eventPreferencesOrientation.checkPreferences(prefMng, context);
            event._eventPreferencesSMS.checkPreferences(prefMng, context);
            event._eventPreferencesCall.checkPreferences(prefMng, context);
            setRedTextToPreferences();
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventsPrefsFragment.doOnActivityResult");
            PPApplication.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_TIME_SCANNING_APP_SETTINGS) {
            event._eventPreferencesTime.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_CALENDAR_SCANNING_APP_SETTINGS) {
            event._eventPreferencesCalendar.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_WIFI_SCANNING_APP_SETTINGS) {
            event._eventPreferencesWifi.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_WIFI_KEEP_ON_SYSTEM_SETTINGS) {
            event._eventPreferencesWifi.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_BLUETOOTH_SCANNING_APP_SETTINGS) {
            event._eventPreferencesBluetooth.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_LOCATION_APP_SETTINGS) {
            event._eventPreferencesLocation.checkPreferences(prefMng, context);
        }
        if (requestCode == LocationGeofencePreferenceX.RESULT_GEOFENCE_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                LocationGeofencePreferenceX preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_GEOFENCES);
                if (preference != null) {
                    preference.setGeofenceFromEditor(/*geofenceId*/);
                }
            }
            /*if (EventPrefsFragment.changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    //long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    // this persistGeofence, for multiselect this mus only refresh listView in preference
                    EventPrefsFragment.changedLocationGeofencePreference.setGeofenceFromEditor();
                    EventPrefsFragment.changedLocationGeofencePreference = null;
                }
            }*/
        }
        if (requestCode == RESULT_ORIENTATION_SCANNING_SETTINGS) {
            event._eventPreferencesOrientation.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_MOBILE_CELLS_SCANNING_SETTINGS) {
            event._eventPreferencesMobileCells.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_WIFI_LOCATION_SYSTEM_SETTINGS) {
            WifiSSIDPreferenceX preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_SSID);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesWifi.checkPreferences(prefMng, context);
            setRedTextToPreferences();
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventsPrefsFragment.doOnActivityResult");
            PPApplication.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS) {
            BluetoothNamePreferenceX preference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesBluetooth.checkPreferences(prefMng, context);
            setRedTextToPreferences();
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventsPrefsFragment.doOnActivityResult");
            PPApplication.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS) {
            LocationGeofencePreferenceX preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_GEOFENCES);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesLocation.checkPreferences(prefMng, context);
            setRedTextToPreferences();
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventsPrefsFragment.doOnActivityResult");
            PPApplication.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS) {
            MobileCellsPreferenceX preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesMobileCells.checkPreferences(prefMng, context);
            setRedTextToPreferences();
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventsPrefsFragment.doOnActivityResult");
            PPApplication.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_TIME_LOCATION_SYSTEM_SETTINGS) {
            PPApplication.restartTwilightScanner(context);

            event._eventPreferencesTime.checkPreferences(prefMng, context);
            setRedTextToPreferences();
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventsPrefsFragment.doOnActivityResult");
            PPApplication.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_USE_PRIORITY_SETTINGS) {

            event.setSummary(prefMng, Event.PREF_EVENT_PRIORITY_APP_SETTINGS, preferences, context);
            event.setSummary(prefMng, Event.PREF_EVENT_PRIORITY, preferences, context);
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            RingtonePreferenceX preference = prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND_START);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND_END);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_WIFI_BT_SCAN_DIALOG) {
            WifiSSIDPreferenceX wifiPreference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_SSID);
            if (wifiPreference != null)
                wifiPreference.refreshListView(true, "");
            BluetoothNamePreferenceX bluetoothPreference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
            if (bluetoothPreference != null)
                bluetoothPreference.refreshListView(true, "");
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG) {
            MobileCellsPreferenceX preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
            if (preference != null)
                preference.refreshListView(true, Integer.MAX_VALUE);
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG) {
            MobileCellsRegistrationDialogPreferenceX preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_REGISTRATION);
            if (preference != null)
                preference.startRegistration();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CALENDAR_DIALOG) {
            CalendarsMultiSelectDialogPreferenceX preference = prefMng.findPreference(EventPreferencesCalendar.PREF_EVENT_CALENDAR_CALENDARS);
            if (preference != null)
                preference.refreshListView(true);
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CONTACT_DIALOG) {
            ContactsMultiSelectDialogPreferenceX preference1 = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(true);
            preference1 = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(true);
            ContactGroupsMultiSelectDialogPreferenceX preference2 = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(true);
            preference2 = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(true);
        }
        /*if (requestCode == NFCTagPreference.RESULT_NFC_TAG_READ_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                NFCTagPreferenceX preference = prefMng.findPreference(EventPreferencesNFC.PREF_EVENT_NFC_NFC_TAGS);
                if (preference != null) {
                    String tagName = data.getStringExtra(NFCTagReadEditorActivity.EXTRA_TAG_NAME);
                    String tagUid = data.getStringExtra(NFCTagReadEditorActivity.EXTRA_TAG_UID);
                    long tagDbId = data.getLongExtra(NFCTagReadEditorActivity.EXTRA_TAG_DB_ID, 0);
                    Log.e("EventPreferencesNestedFragment.doOnActivityResult", "tagName="+tagName);
                    Log.e("EventPreferencesNestedFragment.doOnActivityResult", "tagUid="+tagUid);
                    Log.e("EventPreferencesNestedFragment.doOnActivityResult", "tagDbId="+tagDbId);
                    preference.setNFCTagFromEditor(tagName, tagUid, tagDbId);
                }
            }
        }*/
        if (requestCode == NFCTagPreferenceX.RESULT_NFC_TAG_WRITE) {
            if (resultCode == Activity.RESULT_OK) {
                NFCTagPreferenceX preference = prefMng.findPreference(EventPreferencesNFC.PREF_EVENT_NFC_NFC_TAGS);
                if (preference != null) {
                    String tagName = data.getStringExtra(NFCTagWriteActivity.EXTRA_TAG_NAME);
                    //String tagUid = data.getStringExtra(NFCTagWriteActivity.EXTRA_TAG_UID);
                    long tagDbId = data.getLongExtra(NFCTagWriteActivity.EXTRA_TAG_DB_ID, 0);
                    //Log.e("EventPreferencesNestedFragment.doOnActivityResult", "tagName=" + tagName);
                    //Log.e("EventPreferencesNestedFragment.doOnActivityResult", "tagUid=" + tagUid);
                    //Log.e("EventPreferencesNestedFragment.doOnActivityResult", "tagDbId=" + tagDbId);
                    preference.setNFCTagFromEditor(tagName, "", tagDbId);
                }
            }
        }
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

        event = new Event();

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

    static boolean isRedTextNotificationRequired(Event event, Context context) {
        Context appContext = context.getApplicationContext();
        boolean enabledSomeSensor = event.isEnabledSomeSensor(appContext);
        boolean grantedAllPermissions = Permissions.checkEventPermissions(appContext, event).size() == 0;
        /*if (Build.VERSION.SDK_INT >= 29) {
            if (!Settings.canDrawOverlays(context))
                grantedAllPermissions = false;
        }*/
        boolean accessibilityEnabled =  event.isAccessibilityServiceEnabled(appContext, false) == 1;
        boolean eventIsRunnable = event.isRunnable(appContext, false);

        return (!enabledSomeSensor) || (!grantedAllPermissions) || (!accessibilityEnabled) || (!eventIsRunnable);
    }

    private void setRedTextToPreferences() {
        if (nestedFragment)
            return;

        if (getActivity() == null)
            return;

        final EventsPrefsActivity activity = (EventsPrefsActivity)getActivity();

        Context context = activity.getApplicationContext();

        long event_id = activity.event_id;
        int newEventMode = activity.newEventMode;
        int predefinedEventIndex = activity.predefinedEventIndex;
        final Event event = activity.getEventFromPreferences(event_id, newEventMode, predefinedEventIndex);

        if (event != null) {
            int order = 1;

            // not enabled some sensor
            if (event.isEnabledSomeSensor(context)) {
                Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_NOT_ENABLED_SOME_SENSOR);
                        preference.setIconSpaceReserved(false);
                        preference.setWidgetLayoutResource(R.layout.widget_exclamation_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-99);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    String _title = order + ". " + getString(R.string.event_preferences_no_sensor_is_enabled);
                    ++order;
                    Spannable title = new SpannableString(_title);
                    title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                    preference.setTitle(title);
                    _title = getString(R.string.event_preferences_sensor_parameters_location_summary);
                    Spannable summary = new SpannableString(_title);
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                    preference.setSummary(summary);
                }
            }

            // not some permissions
            if (Permissions.checkEventPermissions(context, event).size() == 0) {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                //PPApplication.logE("EventsPrefsFragment.setRedTextToPreferences", "event._id=" + event._id);
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_GRANT_PERMISSIONS);
                        preference.setIconSpaceReserved(false);
                        if (event._id > 0)
                            preference.setWidgetLayoutResource(R.layout.widget_start_activity_preference);
                        else
                            preference.setWidgetLayoutResource(R.layout.widget_exclamation_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-98);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    String _title = order + ". " + getString(R.string.preferences_grantPermissions_title);
                    ++order;
                    Spannable title = new SpannableString(_title);
                    title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                    preference.setTitle(title);
                    _title = getString(R.string.preferences_grantPermissions_summary) + " " +
                                getString(R.string.event_preferences_red_sensors_summary) + " " +
                                getString(R.string.event_preferences_sensor_parameters_location_summary);
                    Spannable summary = new SpannableString(_title);
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                    preference.setSummary(summary);

                    if (event._id > 0) {
                        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                Permissions.grantEventPermissions(activity, event/*, false, true*/);
                                return false;
                            }
                        });
                    }
                }
            }

            // not enabled accessibility service
            int accessibilityEnabled = event.isAccessibilityServiceEnabled(context, false);
            Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
            if (accessibilityEnabled == 1) {
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                        preference.setIconSpaceReserved(false);
                        preference.setWidgetLayoutResource(R.layout.widget_start_activity_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-97);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    int stringRes = R.string.preferences_not_enabled_accessibility_service_title;
                    if (accessibilityEnabled == -2)
                        stringRes = R.string.preferences_not_installed_PPPExtender_title;
                    else if (accessibilityEnabled == -1)
                        stringRes = R.string.preferences_old_version_PPPExtender_title;
                    String _title = order + ". " + getString(stringRes);
                    ++order;
                    Spannable title = new SpannableString(_title);
                    title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                    preference.setTitle(title);
                    if ((accessibilityEnabled == -1) || (accessibilityEnabled == -2)) {
                        _title = getString(R.string.event_preferences_red_install_PPPExtender) + " " +
                                getString(R.string.event_preferences_red_sensors_summary) + " " +
                                getString(R.string.event_preferences_sensor_parameters_location_summary);
                        Spannable summary = new SpannableString(_title);
                        summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                        preference.setSummary(summary);

                        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                installExtender(getString(R.string.event_preferences_PPPExtenderInstallInfo_summary) + "\n\n" +
                                        getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_2) + " " +
                                        getString(R.string.event_preferences_PPPExtenderInstallInfo_summary_3));
                                return false;
                            }
                        });
                    }
                    else {
                        _title = getString(R.string.event_preferences_red_enable_PPPExtender) + " " +
                                getString(R.string.event_preferences_red_sensors_summary) + " " +
                                getString(R.string.event_preferences_sensor_parameters_location_summary);
                        Spannable summary = new SpannableString(_title);
                        summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                        preference.setSummary(summary);

                        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                enableExtender();
                                return false;
                            }
                        });
                    }
                }
            }

            // not is runnable
            if (event.isRunnable(context, false)) {
                preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_NOT_IS_RUNNABLE);
                        preference.setIconSpaceReserved(false);
                        preference.setWidgetLayoutResource(R.layout.widget_exclamation_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-100);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    String _title = order + ". " + getString(R.string.event_preferences_not_set_underlined_parameters);
                    ++order;
                    Spannable title = new SpannableString(_title);
                    title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                    preference.setTitle(title);
                    _title = getString(R.string.event_preferences_not_set_underlined_parameters_summary) + " " +
                                getString(R.string.event_preferences_red_sensors_summary) + " " +
                                getString(R.string.event_preferences_sensor_parameters_location_summary);
                    Spannable summary = new SpannableString(_title);
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                    preference.setSummary(summary);
                }
            }
        }
        else {
            Preference preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("rootScreen");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("rootScreen");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("rootScreen");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("rootScreen");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
    }

    void doMobileCellsRegistrationCountDownBroadcastReceiver(long millisUntilFinished) {
        MobileCellsRegistrationDialogPreferenceX preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_REGISTRATION);
        if (preference != null) {
            //Log.d("mobileCellsRegistrationCountDownBroadcastReceiver", "xxx");
            preference.updateInterface(millisUntilFinished, false);
            preference.setSummaryDDP(millisUntilFinished);
        }
    }

    void doMobileCellsRegistrationStoppedBroadcastReceiver() {
        MobileCellsPreferenceX preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
        if (preference != null)
            preference.refreshListView(true, Integer.MAX_VALUE);
    }

    private void installExtender(String dialogText) {
        if (getActivity() == null)
            return;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_extender_dialog_title);
        dialogBuilder.setMessage(dialogText);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_install, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!getActivity().isFinishing())
            dialog.show();
    }

    private void enableExtender() {
        if (getActivity() == null)
            return;

        boolean ok = false;
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, getActivity())) {
            try {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                ok = true;
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
        if (!ok) {
            if (getActivity() != null) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }

}