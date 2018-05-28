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

    @SuppressWarnings("unused")
    void copyPreferences(Event fromEvent)
    {
    }

    boolean isRunnable(Context context)
    {
        return true;
    }

    /*
    boolean activateReturnProfile()
    {
        return true;
    }
    */

    @SuppressWarnings("unused")
    void loadSharedPreferences(SharedPreferences preferences)
    {
    }

    @SuppressWarnings("unused")
    void saveSharedPreferences(SharedPreferences preferences)
    {
    }

    @SuppressWarnings("unused")
    String getPreferencesDescription(boolean addBullet, Context context)
    {
        return "";
    }

    @SuppressWarnings("unused")
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
    }

    @SuppressWarnings("unused")
    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
    }

    @SuppressWarnings("unused")
    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
    }

    @SuppressWarnings("unused")
    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
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
