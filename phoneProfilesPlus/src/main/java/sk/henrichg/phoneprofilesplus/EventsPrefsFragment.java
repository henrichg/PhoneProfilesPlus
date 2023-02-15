package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

public class EventsPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;

    private boolean nestedFragment = false;

    private Event event;

    //static boolean forceStart;

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
    private static final int RESULT_NOTIFICATION_SCANNING_APP_SETTINGS = 1997;
    private static final int RESULT_PERIODIC_SCANNING_APP_SETTINGS = 1997;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // is required for to not call onCreate and onDestroy on orientation change
        //noinspection deprecation
        setRetainInstance(true);

        nestedFragment = !(this instanceof EventsPrefsActivity.EventsPrefsRoot);

        initPreferenceFragment(/*savedInstanceState*/);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @NonNull
    @Override
    public RecyclerView onCreateRecyclerView (@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, Bundle state) {
        final RecyclerView view = super.onCreateRecyclerView(inflater, parent, state);
        view.setItemAnimator(null);
        view.setLayoutAnimation(null);

        // do not use this, because this generates exception on orientation change:
        // java.lang.NullPointerException: Attempt to invoke virtual method 'android.widget.ScrollBarDrawable
        // android.widget.ScrollBarDrawable.mutate()' on a null object reference
        //view.setScrollbarFadingEnabled(false);

        return view;
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        PreferenceDialogFragmentCompat dialogFragment = null;

        if (preference instanceof PPListPreference)
        {
            ((PPListPreference)preference).fragment = new PPListPreferenceFragment();
            dialogFragment = ((PPListPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof PPMultiSelectListPreference)
        {
            ((PPMultiSelectListPreference)preference).fragment = new PPMultiSelectListPreferenceFragment();
            dialogFragment = ((PPMultiSelectListPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfilePreference) {
            ((ProfilePreference) preference).fragment = new ProfilePreferenceFragment();
            dialogFragment = ((ProfilePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof InfoDialogPreference) {
            ((InfoDialogPreference) preference).fragment = new InfoDialogPreferenceFragment();
            dialogFragment = ((InfoDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof DurationDialogPreference) {
            ((DurationDialogPreference) preference).fragment = new DurationDialogPreferenceFragment();
            dialogFragment = ((DurationDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ApplicationsMultiSelectDialogPreference) {
            ((ApplicationsMultiSelectDialogPreference) preference).fragment = new ApplicationsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ApplicationsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BetterNumberPickerPreference) {
            ((BetterNumberPickerPreference) preference).fragment = new BetterNumberPickerPreferenceFragment();
            dialogFragment = ((BetterNumberPickerPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof RingtonePreference) {
            ((RingtonePreference) preference).fragment = new RingtonePreferenceFragment();
            dialogFragment = ((RingtonePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof LocationGeofencePreference) {
            ((LocationGeofencePreference) preference).fragment = new LocationGeofencePreferenceFragment();
            dialogFragment = ((LocationGeofencePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfileMultiSelectPreference) {
            ((ProfileMultiSelectPreference) preference).fragment = new ProfileMultiSelectPreferenceFragment();
            dialogFragment = ((ProfileMultiSelectPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof DaysOfWeekPreference) {
            ((DaysOfWeekPreference) preference).fragment = new DaysOfWeekPreferenceFragment();
            dialogFragment = ((DaysOfWeekPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        /*
        else
        if (preference instanceof TimePreferenceX) {
            ((TimePreferenceX) preference).fragment = new TimePreferenceFragmentX();
            dialogFragment = ((TimePreferenceX) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }*/
        else
        if (preference instanceof TimeDialogPreference) {
            ((TimeDialogPreference) preference).fragment = new TimeDialogPreferenceFragment();
            dialogFragment = ((TimeDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof CalendarsMultiSelectDialogPreference) {
            ((CalendarsMultiSelectDialogPreference) preference).fragment = new CalendarsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((CalendarsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof SearchStringPreference) {
            ((SearchStringPreference) preference).fragment = new SearchStringPreferenceFragment();
            dialogFragment = ((SearchStringPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ContactGroupsMultiSelectDialogPreference) {
            ((ContactGroupsMultiSelectDialogPreference) preference).fragment = new ContactGroupsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ContactGroupsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ContactsMultiSelectDialogPreference) {
            ((ContactsMultiSelectDialogPreference) preference).fragment = new ContactsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ContactsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof WifiSSIDPreference) {
            ((WifiSSIDPreference) preference).fragment = new WifiSSIDPreferenceFragment();
            dialogFragment = ((WifiSSIDPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BluetoothNamePreference) {
            ((BluetoothNamePreference) preference).fragment = new BluetoothNamePreferenceFragment();
            dialogFragment = ((BluetoothNamePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof MobileCellsRegistrationDialogPreference) {
            ((MobileCellsRegistrationDialogPreference) preference).fragment = new MobileCellsRegistrationDialogPreferenceFragment();
            dialogFragment = ((MobileCellsRegistrationDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof MobileCellsPreference) {
            ((MobileCellsPreference) preference).fragment = new MobileCellsPreferenceFragment();
            dialogFragment = ((MobileCellsPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof NFCTagPreference) {
            ((NFCTagPreference) preference).fragment = new NFCTagPreferenceFragment();
            dialogFragment = ((NFCTagPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof VolumeDialogPreference)
        {
            ((VolumeDialogPreference)preference).fragment = new VolumeDialogPreferenceFragment();
            dialogFragment = ((VolumeDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ExtenderDialogPreference)
        {
            ((ExtenderDialogPreference)preference).fragment = new ExtenderDialogPreferenceFragment();
            dialogFragment = ((ExtenderDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null) {
            if ((getActivity() != null) && (!getActivity().isFinishing())) {
                FragmentManager fragmentManager = getParentFragmentManager();//getFragmentManager();
                //if (fragmentManager != null) {
                //noinspection deprecation
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".EventsPrefsActivity.DIALOG");
                //}
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() == null)
            return;

        EventsPrefsActivity activity = (EventsPrefsActivity) getActivity();

        final Context context = activity.getBaseContext();

//        PPApplication.forceStartOrientationScanner(context);
//        forceStart = true;

        // must be used handler for rewrite toolbar title/subtitle
        final EventsPrefsFragment fragment = this;
        final TextView preferenceSubTitle = getActivity().findViewById(R.id.activity_preferences_subtitle);

        Handler handler = new Handler(getActivity().getMainLooper());
        handler.postDelayed(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EventsPrefsFragment.onActivityCreated");
            if (getActivity() == null)
                return;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            final String eventName = preferences.getString(Event.PREF_EVENT_NAME, "");
            Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
            if (nestedFragment) {
                toolbar.setTitle(getString(R.string.title_activity_event_preferences));
                preferenceSubTitle.setVisibility(View.VISIBLE);

                Drawable triangle = ContextCompat.getDrawable(getActivity(), R.drawable.ic_submenu_triangle);
                if (triangle != null) {
                    SpannableString headerTitle = new SpannableString("    " +
                            fragment.getPreferenceScreen().getTitle());
                    triangle.setBounds(0, 8, 50, 48);
                    headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    preferenceSubTitle.setText(headerTitle);
                } else
                    preferenceSubTitle.setText(fragment.getPreferenceScreen().getTitle());

                //toolbar.setTitle(fragment.getPreferenceScreen().getTitle());

                toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + eventName);
            } else {
                preferenceSubTitle.setVisibility(View.GONE);

                toolbar.setTitle(getString(R.string.title_activity_event_preferences));
                toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + eventName);
            }

        }, 200);

        setDivider(null); // this remove dividers for categories

        setRedTextToPreferences();

        // update preference summary and also category summary
        event.checkSensorsPreferences(prefMng, !nestedFragment, getActivity().getBaseContext());
        event.setAllSummary(prefMng, preferences, getActivity().getBaseContext());

        Preference notificationAccessPreference = prefMng.findPreference(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS);
        if (notificationAccessPreference != null) {
            //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            notificationAccessPreference.setOnPreferenceClickListener(preference -> {
                boolean ok = false;
                String activity1;
                activity1 = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                if (GlobalGUIRoutines.activityActionExists(activity1, context)) {
                    try {
                        Intent intent = new Intent(activity1);
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        /*
        Preference extenderPreference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(getActivity(), null);
                return false;
            });
        }
        */
        /*
        Preference accessibilityPreference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference -> {
                enableExtender();
                return false;
            });
        }
        */
        Preference preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference1 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "locationScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_LOCATION_APP_SETTINGS);
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference12 -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference12.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference13 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "wifiScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_WIFI_SCANNING_APP_SETTINGS);
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference14 -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_WIFI_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference14.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesTime.PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference15 -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_TIME_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference15.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
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
                preference.setOnPreferenceClickListener(preference16 -> {
                    boolean ok = false;
                    //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_WIFI_SETTINGS, context.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //noinspection deprecation
                            startActivityForResult(intent, RESULT_WIFI_KEEP_ON_SYSTEM_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        if (getActivity() != null) {
                            PPAlertDialog dialog = new PPAlertDialog(
                                    preference16.getTitle(),
                                    getString(R.string.setting_screen_not_found_alert),
                                    getString(android.R.string.ok),
                                    null,
                                    null, null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    true, true,
                                    false, false,
                                    true,
                                    getActivity()
                            );

                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                    }
                    return false;
                });
            }
        }
        preference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference17 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "bluetoothScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_BLUETOOTH_SCANNING_APP_SETTINGS);
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference18 -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference18.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        preference = prefMng.findPreference(PREF_ORIENTATION_SCANNING_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference19 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "orientationScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_ORIENTATION_SCANNING_SETTINGS);
                return false;
            });
        }
        /*
        extenderPreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference110 -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(getActivity(), null);
                return false;
            });
        }
        */
        /*
        Preference orientationPreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ACCESSIBILITY_SETTINGS);
        if (orientationPreference != null) {
            //orientationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            orientationPreference.setOnPreferenceClickListener(preference111 -> {
                enableExtender();
                return false;
            });
        }
        */
        preference = prefMng.findPreference(PREF_MOBILE_CELLS_SCANNING_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference112 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "mobileCellsScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_MOBILE_CELLS_SCANNING_SETTINGS);
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference113 -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference113.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesTime.PREF_EVENT_TIME_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference114 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "periodicScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_TIME_SCANNING_APP_SETTINGS);
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesCalendar.PREF_EVENT_CALENDAR_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference115 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "periodicScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_CALENDAR_SCANNING_APP_SETTINGS);
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference13 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "periodicScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_PERIODIC_SCANNING_APP_SETTINGS);
                return false;
            });
        }

        preference = prefMng.findPreference(PREF_USE_PRIORITY_APP_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference116 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "eventRunCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_USE_PRIORITY_SETTINGS);
                return false;
            });
        }
        preference = prefMng.findPreference(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference117 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "notificationScanningCategoryRoot");
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                //noinspection deprecation
                startActivityForResult(intent, RESULT_NOTIFICATION_SCANNING_APP_SETTINGS);
                return false;
            });
        }
        MobileCellsRegistrationDialogPreference mobileCellsRegistrationDialogPreference =
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
        /*
        extenderPreference = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference118 -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(getActivity(), null);
                return false;
            });
        }
        */
        /*
        Preference smsPreference = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_ACCESSIBILITY_SETTINGS);
        if (smsPreference != null) {
            //smsPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            smsPreference.setOnPreferenceClickListener(preference119 -> {
                enableExtender();
                return false;
            });
        }
        */
        /*
        smsPreference = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_LAUNCH_EXTENDER);
        if (smsPreference != null) {
            //smsPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            smsPreference.setOnPreferenceClickListener(preference120 -> {
                if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
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
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference120.getTitle(),
                                getString(R.string.event_preferences_extender_not_installed),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        */
        /*
        extenderPreference = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference121 -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(getActivity(), null);
                return false;
            });
        }
        */
        /*
        Preference callPreference = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_ACCESSIBILITY_SETTINGS);
        if (callPreference != null) {
            //smsPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            callPreference.setOnPreferenceClickListener(preference122 -> {
                enableExtender();
                return false;
            });
        }
        */
        /*
        callPreference = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_LAUNCH_EXTENDER);
        if (callPreference != null) {
            //callPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            callPreference.setOnPreferenceClickListener(preference123 -> {
                if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
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
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference123.getTitle(),
                                getString(R.string.event_preferences_extender_not_installed),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        */
        /*
        accessibilityPreference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_LAUNCH_EXTENDER);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference124 -> {
                if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
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
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference124.getTitle(),
                                getString(R.string.event_preferences_extender_not_installed),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        */
        /*
        accessibilityPreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_LAUNCH_EXTENDER);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference125 -> {
                if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
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
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference125.getTitle(),
                                getString(R.string.event_preferences_extender_not_installed),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        */
        preference = prefMng.findPreference(EventPreferencesBattery.PREF_EVENT_BATTERY_BATTERY_SAVER_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference126 -> {
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
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference126.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }

        InfoDialogPreference infoDialogPreference = prefMng.findPreference("eventSensorsInfo");
        if (infoDialogPreference != null) {
            String info = "<ul><li>" + getString(R.string.event_preferences_sensorsInfo_summary) + "</li></ul>" +
                    "<br>" +
                    "<ul><li>" + getString(R.string.event_preferences_sensorsInfo_summary_2) + "</li></ul>";
            infoDialogPreference.setInfoText(info);
            infoDialogPreference.setIsHtml(true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() == null)
            return;

        //Log.e("EventPrefsFragment.onResume", "xxxxxx");

        // this is important for update preferences after PPPPS and Extender installation
        event.checkSensorsPreferences(prefMng, !nestedFragment, getActivity().getBaseContext());
        event.setAllSummary(prefMng, preferences, getActivity().getBaseContext());

        if (!nestedFragment) {
            final Context context = getActivity().getBaseContext();

//            event._eventPreferencesApplication.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesOrientation.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesSMS.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesCall.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesNotification.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplication.logE("[PPP_NOTIFICATION] EventsPrefsFragment.onResume", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
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

//            forceStart = false;
//            if (getActivity() != null) {
//                final Context context = getActivity().getBaseContext();
//                PPApplication.restartOrientationScanner(context);
//            }

        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Event.PREF_EVENT_NAME)) {
            String value = sharedPreferences.getString(key, "");
            if (getActivity() != null) {
                // must be used handler for rewrite toolbar title/subtitle
                final String _value = value;
                Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EventsPrefsFragment.onSharedPreferenceChanged");
                    if (getActivity() == null)
                        return;

                    Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                    toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + _value);
                }, 200);
            }
        }

        if (getActivity() == null)
            return;

        event.checkSensorsPreferences(prefMng, !nestedFragment, getActivity().getBaseContext());
        event.setSummary(prefMng, key, sharedPreferences, getActivity(), true);

        setRedTextToPreferences();

        EventsPrefsActivity activity = (EventsPrefsActivity)getActivity();
        if (activity != null) {
            activity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        }
    }

    void doOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (getActivity() == null)
            return;

        final Context context = getActivity().getBaseContext();

        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EVENT)) {
            setRedTextToPreferences();
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            PPApplication.restartNotificationScanner(context);

            event._eventPreferencesNotification.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS) {
            // this is important for update all preferences
            event.checkSensorsPreferences(prefMng, !nestedFragment, getActivity().getBaseContext());
            event.setAllSummary(prefMng, preferences, getActivity().getBaseContext());
//            event._eventPreferencesApplication.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesOrientation.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesSMS.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesCall.checkPreferences(prefMng, !nestedFragment, context);
//
//            event._eventPreferencesApplication.setSummary(prefMng,
//                    EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, preferences, context);
//            event._eventPreferencesOrientation.setSummary(prefMng,
//                    EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, preferences, context);
//            event._eventPreferencesSMS.setSummary(prefMng,
//                    EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, preferences, context);
//            event._eventPreferencesCall.setSummary(prefMng,
//                    EventPreferencesCall.PREF_EVENT_CALL_ENABLED, preferences, context);

            setRedTextToPreferences();
//            PPApplication.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (1)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_TIME_SCANNING_APP_SETTINGS) {
            event._eventPreferencesTime.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_CALENDAR_SCANNING_APP_SETTINGS) {
            event._eventPreferencesCalendar.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_WIFI_SCANNING_APP_SETTINGS) {
            event._eventPreferencesWifi.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_WIFI_KEEP_ON_SYSTEM_SETTINGS) {
            event._eventPreferencesWifi.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_BLUETOOTH_SCANNING_APP_SETTINGS) {
            event._eventPreferencesBluetooth.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_LOCATION_APP_SETTINGS) {
            event._eventPreferencesLocation.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_PERIODIC_SCANNING_APP_SETTINGS) {
            event._eventPreferencesPeriodic.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == LocationGeofencePreference.RESULT_GEOFENCE_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                LocationGeofencePreference preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_GEOFENCES);
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
            event._eventPreferencesOrientation.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_MOBILE_CELLS_SCANNING_SETTINGS) {
            event._eventPreferencesMobileCells.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_NOTIFICATION_SCANNING_APP_SETTINGS) {
            event._eventPreferencesNotification.checkPreferences(prefMng, !nestedFragment, context);
        }
        if (requestCode == RESULT_WIFI_LOCATION_SYSTEM_SETTINGS) {
            WifiSSIDPreference preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_SSID);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesWifi.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplication.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (2)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS) {
            BluetoothNamePreference preference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesBluetooth.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplication.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (3)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS) {
            LocationGeofencePreference preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_GEOFENCES);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesLocation.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplication.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (4)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS) {
            MobileCellsPreference preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesMobileCells.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplication.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (5)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_TIME_LOCATION_SYSTEM_SETTINGS) {
            PPApplication.restartTwilightScanner(context);

            event._eventPreferencesTime.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplication.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (6)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_USE_PRIORITY_SETTINGS) {

            event.setSummary(prefMng, Event.PREF_EVENT_PRIORITY_APP_SETTINGS, preferences, context, false);
            event.setSummary(prefMng, Event.PREF_EVENT_PRIORITY, preferences, context, false);
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE)) {
            RingtonePreference preference = prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND_START);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND_END);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_WIFI_BT_SCAN_DIALOG)) {
            WifiSSIDPreference wifiPreference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_SSID);
            if (wifiPreference != null)
                wifiPreference.refreshListView(true, "");
            BluetoothNamePreference bluetoothPreference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
            if (bluetoothPreference != null)
                bluetoothPreference.refreshListView(true, "");
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG)) {
            MobileCellsPreference preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
            if (preference != null)
                preference.refreshListView(true, Integer.MAX_VALUE);
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG)) {
            MobileCellsRegistrationDialogPreference preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_REGISTRATION);
            if (preference != null)
                preference.startRegistration();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CALENDAR_DIALOG)) {
            CalendarsMultiSelectDialogPreference preference = prefMng.findPreference(EventPreferencesCalendar.PREF_EVENT_CALENDAR_CALENDARS);
            if (preference != null)
                preference.refreshListView(true);
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CONTACT_DIALOG)) {
            ContactsMultiSelectDialogPreference preference1 = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(true);
            preference1 = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(true);
            ContactGroupsMultiSelectDialogPreference preference2 = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(true);
            preference2 = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(true);
        }
        /*if (requestCode == NFCTagPreference.RESULT_NFC_TAG_READ_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                NFCTagPreference preference = prefMng.findPreference(EventPreferencesNFC.PREF_EVENT_NFC_NFC_TAGS);
                if (preference != null) {
                    String tagName = data.getStringExtra(NFCTagReadActivity.EXTRA_TAG_NAME);
                    String tagUid = data.getStringExtra(NFCTagReadActivity.EXTRA_TAG_UID);
                    long tagDbId = data.getLongExtra(NFCTagReadActivity.EXTRA_TAG_DB_ID, 0);
                    preference.setNFCTagFromEditor(tagName, tagUid, tagDbId);
                }
            }
        }*/
        if (requestCode == NFCTagPreference.RESULT_NFC_TAG_WRITE) {
            if (resultCode == Activity.RESULT_OK) {
                NFCTagPreference preference = prefMng.findPreference(EventPreferencesNFC.PREF_EVENT_NFC_NFC_TAGS);
                if (preference != null) {
                    String tagName = data.getStringExtra(NFCTagWriteActivity.EXTRA_TAG_NAME);
                    //String tagUid = data.getStringExtra(NFCTagWriteActivity.EXTRA_TAG_UID);
                    long tagDbId = data.getLongExtra(NFCTagWriteActivity.EXTRA_TAG_DB_ID, 0);
                    preference.setNFCTagFromEditor(tagName, "", tagDbId);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
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

    private void initPreferenceFragment(/*Bundle savedInstanceState*/) {
        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();

        event = new Event();

        /*
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

    static boolean isRedTextNotificationRequired(Event event, boolean againCheckInDelay, Context context) {
        Context appContext = context.getApplicationContext();
        boolean enabledSomeSensor = event.isEnabledSomeSensor(appContext);
        boolean grantedAllPermissions = Permissions.checkEventPermissions(appContext, event, null, EventsHandler.SENSOR_TYPE_ALL).size() == 0;
        /*if (Build.VERSION.SDK_INT >= 29) {
            if (!Settings.canDrawOverlays(context))
                grantedAllPermissions = false;
        }*/
        boolean accessibilityEnabled =  event.isAccessibilityServiceEnabled(appContext, false, againCheckInDelay) == 1;

        boolean eventIsRunnable = event.isRunnable(appContext, false);

        return ((!enabledSomeSensor) || (!grantedAllPermissions) || (!accessibilityEnabled) || (!eventIsRunnable));
    }

    private void setRedTextToPreferences() {
        if (nestedFragment)
            return;

        if (getActivity() == null)
            return;

        final EventsPrefsActivity activity = (EventsPrefsActivity)getActivity();

        Context context = activity.getApplicationContext();

        String rootScreen = "rootScreen";

        long event_id = activity.event_id;
        int newEventMode = activity.newEventMode;
        int predefinedEventIndex = activity.predefinedEventIndex;
        final Event event = activity.getEventFromPreferences(event_id, newEventMode, predefinedEventIndex);

        int errorColor = ContextCompat.getColor(context, R.color.altype_error);

        if (event != null) {
            int order = 1;

            // not enabled some sensor
            if (event.isEnabledSomeSensor(context)) {
                Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference(rootScreen);
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference(rootScreen);
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_NOT_ENABLED_SOME_SENSOR);
                        preference.setIconSpaceReserved(false);
                        preference.setWidgetLayoutResource(R.layout.preference_widget_exclamation_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-99);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    String _title = order + ". " + getString(R.string.event_preferences_no_sensor_is_enabled);
                    ++order;
                    Spannable title = new SpannableString(_title);
                    title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                    preference.setTitle(title);
                    _title = getString(R.string.event_preferences_sensor_parameters_location_summary);
                    Spannable summary = new SpannableString(_title);
                    summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                    preference.setSummary(summary);
                }
            }

            // not some permissions
            if (Permissions.checkEventPermissions(context, event, null, EventsHandler.SENSOR_TYPE_ALL).size() == 0) {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference(rootScreen);
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference(rootScreen);
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_GRANT_PERMISSIONS);
                        preference.setIconSpaceReserved(false);
                        if (event._id > 0)
                            preference.setWidgetLayoutResource(R.layout.preference_widget_preference_with_subpreferences);
                        else
                            preference.setWidgetLayoutResource(R.layout.preference_widget_exclamation_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-98);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    String _title = order + ". " + getString(R.string.preferences_grantPermissions_title);
                    ++order;
                    Spannable title = new SpannableString(_title);
                    title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                    preference.setTitle(title);
                    _title = getString(R.string.preferences_grantPermissions_summary) + " " +
                                getString(R.string.event_preferences_red_sensors_summary) + " " +
                                getString(R.string.event_preferences_sensor_parameters_location_summary);
                    Spannable summary = new SpannableString(_title);
                    summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                    preference.setSummary(summary);

                    if (event._id > 0) {
                        preference.setOnPreferenceClickListener(preference1 -> {
                            Permissions.grantEventPermissions(activity, event/*, false, true*/);
                            return false;
                        });
                    }
                }
            }

            // not enabled accessibility service
            int accessibilityEnabled = event.isAccessibilityServiceEnabled(context, false, false);
            /*if (accessibilityEnabled == 1) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion != 0) {
                    // PPPE is installed
                    if (PPApplication.accessibilityServiceForPPPExtenderConnected == 2)
                        // Extender is not connected
                        accessibilityEnabled = 0;
                }
            }*/
            Preference preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
            if (accessibilityEnabled == 1) {
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference(rootScreen);
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference(rootScreen);
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                        preference.setIconSpaceReserved(false);
                        preference.setWidgetLayoutResource(R.layout.preference_widget_preference_with_subpreferences);
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
                    title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                    preference.setTitle(title);
                    if ((accessibilityEnabled == -1) || (accessibilityEnabled == -2)) {
                        _title = getString(R.string.event_preferences_red_install_PPPExtender) + " " +
                                getString(R.string.event_preferences_red_sensors_summary) + " " +
                                getString(R.string.event_preferences_sensor_parameters_location_summary);
                        Spannable summary = new SpannableString(_title);
                        summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                        preference.setSummary(summary);

                        preference.setOnPreferenceClickListener(preference12 -> {
                            ExtenderDialogPreferenceFragment.installPPPExtender(getActivity(), null, false);
                            return false;
                        });
                    }
                    else {
                        _title = getString(R.string.event_preferences_red_enable_PPPExtender) + " " +
                                getString(R.string.event_preferences_red_sensors_summary) + " " +
                                getString(R.string.event_preferences_sensor_parameters_location_summary);
                        Spannable summary = new SpannableString(_title);
                        summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                        preference.setSummary(summary);

                        preference.setOnPreferenceClickListener(preference13 -> {
                            ExtenderDialogPreferenceFragment.enableExtender(getActivity(), null);
                            return false;
                        });
                    }
                }
            }

            // not is runnable
            if (event.isRunnable(context, false)) {
                preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference(rootScreen);
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference(rootScreen);
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_NOT_IS_RUNNABLE);
                        preference.setIconSpaceReserved(false);
                        preference.setWidgetLayoutResource(R.layout.preference_widget_exclamation_preference);
                        preference.setLayoutResource(R.layout.mp_preference_material_widget);
                        preference.setOrder(-100);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    String _title = order + ". " + getString(R.string.event_preferences_not_set_underlined_parameters);
                    ++order;
                    Spannable title = new SpannableString(_title);
                    title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                    preference.setTitle(title);
                    _title = getString(R.string.event_preferences_not_set_underlined_parameters_summary) + " " +
                                getString(R.string.event_preferences_red_sensors_summary) + " " +
                                getString(R.string.event_preferences_sensor_parameters_location_summary);
                    Spannable summary = new SpannableString(_title);
                    summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                    preference.setSummary(summary);
                }
            }
        }
        else {
            Preference preference = prefMng.findPreference(PRF_NOT_IS_RUNNABLE);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_NOT_ENABLED_SOME_SENSOR);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
    }

    void doMobileCellsRegistrationCountDownBroadcastReceiver(long millisUntilFinished) {
        MobileCellsRegistrationDialogPreference preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_REGISTRATION);
        if (preference != null) {
            //Log.d("mobileCellsRegistrationCountDownBroadcastReceiver", "xxx");
            preference.updateInterface(millisUntilFinished, false);
            preference.setSummaryDDP(millisUntilFinished);
        }
    }

    void doMobileCellsRegistrationStoppedBroadcastReceiver() {
        MobileCellsPreference preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
        if (preference != null)
            preference.refreshListView(true, Integer.MAX_VALUE);
    }

/*
    private void installExtenderFromGitHub() {
        if (getActivity() == null)
            return;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_extender_dialog_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_ppp_pppe_from_github, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_info_text);

        String dialogText = "";
        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
            dialogText = dialogText + getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
        }
        dialogText = dialogText + getString(R.string.install_extender_required_version) +
                " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
        dialogText = dialogText + getString(R.string.install_extender_text1) + " \"" + getString(R.string.alert_button_install) + "\"\n";
        dialogText = dialogText + getString(R.string.install_extender_text2) + "\n";
        dialogText = dialogText + getString(R.string.install_extender_text3);

        text.setText(dialogText);

        text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_github_releases);
        CharSequence str1 = getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPE_RELEASES_URL + "\u00A0»»";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.GITHUB_PPPE_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    if (getActivity() != null)
                        getActivity().startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            //String url = PPApplication.GITHUB_PPPE_RELEASES_URL;
            //String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL_1 + PPApplication.VERSION_NAME_EXTENDER_LATEST + PPApplication.GITHUB_PPPE_DOWNLOAD_URL_2;
            String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplication.recordException(e);
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

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();
    }

    private void installExtender() {
        if (getActivity() == null) {
            return;
        }

        PackageManager packageManager = getActivity().getPackageManager();
        Intent _intent = packageManager.getLaunchIntentForPackage("com.sec.android.app.samsungapps");
        boolean galaxyStoreInstalled = (_intent != null);

        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && galaxyStoreInstalled) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
                dialogText = dialogText + getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
            }
            dialogText = dialogText + getString(R.string.install_extender_required_version) +
                    " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
            dialogText = dialogText + getString(R.string.install_extender_text1) + " \"" + getString(R.string.alert_button_install) + "\".";

            text.setText(dialogText);

            dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            //button.setText(getActivity().getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub();
            });

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

            if ((getActivity() != null) && (!getActivity().isFinishing()))
                dialog.show();
        }
        else
            installExtenderFromGitHub();
    }
*/

/*
    private void enableExtender() {
        if (getActivity() == null)
            return;

        boolean ok = false;
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, getActivity())) {
            try {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                //noinspection deprecation
                startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                ok = true;
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
        if (!ok) {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.event_preferences_applications_AccessibilitySettings_title),
                        getString(R.string.setting_screen_not_found_alert),
                        getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        true,
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }
*/
    void changeCurentLightSensorValue() {
        if (getActivity() != null) {
            Preference currentValuePreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE);
            if (currentValuePreference != null) {
                SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
                if ((sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null)) {
                    PPApplication.startHandlerThreadOrientationScanner();
                    OrientationScannerHandlerThread orientationHandler = PPApplication.handlerThreadOrientationScanner;
                    if (orientationHandler == null) {
                        currentValuePreference.setSummary("0");
                    } else {
                        currentValuePreference.setSummary(String.valueOf(orientationHandler.resultLight));
                    }
                } else {
                    currentValuePreference.setSummary(R.string.event_preferences_orientation_light_currentValue_noHardware);
                }
            }
        }
    }

}