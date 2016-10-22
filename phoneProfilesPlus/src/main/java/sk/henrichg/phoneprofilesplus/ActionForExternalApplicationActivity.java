package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class ActionForExternalApplicationActivity extends Activity {

    private DataWrapper dataWrapper;

    private String action;

    private long profile_id = 0;
    private long event_id = 0;

    private final String ACTION_ACTIVATE_PROFILE = "sk.henrichg.phoneprofilesplus.ACTION_ACTIVATE_PROFILE";
    private final String ACTION_RESTART_EVENTS = "sk.henrichg.phoneprofilesplus.ACTION_RESTART_EVENTS";
    private final String ACTION_ENABLE_RUN_FOR_EVENT = "sk.henrichg.phoneprofilesplus.ACTION_ENABLE_RUN_FOR_EVENT";
    private final String ACTION_PAUSE_EVENT = "sk.henrichg.phoneprofilesplus.ACTION_PAUSE_EVENT";
    private final String ACTION_STOP_EVENT = "sk.henrichg.phoneprofilesplus.ACTION_STOP_EVENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("ActionForExternalApplicationActivity.onCreate", "xxx");

        GlobalData.loadPreferences(getApplicationContext());

        Intent intent = getIntent();

        action = intent.getAction();
        Log.d("ActionForExternalApplicationActivity.onCreate", "action="+action);

        dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

        if (action.equals(ACTION_ACTIVATE_PROFILE)) {
            String profileName = intent.getStringExtra(GlobalData.EXTRA_PROFILE_NAME);
            profileName = profileName.trim();
            Log.d("ActionForExternalApplicationActivity.onCreate", "profileName="+profileName);

            if (!profileName.isEmpty()) {
                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getApplicationContext());

                List<Profile> profileList = dataWrapper.getProfileList();
                for (Profile profile : profileList) {
                    if (profile._name.trim().equals(profileName)) {
                        profile_id = profile._id;
                        break;
                    }
                }
                Log.d("ActionForExternalApplicationActivity.onCreate", "profile_id="+profile_id);
            }
        }
        else if (action.equals(ACTION_RESTART_EVENTS)) {

        }
        else {
            String eventName = intent.getStringExtra(GlobalData.EXTRA_EVENT_NAME);
            eventName = eventName.trim();
            Log.d("ActionForExternalApplicationActivity.onCreate", "eventName=" + eventName);

            if (!eventName.isEmpty()) {
                List<Event> eventList = dataWrapper.getEventList();
                for (Event event : eventList) {
                    if (event._name.trim().equals(eventName)) {
                        event_id = event._id;
                        break;
                    }
                }
                Log.d("ActionForExternalApplicationActivity.onCreate", "event_id=" + event_id);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!GlobalData.getApplicationStarted(getApplicationContext()) || (PhoneProfilesService.instance == null))
            startService(new Intent(getApplicationContext(), PhoneProfilesService.class));

        if (action.equals(ACTION_ACTIVATE_PROFILE)) {
            if (profile_id != 0) {
                Profile profile = dataWrapper.getProfileById(profile_id, false);
                Log.d("ActionForExternalApplicationActivity.onCreate", "profile="+profile);
                if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                        true, false, 0, GlobalData.STARTUP_SOURCE_EXTERNAL_APP, true, this, true, true)) {
                    dataWrapper._activateProfile(profile, false, GlobalData.STARTUP_SOURCE_EXTERNAL_APP, true, this, true);
                }
            }
            else
                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
        else if (action.equals(ACTION_RESTART_EVENTS)) {
            dataWrapper.restartEventsWithRescan(true, true);
            dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
        else if (action.equals(ACTION_ENABLE_RUN_FOR_EVENT)) {
            if (event_id != 0) {
                List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
                Event event = dataWrapper.getEventById(event_id);
                if (event.getStatus() != Event.ESTATUS_RUNNING) {
                    event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, true, null, false); // activate return profile
                    dataWrapper.restartEvents(false, true, true);
                }
                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
            } else
                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
        else if (action.equals(ACTION_PAUSE_EVENT)) {
            if (event_id != 0) {
                List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
                Event event = dataWrapper.getEventById(event_id);
                if (event.getStatus() == Event.ESTATUS_RUNNING) {
                    event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, true, null, false); // activate return profile
                    //dataWrapper.restartEvents(false, true, true);
                }
                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
            } else
                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
        else if (action.equals(ACTION_STOP_EVENT)) {
            if (event_id != 0) {
                List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
                Event event = dataWrapper.getEventById(event_id);
                if (event.getStatus() != Event.ESTATUS_STOP) {
                    event.stopEvent(dataWrapper, eventTimelineList, true, false, true, true, false); // activate return profile
                    dataWrapper.restartEvents(false, true, true);
                }
                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
            } else
                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
        else {
            if (event_id != 0) {
                Event event = dataWrapper.getEventById(event_id);
                Log.d("ActionForExternalApplicationActivity.onCreate", "event=" + event);

                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
            } else
                dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

}
