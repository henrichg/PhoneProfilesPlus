package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class RadioSwitchBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "radioSwitch";

    //private static ContentObserver smsObserver;
    //private static ContentObserver mmsObserver;
    //private static int mmsCount;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### RadioSwitchBroadcastReceiver.onReceive", "xxx");

        int radioSwitchType = intent.getIntExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_TYPE, 0);
        boolean radioSwitchState = intent.getBooleanExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_STATE, false);

        PPApplication.logE("  RadioSwitchBroadcastReceiver.onReceive", "radioSwitchType="+radioSwitchType);
        PPApplication.logE("  RadioSwitchBroadcastReceiver.onReceive", "radioSwitchState="+radioSwitchState);

        startService(context, radioSwitchType, radioSwitchState);
    }

    private static void startService(Context context, int radioSwitchType, boolean radioSwitchState)
    {
        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        if (PPApplication.getGlobalEventsRuning(context))
        {
            PPApplication.logE("@@@ RadioSwitchBroadcastReceiver.startService","xxx");

            /*boolean smsEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            smsEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SMS) > 0;
            PPApplication.logE("SMSBroadcastReceiver.onReceive","smsEventsExists="+smsEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (smsEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(PPApplication.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                eventsServiceIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_TYPE, radioSwitchType);
                eventsServiceIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_STATE, radioSwitchState);
                startWakefulService(context, eventsServiceIntent);
            //}
        }
    }

}
