package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;

public class EventPreferencesMobileCells extends EventPreferences {

    public int _cellId;
    public boolean _whenOutside;

    static final String PREF_EVENT_MOBILE_CELLS_ENABLED = "eventMobileCellsEnabled";
    static final String PREF_EVENT_MOBILE_CELLS_CELL_ID = "eventMobileCellsCellId";
    static final String PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE = "eventMobileCellsStartWhenOutside";

    static final String PREF_EVENT_MOBILE_CELLS_CATEGORY = "eventMobileCellsCategory";

    private DataWrapper dataWrapper = null;

    public EventPreferencesMobileCells(Event event,
                                       boolean enabled,
                                       int cellId,
                                       boolean _whenOutside)
    {
        super(event, enabled);

        this._cellId = cellId;
        this._whenOutside = _whenOutside;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesMobileCells)fromEvent._eventPreferencesMobileCells)._enabled;
        this._cellId = ((EventPreferencesMobileCells)fromEvent._eventPreferencesMobileCells)._cellId;
        this._whenOutside = ((EventPreferencesMobileCells)fromEvent._eventPreferencesMobileCells)._whenOutside;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, _enabled);
        editor.putString(PREF_EVENT_MOBILE_CELLS_CELL_ID, Integer.toString(this._cellId));
        editor.putBoolean(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE, this._whenOutside);
        editor.commit();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_MOBILE_CELLS_ENABLED, false);
        try {
            String sCellId = preferences.getString(PREF_EVENT_MOBILE_CELLS_CELL_ID, "0");
            int cellId = Integer.parseInt(sCellId);
            this._cellId = cellId;
        }
        catch (Exception e) {
            this._cellId = 0;
        }
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
            if (this._cellId != 0) {
                selectedCell = Integer.toString(this._cellId);
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
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (key.equals(PREF_EVENT_MOBILE_CELLS_CELL_ID)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    int iValue;
                    if (!value.isEmpty())
                        iValue = Integer.valueOf(value);
                    else
                        iValue = 0;
                    if (iValue == 0)
                        preference.setSummary(R.string.event_preferences_mobile_cells_cellId_not_selected);
                    else
                        preference.setSummary(Integer.toString(iValue));
                    GUIData.setPreferenceTitleStyle(preference, false, true, false);
                }
            }
        //}
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_MOBILE_CELLS_CELL_ID))
        {
            setSummary(prefMng, key, String.valueOf(preferences.getString(key, "0")), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_MOBILE_CELLS_CELL_ID, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        EventPreferencesMobileCells tmp = new EventPreferencesMobileCells(this._event,
                                                this._enabled, this._cellId, this._whenOutside);
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

        runable = runable && (_cellId != 0);

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        final boolean enabled = PhoneProfilesService.isLocationEnabled(context.getApplicationContext());
        Preference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_CELL_ID);
        if (preference != null) preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_WHEN_OUTSIDE);
        if (preference != null) preference.setEnabled(enabled);
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
