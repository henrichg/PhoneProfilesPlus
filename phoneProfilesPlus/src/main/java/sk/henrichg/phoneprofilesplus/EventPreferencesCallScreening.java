package sk.henrichg.phoneprofilesplus;

import static android.app.role.RoleManager.ROLE_CALL_SCREENING;
import static android.content.Context.ROLE_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.telephony.PhoneNumberUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class EventPreferencesCallScreening extends EventPreferences {

    int _callDirection;
    String _contacts; // contactId#phoneId|...
    String _contactGroups; // groupId|...
    //int _contactListType;  // !!! enabled are ony configured contacts
    boolean _blockCalls;
    boolean _sendSMS;
    String _smsText;
    boolean _permanentRun;
    int _duration;

    long _startTime;

    static final String PREF_EVENT_CALL_SCREENING_ENABLED = "eventCallScreeningEnabled";
    private static final String PREF_EVENT_CALL_SCREENING_CALL_DIRECTION = "eventCallScreeningCallDireciton";
    static final String PREF_EVENT_CALL_SCREENING_CONTACTS = "eventCallScreeningContacts";
    static final String PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS = "eventCallScreeningContactGroups";
    //private static final String PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE = "eventCallScreeningContactListType";
    static final String PREF_EVENT_CALL_SCREENING_BLOCK_CALLS = "eventCallScreeningBlockCalls";
    static final String PREF_EVENT_CALL_SCREENING_SEND_SMS = "eventCallScreeningSendSMS";
    static final String PREF_EVENT_CALL_SCREENING_SMS_TEXT = "eventCallScreeningSMSText";
    static final String PREF_EVENT_CALL_SCREENING_PERMANENT_RUN = "eventCallScreeningPermanentRun";
    private static final String PREF_EVENT_CALL_SCREENING_DURATION = "eventCallScreeningDuration";

    static final String PREF_EVENT_CALL_SCREENING_SET_CALL_SCREENING_ROLE = "eventCallScreeningSetCallScreeningRole";

    static final String PREF_EVENT_CALL_SCREENING_CATEGORY = "eventCallScreeningCategoryRoot";

    //private static final String PREF_EVENT_CALL_SCREENING_ACTIVE = "eventCallScreeningActive";
    private static final String PREF_EVENT_CALL_SCREENING_TIME = "eventCallScreeningTime";
    private static final String PREF_EVENT_CALL_SCREENING_PHONE_NUMBER = "eventCallScreeningPhoneNumber";
    //private static final String PREF_EVENT_CALL_SCREENING_CALL_DIRECTION = "eventCallScreeningCallDirection";

    //static final int CONTACT_LIST_TYPE_WHITE_LIST = 0;
    //static final int CONTACT_LIST_TYPE_BLACK_LIST = 1;
    //static final int CONTACT_LIST_TYPE_NOT_USE = 2;

    static final int CALL_DIRECTION_INCOMING = 0;
    //static final int CALL_DIRECTION_OUTGOING = 1;
    static final int CALL_DIRECTION_ALL = 2;

    EventPreferencesCallScreening(Event event,
                                  boolean enabled,
                                  int callDirection,
                                  String contacts,
                                  String contactGroups,
                                  //int contactListType,
                                  boolean blockCalls,
                                  boolean sendSMS,
                                  String smsText,
                                  boolean permanetRun,
                                  int duration) {
        super(event, enabled);

        this._callDirection = callDirection;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        //this._contactListType = contactListType;
        this._blockCalls = blockCalls;
        this._sendSMS = sendSMS;
        this._smsText = smsText;
        this._permanentRun = permanetRun;
        this._duration = duration;

        this._startTime = 0;
    }

    void copyPreferences(Event fromEvent) {
        this._enabled = fromEvent._eventPreferencesCallScreening._enabled;
        this._callDirection = fromEvent._eventPreferencesCallScreening._callDirection;
        this._contacts = fromEvent._eventPreferencesCallScreening._contacts;
        this._contactGroups = fromEvent._eventPreferencesCallScreening._contactGroups;
        //this._contactListType = fromEvent._eventPreferencesCallScreening._contactListType;
        this._blockCalls = fromEvent._eventPreferencesCallScreening._blockCalls;
        this._sendSMS = fromEvent._eventPreferencesCallScreening._sendSMS;
        this._smsText = fromEvent._eventPreferencesCallScreening._smsText;
        this._permanentRun = fromEvent._eventPreferencesCallScreening._permanentRun;
        this._duration = fromEvent._eventPreferencesCallScreening._duration;
        this.setSensorPassed(fromEvent._eventPreferencesCallScreening.getSensorPassed());

        this._startTime = 0;
    }

    void loadSharedPreferences(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_CALL_SCREENING_ENABLED, _enabled);
        editor.putString(PREF_EVENT_CALL_SCREENING_CALL_DIRECTION, String.valueOf(this._callDirection));
        editor.putString(PREF_EVENT_CALL_SCREENING_CONTACTS, this._contacts);
        editor.putString(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS, this._contactGroups);
        //editor.putString(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
        editor.putBoolean(PREF_EVENT_CALL_SCREENING_BLOCK_CALLS, this._blockCalls);
        editor.putBoolean(PREF_EVENT_CALL_SCREENING_SEND_SMS, this._sendSMS);
        editor.putString(PREF_EVENT_CALL_SCREENING_SMS_TEXT, this._smsText);
        editor.putBoolean(PREF_EVENT_CALL_SCREENING_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_CALL_SCREENING_DURATION, String.valueOf(this._duration));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences) {
        this._enabled = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_ENABLED, false);
        this._callDirection = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_SCREENING_CALL_DIRECTION, String.valueOf(CALL_DIRECTION_INCOMING)));
        this._contacts = preferences.getString(PREF_EVENT_CALL_SCREENING_CONTACTS, "");
        this._contactGroups = preferences.getString(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS, "");
        //this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE, "0"));
        this._blockCalls = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_BLOCK_CALLS, false);
        this._sendSMS = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_SEND_SMS, false);
        this._smsText = preferences.getString(PREF_EVENT_CALL_SCREENING_SMS_TEXT, "");
        this._permanentRun = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_PERMANENT_RUN, false);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_SCREENING_DURATION, "5"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_call_screening_summary));
        } else {
            if (addBullet) {
                _value.append(StringConstants.TAG_BOLD_START_HTML);
                _value.append(getPassStatusString(context.getString(R.string.event_type_call_screening), addPassStatus, DatabaseHandler.ETYPE_CALL_SCREENING, context));
                _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
            }

            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_CALL_SCREENING_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean isHeld = isIsCallScreeningHeld(context);
                if (!isHeld) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.event_preference_callScreening_not_held_call_screening_role));
                } else {
                    _value.append(context.getString(R.string.event_preferences_call_screening_call_direction));
                    String[] callEvents = context.getResources().getStringArray(R.array.eventCallScreeningCallDirecitonArray);
                    _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(callEvents[this._callDirection], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_call_contact_groups)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactGroupsMultiSelectDialogPreference.getSummary(_contactGroups, context), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_call_contacts)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactsMultiSelectDialogPreference.getSummary(_contacts, false, context), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                    //_value.append(context.getString(R.string.event_preferences_contactListType));
                    //String[] contactListTypes = context.getResources().getStringArray(R.array.eventCallContactListTypeArray);
                    //_value.append(StringConstants.STR_BULLET).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(contactListTypes[this._contactListType], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                    if (_blockCalls) {
                        _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preference_callScreeningBlockCalls));
                        if (_sendSMS) {
                            _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preference_callScreeningSendSMS));
                        }
                    }

                    if (this._permanentRun)
                        _value.append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.pref_event_permanentRun), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.pref_event_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._duration), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
            else {
                _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                        .append(StringConstants.STR_COLON_WITH_SPACE).append(preferenceAllowed.getNotAllowedPreferenceReasonString(context));
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_CALL_SCREENING_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals((PREF_EVENT_CALL_SCREENING_CALL_DIRECTION)) /*||
                key.equals(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE)*/) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        if (key.equals(PREF_EVENT_CALL_SCREENING_SMS_TEXT)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
            }
        }

        if (key.equals(PREF_EVENT_CALL_SCREENING_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false, false);
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesCallScreening.saveSharedPreferences(prefMng.getSharedPreferences());
        //boolean isRunnable = event._eventPreferencesCallScreening.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesCall.isAllConfigured(context);
        boolean isAllConfigured = (/*(_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||*/
                                    (!(_contacts.isEmpty() && _contactGroups.isEmpty())));
        boolean roleHeld = isIsCallScreeningHeld(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_ENABLED, false) &&
                    roleHeld;
        Preference preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_SET_CALL_SCREENING_ROLE);
        if (preference != null) {
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, true, !roleHeld, true);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isAllConfigured, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CONTACTS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALL_SCREENING_CONTACTS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isAllConfigured, false);
        }
        //preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE);
        //if (preference != null)
        //    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !isAllConfigured, false);

        preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_BLOCK_CALLS);
        if (preference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_CALL_SCREENING_BLOCK_CALLS, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, false, false, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_SEND_SMS);
        if (preference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_CALL_SCREENING_SEND_SMS, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, false, false, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_SMS_TEXT);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALL_SCREENING_SMS_TEXT, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, false, false, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_CALL_SCREENING_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (/*key.equals(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE) ||
                key.equals(PREF_EVENT_CALL_SCREENING_CONTACTS) ||
                key.equals(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS) ||*/
                key.equals(PREF_EVENT_CALL_SCREENING_CALL_DIRECTION) ||
                key.equals(PREF_EVENT_CALL_SCREENING_SMS_TEXT)) {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_CALL_SCREENING_BLOCK_CALLS) ||
                key.equals(PREF_EVENT_CALL_SCREENING_SEND_SMS)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }

        if (key.equals(PREF_EVENT_CALL_SCREENING_SET_CALL_SCREENING_ROLE)) {
            if (Build.VERSION.SDK_INT >= 29) {
                String summary = context.getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary);
                if (isIsCallScreeningHeld(context)) {
                    summary = context.getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary_ststus_1) +
                            StringConstants.STR_DOUBLE_NEWLINE + summary;
                } else {
                    summary = context.getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary_ststus_0) +
                            StringConstants.STR_DOUBLE_NEWLINE + summary;
                }
                preference.setSummary(summary);
            }
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_CALL_DIRECTION, preferences, context);
        //setSummary(prefMng, PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_SET_CALL_SCREENING_ROLE, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_BLOCK_CALLS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_SEND_SMS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_SMS_TEXT, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        EventPreferencesCallScreening tmp = new EventPreferencesCallScreening(this._event, this._enabled, this._callDirection, this._contacts,
                this._contactGroups,/*this._contactListType,*/ this._blockCalls, this._sendSMS, this._smsText, this._permanentRun, this._duration);
        if (preferences != null)
            tmp.saveSharedPreferences(preferences);

        Preference preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CATEGORY);
        if (preference != null) {
            boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
            boolean runnable = tmp.isRunnable(context) && tmp.isAllConfigured(context) &&
                                    tmp.isIsCallScreeningHeld(context);
            boolean permissionGranted = true;
            if (enabled)
                permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_CALL_SCREENING).isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted), true);
            if (enabled)
                preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
            else
                preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
        }
    }

    @Override
    boolean isRunnable(Context context) {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && isIsCallScreeningHeld(context) &&
                (/*(_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||*/
                (!(_contacts.isEmpty() && _contactGroups.isEmpty())));

        return runnable;
    }

    boolean isIsCallScreeningHeld(Context context)
    {
        boolean isHeld = false;
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager roleManager = (RoleManager) context.getSystemService(ROLE_SERVICE);
            isHeld = roleManager.isRoleHeld(ROLE_CALL_SCREENING);
        }
        return isHeld;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_CALL_SCREENING_ENABLED) != null)
            {
                boolean isRoleHeld = isIsCallScreeningHeld(context);
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_CALL_SCREENING_ENABLED, false);

                if (enabled) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CONTACTS);
                    if (preference != null)
                        preference.setEnabled(isRoleHeld);
                    preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS);
                    if (preference != null)
                        preference.setEnabled(isRoleHeld);
                    //preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE);
                    //if (preference != null)
                    //    preference.setEnabled(isRoleHeld);
                    preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_BLOCK_CALLS);
                    if (preference != null)
                        preference.setEnabled(isRoleHeld);
                    preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_SEND_SMS);
                    if (preference != null)
                        preference.setEnabled(isRoleHeld);

                    boolean sendSMS = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_SEND_SMS, false);
                    preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_SMS_TEXT);
                    if (preference != null)
                        preference.setEnabled(isRoleHeld && sendSMS);
                }

                setSummary(prefMng, PREF_EVENT_CALL_SCREENING_ENABLED, preferences, context);
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
    void setSystemEventForStart(Context context) {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that will change state into RUNNING;
        // from broadcast will by called EventsHandler

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context) {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that will change state into PAUSE;
        // from broadcast will by called EventsHandler

        removeAlarm(context);

        //Log.e("EventPreferencesCallScreening.setSystemEventForPause", "xxxxxxxxx (1)");

        if (!(isRunnable(context) && isAllConfigured(context) && _enabled))
            return;

        //Log.e("EventPreferencesCallScreening.setSystemEventForPause", "xxxxxxxxx (2)");

        setAlarm(computeAlarm(), context);
    }

    @Override
    void removeSystemEvent(Context context) {
        removeAlarm(context);
    }

    boolean isPhoneNumberConfigured(List<Contact> contactList, String phoneNumber/*, DataWrapper dataWrapper*/) {
        boolean phoneNumberFound = false;

        //if (this._contactListType != CONTACT_LIST_TYPE_NOT_USE) {
            /*ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
            if (contactsCache == null)
                return false;
            List<Contact> contactList;
//            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.isPhoneNumberConfigured", "(1) PPApplication.contactsCacheMutex");
            synchronized (PPApplication.contactsCacheMutex) {
                contactList = contactsCache.getList(); //false
            }*/

            // find phone number in groups
            String[] splits = this._contactGroups.split(StringConstants.STR_SPLIT_REGEX);
            for (String split : splits) {
                if (!split.isEmpty()) {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.isPhoneNumberConfigured", "(2) PPApplication.contactsCacheMutex");
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

            //if (this._contactListType == CONTACT_LIST_TYPE_BLACK_LIST)
            //    phoneNumberFound = !phoneNumberFound;
        //} else
        //    phoneNumberFound = true;

        return phoneNumberFound;
    }

    /*
    static void getEventCallScreeningActive(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCallScreening.getEventCallScreeningActive", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallScreeningActive = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(EventPreferencesCallScreening.PREF_EVENT_CALL_SCREENING_ACTIVE, false);
        }
    }
    static void setEventCallScreeningActive(Context context, boolean active) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCallScreening.setEventCallScreeningActive", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(EventPreferencesCallScreening.PREF_EVENT_CALL_SCREENING_ACTIVE, active);
            editor.apply();
            ApplicationPreferences.prefEventCallScreeningActive = active;
        }
    }
    */
    static void getEventCallScreeningTime(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.getEventCallEventTime", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallScreeningTime = ApplicationPreferences.
                    getSharedPreferences(context).getLong(PREF_EVENT_CALL_SCREENING_TIME, 0);
        }
    }
    static void setEventCallScreeningTime(Context context, long time) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.setEventCallEventTime", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(PREF_EVENT_CALL_SCREENING_TIME, time);
            ApplicationPreferences.prefEventCallScreeningTime = time;
            editor.apply();
        }
    }
    static void getEventCallScreeningPhoneNumber(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCallScreening.getEventCallScreeningPhoneNumber", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallScreeningPhoneNumber = ApplicationPreferences.
                    getSharedPreferences(context).getString(PREF_EVENT_CALL_SCREENING_PHONE_NUMBER, "");
        }
    }
    static void setEventCallScreeningPhoneNumber(Context context, String phoneNumber) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCallScreening.setEventCallScreeningPhoneNumber", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_EVENT_CALL_SCREENING_PHONE_NUMBER, phoneNumber);
            editor.apply();
            ApplicationPreferences.prefEventCallScreeningPhoneNumber = phoneNumber;
        }
    }
    static void getEventCallScreeningCallDirection(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.getEventCallEventTime", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallScreeningCallDirection = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_EVENT_CALL_SCREENING_CALL_DIRECTION, CALL_DIRECTION_INCOMING);
        }
    }
    static void setEventCallScreeningCallDirection(Context context, int direction) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.setEventCallEventTime", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(PREF_EVENT_CALL_SCREENING_CALL_DIRECTION, direction);
            ApplicationPreferences.prefEventCallScreeningCallDirection = direction;
            editor.apply();
        }
    }


    void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_CALL_SCREENING_EVENT_END_BROADCAST_RECEIVER);
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
                intent.setAction(PhoneProfilesService.ACTION_CALL_SCREENING_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, SMSEventEndBroadcastReceiver.class);

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
    }

    void saveStartTime(List<Contact> contactList, DataWrapper dataWrapper, String phoneNumber, long startTime) {
        if (this._startTime == 0) {
            // alarm for end is not set

            if (Permissions.checkContacts(dataWrapper.context)) {
                boolean phoneNumberFound = false;

                //if (this._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {
                    // find phone number in groups
                    String[] splits = this._contactGroups.split(StringConstants.STR_SPLIT_REGEX);
                    for (String split : splits) {
                        if (!split.isEmpty()) {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCallScreening.saveStartTime", "(2) PPApplication.contactsCacheMutex");
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

                    //if (this._contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                    //    phoneNumberFound = !phoneNumberFound;

                //} else
                //    phoneNumberFound = true;

                if (phoneNumberFound) {
                    this._startTime = startTime;// + (10 * 1000);
                }
                else {
                    //Log.e("EventPreferencesCallScreening.saveStartTime", "*** (1) _startTime = 0");
                    this._startTime = 0;
                }

                DatabaseHandler.getInstance(dataWrapper.context).updateCallScreeningStartTime(_event);

                if (phoneNumberFound) {
                    //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                    setSystemEventForPause(dataWrapper.context);
                }
            } else {
                //Log.e("EventPreferencesCallScreening.saveStartTime", "*** (2) _startTime = 0");
                this._startTime = 0;
                DatabaseHandler.getInstance(dataWrapper.context).updateCallScreeningStartTime(_event);
            }
        }
    }

    void doHandleEvent(EventsHandler eventsHandler) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(PREF_EVENT_CALL_SCREENING_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {

                if (isIsCallScreeningHeld(eventsHandler.context)) {

                    //Log.e("EventPreferencesCallScreening.doHandleEvent", "_startTime="+_startTime);
                    if (_startTime > 0) {

                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        long startTime = _startTime - gmtOffset;

                        // compute end datetime
                        long endAlarmTime = computeAlarm();

                        Calendar now = Calendar.getInstance();
                        long nowAlarmTime = now.getTimeInMillis();

                        boolean continueHandler;
                        if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_CALL_SCREENING))
                            continueHandler = true;
                        else if (!_permanentRun) {
                            if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_CALL_SCREENING_EVENT_END))
                                continueHandler = false;
                            else
                                continueHandler = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        } else {
                            continueHandler = nowAlarmTime >= startTime;
                        }

                        //Log.e("EventPreferencesCallScreening.doHandleEvent", "continueHandler="+continueHandler);

                        if (continueHandler) {
                            // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                            int callDirection = ApplicationPreferences.prefEventCallScreeningCallDirection;
                            String phoneNumber = ApplicationPreferences.prefEventCallScreeningPhoneNumber;
                            //Log.e("EventPreferencesCallScreening.doHandleEvent", "screeningActive="+screeningActive);
                            //Log.e("EventPreferencesCallScreening.doHandleEvent", "phoneNumber="+phoneNumber);

                            boolean phoneNumberFound = false;

                            if ((_callDirection == CALL_DIRECTION_ALL) || (_callDirection == callDirection)) {
                                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                                if (contactsCache != null) {
                                    List<Contact> contactList;
//                                    PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
                                    synchronized (PPApplication.contactsCacheMutex) {
                                        contactList = contactsCache.getList();
                                    }
                                    phoneNumberFound = isPhoneNumberConfigured(contactList, phoneNumber);
                                    if (contactList != null)
                                        contactList.clear();
                                }

                                //Log.e("EventPreferencesCallScreening.doHandleEvent", "phoneNumberFound="+phoneNumberFound);

                                if (!phoneNumberFound)
                                    eventsHandler.callScreeningPassed = false;

                            } else
                                eventsHandler.callScreeningPassed = false;
                        } else
                            eventsHandler.callScreeningPassed = false;
                    } else
                        eventsHandler.callScreeningPassed = false;
                } else
                    eventsHandler.notAllowedCallScreening = false;

                if (!eventsHandler.callScreeningPassed) {
                    //Log.e("EventPreferencesCallScreening.doHandleEvent", "*** _startTime = 0");
                    _startTime = 0;
                    DatabaseHandler.getInstance(eventsHandler.context).updateCallScreeningStartTime(_event);
                }

                if (!eventsHandler.notAllowedCall) {
                    if (eventsHandler.callScreeningPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            }
            else
                eventsHandler.notAllowedCallScreening = true;

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_CALL_SCREENING);
            }
        }
    }

}
