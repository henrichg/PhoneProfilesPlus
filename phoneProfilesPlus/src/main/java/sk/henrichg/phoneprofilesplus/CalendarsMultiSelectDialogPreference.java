package sk.henrichg.phoneprofilesplus;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract.Calendars;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

public class CalendarsMultiSelectDialogPreference extends DialogPreference {
    CalendarsMultiSelectDialogPreferenceFragment fragment;

    private final Context _context;
    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    List<CalendarEvent> calendarList;

    public CalendarsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        calendarList = new ArrayList<>();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Get the persistent value
        value = getPersistedString((String) defaultValue);
        this.defaultValue = (String) defaultValue;
        setSummaryCMSDP();
    }

    static String getSummary(String value, Context context) {
        String summary = context.getString(R.string.calendars_multiselect_summary_text_not_selected);
        if (Permissions.checkCalendar(context)) {
            if (!value.isEmpty()) {
                String[] splits = value.split("\\|");
                if (splits.length == 1) {
                    boolean found = false;
                    Cursor cur;
                    ContentResolver cr = context.getContentResolver();
                    Uri uri = Calendars.CONTENT_URI;
                    String selection = Calendars._ID + "=" + splits[0];
                    // permission is already checked in Permissions.checkCalendar()
                    cur = cr.query(uri, CalendarsMultiSelectDialogPreferenceFragment.CALENDAR_PROJECTION, selection, null, null);
                    if (cur != null) {
                        //while (cur.moveToNext()) {
                        if (cur.moveToFirst()) {
                            found = true;
                            summary = cur.getString(CalendarsMultiSelectDialogPreferenceFragment.PROJECTION_DISPLAY_NAME_INDEX);
                            //break;
                        }
                        cur.close();
                    }
                    if (!found)
                        summary = context.getString(R.string.calendars_multiselect_summary_text_selected) + ": " + splits.length;
                } else
                    summary = context.getString(R.string.calendars_multiselect_summary_text_selected) + ": " + splits.length;
            }
        }
        return summary;
    }

    private void setSummaryCMSDP()
    {
        setSummary(getSummary(value, _context));
    }

    @SuppressWarnings("SameParameterValue")
    void refreshListView(final boolean notForUnselect) {
        if (fragment != null)
            fragment.refreshListView(notForUnselect);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void getValue() {
        // fill with strings of calendars separated with |
        value = "";
        if (calendarList != null)
        {
            for (CalendarEvent calendar : calendarList)
            {
                if (calendar.checked)
                {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + calendar.calendarId;
                }
            }
        }
    }

    void persistValue() {
        if (shouldPersist())
        {
            getValue();
            persistString(value);

            setSummaryCMSDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummaryCMSDP();
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final CalendarsMultiSelectDialogPreference.SavedState myState = new CalendarsMultiSelectDialogPreference.SavedState(superState);
        getValue();
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(CalendarsMultiSelectDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCMSDP();
            return;
        }

        // restore instance state
        CalendarsMultiSelectDialogPreference.SavedState myState = (CalendarsMultiSelectDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        setSummaryCMSDP();
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<CalendarsMultiSelectDialogPreference.SavedState> CREATOR =
                new Creator<CalendarsMultiSelectDialogPreference.SavedState>() {
                    public CalendarsMultiSelectDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new CalendarsMultiSelectDialogPreference.SavedState(in);
                    }
                    public CalendarsMultiSelectDialogPreference.SavedState[] newArray(int size)
                    {
                        return new CalendarsMultiSelectDialogPreference.SavedState[size];
                    }

                };

    }

}
