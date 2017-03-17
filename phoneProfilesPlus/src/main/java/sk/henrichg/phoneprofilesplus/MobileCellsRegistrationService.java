package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class MobileCellsRegistrationService extends Service {

    public static String ACTION_COUNT_DOWN_TICK = "sk.henrichg.phoneprofilesplus.ACTION_COUNT_DOWN_TICK";
    public static String EXTRA_COUNTDOWN = "countdown";

    CountDownTimer countDownTimer = null;
    Notification notification = null;

    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION = "mobile_cells_autoregistration_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION = "mobile_cells_autoregistration_remaining_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME = "mobile_cells_autoregistration_cell_name";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED = "mobile_cells_autoregistration_enabled";

    @Override
    public void onCreate()
    {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate();
        //Log.d("MobileCellsRegistrationService", "START");

        PhoneStateScanner.autoRegistrationService = this;

        showNotification(getMobileCellsAutoRegistrationRemainingDuration(this));

        int remainingDuration = getMobileCellsAutoRegistrationRemainingDuration(this);

        final Context context = this;

        countDownTimer = new CountDownTimer(remainingDuration * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                //Log.d("MobileCellsRegistrationService", "Countdown seconds remaining: " + millisUntilFinished / 1000);

                showNotification(millisUntilFinished);

                setMobileCellsAutoRegistrationRemainingDuration(context, (int) millisUntilFinished / 1000);

                // broadcast for application preferences
                Intent intent = new Intent(ACTION_COUNT_DOWN_TICK);
                intent.putExtra(EXTRA_COUNTDOWN, millisUntilFinished);
                sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                //Log.d("MobileCellsRegistrationService", "Timer finished");

                PhoneProfilesService.phoneStateScanner.enabledAutoRegistration = false;
                setMobileCellsAutoRegistration(context, false);

                // broadcast for application preferences
                Intent intent = new Intent(ACTION_COUNT_DOWN_TICK);
                intent.putExtra(EXTRA_COUNTDOWN, 0L);
                sendBroadcast(intent);

                stopSelf();
            }
        };

        countDownTimer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {

        countDownTimer.cancel();

        stopForeground(true);

        PhoneStateScanner.autoRegistrationService = null;

        //Log.d("MobileCellsRegistrationService", "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification(long millisUntilFinished) {
        String text = getString(R.string.mobile_cells_registration_pref_dlg_status_started);
        String time = getString(R.string.mobile_cells_registration_pref_dlg_status_remaining_time);
        long iValue = millisUntilFinished / 1000;
        time = time + ": " + GlobalGUIRoutines.getDurationString((int)iValue);
        text = text + "; " + time;

        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text+" ("+getString(R.string.app_name)+")") // message for notification
                .setAutoCancel(true); // clear notification after click

        Intent intent = new Intent(this, PhoneProfilesPreferencesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "mobileCellsScanningCategory");
        //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pi);
        if (android.os.Build.VERSION.SDK_INT >= 16)
            mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        notification = mBuilder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID, notification);
    }


    static public void getMobileCellsAutoRegistration(Context context) {
        if (PhoneProfilesService.isPhoneStateStarted()) {
            ApplicationPreferences.getSharedPreferences(context);
            PhoneProfilesService.phoneStateScanner.durationForAutoRegistration = ApplicationPreferences.preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
            PhoneProfilesService.phoneStateScanner.cellsNameForAutoRegistration = ApplicationPreferences.preferences.getString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
            PhoneProfilesService.phoneStateScanner.enabledAutoRegistration = ApplicationPreferences.preferences.getBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
        }
    }

    static public void setMobileCellsAutoRegistration(Context context, boolean firstStart) {
        if (PhoneProfilesService.isPhoneStateStarted()) {
            ApplicationPreferences.getSharedPreferences(context);
            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, PhoneProfilesService.phoneStateScanner.durationForAutoRegistration);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, PhoneProfilesService.phoneStateScanner.cellsNameForAutoRegistration);
            if (firstStart)
                editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
            else
                editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, PhoneProfilesService.phoneStateScanner.enabledAutoRegistration);
            editor.apply();
        }
    }

    static public int getMobileCellsAutoRegistrationRemainingDuration(Context context) {
        if (PhoneProfilesService.isPhoneStateStarted()) {
            ApplicationPreferences.getSharedPreferences(context);
            return ApplicationPreferences.preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, 0);
        }
        return 0;
    }

    static public void setMobileCellsAutoRegistrationRemainingDuration(Context context, int remainingDuration) {
        if (PhoneProfilesService.isPhoneStateStarted()) {
            ApplicationPreferences.getSharedPreferences(context);
            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, remainingDuration);
            editor.apply();
        }
    }

}
