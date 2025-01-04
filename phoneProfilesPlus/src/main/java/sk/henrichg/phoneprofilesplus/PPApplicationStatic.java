package sk.henrichg.phoneprofilesplus;

import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import org.acra.ACRA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import dev.doubledot.doki.views.DokiContentView;

/** @noinspection ExtractMethodRecommender*/
class PPApplicationStatic {

    private PPApplicationStatic() {
        // private constructor to prevent instantiation
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    static void _cancelWork(final String name, final boolean forceCancel) {
        WorkManager workManager = PPApplication.getWorkManagerInstance();
        if (workManager != null) {
            ListenableFuture<List<WorkInfo>> statuses;
            statuses = workManager.getWorkInfosForUniqueWork(name);
            try {
                List<WorkInfo> workInfoList = statuses.get();
//                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" workInfoList.size()="+workInfoList.size());
                // cancel only enqueued works
                for (WorkInfo workInfo : workInfoList) {
                    WorkInfo.State state = workInfo.getState();
                    if (forceCancel || (state == WorkInfo.State.ENQUEUED)) {
                        // any work is enqueued, cancel it
//                            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" forceCancel="+forceCancel);
//                            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" state="+state);
//                            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" cancel it");
                        workManager.cancelWorkById(workInfo.getId());
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e("PPApplicationStatic._cancelWork", Log.getStackTraceString(e));
            }

            if (name.startsWith(MainWorker.EVENT_DELAY_START_WORK_TAG))
                PPApplication.elapsedAlarmsEventDelayStartWork.remove(name);
            if (name.startsWith(MainWorker.EVENT_DELAY_END_WORK_TAG))
                PPApplication.elapsedAlarmsEventDelayEndWork.remove(name);
            if (name.startsWith(MainWorker.PROFILE_DURATION_WORK_TAG))
                PPApplication.elapsedAlarmsProfileDurationWork.remove(name);
            if (name.startsWith(MainWorker.RUN_APPLICATION_WITH_DELAY_WORK_TAG))
                PPApplication.elapsedAlarmsRunApplicationWithDelayWork.remove(name);
            if (name.startsWith(MainWorker.START_EVENT_NOTIFICATION_WORK_TAG))
                PPApplication.elapsedAlarmsStartEventNotificationWork.remove(name);
        }
    }

    static void cancelWork(final String name,
                           @SuppressWarnings("SameParameterValue") final boolean forceCancel) {
        // cancel only enqueued works
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name);
            _cancelWork(name, forceCancel);
        };
        createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
    }

    // is called from ThreadHandler
    static void cancelAllWorks(/*boolean atStart*/) {
        /*if (atStart) {
            cancelWork(ShowProfileNotificationWorker.WORK_TAG, false);
            cancelWork(UpdateGUIWorker.WORK_TAG, false);
        }*/
        //if (!atStart)
        _cancelWork(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG, false);
        for (String tag : PPApplication.elapsedAlarmsProfileDurationWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsProfileDurationWork.clear();
        for (String tag : PPApplication.elapsedAlarmsRunApplicationWithDelayWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsRunApplicationWithDelayWork.clear();
        for (String tag : PPApplication.elapsedAlarmsEventDelayStartWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsEventDelayStartWork.clear();
        for (String tag : PPApplication.elapsedAlarmsEventDelayEndWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsEventDelayEndWork.clear();
        for (String tag : PPApplication.elapsedAlarmsStartEventNotificationWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsStartEventNotificationWork.clear();
        /*if (atStart) {
            cancelWork(DisableInternalChangeWorker.WORK_TAG, false);
            cancelWork(DisableVolumesInternalChangeWorker.WORK_TAG, false);
            cancelWork(DisableScreenTimeoutInternalChangeWorker.WORK_TAG, false);
        }*/
        _cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
        _cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
        _cancelWork(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG, false);
        _cancelWork(BluetoothScanWorker.WORK_TAG, false);
        _cancelWork(BluetoothScanWorker.WORK_TAG_SHORT, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG, false);
        //_cancelWork(RestartEventsWithDelayWorker.WORK_TAG_1, false);
        //_cancelWork(RestartEventsWithDelayWorker.WORK_TAG_2, false);
        //_cancelWork(GeofenceScanWorker.WORK_TAG, false);
        //_cancelWork(GeofenceScanWorker.WORK_TAG_SHORT, false);
        _cancelWork(MainWorker.LOCATION_SCANNER_SWITCH_GPS_WORK_TAG, false);
        _cancelWork(LocationGeofenceEditorActivityOSM.FETCH_ADDRESS_WORK_TAG_OSM, false);
        //if (atStart)
        //    cancelWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK, false);
        _cancelWork(MainWorker.LOCK_DEVICE_AFTER_SCREEN_OFF_WORK_TAG, false);
        /*if (atStart) {
            cancelWork(PACKAGE_REPLACED_WORK_TAG, false);
            cancelWork(AFTER_FIRST_START_WORK_TAG, false);
            cancelWork(DisableBlockProfileEventActionWorker.WORK_TAG, false);
        }*/
        _cancelWork(SearchCalendarEventsWorker.WORK_TAG, false);
        _cancelWork(SearchCalendarEventsWorker.WORK_TAG_SHORT, false);
        _cancelWork(WifiScanWorker.WORK_TAG, false);
        _cancelWork(WifiScanWorker.WORK_TAG_SHORT, false);
        _cancelWork(WifiScanWorker.WORK_TAG_START_SCAN, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_TWILIGHT_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.ORIENTATION_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_POSTED_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_REMOVED_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_WIFI_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG, false);
        _cancelWork(LocationSensorWorker.LOCATION_SENSOR_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_RESCAN_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_SOUND_PROFILE_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_PERIODIC_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG, false);
        //_cancelWork(DisableInternalChangeWorker.WORK_TAG, false);
        //_cancelWork(DisableScreenTimeoutInternalChangeWorker.WORK_TAG, false);
        //_cancelWork(DisableVolumesInternalChangeWorker.WORK_TAG, false);

    }

    /*
    static void setWorkManagerInstance(Context context) {
        workManagerInstance = WorkManager.getInstance(context);
    }
    */

    /*
    static boolean isNewVersion(Context appContext) {
        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
        int actualVersionCode;
        try {
            if (oldVersionCode == 0) {
                // save version code
                try {
                    PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.PPApplication.PACKAGE_NAME, 0);
                    actualVersionCode = PPApplication.getVersionCode(pInfo);
                    PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                } catch (Exception ignored) {
                }
                return false;
            }

            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.PPApplication.PACKAGE_NAME, 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);

            return (oldVersionCode < actualVersionCode);
        } catch (Exception e) {
            return false;
        }
    }
    */

    static int getVersionCode(PackageInfo pInfo) {
        //return pInfo.versionCode;
        return (int) PackageInfoCompat.getLongVersionCode(pInfo);
    }

    static void setApplicationFullyStarted(Context context) {
        boolean oldApplicationFullyStarted = PPApplication.applicationFullyStarted;
        PPApplication.applicationFullyStarted = true; //started;

//        PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] PPApplication.setApplicationFullyStarted", "oldApplicationFullyStarted="+oldApplicationFullyStarted);

        final Context appContext = context.getApplicationContext();

        if (!oldApplicationFullyStarted) {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPApplication.setApplicationFullyStarted", "call of updateGUI");
            PPApplication.updateGUI(true, false, appContext);
        }

        if (!oldApplicationFullyStarted && PPApplication.normalServiceStart && PPApplication.showToastForProfileActivation) {
            // it is not restart of application by system
            String text = appContext.getString(R.string.ppp_app_name) + " " + context.getString(R.string.application_is_started_toast);
            PPApplication.showToast(appContext, text, Toast.LENGTH_SHORT);
        }

        PPApplication.normalServiceStart = true;
    }

    //--------------------------------------------------------------

    static void addActivityLog(Context context, final int logType, final String eventName,
                               final String profileName, final String profilesEventsCount) {
        if (PPApplication.prefActivityLogEnabled) {
            final Context appContext = context.getApplicationContext();

            if ((logType == PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_ALARM) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_SET_VPN) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_CAMERA_FLASH) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_WIFI) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_WIFIAP) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_CLOSE_ALL_APPLICATIONS) ||
                    (logType == PPApplication.ALTYPE_PROFILE_ERROR_SEND_SMS)) {

                boolean manualProfileActivation = false;
                if (EventStatic.getGlobalEventsRunning(appContext)) {
                    if (EventStatic.getEventsBlocked(appContext)) {
                        if (!EventStatic.getForceRunEventRunning(appContext))
                            manualProfileActivation = true;
                    }
                } else
                    manualProfileActivation = true;
                if (manualProfileActivation) {
                    String title = appContext.getString(R.string.profile_activation_activation_error_title) + " " + profileName;
                    String text = "";
                    int notificationId = 0;
                    String notificationTag = "";
                    switch (logType) {
                        case PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION:
                            text = appContext.getString(R.string.altype_profileError_runApplication_application);
                            notificationId = PPApplication.PROFILE_ACTIVATION_RUN_APPLICATION_APPLICATION_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_RUN_APPLICATION_APPLICATION_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT:
                            text = appContext.getString(R.string.altype_profileError_runApplication_shortcut);
                            notificationId = PPApplication.PROFILE_ACTIVATION_RUN_APPLICATION_SHORTCUT_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_RUN_APPLICATION_SHORTCUT_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT:
                            text = appContext.getString(R.string.altype_profileError_runApplication_intent);
                            notificationId = PPApplication.PROFILE_ACTIVATION_RUN_APPLICATION_INTENT_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_RUN_APPLICATION_INTENT_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE:
                            text = appContext.getString(R.string.altype_profileError_setTone_ringtone);
                            notificationId = PPApplication.PROFILE_ACTIVATION_SET_TONE_RINGTONE_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_SET_TONE_RINGTONE_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION:
                            text = appContext.getString(R.string.altype_profileError_setTone_notification);
                            notificationId = PPApplication.PROFILE_ACTIVATION_SET_TONE_NOTIFICATION_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_SET_TONE_NOTIFICATION_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_ALARM:
                            text = appContext.getString(R.string.altype_profileError_setTone_alarm);
                            notificationId = PPApplication.PROFILE_ACTIVATION_SET_TONE_ALARM_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_SET_TONE_ALARM_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER:
                            text = appContext.getString(R.string.altype_profileError_setWallpaper);
                            notificationId = PPApplication.PROFILE_ACTIVATION_SET_WALLPAPER_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_SET_WALLPAPER_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_SET_VPN:
                            text = appContext.getString(R.string.altype_profileError_setVPN);
                            notificationId = PPApplication.PROFILE_ACTIVATION_SET_VPN_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_SET_VPN_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_CAMERA_FLASH:
                            text = appContext.getString(R.string.altype_profileError_cameraFlash);
                            notificationId = PPApplication.PROFILE_ACTIVATION_CAMERA_FLASH_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_CAMERA_FLASH_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_WIFI:
                            text = appContext.getString(R.string.altype_profileError_wifi);
                            notificationId = PPApplication.PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_WIFIAP:
                            text = appContext.getString(R.string.altype_profileError_wifiAP);
                            notificationId = PPApplication.PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_CLOSE_ALL_APPLICATIONS:
                            text = appContext.getString(R.string.altype_profileError_closeAllApplications);
                            notificationId = PPApplication.PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_TAG;
                            break;
                        case PPApplication.ALTYPE_PROFILE_ERROR_SEND_SMS:
                            text = appContext.getString(R.string.altype_profileError_sendSMS);
                            notificationId = PPApplication.PROFILE_ACTIVATION_SEND_SMS_ERROR_NOTIFICATION_ID;
                            notificationTag = PPApplication.PROFILE_ACTIVATION_SEND_SMS_ERROR_NOTIFICATION_TAG;
                            break;
                    }
                    if (!text.isEmpty()) {
                        text = appContext.getString(R.string.profile_activation_activation_error) + StringConstants.STR_COLON_WITH_SPACE + text + ".";

                        PPApplicationStatic.createExclamationNotificationChannel(appContext, false);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                                .setColor(ContextCompat.getColor(appContext, R.color.errorColor))
                                .setSmallIcon(R.drawable.ic_ppp_notification/*ic_exclamation_notify*/) // notification icon
                                .setLargeIcon(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.ic_exclamation_notification))
                                .setContentTitle(title) // title for notification
                                .setContentText(text) // message for notification
                                .setAutoCancel(true); // clear notification after click
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
                        //PendingIntent pi = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        //mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                        mBuilder.setGroup(PPApplication.PROFILE_ACTIVATION_ERRORS_NOTIFICATION_GROUP);

                        Notification notification = mBuilder.build();

                        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
                        try {
                            mNotificationManager.notify(notificationTag, notificationId, notification);
                        } catch (SecurityException en) {
                            PPApplicationStatic.logException("PPApplicationStatic.addActivityLog", Log.getStackTraceString(en));
                        } catch (Exception e) {
                            //Log.e("ActivateProfileHelper.showError", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
            }

            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPApplication.addActivityLog");

                //Context context= appContextWeakRef.get();
                if (appContext != null) {
                    //if (ApplicationPreferences.preferences == null)
                    //    ApplicationPreferences.preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                    //ApplicationPreferences.setApplicationDeleteOldActivityLogs(context, Integer.valueOf(preferences.getString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7")));
                    DatabaseHandler.getInstance(appContext).addActivityLog(ApplicationPreferences.applicationDeleteOldActivityLogs,
                            logType, eventName, profileName, profilesEventsCount);

                    Intent intent = new Intent(PPApplication.ACTION_ADDED_ACIVITY_LOG);
                    appContext.sendBroadcast(intent);
                }
            };
            createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
    }

    //--------------------------------------------------------------

    static private void resetLog() {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            exportDir.mkdirs();

        File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
        */

        File path = PPApplication.getInstance().getApplicationContext().getExternalFilesDir(null);
        File logFile = new File(path, PPApplication.LOG_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    /** @noinspection SameParameterValue, BlockingMethodInNonBlockingContext */
    static private void logIntoFile(String type, String tag, String text, boolean crash) {
        if (!(crash || PPApplication.logIntoFile))
            return;

        if (PPApplication.getInstance() == null)
            return;

        try {
            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
            */

            File path = PPApplication.getInstance().getApplicationContext().getExternalFilesDir(null);
            File logFile = new File(path, PPApplication.LOG_FILENAME);

            if (logFile.length() > 1024 * 100000)
                resetLog();

            if (!logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + " [ " + type + " ] [ " + tag + " ]: " + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (Exception e) {
            Log.e("***** PPApplication.logIntoFile", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
        }
    }

    private static boolean logContainsFilterTag(String tag) {
        boolean contains = false;
        String[] filterTags = PPApplication.logFilterTags.split(StringConstants.STR_SPLIT_REGEX);
        for (String filterTag : filterTags) {
            if (!filterTag.contains("!")) {
                if (tag.contains(filterTag)) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    static boolean logEnabled() {
        //noinspection ConstantConditions
        return (PPApplication.logIntoLogCat || PPApplication.logIntoFile);
    }

    /*
    static void logI(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.i(tag, text);
            if (PPApplication.logIntoLogCat) Log.i(tag, "[ "+tag+" ]" + StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("I", tag, text);
        }
    }
    */

    /*
    static void logW(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.w(tag, text);
            if (PPApplication.logIntoLogCat) Log.w(tag, "[ "+tag+" ]" + StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("W", tag, text);
        }
    }
    */

    static void logE(String tag, String text) {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag)) {
            //if (logIntoLogCat) Log.e(tag, text);
            if (PPApplication.logIntoLogCat)
                Log.e(tag, "[ " + tag + " ]" + StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("E", tag, text, false);
        }
    }

    @SuppressLint("MissingPermission")
    static void logException(String tag, String text) {
        if (logEnabled()) {
            if (logContainsFilterTag(tag)) {
                //if (logIntoLogCat) Log.e(tag, text);
                if (PPApplication.logIntoLogCat)
                    Log.e("[EXCEPTION] " + tag, "[ " + tag + " ]" + StringConstants.STR_COLON_WITH_SPACE + text);
                logIntoFile("E", tag, text, true);

                if (DebugVersion.enabled && (PPApplication.getInstance() != null)) {
                    Context appContext = PPApplication.getInstance().getApplicationContext();
                    createExclamationNotificationChannel(appContext, false);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                            .setColor(ContextCompat.getColor(appContext, R.color.errorColor))
                            .setSmallIcon(R.drawable.ic_ppp_notification/*ic_exclamation_notify*/) // notification icon
                            .setLargeIcon(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.ic_exclamation_notification))
                            .setContentTitle("App exception occured!!") // title for notification
                            .setContentText("Read log.txt") // message for notification
                            .setAutoCancel(true); // clear notification after click
                    //mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
                    mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
                    mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                    mBuilder.setGroup(PPApplication.APP_EXCEPTION_NOTIFICATION_GROUP);

                    Notification notification = mBuilder.build();

                    NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
                    try {
                        mNotificationManager.notify(PPApplication.APP_EXCEPTION_NOTIFICATION_TAG, PPApplication.APP_EXCEPTION_NOTIFICATION_ID, notification);
                    } catch (Exception en) {
                        Log.e("PPApplicationStatic.logException", Log.getStackTraceString(en));
                    }
                }
            }
        } else
            Log.e(tag, text);
    }

    /*
    static void logD(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.d(tag, text);
            if (PPApplication.logIntoLogCat) Log.d(tag, "[ "+tag+" ]" + StringConstants.STR_COLON_WITH_SPACE + text);
            logIntoFile("D", tag, text);
        }
    }
    */

    /*
    public static String intentToString(Intent intent) {
        if (intent == null) {
            return null;
        }

        return intent.toString() + " " + bundleToString(intent.getExtras());
    }
    */

    /*
    private static String bundleToString(Bundle bundle) {
        StringBuilder out = new StringBuilder("Bundle[");

        if (bundle == null) {
            out.append("null");
        } else {
            boolean first = true;
            for (String key : bundle.keySet()) {
                if (!first) {
                    out.append(", ");
                }

                out.append(key).append('=');

                Object value = bundle.get(key);

                if (value instanceof int[]) {
                    out.append(Arrays.toString((int[]) value));
                } else if (value instanceof byte[]) {
                    out.append(Arrays.toString((byte[]) value));
                } else if (value instanceof boolean[]) {
                    out.append(Arrays.toString((boolean[]) value));
                } else if (value instanceof short[]) {
                    out.append(Arrays.toString((short[]) value));
                } else if (value instanceof long[]) {
                    out.append(Arrays.toString((long[]) value));
                } else if (value instanceof float[]) {
                    out.append(Arrays.toString((float[]) value));
                } else if (value instanceof double[]) {
                    out.append(Arrays.toString((double[]) value));
                } else if (value instanceof String[]) {
                    out.append(Arrays.toString((String[]) value));
                } else if (value instanceof CharSequence[]) {
                    out.append(Arrays.toString((CharSequence[]) value));
                } else if (value instanceof Parcelable[]) {
                    out.append(Arrays.toString((Parcelable[]) value));
                } else if (value instanceof Bundle) {
                    out.append(bundleToString((Bundle) value));
                } else {
                    out.append(value);
                }

                first = false;
            }
        }

        out.append("]");
        return out.toString();
    }
    */

    //--------------------------------------------------------------

    static void startPPService(Context context, Intent serviceIntent, boolean enableStartOnBoot) {
        //if (isPPService)
        //    PhoneProfilesService.startForegroundNotification = true;

        PPApplicationStatic.createNotificationChannels(context, false);

        if (enableStartOnBoot) {
            SharedPreferences settings = ApplicationPreferences.getSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, true);
            editor.apply();

            ApplicationPreferences.applicationStartOnBoot(context);
        }

            boolean notificationsEnbaled = true;
            if (Build.VERSION.SDK_INT >= 33) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationsEnbaled = notificationManager.areNotificationsEnabled();
            }
            if (notificationsEnbaled)
                context.getApplicationContext().startForegroundService(serviceIntent);
    }

    static void runCommand(Context context, Intent intent) {
//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] PPApplicationStatic.runCommand", "xxx");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //--------------------------------------------------------------

    static void loadGlobalApplicationData(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.loadGlobalApplicationData", "PPApplication.applicationStartedMutex");
        synchronized (PPApplication.applicationStartedMutex) {
            PPApplication.applicationStarted = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PPApplication.PREF_APPLICATION_STARTED, false);
        }
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.loadGlobalApplicationData", "PPApplication.globalEventsRunStopMutex");
        synchronized (PPApplication.globalEventsRunStopMutex) {
            PPApplication.globalEventsRunStop = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(Event.PREF_GLOBAL_EVENTS_RUN_STOP, true);
        }

        for (int i = 0; i < PPApplication.quickTileProfileId.length; i++)
            PPApplication.quickTileProfileId[i] = ApplicationPreferences.getQuickTileProfileId(context, i);

        //IgnoreBatteryOptimizationNotification.getShowIgnoreBatteryOptimizationNotificationOnStart(context);
        CheckCriticalPPPReleasesBroadcastReceiver.getShowCriticalGitHubReleasesNotification(context);
        getActivityLogEnabled(context);
        //getNotificationProfileName(context);
        //getWidgetProfileName(context);
        //getActivityProfileName(context);
        getLastActivatedProfile(context);
        getWallpaperChangeTime(context);
        EventStatic.getEventsBlocked(context);
        EventStatic.getForceRunEventRunning(context);
        PPExtenderBroadcastReceiver.getApplicationInForeground(context);
        EventPreferencesCall.getEventCallEventType(context);
        EventPreferencesCall.getEventCallEventTime(context, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
        EventPreferencesCall.getEventCallPhoneNumber(context);
        EventPreferencesCall.getEventCallFromSIMSlot(context, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
        HeadsetConnectionBroadcastReceiver.getEventHeadsetParameters(context);
        WifiScanner.getForceOneWifiScan(context);
        BluetoothScanner.getForceOneBluetoothScan(context);
        BluetoothScanner.getForceOneLEBluetoothScan(context);
        BluetoothScanWorker.getBluetoothEnabledForScan(context);
        BluetoothScanWorker.getScanRequest(context);
        BluetoothScanWorker.getLEScanRequest(context);
        BluetoothScanWorker.getWaitForResults(context);
        BluetoothScanWorker.getWaitForLEResults(context);
        BluetoothScanWorker.getScanKilled(context);
        WifiScanWorker.getWifiEnabledForScan(context);
        WifiScanWorker.getScanRequest(context);
        WifiScanWorker.getWaitForResults(context);
        EventPreferencesRoaming.getEventRoamingInSIMSlot(context, 0);
        EventPreferencesRoaming.getEventRoamingInSIMSlot(context, 1);
        EventPreferencesRoaming.getEventRoamingInSIMSlot(context, 2);
        //EventPreferencesCallScreening.getEventCallScreeningActive(context);
        EventPreferencesCallScreening.getEventCallScreeningTime(context);
        EventPreferencesCallScreening.getEventCallScreeningPhoneNumber(context);
        EventPreferencesCallScreening.getEventCallScreeningCallDirection(context);

        ApplicationPreferences.loadStartTargetHelps(context);
    }

    static void loadApplicationPreferences(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.loadApplicationPreferences", "PPApplication.applicationPreferencesMutex");
        synchronized (PPApplication.applicationPreferencesMutex) {
            //Log.e("PPApplicationStatic.loadApplicationPreferences", "xxxxx");
            ApplicationPreferences.editorOrderSelectedItem(context);
            ApplicationPreferences.editorSelectedView(context);
            ApplicationPreferences.editorProfilesViewSelectedItem(context);
            ApplicationPreferences.editorEventsViewSelectedItem(context);
            //ApplicationPreferences.applicationFirstStart(context);
            ApplicationPreferences.applicationStartOnBoot(context);
            ApplicationPreferences.applicationActivate(context);
            ApplicationPreferences.applicationStartEvents(context);
            ApplicationPreferences.applicationActivateWithAlert(context);
            ApplicationPreferences.applicationClose(context);
            ApplicationPreferences.applicationLongClickActivation(context);
            //ApplicationPreferences.applicationLanguage(context);
            ApplicationPreferences.applicationTheme(context);
            //ApplicationPreferences.applicationActivatorPrefIndicator(context);
            ApplicationPreferences.applicationEditorPrefIndicator(context);
            //ApplicationPreferences.applicationActivatorHeader(context);
            //ApplicationPreferences.applicationEditorHeader(context);
            ApplicationPreferences.notificationsToast(context);
            //ApplicationPreferences.notificationStatusBar(context);
            //ApplicationPreferences.notificationStatusBarPermanent(context);
            //ApplicationPreferences.notificationStatusBarCancel(context);
            ApplicationPreferences.notificationStatusBarStyle(context);
            //ApplicationPreferences.notificationShowInStatusBar(context);
            ApplicationPreferences.notificationTextColor(context);
            //ApplicationPreferences.notificationHideInLockScreen(context);
            //ApplicationPreferences.notificationTheme(context);
            ApplicationPreferences.applicationWidgetListPrefIndicator(context);
            ApplicationPreferences.applicationWidgetListPrefIndicatorLightness(context);
            ApplicationPreferences.applicationWidgetListHeader(context);
            ApplicationPreferences.applicationWidgetListLightnessB(context);
            ApplicationPreferences.applicationWidgetListLightnessT(context);
            ApplicationPreferences.applicationWidgetIconColor(context);
            ApplicationPreferences.applicationWidgetIconLightness(context);
            ApplicationPreferences.applicationWidgetListIconColor(context);
            ApplicationPreferences.applicationWidgetListIconLightness(context);
            //ApplicationPreferences.applicationEditorAutoCloseDrawer(context);
            //ApplicationPreferences.applicationEditorSaveEditorState(context);
            ApplicationPreferences.notificationPrefIndicator(context);
            ApplicationPreferences.notificationPrefIndicatorLightness(context);
            //ApplicationPreferences.applicationHomeLauncher(context);

            //ApplicationPreferences.applicationWidgetLauncher(context);
            ApplicationPreferences.applicationIconWidgetLauncher(context);
            ApplicationPreferences.applicationOneRowWidgetLauncher(context);
            ApplicationPreferences.applicationListWidgetLauncher(context);
            ApplicationPreferences.applicationDashClockWidgetLauncher(context);

            ApplicationPreferences.applicationNotificationLauncher(context);
            ApplicationPreferences.applicationEventWifiScanInterval(context);
            ApplicationPreferences.applicationDefaultProfile(context);
            ApplicationPreferences.applicationDefaultProfileNotificationSound(context);
            ApplicationPreferences.applicationDefaultProfileNotificationVibrate(context);
            //ApplicationPreferences.applicationDefaultProfileUsage(context);
            ApplicationPreferences.applicationActivatorGridLayout(context);
            ApplicationPreferences.applicationWidgetListGridLayout(context);
            ApplicationPreferences.applicationWidgetListCompactGrid(context);
            ApplicationPreferences.applicationEventBluetoothScanInterval(context);
            //ApplicationPreferences.applicationEventWifiRescan(context);
            //ApplicationPreferences.applicationEventBluetoothRescan(context);
            ApplicationPreferences.applicationWidgetIconHideProfileName(context);
            //ApplicationPreferences.applicationShortcutEmblem(context);
            ApplicationPreferences.applicationEventWifiScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode(context);
            //ApplicationPreferences.applicationPowerSaveModeInternal(context);
            ApplicationPreferences.applicationEventBluetoothLEScanDuration(context);
            ApplicationPreferences.applicationEventLocationUpdateInterval(context);
            ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context);
            ApplicationPreferences.applicationEventLocationUseGPS(context);
            //ApplicationPreferences.applicationEventLocationRescan(context);
            ApplicationPreferences.applicationEventOrientationScanInterval(context);
            ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode(context);
            //ApplicationPreferences.applicationEventMobileCellsRescan(context);
            ApplicationPreferences.applicationDeleteOldActivityLogs(context);
            ApplicationPreferences.applicationWidgetIconLightnessB(context);
            ApplicationPreferences.applicationWidgetIconLightnessT(context);
            ApplicationPreferences.applicationEventUsePriority(context);
            ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context);
            ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context);
            //ApplicationPreferences.applicationSamsungEdgePrefIndicator(context);
            ApplicationPreferences.applicationSamsungEdgeHeader(context);
            ApplicationPreferences.applicationSamsungEdgeBackground(context);
            ApplicationPreferences.applicationSamsungEdgeLightnessB(context);
            ApplicationPreferences.applicationSamsungEdgeLightnessT(context);
            ApplicationPreferences.applicationSamsungEdgeIconColor(context);
            ApplicationPreferences.applicationSamsungEdgeIconLightness(context);
            //ApplicationPreferences.applicationSamsungEdgeGridLayout(context);
            ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationRestartEventsWithAlert(context);
            ApplicationPreferences.applicationWidgetListRoundedCorners(context);
            ApplicationPreferences.applicationWidgetIconRoundedCorners(context);
            ApplicationPreferences.applicationWidgetListBackgroundType(context);
            ApplicationPreferences.applicationWidgetListBackgroundColor(context);
            ApplicationPreferences.applicationWidgetIconBackgroundType(context);
            ApplicationPreferences.applicationWidgetIconBackgroundColor(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundType(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundColor(context);
            //ApplicationPreferences.applicationEventWifiEnableWifi(context);
            //ApplicationPreferences.applicationEventBluetoothEnableBluetooth(context);
            ApplicationPreferences.applicationEventWifiScanIfWifiOff(context);
            ApplicationPreferences.applicationEventBluetoothScanIfBluetoothOff(context);
            ApplicationPreferences.applicationEventWifiEnableScanning(context);
            ApplicationPreferences.applicationEventBluetoothEnableScanning(context);
            ApplicationPreferences.applicationEventLocationEnableScanning(context);
            ApplicationPreferences.applicationEventMobileCellEnableScanning(context);
            ApplicationPreferences.applicationEventOrientationEnableScanning(context);
            ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventNeverAskForEnableRun(context);
            ApplicationPreferences.applicationUseAlarmClock(context);
            ApplicationPreferences.applicationNeverAskForGrantRoot(context);
            ApplicationPreferences.applicationNeverAskForGrantG1Permission(context);
            ApplicationPreferences.notificationShowButtonExit(context);
            ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context);
            ApplicationPreferences.applicationWidgetOneRowPrefIndicatorLightness(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessB(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessT(context);
            ApplicationPreferences.applicationWidgetOneRowIconColor(context);
            ApplicationPreferences.applicationWidgetOneRowIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowRoundedCorners(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundType(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundColor(context);
            ApplicationPreferences.applicationWidgetListLightnessBorder(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessBorder(context);
            ApplicationPreferences.applicationWidgetIconLightnessBorder(context);
            ApplicationPreferences.applicationWidgetListShowBorder(context);
            ApplicationPreferences.applicationWidgetOneRowShowBorder(context);
            ApplicationPreferences.applicationWidgetIconShowBorder(context);
            ApplicationPreferences.applicationWidgetListCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetIconCustomIconLightness(context);
            ApplicationPreferences.applicationSamsungEdgeCustomIconLightness(context);
            //ApplicationPreferences.notificationDarkBackground(context);
            ApplicationPreferences.notificationUseDecoration(context);
            ApplicationPreferences.notificationLayoutType(context);
            ApplicationPreferences.notificationBackgroundColor(context);
            //ApplicationPreferences.applicationNightModeOffTheme(context);
            ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(context);
            ApplicationPreferences.applicationSamsungEdgeVerticalPosition(context);
            ApplicationPreferences.notificationBackgroundCustomColor(context);
            //ApplicationPreferences.notificationNightMode(context);
            ApplicationPreferences.applicationEditorHideHeaderOrBottomBar(context);
            ApplicationPreferences.applicationWidgetIconShowProfileDuration(context);
            ApplicationPreferences.notificationNotificationStyle(context);
            ApplicationPreferences.notificationShowProfileIcon(context);
            ApplicationPreferences.applicationEventPeriodicScanningEnableScanning(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInterval(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventWifiScanIgnoreHotspot(context);
            ApplicationPreferences.applicationEventNotificationEnableScanning(context);
            ApplicationPreferences.applicationEventNotificationScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventNotificationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationWidgetOneRowRoundedCornersRadius(context);
            ApplicationPreferences.applicationWidgetListRoundedCornersRadius(context);
            ApplicationPreferences.applicationWidgetIconRoundedCornersRadius(context);
            ApplicationPreferences.applicationActivatorNumColums(context);
            ApplicationPreferences.applicationApplicationInterfaceNotificationSound(context);
            ApplicationPreferences.applicationApplicationInterfaceNotificationVibrate(context);
            ApplicationPreferences.applicationActivatorAddRestartEventsIntoProfileList(context);
            ApplicationPreferences.applicationActivatorIncreaseBrightness(context);
            ApplicationPreferences.applicationWidgetOneRowLayoutHeight(context);
            //ApplicationPreferences.applicationWidgetOneRowHigherLayout(context);
            ApplicationPreferences.applicationWidgetIconChangeColorsByNightMode(context);
            ApplicationPreferences.applicationWidgetOneRowChangeColorsByNightMode(context);
            ApplicationPreferences.applicationWidgetListChangeColorsByNightMode(context);
            ApplicationPreferences.applicationSamsungEdgeChangeColorsByNightMode(context);
            ApplicationPreferences.applicationForceSetBrightnessAtScreenOn(context);
            ApplicationPreferences.notificationProfileIconColor(context);
            ApplicationPreferences.notificationProfileIconLightness(context);
            ApplicationPreferences.notificationCustomProfileIconLightness(context);
            ApplicationPreferences.applicationShortcutIconColor(context);
            ApplicationPreferences.applicationShortcutIconLightness(context);
            ApplicationPreferences.applicationShortcutCustomIconLightness(context);
            ApplicationPreferences.notificationShowRestartEventsAsButton(context);
            ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile(context);
            ApplicationPreferences.applicationRestartEventsIconColor(context);
            //ApplicationPreferences.applicationIncreaseBrightnessForProfileIcon(context);
            ApplicationPreferences.applicationWidgetIconBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationWidgetIconBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationWidgetIconLayoutHeight(context);
            ApplicationPreferences.applicationWidgetIconFillBackground(context);
            ApplicationPreferences.applicationWidgetOneRowFillBackground(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListFillBackground(context);

            ApplicationPreferences.applicationWidgetOneRowProfileListLightnessB(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListIconColor(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCorners(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundType(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColor(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListLightnessBorder(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListShowBorder(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCornersRadius(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListLayoutHeight(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListChangeColorsByNightMode(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListArrowsMarkLightness(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage(context);

            ApplicationPreferences.notificationProfileListDisplayNotification(context);
            //ApplicationPreferences.notificationProfileListShowInStatusBar(context);
            //ApplicationPreferences.notificationProfileListHideInLockScreen(context);
            //ApplicationPreferences.notificationProfileListStatusBarStyle(context);
            ApplicationPreferences.notificationProfileListBackgroundColor(context);
            ApplicationPreferences.notificationProfileListBackgroundCustomColor(context);
            ApplicationPreferences.notificationProfileListPrefArrowsMarkLightness(context);
            ApplicationPreferences.notificationProfileListNumberOfProfilesPerPage(context);
            ApplicationPreferences.notificationProfileListIconColor(context);
            ApplicationPreferences.notificationProfileListIconLightness(context);
            ApplicationPreferences.notificationProfileListCustomIconLightness(context);
            ApplicationPreferences.applicationEventHideNotUsedSensors(context);
            //ApplicationPreferences.applicationContactsInBackupEncripted(context);
            ApplicationPreferences.applicationHyperOsWifiBluetoothDialogs(context);

            ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventLocationScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventLocationScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventLocationScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventMobileCellScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventNotificationScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventOrientationScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventWifiScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventWifiScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventWifiScanInTimeMultiply(context);

            // this must be called before of xxxBackground()
            ApplicationPreferences.applicationWidgetIconUseDynamicColors(context);
            ApplicationPreferences.applicationWidgetOneRowUseDynamicColors(context);
            ApplicationPreferences.applicationWidgetListUseDynamicColors(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListUseDynamicColors(context);
            ApplicationPreferences.applicationWidgetListUseDynamicColors(context);
            ApplicationPreferences.applicationWidgetOneRowPrefIndicatorUseDynamicColor(context);
            ApplicationPreferences.applicationWidgetListPrefIndicatorUseDynamicColor(context);

            // this must be called after of xxxUseDynamicColors()
            ApplicationPreferences.applicationWidgetIconBackground(context);
            ApplicationPreferences.applicationWidgetOneRowBackground(context);
            ApplicationPreferences.applicationWidgetListBackground(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackground(context);

            ApplicationPreferences.applicationEditorHideEventDetails(context);
            ApplicationPreferences.applicationEditorHideEventDetailsForStartOrder(context);

            ApplicationPreferences.deleteBadPreferences(context);
        }
    }

    static void loadProfileActivationData(Context context) {
        ActivateProfileHelper.getRingerVolume(context);
        ActivateProfileHelper.getNotificationVolume(context);
        ActivateProfileHelper.getRingerMode(context);
        ActivateProfileHelper.getZenMode(context);
        ActivateProfileHelper.getLockScreenDisabled(context);
        ActivateProfileHelper.getActivatedProfileScreenTimeoutWhenScreenOff(context);
        ActivateProfileHelper.getKeepScreenOnPermanent(context);
        ActivateProfileHelper.getMergedRingNotificationVolumes(context);
        //Profile.getActivatedProfileForDuration(context);
        ProfileStatic.getActivatedProfileEndDurationTime(context);
    }

    //--------------------------------------------------------------

    static boolean getApplicationStarted(boolean testService, boolean testExport)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.getApplicationStarted", "PPApplication.applicationStartedMutex");
        synchronized (PPApplication.applicationStartedMutex) {
            if (testService) {
                try {
                    return PPApplication.applicationStarted &&
                            ((!testExport) || (!PPApplication.exportIsRunning)) &&
                            (PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().getServiceHasFirstStart();
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return PPApplication.applicationStarted &&
                        ((!testExport) || (!PPApplication.exportIsRunning));
        }
    }

    static void setApplicationStarted(Context context, boolean appStarted)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.setApplicationStarted", "PPApplication.applicationStartedMutex");
        synchronized (PPApplication.applicationStartedMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PPApplication.PREF_APPLICATION_STARTED, appStarted);
            editor.apply();
            PPApplication.applicationStarted = appStarted;
        }
    }

    static boolean getApplicationStopping(Context context) {
        return ApplicationPreferences.
                getSharedPreferences(context).getBoolean(PPApplication.PREF_APPLICATION_STOPPING, false);
    }

    static void setApplicationStopping(Context context, boolean appStopping)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.setApplicationStopping", "PPApplication.applicationStartedMutex");
        synchronized (PPApplication.applicationStartedMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PPApplication.PREF_APPLICATION_STOPPING, appStopping);
            editor.apply();
        }
    }

    static int getSavedVersionCode(Context context) {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PPApplication.PREF_SAVED_VERSION_CODE, 0);
    }

    static void setSavedVersionCode(Context context, int version)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PPApplication.PREF_SAVED_VERSION_CODE, version);
        editor.apply();
    }

    private static void getActivityLogEnabled(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.getActivityLogEnabled", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            PPApplication.prefActivityLogEnabled = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PPApplication.PREF_ACTIVITY_LOG_ENABLED, true);
            //return prefActivityLogEnabled;
        }
    }
    static void setActivityLogEnabled(Context context, boolean enabled)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.setActivityLogEnabled", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PPApplication.PREF_ACTIVITY_LOG_ENABLED, enabled);
            editor.apply();
            PPApplication.prefActivityLogEnabled = enabled;
        }
    }

    /*
    static String prefNotificationProfileName;
    private static void getNotificationProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefNotificationProfileName = ApplicationPreferences.
                    getSharedPreferences(context).getString(PREF_NOTIFICATION_PROFILE_NAME, "");
            //return prefNotificationProfileName;
        }
    }
    static public void setNotificationProfileName(Context context, String notificationProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_NOTIFICATION_PROFILE_NAME, notificationProfileName);
            editor.apply();
            prefNotificationProfileName = notificationProfileName;
        }
    }
     */

    /*
    static String prefWidgetProfileName1;
    static String prefWidgetProfileName2;
    static String prefWidgetProfileName3;
    static String prefWidgetProfileName4;
    static String prefWidgetProfileName5;
    private static void getWidgetProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
            prefWidgetProfileName1 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_1", "");
            prefWidgetProfileName2 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_2", "");
            prefWidgetProfileName3 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_3", "");
            prefWidgetProfileName4 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_4", "");
            prefWidgetProfileName5 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_5", "");
            //return prefNotificationProfileName;
        }
    }
    static void setWidgetProfileName(Context context, int widgetType, String widgetProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_WIDGET_PROFILE_NAME + "_" + widgetType, widgetProfileName);
            editor.apply();
            switch (widgetType) {
                case 1:
                    prefWidgetProfileName1 = widgetProfileName;
                    break;
                case 2:
                    prefWidgetProfileName2 = widgetProfileName;
                    break;
                case 3:
                    prefWidgetProfileName3 = widgetProfileName;
                    break;
                case 4:
                    prefWidgetProfileName4 = widgetProfileName;
                    break;
                case 5:
                    prefWidgetProfileName5 = widgetProfileName;
                    break;
            }
        }
    }

    static String prefActivityProfileName1;
    static String prefActivityProfileName2;
    static String prefActivityProfileName3;
    private static void getActivityProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
            prefActivityProfileName1 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_1", "");
            prefActivityProfileName2 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_2", "");
            prefActivityProfileName3 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_3", "");
            //return prefActivityProfileName;
        }
    }
    static void setActivityProfileName(Context context, int activityType, String activityProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_ACTIVITY_PROFILE_NAME + "_" + activityType, activityProfileName);
            editor.apply();
            switch (activityType) {
                case 1:
                    prefActivityProfileName1 = activityProfileName;
                    break;
                case 2:
                    prefActivityProfileName2 = activityProfileName;
                    break;
                case 3:
                    prefActivityProfileName3 = activityProfileName;
                    break;
            }
        }
    }
    */

    static void getLastActivatedProfile(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.getLastActivatedProfile", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            PPApplication.prefLastActivatedProfile = ApplicationPreferences.
                    getSharedPreferences(context).getLong(PPApplication.PREF_LAST_ACTIVATED_PROFILE, 0);
            //return prefLastActivatedProfile;
        }
    }
    static void setLastActivatedProfile(Context context, long profileId)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.setLastActivatedProfile", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(PPApplication.PREF_LAST_ACTIVATED_PROFILE, profileId);
            editor.apply();
//            PPApplication.prefLastActivatedProfile = profileId;
        }
    }
    static void getProfileBeforeActivation(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.geProfileBeforeActivation", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            PPApplication.prefProfileBeforeActivation = DatabaseHandler.getInstance(context).getActivatedProfileId();
        }
    }

    private static void getWallpaperChangeTime(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.getWallpaperChangeTime", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            PPApplication.wallpaperChangeTime = ApplicationPreferences.
                    getSharedPreferences(context).getLong(PPApplication.PREF_WALLPAPER_CHANGE_TIME, 0);
            //return PPApplication.prefLastActivatedProfile;
        }
    }
    static void setWallpaperChangeTime(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic.setWallpaperChangeTime", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            Calendar now = Calendar.getInstance();
            long _time = now.getTimeInMillis();
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(PPApplication.PREF_WALLPAPER_CHANGE_TIME, _time);
            editor.apply();
            PPApplication.wallpaperChangeTime = _time;
        }
    }

    static int getDaysAfterFirstStart(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PPApplication.PREF_DAYS_AFTER_FIRST_START, 0);
    }
    static void setDaysAfterFirstStart(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PPApplication.PREF_DAYS_AFTER_FIRST_START, days);
        editor.apply();
    }

    static int getDonationNotificationCount(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PPApplication.PREF_DONATION_NOTIFICATION_COUNT, 0);
    }
    static void setDonationNotificationCount(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PPApplication.PREF_DONATION_NOTIFICATION_COUNT, days);
        editor.apply();
    }

    static int getDaysForNextDonationNotification(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PPApplication.PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, 0);
    }
    static void setDaysForNextDonationNotification(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PPApplication.PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, days);
        editor.apply();
    }

    static boolean getDonationDonated(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getBoolean(PPApplication.PREF_DONATION_DONATED, false);
    }
    static void setDonationDonated(Context context, boolean donated)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(PPApplication.PREF_DONATION_DONATED, donated);
        editor.apply();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isIgnoreBatteryOptimizationEnabled(Context appContext) {
        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        try {
            if (pm != null) {
                return pm.isIgnoringBatteryOptimizations(PPApplication.PACKAGE_NAME);
            }
        } catch (Exception ignore) {
            return false;
        }
        return false;
    }

    // --------------------------------

    // notification channels -------------------------

    static void createPPPAppNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.PROFILE_NOTIFICATION_CHANNEL) != null))
                    return;// true;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_activated_profile);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_activated_profile_description_ppp);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.PROFILE_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
                NotificationChannel newChannel = notificationManager.getNotificationChannel(PPApplication.PROFILE_NOTIFICATION_CHANNEL);

                if (newChannel == null)
                    throw new RuntimeException("PPApplication.createPPPAppNotificationChannel - NOT CREATED - newChannel=null");
            } catch (Exception e) {
                recordException(e);
            }
        //return true;
    }

    static void deleteOldMobileCellsRegistrationNotificationChannel(Context context) {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
            if (notificationManager.getNotificationChannel(PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL_OLD) != null)
                notificationManager.deleteNotificationChannel(PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL_OLD);
        } catch (Exception e) {
            recordException(e);
        }
    }
    static void createMobileCellsRegistrationNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL_SILENT) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_mobile_cells_registration_description);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL_SILENT, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createInformationNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.INFORMATION_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_information);
                // The user-visible description of the channel.
                String description = "";

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.INFORMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(false);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createExclamationNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_exclamation);
                // The user-visible description of the channel.
                String description = "";

                NotificationChannel channel = new NotificationChannel(PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                //channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                // must be onlu log, because this channel is used in ACRA
                Log.e("PPApplicationStatic.createExclamationNotificationChannel", Log.getStackTraceString(e));
            }
    }

    static void createGrantPermissionNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_grant_permission);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_grant_permission_description);

                NotificationChannel channel = new NotificationChannel(PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                //channel.setImportance(importance);
                channel.setDescription(description);
                channel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                //channel.setLightColor(ContextCompat.getColor(context, R.color.altype_error));
                channel.enableVibration(true);
                //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createNotifyEventStartNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.NOTIFY_EVENT_START_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_notify_event_start);
                // The user-visible description of the channel.
                String description =
                        context.getString(R.string.notification_channel_notify_event_start_description)+" "+
                        context.getString(R.string.notification_channel_notify_event_start_description_2);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.NOTIFY_EVENT_START_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(true);
                channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createMobileCellsNewCellNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_not_used_mobile_cell);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_not_used_mobile_cell_description);

                NotificationChannel channel = new NotificationChannel(PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                //channel.setImportance(importance);
                channel.setDescription(description);
                channel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                //channel.setLightColor(ContextCompat.getColor(context, R.color.altype_error));
                channel.enableVibration(true);
                //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createDonationNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.DONATION_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_donation);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_donation_description);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.DONATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(false);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createNewReleaseNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.NEW_RELEASE_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_new_release);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_new_release_description);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.NEW_RELEASE_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(false);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createGeneratedByProfileNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_generated_by_profile);
                // The user-visible description of the channel.
                String description =
                        context.getString(R.string.notification_channel_generated_by_profile_description) + " "+
                        context.getString(R.string.notification_channel_generated_by_profile_description_2);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createKeepScreenOnNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.KEEP_SCREEN_ON_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                String name = context.getString(R.string.profile_preferences_deviceScreenOnPermanent);

                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_keep_screen_on_description) +
                        " \"" + context.getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\".";

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.KEEP_SCREEN_ON_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                //channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createProfileListNotificationChannel(Context context, boolean forceChange) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if ((!forceChange) && (notificationManager.getNotificationChannel(PPApplication.PROFILE_LIST_NOTIFICATION_CHANNEL) != null))
                    return;

                // The user-visible name of the channel.
                String name = context.getString(R.string.notification_channel_profile_list);

                // The user-visible description of the channel.
                String description =
                        context.getString(R.string.notification_channel_profile_list_description) + " " +
                        context.getString(R.string.notification_channel_profile_list_description_2);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PPApplication.PROFILE_LIST_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_MIN);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(true);
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_SECRET);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                recordException(e);
            }
    }

    static void createNotificationChannels(Context appContext, boolean forceChange) {
        createDonationNotificationChannel(appContext, forceChange);
        createExclamationNotificationChannel(appContext, forceChange);
        createGeneratedByProfileNotificationChannel(appContext, forceChange);
        createGrantPermissionNotificationChannel(appContext, forceChange);
        createInformationNotificationChannel(appContext, forceChange);
        createKeepScreenOnNotificationChannel(appContext, forceChange);
        createMobileCellsNewCellNotificationChannel(appContext, forceChange);
        createMobileCellsRegistrationNotificationChannel(appContext, forceChange);
        deleteOldMobileCellsRegistrationNotificationChannel(appContext);
        createNewReleaseNotificationChannel(appContext, forceChange);
        createNotifyEventStartNotificationChannel(appContext, forceChange);
        createPPPAppNotificationChannel(appContext, forceChange);
        createProfileListNotificationChannel(appContext, forceChange);

        //createCrashReportNotificationChannel(appContext);
    }

    /*
    static void showProfileNotification() {
        try {
            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().showProfileNotification(false);

        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }
    */

    // -----------------------------------------------

    // scanners ------------------------------------------

    static void registerContentObservers(Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_CONTENT_OBSERVERS, true);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void registerCallbacks(Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_CALLBACKS, true);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    /** @noinspection SameParameterValue*/
    static void registerPhoneCallsListener(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_PHONE_CALLS_LISTENER, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_PHONE_CALLS_LISTENER, true);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartPeriodicScanningScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_PERIODIC_SCANNING_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_PERIODIC_SCANNING_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void forceRegisterReceiversForWifiScanner(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void reregisterReceiversForWifiScanner(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartWifiScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_WIFI_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_WIFI_SCANNER);
            runCommand(context, commandIntent);
//            PPApplicationStatic.logE("[BLUETOOTH] PPApplicationStatic.restartWifiScanner", "*******");
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void forceRegisterReceiversForBluetoothScanner(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void reregisterReceiversForBluetoothScanner(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartBluetoothScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BLUETOOTH_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_BLUETOOTH_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartLocationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_LOCATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_LOCATION_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartOrientationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ORIENTATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_ORIENTATION_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    /*
    public static void forceStartOrientationScanner(Context context) {
        try {
            //Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_ORIENTATION_SCANNER);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            //PPApplication.startPPService(context, serviceIntent);

            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_ORIENTATION_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }
    */

    static void forceStartMobileCellsScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_MOBILE_CELLS_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_FORCE_START_MOBILE_CELLS_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartMobileCellsScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_MOBILE_CELLS_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_MOBILE_CELLS_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartTwilightScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_TWILIGHT_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_TWILIGHT_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartNotificationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_NOTIFICATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_NOTIFICATION_SCANNER);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void restartAllScanners(Context context, boolean fromBatteryChange) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, PPApplication.SCANNER_RESTART_ALL_SCANNERS);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FROM_BATTERY_CHANGE, fromBatteryChange);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void rescanAllScanners(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_RESCAN_SCANNERS, true);
            runCommand(context, commandIntent);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void registerPPPExtenderReceiverForSMSCall(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER, true);
            runCommand(context, commandIntent);
//            Log.e("PPApplication.registerPPPExtenderReceiverForSMSCall", "xxx");
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void registerReceiversForCallSensor(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_FOR_CALL_SENSOR, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_FOR_CALL_SENSOR, true);
            runCommand(context, commandIntent);
//            Log.e("PPApplication.registerReceiversForCallSensor", "xxx");
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void registerReceiversForSMSSensor(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_FOR_SMS_SENSOR, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_FOR_SMS_SENSOR, true);
            runCommand(context, commandIntent);
//            Log.e("PPApplication.registerReceiversForSMSSensor", "xxx");
        } catch (Exception e) {
            recordException(e);
        }
    }

    /*
    static void registerReceiversForCallScreeningSensor(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_FOR_CALL_SCREENING_SENSOR, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_FOR_CALL_SCREENING_SENSOR, true);
            runCommand(context, commandIntent);
//            Log.e("PPApplication.registerReceiversForSMSSensor", "xxx");
        } catch (Exception e) {
            recordException(e);
        }
    }
    */

/*
    public static void restartEvents(Context context, boolean unblockEventsRun, boolean reactivateProfile) {
        try {
//            Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
//            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
//            serviceIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
//            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
//            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
//            PPApplication.startPPService(context, serviceIntent);
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }
*/

    /*
    public static void stopSimulatingRingingCall(boolean disableInternalChnage, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (disableInternalChnage)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_STOP_SIMULATING_RINGING_CALL, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_STOP_SIMULATING_RINGING_CALL_NO_DISABLE_INTERNAL_CHANGE, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }
*/

    //---------------------------------------------------------------

    // others ------------------------------------------------------------------

    /*
    static boolean isScreenOn(PowerManager powerManager) {
        return powerManager.isInteractive();
    }
    */

    /*
    private static String getROMManufacturer() {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.product.brand");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (Exception ex) {
            Log.e("PPApplication.getROMManufacturer", "Unable to read sysprop ro.product.brand", ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (Exception e) {
                    Log.e("PPApplication.getROMManufacturer", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
    */

    private static void _exitApp(final Context context, final DataWrapper dataWrapper, final Activity activity,
                               final boolean shutdown, final boolean removeNotifications, final boolean exitByUser) {
        try {
            PPApplicationStatic.logE("PPApplication._exitApp", "shutdown="+shutdown);

            if (!shutdown)
                cancelAllWorks(/*false*/);

            if (dataWrapper != null)
                dataWrapper.stopAllEvents(false, false, false, false);

            if (!shutdown) {
                // clear cahches
//                PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic._exitApp", "PPApplication.applicationCacheMutex");
                synchronized (PPApplication.applicationCacheMutex) {
                    if (PPApplication.applicationsCache != null) {
                        PPApplication.applicationsCache.cancelCaching();
                        PPApplication.applicationsCache.clearCache(true);
                    }
                    PPApplication.applicationsCache = null;
                }
//                PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic._exitApp", "PPApplication.contactsCacheMutex");
                synchronized (PPApplication.contactsCacheMutex) {
                    if (PPApplication.contactGroupsCache != null) {
                        PPApplication.contactGroupsCache.clearCache();
                    }
                    PPApplication.contactGroupsCache = null;
                    if (PPApplication.contactsCache != null) {
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] PPApplicationStatic._exitApp", "(in contactsCacheMutex) contactsCache.clearCache()");
                        PPApplication.contactsCache.clearCache();
                    }
                    PPApplication.contactsCache = null;
                }

                // remove notifications
                ImportantInfoNotification.removeNotification(context);
                DrawOverAppsPermissionNotification.removeNotification(context);
                IgnoreBatteryOptimizationNotification.removeNotification(context);
                DNDPermissionNotification.removeNotification(context);
                AutostartPermissionNotification.removeNotification(context);
                Permissions.removeNotifications(context);
                ProfileListNotification.clearNotification(context);

                if (removeNotifications) {
                    if (dataWrapper != null) {
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                        //noinspection ForLoopReplaceableByForEach
                        for (Iterator<Profile> it = dataWrapper.profileList.iterator(); it.hasNext(); ) {
                            Profile profile = it.next();
                            try {
                                notificationManager.cancel(
                                        PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG + "_" + profile._id,
                                        PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id);
                                notificationManager.cancel(
                                        PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG,
                                        PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int) profile._id);
                            } catch (Exception e) {
                                recordException(e);
                            }
                        }
                    }
                    ActivateProfileHelper.cancelNotificationsForInteractiveParameters(context);
                }

                addActivityLog(context, PPApplication.ALTYPE_APPLICATION_EXIT, null, null, "");

                ActivateProfileHelper.removeKeepScreenOnView(context);

                //PPApplication.initRoot();

                if (dataWrapper != null) {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic._exitApp", "DataWrapper.profileList");
                    synchronized (dataWrapper.profileList) {
                        if (!dataWrapper.profileListFilled)
                            dataWrapper.fillProfileList(false, false);
                        for (Profile profile : dataWrapper.profileList)
                            ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, context);
                    }

//                    PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic._exitApp", "DataWrapper.eventList");
                    synchronized (dataWrapper.eventList) {
                        if (!dataWrapper.eventListFilled)
                            dataWrapper.fillEventList();
                        for (Event event : dataWrapper.eventList)
                            StartEventNotificationBroadcastReceiver.removeAlarm(event, context);
                    }
                }


                //Profile.setActivatedProfileForDuration(context, 0);
                if (dataWrapper != null) {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] PPApplicationStatic._exitApp", "PPApplication.profileActivationMutex");
                    synchronized (PPApplication.profileActivationMutex) {
                        List<String> activateProfilesFIFO = new ArrayList<>();
                        dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                    }
                }
            }

            LocationScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(context);

            PPApplicationStatic.logE("PPApplication._exitApp", "stop service");
            //PhoneProfilesService.getInstance().showProfileNotification(false);
            //context.stopService(new Intent(context, PhoneProfilesService.class));
            //if (PhoneProfilesService.getInstance() != null)
            //    PhoneProfilesService.getInstance().setApplicationFullyStarted(false, false);

            Permissions.setAllShowRequestPermissions(context, true);

            //WifiBluetoothScanner.setShowEnableLocationNotification(context, true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
            //WifiBluetoothScanner.setShowEnableLocationNotification(context, true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
            //MobileCellsScanner.setShowEnableLocationNotification(context, true);
            //ActivateProfileHelper.setScreenUnlocked(context, true);

            if (!shutdown) {
                //ActivateProfileHelper.updateGUI(context, false, true);
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPApplication._exitApp", "call of forceUpdateGUI");
                PPApplication.forceUpdateGUI(context, false, false, false);

                final Handler _handler = new Handler(context.getMainLooper());
                final WeakReference<Activity> activityWeakRef = new WeakReference<>(activity);
                final Runnable r = () -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication._exitApp");
                    try {
                        Activity _activity = activityWeakRef.get();
                        if (_activity != null)
                            _activity.finish();
                    } catch (Exception e) {
                        recordException(e);
                    }
                };
                _handler.post(r);
                /*if (killProcess) {
                    Handler _handler = new Handler(context.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            android.os.Process.killProcess(PPApplication.pid);
                        }
                    };
                    _handler.postDelayed(r, 1000);
                }*/
            }

            //workManagerInstance.pruneWork();

            PhoneProfilesService.stop(shutdown, context);

            PPApplicationStatic.logE("PPApplication._exitApp", "set application started = false");
            setApplicationStarted(context, false);

            PPApplicationStatic.logE("PPApplication._exitApp", "*********** exitByUser="+exitByUser);
            if (exitByUser) {
                //IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(appContext, true);
                SharedPreferences settings = ApplicationPreferences.getSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, false);

                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, false);
                editor.apply();

                ApplicationPreferences.applicationEventNeverAskForEnableRun(context);
                ApplicationPreferences.applicationNeverAskForGrantRoot(context);
                ApplicationPreferences.applicationNeverAskForGrantG1Permission(context);

                ApplicationPreferences.applicationStartOnBoot(context);
            }

        } catch (Exception e) {
            //Log.e("PPApplication._exitApp", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static void exitApp(final boolean useHandler, final Context context, final DataWrapper _dataWrapper, final Activity _activity,
                                 final boolean fromShutdown, final boolean removeNotifications, final boolean exitByUser) {
        try {
            if (useHandler) {
                final Context appContext = context.getApplicationContext();
                final WeakReference<Activity> activityWeakRef = new WeakReference<>(_activity);
                final WeakReference<DataWrapper> dataWrapperWeakRef = new WeakReference<>(_dataWrapper);
                Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPApplication.exitApp");

                    DataWrapper dataWrapper = dataWrapperWeakRef.get();
                    Activity activity = activityWeakRef.get();

                    //if ((appContext != null) && (dataWrapper != null) && (activity != null)) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPApplication_exitApp);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                            if ((activity != null) && (dataWrapper != null))
                                _exitApp(appContext, dataWrapper, activity, fromShutdown, removeNotifications, exitByUser);

                        } catch (Exception e) {
//                            Log.e("[IN_EXECUTOR] PPApplication.exitApp", Log.getStackTraceString(e));
                            recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}
                };
                createBasicExecutorPool();
                PPApplication.basicExecutorPool.submit(runnable);
            }
            else
                _exitApp(context, _dataWrapper, _activity, fromShutdown, removeNotifications, exitByUser);
        } catch (Exception e) {
            recordException(e);
        }
    }

    static void showDoNotKillMyAppDialog(final Activity activity) {
        if (activity != null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.phone_profiles_pref_applicationDoNotKillMyApp_dialogTitle);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_do_not_kill_my_app, null);
            dialogBuilder.setView(layout);

            DokiContentView doki = layout.findViewById(R.id.do_not_kill_my_app_dialog_dokiContentView);
            if (doki != null) {
                doki.setButtonsVisibility(false);
                doki.loadContent(Build.MANUFACTURER.toLowerCase().replace(" ", "-"));
            }

            AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

            if (!activity.isFinishing())
                dialog.show();
        }

    }

    static void createBasicExecutorPool() {
        if (PPApplication.basicExecutorPool == null)
            PPApplication.basicExecutorPool = Executors.newCachedThreadPool();
    }
    /*
    static void createProfileActiationExecutorPool() {
        if (PPApplication.profileActiationExecutorPool == null)
            PPApplication.profileActiationExecutorPool = Executors.newCachedThreadPool();
    }
    */
    static void createActivateProfileExecuteExecutorPool() {
        if (PPApplication.activateProfileExecuteExecutorPool == null)
            PPApplication.activateProfileExecuteExecutorPool = Executors.newCachedThreadPool();
    }
    /*
    static void createSoundModeExecutorPool() {
        if (PPApplication.soundModeExecutorPool == null)
            PPApplication.soundModeExecutorPool = Executors.newCachedThreadPool();
    }
    */
    static void createProfileVolumesExecutorPool() {
        if (PPApplication.profileVolumesExecutorPool == null)
            PPApplication.profileVolumesExecutorPool = Executors.newCachedThreadPool();
    }
    static void createProfileRadiosExecutorPool() {
        if (PPApplication.profileRadiosExecutorPool == null)
            PPApplication.profileRadiosExecutorPool = Executors.newCachedThreadPool();
    }
    static void createProfileRunApplicationsExecutorPool() {
        if (PPApplication.profileRunApplicationsExecutorPool == null)
            PPApplication.profileRunApplicationsExecutorPool = Executors.newCachedThreadPool();
    }
    static void createProfileIteractivePreferencesExecutorPool() {
        if (PPApplication.profileIteractivePreferencesExecutorPool == null)
            PPApplication.profileIteractivePreferencesExecutorPool = Executors.newCachedThreadPool();
    }
    static void createProfileActivationDurationExecutorPool() {
        if (PPApplication.profileActivationDurationExecutorPool == null)
            PPApplication.profileActivationDurationExecutorPool = Executors.newCachedThreadPool();
    }
    static void createEventsHandlerExecutor() {
        if (PPApplication.eventsHandlerExecutor == null)
            PPApplication.eventsHandlerExecutor = Executors.newCachedThreadPool();
    }
    static void createScannersExecutor() {
        if (PPApplication.scannersExecutor == null)
            PPApplication.scannersExecutor = Executors.newCachedThreadPool();
    }
    static void createPlayToneExecutor() {
        if (PPApplication.playToneExecutor == null)
            PPApplication.playToneExecutor = Executors.newSingleThreadExecutor();
    }
    static void createNonBlockedExecutor() {
        if (PPApplication.disableInternalChangeExecutor == null)
            PPApplication.disableInternalChangeExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedGuiExecutor() {
        if (PPApplication.delayedGuiExecutor == null)
            PPApplication.delayedGuiExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedAppNotificationExecutor() {
        if (PPApplication.delayedAppNotificationExecutor == null)
            PPApplication.delayedAppNotificationExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedProfileListNotificationExecutor() {
        if (PPApplication.delayedProfileListNotificationExecutor == null)
            PPApplication.delayedProfileListNotificationExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedEventsHandlerExecutor() {
        if (PPApplication.delayedEventsHandlerExecutor == null)
            PPApplication.delayedEventsHandlerExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedProfileActivationExecutor() {
        if (PPApplication.delayedProfileActivationExecutor == null)
            PPApplication.delayedProfileActivationExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createUpdateGuiExecutor() {
        if (PPApplication.updateGuiExecutor == null)
            PPApplication.updateGuiExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    static void startHandlerThreadBroadcast(/*String from*/) {
        if (PPApplication.handlerThreadBroadcast == null) {
            PPApplication.handlerThreadBroadcast = new HandlerThread("PPHandlerThreadBroadcast", THREAD_PRIORITY_MORE_FAVORABLE); //);
            PPApplication.handlerThreadBroadcast.start();
        }
    }

    static void startHandlerThreadOrientationScanner() {
        if (PPApplication.handlerThreadOrientationScanner == null) {
            PPApplication.handlerThreadOrientationScanner = new OrientationScannerHandlerThread("PPHandlerThreadOrientationScanner", THREAD_PRIORITY_MORE_FAVORABLE); //);
            PPApplication.handlerThreadOrientationScanner.start();
            if (PPApplication.proximitySensor != null)
                PPApplication.handlerThreadOrientationScanner.maxProximityDistance = PPApplication.proximitySensor.getMaximumRange();
            if (PPApplication.lightSensor != null)
                PPApplication.handlerThreadOrientationScanner.maxLightDistance = PPApplication.lightSensor.getMaximumRange();
        }
    }

    static void startHandlerThreadLocation() {
        if (PPApplication.handlerThreadLocation == null) {
            PPApplication.handlerThreadLocation = new HandlerThread("PPHandlerThreadLocation", THREAD_PRIORITY_MORE_FAVORABLE); //);
            PPApplication.handlerThreadLocation.start();
        }
    }

    static void setBlockProfileEventActions(boolean enable) {
        // if blockProfileEventActions = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
        PPApplication.blockProfileEventActions = enable;
        if (enable) {
            PPExecutors.scheduleDisableBlockProfileEventActionExecutor();
        }
        //else {
        //    cancelWork(DisableBlockProfileEventActionWorker.WORK_TAG, false);
        //}
    }

/*    static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;

        PPHandlerThreadRunnable(Context appContext) {
            this.appContextWeakRef = new WeakReference<>(appContext);
        }

    }*/

/*    private static abstract class ExitAppRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<DataWrapper> dataWrapperWeakRef;
        final WeakReference<Activity> activityWeakRef;

        ExitAppRunnable(Context appContext, DataWrapper dataWrapper, Activity activity) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.dataWrapperWeakRef = new WeakReference<>(dataWrapper);
            this.activityWeakRef = new WeakReference<>(activity);
        }

    }*/

    // Sensor manager ------------------------------------------------------------------------------

    static Sensor getAccelerometerSensor(Context context) {
        if (PPApplication.sensorManager == null)
            PPApplication.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (PPApplication.sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return PPApplication.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else
            return null;
    }

    static Sensor getMagneticFieldSensor(Context context) {
        if (PPApplication.sensorManager == null)
            PPApplication.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (PPApplication.sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return PPApplication.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else
            return null;
    }

    static Sensor getProximitySensor(Context context) {
        if (PPApplication.sensorManager == null)
            PPApplication.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (PPApplication.sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return PPApplication.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        else
            return null;
    }

    /*
    private Sensor getOrientationSensor(Context context) {
        synchronized (PPApplication.orientationScannerMutex) {
            if (mOrientationSensorManager == null)
                mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
    }*/

    static Sensor getLightSensor(Context context) {
        if (PPApplication.sensorManager == null)
            PPApplication.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (PPApplication.sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return PPApplication.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        else
            return null;
    }

    // application cache -----------------

    static void createApplicationsCache(boolean clear)
    {
        if (clear) {
            if (PPApplication.applicationsCache != null) {
                PPApplication.applicationsCache.clearCache(true);
            }
            PPApplication.applicationsCache = null;
        }
        if (PPApplication.applicationsCache == null)
            PPApplication.applicationsCache =  new ApplicationsCache();
    }

    static ApplicationsCache getApplicationsCache()
    {
        return PPApplication.applicationsCache;
    }

    // contacts and contact groups cache -----------------

    static void createContactsCache(Context context, boolean clear, boolean fixEvents/*, boolean forceCache*/)
    {
        if (clear) {
            if (PPApplication.contactsCache != null) {
//                PPApplicationStatic.logE("[CONTACTS_CACHE] PPApplicationStatic.createContactsCache", "contactsCache.clearCache()");
                PPApplication.contactsCache.clearCache();
            }
        }
        if (PPApplication.contactsCache == null)
            PPApplication.contactsCache = new ContactsCache();
//        PPApplicationStatic.logE("[CONTACTS_CACHE] PPApplicationStatic.createContactsCache", "contactsCache.getContactList()");
        PPApplication.contactsCache.getContactList(context, fixEvents/*, forceCache*/);
    }

    static ContactsCache getContactsCache()
    {
        return PPApplication.contactsCache;
    }

    static void createContactGroupsCache(Context context, boolean clear/*, boolean fixEvents*//*, boolean forceCache*/)
    {
        if (clear) {
            if (PPApplication.contactGroupsCache != null) {
//                PPApplicationStatic.logE("[CONTACTS_CACHE] PPApplicationStatic.createContactGroupsCache", "contactGroupsCache.clearCache()");
                PPApplication.contactGroupsCache.clearCache();
                }
        }
        if (PPApplication.contactGroupsCache == null)
            PPApplication.contactGroupsCache = new ContactGroupsCache();
//        PPApplicationStatic.logE("[CONTACTS_CACHE] PPApplicationStatic.createContactGroupsCache", "contactGroupsCache.getContactGroupList()");
        PPApplication.contactGroupsCache.getContactGroupList(context/*, fixEvents*//*, forceCache*/);
    }

    static ContactGroupsCache getContactGroupsCache()
    {
        return PPApplication.contactGroupsCache;
    }

    // check if Pixel Launcher is default --------------------------------------------------

    static boolean isPixelLauncherDefault(Context context) {
        if (Build.VERSION.SDK_INT >= 31) {
            if (context != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    ResolveInfo defaultLauncher;
                    //if (Build.VERSION.SDK_INT < 33)
                    //noinspection deprecation
                    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    //else
                    //    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY));
                    return (defaultLauncher == null) ||
                            defaultLauncher.activityInfo.packageName.toLowerCase().contains(
                                "com.google.android.apps.nexuslauncher");
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
            return false;
    }

    // check if One UI 4 Samsung Launcher is default --------------------------------------------------

    static boolean isOneUILauncherDefault(Context context) {
        if (Build.VERSION.SDK_INT >= 31) {
            if (context != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);

                    //ResolveInfo defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

                    ResolveInfo defaultLauncher;
                    //if (Build.VERSION.SDK_INT < 33)
                    //noinspection deprecation
                    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    //else
                    //    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY));

                    return (defaultLauncher == null) ||
                            defaultLauncher.activityInfo.packageName.toLowerCase().contains(
                            "com.sec.android.app.launcher");
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
            return false;
    }

    // check if One UI 4 Samsung Launcher is default --------------------------------------------------

    static boolean isMIUILauncherDefault(Context context) {
        if (Build.VERSION.SDK_INT >= 31) {
            if (context != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);

                    //ResolveInfo defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

                    ResolveInfo defaultLauncher;
                    //if (Build.VERSION.SDK_INT < 33)
                    //noinspection deprecation
                    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    //else
                    //    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY));

                    //Log.e("PPApplication.isMIUILauncherDefault", "defaultLauncher="+defaultLauncher);
                    return (defaultLauncher == null) ||
                            defaultLauncher.activityInfo.packageName.toLowerCase().contains(
                            "com.miui.home");
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
            return false;
    }

    // get PPP version from relases.md ----------------------------------------------

    static PPPReleaseData getReleaseData(String contents, boolean forceDoData, Context appContext) {
        // this must be added when you tests debug branch
//        if (DebugVersion.enabled)
//            contents = "@@@ppp-release:5.1.1.1b:6651:normal***@@@";
//

        boolean doData = false;
        try {
            PPPReleaseData pppReleaseData = new PPPReleaseData();

//            Log.e("PPApplicationStatic.getReleaseData", "contents="+contents);

            if (!contents.isEmpty()) {
                int startIndex = contents.indexOf("@@@ppp-release:");
                int endIndex = contents.indexOf("***@@@");
                if ((startIndex >= 0) && (endIndex > startIndex)) {
                    String version = contents.substring(startIndex, endIndex);
                    startIndex = version.indexOf(":");
                    if (startIndex != -1) {
                        version = version.substring(startIndex + 1);
                        String[] splits = version.split(":");
                        if (splits.length >= 2) {
                            int versionCode = 0;
                            try {
                                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                                versionCode = getVersionCode(pInfo);
                            } catch (Exception ignored) {
                            }
                            pppReleaseData.versionNameInReleases = splits[0];
                            pppReleaseData.versionCodeInReleases = Integer.parseInt(splits[1]);
//                            Log.e("PPApplicationStatic.getReleaseData", "pppReleaseData.versionCode="+versionCode);
//                            Log.e("PPApplicationStatic.getReleaseData", "pppReleaseData.versionNameInReleases="+pppReleaseData.versionNameInReleases);
//                            Log.e("PPApplicationStatic.getReleaseData", "pppReleaseData.versionCodeInReleases="+pppReleaseData.versionCodeInReleases);
//                            Log.e("PPApplicationStatic.getReleaseData", "ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification="+ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification);
                            if (forceDoData)
                                doData = true;
                            else {
                                if (ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification < pppReleaseData.versionCodeInReleases) {
                                    // not disabled notification about new version by user (not click to notification "Disable" button)
                                    if ((versionCode > 0) && (versionCode < pppReleaseData.versionCodeInReleases))
                                        doData = true;
                                }
                            }
                        }
                        /*if (splits.length == 2) {
                            // old check, always critical update
                            //critical = true;
                        }*/
                        if (splits.length == 3) {
                            // new, better check
                            // last parameter:
                            //  "normal" - normal update
                            //  "critical" - critical update
                            pppReleaseData.critical = splits[2].equals("critical");
                        }
//                        Log.e("PPApplicationStatic.getReleaseData", "pppReleaseData.critical="+pppReleaseData.critical);
//                        Log.e("PPApplicationStatic.getReleaseData", "doData="+doData);
                    }
                }
            }

            if (doData)
                return pppReleaseData;
            else
                return null;
        } catch (Exception e) {
//            Log.e("PPApplication.getReleaseData", Log.getStackTraceString(e));
            return null;
        }
    }

    // ACRA -------------------------------------------------------------------------

    static void recordException(Throwable ex) {
        try {
            if (CustomACRAReportingAdministrator.isRecordedException(ex, null)) {
                //FirebaseCrashlytics.getInstance().recordException(ex);
                ACRA.getErrorReporter().handleException(ex);
                //ACRA.getErrorReporter().putCustomData("NON-FATAL_EXCEPTION", Log.getStackTraceString(ex));
            }
        } catch (Exception ignored) {}
    }

    /*
    static void logToACRA(String s) {
        try {
            //FirebaseCrashlytics.getInstance().log(s);
            ACRA.getErrorReporter().putCustomData("Log", s);
        } catch (Exception ignored) {}
    }
    */

    static void setCustomKey(String key, int value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    static void setCustomKey(String key, String value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, value);
        } catch (Exception ignored) {}
    }

    static void setCustomKey(String key, boolean value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    /*
    static void logAnalyticsEvent(Context context, String itemId, String itemName, String contentType) {
        try {
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } catch (Exception e) {
            //recordException(e);
        }
    }
    */

    //---------------------------------------------------------------------------------------------

}
