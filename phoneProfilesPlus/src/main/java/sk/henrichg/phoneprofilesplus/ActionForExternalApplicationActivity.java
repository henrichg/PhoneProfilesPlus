package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class ActionForExternalApplicationActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;

    private String action;

    private long profile_id = 0;
    private long event_id = 0;

    // !!! do change this actions, these are for Tasker !!!!
    private static final String ACTION_ACTIVATE_PROFILE = PPApplication.PACKAGE_NAME + ".ACTION_ACTIVATE_PROFILE";
    private static final String ACTION_RESTART_EVENTS = PPApplication.PACKAGE_NAME + ".ACTION_RESTART_EVENTS";
    private static final String ACTION_ENABLE_RUN_FOR_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_ENABLE_RUN_FOR_EVENT";
    private static final String ACTION_PAUSE_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_PAUSE_EVENT";
    private static final String ACTION_STOP_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_STOP_EVENT";

    private static final String EXTRA_EVENT_NAME = "event_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        //Log.d("ActionForExternalApplicationActivity.onCreate", "xxx");

        Intent intent = getIntent();

        action = intent.getAction();
        //Log.d("ActionForExternalApplicationActivity.onCreate", "action="+action);

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        if (action != null) {
            if (action.equals(ACTION_ACTIVATE_PROFILE)) {
                String profileName = intent.getStringExtra(ActivateProfileFromExternalApplicationActivity.EXTRA_PROFILE_NAME);
                if (profileName != null) {
                    profileName = profileName.trim();
                    //Log.d("ActionForExternalApplicationActivity.onCreate", "profileName="+profileName);

                    if (!profileName.isEmpty()) {
                        dataWrapper.fillProfileList(false, false);
                        for (Profile profile : this.dataWrapper.profileList) {
                            if (profile._name.trim().equals(profileName)) {
                                profile_id = profile._id;
                                break;
                            }
                        }
                        //Log.d("ActionForExternalApplicationActivity.onCreate", "profile_id="+profile_id);
                    }
                }
            } else if (!action.equals(ACTION_RESTART_EVENTS)) {
                String eventName = intent.getStringExtra(EXTRA_EVENT_NAME);
                if (eventName != null) {
                    eventName = eventName.trim();
                    //Log.d("ActionForExternalApplicationActivity.onCreate", "eventName=" + eventName);

                    if (!eventName.isEmpty()) {
                        dataWrapper.fillEventList();
                        for (Event event : dataWrapper.eventList) {
                            if (event._name.trim().equals(eventName)) {
                                event_id = event._id;
                                break;
                            }
                        }
                        //Log.d("ActionForExternalApplicationActivity.onCreate", "event_id=" + event_id);
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
                PPApplication.setApplicationStarted(getApplicationContext(), true);
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                PPApplication.startPPService(this, serviceIntent);
                finish();
                return;
            }

            switch (action) {
                case ACTION_ACTIVATE_PROFILE:
                    if (profile_id != 0) {
                        Profile profile = dataWrapper.getProfileById(profile_id, false, false, false);
                        //Log.d("ActionForExternalApplicationActivity.onCreate", "profile="+profile);
                        //if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                        //        /*false, false, 0,*/ PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, true, false)) {
                        if (EditorProfilesActivity.displayRedTextToPreferencesNotification(profile, null, getApplicationContext())) {
                            dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                        } else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_profile_text));

                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    }
                    break;
                case ACTION_RESTART_EVENTS:
                    PPApplication.logE("*********** restartEvents", "from ActionForExternalApplicationActivity.onStart() - ACTION_RESTART_EVENTS");
                    dataWrapper.restartEventsWithRescan(/*true, */true, true, false, true, true);
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                case ACTION_ENABLE_RUN_FOR_EVENT:
                    if (event_id != 0) {
                        final Event event = dataWrapper.getEventById(event_id);
                        if (event.getStatus() != Event.ESTATUS_RUNNING) {
                            final DataWrapper _dataWrapper = dataWrapper;
                            PPApplication.startHandlerThread("ActionForExternalApplicationActivity.onStart.1");
                            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) _dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActionForExternalApplicationActivity_ACTION_ENABLE_RUN_FOR_EVENT");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=ActionForExternalApplicationActivity.onStart.1");

                                        List<EventTimeline> eventTimelineList = _dataWrapper.getEventTimelineList(false);
                                        event.pauseEvent(_dataWrapper, eventTimelineList, true, false,
                                                false, true, null, false, false); // activate return profile
                                        //_dataWrapper.restartEvents(false, true, true, true, false);
                                        PPApplication.logE("*********** restartEvents", "from ActionForExternalApplicationActivity.onStart() - ACTION_ENABLE_RUN_FOR_EVENT");
                                        _dataWrapper.restartEventsWithRescan(/*true, */false, false, false, true, true);

                                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=ActionForExternalApplicationActivity.onStart.1");
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
                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    }
                    break;
                case ACTION_PAUSE_EVENT:
                    if (event_id != 0) {
                        List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);
                        Event event = dataWrapper.getEventById(event_id);
                        if (event.getStatus() == Event.ESTATUS_RUNNING) {
                            event.pauseEvent(dataWrapper, eventTimelineList, true, false,
                                    false, true, null, true, false); // activate return profile
                        }
                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    }
                    break;
                case ACTION_STOP_EVENT:
                    if (event_id != 0) {
                        final Event event = dataWrapper.getEventById(event_id);
                        if (event.getStatus() != Event.ESTATUS_STOP) {
                            final DataWrapper _dataWrapper = dataWrapper;
                            PPApplication.startHandlerThread("ActionForExternalApplicationActivity.onStart.2");
                            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) _dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActionForExternalApplicationActivity_ACTION_STOP_EVENT");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=ActionForExternalApplicationActivity.onStart.2");

                                        final List<EventTimeline> eventTimelineList = _dataWrapper.getEventTimelineList(false);
                                        event.stopEvent(_dataWrapper, eventTimelineList, true, false,
                                                true/*, true*/); // activate return profile
                                        //_dataWrapper.restartEvents(false, true, true, true, false);
                                        PPApplication.logE("*********** restartEvents", "from ActionForExternalApplicationActivity.onStart() - ACTION_STOP_EVENT");
                                        _dataWrapper.restartEventsWithRescan(/*true, */false, false, false, true, true);

                                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=ActionForExternalApplicationActivity.onStart.2");
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
                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    }
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
            nTitle = getString(R.string.app_name);
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
        NotificationManager mNotificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
