package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import java.util.List;

public class PackageReplacedReceiver extends BroadcastReceiver {

    static private boolean restartService;

    @Override
    public void onReceive(Context context, Intent intent) {
        CallsCounter.logCounter(context, "PackageReplacedReceiver.onReceive", "PackageReplacedReceiver_onReceive");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

            restartService = false;

            //PackageReplacedJob.start(context.getApplicationContext());

            PPApplication.setBlockProfileEventActions(true);

            final Context appContext = context.getApplicationContext();

            final DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

            final int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
            // save version code
            try {
                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                int actualVersionCode = PPApplication.getVersionCode(pInfo);
                PPApplication.setSavedVersionCode(appContext, actualVersionCode);

                String version = pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONUPGRADE, version, null, null, 0);
            } catch (Exception ignored) {
            }

            PPApplication.startHandlerThread("PackageReplacedReceiver.onReceive.1");
            final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
            handler2.post(new Runnable() {
                @SuppressWarnings("StringConcatenationInLoop")
                @Override
                public void run() {
                    PPApplication.logE("PackageReplacedReceiver.onReceive", "PackageReplacedReceiver.onReceive.1");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PackageReplacedReceiver_onReceive_1");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PackageReplacedReceiver.onReceive.1");

                        Permissions.setAllShowRequestPermissions(appContext, true);

                        //WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
                        //WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
                        //PhoneStateScanner.setShowEnableLocationNotification(appContext, true);
                        //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                        PPApplication.logE("PackageReplacedReceiver.onReceive", "oldVersionCode=" + oldVersionCode);
                        int actualVersionCode;
                        try {
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                            actualVersionCode = PPApplication.getVersionCode(pInfo);
                            PPApplication.logE("PackageReplacedReceiver.onReceive", "actualVersionCode=" + actualVersionCode);

                            if (oldVersionCode < actualVersionCode) {
                                PPApplication.logE("PackageReplacedReceiver.onReceive", "is new version");

                                if (actualVersionCode <= 2322) {
                                    // for old packages use Priority in events
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    PPApplication.logE("PackageReplacedReceiver.onReceive", "applicationEventUsePriority=true");
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                                    editor.apply();

                                    restartService = true;
                                }
                                if (actualVersionCode <= 2400) {
                                    PPApplication.logE("PackageReplacedReceiver.onReceive", "donation alarm restart");
                                    PPApplication.setDaysAfterFirstStart(appContext, 0);
                                    PPApplication.setDonationNotificationCount(appContext, 0);
                                    DonationNotificationJob.scheduleJob(appContext, true);

                                    restartService = true;
                                }
                                if (actualVersionCode <= 2500) {
                                    // for old packages hide profile notification from status bar if notification is disabled
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    if (Build.VERSION.SDK_INT < 26) {
                                        if (!ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true)) {
                                            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                            PPApplication.logE("PackageReplacedReceiver.onReceive", "notificationShowInStatusBar=false");
                                            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                                            editor.apply();

                                            restartService = true;
                                        }
                                    }
                                }
                                if (actualVersionCode <= 2700) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();

                                    //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);

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
                                    editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, false);
                                    editor.putBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, false);
                                    editor.apply();

                                    restartService = true;
                                }
                                if (actualVersionCode <= 3200) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, true);
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

                                        restartService = true;
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

                                    restartService = true;
                                }

                                /*if (actualVersionCode <= 4100) {
                                    SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                                    if ((preferences.getInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0) == 3) &&
                                            (Build.VERSION.SDK_INT >= 26)) {
                                        // Toggle is not supported for wifi AP in Android 8+
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0);
                                        editor.apply();

                                        restartService = true;
                                    }
                                }*/

                                /*if (actualVersionCode <= 4200) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                                    editor.apply();

                                    SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                                    if (preferences.getInt(Profile.PREF_PROFILE_LOCK_DEVICE, 0) == 3) {
                                        editor = preferences.edit();
                                        editor.putInt(Profile.PREF_PROFILE_LOCK_DEVICE, 1);
                                        editor.apply();
                                    }

                                    restartService = true;
                                }*/

                                if (actualVersionCode <= 4400) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    if (!ApplicationPreferences.preferences.contains(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR)) {
                                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, ApplicationPreferences.applicationWidgetListPrefIndicator(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, ApplicationPreferences.applicationWidgetListBackground(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, ApplicationPreferences.applicationWidgetListLightnessB(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, ApplicationPreferences.applicationWidgetListLightnessT(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, ApplicationPreferences.applicationWidgetListIconColor(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetListIconLightness(appContext));
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetListRoundedCorners(appContext));
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetListBackgroundType(appContext));
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetListBackgroundColor(appContext));
                                        editor.apply();

                                        restartService = true;
                                    }
                                }

                                if (actualVersionCode <= 4550) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    boolean darkBackground = ApplicationPreferences.preferences.getBoolean("notificationDarkBackground", false);
                                    if (darkBackground) {
                                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "1");
                                        editor.apply();

                                        restartService = true;
                                    }
                                }

                                if (actualVersionCode <= 4600) {
                                    List<Event> eventList = DatabaseHandler.getInstance(appContext).getAllEvents();
                                    for (Event event : eventList) {
                                        if (!event._eventPreferencesCalendar._searchString.isEmpty()) {
                                            String searchStringOrig = event._eventPreferencesCalendar._searchString;
                                            String searchStringNew = "";
                                            String[] searchStringSplits = searchStringOrig.split("\\|");
                                            for (String split : searchStringSplits) {
                                                if (!split.isEmpty()) {
                                                    String searchPattern = split;
                                                    if (searchPattern.startsWith("!")) {
                                                        searchPattern =  "\\" + searchPattern;
                                                    }
                                                    if (!searchStringNew.isEmpty())
                                                        searchStringNew = searchStringNew + "|";
                                                    searchStringNew = searchStringNew + searchPattern;
                                                }
                                            }
                                            event._eventPreferencesCalendar._searchString = searchStringNew;
                                            DatabaseHandler.getInstance(appContext).updateEvent(event);

                                            restartService = true;
                                        }
                                    }
                                }

                                if (actualVersionCode <= 4870) {
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_FILTER_SPINNER, true);

                                    String theme = ApplicationPreferences.applicationTheme(appContext, false);
                                    if (!(theme.equals("white") || theme.equals("dark") || theme.equals("night_mode"))) {
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "white");
                                        GlobalGUIRoutines.switchNightMode(appContext, true);
                                    }

                                    editor.apply();
                                }

                                if (actualVersionCode <= 5000) {
                                    PPApplication.logE("PackageReplacedReceiver.onReceive", "set \"night_mode\" theme");
                                    ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "night_mode");
                                    GlobalGUIRoutines.switchNightMode(appContext, true);
                                    editor.apply();
                                }

                                PPApplication.logE("PackageReplacedReceiver.onReceive", "restartService="+restartService);
                            }
                        } catch (Exception ignored) {
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PackageReplacedReceiver.onReceive.1");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });

            PPApplication.startHandlerThread("PackageReplacedReceiver.onReceive.2");
            final Handler handler3 = new Handler(PPApplication.handlerThread.getLooper());
            handler3.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PackageReplacedReceiver_onReceive_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PackageReplacedReceiver.onReceive.2");
                        PPApplication.logE("PackageReplacedReceiver.onReceive", "restartService="+restartService);

                        /*
                        ApplicationPreferences.getSharedPreferences(appContext);
                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "white");
                        editor.apply();
                        */

                        if (PhoneStateScanner.enabledAutoRegistration) {
                            PhoneStateScanner.stopAutoRegistration(appContext);
                            PPApplication.sleep(2000);
                        }

                        //if (restartService) {
                            //PPApplication.sleep(3000);
                            if (PPApplication.getApplicationStarted(appContext, false)) {
                                // application was started before upgrade
                                if (!PPApplication.getApplicationStarted(appContext, true)) {
                                    // service is not started, start it
                                    PPApplication.logE("PackageReplacedReceiver.onReceive", "restart PhoneProfilesService");
                                    restartService = true;
                                    startService(dataWrapper);
                                }
                                else
                                    restartService = false;
                            }
                        //}
                        if (!restartService) {
                            //PPApplication.sleep(3000);
                            if (PPApplication.getApplicationStarted(appContext, true)) {
                                // service is started by PPApplication
                                final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                                dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONSTART, null, null, null, 0);

                                // start events
                                if (Event.getGlobalEventsRunning(appContext)) {
                                    PPApplication.logE("PackageReplacedReceiver.onReceive", "global event run is enabled, first start events");

                                    if (!dataWrapper.getIsManualProfileActivation(false)) {
                                        ////// unblock all events for first start
                                        //     that may be blocked in previous application run
                                        dataWrapper.pauseAllEvents(true, false);
                                    }

                                    dataWrapper.firstStartEvents(true, false);
                                    PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from PackageReplacedReceiver.onReceive");
                                    dataWrapper.updateNotificationAndWidgets(true);
                                } else {
                                    PPApplication.logE("PackageReplacedReceiver.onReceive", "global event run is not enabled, manually activate profile");

                                    ////// unblock all events for first start
                                    //     that may be blocked in previous application run
                                    dataWrapper.pauseAllEvents(true, false);

                                    dataWrapper.activateProfileOnBoot();
                                    PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from PackageReplacedReceiver.onReceive");
                                    dataWrapper.updateNotificationAndWidgets(true);
                                }
                            }
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PackageReplacedReceiver.onReceive.2");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }, 15000);

        }
    }

    private void startService(DataWrapper dataWrapper) {
        boolean isStarted = PPApplication.getApplicationStarted(dataWrapper.context, false);

        PPApplication.logE("PPApplication.exitApp", "from PackageReplacedReceiver.startService shutdown=false");
        PPApplication.exitApp(false, dataWrapper.context, dataWrapper, null, false/*, false, true*/);

        if (isStarted)
        {
            PPApplication.sleep(2000);

            // start PhoneProfilesService
            PPApplication.logE("PackageReplacedReceiver.startService", "xxx");
            PPApplication.setApplicationStarted(dataWrapper.context, true);
            Intent serviceIntent = new Intent(dataWrapper.context, PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            PPApplication.startPPService(dataWrapper.context, serviceIntent);

            //PPApplication.sleep(2000);
        }
    }

}
