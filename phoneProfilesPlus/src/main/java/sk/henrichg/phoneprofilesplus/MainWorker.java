package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Set;

public class MainWorker extends Worker {

    static final String GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK = "geofenceScannerSwitchGPSWork";
    static final String LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK = "lockDeviceFinishActivityWork";
    static final String LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK = "lockDeviceAfterScreenOffWork";
    static final String EVENT_DELAY_START_TAG_WORK = "eventDelayStartWork";
    static final String EVENT_DELAY_END_TAG_WORK = "eventDelayEndWork";
    static final String CLOSE_ALL_APPLICATIONS_WORK_TAG = "closeAllApplicationsWork";

    static final String SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG = "scheduleAvoidRescheduleReceiverWork";
    static final String SCHEDULE_LONG_INTERVAL_WIFI_WORK_TAG = "scheduleLongIntervalWifiWork";
    static final String SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG = "scheduleLongIntervalBluetoothWork";
    //static final String SCHEDULE_LONG_INTERVAL_GEOFENCE_WORK_TAG = "scheduleLongIntervalGeofenceWork";
    static final String SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG = "scheduleLongIntervalPeriodicEventsHandlerWork";
    static final String SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG = "scheduleLongIntervalSearchCalendarWork";

    static final String HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG = "handleEventsBluetoothLEScannerWork";
    static final String HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG = "handleEventsBluetoothCLScannerWork";
    static final String HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG = "handleEventsWifiScannerFromReceiverWork";
    static final String HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG = "handleEventsWifiScannerFromScannerWork";
    static final String HANDLE_EVENTS_TWILIGHT_SCANNER_WORK_TAG = "handleEventsTwilightScannerWork";
    static final String HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG = "handleEventsMobileCellsScannerWork";
    static final String HANDLE_EVENTS_ORIENTATION_SCANNER_WORK_TAG = "handleEventsOrientationScannerWork";
    static final String HANDLE_EVENTS_NOTIFICATION_POSTED_SCANNER_WORK_TAG = "handleEventsNotificationPostedScannerWork";
    static final String HANDLE_EVENTS_NOTIFICATION_REMOVED_SCANNER_WORK_TAG = "handleEventsNotificationRemovedScannerWork";
    static final String HANDLE_EVENTS_NOTIFICATION_RESCAN_SCANNER_WORK_TAG = "handleEventsNotificationRescanScannerWork";

    static final String START_EVENT_NOTIFICATION_WORK_TAG = "startEventNotificationWork";
    static final String RUN_APPLICATION_WITH_DELAY_WORK_TAG = "runApplicationWithDelayWork";
    static final String PROFILE_DURATION_WORK_TAG = "profileDurationWork";


    final Context context;

    public MainWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
//            PPApplication.logE("[IN_WORKER]  MainWorker.doWork", "xxxx");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            Context appContext = context.getApplicationContext();

            Set<String> tags = getTags();
            for (String tag : tags) {
                // ignore tags with package name
                if (tag.startsWith(PPApplication.PACKAGE_NAME))
                    continue;

//                PPApplication.logE("[IN_WORKER]  MainWorker.doWork", "tag=" + tag);

                switch (tag) {
                    case WifiScanWorker.WORK_TAG_START_SCAN:
                        //PPApplication.logE("DelayedWorksWorker.doWork", "DELAYED_WORK_START_WIFI_SCAN");
                        WifiScanWorker.startScan(appContext);
                        break;
                    case HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG:
                    case HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_TWILIGHT_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_ORIENTATION_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_NOTIFICATION_POSTED_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_NOTIFICATION_REMOVED_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_NOTIFICATION_RESCAN_SCANNER_WORK_TAG:
                        String sensorType = getInputData().getString(PhoneProfilesService.EXTRA_SENSOR_TYPE);
                        if (Event.getGlobalEventsRunning() && (sensorType != null)) {
                            //PPApplication.logE("DelayedWorksWorker.doWork", "DELAYED_WORK_HANDLE_EVENTS");
                            //PPApplication.logE("DelayedWorksWorker.doWork", "sensorType="+sensorType);
                            // start events handler
                            //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DelayedWorksWorker.doWork (DELAYED_WORK_HANDLE_EVENTS): sensorType="+sensorType);

//                            PPApplication.logE("[EVENTS_HANDLER_CALL] MainWorker.doWork", "sensorType="+sensorType);
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(sensorType);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DelayedWorksWorker.doWork (DELAYED_WORK_HANDLE_EVENTS)");
                        }
                        break;
                    case GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK:
                        GeofencesScannerSwitchGPSBroadcastReceiver.doWork(appContext);
                        break;
                    case LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK:
                        LockDeviceActivityFinishBroadcastReceiver.doWork();
                        break;
                    case LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK:
                        LockDeviceAfterScreenOffBroadcastReceiver.doWork(false, appContext);
                        break;
                    case CLOSE_ALL_APPLICATIONS_WORK_TAG:
                        //Log.e("DelayedWorksWorker.doWork", "DELAYED_WORK_CLOSE_ALL_APPLICATIONS");
                        //Log.e("DelayedWorksWorker.doWork", "PPApplication.blockProfileEventActions="+PPApplication.blockProfileEventActions);
                        if (!PPApplication.blockProfileEventActions) {
                            try {
                                Intent startMain = new Intent(Intent.ACTION_MAIN);
                                startMain.addCategory(Intent.CATEGORY_HOME);
                                startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //startMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                appContext.startActivity(startMain);
                            /*} catch (SecurityException e) {
                                //Log.e("DelayedWorksWorker.doWork", Log.getStackTraceString(e));
                                String profileName = getInputData().getString(ActivateProfileHelper.EXTRA_PROFILE_NAME);
                                ActivateProfileHelper.showError(appContext, profileName, Profile.PARAMETER_CLOSE_ALL_APPLICATION);*/
                            } catch (Exception e) {
                                //Log.e("DelayedWorksWorker.doWork", Log.getStackTraceString(e));
                                //PPApplication.recordException(e);
                                String profileName = getInputData().getString(ActivateProfileHelper.EXTRA_PROFILE_NAME);
                                ActivateProfileHelper.showError(appContext, profileName, Profile.PARAMETER_CLOSE_ALL_APPLICATION);
                            }
                        }
                        break;
                    case PPApplication.AFTER_FIRST_START_WORK_TAG:
                        doAfterFirstStart(appContext,
                                getInputData().getBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true),
                                getInputData().getBoolean(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APPLICATION, false),
                                getInputData().getString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_ACTION),
                                getInputData().getInt(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE, 0),
                                getInputData().getString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE),
                                getInputData().getBoolean(PhoneProfilesService.EXTRA_SHOW_TOAST, true));
                        break;
                    /*case PPApplication.PACKAGE_REPLACED_WORK_TAG:
                        PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "START");

                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
                        int actualVersionCode = 0;
                        // save version code
                        try {
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            actualVersionCode = PPApplication.getVersionCode(pInfo);
                            PPApplication.setSavedVersionCode(appContext, actualVersionCode);

                            String version = pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_UPGRADE, version, null, null, 0, "");
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        Permissions.setAllShowRequestPermissions(appContext, true);

                        //WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
                        //WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
                        //PhoneStateScanner.setShowEnableLocationNotification(appContext, true);
                        //ActivateProfileHelper.setScreenUnlocked(appContext, true);

                        boolean restartService = false;
                        PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "oldVersionCode=" + oldVersionCode);
                        PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "actualVersionCode=" + actualVersionCode);
                        try {
                            if (oldVersionCode < actualVersionCode) {
                                PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "is new version");

                                //PhoneProfilesService.cancelWork(DelayedWorksWorker.DELAYED_WORK_AFTER_FIRST_START_WORK_TAG, appContext);

                                if (actualVersionCode <= 2322) {
                                    // for old packages use Priority in events
                                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                    //PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "applicationEventUsePriority=true");
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                                    editor.apply();

                                    restartService = true;
                                }
                                if (actualVersionCode <= 2400) {
                                    PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "donation alarm restart");
                                    PPApplication.setDaysAfterFirstStart(appContext, 0);
                                    PPApplication.setDonationNotificationCount(appContext, 0);
                                    DonationBroadcastReceiver.setAlarm(appContext);

                                    restartService = true;
                                }

                                //if (actualVersionCode <= 2500) {
                                //    // for old packages hide profile notification from status bar if notification is disabled
                                //    ApplicationPreferences.getSharedPreferences(appContext);
                                //    if (Build.VERSION.SDK_INT < 26) {
                                //        if (!ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true)) {
                                //            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                //            PPApplication.logE("PackageReplacedReceiver.onReceive", "notificationShowInStatusBar=false");
                                //            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                                //            editor.apply();
                                //
                                //            restartService = true;
                                //        }
                                //    }
                                //}

                                if (actualVersionCode <= 2700) {
                                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);

                                    //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);

                                    editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                                    editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                                    editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
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
                                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                    editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, true);
                                    editor.apply();

                                    restartService = true;
                                }
                                if (actualVersionCode <= 3500) {
                                    if (!ApplicationPreferences.getSharedPreferences(appContext).contains(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT)) {
                                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, ApplicationPreferences.applicationActivateWithAlert);

//                                            String rescan;
//                                            rescan = ApplicationPreferences.applicationEventLocationRescan;
//                                            if (rescan.equals("0"))
//                                                editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
//                                            if (rescan.equals("2"))
//                                                editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "3");
//                                            rescan = ApplicationPreferences.applicationEventWifiRescan;
//                                            if (rescan.equals("0"))
//                                                editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
//                                            if (rescan.equals("2"))
//                                                editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "3");
//                                            rescan = ApplicationPreferences.applicationEventBluetoothRescan;
//                                            if (rescan.equals("0"))
//                                                editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
//                                            if (rescan.equals("2"))
//                                                editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "3");
//                                            rescan = ApplicationPreferences.applicationEventMobileCellsRescan;
//                                            if (rescan.equals("0"))
//                                                editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
//                                            if (rescan.equals("2"))
//                                                editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "3");

                                        editor.apply();

                                        restartService = true;
                                    }

                                    // continue donation notification
                                    if (PPApplication.getDaysAfterFirstStart(appContext) == 8) {
                                        PPApplication.setDonationNotificationCount(appContext, 1);

                                        restartService = true;
                                    }
                                }

                                if (actualVersionCode <= 3900) {
                                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF,
                                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true));
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF,
                                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true));
                                    editor.apply();

                                    restartService = true;
                                }

                                //if (actualVersionCode <= 4100) {
                                //    SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                                //    if ((preferences.getInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0) == 3) &&
                                //            (Build.VERSION.SDK_INT >= 26)) {
                                //        // Toggle is not supported for wifi AP in Android 8+
                                //        SharedPreferences.Editor editor = preferences.edit();
                                //        editor.putInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0);
                                //        editor.apply();
                                //
                                //        restartService = true;
                                //    }
                                //}

                                //if (actualVersionCode <= 4200) {
                                //    ApplicationPreferences.getSharedPreferences(appContext);
                                //    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                //    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                                //    editor.apply();

                                //    SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                                //    if (preferences.getInt(Profile.PREF_PROFILE_LOCK_DEVICE, 0) == 3) {
                                //        editor = preferences.edit();
                                //        editor.putInt(Profile.PREF_PROFILE_LOCK_DEVICE, 1);
                                //        editor.apply();
                                //    }
                                //
                                //    restartService = true;
                                //}

                                //if (actualVersionCode <= 4400) {
                                //    ApplicationPreferences.getSharedPreferences(appContext);
                                //    if (!ApplicationPreferences.preferences.contains(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR)) {
                                //        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                //        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, ApplicationPreferences.applicationWidgetOneRowPrefIndicator(appContext));
                                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, ApplicationPreferences.applicationWidgetListBackground(appContext));
                                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, ApplicationPreferences.applicationWidgetListLightnessB(appContext));
                                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, ApplicationPreferences.applicationWidgetListLightnessT(appContext));
                                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, ApplicationPreferences.applicationWidgetListIconColor(appContext));
                                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetListIconLightness(appContext));
                                //        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetListRoundedCorners(appContext));
                                //        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetListBackgroundType(appContext));
                                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetListBackgroundColor(appContext));
                                //        editor.apply();
                                //
                                //        restartService = true;
                                //    }
                                //}

                                if (actualVersionCode <= 4550) {
                                    if (Build.VERSION.SDK_INT < 29) {
                                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                                        boolean darkBackground = preferences.getBoolean("notificationDarkBackground", false);
                                        if (darkBackground) {
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "1");
                                            editor.apply();

                                            restartService = true;
                                        }
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
                                                        searchPattern = "\\" + searchPattern;
                                                    }
                                                    if (!searchStringNew.isEmpty())
                                                        //noinspection StringConcatenationInLoop
                                                        searchStringNew = searchStringNew + "|";
                                                    //noinspection StringConcatenationInLoop
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
                                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                    editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_FILTER_SPINNER, true);

                                    String theme = ApplicationPreferences.applicationTheme(appContext, false);
                                    if (!(theme.equals("white") || theme.equals("dark") || theme.equals("night_mode"))) {
                                        String defaultValue = "white";
                                        if (Build.VERSION.SDK_INT >= 28)
                                            defaultValue = "night_mode";
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, defaultValue);
                                        GlobalGUIRoutines.switchNightMode(appContext, true);
                                    }

                                    editor.apply();

                                    restartService = true;
                                }

                                if (actualVersionCode <= 5020) {
                                    //PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "set \"night_mode\" theme");
                                    if (Build.VERSION.SDK_INT >= 28) {
                                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "night_mode");
                                        GlobalGUIRoutines.switchNightMode(appContext, true);
                                        editor.apply();

                                        restartService = true;
                                    }
                                }

                                if (actualVersionCode <= 5250) {
                                    if (oldVersionCode <= 5210) {
                                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);

                                        if (Build.VERSION.SDK_INT >= 26) {
                                            NotificationManagerCompat manager = NotificationManagerCompat.from(appContext);
                                            try {
                                                NotificationChannel channel = manager.getNotificationChannel(PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL);
                                                if (channel != null) {
                                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED,
                                                            channel.getImportance() != NotificationManager.IMPORTANCE_NONE);
                                                }
                                            } catch (Exception e) {
                                                PPApplication.recordException(e);
                                            }
                                        }

                                        int filterEventsSelectedItem = ApplicationPreferences.editorEventsViewSelectedItem;
                                        if (filterEventsSelectedItem == 2)
                                            filterEventsSelectedItem++;
                                        editor.putInt(ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM, filterEventsSelectedItem);
                                        editor.apply();
                                        ApplicationPreferences.editorEventsViewSelectedItem(appContext);

                                        restartService = true;
                                    }
                                }

                                if (actualVersionCode <= 5330) {
                                    if (oldVersionCode <= 5300) {
                                        // for old packages hide profile notification from status bar if notification is disabled
                                        if (Build.VERSION.SDK_INT < 26) {
                                            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                                            boolean notificationStatusBar = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true);
                                            boolean notificationStatusBarPermanent = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
                                            if (!(notificationStatusBar && notificationStatusBarPermanent)) {
                                                SharedPreferences.Editor editor = preferences.edit();
                                                //PPApplication.logE("PackageReplacedReceiver.onReceive", "status bar is not permanent, set it!!");
                                                editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                                                editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, false);
                                                editor.putString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, "2");
                                                editor.apply();

                                                restartService = true;
                                            }
                                        }
                                    }
                                }

                                if (actualVersionCode <= 5430) {
                                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                                    String notificationBackgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
                                    SharedPreferences.Editor editor = preferences.edit();
                                    if (!preferences.contains(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR))
                                        editor.putInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, 0xFFFFFFFF);
                                    if (!preferences.contains(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE))
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, false);
                                    if (notificationBackgroundColor.equals("2")) {
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, true);
                                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "1");
                                    } else if (notificationBackgroundColor.equals("4")) {
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, true);
                                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "3");
                                        editor.apply();
                                    }
                                    editor.apply();

                                    restartService = true;
                                }

                                if (actualVersionCode <= 5700) {
                                    // restart service for move screen timeout 24hr and permanent to Keep screen on
                                    restartService = true;
                                }

                                if (actualVersionCode <= 5910) {
                                    ApplicationPreferences.startStopTargetHelps(appContext, false);

                                    restartService = true;
                                }
                            }
                        } catch (Exception ee) {
                            PPApplication.recordException(ee);
                        }

                        PPApplication.loadGlobalApplicationData(appContext);
                        PPApplication.loadApplicationPreferences(appContext);
                        PPApplication.loadProfileActivationData(appContext);

                        PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "PhoneStateScanner.enabledAutoRegistration=" + PhoneStateScanner.enabledAutoRegistration);
                        if (PhoneStateScanner.enabledAutoRegistration) {
                            PhoneStateScanner.stopAutoRegistration(appContext, true);
                            PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "start of wait for end of autoregistration");
                            int count = 0;
                            while (MobileCellsRegistrationService.serviceStarted && (count < 50)) {
                                PPApplication.sleep(100);
                                count++;
                            }
                            PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "end of autoregistration");
                        }

                        //restartService = true; // for test only

                        // Start service only when is not started or restart is required
                        // Is not needed to start it when it is not required. Code is already replaced after this call.
                        if (restartService)
                            startService(dataWrapper, true);
                        else if (!isServiceRunning(appContext))
                            startService(dataWrapper, false);
                        else {
                            PhoneProfilesService instance = PhoneProfilesService.getInstance();
                            if (instance != null) {
                                // work after first start
                                //PhoneProfilesService.cancelWork(PPApplication.AFTER_FIRST_START_WORK_TAG);

                                doAfterFirstStart(appContext, true);

                                //instance.setApplicationFullyStarted(true);
                                //PPApplication.updateGUI(appContext, true, true);
                            }
                        }

                        PPApplication.logE("PhoneProfilesService.afterPackageReplaced.doWork", "END");
                        break; */
                    case SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG:
                        AvoidRescheduleReceiverWorker.enqueueWork();
                        break;
                    case SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG:
                        BluetoothScanWorker.scheduleWork(appContext, false);
                        break;
                    //case SCHEDULE_LONG_INTERVAL_GEOFENCE_WORK_TAG:
                    //    GeofenceScanWorker.scheduleWork(appContext, false);
                    //    break;
                    case SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG:
                        PeriodicEventsHandlerWorker.enqueueWork(appContext);
                        break;
                    case SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG:
                        SearchCalendarEventsWorker.scheduleWork(false);
                        break;
                    case SCHEDULE_LONG_INTERVAL_WIFI_WORK_TAG:
//                        PPApplication.logE("[IN_WORKER]  MainWorker.doWork", "SCHEDULE_LONG_INTERVAL_WIFI_WORK_TAG");
                        WifiScanWorker.scheduleWork(appContext, false);
                        break;
                    default:
                        if (tag.startsWith(PROFILE_DURATION_WORK_TAG)) {
                            long profileId = getInputData().getLong(PPApplication.EXTRA_PROFILE_ID, 0);
                            boolean forRestartEvents = getInputData().getBoolean(ProfileDurationAlarmBroadcastReceiver.EXTRA_FOR_RESTART_EVENTS, false);
                            int startupSource = getInputData().getInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SERVICE_MANUAL);
                            ProfileDurationAlarmBroadcastReceiver.doWork(false, appContext, profileId, forRestartEvents, startupSource);
                        }
                        else
                        if (tag.startsWith(RUN_APPLICATION_WITH_DELAY_WORK_TAG)) {
                            String profileName = getInputData().getString(RunApplicationWithDelayBroadcastReceiver.EXTRA_PROFILE_NAME);
                            String runApplicationData = getInputData().getString(RunApplicationWithDelayBroadcastReceiver.EXTRA_RUN_APPLICATION_DATA);
                            RunApplicationWithDelayBroadcastReceiver.doWork(appContext, profileName, runApplicationData);
                        }
                        else
                        if (tag.startsWith(EVENT_DELAY_START_TAG_WORK))
                            EventDelayStartBroadcastReceiver.doWork(false, appContext);
                        else
                        if (tag.startsWith(EVENT_DELAY_END_TAG_WORK))
                            EventDelayEndBroadcastReceiver.doWork(false, appContext);
                        else
                        if (tag.startsWith(START_EVENT_NOTIFICATION_WORK_TAG)) {
                            long eventId = getInputData().getLong(PPApplication.EXTRA_EVENT_ID, 0);
                            StartEventNotificationBroadcastReceiver.doWork(false, appContext, eventId);
                        }

                        break;
                }
            }

            return Result.success();
        } catch (Exception e) {
            PPApplication.recordException(e);
            return Result.failure();
        }
    }

    /*
    private void startService(DataWrapper dataWrapper, boolean exitApp) {
        //boolean isApplicationStarted = PPApplication.getApplicationStarted(false);
        //PPApplication.logE("PackageReplacedReceiver.startService", "isApplicationStarted="+isApplicationStarted);

        if (exitApp)
            PPApplication.exitApp(false, dataWrapper.context, dataWrapper, null, false);

        //DatabaseHandler.getInstance(dataWrapper.context).updateAllEventsStatus(Event.ESTATUS_RUNNING, Event.ESTATUS_PAUSE);
        //DatabaseHandler.getInstance(dataWrapper.context).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_WAITING);
        //DatabaseHandler.getInstance(dataWrapper.context).deactivateProfile();
        //DatabaseHandler.getInstance(dataWrapper.context).unblockAllEvents();
        DatabaseHandler.getInstance(dataWrapper.context).disableNotAllowedPreferences();
        //Event.setEventsBlocked(dataWrapper.context, false);
        //DatabaseHandler.getInstance(dataWrapper.context).unblockAllEvents();
        //Event.setForceRunEventRunning(dataWrapper.context, false);

        //if (isApplicationStarted)
        //{
        PPApplication.logE("PhoneProfilesService.afterPackageReplaced.startService", "start of wait for end of service");
        int count = 0;
        while ((PhoneProfilesService.getInstance() != null) && (count < 50)) {
            PPApplication.sleep(100);
            count++;
        }
        PPApplication.logE("PhoneProfilesService.afterPackageReplaced.startService", "service ended");

        // start PhoneProfilesService
        //PPApplication.logE("DelayedWorksWorker.doWork", "xxx");
        PPApplication.setApplicationStarted(dataWrapper.context, true);
        Intent serviceIntent = new Intent(dataWrapper.context, PhoneProfilesService.class);
        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
        //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
        serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
        PPApplication.startPPService(dataWrapper.context, serviceIntent, true);
        //}
    }

    private static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            Class<?> serviceClass = PhoneProfilesService.class;
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
//                    if (inForeground) {
//                        PPApplication.logE("PhoneProfilesService.isServiceRunningInForeground", "service.foreground=" + service.foreground);
//                        return service.foreground;
//                    }
//                    else
                    PPApplication.logE("PhoneProfilesService.afterPackageReplaced.isServiceRunning", "true");
                    return true;
                }
            }
        }
        PPApplication.logE("PhoneProfilesService.afterPackageReplaced.isServiceRunning", "false");
        return false;
    }
    */

    private static void doAfterFirstStart(Context appContext,
                                          boolean activateProfiles,
                                          boolean startForExternalApplication,
                                          String startForExternalAppAction,
                                          int startForExternalAppDataType,
                                          String startForExternalAppDataValue,
                                          boolean showToast) {
        PPApplication.logE("------- MainWorker.doAfterFirstStart", "START");

        //BootUpReceiver.bootUpCompleted = true;

        //boolean fromDoFirstStart = getInputData().getBoolean(PhoneProfilesService.EXTRA_FROM_DO_FIRST_START, true);

        //if (fromDoFirstStart) {
        PPApplication.createNotificationChannels(appContext);

        // activate profile immediately after start of PPP
        // this is required for some users, for example: francescocaldelli@gmail.com
        //PPApplication.applicationPackageReplaced = false;
        //if (fromDoFirstStart) {
        //PhoneProfilesService instance = PhoneProfilesService.getInstance();
        //if (instance != null)
        //    instance.PhoneProfilesService.setApplicationFullyStarted(appContext/*true*/);
        PPApplication.setApplicationFullyStarted(appContext, showToast);
        //}

        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

        if (Event.getGlobalEventsRunning()) {
            PPApplication.logE("MainWorker.doAfterFirstStart", "global event run is enabled, first start events");

            if (activateProfiles) {
                if (!DataWrapper.getIsManualProfileActivation(false, appContext)) {
                    PPApplication.logE("MainWorker.doAfterFirstStart", "pause all events");
                    ////// unblock all events for first start
                    //     that may be blocked in previous application run
                    synchronized (PPApplication.eventsHandlerMutex) {
                        dataWrapper.pauseAllEvents(false, false);
                    }
                }
            }

            PPApplication.logE("MainWorker.doAfterFirstStart", "start donation and check GitHub releases alarms");

            if (PPApplication.donationBroadcastReceiver == null) {
                PPApplication.donationBroadcastReceiver = new DonationBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_DONATION);
                appContext.registerReceiver(PPApplication.donationBroadcastReceiver, intentFilter5);
            }
            if (PPApplication.checkGitHubReleasesBroadcastReceiver == null) {
                PPApplication.checkGitHubReleasesBroadcastReceiver = new CheckGitHubReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_GITHUB_RELEASES);
                appContext.registerReceiver(PPApplication.checkGitHubReleasesBroadcastReceiver, intentFilter5);
            }
            if (PPApplication.checkCriticalGitHubReleasesBroadcastReceiver == null) {
                PPApplication.checkCriticalGitHubReleasesBroadcastReceiver = new CheckCriticalGitHubReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES);
                appContext.registerReceiver(PPApplication.checkCriticalGitHubReleasesBroadcastReceiver, intentFilter5);
            }

            DonationBroadcastReceiver.setAlarm(appContext);
            CheckGitHubReleasesBroadcastReceiver.setAlarm(appContext);
            CheckCriticalGitHubReleasesBroadcastReceiver.setAlarm(appContext);

            dataWrapper.firstStartEvents(true, false);

            // This is fix for 2, 3 restarts of events after first start.
            // Bradcast, callback registration starts events and this is not good
            // Only observers must be registered in service, because of FC in call of "new Hander()".
            PPApplication.logE("MainWorker.doAfterFirstStart", "register receivers and workers");
            PhoneProfilesService.getInstance().registerReceiversAndWorkers(false, false);

            if (PPApplication.deviceBoot) {
                PPApplication.deviceBoot = false;
                PPApplication.logE("MainWorker.doAfterFirstStart", "device boot");
                boolean deviceBootEvents = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_DEVICE_BOOT);
                if (deviceBootEvents) {
                    PPApplication.logE("MainWorker.doAfterFirstStart", "device boot event exists");

                    // start events handler
                    //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DelayedWorksWorker.doWork (DELAYED_WORK_AFTER_FIRST_START)");

//                    PPApplication.logE("[EVENTS_HANDLER_CALL] MainWorker.doAfterFirstStart", "sensorType=SENSOR_TYPE_DEVICE_BOOT");
                    EventsHandler eventsHandler = new EventsHandler(appContext);

                    Calendar now = Calendar.getInstance();
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    final long _time = now.getTimeInMillis() + gmtOffset;
                    eventsHandler.setEventDeviceBootParameters(_time);

                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_BOOT);

                    //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DelayedWorksWorker.doWork (DELAYED_WORK_AFTER_FIRST_START)");
                }
            }

            //PPApplication.updateNotificationAndWidgets(true, true, appContext);
            //PPApplication.updateGUI(appContext, true, true);
        } else {
            PPApplication.logE("MainWorker.doAfterFirstStart", "global event run is not enabled, manually activate profile");

            if (activateProfiles) {
                ////// unblock all events for first start
                //     that may be blocked in previous application run
                synchronized (PPApplication.eventsHandlerMutex) {
                    dataWrapper.pauseAllEvents(true, false);
                }
            }

            dataWrapper.activateProfileOnBoot();
            //PPApplication.updateNotificationAndWidgets(true, true, appContext);
            //PPApplication.updateGUI(appContext, true, true);
        }

        //PPApplication.logE("-------- PPApplication.forceUpdateGUI", "from=DelayedWorksWorker.doWork");
//        PPApplication.logE("------- PhoneProfilesService.doForFirstStart.doWork", "forceUpdateGUI");
        PPApplication.forceUpdateGUI(appContext, true, true/*, true*/);
        //}

        if (startForExternalApplication) {
            Intent intent = new Intent(startForExternalAppAction);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if (startForExternalAppDataType == PhoneProfilesService.START_FOR_EXTERNAL_APP_PROFILE)
                intent.putExtra(ActivateProfileFromExternalApplicationActivity.EXTRA_PROFILE_NAME, startForExternalAppDataValue);
            if (startForExternalAppDataType == PhoneProfilesService.START_FOR_EXTERNAL_APP_EVENT)
                intent.putExtra(ActionForExternalApplicationActivity.EXTRA_EVENT_NAME, startForExternalAppDataValue);
            appContext.startActivity(intent);
        }

        PPApplication.logE("------- MainWorker.doAfterFirstStart", "END");
    }

}
