package sk.henrichg.phoneprofilesplus;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.CalendarContract.Calendars;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class CalendarsMultiSelectDialogPreference extends DialogPreference
{

    Context _context = null;;
    String value = "";

    private List<CalendarEvent> calendarList = null;
    MaterialDialog mDialog;

    // Layout widgets.
    private ListView listView = null;
    private LinearLayout linlaProgress;
    private LinearLayout linlaLisView;

    private CalendarsMultiselectPreferenceAdapter listAdapter;

    private static final String[] CALENDAR_PROJECTION = new String[] {
        Calendars._ID,                           // 0
        Calendars.CALENDAR_DISPLAY_NAME,         // 1
        Calendars.CALENDAR_COLOR				 // 2
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_COLOR_INDEX = 2;

    public CalendarsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        calendarList = new ArrayList<>();

    }

    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.pref_dlg_change_selection_button_unselect_all)
                .content(getDialogMessage())
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist())
                        {
                            // sem narvi stringy kontatkov oddelenych |
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
                            mDialog.dismiss();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        value="";
                        refreshListView(false);
                    }
                });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_calendars_multiselect_pref_dialog, null);
        onBindDialogView(layout);

        linlaProgress = (LinearLayout)layout.findViewById(R.id.calendars_multiselect_pref_dlg_linla_progress);
        linlaLisView = (LinearLayout)layout.findViewById(R.id.calendars_multiselect_pref_dlg_linla_listview);
        listView = (ListView)layout.findViewById(R.id.calendars_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                CalendarEvent calendar = (CalendarEvent)listAdapter.getItem(position);
                calendar.toggleChecked();
                CalendarViewHolder viewHolder = (CalendarViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(calendar.checked);
            }
        });

        listAdapter = null;

        mBuilder.customView(layout, false);

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
            CalendarsMultiSelectDialogPreference.this.onShow(dialog);
            }
        });

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    public void refreshListView(final boolean notForUnselect) {

        new AsyncTask<Void, Integer, Void>() {

            List<CalendarEvent> _calendarList = null;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                _calendarList = new ArrayList<>();

                if (notForUnselect) {
                    linlaLisView.setVisibility(View.GONE);
                    linlaProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {

                if (Permissions.checkCalendar(_context)) {
                    Cursor cur;
                    ContentResolver cr = _context.getContentResolver();
                    Uri uri = Calendars.CONTENT_URI;
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
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                calendarList = new ArrayList<>(_calendarList);
                //Log.d("CalendarsMultiSelectDialogPreference.refreshListView","caledarList.size()="+calendarList.size());

                getValueCMSDP(notForUnselect);

                if (listAdapter == null) {
                    listAdapter = new CalendarsMultiselectPreferenceAdapter(_context, calendarList);
                    listView.setAdapter(listAdapter);
                }
                else
                    listAdapter.setCalendarList(calendarList);
                if (notForUnselect) {
                    linlaLisView.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);
                }
            }

        }.execute();
    }

    public void onShow(DialogInterface dialog) {
        if (Permissions.grantCalendarDialogPermissions(_context, this))
            refreshListView(true);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueCMSDP(true);
        }
        else {
            // set state
            // sem narvi default string kontaktov oddeleny |
            value = "";
            persistString("");
        }
        setSummaryCMSDP();
    }

    private void getValueCMSDP(boolean notForUnselect)
    {
        //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","notForUnselect="+notForUnselect);

        if (notForUnselect)
            // Get the persistent value
            value = getPersistedString(value);

        // change checked state by value
        if (calendarList != null)
        {
            //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","value="+value);
            //Log.d("CalendarsMultiSelectDialogPreference.getValueCMSDP","caledarList.size()="+calendarList.size());
            String[] splits = value.split("\\|");
            for (CalendarEvent calendar : calendarList)
            {
                calendar.checked = false;
                if (notForUnselect) {
                    for (int i = 0; i < splits.length; i++) {
                        try {
                            long calendarId = Long.parseLong(splits[i]);
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
                    cur = cr.query(uri, CALENDAR_PROJECTION, selection, null, null);
                    if (cur != null) {
                        //while (cur.moveToNext()) {
                        if (cur.moveToFirst()) {
                            found = true;
                            prefVolumeDataSummary = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
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

}
