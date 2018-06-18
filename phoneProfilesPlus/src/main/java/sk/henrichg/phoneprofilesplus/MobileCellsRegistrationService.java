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
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MobileCellsRegistrationService extends Service {

    public static final String ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN = "sk.henrichg.phoneprofilesplus.ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN";
    public static final String EXTRA_COUNTDOWN = "countdown";
    public static final String ACTION_STOP_REGISTRATION_BUTTON = "sk.henrichg.phoneprofilesplus.ACTION_STOP_REGISTRATION_BUTTON";

    private CountDownTimer countDownTimer = null;

    static boolean forceStart;
    Context context;

    static private final List<Long> eventList = new ArrayList<>();

    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION = "mobile_cells_autoregistration_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION = "mobile_cells_autoregistration_remaining_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME = "mobile_cells_autoregistration_cell_name";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED = "mobile_cells_autoregistration_enabled";

    private MobileCellsRegistrationStopButtonBroadcastReceiver mobileCellsRegistrationStopButtonBroadcastReceiver = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //Log.d("MobileCellsRegistrationService", "START");

        context = this;

        PPApplication.forceStartPhoneStateScanner(this);
        forceStart = true;

        PhoneStateScanner.autoRegistrationService = this;

        removeResultNotification();
        showNotification(getMobileCellsAutoRegistrationRemainingDuration(this));

        int remainingDuration = getMobileCellsAutoRegistrationRemainingDuration(this);

        if (mobileCellsRegistrationStopButtonBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_STOP_REGISTRATION_BUTTON);
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {

        countDownTimer.cancel();

        PhoneStateScanner.autoRegistrationService = null;

        forceStart = false;
        PPApplication.restartPhoneStateScanner(this, false);

        stopForeground(true);

        showResultNotification();

        if (mobileCellsRegistrationStopButtonBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(mobileCellsRegistrationStopButtonBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mobileCellsRegistrationStopButtonBroadcastReceiver = null;
        }

        //Log.d("MobileCellsRegistrationService", "Timer cancelled");
        super.onDestroy();
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
                .setColor(ContextCompat.getColor(this, R.color.primary))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click

        Intent stopRegistrationIntent = new Intent(ACTION_STOP_REGISTRATION_BUTTON);
        PendingIntent stopRegistrationPendingIntent = PendingIntent.getBroadcast(context, 0, stopRegistrationIntent, 0);
        mBuilder.addAction(R.drawable.ic_action_stop,
                context.getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_stop),
                stopRegistrationPendingIntent);

        mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        startForeground(PPApplication.MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID, notification);
    }

    void stopRegistration() {
        PhoneStateScanner.enabledAutoRegistration = false;
        setMobileCellsAutoRegistration(context, false);

        // broadcast for event preferences
        Intent intent = new Intent(ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN);
        intent.putExtra(EXTRA_COUNTDOWN, 0L);
        intent.setPackage(context.getPackageName());
        sendBroadcast(intent);

        final Context appContext = context.getApplicationContext();

        PPApplication.startHandlerThread("MobileCellsRegistrationService.stopRegistration");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MobileCellsRegistrationService.stopRegistration");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                List<MobileCellsData> cellsList = new ArrayList<>();

                DatabaseHandler db = DatabaseHandler.getInstance(appContext);
                db.addMobileCellsToList(cellsList, true);

                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);

                for (MobileCellsData cellData : cellsList) {
                    for (Long event_id : eventList) {
                        Event event = dataWrapper.getEventById(event_id);
                        if (event != null) {
                            String cells = event._eventPreferencesMobileCells._cells;
                            cells = addCellId(cells, cellData.cellId);
                            event._eventPreferencesMobileCells._cells = cells;
                            db.updateMobileCellsCells(event);
                            dataWrapper.updateEvent(event);
                        }
                    }
                }
                eventList.clear();

                if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                    // start events handler
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_STATE/*, false*/);
                }

                dataWrapper.invalidateDataWrapper();


                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {}
                }
            }
        });

        stopSelf();
    }

    String addCellId(String cells, int cellId) {
        String[] splits = cells.split("\\|");
        String sCellId = Integer.toString(cellId);
        boolean found = false;
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (cell.equals(sCellId)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            if (!cells.isEmpty())
                cells = cells + "|";
            cells = cells + sCellId;
        }
        return cells;
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
                .setColor(ContextCompat.getColor(this, R.color.primary))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification)) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click

        //mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Notification notification = mBuilder.build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID, notification);
    }

    private void removeResultNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID);
    }

    static public void getMobileCellsAutoRegistration(Context context) {
        ApplicationPreferences.getSharedPreferences(context);
        PhoneStateScanner.durationForAutoRegistration = ApplicationPreferences.preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
        PhoneStateScanner.cellsNameForAutoRegistration = ApplicationPreferences.preferences.getString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
        PhoneStateScanner.enabledAutoRegistration = ApplicationPreferences.preferences.getBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
    }

    static public void setMobileCellsAutoRegistration(Context context, boolean firstStart) {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        if (firstStart) {
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
            editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
        }
        else {
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, PhoneStateScanner.durationForAutoRegistration);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, PhoneStateScanner.cellsNameForAutoRegistration);
            editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, PhoneStateScanner.enabledAutoRegistration);
        }
        editor.apply();
    }

    static private int getMobileCellsAutoRegistrationRemainingDuration(Context context) {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, 0);
    }

    static public void setMobileCellsAutoRegistrationRemainingDuration(Context context, int remainingDuration) {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, remainingDuration);
        editor.apply();
    }

    static boolean isEventAdded(long event_id) {
        return eventList.indexOf(event_id) != -1;
    }

    static void addEvent(long event_id) {
        eventList.add(event_id);
    }

    static void removeEvent(long event_id) {
        eventList.remove(event_id);
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

    public class MobileCellsPreferenceUseBroadcastReceiver extends BroadcastReceiver {

        MobileCellsPreferenceUseBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("MobileCellsRegistrationCellsDialogStateBroadcastReceiver", "xxx");
        }
    }

}
