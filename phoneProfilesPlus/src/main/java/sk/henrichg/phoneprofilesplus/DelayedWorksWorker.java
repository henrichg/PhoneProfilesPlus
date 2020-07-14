package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class DelayedWorksWorker extends Worker {

    //Context context;

    static final String DELAYED_WORK_HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG = "handleEventsBluetoothLEScannerWork";
    static final String DELAYED_WORK_HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG = "handleEventsBluetoothCLScannerWork";
    static final String DELAYED_WORK_HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG = "handleEventsWifiScannerFromReceiverWork";
    static final String DELAYED_WORK_HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG = "handleEventsWifiScannerFromScannerWork";
    static final String DELAYED_WORK_HANDLE_EVENTS_TWILIGHT_SCANNER_WORK_TAG = "handleEventsTwilightScannerWork";
    static final String DELAYED_WORK_HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG = "handleEventsMobileCellsScannerWork";
    static final String DELAYED_WORK_AFTER_FIRST_START_WORK_TAG = "delayedWorkAfterFirstStartWork";
    static final String DELAYED_WORK_CLOSE_ALL_APPLICATIONS_WORK_TAG = "delayedWorkCloseAllApplications";
    static final String DELAYED_WORK_PACKAGE_REPLACED_WORK_TAG = "packageReplacedWork";

    static final String DELAYED_WORK_HANDLE_EVENTS = "handle_events";
    static final String DELAYED_WORK_START_WIFI_SCAN = "start_wifi_scan";
    static final String DELAYED_WORK_BLOCK_PROFILE_EVENT_ACTIONS = "block_profile_event_actions";
    static final String DELAYED_WORK_AFTER_FIRST_START = "after_first_start";
    static final String DELAYED_WORK_PACKAGE_REPLACED = "package_replaced";
    static final String DELAYED_WORK_CLOSE_ALL_APPLICATIONS = "close_all_applications";
    //static final String DELAYED_WORK_CHANGE_FILTER_AFTER_EDITOR_DATA_CHANGE = "change_filter_after_editor_data_change";

    public DelayedWorksWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        //this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //PPApplication.logE("DelayedWorksWorker.doWork", "xxx");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            //Data outputData;

            // Get the input
            String action = getInputData().getString(PhoneProfilesService.EXTRA_DELAYED_WORK);
            if (action == null)
                return Result.success();

            String sensorType = getInputData().getString(PhoneProfilesService.EXTRA_SENSOR_TYPE);

            //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
            //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
            //                                    updateName);

            //return Result.success(outputData);

            Context appContext = getApplicationContext();

            switch (action) {
                case DELAYED_WORK_AFTER_FIRST_START:
                    PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "START");

                    BootUpReceiver.bootUpCompleted = true;

                    //boolean fromDoFirstStart = getInputData().getBoolean(PhoneProfilesService.EXTRA_FROM_DO_FIRST_START, true);

                    // activate profile immediately after start of PPP
                    // this is required for some users, for example: francescocaldelli@gmail.com
                    //PPApplication.applicationPackageReplaced = false;
                    //if (fromDoFirstStart) {
                        //PhoneProfilesService instance = PhoneProfilesService.getInstance();
                        //if (instance != null)
                        //    instance.PhoneProfilesService.setApplicationFullyStarted(appContext/*true*/);
                        PPApplication.setApplicationFullyStarted(appContext);
                    //}

                    //if (fromDoFirstStart) {
                        PPApplication.createNotificationChannels(appContext);

                        boolean activateProfiles = getInputData().getBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);

                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                        if (Event.getGlobalEventsRunning()) {
                            PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "global event run is enabled, first start events");

                            if (activateProfiles) {
                                if (!DataWrapper.getIsManualProfileActivation(false/*, appContext*/)) {
                                    ////// unblock all events for first start
                                    //     that may be blocked in previous application run
                                    dataWrapper.pauseAllEvents(false, false);
                                }
                            }

                            dataWrapper.firstStartEvents(true, false);

                            if (PPApplication.deviceBoot) {
                                PPApplication.deviceBoot = false;
                                PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "device boot");
                                boolean deviceBootEvents = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_DEVICE_BOOT);
                                if (deviceBootEvents) {
                                    PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "device boot event exists");

                                    // start events handler
                                    //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DelayedWorksWorker.doWork (DELAYED_WORK_AFTER_FIRST_START)");

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
                            PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "global event run is not enabled, manually activate profile");

                            if (activateProfiles) {
                                ////// unblock all events for first start
                                //     that may be blocked in previous application run
                                dataWrapper.pauseAllEvents(true, false);
                            }

                            dataWrapper.activateProfileOnBoot();
                            //PPApplication.updateNotificationAndWidgets(true, true, appContext);
                            //PPApplication.updateGUI(appContext, true, true);
                        }

                        //PPApplication.logE("-------- PPApplication.forceUpdateGUI", "from=DelayedWorksWorker.doWork");
                        PPApplication.forceUpdateGUI(appContext, true, true/*, true*/);
                    //}

                    PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "END");
                    break;
                case DELAYED_WORK_PACKAGE_REPLACED:
                    PPApplication.logE("PackageReplacedReceiver.doWork", "START");

                    dataWrapper = new DataWrapper(appContext, false, 0, false);

                    int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
                    int actualVersionCode = 0;
                    // save version code
                    try {
                        PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
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
                    PPApplication.logE("PackageReplacedReceiver.doWork", "oldVersionCode=" + oldVersionCode);
                    PPApplication.logE("PackageReplacedReceiver.doWork", "actualVersionCode=" + actualVersionCode);
                    try {
                        if (oldVersionCode < actualVersionCode) {
                            PPApplication.logE("PackageReplacedReceiver.doWork", "is new version");

                            //PhoneProfilesService.cancelWork(DelayedWorksWorker.DELAYED_WORK_AFTER_FIRST_START_WORK_TAG, appContext);

                            if (actualVersionCode <= 2322) {
                                // for old packages use Priority in events
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                //PPApplication.logE("PackageReplacedReceiver.doWork", "applicationEventUsePriority=true");
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                                editor.apply();

                                restartService = true;
                            }
                            if (actualVersionCode <= 2400) {
                                PPApplication.logE("PackageReplacedReceiver.doWork", "donation alarm restart");
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

                                    /*String rescan;
                                    rescan = ApplicationPreferences.applicationEventLocationRescan;
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "3");
                                    rescan = ApplicationPreferences.applicationEventWifiRescan;
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "3");
                                    rescan = ApplicationPreferences.applicationEventBluetoothRescan;
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "3");
                                    rescan = ApplicationPreferences.applicationEventMobileCellsRescan;
                                    if (rescan.equals("0"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
                                    if (rescan.equals("2"))
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "3");*/

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
                                                    searchPattern =  "\\" + searchPattern;
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
                                //PPApplication.logE("PackageReplacedReceiver.doWork", "set \"night_mode\" theme");
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
                                }
                                else
                                if (notificationBackgroundColor.equals("4")) {
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

                    /*
                    ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "white");
                    editor.apply();
                    */

                    PPApplication.logE("PackageReplacedReceiver.doWork", "PhoneStateScanner.enabledAutoRegistration="+PhoneStateScanner.enabledAutoRegistration);
                    if (PhoneStateScanner.enabledAutoRegistration) {
                        PhoneStateScanner.stopAutoRegistration(appContext, true);
                        PPApplication.logE("PackageReplacedReceiver.doWork", "start of wait for end of autoregistration");
                        int count = 0;
                        while (MobileCellsRegistrationService.serviceStarted && (count < 50)) {
                            PPApplication.sleep(100);
                            count++;
                        }
                        PPApplication.logE("PackageReplacedReceiver.doWork", "end of autoregistration");
                    }

                    /*SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(appContext);
                    if (sharedPreferences != null) {
                        PPApplication.logE("--------------- PackageReplacedReceiver.doWork", "package replaced set to false");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_PACKAGE_REPLACED, false);
                        editor.apply();
                    }*/
                    //PPApplication.applicationPackageReplaced = false;

                    // Start service only when is not started or restart is required
                    // Is not needed to start it when it is not required. Code is already replaced after this call.
                    if (restartService)
                        startService(dataWrapper, true);
                    else
                    if (!isServiceRunning(appContext))
                        startService(dataWrapper, false);
                    else {
                        PhoneProfilesService instance = PhoneProfilesService.getInstance();
                        if (instance != null) {
                            // work after first start
                            //PhoneProfilesService.cancelWork(PPApplication.AFTER_FIRST_START_WORK_TAG);

                            //TODO: use another thread for this
                            PPApplication.startHandlerThreadPPScanners();
                            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Data workData = new Data.Builder()
                                            .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_AFTER_FIRST_START)
                                            .putBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true)
                                            //.putBoolean(PhoneProfilesService.EXTRA_FROM_DO_FIRST_START, false)
                                            .build();

                                    OneTimeWorkRequest worker =
                                            new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                                    .addTag(PPApplication.AFTER_FIRST_START_WORK_TAG)
                                                    .setInputData(workData)
                                                    //.setInitialDelay(5, TimeUnit.SECONDS)
                                                    //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY, TimeUnit.MINUTES)
                                                    .build();
                                    try {
                                        if (PPApplication.getApplicationStarted(true)) {
                                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                                            if (workManager != null)
                                                //workManager.enqueue(worker);
                                                workManager.enqueueUniqueWork(PPApplication.AFTER_FIRST_START_WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, worker);
                                        }
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                            }, 500);

                            //instance.setApplicationFullyStarted(/*true, */true);
                            //PPApplication.updateGUI(appContext, true, true);
                        }
                    }

                    PPApplication.logE("PackageReplacedReceiver.doWork", "END");
                    break;
                case DELAYED_WORK_HANDLE_EVENTS:
                    if (Event.getGlobalEventsRunning() && (sensorType != null)) {
                        //PPApplication.logE("DelayedWorksWorker.doWork", "DELAYED_WORK_HANDLE_EVENTS");
                        //PPApplication.logE("DelayedWorksWorker.doWork", "sensorType="+sensorType);
                        // start events handler
                        //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DelayedWorksWorker.doWork (DELAYED_WORK_HANDLE_EVENTS): sensorType="+sensorType);

                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(sensorType);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DelayedWorksWorker.doWork (DELAYED_WORK_HANDLE_EVENTS)");
                    }
                    break;
                case DELAYED_WORK_START_WIFI_SCAN:
                    //PPApplication.logE("DelayedWorksWorker.doWork", "DELAYED_WORK_START_WIFI_SCAN");
                    WifiScanWorker.startScan(appContext);
                    break;
                case DELAYED_WORK_BLOCK_PROFILE_EVENT_ACTIONS:
                    PPApplication.blockProfileEventActions = false;
                    break;
                case DELAYED_WORK_CLOSE_ALL_APPLICATIONS:
                    //Log.e("DelayedWorksWorker.doWork", "DELAYED_WORK_CLOSE_ALL_APPLICATIONS");
                    //Log.e("DelayedWorksWorker.doWork", "PPApplication.blockProfileEventActions="+PPApplication.blockProfileEventActions);
                    if (!PPApplication.blockProfileEventActions) {
                        try {
                            /*boolean appFound = false;
                            // intentionally using string value as Context.USAGE_STATS_SERVICE was
                            // strangely only added in API 22 (LOLLIPOP_MR1)
                            @SuppressLint("WrongConstant")
                            UsageStatsManager usm = (UsageStatsManager)appContext.getSystemService("usagestats");
                            long time = System.currentTimeMillis();
                            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,time - 1000 * 1000, time);
                            if (appList != null && appList.size() > 0) {
                                appFound = true;
//                                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//                                for (UsageStats usageStats : appList) {
//                                    mySortedMap.put(usageStats.getLastTimeUsed(),
//                                            usageStats);
//                                }
//                                if (mySortedMap != null && !mySortedMap.isEmpty()) {
//                                    currentApp = mySortedMap.get(
//                                            mySortedMap.lastKey()).getPackageName();
//                                }
                            }*/

                            /*boolean appFound = false;
                            ActivityManager manager = (ActivityManager)appContext.getSystemService(Context.ACTIVITY_SERVICE);
                            List<ActivityManager.RunningAppProcessInfo> tasks = manager.getRunningAppProcesses();
                            Log.e("DelayedWorksWorker.doWork", "tasks="+tasks);
                            if ((tasks != null) && (!tasks.isEmpty())) {
                                Log.e("DelayedWorksWorker.doWork", "tasks.size()="+tasks.size());
                                for (ActivityManager.RunningAppProcessInfo task : tasks) {
                                    Log.e("DelayedWorksWorker.doWork", "task.processName="+task.processName);
                                    if (task.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                        Log.e("DelayedWorksWorker.doWork", "IMPORTANCE_FOREGROUND");
                                        appFound = true;
                                        break;
                                    }
                                }
                            }*/
                            //if (appFound) {
                                Intent startMain = new Intent(Intent.ACTION_MAIN);
                                startMain.addCategory(Intent.CATEGORY_HOME);
                                startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //startMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                appContext.startActivity(startMain);
                            //}
                        } catch (SecurityException e) {
                            //Log.e("DelayedWorksWorker.doWork", Log.getStackTraceString(e));
                        } catch (Exception e) {
                            //Log.e("DelayedWorksWorker.doWork", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        }
                    }
                    break;
                /*case DELAYED_WORK_CHANGE_FILTER_AFTER_EDITOR_DATA_CHANGE:
                    if (filterSelectedItem != 0) {
                        Activity activity = PPApplication.getEditorActivity();
                        if (activity instanceof EditorProfilesActivity) {
                            final EditorProfilesActivity editorActivity = (EditorProfilesActivity)activity;
                            Fragment fragment = editorActivity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                            if (fragment instanceof EditorProfileListFragment) {
                                EditorProfileListFragment profileFragment = (EditorProfileListFragment) fragment;
                                boolean changeFilter = false;
                                Profile scrollToProfile = DatabaseHandler.getInstance(context).getProfile(profileId, false);
                                if (scrollToProfile != null) {
                                    switch (filterSelectedItem) {
                                        case EditorProfilesActivity.DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                                            changeFilter = scrollToProfile._showInActivator;
                                            break;
                                        case EditorProfilesActivity.DSI_PROFILES_SHOW_IN_ACTIVATOR:
                                            changeFilter = !scrollToProfile._showInActivator;
                                            break;
                                    }
                                }
                                if (changeFilter) {
                                    profileFragment.scrollToProfile = scrollToProfile;
                                    Handler handler = new Handler(context.getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((GlobalGUIRoutines.HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter()).setSelection(0);
                                            editorActivity.selectFilterItem(0, 0, false, true);
                                        }
                                    });
                                } else
                                    profileFragment.scrollToProfile = null;
                            }
                            if (fragment instanceof EditorEventListFragment) {
                                EditorEventListFragment eventFragment = (EditorEventListFragment) fragment;
                                boolean changeFilter = false;
                                Event scrollToEvent = DatabaseHandler.getInstance(context).getEvent(eventId);
                                if (scrollToEvent != null) {
                                    switch (filterSelectedItem) {
                                        case EditorProfilesActivity.DSI_EVENTS_NOT_STOPPED:
                                            changeFilter = scrollToEvent.getStatus() == Event.ESTATUS_STOP;
                                            break;
                                        case EditorProfilesActivity.DSI_EVENTS_RUNNING:
                                            changeFilter = scrollToEvent.getStatus() != Event.ESTATUS_RUNNING;
                                            break;
                                        case EditorProfilesActivity.DSI_EVENTS_PAUSED:
                                            changeFilter = scrollToEvent.getStatus() != Event.ESTATUS_PAUSE;
                                            break;
                                        case EditorProfilesActivity.DSI_EVENTS_STOPPED:
                                            changeFilter = scrollToEvent.getStatus() != Event.ESTATUS_STOP;
                                            break;
                                    }
                                }
                                if (changeFilter) {
                                    eventFragment.scrollToEvent = scrollToEvent;
                                    Handler handler = new Handler(context.getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((GlobalGUIRoutines.HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter()).setSelection(0);
                                            editorActivity.selectFilterItem(1, 0, false, true);
                                        }
                                    });
                                } else
                                    eventFragment.scrollToEvent = null;
                            }
                        }
                    }
                    break;*/
                default:
                    break;
            }

            return Result.success();
        } catch (Exception e) {
            //Log.e("DelayedWorksWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    /*
    private Data generateResult(int resultCode, String message, boolean updateName) {
        // Create the output of the work
        PPApplication.logE("FetchAddressWorker.generateResult", "resultCode="+resultCode);
        PPApplication.logE("FetchAddressWorker.generateResult", "message="+message);
        PPApplication.logE("FetchAddressWorker.generateResult", "updateName="+updateName);

        return new Data.Builder()
                .putInt(LocationGeofenceEditorActivity.RESULT_CODE, resultCode)
                .putString(LocationGeofenceEditorActivity.RESULT_DATA_KEY, message)
                .putBoolean(LocationGeofenceEditorActivity.UPDATE_NAME_EXTRA, updateName)
                .build();
    }
    */

    private void startService(DataWrapper dataWrapper, boolean exitApp) {
        //boolean isApplicationStarted = PPApplication.getApplicationStarted(false);
        //PPApplication.logE("PackageReplacedReceiver.startService", "isApplicationStarted="+isApplicationStarted);

        if (exitApp)
            PPApplication.exitApp(false, dataWrapper.context, dataWrapper, null, false/*, false, true*/);

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
            PPApplication.logE("PackageReplacedReceiver.startService", "start of wait for end of service");
            int count = 0;
            while ((PhoneProfilesService.getInstance() != null) && (count < 50)) {
                PPApplication.sleep(100);
                count++;
            }
            PPApplication.logE("PackageReplacedReceiver.startService", "service ended");

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
                        PPApplication.logE("PackageReplacedReceiver.isServiceRunning", "true");
                        return true;
                }
            }
        }
        PPApplication.logE("PackageReplacedReceiver.isServiceRunning", "false");
        return false;
    }

}
