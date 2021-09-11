package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

//import android.preference.CheckBoxPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;

class EventPreferencesMobileCells extends EventPreferences {

    String _cells;
    boolean _whenOutside;
    int _forSIMCard;

    static final String PREF_EVENT_MOBILE_CELLS_ENABLED = "eventMobileCellsEnabled";
    static final String PREF_EVENT_MOBILE_CELLS_CELLS = "eventMobileCellsCells";
    private static final String PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE = "eventMobileCellsStartWhenOutside";
    static final String PREF_EVENT_MOBILE_CELLS_REGISTRATION = "eventMobileCellsRegistration";
    private static final String PREF_EVENT_MOBILE_CELLS_APP_SETTINGS = "eventMobileCellsScanningAppSettings";
    static final String PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = "eventMobileCellsLocationSystemSettings";
    private static final String PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD = "eventMobileCellsForSimCard";

    static final String PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM = "eventMobileCellsEnabledNoCheckSim";

    private static final String PREF_EVENT_MOBILE_CELLS_CATEGORY = "eventMobileCellsCategoryRoot";

    //private DataWrapper dataWrapper = null;

    EventPreferencesMobileCells(Event event,
                                boolean enabled,
                                String cells,
                                boolean _whenOutside,
                                int forSIMCard)
    {
        super(event, enabled);

        this._cells = cells;
        this._whenOutside = _whenOutside;
        this._forSIMCard = forSIMCard;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesMobileCells._enabled;
        this._cells = fromEvent._eventPreferencesMobileCells._cells;
        this._whenOutside = fromEvent._eventPreferencesMobileCells._whenOutside;
        this._forSIMCard = fromEvent._eventPreferencesMobileCells._forSIMCard;
        this.setSensorPassed(fromEvent._eventPreferencesMobileCells.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, _enabled);
        editor.putString(PREF_EVENT_MOBILE_CELLS_CELLS, this._cells);
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, this._whenOutside);
        editor.putString(PREF_EVENT_MOBILE_CELLS_REGISTRATION, Long.toString(_event._id));
        editor.putString(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD, String.valueOf(this._forSIMCard));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
        this._cells  = preferences.getString(PREF_EVENT_MOBILE_CELLS_CELLS, "0");
        this._whenOutside = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, false);
        this._forSIMCard = Integer.parseInt(preferences.getString(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_mobile_cells_summary);
        } else {
            if (addBullet) {
                descr = descr + "<b>";
                descr = descr + getPassStatusString(context.getString(R.string.event_type_mobile_cells), addPassStatus, DatabaseHandler.ETYPE_MOBILE_CELLS, context);
                descr = descr + "</b> ";
            }

            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_MOBILE_CELLS_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning) {
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile)
                        descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                    else
                        descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                }
                else
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    if (Build.VERSION.SDK_INT < 28)
                        descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".<br>";
                    else
                        descr = descr + "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *<br>";
                }

                String selectedCells = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                if (!this._cells.isEmpty()) {
                    String[] splits = this._cells.split("\\|");
                    selectedCells = context.getString(R.string.applications_multiselect_summary_text_selected);
                    selectedCells = selectedCells + " " + splits.length;
                }
                descr = descr + context.getString(R.string.event_preferences_mobile_cells_cells) + ": <b>" +selectedCells + "</b>";
                if (this._whenOutside)
                    descr = descr + " • <b>" + context.getString(R.string.event_preferences_mobile_cells_when_outside_description) + "</b>";

                if (Build.VERSION.SDK_INT >= 26) {
                    boolean hasSIMCard = false;
                    final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            boolean simExists;
                            synchronized (PPApplication.simCardsMutext) {
                                simExists = PPApplication.simCardsMutext.simCardsDetected;
                                simExists = simExists && PPApplication.simCardsMutext.sim1Exists;
                                simExists = simExists && PPApplication.simCardsMutext.sim2Exists;
                            }
                            hasSIMCard = simExists;
                        }
                    }
                    if (hasSIMCard) {
                        descr = descr + " • " + context.getString(R.string.event_preferences_mobile_cells_forSimCard);
                        String[] forSimCard = context.getResources().getStringArray(R.array.eventMobileCellsForSimCardArray);
                        descr = descr + ": <b>" + forSimCard[this._forSIMCard] + "</b>";
                    }
                }
            }
            else {
                descr = descr + context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_MOBILE_CELLS_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_MOBILE_CELLS_ENABLED) ||
            key.equals(PREF_EVENT_MOBILE_CELLS_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning) {
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *\n\n" +
                                context.getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
                        titleColor = Color.RED; //0xFFffb000;
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n\n" +
                                context.getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + ".\n\n" +
                            context.getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, sTitle.length(), Object.class);
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
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    if (Build.VERSION.SDK_INT < 28)
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" + summary;
                    else
                        summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *\n\n" + summary;
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n"+
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        boolean hasFeature = false;
        boolean hasSIMCard = false;
        if (Build.VERSION.SDK_INT >= 26) {
            if (key.equals(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD)) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        hasFeature = true;
                        boolean simExists;
                        synchronized (PPApplication.simCardsMutext) {
                            simExists = PPApplication.simCardsMutext.simCardsDetected;
                            simExists = simExists && PPApplication.simCardsMutext.sim1Exists;
                            simExists = simExists && PPApplication.simCardsMutext.sim2Exists;
                        }
                        hasSIMCard = simExists;
                        ListPreference listPreference = prefMng.findPreference(key);
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
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
                else if (!hasSIMCard) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesMobileCells.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesMobileCells.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CELLS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_MOBILE_CELLS_CELLS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key,
                    @SuppressWarnings("unused") SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_MOBILE_CELLS_ENABLED) ||
            key.equals(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true" : "false", context);
        }
        if (key.equals(PREF_EVENT_MOBILE_CELLS_CELLS) ||
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
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELLS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesMobileCells tmp = new EventPreferencesMobileCells(this._event,
                    this._enabled, this._cells, this._whenOutside, this._forSIMCard);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_MOBILE_CELLS).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && (!_cells.isEmpty());

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context) {
        //final boolean enabled = ApplicationPreferences.applicationEventMobileCellEnableScannig(context.getApplicationContext());
        //Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CELLS);
        //if (preference != null) preference.setEnabled(enabled);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        //setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELLS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setCategorySummary(prefMng, preferences, context);

        if (Build.VERSION.SDK_INT >= 26) {
            Preference preference;

            boolean showPreferences = false;
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    boolean sim1Exists;
                    boolean sim2Exists;
                    synchronized (PPApplication.simCardsMutext) {
                        sim1Exists = PPApplication.simCardsMutext.simCardsDetected;
                        sim2Exists = sim1Exists;
                        sim1Exists = sim1Exists && PPApplication.simCardsMutext.sim1Exists;
                        sim2Exists = sim2Exists && PPApplication.simCardsMutext.sim2Exists;
                    }

                    showPreferences = true;
                    //preference = prefMng.findPreference("eventMobileCellsDualSIMInfo");
                    //if (preference != null)
                    //    preference.setEnabled(enabled && sim1Exists && sim2Exists);
                    preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_FOR_SIM_CARD);
                    if (preference != null)
                        preference.setEnabled(enabled && sim1Exists && sim2Exists);
                }
                else {
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
//            PPApplication.logE("EventPreferencesMobileCells.doHandleEvent", "xxxxxxxx");

            int oldSensorPassed = getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning) {
                    //if (forRestartEvents)
                    //    mobileCellPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesMobileCells.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else
                    // not allowed for disabled mobile cells scanner
                    //    notAllowedMobileCell = true;
                    eventsHandler.mobileCellPassed = false;
                } else {
                    //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn) {
                        if (forRestartEvents)
                            eventsHandler.mobileCellPassed = (EventPreferences.SENSOR_PASSED_PASSED & getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                        else
                            // not allowed for screen Off
                            eventsHandler.notAllowedMobileCell = true;
                    } else {
                        synchronized (PPApplication.mobileCellsScannerMutex) {
                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isMobileCellsScannerStarted()) {

                                MobileCellsScanner scanner = PhoneProfilesService.getInstance().getMobileCellsScanner();

                                int simCount = 0;

                                TelephonyManager telephonyManager = (TelephonyManager)eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                                if (telephonyManager != null) {
                                    if (Build.VERSION.SDK_INT >= 26)
                                        simCount = telephonyManager.getPhoneCount();
                                    else
                                        simCount = 1;
                                }

                                boolean cellIsValid = false;
                                if ((Build.VERSION.SDK_INT >= 26) && (simCount > 1)) {
                                    if ((_forSIMCard == 0) || (_forSIMCard == 1)) {
                                        int registeredCell = scanner.getRegisteredCell(1);
//                                        PPApplication.logE("EventPreferencesMobileCells.doHandleEvent", "SIM 1 registeredCell="+registeredCell);
                                        if (MobileCellsScanner.isValidCellId(registeredCell)) {
                                            String sRegisteredCell = Integer.toString(registeredCell);
                                            String[] splits = _cells.split("\\|");
                                            if (_whenOutside) {
                                                // all mobile cells must not be registered
                                                eventsHandler.mobileCellPassed = true;
                                                for (String cell : splits) {
                                                    if (cell.equals(sRegisteredCell)) {
                                                        // one of cells in configuration is registered
                                                        eventsHandler.mobileCellPassed = false;
                                                        break;
                                                    }
                                                }
                                            } else {
                                                // one mobile cell must be registered
                                                eventsHandler.mobileCellPassed = false;
                                                for (String cell : splits) {
                                                    if (cell.equals(sRegisteredCell)) {
                                                        // one of cells in configuration is registered
                                                        eventsHandler.mobileCellPassed = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            cellIsValid = true;
                                        }
                                    }

                                    if (((_forSIMCard == 0) && ((!cellIsValid) || (!eventsHandler.mobileCellPassed))) ||
                                            (_forSIMCard == 2)) {
                                        int registeredCell = scanner.getRegisteredCell(2);
//                                        PPApplication.logE("EventPreferencesMobileCells.doHandleEvent", "SIM 2 registeredCell="+registeredCell);
                                        if (MobileCellsScanner.isValidCellId(registeredCell)) {
                                            String sRegisteredCell = Integer.toString(registeredCell);
                                            String[] splits = _cells.split("\\|");
                                            if (_whenOutside) {
                                                // all mobile cells must not be registered
                                                eventsHandler.mobileCellPassed = true;
                                                for (String cell : splits) {
                                                    if (cell.equals(sRegisteredCell)) {
                                                        // one of cells in configuration is registered
                                                        eventsHandler.mobileCellPassed = false;
                                                        break;
                                                    }
                                                }
                                            } else {
                                                // one mobile cell must be registered
                                                eventsHandler.mobileCellPassed = false;
                                                for (String cell : splits) {
                                                    if (cell.equals(sRegisteredCell)) {
                                                        // one of cells in configuration is registered
                                                        eventsHandler.mobileCellPassed = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            cellIsValid = true;
                                        }
                                    }
                                }
                                else {
                                    int registeredCell = scanner.getRegisteredCell(0);
                                    if (MobileCellsScanner.isValidCellId(registeredCell)) {
                                        String sRegisteredCell = Integer.toString(registeredCell);
                                        String[] splits = _cells.split("\\|");
                                        if (_whenOutside) {
                                            // all mobile cells must not be registered
                                            eventsHandler.mobileCellPassed = true;
                                            for (String cell : splits) {
                                                if (cell.equals(sRegisteredCell)) {
                                                    // one of cells in configuration is registered
                                                    eventsHandler.mobileCellPassed = false;
                                                    break;
                                                }
                                            }
                                        } else {
                                            // one mobile cell must be registered
                                            eventsHandler.mobileCellPassed = false;
                                            for (String cell : splits) {
                                                if (cell.equals(sRegisteredCell)) {
                                                    // one of cells in configuration is registered
                                                    eventsHandler.mobileCellPassed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        cellIsValid = true;
                                    }
                                }

                                if (!cellIsValid)
                                    eventsHandler.notAllowedMobileCell = true;

                            } else
                                eventsHandler.notAllowedMobileCell = true;
                        }
                    }
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
                //PPApplication.logE("[TEST BATTERY] EventPreferencesMobileCells.doHandleEvent", "mobile cells - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_MOBILE_CELLS);
            }
        }
    }

}
