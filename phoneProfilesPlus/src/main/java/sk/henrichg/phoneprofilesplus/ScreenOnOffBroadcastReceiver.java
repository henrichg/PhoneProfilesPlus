package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.WakefulBroadcastReceiver;

public class ScreenOnOffBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "screenOnOff";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### ScreenOnOffBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        boolean lockDeviceEnabled = false;
        if (PPApplication.lockDeviceActivity != null) {
            lockDeviceEnabled = true;
            PPApplication.lockDeviceActivity.finish();
            PPApplication.lockDeviceActivity.overridePendingTransition(0, 0);
        }

        //PPApplication.loadPreferences(context);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");

            ActivateProfileHelper.setScreenUnlocked(context, false);
            if (!Event.getGlobalEventsRuning(context)) {
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
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
            ActivateProfileHelper.setScreenUnlocked(context, true);

            DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
            Profile activatedProfile = dataWrapper.getActivatedProfile();

            if (ApplicationPreferences.notificationShowInStatusBar(context) &&
                    ApplicationPreferences.notificationHideInLockscreen(context)) {
                //dataWrapper.getActivateProfileHelper().removeNotification();
                //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
            }

            // change screen timeout
            if (lockDeviceEnabled && Permissions.checkLockDevice(context))
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
            int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(context);
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screenTimeout="+screenTimeout);
            if (screenTimeout > 0)
                dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);

            dataWrapper.invalidateDataWrapper();

            // enable/disable keyguard
            Intent keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class);
            context.startService(keyguardService);

            return;
        }

        if (Event.getGlobalEventsRuning(context)) {

            /*DataWrapper dataWrapper = new DataWrapper(context, false, false, 0); */

            /*boolean screenEventsExists = false;

            screenEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SCREEN) > 0;
            PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","screenEventsExists="+screenEventsExists);
            dataWrapper.invalidateDataWrapper();
            */

            //if (screenEventsExists*/)
            //{
            // start service
            Intent eventsServiceIntent = new Intent(context, EventsService.class);
            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            startWakefulService(context, eventsServiceIntent);
            //}

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                if (ApplicationPreferences.applicationEventWifiRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                        ApplicationPreferences.applicationEventWifiRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0) {
                        // send broadcast for one wifi scan
                        WifiScanAlarmBroadcastReceiver.setAlarm(context, true, true);
                        //WifiScanAlarmBroadcastReceiver.sendBroadcast(context);
                    }
                }
                if (ApplicationPreferences.applicationEventBluetoothRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                        ApplicationPreferences.applicationEventBluetoothRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0) {
                        // send broadcast for one bluetooth scan
                        BluetoothScanAlarmBroadcastReceiver.setAlarm(context, true, true);
                        //BluetoothScanAlarmBroadcastReceiver.sendBroadcast(context);
                    }
                }
                if (ApplicationPreferences.applicationEventLocationRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                        ApplicationPreferences.applicationEventLocationRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
                        // send broadcast for location scan
                        GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, true, true);
                        //GeofenceScannerAlarmBroadcastReceiver.sendBroadcast(context);
                    }
                }
                if (ApplicationPreferences.applicationEventMobileCellsRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                        ApplicationPreferences.applicationEventMobileCellsRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
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
            if (ApplicationPreferences.notificationShowInStatusBar(context) &&
                    ApplicationPreferences.notificationHideInLockscreen(context)) {
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
