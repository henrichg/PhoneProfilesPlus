package sk.henrichg.phoneprofilesplus;

import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class EventPreferencesScreen extends EventPreferences {

	public int _eventType;
	public boolean _whenUnlocked;
	
	public static final int ETYPE_SCREENON = 0;
	public static final int ETYPE_SCREENOFF = 1;
	
	static final String PREF_EVENT_SCREEN_ENABLED = "eventScreenEnabled";
	static final String PREF_EVENT_SCREEN_EVENT_TYPE = "eventScreenEventType";
	static final String PREF_EVENT_SCREEN_WHEN_UNLOCKED = "eventScreenWhenUnlocked";
	
	public EventPreferencesScreen(Event event, 
									boolean enabled,
									int eventType,
									boolean whenUnlocked)
	{
		super(event, enabled);
	
		this._eventType = eventType;
		this._whenUnlocked = whenUnlocked;
	}
	
	@Override
	public void copyPreferences(Event fromEvent)
	{
		this._enabled = ((EventPreferencesScreen)fromEvent._eventPreferencesScreen)._enabled;
		this._eventType = ((EventPreferencesScreen)fromEvent._eventPreferencesScreen)._eventType;
		this._whenUnlocked = ((EventPreferencesScreen)fromEvent._eventPreferencesScreen)._whenUnlocked;
	}
	
	@Override
	public void loadSharedPrefereces(SharedPreferences preferences)
	{
		Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SCREEN_ENABLED, _enabled);
		editor.putString(PREF_EVENT_SCREEN_EVENT_TYPE, String.valueOf(this._eventType));
        editor.putBoolean(PREF_EVENT_SCREEN_WHEN_UNLOCKED, _whenUnlocked);
		editor.commit();
	}
	
	@Override
	public void saveSharedPrefereces(SharedPreferences preferences)
	{
		this._enabled = preferences.getBoolean(PREF_EVENT_SCREEN_ENABLED, false);
		this._eventType = Integer.parseInt(preferences.getString(PREF_EVENT_SCREEN_EVENT_TYPE, "1"));
		this._whenUnlocked = preferences.getBoolean(PREF_EVENT_SCREEN_WHEN_UNLOCKED, false);
	}
	
	@Override
	public String getPreferencesDescription(String description, Context context)
	{
		String descr = description;
		
		if (!this._enabled)
		{
			//descr = descr + context.getString(R.string.event_type_screen) + ": ";
			//descr = descr + context.getString(R.string.event_preferences_not_enabled);
		}
		else
		{
			descr = descr + context.getString(R.string.event_type_screen) + ": ";
			
			String[] eventListTypeNames = context.getResources().getStringArray(R.array.eventScreenEventTypeArray);
			String[] eventListTypes = context.getResources().getStringArray(R.array.eventScreenEventTypeValues);
			int index = Arrays.asList(eventListTypes).indexOf(Integer.toString(this._eventType));
			descr = descr + eventListTypeNames[index];
			if (this._whenUnlocked)
			{
				if (this._eventType == 0)
					descr = descr + "; " + context.getString(R.string.pref_event_screen_startWhenUnlocked);
				else
					descr = descr + "; " + context.getString(R.string.pref_event_screen_endWhenUnlocked);
			}
		}
		
		return descr;
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
	{
		if (key.equals(PREF_EVENT_SCREEN_EVENT_TYPE))
		{	
			ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
			int index = listPreference.findIndexOfValue(value);
			CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
			listPreference.setSummary(summary);
		}
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
	{
		if (key.equals(PREF_EVENT_SCREEN_EVENT_TYPE))
		{
			setSummary(prefMng, key, preferences.getString(key, ""), context);
		}
	}
	
	@Override
	public void setAllSummary(PreferenceManager prefMng, Context context)
	{
		setSummary(prefMng, PREF_EVENT_SCREEN_EVENT_TYPE, Integer.toString(_eventType), context);
		
		setWhenUnlockedTitle(prefMng, _eventType);
	}
	
	@Override
	public void checkPreferences(PreferenceManager prefMng, Context context)
	{
		final Preference eventTypePreference = prefMng.findPreference(PREF_EVENT_SCREEN_EVENT_TYPE);
		final PreferenceManager _prefMng = prefMng;
		
		eventTypePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String sNewValue = (String)newValue;
                int iNewValue;
                if (sNewValue.isEmpty())
                	iNewValue = 100;
                else
                	iNewValue = Integer.parseInt(sNewValue);
                
                //Log.e("EventPreferencesScreen.checkPreferences.whenUnlockedPreference","iNewValue="+iNewValue);
                
                setWhenUnlockedTitle(_prefMng, iNewValue);
                
                return true;
            }
        });
	}
	
	private void setWhenUnlockedTitle(PreferenceManager prefMng, int value)
	{
		final CheckBoxPreference whenUnlockedPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_SCREEN_WHEN_UNLOCKED);

		if (value == 0)
			whenUnlockedPreference.setTitle(R.string.event_preferences_screen_start_when_unlocked);
		else
			whenUnlockedPreference.setTitle(R.string.event_preferences_screen_end_when_unlocked);
	}
	
	@Override
	public boolean activateReturnProfile()
	{
		return true;
	}
	
	@Override
	public void setSystemRunningEvent(Context context)
	{
	}

	@Override
	public void setSystemPauseEvent(Context context)
	{
	}
	
	@Override
	public void removeSystemEvent(Context context)
	{
	}

}
