package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ActionForExternalApplicationActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;

    private String action;

    private String profileName = "";
    private String eventName = "";
    private long profile_id = 0;
    private long event_id = 0;

    // !!! do change this actions, these are for Tasker !!!!
    static final String ACTION_ACTIVATE_PROFILE = PPApplication.PACKAGE_NAME + ".ACTION_ACTIVATE_PROFILE";
    private static final String ACTION_RESTART_EVENTS = PPApplication.PACKAGE_NAME + ".ACTION_RESTART_EVENTS";
    private static final String ACTION_ENABLE_RUN_FOR_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_ENABLE_RUN_FOR_EVENT";
    private static final String ACTION_PAUSE_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_PAUSE_EVENT";
    private static final String ACTION_STOP_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_STOP_EVENT";

    static final String EXTRA_EVENT_NAME = "event_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        //Log.e("ActionForExternalApplicationActivity.onCreate", "xxx");

        Intent intent = getIntent();

        action = intent.getAction();
        //Log.e("ActionForExternalApplicationActivity.onCreate", "action="+action);

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        if (action != null) {
            if (action.equals(ACTION_ACTIVATE_PROFILE)) {
                profileName = intent.getStringExtra(ActivateProfileFromExternalApplicationActivity.EXTRA_PROFILE_NAME);
                if (profileName != null) {
                    profileName = profileName.trim();
                    //Log.e("ActionForExternalApplicationActivity.onCreate", "profileName="+profileName);

                    if (!profileName.isEmpty()) {
                        //dataWrapper.fillProfileList(false, false);
                        profile_id = dataWrapper.getProfileIdByName(profileName, true);
                        /*for (Profile profile : this.dataWrapper.profileList) {
                            if (profile._name.trim().equals(profileName)) {
                                profile_id = profile._id;
                                break;
                            }
                        }*/
                        //Log.e("ActionForExternalApplicationActivity.onCreate", "profile_id="+profile_id);
                    }
                }
            } else if (!action.equals(ACTION_RESTART_EVENTS)) {
                eventName = intent.getStringExtra(ActionForExternalApplicationActivity.EXTRA_EVENT_NAME);
                if (eventName != null) {
                    eventName = eventName.trim();
                    //Log.e("ActionForExternalApplicationActivity.onCreate", "eventName=" + eventName);

                    if (!eventName.isEmpty()) {
                        event_id = dataWrapper.getEventIdByName(eventName, true);
                        /*dataWrapper.fillEventList();
                        for (Event event : dataWrapper.eventList) {
                            if (event._name.trim().equals(eventName)) {
                                event_id = event._id;
                                break;
                            }
                        }*/
                        //Log.e("ActionForExternalApplicationActivity.onCreate", "event_id=" + event_id);
                    }
                }
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (action != null) {
            if (!PPApplication.getApplicationStarted(true)) {
                //Log.e("ActionForExternalApplicationActivity.onStart", "application not started");
                PPApplication.setApplicationStarted(getApplicationContext(), true);
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                boolean extraDataOk;
                if (action.equals(ACTION_ACTIVATE_PROFILE)) {
                    extraDataOk = profile_id != 0;
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE,
                            PhoneProfilesService.START_FOR_EXTERNAL_APP_PROFILE);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, profileName);
                }
                else
                if (!action.equals(ACTION_RESTART_EVENTS)) {
                    extraDataOk = event_id != 0;
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE,
                            PhoneProfilesService.START_FOR_EXTERNAL_APP_EVENT);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, eventName);
                }
                else
                    extraDataOk = true;
                if (extraDataOk) {
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APPLICATION, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_ACTION, action);
                }
                PPApplication.logE("[START_PP_SERVICE] ActionForExternalApplicationActivity.onStart", "xxx");
                PPApplication.startPPService(this, serviceIntent/*, true*/);
                finish();
                return;
            }

            //Log.e("ActionForExternalApplicationActivity.onStart", "action="+action);

            switch (action) {
                case ACTION_ACTIVATE_PROFILE:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PROFILE_ACTIVATION,
                            null, profileName, null, 0, "");

                    if (profile_id != 0) {
                        Profile profile = dataWrapper.getProfileById(profile_id, false, false, false);
                        if (profile != null) {
                            //Log.e("ActionForExternalApplicationActivity.onStart", "profile=" + profile._name);
                            //if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                            //        /*false, false, 0,*/ PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, true, false)) {
                            if (!PhoneProfilesService.displayPreferencesErrorNotification(profile, null, getApplicationContext())) {
                                //PPApplication.logE("&&&&&&& ActionForExternalApplicationActivity.onStart", "called is DataWrapper.activateProfileFromMainThread");
                                dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this, false);
                            } else
                                dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                        }
                        else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_profile_text));

                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    }
                    break;
                case ACTION_RESTART_EVENTS:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_RESTART_EVENTS,
                            null, null, null, 0, "");

                    //Log.e("ActionForExternalApplicationActivity.onStart", "restart events");
//                    PPApplication.logE("[APP START] ActionForExternalApplicationActivity", "(1)");
                    dataWrapper.restartEventsWithRescan(true, true, true, false, true, true);
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                case ACTION_ENABLE_RUN_FOR_EVENT:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_ENABLE_RUN_FOR_EVENT,
                            eventName, null, null, 0, "");

                    if (event_id != 0) {
                        final Event event = dataWrapper.getEventById(event_id);
                        if (event != null) {
                            //Log.e("ActionForExternalApplicationActivity.onStart", "event=" + event._name);
                            if (event.getStatus() != Event.ESTATUS_RUNNING) {
                                final DataWrapper _dataWrapper = dataWrapper;
                                PPApplication.startHandlerThread(/*"ActionForExternalApplicationActivity.onStart.1"*/);
                                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                handler.post(() -> {
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActionForExternalApplicationActivity.onStart.1");

                                    PowerManager powerManager = (PowerManager) _dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActionForExternalApplicationActivity_ACTION_ENABLE_RUN_FOR_EVENT");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        synchronized (PPApplication.eventsHandlerMutex) {
                                            event.pauseEvent(_dataWrapper, true, false,
                                                    false, true, null, false, false, true);
                                        }
                                        //_dataWrapper.restartEvents(false, true, true, true, false);
//                                            PPApplication.logE("[APP START] ActionForExternalApplicationActivity", "(2)");
                                        _dataWrapper.restartEventsWithRescan(true, false, false, false, true, true);

                                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=ActionForExternalApplicationActivity.onStart.1");
                                    } catch (Exception e) {
//                                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                        PPApplication.recordException(e);
                                    } finally {
                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                            try {
                                                wakeLock.release();
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                });
                            }
                        }
                        else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                case ACTION_PAUSE_EVENT:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PAUSE_EVENT,
                            eventName, null, null, 0, "");

                    if (event_id != 0) {
                        final Event event = dataWrapper.getEventById(event_id);
                        if (event != null) {
                            //Log.e("ActionForExternalApplicationActivity.onStart", "event=" + event._name);
                            if (event.getStatus() == Event.ESTATUS_RUNNING) {
                                final DataWrapper _dataWrapper = dataWrapper;
                                PPApplication.startHandlerThread(/*"ActionForExternalApplicationActivity.onStart.11"*/);
                                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                handler.post(() -> {
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActionForExternalApplicationActivity.onStart.11");

                                    PowerManager powerManager = (PowerManager) _dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActionForExternalApplicationActivity_ACTION_PAUSE_EVENT");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        synchronized (PPApplication.eventsHandlerMutex) {
                                            event.pauseEvent(_dataWrapper, true, false,
                                                    false, true, null, true, false, true);
                                        }

                                    } catch (Exception e) {
//                                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                        PPApplication.recordException(e);
                                    } finally {
                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                            try {
                                                wakeLock.release();
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                });
                            }
                        }
                        else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                case ACTION_STOP_EVENT:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_STOP_EVENT,
                            eventName, null, null, 0, "");

                    if (event_id != 0) {
                        final Event event = dataWrapper.getEventById(event_id);
                        if (event != null) {
                            //Log.e("ActionForExternalApplicationActivity.onStart", "event=" + event._name);
                            if (event.getStatus() != Event.ESTATUS_STOP) {
                                final DataWrapper _dataWrapper = dataWrapper;
                                PPApplication.startHandlerThread(/*"ActionForExternalApplicationActivity.onStart.2"*/);
                                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                handler.post(() -> {
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActionForExternalApplicationActivity.onStart.2");

                                    PowerManager powerManager = (PowerManager) _dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActionForExternalApplicationActivity_ACTION_STOP_EVENT");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        synchronized (PPApplication.eventsHandlerMutex) {
                                            event.stopEvent(_dataWrapper, true, false,
                                                    true, true, true); // activate return profile
                                        }
                                        //_dataWrapper.restartEvents(false, true, true, true, false);
                                        //PPApplication.logE("*********** restartEvents", "from ActionForExternalApplicationActivity.onStart() - ACTION_STOP_EVENT");
//                                            PPApplication.logE("[APP START] ActionForExternalApplicationActivity", "(3)");
                                        _dataWrapper.restartEventsWithRescan(true, false, false, false, true, true);

                                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=ActionForExternalApplicationActivity.onStart.2");
                                    } catch (Exception e) {
//                                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                        PPApplication.recordException(e);
                                    } finally {
                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                            try {
                                                wakeLock.release();
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                });
                            }
                        }
                        else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                default:
                    showNotification(getString(R.string.action_for_external_application_notification_title),
                            getString(R.string.action_for_external_application_notification_bad_action));
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
            }
        }
        else {
            showNotification(getString(R.string.action_for_external_application_notification_title),
                    getString(R.string.action_for_external_application_notification_no_action));
            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean mergedProfile = data.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile) {
                    Profile profile = dataWrapper.getProfileById(profileId, false, false, mergedProfile);
                    dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, this);
                }
            }
        }
    }
    */

    private void showNotification(String title, String text) {
        String nTitle = title;
        String nText = text;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            nTitle = getString(R.string.ppp_app_name);
            nText = title+": "+text;
        }
        PPApplication.createExclamationNotificationChannel(getApplicationContext());
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext(), PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(this, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        /*Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);*/
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        try {
            mNotificationManager.notify(
                    PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_TAG,
                    PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("ActionForExternalApplicationActivity.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
