package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;

import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PPExtenderBroadcastReceiver extends BroadcastReceiver {

    private static final String EXTRA_PACKAGE_NAME = PPApplication.PACKAGE_NAME_EXTENDER + ".package_name";
    private static final String EXTRA_CLASS_NAME = PPApplication.PACKAGE_NAME_EXTENDER + ".class_name";

    private static final String EXTRA_ORIGIN = PPApplication.PACKAGE_NAME_EXTENDER + ".origin";
    private static final String EXTRA_TIME = PPApplication.PACKAGE_NAME_EXTENDER + ".time";
    private static final String EXTRA_SUBSCRIPTION_ID = PPApplication.PACKAGE_NAME_EXTENDER + ".subscription_id";

    //private static final String EXTRA_SERVICE_PHONE_EVENT = PPApplication.PACKAGE_NAME_EXTENDER + ".service_phone_event";
    private static final String EXTRA_CALL_EVENT_TYPE = PPApplication.PACKAGE_NAME_EXTENDER + ".call_event_type";
    private static final String EXTRA_PHONE_NUMBER = PPApplication.PACKAGE_NAME_EXTENDER + ".phone_number";
    private static final String EXTRA_EVENT_TIME = PPApplication.PACKAGE_NAME_EXTENDER + ".event_time";
    private static final String EXTRA_SIM_SLOT = PPApplication.PACKAGE_NAME_EXTENDER + ".sim_slot";

    static final String EXTRA_DISPLAY_NOTIFICATION = "EXTRA_DISPLAY_NOTIFICATION";


    private static final String PREF_APPLICATION_IN_FOREGROUND = "application_in_foreground";

    private static final int ACCESSIBILITY_SERVICE_CONNECTED_DELAY = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] PPExtenderBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

//        PPApplicationStatic.logE("[IN_BROADCAST] PPExtenderBroadcastReceiver.onReceive", intent.getAction());

        final Context appContext = context.getApplicationContext();

        switch (intent.getAction()) {
            case PPApplication.ACTION_PPPEXTENDER_STARTED:
                isAccessibilityServiceEnabled(appContext, true, true
                        /*, "PPExtenderBroadcastReceiver.onReceive (ACTION_PPPEXTENDER_STARTED)"*/);
                break;
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED:
                if (PPApplication.accessibilityServiceForPPPExtenderConnected != 1) {
                    // cancel ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG
                    PPApplicationStatic._cancelWork(MainWorker.ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG, false);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(appContext);
                    notificationManager.cancel(
                            PPApplication.EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION_TAG,
                            PPApplication.EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION_ID);

                    PPApplication.accessibilityServiceForPPPExtenderConnected = 1;
                    //PPApplication.startHandlerThreadBroadcast(/*"PPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED"*/);
                    //final Handler __handler0 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //__handler0.post(new PPApplication.PPHandlerThreadRunnable(
                    //        context.getApplicationContext()) {
                    //__handler0.post(() -> {
                    Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED");

                        //Context appContext= appContextWeakRef.get();
                        //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if (PhoneProfilesService.getInstance() != null) {
                                DataWrapper dataWrapper2 = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                                dataWrapper2.fillEventList();
                                //dataWrapper2.fillProfileList(false, false);
                                PhoneProfilesServiceStatic.registerPPPExtenderReceiver(true, dataWrapper2, appContext);
                                PPApplicationStatic.restartAllScanners(appContext, false);

                                PPApplicationStatic.addActivityLog(dataWrapper2.context, PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_ENABLED,
                                        null, null, "");

                                dataWrapper2.restartEventsWithDelay(false, true, false, PPApplication.ALTYPE_UNDEFINED);
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
                    PPApplicationStatic.createEventsHandlerExecutor();
                    PPApplication.eventsHandlerExecutor.submit(runnable);
                }
                break;
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND:
                // cancel ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG
                PPApplicationStatic._cancelWork(MainWorker.ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG, false);

                PPApplication.accessibilityServiceForPPPExtenderConnected = 2;

                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_UNBIND,
                        null, null, "");

                //PPApplication.startHandlerThreadBroadcast(/*"PPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND"*/);
                //final Handler __handler1 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler1.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                //__handler1.post(() -> {
                Runnable runnable2 = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_ACCESSIBILITY_SERVICE_UNBIND);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        setApplicationInForeground(appContext, "");

                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(new int[]{
                                EventsHandler.SENSOR_TYPE_APPLICATION,
                                EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION});

                        /*
                        boolean applicationsAllowed = false;
                        boolean orientationAllowed = false;

                        DataWrapper dataWrapper4 = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                        dataWrapper4.fillEventList();
                        boolean applicationExists = dataWrapper4.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION);
                        if (applicationExists)
                            applicationsAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED);
                        boolean orientationExists = dataWrapper4.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION);
                        if (orientationExists)
                            orientationAllowed = (EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED);
                        dataWrapper4.invalidateDataWrapper();

                        if ((applicationsAllowed) || (orientationAllowed)) {
                            setApplicationInForeground(appContext, "");

                            if (EventStatic.getGlobalEventsRunning(appContext)) {
                                //DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);

                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                if (applicationExists) {
//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_APPLICATION (2)");
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);
                                }
                                if (orientationExists) {
//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_ORIENTATION (2)");
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);
                                }
                            }
                        }
                        */

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
                PPApplicationStatic.createEventsHandlerExecutor();
                PPApplication.eventsHandlerExecutor.submit(runnable2);

                break;
            case PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED:
                final String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                final String className = intent.getStringExtra(EXTRA_CLASS_NAME);
                //Log.e("PPExtenderBroadcastReceiver.onReceive", "(1) ACTION_FOREGROUND_APPLICATION_CHANGED packageName="+packageName);
                //Log.e("PPExtenderBroadcastReceiver.onReceive", "(1) ACTION_FOREGROUND_APPLICATION_CHANGED className="+className);

                try {
                    ComponentName componentName = new ComponentName(packageName, className);

                    ActivityInfo activityInfo = tryGetActivity(appContext, componentName);
                    boolean isActivity = activityInfo != null;
                    if (isActivity) {
                        setApplicationInForeground(appContext, packageName);

                        if (EventStatic.getGlobalEventsRunning(appContext)) {
                            //PPApplication.startHandlerThreadBroadcast(/*"PPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED"*/);
                            //final Handler __handler2 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                            //__handler2.post(new PPApplication.PPHandlerThreadRunnable(
                            //        context.getApplicationContext()) {
                            //__handler2.post(() -> {
                            Runnable runnable3 = () -> {
//                                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED");

                                //Context appContext= appContextWeakRef.get();
                                //if (appContext != null) {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_FOREGROUND_APPLICATION_CHANGED);
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        //Log.e("PPExtenderBroadcastReceiver.onReceive", "(2) ACTION_FOREGROUND_APPLICATION_CHANGED");
                                        EventsHandler eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(new int[]{
                                                EventsHandler.SENSOR_TYPE_APPLICATION,
                                                EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION});

                                        /*
                                        DataWrapper dataWrapper3 = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                                        dataWrapper3.fillEventList();
                                        //DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                                        EventsHandler eventsHandler = new EventsHandler(appContext);
                                        if (dataWrapper3.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION)) {
//                                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_APPLICATION (1)");
                                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);
                                        }
                                        if (dataWrapper3.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION)) {
//                                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_ORIENTATION (1)");
                                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);
                                        }
                                        dataWrapper3.invalidateDataWrapper();
                                        */

                                    } catch (Exception e) {
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
                            PPApplicationStatic.createEventsHandlerExecutor();
                            PPApplication.eventsHandlerExecutor.submit(runnable3);
                        }
                    }
                } catch (Exception e) {
                    //Log.e("PPExtenderBroadcastReceiver.onReceive", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                }
                break;
            case PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END:
                final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                if (profileId != 0) {
                    //PPApplication.startHandlerThreadBroadcast(/*"PPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END"*/);
                    //final Handler handler2 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //handler2.post(() -> {
                    Runnable runnable3 = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_FORCE_STOP_APPLICATIONS_END);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            Profile profile = DatabaseHandler.getInstance(appContext).getProfile(profileId, false);
                            if (profile != null) {
                                SharedPreferences sharedPreferences = appContext.getSharedPreferences(PPApplication.TMP_SHARED_PREFS_PPP_EXTENDER_BROADCAST_RECEIVER, Context.MODE_PRIVATE);
                                profile.saveProfileToSharedPreferences(sharedPreferences);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "call of ActivateProfileHelper.executeForInteractivePreferences");
                                ActivateProfileHelper.executeForInteractivePreferences(profile, appContext, sharedPreferences);
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
                    }; //);
                    PPApplicationStatic.createProfileActiationExecutorPool();
                    PPApplication.profileActiationExecutorPool.submit(runnable3);
                }
                break;
            case PPApplication.ACTION_SMS_MMS_RECEIVED:
                final String origin = intent.getStringExtra(EXTRA_ORIGIN);
                final long time = intent.getLongExtra(EXTRA_TIME, 0);
                final int subscriptionId = intent.getIntExtra(EXTRA_SUBSCRIPTION_ID, -1);
//                PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionId="+subscriptionId);

                int _simSlot = 0;

                if (subscriptionId != -1) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
//                        PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "mSubscriptionManager != null");
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                            PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionList=" + subscriptionList);
                        } catch (SecurityException e) {
                            PPApplicationStatic.recordException(e);
                        }
                        if (subscriptionList != null) {
//                            PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionList.size()=" + subscriptionList.size());
                            int size = subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/
                            for (int i = 0; i < size; i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionInfo=" + subscriptionInfo);
                                if (subscriptionInfo != null) {
                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                    int _subscriptionId = subscriptionInfo.getSubscriptionId();
//                                    PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionId=" + subscriptionId);
                                    if (subscriptionId == _subscriptionId) {
                                        _simSlot = slotIndex + 1;
                                        break;
                                    }
                                }
//                                else
//                                    PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionInfo == null");
                            }
                        }
//                        else
//                            PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "subscriptionList == null");
                    }
//                    else
//                        PPApplicationStatic.logE("[DUAL_SIM] PPExtenderBroadcastReceiver.onReceive", "mSubscriptionManager == null");
                }

                final int simSlot = _simSlot;

                if (EventStatic.getGlobalEventsRunning(appContext)) {
                    //PPApplication.startHandlerThreadBroadcast(/*"PPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED"*/);
                    //final Handler handler3 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //handler3.post(() -> {
                    Runnable runnable3 = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_SMS_MMS_RECEIVED);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_SMS, false) > 0) {
//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_SMS");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.setEventSMSParameters(origin, time, simSlot);
                                eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_SMS});
                            //}

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
                    }; //);
                    PPApplicationStatic.createEventsHandlerExecutor();
                    PPApplication.eventsHandlerExecutor.submit(runnable3);
                }
                break;
            case PPApplication.ACTION_CALL_RECEIVED:
                //final int servicePhoneEvent = intent.getIntExtra(EXTRA_SERVICE_PHONE_EVENT, 0);
                final int callEventType = intent.getIntExtra(EXTRA_CALL_EVENT_TYPE, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                final String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                final long eventTime = intent.getLongExtra(EXTRA_EVENT_TIME, 0);
                final int slotIndex = intent.getIntExtra(EXTRA_SIM_SLOT, 0);

                if (EventStatic.getGlobalEventsRunning(appContext)) {
                    //PPApplication.startHandlerThreadBroadcast(/*"PPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED"*/);
                    //final Handler handler4 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //handler4.post(() -> {
                    Runnable runnable3 = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPPExtenderBroadcastReceiver_onReceive_ACTION_CALL_RECEIVED);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALL, false) > 0) {
//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_PHONE_CALL");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "callEventType="+callEventType);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "phoneNumber="+phoneNumber);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "eventTime="+eventTime);
//                                Log.e("PPExtenderBroadcastReceiver.onReceive", "slotIndex="+slotIndex);
                                eventsHandler.setEventCallParameters(/*servicePhoneEvent, */callEventType, phoneNumber, eventTime, slotIndex);
                                eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_PHONE_CALL});
                            //}

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
                    }; //);
                    PPApplicationStatic.createEventsHandlerExecutor();
                    PPApplication.eventsHandlerExecutor.submit(runnable3);
                }
                break;
        }
    }

    private ActivityInfo tryGetActivity(Context context, ComponentName componentName) {
        try {
            return context.getPackageManager().getActivityInfo(componentName, 0);
        } catch (Exception e) {
            return null;
        }
    }


    static boolean isAccessibilityServiceEnabled(Context context, boolean againCheckInDelay, boolean displayNotification
                                                 /*, String calledFrom*/) {
        boolean enabled = false;

        //int accessibilityEnabled = 0;
        final String service = PPApplication.EXTENDER_ACCESSIBILITY_PACKAGE_NAME + "/" + PPApplication.EXTENDER_ACCESSIBILITY_PACKAGE_NAME + ".PPPEAccessibilityService";

        // Do not use: it returns always 0 :-(
        /*try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }*/

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        //if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        PPApplication.accessibilityServiceForPPPExtenderConnected = 1;
                        return true;
                    }
                }
            }

        // ------ not enabled --------------------

        if (PPApplication.accessibilityServiceForPPPExtenderConnected != 0) {
            // not started delayed check
            PPApplication.accessibilityServiceForPPPExtenderConnected = 2;
        }

        if (againCheckInDelay) {

            if (PPApplication.accessibilityServiceForPPPExtenderConnected == 2) {
                // not started delayed check, start it

                PPApplication.accessibilityServiceForPPPExtenderConnected = 0;

                // send broadcast to Extender to get if Extender is connected
                //Intent _intent = new Intent(PPApplication.ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED);
                //context.sendBroadcast(_intent, PPApplication.PPP_EXTENDER_PERMISSION);

                Data workData = new Data.Builder()
                        .putBoolean(EXTRA_DISPLAY_NOTIFICATION, displayNotification)
                        .build();

                boolean enqueuedWork = false;

//                PPApplicationStatic.logE("[MAIN_WORKER_CALL] PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "xxxxxxxxxxxxxxxxxxxx");

                // work for check accessibility, when Extender do not send ACTION_ACCESSIBILITY_SERVICE_CONNECTED
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG)
                                .setInputData(workData)
                                .setInitialDelay(ACCESSIBILITY_SERVICE_CONNECTED_DELAY, TimeUnit.MINUTES)
                                .build();
                try {
                    if (PPApplicationStatic.getApplicationStarted(true, false)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {
                            workManager.enqueueUniqueWork(MainWorker.ACCESSIBILITY_SERVICE_CONNECTED_NOT_RECEIVED_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                            enqueuedWork = true;
                        }
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

                if (!enqueuedWork)
                    PPApplication.accessibilityServiceForPPPExtenderConnected = 2;
            }

            enabled = PPApplication.accessibilityServiceForPPPExtenderConnected == 0;

/*
                if (PPApplication.accessibilityServiceForPPPExtenderConnected > 0)
                    enabled = PPApplication.accessibilityServiceForPPPExtenderConnected == 1;
                else
                    enabled = true;
 */
        }

        return enabled;
    }

    static int isExtenderInstalled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_EXTENDER, PackageManager.MATCH_ALL);
            boolean installed = appInfo.enabled;
            if (installed) {
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                return PPApplicationStatic.getVersionCode(pInfo);
            }
            else {
                return 0;
            }
        }
        catch (Exception e) {
            // extender is not installed = package not found
            //Log.e("PPExtenderBroadcastReceiver.isExtenderInstalled", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return 0;
        }
    }

    static String getExtenderVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_EXTENDER, PackageManager.MATCH_ALL);
            boolean installed = appInfo.enabled;
            if (installed) {
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                return pInfo.versionName;
            }
            else {
                return "";
            }
        }
        catch (Exception e) {
            // extender is not installed = package not found
            //Log.e("PPExtenderBroadcastReceiver.getExtenderVersionName", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return "";
        }
    }

    static boolean isEnabled(Context context/*, int version*/, boolean displayNotification, boolean againCheckInDelay
                             /*, String calledFrom*/) {

        int extenderVersion = isExtenderInstalled(context);
        boolean enabled = false;
        //if ((version == -1) || (extenderVersion >= version)) // -1 => do not check version
        if (extenderVersion >= PPApplication.VERSION_CODE_EXTENDER_LATEST)
            enabled = isAccessibilityServiceEnabled(context, againCheckInDelay, displayNotification
                    /*, "PPExtenderBroadcastReceiver.isEnabled"*/);
        //return  (extenderVersion >= version) && enabled;
        return  (extenderVersion >= PPApplication.VERSION_CODE_EXTENDER_LATEST) && enabled;
    }

    static void getApplicationInForeground(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPExtenderBroadcastReceiver.getApplicationInForeground", "PPApplication.eventsRunMutex");
        synchronized (PPApplication.eventsRunMutex) {
            ApplicationPreferences.prefApplicationInForeground = ApplicationPreferences.
                    getSharedPreferences(context).getString(PREF_APPLICATION_IN_FOREGROUND, "");
            //return prefApplicationInForeground;
        }
    }
    static void setApplicationInForeground(Context context, String application)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PPExtenderBroadcastReceiver.setApplicationInForeground", "PPApplication.eventsRunMutex");
        synchronized (PPApplication.eventsRunMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_APPLICATION_IN_FOREGROUND, application);
            editor.apply();
            ApplicationPreferences.prefApplicationInForeground = application;
        }
    }

}
