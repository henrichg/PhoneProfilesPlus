package sk.henrichg.phoneprofilesplus;

import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class EventPreferencesBluetooth extends EventPreferences {

	public String _adapterName;
	public int _connectionType;

	static final int CTYPE_CONNECTED = 0;
	static final int CTYPE_INFRONT = 1;
	
	static final String PREF_EVENT_BLUETOOTH_ENABLED = "eventBluetoothEnabled";
	static final String PREF_EVENT_BLUETOOTH_ADAPTER_NAME = "eventBluetoothAdapterNAME";
	static final String PREF_EVENT_BLUETOOTH_CONNECTION_TYPE = "eventBluetoothConnectionType";
	
	public EventPreferencesBluetooth(Event event, 
									boolean enabled,
									String adapterName,
									int connectionType)
	{
		super(event, enabled);
	
		this._adapterName = adapterName;
		this._connectionType = connectionType;
	}
	
	@Override
	public void copyPreferences(Event fromEvent)
	{
		this._enabled = ((EventPreferencesBluetooth)fromEvent._eventPreferencesBluetooth)._enabled;
		this._adapterName = ((EventPreferencesBluetooth)fromEvent._eventPreferencesBluetooth)._adapterName;
		this._connectionType = ((EventPreferencesBluetooth)fromEvent._eventPreferencesBluetooth)._connectionType;
	}
	
	@Override
	public void loadSharedPrefereces(SharedPreferences preferences)
	{
		Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_BLUETOOTH_ENABLED, _enabled);
		editor.putString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, this._adapterName);
		editor.putString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, String.valueOf(this._connectionType));
		editor.commit();
	}
	
	@Override
	public void saveSharedPrefereces(SharedPreferences preferences)
	{
		this._enabled = preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
		this._adapterName = preferences.getString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, "");
		this._connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, "1"));
	}
	
	@Override
	public String getPreferencesDescription(String description, Context context)
	{
		String descr = description;
		
		if (!this._enabled)
		{
			//descr = descr + context.getString(R.string.event_type_bluetooth) + ": ";
			//descr = descr + context.getString(R.string.event_preferences_not_enabled);
		}
		else
		{
			descr = descr + context.getString(R.string.event_type_bluetooth) + ": ";
			
			descr = descr + context.getString(R.string.pref_event_bluetooth_connectionType);
			String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeArray);
			String[] connectionListTypes = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeValues);
			int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
			descr = descr + ": " + connectionListTypeNames[index] + "; ";
			descr = descr + context.getString(R.string.pref_event_bluetooth_adapterName);
			descr = descr + ": " + this._adapterName;
		}
		
		return descr;
	}
	
	@Override
	public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
	{
		if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME))
		{	
	        Preference preference = prefMng.findPreference(key);
	        preference.setSummary(value);
	    	GUIData.setPreferenceTitleStyle(preference, false, true);
		}
		if (key.equals(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE))
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
		if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME) || 
			key.equals(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE))
		{
			setSummary(prefMng, key, preferences.getString(key, ""), context);
		}
	}
	
	@Override
	public void setAllSummary(PreferenceManager prefMng, Context context)
	{
		setSummary(prefMng, PREF_EVENT_BLUETOOTH_ADAPTER_NAME, _adapterName, context);
		setSummary(prefMng, PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, Integer.toString(_connectionType), context);

		if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) 
				!= GlobalData.HARDWARE_CHECK_ALLOWED)
		{
			prefMng.findPreference(PREF_EVENT_BLUETOOTH_ENABLED).setEnabled(false);
			prefMng.findPreference(PREF_EVENT_BLUETOOTH_ADAPTER_NAME).setEnabled(false);
			prefMng.findPreference(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE).setEnabled(false);
		}
		
	}
	
	@Override
	public boolean isRunable()
	{
		return super.isRunable() && (!this._adapterName.isEmpty());
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
			(!BluetoothScanAlarmBroadcastReceiver.isAlarmSet(context, false)))
			BluetoothScanAlarmBroadcastReceiver.setAlarm(context, false);
	}

	@Override
	public void setSystemPauseEvent(Context context)
	{
		if ((_connectionType == CTYPE_INFRONT) && 
			(!BluetoothScanAlarmBroadcastReceiver.isAlarmSet(context, false)))
			BluetoothScanAlarmBroadcastReceiver.setAlarm(context, false);
	}
	
	@Override
	public void removeSystemEvent(Context context)
	{
	}

}
