package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class MobileCellsRegistrationService extends Service {

    public static String ACTION_COUNT_DOWN_TICK = "sk.henrichg.phoneprofilesplus.ACTION_COUNT_DOWN_TICK";
    public static String EXTRA_COUNTDOWN = "countdown";

    CountDownTimer countDownTimer = null;
    Notification notification = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //Log.d("MobileCellsRegistrationService", "START");

        PhoneStateScanner.autoRegistrationService = this;

        showNotification(GlobalData.getMobileCellsAutoRegistrationRemainingDuration(this));

        int remainingDuration = GlobalData.getMobileCellsAutoRegistrationRemainingDuration(this);

        final Context context = this;

        countDownTimer = new CountDownTimer(remainingDuration * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                //Log.d("MobileCellsRegistrationService", "Countdown seconds remaining: " + millisUntilFinished / 1000);

                showNotification(millisUntilFinished);

                GlobalData.setMobileCellsAutoRegistrationRemainingDuration(context, (int) millisUntilFinished / 1000);

                // broadcast for application preferences
                Intent intent = new Intent(ACTION_COUNT_DOWN_TICK);
                intent.putExtra(EXTRA_COUNTDOWN, millisUntilFinished);
                sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                //Log.d("MobileCellsRegistrationService", "Timer finished");

                PhoneProfilesService.instance.phoneStateScanner.enabledAutoRegistration = false;
                GlobalData.setMobileCellsAutoRegistration(context, false);

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
        long hours = iValue / 3600;
        long minutes = (iValue % 3600) / 60;
        long seconds = iValue % 60;
        time = time + ": " + String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
        startForeground(GlobalData.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID, notification);
    }

}
