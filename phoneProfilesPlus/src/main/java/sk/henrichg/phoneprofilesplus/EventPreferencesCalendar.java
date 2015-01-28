package sk.henrichg.phoneprofilesplus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Instances;
import android.text.format.DateFormat;

public class EventPreferencesCalendar extends EventPreferences {

	public String _calendars;
	public int _searchField;
	public String _searchString;
	
	public long _startTime;
	public long _endTime;
	public boolean _eventFound;
	
	static final String PREF_EVENT_CALENDAR_ENABLED = "eventCalendarEnabled";
	static final String PREF_EVENT_CALENDAR_CALENDARS = "eventCalendarCalendars";
	static final String PREF_EVENT_CALENDAR_SEARCH_FIELD = "eventCalendarSearchField";
	static final String PREF_EVENT_CALENDAR_SEARCH_STRING = "eventCalendarSearchString";
	
	static final int SEARCH_FIELD_TITLE = 0;
	static final int SEARCH_FIELD_DESCRIPTION = 1;
	
	public EventPreferencesCalendar(Event event,
			                    boolean enabled,
								String calendars,
								int searchField,
								String searchString)
	{
		super(event, enabled);

		this._calendars = calendars;
		this._searchField = searchField;
		this._searchString = searchString;
		
		this._startTime = 0;
		this._endTime = 0;
		this._eventFound = false;
	}
	
	@Override
	public void copyPreferences(Event fromEvent)
	{
		this._enabled = ((EventPreferencesCalendar)fromEvent._eventPreferencesCalendar)._enabled;
		this._calendars = ((EventPreferencesCalendar)fromEvent._eventPreferencesCalendar)._calendars;
		this._searchField = ((EventPreferencesCalendar)fromEvent._eventPreferencesCalendar)._searchField;
		this._searchString = ((EventPreferencesCalendar)fromEvent._eventPreferencesCalendar)._searchString;
		
		this._startTime = 0;
		this._endTime = 0;
		this._eventFound = false;
	}
	
	@Override
	public void loadSharedPrefereces(SharedPreferences preferences)
	{
    	Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_CALENDAR_ENABLED, _enabled);
        editor.putString(PREF_EVENT_CALENDAR_CALENDARS, _calendars);
        editor.putString(PREF_EVENT_CALENDAR_SEARCH_FIELD, String.valueOf(_searchField));
        editor.putString(PREF_EVENT_CALENDAR_SEARCH_STRING, _searchString);
		editor.commit();
	}

	@Override
	public void saveSharedPrefereces(SharedPreferences preferences)
	{
		this._enabled = preferences.getBoolean(PREF_EVENT_CALENDAR_ENABLED, false);
		this._calendars = preferences.getString(PREF_EVENT_CALENDAR_CALENDARS, "");
		this._searchField = Integer.parseInt(preferences.getString(PREF_EVENT_CALENDAR_SEARCH_FIELD, "0"));
		this._searchString = preferences.getString(PREF_EVENT_CALENDAR_SEARCH_STRING, "");
		
		this._startTime = 0;
		this._endTime = 0;
		this._eventFound = false;
	}
	
	@Override
	public String getPreferencesDescription(String description, Context context)
	{
		String descr = description;

		if (!this._enabled)
		{
			//descr = descr + context.getString(R.string.event_type_calendar) + ": ";
			//descr = descr + context.getString(R.string.event_preferences_not_enabled);
		}
		else
		{
			descr = descr + context.getString(R.string.event_type_calendar) + ": ";
			
			String[] searchFields = context.getResources().getStringArray(R.array.eventCalendarSearchFieldArray);
			descr = descr + searchFields[this._searchField] + "; ";
			
			descr = descr + "\"" + this._searchString + "\""; 
			
	        //Calendar calendar = Calendar.getInstance();
	
	   		if (GlobalData.getGlobalEventsRuning(context))
	   		{
	   			if (_eventFound)
	   			{
		   			long alarmTime;
		   		    //SimpleDateFormat sdf = new SimpleDateFormat("EEd/MM/yy HH:mm");
		   		    String alarmTimeS = "";
		   			if (_event.getStatus() == Event.ESTATUS_PAUSE)
		   			{
		   				alarmTime = computeAlarm(true);
		   				// date and time format by user system settings configuration
		   	   		    alarmTimeS = "(st) " + DateFormat.getDateFormat(context).format(alarmTime) +
		   	   		    			 " " + DateFormat.getTimeFormat(context).format(alarmTime);
		   	   		    descr = descr + '\n';
		   	   		    descr = descr + "-> " + alarmTimeS;
		   			}
		   			else
		   			if (_event.getStatus() == Event.ESTATUS_RUNNING)
		   			{
		   				alarmTime = computeAlarm(false);
		   				// date and time format by user system settings configuration
		   	   		    alarmTimeS = "(et) " + DateFormat.getDateFormat(context).format(alarmTime) +
		   	   		    			 " " + DateFormat.getTimeFormat(context).format(alarmTime);
		   	   		    descr = descr + '\n';
		   	   		    descr = descr + "-> " + alarmTimeS;
		   			}
	   			}
	   			else
	   			{
	   	   		    descr = descr + '\n';
	   	   		    descr = descr + "-> " + context.getResources().getString(R.string.event_preferences_calendar_no_event);
	   			}
	   		}
		}
   		
		return descr;
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
	{
		if (key.equals(PREF_EVENT_CALENDAR_CALENDARS))
		{
			Preference preference = prefMng.findPreference(key);
	    	GUIData.setPreferenceTitleStyle(preference, false, true);
		}
		if (key.equals(PREF_EVENT_CALENDAR_SEARCH_FIELD))
		{	
			ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
			int index = listPreference.findIndexOfValue(value);
			CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
			listPreference.setSummary(summary);
		}
		if (key.equals(PREF_EVENT_CALENDAR_SEARCH_STRING))
		{
			Preference preference = prefMng.findPreference(key);
			preference.setSummary(value);
	    	GUIData.setPreferenceTitleStyle(preference, false, true);
		}
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
	{
		if (key.equals(PREF_EVENT_CALENDAR_CALENDARS) ||
			key.equals(PREF_EVENT_CALENDAR_SEARCH_FIELD) || 
			key.equals(PREF_EVENT_CALENDAR_SEARCH_STRING))
		{
			setSummary(prefMng, key, preferences.getString(key, ""), context);
		}
	}
	
	@Override
	public void setAllSummary(PreferenceManager prefMng, Context context)
	{
		setSummary(prefMng, PREF_EVENT_CALENDAR_CALENDARS, _calendars, context);
		setSummary(prefMng, PREF_EVENT_CALENDAR_SEARCH_FIELD, Integer.toString(_searchField), context);
		setSummary(prefMng, PREF_EVENT_CALENDAR_SEARCH_STRING, _searchString, context);
	}
	
	@Override
	public boolean isRunable()
	{
		
		boolean runable = super.isRunable();

		runable = runable && (!_calendars.isEmpty());
		runable = runable && (!_searchString.isEmpty());

		return runable;
	}
	
	@Override
	public boolean activateReturnProfile()
	{
		return true;
	}
	
	public long computeAlarm(boolean startEvent)
	{
		GlobalData.logE("EventPreferencesCalendar.computeAlarm","startEvent="+startEvent);

		///// set calendar for startTime and endTime
		Calendar calStartTime = Calendar.getInstance();
		Calendar calEndTime = Calendar.getInstance();

		int gmtOffset = TimeZone.getDefault().getRawOffset();
		
		calStartTime.setTimeInMillis(_startTime - gmtOffset);
		calStartTime.set(Calendar.SECOND, 0);
		calStartTime.set(Calendar.MILLISECOND, 0);

		calEndTime.setTimeInMillis(_endTime - gmtOffset);
		calEndTime.set(Calendar.SECOND, 0);
		calEndTime.set(Calendar.MILLISECOND, 0);

		long alarmTime;
		if (startEvent)
			alarmTime = calStartTime.getTimeInMillis();
		else
			alarmTime = calEndTime.getTimeInMillis();
	    
	    return alarmTime;
		
	}
	
	@Override
	public void setSystemRunningEvent(Context context)
	{
		// set alarm for state PAUSE
		
		// this alarm generates broadcast, that change state into RUNNING;
		// from broadcast will by called EventsService
		

		removeAlarm(context);
		
		searchEvent(context);
		
		if (!(isRunable() && _enabled && _eventFound)) 
			return;

		setAlarm(true, computeAlarm(true), context);
	}

	@Override
	public void setSystemPauseEvent(Context context)
	{
		// set alarm for state RUNNING

		// this alarm generates broadcast, that change state into PAUSE;
		// from broadcast will by called EventsService

		removeAlarm(context);

		searchEvent(context);
		
		if (!(isRunable() && _enabled && _eventFound)) 
			return;
		
		setAlarm(false, computeAlarm(false), context);
	}
	
	@Override
	public void removeSystemEvent(Context context)
	{
		// remove alarms for state STOP

		removeAlarm(context);
		
		GlobalData.logE("EventPreferencesCalendar.removeSystemEvent","xxx");
	}

	public void removeAlarm(Context context)
	{
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

		Intent intent = new Intent(context, EventsCalendarBroadcastReceiver.class);
	    
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
       		GlobalData.logE("EventPreferencesCalendar.removeAlarm","alarm found");
        		
        	alarmManager.cancel(pendingIntent);
        	pendingIntent.cancel();
        }
	}
	
	@SuppressLint("SimpleDateFormat")
	private void setAlarm(boolean startEvent, long alarmTime, Context context)
	{
	    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
	    String result = sdf.format(alarmTime);
	    if (startEvent)
	    	GlobalData.logE("EventPreferencesCalendar.setAlarm","startTime="+result);
	    else
	    	GlobalData.logE("EventPreferencesCalendar.setAlarm","endTime="+result);
	    
	    Intent intent = new Intent(context, EventsCalendarBroadcastReceiver.class);
	    intent.putExtra(GlobalData.EXTRA_EVENT_ID, _event._id);
	    intent.putExtra(GlobalData.EXTRA_START_SYSTEM_EVENT, startEvent);
	    
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

	}

	public void searchEvent(Context context)
	{
		if (!(isRunable() && _enabled))
		{
			_startTime = 0;
			_endTime = 0;
			_eventFound = false;
	    	DatabaseHandler.getInstance(context).updateEventCalendarTimes(_event);
			return;
		}

	    //Log.e("EventPreferencesCalendar.searchEvent", "is runnable");
		
	    final String[] INSTANCE_PROJECTION = new String[] {
	        Instances.BEGIN,         // 0
	        Instances.END,			 // 1
	        Instances.TITLE,         // 2
	        Instances.DESCRIPTION,   // 3
	        Instances.CALENDAR_ID,   // 4
	        Instances.ALL_DAY/*,       // 5
	        Instances.EVENT_TIMEZONE // 6 */
	    };
	      
	    // The indices for the projection array above.
	    final int PROJECTION_BEGIN_INDEX = 0;
	    final int PROJECTION_END_INDEX = 1;
	    //final int PROJECTION_TITLE_INDEX = 2;	    
	    //final int PROJECTION_DESCRIPTION_INDEX = 3;	    
	    //final int PROJECTION_CALENDAR_ID_INDEX = 4;
	    final int PROJECTION_ALL_DAY_INDEX = 5;
	    //final int PROJECTION_EVENT_TIMEZONE_INDEX = 6;

	    Cursor cur = null;
	    ContentResolver cr = context.getContentResolver();

		String selection =  "(    (" + Instances.CALENDAR_ID + " = ?)"; 
		if (_searchField == SEARCH_FIELD_TITLE)
			selection = selection + 
							" AND (lower("+Instances.TITLE+")" + " LIKE lower(?))";
		else
		if (_searchField == SEARCH_FIELD_DESCRIPTION)
					selection = selection + 
					        " AND (lower("+Instances.DESCRIPTION+")" + " LIKE lower(?))";
		selection = selection + ")";
	    //Log.e("EventPreferencesCalendar.searchEvent", "selection="+selection);
		
	    
	    // Construct the query with the desired date range.
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		long startMillis = calendar.getTimeInMillis();
		calendar.add(Calendar.DAY_OF_YEAR, 32);
		long endMillis = calendar.getTimeInMillis();

	    Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
	    ContentUris.appendId(builder, startMillis);
	    ContentUris.appendId(builder, endMillis);

	    _eventFound = false;
    	_startTime = 0;
    	_endTime = 0;
	    
		String[] splits = _calendars.split("\\|");
		for (int i = 0; i < splits.length; i++)
		{
			long calendarId = Long.parseLong(splits[i]);

			String[] selectionArgs = new String[] { String.valueOf(calendarId), "%"+_searchString+"%" };
		    
		    // Submit the query
		    cur =  cr.query(builder.build(), INSTANCE_PROJECTION, selection, selectionArgs, Instances.BEGIN + " ASC");
		    
		    if (cur != null)
		    {
			    while (cur.moveToNext()) {
			        long beginVal = 0;
			        long endVal = 0;
			        //String title = null;
			        
			        // Get the field values
			        beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
			        endVal = cur.getLong(PROJECTION_END_INDEX);
			        
			        if (cur.getInt(PROJECTION_ALL_DAY_INDEX) == 1)
			        {
			        	// get UTC offset
			        	Date _now = new Date();
			    		int utcOffset = TimeZone.getDefault().getOffset(_now.getTime());
			    		
			    		beginVal -= utcOffset;
			    		endVal -= utcOffset;
			        }
			        
			        //title = cur.getString(PROJECTION_TITLE_INDEX);
	
				    //Log.e("EventPreferencesCalendar.searchEvent", "beginVal="+getDate(beginVal));
				    //Log.e("EventPreferencesCalendar.searchEvent", "endVal="+getDate(endVal));
				    //Log.e("EventPreferencesCalendar.searchEvent", "title="+title);
	
		    		int gmtOffset = TimeZone.getDefault().getRawOffset();
				    
				    if ((beginVal <= now) && (endVal > now))
				    {
				    	// event instance is found
					    //Log.e("EventPreferencesCalendar.searchEvent", "found 1");
				    	_eventFound = true;
				    	_startTime = beginVal + gmtOffset;
				    	_endTime = endVal + gmtOffset;
				    	break;
				    }
				    else
				    if (beginVal > now)
				    {
				    	// event instance is found
					    //Log.e("EventPreferencesCalendar.searchEvent", "found 2");
				    	_eventFound = true;
				    	_startTime = beginVal + gmtOffset;
				    	_endTime = endVal + gmtOffset;
				    	break;
				    }
				    //else
				    //	Log.e("EventPreferencesCalendar.searchEvent", "not found");
				    
			    }
			    
			    cur.close();
		    }
		    
		    if (_eventFound)
		    {
		    	break;
		    }
		}

    	DatabaseHandler.getInstance(context).updateEventCalendarTimes(_event);
		
	}
	
	@SuppressLint("SimpleDateFormat")
	private static String getDate(long milliSeconds)
	{
	    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(milliSeconds);
	    return formatter.format(calendar.getTime());
	}	
}
