package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

public class MobileCellsRegistrationService extends Service {

    // this is for show remaining time in "Cell registration" event sensor preference summary
    public static final String ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_COUNTDOWN";
    public static final String EXTRA_COUNTDOWN = "countdown";

    // this is for stop button in notification
    private static final String ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_STOP_BUTTON";

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

    private static final String ACTION_STOP = PPApplication.PACKAGE_NAME + ".MobileCellsRegistrationService.ACTION_STOP_SERVICE";

    private MobileCellsRegistrationStopButtonBroadcastReceiver mobileCellsRegistrationStopButtonBroadcastReceiver = null;

    /*private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PPApplication.logE("MobileCellsRegistrationService.stopReceiver", "xxx");
            try {
                //noinspection deprecation
                context.removeStickyBroadcast(intent);
            } catch (Exception e) {
                PPApplication.logE("MobileCellsRegistrationService.stopReceiver", Log.getStackTraceString(e));
            }
            stopForeground(true);
            stopSelf();
        }
    };*/

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPApplication.logE("MobileCellsRegistrationService.onCreate", "xxx");

        context = this;

        //registerReceiver(stopReceiver, new IntentFilter(MobileCellsRegistrationService.ACTION_STOP));

        removeResultNotification();
        showNotification(getMobileCellsAutoRegistrationRemainingDuration(this));

        int remainingDuration = getMobileCellsAutoRegistrationRemainingDuration(this);
        PPApplication.logE("MobileCellsRegistrationService.onCreate", "remainingDuration="+remainingDuration);

        if (remainingDuration > 0) {
            serviceStarted = true;

            PPApplication.forceStartPhoneStateScanner(this);
            forceStart = true;

            //PhoneStateScanner.autoRegistrationService = this;

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
                    intent.setPackage(context.getPackageName());
                    sendBroadcast(intent);
                }

                @Override
                public void onFinish() {
                    //Log.d("MobileCellsRegistrationService", "Timer finished");

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
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        PPApplication.logE("MobileCellsRegistrationService.onDestroy", "xxx");

        /*try {
            unregisterReceiver(stopReceiver);
        } catch (Exception ignored) {}*/

        if (serviceStarted) {
            countDownTimer.cancel();

            //PhoneStateScanner.autoRegistrationService = null;

            forceStart = false;
            PPApplication.restartPhoneStateScanner(this);

            stopForeground(true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID);

            showResultNotification();

            if (mobileCellsRegistrationStopButtonBroadcastReceiver != null) {
                try {
                    context.unregisterReceiver(mobileCellsRegistrationStopButtonBroadcastReceiver);
                } catch (IllegalArgumentException e) {
                    //Crashlytics.logException(e);
                }
                mobileCellsRegistrationStopButtonBroadcastReceiver = null;
            }
        }
        else {
            stopForeground(true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID);
        }

        serviceStarted = false;
    }

    public static void stop(Context context) {
        try {
            //noinspection deprecation
            context.sendStickyBroadcast(new Intent(ACTION_STOP));
            //context.sendBroadcast(new Intent(ACTION_STOP));
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void showNotification(long millisUntilFinished) {
        String text = getString(R.string.mobile_cells_registration_pref_dlg_status_started);
        String time = getString(R.string.mobile_cells_registration_pref_dlg_status_remaining_time);
        long iValue = millisUntilFinished / 1000;
        time = time + ": " + GlobalGUIRoutines.getDurationString((int)iValue);
        text = text + "; " + time;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            text = text+" ("+getString(R.string.app_name)+")";
        }

        PPApplication.createMobileCellsRegistrationNotificationChannel(this);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this, PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(this, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click

        Intent stopRegistrationIntent = new Intent(ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON);
        PendingIntent stopRegistrationPendingIntent = PendingIntent.getBroadcast(context, 0, stopRegistrationIntent, 0);
        mBuilder.addAction(R.drawable.ic_action_stop_white,
                context.getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_stop),
                stopRegistrationPendingIntent);

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
        PPApplication.logE("MobileCellsRegistrationService.stopRegistration", "xxx");

        setMobileCellsAutoRegistration(context, true);

        // broadcast for event preferences
        Intent intent = new Intent(ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN);
        intent.putExtra(EXTRA_COUNTDOWN, 0L);
        intent.setPackage(context.getPackageName());
        sendBroadcast(intent);

        stopSelf();
    }

    private void showResultNotification() {
        String text = getString(R.string.mobile_cells_registration_pref_dlg_status_stopped);
        String newCount = getString(R.string.mobile_cells_registration_pref_dlg_status_new_cells_count);
        long iValue = DatabaseHandler.getInstance(getApplicationContext()).getNewMobileCellsCount();
        newCount = newCount + " " + iValue;
        text = text + "; " + newCount;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            text = text+" ("+getString(R.string.app_name)+")";
        }

        PPApplication.createMobileCellsRegistrationNotificationChannel(this);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this, PPApplication.MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(this, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click

        //mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        Notification notification = mBuilder.build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            try {
                mNotificationManager.notify(PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID, notification);
            } catch (Exception e) {
                Log.e("MobileCellsRegistrationService.showResultNotification", Log.getStackTraceString(e));
                Crashlytics.logException(e);
            }
        }
    }

    private void removeResultNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID);
    }

    static public void getMobileCellsAutoRegistration(Context context) {
        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
        PhoneStateScanner.durationForAutoRegistration = preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
        PhoneStateScanner.cellsNameForAutoRegistration = preferences.getString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
        PhoneStateScanner.enabledAutoRegistration = preferences.getBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
    }

    static public void setMobileCellsAutoRegistration(Context context, boolean clear) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        if (clear) {
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
            editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
            setMobileCellsAutoRegistrationRemainingDuration(context, 0);
            PhoneStateScanner.durationForAutoRegistration = 0;
            PhoneStateScanner.cellsNameForAutoRegistration = "";
            PhoneStateScanner.enabledAutoRegistration = false;
        }
        else {
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, PhoneStateScanner.durationForAutoRegistration);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, PhoneStateScanner.cellsNameForAutoRegistration);
            editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, PhoneStateScanner.enabledAutoRegistration);
        }
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

        //final MobileCellsRegistrationDialogPreference preference;

        MobileCellsRegistrationStopButtonBroadcastReceiver(/*MobileCellsRegistrationDialogPreference preference*/) {
            //this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("MobileCellsRegistrationStopButtonBroadcastReceiver", "xxx");
            stopRegistration();
        }
    }

    public static class MobileCellsPreferenceUseBroadcastReceiver extends BroadcastReceiver {

        MobileCellsPreferenceUseBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("MobileCellsRegistrationCellsDialogStateBroadcastReceiver", "xxx");
        }
    }

}
