package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesRoaming extends EventPreferences {

    boolean _checkNetwork;
    boolean _checkData;
    int _forSIMCard;

    boolean _networkRoamingInSIMSlot0;
    boolean _dataRoamingInSIMSlot0;
    boolean _networkRoamingInSIMSlot1;
    boolean _dataRoamingInSIMSlot1;
    boolean _networkRoamingInSIMSlot2;
    boolean _dataRoamingInSIMSlot2;

    static final String PREF_EVENT_ROAMING_ENABLED = "eventRoamingEnabled";
    private static final String PREF_EVENT_ROAMING_CHECK_NETWORK = "eventRoamingCheckNetwork";
    static final String PREF_EVENT_ROAMING_CHECK_DATA = "eventRoamingCheckData";
    private static final String PREF_EVENT_ROAMING_FOR_SIM_CARD = "eventRomanigForSimCard";

    static final String PREF_EVENT_ROAMING_ENABLED_NO_CHECK_SIM = "eventRoamingEnabledEnabledNoCheckSim";

    private static final String PREF_EVENT_ROAMING_CATEGORY = "eventRoamingCategoryRoot";

    static final String PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_0 = "eventRoamingNetworkInSIMSlot0";
    static final String PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_0 = "eventRoamingInDataSIMSlot0";
    static final String PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_1 = "eventRoamingNetworkInSIMSlot1";
    static final String PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_1 = "eventRoamingDataInSIMSlot1";
    static final String PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_2 = "eventRoamingNetworkInSIMSlot2";
    static final String PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_2 = "eventRoamingDataInSIMSlot2";

    EventPreferencesRoaming(Event event,
                            boolean enabled,
                            boolean checkNetwork,
                            boolean checkData,
                            int forSIMCard) {
        super(event, enabled);

        this._checkNetwork = checkNetwork;
        this._checkData = checkData;
        this._forSIMCard = forSIMCard;

        this._networkRoamingInSIMSlot0 = false;
        this._dataRoamingInSIMSlot0 = false;
        this._networkRoamingInSIMSlot1 = false;
        this._dataRoamingInSIMSlot1 = false;
        this._networkRoamingInSIMSlot2 = false;
        this._dataRoamingInSIMSlot2 = false;
    }

    void copyPreferences(Event fromEvent) {
        this._enabled = fromEvent._eventPreferencesRoaming._enabled;
        this._checkNetwork = fromEvent._eventPreferencesRoaming._checkNetwork;
        this._checkData = fromEvent._eventPreferencesRoaming._checkData;
        this._forSIMCard = fromEvent._eventPreferencesRoaming._forSIMCard;
        this.setSensorPassed(fromEvent._eventPreferencesRoaming.getSensorPassed());

        this._networkRoamingInSIMSlot0 = false;
        this._dataRoamingInSIMSlot0 = false;
        this._networkRoamingInSIMSlot1 = false;
        this._dataRoamingInSIMSlot1 = false;
        this._networkRoamingInSIMSlot2 = false;
        this._dataRoamingInSIMSlot2 = false;
    }

    void loadSharedPreferences(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_ROAMING_ENABLED, _enabled);
        editor.putBoolean(PREF_EVENT_ROAMING_CHECK_NETWORK, this._checkNetwork);
        editor.putBoolean(PREF_EVENT_ROAMING_CHECK_DATA, this._checkData);
        editor.putString(PREF_EVENT_ROAMING_FOR_SIM_CARD, String.valueOf(this._forSIMCard));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences) {
        this._enabled = preferences.getBoolean(PREF_EVENT_ROAMING_ENABLED, false);
        this._checkNetwork = preferences.getBoolean(PREF_EVENT_ROAMING_CHECK_NETWORK, false);
        this._checkData = preferences.getBoolean(PREF_EVENT_ROAMING_CHECK_DATA, false);
        this._forSIMCard = Integer.parseInt(preferences.getString(PREF_EVENT_ROAMING_FOR_SIM_CARD, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_roaming_summary);
        } else {
            if (addBullet) {
                descr = descr + StringConstants.TAG_BOLD_START_HTML;
                descr = descr + getPassStatusString(context.getString(R.string.event_type_roaming), addPassStatus, DatabaseHandler.ETYPE_ROAMING, context);
                descr = descr + StringConstants.TAG_BOLD_END_HTML+" ";
            }

            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_ROAMING_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (this._checkNetwork) {
                    descr = descr + StringConstants.TAG_BOLD_START_HTML + getColorForChangedPreferenceValue(context.getString(R.string.pref_event_roaming_check_network), disabled, context) + StringConstants.TAG_BOLD_END_HTML;
                }
                if (this._checkData) {
                    if (this._checkNetwork)
                        descr = descr + StringConstants.STR_DOT;
                    descr = descr + StringConstants.TAG_BOLD_START_HTML + getColorForChangedPreferenceValue(context.getString(R.string.pref_event_roaming_check_data), disabled, context) + StringConstants.TAG_BOLD_END_HTML;
                }

                //if (Build.VERSION.SDK_INT >= 26) {
                    boolean hasSIMCard = false;
                    final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            boolean simExists;
                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                            boolean sim1Exists = hasSIMCardData.hasSIM1;
                            boolean sim2Exists = hasSIMCardData.hasSIM2;

                            simExists = sim1Exists;
                            simExists = simExists && sim2Exists;
                            hasSIMCard = simExists;
                        }
                    }
                    if (hasSIMCard) {
                        descr = descr + StringConstants.STR_DOT + context.getString(R.string.event_preferences_roaming_forSimCard);
                        String[] forSimCard = context.getResources().getStringArray(R.array.eventRoamingForSimCardArray);
                        descr = descr + ": "+StringConstants.TAG_BOLD_START_HTML + getColorForChangedPreferenceValue(forSimCard[this._forSIMCard], disabled, context) + StringConstants.TAG_BOLD_END_HTML;
                    }
                //}
            }
            else {
                descr = descr + context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_ROAMING_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        boolean hasFeature = false;
        boolean hasSIMCard = false;
        //if (Build.VERSION.SDK_INT >= 26) {
            if (key.equals(PREF_EVENT_ROAMING_FOR_SIM_CARD)) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        hasFeature = true;
                        boolean simExists;
                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        boolean sim1Exists = hasSIMCardData.hasSIM1;
                        boolean sim2Exists = hasSIMCardData.hasSIM2;

                        simExists = sim1Exists;
                        simExists = simExists && sim2Exists;
                        hasSIMCard = simExists;
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(value);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);
                        }
                    }
                }
                if (!hasFeature) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_ROAMING_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
                else if (!hasSIMCard) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_ROAMING_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
            }
        //}

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesRoaming.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesRoaming.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_ROAMING_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_ROAMING_CHECK_NETWORK);
        if (preference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_ROAMING_CHECK_NETWORK, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_ROAMING_CHECK_DATA);
        if (preference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_ROAMING_CHECK_DATA, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_ROAMING_FOR_SIM_CARD);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, false, !isRunnable, false);
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_ROAMING_ENABLED) ||
                key.equals(PREF_EVENT_ROAMING_CHECK_NETWORK) ||
                key.equals(PREF_EVENT_ROAMING_CHECK_DATA)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_ROAMING_FOR_SIM_CARD)) {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {
        setSummary(prefMng, PREF_EVENT_ROAMING_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_ROAMING_CHECK_NETWORK, preferences, context);
        setSummary(prefMng, PREF_EVENT_ROAMING_CHECK_DATA, preferences, context);
        setSummary(prefMng, PREF_EVENT_ROAMING_FOR_SIM_CARD, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_ROAMING_ENABLED_NO_CHECK_SIM, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesRoaming tmp = new EventPreferencesRoaming(this._event, this._enabled, this._checkNetwork, this._checkData, this._forSIMCard);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ROAMING_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ROAMING_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && (tmp.isAccessibilityServiceEnabled(context, false) == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_ROAMING).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        } else {
            Preference preference = prefMng.findPreference(PREF_EVENT_ROAMING_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context) {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && (_checkNetwork || _checkData);

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_ROAMING_ENABLED) != null) {
                //if (Build.VERSION.SDK_INT >= 26) {
                    boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_ROAMING_ENABLED, false);
                    Preference preference;
                    boolean showPreferences = false;
                    final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                            boolean sim1Exists = hasSIMCardData.hasSIM1;
                            boolean sim2Exists = hasSIMCardData.hasSIM2;

                            showPreferences = true;
                            preference = prefMng.findPreference(PREF_EVENT_ROAMING_FOR_SIM_CARD);
                            if (preference != null)
                                preference.setEnabled(enabled && sim1Exists && sim2Exists);
                        } else {
                            preference = prefMng.findPreference(PREF_EVENT_ROAMING_FOR_SIM_CARD);
                            if (preference != null)
                                preference.setEnabled(false);
                        }
                    }
                    if (!showPreferences) {
                        preference = prefMng.findPreference(PREF_EVENT_ROAMING_FOR_SIM_CARD);
                        if (preference != null)
                            preference.setVisible(false);
                    }
                //}

                setSummary(prefMng, PREF_EVENT_ROAMING_ENABLED, preferences, context);
            }
        }
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

    static void getEventRoamingInSIMSlot(Context context, int simSlot) {
        synchronized (PPApplication.eventRoamingSensorMutex) {
            switch (simSlot) {
                case 0:
                    ApplicationPreferences.prefEventRoamingNetworkInSIMSlot0 = ApplicationPreferences.
                            getSharedPreferences(context).getBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_0, false);
                    ApplicationPreferences.prefEventRoamingDataInSIMSlot0 = ApplicationPreferences.
                            getSharedPreferences(context).getBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_0, false);
                    break;
                case 1:
                    ApplicationPreferences.prefEventRoamingNetworkInSIMSlot1 = ApplicationPreferences.
                            getSharedPreferences(context).getBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_1, false);
                    ApplicationPreferences.prefEventRoamingDataInSIMSlot1 = ApplicationPreferences.
                            getSharedPreferences(context).getBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_1, false);
                    break;
                case 2:
                    ApplicationPreferences.prefEventRoamingNetworkInSIMSlot2 = ApplicationPreferences.
                            getSharedPreferences(context).getBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_2, false);
                    ApplicationPreferences.prefEventRoamingDataInSIMSlot2 = ApplicationPreferences.
                            getSharedPreferences(context).getBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_2, false);
                    break;
            }
        }
    }
    static void setEventRoamingInSIMSlot(Context context, int simSlot, boolean networkRoaming, boolean dataRoaming) {
        synchronized (PPApplication.eventRoamingSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            switch (simSlot) {
                case 0:
                    editor.putBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_0, networkRoaming);
                    ApplicationPreferences.prefEventRoamingNetworkInSIMSlot0 = networkRoaming;
                    editor.putBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_0, dataRoaming);
                    ApplicationPreferences.prefEventRoamingDataInSIMSlot0 = dataRoaming;
                    break;
                case 1:
                    editor.putBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_1, networkRoaming);
                    ApplicationPreferences.prefEventRoamingNetworkInSIMSlot1 = networkRoaming;
                    editor.putBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_1, dataRoaming);
                    ApplicationPreferences.prefEventRoamingDataInSIMSlot1 = dataRoaming;
                    break;
                case 2:
                    editor.putBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_NETWORK_IN_SIM_SLOT_2, networkRoaming);
                    ApplicationPreferences.prefEventRoamingNetworkInSIMSlot2 = networkRoaming;
                    editor.putBoolean(EventPreferencesRoaming.PREF_EVENT_ROAMING_DATA_IN_SIM_SLOT_2, dataRoaming);
                    ApplicationPreferences.prefEventRoamingDataInSIMSlot2 = dataRoaming;
                    break;
            }

            editor.apply();
        }
    }


    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                this._networkRoamingInSIMSlot0 = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot0;
                this._dataRoamingInSIMSlot0 = ApplicationPreferences.prefEventRoamingDataInSIMSlot0;
                this._networkRoamingInSIMSlot1 = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot1;
                this._dataRoamingInSIMSlot1 = ApplicationPreferences.prefEventRoamingDataInSIMSlot1;
                this._networkRoamingInSIMSlot2 = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot2;
                this._dataRoamingInSIMSlot2 = ApplicationPreferences.prefEventRoamingDataInSIMSlot2;

                boolean networkRoaming = false;
                boolean dataRoaming = false;
                /*if (Build.VERSION.SDK_INT < 26) {
                    networkRoaming = _networkRoamingInSIMSlot0;
                    dataRoaming = _dataRoamingInSIMSlot0;
                }
                else*/
                if (_forSIMCard == 0) {
                    networkRoaming = _networkRoamingInSIMSlot0 || _networkRoamingInSIMSlot1 || _networkRoamingInSIMSlot2;
                    dataRoaming = _dataRoamingInSIMSlot0 || _dataRoamingInSIMSlot1 || _dataRoamingInSIMSlot2;
                }
                else
                if (_forSIMCard == 1) {
                    networkRoaming = _networkRoamingInSIMSlot1;
                    dataRoaming = _dataRoamingInSIMSlot1;
                }
                else
                if (_forSIMCard == 2) {
                    networkRoaming = _networkRoamingInSIMSlot2;
                    dataRoaming = _dataRoamingInSIMSlot2;
                }

                if (_checkNetwork && _checkData)
                    eventsHandler.roamingPassed = networkRoaming && dataRoaming;
                else
                if (_checkNetwork)
                    eventsHandler.roamingPassed = networkRoaming;
                else
                if (_checkData)
                    eventsHandler.roamingPassed = dataRoaming;
                else
                    eventsHandler.notAllowedRoaming = true;

                if (!eventsHandler.notAllowedRoaming) {
                    if (eventsHandler.roamingPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            }
            else
                eventsHandler.notAllowedRoaming = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_ROAMING);
            }
        }
    }

}
