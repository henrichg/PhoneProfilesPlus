package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PPNotificationListenerService extends NotificationListenerService {

    public static final String ACTION_REQUEST_INTERRUPTION_FILTER =
            PPNotificationListenerService.class.getPackage().getName() + '.' + "ACTION_REQUEST_INTERRUPTION_FILTER";
    public static final String EXTRA_FILTER = "filter";

    public static final String TAG = PPNotificationListenerService.class.getSimpleName();

    private NLServiceReceiver nlservicereceiver;

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
        super.onNotificationPosted(sbn);
        Log.e(TAG, "**********  onNotificationPosted");
        Log.e(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.e(TAG, "********** onNOtificationRemoved");
        Log.e(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
    }

    // Android 5.0 Lollipop

    @Override public void onListenerConnected() {
        Log.e(TAG, "onListenerConnected()");
    }
    @Override public void onListenerHintsChanged(int hints) {
        Log.e(TAG, "onListenerHintsChanged(" + hints + ')');
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        Log.e(TAG, "onInterruptionFilterChanged(" + interruptionFilter + ')');

        /*
        if (interruptionFilter == NotificationListenerService.INTERRUPTION_FILTER_ALL) {
            final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            SettingsContentObserver.internalChange = true;
            //audioManager.setStreamVolume(AudioManager.STREAM_ALARM,  1, 0);
            //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            audioManager.setStreamMute(AudioManager.STREAM_RING, false);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 6, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 6, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 6, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 6, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 6, 0);
        }
        */
    }

    public static boolean isNotificationListenerServiceEnabled(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = PPNotificationListenerService.class.getName();

        Log.e(TAG, "enabledNotificationListeners(" + enabledNotificationListeners + ')');
        Log.e(TAG, "packageName=" + packageName);

        // check to see if the enabledNotificationListeners String contains our package name
        if ((enabledNotificationListeners == null) || (!enabledNotificationListeners.contains(packageName)))
        {
            // in this situation we know that the user has not granted the app the Notification access permission
            Log.e(TAG, "isNotificationListenerServiceEnabled=false");
            return false;
        }
        else
        {
            Log.e(TAG, "isNotificationListenerServiceEnabled=true");
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
        Log.e(TAG, "requestInterruptionFilter(" + filter + ')');
        Log.e(TAG, "requestInterruptionFilter(" + ACTION_REQUEST_INTERRUPTION_FILTER + ')');
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
            Log.e(TAG, "NLServiceReceiver.onReceive(" + intent.getAction()  + ')');

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                // Handle being told to change the interruption filter (zen mode).
                if (!TextUtils.isEmpty(intent.getAction())) {
                    if (ACTION_REQUEST_INTERRUPTION_FILTER.equals(intent.getAction())) {
                        if (intent.hasExtra(EXTRA_FILTER)) {
                            final int filter = intent.getIntExtra(EXTRA_FILTER, INTERRUPTION_FILTER_ALL);
                            Log.e(TAG, "filter= " + filter);
                            switch (filter) {
                                case INTERRUPTION_FILTER_ALL:
                                case INTERRUPTION_FILTER_PRIORITY:
                                case INTERRUPTION_FILTER_NONE:
                                    requestInterruptionFilter(filter);
                                    break;
                            }
                        }
                    }
                }
            }

        }
    }
}
