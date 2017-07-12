package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.Calendar;

public class ProfileDurationAlarmBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### ProfileDurationAlarmBroadcastReceiver.onReceive", "xxx");
        Intent profileDurationServiceIntent = new Intent(context, ProfileDurationService.class);
        profileDurationServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0));
        WakefulIntentService.sendWakefulWork(context, profileDurationServiceIntent);
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static public void setAlarm(Profile profile, Context context)
    {
        removeAlarm(context);

        if (profile == null)
            return;

        if ((profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING) &&
            (profile._duration > 0))
        {
            // duration for start is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, profile._duration);
            long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

            Profile.setActivatedProfileEndDurationTime(context, alarmTime);

            //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            //String result = sdf.format(alarmTime);

            Intent intent = new Intent(context, ProfileDurationAlarmBroadcastReceiver.class);
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= 23)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
            if (android.os.Build.VERSION.SDK_INT >= 19)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

            //this._isInDelay = true;
        }
        //else
        //	this._isInDelay = false;

        //dataWrapper.getDatabaseHandler().updateEventInDelay(this);

    }

    static public void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, ProfileDurationAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        //this._isInDelay = false;
        //dataWrapper.getDatabaseHandler().updateEventInDelay(this);

        Profile.setActivatedProfileEndDurationTime(context, 0);
    }

}
