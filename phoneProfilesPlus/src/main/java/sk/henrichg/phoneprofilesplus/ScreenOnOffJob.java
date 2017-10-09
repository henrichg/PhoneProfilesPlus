package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ScreenOnOffJob extends Job {

    static final String JOB_TAG  = "ScreenOnOffJob";

    private static final String EXTRA_ACTION = "action";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "ScreenOnOffJob.onRunJob", "ScreenOnOffJob_onRunJob");

        Bundle bundle = params.getTransientExtras();
        String action = bundle.getString(EXTRA_ACTION, "");

        if (action != null) {
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "screen on");
                PPApplication.restartWifiScanner(appContext);
                PPApplication.restartBluetoothScanner(appContext);
                PPApplication.restartGeofenceScanner(appContext);
                PPApplication.restartPhoneStateScanner(appContext);
                PPApplication.restartOrientationScanner(appContext);
            }
            else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "screen off");

                PPApplication.restartWifiScanner(appContext);
                PPApplication.restartBluetoothScanner(appContext);
                PPApplication.restartGeofenceScanner(appContext);
                PPApplication.restartPhoneStateScanner(appContext);
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
            } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "screen unlock");
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
                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "screenTimeout=" + screenTimeout);
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

                return Result.SUCCESS;
            }

            if (Event.getGlobalEventsRunning(appContext)) {

                    /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0); */

                    /*boolean screenEventsExists = false;

                    screenEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SCREEN) > 0;
                    PPApplication.logE("ScreenOnOffJob.onRunJob","screenEventsExists="+screenEventsExists);
                    dataWrapper.invalidateDataWrapper();
                    */

                //if (screenEventsExists*/)
                //{
                // start job
                EventsHandlerJob.startForSensor(EventsHandler.SENSOR_TYPE_SCREEN);
                //}

                /* Not needed for SCREEN_ON are restarted all scanners
                if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    if (ApplicationPreferences.applicationEventWifiRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                            ApplicationPreferences.applicationEventWifiRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                        PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "start of wifi scanner");
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.scheduleWifiJob(true, true, true, true, false);
                    }
                    if (ApplicationPreferences.applicationEventBluetoothRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                            ApplicationPreferences.applicationEventBluetoothRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                        PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "start of bluetooth scanner");
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.scheduleBluetoothJob(true, true, true, true);
                    }
                    if (ApplicationPreferences.applicationEventLocationRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                            ApplicationPreferences.applicationEventLocationRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                        PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "start of location scanner");
                        if (PhoneProfilesService.instance != null)
                            PhoneProfilesService.instance.scheduleGeofenceScannerJob(true, true, true, true);
                    }
                    if (ApplicationPreferences.applicationEventMobileCellsRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON) ||
                            ApplicationPreferences.applicationEventMobileCellsRescan(appContext).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                        if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                            // rescan mobile cells
                            if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateScannerStarted()) {
                                PPApplication.logE("@@@ ScreenOnOffJob.onRunJob", "start of mobile cells scanner");
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
        }
        
        return Result.SUCCESS;
    }

    static void start(String action) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, action);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }
    
}
