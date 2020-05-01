package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class CalendarsMultiSelectDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private CalendarsMultiSelectDialogPreferenceX preference;

    // Layout widgets.
    private ListView listView = null;
    private LinearLayout linlaProgress;
    private RelativeLayout rellaData;

    private CalendarsMultiSelectPreferenceAdapterX listAdapter;

    private AsyncTask asyncTask = null;

    static final String[] CALENDAR_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
            CalendarContract.Calendars.CALENDAR_COLOR                 // 2
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_COLOR_INDEX = 2;


    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (CalendarsMultiSelectDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_calendars_multiselect_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.calendars_multiselect_pref_dlg_linla_progress);
        rellaData = view.findViewById(R.id.calendars_multiselect_pref_dlg_rella_data);
        listView = view.findViewById(R.id.calendars_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                CalendarEvent calendar = (CalendarEvent)listAdapter.getItem(position);
                calendar.toggleChecked();
                CalendarViewHolder viewHolder = (CalendarViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(calendar.checked);
            }
        });

        final Button unselectAllButton = view.findViewById(R.id.calendars_multiselect_pref_dlg_unselect_all);
        unselectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preference.value="";
                refreshListView(false);
            }
        });

        if (Permissions.grantCalendarDialogPermissions(prefContext))
            refreshListView(true);

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        preference.fragment = null;
    }

    @SuppressLint("StaticFieldLeak")
    void refreshListView(final boolean notForUnselect) {
        asyncTask = new AsyncTask<Void, Integer, Void>() {

            List<CalendarEvent> _calendarList = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                _calendarList = new ArrayList<>();

                if (notForUnselect) {
                    rellaData.setVisibility(View.GONE);
                    linlaProgress.setVisibility(View.VISIBLE);
                }
            }

            @SuppressLint("MissingPermission")
            @Override
            protected Void doInBackground(Void... params) {

                if (Permissions.checkCalendar(prefContext)) {
                    Cursor cur;
                    ContentResolver cr = prefContext.getContentResolver();
                    Uri uri = CalendarContract.Calendars.CONTENT_URI;
                    /*
                    String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                                            + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                                            + Calendars.OWNER_ACCOUNT + " = ?))";
                    String[] selectionArgs = new String[] {"sampleuser@gmail.com", "com.google",
                            "sampleuser@gmail.com"};
                    */
                    // Submit the query and get a Cursor object back.
                    //cur = cr.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);
                    //noinspection MissingPermission
                    cur = cr.query(uri, CALENDAR_PROJECTION, null, null, null);
                    if (cur != null) {
                        while (cur.moveToNext()) {
                            long calID;
                            String displayName;
                            int color;

                            // Get the field values
                            calID = cur.getLong(PROJECTION_ID_INDEX);
                            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
                            color = cur.getInt(PROJECTION_COLOR_INDEX);

                            CalendarEvent aCalendar = new CalendarEvent();
                            aCalendar.calendarId = calID;
                            aCalendar.name = displayName;
                            aCalendar.color = color;

                            _calendarList.add(aCalendar);

                        }
                        cur.close();
                    }

                    getValueCMSDP(_calendarList, notForUnselect);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                preference.calendarList = new ArrayList<>(_calendarList);
                //Log.d("CalendarsMultiSelectDialogPreference.refreshListView","calendarList.size()="+calendarList.size());

                if (listAdapter == null) {
                    listAdapter = new CalendarsMultiSelectPreferenceAdapterX(prefContext, preference.calendarList);
                    listView.setAdapter(listAdapter);
                } else
                    listAdapter.setCalendarList(preference.calendarList);
                if (notForUnselect) {
                    rellaData.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);
                }
            }

        }.execute();
    }

    private void getValueCMSDP(List<CalendarEvent> _calendarList, boolean notForUnselect)
    {
        //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","notForUnselect="+notForUnselect);

        //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","value="+value);
        //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","calendarList.size()="+calendarList.size());
        String[] splits = preference.value.split("\\|");
        for (CalendarEvent calendar : _calendarList)
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
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }
                }
            }
        }

        // move checked on top
        int i = 0;
        int ich = 0;
        while (i < _calendarList.size()) {
            CalendarEvent aCalendar = _calendarList.get(i);
            if (aCalendar.checked) {
                _calendarList.remove(i);
                _calendarList.add(ich, aCalendar);
                ich++;
            }
            i++;
        }
    }

}
