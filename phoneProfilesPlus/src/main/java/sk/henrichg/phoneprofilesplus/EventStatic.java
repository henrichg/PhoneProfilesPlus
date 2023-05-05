package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

class EventStatic {

    static PreferenceAllowed isEventPreferenceAllowed(String preferenceKey, Context context)
    {
        Context appContext = context.getApplicationContext();

        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();

        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;

        //boolean checked = false;

        if (preferenceKey.equals(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED)) {
            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            return preferenceAllowed;
        }

        if (preferenceKey.equals(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_WIFI)
                // device has Wifi
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_BLUETOOTH) {
                // device has bluetooth
                if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH) &&
                    (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH_ADMIN)))
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                else {
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_bluetooth_permission);
                }
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED))
        {
            //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            /*else {
                PPApplication.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }*/
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED))
        {
            //if (PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext()))
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            //else
            //    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED))
        {
            boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
            //boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
            boolean hasProximity = PPApplication.proximitySensor != null;
            boolean hasLight = PPApplication.lightSensor != null;

            boolean enabled = hasAccelerometer;
            enabled = enabled || hasProximity || hasLight;

            if (enabled) {
                //if (PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext()))
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                //else
                //    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        GlobalUtils.HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);

        if (preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED) ||
                preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED)) {
                        boolean simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        if (simExists)
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (!Permissions.checkPhone(appContext)) {
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                            }
                            else
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    }
                    else
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_NFC)
                // device has nfc
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED) ||
                preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED)) {
                        boolean simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        if (simExists)
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (!Permissions.checkPhone(appContext)) {
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                            }
                            else
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    }
                    else
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED) ||
                preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED)) {
                        boolean simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        if (simExists)
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (!Permissions.checkPhone(appContext)) {
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                            }
                            else
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    }
                    else
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_LOCATION)
                // device has location
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        /*
        if (preferenceKey.equals(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED))
        {
            //if (android.os.Build.VERSION.SDK_INT >= 21)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            //else
            //    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;
        */

        if (preferenceKey.equals(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED) ||
                preferenceKey.equals(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED)) {
                        boolean simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        if (simExists)
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (!Permissions.checkPhone(appContext)) {
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                            }
                            else
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    }
                    else
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_WIFI)) {
            if (PPApplication.HAS_FEATURE_WIFI)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_BLUETOOTH)) {
            if (PPApplication.HAS_FEATURE_BLUETOOTH)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_SIM_ON_OFF) ||
                preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM) ||
                preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_MOBILE_DATA)/* ||
                preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NO_CHECK_SIM)*/) {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    //if (!preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NO_CHECK_SIM)) {
                        boolean simExists;
                        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM)) {
                            simExists = hasSIMCardData.hasSIM1 && hasSIMCardData.hasSIM2;
                        } else
                            simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        if (simExists)
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (!Permissions.checkPhone(appContext)) {
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                            }
                            else
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    //}
                    //else
                    //    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_GPS)) {
            if (PPApplication.HAS_FEATURE_LOCATION_GPS)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NFC)) {
            if (PPApplication.HAS_FEATURE_NFC)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //noinspection IfStatementWithIdenticalBranches
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_AIRPLANE_MODE)) {
            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            return preferenceAllowed;
        }

        /*
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED) ||
                preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED)) {
                        boolean simExists = GlobalUtils.hasSIMCard(context, 0);
                        if (simExists)
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (!Permissions.checkPhone(appContext)) {
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                            }
                            else
                                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        }
                    }
                    else
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        */

        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
        return preferenceAllowed;
    }

    static boolean getGlobalEventsRunning(Context context)
    {
        synchronized (PPApplication.globalEventsRunStopMutex) {
            if (Build.VERSION.SDK_INT >= 33) {
                try {
                    ActivityManager.RunningServiceInfo serviceInfo = GlobalUtils.getServiceInfo(context.getApplicationContext(), PhoneProfilesService.class);
                    if (serviceInfo == null) {
                        // service is not running
                        return false;
                    }
                } catch (Exception ignored) {}
            }
            return PPApplication.globalEventsRunStop;
        }
    }

    static void setGlobalEventsRunning(Context context, boolean globalEventsRunning)
    {
        synchronized (PPApplication.globalEventsRunStopMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(Event.PREF_GLOBAL_EVENTS_RUN_STOP, globalEventsRunning);
            editor.apply();
            PPApplication.globalEventsRunStop = globalEventsRunning;
        }
    }

    static boolean getEventsBlocked(Context context)
    {
        synchronized (PPApplication.eventsRunMutex) {
            //ApplicationPreferences.prefEventsBlocked = ApplicationPreferences.
            //        getSharedPreferences(context).getBoolean(PREF_EVENTS_BLOCKED, false);
            //return prefEventsBlocked;
            return ApplicationPreferences.getSharedPreferences(context).getBoolean(Event.PREF_EVENTS_BLOCKED, false);
        }
    }
    static void setEventsBlocked(Context context, boolean eventsBlocked)
    {
        synchronized (PPApplication.eventsRunMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(Event.PREF_EVENTS_BLOCKED, eventsBlocked);
            editor.apply();
            //ApplicationPreferences.prefEventsBlocked = eventsBlocked;
        }
    }

    static boolean getForceRunEventRunning(Context context)
    {
        synchronized (PPApplication.eventsRunMutex) {
            //ApplicationPreferences.prefForceRunEventRunning = ApplicationPreferences.
            //        getSharedPreferences(context).getBoolean(PREF_FORCE_RUN_EVENT_RUNNING, false);
            //return prefForceRunEventRunning;
            return ApplicationPreferences.getSharedPreferences(context).getBoolean(Event.PREF_FORCE_RUN_EVENT_RUNNING, false);
        }
    }
    static void setForceRunEventRunning(Context context, boolean forceRunEventRunning)
    {
        synchronized (PPApplication.eventsRunMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(Event.PREF_FORCE_RUN_EVENT_RUNNING, forceRunEventRunning);
            editor.apply();
            //ApplicationPreferences.prefForceRunEventRunning = forceRunEventRunning;
        }
    }

    static boolean runStopEvent(final DataWrapper dataWrapper,
                                final Event event,
                                final EditorActivity editor) {
        if (EventStatic.getGlobalEventsRunning(dataWrapper.context)) {
            // events are not globally stopped

            dataWrapper.getEventTimelineList(true);
            if (event.getStatusFromDB(dataWrapper.context) == Event.ESTATUS_STOP) {
                if (!EventsPrefsFragment.isRedTextNotificationRequired(event, false, dataWrapper.context)) {
                    // pause event
                    //IgnoreBatteryOptimizationNotification.showNotification(activityDataWrapper.context);

                    //final DataWrapper dataWrapper = activityDataWrapper;
                    //PPApplication.startHandlerThread();
                    //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                    //__handler.post(new RunStopEventRunnable(activityDataWrapper, event) {
                    //__handler.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.runStopEvent.1");

                        //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                        //Event event = eventWeakRef.get();

                        //if ((dataWrapper != null) && (event != null)) {
                        PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EditorEventListFragment_runStopEvent_1");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            synchronized (PPApplication.eventsHandlerMutex) {
                                event.pauseEvent(dataWrapper, false, false,
                                        false, true, null, false, false, true);
                            }

                        } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        //}
                    }; //);
                    PPApplicationStatic.createBasicExecutorPool();
                    PPApplication.basicExecutorPool.submit(runnable);

                }
                else {
                    if (editor != null)
                        GlobalGUIRoutines.showDialogAboutRedText(null, event, false, false, false, true, editor);
                    else
                        DataWrapperStatic.displayPreferencesErrorNotification(null, event, false, dataWrapper.context);
                    return false;
                }
            } else {
                // stop event

                //final DataWrapper dataWrapper = activityDataWrapper;
                //PPApplication.startHandlerThread();
                //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                //__handler.post(new RunStopEventRunnable(activityDataWrapper, event) {
                //__handler.post(() -> {
                Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.runStopEvent.2");

                    //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                    //Event event = eventWeakRef.get();

                    //if ((dataWrapper != null) && (event != null)) {
                    PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EditorEventListFragment_runStopEvent_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        synchronized (PPApplication.eventsHandlerMutex) {
                            event.stopEvent(dataWrapper, false, false,
                                    true, true, true); // activate return profile
                        }

                    } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    //}
                }; //);
                PPApplicationStatic.createBasicExecutorPool();
                PPApplication.basicExecutorPool.submit(runnable);

            }

            // redraw event list
            //updateListView(event, false, false, true, 0);
            if (editor != null)
                editor.redrawEventListFragment(event, EditorEventListFragment.EDIT_MODE_EDIT);

            // restart events
            //activityDataWrapper.restartEvents(false, true, true, true, true);
            dataWrapper.restartEventsWithRescan(true, false, true, false, true, false);

            /*Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.startPPService(activityDataWrapper.context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplicationStatic.runCommand(dataWrapper.context, commandIntent);

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG)
                            .setInitialDelay(30, TimeUnit.MINUTES)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplicationStatic.logE("[WORKER_CALL] EditorEventListFragment.runStopEvent", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

        } else {
            if (event.getStatusFromDB(dataWrapper.context) == Event.ESTATUS_STOP) {
                // pause event
                event.setStatus(Event.ESTATUS_PAUSE);
            } else {
                // stop event
                event.setStatus(Event.ESTATUS_STOP);
            }

            // update event in DB
            DatabaseHandler.getInstance(dataWrapper.context).updateEvent(event);

            // redraw event list
            //updateListView(event, false, false, true, 0);
            if (editor != null)
                editor.redrawEventListFragment(event, EditorEventListFragment.EDIT_MODE_EDIT);

            // restart events
            //activityDataWrapper.restartEvents(false, true, true, true, true);
            dataWrapper.restartEventsWithRescan(true, false, true, false, true, false);

            /*Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.startPPService(activityDataWrapper.context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplicationStatic.runCommand(dataWrapper.context, commandIntent);

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG)
                            .setInitialDelay(30, TimeUnit.MINUTES)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplicationStatic.logE("[WORKER_CALL] EditorEventListFragment.runStopEvent", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }
        return true;
    }

}

