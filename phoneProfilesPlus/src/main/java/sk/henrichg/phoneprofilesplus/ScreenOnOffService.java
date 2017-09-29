package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class ScreenOnOffService extends IntentService {

    public ScreenOnOffService() {
        super("ScreenOnOffService");
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "ScreenOnOffService.onHandleIntent", "ScreenOnOffService_onHandleIntent");

        if (intent != null) {

            final Context appContext = getApplicationContext();
            
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen on");
                    PPApplication.restartOrientationScanner(appContext);
                }
                else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen off");

                    PPApplication.restartOrientationScanner(appContext);

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
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screen unlock");
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
                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "screenTimeout=" + screenTimeout);
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
                    PPApplication.logE("ScreenOnOffService.onReceive","screenEventsExists="+screenEventsExists);
                    dataWrapper.invalidateDataWrapper();
                    */

                    //if (screenEventsExists*/)
                    //{
                    // start service
                    Intent eventsServiceIntent = new Intent(appContext, EventsHandlerService.class);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_SCREEN);
                    WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                    //}

                    if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                        if (ApplicationPreferences.applicationEventWifiRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                                ApplicationPreferences.applicationEventWifiRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                            PPApplication.logE("@@@ ScreenOnOffService.onReceive", "start of wifi scanner");
                            if (PhoneProfilesService.instance != null)
                                PhoneProfilesService.instance.scheduleWifiJob(true, false, true, true, false);
                        }
                        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                        if (ApplicationPreferences.applicationEventBluetoothRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                                ApplicationPreferences.applicationEventBluetoothRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                            PPApplication.logE("@@@ ScreenOnOffService.onReceive", "start of bluetooth scanner");
                            if (PhoneProfilesService.instance != null)
                                PhoneProfilesService.instance.scheduleBluetoothJob(true, false, true, true);
                        }
                        if (ApplicationPreferences.applicationEventLocationRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                                ApplicationPreferences.applicationEventLocationRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
                                // send broadcast for location scan
                                PPApplication.logE("@@@ ScreenOnOffService.onReceive", "start of location scanner");
                                if (PhoneProfilesService.instance != null)
                                    PhoneProfilesService.instance.scheduleGeofenceScannerJob(true, false, true, true);
                            }
                        }
                        if (ApplicationPreferences.applicationEventMobileCellsRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                                ApplicationPreferences.applicationEventMobileCellsRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                                // rescan mobile cells
                                if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateScannerStarted()) {
                                    PPApplication.logE("@@@ ScreenOnOffService.onReceive", "start of mobile cells scanner");
                                    PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                                }
                            }
                        }
                        dataWrapper.invalidateDataWrapper();
                    }

                }

                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
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
            }
        }
        
    }

}
