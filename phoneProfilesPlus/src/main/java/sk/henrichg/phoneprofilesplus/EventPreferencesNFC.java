package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.Html;

import java.util.Arrays;

public class EventPreferencesNFC extends EventPreferences {

    public String _nfcTags;

    static final String PREF_EVENT_NFC_ENABLED = "eventNFCEnabled";
    static final String PREF_EVENT_NFC_NFC_TAGS = "eventNFCTags";

    static final String PREF_EVENT_NFC_CATEGORY = "eventNFCCategory";

    public EventPreferencesNFC(Event event,
                               boolean enabled,
                               String nfcTags)
    {
        super(event, enabled);
        _nfcTags = nfcTags;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesNFC)fromEvent._eventPreferencesNFC)._enabled;
        this._nfcTags = ((EventPreferencesNFC)fromEvent._eventPreferencesNFC)._nfcTags;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_NFC_ENABLED, _enabled);
        editor.putString(PREF_EVENT_NFC_NFC_TAGS, _nfcTags);
        editor.commit();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_NFC_ENABLED, false);
        this._nfcTags = preferences.getString(PREF_EVENT_NFC_NFC_TAGS, "");
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled)
        {
            //descr = descr + context.getString(R.string.event_type_screen) + ": ";
            //descr = descr + context.getString(R.string.event_preferences_not_enabled);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_nfc) + ": " + "</b>";

                String selectedNfcTags = context.getString(R.string.event_preferences_nfc_nfcTags) + ": ";
                String[] splits = this._nfcTags.split("\\|");
                for (String _tag : splits) {
                    if (_tag.isEmpty()) {
                        selectedNfcTags = selectedNfcTags + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    }
                    else
                    if (splits.length == 1) {
                        selectedNfcTags = selectedNfcTags + _tag;
                    }
                    else {
                        selectedNfcTags = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedNfcTags = selectedNfcTags + " " + splits.length;
                        break;
                    }
                }
                descr = descr + selectedNfcTags;
            }
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_NFC_NFC_TAGS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String[] splits = value.split("\\|");
                for (String _tag : splits) {
                    if (_tag.isEmpty()) {
                        preference.setSummary(R.string.applications_multiselect_summary_text_not_selected);
                    }
                    else
                    if (splits.length == 1) {
                        preference.setSummary(_tag);
                    }
                    else {
                        String selectedNfcTags = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedNfcTags = selectedNfcTags + " " + splits.length;
                        preference.setSummary(selectedNfcTags);
                        break;
                    }
                }
                GUIData.setPreferenceTitleStyle(preference, false, true, false);
            }
        }
        /*if (key.equals(PREF_EVENT_SCREEN_EVENT_TYPE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }*/
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_NFC_NFC_TAGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NFC_NFC_TAGS, preferences, context);

        if (GlobalData.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, context)
                != GlobalData.PREFERENCE_ALLOWED)
        {
            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_ENABLED);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_NFC_NFC_TAGS);
            if (preference != null) preference.setEnabled(false);
        }
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (GlobalData.isEventPreferenceAllowed(PREF_EVENT_NFC_ENABLED, context) == GlobalData.PREFERENCE_ALLOWED) {
            EventPreferencesNFC tmp = new EventPreferencesNFC(this._event, this._enabled, this._nfcTags);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_CATEGORY);
            if (preference != null) {
                GUIData.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context));
                preference.setSummary(Html.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context)
    {
        /*
        final Preference eventTypePreference = prefMng.findPreference(PREF_EVENT_SCREEN_EVENT_TYPE);
        final PreferenceManager _prefMng = prefMng;

        if (eventTypePreference != null) {
            eventTypePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String) newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 100;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    setWhenUnlockedTitle(_prefMng, iNewValue);

                    return true;
                }
            });
        }
        */
    }

    /*
    private void setWhenUnlockedTitle(PreferenceManager prefMng, int value)
    {
        final CheckBoxPreference whenUnlockedPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_SCREEN_WHEN_UNLOCKED);

        if (whenUnlockedPreference != null) {
            if (value == 0)
                whenUnlockedPreference.setTitle(R.string.event_preferences_screen_start_when_unlocked);
            else
                whenUnlockedPreference.setTitle(R.string.event_preferences_screen_end_when_unlocked);
        }
    }
    */

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

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

}
