package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;

//import android.widget.CursorAdapter;

class ActivityLogAdapter extends CursorAdapter {

    private final int KEY_AL_ID;
    private final int KEY_AL_LOG_DATE_TIME;
    private final int KEY_AL_LOG_TYPE;
    private final int KEY_AL_EVENT_NAME;
    private final int KEY_AL_PROFILE_NAME;
    //private final int KEY_AL_PROFILE_ICON;
    //private final int KEY_AL_DURATION_DELAY;
    private final int KEY_AL_PROFILE_EVENT_COUNT;

    private final SparseIntArray activityTypeStrings = new SparseIntArray();
    private final SparseIntArray activityTypeColors = new SparseIntArray();

    ActivityLogAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);

        KEY_AL_ID = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_ID);
        KEY_AL_LOG_DATE_TIME = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_LOG_DATE_TIME);
        KEY_AL_LOG_TYPE = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_LOG_TYPE);
        KEY_AL_EVENT_NAME = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_EVENT_NAME);
        KEY_AL_PROFILE_NAME = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_PROFILE_NAME);
        //KEY_AL_PROFILE_ICON = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_PROFILE_ICON);
        //KEY_AL_DURATION_DELAY = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_DURATION_DELAY);
        KEY_AL_PROFILE_EVENT_COUNT = cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AL_PROFILE_EVENT_COUNT);

        //activityTypeStrings.put(PPApplication.ALTYPE_LOG_TOP, R.string.altype_logTop);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ACTIVATION, R.string.altype_profileActivation);
        activityTypeStrings.put(PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION, R.string.altype_mergedProfileActivation);
        activityTypeStrings.put(PPApplication.ALTYPE_AFTER_DURATION_UNDO_PROFILE, R.string.altype_afterDuration_undoProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_AFTER_DURATION_DEFAULT_PROFILE, R.string.altype_afterDuration_backgroundProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_AFTER_DURATION_RESTART_EVENTS, R.string.altype_afterDuration_restartEvents);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_START, R.string.altype_eventStart);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_START_DELAY, R.string.altype_eventStartDelay);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_END_NONE, R.string.altype_eventEnd_none);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE, R.string.altype_eventEnd_activateProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_END_UNDO_PROFILE, R.string.altype_eventEnd_undoProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE_UNDO_PROFILE, R.string.altype_eventEnd_activateProfile_undoProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_END_RESTART_EVENTS, R.string.altype_eventEnd_restartEvents);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE_RESTART_EVENTS, R.string.altype_eventEnd_activateProfile_restartEvents);
        activityTypeStrings.put(PPApplication.ALTYPE_RESTART_EVENTS, R.string.altype_restartEvents);
        activityTypeStrings.put(PPApplication.ALTYPE_RUN_EVENTS_DISABLE, R.string.altype_runEvents_disable);
        activityTypeStrings.put(PPApplication.ALTYPE_RUN_EVENTS_ENABLE, R.string.altype_runEvents_enable);
        activityTypeStrings.put(PPApplication.ALTYPE_APPLICATION_START, R.string.altype_applicationStart);
        activityTypeStrings.put(PPApplication.ALTYPE_APPLICATION_START_ON_BOOT, R.string.altype_applicationStartOnBoot);
        activityTypeStrings.put(PPApplication.ALTYPE_APPLICATION_EXIT, R.string.altype_applicationExit);
        activityTypeStrings.put(PPApplication.ALTYPE_DATA_IMPORT, R.string.altype_dataImport);
        activityTypeStrings.put(PPApplication.ALTYPE_PAUSED_LOGGING, R.string.altype_pausedLogging);
        activityTypeStrings.put(PPApplication.ALTYPE_STARTED_LOGGING, R.string.altype_startedLogging);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_END_DELAY, R.string.altype_eventEndDelay);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_STOP, R.string.altype_eventStop);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_PREFERENCES_CHANGED, R.string.altype_eventPreferencesChanged);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_DELETED, R.string.altype_eventDeleted);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_DELETED, R.string.altype_profileDeleted);
        activityTypeStrings.put(PPApplication.ALTYPE_MANUAL_RESTART_EVENTS, R.string.altype_manualRestartEvents);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_PREFERENCES_CHANGED, R.string.altype_profilePreferencesChanged);
        activityTypeStrings.put(PPApplication.ALTYPE_SHARED_PROFILE_PREFERENCES_CHANGED, R.string.altype_sharedProfilePreferencesChanged);
        activityTypeStrings.put(PPApplication.ALTYPE_ALL_EVENTS_DELETED, R.string.altype_allEventsDeleted);
        activityTypeStrings.put(PPApplication.ALTYPE_ALL_PROFILES_DELETED, R.string.altype_allProfilesDeleted);
        activityTypeStrings.put(PPApplication.ALTYPE_APPLICATION_UPGRADE, R.string.altype_applicationUpgrade);
        activityTypeStrings.put(PPApplication.ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE, R.string.altype_afterDuration_specificProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION, R.string.altype_profileError_runApplication_application);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT, R.string.altype_profileError_runApplication_shortcut);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT, R.string.altype_profileError_runApplication_intent);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE, R.string.altype_profileError_setTone_ringtone);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION, R.string.altype_profileError_setTone_notification);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_ALARM, R.string.altype_profileError_setTone_alarm);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER, R.string.altype_profileError_setWallpaper);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_CAMERA_FLASH, R.string.altype_profileError_cameraFlash);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_WIFI, R.string.altype_profileError_wifi);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_WIFIAP, R.string.altype_profileError_wifiAP);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_CLOSE_ALL_APPLICATIONS, R.string.altype_profileError_closeAllApplications);
        activityTypeStrings.put(PPApplication.ALTYPE_DATA_EXPORT, R.string.altype_dataExport);
        activityTypeStrings.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PROFILE_ACTIVATION, R.string.altype_actionFromExternalApp_profileActivation);
        activityTypeStrings.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_RESTART_EVENTS, R.string.altype_actionFromExternalApp_restartEvents);
        activityTypeStrings.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_ENABLE_RUN_FOR_EVENT, R.string.altype_actionFromExternalApp_enableRunForEvent);
        activityTypeStrings.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PAUSE_EVENT, R.string.altype_actionFromExternalApp_pauseEvent);
        activityTypeStrings.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_STOP_EVENT, R.string.altype_actionFromExternalApp_stopEvent);
        activityTypeStrings.put(PPApplication.ALTYPE_APPLICATION_SYSTEM_RESTART, R.string.altype_applicationSystemRestart);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ADDED, R.string.altype_profileAdded);
        activityTypeStrings.put(PPApplication.ALTYPE_EVENT_ADDED, R.string.altype_eventAdded);
        activityTypeStrings.put(PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_UNDO_PROFILE, R.string.altype_afterEndOfActivationTime_undoProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_DEFAULT_PROFILE, R.string.altype_afterEndOfActivationTime_defaultProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_RESTART_EVENTS, R.string.altype_afterEndOfActivationTime_restartEvents);
        activityTypeStrings.put(PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_SPECIFIC_PROFILE, R.string.altype_afterEndOfActivationTime_specificProfile);
        activityTypeStrings.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_VPN, R.string.altype_profileError_setVPN);
        activityTypeStrings.put(PPApplication.ALTYPE_TIMEZONE_CHANGED, R.string.altype_timezone_changed);
        activityTypeStrings.put(PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_ENABLED, R.string.altype_extender_accessibility_service_enabled);
        activityTypeStrings.put(PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED, R.string.altype_extender_accessibility_service_not_enabled);
        activityTypeStrings.put(PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_UNBIND, R.string.altype_extender_accessibility_service_unbind);

        //int otherColor = R.color.altype_other;
        /*
        switch (ApplicationPreferences.applicationTheme(context, true)) {
//            case "color":
//                otherColor = R.color.altype_other;
//                break;
            case "white":
                otherColor = R.color.altype_other_white;
                break;
            default:
                otherColor = R.color.altype_other_dark;
                break;
        }*/

        int color = shiftColor(ContextCompat.getColor(context, R.color.altype_profile), context);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ACTIVATION, color);
        activityTypeColors.put(PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION, color);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_DURATION_UNDO_PROFILE, color);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_DURATION_DEFAULT_PROFILE, color);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_DURATION_RESTART_EVENTS, color);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE, color);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_UNDO_PROFILE, color);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_DEFAULT_PROFILE, color);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_RESTART_EVENTS, color);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_SPECIFIC_PROFILE, color);
        color = shiftColor(ContextCompat.getColor(context, R.color.altype_eventStart), context);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_START, color);
        color = shiftColor(ContextCompat.getColor(context, R.color.altype_eventDelayStartEnd), context);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_START_DELAY, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_DELAY, color);
        color = shiftColor(ContextCompat.getColor(context, R.color.altype_eventEnd), context);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_NONE, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_UNDO_PROFILE, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE_UNDO_PROFILE, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_RESTART_EVENTS, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE_RESTART_EVENTS, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_STOP, color);
        color = shiftColor(ContextCompat.getColor(context, R.color.altype_restartEvents), context);
        activityTypeColors.put(PPApplication.ALTYPE_RESTART_EVENTS, color);
        activityTypeColors.put(PPApplication.ALTYPE_MANUAL_RESTART_EVENTS, color);
        color = ContextCompat.getColor(context, R.color.altype_error);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_ALARM, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_VPN, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_CAMERA_FLASH, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_WIFI, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_WIFIAP, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_CLOSE_ALL_APPLICATIONS, color);
        activityTypeColors.put(PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED, color);
        activityTypeColors.put(PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_UNBIND, color);
        color = shiftColor(ContextCompat.getColor(context, R.color.altype_other), context);
        //activityTypeColors.put(PPApplication.ALTYPE_LOG_TOP, color);
        activityTypeColors.put(PPApplication.ALTYPE_RUN_EVENTS_DISABLE, color);
        activityTypeColors.put(PPApplication.ALTYPE_RUN_EVENTS_ENABLE, color);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_START, color);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_START_ON_BOOT, color);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_EXIT, color);
        activityTypeColors.put(PPApplication.ALTYPE_DATA_IMPORT, color);
        activityTypeColors.put(PPApplication.ALTYPE_PAUSED_LOGGING, color);
        activityTypeColors.put(PPApplication.ALTYPE_STARTED_LOGGING, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_PREFERENCES_CHANGED, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_DELETED, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_DELETED, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_PREFERENCES_CHANGED, color);
        activityTypeColors.put(PPApplication.ALTYPE_SHARED_PROFILE_PREFERENCES_CHANGED, color);
        activityTypeColors.put(PPApplication.ALTYPE_ALL_EVENTS_DELETED, color);
        activityTypeColors.put(PPApplication.ALTYPE_ALL_PROFILES_DELETED, color);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_UPGRADE, color);
        activityTypeColors.put(PPApplication.ALTYPE_DATA_EXPORT, color);
        activityTypeColors.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PROFILE_ACTIVATION, color);
        activityTypeColors.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_RESTART_EVENTS, color);
        activityTypeColors.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_ENABLE_RUN_FOR_EVENT, color);
        activityTypeColors.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PAUSE_EVENT, color);
        activityTypeColors.put(PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_STOP_EVENT, color);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_SYSTEM_RESTART, color);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ADDED, color);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_ADDED, color);
        activityTypeColors.put(PPApplication.ALTYPE_TIMEZONE_CHANGED, color);
        activityTypeColors.put(PPApplication.ALTYPE_EXTENDER_ACCESSIBILITY_SERVICE_ENABLED, color);
    }

    private void setRowData(MyRowViewHolder rowData, Cursor cursor, Context context) {
        if (cursor.getInt(KEY_AL_ID) == -1) {
            //Log.e("ActivityLogAdapter.setRowData", "KEY_AL_ID=-1");
            rowData.logTypeColor.setBackgroundResource(R.color.activityBackgroundColor);
            rowData.logTypeColor.setAlpha(0);
            rowData.logDateTime.setText("");
        }
        else {
            //Log.e("ActivityLogAdapter.setRowData", "color="+activityTypeColors.get(cursor.getInt(KEY_AL_LOG_TYPE)));
            rowData.logTypeColor.setBackgroundColor(activityTypeColors.get(cursor.getInt(KEY_AL_LOG_TYPE)));
            rowData.logTypeColor.setAlpha(1);
            rowData.logDateTime.setText(StringFormatUtils.formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));
        }

        int logType = cursor.getInt(KEY_AL_LOG_TYPE);
        String logTypeText;
        if (cursor.getInt(KEY_AL_ID) == -1) {
            logTypeText = "---";
        } else {
            logTypeText = context.getString(activityTypeStrings.get(logType));
            if (logType == PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION) {
                String profileEventCount = cursor.getString(KEY_AL_PROFILE_EVENT_COUNT);
                if (profileEventCount != null)
                    logTypeText = logTypeText + " " + profileEventCount;
            }
        }
        rowData.logType.setText(logTypeText);

        String logData = "";
        String event_name = cursor.getString(KEY_AL_EVENT_NAME);
        String profile_name = cursor.getString(KEY_AL_PROFILE_NAME);
        if (event_name != null)
            logData = logData + event_name;
        if (profile_name != null) {
            if (!logData.isEmpty())
                logData = logData + " ";
            logData = logData + profile_name;
        }
        rowData.logData.setText(logData);
        //rowData.eventName.setText(cursor.getString(KEY_AL_EVENT_NAME));
        //rowData.profileName.setText(cursor.getString(KEY_AL_PROFILE_NAME));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.listitem_ppp_activity_log, parent, false);

        MyRowViewHolder rowData  = new MyRowViewHolder();

        rowData.logTypeColor = view.findViewById(R.id.activity_log_row_color);
        rowData.logDateTime  = view.findViewById(R.id.activity_log_row_log_date_time);
        rowData.logType  = view.findViewById(R.id.activity_log_row_log_type);
        rowData.logData  = view.findViewById(R.id.activity_log_row_log_data);
        //rowData.eventName  = view.findViewById(R.id.activity_log_row_event_name);
        //rowData.profileName  = view.findViewById(R.id.activity_log_row_profile_name);

        setRowData(rowData, cursor, context);

        view.setTag(rowData);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        MyRowViewHolder rowData = (MyRowViewHolder) view.getTag();

        setRowData(rowData, cursor, context);
    }

    private static class MyRowViewHolder {
        FrameLayout logTypeColor;
        TextView logDateTime;
        TextView logType;
        TextView logData;
        //TextView eventName;
        //TextView profileName;
    }

    void reload(Context context/*DataWrapper dataWrapper*/) {
        changeCursor(DatabaseHandler.getInstance(/*dataWrapper.*/context.getApplicationContext()).getActivityLogCursor());
    }

    private int shiftColor(int color, Context context) {
        String applicationTheme = ApplicationPreferences.applicationTheme(context, true);
        if (!applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_DARK)) {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] = 0.75f; // value component
            return Color.HSVToColor(hsv);
        }
        else
            return color;
    }

}
