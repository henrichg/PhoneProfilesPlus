package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class ScreenOnOffBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "screenOnOff";

    @Override
    public void onReceive(Context context, Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("##### ScreenOnOffBroadcastReceiver.onReceive", "xxx");

        if (!GlobalData.getApplicationStarted(context, true))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            GlobalData.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            GlobalData.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");
            GlobalData.setScreenUnlocked(context, false);
            if (!GlobalData.getGlobalEventsRuning(context)) {
                DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
                //dataWrapper.getActivateProfileHelper().removeNotification();
                //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                Profile activatedProfile = dataWrapper.getActivatedProfile();
                dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                dataWrapper.invalidateDataWrapper();
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            GlobalData.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
            GlobalData.setScreenUnlocked(context, true);

            if (GlobalData.getApplicationStarted(context, true)) {
                if (GlobalData.notificationShowInStatusBar &&
                    GlobalData.notificationHideInLockscreen) {
                    DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                    dataWrapper.invalidateDataWrapper();
                }
            }

            // enable/disable keyguard
            Intent keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class);
            context.startService(keyguardService);
            return;
        }

        if (GlobalData.getGlobalEventsRuning(context)) {

            /*DataWrapper dataWrapper = new DataWrapper(context, false, false, 0); */

            /*boolean screenEventsExists = false;

            screenEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SCREEN) > 0;
            GlobalData.logE("ScreenOnOffBroadcastReceiver.onReceive","screenEventsExists="+screenEventsExists);
            dataWrapper.invalidateDataWrapper();
            */

            //if (screenEventsExists*/)
            //{
            // start service
            Intent eventsServiceIntent = new Intent(context, EventsService.class);
            eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            startWakefulService(context, eventsServiceIntent);
            //}

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                if (GlobalData.applicationEventWifiRescan.equals(GlobalData.RESCAN_TYPE_SCREEN_ON) ||
                        GlobalData.applicationEventWifiRescan.equals(GlobalData.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0) {
                        // send broadcast for one wifi scan
                        WifiScanAlarmBroadcastReceiver.setAlarm(context, true, true);
                        //WifiScanAlarmBroadcastReceiver.sendBroadcast(context);
                    }
                }
                if (GlobalData.applicationEventBluetoothRescan.equals(GlobalData.RESCAN_TYPE_SCREEN_ON) ||
                        GlobalData.applicationEventBluetoothRescan.equals(GlobalData.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0) {
                        // send broadcast for one bluetooth scan
                        BluetoothScanAlarmBroadcastReceiver.setAlarm(context, true, true);
                        //BluetoothScanAlarmBroadcastReceiver.sendBroadcast(context);
                    }
                }
                if (GlobalData.applicationEventLocationRescan.equals(GlobalData.RESCAN_TYPE_SCREEN_ON) ||
                        GlobalData.applicationEventLocationRescan.equals(GlobalData.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
                        // send broadcast for location scan
                        GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, true, true);
                        //GeofenceScannerAlarmBroadcastReceiver.sendBroadcast(context);
                    }
                }
                if (GlobalData.applicationEventMobileCellsRescan.equals(GlobalData.RESCAN_TYPE_SCREEN_ON) ||
                        GlobalData.applicationEventMobileCellsRescan.equals(GlobalData.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                        // rescan mobile cells
                        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateStarted()) {
                            PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                        }
                    }
                }
                dataWrapper.invalidateDataWrapper();
            }

        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if (GlobalData.getApplicationStarted(context, true)) {
                if (GlobalData.notificationShowInStatusBar &&
                    GlobalData.notificationHideInLockscreen) {
                    DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
                    //dataWrapper.getActivateProfileHelper().removeNotification();
                    //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                    dataWrapper.invalidateDataWrapper();
                }
            }
        }

    }

}
