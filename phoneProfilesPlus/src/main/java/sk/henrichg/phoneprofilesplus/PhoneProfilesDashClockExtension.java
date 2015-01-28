package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class PhoneProfilesDashClockExtension extends DashClockExtension {

	private DataWrapper dataWrapper;
	private static PhoneProfilesDashClockExtension instance;
	
	public PhoneProfilesDashClockExtension()
	{
		instance = this;
	}
	
	public static PhoneProfilesDashClockExtension getInstance()
	{
		return instance;
	}
	
	@Override
    protected void onInitialize(boolean isReconnect) {
		super.onInitialize(isReconnect);

		GlobalData.loadPreferences(this);
		GUIData.setLanguage(this);
		
		if (dataWrapper == null)
			dataWrapper = new DataWrapper(this, true, false, 0);
	
		setUpdateWhenScreenOn(true);
	}
	
	@Override
	public void onDestroy()
	{
		/*if (dataWrapper != null) 
			dataWrapper.invalidateDataWrapper();
		dataWrapper = null;*/
	}
	

	private int maxLength;
	private String addIntoIndicator(String indicator, String preference)
	{
		String ind = indicator;
		if (ind.length() > maxLength)
		{
			ind = ind + '\n';
			maxLength += 25;
		}
		else
			if (ind != "") ind = ind + "-";
		ind = ind + preference;
		return ind;
	}
	
	@Override
	protected void onUpdateData(int reason) {
		
		Profile profile;
		
		if (dataWrapper == null)
			return;
		
		profile = GlobalData.getMappedProfile(
									dataWrapper.getActivatedProfile(), this);
		
		boolean isIconResourceID;
		String iconIdentifier;
		String profileName;
		if (profile != null)
		{
			isIconResourceID = profile.getIsIconResourceID();
			iconIdentifier = profile.getIconIdentifier();
			profileName = dataWrapper.getProfileNameWithManualIndicator(profile, true); 
		}
		else
		{
			isIconResourceID = true;
			iconIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
			profileName = getResources().getString(R.string.profiles_header_profile_name_no_activated);
		}
		int iconResource;
		if (isIconResourceID)
			iconResource = getResources().getIdentifier(iconIdentifier, "drawable", getPackageName());
		else
			iconResource = getResources().getIdentifier(GlobalData.PROFILE_ICON_DEFAULT, "drawable", getPackageName());
	
		// profile preferences indicator
		String indicator1 = "";
		if (profile != null)
		{
			maxLength = 25;
			if (profile._volumeRingerMode == 5)
			{
				// zen mode
				if (profile._volumeZenMode == 1)
				{
					indicator1 = addIntoIndicator(indicator1, "int");
				}
				if (profile._volumeZenMode == 2)
				{
					indicator1 = addIntoIndicator(indicator1, "int");
					indicator1 = addIntoIndicator(indicator1, "pri");
				}
				if (profile._volumeZenMode == 3)
				{
					indicator1 = addIntoIndicator(indicator1, "int");
					indicator1 = addIntoIndicator(indicator1, "non");
				}
			}
			else
			{
				// volume on
				if ((profile._volumeRingerMode == 1) || (profile._volumeRingerMode == 2))
					indicator1 = addIntoIndicator(indicator1, "rng");
				// vibration
				if ((profile._volumeRingerMode == 2) || (profile._volumeRingerMode == 3))
					indicator1 = addIntoIndicator(indicator1, "vib");
				// volume off
				if (profile._volumeRingerMode == 4)
					indicator1 = addIntoIndicator(indicator1, "sil");
			}
			// volume level
			if (profile.getVolumeAlarmChange() ||
				profile.getVolumeMediaChange() ||
				profile.getVolumeNotificationChange() ||
				profile.getVolumeRingtoneChange() ||
				profile.getVolumeSystemChange() ||
				profile.getVolumeVoiceChange())
				indicator1 = addIntoIndicator(indicator1, "vol");
			// speaker phone
			if (profile._volumeSpeakerPhone == 1)
				indicator1 = addIntoIndicator(indicator1, "sp1");
			if (profile._volumeSpeakerPhone == 2)
				indicator1 = addIntoIndicator(indicator1, "sp0");
			// sound
			if ((profile._soundRingtoneChange == 1) || 
				(profile._soundNotificationChange == 1) || 
				(profile._soundAlarmChange == 1))
				indicator1 = addIntoIndicator(indicator1, "snd");
			// airplane mode
			if ((profile._deviceAirplaneMode == 1) || (profile._deviceAirplaneMode == 3))
				indicator1 = addIntoIndicator(indicator1, "am1");
			if (profile._deviceAirplaneMode == 2)
				indicator1 = addIntoIndicator(indicator1, "am0");
			// auto-sync
			if ((profile._deviceAutosync == 1) || (profile._deviceAutosync == 3))
				indicator1 = addIntoIndicator(indicator1, "as1");
			if (profile._deviceAutosync == 2)
				indicator1 = addIntoIndicator(indicator1, "as0");
			// mobile data
			if ((profile._deviceMobileData == 1) || (profile._deviceMobileData == 3))
				indicator1 = addIntoIndicator(indicator1, "md1");
			if (profile._deviceMobileData == 2)
				indicator1 = addIntoIndicator(indicator1, "md0");
			// mobile data preferences
			if (profile._deviceMobileDataPrefs == 1)
				indicator1 = addIntoIndicator(indicator1, "mdP");
			// wifi
			if ((profile._deviceWiFi == 1) || (profile._deviceWiFi == 3))
				indicator1 = addIntoIndicator(indicator1, "wf1");
			if (profile._deviceWiFi == 2)
				indicator1 = addIntoIndicator(indicator1, "wf0");
			// bluetooth
			if ((profile._deviceBluetooth == 1) || (profile._deviceBluetooth == 3))
				indicator1 = addIntoIndicator(indicator1, "bt1");
			if (profile._deviceBluetooth == 2)
				indicator1 = addIntoIndicator(indicator1, "bt0");
			// gps
			if ((profile._deviceGPS == 1) || (profile._deviceGPS == 3))
				indicator1 = addIntoIndicator(indicator1, "gp1");
			if (profile._deviceGPS == 2)
				indicator1 = addIntoIndicator(indicator1, "gp0");
			// location settings preferences
			if (profile._deviceLocationServicePrefs == 1)
				indicator1 = addIntoIndicator(indicator1, "loP");
			// nfc
			if ((profile._deviceNFC == 1) || (profile._deviceNFC == 3))
				indicator1 = addIntoIndicator(indicator1, "nf1");
			if (profile._deviceNFC == 2)
				indicator1 = addIntoIndicator(indicator1, "nf0");
			// screen timeout
			if (profile._deviceScreenTimeout != 0)
				indicator1 = addIntoIndicator(indicator1, "stm");
			// lockscreen
			if ((profile._deviceKeyguard == 1) || (profile._deviceKeyguard == 3))
				indicator1 = addIntoIndicator(indicator1, "ls1");
			if (profile._deviceKeyguard == 2)
				indicator1 = addIntoIndicator(indicator1, "ls0");
			// brightness/autobrightness
			if (profile.getDeviceBrightnessChange())
			{
				if (profile.getDeviceBrightnessAutomatic())
					indicator1 = addIntoIndicator(indicator1, "brA");
				else
					indicator1 = addIntoIndicator(indicator1, "brt");
			}
			// autorotation
			if (profile._deviceAutoRotate == 1)
				indicator1 = addIntoIndicator(indicator1, "aro");
			else
			if (profile._deviceAutoRotate != 0)
				indicator1 = addIntoIndicator(indicator1, "rot");
			// run application
			if (profile._deviceRunApplicationChange == 1)
				indicator1 = addIntoIndicator(indicator1, "rap");
			// wallpaper
			if (profile._deviceWallpaperChange == 1)
				indicator1 = addIntoIndicator(indicator1, "wlp");
		}
		/////////////////////////////////////////////////////////////
		
		// intent
		Intent intent = new Intent(this, LauncherActivity.class);
		intent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_WIDGET);
		
		String status = "";
		if (GlobalData.getEventsBlocked(getBaseContext()))
			if (GlobalData.getForceRunEventRunning(getBaseContext()))
			{
	   			/*if (android.os.Build.VERSION.SDK_INT >= 16)
	   				status = "\u23E9";
	   			else*/
	   				status = "[\u00BB]";
			}
			else
			{
	   			/*if (android.os.Build.VERSION.SDK_INT >= 16)
	   				status = "\uD83D\uDC46";
	   			else */
	   				status = "[M]";
			}
		
	    // Publish the extension data update.
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(iconResource)
                .status(status)
                .expandedTitle(profileName)
                .expandedBody(indicator1)
                .contentDescription("PhoneProfilesPlus - "+profileName)
                .clickIntent(intent));		
	}

	public void updateExtension()
	{
		onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
	}
	
}
