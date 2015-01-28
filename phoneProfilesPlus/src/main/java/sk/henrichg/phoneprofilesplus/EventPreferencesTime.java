package sk.henrichg.phoneprofilesplus;

import java.sql.Date;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

public class EventPreferencesTime extends EventPreferences {

	public boolean _sunday;
	public boolean _monday;
	public boolean _tuesday;
	public boolean _wendesday;
	public boolean _thursday;
	public boolean _friday;
	public boolean _saturday;
	public long _startTime;
	public long _endTime;
	public boolean _useEndTime;
	
	static final String PREF_EVENT_TIME_ENABLED = "eventTimeEnabled";
	static final String PREF_EVENT_TIME_DAYS = "eventTimeDays";
	static final String PREF_EVENT_TIME_START_TIME = "eventTimeStartTime";
	static final String PREF_EVENT_TIME_END_TIME = "eventTimeEndTime";
	static final String PREF_EVENT_TIME_USE_END_TIME = "eventTimeUseEndTime";
	
	public EventPreferencesTime(Event event,
			                    boolean enabled,
								boolean sunday,
								boolean monday,
								boolean tuesday,
								boolean wendesday,
								boolean thursday,
								boolean friday,
								boolean saturday,
								long startTime,
								long endTime,
								boolean useEndTime)
	{
		super(event, enabled);

		this._sunday = sunday;
		this._monday = monday;
		this._tuesday = tuesday;
		this._wendesday = wendesday;
		this._thursday = thursday;
		this._friday = friday;
		this._saturday = saturday;
		this._startTime = startTime;
		this._endTime = endTime;
		this._useEndTime = useEndTime;
	}
	
	@Override
	public void copyPreferences(Event fromEvent)
	{
		this._enabled = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._enabled;
		this._sunday = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._sunday;
		this._monday = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._monday;
		this._tuesday = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._tuesday;
		this._wendesday = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._wendesday;
		this._thursday = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._thursday;
		this._friday = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._friday;
		this._saturday = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._saturday;
		this._startTime = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._startTime;
		this._endTime = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._endTime;
		this._useEndTime = ((EventPreferencesTime)fromEvent._eventPreferencesTime)._useEndTime;
	}
	
	@Override
	public void loadSharedPrefereces(SharedPreferences preferences)
	{
    	Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_TIME_ENABLED, _enabled);
    	String sValue = "";
    	if (this._sunday) sValue = sValue + "0|";
    	if (this._monday) sValue = sValue + "1|";
    	if (this._tuesday) sValue = sValue + "2|";
    	if (this._wendesday) sValue = sValue + "3|";
    	if (this._thursday) sValue = sValue + "4|";
    	if (this._friday) sValue = sValue + "5|";
    	if (this._saturday) sValue = sValue + "6|";
		//Log.e("EventPreferencesTime.loadSharedPreferences",sValue);

    	int gmtOffset = TimeZone.getDefault().getRawOffset();
    	
        editor.putString(PREF_EVENT_TIME_DAYS, sValue);
        editor.putLong(PREF_EVENT_TIME_START_TIME, this._startTime - gmtOffset);
        editor.putLong(PREF_EVENT_TIME_END_TIME, this._endTime - gmtOffset);
        editor.putBoolean(PREF_EVENT_TIME_USE_END_TIME, this._useEndTime);
		editor.commit();
	}

	@Override
	public void saveSharedPrefereces(SharedPreferences preferences)
	{
		this._enabled = preferences.getBoolean(PREF_EVENT_TIME_ENABLED, false);
		
		String sDays = preferences.getString(PREF_EVENT_TIME_DAYS, DaysOfWeekPreference.allValue);
		//Log.e("EventPreferencesTime.saveSharedPreferences",sDays);
		String[] splits = sDays.split("\\|");
		if (splits[0].equals(DaysOfWeekPreference.allValue))
		{
			this._sunday = true;
			this._monday = true;
			this._tuesday = true;
			this._wendesday = true;
			this._thursday = true;
			this._friday = true;
			this._saturday = true;
		}
		else
		{
			this._sunday = false;
			this._monday = false;
			this._tuesday = false;
			this._wendesday = false;
			this._thursday = false;
			this._friday = false;
			this._saturday = false;
			for (String value : splits)
			{
				this._sunday = this._sunday || value.equals("0");
				this._monday = this._monday || value.equals("1");
				this._tuesday = this._tuesday || value.equals("2");
				this._wendesday = this._wendesday || value.equals("3");
				this._thursday = this._thursday || value.equals("4");
				this._friday = this._friday || value.equals("5");
				this._saturday = this._saturday || value.equals("6");
			}
		}
		
		int gmtOffset = TimeZone.getDefault().getRawOffset();
		
		this._startTime = preferences.getLong(PREF_EVENT_TIME_START_TIME, System.currentTimeMillis()) + gmtOffset;
		this._endTime = preferences.getLong(PREF_EVENT_TIME_END_TIME, System.currentTimeMillis()) + gmtOffset;
		this._useEndTime = preferences.getBoolean(PREF_EVENT_TIME_USE_END_TIME, false);
	}
	
	@Override
	public String getPreferencesDescription(String description, Context context)
	{
		String descr = description;

		if (!this._enabled)
		{
			//descr = descr + context.getString(R.string.event_type_time) + ": ";
			//descr = descr + context.getString(R.string.event_preferences_not_enabled);
		}
		else
		{
			descr = descr + context.getString(R.string.event_type_time) + ": ";
			
	    	boolean[] daySet = new boolean[7];
			daySet[0] = this._sunday;
			daySet[1] = this._monday;
			daySet[2] = this._tuesday;
			daySet[3] = this._wendesday;
			daySet[4] = this._thursday;
			daySet[5] = this._friday;
			daySet[6] = this._saturday;
	    	
			boolean allDays = true;
	    	for (int i = 0; i < 7; i++)
	    		allDays = allDays && daySet[i]; 
			
	    	if (allDays)
	    	{
	    		descr = descr + context.getString(R.string.array_pref_event_all);
	    		descr = descr + " ";
	    	}
	    	else
	    	{
		    	String[] namesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();
		    	
		    	int dayOfWeek;
		    	for (int i = 0; i < 7; i++)
		    	{
		    		dayOfWeek = getDayOfWeekByLocale(i);
		    		
		    		if (daySet[dayOfWeek])
		    			descr = descr + namesOfDay[dayOfWeek+1] + " ";
		    	}
	    	}
	
	    	int gmtOffset = TimeZone.getDefault().getRawOffset();
	    	
	        Calendar calendar = Calendar.getInstance();
	
	        calendar.setTimeInMillis(this._startTime - gmtOffset);
			descr = descr + "- ";
			descr = descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
			if (this._useEndTime)
			{
		        calendar.setTimeInMillis(this._endTime - gmtOffset);
				descr = descr + "-";
				descr = descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
			}
			
			
	   		if (GlobalData.getGlobalEventsRuning(context))
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
	   			if ((_event.getStatus() == Event.ESTATUS_RUNNING) && _useEndTime)
	   			{
	   				alarmTime = computeAlarm(false);
	   				// date and time format by user system settings configuration
	   	   		    alarmTimeS = "(et) " + DateFormat.getDateFormat(context).format(alarmTime) +
	   	   		    			 " " + DateFormat.getTimeFormat(context).format(alarmTime);
	   	   		    descr = descr + '\n';
	   	   		    descr = descr + "-> " + alarmTimeS;
	   			}
	   		}
		}
   		
		return descr;
	}
	
    // dayOfWeek: value are (for exapmple) Calendar.SUNDAY-1
    // return: value are (for exapmple) Calendar.MONDAY-1
    public static int getDayOfWeekByLocale(int dayOfWeek)
    {
    	
    	Calendar cal = Calendar.getInstance(); 
    	int firstDayOfWeek = cal.getFirstDayOfWeek();
    	
    	int resDayOfWeek = dayOfWeek + (firstDayOfWeek-1);
    	if (resDayOfWeek > 6)
    		resDayOfWeek = resDayOfWeek - 7;

    	//Log.e("DaysOfWeekPreference.getDayOfWeekByLocale","resDayOfWeek="+resDayOfWeek);
    	
    	return resDayOfWeek;
    }

	@Override
	public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
	{
		if (key.equals(PREF_EVENT_TIME_DAYS))
		{
			Preference preference = prefMng.findPreference(key);
	    	GUIData.setPreferenceTitleStyle(preference, false, true);
		}
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
	{
		if (key.equals(PREF_EVENT_TIME_DAYS))
		{
			setSummary(prefMng, key, preferences.getString(key, ""), context);
		}
	}
	
	@Override
	public void setAllSummary(PreferenceManager prefMng, Context context)
	{
		setSummary(prefMng, PREF_EVENT_TIME_DAYS, "", context);
	}
    
	@Override
	public boolean isRunable()
	{
		
		boolean runable = super.isRunable();

		boolean dayOfWeek = false;
		dayOfWeek = dayOfWeek || this._sunday;
		dayOfWeek = dayOfWeek || this._monday;
		dayOfWeek = dayOfWeek || this._tuesday;
		dayOfWeek = dayOfWeek || this._wendesday;
		dayOfWeek = dayOfWeek || this._thursday;
		dayOfWeek = dayOfWeek || this._friday;
		dayOfWeek = dayOfWeek || this._saturday;
		runable = runable && dayOfWeek;

		return runable;
	}
	
	@Override
	public boolean activateReturnProfile()
	{
		return _useEndTime;
	}
	
	public long computeAlarm(boolean startEvent)
	{
		GlobalData.logE("EventPreferencesTime.computeAlarm","startEvent="+startEvent);

		boolean[] daysOfWeek =  new boolean[8];
		daysOfWeek[Calendar.SUNDAY] = this._sunday;
		daysOfWeek[Calendar.MONDAY] = this._monday;
		daysOfWeek[Calendar.TUESDAY] = this._tuesday;
		daysOfWeek[Calendar.WEDNESDAY] = this._wendesday;
		daysOfWeek[Calendar.THURSDAY] = this._thursday;
		daysOfWeek[Calendar.FRIDAY] = this._friday;
		daysOfWeek[Calendar.SATURDAY] = this._saturday;
		
		Calendar now = Calendar.getInstance();
		
		///// set calendar for startTime and endTime
		Calendar calStartTime = Calendar.getInstance();
		Calendar calEndTime = Calendar.getInstance();

		int gmtOffset = TimeZone.getDefault().getRawOffset();
		
		calStartTime.setTimeInMillis(_startTime - gmtOffset);
		calStartTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
		calStartTime.set(Calendar.MONTH, now.get(Calendar.MONTH)); 
		calStartTime.set(Calendar.YEAR,  now.get(Calendar.YEAR));
		calStartTime.set(Calendar.SECOND, 0);
		calStartTime.set(Calendar.MILLISECOND, 0);

		long computedEndTime = _endTime - gmtOffset;
		if (!_useEndTime)
			computedEndTime = (_startTime - gmtOffset) + (5 * 1000);
		calEndTime.setTimeInMillis(computedEndTime);
		calEndTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
		calEndTime.set(Calendar.MONTH, now.get(Calendar.MONTH)); 
		calEndTime.set(Calendar.YEAR,  now.get(Calendar.YEAR));
		calEndTime.set(Calendar.SECOND, 0);
		calEndTime.set(Calendar.MILLISECOND, 0);

		if (calStartTime.getTimeInMillis() >= calEndTime.getTimeInMillis())
	    {
			// endTime is over midnight
			GlobalData.logE("EventPreferencesTime.computeAlarm","startTime >= endTime");
			
			if (now.getTimeInMillis() < calEndTime.getTimeInMillis())
			{
				// now is before endTime
				// decrease start/end time
				calStartTime.add(Calendar.DAY_OF_YEAR, -1);
				calEndTime.add(Calendar.DAY_OF_YEAR, -1);
			}
				
			calEndTime.add(Calendar.DAY_OF_YEAR, 1);
	    }
		
		if (calEndTime.getTimeInMillis() < now.getTimeInMillis())
		{
			// endTime is before actual time, compute for future
			calStartTime.add(Calendar.DAY_OF_YEAR, 1);
			calEndTime.add(Calendar.DAY_OF_YEAR, 1);
		}	
		////////////////////////////

		//// update calendar for startTime a endTime by selected day of week
		int startDayOfWeek = calStartTime.get(Calendar.DAY_OF_WEEK);
		if (daysOfWeek[startDayOfWeek])
		{
			// startTime of week is selected
			GlobalData.logE("EventPreferencesTime.computeAlarm","startTime of week is selected");
		}
		else
		{
			// startTime of week is not selected,
			GlobalData.logE("EventPreferencesTime.computeAlarm","startTime of week is NOT selected");
			GlobalData.logE("EventPreferencesTime.computeAlarm","startDayOfWeek="+startDayOfWeek);
			
			// search for selected day of week
			boolean found = false;
			int daysToAdd = 0;
			for (int i = startDayOfWeek+1; i < 8; i++)
			{
				++daysToAdd;
				if (daysOfWeek[i])
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				for (int i = 1; i < startDayOfWeek; i++)
				{
					++daysToAdd;
					if (daysOfWeek[i])
					{
						found = true;
						break;
					}
				}
			}
			if (found)
			{
				GlobalData.logE("EventPreferencesTime.computeAlarm","daysToAdd="+daysToAdd);
				calStartTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
				calEndTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
			}
		}
		//////////////////////

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
		
		if (!(isRunable() && _enabled)) 
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
		
		if (!(isRunable() && _enabled)) 
			return;
		
		setAlarm(false, computeAlarm(false), context);
	}
	
	@Override
	public void removeSystemEvent(Context context)
	{
		// remove alarms for state STOP

		removeAlarm(context);
		
		GlobalData.logE("EventPreferencesTime.removeSystemEvent","xxx");
	}

	public void removeAlarm(Context context)
	{
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

		Intent intent = new Intent(context, EventsTimeBroadcastReceiver.class);
	    
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
       		GlobalData.logE("EventPreferencesTime.removeAlarm","alarm found");
        		
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
	    	GlobalData.logE("EventPreferencesTime.setAlarm","startTime="+result);
	    else
	    	GlobalData.logE("EventPreferencesTime.setAlarm","endTime="+result);
	    
	    Intent intent = new Intent(context, EventsTimeBroadcastReceiver.class);
	    intent.putExtra(GlobalData.EXTRA_EVENT_ID, _event._id);
	    intent.putExtra(GlobalData.EXTRA_START_SYSTEM_EVENT, startEvent);
	    
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
        
	}
	
}
