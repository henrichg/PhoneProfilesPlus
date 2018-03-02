package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        CallsCounter.logCounter(context, "PackageReplacedReceiver.onReceive", "PackageReplacedReceiver_onReceive");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

            //PackageReplacedJob.start(context.getApplicationContext());

            final Context appContext = context.getApplicationContext();

            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PackageReplacedReceiver.onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    // if startedOnBoot = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
                    PPApplication.startedOnBoot = true;
                    PPApplication.startHandlerThread();
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PackageReplacedReceiver.onReceive", "delayed boot up");
                            PPApplication.startedOnBoot = false;
                        }
                    }, 10000);

                    Permissions.setShowRequestAccessNotificationPolicyPermission(appContext, true);
                    Permissions.setShowRequestWriteSettingsPermission(appContext, true);
                    WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true);
                    //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                    int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
                    PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "oldVersionCode="+oldVersionCode);
                    int actualVersionCode;
                    try {
                        PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                        actualVersionCode = pInfo.versionCode;
                        PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "actualVersionCode=" + actualVersionCode);

                        if (oldVersionCode < actualVersionCode) {
                            if (actualVersionCode <= 2322) {
                                // for old packages use Priority in events
                                ApplicationPreferences.getSharedPreferences(appContext);
                                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "applicationEventUsePriority=true");
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                                editor.apply();
                            }
                            if (actualVersionCode <= 2400) {
                                PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "donation alarm restart");
                                PPApplication.setDaysAfterFirstStart(appContext, 0);
                                PPApplication.setDonationNotificationCount(appContext, 0);
                                AboutApplicationJob.scheduleJob(appContext, true);
                            }
                            if (actualVersionCode <= 2500) {
                                // for old packages hide profile notification from status bar if notification is disabled
                                ApplicationPreferences.getSharedPreferences(appContext);
                                if (!ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true)) {
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "notificationShowInStatusBar=false");
                                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                                    editor.apply();
                                }
                            }
                            if (actualVersionCode <= 2700) {
                                ApplicationPreferences.getSharedPreferences(appContext);
                                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();

                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);

                                editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                                editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                                editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, false);
                                editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS_SAVE, false);
                                editor.putBoolean(EventPreferencesActivity.PREF_START_TARGET_HELPS, false);
                                editor.apply();
                            }
                            if (actualVersionCode <= 3200) {
                                ApplicationPreferences.getSharedPreferences(appContext);
                                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, true);
                                editor.apply();
                            }
                            if (actualVersionCode <= 3500) {
                                ApplicationPreferences.getSharedPreferences(appContext);
                                if (!ApplicationPreferences.preferences.contains(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT)) {
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, ApplicationPreferences.applicationActivateWithAlert(appContext));

                                    String rescan;
                                    rescan = ApplicationPreferences.applicationEventLocationRescan(appContext);
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "3");
                                    rescan = ApplicationPreferences.applicationEventWifiRescan(appContext);
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "3");
                                    rescan = ApplicationPreferences.applicationEventBluetoothRescan(appContext);
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "3");
                                    rescan = ApplicationPreferences.applicationEventMobileCellsRescan(appContext);
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "3");
                                    rescan = ApplicationPreferences.applicationEventMobileCellsRescan(appContext);
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "3");
                                    editor.apply();
                                }

                                // continue donation notification
                                if (PPApplication.getDaysAfterFirstStart(appContext) == 8)
                                    PPApplication.setDonationNotificationCount(appContext, 1);
                            }

                            if (actualVersionCode <= 3900) {
                                ApplicationPreferences.getSharedPreferences(appContext);
                                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF,
                                        ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true));
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF,
                                        ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true));
                                editor.apply();
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    PPApplication.logE("PackageReplacedReceiver.onReceive","PhoneProfilesService.instance="+PhoneProfilesService.instance);

                    if (PPApplication.getApplicationStarted(appContext, false))
                    {
                        PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "start PhoneProfilesService");

                        if (PhoneProfilesService.instance != null) {
                            // stop PhoneProfilesService
                            appContext.stopService(new Intent(appContext, PhoneProfilesService.class));
                            PPApplication.sleep(2000);
                            startService(appContext);
                        }
                        else
                            startService(appContext);
                    }

                    if ((wakeLock != null) && wakeLock.isHeld())
                        wakeLock.release();
                }
            });

        }
    }

    private void startService(Context context) {
        PPApplication.logE("@@@ PackageReplacedReceiver.startService", "xxx");

        // must by false for avoiding starts/pause events before restart events
        PPApplication.setApplicationStarted(context, false);

        // start PhoneProfilesService
        Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        //TODO Android O
        //if (Build.VERSION.SDK_INT < 26)
        context.startService(serviceIntent);
        //else
        //    context.startForegroundService(serviceIntent);
    }

}
