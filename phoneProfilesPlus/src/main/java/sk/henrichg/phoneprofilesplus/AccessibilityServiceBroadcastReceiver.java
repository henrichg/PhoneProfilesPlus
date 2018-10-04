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

import static android.content.Context.POWER_SERVICE;

public class AccessibilityServiceBroadcastReceiver extends BroadcastReceiver {

    private static final String EXTRA_PACKAGE_NAME = "sk.henrichg.phoneprofilesplusextender.package_name";
    private static final String EXTRA_CLASS_NAME = "sk.henrichg.phoneprofilesplusextender.class_name";

    private static final String PREF_APPLICATION_IN_FOREGROUND = "application_in_foreground";

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context appContext = context.getApplicationContext();

        CallsCounter.logCounter(context.getApplicationContext(), "AccessibilityServiceBroadcastReceiver.onReceive", "ForegroundApplicationChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if ((intent == null) || (intent.getAction() == null))
            return;

        PPApplication.logE("AccessibilityServiceBroadcastReceiver.onReceive", "action="+intent.getAction());

        switch (intent.getAction()) {
            case PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED:
                final String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                final String className = intent.getStringExtra(EXTRA_CLASS_NAME);

                PPApplication.logE("AccessibilityServiceBroadcastReceiver.onReceive", "packageName=" + packageName);
                PPApplication.logE("AccessibilityServiceBroadcastReceiver.onReceive", "className=" + className);

                try {
                    ComponentName componentName = new ComponentName(packageName, className);

                    ActivityInfo activityInfo = tryGetActivity(appContext, componentName);
                    boolean isActivity = activityInfo != null;
                    if (isActivity) {
                        setApplicationInForeground(appContext, packageName);

                        if (Event.getGlobalEventsRunning(appContext)) {
                            //EventsHandlerJob.startForSensor(context, EventsHandler.SENSOR_TYPE_APPLICATION);
                            PPApplication.startHandlerThread("AccessibilityServiceBroadcastReceiver.onReceive.1");
                            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AccessibilityServiceBroadcastReceiver.onReceive.1");
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);

                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                        try {
                                            wakeLock.release();
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e("AccessibilityServiceBroadcastReceiver.onReceive", Log.getStackTraceString(e));
                }
                break;
            case PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND:
                setApplicationInForeground(appContext, "");

                //EventsHandlerJob.startForSensor(context, EventsHandler.SENSOR_TYPE_APPLICATION);
                PPApplication.startHandlerThread("AccessibilityServiceBroadcastReceiver.onReceive.2");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AccessibilityServiceBroadcastReceiver.onReceive.2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_APPLICATION);

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });
                break;
            case PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END:
                final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                if (profileId != 0) {
                    PPApplication.startHandlerThread("AccessibilityServiceBroadcastReceiver.onReceive.3");
                    final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AccessibilityServiceBroadcastReceiver.onReceive.3");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            Profile profile = DatabaseHandler.getInstance(appContext).getProfile(profileId, false);
                            if (profile != null)
                                ActivateProfileHelper.executeForInteractivePreferences(profile, appContext);

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
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null) {
            List<AccessibilityServiceInfo> runningServices =
                    manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);

            for (AccessibilityServiceInfo service : runningServices) {
                if (service != null) {
                    PPApplication.logE("AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled", "serviceId=" + service.getId());
                    if (PPApplication.EXTENDER_ACCESSIBILITY_SERVICE_ID.equals(service.getId())) {
                        PPApplication.logE("AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled", "true");
                        return true;
                    }
                }
            }
            PPApplication.logE("AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled", "false");
            return false;
        }
        PPApplication.logE("AccessibilityServiceBroadcastReceiver.isAccessibilityServiceEnabled", "false");
        return false;
    }

    static int isExtenderInstalled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo("sk.henrichg.phoneprofilesplusextender", 0);
            boolean installed = appInfo.enabled;
            if (installed) {
                PPApplication.logE("AccessibilityServiceBroadcastReceiver.isExtenderInstalled", "installed=true");
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                int version = pInfo.versionCode;
                PPApplication.logE("AccessibilityServiceBroadcastReceiver.isExtenderInstalled", "version="+version);
                return version;
            }
            else {
                PPApplication.logE("AccessibilityServiceBroadcastReceiver.isExtenderInstalled", "installed=false");
                return 0;
            }
        }
        catch (Exception e) {
            PPApplication.logE("AccessibilityServiceBroadcastReceiver.isExtenderInstalled", "exception");
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
