package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MobileCellsRegistrationService extends Service
        implements MobileCellsRegistrationServiceStopRegistrationListener {

    // this is for show remaining time in "Cell registration" event sensor preference summary
    static final String ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_COUNTDOWN";
    static final String EXTRA_COUNTDOWN = "countdown";

    // this is for stop button in notification
    static final String ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_STOP_BUTTON";

    // this is for show new cell count in "Cell registration" event sensor preference summary
    static final String ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_NEW_CELL";
    //static final String EXTRA_NEW_CELL_VALUE = "new_cell_value";

    private CountDownTimer countDownTimer = null;

    static volatile boolean serviceStarted = false;
    private Context context;

    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION = "mobile_cells_autoregistration_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION = "mobile_cells_autoregistration_remaining_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME = "mobile_cells_autoregistration_cell_name";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED = "mobile_cells_autoregistration_enabled";
    //private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_EVENT_LIST = "mobile_cells_autoregistration_event_list";

    private MobileCellsRegistrationStopButtonBroadcastReceiver mobileCellsRegistrationStopButtonBroadcastReceiver = null;

    @Override
    public void onCreate()
    {
        super.onCreate();

        context = this;

        PPApplicationStatic.createMobileCellsRegistrationNotificationChannel(getApplicationContext(), false);
        removeResultNotification();
        showNotification(getMobileCellsAutoRegistrationRemainingDuration(this));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int remainingDuration = getMobileCellsAutoRegistrationRemainingDuration(this);

        if (remainingDuration > 0) {
            serviceStarted = true;

            PPApplication.mobileCellsRegistraitonForceStart = true;
            PPApplicationStatic.forceStartMobileCellsScanner(this);

            //MobileCellsScanner.autoRegistrationService = this;

            if (mobileCellsRegistrationStopButtonBroadcastReceiver == null) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON);
                mobileCellsRegistrationStopButtonBroadcastReceiver =
                        new MobileCellsRegistrationService.MobileCellsRegistrationStopButtonBroadcastReceiver(this);
                int receiverFlags = 0;
                if (Build.VERSION.SDK_INT >= 34)
                    receiverFlags = RECEIVER_NOT_EXPORTED;
                context.registerReceiver(mobileCellsRegistrationStopButtonBroadcastReceiver, intentFilter, receiverFlags);
            }

            PPApplicationStatic.createMobileCellsRegistrationNotificationChannel(getApplicationContext(), false);

            countDownTimer = new CountDownTimer(remainingDuration * 1000L, 1000) {

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

                    if (serviceStarted && (PPApplication.mobileCellsScannerEnabledAutoRegistration))
                        stopRegistration();
                }
            };

            countDownTimer.start();
        }
        else {
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

        if (countDownTimer != null)
            countDownTimer.cancel();

        stopForeground(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        if (serviceStarted) {

            //MobileCellsScanner.autoRegistrationService = null;

            PPApplication.mobileCellsRegistraitonForceStart = false;
            PPApplicationStatic.restartMobileCellsScanner(this);

            showResultNotification();
        }

        try {
            context.unregisterReceiver(mobileCellsRegistrationStopButtonBroadcastReceiver);
        } catch (Exception ignored) {}
        mobileCellsRegistrationStopButtonBroadcastReceiver = null;

        serviceStarted = false;

    }

    private void showNotification(long millisUntilFinished) {
        String text;
        if (millisUntilFinished > 0) {
            text = getString(R.string.mobile_cells_registration_pref_dlg_status_started);
            String time = getString(R.string.mobile_cells_registration_pref_dlg_status_remaining_time);
            long iValue = millisUntilFinished / 1000;
            time = time + StringConstants.STR_COLON_WITH_SPACE + StringFormatUtils.getDurationString((int) iValue);
            text = text + "; " + time;
        }
        else {
            text = getString(R.string.mobile_cells_registration_pref_dlg_status_stopped);
        }

        //PPApplicationStatic.createMobileCellsRegistrationNotificationChannel(this);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext(), PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL_SILENT)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.informationColor))
                .setSmallIcon(R.drawable.ic_ppp_notification/*ic_information_notify*/) // notification icon
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_information_notification))
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text) // message for notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true); // clear notification after click

        if (millisUntilFinished > 0) {
            // Android 12 - this do not starts activity - OK
            Intent stopRegistrationIntent = new Intent(ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON);
            PendingIntent stopRegistrationPendingIntent = PendingIntent.getBroadcast(context, 0, stopRegistrationIntent, 0);
            mBuilder.addAction(R.drawable.ic_action_stop,
                    context.getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_stop),
                    stopRegistrationPendingIntent);
        }

        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        mBuilder.setGroup(PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_GROUP);

        if (Build.VERSION.SDK_INT >= 33) {
            // required, because in API 33+ foreground serbice notification is dismissable
            mBuilder.setOngoing(true);
        }

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        /*notification.flags &= ~Notification.FLAG_SHOW_LIGHTS;
        notification.ledOnMS = 0;
        notification.ledOffMS = 0;
        notification.sound = null;
        notification.vibrate = null;
        notification.defaults &= ~DEFAULT_SOUND;
        notification.defaults &= ~DEFAULT_VIBRATE;*/
        startForeground(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID, notification);
    }

    private void stopRegistration() {
        PPApplicationStatic.createMobileCellsRegistrationNotificationChannel(getApplicationContext(), false);
        showNotification(0);
        GlobalUtils.sleep(500);

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

        PPApplicationStatic.createInformationNotificationChannel(getApplicationContext(), false);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext(), PPApplication.INFORMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.informationColor))
                .setSmallIcon(R.drawable.ic_ppp_notification/*ic_information_notify*/) // notification icon
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_information_notification))
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text) // message for notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true); // clear notification after click

        //mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        mBuilder.setGroup(PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_GROUP);

        Notification notification = mBuilder.build();
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        try {
            mNotificationManager.notify(
                    PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_TAG,
                    PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID, notification);
        } catch (SecurityException en) {
            PPApplicationStatic.logException("MobileCellsRegistrationService.showResultNotification", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("MobileCellsRegistrationService.showResultNotification", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    private void removeResultNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.cancel(
                    PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_TAG,
                    PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    static void getMobileCellsAutoRegistration(Context context) {
        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
        PPApplication.mobileCellsScannerDurationForAutoRegistration = preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
        PPApplication.mobileCellsScannerCellsNameForAutoRegistration = preferences.getString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
        PPApplication.mobileCellsScannerEnabledAutoRegistration = preferences.getBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
        //MobileCellsScanner.getAllEvents(preferences, PREF_MOBILE_CELLS_AUTOREGISTRATION_EVENT_LIST);
    }

    static void setMobileCellsAutoRegistration(Context context, boolean clear) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        if (clear) {
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
            editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
            setMobileCellsAutoRegistrationRemainingDuration(context, 0);
            PPApplication.mobileCellsScannerDurationForAutoRegistration = 0;
            PPApplication.mobileCellsScannerCellsNameForAutoRegistration = "";
            PPApplication.mobileCellsScannerEnabledAutoRegistration = false;
            //MobileCellsScanner.clearEventList();
        }
        else {
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, PPApplication.mobileCellsScannerDurationForAutoRegistration);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, PPApplication.mobileCellsScannerCellsNameForAutoRegistration);
            editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, PPApplication.mobileCellsScannerEnabledAutoRegistration);
        }
        //MobileCellsScanner.saveAllEvents(editor, PREF_MOBILE_CELLS_AUTOREGISTRATION_EVENT_LIST);
        editor.apply();
    }

    static private int getMobileCellsAutoRegistrationRemainingDuration(Context context) {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, 0);
    }

    static void setMobileCellsAutoRegistrationRemainingDuration(Context context, int remainingDuration) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, remainingDuration);
        editor.apply();
    }

    static private class MobileCellsRegistrationStopButtonBroadcastReceiver extends BroadcastReceiver {

        private final MobileCellsRegistrationServiceStopRegistrationListener listener;

        public MobileCellsRegistrationStopButtonBroadcastReceiver(
                MobileCellsRegistrationServiceStopRegistrationListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.stopRegistrationFromListener();
        }

    }

    @Override
    public void stopRegistrationFromListener() {
//            PPApplicationStatic.logE("[IN_BROADCAST] MobileCellsRegistrationService.MobileCellsRegistrationStopButtonBroadcastReceiver", "xxx");
        stopRegistration();
    }

    /*
    public static class MobileCellsPreferenceUseBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            PPApplicationStatic.logE("[IN_BROADCAST] MobileCellsRegistrationService.MobileCellsPreferenceUseBroadcastReceiver", "xxx");
            //Log.d("MobileCellsRegistrationCellsDialogStateBroadcastReceiver", "xxx");
        }
    }
    */
}
