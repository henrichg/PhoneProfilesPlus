package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.List;

public class ActionForExternalApplicationActivity extends Activity {

    private DataWrapper dataWrapper;

    private String action;

    private long profile_id = 0;
    private long event_id = 0;

    private static final String ACTION_ACTIVATE_PROFILE = "sk.henrichg.phoneprofilesplus.ACTION_ACTIVATE_PROFILE";
    static final String ACTION_RESTART_EVENTS = "sk.henrichg.phoneprofilesplus.ACTION_RESTART_EVENTS";
    private static final String ACTION_ENABLE_RUN_FOR_EVENT = "sk.henrichg.phoneprofilesplus.ACTION_ENABLE_RUN_FOR_EVENT";
    private static final String ACTION_PAUSE_EVENT = "sk.henrichg.phoneprofilesplus.ACTION_PAUSE_EVENT";
    private static final String ACTION_STOP_EVENT = "sk.henrichg.phoneprofilesplus.ACTION_STOP_EVENT";

    private static final String EXTRA_EVENT_NAME = "event_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log.d("ActionForExternalApplicationActivity.onCreate", "xxx");

        Intent intent = getIntent();

        action = intent.getAction();
        //Log.d("ActionForExternalApplicationActivity.onCreate", "action="+action);

        dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

        if (action != null) {
            if (action.equals(ACTION_ACTIVATE_PROFILE)) {
                String profileName = intent.getStringExtra(ActivateProfileFromExternalApplicationActivity.EXTRA_PROFILE_NAME);
                if (profileName != null) {
                    profileName = profileName.trim();
                    //Log.d("ActionForExternalApplicationActivity.onCreate", "profileName="+profileName);

                    if (!profileName.isEmpty()) {
                        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());

                        List<Profile> profileList = dataWrapper.getProfileList();
                        for (Profile profile : profileList) {
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
                        List<Event> eventList = dataWrapper.getEventList();
                        for (Event event : eventList) {
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
            if (!PPApplication.getApplicationStarted(getApplicationContext(), true)) {
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
                //TODO Android O
                // if (Build.VERSION.SDK_INT < 26)
                    startService(serviceIntent);
                //else
                //    startForegroundService(serviceIntent);
            }

            if (action.equals(ACTION_ACTIVATE_PROFILE)) {
                if (profile_id != 0) {
                    Profile profile = dataWrapper.getProfileById(profile_id, false);
                    //Log.d("ActionForExternalApplicationActivity.onCreate", "profile="+profile);
                    if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                            true, false, 0, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, true, this, true)) {
                        dataWrapper._activateProfile(profile, false, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, true, this);
                    }
                } else {
                    showNotification(getString(R.string.action_for_external_application_notification_title),
                            getString(R.string.action_for_external_application_notification_no_profile_text));

                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                }
            } else if (action.equals(ACTION_RESTART_EVENTS)) {
                dataWrapper.restartEventsWithRescan(true, true);
                dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
            } else if (action.equals(ACTION_ENABLE_RUN_FOR_EVENT)) {
                if (event_id != 0) {
                    List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
                    Event event = dataWrapper.getEventById(event_id);
                    if (event.getStatus() != Event.ESTATUS_RUNNING) {
                        event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, true, null, false); // activate return profile
                        dataWrapper.restartEvents(false, true, true);
                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                } else {
                    showNotification(getString(R.string.action_for_external_application_notification_title),
                            getString(R.string.action_for_external_application_notification_no_event_text));

                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                }
            } else if (action.equals(ACTION_PAUSE_EVENT)) {
                if (event_id != 0) {
                    List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
                    Event event = dataWrapper.getEventById(event_id);
                    if (event.getStatus() == Event.ESTATUS_RUNNING) {
                        event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, true, null, false); // activate return profile
                        //dataWrapper.restartEvents(false, true, true);
                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                } else {
                    showNotification(getString(R.string.action_for_external_application_notification_title),
                            getString(R.string.action_for_external_application_notification_no_event_text));

                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                }
            } else if (action.equals(ACTION_STOP_EVENT)) {
                if (event_id != 0) {
                    List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
                    Event event = dataWrapper.getEventById(event_id);
                    if (event.getStatus() != Event.ESTATUS_STOP) {
                        event.stopEvent(dataWrapper, eventTimelineList, true, false, true, true, false); // activate return profile
                        dataWrapper.restartEvents(false, true, true);
                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                } else {
                    showNotification(getString(R.string.action_for_external_application_notification_title),
                            getString(R.string.action_for_external_application_notification_no_event_text));

                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                }
            } else {
                showNotification(getString(R.string.action_for_external_application_notification_title),
                        getString(R.string.action_for_external_application_notification_bad_action));
                dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
            }
        }
        else {
            showNotification(getString(R.string.action_for_external_application_notification_title),
                    getString(R.string.action_for_external_application_notification_no_action));
            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    private void showNotification(String title, String text) {
        String nTitle = title;
        String nText = text;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            nTitle = getString(R.string.app_name);
            nText = title+": "+text;
        }
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        /*Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);*/
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager mNotificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID, mBuilder.build());
    }

}
