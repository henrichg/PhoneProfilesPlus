package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PPNotificationListenerService extends NotificationListenerService {

    private static final String ACTION_REQUEST_INTERRUPTION_FILTER =
            PPNotificationListenerService.class.getPackage().getName() + '.' + "ACTION_REQUEST_INTERRUPTION_FILTER";
    private static final String EXTRA_FILTER = "filter";

    private static final String TAG = PPNotificationListenerService.class.getSimpleName();

    private NLServiceReceiver nlservicereceiver;

    private static List<PostedNotificationData> notifications = null;


    @Override
    public void onCreate() {
        super.onCreate();

        nlservicereceiver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REQUEST_INTERRUPTION_FILTER);
        registerReceiver(nlservicereceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(nlservicereceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        CallsCounter.logCounter(getApplicationContext(), "PPNotificationListenerService.onNotificationPosted", "PPNotificationListenerService_onNotificationPosted");

        PPApplication.logE("PPNotificationListenerService.onNotificationPosted","sbn="+sbn);

        if (sbn == null)
            return;

        final Context context = getApplicationContext();

        if (sbn.getPackageName().equals(context.getPackageName()))
            return;

        /*
        String ticker ="";
        if(sbn.getNotification().tickerText !=null) {
            ticker = sbn.getNotification().tickerText.toString();
        }
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getCharSequence("android.text").toString();
        */

        if (PPApplication.logEnabled()) {
            PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "from=" + sbn.getPackageName());
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String alarmTimeS = sdf.format(sbn.getPostTime());
            PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "time=" + alarmTimeS);
        }

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
        long time = sbn.getPostTime() + gmtOffset;

        getNotifiedPackages(context);
        addNotifiedPackage(sbn.getPackageName(), time);
        saveNotifiedPackages(context);

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(context)) {
            //EventsHandlerJob.startForNotificationSensor(context, "posted");
            PPApplication.startHandlerThread("PPNotificationListenerService.onNotificationPosted");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPNotificationListenerService_onNotificationPosted");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        EventsHandler eventsHandler = new EventsHandler(context);
                        //eventsHandler.setEventNotificationParameters("posted");
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NOTIFICATION);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        CallsCounter.logCounter(getApplicationContext(), "PPNotificationListenerService.onNotificationRemoved", "PPNotificationListenerService_onNotificationRemoved");

        PPApplication.logE("PPNotificationListenerService.onNotificationRemoved","sbn="+sbn);

        if (sbn == null)
            return;

        final Context context = getApplicationContext();

        if (sbn.getPackageName().equals(context.getPackageName()))
            return;

        PPApplication.logE("PPNotificationListenerService.onNotificationRemoved","packageName="+sbn.getPackageName());

        getNotifiedPackages(context);
        removeNotifiedPackage(sbn.getPackageName());
        saveNotifiedPackages(context);

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(context)) {
            //EventsHandlerJob.startForNotificationSensor(context, "removed");
            PPApplication.startHandlerThread("PPNotificationListenerService.onNotificationRemoved");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPNotificationListenerService_onNotificationRemoved");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        EventsHandler eventsHandler = new EventsHandler(context);
                        //eventsHandler.setEventNotificationParameters("removed");
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NOTIFICATION);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });
        }
    }

    // Android 5.0 Lollipop

    @Override public void onListenerConnected() {
    }
    @Override public void onListenerHintsChanged(int hints) {
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        CallsCounter.logCounter(getApplicationContext(), "PPNotificationListenerService.onInterruptionFilterChanged", "PPNotificationListenerService_onInterruptionFilterChanged");

        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) || a60) {
            PPApplication.logE(TAG, "onInterruptionFilterChanged(interruptionFilter=" + interruptionFilter + ')');
            PPApplication.logE(TAG, "onInterruptionFilterChanged(internalChange=" + RingerModeChangeReceiver.internalChange + ")");
            if (!RingerModeChangeReceiver.internalChange) {

                final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

                // convert to profile zenMode
                int zenMode = 0;
                switch (interruptionFilter) {
                    case NotificationListenerService.INTERRUPTION_FILTER_ALL:
                        if (ActivateProfileHelper.vibrationIsOn(/*getApplicationContext(), */audioManager, true))
                            zenMode = 4;
                        else
                            zenMode = 1;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_PRIORITY:
                        if (ActivateProfileHelper.vibrationIsOn(/*getApplicationContext(), */audioManager, true))
                            zenMode = 5;
                        else
                            zenMode = 2;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_NONE:
                        zenMode = 3;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_ALARMS: // new filter - Alarm only - Android M
                        zenMode = 6;
                        break;
                }
                PPApplication.logE(TAG, "onInterruptionFilterChanged(zenMode=" + zenMode + ')');
                if (zenMode != 0) {
                    RingerModeChangeReceiver.notUnlinkVolumes = true;
                    ActivateProfileHelper.setRingerMode(getApplicationContext(), 5);
                    ActivateProfileHelper.setZenMode(getApplicationContext(), zenMode);
                }
            }

            //RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
        }
    }

    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        int systemZenMode = ActivateProfileHelper.getSystemZenMode(context/*, -1*/);
        PPApplication.logE("PPNotificationListenerService.getZenMode", "systemZenMode=" + systemZenMode);
        switch (systemZenMode) {
            case ActivateProfileHelper.ZENMODE_ALL:
                if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, true))
                    zenMode = 4;
                else
                    zenMode = 1;
                break;
            case ActivateProfileHelper.ZENMODE_PRIORITY:
                if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, true))
                    zenMode = 5;
                else
                    zenMode = 2;
                break;
            case ActivateProfileHelper.ZENMODE_NONE:
                zenMode = 3;
                break;
            case ActivateProfileHelper.ZENMODE_ALARMS: // new filter - Alarm only - Android M
                zenMode = 6;
                break;
        }
        PPApplication.logE("PPNotificationListenerService.getZenMode", "zenMode=" + zenMode);
        return zenMode;
    }

    public static void setZenMode(Context context, AudioManager audioManager) {
        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) || a60) {
            int zenMode = getZenMode(context, audioManager);
            if (zenMode != 0) {
                ActivateProfileHelper.setRingerMode(context, 5);
                ActivateProfileHelper.setZenMode(context, zenMode);
            }
        }
    }

    public static boolean isNotificationListenerServiceEnabled(Context context) {
        /*
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String className = PPNotificationListenerService.class.getName();
        // check to see if the enabledNotificationListeners String contains our package name
        if ((enabledNotificationListeners == null) || (!enabledNotificationListeners.contains(className)))
        {
            // in this situation we know that the user has not granted the app the Notification access permission
            return false;
        }
        else
        {
            return true;
        }
        */

        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages (context);
        //String className = PPNotificationListenerService.class.getName();
        String packageName = context.getPackageName();

        //if (packageNames != null) {
            for (String pkgName : packageNames) {
                //if (className.contains(pkgName)) {
                if (packageName.equals(pkgName)) {
                    return true;
                }
            }
            return false;
        //}
        //else
        //    return false;
    }

    private static Intent getInterruptionFilterRequestIntent(final int filter, final Context context) {
        Intent request = new Intent(ACTION_REQUEST_INTERRUPTION_FILTER);
        request.putExtra(EXTRA_FILTER, filter);
        request.setPackage(context.getPackageName());
        return request;
    }

    /** Convenience method for sending an {@link android.content.Intent} with {@link #ACTION_REQUEST_INTERRUPTION_FILTER}. */
    @SuppressLint("InlinedApi")
    public static void requestInterruptionFilter(final Context context, final int zenMode) {
        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) || a60) {
            if (isNotificationListenerServiceEnabled(context)) {
                int interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALL;
                switch (zenMode) {
                    case ActivateProfileHelper.ZENMODE_ALL:
                        interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALL;
                        break;
                    case ActivateProfileHelper.ZENMODE_PRIORITY:
                        interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_PRIORITY;
                        break;
                    case ActivateProfileHelper.ZENMODE_NONE:
                        interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_NONE;
                        break;
                    case ActivateProfileHelper.ZENMODE_ALARMS:
                        interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALARMS;
                        break;
                }
                Intent request = getInterruptionFilterRequestIntent(interruptionFilter, context);
                context.sendBroadcast(request);
            }
        }
    }

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            // Handle being told to change the interruption filter (zen mode).
            if (!TextUtils.isEmpty(intent.getAction())) {
                if (ACTION_REQUEST_INTERRUPTION_FILTER.equals(intent.getAction())) {
                    if (intent.hasExtra(EXTRA_FILTER)) {
                        final int zenMode = intent.getIntExtra(EXTRA_FILTER, ActivateProfileHelper.ZENMODE_ALL);
                        switch (zenMode) {
                            case ActivateProfileHelper.ZENMODE_ALL:
                                requestInterruptionFilter(INTERRUPTION_FILTER_ALL);
                                break;
                            case ActivateProfileHelper.ZENMODE_PRIORITY:
                                requestInterruptionFilter(INTERRUPTION_FILTER_PRIORITY);
                                break;
                            case ActivateProfileHelper.ZENMODE_NONE:
                                requestInterruptionFilter(INTERRUPTION_FILTER_NONE);
                                break;
                            case ActivateProfileHelper.ZENMODE_ALARMS:
                                requestInterruptionFilter(INTERRUPTION_FILTER_ALARMS);
                                break;
                        }
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
    */

    class NLServiceReceiver extends BroadcastReceiver {

        @SuppressLint("InlinedApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            CallsCounter.logCounter(getApplicationContext(), "PPNotificationListenerService.NLServiceReceiver.onReceive", "PPNotificationListenerService_NLServiceReceiver_onReceive");

            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if (((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) || a60) {
                // Handle being told to change the interruption filter (zen mode).
                if (!TextUtils.isEmpty(intent.getAction())) {
                    if (ACTION_REQUEST_INTERRUPTION_FILTER.equals(intent.getAction())) {
                        if (intent.hasExtra(EXTRA_FILTER)) {
                            final int filter = intent.getIntExtra(EXTRA_FILTER, INTERRUPTION_FILTER_ALL);
                            switch (filter) {
                                case INTERRUPTION_FILTER_ALL:
                                case INTERRUPTION_FILTER_PRIORITY:
                                case INTERRUPTION_FILTER_NONE:
                                case INTERRUPTION_FILTER_ALARMS:
                                    try {
                                        if (isNotificationListenerServiceEnabled(context))
                                            requestInterruptionFilter(filter);
                                    } catch (SecurityException ignored) {
                                        // Fix disallowed call from unknown listener exception.
                                        // java.lang.SecurityException: Disallowed call from unknown listener
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

        }
    }


    private static final String POSTED_NOTIFICATIONS_COUNT_PREF = "count";
    private static final String POSTED_NOTIFICATIONS_PACKAGE_PREF = "package";

    public static void getNotifiedPackages(Context context)
    {
        synchronized (PPApplication.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<>();

            notifications.clear();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.POSTED_NOTIFICATIONS_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(POSTED_NOTIFICATIONS_COUNT_PREF, 0);

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(POSTED_NOTIFICATIONS_PACKAGE_PREF + i, "");
                if (!json.isEmpty()) {
                    PostedNotificationData notification = gson.fromJson(json, PostedNotificationData.class);
                    notifications.add(notification);
                }
            }
        }
    }

    private static void saveNotifiedPackages(Context context)
    {
        synchronized (PPApplication.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.POSTED_NOTIFICATIONS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(POSTED_NOTIFICATIONS_COUNT_PREF, notifications.size());

            Gson gson = new Gson();

            for (int i = 0; i < notifications.size(); i++) {
                String json = gson.toJson(notifications.get(i));
                editor.putString(POSTED_NOTIFICATIONS_PACKAGE_PREF + i, json);
            }

            editor.apply();
        }
    }

    private void addNotifiedPackage(String packageName, long time)
    {
        synchronized (PPApplication.notificationsChangeMutex) {
            PPApplication.logE("PPNotificationListenerService.addNotifiedPackage", "packageName=" + packageName);
            PPApplication.logE("PPNotificationListenerService.addNotifiedPackage", "time=" + time);

            if (notifications == null)
                notifications = new ArrayList<>();

            boolean found = false;
            for (PostedNotificationData _notification : notifications) {
                if (_notification.packageName.equals(packageName)) {
                    found = true;
                    break;
                }
            }
            PPApplication.logE("PPNotificationListenerService.addNotifiedPackage", "found=" + found);
            if (!found) {
                notifications.add(new PostedNotificationData(packageName, time));
            }

        }
    }

    private void removeNotifiedPackage(String packageName)
    {
        synchronized (PPApplication.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<>();

            int index = 0;
            boolean found = false;
            for (PostedNotificationData _notification : notifications) {
                if (_notification.packageName.equals(packageName)) {
                    found = true;
                    break;
                }
                ++index;
            }
            if (found)
                notifications.remove(index);
        }
    }

    static void clearNotifiedPackages(Context context) {
        synchronized (PPApplication.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<>();
            notifications.clear();

            saveNotifiedPackages(context);
        }
    }

    public static PostedNotificationData getNotificationPosted(String packageName, boolean checkEnd)
    {
        synchronized (PPApplication.notificationsChangeMutex) {
            //packageName = ApplicationsCache.getPackageName(packageName);
            PPApplication.logE("PPNotificationListenerService.getNotificationPosted", "packageName=" + packageName);

            if (notifications == null)
                notifications = new ArrayList<>();

            for (PostedNotificationData _notification : notifications) {
                String _packageName = _notification.getPackageName();
                if (checkEnd) {
                    if (_packageName.endsWith(packageName)) {
                        PPApplication.logE("PPNotificationListenerService.getNotificationPosted", "_packageName returned=" + _packageName);
                        return _notification;
                    }
                }
                else {
                    if (_packageName.equals(packageName)) {
                        PPApplication.logE("PPNotificationListenerService.getNotificationPosted", "_packageName returned=" + _packageName);
                        return _notification;
                    }
                }
            }

            PPApplication.logE("PPNotificationListenerService.getNotificationPosted", "_packageName returned=null");
            return null;
        }
    }

}
