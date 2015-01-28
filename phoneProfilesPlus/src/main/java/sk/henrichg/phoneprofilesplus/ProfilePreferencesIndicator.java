package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;


public class ProfilePreferencesIndicator {
	
	private static Bitmap createIndicatorBitmap(Context context, int countDrawables)
	{
		// bitmapa, z ktorej zobrerieme velkost
    	Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_pref_volume_on);

		int width  = bmp.getWidth() * countDrawables; 
		int height  = bmp.getHeight();
		
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}
	
	private static void addIndicator(Bitmap indicatorBitmap, int preferenceBitmapResourceID, int index, Context context, Canvas canvas)
	{
		Bitmap preferenceBitmap = BitmapFactory.decodeResource(context.getResources(), preferenceBitmapResourceID);
		
		canvas.drawBitmap(preferenceBitmap, preferenceBitmap.getWidth() * index, 0, null);
		//canvas.save();
		
	}
	
	public static Bitmap paint(Profile _profile, Context context)
	{
		
		int[] drawables = new int[20];
		int countDrawables = 0;
		
		Profile profile = GlobalData.getMappedProfile(_profile, context);

		if (profile != null)
		{
			if (profile._volumeRingerMode == 5)
			{
				// zen mode
				// zen mode
				if (profile._volumeZenMode == 1)
				{
					drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
				}
				if (profile._volumeZenMode == 2)
				{
					drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
					drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_priority;
				}
				if (profile._volumeZenMode == 3)
				{
					drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
					drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_none;
				}
			}
			else
			{
				// volume on
				if ((profile._volumeRingerMode == 1) || (profile._volumeRingerMode == 2))
					drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_on;
				// vibration
				if ((profile._volumeRingerMode == 2) || (profile._volumeRingerMode == 3))
					drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
				// volume off
				if (profile._volumeRingerMode == 4)
					drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_off;
			}
			// volume level
			if (profile.getVolumeAlarmChange() ||
				profile.getVolumeMediaChange() ||
				profile.getVolumeNotificationChange() ||
				profile.getVolumeRingtoneChange() ||
				profile.getVolumeSystemChange() ||
				profile.getVolumeVoiceChange())
				drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_level;
			// speaker phone
			if (profile._volumeSpeakerPhone == 1)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone;
			if (profile._volumeSpeakerPhone == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone_off;
			// sound
			if ((profile._soundRingtoneChange == 1) || 
				(profile._soundNotificationChange == 1) || 
				(profile._soundAlarmChange == 1))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_sound;
			// airplane mode
			if ((profile._deviceAirplaneMode == 1) || (profile._deviceAirplaneMode == 3))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode;
			if (profile._deviceAirplaneMode == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode_off;
			// auto-sync
			if ((profile._deviceAutosync == 1) || (profile._deviceAutosync == 3))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync;
			if (profile._deviceAutosync == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync_off;
			// mobile data
			if ((profile._deviceMobileData == 1) || (profile._deviceMobileData == 3))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata;
			if (profile._deviceMobileData == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_off;
			// mobile data preferences
			if (profile._deviceMobileDataPrefs == 1)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_pref;
			// wifi
			if ((profile._deviceWiFi == 1) || (profile._deviceWiFi == 3))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi;
			if (profile._deviceWiFi == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_off;
			// bluetooth
			if ((profile._deviceBluetooth == 1) || (profile._deviceBluetooth == 3))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth;
			if (profile._deviceBluetooth == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth_off;
			// gps
			if ((profile._deviceGPS == 1) || (profile._deviceGPS == 3))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_on;
			if (profile._deviceGPS == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_off;
			// location settings preferences
			if (profile._deviceLocationServicePrefs == 1)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_locationsettings_pref;
			// nfc
			if ((profile._deviceNFC == 1) || (profile._deviceNFC == 3))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc;
			if (profile._deviceNFC == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc_off;
			// screen timeout
			if (profile._deviceScreenTimeout != 0)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_timeout;
			// lockscreen
			if ((profile._deviceKeyguard == 1) || (profile._deviceKeyguard == 3))
				drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen;
			if (profile._deviceKeyguard == 2)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen_off;
			// brightness/autobrightness
			if (profile.getDeviceBrightnessChange())
				if (profile.getDeviceBrightnessAutomatic())
					drawables[countDrawables++] = R.drawable.ic_profile_pref_autobrightness;
				else
					drawables[countDrawables++] = R.drawable.ic_profile_pref_brightness;
			// auto-rotate
			if (profile._deviceAutoRotate == 1)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate_off;
			else
			if (profile._deviceAutoRotate != 0)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate;
			// run application
			if (profile._deviceRunApplicationChange == 1)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_run_application;
			// wallpaper
			if (profile._deviceWallpaperChange == 1)
				drawables[countDrawables++] = R.drawable.ic_profile_pref_wallpaper;
			
		}
		else
			countDrawables = -1;
		
		Bitmap indicatorBitmap;
		if (countDrawables >= 0)
		{
			if (countDrawables > 0)
			{
				indicatorBitmap = createIndicatorBitmap(context, countDrawables);
				Canvas canvas = new Canvas(indicatorBitmap);
			
				for (int i = 0; i < countDrawables; i++)
					addIndicator(indicatorBitmap, drawables[i], i, context, canvas);
			}
			else
				indicatorBitmap = createIndicatorBitmap(context, 1);
		}
		else
			indicatorBitmap = null;
		
		return indicatorBitmap;
		
	}

}
