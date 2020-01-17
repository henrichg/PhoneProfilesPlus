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
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class PPPExtenderBroadcastReceiver extends BroadcastReceiver {

    private static final String EXTRA_PACKAGE_NAME = PPApplication.PACKAGE_NAME_EXTENDER + ".package_name";
    private static final String EXTRA_CLASS_NAME = PPApplication.PACKAGE_NAME_EXTENDER + ".class_name";

    private static final String EXTRA_ORIGIN = PPApplication.PACKAGE_NAME_EXTENDER + ".origin";
    private static final String EXTRA_TIME = PPApplication.PACKAGE_NAME_EXTENDER + ".time";

    //private static final String EXTRA_SERVICE_PHONE_EVENT = PPApplication.PACKAGE_NAME_EXTENDER + ".service_phone_event";
    private static final String EXTRA_CALL_EVENT_TYPE = PPApplication.PACKAGE_NAME_EXTENDER + ".call_event_type";
    private static final String EXTRA_PHONE_NUMBER = PPApplication.PACKAGE_NAME_EXTENDER + ".phone_number";
    private static final String EXTRA_EVENT_TIME = PPApplication.PACKAGE_NAME_EXTENDER + ".event_time";

    private static final String PREF_APPLICATION_IN_FOREGROUND = "application_in_foreground";


    @Override
    public void onReceive(Context context, Intent intent) {
        final Context appContext = context.getApplicationContext();

        //CallsCounter.logCounter(context.getApplicationContext(), "PPPExtenderBroadcastReceiver.onReceive", "ForegroundApplicationChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        //PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "action="+intent.getAction());

        switch (intent.getAction()) {
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED:
                PPApplication.startHandlerThread("PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED");
                final Handler handler0 = new Handler(PPApplication.handlerThread.getLooper());
                handler0.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_ACCESSIBILITY_SERVICE_CONNECTED");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED");

                            if (PhoneProfilesService.getInstance() != null)
                                PhoneProfilesService.getInstance().registerPPPPExtenderReceiver(true, true);

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_CONNECTED");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });
                break;
            case PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED:
                final String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                final String className = intent.getStringExtra(EXTRA_CLASS_NAME);

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "packageName=" + packageName);
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "className=" + className);
                }*/

                try {
                    ComponentName componentName = new ComponentName(packageName, className);

                    ActivityInfo activityInfo = tryGetActivity(appContext, componentName);
                    boolean isActivity = activityInfo != null;
                    if (isActivity) {
                        setApplicationInForeground(appContext, packageName);

                        if (Event.getGlobalEventsRunning(appContext)) {
                            PPApplication.startHandlerThread("PPPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED");
                            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_FOREGROUND_APPLICATION_CHANGED");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED");

                                        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                                        EventsHandler eventsHandler = new EventsHandler(appContext);
                                        if (databaseHandler.getTypeEventsCount(DatabaseHandler.ETYPE_APPLICATION, false) > 0)
                                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);
                                        if (databaseHandler.getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false) > 0)
                                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);

                                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_FOREGROUND_APPLICATION_CHANGED");
                                    } finally {
                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                            try {
                                                wakeLock.release();
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e("PPPExtenderBroadcastReceiver.onReceive", Log.getStackTraceString(e));
                }
                break;
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND:
                setApplicationInForeground(appContext, "");

                PPApplication.startHandlerThread("PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_ACCESSIBILITY_SERVICE_UNBIND");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND");

                            DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            if (databaseHandler.getTypeEventsCount(DatabaseHandler.ETYPE_APPLICATION, false) > 0)
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);
                            if (databaseHandler.getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false) > 0)
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_ACCESSIBILITY_SERVICE_UNBIND");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });
                break;
            case PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END:
                final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                if (profileId != 0) {
                    PPApplication.startHandlerThread("PPPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END");
                    final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_FORCE_STOP_APPLICATIONS_END");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END");

                                Profile profile = DatabaseHandler.getInstance(appContext).getProfile(profileId, false);
                                if (profile != null)
                                    ActivateProfileHelper.executeForInteractivePreferences(profile, appContext);

                                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_FORCE_STOP_APPLICATIONS_END");
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    });
                }
                break;
            case PPApplication.ACTION_SMS_MMS_RECEIVED:
                final String origin = intent.getStringExtra(EXTRA_ORIGIN);
                final long time = intent.getLongExtra(EXTRA_TIME, 0);
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "origin=" + origin);
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "time=" + time);
                }*/

                if (Event.getGlobalEventsRunning(appContext)) {
                    PPApplication.startHandlerThread("PPPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED");
                    final Handler handler3 = new Handler(PPApplication.handlerThread.getLooper());
                    handler3.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_SMS_MMS_RECEIVED");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED");

                                if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_SMS, false) > 0) {
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.setEventSMSParameters(origin, time);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SMS);
                                }

                                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_SMS_MMS_RECEIVED");
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
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

                /*if (PPApplication.logEnabled()) {
                    //PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "servicePhoneEvent="+servicePhoneEvent);
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "callEventType=" + callEventType);
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "phoneNumber=" + phoneNumber);
                    PPApplication.logE("PPPExtenderBroadcastReceiver.onReceive", "eventTime=" + eventTime);
                }*/

                if (Event.getGlobalEventsRunning(appContext)) {
                    PPApplication.startHandlerThread("PPPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED");
                    final Handler handler4 = new Handler(PPApplication.handlerThread.getLooper());
                    handler4.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExtenderBroadcastReceiver_onReceive_ACTION_CALL_RECEIVED");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED");

                                if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALL, false) > 0) {
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.setEventCallParameters(/*servicePhoneEvent, */callEventType, phoneNumber, eventTime);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_CALL);
                                }

                                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPPExtenderBroadcastReceiver.onReceive.ACTION_CALL_RECEIVED");
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
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
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<AccessibilityServiceInfo> runningServices =
                    manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);

            for (AccessibilityServiceInfo service : runningServices) {
                if (service != null) {
                    //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "serviceId=" + service.getId());
                    if (PPApplication.EXTENDER_ACCESSIBILITY_SERVICE_ID.equals(service.getId())) {
                        //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "true");
                        return true;
                    }
                }
            }
            //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "false");
            return false;
        }
        //PPApplication.logE("PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled", "false");
        return false;
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
            Log.e("PPPExtenderBroadcastReceiver.isExtenderInstalled", Log.getStackTraceString(e));
            return 0;
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

    static public String getApplicationInForeground(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getString(PREF_APPLICATION_IN_FOREGROUND, "");
    }

    static public void setApplicationInForeground(Context context, String application)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putString(PREF_APPLICATION_IN_FOREGROUND, application);
        editor.apply();
    }

}
