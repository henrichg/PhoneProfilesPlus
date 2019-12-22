package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;

@SuppressWarnings("unused")
class CallsCounter {

    private static SharedPreferences preferences = null;

    private static final String LOG_TAG = "CallsCounter";

    private static SharedPreferences getSharedPreferences(Context context) {
        if (preferences == null)
            preferences = context.getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences;
    }

    private static int incCounter(Context context, String counterName) {
        int counterValue = getCounterValue(context, counterName);
        ++counterValue;
        getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(counterName, counterValue);
        editor.apply();
        return counterValue;
    }

    private static int getCounterValue(Context context, String counterName) {
        return getSharedPreferences(context).getInt(counterName, 0);
    }

    static void logCounter(Context context, String text, String counterName) {
        if (PPApplication.logEnabled())
            PPApplication.logE(LOG_TAG, text + " -> counterValue="+incCounter(context, counterName));
    }

    static void logCounterNoInc(Context context, String text, String counterName) {
        if (PPApplication.logEnabled())
            PPApplication.logE(LOG_TAG, text + " -> counterValue="+getCounterValue(context, counterName));
    }

}
