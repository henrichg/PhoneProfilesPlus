package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.ArrayList;
import java.util.List;

class EventPreferencesMobileCells extends EventPreferences {

    String _cellsNames;
    boolean _whenOutside;
    int _forSIMCard;

    static final String PREF_EVENT_MOBILE_CELLS_ENABLED = "eventMobileCellsEnabled";
    static final String PREF_EVENT_MOBILE_CELLS_CELL_NAMES = "eventMobileCellsCellNames";
    private static final String PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE = "eventMobileCellsStartWhenOutside";
    private static final String PREF_EVENT_MOBILE_CELLS_APP_SETTINGS = "eventMobileCellsScanningAppSettings";
    static final String PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = "eventMobileCellsLocationSystemSettings";
    private static final String PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD = "eventMobileCellsForSimCard";

    static final String PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM = "eventMobileCellsEnabledNoCheckSim";

    static final String PREF_EVENT_MOBILE_CELLS_CATEGORY = "eventMobileCellsCategoryRoot";

    //private DataWrapper dataWrapper = null;

    EventPreferencesMobileCells(Event event,
                                boolean enabled,
                                String cellNames,
                                boolean _whenOutside,
                                int forSIMCard)
    {
        super(event, enabled);
//        Log.e("EventPreferencesMobileCells", "event._id="+event._id);

        this._cellsNames = cellNames;
        this._whenOutside = _whenOutside;
        this._forSIMCard = forSIMCard;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesMobileCells._enabled;
        this._cellsNames = fromEvent._eventPreferencesMobileCells._cellsNames;
        this._whenOutside = fromEvent._eventPreferencesMobileCells._whenOutside;
        this._forSIMCard = fromEvent._eventPreferencesMobileCells._forSIMCard;
        this.setSensorPassed(fromEvent._eventPreferencesMobileCells.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, _enabled);
        editor.putString(PREF_EVENT_MOBILE_CELLS_CELL_NAMES, this._cellsNames);
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, this._whenOutside);
        editor.putString(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD, String.valueOf(this._forSIMCard));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
        this._cellsNames  = preferences.getString(PREF_EVENT_MOBILE_CELLS_CELL_NAMES, "");
        this._whenOutside = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, false);
        this._forSIMCard = Integer.parseInt(preferences.getString(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_mobile_cells_summary));
        } else {
            if (addBullet) {
                _value.append(StringConstants.TAG_BOLD_START_HTML);
                _value.append(getPassStatusString(context.getString(R.string.event_type_mobile_cells), addPassStatus, DatabaseHandler.ETYPE_MOBILE_CELLS, context));
                _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
            }

            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_MOBILE_CELLS_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                    PPApplicationStatic.logE("[TEST BATTERY] EventPreferencesMobileCells.getPreferencesDescription", "******** ### *******");
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile)
                        _value.append("* ").append(context.getString(R.string.array_pref_applicationDisableScanning_disabled)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                    else
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile)).append(StringConstants.TAG_BREAK_HTML);
                }
                else
                if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                    if (Build.VERSION.SDK_INT < 28)
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary)).append(".").append(StringConstants.TAG_BREAK_HTML);
                    else
                        _value.append("* ").append(context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                } else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventMobileCellScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused)).append(StringConstants.TAG_BREAK_HTML);
                    }
                }

                _value.append(context.getString(R.string.event_preferences_mobile_cells_cellNames)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(MobileCellNamesPreference.getSummary(this._cellsNames, context), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                if (this._whenOutside)
                    _value.append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_mobile_cells_when_outside_description), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                boolean hasSIMCard = false;
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        //boolean simExists;
//                        Log.e("EventPreferencesMobileCells.getPreferencesDescription", "called hasSIMCard");
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
                    _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_mobile_cells_forSimCard));
                    String[] forSimCard = context.getResources().getStringArray(R.array.eventMobileCellsForSimCardArray);
                    _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(forSimCard[this._forSIMCard], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
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

        if (key.equals(PREF_EVENT_MOBILE_CELLS_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_MOBILE_CELLS_ENABLED) ||
            key.equals(PREF_EVENT_MOBILE_CELLS_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                    PPApplicationStatic.logE("[TEST BATTERY] EventPreferencesMobileCells.setSummary", "******** ### *******");
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *"+StringConstants.STR_DOUBLE_NEWLINE +
                                context.getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
                        titleColor = ContextCompat.getColor(context, R.color.error_color);
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + StringConstants.STR_DOUBLE_NEWLINE +
                                context.getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventMobileCellScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
                    } else {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
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
                if (preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary;
                if (Build.VERSION.SDK_INT < 28)
                    summary = context.getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettingsNotA9_summary);
                else
                    summary = context.getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettings_summary);
                if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                    if (Build.VERSION.SDK_INT < 28)
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT + summary;
                    else
                        summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *"+StringConstants.STR_DOUBLE_NEWLINE + summary;
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT+
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_MOBILE_CELLS_CELL_NAMES)) {
            MobileCellNamesPreference preference = prefMng.findPreference(key);
            if (preference != null)
                preference.setSummary();
        }

        boolean hasFeature = false;
        boolean hasSIMCard = false;
            if (key.equals(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD)) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        hasFeature = true;
                        //boolean simExists;
//                        Log.e("EventPreferencesMobileCells.setSummary", "called hasSIMCard");
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
                    Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
                else if (!hasSIMCard) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
            }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesMobileCells.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesMobileCells.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CELL_NAMES);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_MOBILE_CELLS_CELL_NAMES, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_MOBILE_CELLS_ENABLED) ||
            key.equals(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_MOBILE_CELLS_CELL_NAMES) ||
            key.equals(PREF_EVENT_MOBILE_CELLS_APP_SETTINGS) ||
            key.equals(PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS) ||
            key.equals(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELL_NAMES, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesMobileCells tmp = new EventPreferencesMobileCells(this._event,
                    this._enabled, this._cellsNames, this._whenOutside, this._forSIMCard);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_MOBILE_CELLS).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CATEGORY);
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

        runnable = runnable && (!_cellsNames.isEmpty());

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_ENABLED) != null) {
                //setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELL_NAMES, preferences, context);
                setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_APP_SETTINGS, preferences, context);
                setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS, preferences, context);

                    Preference preference;

                    boolean showPreferences = false;
                    final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
//                            Log.e("EventPreferencesMobileCells.checkPreferences", "called hasSIMCard");
                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                            boolean sim1Exists = hasSIMCardData.hasSIM1;
                            boolean sim2Exists = hasSIMCardData.hasSIM2;

                            showPreferences = true;
                            //preference = prefMng.findPreference("eventMobileCellsDualSIMInfo");
                            //if (preference != null)
                            //    preference.setEnabled(enabled && sim1Exists && sim2Exists);
                            preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD);
                            if (preference != null)
                                preference.setEnabled(enabled && sim1Exists && sim2Exists);
                        } else {
                            //preference = prefMng.findPreference("eventMobileCellsDualSIMInfo");
                            //if (preference != null)
                            //    preference.setEnabled(false);
                            preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD);
                            if (preference != null)
                                preference.setEnabled(false);
                        }
                    }
                    if (!showPreferences) {
                        //preference = prefMng.findPreference("eventMobileCellsDualSIMInfo");
                        //if (preference != null)
                        //    preference.setVisible(false);
                        preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD);
                        if (preference != null)
                            preference.setVisible(false);
                    }
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }


    void updateConfguredCellNames(PreferenceManager prefMng, Context context) {
//        Log.e("EventPreferencesMobileCells.updateConfguredCellNames", "---- XXXX -----");
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences != null) {
            DatabaseHandler db = DatabaseHandler.getInstance(context.getApplicationContext());
//                    Log.e("EventPreferencesMobileCells.checkPreferences", "_event._id=" + _event._id);
            String cellNames = db.getEventMobileCellsCells(_event._id);
            if ((cellNames != null) && (!cellNames.isEmpty())) {
//                        Log.e("EventPreferencesMobileCells.checkPreferences", "cellNames=" + cellNames);
                Editor editor = preferences.edit();
                editor.putString(PREF_EVENT_MOBILE_CELLS_CELL_NAMES, cellNames);
                editor.apply();
                MobileCellNamesPreference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CELL_NAMES);
                if (preference != null)
                    preference.setValue(cellNames);
            }
        }

        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELL_NAMES, preferences, context);
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

    void doHandleEvent(EventsHandler eventsHandler, boolean forRestartEvents) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning) {
                    //if (forRestartEvents)
                    //    mobileCellPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesMobileCells.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else
                    // not allowed for disabled mobile cells scanner
                    //    notAllowedMobileCell = true;
                    eventsHandler.mobileCellPassed = false;
                } else {
//                    PPApplicationStatic.logE("[TEST BATTERY] EventPreferencesMobileCells.doHandleEvent", "******** ### *******");
                    if ((Build.VERSION.SDK_INT < 28) || GlobalUtils.isLocationEnabled(eventsHandler.context)) {

                        //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn) {
                            if (forRestartEvents)
                                eventsHandler.mobileCellPassed = (EventPreferences.SENSOR_PASSED_PASSED & getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                            else
                                // not allowed for screen Off
                                eventsHandler.notAllowedMobileCell = true;
                        } else {
                            boolean scanningPaused = ApplicationPreferences.applicationEventMobileCellScanInTimeMultiply.equals("2") &&
                                    GlobalUtils.isNowTimeBetweenTimes(
                                            ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyFrom,
                                            ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyTo);

                            if (!scanningPaused) {

//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesMobileCells.doHandleEvent", "PPApplication.mobileCellsScannerMutex");
                                synchronized (PPApplication.mobileCellsScannerMutex) {
                                    if ((PhoneProfilesService.getInstance() != null) && (PPApplication.mobileCellsScanner != null)) {

                                        try {
                                            int simCount = 0;

                                            TelephonyManager telephonyManager = (TelephonyManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                                            if (telephonyManager != null) {
                                                simCount = telephonyManager.getPhoneCount();
                                            }

                                            DatabaseHandler db = DatabaseHandler.getInstance(eventsHandler.context);

                                            boolean cellIsValid = false;
                                            if ((simCount > 1)) {
                                                if ((_forSIMCard == 0) || (_forSIMCard == 1)) {
                                                    if (PPApplication.mobileCellsScanner != null) {
                                                        int registeredCell = PPApplication.mobileCellsScanner.getRegisteredCell(1);
                                                        if (MobileCellsScanner.isValidCellId(registeredCell)) {
                                                            //String sRegisteredCell = Integer.toString(registeredCell);
                                                            List<MobileCellsData> _cellsList = new ArrayList<>();
                                                            db.addMobileCellsToList(_cellsList, registeredCell);
                                                            if (!_cellsList.isEmpty()) {
                                                                // registered cell is configured
                                                                String[] splits = _cellsNames.split(StringConstants.STR_SPLIT_REGEX);
                                                                if (_whenOutside) {
                                                                    // all mobile cells must not be registered
                                                                    eventsHandler.mobileCellPassed = true;
                                                                    for (String cellName : splits) {
                                                                        if (cellName.equals(_cellsList.get(0).name)) {
                                                                            // one of cell names in configuration is registered
                                                                            eventsHandler.mobileCellPassed = false;
                                                                            break;
                                                                        }
                                                                    }
                                                                } else {
                                                                    // one mobile cell must be registered
                                                                    eventsHandler.mobileCellPassed = false;
                                                                    for (String cellName : splits) {
                                                                        if (cellName.equals(_cellsList.get(0).name)) {
                                                                            // one of cell names in configuration is registered
                                                                            eventsHandler.mobileCellPassed = true;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                //eventsHandler.mobileCellPassed = false;
                                                                eventsHandler.mobileCellPassed = _whenOutside;
                                                            }
                                                            cellIsValid = true;
                                                        }
                                                    }
                                                }

                                                if (((_forSIMCard == 0) && ((!cellIsValid) || (!eventsHandler.mobileCellPassed))) ||
                                                        (_forSIMCard == 2)) {
                                                    if (PPApplication.mobileCellsScanner != null) {
                                                        int registeredCell = PPApplication.mobileCellsScanner.getRegisteredCell(2);
                                                        if (MobileCellsScanner.isValidCellId(registeredCell)) {
                                                            //String sRegisteredCell = Integer.toString(registeredCell);
                                                            List<MobileCellsData> _cellsList = new ArrayList<>();
                                                            db.addMobileCellsToList(_cellsList, registeredCell);
                                                            if (!_cellsList.isEmpty()) {
                                                                String[] splits = _cellsNames.split(StringConstants.STR_SPLIT_REGEX);
                                                                if (_whenOutside) {
                                                                    // all mobile cells must not be registered
                                                                    eventsHandler.mobileCellPassed = true;
                                                                    for (String cellName : splits) {
                                                                        if (cellName.equals(_cellsList.get(0).name)) {
                                                                            // one of cells in configuration is registered
                                                                            eventsHandler.mobileCellPassed = false;
                                                                            break;
                                                                        }
                                                                    }
                                                                } else {
                                                                    // one mobile cell must be registered
                                                                    eventsHandler.mobileCellPassed = false;
                                                                    for (String cellName : splits) {
                                                                        if (cellName.equals(_cellsList.get(0).name)) {
                                                                            // one of cells in configuration is registered
                                                                            eventsHandler.mobileCellPassed = true;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                //eventsHandler.mobileCellPassed = false;
                                                                eventsHandler.mobileCellPassed = _whenOutside;
                                                            }
                                                            cellIsValid = true;
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (PPApplication.mobileCellsScanner != null) {
                                                    int registeredCell = PPApplication.mobileCellsScanner.getRegisteredCell(0);
                                                    if (MobileCellsScanner.isValidCellId(registeredCell)) {
                                                        //String sRegisteredCell = Integer.toString(registeredCell);
                                                        List<MobileCellsData> _cellsList = new ArrayList<>();
                                                        db.addMobileCellsToList(_cellsList, registeredCell);
                                                        if (!_cellsList.isEmpty()) {
                                                            String[] splits = _cellsNames.split(StringConstants.STR_SPLIT_REGEX);
                                                            if (_whenOutside) {
                                                                // all mobile cells must not be registered
                                                                eventsHandler.mobileCellPassed = true;
                                                                for (String cellName : splits) {
                                                                    if (cellName.equals(_cellsList.get(0).name)) {
                                                                        // one of cells in configuration is registered
                                                                        eventsHandler.mobileCellPassed = false;
                                                                        break;
                                                                    }
                                                                }
                                                            } else {
                                                                // one mobile cell must be registered
                                                                eventsHandler.mobileCellPassed = false;
                                                                for (String cellName : splits) {
                                                                    if (cellName.equals(_cellsList.get(0).name)) {
                                                                        // one of cells in configuration is registered
                                                                        eventsHandler.mobileCellPassed = true;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            //eventsHandler.mobileCellPassed = false;
                                                            eventsHandler.mobileCellPassed = _whenOutside;
                                                        }
                                                        cellIsValid = true;
                                                    }
                                                }
                                            }

                                            if (!cellIsValid)
                                                eventsHandler.notAllowedMobileCell = true;

                                        } catch (Exception e) {
                                            eventsHandler.notAllowedMobileCell = true;

                                            if (PPApplication.mobileCellsScanner != null)
                                                PPApplicationStatic.recordException(e);
                                        }
                                    } else
                                        eventsHandler.notAllowedMobileCell = true;
                                }
                            } else
                                eventsHandler.mobileCellPassed = false;
                        }
                    }  else
                        eventsHandler.mobileCellPassed = false;
                }

                if (!eventsHandler.notAllowedMobileCell) {
                    if (eventsHandler.mobileCellPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedMobileCell = true;

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_MOBILE_CELLS);
            }
        }
    }

}
