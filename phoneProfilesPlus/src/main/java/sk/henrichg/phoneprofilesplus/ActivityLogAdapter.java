package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class ActivityLogAdapter extends CursorAdapter {

    private final int KEY_AL_LOG_DATE_TIME;
    private final int KEY_AL_LOG_TYPE;
    private final int KEY_AL_EVENT_NAME;
    private final int KEY_AL_PROFILE_NAME;
    //private final int KEY_AL_PROFILE_ICON;
    //private final int KEY_AL_DURATION_DELAY;

    HashMap<Integer, Integer> activityTypeStrings = new HashMap<Integer, Integer>();
    HashMap<Integer, Integer> activityTypeColors = new HashMap<Integer, Integer>();

    public ActivityLogAdapter(Context context, Cursor cursor) {
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
        activityTypeStrings.put(DatabaseHandler.ALTYPE_APPLICATIONEXIT, R.string.altype_applicationExit);
        activityTypeStrings.put(DatabaseHandler.ALTYPE_DATAIMPORT, R.string.altype_dataImport);

        activityTypeColors.put(DatabaseHandler.ALTYPE_PROFILEACTIVATION, R.color.altype_profile);
        activityTypeColors.put(DatabaseHandler.ALTYPE_AFTERDURATION_UNDOPROFILE, R.color.altype_profile);
        activityTypeColors.put(DatabaseHandler.ALTYPE_AFTERDURATION_BACKGROUNDPROFILE, R.color.altype_profile);
        activityTypeColors.put(DatabaseHandler.ALTYPE_AFTERDURATION_RESTARTEVENTS, R.color.altype_profile);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTSTART, R.color.altype_eventStart);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTSTARTDELAY, R.color.altype_eventStart);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_NONE, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_UNDOPROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_UNDOPROFILE, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_RESTARTEVENTS, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_RESTARTEVENTS, R.color.altype_eventEnd);
        activityTypeColors.put(DatabaseHandler.ALTYPE_RESTARTEVENTS, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_RUNEVENTS_DISABLE, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_RUNEVENTS_ENABLE, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_APPLICATIONSTART, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_APPLICATIONEXIT, R.color.altype_other);
        activityTypeColors.put(DatabaseHandler.ALTYPE_DATAIMPORT, R.color.altype_other);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_log_row, parent, false);

        MyRowViewHolder rowData  = new MyRowViewHolder();

        rowData.logTypeColor = (FrameLayout) view.findViewById(R.id.activity_log_row_color);
        rowData.logDateTime  = (TextView) view.findViewById(R.id.activity_log_row_log_date_time);
        rowData.logType  = (TextView) view.findViewById(R.id.activity_log_row_log_type);
        rowData.eventName  = (TextView) view.findViewById(R.id.activity_log_row_event_name);
        rowData.profileName  = (TextView) view.findViewById(R.id.activity_log_row_profile_name);
        //rowData.profileIcon  = (ImageView) view.findViewById(R.id.activity_log_row_profile_icon);
        //rowData.durationDelay  = (TextView) view.findViewById(R.id.activity_log_row_duration_delay);

        rowData.logTypeColor.setBackgroundColor(context.getResources().getColor(activityTypeColors.get(cursor.getInt(KEY_AL_LOG_TYPE))));
        rowData.logDateTime.setText(formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));
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

        rowData.logTypeColor.setBackgroundColor(context.getResources().getColor(activityTypeColors.get(cursor.getInt(KEY_AL_LOG_TYPE))));
        rowData.logDateTime.setText(formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));
        rowData.logType.setText(activityTypeStrings.get(cursor.getInt(KEY_AL_LOG_TYPE)));
        rowData.eventName.setText(cursor.getString(KEY_AL_EVENT_NAME));
        rowData.profileName.setText(cursor.getString(KEY_AL_PROFILE_NAME));
        //rowData.durationDelay.setText(cursor.getString(KEY_AL_DURATION_DELAY));
    }

    public static class MyRowViewHolder {
        FrameLayout logTypeColor;
        TextView logDateTime;
        TextView logType;
        TextView eventName;
        TextView profileName;
        //ImageView profileIcon;
        //TextView durationDelay;
    }

    public static String formatDateTime(Context context, String timeToFormat) {

        String finalDateTime = "";

        SimpleDateFormat iso8601Format = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        Date date = null;
        if (timeToFormat != null) {
            try {
                date = iso8601Format.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                when += TimeZone.getDefault().getOffset(when);

                /*
                int flags = 0;
                flags |= DateUtils.FORMAT_SHOW_TIME;
                flags |= DateUtils.FORMAT_SHOW_DATE;
                flags |= DateUtils.FORMAT_NUMERIC_DATE;
                flags |= DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = android.text.format.DateUtils.formatDateTime(context,
                        when, flags);

                finalDateTime = DateFormat.getDateFormat(context).format(when) +
                        " " + DateFormat.getTimeFormat(context).format(when);
                */

                /*
                SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");
                finalDateTime = sdf.format(when);
                */

                finalDateTime = timeDateStringFromTimestamp(context, when);
            }
        }
        return finalDateTime;
    }

    private static String timeDateStringFromTimestamp(Context applicationContext,long timestamp){
        String timeDate;
        String androidDateTime=android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(timestamp))+" "+
                android.text.format.DateFormat.getTimeFormat(applicationContext).format(new Date(timestamp));
        String javaDateTime = DateFormat.getDateTimeInstance().format(new Date(timestamp));
        String AmPm="";
        if(!Character.isDigit(androidDateTime.charAt(androidDateTime.length()-1))) {
            if(androidDateTime.contains(new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM])){
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM];
            }else{
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM];
            }
            androidDateTime=androidDateTime.replace(AmPm, "");
        }
        if(!Character.isDigit(javaDateTime.charAt(javaDateTime.length()-1))){
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM], "");
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM], "");
        }
        javaDateTime=javaDateTime.substring(javaDateTime.length()-3);
        timeDate=androidDateTime.concat(javaDateTime);
        return timeDate.concat(AmPm);
    }

    public void reload(DataWrapper dataWrapper) {
        changeCursor(dataWrapper.getDatabaseHandler().getActivityLogCursor());
    }
}
