package sk.henrichg.phoneprofilesplus;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
public class EventsPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;

    private boolean nestedFragment = false;

    private Event event;

    //static boolean forceStart;

    private SetRedTextToPreferencesAsyncTask setRedTextToPreferencesAsyncTask = null;

    private ShortcutToReadNFCTagAddedBroadcastReceiver shortcutToReadNFCTagAddedReceiver;

    private static final String PREF_GRANT_PERMISSIONS = "eventGrantPermissions";
    private static final String PREF_NOT_IS_RUNNABLE = "eventNotIsRunnable";
    private static final String PREF_NOT_IS_ALL_CONFIGURED = "eventNotIsAllConfigured";
    private static final String PREF_NOT_ENABLED_SOME_SENSOR = "eventNotEnabledSomeSensors";
    private static final String PREF_NOT_ENABLED_ACCESSIBILITY_SERVICE = "eventNotEnabledAccessibilityService";
    private static final String PREF_EVENT_SENSORS_INFO = "eventSensorsInfo";

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
    private static final String PREF_EVENT_MOBILE_CELLS_CONFIGURE_CELLS = "eventMobileCellsConfrigureCells";
    private static final int RESULT_EVENT_MOBILE_CELLS_CONFIGURE_CELLS = 1998;
    private static final String PREF_EVENT_MOBILE_CELLS_CELLS_REGISTRATION = "eventMobileCellsCellsRegistration";
    private static final String PREF_EVENT_HIDE_NOT_USED_SENSORS = "eventHideNotUsedSensors";
    private static final int RESULT_MUSIC_NOTIFICATION_ACCESS_SYSTEM_SETTINGS = 1999;
    private static final int RESULT_SET_CALL_SCREENING_ROLE = 2000;
    private static final int RESULT_SIMULATE_RINGING_CALL = 2001;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // is required for to not call onCreate and onDestroy on orientation change
        //noinspection deprecation
        setRetainInstance(true);

        nestedFragment = !(this instanceof EventsPrefsRoot);

        initPreferenceFragment(/*savedInstanceState*/);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @NonNull
    @Override
    public RecyclerView onCreateRecyclerView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, Bundle state) {
        final RecyclerView view = super.onCreateRecyclerView(inflater, parent, state);
        view.setItemAnimator(null);
        view.setLayoutAnimation(null);

        // WARNING: must be in base_styles_phoneprofilestheme_preferences_daynight:
        //             <item name="android:scrollbars">vertical</item>
        view.setScrollbarFadingEnabled(false);

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
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof PPMultiSelectListPreference)
        {
            ((PPMultiSelectListPreference)preference).fragment = new PPMultiSelectListPreferenceFragment();
            dialogFragment = ((PPMultiSelectListPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfilePreference) {
            ((ProfilePreference) preference).fragment = new ProfilePreferenceFragment();
            dialogFragment = ((ProfilePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof InfoDialogPreference) {
            ((InfoDialogPreference) preference).fragment = new InfoDialogPreferenceFragment();
            dialogFragment = ((InfoDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof DurationDialogPreference) {
            ((DurationDialogPreference) preference).fragment = new DurationDialogPreferenceFragment();
            dialogFragment = ((DurationDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ApplicationsMultiSelectDialogPreference) {
            ((ApplicationsMultiSelectDialogPreference) preference).fragment = new ApplicationsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ApplicationsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BetterNumberPickerPreference) {
            ((BetterNumberPickerPreference) preference).fragment = new BetterNumberPickerPreferenceFragment();
            dialogFragment = ((BetterNumberPickerPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof RingtonePreference) {
            ((RingtonePreference) preference).fragment = new RingtonePreferenceFragment();
            dialogFragment = ((RingtonePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof LocationGeofencePreference) {
            ((LocationGeofencePreference) preference).fragment = new LocationGeofencePreferenceFragment();
            dialogFragment = ((LocationGeofencePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfileMultiSelectPreference) {
            ((ProfileMultiSelectPreference) preference).fragment = new ProfileMultiSelectPreferenceFragment();
            dialogFragment = ((ProfileMultiSelectPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof DaysOfWeekPreference) {
            ((DaysOfWeekPreference) preference).fragment = new DaysOfWeekPreferenceFragment();
            dialogFragment = ((DaysOfWeekPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
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
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof CalendarsMultiSelectDialogPreference) {
            ((CalendarsMultiSelectDialogPreference) preference).fragment = new CalendarsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((CalendarsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof SearchStringPreference) {
            ((SearchStringPreference) preference).fragment = new SearchStringPreferenceFragment();
            dialogFragment = ((SearchStringPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ContactGroupsMultiSelectDialogPreference) {
            ((ContactGroupsMultiSelectDialogPreference) preference).fragment = new ContactGroupsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ContactGroupsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ContactsMultiSelectDialogPreference) {
            ((ContactsMultiSelectDialogPreference) preference).fragment = new ContactsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ContactsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof WifiSSIDPreference) {
            ((WifiSSIDPreference) preference).fragment = new WifiSSIDPreferenceFragment();
            dialogFragment = ((WifiSSIDPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BluetoothNamePreference) {
            ((BluetoothNamePreference) preference).fragment = new BluetoothNamePreferenceFragment();
            dialogFragment = ((BluetoothNamePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof NFCTagPreference) {
            ((NFCTagPreference) preference).fragment = new NFCTagPreferenceFragment();
            dialogFragment = ((NFCTagPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof VolumeDialogPreference)
        {
            ((VolumeDialogPreference)preference).fragment = new VolumeDialogPreferenceFragment();
            dialogFragment = ((VolumeDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ExtenderDialogPreference)
        {
            ((ExtenderDialogPreference)preference).fragment = new ExtenderDialogPreferenceFragment();
            dialogFragment = ((ExtenderDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BrightnessDialogPreference)
        {
            ((BrightnessDialogPreference)preference).fragment = new BrightnessDialogPreferenceFragment();
            dialogFragment = ((BrightnessDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof MobileCellNamesPreference)
        {
            ((MobileCellNamesPreference)preference).fragment = new MobileCellNamesPreferenceFragment();
            dialogFragment = ((MobileCellNamesPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if ((Build.VERSION.SDK_INT >= 29) && (preference instanceof SendSMSDialogPreference))
        {
            ((SendSMSDialogPreference)preference).fragment = new SendSMSDialogPreferenceFragment();
            dialogFragment = ((SendSMSDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof PPEditTextDialogPreference)
        {
            ((PPEditTextDialogPreference)preference).fragment = new PPEditTextDialogPreferenceFragment();
            dialogFragment = ((PPEditTextDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null) {
            if ((getActivity() != null) && (!getActivity().isFinishing())) {
                FragmentManager fragmentManager = getParentFragmentManager();//getFragmentManager();
                //if (fragmentManager != null) {
                //noinspection deprecation
                dialogFragment.setTargetFragment(this, 0);
                if (!fragmentManager.isDestroyed())
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

//        PPApplication.forceStartOrientationScanner(context);
//        forceStart = true;

        // must be used handler for rewrite toolbar title/subtitle
        final Handler handler = new Handler(activity.getMainLooper());
        final WeakReference<EventsPrefsActivity> activityWeakRef = new WeakReference<>(activity);
        handler.postDelayed(() -> {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EventsPrefsFragment.onActivityCreated");
            EventsPrefsActivity __activity = activityWeakRef.get();
            if ((__activity == null) || __activity.isFinishing() || __activity.isDestroyed())
                return;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(__activity.getApplicationContext());
            final String eventName = preferences.getString(Event.PREF_EVENT_NAME, "");
            Toolbar toolbar = __activity.findViewById(R.id.activity_preferences_toolbar);
            //noinspection DataFlowIssue
            toolbar.setSubtitle(__activity.getString(R.string.title_activity_event_preferences)+"   ");
            toolbar.setTitle(__activity.getString(R.string.event_string_0) + StringConstants.STR_COLON_WITH_SPACE + eventName);
        }, 200);

        //final EventsPrefsActivity activity = (EventsPrefsActivity) getActivity();
        final Context context = activity.getBaseContext();
        final EventsPrefsFragment fragment = this;
        final TextView preferenceSubTitle = activity.findViewById(R.id.activity_preferences_subtitle);

        // subtitle
        if (nestedFragment) {
            //noinspection DataFlowIssue
            preferenceSubTitle.setVisibility(View.VISIBLE);

            Drawable triangle = ContextCompat.getDrawable(activity, R.drawable.ic_submenu_triangle);
            if (triangle != null) {
                triangle.setTint(ContextCompat.getColor(activity, R.color.activityNormalTextColor));
                SpannableString headerTitle = new SpannableString("    " +
                        fragment.getPreferenceScreen().getTitle());
                triangle.setBounds(
                        GlobalGUIRoutines.sip(2),
                        GlobalGUIRoutines.sip(1),
                        GlobalGUIRoutines.sip(13),
                        GlobalGUIRoutines.sip(10));
                headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                preferenceSubTitle.setText(headerTitle);
            } else
                preferenceSubTitle.setText(fragment.getPreferenceScreen().getTitle());

            //toolbar.setTitle(fragment.getPreferenceScreen().getTitle());

        } else {
            //noinspection DataFlowIssue
            preferenceSubTitle.setVisibility(View.GONE);

            SwitchPreferenceCompat preference = prefMng.findPreference(PREF_EVENT_HIDE_NOT_USED_SENSORS);
            if (preference != null) {
                //    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, true, , false, false);
                preference.setTitle("[ " + getString(R.string.event_preferences_hideNotUsedSensors) + " ]");
            }

            // load PREF_EVENT_HIDE_NOT_USED_SENSORS from Application preferences
            //Log.e("EventPrefsFragment.onActivityCreated", "ApplicationPreferences.applicationEventHideNotUsedSensors="+ApplicationPreferences.applicationEventHideNotUsedSensors);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_HIDE_NOT_USED_SENSORS, ApplicationPreferences.applicationEventHideNotUsedSensors);
            editor.apply();
            if (preference != null)
                preference.setChecked(ApplicationPreferences.applicationEventHideNotUsedSensors);
            doEventHideNotUsedSensors(ApplicationPreferences.applicationEventHideNotUsedSensors,
                    (!activity.showSaveMenu) ||
                            (!ApplicationPreferences.applicationEventHideNotUsedSensors));
        }

        //activity.progressLinearLayout.setVisibility(View.GONE);
        activity.settingsLinearLayout.setVisibility(View.VISIBLE);

        setDivider(null); // this remove dividers for categories

        //setRedTextToPreferences();

        // update preference summary and also category summary
        //event.checkSensorsPreferences(prefMng, !nestedFragment, activity.getBaseContext());
        //event.setAllSummary(prefMng, preferences, activity.getBaseContext());

        Preference notificationAccessPreference = prefMng.findPreference(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS);
        if (notificationAccessPreference != null) {
            //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            notificationAccessPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_NOTIFICATION_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                startActivityForResult(intent, RESULT_NOTIFICATION_SCANNING_APP_SETTINGS);
                /*
                boolean ok = false;
                String activity1;
                activity1 = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                if (GlobalGUIRoutines.activityActionExists(activity1, context)) {
                    try {
                        Intent intent = new Intent(activity1);
                        startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    //if (getActivity() != null) {
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
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.show();
                    //}
                }
                */
                return false;
            });
        }
        /*
        Preference extenderPreference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(activity, null);
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_LOCATION_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                        startActivityForResult(intent, RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    //if (getActivity() != null) {
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
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    //}
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_WIFI_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                        startActivityForResult(intent, RESULT_WIFI_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    //if (getActivity() != null) {
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
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    //}
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
                        startActivityForResult(intent, RESULT_TIME_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    //if (getActivity() != null) {
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
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    //}
                }
                return false;
            });
        }
        if (Build.VERSION.SDK_INT >= 27) {
            preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_CATEGORY);
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
                            startActivityForResult(intent, RESULT_WIFI_KEEP_ON_SYSTEM_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        //if (getActivity() != null) {
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
                                    null,
                                    true, true,
                                    false, false,
                                    true,
                                    false,
                                    activity
                            );

                            if (!activity.isFinishing())
                                dialog.showDialog();
                        //}
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_BLUETOOTH_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                        startActivityForResult(intent, RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    //if (getActivity() != null) {
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
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    //}
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_ORIENTATION_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                startActivityForResult(intent, RESULT_ORIENTATION_SCANNING_SETTINGS);
                return false;
            });
        }
        /*
        extenderPreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference110 -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(activity, null);
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_MOBILE_CELLS_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                        startActivityForResult(intent, RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    //if (getActivity() != null) {
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
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    //}
                }
                return false;
            });
        }
        preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CONFIGURE_CELLS);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference112 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_MOBILE_CELLS_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                startActivityForResult(intent, RESULT_EVENT_MOBILE_CELLS_CONFIGURE_CELLS);
                return false;
            });
        }
        preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CELLS_REGISTRATION);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference112 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_MOBILE_CELLS_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                startActivity(intent);
                return false;
            });
        }

        preference = prefMng.findPreference(EventPreferencesTime.PREF_EVENT_TIME_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference114 -> {
                Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_PERIODIC_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_PERIODIC_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_PERIODIC_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_EVENT_RUN_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
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
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_NOTIFICATION_SCANNING_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                startActivityForResult(intent, RESULT_NOTIFICATION_SCANNING_APP_SETTINGS);
                return false;
            });
        }
        /*
        MobileCellsRegistrationDialogPreference mobileCellsRegistrationDialogPreference =
                prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_REGISTRATION);
        if (mobileCellsRegistrationDialogPreference != null) {
            mobileCellsRegistrationDialogPreference.event_id = activity.event_id;
        }
        */
        /*
        MobileCellsEditorPreference mobileCellsPreference =
                (MobileCellsEditorPreference)prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
        if (mobileCellsPreference != null) {
            mobileCellsPreference.event_id = event_id;
        }
        */
        /*
        extenderPreference = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference118 -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(activity, null);
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
                if (PPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
                else {
                    //if (getActivity() != null) {
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
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.show();
                    //}
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
                ExtenderDialogPreferenceFragment.installPPPExtender(activity, null);
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
                if (PPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
                else {
                    //if (getActivity() != null) {
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
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.show();
                    //}
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
                if (PPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
                else {
                    //if (getActivity() != null) {
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
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.show();
                    //}
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
                if (PPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
                else {
                    //if (getActivity() != null) {
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
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.show();
                    //}
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
                activityExists = GlobalGUIRoutines.activityActionExists(Settings.ACTION_BATTERY_SAVER_SETTINGS, context);
                intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                if (activityExists) {
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        intent = new Intent();
                        intent.setComponent(new ComponentName(StringConstants.SETTINGS_PACKAGE_NAME, StringConstants.SETTINGS_BATTERY_SAVER_CLASS_NAME));
                        activityExists = GlobalGUIRoutines.activityIntentExists(intent, context);
                        if (activityExists) {
                            try {
                                startActivity(intent);
                            } catch (Exception ee) {
                                PPApplicationStatic.recordException(ee);
                            }
                        }
                    }
                }
                if (!activityExists) {
                    //if (getActivity() != null) {
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
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    //}
                }
                return false;
            });
        }

        InfoDialogPreference infoDialogPreference = prefMng.findPreference(PREF_EVENT_SENSORS_INFO);
        if (infoDialogPreference != null) {
            String info = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML + getString(R.string.event_preferences_sensorsInfo_summary) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML +
                    StringConstants.TAG_BREAK_HTML +
                    StringConstants.TAG_LIST_START_FIRST_ITEM_HTML + getString(R.string.event_preferences_sensorsInfo_summary_2) + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
            infoDialogPreference.setInfoText(info);
            infoDialogPreference.setIsHtml(true);
        }

        preference = prefMng.findPreference(EventPreferencesNFC.PREF_EVENT_NFC_READ_NFC_TAG_SHORTCUT);
        if (preference != null) {
            Context appContext = context.getApplicationContext();
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(appContext)) {
                List<ShortcutInfoCompat> shortcuts = ShortcutManagerCompat.getShortcuts(appContext, ShortcutManagerCompat.FLAG_MATCH_PINNED);
                boolean exists = false;
                for (ShortcutInfoCompat shortcut : shortcuts) {
                    if (shortcut.getId().equals(EventPreferencesNFC.SHORTCUT_ID_READ_NFC_TAG)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    if (shortcutToReadNFCTagAddedReceiver == null) {
                        shortcutToReadNFCTagAddedReceiver = new ShortcutToReadNFCTagAddedBroadcastReceiver();
                        IntentFilter shortcutAddedFilter = new IntentFilter(EventPreferencesNFC.ACTION_SHORTCUT_TO_READ_NFC_TAG_ADDED);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_NOT_EXPORTED;
                        activity.registerReceiver(shortcutToReadNFCTagAddedReceiver, shortcutAddedFilter, receiverFlags);
                    }

                    preference.setVisible(true);
                    preference.setOnPreferenceClickListener(preference120 -> {
                        PPEditTextAlertDialog editTextDialog = new PPEditTextAlertDialog(
                                getString(R.string.shortcut_to_read_nfc_tag_dialog_title),
                                getString(R.string.shortcut_to_dialog_lablel),
                                getString(R.string.read_nfc_tag_short_shortcut_name),
                                getString(R.string.shortcut_to_dialog_create_button),
                                getString(android.R.string.cancel),
                                (dialog1, which) -> {
                                    String iconName = "";
                                    AlertDialog dialog = (AlertDialog) dialog1;
                                    EditText editText = dialog.findViewById(R.id.dialog_with_edittext_edit);
                                    if (editText != null)
                                        iconName = editText.getText().toString();
                                    if (iconName.isEmpty())
                                        iconName = getString(R.string.read_nfc_tag_short_shortcut_name);
                                    //Log.e("PhoneProfilesPrefsFragment createEditorShortcut", "iconName="+iconName);

                                    Intent shortcutIntent = new Intent(appContext, NFCTagReadForegroundActivity.class);
                                    //shortcutIntent.setAction(Intent.ACTION_MAIN);
                                    shortcutIntent.setAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
                                    //<data android:mimeType="application/vnd.phoneprofilesplus.events"/>
                                    //shortcutIntent.setData(Uri.parse("android:mimeType=\"application/vnd.phoneprofilesplus.events\""));
                                    shortcutIntent.setType("application/vnd.phoneprofilesplus.events");
                                    shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
                                    shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    ShortcutInfoCompat.Builder shortcutBuilderCompat = new ShortcutInfoCompat.Builder(appContext, EventPreferencesNFC.SHORTCUT_ID_READ_NFC_TAG);
                                    shortcutBuilderCompat.setIntent(shortcutIntent);
                                    shortcutBuilderCompat.setShortLabel(iconName);
                                    shortcutBuilderCompat.setLongLabel(getString(R.string.nfc_tag_pref_dlg_readNfcTag_text));
                                    shortcutBuilderCompat.setIcon(IconCompat.createWithResource(appContext, R.mipmap.ic_read_nfc_tag));

                                    try {
                                        Intent pinnedShortcutCallbackIntent = new Intent(EventPreferencesNFC.ACTION_SHORTCUT_TO_READ_NFC_TAG_ADDED);
                                        PendingIntent successCallback = PendingIntent.getBroadcast(appContext, 10, pinnedShortcutCallbackIntent,  0);

                                        ShortcutInfoCompat shortcutInfo = shortcutBuilderCompat.build();
                                        ShortcutManagerCompat.requestPinShortcut(appContext, shortcutInfo, successCallback.getIntentSender());
                                        //activity.setResult(Activity.RESULT_OK, intent);
                                    } catch (Exception e) {
                                        // show dialog about this crash
                                        // for Microsft laucher it is:
                                        // java.lang.IllegalArgumentException ... already exists but disabled
                                    }
                                },
                                null, null,
                                true, true, //false,
                                activity
                        );
                        if (!activity.isFinishing())
                            editTextDialog.showDialog();

                        return false;
                    });
                }
                else
                    preference.setVisible(false);
            } else
                preference.setVisible(false);
        }
        preference = prefMng.findPreference(EventPreferencesMusic.PREF_EVENT_MUSIC_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference126 -> {
                boolean ok = false;
                String action;
                action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                if (GlobalGUIRoutines.activityActionExists(action, activity.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(action);
                        startActivityForResult(intent, RESULT_MUSIC_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
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
                            null,
                            true, true,
                            false, false,
                            true,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.showDialog();
                }
                return false;
            });
        }
        if (Build.VERSION.SDK_INT >= 33) {
            final InfoDialogPreference _infoDialogPreference = prefMng.findPreference(EventPreferencesMusic.PREF_EVENT_MUSIC_NOTIFICATION_ACCESS_RESTRICTED_SETTINGS);
            if (_infoDialogPreference != null) {
                _infoDialogPreference.setOnPreferenceClickListener(preference120 -> {
                    _infoDialogPreference.setInfoText(
                            StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.PPP_APP_INFO_SCREEN + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                                    EventsPrefsFragment.this.getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML + StringConstants.TAG_URL_LINK_END_HTML + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    EventsPrefsFragment.this.getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_3) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    EventsPrefsFragment.this.getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_4) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    EventsPrefsFragment.this.getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_5) + StringConstants.TAG_BREAK_HTML +
                                    EventsPrefsFragment.this.getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_6) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.DROIDIFY_INSTALLATION_SITE + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                                    EventsPrefsFragment.this.getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_10) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML + StringConstants.TAG_URL_LINK_END_HTML + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    EventsPrefsFragment.this.getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_7) + " " +
                                    "\"" + EventsPrefsFragment.this.getString(R.string.menu_import_export) + "\"/\"" + EventsPrefsFragment.this.getString(R.string.menu_export) + "\"." + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    EventsPrefsFragment.this.getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_8) + " " +
                                    "\"" + EventsPrefsFragment.this.getString(R.string.menu_import_export) + "\"/\"" + EventsPrefsFragment.this.getString(R.string.menu_import) + "\"."
                    );
                    _infoDialogPreference.setIsHtml(true);

                    return false;
                });
            }
        }

        if (Build.VERSION.SDK_INT >= 29) {
            preference = prefMng.findPreference(EventPreferencesCallControl.PREF_EVENT_CALL_CONTROL_SET_CALL_SCREENING_ROLE);
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference1 -> {
                    // start preferences activity for default profile
                    //if (getActivity() != null) {
                        Intent intent = new Intent(activity.getBaseContext(), PhoneProfilesPrefsActivity.class);
                        intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_DEFAULT_ROLES_APPLICATIONS_ROOT);
                        getActivity().startActivityForResult(intent, RESULT_SET_CALL_SCREENING_ROLE);
                    //}
                    return false;
                });
            }
        }

        preference = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_SIMULATE_RINGING_CALL_SETTINGS);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference1 -> {
                // start preferences activity for default profile
                //if (activity != null) {
                Intent intent = new Intent(activity.getBaseContext(), PhoneProfilesPrefsActivity.class);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_SYSTEM_CATEGORY_ROOT);
                //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                activity.startActivityForResult(intent, RESULT_SIMULATE_RINGING_CALL);
                //}
                return false;
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() == null)
            return;

        //Log.e("EventPrefsFragment.onResume", "xxxxxx");

        if (event != null) {
            // this is important for update preferences after PPPPS and Extender installation
            event.checkSensorsPreferences(prefMng, !nestedFragment, getActivity().getBaseContext());
            //Log.e("EventPrefsFragment.onResume", "called event.setAllSummary()");
            event.setAllSummary(prefMng, preferences, getActivity().getBaseContext());
        }

        if (!nestedFragment) {
            final Context context = getActivity().getBaseContext();

//            if (event != null) {
//            event._eventPreferencesApplication.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesOrientation.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesSMS.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesCall.checkPreferences(prefMng, !nestedFragment, context);
//            event._eventPreferencesNotification.checkPreferences(prefMng, !nestedFragment, context);
//            }
            setRedTextToPreferences();
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsPrefsFragment.onResume", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if (getActivity() != null)
                getActivity().unregisterReceiver(shortcutToReadNFCTagAddedReceiver);
        } catch (Exception ignored) {}
        shortcutToReadNFCTagAddedReceiver = null;

        if ((setRedTextToPreferencesAsyncTask != null) &&
                setRedTextToPreferencesAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            setRedTextToPreferencesAsyncTask.cancel(true);
        setRedTextToPreferencesAsyncTask = null;

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
            PPApplicationStatic.recordException(e);
        }

        event = null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ((key != null) && key.equals(Event.PREF_EVENT_NAME)) {
            String value = sharedPreferences.getString(key, "");
            if (getActivity() != null) {

                // must be used handler for rewrite toolbar title/subtitle
                final String _value = value;
                final Handler handler = new Handler(getActivity().getMainLooper());
                final WeakReference<EventsPrefsActivity> activityWeakRef = new WeakReference<>((EventsPrefsActivity) getActivity());
                handler.postDelayed(() -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EventsPrefsFragment.onSharedPreferenceChanged");
                    EventsPrefsActivity activity = activityWeakRef.get();
                    if ((activity == null) || activity.isFinishing() || activity.isDestroyed())
                        return;

                    Toolbar toolbar = activity.findViewById(R.id.activity_preferences_toolbar);
                    //toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + _value);
                    //noinspection DataFlowIssue
                    toolbar.setTitle(activity.getString(R.string.event_string_0) + StringConstants.STR_COLON_WITH_SPACE + _value);
                }, 200);
            }
        }

        if (getActivity() == null)
            return;

        if ((key != null) && key.equals(PREF_EVENT_HIDE_NOT_USED_SENSORS)) {
            // save PREF_EVENT_HIDE_NOT_USED_SENSORS into Application preferences
            boolean hideNotUsedSensors = preferences.getBoolean(PREF_EVENT_HIDE_NOT_USED_SENSORS, false);
            //Log.e("EventPrefsFragment.onSharedPreferenceChanged", "hideNotUsedSensors="+hideNotUsedSensors);
            SharedPreferences appSharedPreferences = ApplicationPreferences.getSharedPreferences(getActivity().getApplicationContext());
            if (appSharedPreferences != null) {
                SharedPreferences.Editor editor = appSharedPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_HIDE_NOT_USED_EVENTS, hideNotUsedSensors);
                editor.apply();
                ApplicationPreferences.applicationEventHideNotUsedSensors(getActivity().getApplicationContext());
            }
            doEventHideNotUsedSensors(ApplicationPreferences.applicationEventHideNotUsedSensors,
                                        !ApplicationPreferences.applicationEventHideNotUsedSensors);
        }

        if (event != null) {
            event.checkSensorsPreferences(prefMng, !nestedFragment, getActivity().getBaseContext());
            if (key != null)
                //Log.e("EventPrefsFragment.onSharedPreferenceChanged", "called Event.setSummary (1)");
                event.setSummary(prefMng, key, sharedPreferences, getActivity(), true);
        }

        setRedTextToPreferences();

        if ((key != null) && (!key.equals(PREF_EVENT_HIDE_NOT_USED_SENSORS))) {
            EventsPrefsActivity activity = (EventsPrefsActivity) getActivity();
            if (activity != null) {
                activity.showSaveMenu = true;
                activity.invalidateOptionsMenu();
            }
        }
    }

    void doOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (getActivity() == null)
            return;

        if (event == null)
            return;

        final Context context = getActivity().getBaseContext();

        //EventsPrefsActivity activity = (EventsPrefsActivity) getActivity();

        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EVENT)) {
            setRedTextToPreferences();
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            PPApplicationStatic.restartNotificationScanner(context);

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
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (1)", "call of updateGUI");
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
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (2)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS) {
            BluetoothNamePreference preference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesBluetooth.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (3)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_LOCATION_LOCATION_SYSTEM_SETTINGS) {
            LocationGeofencePreference preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_GEOFENCES);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesLocation.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (4)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS) {
            MobileCellNamesPreference preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELL_NAMES);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }

            event._eventPreferencesMobileCells.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (2)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);
        }
        if (requestCode == RESULT_TIME_LOCATION_SYSTEM_SETTINGS) {
            PPApplicationStatic.restartTwilightScanner(context);

            event._eventPreferencesTime.checkPreferences(prefMng, !nestedFragment, context);
            setRedTextToPreferences();
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (6)", "call of updateGUI");
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
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CALENDAR_DIALOG)) {
            CalendarsMultiSelectDialogPreference preference = prefMng.findPreference(EventPreferencesCalendar.PREF_EVENT_CALENDAR_CALENDARS);
            if (preference != null)
                preference.refreshListView(true);
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CONTACT_DIALOG)) {
            ContactsMultiSelectDialogPreference preference1 = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(false, false);
            preference1 = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(false, false);
            ContactGroupsMultiSelectDialogPreference preference2 = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(false, false);
            preference2 = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(false, false);
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
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)) {
            BrightnessDialogPreference preference = prefMng.findPreference(EventPreferencesBrightness.PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM);
            if (preference != null)
                preference.enableViews();
            preference = prefMng.findPreference(EventPreferencesBrightness.PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_TO);
            if (preference != null)
                preference.enableViews();
        }
        if (requestCode == RESULT_EVENT_MOBILE_CELLS_CONFIGURE_CELLS) {
            if (nestedFragment)
                event._eventPreferencesMobileCells.updateConfguredCellNames(prefMng, context);
        }
        if (requestCode == RESULT_MUSIC_NOTIFICATION_ACCESS_SYSTEM_SETTINGS) {
            event._eventPreferencesMusic.checkPreferences(prefMng, !nestedFragment, context);
        }

        if (requestCode == RESULT_SET_CALL_SCREENING_ROLE) {
            if (Build.VERSION.SDK_INT >= 29) {
                event._eventPreferencesCallControl.checkPreferences(prefMng, !nestedFragment, context);
                setRedTextToPreferences();

                PPApplication.updateGUI(true, false, context);
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

        if (getActivity() != null) {
            EventsPrefsActivity activity = (EventsPrefsActivity) getActivity();
            event = activity.event;
        } else {
            event = new Event();
            event.createEventPreferences();
        }

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

    private void setRedTextToPreferences() {
        if (nestedFragment)
            return;

        if (getActivity() == null)
            return;

        final EventsPrefsActivity activity = (EventsPrefsActivity)getActivity();

        Context context = activity.getApplicationContext();

        setRedTextToPreferencesAsyncTask =
                new SetRedTextToPreferencesAsyncTask
                        ((EventsPrefsActivity) getActivity(), this, prefMng, context);
        setRedTextToPreferencesAsyncTask.execute();
    }

    void changeCurentLightSensorValue() {
        if (getActivity() != null) {
            Preference currentValuePreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE);
            if (currentValuePreference != null) {
                SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
                if ((sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null)) {
                    PPApplicationStatic.startHandlerThreadOrientationScanner();
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

    private void doEventHideNotUsedSensors(boolean hideSensors, boolean saveDisplayed) {
        EventsPrefsActivity activity = (EventsPrefsActivity) getActivity();
        /*
        if (!hideSensors) {
            // clear displayedSensors, because all muust be visible
            if (activity != null)
                activity.displayedSensors.clear();
        }
        */

        if ((!nestedFragment) && (prefMng != null)) {
            boolean showAccessoriesSensor;
            boolean showActivatedProfileSensor;
            boolean showAlarmClockSensor;
            boolean showApplicationSensor;
            boolean showBatterySensor;
            boolean showBluetoothSensor;
            boolean showBrightnessSensor;
            boolean showCalendarSensor;
            boolean showCallSensor;
            boolean showDeviceBootSensor;
            boolean showLocationSensor;
            boolean showMobileCellsSensor;
            boolean showNFCSensor;
            boolean showNotificationSensor;
            boolean showOrientationSensor;
            boolean showPeriodicSensor;
            boolean showRadioSwitchSensor;
            boolean showRoamingSensor;
            boolean showScreenSensor;
            boolean showSMSSensor;
            boolean showSoundProfileSensor;
            boolean showTimeSensor;
            boolean showVolumesSensor;
            boolean showVPNSensor;
            boolean showWifiSensor;
            boolean showMusicSensor;
            boolean showCallControlSensor;

            if ((activity != null) && (!saveDisplayed) && (!activity.displayedSensors.isEmpty())) {
                showAccessoriesSensor = activity.displayedSensors.contains(EventPreferencesAccessories.PREF_EVENT_ACCESSORIES_ENABLED);
                showActivatedProfileSensor= activity.displayedSensors.contains(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_ENABLED);
                showAlarmClockSensor = activity.displayedSensors.contains(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED);
                showApplicationSensor = activity.displayedSensors.contains(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED);
                showBatterySensor= activity.displayedSensors.contains(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED);
                showBluetoothSensor = activity.displayedSensors.contains(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED);
                showBrightnessSensor = activity.displayedSensors.contains(EventPreferencesBrightness.PREF_EVENT_BRIGHTNESS_ENABLED);
                showCalendarSensor= activity.displayedSensors.contains(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED);
                showCallSensor = activity.displayedSensors.contains(EventPreferencesCall.PREF_EVENT_CALL_ENABLED);
                showDeviceBootSensor = activity.displayedSensors.contains(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_ENABLED);
                showLocationSensor = activity.displayedSensors.contains(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED);
                showMobileCellsSensor = activity.displayedSensors.contains(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED);
                showNFCSensor = activity.displayedSensors.contains(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED);
                showNotificationSensor = activity.displayedSensors.contains(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED);
                showOrientationSensor = activity.displayedSensors.contains(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED);
                showPeriodicSensor = activity.displayedSensors.contains(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_ENABLED);
                showRadioSwitchSensor = activity.displayedSensors.contains(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED);
                showRoamingSensor = activity.displayedSensors.contains(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED);
                showScreenSensor = activity.displayedSensors.contains(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED);
                showSMSSensor = activity.displayedSensors.contains(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED);
                showSoundProfileSensor = activity.displayedSensors.contains(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED);
                showTimeSensor = activity.displayedSensors.contains(EventPreferencesTime.PREF_EVENT_TIME_ENABLED);
                showVolumesSensor = activity.displayedSensors.contains(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED);
                showVPNSensor = activity.displayedSensors.contains(EventPreferencesVPN.PREF_EVENT_VPN_ENABLED);
                showWifiSensor= activity.displayedSensors.contains(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED);
                showMusicSensor = activity.displayedSensors.contains(EventPreferencesMusic.PREF_EVENT_MUSIC_ENABLED);
                showCallControlSensor = activity.displayedSensors.contains(EventPreferencesCallControl.PREF_EVENT_CALL_CONTROL_ENABLED);
            }
            else {
                showAccessoriesSensor = preferences.getBoolean(EventPreferencesAccessories.PREF_EVENT_ACCESSORIES_ENABLED, false);
                showActivatedProfileSensor= preferences.getBoolean(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false);
                showAlarmClockSensor = preferences.getBoolean(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, false);
                showApplicationSensor = preferences.getBoolean(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, false);
                showBatterySensor= preferences.getBoolean(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, false);
                showBluetoothSensor = preferences.getBoolean(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, false);
                showBrightnessSensor = preferences.getBoolean(EventPreferencesBrightness.PREF_EVENT_BRIGHTNESS_ENABLED, false);
                showCalendarSensor= preferences.getBoolean(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, false);
                showCallSensor = preferences.getBoolean(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, false);
                showDeviceBootSensor = preferences.getBoolean(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_ENABLED, false);
                showLocationSensor = preferences.getBoolean(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, false);
                showMobileCellsSensor = preferences.getBoolean(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, false);
                showNFCSensor = preferences.getBoolean(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, false);
                showNotificationSensor = preferences.getBoolean(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, false);
                showOrientationSensor = preferences.getBoolean(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, false);
                showPeriodicSensor = preferences.getBoolean(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_ENABLED, false);
                showRadioSwitchSensor = preferences.getBoolean(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, false);
                showRoamingSensor = preferences.getBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED, false);
                showScreenSensor = preferences.getBoolean(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED, false);
                showSMSSensor = preferences.getBoolean(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, false);
                showSoundProfileSensor = preferences.getBoolean(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED, false);
                showTimeSensor = preferences.getBoolean(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, false);
                showVolumesSensor = preferences.getBoolean(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED, false);
                showVPNSensor = preferences.getBoolean(EventPreferencesVPN.PREF_EVENT_VPN_ENABLED, false);
                showWifiSensor= preferences.getBoolean(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, false);
                showMusicSensor = preferences.getBoolean(EventPreferencesMusic.PREF_EVENT_MUSIC_ENABLED, false);
                showCallControlSensor = preferences.getBoolean(EventPreferencesCallControl.PREF_EVENT_CALL_CONTROL_ENABLED, false);
            }

            if (saveDisplayed && (activity != null)) {
                activity.displayedSensors.clear();
                if (showAccessoriesSensor)
                    activity.displayedSensors.add(EventPreferencesAccessories.PREF_EVENT_ACCESSORIES_ENABLED);
                if (showActivatedProfileSensor)
                    activity.displayedSensors.add(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_ENABLED);
                if (showAlarmClockSensor)
                    activity.displayedSensors.add(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED);
                if (showApplicationSensor)
                    activity.displayedSensors.add(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED);
                if (showBatterySensor)
                    activity.displayedSensors.add(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED);
                if (showBluetoothSensor)
                    activity.displayedSensors.add(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED);
                if (showBrightnessSensor)
                    activity.displayedSensors.add(EventPreferencesBrightness.PREF_EVENT_BRIGHTNESS_ENABLED);
                if (showCalendarSensor)
                    activity.displayedSensors.add(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED);
                if (showCallSensor)
                    activity.displayedSensors.add(EventPreferencesCall.PREF_EVENT_CALL_ENABLED);
                if (showDeviceBootSensor)
                    activity.displayedSensors.add(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_ENABLED);
                if (showLocationSensor)
                    activity.displayedSensors.add(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED);
                if (showMobileCellsSensor)
                    activity.displayedSensors.add(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED);
                if (showNFCSensor)
                    activity.displayedSensors.add(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED);
                if (showNotificationSensor)
                    activity.displayedSensors.add(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED);
                if (showOrientationSensor)
                    activity.displayedSensors.add(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED);
                if (showPeriodicSensor)
                    activity.displayedSensors.add(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_ENABLED);
                if (showRadioSwitchSensor)
                    activity.displayedSensors.add(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED);
                if (showRoamingSensor)
                    activity.displayedSensors.add(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED);
                if (showScreenSensor)
                    activity.displayedSensors.add(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED);
                if (showSMSSensor)
                    activity.displayedSensors.add(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED);
                if (showSoundProfileSensor)
                    activity.displayedSensors.add(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED);
                if (showTimeSensor)
                    activity.displayedSensors.add(EventPreferencesTime.PREF_EVENT_TIME_ENABLED);
                if (showVolumesSensor)
                    activity.displayedSensors.add(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED);
                if (showVPNSensor)
                    activity.displayedSensors.add(EventPreferencesVPN.PREF_EVENT_VPN_ENABLED);
                if (showWifiSensor)
                    activity.displayedSensors.add(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED);
                if (showMusicSensor)
                    activity.displayedSensors.add(EventPreferencesMusic.PREF_EVENT_MUSIC_ENABLED);
                if (showCallControlSensor)
                    activity.displayedSensors.add(EventPreferencesCallControl.PREF_EVENT_CALL_CONTROL_ENABLED);
            }

            Preference preference = prefMng.findPreference(PREF_EVENT_HIDE_NOT_USED_SENSORS);
            if ((!showAccessoriesSensor) &&
                    (!showActivatedProfileSensor) &&
                    (!showAlarmClockSensor) &&
                    (!showApplicationSensor) &&
                    (!showBatterySensor) &&
                    (!showBluetoothSensor) &&
                    (!showBrightnessSensor) &&
                    (!showCalendarSensor) &&
                    (!showCallSensor) &&
                    (!showDeviceBootSensor) &&
                    (!showLocationSensor) &&
                    (!showMobileCellsSensor) &&
                    (!showNFCSensor) &&
                    (!showNotificationSensor) &&
                    (!showOrientationSensor) &&
                    (!showPeriodicSensor) &&
                    (!showRadioSwitchSensor) &&
                    (!showRoamingSensor) &&
                    (!showScreenSensor) &&
                    (!showSMSSensor) &&
                    (!showSoundProfileSensor) &&
                    (!showTimeSensor) &&
                    (!showVolumesSensor) &&
                    (!showVPNSensor) &&
                    (!showWifiSensor) &&
                    (!showMusicSensor) &&
                    (!showCallControlSensor)) {
                hideSensors = false;
                if (preference != null)
                    preference.setEnabled(false);
            } else {
                if (preference != null)
                    preference.setEnabled(true);
            }

            preference = prefMng.findPreference(EventPreferencesAccessories.PREF_EVENT_ACCESSORIES_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showAccessoriesSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showActivatedProfileSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showAlarmClockSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showApplicationSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesBattery.PREF_EVENT_BATTERY_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showBatterySensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showBluetoothSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesBrightness.PREF_EVENT_BRIGHTNESS_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showBrightnessSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesCalendar.PREF_EVENT_CALENDAR_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showCalendarSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showCallSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showDeviceBootSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showLocationSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showMobileCellsSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesNFC.PREF_EVENT_NFC_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showNFCSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showNotificationSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showOrientationSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showPeriodicSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showRadioSwitchSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesRoaming.PREF_EVENT_ROAMING_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showRoamingSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesScreen.PREF_EVENT_SCREEN_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showScreenSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showSMSSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showSoundProfileSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesTime.PREF_EVENT_TIME_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showTimeSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesVolumes.PREF_EVENT_VOLUMES_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showVolumesSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesVPN.PREF_EVENT_VPN_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showVPNSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_CATEGORY_ROOT);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showWifiSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesMusic.PREF_EVENT_MUSIC_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showMusicSensor;
                preference.setVisible(showSensor);
            }
            preference = prefMng.findPreference(EventPreferencesCallControl.PREF_EVENT_CALL_CONTROL_CATEGORY);
            if (preference != null) {
                boolean showSensor = !hideSensors;
                if (hideSensors)
                    showSensor = showCallControlSensor;
                preference.setVisible(showSensor);
            }
        }
    }

    private static class SetRedTextToPreferencesAsyncTask extends AsyncTask<Void, Integer, Void> {

        Event event;
        boolean isEnabledSomeSensor;
        ArrayList<PermissionType> eventPermissions;
        boolean eventIsRunnable;
        boolean eventIsAllConfigured;
        int accessibilityEnabled;

        private final WeakReference<PreferenceManager> prefMngWeakRef;
        private final WeakReference<Context> contextWeakReference;
        private final WeakReference<EventsPrefsActivity> activityWeakReference;
        private final WeakReference<EventsPrefsFragment> fragmentWeakReference;

        public SetRedTextToPreferencesAsyncTask(final EventsPrefsActivity activity,
                                                final EventsPrefsFragment fragment,
                                                final PreferenceManager prefMng,
                                                final Context context) {
            this.prefMngWeakRef = new WeakReference<>(prefMng);
            this.contextWeakReference = new WeakReference<>(context);
            this.activityWeakReference = new WeakReference<>(activity);
            this.fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            EventsPrefsActivity activity = activityWeakReference.get();

            if ((context != null) && (activity != null)) {

                long event_id = activity.event_id;
                int newEventMode = activity.newEventMode;
                int predefinedEventIndex = activity.predefinedEventIndex;
                event = activity.getEventFromPreferences(event_id, newEventMode, predefinedEventIndex);

                if (event != null) {
                    isEnabledSomeSensor = event.isEnabledSomeSensor(context);
                    eventPermissions = Permissions.checkEventPermissions(context, event, null, EventsHandler.SENSOR_TYPE_ALL);
                    accessibilityEnabled = event.isAccessibilityServiceEnabled(context, false, false);
                    eventIsRunnable = event.isRunnable(context, false);
                    eventIsAllConfigured = event.isAllConfigured(context, false);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Context context = contextWeakReference.get();
            PreferenceManager prefMng = prefMngWeakRef.get();
            EventsPrefsActivity activity = activityWeakReference.get();
            EventsPrefsFragment fragment = fragmentWeakReference.get();

            if ((context != null) && (activity != null) && (fragment != null) && (prefMng != null)) {

                String rootScreen = PPApplication.PREF_ROOT_SCREEN;
                int errorColor = ContextCompat.getColor(context, R.color.errorColor);

                if (event != null) {
                    int order = 1;

                    // not enabled some sensor
                    Preference preference = prefMng.findPreference(PREF_NOT_ENABLED_SOME_SENSOR);
                    if (isEnabledSomeSensor) {
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    }
                    else {
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new ExclamationPreference(context);
                                preference.setKey(PREF_NOT_ENABLED_SOME_SENSOR);
                                preference.setIconSpaceReserved(false);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-99);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title = order + ". " + context.getString(R.string.event_preferences_no_sensor_is_enabled);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            _title = context.getString(R.string.event_preferences_sensor_parameters_location_summary);
                            Spannable summary = new SpannableString(_title);
                            summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                            preference.setSummary(summary);
                        }
                    }

                    // not some permissions
                    if (eventPermissions.isEmpty()) {
                        preference = prefMng.findPreference(PREF_GRANT_PERMISSIONS);
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    }
                    else {
                        preference = prefMng.findPreference(PREF_GRANT_PERMISSIONS);
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                if (event._id > 0)
                                    preference = new StartActivityPreference(context);
                                else
                                    preference = new ExclamationPreference(context);
                                preference.setKey(PREF_GRANT_PERMISSIONS);
                                preference.setIconSpaceReserved(false);
                                //if (event._id > 0)
                                //    preference.setWidgetLayoutResource(R.layout.preference_widget_start_activity);
                                //else
                                //    preference.setWidgetLayoutResource(R.layout.preference_widget_exclamation_preference);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-98);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title = order + ". " + context.getString(R.string.preferences_grantPermissions_title);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            _title = context.getString(R.string.preferences_grantPermissions_summary) + " " +
                                    context.getString(R.string.event_preferences_red_sensors_summary) + " " +
                                    context.getString(R.string.event_preferences_sensor_parameters_location_summary);
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

                    preference = prefMng.findPreference(PREF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                    if (accessibilityEnabled == 1) {
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    }
                    else {
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new StartActivityPreference(context);
                                preference.setKey(PREF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                                preference.setIconSpaceReserved(false);
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
                            String _title = order + ". " + context.getString(stringRes);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            if ((accessibilityEnabled == -1) || (accessibilityEnabled == -2)) {
                                _title = context.getString(R.string.event_preferences_red_install_PPPExtender) + " " +
                                        context.getString(R.string.event_preferences_red_sensors_summary) + " " +
                                        context.getString(R.string.event_preferences_sensor_parameters_location_summary);
                                Spannable summary = new SpannableString(_title);
                                summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                                preference.setSummary(summary);

                                preference.setOnPreferenceClickListener(preference12 -> {
                                    ExtenderDialogPreferenceFragment.installPPPExtender(activity, /*null,*/ false);
                                    return false;
                                });
                            }
                            else {
                                _title = context.getString(R.string.event_preferences_red_enable_PPPExtender) + " " +
                                        context.getString(R.string.event_preferences_red_sensors_summary) + " " +
                                        context.getString(R.string.event_preferences_sensor_parameters_location_summary);
                                Spannable summary = new SpannableString(_title);
                                summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                                preference.setSummary(summary);

                                preference.setOnPreferenceClickListener(preference13 -> {
                                    ExtenderDialogPreferenceFragment.enableExtender(activity/*, null*/);
                                    return false;
                                });
                            }
                        }
                    }

                    // not is runnable
                    if (eventIsRunnable) {
                        preference = prefMng.findPreference(PREF_NOT_IS_RUNNABLE);
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    }
                    else {
                        preference = prefMng.findPreference(PREF_NOT_IS_RUNNABLE);
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new ExclamationPreference(context);
                                preference.setKey(PREF_NOT_IS_RUNNABLE);
                                preference.setIconSpaceReserved(false);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-100);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title = order + ". " + context.getString(R.string.event_preferences_not_set_underlined_parameters);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            _title = context.getString(R.string.event_preferences_not_set_underlined_parameters_summary) + " " +
                                    context.getString(R.string.event_preferences_red_sensors_summary) + " " +
                                    context.getString(R.string.event_preferences_sensor_parameters_location_summary);
                            Spannable summary = new SpannableString(_title);
                            summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                            preference.setSummary(summary);
                        }
                    }

                    // not is configures some parameter
                    if (eventIsAllConfigured) {
                        preference = prefMng.findPreference(PREF_NOT_IS_ALL_CONFIGURED);
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    }
                    else {
                        preference = prefMng.findPreference(PREF_NOT_IS_ALL_CONFIGURED);
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new ExclamationPreference(context);
                                preference.setKey(PREF_NOT_IS_ALL_CONFIGURED);
                                preference.setIconSpaceReserved(false);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-100);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title = order + ". " + context.getString(R.string.event_preferences_not_all_parameters_are_configured);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            _title = context.getString(R.string.event_preferences_not_all_parameters_are_configured_summary) + " " +
                                    //context.getString(R.string.event_preferences_red_sensors_summary) + " " +
                                    context.getString(R.string.event_preferences_sensor_parameters_location_summary);
                            Spannable summary = new SpannableString(_title);
                            summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                            preference.setSummary(summary);
                        }
                    }
                }
                else {
                    Preference preference = prefMng.findPreference(PREF_NOT_IS_RUNNABLE);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_NOT_IS_ALL_CONFIGURED);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_NOT_ENABLED_SOME_SENSOR);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_GRANT_PERMISSIONS);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                }

            }
        }

    }

}