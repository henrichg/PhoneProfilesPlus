package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import java.util.Calendar;

public class AlarmClockBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_ALARM_PACKAGE_NAME = "alarm_package_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### AlarmClockBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "AlarmClockBroadcastReceiver.onReceive", "AlarmClockBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            return;

        Calendar now = Calendar.getInstance();
        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
        final long _time = now.getTimeInMillis() + gmtOffset;

        final String alarmPackageName = intent.getStringExtra(EXTRA_ALARM_PACKAGE_NAME);

        if (Event.getGlobalEventsRunning()) {
            //PPApplication.logE("@@@ AlarmClockBroadcastReceiver.onReceive", "start service");

            PPApplication.startHandlerThread(/*"AlarmClockBroadcastReceiver.onReceive"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AlarmClockBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=AlarmClockBroadcastReceiver.onReceive");

                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.setEventAlarmClockParameters(_time, alarmPackageName);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_ALARM_CLOCK);

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=AlarmClockBroadcastReceiver.onReceive");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            });
        }
    }
}

/*

<receiver
            android:name=".AlarmClockBroadcastReceiver"
            android:enabled="false"
            android:exported="true"
            tools:ignore="ExportedReceiver">
            <intent-filter>

                <!--
                // Stock alarms
                // Nexus (?)
                -->
                <action android:name="com.android.deskclock.ALARM_ALERT"/>
                <!--
                <action android:name="com.android.deskclock.ALARM_DISMISS" />
                <action android:name="com.android.deskclock.ALARM_DONE" />
                <action android:name="com.android.deskclock.ALARM_SNOOZE" />
                -->
                <!-- // stock Android (?) -->
                <action android:name="com.android.alarmclock.ALARM_ALERT"/>
                <!--
                // Stock alarm Manufactures
                // Samsung
                -->
                <action android:name="com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT"/>
                <!-- // HTC -->
                <action android:name="com.htc.android.worldclock.ALARM_ALERT"/>
                <action android:name="com.htc.android.ALARM_ALERT"/>
                <!-- // Sony -->
                <action android:name="com.sonyericsson.alarm.ALARM_ALERT"/>
                <!-- // ZTE -->
                <action android:name="zte.com.cn.alarmclock.ALARM_ALERT"/>
                <!-- // Motorola -->
                <action android:name="com.motorola.blur.alarmclock.ALARM_ALERT"/>
                <!-- // LG -->
                <action android:name="com.lge.clock.ALARM_ALERT"/>
                <!--
                // Third-party Alarms
                // Gentle Alarm
                -->
                <action android:name="com.mobitobi.android.gentlealarm.ALARM_INFO"/>
                <!-- // Sleep As Android -->
                <action android:name="com.urbandroid.sleep.alarmclock.ALARM_ALERT"/>
                <!-- // Alarmdroid (1.13.2) -->
                <action android:name="com.splunchy.android.alarmclock.ALARM_ALERT"/>
            </intent-filter>
        </receiver>

*/
