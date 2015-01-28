package sk.henrichg.phoneprofilesplus;
 
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
 
public class ProfilePreferencesFragment extends PreferenceFragment 
										implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private DataWrapper dataWrapper;
	private Profile profile;
	public long profile_id;
	//private boolean first_start_activity;
	private int new_profile_mode;
	private int startupSource;
	public boolean profileNonEdited = true;
	private PreferenceManager prefMng;
	private SharedPreferences preferences;
	private Context context;
	private ActionMode actionMode;
	private Callback actionModeCallback;
	
	private int actionModeButtonClicked = BUTTON_UNDEFINED;
	
	private static ImageViewPreference changedImageViewPreference;
	private static Activity preferencesActivity = null;
		
	static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
	static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
	static final String PREFS_NAME_DEFAULT_PROFILE = GlobalData.DEFAULT_PROFILE_PREFS_NAME;
	private String PREFS_NAME;

	static final String SP_ACTION_MODE_SHOWED = "action_mode_showed";
	
	static final int BUTTON_UNDEFINED = 0;
	static final int BUTTON_CANCEL = 1;
	static final int BUTTON_SAVE = 2;
	
	private OnShowActionModeInProfilePreferences onShowActionModeInProfilePreferencesCallback = sDummyOnShowActionModeInProfilePreferencesCallback;
	private OnHideActionModeInProfilePreferences onHideActionModeInProfilePreferencesCallback = sDummyOnHideActionModeInProfilePreferencesCallback;
	private OnRestartProfilePreferences onRestartProfilePreferencesCallback = sDummyOnRestartProfilePreferencesCallback;
	private OnRedrawProfileListFragment onRedrawProfileListFragmentCallback = sDummyOnRedrawProfileListFragmentCallback;

	// invokes when action mode shows
	public interface OnShowActionModeInProfilePreferences {
		public void onShowActionModeInProfilePreferences();
	}

	private static OnShowActionModeInProfilePreferences sDummyOnShowActionModeInProfilePreferencesCallback = new OnShowActionModeInProfilePreferences() {
		public void onShowActionModeInProfilePreferences() {
		}
	};

	// invokes when action mode hides
	public interface OnHideActionModeInProfilePreferences {
		public void onHideActionModeInProfilePreferences();
	}

	private static OnHideActionModeInProfilePreferences sDummyOnHideActionModeInProfilePreferencesCallback = new OnHideActionModeInProfilePreferences() {
		public void onHideActionModeInProfilePreferences() {
		}
	};
	
	// invokes when restart of profile preferences fragment needed (undo preference changes)
	public interface OnRestartProfilePreferences {
		/**
		 * Callback for restart fragment.
		 */
		public void onRestartProfilePreferences(Profile profile, int newProfileMode);
	}

	private static OnRestartProfilePreferences sDummyOnRestartProfilePreferencesCallback = new OnRestartProfilePreferences() {
		public void onRestartProfilePreferences(Profile profile, int newProfileMode) {
		}
	};
	
	// invokes when profile list fragment redraw needed (preference changes accepted)
	public interface OnRedrawProfileListFragment {
		/**
		 * Callback for redraw profile list fragment.
		 */
		public void onRedrawProfileListFragment(Profile profile, int newProfileMode);
	}

	private static OnRedrawProfileListFragment sDummyOnRedrawProfileListFragmentCallback = new OnRedrawProfileListFragment() {
		public void onRedrawProfileListFragment(Profile profile, int newProfileMode) {
		}
	};
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof OnShowActionModeInProfilePreferences)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onShowActionModeInProfilePreferencesCallback = (OnShowActionModeInProfilePreferences) activity;
		
		if (!(activity instanceof OnHideActionModeInProfilePreferences)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onHideActionModeInProfilePreferencesCallback = (OnHideActionModeInProfilePreferences) activity;
		
		if (!(activity instanceof OnRestartProfilePreferences)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onRestartProfilePreferencesCallback = (OnRestartProfilePreferences) activity;
		
		if (!(activity instanceof OnRedrawProfileListFragment)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onRedrawProfileListFragmentCallback = (OnRedrawProfileListFragment) activity;
		
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		onShowActionModeInProfilePreferencesCallback = sDummyOnShowActionModeInProfilePreferencesCallback;
		onHideActionModeInProfilePreferencesCallback = sDummyOnHideActionModeInProfilePreferencesCallback;
		onRestartProfilePreferencesCallback = sDummyOnRestartProfilePreferencesCallback;
		onRedrawProfileListFragmentCallback = sDummyOnRedrawProfileListFragmentCallback;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// must by false to avoid FC when rotation changes and preference dialogs are shown
		setRetainInstance(false);
		
		preferencesActivity = getActivity();
        context = getActivity().getBaseContext();
		
        dataWrapper = new DataWrapper(context, true, false, 0);
        
	    startupSource = getArguments().getInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
	    if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
	    	PREFS_NAME = PREFS_NAME_ACTIVITY;
	    else
	    if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
	    	PREFS_NAME = PREFS_NAME_FRAGMENT;
	    else
	    if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
	    	PREFS_NAME = PREFS_NAME_DEFAULT_PROFILE;
	    else
			PREFS_NAME = PREFS_NAME_FRAGMENT;
		
		prefMng = getPreferenceManager();
		prefMng.setSharedPreferencesName(PREFS_NAME);
		prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);
		
        // getting attached fragment data
		if (getArguments().containsKey(GlobalData.EXTRA_NEW_PROFILE_MODE))
			new_profile_mode = getArguments().getInt(GlobalData.EXTRA_NEW_PROFILE_MODE);
		if (getArguments().containsKey(GlobalData.EXTRA_PROFILE_ID))
			profile_id = getArguments().getLong(GlobalData.EXTRA_PROFILE_ID);
    	//Log.e("ProfilePreferencesFragment.onCreate", "profile_id=" + profile_id);
		
		if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
		{
			profile = GlobalData.getDefaultProfile(context);
			profile_id = profile._id;
		}
		else
		if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT)
		{
			// create new profile
			profile = dataWrapper.getNoinitializedProfile(
						getResources().getString(R.string.profile_name_default), 
						GlobalData.PROFILE_ICON_DEFAULT, 0); 
			profile._showInActivator = true;
			profile_id = 0;
		}
		else
		if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)
		{
			// duplicate profile
			Profile origProfile = dataWrapper.getProfileById(profile_id);
			profile = new Profile(
						   origProfile._name+"_d", 
						   origProfile._icon, 
						   false, 
						   origProfile._porder,
						   origProfile._volumeRingerMode,
						   origProfile._volumeRingtone,
						   origProfile._volumeNotification,
						   origProfile._volumeMedia,
						   origProfile._volumeAlarm,
						   origProfile._volumeSystem,
						   origProfile._volumeVoice,
						   origProfile._soundRingtoneChange,
						   origProfile._soundRingtone,
						   origProfile._soundNotificationChange,
						   origProfile._soundNotification,
						   origProfile._soundAlarmChange,
						   origProfile._soundAlarm,
						   origProfile._deviceAirplaneMode,
						   origProfile._deviceWiFi,
						   origProfile._deviceBluetooth,
						   origProfile._deviceScreenTimeout,
						   origProfile._deviceBrightness,
						   origProfile._deviceWallpaperChange,
						   origProfile._deviceWallpaper,
						   origProfile._deviceMobileData,
						   origProfile._deviceMobileDataPrefs,
						   origProfile._deviceGPS,
						   origProfile._deviceRunApplicationChange,
						   origProfile._deviceRunApplicationPackageName,
						   origProfile._deviceAutosync,
						   origProfile._showInActivator,
						   origProfile._deviceAutoRotate,
						   origProfile._deviceLocationServicePrefs,
						   origProfile._volumeSpeakerPhone,
						   origProfile._deviceNFC,
						   origProfile._duration,
						   origProfile._afterDurationDo,
						   origProfile._volumeZenMode,
						   origProfile._deviceKeyguard);
			profile_id = 0;
		}
		else
			profile = dataWrapper.getProfileById(profile_id);

		preferences = prefMng.getSharedPreferences();

        /*if (first_start_activity)*/
		if (savedInstanceState == null)
        	loadPreferences();
    	
		if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
			addPreferencesFromResource(R.xml.default_profile_preferences);
		else
			addPreferencesFromResource(R.xml.profile_preferences);
		
    	if (android.os.Build.VERSION.SDK_INT >= 21)
    	{
    		// add zen mode option to preference Ringer mode
    		ListPreference ringerModePreference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);
    		CharSequence[] entries = ringerModePreference.getEntries();
    		CharSequence[] entryValues = ringerModePreference.getEntryValues();
    		
        	CharSequence[] newEntries = new CharSequence[entries.length+1];
        	CharSequence[] newEntryValues = new CharSequence[entries.length+1];
    		
    		for (int i = 0; i < entries.length; i++)
    		{
    			newEntries[i] = entries[i];
    			newEntryValues[i] = entryValues[i];
    		}
    		
    		newEntries[entries.length] = context.getString(R.string.array_pref_ringerModeArray_ZenMode);
    		newEntryValues[entries.length] = "5";
    		
    		ringerModePreference.setEntries(newEntries);
    		ringerModePreference.setEntryValues(newEntryValues);
    		ringerModePreference.setValue(Integer.toString(profile._volumeRingerMode));

           	final boolean canEnableZenMode = (GlobalData.isRooted(false)) && (GlobalData.settingsBinaryExists());
    		
           	Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
           	zenModePreference.setEnabled((profile._volumeRingerMode == 5) && canEnableZenMode);
    		
    		ringerModePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String)newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                    	iNewValue = 0;
                    else
                    	iNewValue = Integer.parseInt(sNewValue);

                   	Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);

                   	zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode);
                   	GUIData.setPreferenceTitleStyle(zenModePreference, false, false);
                   	
                   	return true;
                }
            });
    		
    		// set mobile data preference title
           	Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
           	mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData_21);
    		
    	}
    	else
    	{
    		// remove zen mode types from preferences screen
    		// for Android version < 5.0 this is not supported
    		Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
    		if (preference != null)
    		{
	    		PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("prf_pref_volumeCategory");
	    		preferenceCategory.removePreference(preference);
    		}
    		
    		// set mobile data preference title
           	Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
           	mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
    	}

    	preferences.registerOnSharedPreferenceChangeListener(this);
        createActionModeCallback();
	        
		if (savedInstanceState == null)
		{
	    	SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
	    	Editor editor = preferences.edit();
	    	editor.remove(SP_ACTION_MODE_SHOWED);
			editor.commit();
		}

		updateSharedPreference();
		
    	//Log.d("ProfilePreferencesFragment.onCreate", "xxxx");
    }
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		// must by in onStart(), in ocCreate() crashed
    	SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
    	int actionModeShowed = preferences.getInt(SP_ACTION_MODE_SHOWED, 0);
        if (actionModeShowed == 2)
        	showActionMode();
        else
        if (((new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
            (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
        	&& (actionModeShowed == 0))
        	showActionMode();

    	//Log.d("ProfilePreferencesFragment.onStart", preferences.getString(PREF_PROFILE_NAME, ""));

    	//Log.d("ProfilePreferencesFragment.onStart", "profile activated="+profile.getChecked());

	}
	
	@Override
	public void onPause()
	{
		super.onPause();

	/*	if (actionMode != null)
		{
			restart = false; // nerestartovat fragment
			actionMode.finish();
		} */
		
    	//Log.d("ProfilePreferencesFragment.onPause", "xxxx");
		
	}

	@Override
	public void onDestroy()
	{
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        profile = null;
        
		if (dataWrapper != null)
			dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
        
		super.onDestroy();
	}

	public void doOnActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == ImageViewPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
		{
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			
			Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();
			
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			
			cursor.close();
			
			//Log.d("ProfilePreferencesFragment.onActivityResult", picturePath);
			
			if (changedImageViewPreference != null)
				// nastavime image identifikatoru na ziskanu cestu ku obrazku
				changedImageViewPreference.setImageIdentifierAndType(picturePath, false);
			
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		doOnActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
    	Editor editor = preferences.edit();
    	if (actionMode != null) 
    		editor.putInt(SP_ACTION_MODE_SHOWED, 2);
    	else
    		editor.putInt(SP_ACTION_MODE_SHOWED, 1);
		editor.commit();
	}	
	
	private void loadPreferences()
	{
    	if (profile != null)
    	{
	    	//SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
	
			//Log.e("ProfilePreferencesFragment.loadPreferences","profile="+profile);
	    	
	    	Editor editor = preferences.edit();
			if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
			{
				/*
				editor.remove(GlobalData.PREF_PROFILE_NAME).putString(GlobalData.PREF_PROFILE_NAME, profile._name);
				editor.remove(GlobalData.PREF_PROFILE_ICON).putString(GlobalData.PREF_PROFILE_ICON, profile._icon);
		        editor.remove(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR).putBoolean(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR, profile._showInActivator);
	        	editor.remove(GlobalData.PREF_PROFILE_DURATION).editor.putString(GlobalData.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
	        	editor.remove(GlobalData.PREF_PROFILE_AFTER_DURATION_DO).editor.putString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
		        */
				editor.putString(GlobalData.PREF_PROFILE_NAME, profile._name);
				editor.putString(GlobalData.PREF_PROFILE_ICON, profile._icon);
		        editor.putBoolean(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR, profile._showInActivator);
		        editor.putString(GlobalData.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
		        editor.putString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
			}
			/*
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE).putString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, Integer.toString(profile._volumeRingerMode));
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE).putString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, Integer.toString(profile._volumeZenMode));
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_RINGTONE).putString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION).putString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_MEDIA).putString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_ALARM).putString(GlobalData.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_SYSTEM).putString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_VOICE).putString(GlobalData.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
	        editor.remove(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE).putString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, Integer.toString(profile._soundRingtoneChange));
	        editor.remove(GlobalData.PREF_PROFILE_SOUND_RINGTONE).putString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
	        editor.remove(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE).putString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, Integer.toString(profile._soundNotificationChange));
	        editor.remove(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION).putString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
	        editor.remove(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE).putString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, Integer.toString(profile._soundAlarmChange));
	        editor.remove(GlobalData.PREF_PROFILE_SOUND_ALARM).putString(GlobalData.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE).putString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, Integer.toString(profile._deviceAirplaneMode));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_WIFI).putString(GlobalData.PREF_PROFILE_DEVICE_WIFI, Integer.toString(profile._deviceWiFi));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH).putString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, Integer.toString(profile._deviceBluetooth));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT).putString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, Integer.toString(profile._deviceScreenTimeout));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS).putString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE).putString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, Integer.toString(profile._deviceWallpaperChange));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER).putString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, profile._deviceWallpaper);
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA).putString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, Integer.toString(profile._deviceMobileData));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS).putString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, Integer.toString(profile._deviceMobileDataPrefs));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_GPS).putString(GlobalData.PREF_PROFILE_DEVICE_GPS, Integer.toString(profile._deviceGPS));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE).putString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, Integer.toString(profile._deviceRunApplicationChange));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME).putString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC).putString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, Integer.toString(profile._deviceAutosync));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE).putString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, Integer.toString(profile._deviceAutoRotate));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS).editor.putString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, Integer.toString(profile._deviceLocationServicePrefs));
	        editor.remove(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE).editor.putString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, Integer.toString(profile._speakerPhone));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_NFC).editor.putString(GlobalData.PREF_PROFILE_DEVICE_NFC, Integer.toString(profile._deviceNFC));
	        editor.remove(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD).editor.putString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, Integer.toString(profile._deviceKeyguard));
	        */
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, Integer.toString(profile._volumeRingerMode));
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, Integer.toString(profile._volumeZenMode));
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
	        editor.putString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, Integer.toString(profile._soundRingtoneChange));
	        editor.putString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
	        editor.putString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, Integer.toString(profile._soundNotificationChange));
	        editor.putString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
	        editor.putString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, Integer.toString(profile._soundAlarmChange));
	        editor.putString(GlobalData.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, Integer.toString(profile._deviceAirplaneMode));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_WIFI, Integer.toString(profile._deviceWiFi));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, Integer.toString(profile._deviceBluetooth));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, Integer.toString(profile._deviceScreenTimeout));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, Integer.toString(profile._deviceWallpaperChange));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, profile._deviceWallpaper);
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, Integer.toString(profile._deviceMobileData));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, Integer.toString(profile._deviceMobileDataPrefs));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_GPS, Integer.toString(profile._deviceGPS));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, Integer.toString(profile._deviceRunApplicationChange));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, Integer.toString(profile._deviceAutosync));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, Integer.toString(profile._deviceAutoRotate));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, Integer.toString(profile._deviceLocationServicePrefs));
	        editor.putString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, Integer.toString(profile._volumeSpeakerPhone));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_NFC, Integer.toString(profile._deviceNFC));
	        editor.putString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, Integer.toString(profile._deviceKeyguard));
			editor.commit();
    	}
		
	}
	
	private void savePreferences()
	{
		if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
		{
			profile._name = preferences.getString(GlobalData.PREF_PROFILE_NAME, "");
			profile._icon = preferences.getString(GlobalData.PREF_PROFILE_ICON, "");
	    	profile._showInActivator = preferences.getBoolean(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR, true);
	    	profile._duration = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DURATION, ""));
	    	profile._afterDurationDo = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, ""));
		}
    	profile._volumeRingerMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, ""));
    	profile._volumeZenMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, ""));
    	profile._volumeRingtone = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, "");
    	profile._volumeNotification = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, "");
    	profile._volumeMedia = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, "");
    	profile._volumeAlarm = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ALARM, "");
    	profile._volumeSystem = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, "");
    	profile._volumeVoice = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_VOICE, "");
    	profile._soundRingtoneChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, ""));
    	profile._soundRingtone = preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, "");
    	profile._soundNotificationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, ""));
    	profile._soundNotification = preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, "");
    	profile._soundAlarmChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, ""));
    	profile._soundAlarm = preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM, "");
    	profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, ""));
    	profile._deviceWiFi = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WIFI, ""));
    	profile._deviceBluetooth = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, ""));
    	profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, ""));
    	profile._deviceBrightness = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
    	profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, ""));
    	if (profile._deviceWallpaperChange == 1)
    		profile._deviceWallpaper = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, "");
    	else
    		profile._deviceWallpaper = "-|0";
    	profile._deviceMobileData = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, ""));
    	profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, ""));
    	profile._deviceGPS = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_GPS, ""));
    	profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, ""));
    	if (profile._deviceRunApplicationChange == 1)
    		profile._deviceRunApplicationPackageName = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
    	else
    		profile._deviceRunApplicationPackageName = "-";
    	profile._deviceAutosync = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, ""));
    	profile._deviceAutoRotate = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, ""));
    	profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, ""));
    	profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, ""));
    	profile._deviceNFC = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NFC, ""));
    	profile._deviceKeyguard = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, ""));

    	//Log.d("ProfilePreferencesFragment.onPause", "profile activated="+profile.getChecked());
    	
		if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
		{
	    	// update bitmaps
			profile.generateIconBitmap(context, false, 0);
			profile.generatePreferencesIndicator(context, false, 0);
	
	    	//Log.d("ProfilePreferencesFragment.onPause", "profile activated="+profile.getChecked());
			
			if ((new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
			    (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
			{
				// add profile into DB
				dataWrapper.getDatabaseHandler().addProfile(profile);
				profile_id = profile._id;
	
	        	//Log.d("ProfilePreferencesFragment.onPause", "addProfile");
				
			}
			else
	        if (profile_id > 0) 
	        {
				dataWrapper.getDatabaseHandler().updateProfile(profile);
	        	
	        	//Log.d("ProfilePreferencesFragment.onPause", "updateProfile");
	
	        }
		}

        onRedrawProfileListFragmentCallback.onRedrawProfileListFragment(profile, new_profile_mode);
	}
	
	private void setSummary(String key, Object value)
	{
		if (key.equals(GlobalData.PREF_PROFILE_NAME))
		{	
			Preference preference = prefMng.findPreference(key);
			preference.setSummary(value.toString());
			GUIData.setPreferenceTitleStyle(preference, false, true);
		}
		if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE))
		{
			String sValue = value.toString();
			//Log.e("ProfilePreferencesFragment.setSummary","key="+key);
			//Log.e("ProfilePreferencesFragment.setSummary","value="+sValue);
			ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
			int index = listPreference.findIndexOfValue(sValue);
			CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
			listPreference.setSummary(summary);
			GUIData.setPreferenceTitleStyle(listPreference, index > 0, false);
		}
		if (key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE))
		{
			if (android.os.Build.VERSION.SDK_INT >= 21)
			{
				if ((!GlobalData.isRooted(false)) ||
					(!GlobalData.settingsBinaryExists()))
				{
					ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
					listPreference.setEnabled(false);
					listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed));
					GUIData.setPreferenceTitleStyle(listPreference, false, false);
				}
				else
				{
					String sValue = value.toString();
					//Log.e("ProfilePreferencesFragment.setSummary","key="+key);
					//Log.e("ProfilePreferencesFragment.setSummary","value="+sValue);
					ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
					int index = listPreference.findIndexOfValue(sValue);
					CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
					listPreference.setSummary(summary);
					GUIData.setPreferenceTitleStyle(listPreference, index > 0, false);
				}
			}
		}
		if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
				key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
				key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE))
		{
			String sValue = value.toString();
			ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
			int index = listPreference.findIndexOfValue(sValue);
			CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
			listPreference.setSummary(summary);
			GUIData.setPreferenceTitleStyle(listPreference, index > 0, false);
		}
		if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE) ||
			key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION) ||
			key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM))
		{
			String ringtoneUri = value.toString();
			
			//Log.d("ProfilePreferencesFragment.setSummary", ringtoneUri);
			
			if (ringtoneUri.isEmpty())
		        prefMng.findPreference(key).setSummary(R.string.preferences_notificationSound_None);
			else
			{
				Uri uri = Uri.parse(ringtoneUri);
				Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
				String ringtoneName;
				if (ringtone == null)
					ringtoneName = "";
				else
					ringtoneName = ringtone.getTitle(context);
		        prefMng.findPreference(key).setSummary(ringtoneName);
			}
		}
		if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE) || 
			key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC) ||
			key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI) ||
			key.equals(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH) ||
			key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
			key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
			key.equals(GlobalData.PREF_PROFILE_DEVICE_GPS) ||
			key.equals(GlobalData.PREF_PROFILE_DEVICE_NFC))
		{
			int canChange = GlobalData.hardwareCheck(key, context);
			if (canChange != GlobalData.HARDWARE_CHECK_ALLOWED)
			{
				ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
				listPreference.setEnabled(false);
				if (canChange == GlobalData.HARDWARE_CHECK_NOT_ALLOWED)
					listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed));
				else
				if (canChange == GlobalData.HARDWARE_CHECK_INSTALL_PPHELPER)
					listPreference.setSummary(getResources().getString(R.string.profile_preferences_install_pphelper));
				else
				if (canChange == GlobalData.HARDWARE_CHECK_UPGRADE_PPHELPER)
					listPreference.setSummary(getResources().getString(R.string.profile_preferences_upgrade_pphelper));
				GUIData.setPreferenceTitleStyle(listPreference, false, false);
			}
			else
			{
				String sValue = value.toString();
				ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
				int index = listPreference.findIndexOfValue(sValue);
				CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
				listPreference.setSummary(summary);
				GUIData.setPreferenceTitleStyle(listPreference, index > 0, false);
			}
			
		}
		if (key.equals(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
			key.equals(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD))
		{
			String sValue = value.toString();
			ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
			int index = listPreference.findIndexOfValue(sValue);
			CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
			listPreference.setSummary(summary);
			GUIData.setPreferenceTitleStyle(listPreference, index > 0, false);
		}
		if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE))
		{
			String sValue = value.toString();
			ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
			int index = listPreference.findIndexOfValue(sValue);
			CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
			listPreference.setSummary(summary);
			GUIData.setPreferenceTitleStyle(listPreference, index > 0, false);
		}
    	if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
	    	key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) || 
	    	key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
	    	key.equals(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
	    	key.equals(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE))
    	{
			String sValue = value.toString();
			ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
			int index = listPreference.findIndexOfValue(sValue);
			CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
			listPreference.setSummary(summary);
			GUIData.setPreferenceTitleStyle(listPreference, index > 0, false);
    	}
		if (key.equals(GlobalData.PREF_PROFILE_DURATION))
		{	
			Preference preference = prefMng.findPreference(key);
			String sValue = value.toString();
			int iValue = 0;
			if (!sValue.isEmpty())
				iValue = Integer.valueOf(sValue);
	        preference.setSummary(sValue);
	        GUIData.setPreferenceTitleStyle(preference, iValue > 0, false);
		}
    	if (key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO))
       	{
   			String sValue = value.toString();
   			ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
   			int index = listPreference.findIndexOfValue(sValue);
   			CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
   			listPreference.setSummary(summary);
   			GUIData.setPreferenceTitleStyle(listPreference, index > 0, false);
       	}
    	if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGTONE) ||
    	    key.equals(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION) ||
	    	key.equals(GlobalData.PREF_PROFILE_VOLUME_MEDIA) ||
	    	key.equals(GlobalData.PREF_PROFILE_VOLUME_ALARM) ||
	    	key.equals(GlobalData.PREF_PROFILE_VOLUME_SYSTEM) ||
	    	key.equals(GlobalData.PREF_PROFILE_VOLUME_VOICE))
	    {
			Preference preference = prefMng.findPreference(key);
			String sValue = value.toString();
			String[] splits = sValue.split("\\|");
			int noChange;
			try {
				noChange = Integer.parseInt(splits[1]);
			} catch (Exception e) {
				noChange = 1;
			}
			GUIData.setPreferenceTitleStyle(preference, noChange != 1, false);	
	    }
    	if (key.equals(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS))
	    {
			Preference preference = prefMng.findPreference(key);
			String sValue = value.toString();
			String[] splits = sValue.split("\\|");
			int noChange;
			try {
				noChange = Integer.parseInt(splits[1]);
			} catch (Exception e) {
				noChange = 1;
			}
			GUIData.setPreferenceTitleStyle(preference, noChange != 1, false);	
	    }
    	
	}
	
	private void disableDependedPref(String key, Object value)
	{
		String sValue = value.toString();
		
		final String NO_CHANGE = "0";
		final String DEFAULT_PROFILE = "99";
		
		if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE))
		{
			boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
			prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_RINGTONE).setEnabled(enabled);
		}
		if (key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE))
		{
			boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
			prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION).setEnabled(enabled);
		}
		if (key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE))
		{
			boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
			prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_ALARM).setEnabled(enabled);
		}
		if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE))
		{
			boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
			prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER).setEnabled(enabled);
		}
		if (key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
		{
			boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
			prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME).setEnabled(enabled);
		}
		
	}
	
	private void updateSharedPreference()
	{
        if (profile != null) 
        {	

	    	// updating activity with selected profile preferences
	    	
        	//Log.d("PhonePreferencesActivity.updateSharedPreference", profile.getName());
        	
    		if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
    		{
    			setSummary(GlobalData.PREF_PROFILE_NAME, profile._name);
    	        setSummary(GlobalData.PREF_PROFILE_DURATION, profile._duration); 
    	        setSummary(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, profile._afterDurationDo); 
    		}
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, profile._volumeRingerMode);
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, profile._volumeZenMode);
	        setSummary(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
	        setSummary(GlobalData.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
	        setSummary(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
	        setSummary(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
	        setSummary(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
	        setSummary(GlobalData.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_WIFI, profile._deviceWiFi);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, profile._deviceBluetooth);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, profile._deviceMobileData);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_GPS, profile._deviceGPS);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, profile._deviceAutosync);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs); 
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange); 
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs); 
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone); 
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_NFC, profile._deviceNFC); 
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, profile._deviceKeyguard); 
			
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
	        setSummary(GlobalData.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
	        setSummary(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
	        
		    // disable depended preferences
		    disableDependedPref(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
		    disableDependedPref(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
		    disableDependedPref(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
		    disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
		    disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
	        
        }
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
    	if (!key.equals(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR))
	    		setSummary(key, sharedPreferences.getString(key, ""));
    	
	    // disable depended preferences
    	if (!key.equals(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR))
    		disableDependedPref(key, sharedPreferences.getString(key, ""));
    	
    	Activity activity = getActivity();
    	boolean canShow = (EditorProfilesActivity.mTwoPane) && (activity instanceof EditorProfilesActivity);
    	canShow = canShow || ((!EditorProfilesActivity.mTwoPane) && (activity instanceof ProfilePreferencesFragmentActivity));
    	if (canShow)
    		showActionMode();
	}
	
	private void createActionModeCallback()
	{
		actionModeCallback = new ActionMode.Callback() {
			 
            /** Invoked whenever the action mode is shown. This is invoked immediately after onCreateActionMode */
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        		MenuItem menuItem = menu.findItem(R.id.profile_preferences_action_mode_save);
        		menuItem.setTitle(menuItem.getTitle() + "    ");
                return true;
            }
 
            /** Called when user exits action mode */
            public void onDestroyActionMode(ActionMode mode) {
               actionMode = null;
               if (actionModeButtonClicked == BUTTON_CANCEL)
            	   onRestartProfilePreferencesCallback.onRestartProfilePreferences(profile, new_profile_mode);
            }
 
            /** This is called when the action mode is created. This is called by startActionMode() */
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.profile_preferences_action_mode, menu);
            	return true;
            }
 
            /** This is called when an item in the context menu is selected */
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.profile_preferences_action_mode_save:
        				savePreferences();
        				finishActionMode(BUTTON_SAVE);
                        return true;
                    default:
        				finishActionMode(BUTTON_CANCEL);
                        return false;                        
                }
            }

        };		
	}
	
	@SuppressLint("InflateParams")
	private void showActionMode()
	{
		profileNonEdited = false;
		
		if (actionMode != null)
			actionMode.finish();
		
		
    	actionModeButtonClicked = BUTTON_UNDEFINED;
    	
    	LayoutInflater inflater = LayoutInflater.from(getActivity());
    	View actionView = inflater.inflate(R.layout.profile_preferences_action_mode, null);
    	TextView title = (TextView)actionView.findViewById(R.id.profile_preferences_action_menu_title);
    	
       	title.setText(R.string.title_activity_profile_preferences);		

        actionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(actionModeCallback);
        actionMode.setCustomView(actionView); 
        
        onShowActionModeInProfilePreferencesCallback.onShowActionModeInProfilePreferences();        
        
        actionMode.getCustomView().findViewById(R.id.profile_preferences_action_menu_cancel).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				//Log.d("actionMode.onClick", "cancel");
				
				finishActionMode(BUTTON_CANCEL);
				
			}
       	});

	}
	
	public boolean isActionModeActive()
	{
		return (actionMode != null);
	}
	
	public void finishActionMode(int button)
	{
		int _button = button;
		
		if (_button == BUTTON_SAVE)
			new_profile_mode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;
		
		if (getActivity() instanceof ProfilePreferencesFragmentActivity)
		{
			actionModeButtonClicked = BUTTON_UNDEFINED;
			getActivity().finish(); // finish activity;
		}
		else
		if (actionMode != null)
		{	
			actionModeButtonClicked = _button;
			actionMode.finish();
		}
		
        onHideActionModeInProfilePreferencesCallback.onHideActionModeInProfilePreferences();        
		
	}

	static public Activity getPreferencesActivity()
	{
		return preferencesActivity;
	}
	
	static public void setChangedImageViewPreference(ImageViewPreference changedImageViewPref)
	{
		changedImageViewPreference = changedImageViewPref;
	}

}
