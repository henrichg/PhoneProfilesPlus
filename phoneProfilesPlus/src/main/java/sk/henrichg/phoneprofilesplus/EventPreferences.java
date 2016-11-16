package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class EventPreferences {

    Event _event;
    boolean _enabled;

    EventPreferences()
    {
        _enabled = false;
    }

    EventPreferences(Event event, boolean enabled)
    {
        _event = event;
        _enabled = enabled;
    }

    void copyPreferences(Event fromEvent)
    {
    }

    boolean isRunnable(Context context)
    {
        return true;
    }

    boolean activateReturnProfile()
    {
        return true;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
    }

    String getPreferencesDescription(boolean addBullet, Context context)
    {
        return "";
    }

    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
    }

    void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
    }

    void checkPreferences(PreferenceManager prefMng, Context context)
    {
    }

    void setSystemEventForStart(Context context)
    {

    }

    void setSystemEventForPause(Context context)
    {

    }

    void removeSystemEvent(Context context)
    {

    }

}
