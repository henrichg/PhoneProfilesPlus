package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class EventPreferences {

    Event _event;
    boolean _enabled;
    private int _sensorPassed;

    static final int SENSOR_PASSED_NOT_PASSED = 0x0;
    static final int SENSOR_PASSED_PASSED = 0x1;
    static final int SENSOR_PASSED_WAITING = 0x2;

    EventPreferences()
    {
        _enabled = false;
    }

    EventPreferences(Event event, boolean enabled)
    {
        _event = event;
        _enabled = enabled;
        _sensorPassed = SENSOR_PASSED_NOT_PASSED;
    }

    @SuppressWarnings("unused")
    void copyPreferences(Event fromEvent)
    {
    }

    boolean isRunnable(Context context)
    {
        return true;
    }

    int isAccessibilityServiceEnabled(Context context)
    {
        return 1;
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
    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        return "";
    }

    public int getSensorPassed()
    {
        return _sensorPassed;
    }

    public void setSensorPassed(int sensorPassed)
    {
        _sensorPassed = sensorPassed;
    }

    private int getSensorPassedFromDB(int eventType, Context context)
    {
        _sensorPassed = DatabaseHandler.getInstance(context).getEventSensorPassed(this, eventType);
        return _sensorPassed;
    }

    String getPassStatusString(String sensorTitle, boolean addPassStatus, int eventType, Context context) {
        if (addPassStatus && (this._event != null) && (this._event.getStatusFromDB(context) != Event.ESTATUS_STOP)) {
            //Log.e("EventPreferences.getPassStatusString", "_event="+_event._name + "->_sensorPassed="+this._sensorPassed);
            int sensorPassed = getSensorPassedFromDB(eventType, context);
            if ((sensorPassed & SENSOR_PASSED_WAITING) == SENSOR_PASSED_WAITING) {
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_WAITING, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font> ", colorString, sensorTitle);
            }
            if ((sensorPassed & SENSOR_PASSED_PASSED) == SENSOR_PASSED_PASSED) {
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_PASSED, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font> ", colorString, sensorTitle);
            }
            else {
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_NOT_PASSED, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font> ", colorString, sensorTitle);
            }
        }
        else
            return sensorTitle;
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
