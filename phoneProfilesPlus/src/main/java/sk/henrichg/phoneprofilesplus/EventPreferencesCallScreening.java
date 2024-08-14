package sk.henrichg.phoneprofilesplus;

import static android.app.role.RoleManager.ROLE_CALL_SCREENING;
import static android.content.Context.ROLE_SERVICE;

import android.annotation.SuppressLint;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.telephony.PhoneNumberUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class EventPreferencesCallScreening extends EventPreferences {

    String _contacts; // contactId#phoneId|...
    String _contactGroups; // groupId|...
    int _contactListType;
    boolean _blockCalls;
    boolean _sendSMS;
    String _smsText;


    static final String PREF_EVENT_CALL_SCREENING_ENABLED = "eventCallScreeningEnabled";
    static final String PREF_EVENT_CALL_SCREENING_CONTACTS = "eventCallScreeningContacts";
    static final String PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS = "eventCallScreeningContactGroups";
    private static final String PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE = "eventCallScreeningContactListType";
    static final String PREF_EVENT_CALL_SCREENING_BLOCK_CALLS = "eventCallScreeningBlockCalls";
    static final String PREF_EVENT_CALL_SCREENING_SEND_SMS = "eventCallScreeningSendSMS";
    static final String PREF_EVENT_CALL_SCREENING_SMS_TEXT = "eventCallScreeningSMSText";

    static final String PREF_EVENT_CALL_SCREENING_SET_CALL_SCREENING_ROLE = "eventCallScreeningSetCallScreeningRole";

    static final String PREF_EVENT_CALL_SCREENING_CATEGORY = "eventCallScreeningCategoryRoot";

    static final int CONTACT_LIST_TYPE_WHITE_LIST = 0;
    static final int CONTACT_LIST_TYPE_BLACK_LIST = 1;
    static final int CONTACT_LIST_TYPE_NOT_USE = 2;

    EventPreferencesCallScreening(Event event,
                                  boolean enabled,
                                  String contacts,
                                  String contactGroups,
                                  int contactListType,
                                  boolean blockCalls,
                                  boolean sendSMS,
                                  String smsText) {
        super(event, enabled);

        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._contactListType = contactListType;
        this._blockCalls = blockCalls;
        this._sendSMS = sendSMS;
        this._smsText = smsText;
    }

    void copyPreferences(Event fromEvent) {
        this._enabled = fromEvent._eventPreferencesCallScreening._enabled;
        this._contacts = fromEvent._eventPreferencesCallScreening._contacts;
        this._contactGroups = fromEvent._eventPreferencesCallScreening._contactGroups;
        this._contactListType = fromEvent._eventPreferencesCallScreening._contactListType;
        this._blockCalls = fromEvent._eventPreferencesCallScreening._blockCalls;
        this._sendSMS = fromEvent._eventPreferencesCallScreening._sendSMS;
        this._smsText = fromEvent._eventPreferencesCallScreening._smsText;
        this.setSensorPassed(fromEvent._eventPreferencesCallScreening.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_CALL_SCREENING_ENABLED, _enabled);
        editor.putString(PREF_EVENT_CALL_SCREENING_CONTACTS, this._contacts);
        editor.putString(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS, this._contactGroups);
        editor.putString(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
        editor.putBoolean(PREF_EVENT_CALL_SCREENING_BLOCK_CALLS, this._blockCalls);
        editor.putBoolean(PREF_EVENT_CALL_SCREENING_SEND_SMS, this._sendSMS);
        editor.putString(PREF_EVENT_CALL_SCREENING_SMS_TEXT, this._smsText);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences) {
        this._enabled = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_ENABLED, false);
        this._contacts = preferences.getString(PREF_EVENT_CALL_SCREENING_CONTACTS, "");
        this._contactGroups = preferences.getString(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS, "");
        this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE, "0"));
        this._blockCalls = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_BLOCK_CALLS, false);
        this._sendSMS = preferences.getBoolean(PREF_EVENT_CALL_SCREENING_SEND_SMS, false);
        this._smsText = preferences.getString(PREF_EVENT_CALL_SCREENING_SMS_TEXT, "");
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
                    _value.append(context.getString(R.string.event_preferences_call_contact_groups)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactGroupsMultiSelectDialogPreference.getSummary(_contactGroups, context), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_call_contacts)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactsMultiSelectDialogPreference.getSummary(_contacts, false, context), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_contactListType));
                    String[] contactListTypes = context.getResources().getStringArray(R.array.eventCallContactListTypeArray);
                    _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(contactListTypes[this._contactListType], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                    if (_blockCalls) {
                        _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preference_callScreeningBlockCalls));
                        if (_sendSMS) {
                            _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preference_callScreeningSendSMS));
                        }
                    }
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

        if (key.equals(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE)) {
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

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesCallScreening.saveSharedPreferences(prefMng.getSharedPreferences());
        //boolean isRunnable = event._eventPreferencesCallScreening.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesCall.isAllConfigured(context);
        boolean isAllConfigured = ((_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||
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
        preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !isAllConfigured, false);

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
        if (key.equals(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE) ||
                key.equals(PREF_EVENT_CALL_SCREENING_CONTACTS) ||
                key.equals(PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS) ||
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
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_SET_CALL_SCREENING_ROLE, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_BLOCK_CALLS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_SEND_SMS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SCREENING_SMS_TEXT, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        EventPreferencesCallScreening tmp = new EventPreferencesCallScreening(this._event, this._enabled, this._contacts, this._contactGroups,
                this._contactListType, this._blockCalls, this._sendSMS, this._smsText);
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
                ((_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||
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
                    preference = prefMng.findPreference(PREF_EVENT_CALL_SCREENING_CONTACT_LIST_TYPE);
                    if (preference != null)
                        preference.setEnabled(isRoleHeld);
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

    @Override
    void setSystemEventForStart(Context context) {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that will change state into RUNNING;
        // from broadcast will by called EventsHandler

        //removeRunAfterCallEndAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context) {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that will change state into PAUSE;
        // from broadcast will by called EventsHandler

        //removeRunAfterCallEndAlarm(context);

        if (!(isRunnable(context) && isAllConfigured(context) && _enabled))
            return;

        /*
        if ((_callEvent == CALL_EVENT_MISSED_CALL) ||
                (_callEvent == CALL_EVENT_INCOMING_CALL_ENDED) ||
                (_callEvent == CALL_EVENT_OUTGOING_CALL_ENDED))
            setRunAfterCallEndAlarm(computeRunAfterCallEndAlarm(), context);
        */
    }

    @Override
    void removeSystemEvent(Context context) {
        //removeRunAfterCallEndAlarm(context);
    }

    boolean isPhoneNumberConfigured(List<Contact> contactList, String phoneNumber/*, DataWrapper dataWrapper*/) {
        boolean phoneNumberFound = false;

        if (this._contactListType != CONTACT_LIST_TYPE_NOT_USE) {
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

            if (this._contactListType == CONTACT_LIST_TYPE_BLACK_LIST)
                phoneNumberFound = !phoneNumberFound;
        } else
            phoneNumberFound = true;

        return phoneNumberFound;
    }

    @SuppressLint({"MissingPermission", "PrivateApi"})
    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(PREF_EVENT_CALL_SCREENING_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
/*
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                int callEventType = ApplicationPreferences.prefEventCallEventType;
                String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;
                int runAfterCallEndSIMSlot = ApplicationPreferences.prefEventCallRunAfterCallEndFromSIMSlot;
                //Log.e("EventPreferencesCall.doHandleEvent", "callEventType="+callEventType);
                //Log.e("EventPreferencesCall.doHandleEvent", "phoneNumber="+phoneNumber);
                //Log.e("EventPreferencesCall.doHandleEvent", "simSlot="+simSlot);

                boolean phoneNumberFound = false;

                if (callEventType != PHONE_CALL_EVENT_UNDEFINED) {
                    if (callEventType == PHONE_CALL_EVENT_SERVICE_UNBIND)
                        eventsHandler.callPassed = false;
                    else {
                        ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                        if (contactsCache != null) {
                            List<Contact> contactList;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
                            synchronized (PPApplication.contactsCacheMutex) {
                                contactList = contactsCache.getList();
                            }
                            phoneNumberFound = isPhoneNumberConfigured(contactList, phoneNumber);
                            if (contactList != null)
                                contactList.clear();
                        }
                    }

                    if (phoneNumberFound) {
                        boolean fromSIMSlot = false;
                        if ((_callEvent == CALL_EVENT_MISSED_CALL) ||
                            (_callEvent == CALL_EVENT_INCOMING_CALL_ENDED) ||
                            (_callEvent == CALL_EVENT_OUTGOING_CALL_ENDED)) {
                            _runAfterCallEndFromSIMSlot = runAfterCallEndSIMSlot;
                            fromSIMSlot = _forSIMCard == _runAfterCallEndFromSIMSlot;
                        }

                        if ((_forSIMCard == 0) || fromSIMSlot) {
                            if (_callEvent == CALL_EVENT_RINGING) {
                                //noinspection StatementWithEmptyBody
                                if (callEventType == PHONE_CALL_EVENT_INCOMING_CALL_RINGING)
                                    ;//eventStart = eventStart && true;
                                else
                                //noinspection StatementWithEmptyBody
                                if (callEventType == PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED)
                                    ;//eventStart = eventStart && true;
                                else
                                    eventsHandler.callPassed = false;
                            } else if (_callEvent == CALL_EVENT_INCOMING_CALL_ANSWERED) {
                                //noinspection StatementWithEmptyBody
                                if (callEventType == PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED)
                                    ;//eventStart = eventStart && true;
                                else
                                    eventsHandler.callPassed = false;
                            } else if (_callEvent == CALL_EVENT_OUTGOING_CALL_STARTED) {
                                //noinspection StatementWithEmptyBody
                                if (callEventType == PHONE_CALL_EVENT_OUTGOING_CALL_ANSWERED)
                                    ;//eventStart = eventStart && true;
                                else
                                    eventsHandler.callPassed = false;
                            } else if ((_callEvent == CALL_EVENT_MISSED_CALL) ||
                                    (_callEvent == CALL_EVENT_INCOMING_CALL_ENDED) ||
                                    (_callEvent == CALL_EVENT_OUTGOING_CALL_ENDED)) {
//                                Log.e("EventPreferencesCall.doHandleEvent", "_startTime="+_startTime);
                                if (_runAfterCallEndTime > 0) {
                                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                    long startTime = _runAfterCallEndTime - gmtOffset;


                                    // compute end datetime
                                    long endAlarmTime = computeRunAfterCallEndAlarm();

                                    Calendar now = Calendar.getInstance();
                                    long nowAlarmTime = now.getTimeInMillis();

                                    if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_PHONE_CALL)) {
//                                        Log.e("EventPreferencesCall.doHandleEvent", "SENSOR_TYPE_PHONE_CALL");
                                        //noinspection StatementWithEmptyBody
                                        if (((callEventType == PHONE_CALL_EVENT_MISSED_CALL) && (_callEvent == EventPreferencesCallScreening.CALL_EVENT_MISSED_CALL)) ||
                                                ((callEventType == PHONE_CALL_EVENT_INCOMING_CALL_ENDED) && (_callEvent == EventPreferencesCallScreening.CALL_EVENT_INCOMING_CALL_ENDED)) ||
                                                ((callEventType == PHONE_CALL_EVENT_OUTGOING_CALL_ENDED) && (_callEvent == EventPreferencesCallScreening.CALL_EVENT_OUTGOING_CALL_ENDED)))
                                            ;//eventStart = eventStart && true;
                                        else
                                            eventsHandler.callPassed = false;
                                    } else if (!_runAfterCallEndPermanentRun) {
                                        if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                            eventsHandler.callPassed = false;
                                        else
                                            eventsHandler.callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                                    } else {
                                        eventsHandler.callPassed = nowAlarmTime >= startTime;
                                    }
                                } else
                                    eventsHandler.callPassed = false;
                            }
                        }
                        else
                            eventsHandler.callPassed = false;

                        //if ((callEventType == PhoneCallsListener.CALL_EVENT_INCOMING_CALL_ENDED) ||
                        //        (callEventType == PhoneCallsListener.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        //    //callPassed = true;
                        //    //eventStart = eventStart && false;
                        //    callPassed = false;
                        //}
                    } else
                        eventsHandler.callPassed = false;

                    if (!eventsHandler.callPassed) {
                        _runAfterCallEndTime = 0;
                        _runAfterCallEndFromSIMSlot = 0;
                        DatabaseHandler.getInstance(eventsHandler.context).updateCallRunAfterCallEndTime(_event);
                    }
                } else {
                    if ((_callEvent == CALL_EVENT_MISSED_CALL) ||
                            (_callEvent == CALL_EVENT_INCOMING_CALL_ENDED) ||
                            (_callEvent == CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        if (_runAfterCallEndTime > 0) {
                            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                            long startTime = _runAfterCallEndTime - gmtOffset;

                            // compute end datetime
                            long endAlarmTime = computeRunAfterCallEndAlarm();

                            Calendar now = Calendar.getInstance();
                            long nowAlarmTime = now.getTimeInMillis();

                            if (!_runAfterCallEndPermanentRun) {
                                if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                    eventsHandler.callPassed = false;
                                else
                                    eventsHandler.callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                            } else {
                                eventsHandler.callPassed = nowAlarmTime >= startTime;
                            }
                        }
                        else
                            eventsHandler.callPassed = false;

                        if (!eventsHandler.callPassed) {
                            _runAfterCallEndTime = 0;
                            _runAfterCallEndFromSIMSlot = 0;
                            DatabaseHandler.getInstance(eventsHandler.context).updateCallRunAfterCallEndTime(_event);
                        }
                    }
                    else
                        eventsHandler.notAllowedCall = true;
                }

                if (!eventsHandler.notAllowedCall) {
                    if (eventsHandler.callPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }*/
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
