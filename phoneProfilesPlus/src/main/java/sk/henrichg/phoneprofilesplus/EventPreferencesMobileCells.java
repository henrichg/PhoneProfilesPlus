package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;

public class EventPreferencesMobileCells extends EventPreferences {

    public String _cells;
    public boolean _whenOutside;

    static final String PREF_EVENT_MOBILE_CELLS_ENABLED = "eventMobileCellsEnabled";
    static final String PREF_EVENT_MOBILE_CELLS_CELLS = "eventMobileCellsCells";
    static final String PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE = "eventMobileCellsStartWhenOutside";

    static final String PREF_EVENT_MOBILE_CELLS_CATEGORY = "eventMobileCellsCategory";

    private DataWrapper dataWrapper = null;

    public EventPreferencesMobileCells(Event event,
                                       boolean enabled,
                                       String cells,
                                       boolean _whenOutside)
    {
        super(event, enabled);

        this._cells = cells;
        this._whenOutside = _whenOutside;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesMobileCells)fromEvent._eventPreferencesMobileCells)._enabled;
        this._cells = ((EventPreferencesMobileCells)fromEvent._eventPreferencesMobileCells)._cells;
        this._whenOutside = ((EventPreferencesMobileCells)fromEvent._eventPreferencesMobileCells)._whenOutside;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, _enabled);
        editor.putString(PREF_EVENT_MOBILE_CELLS_CELLS, this._cells);
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, this._whenOutside);
        editor.commit();
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

        if (!this._enabled)
        {
            ;
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_mobile_cells) + ": " + "</b>";
            }

            String selectedCell = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            if (!this._cells.isEmpty()) {
                selectedCell = this._cells;
            }
            descr = descr + selectedCell;
            if (this._whenOutside)
                descr = descr + "; " + context.getString(R.string.event_preferences_mobile_cells_when_outside_description);
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_MOBILE_CELLS_CELLS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (value.isEmpty())
                    preference.setSummary(R.string.event_preferences_mobile_cells_cellId_not_selected);
                else
                    preference.setSummary(value);
                GUIData.setPreferenceTitleStyle(preference, false, true, false);
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_MOBILE_CELLS_CELLS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELLS, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        EventPreferencesMobileCells tmp = new EventPreferencesMobileCells(this._event,
                                                this._enabled, this._cells, this._whenOutside);
        if (preferences != null)
            tmp.saveSharedPreferences(preferences);

        Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CATEGORY);
        if (preference != null) {
            GUIData.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context));
            preference.setSummary(Html.fromHtml(tmp.getPreferencesDescription(false, context)));
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runable = super.isRunnable(context);

        runable = runable && (!_cells.isEmpty());

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    @Override
    public void setSystemEventForStart(Context context)
    {
        if (GlobalData.phoneProfilesService != null) {
            if (_enabled && (!PhoneProfilesService.isPhoneStateStarted())) {
                GlobalData.sendMessageToService(context, PhoneProfilesService.MSG_START_PHONE_STATE_SCANNER);
            }
        }
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
    }

    @Override
    public void removeSystemEvent(Context context)
    {
    }

}
