package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CalendarsMultiSelectDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private CalendarsMultiSelectDialogPreference preference;

    // Layout widgets.
    private ListView listView = null;
    private LinearLayout linlaProgress;
    private LinearLayout rellaData;
    RelativeLayout emptyList;
    private Button unselectAllButton;

    private CalendarsMultiSelectPreferenceAdapter listAdapter;

    private RefreshListViewAsyncTask asyncTask = null;

    static final String[] CALENDAR_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
            CalendarContract.Calendars.CALENDAR_COLOR                 // 2
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_COLOR_INDEX = 2;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (CalendarsMultiSelectDialogPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_calendars_multiselect_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        linlaProgress = view.findViewById(R.id.calendars_multiselect_pref_dlg_linla_progress);
        rellaData = view.findViewById(R.id.calendars_multiselect_pref_dlg_rella_data);
        listView = view.findViewById(R.id.calendars_multiselect_pref_dlg_listview);
        emptyList = view.findViewById(R.id.calendars_multiselect_pref_dlg_empty);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            CalendarEvent calendar = (CalendarEvent)listAdapter.getItem(position);
            calendar.toggleChecked();
            CalendarViewHolder viewHolder = (CalendarViewHolder) item.getTag();
            viewHolder.checkBox.setChecked(calendar.checked);
        });

        unselectAllButton = view.findViewById(R.id.calendars_multiselect_pref_dlg_unselect_all);
        //noinspection DataFlowIssue
        unselectAllButton.setOnClickListener(v -> {
            preference.value="";
            refreshListView(false);
        });

        if (Permissions.grantCalendarDialogPermissions(prefContext)) {
            if (preference.calendarList != null)
                preference.calendarList.clear();
            if (listAdapter != null)
                listAdapter.notifyDataSetChanged();
            final Handler handler = new Handler(prefContext.getMainLooper());
            final WeakReference<CalendarsMultiSelectDialogPreferenceFragment> fragmentWeakRef
                    = new WeakReference<>(this);
            handler.postDelayed(() -> {
                CalendarsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
                if (fragment != null)
                    fragment.refreshListView(true);
            }, 200);
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask.cancel(true);
        asyncTask = null;

        preference.fragment = null;
    }

    void refreshListView(final boolean notForUnselect) {
        asyncTask = new RefreshListViewAsyncTask(notForUnselect,
                            preference, this, prefContext) ;
        asyncTask.execute();
    }

    private void getValueCMSDP(List<CalendarEvent> _calendarList, boolean notForUnselect)
    {
        //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","notForUnselect="+notForUnselect);

        //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","value="+value);
        //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","calendarList.size()="+calendarList.size());
        String[] splits = preference.value.split(StringConstants.STR_SPLIT_REGEX);
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
                        //PPApplicationStatic.recordException(e);
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

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        List<CalendarEvent> _calendarList = null;

        final boolean notForUnselect;

        private final WeakReference<CalendarsMultiSelectDialogPreference> preferenceWeakRef;
        private final WeakReference<CalendarsMultiSelectDialogPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final boolean notForUnselect,
                                        CalendarsMultiSelectDialogPreference preference,
                                        CalendarsMultiSelectDialogPreferenceFragment fragment,
                                        Context prefContext) {
            this.notForUnselect = notForUnselect;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            _calendarList = new ArrayList<>();

            CalendarsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (notForUnselect) {
                    fragment.rellaData.setVisibility(View.GONE);
                    fragment.linlaProgress.setVisibility(View.VISIBLE);
                }
                fragment.unselectAllButton.setEnabled(false);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            CalendarsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            CalendarsMultiSelectDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

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

                    fragment.getValueCMSDP(_calendarList, notForUnselect);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            CalendarsMultiSelectDialogPreferenceFragment fragment = fragmentWeakRef.get();
            CalendarsMultiSelectDialogPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                fragment.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(prefContext.getMainLooper());
                handler.post(() -> {
                    fragment.rellaData.setVisibility(View.VISIBLE);

                    preference.calendarList = new ArrayList<>(_calendarList);
                    //Log.d("CalendarsMultiSelectDialogPreference.refreshListView","calendarList.size()="+calendarList.size());

                    if (fragment.listAdapter == null) {
                        fragment.listAdapter = new CalendarsMultiSelectPreferenceAdapter(prefContext, preference.calendarList);
                        fragment.listView.setAdapter(fragment.listAdapter);
                    } else
                        fragment.listAdapter.setCalendarList(preference.calendarList);
                    if (notForUnselect) {
                        if (preference.calendarList.isEmpty()) {
                            fragment.listView.setVisibility(View.GONE);
                            fragment.emptyList.setVisibility(View.VISIBLE);
                        } else {
                            fragment.emptyList.setVisibility(View.GONE);
                            fragment.listView.setVisibility(View.VISIBLE);
                        }
                    }

                    fragment.unselectAllButton.setEnabled(true);
                });
            }
        }

    }

}
