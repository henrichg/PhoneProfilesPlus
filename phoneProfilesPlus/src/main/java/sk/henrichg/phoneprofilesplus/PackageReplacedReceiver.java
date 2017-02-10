package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.Calendar;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

        //int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
        //int myUid = android.os.Process.myUid();
        //if (intentUid == myUid)
        //{

            // start delayed bootup broadcast
            PPApplication.startedOnBoot = true;
            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent delayedBootUpIntent = new Intent(context, DelayedBootUpReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, delayedBootUpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 10);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

            PPApplication.setShowRequestAccessNotificationPolicyPermission(context.getApplicationContext(), true);
            PPApplication.setShowRequestWriteSettingsPermission(context.getApplicationContext(), true);
            PPApplication.setShowEnableLocationNotification(context.getApplicationContext(), true);
            PPApplication.setScreenUnlocked(context.getApplicationContext(), true);

            int oldVersionCode = PPApplication.getSavedVersionCode(context.getApplicationContext());
            PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "oldVersionCode="+oldVersionCode);
            int actualVersionCode;
            try {
                PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                actualVersionCode = pinfo.versionCode;
                PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "actualVersionCode=" + actualVersionCode);

                if (oldVersionCode < actualVersionCode) {
                    if (actualVersionCode <= 2322) {
                        // for old packages use Priority in events
                        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "applicationEventUsePriority=true");
                        editor.putBoolean(PPApplication.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                        editor.commit();
                        PPApplication.loadPreferences(context);
                    }
                    if (actualVersionCode <= 2400) {
                        PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "donation alarm restart");
                        PPApplication.setDaysAfterFirstStart(context, 0);
                        AboutApplicationBroadcastReceiver.setAlarm(context);
                    }
                    if (actualVersionCode <= 2500) {
                        // for old packages hide profile notification from status bar if notification is disabled
                        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                        if (!preferences.getBoolean(PPApplication.PREF_NOTIFICATION_STATUS_BAR, true)) {
                            SharedPreferences.Editor editor = preferences.edit();
                            PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "notificationShowInStatusBar=false");
                            editor.putBoolean(PPApplication.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                            editor.commit();
                            PPApplication.loadPreferences(context);
                        }
                    }
                    if (actualVersionCode <= 2700) {
                        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, false);
                        editor.putBoolean(EventPreferencesActivity.PREF_START_TARGET_HELPS, false);
                        editor.commit();
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }

            PPApplication.logE("PackageReplacedReceiver.onReceive","PhoneProfilesService.instance="+PhoneProfilesService.instance);

            if (PPApplication.getApplicationStarted(context, false))
            {
                PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "start PhoneProfilesService");

                if (PhoneProfilesService.instance != null) {
                    // stop PhoneProfilesService
                    context.stopService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
                    PPApplication.sleep(2000);
                }

                // must by false for avoiding starts/pause events before restart events
                PPApplication.setApplicationStarted(context, false);

                // start PhoneProfilesService
                Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PPApplication.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_START_ON_BOOT, false);
                context.startService(serviceIntent);

            }
        //}

    }

}
