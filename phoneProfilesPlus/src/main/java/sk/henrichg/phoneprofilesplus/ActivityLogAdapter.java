package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ActivityLogAdapter extends CursorAdapter {

    private final int KEY_AL_LOG_DATE_TIME;
    private final int KEY_AL_LOG_TYPE;
    private final int KEY_AL_EVENT_NAME;
    private final int KEY_AL_PROFILE_NAME;
    private final int KEY_AL_PROFILE_ICON;
    private final int KEY_AL_DURATION_DELAY;

    public ActivityLogAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);

        KEY_AL_LOG_DATE_TIME = cursor.getColumnIndex(DatabaseHandler.KEY_AL_LOG_DATE_TIME);
        KEY_AL_LOG_TYPE = cursor.getColumnIndex(DatabaseHandler.KEY_AL_LOG_TYPE);
        KEY_AL_EVENT_NAME = cursor.getColumnIndex(DatabaseHandler.KEY_AL_EVENT_NAME);
        KEY_AL_PROFILE_NAME = cursor.getColumnIndex(DatabaseHandler.KEY_AL_PROFILE_NAME);
        KEY_AL_PROFILE_ICON = cursor.getColumnIndex(DatabaseHandler.KEY_AL_PROFILE_ICON);
        KEY_AL_DURATION_DELAY = cursor.getColumnIndex(DatabaseHandler.KEY_AL_DURATION_DELAY);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_log_row, parent, false);

        MyRowViewHolder rowData  = new MyRowViewHolder();

        rowData.logDateTime  = (TextView) view.findViewById(R.id.activity_log_row_log_date_time);
        rowData.logType  = (TextView) view.findViewById(R.id.activity_log_row_log_type);
        rowData.eventName  = (TextView) view.findViewById(R.id.activity_log_row_event_name);
        rowData.profileName  = (TextView) view.findViewById(R.id.activity_log_row_profile_name);
        //rowData.profileIcon  = (ImageView) view.findViewById(R.id.activity_log_row_profile_icon);
        rowData.durationDelay  = (TextView) view.findViewById(R.id.activity_log_row_duration_delay);

        rowData.logDateTime.setText(formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));
        rowData.logType.setText(cursor.getString(KEY_AL_LOG_TYPE));
        rowData.eventName.setText(cursor.getString(KEY_AL_EVENT_NAME));
        rowData.profileName.setText(cursor.getString(KEY_AL_PROFILE_NAME));
        rowData.durationDelay.setText(cursor.getString(KEY_AL_DURATION_DELAY));

        view.setTag(rowData);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        MyRowViewHolder rowData = (MyRowViewHolder) view.getTag();

        rowData.logDateTime.setText(formatDateTime(context, cursor.getString(KEY_AL_LOG_DATE_TIME)));
        rowData.logType.setText(cursor.getString(KEY_AL_LOG_TYPE));
        rowData.eventName.setText(cursor.getString(KEY_AL_EVENT_NAME));
        rowData.profileName.setText(cursor.getString(KEY_AL_PROFILE_NAME));
        rowData.durationDelay.setText(cursor.getString(KEY_AL_DURATION_DELAY));
    }

    public static class MyRowViewHolder {
        TextView logDateTime;
        TextView logType;
        TextView eventName;
        TextView profileName;
        ImageView profileIcon;
        TextView durationDelay;
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
                int flags = 0;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
                flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = android.text.format.DateUtils.formatDateTime(context,
                        when + TimeZone.getDefault().getOffset(when), flags);
            }
        }
        return finalDateTime;
    }
}
