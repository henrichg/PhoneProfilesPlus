package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class DataWrapper {

    public Context context = null;
    private boolean forGUI = false;
    private boolean monochrome = false;
    private int monochromeValue = 0xFF;

    private DatabaseHandler databaseHandler = null;
    private ActivateProfileHelper activateProfileHelper = null;
    private List<Profile> profileList = null;
    private List<Event> eventList = null;

    static final String EXTRA_UNBLOCKEVENTSRUN = "unblock_events_run";
    static final String EXTRA_INTERACTIVE = "interactive";

    DataWrapper(Context c,
                        boolean fgui,
                        boolean mono,
                        int monoVal)
    {
        context = c;

        setParameters(fgui, mono, monoVal);

        databaseHandler = getDatabaseHandler();
    }

    void setParameters(
            boolean fgui,
            boolean mono,
            int monoVal)
    {
        forGUI = fgui;
        monochrome = mono;
        monochromeValue = monoVal;
    }

    public DatabaseHandler getDatabaseHandler()
    {
        if (databaseHandler == null)
            // parameter must by application context
            databaseHandler = DatabaseHandler.getInstance(context);

        return databaseHandler;
    }

    public ActivateProfileHelper getActivateProfileHelper()
    {
        if (activateProfileHelper == null)
            activateProfileHelper = new ActivateProfileHelper();

        return activateProfileHelper;
    }

    public List<Profile> getProfileList()
    {
        if (profileList == null)
        {
            profileList = getNewProfileList();
        }

        return profileList;
    }

    List<Profile> getNewProfileList() {
        List<Profile> newProfileList = getDatabaseHandler().getAllProfiles();

        if (forGUI)
        {
            for (Iterator<Profile> it = newProfileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                profile.generateIconBitmap(context, monochrome, monochromeValue);
                //if (generateIndicators)
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        }
        return newProfileList;
    }

    void setProfileList(List<Profile> profileList, boolean recycleBitmaps)
    {
        if (recycleBitmaps)
            invalidateProfileList();
        else
            if (this.profileList != null)
                this.profileList.clear();
        this.profileList = profileList;
    }

    static Profile getNoinitializedProfile(String name, String icon, int order)
    {
        return new Profile(
                  name,
                  icon + "|1|0|0",
                  false,
                  order,
                  0,
                  "-1|1|0",
                  "-1|1|0",
                  "-1|1|0",
                  "-1|1|0",
                  "-1|1|0",
                  "-1|1|0",
                  0,
                  Settings.System.DEFAULT_RINGTONE_URI.toString(),
                  0,
                  Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                  0,
                  Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
                  0,
                  0,
                  0,
                  0,
                  Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|1|1|0",
                  0,
                  "-",
                  0,
                  0,
                  0,
                  0,
                  "-",
                  0,
                  false,
                  0,
                  0,
                  0,
                  0,
                  0,
                  Profile.AFTERDURATIONDO_RESTARTEVENTS,
                  0,
                  0,
                  0,
                  0,
                  0,
                  false,
                  0,
                  0,
                  0,
                  0,
                  false,
                  0,
                  Profile.CONNECTTOSSID_JUSTANY,
                  0,
                  0
            );
    }

    private String getVolumeLevelString(int percentage, int maxValue)
    {
        Double dValue = maxValue / 100.0 * percentage;
        return String.valueOf(dValue.intValue());
    }

    Profile getPredefinedProfile(int index, boolean saveToDB) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int	maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int	maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int	maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int	maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        //int	maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        //int	maximumValueVoicecall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

        Profile profile;

        switch (index) {
            case 0:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_home), "ic_profile_home_2", 1);
                profile._showInActivator = true;
                if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else
                            profile._volumeRingerMode = 1;
                    } else
                        profile._volumeRingerMode = 1;
                } else
                    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 1;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 1:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_outdoor), "ic_profile_outdoors_1", 2);
                profile._showInActivator = true;
                if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else
                            profile._volumeRingerMode = 2;
                    } else
                        profile._volumeRingerMode = 2;
                } else
                    profile._volumeRingerMode = 2;
                profile._volumeRingtone = getVolumeLevelString(100, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(100, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(93, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "255|0|0|0";
                break;
            case 2:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_work), "ic_profile_work_5", 3);
                profile._showInActivator = true;
                if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else
                            profile._volumeRingerMode = 1;
                    } else
                        profile._volumeRingerMode = 1;
                } else
                    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 3:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_meeting), "ic_profile_meeting_2", 4);
                profile._showInActivator = true;
                if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else
                            profile._volumeRingerMode = 4;
                    } else
                        profile._volumeRingerMode = 4;
                } else
                    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|1|1|0";
                break;
            case 4:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_sleep), "ic_profile_sleep", 5);
                profile._showInActivator = true;
                if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 6; // ALARMS
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else
                            profile._volumeRingerMode = 4;
                    } else
                        profile._volumeRingerMode = 4;
                } else
                    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = "10|0|0|0";
                break;
            case 5:
                profile = getNoinitializedProfile(context.getString(R.string.default_profile_name_battery_low), "ic_profile_battery_1", 6);
                profile._showInActivator = false;
                profile._deviceAutosync = 2;
                profile._deviceMobileData = 2;
                profile._deviceWiFi = 2;
                profile._deviceBluetooth = 2;
                profile._deviceGPS = 2;
                break;
            default:
                profile = null;
        }

        if (profile != null) {
            if (saveToDB)
                getDatabaseHandler().addProfile(profile, false);
        }

        return profile;
    }

    List<Profile>  getPredefinedProfileList()
    {
        invalidateProfileList();
        getDatabaseHandler().deleteAllProfiles();

        for (int index = 0; index < 6; index++)
            getPredefinedProfile(index, true);

        return getProfileList();
    }

    void invalidateProfileList()
    {
        if (profileList != null)
        {
            for(Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                profile.releaseIconBitmap();
                profile.releasePreferencesIndicator();
                it.remove();
            }
        }
        profileList = null;
    }

    Profile getActivatedProfileFromDB()
    {
        Profile profile = getDatabaseHandler().getActivatedProfile();
        if (forGUI && (profile != null))
        {
            profile.generateIconBitmap(context, monochrome, monochromeValue);
            profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        }
        return profile;
    }

    public Profile getActivatedProfile()
    {
        return getActivatedProfile(profileList);
    }

    public Profile getActivatedProfile(List<Profile> profileList) {
        if (profileList == null)
        {
            return getActivatedProfileFromDB();
        }
        else
        {
            Profile profile;
            for (int i = 0; i < profileList.size(); i++)
            {
                profile = profileList.get(i);
                if (profile._checked)
                    return profile;
            }
            // when filter is set and profile not found, get profile from db
            return getActivatedProfileFromDB();
        }
    }
/*	
    public Profile getFirstProfile()
    {
        if (profileList == null)
        {
            Profile profile = getDatabaseHandler().getFirstProfile();
            if (forGUI && (profile != null))
            {
                profile.generateIconBitmap(context, monochrome, monochromeValue);
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
            return profile;
        }
        else
        {
            Profile profile;
            if (profileList.size() > 0)
                profile = profileList.get(0);
            else
                profile = null;

            return profile;
        }
    }
*/	
/*	
    public int getProfileItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (profileList == null)
            return getDatabaseHandler().getProfilePosition(profile);
        else
        {
            for (int i = 0; i < profileList.size(); i++)
            {
                if (profileList.get(i)._id == profile._id)
                    return i;
            }
            return -1;
        }
    }
*/	
    void setProfileActive(Profile profile)
    {
        if (profileList == null)
            return;

        for (Profile p : profileList)
        {
            p._checked = false;
        }

        if (profile != null)
            profile._checked = true;
    }

    void activateProfileFromEvent(long profile_id, boolean interactive, boolean manual,
                                         boolean merged)
    {
        int startupSource = PPApplication.STARTUP_SOURCE_SERVICE;
        if (manual)
            startupSource = PPApplication.STARTUP_SOURCE_SERVICE_MANUAL;
        Profile profile = getProfileById(profile_id, merged);
        if (Permissions.grantProfilePermissions(context, profile, merged, true,
                forGUI, monochrome, monochromeValue,
                startupSource, interactive, null, true)) {
            getActivateProfileHelper().initialize(this, context);
            _activateProfile(profile, merged, startupSource, interactive, null);
        }
    }

    void updateNotificationAndWidgets(Profile profile)
    {
        getActivateProfileHelper().initialize(this, context);
        getActivateProfileHelper().showNotification(profile);
        getActivateProfileHelper().updateWidget();
    }

    /*
    public void deactivateProfile()
    {
        if (profileList == null)
            return;

        for (Profile p : profileList)
        {
            p._checked = false;
        }
    }
    */

    private Profile getProfileByIdFromDB(long id, boolean merged)
    {
        Profile profile = getDatabaseHandler().getProfile(id, merged);
        if (forGUI && (profile != null))
        {
            profile.generateIconBitmap(context, monochrome, monochromeValue);
            profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        }
        return profile;
    }

    public Profile getProfileById(long id, boolean merged)
    {
        if ((profileList == null) || merged)
        {
            return getProfileByIdFromDB(id, merged);
        }
        else
        {
            Profile profile;
            for (int i = 0; i < profileList.size(); i++)
            {
                profile = profileList.get(i);
                if (profile._id == id)
                    return profile;
            }

            // when filter is set and profile not found, get profile from db
            return getProfileByIdFromDB(id, false);
        }
    }

    void updateProfile(Profile profile)
    {
        if (profile != null)
        {
            Profile origProfile = getProfileById(profile._id, false);
            if (origProfile != null)
                origProfile.copyProfile(profile);
        }
    }

    /*
    public void reloadProfilesData()
    {
        invalidateProfileList();
        getProfileList();
    }
    */

    void deleteProfile(Profile profile)
    {
        if (profile == null)
            return;

        profileList.remove(profile);
        if (eventList == null)
            eventList = getEventList();
        // unlink profile from events
        for (Event event : eventList)
        {
            if (event._fkProfileStart == profile._id)
                event._fkProfileStart = 0;
            if (event._fkProfileEnd == profile._id)
                event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
        }
        // unlink profile from Background profile
        if (Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context)) == profile._id)
        {
            ApplicationPreferences.getSharedPreferences(context);
            Editor editor = ApplicationPreferences.preferences.edit();
            editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
            editor.apply();
        }
    }

    void deleteAllProfiles()
    {
        profileList.clear();
        if (eventList == null)
            eventList = getEventList();
        // unlink profiles from events
        for (Event event : eventList)
        {
            event._fkProfileStart = 0;
            event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
        }
        // unlink profiles from Background profile
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
        editor.apply();
    }

    void refreshProfileIcon(Profile profile, boolean monochrome, int monochromeValue) {
        if (profile != null) {
            boolean isIconResourceID = profile.getIsIconResourceID();
            String iconIdentifier = profile.getIconIdentifier();
            getDatabaseHandler().getProfileIcon(profile);
            if (isIconResourceID && iconIdentifier.equals("ic_profile_default") && (!profile.getIsIconResourceID())) {
                profile.generateIconBitmap(context, monochrome, monochromeValue);
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        }
    }

//---------------------------------------------------

    List<Event> getEventList()
    {
        if (eventList == null)
        {
            eventList = getDatabaseHandler().getAllEvents();
        }

        return eventList;
    }

    void setEventList(List<Event> eventList)
    {
        if (this.eventList != null)
            this.eventList.clear();
        this.eventList = eventList;
    }

    void invalidateEventList()
    {
        if (eventList != null)
            eventList.clear();
        eventList = null;
    }

/*	
    Event getFirstEvent(int filterType)
    {
        if (eventList == null)
        {
            Event event = getDatabaseHandler().getFirstEvent();
            return event;
        }
        else
        {
            Event event;
            if (eventList.size() > 0)
                event = eventList.get(0);
            else
                event = null;

            return event;
        }
    }
*/	
/*
    int getEventItemPosition(Event event)
    {
        if (event == null)
            return - 1;

        if (eventList == null)
            return getDatabaseHandler().getEventPosition(event);
        else
        {
            for (int i = 0; i < eventList.size(); i++)
            {
                if (eventList.get(i)._id == event._id)
                    return i;
            }
            return -1;
        }
    }
*/	
    void sortEventsByStartOrderAsc()
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  lhs._startOrder - rhs._startOrder;
                return res;
            }
        }

        getEventList();
        if (eventList != null)
        {
            Collections.sort(eventList, new PriorityComparator());
        }
    }

    void sortEventsByStartOrderDesc()
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  rhs._startOrder - lhs._startOrder;
                return res;
            }
        }

        getEventList();
        if (eventList != null)
        {
            Collections.sort(eventList, new PriorityComparator());
        }
    }

    Event getEventById(long id)
    {
        if (eventList == null)
        {
            return getDatabaseHandler().getEvent(id);
        }
        else
        {
            Event event;
            for (int i = 0; i < eventList.size(); i++)
            {
                event = eventList.get(i);
                if (event._id == id)
                    return event;
            }

            // when filter is set and profile not found, get profile from db
            return getDatabaseHandler().getEvent(id);
        }
    }

    void updateEvent(Event event)
    {
        if (event != null)
        {
            Event origEvent = getEventById(event._id);
            origEvent.copyEvent(event);
        }
    }

    /*
    public void reloadEventsData()
    {
        invalidateEventList();
        getEventList();
    }
    */

    // stops all events associated with profile
    void stopEventsForProfile(Profile profile, boolean saveEventStatus)
    {
        List<EventTimeline> eventTimelineList = getEventTimelineList();

        for (Event event : getEventList())
        {
            //if ((event.getStatusFromDB(this) == Event.ESTATUS_RUNNING) &&
            //	(event._fkProfileStart == profile._id))
            if (event._fkProfileStart == profile._id)
                event.stopEvent(this, eventTimelineList, false, true, saveEventStatus, false, false);
        }
        PPApplication.logE("$$$ restartEvents", "from DataWrapper.stopEventsForProfile");
        restartEvents(false, true, false);
    }

    // pauses all events
    private void pauseAllEvents(boolean noSetSystemEvent, boolean blockEvents/*, boolean activateReturnProfile*/)
    {
        List<EventTimeline> eventTimelineList = getEventTimelineList();

        for (Event event : getEventList())
        {
            if (event != null)
            {
                int status = event.getStatusFromDB(this);

                if (status == Event.ESTATUS_RUNNING)
                    event.pauseEvent(this, eventTimelineList, false, true, noSetSystemEvent, true, null, false);

                setEventBlocked(event, false);
                if (blockEvents && (status == Event.ESTATUS_RUNNING) && event._forceRun)
                {
                    // block only running forcerun events
                    setEventBlocked(event, true);
                }

                // for "push" events, set startTime to 0
                event._eventPreferencesSMS._startTime = 0;
                getDatabaseHandler().updateSMSStartTime(event);
                event._eventPreferencesNotification._startTime = 0;
                getDatabaseHandler().updateNotificationStartTime(event);
                event._eventPreferencesNFC._startTime = 0;
                getDatabaseHandler().updateNFCStartTime(event);
            }
        }

        // blockEvents == true -> manual profile activation is set
        PPApplication.logE("$$$ setEventsBlocked", "DataWrapper.pauseAllEvents, " + blockEvents);
        Event.setEventsBlocked(context, blockEvents);
    }

    // stops all events
    void stopAllEvents(boolean saveEventStatus, boolean activateRetirnProfile)
    {
        List<EventTimeline> eventTimelineList = getEventTimelineList();

        //for (Event event : getEventList())
        for (int i = eventTimelineList.size()-1; i >= 0; i--)
        {
            EventTimeline eventTimeline = eventTimelineList.get(i);
            if (eventTimeline != null)
            {
                long eventId = eventTimeline._fkEvent;
                Event event = getEventById(eventId);
                if (event != null)
                {
                //if (event.getStatusFromDB(this) != Event.ESTATUS_STOP)
                    event.stopEvent(this, eventTimelineList, activateRetirnProfile, true, saveEventStatus, false, false);
                }
            }
        }
    }

    void unlinkEventsFromProfile(Profile profile)
    {
        for (Event event : getEventList())
        {
            if (event._fkProfileStart == profile._id)
                event._fkProfileStart = 0;
            if (event._fkProfileEnd == profile._id)
                event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
        }
    }

    void unlinkAllEvents()
    {
        for (Event event : getEventList())
        {
            event._fkProfileStart = 0;
            event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
        }
    }

    void activateProfileOnBoot()
    {
        if (ApplicationPreferences.applicationActivate(context))
        {
            Profile profile = getDatabaseHandler().getActivatedProfile();
            long profileId;
            if (profile != null)
                profileId = profile._id;
            else
            {
                profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                    profileId = 0;
            }
            activateProfile(profileId, PPApplication.STARTUP_SOURCE_BOOT, null/*, ""*/);
        }
        else
            activateProfile(0, PPApplication.STARTUP_SOURCE_BOOT, null/*, ""*/);
    }

    // this is called in boot or first start application
    void firstStartEvents(boolean startedFromService)
    {
        PPApplication.logE("DataWrapper.firstStartEvents", "startedFromService="+startedFromService);

        if (startedFromService)
            invalidateEventList();  // force load form db

        if (!startedFromService) {
            Event.setEventsBlocked(context, false);
            for (Event event : getEventList())
            {
                if (event != null)
                    event._blocked = false;
            }
            getDatabaseHandler().unblockAllEvents();
            Event.setForceRunEventRunning(context, false);
        }

        /*
        if (startedFromService) {
            // deactivate profile, profile will by activated in call of RestartEventsBroadcastReceiver
            getDatabaseHandler().deactivateProfile();
        }
        */

        resetAllEventsInDelayStart(true);
        resetAllEventsInDelayEnd(true);

        WifiScanAlarmBroadcastReceiver.setAlarm(context, true, false, false);
        BluetoothScanAlarmBroadcastReceiver.setAlarm(context, true, false);
        GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, true, false);
        SearchCalendarEventsBroadcastReceiver.setAlarm(context, true);

        if (!getIsManualProfileActivation()) {
            PPApplication.logE("DataWrapper.firstStartEvents", "no manual profile activation, restart events");
            Intent intent = new Intent(context, RestartEventsBroadcastReceiver.class);
            intent.putExtra(EXTRA_UNBLOCKEVENTSRUN, false);
            intent.putExtra(EXTRA_INTERACTIVE, false);
            context.sendBroadcast(intent);
        }
        else
        {
            PPApplication.logE("DataWrapper.firstStartEvents", "manual profile activation, activate profile");
            activateProfileOnBoot();
        }
    }

    Event getNoinitializedEvent(String name, int startOrder)
    {
        return new Event(name,
                startOrder,
                0,
                Profile.PROFILE_NO_ACTIVATE,
                Event.ESTATUS_STOP,
                "",
                false,
                false,
                Event.EPRIORITY_MEDIUM,
                0,
                false,
                Event.EATENDDO_UNDONE_PROFILE,
                false,
                Profile.PROFILE_NO_ACTIVATE,
                0,
                false,
                0,
                0
         );
    }

    private long getProfileIdByName(String name)
    {
        if (profileList == null)
        {
            return getDatabaseHandler().getProfileIdByName(name);
        }
        else
        {
            Profile profile;
            for (int i = 0; i < profileList.size(); i++)
            {
                profile = profileList.get(i);
                if (profile._name.equals(name))
                    return profile._id;
            }
            return 0;
        }
    }

    Event getPredefinedEvent(int index, boolean saveToDB) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        int gmtOffset = TimeZone.getDefault().getRawOffset();

        Event event;

        switch (index) {
            case 0:
                event = getNoinitializedEvent(context.getString(R.string.default_event_name_during_the_week), 5);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_home));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_NONE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wendesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._startTime = calendar.getTimeInMillis() + gmtOffset;
                ///calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._endTime = calendar.getTimeInMillis() + gmtOffset;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 1:
                event = getNoinitializedEvent(context.getString(R.string.default_event_name_weekend), 5);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_home));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_NONE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._startTime = calendar.getTimeInMillis() + gmtOffset;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._endTime = calendar.getTimeInMillis() + gmtOffset;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 2:
                event = getNoinitializedEvent(context.getString(R.string.default_event_name_during_the_work), 8);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_work));
                //event._undoneProfile = true;
                event._atEndDo = Event.EATENDDO_NONE;
                event._priority = Event.EPRIORITY_HIGHER;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wendesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 30);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._startTime = calendar.getTimeInMillis() + gmtOffset;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 17);
                calendar.set(Calendar.MINUTE, 30);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._endTime = calendar.getTimeInMillis() + gmtOffset;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 3:
                event = getNoinitializedEvent(context.getString(R.string.default_event_name_overnight), 5);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_sleep));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_UNDONE_PROFILE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wendesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._startTime = calendar.getTimeInMillis() + gmtOffset;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._endTime = calendar.getTimeInMillis() + gmtOffset;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 4:
                event = getNoinitializedEvent(context.getString(R.string.default_event_name_night_call), 10);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_home));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_UNDONE_PROFILE;
                event._priority = Event.EPRIORITY_HIGHEST;
                event._forceRun = true;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wendesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._startTime = calendar.getTimeInMillis() + gmtOffset;
                //calendar.clear(Calendar.DATE);
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                calendar.set(Calendar.MINUTE, 0);
                //calendar.set(Calendar.SECOND, 0);
                //calendar.set(Calendar.MILLISECOND, 0);
                event._eventPreferencesTime._endTime = calendar.getTimeInMillis() + gmtOffset;
                //event._eventPreferencesTime._useEndTime = true;
                event._eventPreferencesCall._enabled = true;
                event._eventPreferencesCall._callEvent = EventPreferencesCall.CALL_EVENT_RINGING;
                event._eventPreferencesCall._contactListType = EventPreferencesCall.CONTACT_LIST_TYPE_WHITE_LIST;
                break;
            case 5:
                event = getNoinitializedEvent(context.getString(R.string.default_event_name_low_battery), 10);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_battery_low));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_UNDONE_PROFILE;
                event._priority = Event.EPRIORITY_HIGHEST;
                event._forceRun = true;
                event._eventPreferencesBattery._enabled = true;
                event._eventPreferencesBattery._levelLow = 0;
                event._eventPreferencesBattery._levelHight = 10;
                event._eventPreferencesBattery._charging = false;
                break;
            default:
                event = null;
        }

        if (event != null) {
            if (saveToDB)
                getDatabaseHandler().addEvent(event);
        }

        return event;
    }

    void generatePredefinedEventList()
    {
        invalidateEventList();
        getDatabaseHandler().deleteAllEvents();

        for (int index = 0; index < 5; index++)
            getPredefinedEvent(index, true);
    }


//---------------------------------------------------

    List<EventTimeline> getEventTimelineList()
    {
        return getDatabaseHandler().getAllEventTimelines();
    }

    public void invalidateDataWrapper()
    {
        invalidateProfileList();
        invalidateEventList();
        databaseHandler = null;
        if (activateProfileHelper != null)
            activateProfileHelper.deinitialize();
        activateProfileHelper = null;
    }

//----- Activate profile ---------------------------------------------------------------------------------------------

    void _activateProfile(Profile _profile, boolean merged, int startupSource,
                                    boolean _interactive, Activity _activity)
    {
        // remove last configured profile duration alarm
        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        Profile.setActivatedProfileForDuration(context, 0);

        Profile profile = Profile.getMappedProfile(_profile, context);
        //profile = filterProfileWithBatteryEvents(profile);

        if (profile != null)
            PPApplication.logE("$$$ DataWrapper._activateProfile","profileName="+profile._name);
        else
            PPApplication.logE("$$$ DataWrapper._activateProfile","profile=null");

        PPApplication.logE("$$$ DataWrapper._activateProfile","startupSource="+startupSource);
        PPApplication.logE("$$$ DataWrapper._activateProfile","merged="+merged);

        //boolean interactive = _interactive;
        //final Activity activity = _activity;

        // get currently activated profile
        Profile activatedProfile = getActivatedProfile();

        if ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE) &&
            //(startupSource != PPApplication.STARTUP_SOURCE_BOOT) &&  // on boot must set as manual activation
            (startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START))
        {
            // manual profile activation

            ActivateProfileHelper.lockRefresh = true;

            // pause all events
            // for forcerRun events set system events and block all events
            pauseAllEvents(false, true/*, true*/);

            ActivateProfileHelper.lockRefresh = false;
        }

        databaseHandler.activateProfile(profile);
        setProfileActive(profile);

        String profileIcon = "";
        int profileDuration = 0;

        if (profile != null)
        {
            profileIcon = profile._icon;

            if ((profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING) &&
                    (profile._duration > 0))
                profileDuration = profile._duration;

            activateProfileHelper.execute(profile, merged, _interactive);

            if ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE) &&
                (startupSource != PPApplication.STARTUP_SOURCE_BOOT) &&
                (startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START))
            {
                // manual profile activation
                PPApplication.logE("$$$ DataWrapper._activateProfile","manual profile activation");

                //// set profile duration alarm

                // save before activated profile
                long profileId = 0;
                if (activatedProfile != null)
                    profileId = activatedProfile._id;
                PPApplication.logE("$$$ DataWrapper._activateProfile","setActivatedProfileForDuration profileId="+profileId);
                Profile.setActivatedProfileForDuration(context, profileId);

                ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, context);
                ///////////
            }
            else {
                PPApplication.logE("$$$ DataWrapper._activateProfile","NO manual profile activation");
                profileDuration = 0;
            }
        }

        activatedProfile = getActivatedProfile();
        activateProfileHelper.showNotification(activatedProfile);
        activateProfileHelper.updateWidget();

        if ((profile != null) && (!merged)) {
            addActivityLog(DatabaseHandler.ALTYPE_PROFILEACTIVATION, null,
                    getProfileNameWithManualIndicator(profile, true, profileDuration > 0, false),
                    profileIcon, profileDuration);
        }

        if (profile != null)
        {
            if (ApplicationPreferences.notificationsToast(context) && (!ActivateProfileHelper.lockRefresh))
            {
                // toast notification
                if (PPApplication.toastHandler != null) {
                    final Profile __profile = profile;
                    PPApplication.toastHandler.post(new Runnable() {
                        public void run() {
                            showToastAfterActivation(__profile);
                        }
                    });
                } else
                    showToastAfterActivation(profile);
            }
        }

        // for startActivityForResult
        if (_activity != null)
        {
            Intent returnIntent = new Intent();
            if (profile == null)
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            else
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            returnIntent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            _activity.setResult(Activity.RESULT_OK,returnIntent);
        }

        finishActivity(startupSource, true, _activity);
    }

    private void showToastAfterActivation(Profile profile)
    {
        //Log.d("DataWrapper.showToastAfterActivation", "xxx");
        try {
            String profileName = getProfileNameWithManualIndicator(profile, true, false, false);
            Toast msg = Toast.makeText(context,
                    context.getResources().getString(R.string.toast_profile_activated_0) + ": " + profileName + " " +
                            context.getResources().getString(R.string.toast_profile_activated_1),
                    Toast.LENGTH_SHORT);
            msg.show();
        }
        catch (Exception ignored) {
        }
        //Log.d("DataWrapper.showToastAfterActivation", "-- end");
    }

    private void activateProfileWithAlert(Profile profile, int startupSource, final boolean interactive,
                                            Activity activity)
    {
        if (interactive && (ApplicationPreferences.applicationActivateWithAlert(context) ||
                            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)))
        {
            // set theme and language for dialog alert ;-)
            // not working on Android 2.3.x
            GlobalGUIRoutines.setTheme(activity, true, false);
            GlobalGUIRoutines.setLanguage(activity.getBaseContext());

            final Profile _profile = profile;
            //final boolean _interactive = interactive;
            final int _startupSource = startupSource;
            final Activity _activity = activity;
            final DataWrapper _dataWrapper = this;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(activity.getResources().getString(R.string.profile_string_0) + ": " + profile._name);
            dialogBuilder.setMessage(activity.getResources().getString(R.string.activate_profile_alert_message));
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (Permissions.grantProfilePermissions(context, _profile, false, false,
                            forGUI, monochrome, monochromeValue,
                            _startupSource, true, _activity, true)) {
                        if (_profile._askForDuration) {
                            FastAccessDurationDialog dlg = new FastAccessDurationDialog(_activity, _profile, _dataWrapper, _startupSource,
                                    true);
                            dlg.show();
                        }
                        else
                            _activateProfile(_profile, false, _startupSource, true, _activity);
                    }
                    else {
                        Intent returnIntent = new Intent();
                        _activity.setResult(Activity.RESULT_CANCELED,returnIntent);

                        finishActivity(_startupSource, false, _activity);
                    }
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // for startActivityForResult
                    Intent returnIntent = new Intent();
                    _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                    finishActivity(_startupSource, false, _activity);
                }
            });
            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    // for startActivityForResult
                    Intent returnIntent = new Intent();
                    _activity.setResult(Activity.RESULT_CANCELED,returnIntent);

                    finishActivity(_startupSource, false, _activity);
                }
            });
            dialogBuilder.show();
        }
        else
        {
            boolean granted;
            if (interactive) {
                // set theme and language for dialog alert ;-)
                // not working on Android 2.3.x
                GlobalGUIRoutines.setTheme(activity, true, false);
                GlobalGUIRoutines.setLanguage(activity.getBaseContext());

                granted = Permissions.grantProfilePermissions(context, profile, false, false,
                        forGUI, monochrome, monochromeValue,
                        startupSource, true, activity, true);
            }
            else
                granted = Permissions.grantProfilePermissions(context, profile, false, true,
                                        forGUI, monochrome, monochromeValue,
                                        startupSource, false, null, true);
            if (granted) {
                if (profile._askForDuration && interactive) {
                    FastAccessDurationDialog dlg = new FastAccessDurationDialog(activity, profile, this, startupSource,
                                                            true);
                    dlg.show();
                }
                else
                    _activateProfile(profile, false, startupSource, interactive, activity);
            }
        }
    }

    void finishActivity(int startupSource, boolean afterActivation, Activity _activity)
    {
        if (_activity == null)
            return;

        //final Activity activity = _activity;

        boolean finish = true;

        if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR)
        {
            finish = false;
            if (ApplicationPreferences.applicationClose(context))
            {
                // ma sa zatvarat aktivita po aktivacii
                if (PPApplication.getApplicationStarted(context, false))
                    // aplikacia je uz spustena, mozeme aktivitu zavriet
                    // tymto je vyriesene, ze pri spusteni aplikacie z launchera
                    // sa hned nezavrie
                    finish = afterActivation;
            }
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)
        {
            finish = false;
        }

        if (finish)
            _activity.finish();
    }

    public void activateProfile(long profile_id, int startupSource, Activity activity)
    {
        Profile profile;

        // pre profil, ktory je prave aktivny, treba aktualizovat aktivitu
        profile = getActivatedProfile();

        boolean actProfile = false;
        boolean interactive = false;
        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER))
        {
            // aktivacia spustena z shortcutu, widgetu, aktivatora, editora, zo service, profil aktivujeme
            actProfile = true;
            interactive = ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE));
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_BOOT)
        {
            // aktivacia bola spustena po boote telefonu

            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);

            if (ApplicationPreferences.applicationActivate(context))
            {
                // je nastavene, ze pri starte sa ma aktivita aktivovat
                actProfile = true;
            }
            /*else
            {
                // nema sa aktivovat profil pri starte, ale musim pozriet, ci daky event bezi
                // a ak ano, aktivovat profil posledneho eventu v timeline
                boolean eventRunning = false;
                List<EventTimeline> eventTimelineList = getEventTimelineList();
                if (eventTimelineList.size() > 0)
                {
                    eventRunning = true;

                    EventTimeline eventTimeline = eventTimelineList.get(eventTimelineList.size()-1);

                    Event _event = getEventById(eventTimeline._fkEvent);
                    profile = getProfileById(_event._fkProfileStart);
                    actProfile = true;
                }


                if ((profile != null) && (!eventRunning))
                {
                    getDatabaseHandler().deactivateProfile();
                    //profile._checked = false;
                    profile = null;
                }
            }*/
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER_START)
        {
            // aktivacia bola spustena z lauchera

            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);

            if (ApplicationPreferences.applicationActivate(context))
            {
                // je nastavene, ze pri starte sa ma aktivita aktivovat
                actProfile = true;
            }
            /*else
            {
                if (profile != null)
                {
                    getDatabaseHandler().deactivateProfile();
                    //profile._checked = false;
                    profile = null;
                }
            }*/
        }

        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER_START) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER))
        {
            if (profile_id == 0)
                profile = null;
            else
                profile = getProfileById(profile_id, false);
        }


        if (actProfile && (profile != null))
        {
            // aktivacia profilu
            activateProfileWithAlert(profile, startupSource, interactive, activity);
        }
        else
        {
            activateProfileHelper.showNotification(profile);
            activateProfileHelper.updateWidget();

            // for startActivityForResult
            if (activity != null)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
                returnIntent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
                activity.setResult(Activity.RESULT_OK,returnIntent);
            }

            finishActivity(startupSource, true, activity);
        }

    }

    @SuppressWarnings("deprecation")
    @SuppressLint({ "NewApi", "SimpleDateFormat" })
    void doEventService(Event event, boolean statePause,
                                    boolean restartEvent, boolean interactive,
                                    boolean forDelayStartAlarm, boolean forDelayEndAlarm,
                                    boolean reactivate, Profile mergedProfile,
                                    String broadcastType)
    {
        if (!Permissions.grantEventPermissions(context, event, true))
            return;

        int newEventStatus;// = Event.ESTATUS_NONE;

        boolean ignoreTime = true;
        boolean ignoreBattery = true;
        boolean ignoreCall = true;
        boolean ignorePeripheral = true;
        boolean ignoreCalendar = true;
        boolean ignoreWifi = true;
        boolean ignoreScreen = true;
        boolean ignoreBluetooth = true;
        boolean ignoreSms = true;
        boolean ignoreNotification = true;
        boolean ignoreApplication = true;
        boolean ignoreLocation = true;
        boolean ignoreOrientation = true;
        boolean ignoreMobileCell = true;
        boolean ignoreNfc = true;
        boolean ignoreRadioSwitch = true;

        boolean timePassed = true;
        boolean batteryPassed = true;
        boolean callPassed = true;
        boolean peripheralPassed = true;
        boolean calendarPassed = true;
        boolean wifiPassed = true;
        boolean screenPassed = true;
        boolean bluetoothPassed = true;
        boolean smsPassed = true;
        boolean notificationPassed = true;
        boolean applicationPassed = true;
        boolean locationPassed = true;
        boolean orientationPassed = true;
        boolean mobileCellPassed = true;
        boolean nfcPassed = true;
        boolean radioSwitchPassed = true;

        PPApplication.logE("%%% DataWrapper.doEventService","--- start --------------------------");
        PPApplication.logE("%%% DataWrapper.doEventService","------- event._id="+event._id);
        PPApplication.logE("%%% DataWrapper.doEventService","------- event._name="+event._name);
        PPApplication.logE("%%% DataWrapper.doEventService","------- broadcastType="+broadcastType);

        if (event._eventPreferencesTime._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignoreTime = false;

            // compute start datetime
            long startAlarmTime;
            long endAlarmTime;

            startAlarmTime = event._eventPreferencesTime.computeAlarm(true);
            //if (broadcastType.equals(EventTimeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            //    startAlarmTime -= 30 * 1000;

            String alarmTimeS = DateFormat.getDateFormat(context).format(startAlarmTime) +
                                " " + DateFormat.getTimeFormat(context).format(startAlarmTime);
            PPApplication.logE("%%% DataWrapper.doEventService","startAlarmTime="+alarmTimeS);

            endAlarmTime = event._eventPreferencesTime.computeAlarm(false);
            //if (broadcastType.equals(EventTimeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            //    endAlarmTime += 60 * 1000;

            alarmTimeS = DateFormat.getDateFormat(context).format(endAlarmTime) +
                         " " + DateFormat.getTimeFormat(context).format(endAlarmTime);
            PPApplication.logE("%%% DataWrapper.doEventService","endAlarmTime="+alarmTimeS);

            Calendar now = Calendar.getInstance();
            long nowAlarmTime = now.getTimeInMillis();
            alarmTimeS = DateFormat.getDateFormat(context).format(nowAlarmTime) +
                 " " + DateFormat.getTimeFormat(context).format(nowAlarmTime);
            PPApplication.logE("%%% DataWrapper.doEventService","nowAlarmTime="+alarmTimeS);

            timePassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));

            PPApplication.logE("%%% DataWrapper.doEventService","timePassed="+timePassed);

            //eventStart = eventStart && timePassed;
        }

        if (event._eventPreferencesBattery._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignoreBattery = false;

            boolean isCharging;
            int batteryPct;

            // get battery status
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            if (batteryStatus != null)
            {
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                PPApplication.logE("*** DataWrapper.doEventService","status="+status);
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL;
                PPApplication.logE("*** DataWrapper.doEventService","isCharging="+isCharging);

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                PPApplication.logE("*** DataWrapper.doEventService","level="+level);
                PPApplication.logE("*** DataWrapper.doEventService","scale="+scale);

                batteryPct = Math.round(level/(float)scale*100);
                PPApplication.logE("*** DataWrapper.doEventService","batteryPct="+batteryPct);

                batteryPassed = (isCharging == event._eventPreferencesBattery._charging);

                if (batteryPassed)
                {
                    if ((batteryPct >= event._eventPreferencesBattery._levelLow) &&
                        (batteryPct <= event._eventPreferencesBattery._levelHight))
                    {
                        //eventStart = eventStart && true;
                    }
                    else
                    {
                        batteryPassed = false;
                        //eventStart = eventStart && false;
                    }
                }
                if (batteryPassed && event._eventPreferencesBattery._powerSaveMode) {
                    batteryPassed = isPowerSaveMode();
                }
            }
            else
                batteryPassed = false;

        }

        if ((event._eventPreferencesCall._enabled)  &&
                (Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)&&
                Permissions.checkEventCallContacts(context, event) &&
                Permissions.checkEventPhoneBroadcast(context, event))
        {
            ignoreCall = false;

            ApplicationPreferences.getSharedPreferences(context);
            int callEventType = ApplicationPreferences.preferences.getInt(PhoneCallService.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallService.CALL_EVENT_UNDEFINED);
            String phoneNumber = ApplicationPreferences.preferences.getString(PhoneCallService.PREF_EVENT_CALL_PHONE_NUMBER, "");

            boolean phoneNumberFinded = false;

            if (callEventType != PhoneCallService.CALL_EVENT_UNDEFINED)
            {
                if (event._eventPreferencesCall._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE)
                {
                    // find phone number in groups
                    String[] splits = event._eventPreferencesCall._contactGroups.split("\\|");
                    for (String split : splits) {
                        String[] projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};
                        String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=? AND "
                                + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                        String[] selectionArgs = new String[]{split};
                        Cursor mCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
                        if (mCursor != null) {
                            while (mCursor.moveToNext()) {
                                String contactId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                                String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                                String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " +
                                        ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1";
                                String[] selection2Args = new String[]{contactId};
                                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                                if (phones != null) {
                                    while (phones.moveToNext()) {
                                        String _phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                            phoneNumberFinded = true;
                                            break;
                                        }
                                    }
                                    phones.close();
                                }
                                if (phoneNumberFinded)
                                    break;
                            }
                            mCursor.close();
                        }
                        if (phoneNumberFinded)
                            break;
                    }

                    if (!phoneNumberFinded) {
                        // find phone number in contacts
                        splits = event._eventPreferencesCall._contacts.split("\\|");
                        for (String split : splits) {
                            String[] splits2 = split.split("#");

                            // get phone number from contacts
                            String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER};
                            String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1' and " + ContactsContract.Contacts._ID + "=?";
                            String[] selectionArgs = new String[]{splits2[0]};
                            Cursor mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs, null);
                            if (mCursor != null) {
                                while (mCursor.moveToNext()) {
                                    String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
                                    String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " + ContactsContract.CommonDataKinds.Phone._ID + "=?";
                                    String[] selection2Args = new String[]{splits2[0], splits2[1]};
                                    Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                                    if (phones != null) {
                                        while (phones.moveToNext()) {
                                            String _phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                            if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                phoneNumberFinded = true;
                                                break;
                                            }
                                        }
                                        phones.close();
                                    }
                                    if (phoneNumberFinded)
                                        break;
                                }
                                mCursor.close();
                            }
                            if (phoneNumberFinded)
                                break;
                        }
                    }

                    if (event._eventPreferencesCall._contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                        phoneNumberFinded = !phoneNumberFinded;
                }
                else
                    phoneNumberFinded = true;

                if (phoneNumberFinded)
                {
                    if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_RINGING)
                    {
                        if ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_RINGING) ||
                            ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ANSWERED)))
                            ;//eventStart = eventStart && true;
                        else
                            callPassed = false;
                    }
                    else
                    if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ANSWERED)
                    {
                        if (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ANSWERED)
                            ;//eventStart = eventStart && true;
                        else
                            callPassed = false;
                    }
                    else
                    if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_STARTED)
                    {
                        if (callEventType == PhoneCallService.CALL_EVENT_OUTGOING_CALL_ANSWERED)
                            ;//eventStart = eventStart && true;
                        else
                            callPassed = false;
                    }

                    if ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ENDED) ||
                        (callEventType == PhoneCallService.CALL_EVENT_OUTGOING_CALL_ENDED))
                    {
                        //callPassed = true;
                        //eventStart = eventStart && false;
                        callPassed = false;
                    }
                }
                else
                    callPassed = false;

            }
            else
                callPassed = false;
        }

        if (event._eventPreferencesPeripherals._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignorePeripheral = false;

            if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK) ||
                (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK))
            {
                // get dock status
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
                Intent dockStatus = context.registerReceiver(null, ifilter);

                boolean isDocked = false;
                boolean isCar = false;
                boolean isDesk = false;

                if (dockStatus != null)
                {
                    int dockState = dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
                    isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;
                    isCar = dockState == Intent.EXTRA_DOCK_STATE_CAR;
                    isDesk = dockState == Intent.EXTRA_DOCK_STATE_DESK ||
                             dockState == Intent.EXTRA_DOCK_STATE_LE_DESK ||
                             dockState == Intent.EXTRA_DOCK_STATE_HE_DESK;
                }

                if (isDocked)
                {
                    if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK)
                            && isDesk)
                        peripheralPassed = true;
                    else
                        peripheralPassed = (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK)
                                && isCar;
                }
                else
                    peripheralPassed = false;
                //eventStart = eventStart && peripheralPassed;
            }
            else
            if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET) ||
                (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET) ||
                (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES))
            {
                ApplicationPreferences.getSharedPreferences(context);
                boolean headsetConnected = ApplicationPreferences.preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_HEADSET_CONNECTED, false);
                boolean headsetMicrophone = ApplicationPreferences.preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_HEADSET_MICROPHONE, false);
                boolean bluetoothHeadset = ApplicationPreferences.preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_HEADSET_BLUETOOTH, false);

                if (headsetConnected)
                {
                    if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET)
                            && headsetMicrophone && (!bluetoothHeadset))
                        peripheralPassed = true;
                    else
                    if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET)
                            && headsetMicrophone && bluetoothHeadset)
                        peripheralPassed = true;
                    else
                        peripheralPassed = (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES)
                                && (!headsetMicrophone) && (!bluetoothHeadset);
                }
                else
                    peripheralPassed = false;
                //eventStart = eventStart && peripheralPassed;
            }
        }

        if ((event._eventPreferencesCalendar._enabled) &&
                (Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) &&
                (Permissions.checkEventCalendar(context, event)))
        {
            ignoreCalendar = false;

            // compute start datetime
            long startAlarmTime;
            long endAlarmTime;

            if (event._eventPreferencesCalendar._eventFound)
            {
                startAlarmTime = event._eventPreferencesCalendar.computeAlarm(true);
                //if (broadcastType.equals(EventCalendarBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
                //    startAlarmTime -= 30 * 1000;

                String alarmTimeS = DateFormat.getDateFormat(context).format(startAlarmTime) +
                                    " " + DateFormat.getTimeFormat(context).format(startAlarmTime);
                PPApplication.logE("DataWrapper.doEventService","startAlarmTime="+alarmTimeS);

                endAlarmTime = event._eventPreferencesCalendar.computeAlarm(false);
                //if (broadcastType.equals(EventCalendarBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
                //    endAlarmTime += 60 * 1000;

                alarmTimeS = DateFormat.getDateFormat(context).format(endAlarmTime) +
                             " " + DateFormat.getTimeFormat(context).format(endAlarmTime);
                PPApplication.logE("DataWrapper.doEventService","endAlarmTime="+alarmTimeS);

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();
                alarmTimeS = DateFormat.getDateFormat(context).format(nowAlarmTime) +
                     " " + DateFormat.getTimeFormat(context).format(nowAlarmTime);
                PPApplication.logE("DataWrapper.doEventService","nowAlarmTime="+alarmTimeS);

                calendarPassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
            }
            else
                calendarPassed = false;

            //eventStart = eventStart && calendarPassed;
        }

        if (event._eventPreferencesWifi._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventLocation(context, event))
        {
            ignoreWifi = false;

            PPApplication.logE("----- DataWrapper.doEventService","-------- eventSSID="+event._eventPreferencesWifi._SSID);

            wifiPassed = false;

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            boolean isWifiEnabled = wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;

            List<WifiSSIDData> wifiConfigurationList = WifiScanAlarmBroadcastReceiver.getWifiConfigurationList(context);

            boolean done = false;

            if (isWifiEnabled)
            {
                PPApplication.logE("----- DataWrapper.doEventService","wifiStateEnabled=true");

                //PPApplication.logE("----- DataWrapper.doEventService","-- eventSSID="+event._eventPreferencesWifi._SSID);

                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                boolean wifiConnected = false;

                ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    Network[] networks = connManager.getAllNetworks();
                    if ((networks != null) && (networks.length > 0)) {
                        for (Network ntk : networks) {
                            NetworkInfo ntkInfo = connManager.getNetworkInfo(ntk);
                            if (ntkInfo.getType() == ConnectivityManager.TYPE_WIFI && ntkInfo.isConnected()) {
                                if (wifiInfo != null) {
                                    wifiConnected = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                else {
                    NetworkInfo ntkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    wifiConnected = (ntkInfo != null) && ntkInfo.isConnected();
                }

                if (wifiConnected)
                {
                    PPApplication.logE("----- DataWrapper.doEventService","wifi connected");

                    PPApplication.logE("----- DataWrapper.doEventService","wifiSSID="+WifiScanAlarmBroadcastReceiver.getSSID(wifiInfo, wifiConfigurationList));
                    PPApplication.logE("----- DataWrapper.doEventService","wifiBSSID="+wifiInfo.getBSSID());

                    //PPApplication.logE("----- DataWrapper.doEventService","SSID="+event._eventPreferencesWifi._SSID);

                    String[] splits = event._eventPreferencesWifi._SSID.split("\\|");
                    for (String _ssid : splits) {
                        if (_ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)){
                            wifiPassed = true;
                        }
                        else
                        if (_ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE)){
                            for (WifiSSIDData data : wifiConfigurationList) {
                                wifiPassed = WifiScanAlarmBroadcastReceiver.compareSSID(wifiInfo, data.ssid.replace("\"", ""), wifiConfigurationList);
                                if (wifiPassed)
                                    break;
                            }
                        }
                        else
                            wifiPassed = WifiScanAlarmBroadcastReceiver.compareSSID(wifiInfo, _ssid, wifiConfigurationList);
                        if (wifiPassed)
                            break;
                    }

                    //PPApplication.logE("----- DataWrapper.doEventService","wifiPassed="+wifiPassed);

                    if (wifiPassed)
                    {
                        // event SSID is connected
                        done = true;

                        if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED) ||
                            (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTINFRONT))
                            // for this connectionTypes, wifi must not be connected to event SSID
                            wifiPassed = false;
                        //PPApplication.logE("----- DataWrapper.doEventService","wifiPassed="+wifiPassed);
                    }

                }
                else
                {
                    PPApplication.logE("----- DataWrapper.doEventService", "wifi not connected");

                    if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED) {
                        // for this connectionTypes, wifi must not be connected to event SSID
                        done = true;
                        wifiPassed = true;
                    }
                }

            }
            else {
                PPApplication.logE("----- DataWrapper.doEventService", "wifiStateEnabled=false");
                if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                    (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED)) {
                    // for this connectionTypes, wifi must not be connected to event SSID
                    done = true;
                    wifiPassed = (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED);
                }
            }

            PPApplication.logE("----- DataWrapper.doEventService","wifiPassed="+wifiPassed);

            if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_INFRONT) ||
                (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTINFRONT))
            {
                if (!done)
                {
                    wifiPassed = false;

                    List<WifiSSIDData> scanResults = WifiScanAlarmBroadcastReceiver.getScanResults(context);

                    //PPApplication.logE("----- DataWrapper.doEventService","scanResults="+scanResults);

                    //if (WifiScanAlarmBroadcastReceiver.scanResults != null)
                    if (scanResults != null)
                    {
                        PPApplication.logE("----- DataWrapper.doEventService","scanResults != null");
                        PPApplication.logE("----- DataWrapper.doEventService","scanResults.size="+scanResults.size());
                        //PPApplication.logE("----- DataWrapper.doEventService","-- eventSSID="+event._eventPreferencesWifi._SSID);

                        for (WifiSSIDData result : scanResults)
                        {
                            PPApplication.logE("----- DataWrapper.doEventService","scanSSID="+result.ssid);
                            PPApplication.logE("----- DataWrapper.doEventService","scanBSSID="+result.bssid);
                            String[] splits = event._eventPreferencesWifi._SSID.split("\\|");
                            for (String _ssid : splits) {
                                if (_ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)) {
                                    PPApplication.logE("----- DataWrapper.doEventService","all ssids");
                                    wifiPassed = true;
                                    break;
                                }
                                else
                                if (_ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE)) {
                                    PPApplication.logE("----- DataWrapper.doEventService","configured ssids");
                                    for (WifiSSIDData data : wifiConfigurationList) {
                                        PPApplication.logE("----- DataWrapper.doEventService","configured SSID="+data.ssid.replace("\"", ""));
                                        if (WifiScanAlarmBroadcastReceiver.compareSSID(result, data.ssid.replace("\"", ""), wifiConfigurationList)) {
                                            PPApplication.logE("----- DataWrapper.doEventService", "wifi found");
                                            wifiPassed = true;
                                            break;
                                        }
                                    }
                                    if (wifiPassed)
                                        break;
                                } else {
                                    PPApplication.logE("----- DataWrapper.doEventService","event SSID="+event._eventPreferencesWifi._SSID);
                                    if (WifiScanAlarmBroadcastReceiver.compareSSID(result, _ssid, wifiConfigurationList)) {
                                        PPApplication.logE("----- DataWrapper.doEventService", "wifi found");
                                        wifiPassed = true;
                                        break;
                                    }
                                }
                            }
                            if (wifiPassed)
                                break;
                        }

                        PPApplication.logE("----- DataWrapper.doEventService","wifiPassed="+wifiPassed);

                        if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTINFRONT)
                            // if wifi is not in front of event SSID, then passed
                            wifiPassed = !wifiPassed;

                        PPApplication.logE("----- DataWrapper.doEventService","wifiPassed="+wifiPassed);

                    }
                    else
                        PPApplication.logE("----- DataWrapper.doEventService","scanResults == null");

                }
            }

            PPApplication.logE("----- DataWrapper.doEventService","------- wifiPassed="+wifiPassed);

            //eventStart = eventStart && wifiPassed;
        }

        if (event._eventPreferencesScreen._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignoreScreen = false;

            boolean isScreenOn;
            //if (android.os.Build.VERSION.SDK_INT >= 20)
            //{
            //	Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            //	isScreenOn = display.getState() != Display.STATE_OFF;
            //}
            //else
            //{
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                isScreenOn = pm.isScreenOn();
            //}
            boolean keyguardShowing = false;

            if (event._eventPreferencesScreen._whenUnlocked)
            {
                KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= 16)
                    keyguardShowing = kgMgr.isKeyguardLocked();
                else
                    keyguardShowing = kgMgr.inKeyguardRestrictedInputMode();
            }

            if (event._eventPreferencesScreen._eventType == EventPreferencesScreen.ETYPE_SCREENON)
            {
                if (event._eventPreferencesScreen._whenUnlocked)
                    screenPassed = isScreenOn && (!keyguardShowing);
                else
                    screenPassed = isScreenOn;
            }
            else
            {
                if (event._eventPreferencesScreen._whenUnlocked)
                    screenPassed = (!isScreenOn) || keyguardShowing;
                else
                    screenPassed = !isScreenOn;
            }

            //eventStart = eventStart && screenPassed;
        }

        if (event._eventPreferencesBluetooth._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventLocation(context, event))
        {
            ignoreBluetooth = false;

            bluetoothPassed = false;

            List<BluetoothDeviceData> boundedDevicesList = BluetoothScanAlarmBroadcastReceiver.getBoundedDevicesList(context);

            BluetoothAdapter bluetooth = BluetoothScanAlarmBroadcastReceiver.getBluetoothAdapter(context);
            boolean isBluetoothEnabled = bluetooth.isEnabled();

            boolean done = false;

            if (isBluetoothEnabled)
            {
                PPApplication.logE("[BTScan] DataWrapper.doEventService","bluetoothEnabled=true");

                PPApplication.logE("[BTScan] DataWrapper.doEventService","-- eventAdapterName="+event._eventPreferencesBluetooth._adapterName);

                if (BluetoothConnectionBroadcastReceiver.isBluetoothConnected(context, "")) {

                    PPApplication.logE("[BTScan] DataWrapper.doEventService", "bluetooth connected");

                    boolean connected = false;
                    String[] splits = event._eventPreferencesBluetooth._adapterName.split("\\|");
                    for (String _bluetoothName : splits) {
                        if (_bluetoothName.equals(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE)) {
                            connected = true;
                            break;
                        }
                        else
                        if (_bluetoothName.equals(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE)) {
                            for (BluetoothDeviceData data : boundedDevicesList) {
                                connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(context, data.getName());
                                if (connected)
                                    break;
                            }
                        } else
                            connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(context, _bluetoothName);
                        if (connected)
                            break;
                    }

                    if (connected) {
                        // event BT adapter is connected
                        done = true;

                        bluetoothPassed = !((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED) ||
                                (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTINFRONT));
                    }
                    else {
                        if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED) {
                            // for this connectionTypes, BT must not be connected to event BT adapter
                            done = true;
                            bluetoothPassed = true;
                        }
                    }
                }
                else
                {
                    PPApplication.logE("[BTScan] DataWrapper.doEventService", "bluetooth not connected");

                    if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED) {
                        // for this connectionTypes, BT must not be connected to event BT adapter
                        done = true;
                        bluetoothPassed = true;
                    }
                }
            }
            else {
                PPApplication.logE("[BTScan] DataWrapper.doEventService", "bluetoothEnabled=true");

                if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                    (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED)) {
                    // for this connectionTypes, BT must not be connected to event BT adapter
                    done = true;
                    bluetoothPassed = (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED);
                }
            }

            PPApplication.logE("[BTScan] DataWrapper.doEventService","bluetoothPassed="+bluetoothPassed);

            if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_INFRONT) ||
                (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTINFRONT))
            {
                if (!done)
                {
                    bluetoothPassed = false;

                    List<BluetoothDeviceData> scanResults = BluetoothScanAlarmBroadcastReceiver.getScanResults(context);

                    if (scanResults != null)
                    {
                        //PPApplication.logE("@@@ DataWrapper.doEventService","-- eventAdapterName="+event._eventPreferencesBluetooth._adapterName);

                        for (BluetoothDeviceData device : scanResults)
                        {
                            String[] splits = event._eventPreferencesBluetooth._adapterName.split("\\|");
                            for (String _bluetoothName : splits) {
                                if (_bluetoothName.equals(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE)) {
                                    bluetoothPassed = true;
                                    break;
                                }
                                else
                                if (_bluetoothName.equals(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE)) {
                                    for (BluetoothDeviceData data : boundedDevicesList) {
                                        String _device = device.getName().toUpperCase();
                                        String _adapterName = data.getName().toUpperCase();
                                        if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                            PPApplication.logE("[BTScan] DataWrapper.doEventService", "bluetooth found");
                                            //PPApplication.logE("@@@ DataWrapper.doEventService","bluetoothAdapterName="+device.getName());
                                            //PPApplication.logE("@@@ DataWrapper.doEventService","bluetoothAddress="+device.getAddress());
                                            bluetoothPassed = true;
                                            break;
                                        }
                                    }
                                    if (bluetoothPassed)
                                        break;
                                } else {
                                    String _device = device.getName().toUpperCase();
                                    if ((device.getName() == null) || device.getName().isEmpty()) {
                                        // scanned device has not name (hidden BT?)
                                        if ((device.getAddress() != null) && (!device.getAddress().isEmpty()))
                                        {
                                            // device has address
                                            for (BluetoothDeviceData data : boundedDevicesList) {
                                                if ((data.getAddress() != null) && data.getAddress().equals(device.getAddress())) {
                                                    PPApplication.logE("[BTScan] DataWrapper.doEventService", "bluetooth found");
                                                    //PPApplication.logE("@@@ DataWrapper.doEventService","bluetoothAdapterName="+device.getName());
                                                    //PPApplication.logE("@@@ DataWrapper.doEventService","bluetoothAddress="+device.getAddress());
                                                    bluetoothPassed = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        String _adapterName = _bluetoothName.toUpperCase();
                                        if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                            PPApplication.logE("[BTScan] DataWrapper.doEventService", "bluetooth found");
                                            //PPApplication.logE("@@@ DataWrapper.doEventService","bluetoothAdapterName="+device.getName());
                                            //PPApplication.logE("@@@ DataWrapper.doEventService","bluetoothAddress="+device.getAddress());
                                            bluetoothPassed = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (bluetoothPassed)
                                break;
                        }

                        if (!bluetoothPassed)
                            PPApplication.logE("[BTScan] DataWrapper.doEventService","bluetooth not found");

                        if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTINFRONT)
                            // if bluetooth is not in front of event BT adapter name, then passed
                            bluetoothPassed = !bluetoothPassed;
                    }
                    else
                        PPApplication.logE("[BTScan] DataWrapper.doEventService","scanResults == null");

                }
            }

            PPApplication.logE("[BTScan] DataWrapper.doEventService","bluetoothPassed="+bluetoothPassed);

            //eventStart = eventStart && bluetoothPassed;
        }

        if ((event._eventPreferencesSMS._enabled) &&
                (Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventSMSContacts(context, event) &&
                Permissions.checkEventSMSBroadcast(context, event))
        {
            ignoreSms = false;

            // compute start time

            if (event._eventPreferencesSMS._startTime > 0) {
                int gmtOffset = TimeZone.getDefault().getRawOffset();
                long startTime = event._eventPreferencesSMS._startTime - gmtOffset;

                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String alarmTimeS = sdf.format(startTime);
                PPApplication.logE("DataWrapper.doEventService", "startTime=" + alarmTimeS);

                // compute end datetime
                long endAlarmTime = event._eventPreferencesSMS.computeAlarm();
                alarmTimeS = sdf.format(endAlarmTime);
                PPApplication.logE("DataWrapper.doEventService", "endAlarmTime=" + alarmTimeS);

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();
                alarmTimeS = sdf.format(nowAlarmTime);
                PPApplication.logE("DataWrapper.doEventService", "nowAlarmTime=" + alarmTimeS);

                if (broadcastType.equals(SMSBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
                    smsPassed = true;
                else if (!event._eventPreferencesSMS._permanentRun) {
                    if (broadcastType.equals(SMSEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
                        smsPassed = false;
                    else
                        smsPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                }
                else {
                    smsPassed = nowAlarmTime >= startTime;
                }
            }
            else
                smsPassed = false;

            if (!smsPassed) {
                event._eventPreferencesSMS._startTime = 0;
                getDatabaseHandler().updateSMSStartTime(event);
            }
        }

        if (event._eventPreferencesNotification._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignoreNotification = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!event._eventPreferencesNotification._endWhenRemoved) {

                    if (event._eventPreferencesNotification._startTime > 0) {
                        // comute start time
                        int gmtOffset = TimeZone.getDefault().getRawOffset();
                        long startTime = event._eventPreferencesNotification._startTime - gmtOffset;

                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("DataWrapper.doEventService", "startTime=" + alarmTimeS);

                        // compute end datetime
                        long endAlarmTime = event._eventPreferencesNotification.computeAlarm();
                        alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("DataWrapper.doEventService", "endAlarmTime=" + alarmTimeS);

                        Calendar now = Calendar.getInstance();
                        long nowAlarmTime = now.getTimeInMillis();
                        alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("DataWrapper.doEventService", "nowAlarmTime=" + alarmTimeS);

                        if (broadcastType.equals(NotificationBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
                            notificationPassed = true;
                        else if (!event._eventPreferencesNotification._permanentRun) {
                            if (broadcastType.equals(NotificationEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
                                notificationPassed = false;
                            else
                                notificationPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        }
                        else
                            notificationPassed = nowAlarmTime >= startTime;
                    } else
                        notificationPassed = false;
                } else {
                    notificationPassed = event._eventPreferencesNotification.isNotificationVisible(this);
                }

                if (!notificationPassed) {
                    event._eventPreferencesNotification._startTime = 0;
                    getDatabaseHandler().updateNotificationStartTime(event);
                }
            }
            else
                ignoreNotification = true;
        }

        if (event._eventPreferencesApplication._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignoreApplication = false;

            applicationPassed = false;

            String foregroundApplication = ForegroundApplicationChangedService.getApplicationInForeground(context);

            if (!foregroundApplication.isEmpty()) {
                String[] splits = event._eventPreferencesApplication._applications.split("\\|");
                for (String split : splits) {
                    String packageName = ApplicationsCache.getPackageName(split);

                    if (foregroundApplication.equals(packageName)) {
                        applicationPassed = true;
                        break;
                    }
                }
            }
        }

        if (event._eventPreferencesLocation._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventLocation(context, event))
        {
            ignoreLocation = false;

            locationPassed = false;

            String[] splits = event._eventPreferencesLocation._geofences.split("\\|");
            //Log.d("DataWrapper.doEventService", "geofences="+event._eventPreferencesLocation._geofences);
            for (String _geofence : splits) {
                if (!_geofence.isEmpty()) {
                    //Log.d("DataWrapper.doEventService", "geofence="+getDatabaseHandler().getGeofenceName(Long.valueOf(_geofence)));

                    int geofenceTransition = getDatabaseHandler().getGeofenceTransition(Long.valueOf(_geofence));

                    if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER) {
                        locationPassed = true;
                        break;
                    }
                }
            }
            //Log.d("DataWrapper.doEventService", "locationPassed="+locationPassed);

            if (event._eventPreferencesLocation._whenOutside)
                locationPassed = !locationPassed;
        }

        if (event._eventPreferencesOrientation._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignoreOrientation = false;

            ApplicationPreferences.getSharedPreferences(context);
            int callEventType = ApplicationPreferences.preferences.getInt(PhoneCallService.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallService.CALL_EVENT_UNDEFINED);

            if (/*Permissions.checkEventPhoneBroadcast(context, event) &&*/
                (callEventType != PhoneCallService.CALL_EVENT_UNDEFINED) &&
                (callEventType != PhoneCallService.CALL_EVENT_INCOMING_CALL_ENDED) &&
                (callEventType != PhoneCallService.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                // ignore changes during call
                ignoreOrientation = true;
            }
            else
            {
                if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isOrientationScannerStarted()) {

                    String foregroundApplication = ForegroundApplicationChangedService.getApplicationInForeground(context);
                    boolean lApplicationPassed = false;
                    if (!foregroundApplication.isEmpty()) {
                        String[] splits = event._eventPreferencesOrientation._ignoredApplications.split("\\|");
                        for (String split : splits) {
                            String packageName = ApplicationsCache.getPackageName(split);

                            if (foregroundApplication.equals(packageName)) {
                                lApplicationPassed = true;
                                break;
                            }
                        }
                    }
                    if (!lApplicationPassed) {

                        boolean enabledAccelerometer = PhoneProfilesService.getAccelerometerSensor(context) != null;
                        boolean enabledMagneticField = PhoneProfilesService.getMagneticFieldSensor(context) != null;
                        boolean enabledAll = (enabledAccelerometer) && (enabledMagneticField);
                        boolean lDisplayPassed = true;
                        if (enabledAccelerometer) {
                            if (!event._eventPreferencesOrientation._display.isEmpty()) {
                                String[] splits = event._eventPreferencesOrientation._display.split("\\|");
                                if (splits.length > 0) {
                                    lDisplayPassed = false;
                                    for (String split : splits) {
                                        try {
                                            int side = Integer.valueOf(split);
                                            if (side == PhoneProfilesService.mDisplayUp) {
                                                lDisplayPassed = true;
                                                break;
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            }
                        }

                        boolean lSidePassed = true;
                        if (enabledAll) {
                            if (!event._eventPreferencesOrientation._sides.isEmpty()) {
                                String[] splits = event._eventPreferencesOrientation._sides.split("\\|");
                                if (splits.length > 0) {
                                    lSidePassed = false;
                                    for (String split : splits) {
                                        try {
                                            int side = Integer.valueOf(split);
                                            if (side == PhoneProfilesService.DEVICE_ORIENTATION_HORIZONTAL) {
                                                if (PhoneProfilesService.mSideUp == PhoneProfilesService.mDisplayUp) {
                                                    lSidePassed = true;
                                                    break;
                                                }
                                            } else {
                                                if (side == PhoneProfilesService.mSideUp) {
                                                    lSidePassed = true;
                                                    break;
                                                }
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            }
                        }

                        boolean enabled = PhoneProfilesService.getProximitySensor(context) != null;
                        boolean lDistancePassed = true;
                        if (enabled) {
                            if (event._eventPreferencesOrientation._distance != 0) {
                                lDistancePassed = event._eventPreferencesOrientation._distance == PhoneProfilesService.mDeviceDistance;
                            }
                        }

                        //Log.d("**** DataWrapper.doEventService","lDisplayPassed="+lDisplayPassed);
                        //Log.d("**** DataWrapper.doEventService","lSidePassed="+lSidePassed);
                        //Log.d("**** DataWrapper.doEventService","lDistancePassed="+lDistancePassed);

                        orientationPassed = lDisplayPassed && lSidePassed && lDistancePassed;
                    }
                    else {
                        ignoreOrientation = true;
                    }
                }
            }
        }

        if (event._eventPreferencesMobileCells._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventLocation(context, event))
        {
            ignoreMobileCell = false;

            if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateStarted()) {

                String[] splits = event._eventPreferencesMobileCells._cells.split("\\|");
                String registeredCell = Integer.toString(PhoneProfilesService.phoneStateScanner.registeredCell);
                boolean found = false;
                for (String cell : splits) {
                    if (cell.equals(registeredCell)) {
                        found = true;
                        break;
                    }
                }
                mobileCellPassed = found;

                if (event._eventPreferencesMobileCells._whenOutside)
                    mobileCellPassed = !mobileCellPassed;
            }
            else
                ignoreMobileCell = true;
        }

        if (event._eventPreferencesNFC._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignoreNfc = false;

            // compute start time

            if (event._eventPreferencesNFC._startTime > 0) {
                int gmtOffset = TimeZone.getDefault().getRawOffset();
                long startTime = event._eventPreferencesNFC._startTime - gmtOffset;

                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String alarmTimeS = sdf.format(startTime);
                PPApplication.logE("DataWrapper.doEventService", "startTime=" + alarmTimeS);

                // compute end datetime
                long endAlarmTime = event._eventPreferencesNFC.computeAlarm();
                alarmTimeS = sdf.format(endAlarmTime);
                PPApplication.logE("DataWrapper.doEventService", "endAlarmTime=" + alarmTimeS);

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();
                alarmTimeS = sdf.format(nowAlarmTime);
                PPApplication.logE("DataWrapper.doEventService", "nowAlarmTime=" + alarmTimeS);

                if (broadcastType.equals(NFCBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
                    nfcPassed = true;
                else if (!event._eventPreferencesNFC._permanentRun) {
                    if (broadcastType.equals(NFCEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
                        nfcPassed = false;
                    else
                        nfcPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                }
                else
                    nfcPassed = nowAlarmTime >= startTime;
            }
            else
                nfcPassed = false;

            if (!nfcPassed) {
                event._eventPreferencesNFC._startTime = 0;
                getDatabaseHandler().updateNFCStartTime(event);
            }
        }

        if (event._eventPreferencesRadioSwitch._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ignoreRadioSwitch = false;

            radioSwitchPassed = true;
            boolean tested = false;

            if ((event._eventPreferencesRadioSwitch._wifi == 1 || event._eventPreferencesRadioSwitch._wifi == 2)
                    && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {

                if (!((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                        (WifiScanAlarmBroadcastReceiver.getWaitForResults(context)) ||
                        (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)))) {
                    // ignore for wifi scanning

                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int wifiState = wifiManager.getWifiState();
                    boolean enabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                    PPApplication.logE("-###- DataWrapper.doEventService", "wifiState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._wifi == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }
                else
                    ignoreRadioSwitch = true;
            }

            if ((event._eventPreferencesRadioSwitch._bluetooth == 1 || event._eventPreferencesRadioSwitch._bluetooth == 2)
                    && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {

                if (!((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getLEScanRequest(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)))) {
                    // ignore for bluetooth scanning


                    BluetoothAdapter bluetoothAdapter = BluetoothScanAlarmBroadcastReceiver.getBluetoothAdapter(context);
                    if (bluetoothAdapter != null) {
                        boolean enabled = bluetoothAdapter.isEnabled();
                        PPApplication.logE("-###- DataWrapper.doEventService", "bluetoothState=" + enabled);
                        tested = true;
                        if (event._eventPreferencesRadioSwitch._bluetooth == 1)
                            radioSwitchPassed = radioSwitchPassed && enabled;
                        else
                            radioSwitchPassed = radioSwitchPassed && !enabled;
                    }
                }
                else
                    ignoreRadioSwitch = true;
            }

            if ((event._eventPreferencesRadioSwitch._mobileData == 1 || event._eventPreferencesRadioSwitch._mobileData == 2)
                    && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {

                ignoreRadioSwitch = false;

                boolean enabled = ActivateProfileHelper.isMobileData(context);
                PPApplication.logE("-###- DataWrapper.doEventService", "mobileDataState=" + enabled);
                tested = true;
                if (event._eventPreferencesRadioSwitch._mobileData == 1)
                    radioSwitchPassed = radioSwitchPassed && enabled;
                else
                    radioSwitchPassed = radioSwitchPassed && !enabled;
            }

            if ((event._eventPreferencesRadioSwitch._gps == 1 || event._eventPreferencesRadioSwitch._gps == 2)
                    && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {

                ignoreRadioSwitch = false;

                boolean enabled;
                if (android.os.Build.VERSION.SDK_INT < 19)
                    enabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                else {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                }
                PPApplication.logE("-###- DataWrapper.doEventService", "gpsState=" + enabled);
                tested = true;
                if (event._eventPreferencesRadioSwitch._gps == 1)
                    radioSwitchPassed = radioSwitchPassed && enabled;
                else
                    radioSwitchPassed = radioSwitchPassed && !enabled;
            }

            if ((event._eventPreferencesRadioSwitch._nfc == 1 || event._eventPreferencesRadioSwitch._nfc == 2)
                    && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {

                ignoreRadioSwitch = false;

                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
                if (nfcAdapter != null) {
                    boolean enabled = nfcAdapter.isEnabled();
                    PPApplication.logE("-###- DataWrapper.doEventService", "nfcState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._nfc == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }
            }

            if (event._eventPreferencesRadioSwitch._airplaneMode == 1 || event._eventPreferencesRadioSwitch._airplaneMode == 2) {

                ignoreRadioSwitch = false;

                boolean enabled = ActivateProfileHelper.isAirplaneMode(context);
                PPApplication.logE("-###- DataWrapper.doEventService", "airplanModeState=" + enabled);
                tested = true;
                if (event._eventPreferencesRadioSwitch._airplaneMode == 1)
                    radioSwitchPassed = radioSwitchPassed && enabled;
                else
                    radioSwitchPassed = radioSwitchPassed && !enabled;
            }

            radioSwitchPassed = radioSwitchPassed && tested;
        }

        PPApplication.logE("DataWrapper.doEventService","timePassed="+timePassed);
        PPApplication.logE("DataWrapper.doEventService","batteryPassed="+batteryPassed);
        PPApplication.logE("DataWrapper.doEventService","callPassed="+callPassed);
        PPApplication.logE("DataWrapper.doEventService","peripheralPassed="+peripheralPassed);
        PPApplication.logE("DataWrapper.doEventService","calendarPassed="+calendarPassed);
        PPApplication.logE("DataWrapper.doEventService","wifiPassed="+wifiPassed);
        PPApplication.logE("DataWrapper.doEventService","screenPassed="+screenPassed);
        PPApplication.logE("DataWrapper.doEventService","bluetoothPassed="+bluetoothPassed);
        PPApplication.logE("DataWrapper.doEventService","smsPassed="+smsPassed);
        PPApplication.logE("DataWrapper.doEventService","notificationPassed="+notificationPassed);
        PPApplication.logE("DataWrapper.doEventService","applicationPassed="+applicationPassed);
        PPApplication.logE("DataWrapper.doEventService","locationPassed="+locationPassed);
        PPApplication.logE("DataWrapper.doEventService","orientationPassed="+orientationPassed);
        PPApplication.logE("DataWrapper.doEventService","mobileCellPassed="+mobileCellPassed);
        PPApplication.logE("DataWrapper.doEventService","nfcPassed="+nfcPassed);
        PPApplication.logE("DataWrapper.doEventService","radioSwitchPassed="+radioSwitchPassed);

        PPApplication.logE("DataWrapper.doEventService","ignoreTime="+ignoreTime);
        PPApplication.logE("DataWrapper.doEventService","ignoreBattery="+ignoreBattery);
        PPApplication.logE("DataWrapper.doEventService","ignoreCall="+ignoreCall);
        PPApplication.logE("DataWrapper.doEventService","ignorePeripheral="+ignorePeripheral);
        PPApplication.logE("DataWrapper.doEventService","ignoreCalendar="+ignoreCalendar);
        PPApplication.logE("DataWrapper.doEventService","ignoreWifi="+ignoreWifi);
        PPApplication.logE("DataWrapper.doEventService","ignoreScreen="+ignoreScreen);
        PPApplication.logE("DataWrapper.doEventService","ignoreBluetooth="+ignoreBluetooth);
        PPApplication.logE("DataWrapper.doEventService","ignoreSms="+ignoreSms);
        PPApplication.logE("DataWrapper.doEventService","ignoreNotification="+ignoreNotification);
        PPApplication.logE("DataWrapper.doEventService","ignoreApplication="+ignoreApplication);
        PPApplication.logE("DataWrapper.doEventService","ignoreLocation="+ignoreLocation);
        PPApplication.logE("DataWrapper.doEventService","ignoreOrientation="+ignoreOrientation);
        PPApplication.logE("DataWrapper.doEventService","ignoreMobileCell="+ignoreMobileCell);
        PPApplication.logE("DataWrapper.doEventService","ignoreNfc="+ignoreNfc);
        PPApplication.logE("DataWrapper.doEventService","ignoreEadioSwitch="+ignoreRadioSwitch);

        //PPApplication.logE("DataWrapper.doEventService","eventStart="+eventStart);
        PPApplication.logE("DataWrapper.doEventService","restartEvent="+restartEvent);
        PPApplication.logE("DataWrapper.doEventService","statePause="+statePause);

        List<EventTimeline> eventTimelineList = getEventTimelineList();

        if (!(ignoreTime &&
              ignoreBattery &&
              ignoreCall &&
              ignorePeripheral &&
              ignoreCalendar &&
              ignoreWifi &&
              ignoreScreen &&
              ignoreBluetooth &&
              ignoreSms &&
              ignoreNotification &&
              ignoreApplication &&
              ignoreLocation &&
              ignoreOrientation &&
              ignoreMobileCell &&
              ignoreNfc &&
              ignoreRadioSwitch)) {
            // if some sensor is not ignored, do event start/apuse

            if (timePassed &&
                batteryPassed &&
                callPassed &&
                peripheralPassed &&
                calendarPassed &&
                wifiPassed &&
                screenPassed &&
                bluetoothPassed &&
                smsPassed &&
                notificationPassed &&
                applicationPassed &&
                locationPassed &&
                orientationPassed &&
                mobileCellPassed &&
                nfcPassed &&
                radioSwitchPassed) {
                // podmienky sedia, vykoname, co treba

                //if (eventStart)
                newEventStatus = Event.ESTATUS_RUNNING;
                //else
                //    newEventStatus = Event.ESTATUS_PAUSE;

            } else
                newEventStatus = Event.ESTATUS_PAUSE;

            PPApplication.logE("[***] DataWrapper.doEventService", "event.getStatus()=" + event.getStatus());
            PPApplication.logE("[***] DataWrapper.doEventService", "newEventStatus=" + newEventStatus);

            //PPApplication.logE("@@@ DataWrapper.doEventService","restartEvent="+restartEvent);

            if ((event.getStatus() != newEventStatus) || restartEvent || event._isInDelayStart || event._isInDelayEnd) {
                PPApplication.logE("[***] DataWrapper.doEventService", " do new event status");

                if ((newEventStatus == Event.ESTATUS_RUNNING) && (!statePause)) {
                    PPApplication.logE("[***] DataWrapper.doEventService", "start event");
                    PPApplication.logE("[***] DataWrapper.doEventService", "event._name=" + event._name);

                    if (event._isInDelayEnd)
                        event.removeDelayEndAlarm(this);
                    else {
                        if (!forDelayStartAlarm) {
                            // called not for delay alarm
                            if (restartEvent) {
                                event._isInDelayStart = false;
                            } else {
                                if (!event._isInDelayStart) {
                                    // if not delay alarm is set, set it
                                    event.setDelayStartAlarm(this); // for start delay
                                }
                                if (event._isInDelayStart) {
                                    // if delay timeouted, start event
                                    event.checkDelayStart(/*this*/);
                                }
                            }
                            PPApplication.logE("[***] DataWrapper.doEventService", "event._isInDelayStart=" + event._isInDelayStart);
                            if (!event._isInDelayStart) {
                                // no delay alarm is set
                                // start event
                                event.startEvent(this, eventTimelineList, false, interactive, reactivate, true, mergedProfile);
                                PPApplication.logE("[***] DataWrapper.doEventService", "mergedProfile._id=" + mergedProfile._id);
                            }
                        }

                        if (forDelayStartAlarm && event._isInDelayStart) {
                            // called for delay alarm
                            // start event
                            event.startEvent(this, eventTimelineList, false, interactive, reactivate, true, mergedProfile);
                        }
                    }
                } else if (((newEventStatus == Event.ESTATUS_PAUSE) || restartEvent) && statePause) {
                    // when pausing and it is for restart events, force pause

                    PPApplication.logE("[***] DataWrapper.doEventService", "pause event");
                    PPApplication.logE("[***] DataWrapper.doEventService", "event._name=" + event._name);

                    if (event._isInDelayStart)
                        event.removeDelayStartAlarm(this);
                    else {
                        if (!forDelayEndAlarm) {
                            // called not for delay alarm
                            if (restartEvent) {
                                event._isInDelayEnd = false;
                            } else {
                                if (!event._isInDelayEnd) {
                                    // if not delay alarm is set, set it
                                    event.setDelayEndAlarm(this); // for end delay
                                }
                                if (event._isInDelayEnd) {
                                    // if delay timeouted, pause event
                                    event.checkDelayEnd(/*this*/);
                                }
                            }
                            if (!event._isInDelayEnd) {
                                // no delay alarm is set
                                // pause event
                                event.pauseEvent(this, eventTimelineList, true, false, false, true, mergedProfile, !restartEvent);
                            }
                        }

                        if (forDelayEndAlarm && event._isInDelayEnd) {
                            // called for delay alarm
                            // pause event
                            event.pauseEvent(this, eventTimelineList, true, false, false, true, mergedProfile, !restartEvent);
                        }
                    }
                }
            }
        }

        PPApplication.logE("%%% DataWrapper.doEventService","--- end --------------------------");
    }

    public void restartEvents(boolean unblockEventsRun, boolean keepActivatedProfile, boolean interactive)
    {
        if (!Event.getGlobalEventsRuning(context))
            // events are globally stopped
            return;

        PPApplication.logE("$$$ restartEvents", "in DataWrapper.restartEvents");

        if (Event.getEventsBlocked(context) && (!unblockEventsRun)) {

            Intent intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);
            context.sendBroadcast(intent);

            return;
        }

        PPApplication.logE("DataWrapper.restartEvents", "events are not blocked");

        //Profile activatedProfile = getActivatedProfile();

        if (unblockEventsRun)
        {
            PPApplication.logE("$$$ setEventsBlocked", "DataWrapper.restartEvents, false");
            Event.setEventsBlocked(context, false);
            for (Event event : getEventList())
            {
                if (event != null)
                    event._blocked = false;
            }
            getDatabaseHandler().unblockAllEvents();
            Event.setForceRunEventRunning(context, false);
        }

        if (!keepActivatedProfile) {
            getDatabaseHandler().deactivateProfile();
            setProfileActive(null);
        }

        //Intent intent = new Intent();
        //intent.setAction(RestartEventsBroadcastReceiver.INTENT_RESTART_EVENTS);
        Intent intent = new Intent(context, RestartEventsBroadcastReceiver.class);
        intent.putExtra(EXTRA_UNBLOCKEVENTSRUN, false);
        intent.putExtra(EXTRA_INTERACTIVE, interactive);
        context.sendBroadcast(intent);

    }

    void restartEventsWithRescan(boolean showToast, boolean interactive)
    {
        PPApplication.logE("$$$ DataWrapper.restartEventsWithRescan","xxx");

        // remove all event delay alarms
        resetAllEventsInDelayStart(false);
        resetAllEventsInDelayEnd(false);
        // ignoruj manualnu aktivaciu profilu
        // a odblokuj forceRun eventy
        restartEvents(true, false, interactive);

        if (ApplicationPreferences.applicationEventWifiRescan(context).equals(PPApplication.RESCAN_TYPE_RESTART_EVENTS) ||
                ApplicationPreferences.applicationEventWifiRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS))
        {
            if (getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0) {
                // rescan wifi
                PPApplication.logE("$$$ DataWrapper.restartEventsWithRescan","start of wifi scanner");
                WifiScanAlarmBroadcastReceiver.setAlarm(context, true, false, false);
            }
        }
        if (ApplicationPreferences.applicationEventBluetoothRescan(context).equals(PPApplication.RESCAN_TYPE_RESTART_EVENTS) ||
                ApplicationPreferences.applicationEventBluetoothRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS))
        {
            if (getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0) {
                // rescan bluetooth
                PPApplication.logE("$$$ DataWrapper.restartEventsWithRescan","start of bluetooth scanner");
                BluetoothScanAlarmBroadcastReceiver.setAlarm(context, true, false);
            }
        }
        if (ApplicationPreferences.applicationEventLocationRescan(context).equals(PPApplication.RESCAN_TYPE_RESTART_EVENTS) ||
                ApplicationPreferences.applicationEventLocationRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS))
        {
            if (getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
                // send broadcast for location scan
                PPApplication.logE("$$$ DataWrapper.restartEventsWithRescan","start of location scanner");
                GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, true, false);
            }
        }
        if (ApplicationPreferences.applicationEventMobileCellsRescan(context).equals(PPApplication.RESCAN_TYPE_RESTART_EVENTS) ||
                ApplicationPreferences.applicationEventMobileCellsRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS))
        {
            if (getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0)
                // rescan mobile cells
                if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateStarted()) {
                    PPApplication.logE("$$$ DataWrapper.restartEventsWithRescan","start of mobile cells scanner");
                    PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                }
        }


        if (showToast)
        {
            Toast msg = Toast.makeText(context,
                    context.getResources().getString(R.string.toast_events_restarted),
                    Toast.LENGTH_SHORT);
            msg.show();
        }
    }

    void restartEventsWithAlert(Activity activity)
    {
        if (!Event.getGlobalEventsRuning(context))
            // events are globally stopped
            return;

        /*
        if (!PPApplication.getEventsBlocked(context))
            return;
        */

        PPApplication.logE("$$$ restartEvents", "in DataWrapper.restartEventsWithAlert");

        if (ApplicationPreferences.applicationActivateWithAlert(context) || (activity instanceof EditorProfilesActivity))
        {
            final Activity _activity = activity;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.restart_events_alert_title);
            dialogBuilder.setMessage(R.string.restart_events_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PPApplication.logE("$$$ restartEvents", "from DataWrapper.restartEventsWithAlert");
                    restartEventsWithRescan(true, true);

                    boolean finish;
                    if (_activity instanceof ActivateProfileActivity)
                        finish = ApplicationPreferences.applicationClose(context);
                    else
                    if (_activity instanceof RestartEventsFromNotificationActivity)
                        finish = true;
                    else
                        finish = false;
                    if (finish)
                        _activity.finish();
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    boolean finish = (!(_activity instanceof ActivateProfileActivity)) &&
                                     (!(_activity instanceof EditorProfilesActivity));
                    if (finish)
                        _activity.finish();
                }
            });
            dialogBuilder.show();
        }
        else
        {
            PPApplication.logE("$$$ restartEvents", "from DataWrapper.restartEventsWithAlert");
            restartEventsWithRescan(true, true);

            boolean finish;
            if (activity instanceof ActivateProfileActivity)
                finish = ApplicationPreferences.applicationClose(context);
            else
            if (activity instanceof RestartEventsFromNotificationActivity)
                finish = true;
            else
                finish = false;
            if (finish)
                activity.finish();
        }
    }

    @SuppressLint("NewApi")
    void restartEventsWithDelay(int delay, boolean unblockEventsRun, boolean interactive)
    {
        PPApplication.logE("$$$ restartEvents","in DataWrapper.restartEventsWithDelay");

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RestartEventsBroadcastReceiver.class);
        intent.putExtra(EXTRA_UNBLOCKEVENTSRUN, unblockEventsRun);
        intent.putExtra(EXTRA_INTERACTIVE, interactive);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, delay);
        long alarmTime = calendar.getTimeInMillis();

        //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
        //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (android.os.Build.VERSION.SDK_INT >= 23)
            //alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
        else
        if (android.os.Build.VERSION.SDK_INT >= 19)
            //alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
        else
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

    void setEventBlocked(Event event, boolean blocked)
    {
        event._blocked = blocked;
        getDatabaseHandler().updateEventBlocked(event);
    }

    // returns true if:
    // 1. events are blocked = any profile is activated manually
    // 2. no any forceRun event is running
    boolean getIsManualProfileActivation()
    {
        if (!Event.getEventsBlocked(context))
            return false;
        else
            return !Event.getForceRunEventRunning(context);
    }

    private String getProfileNameWithManualIndicator(Profile profile, List<EventTimeline> eventTimelineList, boolean addIndicators, boolean addDuration, boolean multyline, Context context)
    {
        if (profile == null)
            return "";

        String name;
        if (addDuration)
            name = profile.getProfileNameWithDuration(multyline, context);
        else
            name = profile._name;

        if (Event.getEventsBlocked(context))
        {
            if (addIndicators)
            {
                if (Event.getForceRunEventRunning(context))
                {
                    name = "[\u00BB] " + name;
                }
                else
                {
                    name = "[M] " + name;
                }
            }
        }

        if (addIndicators)
        {
            String eventName = getLastStartedEventName(eventTimelineList);
            if (!eventName.isEmpty())
                name = name + " [" + eventName + "]";
        }

        return name;
    }

    public String getProfileNameWithManualIndicator(Profile profile, boolean addIndicators, boolean addDuration, boolean multyline) {
        List<EventTimeline> eventTimelineList = getEventTimelineList();

        return getProfileNameWithManualIndicator(profile, eventTimelineList, addIndicators, addDuration, multyline, context);
    }

    /*
    public String getProfileNameWithManualIndicator(Profile profile, boolean addIndicators) {
        List<EventTimeline> eventTimelineList = getEventTimelineList();

        boolean addDuration = (PPApplication.getActivatedProfileForDuration(context) != 0);

        return getProfileNameWithManualIndicator(profile, eventTimelineList, addIndicators, addDuration);
    }
    */

    private String getLastStartedEventName(List<EventTimeline> eventTimelineList)
    {

        if (Event.getGlobalEventsRuning(context) && PPApplication.getApplicationStarted(context, false))
        {
            if (eventTimelineList.size() > 0)
            {
                EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size()-1);
                long event_id = eventTimeLine._fkEvent;
                Event event = getEventById(event_id);
                if (event != null)
                {
                    if ((!Event.getEventsBlocked(context)) || (event._forceRun))
                    {
                        Profile profile = getActivatedProfile();
                        if ((profile != null) && (event._fkProfileStart == profile._id))
                            // last started event activatees activated profile
                            return event._name;
                        else
                            return "";
                    }
                    else
                        return "";
                }
                else
                    return "";
            }
            else
            {
                long profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
                if ((!Event.getEventsBlocked(context)) && (profileId != Profile.PROFILE_NO_ACTIVATE))
                {
                    Profile profile = getActivatedProfile();
                    if ((profile != null) && (profile._id == profileId))
                        return context.getString(R.string.event_name_background_profile);
                    else
                        return "";
                }
                else
                    return "";
            }
        }
        else
            return "";
    }

    private void resetAllEventsInDelayStart(boolean onlyFromDb)
    {
        if (!onlyFromDb) {
            for (Event event : getEventList()) {
                event.removeDelayStartAlarm(this);
                event.removeDelayStartAlarm(this);
            }
        }
        getDatabaseHandler().resetAllEventsInDelayStart();
    }

    private void resetAllEventsInDelayEnd(boolean onlyFromDb)
    {
        if (!onlyFromDb) {
            for (Event event : getEventList()) {
                event.removeDelayEndAlarm(this);
                event.removeDelayEndAlarm(this);
            }
        }
        getDatabaseHandler().resetAllEventsInDelayStart();
    }

    public static boolean isPowerSaveMode(/*Context context*/) {
        // Internal Power save mode
        if (Build.VERSION.SDK_INT < 21) {
            return PPApplication.isPowerSaveMode;
            /*else {
                boolean isCharging;
                int batteryPct;

                // get battery status
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = context.registerReceiver(null, ifilter);

                if (batteryStatus != null) {
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    batteryPct = Math.round(level / (float) scale * 100);

                    if ((!isCharging) &&
                            ((PPApplication.applicationPowerSaveModeInternal.equals("1") && (batteryPct <= 5)) ||
                                    (PPApplication.applicationPowerSaveModeInternal.equals("2") && (batteryPct <= 15)))) {
                        return true;
                    }
                }
            }*/
        }
        else {
            //PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //return powerManager.isPowerSaveMode();
            return PPApplication.isPowerSaveMode;
        }
        //return false;
    }

    public void addActivityLog(int logType, String eventName, String profileName, String profileIcon,
                               int durationDelay) {
        if (PPApplication.getActivityLogEnabled(context)) {
            //if (ApplicationPreferences.preferences == null)
            //    ApplicationPreferences.preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            //ApplicationPreferences.setApplicationDeleteOldActivityLogs(context, Integer.valueOf(preferences.getString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7")));
            getDatabaseHandler().addActivityLog(ApplicationPreferences.applicationDeleteOldActivityLogs(context),
                                    logType, eventName, profileName, profileIcon, durationDelay);
        }
    }

    void runStopEvents() {
        if (Event.getGlobalEventsRuning(context))
        {
            //noinspection ConstantConditions
            addActivityLog(DatabaseHandler.ALTYPE_RUNEVENTS_DISABLE, null, null, null, 0);

            // no setup for next start
            resetAllEventsInDelayStart(false);
            resetAllEventsInDelayEnd(false);
            // no set system events, unblock all events, no activate return profile
            pauseAllEvents(true, false/*, false*/);
            Event.setGlobalEventsRuning(context, false);
            // stop Wifi scanner
            WifiScanAlarmBroadcastReceiver.initialize(context);
            WifiScanAlarmBroadcastReceiver.removeAlarm(context/*, false*/);
            // stop bluetooth scanner
            BluetoothScanAlarmBroadcastReceiver.initialize(context);
            BluetoothScanAlarmBroadcastReceiver.removeAlarm(context/*, false*/);
            // stop geofences scanner
            GeofenceScannerAlarmBroadcastReceiver.removeAlarm(context/*, false*/);
            if (PhoneProfilesService.instance != null) {
                PPApplication.stopGeofenceScanner(context);
                PPApplication.stopOrientationScanner(context);
                // no stop mobile cells scanner, must run for last connection time
                //PPApplication.stopPhoneStateScanner(getApplicationContext());
            }
        }
        else
        {
            //noinspection ConstantConditions
            addActivityLog(DatabaseHandler.ALTYPE_RUNEVENTS_ENABLE, null, null, null, 0);

            Event.setGlobalEventsRuning(context, true);

            if (PhoneProfilesService.instance != null) {
                PPApplication.startGeofenceScanner(context);
                PPApplication.startOrientationScanner(context);
                PPApplication.startPhoneStateScanner(context);
            }

            // setup for next start
            firstStartEvents(false);
        }
    }
}
