package sk.henrichg.phoneprofilesplus;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.provider.CalendarContract.Calendars;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class CalendarsMultiSelectDialogPreference extends DialogPreference
{

	Context _context = null;
	String value = "";
	
	List<CalendarEvent> calendarList = null;	
	
	// Layout widgets.
	private ListView listView = null;
	private LinearLayout linlaProgress;

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
		
		calendarList = new ArrayList<CalendarEvent>();

	}

	protected View onCreateDialogView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());

		View view = layoutInflater.inflate(
			R.layout.activity_calendars_multiselect_pref_dialog, null);
		
		linlaProgress = (LinearLayout)view.findViewById(R.id.calendars_multiselect_pref_dlg_linla_progress);
		listView = (ListView)view.findViewById(R.id.calendars_multiselect_pref_dlg_listview);
		
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

		new AsyncTask<Void, Integer, Void>() {

			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				linlaProgress.setVisibility(View.VISIBLE);
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				
				calendarList.clear();
				
				Cursor cur = null;
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
				cur = cr.query(uri, CALENDAR_PROJECTION, null, null, null);
				while (cur.moveToNext()) {
				    long calID = 0;
				    String displayName = null;
				    int color = 0;
				      
				    // Get the field values
				    calID = cur.getLong(PROJECTION_ID_INDEX);
				    displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
				    color = cur.getInt(PROJECTION_COLOR_INDEX);
				              
					CalendarEvent aCalendar = new CalendarEvent();
					aCalendar.calendarId = calID;
					aCalendar.name = displayName;
					aCalendar.color = color;
					
					calendarList.add(aCalendar);

				}
				cur.close();
				
				getValueCMSDP();
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result)
			{
				super.onPostExecute(result);
				
				listAdapter = new CalendarsMultiselectPreferenceAdapter(_context, calendarList);
			    listView.setAdapter(listAdapter);
				linlaProgress.setVisibility(View.GONE);
			}
			
		}.execute();
		
		return view;
	}

	public void onClick(DialogInterface dialog, int which) {
		// if the positive button is clicked, we persist the value.
		if (which == DialogInterface.BUTTON_POSITIVE) {
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
			}
		}
		super.onClick(dialog, which);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		if (restoreValue) {
			// restore state
			getValueCMSDP();
		}
		else {
			// set state
			// sem narvi default string kontaktov oddeleny |
			value = "";
			persistString("");
		}
		setSummaryCMSDP();
	}
	
	private void getValueCMSDP()
	{
		// Get the persistent value
		value = getPersistedString(value);
		
		// change checked state by value
		if (calendarList != null)
		{
			String[] splits = value.split("\\|");
			for (CalendarEvent calendar : calendarList)
			{
				calendar.checked = false;
				for (int i = 0; i < splits.length; i++)
				{
					try {
						long calendarId = Long.parseLong(splits[i]);
						if (calendar.calendarId == calendarId)
							calendar.checked = true;
					} catch (Exception e) {
					}
				}
			}
		}
	}
	
	private void setSummaryCMSDP()
	{
		String prefVolumeDataSummary = _context.getString(R.string.calendars_multiselect_summary_text_not_selected);
		if (!value.isEmpty())
			prefVolumeDataSummary = _context.getString(R.string.calendars_multiselect_summary_text_selected);
		setSummary(prefVolumeDataSummary);
	}
	
}
