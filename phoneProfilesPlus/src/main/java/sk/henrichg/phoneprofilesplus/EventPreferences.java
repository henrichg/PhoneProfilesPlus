package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EventPreferences {

    public Event _event;
    public boolean _enabled;

    public EventPreferences()
    {
        _enabled = false;
    }

    public EventPreferences(Event event, boolean enabled)
    {
        _event = event;
        _enabled = enabled;
    }

    public void copyPreferences(Event fromEvent)
    {
    }

    public boolean isRunnable(Context context)
    {
        return true;
    }

    public boolean activateReturnProfile()
    {
        return true;
    }

    public void loadSharedPreferences(SharedPreferences preferences)
    {
    }

    public void saveSharedPreferences(SharedPreferences preferences)
    {
    }

    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        return "";
    }

    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
    }

    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
    }

    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
    }

    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
    }

    public void checkPreferences(PreferenceManager prefMng, Context context)
    {
    }

    public void setSystemEventForStart(Context context)
    {

    }

    public void setSystemEventForPause(Context context)
    {

    }

    public void removeSystemEvent(Context context)
    {

    }

}
