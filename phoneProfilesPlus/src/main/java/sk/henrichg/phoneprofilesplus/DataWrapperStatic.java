package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class DataWrapperStatic {

    static Profile getNonInitializedProfile(String name, String icon, int order)
    {
        //noinspection ConstantConditions
        return new Profile(
                name,
                icon + Profile.defaultValuesString.get("prf_pref_profileIcon_withoutIcon"),
                false,
                order,
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_RINGER_MODE)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_RINGTONE),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_NOTIFICATION),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_MEDIA),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ALARM),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_SYSTEM),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_VOICE),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE)),
                Settings.System.DEFAULT_RINGTONE_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE)),
                Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE)),
                Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BLUETOOTH)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT)),
                Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + Profile.defaultValuesString.get("prf_pref_deviceBrightness_withoutLevel"),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_GPS)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AUTOSYNC)),
                Profile.defaultValuesBoolean.get("prf_pref_showInActivator_notShow"),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AUTOROTATE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NFC)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION)),
                Profile.AFTER_DURATION_DO_RESTART_EVENTS,
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ZEN_MODE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_KEYGUARD)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI_AP)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE)),
                Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_ASK_FOR_DURATION),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_NOTIFICATION_LED)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR)),
                Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_LOCK_DEVICE)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND),
                Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME),
                0,
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_DARK_MODE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ON_TOUCH)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_DTMF),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO),
                Long.parseLong(Profile.defaultValuesString.get(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT)),
                Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_NOTIFICATION_SCANNING)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_GENERATE_NOTIFICATION),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_CAMERA_FLASH)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1)),
                Settings.System.DEFAULT_RINGTONE_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2)),
                Settings.System.DEFAULT_RINGTONE_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1)),
                Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2)),
                Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_LIVE_WALLPAPER),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME)),
                Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_PERIODIC_SCANNING)),
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_VPN)
        );
    }

    static Event getNonInitializedEvent(String name, int startOrder)
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
                Event.EATENDDO_RESTART_EVENTS,
                false,
                "",
                0,
                false,
                0,
                0,
                false,
                false,
                false,
                15,
                "",
                false,
                //0*
                false,
                false,
                false
        );
    }

    // returns true if:
    // 1. events are blocked = any profile is activated manually
    // 2. no any forceRun event is running
    static boolean getIsManualProfileActivation(boolean afterDuration, Context context)
    {
        if (afterDuration)
            //return ApplicationPreferences.prefEventsBlocked;
            return Event.getEventsBlocked(context);
        else {
            //if (!ApplicationPreferences.prefEventsBlocked)
            if (!Event.getEventsBlocked(context))
                return false;
            else
                return !Event.getForceRunEventRunning(context);
        }
    }

    static private Spannable _getProfileNameWithManualIndicator(
            Profile profile, boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
            boolean durationInNextLine, DataWrapper dataWrapper)
    {
        if (profile == null)
            return new SpannableString("");

        String eventName = "";
        String manualIndicators = "";
        if (addEventName)
        {
            if (Event.getGlobalEventsRunning()) {
                if (Event.getEventsBlocked(dataWrapper.context)) {
                    if (Event.getForceRunEventRunning(dataWrapper.context))
                        manualIndicators = "[»]";
                    else
                        manualIndicators = "[M]";
                }
            }
            else
                manualIndicators = "[M]";

            String _eventName = getLastStartedEventName(dataWrapper, profile);
            if (!_eventName.equals("?"))
                eventName = "[" + _eventName + "]";

            if (!manualIndicators.isEmpty())
                eventName = manualIndicators + " " + eventName;
        }

        if (!PPApplication.getApplicationStarted(true))
            eventName = eventName + " ";

        Spannable sName;
        if (addDuration) {
            if (!addEventName || manualIndicators.equals("[M]"))
                sName = profile.getProfileNameWithDuration(eventName, indicators, multiLine, durationInNextLine, dataWrapper.context);
            else {
                String name = profile._name;
                if (!eventName.isEmpty())
                    name = name + " " + eventName;
                if (!indicators.isEmpty()) {
                    if (multiLine)
                        name = name + "\n" + indicators;
                    else
                        name = name + " " + indicators;
                }
                sName = new SpannableString(name);
            }
        }
        else {
            String name = profile._name;
            if (!eventName.isEmpty())
                name = name + " " + eventName;
            if (!indicators.isEmpty()) {
                if (multiLine)
                    name = name + "\n" + indicators;
                else
                    name = name + " " + indicators;
            }
            sName = new SpannableString(name);
        }

        return sName;
    }

    static Spannable getProfileNameWithManualIndicator(
            Profile profile, boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
            boolean durationInNextLine, DataWrapper dataWrapper) {
        if (dataWrapper != null) {
            return _getProfileNameWithManualIndicator(profile, addEventName, indicators, addDuration, multiLine, durationInNextLine, dataWrapper);
        }
        else {
            return _getProfileNameWithManualIndicator(profile, false, indicators, addDuration, multiLine, durationInNextLine, null);
        }
    }

    @SuppressWarnings("SameParameterValue")
    static String getProfileNameWithManualIndicatorAsString(
            Profile profile, boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
            boolean durationInNextLine, DataWrapper dataWrapper) {
        Spannable sProfileName = getProfileNameWithManualIndicator(profile, addEventName, indicators, addDuration, multiLine, durationInNextLine, dataWrapper);
        Spannable sbt = new SpannableString(sProfileName);
        Object[] spansToRemove = sbt.getSpans(0, sProfileName.length(), Object.class);
        for (Object span : spansToRemove) {
            if (span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        return sbt.toString();
    }

    static private String getLastStartedEventName(DataWrapper dataWrapper, Profile forProfile)
    {
        if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(false))
        {
            synchronized (dataWrapper.eventTimelines) {
                if (dataWrapper.eventListFilled && dataWrapper.eventTimelineListFilled) {
                    List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);
                    if (eventTimelineList.size() > 0) {
                        EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size() - 1);
                        long event_id = eventTimeLine._fkEvent;
                        Event event = dataWrapper.getEventById(event_id);
                        if (event != null) {
                            //if ((!ApplicationPreferences.prefEventsBlocked) || (event._forceRun))
                            if ((!Event.getEventsBlocked(dataWrapper.context)) || (event._ignoreManualActivation)) {
                                //Profile profile;
                                //profile = dataWrapper.getActivatedProfile(false, false);
                                //if ((profile != null) && (event._fkProfileStart == profile._id))
                                // last started event activates activated profile
                                return event._name;
                                //else
                                //    return "?";
                            } else
                                return "?";
                        } else
                            return "?";
                    } else {
                        long profileId = ApplicationPreferences.applicationDefaultProfile;
                        //if ((!ApplicationPreferences.prefEventsBlocked) &&
                        if ((!Event.getEventsBlocked(dataWrapper.context)) &&
                                (profileId != Profile.PROFILE_NO_ACTIVATE) &&
                                (profileId == forProfile._id)) {
                            //Profile profile;
                            //profile = dataWrapper.getActivatedProfile(false, false);
                            //if ((profile != null) && (profile._id == profileId))
                            return dataWrapper.context.getString(R.string.event_name_background_profile);
                            //else
                            //    return "?";
                        } else
                            return "?";
                    }
                } else {
                    String eventName = DatabaseHandler.getInstance(dataWrapper.context).getLastStartedEventName();
                    if (!eventName.equals("?")) {
                        return eventName;
                    }
                    /*
                    List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(true);
                    if (eventTimelineList.size() > 0)
                    {
                        EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size()-1);
                        long event_id = eventTimeLine._fkEvent;
                        Event event = dataWrapper.getEventById(event_id);
                        if (event != null)
                        {
                            if ((!ApplicationPreferences.prefEventsBlocked) || (event._forceRun))
                            {
                                //Profile profile;
                                //profile = dataWrapper.getActivatedProfileFromDB(false, false);
                                //if ((profile != null) && (event._fkProfileStart == profile._id))
                                    // last started event activates activated profile
                                    return event._name;
                                //else
                                //    return "?";
                            }
                            else
                                return "?";
                        }
                        else
                            return "?";
                    }*/
                    else {
                        long profileId = ApplicationPreferences.applicationDefaultProfile;
                        //if ((!ApplicationPreferences.prefEventsBlocked) &&
                        if ((!Event.getEventsBlocked(dataWrapper.context)) &&
                                (profileId != Profile.PROFILE_NO_ACTIVATE) &&
                                (profileId == forProfile._id)) {
                            //Profile profile;
                            //profile = dataWrapper.getActivatedProfileFromDB(false, false);
                            //if ((profile != null) && (profile._id == profileId))
                            return dataWrapper.context.getString(R.string.event_name_background_profile);
                            //else
                            //    return "?";
                        } else
                            return "?";
                    }
                }
            }
        }
        else
            return "?";
    }
/*
    static String _getLastStartedEventName(DataWrapper dataWrapper, Profile forProfile)
    {

        if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(false))
        {
            if (dataWrapper.eventListFilled && dataWrapper.eventTimelineListFilled) {
                List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);
                if (eventTimelineList.size() > 0)
                {

                    EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size()-1);
                    long event_id = eventTimeLine._fkEvent;
                    Event event = dataWrapper.getEventById(event_id);
                    if (event != null)
                    {

                        //if ((!ApplicationPreferences.prefEventsBlocked) || (event._forceRun))
                        if ((!Event.getEventsBlocked(dataWrapper.context)) || (event._ignoreManualActivation))
                        {

                            //Profile profile;
                            //profile = dataWrapper.getActivatedProfile(false, false);
                            //if ((profile != null) && (event._fkProfileStart == profile._id))
                            // last started event activates activated profile
                            return event._name;
                            //else
                            //    return "?";
                        }
                        else {
                            return "?";
                        }
                    }
                    else {
                        return "?";
                    }
                }
                else
                {
                    long profileId = ApplicationPreferences.applicationDefaultProfile;
                    //if ((!ApplicationPreferences.prefEventsBlocked) &&
                    if ((!Event.getEventsBlocked(dataWrapper.context)) &&
                            (profileId != Profile.PROFILE_NO_ACTIVATE) &&
                            (profileId == forProfile._id))
                    {
                        //Profile profile;
                        //profile = dataWrapper.getActivatedProfile(false, false);
                        //if ((profile != null) && (profile._id == profileId))
                        return dataWrapper.context.getString(R.string.event_name_background_profile);
                        //else
                        //    return "?";
                    }
                    else {
                        return "?";
                    }
                }
            }
            else {
                String eventName = DatabaseHandler.getInstance(dataWrapper.context).getLastStartedEventName();
                if (!eventName.equals("?")) {
                    return eventName;
                }
//                List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(true);
//                if (eventTimelineList.size() > 0)
//                {
//                    EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size()-1);
//                    long event_id = eventTimeLine._fkEvent;
//                    Event event = dataWrapper.getEventById(event_id);
//                    if (event != null)
//                    {
//                        if ((!ApplicationPreferences.prefEventsBlocked) || (event._forceRun))
//                        {
//                            //Profile profile;
//                            //profile = dataWrapper.getActivatedProfileFromDB(false, false);
//                            //if ((profile != null) && (event._fkProfileStart == profile._id))
//                                // last started event activates activated profile
//                                return event._name;
//                            //else
//                            //    return "?";
//                        }
//                        else
//                            return "?";
//                    }
//                    else
//                        return "?";
//                }
                else
                {
                    long profileId = ApplicationPreferences.applicationDefaultProfile;
                    //if ((!ApplicationPreferences.prefEventsBlocked) &&
                    if ((!Event.getEventsBlocked(dataWrapper.context)) &&
                            (profileId != Profile.PROFILE_NO_ACTIVATE) &&
                            (profileId == forProfile._id))
                    {
                        //Profile profile;
                        //profile = dataWrapper.getActivatedProfileFromDB(false, false);
                        //if ((profile != null) && (profile._id == profileId))
                        return dataWrapper.context.getString(R.string.event_name_background_profile);
                        //else
                        //    return "?";
                    }
                    else {
                        return "?";
                    }
                }
            }

        }
        else {
            return "?";
        }
    }
*/


    @TargetApi(Build.VERSION_CODES.N_MR1)
    static private ShortcutInfo createShortcutInfo(Profile profile, boolean restartEvents, Context context) {
        boolean isIconResourceID;
        String iconIdentifier;
        Bitmap profileBitmap;
        boolean useCustomColor;

        Intent shortcutIntent;

        isIconResourceID = profile.getIsIconResourceID();
        iconIdentifier = profile.getIconIdentifier();
        useCustomColor = profile.getUseCustomColorForIcon();

        if (isIconResourceID) {
            Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(context, profile._iconBitmap);
            if (bitmap != null)
                profileBitmap = bitmap;
            else {
                if (profile._iconBitmap != null)
                    profileBitmap = profile._iconBitmap;
                else {
                    //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                    int iconResource = ProfileStatic.getIconResource(iconIdentifier);
                    //profileBitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
                    profileBitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
                }
            }
        } else {
            int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            //Log.d("---- ShortcutCreatorListFragment.generateIconBitmap","resampleBitmapUri");
            Bitmap oringBitmap = BitmapManipulator.resampleBitmapUri(iconIdentifier, width, height, true, false, context.getApplicationContext());
            Bitmap bitmap = profile.increaseProfileIconBrightnessForContext(context, oringBitmap);
            if (bitmap != null)
                profileBitmap = bitmap;
            else {
                if (oringBitmap != null)
                    profileBitmap = oringBitmap;
                else {
                    int iconResource = R.drawable.ic_profile_default;
                    //profileBitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
                    profileBitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
                }
            }
        }

        if (ApplicationPreferences.applicationShortcutIconColor.equals("1")) {
            if (isIconResourceID || useCustomColor) {
                // icon is from resource or colored by custom color
                int monochromeValue = 0xFF;
                String applicationWidgetIconLightness = ApplicationPreferences.applicationShortcutIconLightness;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) monochromeValue = 0x00;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12)) monochromeValue = 0x20;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25)) monochromeValue = 0x40;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37)) monochromeValue = 0x60;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50)) monochromeValue = 0x80;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62)) monochromeValue = 0xA0;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75)) monochromeValue = 0xC0;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87)) monochromeValue = 0xE0;
                //if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100)) monochromeValue = 0xFF;
                profileBitmap = BitmapManipulator.monochromeBitmap(profileBitmap, monochromeValue/*, getActivity().getBaseContext()*/);
            } else {
                float monochromeValue = 255f;
                String applicationWidgetIconLightness = ApplicationPreferences.applicationShortcutIconLightness;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0)) monochromeValue = -255f;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12)) monochromeValue = -192f;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25)) monochromeValue = -128f;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37)) monochromeValue = -64f;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50)) monochromeValue = 0f;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62)) monochromeValue = 64f;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75)) monochromeValue = 128f;
                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87)) monochromeValue = 192f;
                //if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100)) monochromeValue = 255f;
                profileBitmap = BitmapManipulator.grayScaleBitmap(profileBitmap);
                if (ApplicationPreferences.applicationShortcutCustomIconLightness)
                    profileBitmap = BitmapManipulator.setBitmapBrightness(profileBitmap, monochromeValue);
            }
        }

        if (restartEvents) {
            /*shortcutIntent = new Intent(context.getApplicationContext(), ActionForExternalApplicationActivity.class);
            shortcutIntent.setAction(ActionForExternalApplicationActivity.ACTION_RESTART_EVENTS);*/
            shortcutIntent = new Intent(context.getApplicationContext(), BackgroundActivateProfileActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.ACTION_DEFAULT);
            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
        }
        else {
            shortcutIntent = new Intent(context.getApplicationContext(), BackgroundActivateProfileActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.ACTION_DEFAULT);
            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        }

        String id;
        String profileName;
        String longLabel;

        if (restartEvents) {
            id = "restart_events";
            profileName = context.getString(R.string.menu_restart_events);
        }
        else {
            id = "profile_" + profile._id;
            profileName = profile._name;
        }
        longLabel = profileName;
        if (profileName.isEmpty())
            profileName = " ";
        if (longLabel.isEmpty())
            longLabel = " ";

        return new ShortcutInfo.Builder(context, id)
                .setShortLabel(profileName)
                .setLongLabel(longLabel)
                .setIcon(Icon.createWithBitmap(profileBitmap))
                .setIntent(shortcutIntent)
                .build();
    }

    static void setDynamicLauncherShortcuts(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            try {
                ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

                if (shortcutManager != null) {
                    final int limit = 4;

                    //List<Profile> countedProfiles = DatabaseHandler.getInstance(context).getProfilesForDynamicShortcuts(true);
                    List<Profile> countedProfiles = DatabaseHandler.getInstance(context).getProfilesInQuickTilesForDynamicShortcuts();
                    List<Profile> notCountedProfiles = DatabaseHandler.getInstance(context).getProfilesForDynamicShortcuts(/*false*/);

                    ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();

                    //Profile _profile = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
                    Profile _profile = getNonInitializedProfile(context.getString(R.string.menu_restart_events),
                            "ic_profile_restart_events|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
                    _profile.generateIconBitmap(context, false, 0, false);
                    // first profile is restart events
                    shortcuts.add(createShortcutInfo(_profile, true, context));

                    int shortcutsCount = 0;
                    for (Profile profile : countedProfiles) {
                        profile.generateIconBitmap(context, false, 0, false);
                        shortcuts.add(createShortcutInfo(profile, false, context));
                        ++shortcutsCount;
                        if (shortcutsCount == limit)
                            break;
                    }

                    //int shortcutsCount = countedProfiles.size();
                    if (shortcutsCount < limit) {
                        for (Profile profile : notCountedProfiles) {
                            profile.generateIconBitmap(context, false, 0, false);
                            shortcuts.add(createShortcutInfo(profile, false, context));
                            ++shortcutsCount;
                            if (shortcutsCount == limit)
                                break;
                        }
                    }

//                    PPApplication.logE("DataWrapperStatic.setDynamicLauncherShortcuts", "shortcuts.size()="+shortcuts.size());

                    shortcutManager.removeAllDynamicShortcuts();
                    if (shortcuts.size() > 0)
                        shortcutManager.addDynamicShortcuts(shortcuts);
                }
            } catch (Exception e) {
//                java.lang.IllegalStateException: Launcher activity not found for package sk.henrichg.phoneprofilesplus
//                at android.os.Parcel.createException(Parcel.java:2096)
//                at android.os.Parcel.readException(Parcel.java:2056)
//                at android.os.Parcel.readException(Parcel.java:2004)
//                at android.content.pm.IShortcutService$Stub$Proxy.setDynamicShortcuts(IShortcutService.java:830)
//                at android.content.pm.ShortcutManager.setDynamicShortcuts(ShortcutManager.java:112)
//                at sk.henrichg.phoneprofilesplus.DataWrapper.setDynamicLauncherShortcuts(DataWrapper.java:818)
//                - Generated, when device is rooted?

                //Log.e("DataWrapper.setDynamicLauncherShortcuts", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    static final String EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION = "from_red_text_preferences_notification";

    static boolean displayPreferencesErrorNotification(Profile profile, Event event, boolean againCheckAccessibilityInDelay, Context context) {
        if ((profile == null) && (event == null))
            return false;

        if (!PPApplication.getApplicationStarted(true))
            return false;

        if ((profile != null) && (!ProfilesPrefsFragment.isRedTextNotificationRequired(profile, againCheckAccessibilityInDelay, context))) {
            // clear notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            try {
                notificationManager.cancel(
                        PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile._id,
                        PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            return false;
        }
        if ((event != null) && (!EventsPrefsFragment.isRedTextNotificationRequired(event, againCheckAccessibilityInDelay, context))) {
            // clear notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            try {
                notificationManager.cancel(
                        PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id,
                        PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            return false;
        }

        int notificationID = 0;
        String notificationTag = null;

        String nTitle = "";
        String nText = "";

        Intent intent = null;

        if (profile != null) {
            intent = new Intent(context, ProfilesPrefsActivity.class);
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            intent.putExtra(EditorActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_EDIT);
            intent.putExtra(EditorActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
        }
        if (event != null) {
            intent = new Intent(context, EventsPrefsActivity.class);
            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
            intent.putExtra(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
            intent.putExtra(EditorActivity.EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_EDIT);
            intent.putExtra(EditorActivity.EXTRA_PREDEFINED_EVENT_INDEX, 0);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (profile != null) {
            nTitle = context.getString(R.string.profile_preferences_red_texts_title);
            nText = context.getString(R.string.profile_preferences_red_texts_text_1) + " " +
                    "\"" + profile._name + "\" " +
                    context.getString(R.string.preferences_red_texts_text_2) + " " +
                    context.getString(R.string.preferences_red_texts_text_click);
//            if (android.os.Build.VERSION.SDK_INT < 24) {
//                nTitle = context.getString(R.string.ppp_app_name);
//                nText = context.getString(R.string.profile_preferences_red_texts_title) + ": " +
//                        context.getString(R.string.profile_preferences_red_texts_text_1) + " " +
//                        "\"" + profile._name + "\" " +
//                        context.getString(R.string.preferences_red_texts_text_2) + " " +
//                        context.getString(R.string.preferences_red_texts_text_click);
//            }

            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            notificationID = PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id;
            notificationTag = PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile._id;
        }

        if (event != null) {
            nTitle = context.getString(R.string.event_preferences_red_texts_title);
            nText = context.getString(R.string.event_preferences_red_texts_text_1) + " " +
                    "\"" + event._name + "\" " +
                    context.getString(R.string.preferences_red_texts_text_2) + " " +
                    context.getString(R.string.preferences_red_texts_text_click);
//            if (android.os.Build.VERSION.SDK_INT < 24) {
//                nTitle = context.getString(R.string.ppp_app_name);
//                nText = context.getString(R.string.event_preferences_red_texts_title) + ": " +
//                        context.getString(R.string.event_preferences_red_texts_text_1) + " " +
//                        "\"" + event._name + "\" " +
//                        context.getString(R.string.preferences_red_texts_text_2) + " " +
//                        context.getString(R.string.preferences_red_texts_text_click);
//            }

            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
            notificationID = PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id;
            notificationTag = PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id;
        }

        intent.putExtra(EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION, true);

        PPApplication.createGrantPermissionNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setOnlyAlertOnce(true);

        mBuilder.setGroup(PPApplication.PROFILE_ACTIVATION_ERRORS_NOTIFICATION_GROUP);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            // do not cancel, mBuilder.setOnlyAlertOnce(true); will not be working
            // mNotificationManager.cancel(notificationID);
            mNotificationManager.notify(notificationTag, notificationID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("EditorActivity.displayNotGrantedPermissionsNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }

        return true;
    }

}
