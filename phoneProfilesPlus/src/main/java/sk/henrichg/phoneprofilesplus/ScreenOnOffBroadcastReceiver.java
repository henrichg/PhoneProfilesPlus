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

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        boolean lockDeviceEnabled = false;
        if (PPApplication.lockDeviceActivity != null) {
            lockDeviceEnabled = true;
            PPApplication.lockDeviceActivity.finish();
            PPApplication.lockDeviceActivity.overridePendingTransition(0, 0);
        }

        //PPApplication.loadPreferences(appContext);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");

            ActivateProfileHelper.setScreenUnlocked(appContext, false);
            if (!Event.getGlobalEventsRuning(appContext)) {
                DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                //dataWrapper.getActivateProfileHelper().removeNotification();
                //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                Profile activatedProfile = dataWrapper.getActivatedProfile();
                dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                dataWrapper.invalidateDataWrapper();
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
            ActivateProfileHelper.setScreenUnlocked(appContext, true);

            DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
            Profile activatedProfile = dataWrapper.getActivatedProfile();

            if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                    ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                //dataWrapper.getActivateProfileHelper().removeNotification();
                //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
            }

            // change screen timeout
            if (lockDeviceEnabled && Permissions.checkLockDevice(appContext))
                Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
            int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screenTimeout="+screenTimeout);
            if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(context)))
                dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);

            dataWrapper.invalidateDataWrapper();

            // enable/disable keyguard
            Intent keyguardService = new Intent(appContext, KeyguardService.class);
            appContext.startService(keyguardService);

            return;
        }

        if (Event.getGlobalEventsRuning(appContext)) {

            /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0); */

            /*boolean screenEventsExists = false;

            screenEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SCREEN) > 0;
            PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","screenEventsExists="+screenEventsExists);
            dataWrapper.invalidateDataWrapper();
            */

            //if (screenEventsExists*/)
            //{
            // start service
            Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            startWakefulService(appContext, eventsServiceIntent);
            //}

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                if (ApplicationPreferences.applicationEventWifiRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                        ApplicationPreferences.applicationEventWifiRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0) {
                        // send broadcast for one wifi scan
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of wifi scanner");
                        WifiScanAlarmBroadcastReceiver.setAlarm(appContext, true, true, false);
                        //WifiScanAlarmBroadcastReceiver.sendBroadcast(appContext);
                    }
                }
                if (ApplicationPreferences.applicationEventBluetoothRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                        ApplicationPreferences.applicationEventBluetoothRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0) {
                        // send broadcast for one bluetooth scan
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of bluetooth scanner");
                        BluetoothScanAlarmBroadcastReceiver.setAlarm(appContext, true, true);
                        //BluetoothScanAlarmBroadcastReceiver.sendBroadcast(appContext);
                    }
                }
                if (ApplicationPreferences.applicationEventLocationRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                        ApplicationPreferences.applicationEventLocationRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
                        // send broadcast for location scan
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of location scanner");
                        GeofenceScannerAlarmBroadcastReceiver.setAlarm(appContext, true, true);
                        //GeofenceScannerAlarmBroadcastReceiver.sendBroadcast(appContext);
                    }
                }
                if (ApplicationPreferences.applicationEventMobileCellsRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                        ApplicationPreferences.applicationEventMobileCellsRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                        // rescan mobile cells
                        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateStarted()) {
                            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of mobile cells scanner");
                            PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                        }
                    }
                }
                dataWrapper.invalidateDataWrapper();
            }

        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                    ApplicationPreferences.notificationHideInLockscreen(appContext)) {
                DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                //dataWrapper.getActivateProfileHelper().removeNotification();
                //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                Profile activatedProfile = dataWrapper.getActivatedProfile();
                dataWrapper.getActivateProfileHelper().showNotification(activatedProfile);
                dataWrapper.invalidateDataWrapper();
            }
        }

    }

}
