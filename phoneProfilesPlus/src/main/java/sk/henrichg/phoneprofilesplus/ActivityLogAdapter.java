package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.HashMap;

class ActivityLogAdapter extends CursorAdapter {

    private final int KEY_AL_LOG_DATE_TIME;
    private final int KEY_AL_LOG_TYPE;
    private final int KEY_AL_EVENT_NAME;
    private final int KEY_AL_PROFILE_NAME;
    //private final int KEY_AL_PROFILE_ICON;
    //private final int KEY_AL_DURATION_DELAY;

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

        activityTypeStrings.put(DatabaseHandler.ALTYPE_PROFILEACTIVATION, R.string.altype_profileActivation);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_AFTERDURATION_UNDOPROFILE, R.string.altype_afterDuration_undoProfile);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_AFTERDURATION_BACKGROUNDPROFILE, R.string.altype_afterDuration_backgroundProfile);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_AFTERDURATION_RESTARTEVENTS, R.string.altype_afterDuration_restartEvents);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTSTART, R.string.altype_eventStart);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTSTARTDELAY, R.string.altype_eventStartDelay);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTEND_NONE, R.string.altype_eventEnd_none);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE, R.string.altype_eventEnd_activateProfile);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTEND_UNDOPROFILE, R.string.altype_eventEnd_undoProfile);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_UNDOPROFILE, R.string.altype_eventEnd_activateProfile_undoProfile);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTEND_RESTARTEVENTS, R.string.altype_eventEnd_restartEvents);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_RESTARTEVENTS, R.string.altype_eventEnd_activateProfile_restartEvents);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_RESTARTEVENTS, R.string.altype_restartEvents);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_RUNEVENTS_DISABLE, R.string.altype_runEvents_disable);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_RUNEVENTS_ENABLE, R.string.altype_runEvents_enable);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_APPLICATIONSTART, R.string.altype_applicationStart);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_APPLICATIONSTARTONBOOT, R.string.altype_applicationStartOnBoot);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_APPLICATIONEXIT, R.string.altype_applicationExit);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_DATAIMPORT, R.string.altype_dataImport);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_PAUSEDLOGGING, R.string.altype_pausedLogging);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_STARTEDLOGGING, R.string.altype_startedLogging);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTENDDELAY, R.string.altype_eventEndDelay);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_EVENTSTOP, R.string.altype_eventStop);

        activityTypeColors.put(DatabaseHandler.ALTYPE_PROFILEACTIVATION, R.color.altype_profile);
        activityTypeColors.put(DatabaseHandler.ALTYPE_AFTERDURATION_UNDOPROFILE, R.color.altype_profile);
        activityTypeColors.put(DatabaseHandler.ALTYPE_AFTERDURATION_BACKGROUNDPROFILE, R.color.altype_profile);
        activityTypeColors.put(DatabaseHandler.ALTYPE_AFTERDURATION_RESTARTEVENTS, R.color.altype_profile);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTSTART, R.color.altype_eventStart);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTSTARTDELAY, R.color.altype_eventStart);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_NONE, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTENDDELAY, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_UNDOPROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_UNDOPROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_RESTARTEVENTS, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_RESTARTEVENTS, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTSTOP, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_RESTARTEVENTS, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_RUNEVENTS_DISABLE, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_RUNEVENTS_ENABLE, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_APPLICATIONSTART, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_APPLICATIONSTARTONBOOT, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_APPLICATIONEXIT, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_DATAIMPORT, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_PAUSEDLOGGING, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_STARTEDLOGGING, R.color.altype_other);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_log_row, parent, false);

        MyRowViewHolder rowData  = new MyRowViewHolder();

        rowData.logTypeColor = view.findViewById(R.id.activity_log_row_color);
        rowData.logDateTime  = view.findViewById(R.id.activity_log_row_log_date_time);
        rowData.logType  = view.findViewById(R.id.activity_log_row_log_type);
        rowData.eventName  = view.findViewById(R.id.activity_log_row_event_name);
        rowData.profileName  = view.findViewById(R.id.activity_log_row_profile_name);
        //rowData.profileIcon  = view.findViewById(R.id.activity_log_row_profile_icon);
        //rowData.durationDelay  = view.findViewById(R.id.activity_log_row_duration_delay);

        rowData.logTypeColor.setBackgroundColor(ContextCompat.getColor(context, activityTypeColors.get(cursor.getInt(KEY_AL_LOG_TYPE))));
        rowData.logDateTime.setText(GlobalGUIRoutines.formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));
        rowData.logType.setText(activityTypeStrings.get(cursor.getInt(KEY_AL_LOG_TYPE)));
        rowData.eventName.setText(cursor.getString(KEY_AL_EVENT_NAME));
        rowData.profileName.setText(cursor.getString(KEY_AL_PROFILE_NAME));
        //rowData.durationDelay.setText(cursor.getString(KEY_AL_DURATION_DELAY));

        view.setTag(rowData);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        MyRowViewHolder rowData = (MyRowViewHolder) view.getTag();

        rowData.logTypeColor.setBackgroundColor(ContextCompat.getColor(context, activityTypeColors.get(cursor.getInt(KEY_AL_LOG_TYPE))));
        rowData.logDateTime.setText(GlobalGUIRoutines.formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));
        rowData.logType.setText(activityTypeStrings.get(cursor.getInt(KEY_AL_LOG_TYPE)));
        rowData.eventName.setText(cursor.getString(KEY_AL_EVENT_NAME));
        rowData.profileName.setText(cursor.getString(KEY_AL_PROFILE_NAME));
        //rowData.durationDelay.setText(cursor.getString(KEY_AL_DURATION_DELAY));
    }

    private static class MyRowViewHolder {
        FrameLayout logTypeColor;
        TextView logDateTime;
        TextView logType;
        TextView eventName;
        TextView profileName;
        //ImageView profileIcon;
        //TextView durationDelay;
    }

    public void reload(DataWrapper dataWrapper) {
        changeCursor(DatabaseHandler.getInstance(dataWrapper.context.getApplicationContext()).getActivityLogCursor());
    }
}
