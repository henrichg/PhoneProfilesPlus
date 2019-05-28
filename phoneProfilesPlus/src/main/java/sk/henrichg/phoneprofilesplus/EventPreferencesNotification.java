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

class EventPreferencesNotification extends EventPreferences {

    String _applications;
    boolean _inCall;
    boolean _missedCall;

    static final String PREF_EVENT_NOTIFICATION_ENABLED = "eventNotificationEnabled";
    private static final String PREF_EVENT_NOTIFICATION_APPLICATIONS = "eventNotificationApplications";
    private static final String PREF_EVENT_NOTIFICATION_IN_CALL = "eventNotificationInCall";
    private static final String PREF_EVENT_NOTIFICATION_MISSED_CALL = "eventNotificationMissedCall";
    static final String PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS = "eventNotificationNotificationsAccessSettings";

    private static final String PREF_EVENT_NOTIFICATION_CATEGORY = "eventNotificationCategoryRoot";

    EventPreferencesNotification(Event event,
                                        boolean enabled,
                                        String applications,
                                        boolean inCall,
                                        boolean missedCall)
    {
        super(event, enabled);

        this._applications = applications;
        this._inCall = inCall;
        this._missedCall = missedCall;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesNotification._enabled;
        this._applications = fromEvent._eventPreferencesNotification._applications;
        this._inCall = fromEvent._eventPreferencesNotification._inCall;
        this._missedCall = fromEvent._eventPreferencesNotification._missedCall;
        this.setSensorPassed(fromEvent._eventPreferencesNotification.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_NOTIFICATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_NOTIFICATION_APPLICATIONS, this._applications);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, this._inCall);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, this._missedCall);
            editor.apply();
        //}
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
            this._applications = preferences.getString(PREF_EVENT_NOTIFICATION_APPLICATIONS, "");
            this._inCall = preferences.getBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, false);
            this._missedCall = preferences.getBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, false);
        //}
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_notification_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_NOTIFICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_notifications), addPassStatus, DatabaseHandler.ETYPE_NOTIFICATION, context);
                    descr = descr + ": </b>";
                }

                if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                    descr = descr + "* " + context.getString(R.string.event_preferences_notificationsAccessSettings_disabled_summary) + "! *";
                } else {
                    descr = descr + context.getString(R.string.event_preferences_notificationsAccessSettings_enabled_summary) + "<br>";

                    if (this._inCall) {
                        descr = descr + context.getString(R.string.event_preferences_notifications_inCall);
                    }
                    if (this._missedCall) {
                        if (this._inCall)
                            descr = descr + " • ";
                        descr = descr + context.getString(R.string.event_preferences_notifications_missedCall);
                    }
                    String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    if (!this._applications.isEmpty() && !this._applications.equals("-")) {
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
                    if (this._inCall || this._missedCall)
                        descr = descr + " • ";
                    descr = descr + /*"(S) "+*/context.getString(R.string.event_preferences_notifications_applications) + ": " + selectedApplications;// + "; ";
                }
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_NOTIFICATION_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preference.isChecked(), true, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.event_preferences_volumeNotificationsAccessSettings_summary);
                if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.event_preferences_notificationsAccessSettings_disabled_summary) + "! *\n\n"+
                            summary;
                }
                else {
                    summary = context.getString(R.string.event_preferences_notificationsAccessSettings_enabled_summary) + ".\n\n"+
                            summary;
                }
                preference.setSummary(summary);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesNotification.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesNotification.isRunnable(context);
        SwitchPreferenceCompat enabledPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_ENABLED);
        boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
        SwitchPreferenceCompat preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_IN_CALL);
        if (preference != null) {
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, preference.isChecked(), true, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_MISSED_CALL);
        if (preference != null) {
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, preference.isChecked(), true, true, !isRunnable, false);
        }
        Preference applicationsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_APPLICATIONS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(applicationsPreference, enabled, bold, true, true, !isRunnable, false);
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_NOTIFICATION_ENABLED) ||
            key.equals(PREF_EVENT_NOTIFICATION_IN_CALL) ||
            key.equals(PREF_EVENT_NOTIFICATION_MISSED_CALL)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_APPLICATIONS)||
            key.equals(PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS)/* ||
            key.equals(PREF_EVENT_NOTIFICATION_DURATION)*/)
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_IN_CALL, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_MISSED_CALL, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_NOTIFICATION_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesNotification tmp = new EventPreferencesNotification(this._event, this._enabled,
                                                        this._applications, this._inCall, this._missedCall/*,
                                                        this._permanentRun, this._duration, this._endWhenRemoved*/);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
            if (preference != null) {
                SwitchPreferenceCompat enabledPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_ENABLED);
                boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, true, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && (_inCall || _missedCall || (!_applications.isEmpty()));

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            boolean enabled = PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
            ApplicationsMultiSelectDialogPreferenceX applicationsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
            Preference ringingCallPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_IN_CALL);
            Preference missedCallPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_MISSED_CALL);
            if (applicationsPreference != null) {
                applicationsPreference.setEnabled(enabled);
                applicationsPreference.setSummaryAMSDP();
            }
            if (ringingCallPreference != null) {
                ringingCallPreference.setEnabled(enabled);
            }
            if (missedCallPreference != null) {
                missedCallPreference.setEnabled(enabled);
            }

            SharedPreferences preferences = prefMng.getSharedPreferences();
            setSummary(prefMng, PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS, preferences, context);
            setCategorySummary(prefMng, preferences, context);
        /*}
        else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) prefMng.findPreference("eventPreferenceScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) prefMng.findPreference("eventNotificationCategory");
            if ((preferenceCategory != null) && (preferenceScreen != null))
                preferenceScreen.removePreference(preferenceCategory);
        }*/
    }

    // search if any configured package names are visible in status bar
    boolean isNotificationVisible(DataWrapper dataWrapper) {
        // get all saved notifications
        PPNotificationListenerService.getNotifiedPackages(dataWrapper.context);

        // com.android.incallui - in call
        // com.samsung.android.incallui - in call
        // com.google.android.dialer - in call
        // com.android.server.telecom - missed call

        if (this._inCall) {
            // Nexus/Pixel??? stock ROM
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted("com.google.android.dialer", false);
            if (notification != null)
                return true;
            // Samsung, MIUI, Sony
            notification = PPNotificationListenerService.getNotificationPosted("android.incallui", true);
            if (notification != null)
                return true;
        }
        if (this._missedCall) {
            // Samsung, MIUI, Nexus/Pixel??? stock ROM, Sony
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted("com.android.server.telecom", false);
            if (notification != null)
                return true;
            // LG
            notification = PPNotificationListenerService.getNotificationPosted("com.android.phone", false);
            if (notification != null)
                return true;
        }

        String[] splits = this._applications.split("\\|");
        for (String split : splits) {
            // get only package name = remove activity
            String packageName = Application.getPackageName(split);
            // search for package name in saved package names
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted(packageName, false);
            if (notification != null)
                return true;
        }
        return false;
    }

}
