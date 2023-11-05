package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

public class PPNotificationListenerService extends NotificationListenerService {

    //private static final String ACTION_REQUEST_INTERRUPTION_FILTER = PPApplication.PACKAGE_NAME + ".PPNotificationListenerService.ACTION_REQUEST_INTERRUPTION_FILTER";
    //private static final String EXTRA_FILTER = "filter";

    //private static final String TAG = PPNotificationListenerService.class.getSimpleName();

    private static volatile PPNotificationListenerService instance;
    private static volatile boolean connected;

    //private NLServiceReceiver nlservicereceiver;

    //private static List<PostedNotificationData> notifications = null;


    @Override
    public void onCreate() {
        super.onCreate();

        PPApplicationStatic.logE("[SYNCHRONIZED] PPNotificationListenerService.onCreate", "PPApplication.ppNotificationListenerService");
        synchronized (PPApplication.ppNotificationListenerService) {
            instance = this;
            connected = false;
        }

        //nlservicereceiver = new NLServiceReceiver();
        //IntentFilter filter = new IntentFilter();
        //filter.addAction(PPNotificationListenerService.ACTION_REQUEST_INTERRUPTION_FILTER);
        //registerReceiver(nlservicereceiver, filter);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //try {
        //    unregisterReceiver(nlservicereceiver);
        //} catch (Exception ignored) {}

        PPApplicationStatic.logE("[SYNCHRONIZED] PPNotificationListenerService.onDestroy", "PPApplication.ppNotificationListenerService");
        synchronized (PPApplication.ppNotificationListenerService) {
            instance = null;
            connected = false;
        }
    }

    static PPNotificationListenerService getInstance() {
        //synchronized (PPApplication.ppNotificationListenerService) {
        return instance;
        //}
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn,
                                     NotificationListenerService.RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);
//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationPosted", "PPApplication.notificationScannerRunning="+PPApplication.notificationScannerRunning);

        if (!PPApplication.notificationScannerRunning)
            return;

        //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
        boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(getApplicationContext());
        if (isPowerSaveMode) {
            if (ApplicationPreferences.applicationEventNotificationScanInPowerSaveMode.equals("2"))
                return;
        }
        else {
            if (ApplicationPreferences.applicationEventNotificationScanInTimeMultiply.equals("2")) {
                if (GlobalUtils.isNowTimeBetweenTimes(
                        ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyFrom,
                        ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyTo)) {
                    // not scan in configured time
                    return;
                }
            }
        }

        if (sbn == null) {
//            PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationPosted", "sbn=null");
            return;
        }

        String packageName = sbn.getPackageName();
        if (packageName.equals(PPApplication.PACKAGE_NAME)) {
//            PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationPosted", "sbn= for PPP");
            return;
        }
        // check also systemui notificatyion, may be required for notification sensor
        //if (packageName.equals("com.android.systemui"))
        //    return;

//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationPosted", "sbn="+sbn);

        final Context appContext = getApplicationContext();

//        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
//        long time = sbn.getPostTime() + gmtOffset;
        //getNotifiedPackages(context);
        //addNotifiedPackage(sbn.getPackageName(), time);
        //saveNotifiedPackages(context);

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(appContext)) {
            PPExecutors.handleEvents(appContext,
                    new int[]{EventsHandler.SENSOR_TYPE_NOTIFICATION},
                    PPExecutors.SENSOR_NAME_SENSOR_TYPE_NOTIFICATION, 5);
        }

//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationPosted", "END");

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn,
                                      NotificationListenerService.RankingMap rankingMap,
                                      int reason) {
        super.onNotificationRemoved(sbn, rankingMap, reason);
//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationRemoved", "PPApplication.notificationScannerRunning="+PPApplication.notificationScannerRunning);

        if (!PPApplication.notificationScannerRunning)
            return;

        //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
        boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(getApplicationContext());
        if (isPowerSaveMode) {
            if (ApplicationPreferences.applicationEventNotificationScanInPowerSaveMode.equals("2"))
                return;
        }
        else {
            if (ApplicationPreferences.applicationEventNotificationScanInTimeMultiply.equals("2")) {
                if (GlobalUtils.isNowTimeBetweenTimes(
                        ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyFrom,
                        ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyTo)) {
                    // not scan in configured time
                    return;
                }
            }
        }

        if (sbn == null) {
//            PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationRemoved", "sbn=null");
            return;
        }

        String packageName = sbn.getPackageName();
        if (packageName.equals(PPApplication.PACKAGE_NAME)) {
//            PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationRemoved", "sbn=for PPP");
            return;
        }
        // check also systemui notificatyion, may be required for notification sensor
        //if (packageName.equals("com.android.systemui"))
        //    return;

//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationRemoved", "sbn="+sbn);

        final Context appContext = getApplicationContext();

        //getNotifiedPackages(context);
        //removeNotifiedPackage(sbn.getPackageName());
        //saveNotifiedPackages(context);

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        PPExecutors.handleEvents(appContext,
                new int[]{EventsHandler.SENSOR_TYPE_NOTIFICATION},
                PPExecutors.SENSOR_NAME_SENSOR_TYPE_NOTIFICATION, 5);

//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onNotificationRemoved", "END");
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onListenerConnected", "xxx");

        PPApplicationStatic.logE("[SYNCHRONIZED] PPNotificationListenerService.onListenerConnected", "PPApplication.ppNotificationListenerService");
        synchronized (PPApplication.ppNotificationListenerService) {
            connected = true;
        }
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onListenerDisconnected", "xxx");

        PPApplicationStatic.logE("[SYNCHRONIZED] PPNotificationListenerService.onListenerDisconnected", "PPApplication.ppNotificationListenerService");
        synchronized (PPApplication.ppNotificationListenerService) {
            connected = false;
        }
    }

    /*
    @Override
    public void onListenerHintsChanged(int hints) {
        super.onListenerHintsChanged(hints);
    }
    */

/*
    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        super.onInterruptionFilterChanged(interruptionFilter);

//        PPApplicationStatic.logE("[IN_LISTENER] PPNotificationListenerService.onInterruptionFilterChanged", "xxx");

            if (!RingerModeChangeReceiver.internalChange) {

                final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                int ringerMode = AudioManager.RINGER_MODE_NORMAL;
                if (audioManager != null)
                    ringerMode = audioManager.getRingerMode();

                // convert to profile zenMode
                int zenMode = 0;
                switch (interruptionFilter) {
                    case NotificationListenerService.INTERRUPTION_FILTER_ALL:
                        //if (ActivateProfileHelper.vibrationIsOn(audioManager, true))
                        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                            zenMode = Profile.ZENMODE_ALL_AND_VIBRATE;
                        else
                            zenMode = Profile.ZENMODE_ALL;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_PRIORITY:
                        //if (ActivateProfileHelper.vibrationIsOn(audioManager, true))
                        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                            zenMode = Profile.ZENMODE_PRIORITY_AND_VIBRATE;
                        else
                            zenMode = Profile.ZENMODE_PRIORITY;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_NONE:
                        zenMode = Profile.ZENMODE_NONE;
                        break;
                    case NotificationListenerService.INTERRUPTION_FILTER_ALARMS: // new filter - Alarm only - Android M
                        zenMode = Profile.ZENMODE_ALARMS;
                        break;
                }
                if (zenMode != 0) {
                    synchronized (PPApplication.notUnlinkVolumesMutex) {
                        RingerModeChangeReceiver.notUnlinkVolumes = true;
                    }
                    ActivateProfileHelper.saveRingerMode(getApplicationContext(), Profile.RINGERMODE_ZENMODE);
                    ActivateProfileHelper.saveZenMode(getApplicationContext(), zenMode);
                }
            }

            //RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
    }
*/
/*
    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        int systemZenMode = ActivateProfileHelper.getSystemZenMode(context);
        int ringerMode = audioManager.getRingerMode();
        switch (systemZenMode) {
            case ActivateProfileHelper.ZENMODE_ALL:
                //if (ActivateProfileHelper.vibrationIsOn(audioManager, true))
                if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                    zenMode = Profile.ZENMODE_ALL_AND_VIBRATE;
                else
                    zenMode = Profile.ZENMODE_ALL;
                break;
            case ActivateProfileHelper.ZENMODE_PRIORITY:
                //if (ActivateProfileHelper.vibrationIsOn(audioManager, true))
                if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                    zenMode = Profile.ZENMODE_PRIORITY_AND_VIBRATE;
                else
                    zenMode = Profile.ZENMODE_PRIORITY;
                break;
            case ActivateProfileHelper.ZENMODE_NONE:
                zenMode = Profile.ZENMODE_NONE;
                break;
            case ActivateProfileHelper.ZENMODE_ALARMS: // new filter - Alarm only - Android M
                zenMode = Profile.ZENMODE_ALARMS;
                break;
        }
        return zenMode;
    }
 */
/*
    public static void setZenMode(Context context, AudioManager audioManager) {
                                     //, String from*) {
            int zenMode = getZenMode(context, audioManager);
            if (zenMode != 0) {
                ActivateProfileHelper.saveRingerMode(context, Profile.RINGERMODE_ZENMODE);
                ActivateProfileHelper.saveZenMode(context, zenMode);
            }
    }
*/
    static boolean isNotificationListenerServiceEnabled(Context context,
                                                        @SuppressWarnings("SameParameterValue") boolean checkConnected) {
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
        String packageName = PPApplication.PACKAGE_NAME;

        //if (packageNames != null) {
            if (checkConnected) {
                PPApplicationStatic.logE("[SYNCHRONIZED] PPNotificationListenerService.isNotificationListenerServiceEnabled", "(1) PPApplication.ppNotificationListenerService");
                synchronized (PPApplication.ppNotificationListenerService) {
                    return packageNames.contains(packageName) && connected;
                }
            }
            else {
                PPApplicationStatic.logE("[SYNCHRONIZED] PPNotificationListenerService.isNotificationListenerServiceEnabled", "(2) PPApplication.ppNotificationListenerService");
                synchronized (PPApplication.ppNotificationListenerService) {
                    return packageNames.contains(packageName);
                }
            }

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
        request.setPackage(context.PPApplication.PACKAGE_NAME);
        return request;
    }
    */

    // Convenience method for sending an {@link android.content.Intent} with {@link #ACTION_REQUEST_INTERRUPTION_FILTER}.
/* public static void requestInterruptionFilter(final Context context, final int zenMode) {
        try {
                if (isNotificationListenerServiceEnabled(context, false)) {
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
                    request.setPackage(PPApplication.PACKAGE_NAME);
                    context.sendBroadcast(request);
                }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }
*/
    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

        return super.onStartCommand(intent, flags, startId);
    }
    */

/*
    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            PPApplicationStatic.logE("[IN_BROADCAST] PPNotificationListenerService.NLServiceReceiver.onReceive", "xxx");

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
                                        if (isNotificationListenerServiceEnabled(context, false))
                                            requestInterruptionFilter(filter);
                                    } catch (SecurityException e) {
                                        // Fix disallowed call from unknown listener exception.
                                        // java.lang.SecurityException: Disallowed call from unknown listener
                                        PPApplicationStatic.recordException(e);
                                    }
                                    break;
                            }
                        }
                    }
                }
        }
    }
*/
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
            if (notifications == null)
                notifications = new ArrayList<>();

            boolean found = false;
            for (PostedNotificationData _notification : notifications) {
                if (_notification.packageName.equals(packageName)) {
                    found = true;
                    break;
                }
            }
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

            if (notifications == null)
                notifications = new ArrayList<>();

            for (PostedNotificationData _notification : notifications) {
                String _packageName = _notification.getPackageName();
                if (checkEnd) {
                    if (_packageName.endsWith(packageName)) {
                        return _notification;
                    }
                }
                else {
                    if (_packageName.equals(packageName)) {
                        return _notification;
                    }
                }
            }

            return null;
        }
    }
    */

}
