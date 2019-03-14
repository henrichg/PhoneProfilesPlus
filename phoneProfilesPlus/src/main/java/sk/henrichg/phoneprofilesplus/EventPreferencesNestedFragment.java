package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

public class EventPreferencesNestedFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private long event_id;
    int startupSource;

    private Event event;
    //private boolean first_start_activity;
    PreferenceManager prefMng;
    SharedPreferences preferences;
    private Context context;

    private MobileCellsRegistrationCountDownBroadcastReceiver mobileCellsRegistrationCountDownBroadcastReceiver = null;
    private MobileCellsRegistrationStoppedBroadcastReceiver mobileCellsRegistrationStoppedBroadcastReceiver = null;

    private static final String PREFS_NAME_ACTIVITY = "event_preferences_activity";
    //static final String PREFS_NAME_FRAGMENT = "event_preferences_fragment";

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
    private static final String PREF_MOBILE_CELLS_REGISTRATION = "eventMobileCellsRegistration";
    private static final int RESULT_WIFI_LOCATION_SYSTEM_SETTINGS = 1989;
    private static final int RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = 1990;
    private static final int RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS = 1991;
    private static final int RESULT_WIFI_KEEP_ON_SYSTEM_SETTINGS = 1992;
    private static final int RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = 1993;

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);
        // this is really important in order to save the state across screen
        // configuration changes for example
        //setRetainInstance(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            event_id = bundle.getLong(PPApplication.EXTRA_EVENT_ID, 0);
            startupSource = bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0);
        }

        context = getActivity().getBaseContext();

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();

        event = new Event();

        if (mobileCellsRegistrationCountDownBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN);
            mobileCellsRegistrationCountDownBroadcastReceiver =
                    new MobileCellsRegistrationCountDownBroadcastReceiver(
                            (MobileCellsRegistrationDialogPreference)prefMng.findPreference(PREF_MOBILE_CELLS_REGISTRATION));
            context.registerReceiver(mobileCellsRegistrationCountDownBroadcastReceiver, intentFilter);
        }

        if (mobileCellsRegistrationStoppedBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELLS);
            mobileCellsRegistrationStoppedBroadcastReceiver =
                    new MobileCellsRegistrationStoppedBroadcastReceiver(
                            (MobileCellsPreference)prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS));
            context.registerReceiver(mobileCellsRegistrationStoppedBroadcastReceiver, intentFilter);
        }
    }

    public static String getPreferenceName(int startupSource) {
        String PREFS_NAME;
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        /*else
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;*/
        else
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        return PREFS_NAME;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(EventPreferencesNestedFragment.getPreferenceName(startupSource));
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        //RingtonePreference notificationSoundPreference = (RingtonePreference)prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND);
        //notificationSoundPreference.setEnabled(PPApplication.notificationStatusBar);

        setPreferencesStatusPreference();

        event.checkPreferences(prefMng, context);

        Preference notificationAccessPreference = prefMng.findPreference(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS);
        if (notificationAccessPreference != null) {
            //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String activity;
                    if (Build.VERSION.SDK_INT >= 22)
                        activity = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                    else
                        activity = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
                    if (GlobalGUIRoutines.activityActionExists(activity, context)) {
                        Intent intent = new Intent(activity);
                        startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                if (positive != null) positive.setAllCaps(false);
                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                if (negative != null) negative.setAllCaps(false);
                            }
                        });*/
                        if (!getActivity().isFinishing())
                            dialog.show();
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
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
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
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //    @Override
                        //    public void onShow(DialogInterface dialog) {
                        //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //        if (positive != null) positive.setAllCaps(false);
                        //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //        if (negative != null) negative.setAllCaps(false);
                        //    }
                        //});
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
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
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "locationScanningCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(intent, RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        if (!getActivity().isFinishing())
                            dialog.show();
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
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "wifiScanningCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(intent, RESULT_WIFI_LOCATION_SYSTEM_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                    return false;
                }
            });
        }
        if (Build.VERSION.SDK_INT >= 27) {
            preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventWifiCategory");
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
                        //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_WIFI_IP_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_KEEP_ON_SYSTEM_SETTINGS);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            if (!getActivity().isFinishing())
                                dialog.show();
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
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "bluetoothScanningCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(intent, RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        if (!getActivity().isFinishing())
                            dialog.show();
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
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "orientationScanningCategory");
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
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
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
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //    @Override
                        //    public void onShow(DialogInterface dialog) {
                        //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //        if (positive != null) positive.setAllCaps(false);
                        //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //        if (negative != null) negative.setAllCaps(false);
                        //    }
                        //});
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
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
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "mobileCellsScanningCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(intent, RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(PREF_USE_PRIORITY_APP_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setSummary(getString(R.string.event_preferences_event_priorityInfo_summary)+"\n"+
                                  getString(R.string.phone_profiles_pref_eventUsePriorityAppSettings_summary));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "eventRunCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_USE_PRIORITY_SETTINGS);
                    return false;
                }
            });
        }
        MobileCellsRegistrationDialogPreference mobileCellsRegistrationDialogPreference =
                (MobileCellsRegistrationDialogPreference)prefMng.findPreference(PREF_MOBILE_CELLS_REGISTRATION);
        if (mobileCellsRegistrationDialogPreference != null) {
            mobileCellsRegistrationDialogPreference.event_id = event_id;
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
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
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
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //    @Override
                        //    public void onShow(DialogInterface dialog) {
                        //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //        if (positive != null) positive.setAllCaps(false);
                        //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //        if (negative != null) negative.setAllCaps(false);
                        //    }
                        //});
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
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
                        Intent intent = packageManager.getLaunchIntentForPackage("sk.henrichg.phoneprofilesplusextender");
                        if (intent != null) {
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.event_preferences_extender_not_installed);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //    @Override
                        //    public void onShow(DialogInterface dialog) {
                        //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //        if (positive != null) positive.setAllCaps(false);
                        //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //        if (negative != null) negative.setAllCaps(false);
                        //    }
                        //});
                        if (!getActivity().isFinishing())
                            dialog.show();
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
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
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
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //    @Override
                        //    public void onShow(DialogInterface dialog) {
                        //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //        if (positive != null) positive.setAllCaps(false);
                        //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //        if (negative != null) negative.setAllCaps(false);
                        //    }
                        //});
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
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
                        Intent intent = packageManager.getLaunchIntentForPackage("sk.henrichg.phoneprofilesplusextender");
                        if (intent != null) {
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.event_preferences_extender_not_installed);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //    @Override
                        //    public void onShow(DialogInterface dialog) {
                        //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //        if (positive != null) positive.setAllCaps(false);
                        //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //        if (negative != null) negative.setAllCaps(false);
                        //    }
                        //});
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                    return false;
                }
            });
        }

    }

    private void setPreferencesStatusPreference() {
        Bundle bundle = this.getArguments();

        if (bundle.getBoolean(EXTRA_NESTED, true))
            return;

        //Log.e("***** EventPreferencesNestedFragment.setPreferencesStatusPreference","xxx");

        long event_id = bundle.getLong(PPApplication.EXTRA_EVENT_ID, 0);
        int newEventMode = bundle.getInt(EditorProfilesActivity.EXTRA_NEW_EVENT_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
        int predefinedEventIndex = bundle.getInt(EditorProfilesActivity.EXTRA_PREDEFINED_EVENT_INDEX, 0);
        final Event event = ((EventPreferencesActivity) getActivity())
                .getEventFromPreferences(event_id, newEventMode, predefinedEventIndex);

        if (event != null) {
            // not is runnable
            if (event.isRunnable(context, false)) {
                Preference preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                    preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                    preference = new Preference(context);
                    preference.setKey(PRF_NOT_IS_RUNNABLE);
                    preference.setWidgetLayoutResource(R.layout.exclamation_preference);
                    preference.setLayoutResource(R.layout.mp_preference_material_widget);
                    preference.setOrder(-100);
                    preferenceCategory.addPreference(preference);
                }
                Spannable title = new SpannableString(getString(R.string.event_preferences_not_set_underlined_parameters));
                title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                preference.setTitle(title);
            }

            // not enabled some sensor
            if (event.isEnabledSomeSensor(context)) {
                Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                    preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                    preference = new Preference(context);
                    preference.setKey(PRF_NOT_ENABLED_SOME_SENSOR);
                    preference.setWidgetLayoutResource(R.layout.exclamation_preference);
                    preference.setLayoutResource(R.layout.mp_preference_material_widget);
                    preference.setOrder(-99);
                    preferenceCategory.addPreference(preference);
                }
                Spannable title = new SpannableString(getString(R.string.event_preferences_no_sensor_is_enabled));
                title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                preference.setTitle(title);
            }

            // not some permissions
            if (Permissions.checkEventPermissions(context, event).size() == 0) {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                    preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                    preference = new Preference(context);
                    preference.setKey(PRF_GRANT_PERMISSIONS);
                    preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setLayoutResource(R.layout.mp_preference_material_widget);
                    preference.setOrder(-98);
                    preferenceCategory.addPreference(preference);
                }

                Spannable title = new SpannableString(getString(R.string.preferences_grantPermissions_title));
                title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                preference.setTitle(title);
                Spannable summary = new SpannableString(getString(R.string.preferences_grantPermissions_summary));
                summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                preference.setSummary(summary);

                final Activity activity = getActivity();
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Permissions.grantEventPermissions(activity, event, false, true);
                        return false;
                    }
                });
            }

            // not enabled accessibility service
            int accessibilityEnabled = event.isAccessibilityServiceEnabled(context, false);
            if (accessibilityEnabled == 1) {
                Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                    preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                    preference = new Preference(context);
                    preference.setKey(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                    preference.setWidgetLayoutResource(R.layout.exclamation_preference);
                    preference.setLayoutResource(R.layout.mp_preference_material_widget);
                    preference.setOrder(-97);
                    preferenceCategory.addPreference(preference);
                }
                int stringRes = R.string.preferences_not_enabled_accessibility_service_title;
                if (accessibilityEnabled == -2)
                    stringRes = R.string.preferences_not_installed_PPPExtender_title;
                else
                if (accessibilityEnabled == -1)
                    stringRes = R.string.preferences_old_version_PPPExtender_title;
                Spannable title = new SpannableString(getString(stringRes));
                title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                preference.setTitle(title);
            }
        }
        else {
            Preference preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("eventPreferenceScreen");
                preferenceCategory.removePreference(preference);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        //Log.e("****** EventPreferencesNestedFragment.onDestroy","xxx");
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}

        if (mobileCellsRegistrationCountDownBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(mobileCellsRegistrationCountDownBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mobileCellsRegistrationCountDownBroadcastReceiver = null;
        }

        if (mobileCellsRegistrationStoppedBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(mobileCellsRegistrationStoppedBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mobileCellsRegistrationStoppedBroadcastReceiver = null;
        }

        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Log.d("EventPreferencesFragment.doOnActivityResult", "requestCode="+requestCode);

        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EVENT) {
            setPreferencesStatusPreference();
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            event._eventPreferencesNotification.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS) {
            event._eventPreferencesApplication.checkPreferences(prefMng, context);
            event._eventPreferencesOrientation.checkPreferences(prefMng, context);
            event._eventPreferencesSMS.checkPreferences(prefMng, context);
            event._eventPreferencesCall.checkPreferences(prefMng, context);
            setPreferencesStatusPreference();
            PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventPreferencesNestedFragment.doOnActivityResult");
            ActivateProfileHelper.updateGUI(context.getApplicationContext(), true, true);
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
        if (requestCode == LocationGeofencePreference.RESULT_GEOFENCE_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                LocationGeofencePreference preference = (LocationGeofencePreference)prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_GEOFENCES);
                if (preference != null) {
                    preference.setGeofenceFromEditor(/*geofenceId*/);
                }
            }
            /*if (EventPreferencesFragment.changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    //long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    // this persistGeofence, for multiselect this mus only refresh listView in preference
                    EventPreferencesFragment.changedLocationGeofencePreference.setGeofenceFromEditor();
                    EventPreferencesFragment.changedLocationGeofencePreference = null;
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
            event._eventPreferencesWifi.checkPreferences(prefMng, context);
            setPreferencesStatusPreference();
            PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventPreferencesNestedFragment.doOnActivityResult");
            ActivateProfileHelper.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS) {
            event._eventPreferencesBluetooth.checkPreferences(prefMng, context);
            setPreferencesStatusPreference();
            PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventPreferencesNestedFragment.doOnActivityResult");
            ActivateProfileHelper.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS) {
            event._eventPreferencesLocation.checkPreferences(prefMng, context);
            setPreferencesStatusPreference();
            PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventPreferencesNestedFragment.doOnActivityResult");
            ActivateProfileHelper.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS) {
            event._eventPreferencesMobileCells.checkPreferences(prefMng, context);
            setPreferencesStatusPreference();
            PPApplication.logE("ActivateProfileHelper.updateGUI", "from EventPreferencesNestedFragment.doOnActivityResult");
            ActivateProfileHelper.updateGUI(context.getApplicationContext(), true, true);
        }
        if (requestCode == RESULT_USE_PRIORITY_SETTINGS) {
            event.setSummary(prefMng, Event.PREF_EVENT_PRIORITY, preferences, context);
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            RingtonePreference preference = (RingtonePreference) prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND_START);
            if (preference != null)
                preference.refreshListView();
            preference = (RingtonePreference) prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND_END);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_WIFI_BT_SCAN_DIALOG) {
            WifiSSIDPreference wifiPreference = (WifiSSIDPreference) prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_SSID);
            if (wifiPreference != null)
                wifiPreference.refreshListView(true, "");
            BluetoothNamePreference bluetoothPreference = (BluetoothNamePreference) prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
            if (bluetoothPreference != null)
                bluetoothPreference.refreshListView(true, "");
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG) {
            MobileCellsPreference preference = (MobileCellsPreference) prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
            if (preference != null)
                preference.refreshListView(true, Integer.MAX_VALUE);
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG) {
            MobileCellsRegistrationDialogPreference preference = (MobileCellsRegistrationDialogPreference) prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_REGISTRATION);
            if (preference != null)
                preference.startRegistration();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CALENDAR_DIALOG) {
            CalendarsMultiSelectDialogPreference preference = (CalendarsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesCalendar.PREF_EVENT_CALENDAR_CALENDARS);
            if (preference != null)
                preference.refreshListView(true);
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CONTACT_DIALOG) {
            ContactsMultiSelectDialogPreference preference1 = (ContactsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(true);
            preference1 = (ContactsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(true);
            ContactGroupsMultiSelectDialogPreference preference2 = (ContactGroupsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(true);
            preference2 = (ContactGroupsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(true);
        }
        /*if (requestCode == NFCTagPreference.RESULT_NFC_TAG_READ_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                NFCTagPreference preference = (NFCTagPreference) prefMng.findPreference(EventPreferencesNFC.PREF_EVENT_NFC_NFC_TAGS);
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
        if (requestCode == NFCTagPreference.RESULT_NFC_TAG_WRITE) {
            if (resultCode == Activity.RESULT_OK) {
                NFCTagPreference preference = (NFCTagPreference) prefMng.findPreference(EventPreferencesNFC.PREF_EVENT_NFC_NFC_TAGS);
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

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(Event.PREF_EVENT_NAME)) {
            String value = sharedPreferences.getString(key, "");
            Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
            toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + value);
        }

        //eventTypeChanged = false;

        event.setSummary(prefMng, key, sharedPreferences, context);

        setPreferencesStatusPreference();

        //Activity activity = getActivity();
        //boolean canShow = (EditorProfilesActivity.mTwoPane) && (activity instanceof EditorProfilesActivity);
        //canShow = canShow || ((!EditorProfilesActivity.mTwoPane) && (activity instanceof EventPreferencesActivity));
        //if (canShow)
        //    showActionMode();
        EventPreferencesActivity activity = (EventPreferencesActivity)getActivity();
        EventPreferencesActivity.showSaveMenu = true;
        activity.invalidateOptionsMenu();
    }

    @Override
    protected String getSavedInstanceStateKeyName() {
        return "EventPreferencesFragment_PreferenceScreenKey";
    }

    public class MobileCellsRegistrationCountDownBroadcastReceiver extends BroadcastReceiver {

        final MobileCellsRegistrationDialogPreference preference;

        MobileCellsRegistrationCountDownBroadcastReceiver(MobileCellsRegistrationDialogPreference preference) {
            this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (preference != null) {
                //Log.d("mobileCellsRegistrationCountDownBroadcastReceiver", "xxx");
                long millisUntilFinished = intent.getLongExtra(MobileCellsRegistrationService.EXTRA_COUNTDOWN, 0L);
                preference.updateInterface(millisUntilFinished, false);
                preference.setSummaryDDP(millisUntilFinished);
            }
        }
    }

    public class MobileCellsRegistrationStoppedBroadcastReceiver extends BroadcastReceiver {

        final MobileCellsPreference preference;

        MobileCellsRegistrationStoppedBroadcastReceiver(MobileCellsPreference preference) {
            this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (preference != null) {
                //Log.d("MobileCellsRegistrationStoppedBroadcastReceiver", "xxx");
                preference.refreshListView(true, Integer.MAX_VALUE);
            }
        }
    }
}
