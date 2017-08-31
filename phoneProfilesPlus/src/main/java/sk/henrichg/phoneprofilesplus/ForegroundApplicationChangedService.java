package sk.henrichg.phoneprofilesplus;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.List;

public class ForegroundApplicationChangedService extends AccessibilityService {

    private static final String SERVICE_ID = "sk.henrichg.phoneprofilesplus/.ForegroundApplicationChangedService";

    private static final String PREF_APPLICATION_IN_FOREGROUND = "application_in_foreground";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        //Just in case this helps
        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Context context = getApplicationContext();

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            try {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
                    //Log.d("ForegroundApplicationChangedService", "currentActivity="+componentName.flattenToShortString());

                    String packageInForeground = event.getPackageName().toString();
                    //Log.d("ForegroundApplicationChangedService", "packageInForeground="+packageInForeground);
                    setApplicationInForeground(context, packageInForeground);

                    if (Event.getGlobalEventsRunning(context)) {
                        Intent eventsServiceIntent = new Intent(context, EventsService.class);
                        eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_APPLICATION);
                        WakefulIntentService.sendWakefulWork(context, eventsServiceIntent);
                    }
                }
            } catch (Exception e) {
                Log.e("ForegroundApplicationChangedService.onAccessibilityEvent", e.toString());
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.d("ForegroundApplicationChangedService", "onUnbind");

        Context context = getApplicationContext();

        setApplicationInForeground(context, "");

        Intent eventsServiceIntent = new Intent(context, EventsService.class);
        eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_APPLICATION);
        WakefulIntentService.sendWakefulWork(context, eventsServiceIntent);

        return super.onUnbind(intent);
    }

    public static boolean isEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices =
                manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);

        for (AccessibilityServiceInfo service : runningServices) {
            //Log.d("ForegroundApplicationChangedService", "serviceId="+service.getId());
            if (SERVICE_ID.equals(service.getId()))
                return true;
        }

        return false;
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
