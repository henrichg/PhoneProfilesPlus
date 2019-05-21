package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract.Calendars;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.DialogPreference;

public class CalendarsMultiSelectDialogPreferenceX extends DialogPreference
{
    CalendarsMultiSelectDialogPreferenceFragmentX fragment;

    private final Context _context;
    String value = "";

    List<CalendarEvent> calendarList;

    public CalendarsMultiSelectDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        calendarList = new ArrayList<>();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value
        value = getPersistedString((String)defaultValue);
        getValueCMSDP(true);
        setSummaryCMSDP();
    }

    void getValueCMSDP(boolean notForUnselect)
    {
        //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","notForUnselect="+notForUnselect);

        // change checked state by value
        if (calendarList != null)
        {
            //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","value="+value);
            //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","calendarList.size()="+calendarList.size());
            String[] splits = value.split("\\|");
            for (CalendarEvent calendar : calendarList)
            {
                calendar.checked = false;
                if (notForUnselect) {
                    for (String split : splits) {
                        try {
                            long calendarId = Long.parseLong(split);
                            //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP", "calendar.calendarId=" + calendar.calendarId);
                            //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP", "calendarId=" + calendarId);
                            if (calendar.calendarId == calendarId)
                                calendar.checked = true;
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void setSummaryCMSDP()
    {
        String prefVolumeDataSummary = _context.getString(R.string.calendars_multiselect_summary_text_not_selected);
        if (Permissions.checkCalendar(_context)) {
            if (!value.isEmpty()) {
                String[] splits = value.split("\\|");
                if (splits.length == 1) {
                    boolean found = false;
                    Cursor cur;
                    ContentResolver cr = _context.getContentResolver();
                    Uri uri = Calendars.CONTENT_URI;
                    String selection = Calendars._ID + "=" + splits[0];
                    //noinspection MissingPermission
                    cur = cr.query(uri, CalendarsMultiSelectDialogPreferenceFragmentX.CALENDAR_PROJECTION, selection, null, null);
                    if (cur != null) {
                        //while (cur.moveToNext()) {
                        if (cur.moveToFirst()) {
                            found = true;
                            prefVolumeDataSummary = cur.getString(CalendarsMultiSelectDialogPreferenceFragmentX.PROJECTION_DISPLAY_NAME_INDEX);
                            //break;
                        }
                        cur.close();
                    }
                    if (!found)
                        prefVolumeDataSummary = _context.getString(R.string.calendars_multiselect_summary_text_selected) + ": " + splits.length;
                } else
                    prefVolumeDataSummary = _context.getString(R.string.calendars_multiselect_summary_text_selected) + ": " + splits.length;
            }
        }
        setSummary(prefVolumeDataSummary);
    }

    @SuppressWarnings("SameParameterValue")
    void refreshListView(final boolean notForUnselect) {
        if (fragment != null)
            fragment.refreshListView(notForUnselect);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void persistValue() {
        if (shouldPersist())
        {
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
            persistString(value);

            setSummaryCMSDP();
        }
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final CalendarsMultiSelectDialogPreferenceX.SavedState myState = new CalendarsMultiSelectDialogPreferenceX.SavedState(superState);
        myState.value = value;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(CalendarsMultiSelectDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryCMSDP();
            return;
        }

        // restore instance state
        CalendarsMultiSelectDialogPreferenceX.SavedState myState = (CalendarsMultiSelectDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;

        setSummaryCMSDP();
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<CalendarsMultiSelectDialogPreferenceX.SavedState> CREATOR =
                new Creator<CalendarsMultiSelectDialogPreferenceX.SavedState>() {
                    public CalendarsMultiSelectDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new CalendarsMultiSelectDialogPreferenceX.SavedState(in);
                    }
                    public CalendarsMultiSelectDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new CalendarsMultiSelectDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
