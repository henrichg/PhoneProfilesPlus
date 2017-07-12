package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class RestartEventsFromNotificationActivity extends Activity
{

    private DataWrapper dataWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        PPApplication.logE("RestartEventsFromNotificationActivity.onCreate", "xxx");

        //PPApplication.loadPreferences(getApplicationContext());

        // close notification drawer - broadcast pending intent not close it :-/
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);

        dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        // not working on Android 2.3.x
        GlobalGUIRoutines.setTheme(this, true, false);
        GlobalGUIRoutines.setLanguage(getBaseContext());

        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

        PPApplication.logE("RestartEventsFromNotificationActivity.onStart", "xxx");
        dataWrapper.restartEventsWithAlert(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

}
