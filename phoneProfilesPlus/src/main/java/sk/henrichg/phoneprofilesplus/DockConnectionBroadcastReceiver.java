package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DockConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] DockConnectionBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context))
        {
            /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            boolean accessoryEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ACCESSORY) > 0;
            dataWrapper.invalidateDataWrapper();

            if (accessoryEventsExists)
            {*/
                final Context appContext = context.getApplicationContext();
                PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_DOCK_CONNECTION, PPExecutors.SENSOR_NAME_SENSOR_TYPE_DOCK_CONNECTION, 0);
            //}

        }
    }
}
