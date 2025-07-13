package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class EventPreferencesSMS extends EventPreferences {

    //int _smsEvent;
    String _contacts; // contactId#phoneId|...
    String _contactGroups; // groupId|...
    int _contactListType;
    boolean _permanentRun;
    int _duration;
    int _forSIMCard;
    boolean _sendSMS;
    String _smsText;

    long _startTime;
    int _fromSIMSlot;

    static final String PREF_EVENT_SMS_ENABLED = "eventSMSEnabled";
    //static final String PREF_EVENT_SMS_EVENT = "eventSMSEvent";
    static final String PREF_EVENT_SMS_CONTACTS = "eventSMSContacts";
    static final String PREF_EVENT_SMS_CONTACT_GROUPS = "eventSMSContactGroups";
    private static final String PREF_EVENT_SMS_CONTACT_LIST_TYPE = "eventSMSContactListType";
    private static final String PREF_EVENT_SMS_PERMANENT_RUN = "eventSMSPermanentRun";
    private static final String PREF_EVENT_SMS_DURATION = "eventSMSDuration";
    static final String PREF_EVENT_SMS_EXTENDER = "eventSMSExtender";
    //static final String PREF_EVENT_SMS_INSTALL_EXTENDER = "eventSMSInstallExtender";
    //static final String PREF_EVENT_SMS_ACCESSIBILITY_SETTINGS = "eventSMSAccessibilitySettings";
    //static final String PREF_EVENT_SMS_LAUNCH_EXTENDER = "eventSMSLaunchExtender";
    private static final String PREF_EVENT_SMS_FOR_SIM_CARD = "eventSMSForSimCard";
    static final String PREF_EVENT_SMS_SEND_SMS = "eventSMSSendSMS";
    static final String PREF_EVENT_SMS_SMS_TEXT = "eventSMSSMSText";

    static final String PREF_EVENT_SMS_ENABLED_NO_CHECK_SIM = "eventSMSEnabledEnabledNoCheckSim";

    static final String PREF_EVENT_SMS_CATEGORY = "eventSMSCategoryRoot";

    //static final int SMS_EVENT_UNDEFINED = -1;
    //static final int SMS_EVENT_INCOMING = 0;
    //static final int SMS_EVENT_OUTGOING = 1;

    static final int CONTACT_LIST_TYPE_WHITE_LIST = 0;
    //static final int CONTACT_LIST_TYPE_BLACK_LIST = 1;
    private static final int CONTACT_LIST_TYPE_NOT_USE = 2;

    EventPreferencesSMS(Event event,
                                    boolean enabled,
                                    //int smsEvent,
                                    String contacts,
                                    String contactGroups,
                                    int contactListType,
                                    boolean permanentRun,
                                    int duration,
                                    int forSIMCard,
                                    boolean sendSMS,
                                    String smsText)
    {
        super(event, enabled);

        //this._smsEvent = smsEvent;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._contactListType = contactListType;
        this._permanentRun = permanentRun;
        this._duration = duration;
        this._forSIMCard = forSIMCard;
        this._sendSMS = sendSMS;
        this._smsText = smsText;

        this._startTime = 0;
        this._fromSIMSlot = 0;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesSMS._enabled;
        //this._smsEvent = fromEvent._eventPreferencesSMS._smsEvent;
        this._contacts = fromEvent._eventPreferencesSMS._contacts;
        this._contactGroups = fromEvent._eventPreferencesSMS._contactGroups;
        this._contactListType = fromEvent._eventPreferencesSMS._contactListType;
        this._permanentRun = fromEvent._eventPreferencesSMS._permanentRun;
        this._duration = fromEvent._eventPreferencesSMS._duration;
        this._forSIMCard = fromEvent._eventPreferencesSMS._forSIMCard;
        this._sendSMS = fromEvent._eventPreferencesSMS._sendSMS;
        this._smsText = fromEvent._eventPreferencesSMS._smsText;
        this.setSensorPassed(fromEvent._eventPreferencesSMS.getSensorPassed());

        this._startTime = 0;
        this._fromSIMSlot = 0;

    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SMS_ENABLED, _enabled);
        //editor.putString(PREF_EVENT_SMS_EVENT, String.valueOf(this._smsEvent));
        editor.putString(PREF_EVENT_SMS_CONTACTS, this._contacts);
        editor.putString(PREF_EVENT_SMS_CONTACT_GROUPS, this._contactGroups);
        editor.putString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
        editor.putBoolean(PREF_EVENT_SMS_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_SMS_DURATION, String.valueOf(this._duration));
        editor.putString(PREF_EVENT_SMS_FOR_SIM_CARD, String.valueOf(this._forSIMCard));
        editor.putBoolean(PREF_EVENT_SMS_SEND_SMS, this._sendSMS);
        editor.putString(PREF_EVENT_SMS_SMS_TEXT, this._smsText);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
        //this._smsEvent = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_EVENT, "0"));
        this._contacts = preferences.getString(PREF_EVENT_SMS_CONTACTS, "");
        this._contactGroups = preferences.getString(PREF_EVENT_SMS_CONTACT_GROUPS, "");
        this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, "0"));
        this._permanentRun = preferences.getBoolean(PREF_EVENT_SMS_PERMANENT_RUN, false);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_DURATION, "5"));
        this._forSIMCard = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_FOR_SIM_CARD, "0"));
        this._sendSMS = preferences.getBoolean(PREF_EVENT_SMS_SEND_SMS, false);
        this._smsText = preferences.getString(PREF_EVENT_SMS_SMS_TEXT, "");
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_sms_summary));
        } else {
            if (addBullet) {
                _value.append(StringConstants.TAG_BOLD_START_HTML);
                _value.append(getPassStatusString(context.getString(R.string.event_type_sms), addPassStatus, DatabaseHandler.ETYPE_SMS, context));
                _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
            }

            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_SMS_ENABLED, false, context);
            if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.preference_not_allowed_reason_not_extender_installed));
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded));
                } else if (!PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context.getApplicationContext(), false, true
                        /*, "EventPreferencesSMS.getPreferencesDescription"*/)) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender));
                } if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined));
                } else {
                    //descr = descr + context.getString(R.string.pref_event_sms_event);
                    //String[] smsEvents = context.getResources().getStringArray(R.array.eventSMSEventsArray);
                    //descr = descr + ": " + smsEvents[tmp._smsEvent] + "; ";

                    _value.append(context.getString(R.string.event_preferences_sms_contact_groups)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactGroupsMultiSelectDialogPreference.getSummary(_contactGroups, context), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_sms_contacts)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactsMultiSelectDialogPreference.getSummary(_contacts, false, context), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.pref_event_sms_contactListType));
                    String[] contactListTypes = context.getResources().getStringArray(R.array.eventSMSContactListTypeArray);
                    _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(contactListTypes[this._contactListType], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);

                        boolean hasSIMCard = false;
                            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null) {
                                int phoneCount = telephonyManager.getPhoneCount();
                                if (phoneCount > 1) {
                                    //boolean simExists;
//                                    Log.e("EventPreferencesSMS.getPreferencesDescription", "called hasSIMCard");
                                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                                    hasSIMCard = hasSIMCardData.simCount > 0;
                                    //boolean sim1Exists = hasSIMCardData.hasSIM1;
                                    //boolean sim2Exists = hasSIMCardData.hasSIM2;

                                    //simExists = sim1Exists;
                                    //simExists = simExists && sim2Exists;
                                    //hasSIMCard = simExists;
                                }
                            }
                        if (hasSIMCard) {
                            _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_sms_forSimCard));
                            String[] forSimCard = context.getResources().getStringArray(R.array.eventSMSForSimCardArray);
                            _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(forSimCard[this._forSIMCard], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        }

                    if (this._permanentRun)
                        _value.append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.pref_event_permanentRun), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.pref_event_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._duration), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);

                    if (Build.VERSION.SDK_INT >= 29) {
                        if (this._sendSMS)
                            _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preference_smsSendSMS));
                    }
                }
            }
            else {
                _value.append(context.getString(R.string.profile_preferences_device_not_allowed)).append(StringConstants.STR_COLON_WITH_SPACE).append(preferenceAllowed.getNotAllowedPreferenceReasonString(context));
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_SMS_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (/*key.equals(PREF_EVENT_SMS_EVENT) ||*/ key.equals(PREF_EVENT_SMS_CONTACT_LIST_TYPE))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_SMS_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals(StringConstants.FALSE_STRING));
            }
        }
        if (key.equals(PREF_EVENT_SMS_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false, false);
        }
        if (key.equals(PREF_EVENT_SMS_SMS_TEXT)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
            }
        }

        boolean hasFeature = false;
        boolean hasSIMCard = false;
            if (key.equals(PREF_EVENT_SMS_FOR_SIM_CARD)) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        hasFeature = true;
                        //boolean simExists;
//                        Log.e("EventPreferencesSMS.setSummary", "called hasSIMCard");
                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        hasSIMCard = hasSIMCardData.simCount > 1;
                        //boolean sim1Exists = hasSIMCardData.hasSIM1;
                        //boolean sim2Exists = hasSIMCardData.hasSIM2;

                        //simExists = sim1Exists;
                        //simExists = simExists && sim2Exists;
                        //hasSIMCard = simExists;
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(value);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);
                        }
                    }
                }
                if (!hasFeature) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
                else if (!hasSIMCard) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
            }
        /*
        if (key.equals(PREF_EVENT_SMS_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_not_installed_summary);// +
                            //"\n\n" + context.getString(R.string.event_preferences_sms_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + context.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + context.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        */

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesSMS.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesSMS.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesSMS.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_SMS_CONTACT_GROUPS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_SMS_CONTACT_GROUPS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_SMS_CONTACTS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_SMS_CONTACTS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_SMS_CONTACT_LIST_TYPE);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !isRunnable, false);
        preference = prefMng.findPreference(PREF_EVENT_SMS_SEND_SMS);
        if (preference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_SMS_SEND_SMS, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, false, false, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_SMS_SMS_TEXT);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_SMS_SMS_TEXT, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, false, false, false);
        }

        int _isAccessibilityEnabled = event._eventPreferencesSMS.isAccessibilityServiceEnabled(context, false);
        boolean isAccessibilityEnabled = _isAccessibilityEnabled == 1;

        ExtenderDialogPreference extenderPreference = prefMng.findPreference(PREF_EVENT_SMS_EXTENDER);
        if (extenderPreference != null) {
            extenderPreference.setSummaryEDP();
            GlobalGUIRoutines.setPreferenceTitleStyleX(extenderPreference, enabled, false, false, true,
                    !(isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1)), true);
        }
        /*
        preference = prefMng.findPreference(PREF_EVENT_SMS_ACCESSIBILITY_SETTINGS);
        if (preference != null) {

            String summary;
            if (isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                summary = context.getString(R.string.accessibility_service_enabled);
            else {
                if (_isAccessibilityEnabled == -1) {
                    summary = context.getString(R.string.accessibility_service_not_used);
                    summary = summary + "\n\n" + context.getString(R.string.preference_not_used_extender_reason) + " " +
                            context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else {
                    summary = context.getString(R.string.accessibility_service_disabled);
                    summary = summary + "\n\n" + context.getString(R.string.event_preferences_sms_AccessibilitySettingsForExtender_summary);
                }
            }
            preference.setSummary(summary);

            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true,
                    !(isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1)), true);
        }
        */
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_SMS_ENABLED) ||
            key.equals(PREF_EVENT_SMS_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (/*key.equals(PREF_EVENT_SMS_EVENT) ||*/
            key.equals(PREF_EVENT_SMS_CONTACT_LIST_TYPE) ||
            key.equals(PREF_EVENT_SMS_CONTACTS) ||
            key.equals(PREF_EVENT_SMS_CONTACT_GROUPS) ||
            key.equals(PREF_EVENT_SMS_DURATION) ||
            key.equals(PREF_EVENT_SMS_EXTENDER) ||
            //key.equals(PREF_EVENT_SMS_INSTALL_EXTENDER) ||
            key.equals(PREF_EVENT_SMS_FOR_SIM_CARD) ||
            key.equals(PREF_EVENT_SMS_SMS_TEXT))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_SMS_SEND_SMS)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_SMS_ENABLED, preferences, context);
        //setSummary(prefMng, PREF_EVENT_SMS_EVENT, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACT_LIST_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_EXTENDER, preferences, context);
        //setSummary(prefMng, PREF_EVENT_SMS_INSTALL_EXTENDER, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_FOR_SIM_CARD, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_SEND_SMS, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_SMS_TEXT, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_SMS_ENABLED_NO_CHECK_SIM, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesSMS tmp = new EventPreferencesSMS(this._event, this._enabled, this._contacts, this._contactGroups, this._contactListType,
                                                                this._permanentRun, this._duration, this._forSIMCard, this._sendSMS, this._smsText);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && tmp.isAllConfigured(context) &&
                        (tmp.isAccessibilityServiceEnabled(context, false) == 1) &&
                        (PPApplication.accessibilityServiceForPPPExtenderConnected == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_SMS).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted), true);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_CATEGORY);
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

        runnable = runnable && ((_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||
                              (!(_contacts.isEmpty() && _contactGroups.isEmpty())));

        return runnable;
    }

    @Override
    int isAccessibilityServiceEnabled(Context context, boolean againCheckInDelay)
    {
        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
        if (extenderVersion == 0)
            return -2;
        if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED)
            return -1;
        if ((_event.getStatus() != Event.ESTATUS_STOP) && this._enabled &&
                isRunnable(context) && isAllConfigured(context)) {
            if (PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, againCheckInDelay, true
                        /*, "EventPreferencesSMS.isAccessibilityServiceEnabled"*/))
                return 1;
        } else
            return 1;
        return 0;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_SMS_ENABLED) != null)
            {
                final boolean accessibilityEnabled =
                        PPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_REQUIRED, true, false
                                /*, "EventPreferencesSMS.checkPreferences"*/);

                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
                Preference preference = prefMng.findPreference(PREF_EVENT_SMS_EXTENDER);
                if (preference != null)
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !accessibilityEnabled, true);

//                preference = prefMng.findPreference(PREF_EVENT_SMS_ACCESSIBILITY_SETTINGS);
//                if (preference != null)
//                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !accessibilityEnabled, true);

                boolean showPreferences = false;
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        boolean sim1Exists;
                        boolean sim2Exists;
//                        Log.e("EventPreferencesSMS.checkPreferences", "called hasSIMCard");
                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        sim1Exists = hasSIMCardData.hasSIM1;
                        sim2Exists = hasSIMCardData.hasSIM2;

                        showPreferences = true;
                        preference = prefMng.findPreference("eventSMSDualSIMInfo");
                        if (preference != null)
                            preference.setEnabled(enabled && sim1Exists && sim2Exists);
                        preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                        if (preference != null)
                            preference.setEnabled(enabled && sim1Exists && sim2Exists);
                    } else {
                        preference = prefMng.findPreference("eventSMSDualSIMInfo");
                        if (preference != null)
                            preference.setEnabled(false);
                        preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                        if (preference != null)
                            preference.setEnabled(false);
                    }
                }
                if (!showPreferences) {
                    preference = prefMng.findPreference("eventSMSDualSIMInfo");
                    if (preference != null)
                        preference.setVisible(false);
                    preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                    if (preference != null)
                        preference.setVisible(false);
                }
                if (preferences != null) {
                    int contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, "0"));
                    preference = prefMng.findPreference(PREF_EVENT_SMS_SEND_SMS);
                    if (preference != null) {
                        if (Build.VERSION.SDK_INT >= 29) {
                            boolean contactsConfigured = !prefMng.getSharedPreferences().getString(PREF_EVENT_SMS_CONTACTS, "").isEmpty();
                            boolean contactGroupsConfigured = !prefMng.getSharedPreferences().getString(PREF_EVENT_SMS_CONTACT_GROUPS, "").isEmpty();
                            boolean sendSMSEnabled =
                                    ((contactsConfigured || contactGroupsConfigured) &&
                                            (contactListType == CONTACT_LIST_TYPE_WHITE_LIST));
                            preference.setEnabled(sendSMSEnabled);

                            boolean sendSMS = preferences.getBoolean(PREF_EVENT_SMS_SEND_SMS, false);
                            preference = prefMng.findPreference(PREF_EVENT_SMS_SMS_TEXT);
                            if (preference != null)
                                preference.setEnabled(sendSMS && (contactListType == CONTACT_LIST_TYPE_WHITE_LIST)
                                        && sendSMSEnabled);
                        }
                    }
                }
                setSummary(prefMng, PREF_EVENT_SMS_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeAlarm()
    {
        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 1000L));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
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

        setAlarm(computeAlarm(), context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);
    }

    void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, SMSEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_SMS_EVENT_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            if (_startTime > 0) {
                //Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, SMSEventEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo;
                        if (_duration * 1000L >= Event.EVENT_ALARM_TIME_SOFT_OFFSET)
                            clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        else
                            clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                    else {
                        if (_duration * 1000L >= Event.EVENT_ALARM_TIME_OFFSET)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                        else
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    }
                }
            }
        }
    }

    void saveStartTime(List<Contact> contactList, DataWrapper dataWrapper, String phoneNumber, long startTime, int fromSIMSlot) {
        if (this._startTime == 0) {
            // alarm for end is not set

            if (Permissions.checkContacts(dataWrapper.context)) {
                boolean phoneNumberFound = false;

                if (this._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {
                    // find phone number in groups
                    String[] splits = this._contactGroups.split(StringConstants.STR_SPLIT_REGEX);
                    for (String split : splits) {
                        if (!split.isEmpty()) {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesSMS.saveStartTime", "(2) PPApplication.contactsCacheMutex");
                            synchronized (PPApplication.contactsCacheMutex) {
                                if (contactList != null) {
                                    for (Contact contact : contactList) {
                                        if (contact.groups != null) {
                                            long groupId = contact.groups.indexOf(Long.valueOf(split));
                                            if (groupId != -1) {
                                                // group found in contact
                                                if (contact.phoneId != 0) {
                                                    String _phoneNumber = contact.phoneNumber;
                                                    if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                        phoneNumberFound = true;
                                                        break;
                                                    }
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
                                String contactPhoneNumber = splits2[1];
                                if (PhoneNumberUtils.compare(contactPhoneNumber, phoneNumber)) {
                                    // phone number is in sensor configured
                                    phoneNumberFound = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (this._contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                        phoneNumberFound = !phoneNumberFound;

                } else
                    phoneNumberFound = true;

                if (phoneNumberFound) {
                    this._startTime = startTime;// + (10 * 1000);
                    this._fromSIMSlot = fromSIMSlot;
                }
                else {
                    this._startTime = 0;
                    this._fromSIMSlot = 0;
                }

                DatabaseHandler.getInstance(dataWrapper.context).updateSMSStartTime(_event);

                if (phoneNumberFound) {
                    //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                        setSystemEventForPause(dataWrapper.context);
                }
            } else {
                this._startTime = 0;
                this._fromSIMSlot = 0;
                DatabaseHandler.getInstance(dataWrapper.context).updateSMSStartTime(_event);
            }
        }
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventSMSContacts(context, event, null)*/
                /* moved to Extender && Permissions.checkEventSMSBroadcast(context, event, null)*/) {
                // compute start time

                if (_startTime > 0) {
                    if ((_forSIMCard == 0) || (_forSIMCard == _fromSIMSlot)) {

                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        long startTime = _startTime - gmtOffset;

                        // compute end datetime
                        long endAlarmTime = computeAlarm();

                        Calendar now = Calendar.getInstance();
                        long nowAlarmTime = now.getTimeInMillis();

                        if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_SMS))
                            eventsHandler.smsPassed = true;
                        else if (!_permanentRun) {
                            if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_SMS_EVENT_END))
                                eventsHandler.smsPassed = false;
                            else
                                eventsHandler.smsPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        } else {
                            eventsHandler.smsPassed = nowAlarmTime >= startTime;
                        }
                    }
                    else
                        eventsHandler.smsPassed = false;
                } else {
                    eventsHandler.smsPassed = false;
                }

                if (!eventsHandler.smsPassed) {
                    _startTime = 0;
                    _fromSIMSlot = 0;
                    DatabaseHandler.getInstance(eventsHandler.context).updateSMSStartTime(_event);
                }

                if (!eventsHandler.notAllowedSms) {
                    if (eventsHandler.smsPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedSms = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_SMS);
            }
        }
    }

}
