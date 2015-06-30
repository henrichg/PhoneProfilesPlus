package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

import java.util.Arrays;

public class EventPreferencesProfile extends EventPreferences {

    public long _profileId;

    static final String PREF_EVENT_PROFILE_ENABLED = "eventProfileEnabled";
    static final String PREF_EVENT_PROFILE_PROFILE_ID = "eventProfileProfileId";

    public EventPreferencesProfile(Event event,
                                   boolean enabled,
                                   long profileId)
    {
        super(event, enabled);

        this._profileId = profileId;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesProfile)fromEvent._eventPreferencesProfile)._enabled;
        this._profileId = ((EventPreferencesProfile)fromEvent._eventPreferencesProfile)._profileId;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_PROFILE_ENABLED, _enabled);
        editor.putLong(PREF_EVENT_PROFILE_PROFILE_ID, this._profileId);
        editor.commit();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_PROFILE_ENABLED, false);
        this._profileId = preferences.getLong(PREF_EVENT_PROFILE_PROFILE_ID, 0);
    }

    @Override
    public String getPreferencesDescription(String description, Context context)
    {
        String descr = description;

        if (!this._enabled)
        {
            //descr = descr + context.getString(R.string.event_type_profile) + ": ";
            //descr = descr + context.getString(R.string.event_preferences_not_enabled);
        }
        else
        {
            descr = descr + "\u2022 " + context.getString(R.string.event_type_profile) + ": ";
            DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
            Profile profile = dataWrapper.getProfileById(_profileId, false);
            if (profile == null)
                descr = descr + context.getString(R.string.profile_preference_profile_not_set);
            else
                descr = descr + profile.getProfileNameWithDuration();
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, Context context)
    {
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context)
    {
    }

    @Override
    public boolean activateReturnProfile()
    {
        return _profileId > 0;
    }

    @Override
    public void setSystemRunningEvent(Context context)
    {
    }

    @Override
    public void setSystemPauseEvent(Context context)
    {
    }

    @Override
    public void removeSystemEvent(Context context)
    {
    }

}
