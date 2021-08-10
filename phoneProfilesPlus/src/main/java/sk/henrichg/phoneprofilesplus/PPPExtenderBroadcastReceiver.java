package sk.henrichg.phoneprofilesplus;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class PPPExtenderBroadcastReceiver extends BroadcastReceiver {

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

    private static final String PREF_APPLICATION_IN_FOREGROUND = "application_in_foreground";


    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] PPPExtenderBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context.getApplicationContext(), "PPPExtenderBroadcastReceiver.onReceive", "ForegroundApplicationChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "action="+intent.getAction());

        final Context appContext = context.getApplicationContext();

        switch (intent.getAction()) {
            case PPApplication.ACTION_PPPEXTENDER_IS_RUNNING_ANSWER:
                PPApplication.accessibilityServiceForPPPExtenderConnected = true;
                break;
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED:
                PPApplication.accessibilityServiceForPPPExtenderConnected = true;
                PPApplication.startHandlerThreadBroadcast(/*"PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED"*/);
                final Handler __handler0 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler0.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                __handler0.post(new Runnable() {
                    @Override
                    public void run() {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED");

                        //Context appContext= appContextWeakRef.get();
                        //if (appContext != null) {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_ACCESSIBILITY_SERVICE_CONNECTED");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                if (PhoneProfilesService.getInstance() != null) {
                                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0f);
                                    dataWrapper.fillEventList();
                                    //dataWrapper.fillProfileList(false, false);
                                    PhoneProfilesService.getInstance().registerPPPPExtenderReceiver(true, dataWrapper);
                                }

                                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED");
                            } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplication.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        //}
                    }
                });
                break;
            case PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED:
                //PPApplication.logE("[TEST BATTERY] PPPExtenderBroadcastReceiver.onReceive", "ACTION_FOREGROUND_APPLICATION_CHANGED");

                final String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                final String className = intent.getStringExtra(EXTRA_CLASS_NAME);

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[TEST BATTERY] PPPExtenderBroadcastReceiver.onReceive", "packageName=" + packageName);
                    PPApplication.logE("[TEST BATTERY] PPPExtenderBroadcastReceiver.onReceive", "className=" + className);
                }*/

                try {
                    ComponentName componentName = new ComponentName(packageName, className);

                    ActivityInfo activityInfo = tryGetActivity(appContext, componentName);
                    boolean isActivity = activityInfo != null;
                    if (isActivity) {
                        setApplicationInForeground(appContext, packageName);

                        if (Event.getGlobalEventsRunning()) {
                            PPApplication.startHandlerThreadBroadcast(/*"PPPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED"*/);
                            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                            //        context.getApplicationContext()) {
                            __handler.post(new Runnable() {
                                @Override
                                public void run() {
//                                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED");

                                    //Context appContext= appContextWeakRef.get();
                                    //if (appContext != null) {
                                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                        PowerManager.WakeLock wakeLock = null;
                                        try {
                                            if (powerManager != null) {
                                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_FOREGROUND_APPLICATION_CHANGED");
                                                wakeLock.acquire(10 * 60 * 1000);
                                            }

                                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0f);
                                            dataWrapper.fillEventList();
                                            //DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);

                                            EventsHandler eventsHandler = new EventsHandler(appContext);
                                            if (dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION/*, false*/)) {
//                                            PPApplication.logE("[EVENTS_HANDLER_CALL] PPPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_APPLICATION (1)");
                                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);
                                            }
                                            if (dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/)) {
//                                            PPApplication.logE("[EVENTS_HANDLER_CALL] PPPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_ORIENTATION (1)");
                                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);
                                            }

                                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED");
                                        } catch (Exception e) {
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                            PPApplication.recordException(e);
                                        } finally {
                                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                                try {
                                                    wakeLock.release();
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                    //}
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    //Log.e("PPPExtenderBroadcastReceiver.onReceive", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                }
                break;
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND:
                //PPApplication.logE("[TEST BATTERY] PPPExtenderBroadcastReceiver.onReceive", "ACTION_ACCESSIBILITY_SERVICE_UNBIND");
                PPApplication.accessibilityServiceForPPPExtenderConnected = false;

                setApplicationInForeground(appContext, "");

                if (Event.getGlobalEventsRunning()) {
                    PPApplication.startHandlerThreadBroadcast(/*"PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND"*/);
                    final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                    //        context.getApplicationContext()) {
                    __handler.post(new Runnable() {
                        @Override
                        public void run() {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND");

                            //Context appContext= appContextWeakRef.get();
                            //if (appContext != null) {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_ACCESSIBILITY_SERVICE_UNBIND");
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0f);
                                    dataWrapper.fillEventList();
                                    //DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);

                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    if (dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION/*, false*/)) {
//                                    PPApplication.logE("[EVENTS_HANDLER_CALL] PPPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_APPLICATION (2)");
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);
                                    }
                                    if (dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/)) {
//                                    PPApplication.logE("[EVENTS_HANDLER_CALL] PPPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_ORIENTATION (2)");
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);
                                    }

                                    //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND");
                                } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                    PPApplication.recordException(e);
                                } finally {
                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                        try {
                                            wakeLock.release();
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            //}
                        }
                    });
                }
                break;
            case PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END:
                final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                if (profileId != 0) {
                    PPApplication.startHandlerThreadBroadcast(/*"PPPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END"*/);
                    final Handler handler2 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    handler2.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_FORCE_STOP_APPLICATIONS_END");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            Profile profile = DatabaseHandler.getInstance(appContext).getProfile(profileId, false);
                            if (profile != null) {
                                SharedPreferences sharedPreferences = appContext.getSharedPreferences("temp_pppExtenderBroadcastReceiver", Context.MODE_PRIVATE);
                                profile.saveProfileToSharedPreferences(sharedPreferences);
                                ActivateProfileHelper.executeForInteractivePreferences(profile, appContext, sharedPreferences);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END");
                        } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    });
                }
                break;
            case PPApplication.ACTION_SMS_MMS_RECEIVED:
                final String origin = intent.getStringExtra(EXTRA_ORIGIN);
                final long time = intent.getLongExtra(EXTRA_TIME, 0);
                final int subscriptionId = intent.getIntExtra(EXTRA_SUBSCRIPTION_ID, -1);
//                PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "subscriptionId="+subscriptionId);

                int _simSlot = 0;

                if (subscriptionId != -1) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
//                        PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "mSubscriptionManager != null");
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                            PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "subscriptionList=" + subscriptionList);
                        } catch (SecurityException e) {
                            PPApplication.recordException(e);
                        }
                        if (subscriptionList != null) {
//                            PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "subscriptionList.size()=" + subscriptionList.size());
                            for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "subscriptionInfo=" + subscriptionInfo);
                                if (subscriptionInfo != null) {
                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                    int _subscriptionId = subscriptionInfo.getSubscriptionId();
//                                    PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "subscriptionId=" + subscriptionId);
                                    if (subscriptionId == _subscriptionId) {
                                        _simSlot = slotIndex + 1;
                                        break;
                                    }
                                }
//                                else
//                                    PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "subscriptionInfo == null");
                            }
                        }
//                        else
//                            PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "subscriptionList == null");
                    }
//                    else
//                        PPApplication.logE("[DUAL_SIM] PPPExtenderBroadcastReceiver.onReceive", "mSubscriptionManager == null");
                }

                final int simSlot = _simSlot;

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "origin=" + origin);
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "time=" + time);
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "simSlot=" + simSlot);
                }*/

                if (Event.getGlobalEventsRunning()) {
                    PPApplication.startHandlerThreadBroadcast(/*"PPPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED"*/);
                    final Handler handler3 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    handler3.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_SMS_MMS_RECEIVED");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_SMS, false) > 0) {
//                                    PPApplication.logE("[EVENTS_HANDLER_CALL] PPPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_SMS");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.setEventSMSParameters(origin, time, simSlot);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SMS);
                            //}

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED");
                        } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    });
                }
                break;
            case PPApplication.ACTION_CALL_RECEIVED:
                //final int servicePhoneEvent = intent.getIntExtra(EXTRA_SERVICE_PHONE_EVENT, 0);
                final int callEventType = intent.getIntExtra(EXTRA_CALL_EVENT_TYPE, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                final String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                final long eventTime = intent.getLongExtra(EXTRA_EVENT_TIME, 0);
                final int slotIndex = intent.getIntExtra(EXTRA_SIM_SLOT, 0);

//                if (PPApplication.logEnabled()) {
//                    //PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "servicePhoneEvent="+servicePhoneEvent);
//                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "callEventType=" + callEventType);
//                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "phoneNumber=" + phoneNumber);
//                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "eventTime=" + eventTime);
//                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "slotIndex=" + slotIndex);
//                }

                if (Event.getGlobalEventsRunning()) {
                    PPApplication.startHandlerThreadBroadcast(/*"PPPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED"*/);
                    final Handler handler4 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    handler4.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_CALL_RECEIVED");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALL, false) > 0) {
//                                    PPApplication.logE("[EVENTS_HANDLER_CALL] PPPExtenderBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_PHONE_CALL");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.setEventCallParameters(/*servicePhoneEvent, */callEventType, phoneNumber, eventTime, slotIndex);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_CALL);
                            //}

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED");
                        } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    });
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

    static boolean isAccessibilityServiceEnabled(Context context) {
        boolean enabled = false;
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<AccessibilityServiceInfo> runningServices =
                    manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);

            for (AccessibilityServiceInfo service : runningServices) {
                if (service != null) {
                    //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "serviceId=" + service.getId());
                    try {
                        if (service.getId().contains(PPApplication.EXTENDER_ACCESSIBILITY_PACKAGE_NAME)) {
                            //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "true");
                            enabled = true;
                        }
/*
                        if (service.packageNames != null) {
                            for (String packageName : service.packageNames) {
                                if (PPApplication.EXTENDER_ACCESSIBILITY_PACKAGE_NAME.equals(packageName)) {
                                    //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "true");
                                    enabled = true;
                                }
                            }
                        }
 */
                    } catch (Exception ignored) {}
                }
            }
        }

        if (!enabled) {
            enabled = PPApplication.accessibilityServiceForPPPExtenderConnected;
        }
//        PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "enabled="+enabled);

        return enabled;
    }

    static int isExtenderInstalled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_EXTENDER, 0);
            boolean installed = appInfo.enabled;
            if (installed) {
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "installed=true");
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                //noinspection UnnecessaryLocalVariable
                int version = PPApplication.getVersionCode(pInfo);
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "version="+version);
                return version;
            }
            else {
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "installed=false");
                return 0;
            }
        }
        catch (Exception e) {
            // extender is not installed = package not found
            //Log.e("PPPExtenderBroadcastReceiver.isExtenderInstalled", Log.getStackTraceString(e));
            //PPApplication.recordException(e);
            return 0;
        }
    }

    static String getExtenderVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_EXTENDER, 0);
            boolean installed = appInfo.enabled;
            if (installed) {
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "installed=true");
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                //noinspection UnnecessaryLocalVariable

                String version =  pInfo.versionName;
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "version="+version);
                return version;
            }
            else {
                //PPApplication.logE("PPPExtenderBroadcastReceiver.isExtenderInstalled", "installed=false");
                return "";
            }
        }
        catch (Exception e) {
            // extender is not installed = package not found
            //Log.e("PPPExtenderBroadcastReceiver.isExtenderInstalled", Log.getStackTraceString(e));
            //PPApplication.recordException(e);
            return "";
        }
    }

    static boolean isEnabled(Context context,
                             @SuppressWarnings("SameParameterValue") int version) {
        int extenderVersion = isExtenderInstalled(context);
        boolean enabled = false;
        if (extenderVersion >= version)
            enabled = isAccessibilityServiceEnabled(context);
        return  (extenderVersion >= version) && enabled;
    }

    static void getApplicationInForeground(Context context)
    {
        synchronized (PPApplication.eventsRunMutex) {
            ApplicationPreferences.prefApplicationInForeground = ApplicationPreferences.
                    getSharedPreferences(context).getString(PREF_APPLICATION_IN_FOREGROUND, "");
            //return prefApplicationInForeground;
        }
    }
    static void setApplicationInForeground(Context context, String application)
    {
        synchronized (PPApplication.eventsRunMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_APPLICATION_IN_FOREGROUND, application);
            editor.apply();
            ApplicationPreferences.prefApplicationInForeground = application;
        }
    }

}
