package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PPNotificationListenerService extends NotificationListenerService {

    public static final String ACTION_REQUEST_INTERRUPTION_FILTER =
            PPNotificationListenerService.class.getPackage().getName() + '.' + "ACTION_REQUEST_INTERRUPTION_FILTER";
    public static final String EXTRA_FILTER = "filter";

    public static final String TAG = PPNotificationListenerService.class.getSimpleName();

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
        //Log.e(TAG, "**********  onNotificationPosted");
        //Log.e(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());

        Context context = getApplicationContext();

        if (sbn.getPackageName().equals(context.getPackageName()))
            return;

        //GlobalData.logE("#### PPNotificationListenerService.onNotificationPosted","xxx");

        //GlobalData.logE("PPNotificationListenerService.onNotificationPosted", "from=" + sbn.getPackageName());
        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
        String alarmTimeS = sdf.format(sbn.getPostTime());
        //GlobalData.logE("PPNotificationListenerService.onNotificationPosted", "time=" + alarmTimeS);

        int gmtOffset = TimeZone.getDefault().getRawOffset();
        long time = sbn.getPostTime() + gmtOffset;

        getNotifiedPackages(context);
        addNotifiedPackage(sbn.getPackageName(), time);
        saveNotifiedPackages(context);

        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        //intent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME, sbn.getPackageName());
        //intent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_TIME, time);
        intent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED, "posted");
        context.sendBroadcast(intent);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //Log.e(TAG, "********** onNOtificationRemoved");
        //Log.e(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());

        Context context = getApplicationContext();

        if (sbn.getPackageName().equals(context.getPackageName()))
            return;

        //GlobalData.logE("#### PPNotificationListenerService.onNotificationRemoved","xxx");

        getNotifiedPackages(context);
        removeNotifiedPackage(sbn.getPackageName());
        saveNotifiedPackages(context);

        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        //intent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME, sbn.getPackageName());
        //intent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_TIME, time);
        intent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED, "removed");
        context.sendBroadcast(intent);

    }

    // Android 5.0 Lollipop

    @Override public void onListenerConnected() {
        //Log.e(TAG, "onListenerConnected()");
    }
    @Override public void onListenerHintsChanged(int hints) {
        //Log.e(TAG, "onListenerHintsChanged(" + hints + ')');
    }

    @SuppressWarnings("deprecation")
    private static boolean vibrationIsOn(Context context, AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();
        int vibrateType = -999;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)
            vibrateType = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        int vibrateWhenRinging;
        if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
            vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), "vibrate_when_ringing", 0);
        else
            vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);

        GlobalData.logE(TAG, "vibrationIsOn(ringerMode="+ringerMode+")");
        GlobalData.logE(TAG, "vibrationIsOn(vibrateType="+vibrateType+")");
        GlobalData.logE(TAG, "vibrationIsOn(vibrateWhenRinging="+vibrateWhenRinging+")");

        return (ringerMode == AudioManager.RINGER_MODE_VIBRATE) ||
               (vibrateType == AudioManager.VIBRATE_SETTING_ON) ||
               (vibrateType == AudioManager.VIBRATE_SETTING_ONLY_SILENT) ||
               (vibrateWhenRinging == 1);
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        GlobalData.logE(TAG, "onInterruptionFilterChanged(interruptionFilter=" + interruptionFilter + ')');
        if (!RingerModeChangeReceiver.internalChange) {
            GlobalData.logE(TAG, "onInterruptionFilterChanged(!internalChange)");

            final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

            // convert to profile zenMode
            int zenMode = 0;
            switch (interruptionFilter) {
                case NotificationListenerService.INTERRUPTION_FILTER_ALL:
                    if (vibrationIsOn(getApplicationContext(), audioManager))
                        zenMode = 4;
                    else
                        zenMode = 1;
                    break;
                case NotificationListenerService.INTERRUPTION_FILTER_PRIORITY:
                    if (vibrationIsOn(getApplicationContext(), audioManager))
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
            GlobalData.logE(TAG, "onInterruptionFilterChanged(zenMode=" + zenMode + ')');
            if (zenMode != 0) {
                //Log.e(TAG, "onInterruptionFilterChanged  new zenMode=" + zenMode);
                GlobalData.setRingerMode(getApplicationContext(), 5);
                GlobalData.setZenMode(getApplicationContext(), zenMode);
            }
        }

        //RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());

    }

    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        int interruptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
        GlobalData.logE(TAG, "getZenMode(interruptionFilter=" + interruptionFilter + ')');
        switch (interruptionFilter) {
            case ActivateProfileHelper.ZENMODE_ALL:
                if (vibrationIsOn(context, audioManager))
                    zenMode = 4;
                else
                    zenMode = 1;
                break;
            case ActivateProfileHelper.ZENMODE_PRIORITY:
                if (vibrationIsOn(context, audioManager))
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
        GlobalData.logE(TAG, "getZenMode(zenMode=" + zenMode + ')');
        return zenMode;
    }

    public static void setZenMode(Context context, AudioManager audioManager) {
        int zenMode = getZenMode(context, audioManager);
        if (zenMode != 0) {
            GlobalData.setRingerMode(context, 5);
            GlobalData.setZenMode(context, zenMode);
        }
    }

    public static boolean isNotificationListenerServiceEnabled(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = PPNotificationListenerService.class.getName();

        //Log.e(TAG, "enabledNotificationListeners(" + enabledNotificationListeners + ')');
        //Log.e(TAG, "packageName=" + packageName);

        // check to see if the enabledNotificationListeners String contains our package name
        if ((enabledNotificationListeners == null) || (!enabledNotificationListeners.contains(packageName)))
        {
            // in this situation we know that the user has not granted the app the Notification access permission
            //Log.e(TAG, "isNotificationListenerServiceEnabled=false");
            return false;
        }
        else
        {
            //Log.e(TAG, "isNotificationListenerServiceEnabled=true");
            return true;
        }
    }

    public static Intent getInterruptionFilterRequestIntent(Context context, final int filter) {
        Intent request = new Intent(ACTION_REQUEST_INTERRUPTION_FILTER);
        //request.setComponent(new ComponentName(context, PPNotificationListenerService.class));
        //request.setPackage(context.getPackageName());
        request.putExtra(EXTRA_FILTER, filter);
        return request;
    }

    /** Convenience method for sending an {@link android.content.Intent} with {@link #ACTION_REQUEST_INTERRUPTION_FILTER}. */
    public static void requestInterruptionFilter(Context context, final int filter) {
        //Log.e(TAG, "requestInterruptionFilter(" + filter + ')');
        //Log.e(TAG, "requestInterruptionFilter(" + ACTION_REQUEST_INTERRUPTION_FILTER + ')');
        Intent request = getInterruptionFilterRequestIntent(context, filter);
        context.sendBroadcast(request);
    }

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand(" + intent.getAction() + ", " + flags + ", " + startId + ')');

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            // Handle being told to change the interruption filter (zen mode).
            if (!TextUtils.isEmpty(intent.getAction())) {
                if (ACTION_REQUEST_INTERRUPTION_FILTER.equals(intent.getAction())) {
                    if (intent.hasExtra(EXTRA_FILTER)) {
                        final int zenMode = intent.getIntExtra(EXTRA_FILTER, ActivateProfileHelper.ZENMODE_ALL);
                        Log.e(TAG, "zenMode = " + zenMode);
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

        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.e(TAG, "NLServiceReceiver.onReceive(" + intent.getAction()  + ')');

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                // Handle being told to change the interruption filter (zen mode).
                if (!TextUtils.isEmpty(intent.getAction())) {
                    if (ACTION_REQUEST_INTERRUPTION_FILTER.equals(intent.getAction())) {
                        if (intent.hasExtra(EXTRA_FILTER)) {
                            final int filter = intent.getIntExtra(EXTRA_FILTER, INTERRUPTION_FILTER_ALL);
                            //Log.e(TAG, "filter= " + filter);
                            switch (filter) {
                                case INTERRUPTION_FILTER_ALL:
                                case INTERRUPTION_FILTER_PRIORITY:
                                case INTERRUPTION_FILTER_NONE:
                                case INTERRUPTION_FILTER_ALARMS:
                                    requestInterruptionFilter(filter);
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
        synchronized (GlobalData.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<PostedNotificationData>();

            notifications.clear();

            SharedPreferences preferences = context.getSharedPreferences(GlobalData.POSTED_NOTIFICATIONS_PREFS_NAME, Context.MODE_PRIVATE);

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
        synchronized (GlobalData.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<PostedNotificationData>();

            SharedPreferences preferences = context.getSharedPreferences(GlobalData.POSTED_NOTIFICATIONS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(POSTED_NOTIFICATIONS_COUNT_PREF, notifications.size());

            Gson gson = new Gson();

            for (int i = 0; i < notifications.size(); i++) {
                String json = gson.toJson(notifications.get(i));
                editor.putString(POSTED_NOTIFICATIONS_PACKAGE_PREF + i, json);
            }

            editor.commit();
        }
    }

    private void addNotifiedPackage(String packageName, long time)
    {
        synchronized (GlobalData.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<PostedNotificationData>();

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
        synchronized (GlobalData.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<PostedNotificationData>();

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

    public static PostedNotificationData getNotificationPosted(Context context, String packageName)
    {
        synchronized (GlobalData.notificationsChangeMutex) {

            if (notifications == null)
                notifications = new ArrayList<PostedNotificationData>();

            for (PostedNotificationData _notification : notifications) {
                String _packageName = _notification.getPackageName();
                if (packageName.equals(_packageName))
                    return _notification;
            }

            return null;
        }
    }

}
