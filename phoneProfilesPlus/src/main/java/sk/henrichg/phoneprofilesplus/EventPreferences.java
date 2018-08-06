package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class EventPreferences {

    Event _event;
    boolean _enabled;
    int _sensorPassed;

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

    void copyPreferences(Event fromEvent)
    {
        _sensorPassed = SENSOR_PASSED_NOT_PASSED;
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
    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        return "";
    }

    String getPassStatusString(String sensorTitle, boolean addPassStatus, Context context) {

        //int labelColor = context.getResources().getColor(R.color.label_color);
        //String сolorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
        //Html.fromHtml(String.format("<font color=\"#%s\">text</font>", сolorString), TextView.BufferType.SPANNABLE);

        if (addPassStatus && (this._event != null) && (this._event.getStatus() != Event.ESTATUS_STOP)) {
            if ((this._sensorPassed & SENSOR_PASSED_WAITING) == SENSOR_PASSED_WAITING) {
                //int labelColor = context.getResources().getColor(R.color.sensor_pass_status_waiting);
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_WAITING, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font> ", colorString, sensorTitle);
            }
            if ((this._sensorPassed & SENSOR_PASSED_PASSED) == SENSOR_PASSED_PASSED) {
                //int labelColor = context.getResources().getColor(R.color.sensor_pass_status_passed);
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_PASSED, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font> ", colorString, sensorTitle);
            } else if ((this._sensorPassed & SENSOR_PASSED_PASSED) == SENSOR_PASSED_NOT_PASSED) {
                //int labelColor = context.getResources().getColor(R.color.sensor_pass_status_not_passed);
                int labelColor = GlobalGUIRoutines.getThemeSensorPassStatusColor(SENSOR_PASSED_NOT_PASSED, context);
                String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
                return String.format("<font color=\"#%s\">%s</font> ", colorString, sensorTitle);
            } else
                return sensorTitle;
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
