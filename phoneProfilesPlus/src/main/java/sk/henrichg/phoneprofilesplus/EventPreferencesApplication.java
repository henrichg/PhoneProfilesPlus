package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
//import android.preference.CheckBoxPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;

class EventPreferencesApplication extends EventPreferences {

    String _applications;
    //long _startTime;
    //int _duration;

    static final String PREF_EVENT_APPLICATION_ENABLED = "eventApplicationEnabled";
    private static final String PREF_EVENT_APPLICATION_APPLICATIONS = "eventApplicationApplications";
    static final String PREF_EVENT_APPLICATION_INSTALL_EXTENDER = "eventApplicationInstallExtender";
    static final String PREF_EVENT_APPLICATION_ACCESSIBILITY_SETTINGS = "eventApplicationAccessibilitySettings";
    static final String PREF_EVENT_APPLICATION_LAUNCH_EXTENDER = "eventApplicationLaunchExtender";

    private static final String PREF_EVENT_APPLICATION_CATEGORY = "eventApplicationCategoryRoot";

    EventPreferencesApplication(Event event,
                                       boolean enabled,
                                       String applications/*,
                                       int duration*/)
    {
        super(event, enabled);

        this._applications = applications;
        //this._duration = duration;

        //this._startTime = 0;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesApplication._enabled;
        this._applications = fromEvent._eventPreferencesApplication._applications;
        //this._duration = fromEvent._eventPreferencesNotification._duration;
        this.setSensorPassed(fromEvent._eventPreferencesApplication.getSensorPassed());

        //this._startTime = 0;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_APPLICATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_APPLICATION_APPLICATIONS, this._applications);
            //editor.putString(PREF_EVENT_NOTIFICATION_DURATION, String.valueOf(this._duration));
            editor.apply();
        //}
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
            this._applications = preferences.getString(PREF_EVENT_APPLICATION_APPLICATIONS, "");
            //this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_DURATION, "5"));
        //}
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_application_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_APPLICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_applications), addPassStatus, DatabaseHandler.ETYPE_APPLICATION, context);
                    descr = descr + "</b> ";
                }

                String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    selectedApplications = context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0) {
                    selectedApplications = context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context.getApplicationContext())) {
                    selectedApplications = context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                } else if (!this._applications.isEmpty() && !this._applications.equals("-")) {
                    String[] splits = this._applications.split("\\|");
                    if (splits.length == 1) {
                        String packageName = Application.getPackageName(splits[0]);
                        String activityName = Application.getActivityName(splits[0]);
                        PackageManager packageManager = context.getPackageManager();
                        if (activityName.isEmpty()) {
                            ApplicationInfo app;
                            try {
                                app = packageManager.getApplicationInfo(packageName, 0);
                                if (app != null)
                                    selectedApplications = packageManager.getApplicationLabel(app).toString();
                            } catch (Exception e) {
                                selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(packageName, activityName);
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                            if (info != null)
                                selectedApplications = info.loadLabel(packageManager).toString();
                        }
                    } else
                        selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                }
                descr = descr + /*"(S) "+*/context.getString(R.string.event_preferences_applications_applications) + ": <b>" + selectedApplications + "</b>";

                //descr = descr + context.getString(R.string.event_preferences_notifications_applications) + ": " +selectedApplications + "; ";
                //descr = descr + context.getString(R.string.pref_event_duration) + ": " +tmp._duration;
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key/*, String value*/, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_APPLICATION_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_APPLICATION_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_install_summary);
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesApplication.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesApplication.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_APPLICATIONS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_APPLICATION_APPLICATIONS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }
        boolean isAccessibilityEnabled = event._eventPreferencesApplication.isAccessibilityServiceEnabled(context) == 1;
        preference = prefMng.findPreference(PREF_EVENT_APPLICATION_ACCESSIBILITY_SETTINGS);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, true, !isAccessibilityEnabled, false);
    }

    void setSummary(PreferenceManager prefMng, String key,
                    @SuppressWarnings("unused") SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_APPLICATION_ENABLED)) {
            //boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, /*value ? "true" : "false",*/ context);
        }
        if (key.equals(PREF_EVENT_APPLICATION_APPLICATIONS) ||
            key.equals(PREF_EVENT_APPLICATION_INSTALL_EXTENDER))
        {
            setSummary(prefMng, key, /*preferences.getString(key, ""),*/ context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_APPLICATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_APPLICATION_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_APPLICATION_INSTALL_EXTENDER, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_APPLICATION_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesApplication tmp = new EventPreferencesApplication(this._event, this._enabled, this._applications);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && (tmp.isAccessibilityServiceEnabled(context) == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(runnable && permissionGranted), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {
        boolean runnable = super.isRunnable(context);

        runnable = runnable && (!_applications.isEmpty());

        return runnable;
    }

    @Override
    int isAccessibilityServiceEnabled(Context context)
    {
        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
        if (extenderVersion == 0)
            return -2;
        if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0)
            return -1;
        if (PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context))
            return 1;
        return 0;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context) {
        final boolean accessibilityEnabled =
                PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0);
        ApplicationsMultiSelectDialogPreferenceX applicationsPreference = prefMng.findPreference(PREF_EVENT_APPLICATION_APPLICATIONS);
        if (applicationsPreference != null) {
            applicationsPreference.setEnabled(accessibilityEnabled);
            applicationsPreference.setSummaryAMSDP();
        }

        SharedPreferences preferences = prefMng.getSharedPreferences();

        boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_ACCESSIBILITY_SETTINGS);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, true, !accessibilityEnabled, false);

        setCategorySummary(prefMng, preferences, context);
    }

    /*
    @Override
    void setSystemEventForStart(Context context)
    {
    }

    @Override
    void setSystemEventForPause(Context context)
    {
    }

    @Override
    void removeSystemEvent(Context context)
    {
    }
    */

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                eventsHandler.applicationPassed = false;

                if (PPPExtenderBroadcastReceiver.isEnabled(eventsHandler.context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0)) {
                    String foregroundApplication = ApplicationPreferences.prefApplicationInForeground;

                    if (!foregroundApplication.isEmpty()) {
                        String[] splits = _applications.split("\\|");
                        for (String split : splits) {
                            String packageName = Application.getPackageName(split);

                            if (foregroundApplication.equals(packageName)) {
                                eventsHandler.applicationPassed = true;
                                break;
                            }
                        }
                    } else
                        eventsHandler.notAllowedApplication = true;
                } else
                    eventsHandler.notAllowedApplication = true;

                if (!eventsHandler.notAllowedApplication) {
                    if (eventsHandler.applicationPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedApplication = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesApplication.doHandleEvent", "application - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_APPLICATION);
            }
        }
    }

}
