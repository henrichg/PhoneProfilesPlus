package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class EventPreferencesBattery extends EventPreferences {

	public int _levelLow;
	public int _levelHight;
	public boolean _charging;
	
	static final String PREF_EVENT_BATTERY_ENABLED = "eventBatteryEnabled";
	static final String PREF_EVENT_BATTERY_LEVEL_LOW = "eventBatteryLevelLow";
	static final String PREF_EVENT_BATTERY_LEVEL_HIGHT = "eventBatteryLevelHight";
	static final String PREF_EVENT_BATTERY_CHARGING = "eventBatteryCharging";
	
	public EventPreferencesBattery(Event event, 
									boolean enabled,
									int levelLow,
									int levelHight,
									boolean charging)
	{
		super(event, enabled);
		
		this._levelLow = levelLow;
		this._levelHight = levelHight;
		this._charging = charging;
	}
	
	@Override
	public void copyPreferences(Event fromEvent)
	{
		this._enabled = ((EventPreferencesBattery)fromEvent._eventPreferencesBattery)._enabled;
		this._levelLow = ((EventPreferencesBattery)fromEvent._eventPreferencesBattery)._levelLow;
		this._levelHight = ((EventPreferencesBattery)fromEvent._eventPreferencesBattery)._levelHight;
		this._charging = ((EventPreferencesBattery)fromEvent._eventPreferencesBattery)._charging;
	}
	
	@Override
	public void loadSharedPrefereces(SharedPreferences preferences)
	{
		Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_BATTERY_ENABLED, _enabled);
		editor.putString(PREF_EVENT_BATTERY_LEVEL_LOW, String.valueOf(this._levelLow));
		editor.putString(PREF_EVENT_BATTERY_LEVEL_HIGHT, String.valueOf(this._levelHight));
		editor.putBoolean(PREF_EVENT_BATTERY_CHARGING, this._charging);
		editor.commit();
	}
	
	@Override
	public void saveSharedPrefereces(SharedPreferences preferences)
	{
		this._enabled = preferences.getBoolean(PREF_EVENT_BATTERY_ENABLED, false);
		
		String sLevel;
		int iLevel;
		
		sLevel = preferences.getString(PREF_EVENT_BATTERY_LEVEL_LOW, "0");
		if (sLevel.isEmpty()) sLevel = "0";
		iLevel = Integer.parseInt(sLevel);
		if ((iLevel < 0) || (iLevel > 100)) iLevel = 0;
		this._levelLow= iLevel;

		sLevel = preferences.getString(PREF_EVENT_BATTERY_LEVEL_HIGHT, "100");
		if (sLevel.isEmpty()) sLevel = "100";
		iLevel = Integer.parseInt(sLevel);
		if ((iLevel < 0) || (iLevel > 100)) iLevel = 100;
		this._levelHight= iLevel;
		
		this._charging = preferences.getBoolean(PREF_EVENT_BATTERY_CHARGING, false);
	}
	
	@Override
	public String getPreferencesDescription(String description, Context context)
	{
		String descr = description;
		
		if (!this._enabled)
		{
			//descr = descr + context.getString(R.string.event_type_battery) + ": ";
			//descr = descr + context.getString(R.string.event_preferences_not_enabled);
		}
		else
		{
			descr = descr + context.getString(R.string.event_type_battery) + ": ";
			
			descr = descr + context.getString(R.string.pref_event_battery_level);
			descr = descr + ": " + this._levelLow + "% - " + this._levelHight + "%";
			if (this._charging)
				descr = descr + ", " + context.getString(R.string.pref_event_battery_charging);
		}
		
		return descr;
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
	{
		if (key.equals(PREF_EVENT_BATTERY_LEVEL_LOW) || key.equals(PREF_EVENT_BATTERY_LEVEL_HIGHT))
		{	
	        prefMng.findPreference(key).setSummary(value + "%");
		}
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
	{
		if (key.equals(PREF_EVENT_BATTERY_LEVEL_LOW) || key.equals(PREF_EVENT_BATTERY_LEVEL_HIGHT))
		{
			setSummary(prefMng, key, preferences.getString(key, ""), context);
		}
	}
	
	@Override
	public void setAllSummary(PreferenceManager prefMng, Context context)
	{
		setSummary(prefMng, PREF_EVENT_BATTERY_LEVEL_LOW, Integer.toString(_levelLow), context);
		setSummary(prefMng, PREF_EVENT_BATTERY_LEVEL_HIGHT, Integer.toString(_levelHight), context);
	}
	
	@Override
	public boolean activateReturnProfile()
	{
		return true;
	}

	@Override
	public void checkPreferences(PreferenceManager prefMng, Context context)
	{
		final Preference lowLevelPreference = prefMng.findPreference(PREF_EVENT_BATTERY_LEVEL_LOW);
		final Preference hightLevelPreference = prefMng.findPreference(PREF_EVENT_BATTERY_LEVEL_HIGHT);
		final PreferenceManager _prefMng = prefMng;
		final Context _context = context;
		
		lowLevelPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String sNewValue = (String)newValue;
                int iNewValue;
                if (sNewValue.isEmpty())
                	iNewValue = 0;
                else
                	iNewValue = Integer.parseInt(sNewValue);
                
                //Log.e("EventPreferencesBattery.checkPreferences.lowLevelPreference","iNewValue="+iNewValue);
                
                String sHightLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_BATTERY_LEVEL_HIGHT, "100");
                int iHightLevelValue;
                if (sHightLevelValue.isEmpty())
                	iHightLevelValue = 100;
                else
                	iHightLevelValue = Integer.parseInt(sHightLevelValue);

                //Log.e("EventPreferencesBattery.checkPreferences.lowLevelPreference","iHightLevelValue="+iHightLevelValue);
                
                boolean OK = ((iNewValue >= 0) && (iNewValue <= iHightLevelValue));
                
                if (!OK)
                {
            		Toast msg = Toast.makeText(_context, 
            				_context.getResources().getString(R.string.event_preferences_battery_level_low) + ": " +
            				_context.getResources().getString(R.string.event_preferences_battery_level_bad_value), 
            				Toast.LENGTH_SHORT);
            		msg.show();
                }
                
                return OK;
            }
        });

		hightLevelPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String sNewValue = (String)newValue;
                int iNewValue;
                if (sNewValue.isEmpty())
                	iNewValue = 100;
                else
                	iNewValue = Integer.parseInt(sNewValue);
                
                //Log.e("EventPreferencesBattery.checkPreferences.hightLevelPreference","iNewValue="+iNewValue);

                String sLowLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_BATTERY_LEVEL_LOW, "0");
                int iLowLevelValue;
                if (sLowLevelValue.isEmpty())
                	iLowLevelValue = 0;
                else
                	iLowLevelValue = Integer.parseInt(sLowLevelValue);

                //Log.e("EventPreferencesBattery.checkPreferences.hightLevelPreference","iLowLevelValue="+iLowLevelValue);
                
                boolean OK = ((iNewValue >= iLowLevelValue) && (iNewValue <= 100));
                
                if (!OK)
                {
            		Toast msg = Toast.makeText(_context, 
            				_context.getResources().getString(R.string.event_preferences_battery_level_hight) + ": " +
            				_context.getResources().getString(R.string.event_preferences_battery_level_bad_value), 
            				Toast.LENGTH_SHORT);
            		msg.show();
                }
                
                return OK;
            }
        });
		
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
