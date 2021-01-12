package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.preference.PreferenceManager;

abstract class EventPreferences {

    final Event _event;
    boolean _enabled;
    private int _sensorPassed;

    static final int SENSOR_PASSED_NOT_PASSED = 0x0;
    static final int SENSOR_PASSED_PASSED = 0x1;
    static final int SENSOR_PASSED_WAITING = 0x2;

    /*EventPreferences()
    {
        _enabled = false;
    }*/

    EventPreferences(Event event, boolean enabled)
    {
        //this();
        _event = event;
        _enabled = enabled;
        _sensorPassed = SENSOR_PASSED_NOT_PASSED;
    }

    //abstract void copyPreferences(Event fromEvent);

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

    //abstract void loadSharedPreferences(SharedPreferences preferences);

    //abstract void saveSharedPreferences(SharedPreferences preferences);

    //abstract String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context);

    int getSensorPassed()
    {
        return _sensorPassed;
    }

    void setSensorPassed(int sensorPassed)
    {
        _sensorPassed = sensorPassed;
    }

    private int getSensorPassedFromDB(int eventType, Context context)
    {
        _sensorPassed = DatabaseHandler.getInstance(context).getEventSensorPassed(this, eventType);
        return _sensorPassed;
    }

    String getPassStatusString(String sensorTitle, boolean addPassStatus, int eventType, Context context) {
        if (Event.getGlobalEventsRunning() && addPassStatus && (this._event != null) && (this._event.getStatusFromDB(context) != Event.ESTATUS_STOP)) {
            //Log.e("EventPreferences.getPassStatusString", "_event="+_event._name + "->_sensorPassed="+this._sensorPassed);
            int sensorPassed = getSensorPassedFromDB(eventType, context);
            if (/*(!Event.getGlobalEventsRunning()) ||*/ (sensorPassed & SENSOR_PASSED_WAITING) == SENSOR_PASSED_WAITING) {
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_WAITING, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font>:", colorString, sensorTitle);
            }
            if ((sensorPassed & SENSOR_PASSED_PASSED) == SENSOR_PASSED_PASSED) {
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_PASSED, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font>:", colorString, sensorTitle);
            }
            else {
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_NOT_PASSED, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font>:", colorString, sensorTitle);
            }
        }
        else {
            //int labelColor = GlobalGUIRoutines.getThemeWhiteTextColor(context);
            //String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
            //return String.format("<font color=\"#%s\">%s</font> ", colorString, sensorTitle);
            return sensorTitle+":";
        }
    }

    //abstract void setSummary(PreferenceManager prefMng, String key, String value, Context context);

    //abstract void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context);

    //abstract void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context);

    //abstract void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context);

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

    //abstract void doHandleEvent(EventsHandler eventsHandler, boolean forRestartEvents);

}
