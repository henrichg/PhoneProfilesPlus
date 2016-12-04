package sk.henrichg.phoneprofilesplus;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class ForegroundApplicationChangedService extends AccessibilityService {

    private static final String SERVICE_ID = "sk.henrichg.phoneprofilesplus/.ForegroundApplicationChangedService";

    @Override
    protected void onServiceConnected() {

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onServiceConnected();

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Context context = getApplicationContext();

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
                    GlobalData.setApplicationInForeground(context, packageInForeground);

                    Intent intent = new Intent(context, ForegroundApplicationChangedBroadcastReceiver.class);
                    context.sendBroadcast(intent);
                }
            } catch (Exception e) {
                Log.e("ForegroundApplicationChangedService.onAccessibilityEvent", e.toString());
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
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

        GlobalData.setApplicationInForeground(context, "");

        Intent bintent = new Intent(context, ForegroundApplicationChangedBroadcastReceiver.class);
        context.sendBroadcast(bintent);

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
}