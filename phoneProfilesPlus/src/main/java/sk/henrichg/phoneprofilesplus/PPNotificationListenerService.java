package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

public class PPNotificationListenerService extends NotificationListenerService {

    private static final String ACTION_REQUEST_INTERRUPTION_FILTER = PPApplication.PACKAGE_NAME + ".PPNotificationListenerService.ACTION_REQUEST_INTERRUPTION_FILTER";
    private static final String EXTRA_FILTER = "filter";

    //private static final String TAG = PPNotificationListenerService.class.getSimpleName();

    private static volatile PPNotificationListenerService instance;

    private NLServiceReceiver nlservicereceiver;

    //private static List<PostedNotificationData> notifications = null;


    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (PPApplication.ppNotificationListenerService) {
            instance = this;
        }

        nlservicereceiver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PPNotificationListenerService.ACTION_REQUEST_INTERRUPTION_FILTER);
        registerReceiver(nlservicereceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(nlservicereceiver);
        } catch (Exception ignored) {}

        synchronized (PPApplication.ppNotificationListenerService) {
            instance = null;
        }
    }

    static PPNotificationListenerService getInstance() {
        //synchronized (PPApplication.ppNotificationListenerService) {
        return instance;
        //}
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        //CallsCounter.logCounter(getApplicationContext(), "PPNotificationListenerService.onNotificationPosted", "PPNotificationListenerService_onNotificationPosted");

        //PPApplication.logE("[TEST BATTERY] PPNotificationListenerService.onNotificationPosted", "xxx");

        if (sbn == null)
            return;

        final Context appContext = getApplicationContext();

        //PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "sbn.getPackageName()="+sbn.getPackageName());
        //PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "appContext.getPackageName()="+appContext.getPackageName());

        if (sbn.getPackageName().equals(appContext.getPackageName()))
            return;

        //PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "is not PPP");

//        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
//        long time = sbn.getPostTime() + gmtOffset;
        //getNotifiedPackages(context);
        //addNotifiedPackage(sbn.getPackageName(), time);
        //saveNotifiedPackages(context);

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "from=" + sbn.getPackageName());

                if (PPApplication.logEnabled()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String alarmTimeS = sdf.format(sbn.getPostTime());
                    PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "time=" + alarmTimeS);
                }

                if(sbn.getNotification().tickerText !=null) {
                    String ticker = sbn.getNotification().tickerText.toString();
                    PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "ticker=" + ticker);
                }
                Bundle extras = sbn.getNotification().extras;
                if (extras != null) {
                    String title;
                    if (extras.getString("android.title") != null)
                        title = extras.getString("android.title");
                    else
                        title = "";
                    String text;
                    if (extras.getCharSequence("android.text") != null)
                        text = extras.getCharSequence("android.text").toString();
                    else
                        text = "";
                    PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "title=" + title);
                    PPApplication.logE("PPNotificationListenerService.onNotificationPosted", "text=" + text);
                }
            }*/

            PPApplication.startHandlerThread(/*"PPNotificationListenerService.onNotificationPosted"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPNotificationListenerService_onNotificationPosted");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPNotificationListenerService.onNotificationPosted");

                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        //eventsHandler.setEventNotificationParameters("posted");
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NOTIFICATION);

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPNotificationListenerService.onNotificationPosted");
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
        super.onNotificationRemoved(sbn);
        //PPApplication.logE("[TEST BATTERY] PPNotificationListenerService.onNotificationRemoved","xxx");

        //CallsCounter.logCounter(getApplicationContext(), "PPNotificationListenerService.onNotificationRemoved", "PPNotificationListenerService_onNotificationRemoved");

        if (sbn == null)
            return;

        final Context appContext = getApplicationContext();

        if (sbn.getPackageName().equals(appContext.getPackageName()))
            return;

        //PPApplication.logE("PPNotificationListenerService.onNotificationRemoved","packageName="+sbn.getPackageName());

        //getNotifiedPackages(context);
        //removeNotifiedPackage(sbn.getPackageName());
        //saveNotifiedPackages(context);

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            PPApplication.startHandlerThread(/*"PPNotificationListenerService.onNotificationRemoved"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPNotificationListenerService_onNotificationRemoved");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPNotificationListenerService.onNotificationRemoved");

                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        //eventsHandler.setEventNotificationParameters("removed");
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NOTIFICATION);

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPNotificationListenerService.onNotificationRemoved");
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

    /*
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
    }

    @Override
    public void onListenerHintsChanged(int hints) {
        super.onListenerHintsChanged(hints);
    }
    */

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        super.onInterruptionFilterChanged(interruptionFilter);

        //CallsCounter.logCounter(getApplicationContext(), "PPNotificationListenerService.onInterruptionFilterChanged", "PPNotificationListenerService_onInterruptionFilterChanged");

        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (/*((android.os.Build.VERSION.SDK_INT >= 21) &&*/ /*(android.os.Build.VERSION.SDK_INT < 23)) ||*/ a60) {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE(TAG, "onInterruptionFilterChanged(interruptionFilter=" + interruptionFilter + ')');
                PPApplication.logE(TAG, "onInterruptionFilterChanged(internalChange=" + RingerModeChangeReceiver.internalChange + ")");
            }*/
            if (!RingerModeChangeReceiver.internalChange) {

                final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                int ringerMode = AudioManager.RINGER_MODE_NORMAL;
                if (audioManager != null)
                    ringerMode = audioManager.getRingerMode();

                // convert to profile zenMode
                int zenMode = 0;
                switch (interruptionFilter) {
                    case NotificationListenerService.INTERRUPTION_FILTER_ALL:
                        //if (ActivateProfileHelper.vibrationIsOn(/*getApplicationContext(), */audioManager, true))
                        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                            zenMode = 4;
                        else
                            zenMode = 1;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_PRIORITY:
                        //if (ActivateProfileHelper.vibrationIsOn(/*getApplicationContext(), */audioManager, true))
                        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
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
                //PPApplication.logE(TAG, "onInterruptionFilterChanged(zenMode=" + zenMode + ')');
                if (zenMode != 0) {
                    RingerModeChangeReceiver.notUnlinkVolumes = true;
                    ActivateProfileHelper.saveRingerMode(getApplicationContext(), 5);
                    ActivateProfileHelper.saveZenMode(getApplicationContext(), zenMode);
                }
            }

            //RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
        }
    }

    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        int systemZenMode = ActivateProfileHelper.getSystemZenMode(context/*, -1*/);
        //PPApplication.logE("PPNotificationListenerService.getZenMode", "systemZenMode=" + systemZenMode);
        int ringerMode = audioManager.getRingerMode();
        switch (systemZenMode) {
            case ActivateProfileHelper.ZENMODE_ALL:
                //if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, true))
                if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                    zenMode = 4;
                else
                    zenMode = 1;
                break;
            case ActivateProfileHelper.ZENMODE_PRIORITY:
                //if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, true))
                if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
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
        //PPApplication.logE("PPNotificationListenerService.getZenMode", "zenMode=" + zenMode);
        return zenMode;
    }

    public static void setZenMode(Context context, AudioManager audioManager) {
        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (/*((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) ||*/ a60) {
            int zenMode = getZenMode(context, audioManager);
            if (zenMode != 0) {
                ActivateProfileHelper.saveRingerMode(context, 5);
                ActivateProfileHelper.saveZenMode(context, zenMode);
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
            return packageNames.contains(packageName);

            /*for (String pkgName : packageNames) {
                //if (className.contains(pkgName)) {
                if (packageName.equals(pkgName)) {
                    return true;
                }
            }
            return false;*/
        //}
        //else
        //    return false;
    }

    /*
    private static Intent getInterruptionFilterRequestIntent(final int filter, final Context context) {
        Intent request = new Intent(PPNotificationListenerService.ACTION_REQUEST_INTERRUPTION_FILTER);
        request.putExtra(EXTRA_FILTER, filter);
        request.setPackage(context.getPackageName());
        return request;
    }
    */

    /** Convenience method for sending an {@link android.content.Intent} with {@link #ACTION_REQUEST_INTERRUPTION_FILTER}. */
    @SuppressLint("InlinedApi")
    public static void requestInterruptionFilter(final Context context, final int zenMode) {
        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
        if (/*((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) ||*/ a60) {
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
                //Intent request = getInterruptionFilterRequestIntent(interruptionFilter, context);
                Intent request = new Intent(PPNotificationListenerService.ACTION_REQUEST_INTERRUPTION_FILTER);
                request.putExtra(EXTRA_FILTER, interruptionFilter);
                request.setPackage(context.getPackageName());
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
            //CallsCounter.logCounter(getApplicationContext(), "PPNotificationListenerService.NLServiceReceiver.onReceive", "PPNotificationListenerService_NLServiceReceiver_onReceive");

            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            if (/*((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) ||*/ a60) {
                // Handle being told to change the interruption filter (zen mode).
                if (!TextUtils.isEmpty(intent.getAction())) {
                    if (PPNotificationListenerService.ACTION_REQUEST_INTERRUPTION_FILTER.equals(intent.getAction())) {
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
                                    } catch (SecurityException e) {
                                        // Fix disallowed call from unknown listener exception.
                                        // java.lang.SecurityException: Disallowed call from unknown listener
                                        PPApplication.recordException(e);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

        }
    }

    /*
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
    */

}
