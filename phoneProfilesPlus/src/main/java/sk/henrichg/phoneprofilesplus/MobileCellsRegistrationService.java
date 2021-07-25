package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

public class MobileCellsRegistrationService extends Service {

    // this is for show remaining time in "Cell registration" event sensor preference summary
    public static final String ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_COUNTDOWN";
    public static final String EXTRA_COUNTDOWN = "countdown";

    // this is for stop button in notification
    static final String ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_STOP_BUTTON";

    // this is for show new cell count in "Cell registration" event sensor preference summary
    public static final String ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_NEW_CELL";
    public static final String EXTRA_NEW_CELL_VALUE = "new_cell_value";

    private CountDownTimer countDownTimer = null;

    static boolean serviceStarted = false;
    static boolean forceStart;
    private Context context;

    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION = "mobile_cells_autoregistration_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION = "mobile_cells_autoregistration_remaining_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME = "mobile_cells_autoregistration_cell_name";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED = "mobile_cells_autoregistration_enabled";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_EVENT_LIST = "mobile_cells_autoregistration_event_list";

    private MobileCellsRegistrationStopButtonBroadcastReceiver mobileCellsRegistrationStopButtonBroadcastReceiver = null;

    @Override
    public void onCreate()
    {
        super.onCreate();

        //PPApplication.logE("MobileCellsRegistrationService.onCreate", "xxx");

        context = this;

        removeResultNotification();
        showNotification(getMobileCellsAutoRegistrationRemainingDuration(this));

        //registerReceiver(stopReceiver, new IntentFilter(MobileCellsRegistrationService.ACTION_STOP));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int remainingDuration = getMobileCellsAutoRegistrationRemainingDuration(this);
        //PPApplication.logE("MobileCellsRegistrationService.onCreate", "remainingDuration="+remainingDuration);

        if (remainingDuration > 0) {
            serviceStarted = true;

            PPApplication.forceStartMobileCellsScanner(this);
            forceStart = true;

            //MobileCellsScanner.autoRegistrationService = this;

            if (mobileCellsRegistrationStopButtonBroadcastReceiver == null) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON);
                mobileCellsRegistrationStopButtonBroadcastReceiver =
                        new MobileCellsRegistrationService.MobileCellsRegistrationStopButtonBroadcastReceiver();
                context.registerReceiver(mobileCellsRegistrationStopButtonBroadcastReceiver, intentFilter);
            }

            countDownTimer = new CountDownTimer(remainingDuration * 1000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    //Log.d("MobileCellsRegistrationService", "Countdown seconds remaining: " + millisUntilFinished / 1000);

                    showNotification(millisUntilFinished);

                    setMobileCellsAutoRegistrationRemainingDuration(context, (int) millisUntilFinished / 1000);

                    // broadcast for event preferences
                    Intent intent = new Intent(ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN);
                    intent.putExtra(EXTRA_COUNTDOWN, millisUntilFinished);
                    intent.setPackage(PPApplication.PACKAGE_NAME);
                    sendBroadcast(intent);
                }

                @Override
                public void onFinish() {
                    //Log.d("MobileCellsRegistrationService", "Timer finished");

                    if (serviceStarted && (MobileCellsScanner.enabledAutoRegistration))
                        stopRegistration();
                }
            };

            countDownTimer.start();
        }
        else {
            //PPApplication.logE("[REG] MobileCellsRegistrationService.onCreate", "setMobileCellsAutoRegistration(true)");
            setMobileCellsAutoRegistration(context, true);

            /*stopForeground(true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID);*/

            stopSelf();
        }

        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //PPApplication.logE("MobileCellsRegistrationService.onDestroy", "start");

        if (countDownTimer != null)
            countDownTimer.cancel();

        stopForeground(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        if (serviceStarted) {

            //MobileCellsScanner.autoRegistrationService = null;

            forceStart = false;
            PPApplication.restartMobileCellsScanner(this);

            showResultNotification();

            if (mobileCellsRegistrationStopButtonBroadcastReceiver != null) {
                try {
                    context.unregisterReceiver(mobileCellsRegistrationStopButtonBroadcastReceiver);
                } catch (IllegalArgumentException e) {
                    //PPApplication.recordException(e);
                }
                mobileCellsRegistrationStopButtonBroadcastReceiver = null;
            }
        }

        serviceStarted = false;

        //PPApplication.logE("MobileCellsRegistrationService.onDestroy", "end");
    }

    /*
    public static void stop(Context context) {
        try {
            context.sendBroadcast(new Intent(ACTION_STOP));
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }
    */

    private void showNotification(long millisUntilFinished) {
        String text;
        if (millisUntilFinished > 0) {
            text = getString(R.string.mobile_cells_registration_pref_dlg_status_started);
            String time = getString(R.string.mobile_cells_registration_pref_dlg_status_remaining_time);
            long iValue = millisUntilFinished / 1000;
            time = time + ": " + GlobalGUIRoutines.getDurationString((int) iValue);
            text = text + "; " + time;
//            if (android.os.Build.VERSION.SDK_INT < 24) {
//                text = text + " (" + getString(R.string.ppp_app_name) + ")";
//            }
        }
        else {
            text = getString(R.string.mobile_cells_registration_pref_dlg_status_stopped);
        }

        PPApplication.createMobileCellsRegistrationNotificationChannel(this);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this, PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(this, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_information_notify) // notification icon
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text) // message for notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true); // clear notification after click

        if (millisUntilFinished > 0) {
            // Android 12 - this do nit starts activity - OK
            Intent stopRegistrationIntent = new Intent(ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON);
            PendingIntent stopRegistrationPendingIntent = PendingIntent.getBroadcast(context, 0, stopRegistrationIntent, 0);
            mBuilder.addAction(R.drawable.ic_action_stop_white,
                    context.getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_stop),
                    stopRegistrationPendingIntent);
        }

        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notification.flags &= ~Notification.FLAG_SHOW_LIGHTS;
        notification.ledOnMS = 0;
        notification.ledOffMS = 0;
        notification.sound = null;
        notification.vibrate = null;
        notification.defaults &= ~DEFAULT_SOUND;
        notification.defaults &= ~DEFAULT_VIBRATE;
        startForeground(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID, notification);
    }

    private void stopRegistration() {
        //PPApplication.logE("MobileCellsRegistrationService.stopRegistration", "xxx");

        showNotification(0);
        PPApplication.sleep(500);

        //PPApplication.logE("[REG] MobileCellsRegistrationService.stopRegistration", "setMobileCellsAutoRegistration(true)");
        setMobileCellsAutoRegistration(context, true);

        // broadcast for event preferences
        Intent intent = new Intent(ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN);
        intent.putExtra(EXTRA_COUNTDOWN, 0L);
        intent.setPackage(PPApplication.PACKAGE_NAME);
        sendBroadcast(intent);

        stopSelf();
    }

    private void showResultNotification() {
        String text = getString(R.string.mobile_cells_registration_pref_dlg_status_stopped);
        String newCount = getString(R.string.mobile_cells_registration_pref_dlg_status_new_cells_count);
        long iValue = DatabaseHandler.getInstance(getApplicationContext()).getNewMobileCellsCount();
        newCount = newCount + " " + iValue;
        text = text + "; " + newCount;
//        if (android.os.Build.VERSION.SDK_INT < 24) {
//            text = text+" ("+getString(R.string.ppp_app_name)+")";
//        }

        PPApplication.createMobileCellsRegistrationNotificationChannel(this);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this, PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(this, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_information_notify) // notification icon
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text) // message for notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true); // clear notification after click

        //mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        Notification notification = mBuilder.build();
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        try {
            mNotificationManager.notify(
                    PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_TAG,
                    PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID, notification);
        } catch (Exception e) {
            //Log.e("MobileCellsRegistrationService.showResultNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    private void removeResultNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.cancel(
                    PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_TAG,
                    PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static public void getMobileCellsAutoRegistration(Context context) {
        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
        MobileCellsScanner.durationForAutoRegistration = preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
        MobileCellsScanner.cellsNameForAutoRegistration = preferences.getString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
        MobileCellsScanner.enabledAutoRegistration = preferences.getBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
        MobileCellsScanner.getAllEvents(preferences, PREF_MOBILE_CELLS_AUTOREGISTRATION_EVENT_LIST);
    }

    static public void setMobileCellsAutoRegistration(Context context, boolean clear) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        if (clear) {
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
            editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
            setMobileCellsAutoRegistrationRemainingDuration(context, 0);
            MobileCellsScanner.durationForAutoRegistration = 0;
            MobileCellsScanner.cellsNameForAutoRegistration = "";
            MobileCellsScanner.enabledAutoRegistration = false;
            MobileCellsScanner.clearEventList();
        }
        else {
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, MobileCellsScanner.durationForAutoRegistration);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, MobileCellsScanner.cellsNameForAutoRegistration);
            editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, MobileCellsScanner.enabledAutoRegistration);
        }
        MobileCellsScanner.saveAllEvents(editor, PREF_MOBILE_CELLS_AUTOREGISTRATION_EVENT_LIST);
        editor.apply();
    }

    static private int getMobileCellsAutoRegistrationRemainingDuration(Context context) {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, 0);
    }

    static public void setMobileCellsAutoRegistrationRemainingDuration(Context context, int remainingDuration) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, remainingDuration);
        editor.apply();
    }

    public class MobileCellsRegistrationStopButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            PPApplication.logE("[IN_BROADCAST] MobileCellsRegistrationService.MobileCellsRegistrationStopButtonBroadcastReceiver", "xxx");
            stopRegistration();
        }
    }

    public static class MobileCellsPreferenceUseBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            PPApplication.logE("[IN_BROADCAST] MobileCellsRegistrationService.MobileCellsPreferenceUseBroadcastReceiver", "xxx");
            //Log.d("MobileCellsRegistrationCellsDialogStateBroadcastReceiver", "xxx");
        }
    }

}
