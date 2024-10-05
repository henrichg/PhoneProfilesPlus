package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class EventPreferencesNotification extends EventPreferences {

    String _applications;
    boolean _inCall;
    boolean _missedCall;
    int _duration;
    boolean _checkContacts;
    String _contacts; // contactId#phoneId|...
    String _contactGroups; // groupId|...
    int _contactListType;
    boolean _checkText;
    String _text;

    static final String PREF_EVENT_NOTIFICATION_ENABLED = "eventNotificationEnabled";
    private static final String PREF_EVENT_NOTIFICATION_APPLICATIONS = "eventNotificationApplications";
    private static final String PREF_EVENT_NOTIFICATION_IN_CALL = "eventNotificationInCall";
    private static final String PREF_EVENT_NOTIFICATION_MISSED_CALL = "eventNotificationMissedCall";
    static final String PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS = "eventNotificationNotificationsAccessSettings";
    private static final String PREF_EVENT_NOTIFICATION_DURATION = "eventNotificationDuration";
    private static final String PREF_EVENT_NOTIFICATION_CHECK_CONTACTS = "eventNotificationCheckContacts";
    private static final String PREF_EVENT_NOTIFICATION_CHECK_TEXT = "eventNotificationCheckText";
    private static final String PREF_EVENT_NOTIFICATION_CONTACT_GROUPS = "eventNotificationContactGroups";
    private static final String PREF_EVENT_NOTIFICATION_CONTACTS = "eventNotificationContacts";
    private static final String PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE = "eventNotificationContactListType";
    private static final String PREF_EVENT_NOTIFICATION_TEXT = "eventNotificationText";
    static final String PREF_EVENT_NOTIFICATION_APP_SETTINGS = "eventEnableNotificationScanningAppSettings";

    static final String PREF_EVENT_NOTIFICATION_CATEGORY = "eventNotificationCategoryRoot";

    EventPreferencesNotification(Event event,
                                        boolean enabled,
                                        String applications,
                                        boolean inCall,
                                        boolean missedCall,
                                        int duration,
                                        boolean checkContacts,
                                        String contactGroups,
                                        String contacts,
                                        boolean checkText,
                                        String text,
                                        int contactListType
                                 )
    {
        super(event, enabled);

        this._applications = applications;
        this._inCall = inCall;
        this._missedCall = missedCall;
        this._duration = duration;
        this._checkContacts = checkContacts;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._checkText = checkText;
        this._text = text;
        this._contactListType = contactListType;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesNotification._enabled;
        this._applications = fromEvent._eventPreferencesNotification._applications;
        this._inCall = fromEvent._eventPreferencesNotification._inCall;
        this._missedCall = fromEvent._eventPreferencesNotification._missedCall;
        this._duration = fromEvent._eventPreferencesNotification._duration;
        this._checkContacts = fromEvent._eventPreferencesNotification._checkContacts;
        this._contacts = fromEvent._eventPreferencesNotification._contacts;
        this._contactGroups = fromEvent._eventPreferencesNotification._contactGroups;
        this._checkText = fromEvent._eventPreferencesNotification._checkText;
        this._text = fromEvent._eventPreferencesNotification._text;
        this._contactListType = fromEvent._eventPreferencesNotification._contactListType;

        this.setSensorPassed(fromEvent._eventPreferencesNotification.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_NOTIFICATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_NOTIFICATION_APPLICATIONS, this._applications);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, this._inCall);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, this._missedCall);
            editor.putString(PREF_EVENT_NOTIFICATION_DURATION, String.valueOf(this._duration));
            editor.putBoolean(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS, this._checkContacts);
            editor.putString(PREF_EVENT_NOTIFICATION_CONTACTS, this._contacts);
            editor.putString(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS, this._contactGroups);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_CHECK_TEXT, this._checkText);
            editor.putString(PREF_EVENT_NOTIFICATION_TEXT, this._text);
            editor.putString(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
            editor.apply();
        //}
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
            this._applications = preferences.getString(PREF_EVENT_NOTIFICATION_APPLICATIONS, "");
            this._inCall = preferences.getBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, false);
            this._missedCall = preferences.getBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, false);
            this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_DURATION, "0"));
            this._checkContacts = preferences.getBoolean(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS, false);
            this._contacts = preferences.getString(PREF_EVENT_NOTIFICATION_CONTACTS, "");
            this._contactGroups = preferences.getString(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS, "");
            this._checkText = preferences.getBoolean(PREF_EVENT_NOTIFICATION_CHECK_TEXT, false);
            this._text = preferences.getString(PREF_EVENT_NOTIFICATION_TEXT, "");
            this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE, "0"));
        //}
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_notification_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_NOTIFICATION_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_notifications), addPassStatus, DatabaseHandler.ETYPE_NOTIFICATION, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                if (!ApplicationPreferences.applicationEventNotificationEnableScanning) {
                    if (!ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile)
                        _value.append("* ").append(context.getString(R.string.array_pref_applicationDisableScanning_disabled)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                    else
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile)).append(StringConstants.TAG_BREAK_HTML);
                } else if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
                    _value.append("* ").append(context.getString(R.string.event_preferences_notificationsAccessSettings_disabled_summary)).append("! *");
                } else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventNotificationScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused)).append(StringConstants.TAG_BREAK_HTML);
                    } else {

                        //descr = descr + context.getString(R.string.event_preferences_notificationsAccessSettings_enabled_summary) + "<br>";

                        if (this._inCall) {
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_notifications_inCall), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        if (this._missedCall) {
                            if (this._inCall)
                                _value.append(StringConstants.STR_BULLET);
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_notifications_missedCall), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                        if (!this._applications.isEmpty() && !this._applications.equals("-")) {
                            String[] splits = this._applications.split(StringConstants.STR_SPLIT_REGEX);
                            if (splits.length == 1) {
                                String packageName = Application.getPackageName(splits[0]);
                                String activityName = Application.getActivityName(splits[0]);
                                PackageManager packageManager = context.getPackageManager();
                                if (activityName.isEmpty()) {
                                    ApplicationInfo app;
                                    try {
                                        app = packageManager.getApplicationInfo(packageName, PackageManager.MATCH_ALL);
                                        //if (app != null)
                                            selectedApplications = packageManager.getApplicationLabel(app).toString();
                                    } catch (Exception e) {
                                        selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                                    }
                                } else {
                                    Intent intent = new Intent();
                                    intent.setClassName(packageName, activityName);
                                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                                    if (info != null)
                                        selectedApplications = info.loadLabel(packageManager).toString();
                                }
                            } else
                                selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                        }
                        if (this._inCall || this._missedCall)
                            _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_notifications_applications)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedApplications, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);

                        if (this._checkContacts) {
                            _value.append(StringConstants.STR_BULLET);
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_notifications_checkContacts), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_COLON_WITH_SPACE);

                            _value.append(context.getString(R.string.event_preferences_notifications_contact_groups)).append(StringConstants.STR_COLON_WITH_SPACE);
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactGroupsMultiSelectDialogPreference.getSummary(_contactGroups, context), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                            _value.append(context.getString(R.string.event_preferences_notifications_contacts)).append(StringConstants.STR_COLON_WITH_SPACE);
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactsMultiSelectDialogPreference.getSummary(_contacts, true, context), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                            _value.append(context.getString(R.string.event_preferences_contactListType)).append(StringConstants.STR_COLON_WITH_SPACE);
                            String[] contactListTypes = context.getResources().getStringArray(R.array.eventNotificationContactListTypeArray);
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(contactListTypes[this._contactListType], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        if (this._checkText) {
                            _value.append(StringConstants.STR_BULLET);
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_notifications_checkText), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_COLON_WITH_SPACE);

                            _value.append(context.getString(R.string.event_preferences_notifications_text)).append(StringConstants.STR_COLON_WITH_SPACE);
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(_text, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.pref_event_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._duration), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    }
                }
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_NOTIFICATION_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_NOTIFICATION_ENABLED) ||
            key.equals(PREF_EVENT_NOTIFICATION_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventNotificationEnableScanning) {
                    if (!ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *"+StringConstants.STR_DOUBLE_NEWLINE +
                                context.getString(R.string.phone_profiles_pref_eventNotificationAppSettings_summary);
                        titleColor = ContextCompat.getColor(context, R.color.errorColor);
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + StringConstants.STR_DOUBLE_NEWLINE +
                                context.getString(R.string.phone_profiles_pref_eventNotificationAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventNotificationScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventNotificationAppSettings_summary);
                    } else {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventNotificationAppSettings_summary);
                    }
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                int titleLenght = 0;
                if (sTitle != null)
                    titleLenght = sTitle.length();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, titleLenght, Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                if (preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int titleColor;
                String summary = context.getString(R.string.event_preferences_volumeNotificationsAccessSettings_summary2);
                if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext(), true)) {
                    summary = "* " + context.getString(R.string.event_preferences_notificationsAccessSettings_disabled_summary) + "! *"+StringConstants.STR_DOUBLE_NEWLINE+
                            summary;
                    titleColor = ContextCompat.getColor(context, R.color.errorColor);
                }
                else {
                    summary = context.getString(R.string.event_preferences_notificationsAccessSettings_enabled_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT+
                            summary;
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                int titleLenght = 0;
                if (sTitle != null)
                    titleLenght = sTitle.length();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, titleLenght, Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                if (preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 0;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 0, false, false, false, false);
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        if (key.equals(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACTS);
            if (preference != null) {
                preference.setEnabled(value.equals(StringConstants.TRUE_STRING));
            }
            preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS);
            if (preference != null) {
                preference.setEnabled(value.equals(StringConstants.TRUE_STRING));
            }
            preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE);
            if (preference != null) {
                preference.setEnabled(value.equals(StringConstants.TRUE_STRING));
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_CHECK_TEXT)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_TEXT);
            if (preference != null) {
                preference.setEnabled(value.equals(StringConstants.TRUE_STRING));
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesNotification.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesNotification.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesNotification.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
        SwitchPreferenceCompat switchPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_IN_CALL);
        if (switchPreference != null) {
            GlobalGUIRoutines.setPreferenceTitleStyleX(switchPreference, enabled, preferences.getBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, false), false, true, !isRunnable, false);
        }
        switchPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_MISSED_CALL);
        if (switchPreference != null) {
            GlobalGUIRoutines.setPreferenceTitleStyleX(switchPreference, enabled, preferences.getBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, false), false, true, !isRunnable, false);
        }
        Preference applicationsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
        if (applicationsPreference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_APPLICATIONS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(applicationsPreference, enabled, bold, false, true, !isRunnable, false);
        }

        Preference checkContactsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS);
        if (checkContactsPreference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(checkContactsPreference, enabled, bold, false, true, !isRunnable, false);

            Preference _preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS);
            if (_preference != null) {
                bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS, "").isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(_preference, enabled, bold, false, true, !isRunnable, false);
            }
            _preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACTS);
            if (_preference != null) {
                bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_CONTACTS, "").isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(_preference, enabled, bold, false, true, !isRunnable, false);
            }
            _preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE);
            if (_preference != null) {
                bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE, "").isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(_preference, enabled, bold, false, false, !isRunnable, false);
            }
        }

        Preference checkTextPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CHECK_TEXT);
        if (checkTextPreference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_NOTIFICATION_CHECK_TEXT, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(checkTextPreference, enabled, bold, false, true, !isRunnable, false);

            Preference _preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_TEXT);
            if (_preference != null) {
                bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_TEXT, "").isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(_preference, enabled, bold, false, false, !isRunnable, false);
            }
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_NOTIFICATION_ENABLED) ||
            key.equals(PREF_EVENT_NOTIFICATION_IN_CALL) ||
            key.equals(PREF_EVENT_NOTIFICATION_MISSED_CALL) ||
            key.equals(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS) ||
            key.equals(PREF_EVENT_NOTIFICATION_CHECK_TEXT)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_APPLICATIONS)||
            key.equals(PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS) ||
            key.equals(PREF_EVENT_NOTIFICATION_DURATION) ||
            key.equals(PREF_EVENT_NOTIFICATION_CONTACTS) ||
            key.equals(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS) ||
            key.equals(PREF_EVENT_NOTIFICATION_TEXT) ||
            key.equals(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE) ||
            key.equals(PREF_EVENT_NOTIFICATION_APP_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_IN_CALL, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_MISSED_CALL, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CHECK_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CHECK_TEXT, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_TEXT, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_APP_SETTINGS, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_NOTIFICATION_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesNotification tmp = new EventPreferencesNotification(this._event, this._enabled,
                                                        this._applications, this._inCall, this._missedCall, this._duration,
                                                        this._checkContacts, this._contactGroups, this._contacts,
                                                        this._checkText, this._text, this._contactListType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_NOTIFICATION).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        boolean okCheck = false;
        if (_inCall || _missedCall || (!_applications.isEmpty())) {
            if (_checkContacts) {
                runnable = runnable && ((_contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||
                        (!(_contacts.isEmpty() && _contactGroups.isEmpty())));
                okCheck = true;
            }

            if (_checkText) {
                runnable = runnable && (!_text.isEmpty());
                okCheck = true;
            }
        }

        return runnable && okCheck;
    }

    @Override
    boolean isAllConfigured(Context context)
    {
        boolean allConfigured = super.isAllConfigured(context);

        allConfigured = allConfigured && isRunnable(context);

        allConfigured = allConfigured &&
                (ApplicationPreferences.applicationEventNotificationEnableScanning ||
                        ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile);

        return allConfigured;
    }


    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_NOTIFICATION_ENABLED) != null) {
                //boolean enabled = /*ApplicationPreferences.applicationEventNotificationEnableScanning &&*/
                //        PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true);
                //Preference notififcationAccess = prefMng.findPreference(PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS);
                ApplicationsMultiSelectDialogPreference applicationsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
                //Preference ringingCallPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_IN_CALL);
                //Preference missedCallPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_MISSED_CALL);
                SwitchPreferenceCompat checkContactsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS);
                Preference contactGroupsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS);
                Preference contactsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACTS);
                SwitchPreferenceCompat checkTextPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CHECK_TEXT);
                Preference textPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_TEXT);
                Preference contactListTypePreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE);
                //Preference maximumDuration = prefMng.findPreference(PREF_EVENT_NOTIFICATION_DURATION);

                /*if (notififcationAccess != null) {
                    notififcationAccess.setEnabled(ApplicationPreferences.applicationEventNotificationEnableScanning);
                }*/
                if (applicationsPreference != null) {
                    //applicationsPreference.setEnabled(enabled);
                    applicationsPreference.setSummaryAMSDP();
                }
                //if (ringingCallPreference != null) {
                //    ringingCallPreference.setEnabled(enabled);
                //}
                //if (missedCallPreference != null) {
                //    missedCallPreference.setEnabled(enabled);
                //}
                //if (checkContactsPreference != null) {
                //    checkContactsPreference.setEnabled(enabled);
                //}
                if (contactGroupsPreference != null) {
                    boolean checkEnabled = (checkContactsPreference != null) && (checkContactsPreference.isChecked());
                    contactGroupsPreference.setEnabled(/*enabled &&*/ checkEnabled);
                }
                if (contactsPreference != null) {
                    boolean checkEnabled = (checkContactsPreference != null) && (checkContactsPreference.isChecked());
                    contactsPreference.setEnabled(/*enabled &&*/ checkEnabled);
                }
                if (contactListTypePreference != null) {
                    boolean checkEnabled = (checkContactsPreference != null) && (checkContactsPreference.isChecked());
                    contactListTypePreference.setEnabled(/*enabled &&*/ checkEnabled);
                }
                //if (checkTextPreference != null) {
                //    checkTextPreference.setEnabled(enabled);
                //}
                if (textPreference != null) {
                    boolean checkEnabled = (checkTextPreference != null) && (checkTextPreference.isChecked());
                    textPreference.setEnabled(/*enabled &&*/ checkEnabled);
                }
                //if (maximumDuration != null) {
                //    maximumDuration.setEnabled(enabled);
                //}

                setSummary(prefMng, PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS, preferences, context);
                setSummary(prefMng, PREF_EVENT_NOTIFICATION_APP_SETTINGS, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeAlarm(Context context)
    {
        if (this._duration != 0) {
            StatusBarNotification newestNotification = getNewestVisibleNotification(context);
            if (newestNotification != null) {
                return newestNotification.getPostTime() + this._duration * 1000L;
            }
        }
        return 0;
    }

    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        removeAlarm(context);

        if (!(isRunnable(context) && isAllConfigured(context) && _enabled))
            return;

        setAlarm(computeAlarm(context), context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);
    }

    private void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, NotificationEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_NOTIFICATION_EVENT_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setAlarm(long alarmTime, Context context)
    {
        if (alarmTime > 0) {
            //Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER);
            //intent.setClass(context, NotificationEventEndBroadcastReceiver.class);

            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    Intent editorIntent = new Intent(context, EditorActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                }
            }
        }
    }


    // test statusBarNotification for event parameters
    private StatusBarNotification isNotificationActive(StatusBarNotification statusBarNotification,
                                                       String packageName,
                                                       boolean checkEnd,
                                                       List<Contact> contactList) {
        try {
            String packageNameFromNotification = statusBarNotification.getPackageName();
//            Log.e("EventPreferencesNotification.isNotificationActive", "packageNameFromNotification="+packageNameFromNotification);

            boolean packageNameFound = false;
            if (checkEnd) {
                if (packageNameFromNotification.endsWith(packageName)) {
                    packageNameFound = true;
                }
            } else {
                if (packageNameFromNotification.equals(packageName)) {
                    packageNameFound = true;
                }
            }

            if (packageNameFound) {

                boolean testText = false;

                String notificationTicker = "";
                String notificationTitle = "";
                String notificationText = "";

                if (_checkContacts || _checkText) {
                    if (statusBarNotification.getNotification().tickerText != null) {
                        notificationTicker = statusBarNotification.getNotification().tickerText.toString();
                        testText = true;
                    }
                    Bundle extras = statusBarNotification.getNotification().extras;
                    if (extras != null) {
                        String _text1 = extras.getString("android.title");
                        if (_text1 != null) {
                            notificationTitle = _text1;
                            testText = true;
                        }
                        CharSequence _text2 = extras.getCharSequence("android.text");
                        if (_text2 != null) {
                            notificationText = _text2.toString();
                            testText = true;
                        }
                    }
                }

                boolean textFound = false;
                if (testText) {
                    // title or text or ticker is set in notification

                    if (_checkContacts) {
                        if (_contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {
                            boolean phoneNumberFound = false;
                            if (!notificationTitle.isEmpty())
                                phoneNumberFound = isContactConfigured(notificationTitle, contactList);
                            if (!notificationText.isEmpty() && (!phoneNumberFound))
                                phoneNumberFound = isContactConfigured(notificationText, contactList);
                            if (!notificationTicker.isEmpty() && (!phoneNumberFound))
                                phoneNumberFound = isContactConfigured(notificationTicker, contactList);

                            if (_contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_WHITE_LIST)
                                textFound = phoneNumberFound;
                            else
                                textFound = !phoneNumberFound;
                        } else
                            // all contacts
                            textFound = true;
                    }
                    if (_checkText) {
                        String searchText = "";
                        for (int whatTest = 0; whatTest < 3; whatTest++) {
                            // test in loop title (0), text(1), ticker(2)
                            if (whatTest == 0) {
                                if (!notificationTitle.isEmpty())
                                    searchText = notificationTitle;
                                else
                                    continue;
                            }
                            if (whatTest == 1) {
                                if (!notificationText.isEmpty())
                                    searchText = notificationText;
                                else
                                    continue;
                            }
                            if (whatTest == 2) {
                                if (!notificationTicker.isEmpty())
                                    searchText = notificationTicker;
                                else
                                    continue;
                            }

                            String[] textSplits = _text.split(StringConstants.STR_SPLIT_REGEX);

                            String[] positiveList = new String[textSplits.length];
                            String[] negativeList = new String[textSplits.length];
                            int argsId;

                            // positive strings
                            boolean positiveExists = false;
                            argsId = 0;
                            for (String split : textSplits) {
                                if (!split.isEmpty()) {
                                    String searchPattern = split;

                                    if (searchPattern.startsWith("!")) {
                                        // only positive
                                        continue;
                                    }

                                    // trim leading and trailing spaces
                                    searchPattern = searchPattern.trim();

                                    // when in searchPattern are not wildcards add %
                                    if (!(searchPattern.contains("%") || searchPattern.contains("_")))
                                        searchPattern = "%" + searchPattern + "%";

                                    searchPattern = searchPattern.replace("\\%", "{^^}");
                                    searchPattern = searchPattern.replace("\\_", "[^^]");

                                    searchPattern = searchPattern.replace("%", "(.*)");
                                    searchPattern = searchPattern.replace("_", "(.)");

                                    searchPattern = searchPattern.replace("{^^}", "\\%");
                                    searchPattern = searchPattern.replace("[^^]", "\\_");

                                    //if (!searchPattern.startsWith("(.*)"))
                                    //    searchPattern = searchPattern + "^";
                                    //if (!searchPattern.endsWith("(.*)"))
                                    //    searchPattern = searchPattern + "$";

                                    positiveList[argsId] = searchPattern;

                                    positiveExists = true;

                                    ++argsId;

                                }
                            }

                            // negative strings
                            boolean negativeExists = false;
                            argsId = 0;
                            for (String split : textSplits) {
                                if (!split.isEmpty()) {
                                    String searchPattern = split;

                                    if (!searchPattern.startsWith("!")) {
                                        // only negative
                                        continue;
                                    }

                                    // remove !
                                    searchPattern = searchPattern.substring(1);

                                    // trim leading and trailing spaces
                                    searchPattern = searchPattern.trim();

                                    // when in searchPattern are not wildcards add %
                                    if (!(searchPattern.contains("%") || searchPattern.contains("_")))
                                        searchPattern = "%" + searchPattern + "%";

                                    searchPattern = searchPattern.replace("\\%", "{^^}");
                                    searchPattern = searchPattern.replace("\\_", "[^^]");

                                    searchPattern = searchPattern.replace("%", "(.*)");
                                    searchPattern = searchPattern.replace("_", "(.)");

                                    searchPattern = searchPattern.replace("{^^}", "\\%");
                                    searchPattern = searchPattern.replace("[^^]", "\\_");

                                    //if (!searchPattern.startsWith("(.*)"))
                                    //    searchPattern = searchPattern + "^";
                                    //if (!searchPattern.endsWith("(.*)"))
                                    //    searchPattern = searchPattern + "$";

                                    negativeList[argsId] = searchPattern;

                                    negativeExists = true;

                                    ++argsId;

                                }
                            }

                            boolean foundPositive = false;
                            if (positiveExists) {
                                for (String _positiveText : positiveList) {
                                    if ((_positiveText != null) && (!_positiveText.isEmpty())) {
                                        if (searchText.toLowerCase().matches(_positiveText.toLowerCase())) {
                                            foundPositive = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            boolean foundNegative = true;
                            if (negativeExists) {
                                for (String _negativeText : negativeList) {
                                    if ((_negativeText != null) && (!_negativeText.isEmpty())) {
                                        if (searchText.toLowerCase().matches(_negativeText.toLowerCase())) {
                                            foundNegative = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            textFound = foundPositive && foundNegative;

                            if (textFound)
                                break;
                        }
                    }
                }

                if (testText) {
                    // is configured test text (_checkContacts or _checkText = true)
                    if (textFound)
                        return statusBarNotification;
                    else
                        return null;
                } else {
                    // is not configured test text (_checkContacts and _checkText = false)
                    return statusBarNotification;
                }
            }
        } catch (Exception e) {
            //Log.e("EventPreferencesNotification.isNotificationActive", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
        // package name not found
        return null;
    }

    private boolean isNotificationVisible(Context context) {
        if (ApplicationPreferences.applicationEventNotificationEnableScanning &&
            PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
            PPNotificationListenerService service = PPNotificationListenerService.getInstance();
            if (service != null) {
                try {
                    StatusBarNotification[] statusBarNotifications = service.getActiveNotifications();
                    //noinspection RedundantLengthCheck
                    if ((statusBarNotifications != null) && (statusBarNotifications.length > 0)) {
                        PPApplicationStatic.logE("[CONTACTS_CACHE] EventPreferencesNotification.isNotificationVisible", "PPApplicationStatic.getContactsCache()");
                        ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                        if (contactsCache == null)
                            return false;
                        List<Contact> contactList;
//                        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesNotification.isNotificationVisible", "PPApplication.contactsCacheMutex");
                        PPApplicationStatic.logE("[CONTACTS_CACHE] EventPreferencesNotification.isNotificationVisible", "contactsCache.getList()");
                        contactList = contactsCache.getList(/*false*/);

                        for (StatusBarNotification statusBarNotification : statusBarNotifications) {

                            // ignore PPP notification
                            if (statusBarNotification.getPackageName().equals(PPApplication.PACKAGE_NAME_PP))
                                continue;
                            if (statusBarNotification.getPackageName().equals(PPApplication.PACKAGE_NAME))
                                continue;
                            if (statusBarNotification.getPackageName().equals(PPApplication.PACKAGE_NAME_PP))
                                continue;
                            if (statusBarNotification.getPackageName().equals(PPApplication.PACKAGE_NAME_EXTENDER))
                                continue;

                            if (this._inCall) {
                                // Nexus/Pixel??? stock ROM
                                StatusBarNotification activeNotification =
                                        isNotificationActive(statusBarNotification,
                                                "com.google.android.dialer", false,
                                                contactList);
                                if (activeNotification != null) {
                                    if (_duration != 0) {
                                        long postTime = activeNotification.getPostTime() + this._duration * 1000L;

                                        if (System.currentTimeMillis() < postTime)
                                            return true;
                                    } else
                                        return true;
                                }
                                // Samsung, MIUI, EMUI, Sony
                                activeNotification = isNotificationActive(statusBarNotification,
                                        "android.incallui", true,
                                        contactList);
                                if (activeNotification != null) {
                                    if (_duration != 0) {
                                        long postTime = activeNotification.getPostTime() + this._duration * 1000L;

                                        if (System.currentTimeMillis() < postTime)
                                            return true;
                                    } else
                                        return true;
                                }
                            }
                            if (this._missedCall) {
                                // Samsung, MIUI, Nexus/Pixel??? stock ROM, Sony
                                StatusBarNotification activeNotification = isNotificationActive(statusBarNotification,
                                        "com.android.server.telecom", false,
                                        contactList);
                                if (activeNotification != null) {
                                    if (_duration != 0) {
                                        long postTime = activeNotification.getPostTime() + this._duration * 1000L;

                                        if (System.currentTimeMillis() < postTime)
                                            return true;
                                    } else
                                        return true;
                                }
                                // MIUI
                                activeNotification = isNotificationActive(statusBarNotification,
                                        "com.google.android.dialer", false,
                                        contactList);
                                if (activeNotification != null) {
                                    if (_duration != 0) {
                                        long postTime = activeNotification.getPostTime() + this._duration * 1000L;

                                        if (System.currentTimeMillis() < postTime)
                                            return true;
                                    } else
                                        return true;
                                }
                                // Samsung One UI
                                activeNotification = isNotificationActive(statusBarNotification,
                                        "com.samsung.android.dialer", false,
                                        contactList);
                                if (activeNotification != null) {
                                    if (_duration != 0) {
                                        long postTime = activeNotification.getPostTime() + this._duration * 1000L;

                                        if (System.currentTimeMillis() < postTime)
                                            return true;
                                    } else
                                        return true;
                                }
                                // LG
                                activeNotification = isNotificationActive(statusBarNotification,
                                        StringConstants.PHONE_PACKAGE_NAME, false,
                                        contactList);
                                if (activeNotification != null) {
                                    if (_duration != 0) {
                                        long postTime = activeNotification.getPostTime() + this._duration * 1000L;

                                        if (System.currentTimeMillis() < postTime)
                                            return true;
                                    } else
                                        return true;
                                }
                                // EMUI
                                activeNotification = isNotificationActive(statusBarNotification,
                                        "com.android.contacts", false,
                                        contactList);
                                if (activeNotification != null) {
                                    if (_duration != 0) {
                                        long postTime = activeNotification.getPostTime() + this._duration * 1000L;

                                        if (System.currentTimeMillis() < postTime)
                                            return true;
                                    } else
                                        return true;
                                }
                            }

                            String[] splits = this._applications.split(StringConstants.STR_SPLIT_REGEX);
                            for (String split : splits) {
                                // get only package name = remove activity
                                String packageName = Application.getPackageName(split);
                                // search for package name in saved package names
                                StatusBarNotification activeNotification = isNotificationActive(statusBarNotification,
                                        packageName, false,
                                        contactList);
                                if (activeNotification != null) {
                                    if (_duration != 0) {
                                        long postTime = activeNotification.getPostTime() + this._duration * 1000L;

                                        if (System.currentTimeMillis() < postTime)
                                            return true;
                                    } else
                                        return true;
                                }
                            }
                        }

                        if (contactList != null)
                            contactList.clear();
                    }
                } catch (Exception e) {
                    //Log.e("EventPreferencesNotification.isNotificationVisible", Log.getStackTraceString(e));

                    // Hm: java.lang.RuntimeException: Could not read bitmap blob.
                    //     in StatusBarNotification[] statusBarNotifications = service.getActiveNotifications();
                    //PPApplicationStatic.recordException(e);
                }
            }
        }

        /*
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

        String[] splits = this._applications.split(StringConstants.STR_SPLIT_REGEX);
        for (String split : splits) {
            // get only package name = remove activity
            String packageName = Application.getPackageName(split);
            // search for package name in saved package names
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted(packageName, false);
            if (notification != null)
                return true;
        }*/

        return false;
    }

    private StatusBarNotification getNewestVisibleNotification(Context context) {
        if (ApplicationPreferences.applicationEventNotificationEnableScanning &&
                PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
            PPNotificationListenerService service = PPNotificationListenerService.getInstance();
            if (service != null) {
                StatusBarNotification[] statusBarNotifications = service.getActiveNotifications();
                StatusBarNotification newestNotification = null;
                StatusBarNotification activeNotification;

                PPApplicationStatic.logE("[CONTACTS_CACHE] EventPreferencesNotification.getNewestVisibleNotification", "PPApplicationStatic.getContactsCache()");
                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache == null)
                    return null;
                List<Contact> contactList;
//                PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesNotification.getNewestVisibleNotification", "PPApplication.contactsCacheMutex");
                PPApplicationStatic.logE("[CONTACTS_CACHE] EventPreferencesNotification.getNewestVisibleNotification", "contactsCache.getList()");
                contactList = contactsCache.getList(/*false*/);

                for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                    if (this._inCall) {
                        // Nexus/Pixel??? stock ROM
                        activeNotification = isNotificationActive(statusBarNotification,
                                "com.google.android.dialer", false,
                                contactList);
                        if (activeNotification != null) {
                            if ((newestNotification == null) || (activeNotification.getPostTime() > newestNotification.getPostTime())) {
                                newestNotification = activeNotification;
                            }
                        }
                        // Samsung, MIUI, EMUI, Sony
                        activeNotification = isNotificationActive(statusBarNotification,
                                "android.incallui", true,
                                contactList);
                        if (activeNotification != null) {
                            if ((newestNotification == null) || (activeNotification.getPostTime() > newestNotification.getPostTime())) {
                                newestNotification = activeNotification;
                            }
                        }
                    }
                    if (this._missedCall) {
                        // Samsung, MIUI, Nexus/Pixel??? stock ROM, Sony
                        activeNotification = isNotificationActive(statusBarNotification,
                                "com.android.server.telecom", false,
                                contactList);
                        if (activeNotification != null) {
                            if ((newestNotification == null) || (activeNotification.getPostTime() > newestNotification.getPostTime())) {
                                newestNotification = activeNotification;
                            }
                        }
                        // Samsung One UI
                        activeNotification = isNotificationActive(statusBarNotification,
                                "com.samsung.android.dialer", false,
                                contactList);
                        if (activeNotification != null) {
                            if ((newestNotification == null) || (activeNotification.getPostTime() > newestNotification.getPostTime())) {
                                newestNotification = activeNotification;
                            }
                        }
                        // LG
                        activeNotification = isNotificationActive(statusBarNotification,
                                StringConstants.PHONE_PACKAGE_NAME, false,
                                contactList);
                        if (activeNotification != null) {
                            if ((newestNotification == null) || (activeNotification.getPostTime() > newestNotification.getPostTime())) {
                                newestNotification = activeNotification;
                            }
                        }
                        // EMUI
                        activeNotification = isNotificationActive(statusBarNotification,
                                "com.android.contacts", false,
                                contactList);
                        if (activeNotification != null) {
                            if ((newestNotification == null) || (activeNotification.getPostTime() > newestNotification.getPostTime())) {
                                newestNotification = activeNotification;
                            }
                        }
                    }

                    String[] splits = this._applications.split(StringConstants.STR_SPLIT_REGEX);
                    for (String split : splits) {
                        // get only package name = remove activity
                        String packageName = Application.getPackageName(split);
                        // search for package name in saved package names
                        activeNotification = isNotificationActive(statusBarNotification,
                                packageName, false,
                                contactList);
                        if (activeNotification != null) {
                            if ((newestNotification == null) || (activeNotification.getPostTime() > newestNotification.getPostTime())) {
                                newestNotification = activeNotification;
                            }
                        }
                    }
                }

                if (contactList != null)
                    contactList.clear();

                return newestNotification;
            }
        }

        return null;
    }

    private boolean isContactConfigured(String text, List<Contact> contactList) {
        boolean phoneNumberFound = false;

        // find phone number in groups
        String[] splits = this._contactGroups.split(StringConstants.STR_SPLIT_REGEX);
        for (String split : splits) {
            if (!split.isEmpty()) {
//                PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesNotification.isContactConfigured", "PPApplication.contactsCacheMutex");
                synchronized (PPApplication.contactsCacheMutex) {
                    if (contactList != null) {
                        for (Contact contact : contactList) {
                            if (contact.groups != null) {
                                long groupId = contact.groups.indexOf(Long.valueOf(split));
                                if (groupId != -1) {
                                    // group found in contact
                                    String _contactName = contact.name;
                                    if (text.toLowerCase().contains(_contactName.toLowerCase())) {
                                        phoneNumberFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (phoneNumberFound)
                break;
        }

        if (!phoneNumberFound) {
            // find phone number in contacts
            // contactId#phoneId|...
            splits = this._contacts.split(StringConstants.STR_SPLIT_REGEX);
            for (String split : splits) {
                String[] splits2 = split.split(StringConstants.STR_SPLIT_CONTACTS_REGEX);

                if ((!split.isEmpty()) &&
                        (splits2.length == 3) &&
                        (!splits2[0].isEmpty()) &&
                        (!splits2[1].isEmpty()) &&
                        (!splits2[2].isEmpty())) {
                    String contactName = splits2[0];
                    if (text.toLowerCase().contains(contactName.toLowerCase())) {
                        // phone number is in sensor configured
                        phoneNumberFound = true;
                        break;
                    }
                }
            }
        }

        return phoneNumberFound;
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {

                boolean scanningPaused = ApplicationPreferences.applicationEventNotificationScanInTimeMultiply.equals("2") &&
                        GlobalUtils.isNowTimeBetweenTimes(
                                ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyFrom,
                                ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyTo);

                if (!scanningPaused) {
                    eventsHandler.notificationPassed = isNotificationVisible(eventsHandler.context);

                    if (!eventsHandler.notAllowedNotification) {
                        if (eventsHandler.notificationPassed)
                            setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                        else
                            setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                    }
                } else
                    eventsHandler.notificationPassed = false;
            } else
                eventsHandler.notAllowedNotification = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_NOTIFICATION);
            }
        }
    }

}
