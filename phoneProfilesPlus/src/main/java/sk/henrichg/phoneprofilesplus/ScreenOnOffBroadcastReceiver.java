package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### ScreenOnOffBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "ScreenOnOffBroadcastReceiver.onReceive", "ScreenOnOffBroadcastReceiver_onReceive");

        if (intent != null)
            PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());
        else
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //ScreenOnOffJob.start(appContext, intent.getAction());

        final String action = intent.getAction();

        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "before start handler");
        final Handler handler = new Handler(appContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of handler post");

                if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
                    PPApplication.restartWifiScanner(appContext, true);
                    PPApplication.restartBluetoothScanner(appContext, true);
                    PPApplication.restartGeofenceScanner(appContext, true);
                    PPApplication.restartPhoneStateScanner(appContext, true);
                    PPApplication.restartOrientationScanner(appContext, true);
                }
                else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");

                    PPApplication.restartWifiScanner(appContext, true);
                    PPApplication.restartBluetoothScanner(appContext, true);
                    PPApplication.restartGeofenceScanner(appContext, true);
                    PPApplication.restartPhoneStateScanner(appContext, true);
                    PPApplication.restartOrientationScanner(appContext, true);

                    //boolean lockDeviceEnabled = false;
                    if (PPApplication.lockDeviceActivity != null) {
                        //lockDeviceEnabled = true;
                        PPApplication.lockDeviceActivity.finish();
                        PPApplication.lockDeviceActivity.overridePendingTransition(0, 0);
                    }

                    //ActivateProfileHelper.setScreenUnlocked(appContext, false);
                    if (!Event.getGlobalEventsRunning(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        Profile activatedProfile = dataWrapper.getActivatedProfile();
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.showProfileNotification(activatedProfile, dataWrapper);
                        dataWrapper.invalidateDataWrapper();
                    }
                } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");
                    //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                    final DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                    Profile activatedProfile = dataWrapper.getActivatedProfile();

                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.showProfileNotification(activatedProfile, dataWrapper);
                    }

                    // change screen timeout
                    /*if (lockDeviceEnabled && Permissions.checkLockDevice(appContext))
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);*/
                    final int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screenTimeout=" + screenTimeout);
                    if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext))) {
                        if (PPApplication.screenTimeoutHandler != null) {
                            PPApplication.screenTimeoutHandler.post(new Runnable() {
                                public void run() {
                                    dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout, appContext);
                                    dataWrapper.invalidateDataWrapper();
                                }
                            });
                        }/* else {
                            dataWrapper.getActivateProfileHelper().setScreenTimeout(screenTimeout);
                            dataWrapper.invalidateDataWrapper();
                        }*/
                    }

                    // enable/disable keyguard
                    try {
                        // start PhoneProfilesService
                        //PPApplication.firstStartServiceStarted = false;
                        Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                        //TODO Android O
                        //if (Build.VERSION.SDK_INT < 26)
                        appContext.startService(serviceIntent);
                        //else
                        //    startForegroundService(serviceIntent);
                    } catch (Exception ignored) {}

                    return;
                }

                if (Event.getGlobalEventsRunning(appContext)) {

                    /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0); */

                    /*boolean screenEventsExists = false;

                    screenEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SCREEN) > 0;
                    PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive","screenEventsExists="+screenEventsExists);
                    dataWrapper.invalidateDataWrapper();
                    */

                    //if (screenEventsExists*/)
                    //{
                    // start job
                    //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_SCREEN);
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SCREEN, false);
                    //}

                /* Not needed for SCREEN_ON are restarted all scanners
                if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    if (ApplicationPreferences.applicationEventWifiRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                            ApplicationPreferences.applicationEventWifiRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of wifi scanner");
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.scheduleWifiJob(true, true, true, true, false);
                    }
                    if (ApplicationPreferences.applicationEventBluetoothRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                            ApplicationPreferences.applicationEventBluetoothRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of bluetooth scanner");
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.scheduleBluetoothJob(true, true, true, true);
                    }
                    if (ApplicationPreferences.applicationEventLocationRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                            ApplicationPreferences.applicationEventLocationRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of location scanner");
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.scheduleGeofenceScannerJob(true, true, true, true);
                    }
                    if (ApplicationPreferences.applicationEventMobileCellsRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                            ApplicationPreferences.applicationEventMobileCellsRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                        if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                            // rescan mobile cells
                            if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateScannerStarted()) {
                                PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of mobile cells scanner");
                                PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                            }
                        }
                        dataWrapper.invalidateDataWrapper();
                    }
                }
                */

                }

                if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                            ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
                        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                        //dataWrapper.getActivateProfileHelper().removeNotification();
                        //dataWrapper.getActivateProfileHelper().setAlarmForRecreateNotification();
                        Profile activatedProfile = dataWrapper.getActivatedProfile();
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.showProfileNotification(activatedProfile, dataWrapper);
                        dataWrapper.invalidateDataWrapper();
                    }
                }

                PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "end of handler post");

            }
        });
        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "after start handler");
    }

}
