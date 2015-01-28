package sk.henrichg.phoneprofilesplus;

import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class EventPreferencesWifi extends EventPreferences {

	public String _SSID;
	public int _connectionType;

	static final int CTYPE_CONNECTED = 0;
	static final int CTYPE_INFRONT = 1;
	
	static final String PREF_EVENT_WIFI_ENABLED = "eventWiFiEnabled";
	static final String PREF_EVENT_WIFI_SSID = "eventWiFiSSID";
	static final String PREF_EVENT_WIFI_CONNECTION_TYPE = "eventWiFiConnectionType";
	
	public EventPreferencesWifi(Event event, 
									boolean enabled,
									String SSID,
									int connectionType)
	{
		super(event, enabled);
	
		this._SSID = SSID;
		this._connectionType = connectionType;
	}
	
	@Override
	public void copyPreferences(Event fromEvent)
	{
		this._enabled = ((EventPreferencesWifi)fromEvent._eventPreferencesWifi)._enabled;
		this._SSID = ((EventPreferencesWifi)fromEvent._eventPreferencesWifi)._SSID;
		this._connectionType = ((EventPreferencesWifi)fromEvent._eventPreferencesWifi)._connectionType;
	}
	
	@Override
	public void loadSharedPrefereces(SharedPreferences preferences)
	{
		Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_ENABLED, _enabled);
		editor.putString(PREF_EVENT_WIFI_SSID, this._SSID);
		editor.putString(PREF_EVENT_WIFI_CONNECTION_TYPE, String.valueOf(this._connectionType));
		editor.commit();
	}
	
	@Override
	public void saveSharedPrefereces(SharedPreferences preferences)
	{
		this._enabled = preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false);
		this._SSID = preferences.getString(PREF_EVENT_WIFI_SSID, "");
		this._connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_WIFI_CONNECTION_TYPE, "1"));
	}
	
	@Override
	public String getPreferencesDescription(String description, Context context)
	{
		String descr = description;
		
		if (!this._enabled)
		{
			//descr = descr + context.getString(R.string.event_type_wifi) + ": ";
			//descr = descr + context.getString(R.string.event_preferences_not_enabled);
		}
		else
		{
			descr = descr + context.getString(R.string.event_type_wifi) + ": ";
			
			descr = descr + context.getString(R.string.pref_event_wifi_connectionType);
			String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventWifiConnectionTypeArray);
			String[] connectionListTypes = context.getResources().getStringArray(R.array.eventWifiConnectionTypeValues);
			int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
			descr = descr + ": " + connectionListTypeNames[index] + "; ";
			descr = descr + context.getString(R.string.pref_event_wifi_ssid);
			descr = descr + ": " + this._SSID;
		}
		
		return descr;
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
	{
		if (key.equals(PREF_EVENT_WIFI_SSID))
		{	
	        Preference preference = prefMng.findPreference(key);
	        preference.setSummary(value);
	    	GUIData.setPreferenceTitleStyle(preference, false, true);
		}
		if (key.equals(PREF_EVENT_WIFI_CONNECTION_TYPE))
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
		if (key.equals(PREF_EVENT_WIFI_SSID) || 
			key.equals(PREF_EVENT_WIFI_CONNECTION_TYPE))
		{
			setSummary(prefMng, key, preferences.getString(key, ""), context);
		}
	}
	
	@Override
	public void setAllSummary(PreferenceManager prefMng, Context context)
	{
		setSummary(prefMng, PREF_EVENT_WIFI_SSID, _SSID, context);
		setSummary(prefMng, PREF_EVENT_WIFI_CONNECTION_TYPE, Integer.toString(_connectionType), context);

		if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) 
				!= GlobalData.HARDWARE_CHECK_ALLOWED)
		{
			prefMng.findPreference(PREF_EVENT_WIFI_ENABLED).setEnabled(false);
			prefMng.findPreference(PREF_EVENT_WIFI_SSID).setEnabled(false);
			prefMng.findPreference(PREF_EVENT_WIFI_CONNECTION_TYPE).setEnabled(false);
		}
		
	}
	
	@Override
	public boolean isRunable()
	{
		return super.isRunable() && (!this._SSID.isEmpty());
	}
	
	@Override
	public boolean activateReturnProfile()
	{
		return true;
	}
	
	@Override
	public void setSystemRunningEvent(Context context)
	{
		if ((_connectionType == CTYPE_INFRONT) && 
			(!WifiScanAlarmBroadcastReceiver.isAlarmSet(context, false)))
			WifiScanAlarmBroadcastReceiver.setAlarm(context, false);
	}

	@Override
	public void setSystemPauseEvent(Context context)
	{
		if ((_connectionType == CTYPE_INFRONT) && 
			(!WifiScanAlarmBroadcastReceiver.isAlarmSet(context, false)))
			WifiScanAlarmBroadcastReceiver.setAlarm(context, false);
	}
	
	@Override
	public void removeSystemEvent(Context context)
	{
	}

}
