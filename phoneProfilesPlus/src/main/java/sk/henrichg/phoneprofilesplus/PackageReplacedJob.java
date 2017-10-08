package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class PackageReplacedJob extends Job {

    static final String JOB_TAG  = "PackageReplacedJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "PackageReplacedJob.onRunJob", "PackageReplacedJob_onRunJob");

        // if startedOnBoot = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
        PPApplication.startedOnBoot = true;
        final Handler handler = new Handler(appContext.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("PackageReplacedJob.onRunJob", "delayed boot up");
                PPApplication.startedOnBoot = false;
            }
        }, 10000);

        Permissions.setShowRequestAccessNotificationPolicyPermission(appContext, true);
        Permissions.setShowRequestWriteSettingsPermission(appContext, true);
        Scanner.setShowEnableLocationNotification(appContext, true);
        //ActivateProfileHelper.setScreenUnlocked(appContext, true);

        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
        PPApplication.logE("@@@ PackageReplacedJob.onRunJob", "oldVersionCode="+oldVersionCode);
        int actualVersionCode;
        try {
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            actualVersionCode = pInfo.versionCode;
            PPApplication.logE("@@@ PackageReplacedJob.onRunJob", "actualVersionCode=" + actualVersionCode);

            if (oldVersionCode < actualVersionCode) {
                if (actualVersionCode <= 2322) {
                    // for old packages use Priority in events
                    ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    PPApplication.logE("@@@ PackageReplacedJob.onRunJob", "applicationEventUsePriority=true");
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                    editor.apply();
                }
                if (actualVersionCode <= 2400) {
                    PPApplication.logE("@@@ PackageReplacedJob.onRunJob", "donation alarm restart");
                    PPApplication.setDaysAfterFirstStart(appContext, 0);
                    AboutApplicationJob.scheduleJob();
                }
                if (actualVersionCode <= 2500) {
                    // for old packages hide profile notification from status bar if notification is disabled
                    ApplicationPreferences.getSharedPreferences(appContext);
                    if (!ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true)) {
                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                        PPApplication.logE("@@@ PackageReplacedJob.onRunJob", "notificationShowInStatusBar=false");
                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                        editor.apply();
                    }
                }
                if (actualVersionCode <= 2700) {
                    ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();

                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);

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
                    editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS_SAVE, false);
                    editor.putBoolean(EventPreferencesActivity.PREF_START_TARGET_HELPS, false);
                    editor.apply();
                }
                if (actualVersionCode <= 3200) {
                    ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, true);
                    editor.apply();
                }
                if (actualVersionCode <= 3500) {
                    ApplicationPreferences.getSharedPreferences(appContext);
                    if (!ApplicationPreferences.preferences.contains(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT)) {
                        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, ApplicationPreferences.applicationActivateWithAlert(appContext));
                        editor.apply();
                    }
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        PPApplication.logE("PackageReplacedJob.onRunJob","PhoneProfilesService.instance="+PhoneProfilesService.instance);

        if (PPApplication.getApplicationStarted(appContext, false))
        {
            PPApplication.logE("@@@ PackageReplacedJob.onRunJob", "start PhoneProfilesService");

            if (PhoneProfilesService.instance != null) {
                // stop PhoneProfilesService
                appContext.stopService(new Intent(appContext, PhoneProfilesService.class));
                Handler _handler = new Handler(appContext.getMainLooper());
                Runnable r = new Runnable() {
                    public void run() {
                        startService(appContext);
                    }
                };
                _handler.postDelayed(r, 2000);
            }
            else
                startService(appContext);
        }

        return Result.SUCCESS;
    }

    static void start() {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);
        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .startNow()
                .build()
                .schedule();
    }

    private void startService(Context context) {
        // must by false for avoiding starts/pause events before restart events
        PPApplication.setApplicationStarted(context, false);

        // start PhoneProfilesService
        Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        //TODO Android O
        //if (Build.VERSION.SDK_INT < 26)
        context.startService(serviceIntent);
        //else
        //    context.startForegroundService(serviceIntent);
    }

}
