package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;

import java.util.HashMap;

//import android.widget.CursorAdapter;

class ActivityLogAdapter extends CursorAdapter {

    private final int KEY_AL_LOG_DATE_TIME;
    private final int KEY_AL_LOG_TYPE;
    private final int KEY_AL_EVENT_NAME;
    private final int KEY_AL_PROFILE_NAME;
    //private final int KEY_AL_PROFILE_ICON;
    //private final int KEY_AL_DURATION_DELAY;
    private final int KEY_AL_PROFILE_EVENT_COUNT;

    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, Integer> activityTypeStrings = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, Integer> activityTypeColors = new HashMap<>();

    ActivityLogAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);

        KEY_AL_LOG_DATE_TIME = cursor.getColumnIndex(DatabaseHandler.KEY_AL_LOG_DATE_TIME);
        KEY_AL_LOG_TYPE = cursor.getColumnIndex(DatabaseHandler.KEY_AL_LOG_TYPE);
        KEY_AL_EVENT_NAME = cursor.getColumnIndex(DatabaseHandler.KEY_AL_EVENT_NAME);
        KEY_AL_PROFILE_NAME = cursor.getColumnIndex(DatabaseHandler.KEY_AL_PROFILE_NAME);
        //KEY_AL_PROFILE_ICON = cursor.getColumnIndex(DatabaseHandler.KEY_AL_PROFILE_ICON);
        //KEY_AL_DURATION_DELAY = cursor.getColumnIndex(DatabaseHandler.KEY_AL_DURATION_DELAY);
        KEY_AL_PROFILE_EVENT_COUNT = cursor.getColumnIndex(DatabaseHandler.KEY_AL_PROFILE_EVENT_COUNT);

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
        activityTypeStrings.put(PPApplication.ALTYPE_DATA_IMPORT_FROM_PP, R.string.altype_dataImport_fromPP);

        int otherColor = R.color.altype_other;
        /*//noinspection SwitchStatementWithTooFewBranches
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

        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ACTIVATION, R.color.altype_profile);
        activityTypeColors.put(PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION, R.color.altype_profile);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_DURATION_UNDO_PROFILE, R.color.altype_profile);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_DURATION_DEFAULT_PROFILE, R.color.altype_profile);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_DURATION_RESTART_EVENTS, R.color.altype_profile);
        activityTypeColors.put(PPApplication.ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE, R.color.altype_profile);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_START, R.color.altype_eventStart);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_START_DELAY, R.color.altype_eventDelayStartEnd);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_DELAY, R.color.altype_eventDelayStartEnd);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_NONE, R.color.altype_eventEnd);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_UNDO_PROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE_UNDO_PROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_RESTART_EVENTS, R.color.altype_eventEnd);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE_RESTART_EVENTS, R.color.altype_eventEnd);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_STOP, R.color.altype_eventEnd);
        activityTypeColors.put(PPApplication.ALTYPE_RESTART_EVENTS, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_RUN_EVENTS_DISABLE, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_RUN_EVENTS_ENABLE, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_START, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_START_ON_BOOT, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_EXIT, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_DATA_IMPORT, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_PAUSED_LOGGING, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_STARTED_LOGGING, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_PREFERENCES_CHANGED, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_EVENT_DELETED, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_DELETED, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_MANUAL_RESTART_EVENTS, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_PREFERENCES_CHANGED, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_SHARED_PROFILE_PREFERENCES_CHANGED, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_ALL_EVENTS_DELETED, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_ALL_PROFILES_DELETED, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_APPLICATION_UPGRADE, otherColor);
        activityTypeColors.put(PPApplication.ALTYPE_DATA_IMPORT_FROM_PP, otherColor);

        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION, R.color.altype_error);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT, R.color.altype_error);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT, R.color.altype_error);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE, R.color.altype_error);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION, R.color.altype_error);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_ALARM, R.color.altype_error);
        activityTypeColors.put(PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER, R.color.altype_error);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_log_list_item, parent, false);

        MyRowViewHolder rowData  = new MyRowViewHolder();

        rowData.logTypeColor = view.findViewById(R.id.activity_log_row_color);
        rowData.logDateTime  = view.findViewById(R.id.activity_log_row_log_date_time);
        rowData.logType  = view.findViewById(R.id.activity_log_row_log_type);
        rowData.logData  = view.findViewById(R.id.activity_log_row_log_data);
        //rowData.eventName  = view.findViewById(R.id.activity_log_row_event_name);
        //rowData.profileName  = view.findViewById(R.id.activity_log_row_profile_name);

        //noinspection ConstantConditions
        rowData.logTypeColor.setBackgroundColor(ContextCompat.getColor(context, activityTypeColors.get(cursor.getInt(KEY_AL_LOG_TYPE))));
        rowData.logDateTime.setText(GlobalGUIRoutines.formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));

        int logType = cursor.getInt(KEY_AL_LOG_TYPE);
        //noinspection ConstantConditions
        String logTypeText = context.getString(activityTypeStrings.get(logType));
        if (logType == PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION) {
            String profileEventCount = cursor.getString(KEY_AL_PROFILE_EVENT_COUNT);
            if (profileEventCount != null)
                logTypeText = logTypeText + " " + profileEventCount;
        }
        rowData.logType.setText(logTypeText);

        //noinspection ConstantConditions
        rowData.logType.setText(activityTypeStrings.get(cursor.getInt(KEY_AL_LOG_TYPE)));
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

        view.setTag(rowData);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        MyRowViewHolder rowData = (MyRowViewHolder) view.getTag();

        //noinspection ConstantConditions
        rowData.logTypeColor.setBackgroundColor(ContextCompat.getColor(context, activityTypeColors.get(cursor.getInt(KEY_AL_LOG_TYPE))));
        rowData.logDateTime.setText(GlobalGUIRoutines.formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));

        int logType = cursor.getInt(KEY_AL_LOG_TYPE);
        //noinspection ConstantConditions
        String logTypeText = context.getString(activityTypeStrings.get(logType));
        if (logType == PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION) {
            String profileEventCount = cursor.getString(KEY_AL_PROFILE_EVENT_COUNT);
            if (profileEventCount != null)
                logTypeText = logTypeText + ": " + profileEventCount;
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

    private static class MyRowViewHolder {
        FrameLayout logTypeColor;
        TextView logDateTime;
        TextView logType;
        TextView logData;
        //TextView eventName;
        //TextView profileName;
    }

    void reload(DataWrapper dataWrapper) {
        changeCursor(DatabaseHandler.getInstance(dataWrapper.context.getApplicationContext()).getActivityLogCursor());
    }
}
