package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.android.internal.graphics.ColorUtils;

class EventPreferencesMobileCells extends EventPreferences {

    String _cells;
    boolean _whenOutside;

    boolean _sensorPassed;

    static final String PREF_EVENT_MOBILE_CELLS_ENABLED = "eventMobileCellsEnabled";
    static final String PREF_EVENT_MOBILE_CELLS_CELLS = "eventMobileCellsCells";
    private static final String PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE = "eventMobileCellsStartWhenOutside";
    static final String PREF_EVENT_MOBILE_CELLS_REGISTRATION = "eventMobileCellsRegistration";
    private static final String PREF_EVENT_MOBILE_CELLS_APP_SETTINGS = "eventMobileCellsScanningAppSettings";

    private static final String PREF_EVENT_MOBILE_CELLS_CATEGORY = "eventMobileCellsCategory";

    //private DataWrapper dataWrapper = null;

    EventPreferencesMobileCells(Event event,
                                       boolean enabled,
                                       String cells,
                                       boolean _whenOutside)
    {
        super(event, enabled);

        this._cells = cells;
        this._whenOutside = _whenOutside;

        this._sensorPassed = false;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesMobileCells._enabled;
        this._cells = fromEvent._eventPreferencesMobileCells._cells;
        this._whenOutside = fromEvent._eventPreferencesMobileCells._whenOutside;

        this._sensorPassed = false;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, _enabled);
        editor.putString(PREF_EVENT_MOBILE_CELLS_CELLS, this._cells);
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, this._whenOutside);
        editor.putString(PREF_EVENT_MOBILE_CELLS_REGISTRATION, Long.toString(_event._id));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
        this._cells  = preferences.getString(PREF_EVENT_MOBILE_CELLS_CELLS, "0");
        this._whenOutside = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, false);
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_mobile_cells_summary);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_mobile_cells) + ": " + "</b>";
            }

            String selectedCells = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            if (!this._cells.isEmpty()) {
                String[] splits = this._cells.split("\\|");
                selectedCells = context.getString(R.string.applications_multiselect_summary_text_selected);
                selectedCells = selectedCells + " " + splits.length;
            }
            descr = descr + selectedCells;
            if (this._whenOutside)
                descr = descr + "; " + context.getString(R.string.event_preferences_mobile_cells_when_outside_description);
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_MOBILE_CELLS_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning(context)) {
                    summary = context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabled) + "\n" +
                            context.getResources().getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
                    titleColor = Color.RED; //0xFFffb000;
                }
                else {
                    summary = context.getResources().getString(R.string.phone_profiles_pref_eventMobileCellsAppSettings_summary);
                    titleColor = ColorUtils.setAlphaComponent(GlobalGUIRoutines.getThemeTextColor(context), 0xFF);
                }
                Spannable title = new SpannableString(context.getResources().getString(R.string.phone_profiles_pref_category_location));
                title.setSpan(new ForegroundColorSpan(titleColor), 0, title.length(), 0);
                preference.setTitle(title);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_MOBILE_CELLS_CELLS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                /*if (!ApplicationPreferences.applicationEventMobileCellEnableScannig(context.getApplicationContext())) {
                    preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+context.getResources().getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
                }
                else {*/
                    if (value.isEmpty())
                        preference.setSummary(R.string.applications_multiselect_summary_text_not_selected);
                    else {
                        String[] splits = value.split("\\|");
                        String selectedCells = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedCells = selectedCells + " " + splits.length;
                        preference.setSummary(selectedCells);
                    }
                //}
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_MOBILE_CELLS_CELLS) ||
            key.equals(PREF_EVENT_MOBILE_CELLS_APP_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELLS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_APP_SETTINGS, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_MOBILE_CELLS_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesMobileCells tmp = new EventPreferencesMobileCells(this._event,
                    this._enabled, this._cells, this._whenOutside);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CATEGORY);
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

        runnable = runnable && (!_cells.isEmpty());

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        //final boolean enabled = ApplicationPreferences.applicationEventMobileCellEnableScannig(context.getApplicationContext());
        //Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CELLS);
        //if (preference != null) preference.setEnabled(enabled);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        //setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELLS, preferences, context);
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_APP_SETTINGS, preferences, context);
        setCategorySummary(prefMng, preferences, context);
    }

    /*
    @Override
    public void setSystemEventForStart(Context context)
    {
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
    }

    @Override
    public void removeSystemEvent(Context context)
    {
    }
    */
}
