package sk.henrichg.phoneprofilesplus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.stericson.RootShell.RootShell;
import com.stericson.RootTools.RootTools;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

public class GlobalData extends Application {

	static String PACKAGE_NAME;
	
	private static boolean logIntoLogCat = false;
	private static boolean logIntoFile = false;
	private static boolean rootToolsDebug = false;
	public static String logFilterTags = //"@@@ BatteryEventBroadcastReceiver|"+
										 //"@@@ CalendarProviderChangedBroadcastReceiver|"+
										 //"@@@ EventsCalendarBroadcastReceiver|"+
										 //"@@@ EventsTimeBroadcastReceiver|"+
										 //"HeadsetConnectionBroadcastReceiver"
										 //"@@@ SearchCalendarEventsBroadcastReceiver|"+
										 //"@@@ WifiConnectionBroadcastReceiver|"+
										 //"@@@ WifiScanAlarmBroadcastReceiver|"+
										 //"@@@ WifiScanBroadcastReceiver|"+
										 //"@@@ WifiStateChangedBroadcastReceiver|"+
										 //"@@@ ActivateProfileHelper"
										 //"@@@ BluetoothConnectionBroadcastReceiver|"+
										 //"@@@ BluetoothScanAlarmBroadcastReceiver|"+
										 //"@@@ BluetoothScanBroadcastReceiver|"+
										 //"@@@ BluetoothStateChangedBroadcastReceiver|"+
										 //"@@@ ScreenOnOffBroadcastReceiver"
										 //"@@@ RestartEventsBroadcastReceiver|"+
										 //"@@@ EventDelayBroadcastReceiver|"+
										 //"@@@ BootUpReceiver|"+
										 //"@@@ PackageReplacedReceiver|"+
										 //"DataWrapper.doEventService|"+
										 //"@@@ EventsService|"+
										 //"@@@ Event|"
										 //"### ScannerService|"+
										 //"@@@ ScannerService|"+
										 //"@@@ ActivateProfileHelper|"+
										 //"PhoneProfilesHelper.doInstallPPHelper"
										 //"ExecuteRadioProfilePrefsService"
										 "### EventsService"
			;
	
	
	public static final String EXPORT_PATH = "/PhoneProfilesPlus";
	public static final String LOG_FILENAME = "log.txt";

	static final String EXTRA_PROFILE_ID = "profile_id";
	static final String EXTRA_EVENT_ID = "event_id";
	static final String EXTRA_START_APP_SOURCE = "start_app_source";
	static final String EXTRA_RESET_EDITOR = "reset_editor";
	static final String EXTRA_NEW_PROFILE_MODE = "new_profile_mode";
	static final String EXTRA_NEW_EVENT_MODE = "new_event_mode";
	static final String EXTRA_PREFERENCES_STARTUP_SOURCE = "preferences_startup_source";
	static final String EXTRA_START_SYSTEM_EVENT = "start_system_event";
	static final String EXTRA_SECOND_SET_VOLUMES = "second_set_volumes";
	static final String EXTRA_BROADCAST_RECEIVER_TYPE = "broadcast_receiver_type";
	static final String EXTRA_SCANNER_TYPE = "scanner_type";

	static final int STARTUP_SOURCE_NOTIFICATION = 1;
	static final int STARTUP_SOURCE_WIDGET = 2;
	static final int STARTUP_SOURCE_SHORTCUT = 3;
	static final int STARTUP_SOURCE_BOOT = 4;
	static final int STARTUP_SOURCE_ACTIVATOR = 5;
	static final int STARTUP_SOURCE_SERVICE = 6;
	static final int STARTUP_SOURCE_EDITOR = 8;
	static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
	static final int STARTUP_SOURCE_LAUNCHER_START = 10;
	static final int STARTUP_SOURCE_LAUNCHER = 11;

	static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
	static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
	static final int PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE = 3;
	
	// request code for startActivityForResult with intent BackgroundActivateProfileActivity
	static final int REQUEST_CODE_ACTIVATE_PROFILE = 6220;
	// request code for startActivityForResult with intent ProfilePreferencesFragmentActivity
	static final int REQUEST_CODE_PROFILE_PREFERENCES = 6221;
	// request code for startActivityForResult with intent EventPreferencesFragmentActivity
	static final int REQUEST_CODE_EVENT_PREFERENCES = 6222;
	// request code for startActivityForResult with intent PhoneProfilesActivity
	static final int REQUEST_CODE_APPLICATION_PREFERENCES = 6229;
	// request code for startActivityForResult with intent "phoneprofiles.intent.action.EXPORTDATA"
	static final int REQUEST_CODE_REMOTE_EXPORT = 6250;
	
	static final int NOTIFICATION_ID = 700420;
	
	static final String PREF_PROFILE_NAME = "prf_pref_profileName";
	static final String PREF_PROFILE_ICON = "prf_pref_profileIcon";
	static final String PREF_PROFILE_VOLUME_RINGER_MODE = "prf_pref_volumeRingerMode";
	static final String PREF_PROFILE_VOLUME_ZEN_MODE = "prf_pref_volumeZenMode";
	static final String PREF_PROFILE_VOLUME_RINGTONE = "prf_pref_volumeRingtone";
	static final String PREF_PROFILE_VOLUME_NOTIFICATION = "prf_pref_volumeNotification";
	static final String PREF_PROFILE_VOLUME_MEDIA = "prf_pref_volumeMedia";
	static final String PREF_PROFILE_VOLUME_ALARM = "prf_pref_volumeAlarm";
	static final String PREF_PROFILE_VOLUME_SYSTEM = "prf_pref_volumeSystem";
	static final String PREF_PROFILE_VOLUME_VOICE = "prf_pref_volumeVoice";
	static final String PREF_PROFILE_SOUND_RINGTONE_CHANGE = "prf_pref_soundRingtoneChange";
	static final String PREF_PROFILE_SOUND_RINGTONE = "prf_pref_soundRingtone";
	static final String PREF_PROFILE_SOUND_NOTIFICATION_CHANGE = "prf_pref_soundNotificationChange";
	static final String PREF_PROFILE_SOUND_NOTIFICATION = "prf_pref_soundNotification";
	static final String PREF_PROFILE_SOUND_ALARM_CHANGE = "prf_pref_soundAlarmChange";
	static final String PREF_PROFILE_SOUND_ALARM = "prf_pref_soundAlarm";
	static final String PREF_PROFILE_DEVICE_AIRPLANE_MODE = "prf_pref_deviceAirplaneMode";
	static final String PREF_PROFILE_DEVICE_WIFI = "prf_pref_deviceWiFi";
	static final String PREF_PROFILE_DEVICE_BLUETOOTH = "prf_pref_deviceBluetooth";
	static final String PREF_PROFILE_DEVICE_SCREEN_TIMEOUT = "prf_pref_deviceScreenTimeout";
	static final String PREF_PROFILE_DEVICE_BRIGHTNESS = "prf_pref_deviceBrightness";
	static final String PREF_PROFILE_DEVICE_WALLPAPER_CHANGE = "prf_pref_deviceWallpaperChange";
	static final String PREF_PROFILE_DEVICE_WALLPAPER = "prf_pref_deviceWallpaper";
	static final String PREF_PROFILE_DEVICE_MOBILE_DATA = "prf_pref_deviceMobileData";
	static final String PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS = "prf_pref_deviceMobileDataPrefs";
	static final String PREF_PROFILE_DEVICE_GPS = "prf_pref_deviceGPS";
	static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE = "prf_pref_deviceRunApplicationChange";
	static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME = "prf_pref_deviceRunApplicationPackageName";
	static final String PREF_PROFILE_DEVICE_AUTOSYNC = "prf_pref_deviceAutosync";
	static final String PREF_PROFILE_SHOW_IN_ACTIVATOR = "prf_pref_showInActivator";
	static final String PREF_PROFILE_DEVICE_AUTOROTATE = "prf_pref_deviceAutoRotation";
	static final String PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS = "prf_pref_deviceLocationServicePrefs";
	static final String PREF_PROFILE_VOLUME_SPEAKER_PHONE = "prf_pref_volumeSpeakerPhone";
	static final String PREF_PROFILE_DEVICE_NFC = "prf_pref_deviceNFC";
	static final String PREF_PROFILE_DURATION = "prf_pref_duration";
	static final String PREF_PROFILE_AFTER_DURATION_DO = "prf_pref_afterDurationDo";
	static final String PREF_PROFILE_DEVICE_KEYGUARD = "prf_pref_deviceKeyguard";
	
	static final String PROFILE_ICON_DEFAULT = "ic_profile_default";
	
	static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
	static final String DEFAULT_PROFILE_PREFS_NAME = "profile_preferences_default_profile"; 
	// for synchronization between wifi/bluetooth scanner, local radio changes and PPHelper radio changes
	static final String RADIO_CHANGE_PREFS_NAME = "sk.henrichg.phoneprofiles.radio_change"; 

	static final String PREF_RADIO_CHANGE_STATE = "sk.henrichg.phoneprofiles.radioChangeState";
	
    public static final String PREF_APPLICATION_START_ON_BOOT = "applicationStartOnBoot";
    public static final String PREF_APPLICATION_ACTIVATE = "applicationActivate";
    public static final String PREF_APPLICATION_ALERT = "applicationAlert";
    public static final String PREF_APPLICATION_CLOSE = "applicationClose";
    public static final String PREF_APPLICATION_LONG_PRESS_ACTIVATION = "applicationLongClickActivation";
    public static final String PREF_APPLICATION_LANGUAGE = "applicationLanguage";
    public static final String PREF_APPLICATION_THEME = "applicationTheme";
    public static final String PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR = "applicationActivatorPrefIndicator";
    public static final String PREF_APPLICATION_EDITOR_PREF_INDICATOR = "applicationEditorPrefIndicator";
    public static final String PREF_APPLICATION_ACTIVATOR_HEADER = "applicationActivatorHeader";
    public static final String PREF_APPLICATION_EDITOR_HEADER = "applicationEditorHeader";
    public static final String PREF_NOTIFICATION_TOAST = "notificationsToast";
    public static final String PREF_NOTIFICATION_STATUS_BAR  = "notificationStatusBar";
    public static final String PREF_NOTIFICATION_STATUS_BAR_STYLE  = "notificationStatusBarStyle";
    public static final String PREF_NOTIFICATION_STATUS_BAR_PERMANENT  = "notificationStatusBarPermanent";
    public static final String PREF_NOTIFICATION_STATUS_BAR_CANCEL  = "notificationStatusBarCancel";
    public static final String PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR = "applicationWidgetListPrefIndicator";
    public static final String PREF_APPLICATION_WIDGET_LIST_HEADER = "applicationWidgetListHeader";
    public static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND = "applicationWidgetListBackground";
    public static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B = "applicationWidgetListLightnessB";
    public static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T = "applicationWidgetListLightnessT";
    public static final String PREF_APPLICATION_WIDGET_ICON_COLOR = "applicationWidgetIconColor";
    public static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS = "applicationWidgetIconLightness";
    public static final String PREF_APPLICATION_WIDGET_LIST_ICON_COLOR = "applicationWidgetListIconColor";
    public static final String PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS = "applicationWidgetListIconLightness";
	public static final String PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER = "applicationEditorAutoCloseDrawer";
	public static final String PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE = "applicationEditorSaveEditorState";
    public static final String PREF_NOTIFICATION_PREF_INDICATOR = "notificationPrefIndicator";
    public static final String PREF_APPLICATION_HOME_LAUNCHER = "applicationHomeLauncher";
    public static final String PREF_APPLICATION_WIDGET_LAUNCHER = "applicationWidgetLauncher";
    public static final String PREF_APPLICATION_NOTIFICATION_LAUNCHER = "applicationNotificationLauncher";
    public static final String PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL = "applicationEventWifiScanInterval";
    public static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI = "applicationEventWifiEnableWifi";
    public static final String PREF_APPLICATION_BACKGROUND_PROFILE = "applicationBackgroundProfile";
    public static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT= "applicationActivatorGridLayout";
    public static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT= "applicationWidgetListGridLayout";
    public static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL = "applicationEventBluetoothScanInterval";
    public static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH = "applicationEventBluetoothEnableBluetooth";
    public static final String PREF_APPLICATION_EVENT_WIFI_RESCAN = "applicationEventWifiRescan";
    public static final String PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN = "applicationEventBluetoothRescan";
    public static final String PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME = "applicationWidgetIconHideProfileName";

    public static final int HARDWARE_CHECK_NOT_ALLOWED = 0;
    public static final int HARDWARE_CHECK_ALLOWED = 1;
    public static final int HARDWARE_CHECK_INSTALL_PPHELPER = 2;
    public static final int HARDWARE_CHECK_UPGRADE_PPHELPER = 3;
    
	public static final long DEFAULT_PROFILE_ID = -999;  // source profile id
	public static final long PROFILE_NO_ACTIVATE = -999;
	
	public static final String SCANNER_TYPE_WIFI = "wifi";
	public static final String SCANNER_TYPE_BLUETOOTH = "bluetooth";
	
	public static final String RESCAN_TYPE_NONE = "0";
	public static final String RESCAN_TYPE_SCREEN_ON = "1";
	public static final String RESCAN_TYPE_RESTART_EVENTS = "2";
	public static final String RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS = "3";

	// global internal preferences
	private static final String PREF_GLOBAL_EVENTS_RUN_STOP = "globalEventsRunStop";
	private static final String PREF_APPLICATION_STARTED = "applicationStarted";
	private static final String PREF_EVENTS_BLOCKED = "eventsBlocked";
	private static final String PREF_FORCE_RUN_EVENT_RUNNING = "forceRunEventRunning";
	private static final String PREF_ACTIVATED_PROFILE_FOR_DURATION = "activatedProfileForDuration";
	private static final String PREF_FORCE_ONE_BLUETOOTH_SCAN = "forceOneBluetoothScan";
	private static final String PREF_FORCE_ONE_WIFI_SCAN = "forceOneWifiScan";

	// preferences for event - filled with broadcast receivers
	static final String PREF_EVENT_CALL_EVENT_TYPE = "eventCallEventType";
	static final String PREF_EVENT_CALL_PHONE_NUMBER = "eventCallPhoneNumber";
	static final String PREF_EVENT_HEADSET_CONNECTED = "eventHeadsetConnected";
	static final String PREF_EVENT_HEADSET_MICROPHONE = "eventHeadsetMicrophone";
	static final String PREF_EVENT_HEADSET_BLUETOOTH = "eventHeadsetBluetooth";
	static final String PREF_EVENT_WIFI_START_SCAN = "eventWifiStartScan";
	static final String PREF_EVENT_WIFI_ENABLED_FOR_SCAN = "eventWifiEnabledForScan"; 
	static final String PREF_EVENT_BLUETOOTH_START_SCAN = "eventBluetoothStartScan";
	static final String PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN = "eventBluetoothEnabledForScan"; 
	static final String PREF_EVENT_SMS_EVENT_TYPE = "eventSMSEventType";
	static final String PREF_EVENT_SMS_PHONE_NUMBER = "eventSMSPhoneNumber";
	static final String PREF_EVENT_SMS_DATE = "eventSMSDate";
	static final String PREF_EVENT_WIFI_LAST_STATE = "eventWifiLastState";
	static final String PREF_EVENT_BLUETOOTH_LAST_STATE = "eventBluetoothLastState";
	
    public static boolean applicationStartOnBoot;
    public static boolean applicationActivate;
    public static boolean applicationActivateWithAlert;
    public static boolean applicationClose;
    public static boolean applicationLongClickActivation;
    public static String applicationLanguage;
    public static String applicationTheme;
    public static boolean applicationActivatorPrefIndicator;
    public static boolean applicationEditorPrefIndicator;
    public static boolean applicationActivatorHeader;
    public static boolean applicationEditorHeader;
    public static boolean notificationsToast;
    public static boolean notificationStatusBar;
    public static boolean notificationStatusBarPermanent;
    public static String notificationStatusBarCancel;
    public static String notificationStatusBarStyle;
    public static boolean applicationWidgetListPrefIndicator;
    public static boolean applicationWidgetListHeader;
    public static String applicationWidgetListBackground;
    public static String applicationWidgetListLightnessB;
    public static String applicationWidgetListLightnessT;
    public static String applicationWidgetIconColor;
    public static String applicationWidgetIconLightness;
    public static String applicationWidgetListIconColor;
    public static String applicationWidgetListIconLightness;
    public static boolean applicationEditorAutoCloseDrawer;
    public static boolean applicationEditorSaveEditorState;
    public static boolean notificationPrefIndicator;
    public static String applicationHomeLauncher;
    public static String applicationWidgetLauncher;
    public static String applicationNotificationLauncher;
    public static int applicationEventWifiScanInterval;
    public static boolean applicationEventWifiEnableWifi;
    public static String applicationBackgroundProfile;
    public static boolean applicationActivatorGridLayout;
    public static boolean applicationWidgetListGridLayout;
    public static int applicationEventBluetoothScanInterval;
    public static boolean applicationEventBluetoothEnableBluetooth;
    public static String applicationEventWifiRescan;
    public static String applicationEventBluetoothRescan;
    public static boolean applicationWidgetIconHideProfileName;
    
    public static RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    
	public void onCreate()
	{
	//	Debug.startMethodTracing("phoneprofiles");
		
		//resetLog();
		
		super.onCreate();
		
		PACKAGE_NAME = this.getPackageName();
		
		// initialization
		loadPreferences(this);
		
		Keyguard.initialize(getApplicationContext());

		//Log.d("GlobalData.onCreate", "memory usage (after create activateProfileHelper)=" + Debug.getNativeHeapAllocatedSize());
		
		//Log.d("GlobalData.onCreate","xxx");
		
	}
	
	public void onTerminate ()
	{
		DatabaseHandler.getInstance(this).closeConnecion();
	}
	
	//--------------------------------------------------------------
	
	static private void resetLog()
	{
		File sd = Environment.getExternalStorageDirectory();
		File exportDir = new File(sd, GlobalData.EXPORT_PATH);
		if (!(exportDir.exists() && exportDir.isDirectory()))
			exportDir.mkdirs();
		
		File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
		logFile.delete();
	}

	@SuppressLint("SimpleDateFormat")
	static private void logIntoFile(String type, String tag, String text)
	{
		if (!logIntoFile)
			return;

		File sd = Environment.getExternalStorageDirectory();
		File exportDir = new File(sd, GlobalData.EXPORT_PATH);
		if (!(exportDir.exists() && exportDir.isDirectory()))
			exportDir.mkdirs();
		
		File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);

		if (logFile.length() > 1024 * 10000)
			resetLog();

	    if (!logFile.exists())
	    {
	        try
	        {
	            logFile.createNewFile();
	        } 
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
	    }
	    try
	    {
	        //BufferedWriter for performance, true to set append to file flag
	        BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
	        String log = "";
		    SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
		    String time = sdf.format(Calendar.getInstance().getTimeInMillis());
	        log = log + time + "--" + type + "-----" + tag + "------" + text;
	        buf.append(log);
	        buf.newLine();
	        buf.flush();
	        buf.close();
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	    }		
	}

	private static boolean logContainsFilterTag(String tag)
	{
		boolean contains = false;
		String[] splits = logFilterTags.split("\\|");
		for (int i = 0; i < splits.length; i++)
		{
			if (tag.contains(splits[i]))
			{
				contains = true;
				break;		
			}
		}
		return contains;
	}
	
	static public void logI(String tag, String text)
	{
		if (!(logIntoLogCat || logIntoFile))
			return;
			
		if (logContainsFilterTag(tag))
		{
			if (logIntoLogCat) Log.i(tag, text);
			logIntoFile("I", tag, text);
		}
	}
	
	static public void logW(String tag, String text)
	{
		if (!(logIntoLogCat || logIntoFile))
			return;

		if (logContainsFilterTag(tag))
		{
			if (logIntoLogCat) Log.w(tag, text);
			logIntoFile("W", tag, text);
		}
	}
	
	static public void logE(String tag, String text)
	{
		if (!(logIntoLogCat || logIntoFile))
			return;

		if (logContainsFilterTag(tag))
		{
			if (logIntoLogCat) Log.e(tag, text);
			logIntoFile("E", tag, text);
		}
	}

	static public void logD(String tag, String text)
	{
		if (!(logIntoLogCat || logIntoFile))
			return;

		if (logContainsFilterTag(tag))
		{
			if (logIntoLogCat) Log.d(tag, text);
			logIntoFile("D", tag, text);
		}
	}
	
	//--------------------------------------------------------------
	
	static public void loadPreferences(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

	    applicationStartOnBoot = preferences.getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
	    applicationActivate = preferences.getBoolean(PREF_APPLICATION_ACTIVATE, true);
	    applicationActivateWithAlert = preferences.getBoolean(PREF_APPLICATION_ALERT, true);
	    applicationClose = preferences.getBoolean(PREF_APPLICATION_CLOSE, true);
	    applicationLongClickActivation = preferences.getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
	    applicationLanguage = preferences.getString(PREF_APPLICATION_LANGUAGE, "system");
	    applicationTheme = preferences.getString(PREF_APPLICATION_THEME, "material");
	    applicationActivatorPrefIndicator = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
	    applicationEditorPrefIndicator = preferences.getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
	    applicationActivatorHeader = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
	    applicationEditorHeader = preferences.getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
	    notificationsToast = preferences.getBoolean(PREF_NOTIFICATION_TOAST, true);
	    notificationStatusBar = preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
	    notificationStatusBarPermanent = preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
	    notificationStatusBarCancel = preferences.getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
	    notificationStatusBarStyle = preferences.getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
	    applicationWidgetListPrefIndicator = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
	    applicationWidgetListHeader = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
	    applicationWidgetListBackground = preferences.getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
	    applicationWidgetListLightnessB = preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
	    applicationWidgetListLightnessT = preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
	    applicationWidgetIconColor = preferences.getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
	    applicationWidgetIconLightness = preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");;
	    applicationWidgetListIconColor = preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
	    applicationWidgetListIconLightness = preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");;
	    applicationEditorAutoCloseDrawer = preferences.getBoolean(PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true);
	    applicationEditorSaveEditorState = preferences.getBoolean(PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, false);
	    notificationPrefIndicator = preferences.getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
	    applicationHomeLauncher = preferences.getString(PREF_APPLICATION_HOME_LAUNCHER, "activator");
	    applicationWidgetLauncher = preferences.getString(PREF_APPLICATION_WIDGET_LAUNCHER, "activator");
	    applicationNotificationLauncher = preferences.getString(PREF_APPLICATION_NOTIFICATION_LAUNCHER, "activator");
	    applicationEventWifiScanInterval = Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "10"));
	    applicationEventWifiEnableWifi = preferences.getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true);
	    applicationBackgroundProfile = preferences.getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
	    applicationActivatorGridLayout = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, false);
	    applicationWidgetListGridLayout = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, false);
	    applicationEventBluetoothScanInterval = Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "10"));
	    applicationEventBluetoothEnableBluetooth = preferences.getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true);
	    applicationEventWifiRescan = preferences.getString(PREF_APPLICATION_EVENT_WIFI_RESCAN, "0");
	    applicationEventBluetoothRescan = preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "0");
	    applicationWidgetIconHideProfileName = preferences.getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);

		if (applicationTheme.equals("light"))
		{
            applicationTheme = "material";
    		Editor editor = preferences.edit();
    		editor.putString(PREF_APPLICATION_THEME, applicationTheme);
    		editor.commit();
		}
	}
	
	private static String getVolumeLevelString(int percentage, int maxValue)
	{
		Double dValue = maxValue / 100.0 * percentage;
		return String.valueOf(dValue.intValue());
	}
	
	private static void moveDefaultProfilesPreference(Context context)
	{
		SharedPreferences oldPreferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences newPreferences = context.getSharedPreferences(DEFAULT_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editorNew = newPreferences.edit();
		SharedPreferences.Editor editorOld = oldPreferences.edit();
		Map<String, ?> all = oldPreferences.getAll();
		for (Entry<String, ?> x : all.entrySet()) {
			
			if (x.getKey().equals(PREF_PROFILE_NAME) ||
				x.getKey().equals(PREF_PROFILE_NAME) ||
				x.getKey().equals(PREF_PROFILE_ICON) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_RINGER_MODE) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_ZEN_MODE) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_RINGTONE) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_NOTIFICATION) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_MEDIA) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_ALARM) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_SYSTEM) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_VOICE) ||
				x.getKey().equals(PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
				x.getKey().equals(PREF_PROFILE_SOUND_RINGTONE) ||
				x.getKey().equals(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
				x.getKey().equals(PREF_PROFILE_SOUND_NOTIFICATION) ||
				x.getKey().equals(PREF_PROFILE_SOUND_ALARM_CHANGE) ||
				x.getKey().equals(PREF_PROFILE_SOUND_ALARM) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_WIFI) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_BLUETOOTH) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_BRIGHTNESS) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_WALLPAPER) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_MOBILE_DATA) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_GPS) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_AUTOSYNC) ||
				x.getKey().equals(PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_AUTOROTATE) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
				x.getKey().equals(PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_NFC) ||
				x.getKey().equals(PREF_PROFILE_DURATION) ||
				x.getKey().equals(PREF_PROFILE_AFTER_DURATION_DO) ||
				x.getKey().equals(PREF_PROFILE_DEVICE_KEYGUARD))
			{
			    if      (x.getValue().getClass().equals(Boolean.class)) editorNew.putBoolean(x.getKey(), (Boolean)x.getValue());
			    else if (x.getValue().getClass().equals(Float.class))   editorNew.putFloat(x.getKey(),   (Float)x.getValue());
			    else if (x.getValue().getClass().equals(Integer.class)) editorNew.putInt(x.getKey(),     (Integer)x.getValue());
			    else if (x.getValue().getClass().equals(Long.class))    editorNew.putLong(x.getKey(),    (Long)x.getValue());
			    else if (x.getValue().getClass().equals(String.class))  editorNew.putString(x.getKey(),  (String)x.getValue());

				editorOld.remove(x.getKey());
			}
		}
		editorNew.commit();
		editorOld.commit(); 		
	}
	
	static public Profile getDefaultProfile(Context context)
	{
		// move default profile preferences into new file
		moveDefaultProfilesPreference(context);
		
		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		int	maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		int	maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		int	maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int	maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		int	maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		int	maximumValueVoicecall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		
		SharedPreferences preferences = context.getSharedPreferences(DEFAULT_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
		
		Profile profile = new Profile();
		profile._id = DEFAULT_PROFILE_ID;
		profile._name = context.getResources().getString(R.string.default_profile_name);
		profile._icon = PROFILE_ICON_DEFAULT;
		profile._checked = false;
		profile._porder = 0;
		profile._duration = 0;
		profile._afterDurationDo = Profile.AFTERDURATIONDO_NOTHING;
    	profile._volumeRingerMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "1")); // ring
    	profile._volumeZenMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, "1")); // all
    	profile._volumeRingtone = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, getVolumeLevelString(71, maximumValueRing)+"|0|0");
    	profile._volumeNotification = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, getVolumeLevelString(86, maximumValueNotification)+"|0|0");
    	profile._volumeMedia = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, getVolumeLevelString(80, maximumValueMusic)+"|0|0");
    	profile._volumeAlarm = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ALARM, getVolumeLevelString(100, maximumValueAlarm)+"|0|0");
    	profile._volumeSystem = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, getVolumeLevelString(70, maximumValueSystem)+"|0|0");
    	profile._volumeVoice = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_VOICE, getVolumeLevelString(70, maximumValueVoicecall)+"|0|0");
    	profile._soundRingtoneChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
    	profile._soundRingtone = preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, Settings.System.DEFAULT_RINGTONE_URI.toString());
    	profile._soundNotificationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
    	profile._soundNotification = preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    	profile._soundAlarmChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
    	profile._soundAlarm = preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM, Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
    	profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "2")); // OFF
    	profile._deviceWiFi = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WIFI, "2")); // OFF
    	profile._deviceBluetooth = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, "2")); //OFF
    	profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "2")); // 30 seconds
    	profile._deviceBrightness = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|0|1|0");  // automatic on
    	profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
   		profile._deviceWallpaper = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, "-|0");
    	profile._deviceMobileData = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, "1")); //ON
    	profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, "0"));
    	profile._deviceGPS = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_GPS, "2")); //OFF
    	profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, "0"));
   		profile._deviceRunApplicationPackageName = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
    	profile._deviceAutosync = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, "1")); // ON
    	profile._deviceAutoRotate = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, "1")); // ON
    	profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, "0"));
    	profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
    	profile._deviceNFC = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NFC, "0"));
    	profile._deviceKeyguard = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, "0"));
    	
    	return profile;
	}
	
	static public Profile getMappedProfile(Profile profile, Context context)
	{
		if (profile != null)
		{
			Profile defaultProfile = getDefaultProfile(context);
			
			Profile mappedProfile = new Profile(
					           profile._id,
							   profile._name, 
							   profile._icon, 
							   profile._checked, 
							   profile._porder,
							   profile._volumeRingerMode,
							   profile._volumeRingtone,
							   profile._volumeNotification,
							   profile._volumeMedia,
							   profile._volumeAlarm,
							   profile._volumeSystem,
							   profile._volumeVoice,
							   profile._soundRingtoneChange,
							   profile._soundRingtone,
							   profile._soundNotificationChange,
							   profile._soundNotification,
							   profile._soundAlarmChange,
							   profile._soundAlarm,
							   profile._deviceAirplaneMode,
							   profile._deviceWiFi,
							   profile._deviceBluetooth,
							   profile._deviceScreenTimeout,
							   profile._deviceBrightness,
							   profile._deviceWallpaperChange,
							   profile._deviceWallpaper,
							   profile._deviceMobileData,
							   profile._deviceMobileDataPrefs,
							   profile._deviceGPS,
							   profile._deviceRunApplicationChange,
							   profile._deviceRunApplicationPackageName,
							   profile._deviceAutosync,
							   profile._showInActivator,
							   profile._deviceAutoRotate,
							   profile._deviceLocationServicePrefs,
							   profile._volumeSpeakerPhone,
							   profile._deviceNFC,
							   profile._duration,
							   profile._afterDurationDo,
							   profile._volumeZenMode,
							   profile._deviceKeyguard);
		
			if (profile._volumeRingerMode == 99)
				mappedProfile._volumeRingerMode = defaultProfile._volumeRingerMode;
			if (profile._volumeZenMode == 99)
				mappedProfile._volumeZenMode = defaultProfile._volumeZenMode;
			if (profile.getVolumeRingtoneDefaultProfile())
				mappedProfile._volumeRingtone = defaultProfile._volumeRingtone;
			if (profile.getVolumeNotificationDefaultProfile())
				mappedProfile._volumeNotification = defaultProfile._volumeNotification;
			if (profile.getVolumeAlarmDefaultProfile())
				mappedProfile._volumeAlarm = defaultProfile._volumeAlarm;
			if (profile.getVolumeMediaDefaultProfile())
				mappedProfile._volumeMedia = defaultProfile._volumeMedia;
			if (profile.getVolumeSystemDefaultProfile())
				mappedProfile._volumeSystem = defaultProfile._volumeSystem;
			if (profile.getVolumeVoiceDefaultProfile())
				mappedProfile._volumeVoice = defaultProfile._volumeVoice;
			if (profile._soundRingtoneChange == 99)
			{
				mappedProfile._soundRingtoneChange = defaultProfile._soundRingtoneChange;
				mappedProfile._soundRingtone = defaultProfile._soundRingtone;
			}
			if (profile._soundNotificationChange == 99)
			{
				mappedProfile._soundNotificationChange = defaultProfile._soundNotificationChange;
				mappedProfile._soundNotification = defaultProfile._soundNotification;
			}
			if (profile._soundAlarmChange == 99)
			{
				mappedProfile._soundAlarmChange = defaultProfile._soundAlarmChange;
				mappedProfile._soundAlarm = defaultProfile._soundAlarm;
			}
			if (profile._deviceAirplaneMode == 99)
				mappedProfile._deviceAirplaneMode = defaultProfile._deviceAirplaneMode;
			if (profile._deviceAutosync == 99)
				mappedProfile._deviceAutosync = defaultProfile._deviceAutosync;
			if (profile._deviceMobileData == 99)
				mappedProfile._deviceMobileData = defaultProfile._deviceMobileData;
			if (profile._deviceMobileDataPrefs == 99)
				mappedProfile._deviceMobileDataPrefs = defaultProfile._deviceMobileDataPrefs;
			if (profile._deviceWiFi == 99)
				mappedProfile._deviceWiFi = defaultProfile._deviceWiFi;
			if (profile._deviceBluetooth == 99)
				mappedProfile._deviceBluetooth = defaultProfile._deviceBluetooth;
			if (profile._deviceGPS == 99)
				mappedProfile._deviceGPS = defaultProfile._deviceGPS;
			if (profile._deviceLocationServicePrefs == 99)
				mappedProfile._deviceLocationServicePrefs = defaultProfile._deviceLocationServicePrefs;
			if (profile._deviceScreenTimeout == 99)
				mappedProfile._deviceScreenTimeout = defaultProfile._deviceScreenTimeout;
			if (profile.getDeviceBrightnessDefaultProfile())
				mappedProfile._deviceBrightness = defaultProfile._deviceBrightness;
			if (profile._deviceAutoRotate == 99)
				mappedProfile._deviceAutoRotate = defaultProfile._deviceAutoRotate;
			if (profile._deviceRunApplicationChange == 99)
			{
				mappedProfile._deviceRunApplicationChange = defaultProfile._deviceRunApplicationChange;
				mappedProfile._deviceRunApplicationPackageName = defaultProfile._deviceRunApplicationPackageName;
			}
			if (profile._deviceWallpaperChange == 99)
			{
				mappedProfile._deviceWallpaperChange = defaultProfile._deviceWallpaperChange;
				mappedProfile._deviceWallpaper = defaultProfile._deviceWallpaper;
			}
			if (profile._volumeSpeakerPhone == 99)
				mappedProfile._volumeSpeakerPhone = defaultProfile._volumeSpeakerPhone;
			if (profile._deviceNFC == 99)
				mappedProfile._deviceNFC = defaultProfile._deviceNFC;
			if (profile._deviceKeyguard == 99)
				mappedProfile._deviceKeyguard = defaultProfile._deviceKeyguard;
			
			mappedProfile._iconBitmap = profile._iconBitmap;
			mappedProfile._preferencesIndicator = profile._preferencesIndicator;
			
			return mappedProfile;
		}
		else 
			return profile;
	}
	
	static public boolean getGlobalEventsRuning(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_GLOBAL_EVENTS_RUN_STOP, true);
	}
	
	static public void setGlobalEventsRuning(Context context, boolean globalEventsRuning)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(PREF_GLOBAL_EVENTS_RUN_STOP, globalEventsRuning);
		editor.commit();
	}
	
	static public boolean getApplicationStarted(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_APPLICATION_STARTED, false);
	}

	static public void setApplicationStarted(Context context, boolean globalEventsStarted)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(PREF_APPLICATION_STARTED, globalEventsStarted);
		editor.commit();
	}

	static public boolean getEventsBlocked(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_EVENTS_BLOCKED, false);
	}

	static public void setEventsBlocked(Context context, boolean eventsBlocked)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(PREF_EVENTS_BLOCKED, eventsBlocked);
		editor.commit();
	}

	static public boolean getForceRunEventRunning(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_FORCE_RUN_EVENT_RUNNING, false);
	}

	static public void setForceRunEventRunning(Context context, boolean forceRunEventRunning)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(PREF_FORCE_RUN_EVENT_RUNNING, forceRunEventRunning);
		editor.commit();
	}
	
	static public long getActivatedProfileForDuration(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, 0);
	}

	static public void setActivatedProfileForDuration(Context context, long profileId)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, profileId);
		editor.commit();
	}

	static public boolean getForceOneBluetoothScan(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_FORCE_ONE_BLUETOOTH_SCAN, false);
	}

	static public void setForceOneBluetoothScan(Context context, boolean forceScan)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(PREF_FORCE_ONE_BLUETOOTH_SCAN, forceScan);
		editor.commit();
	}

	static public boolean getForceOneWifiScan(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(PREF_FORCE_ONE_WIFI_SCAN, false);
	}

	static public void setForceOneWifiScan(Context context, boolean forceScan)
	{
		SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(PREF_FORCE_ONE_WIFI_SCAN, forceScan);
		editor.commit();
	}

	/*
	static public boolean getRadioChangeState(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.RADIO_CHANGE_PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		return preferences.getBoolean(GlobalData.PREF_RADIO_CHANGE_STATE, false);
	}

	static public void setRadioChangeState(Context context, boolean state)
	{
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.RADIO_CHANGE_PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		Editor editor = preferences.edit();
		editor.putBoolean(GlobalData.PREF_RADIO_CHANGE_STATE, state);
		editor.commit();
      	GlobalData.logE("@@@ GlobalData.setRadioChangeState","state="+state);
	}
	
	static public void waitForRadioChangeState(Context context)
	{
    	for (int i = 0; i < 5 * 60; i++) // 60 seconds for wifi scan (Android 5.0 bug, normally required 5 seconds :-/) 
    	{
        	if (!getRadioChangeState(context))
        		break;
	        try {
	        	Thread.sleep(200);
		    } catch (InterruptedException e) {
		        System.out.println(e);
		    }
    	}
	}
	*/
	// ----- Hardware check -------------------------------------
	
	static int hardwareCheck(String preferenceKey, Context context)
	{
		int featurePresented = HARDWARE_CHECK_NOT_ALLOWED;

		if (preferenceKey.equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE))
		{	
			if (android.os.Build.VERSION.SDK_INT >= 17)
			{
				if (PhoneProfilesHelper.isPPHelperInstalled(context, 7))
				{
					// je nainstalovany PhonProfilesHelper
					featurePresented = HARDWARE_CHECK_ALLOWED;
				}
				else
				{
					if (isRooted(false))
					{
						// zariadenie je rootnute
						if (settingsBinaryExists())
							featurePresented = HARDWARE_CHECK_ALLOWED;
						else
						{
							// "settings" binnary not exists 
							if (PhoneProfilesHelper.PPHelperVersion == -1)
								featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
							else
								featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
						}
					}
				}
			}
			else
				featurePresented = HARDWARE_CHECK_ALLOWED;
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_WIFI))
		{	
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
				// device ma Wifi
				featurePresented = HARDWARE_CHECK_ALLOWED;
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_BLUETOOTH))
		{	
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
				// device ma bluetooth
				featurePresented = HARDWARE_CHECK_ALLOWED;
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_MOBILE_DATA))
		{	
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
			{
				if (android.os.Build.VERSION.SDK_INT >= 21)
				{
					if (PhoneProfilesHelper.isPPHelperInstalled(context, 22))
					{
						// je nainstalovany PhonProfilesHelper
						featurePresented = HARDWARE_CHECK_ALLOWED;
				    }
					else
					{
						if (isRooted(false))
						{
							if (PhoneProfilesHelper.PPHelperVersion == -1)
								featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
							else
								featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
						}
					}
					/*if (isRooted(false) && settingsBinaryExists())
					{
						featurePresented = HARDWARE_CHECK_ALLOWED;
					}*/
				}
				else
				{
					if (canSetMobileData(context))
						featurePresented = HARDWARE_CHECK_ALLOWED;
				}
			}
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS))
		{
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
			{
				featurePresented = HARDWARE_CHECK_ALLOWED;
			}
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_GPS))
		{	
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
			{
				// device ma gps
				if (canExploitGPS(context))
				{
					featurePresented = HARDWARE_CHECK_ALLOWED;
					//Log.e("GlobalData.hardwareCheck - GPS","exploit");
			    }
				else
				if (android.os.Build.VERSION.SDK_INT < 17)
				{
					if (PhoneProfilesHelper.isPPHelperInstalled(context, 7))
					{
						// je nainstalovany PhonProfilesHelper
						featurePresented = HARDWARE_CHECK_ALLOWED;
						//Log.e("GlobalData.hardwareCheck - GPS","system app.");
				    }
					else
					{
						if (isRooted(false))
						{
							if (PhoneProfilesHelper.PPHelperVersion == -1)
								featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
							else
								featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
						}
					}
				}
				else
				if (isRooted(false))
				{
					// zariadenie je rootnute
					if (settingsBinaryExists())
						featurePresented = HARDWARE_CHECK_ALLOWED;
					else
					{
						// "settings" binnary not exists 
						if (PhoneProfilesHelper.PPHelperVersion == -1)
							featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
						else
							featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
					}
				}
			}
		}
		else
		if (preferenceKey.equals(PREF_PROFILE_DEVICE_NFC))
		{
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
			{
				logE("GlobalData.hardwareCheck","NFC=presented");
				
				// device ma nfc
				if (PhoneProfilesHelper.isPPHelperInstalled(context, 7))
				{
					// je nainstalovany PhonProfilesHelper
					featurePresented = HARDWARE_CHECK_ALLOWED;
			    }
				else
				{
					if (isRooted(false))
					{
						if (PhoneProfilesHelper.PPHelperVersion == -1)
							featurePresented = HARDWARE_CHECK_INSTALL_PPHELPER;
						else
							featurePresented = HARDWARE_CHECK_UPGRADE_PPHELPER;
					}
				}
			}
			else
			{
				logE("GlobalData.hardwareCheck","NFC=not presented");
			}
		}
		else
			featurePresented = HARDWARE_CHECK_ALLOWED;
		
		return featurePresented;
	}
	
	static boolean canExploitGPS(Context context)
	{
		// test expoiting power manager widget
	    PackageManager pacman = context.getPackageManager();
	    PackageInfo pacInfo = null;
	    try {
	        pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);

		    if(pacInfo != null){
		        for(ActivityInfo actInfo : pacInfo.receivers){
		            //test if recevier is exported. if so, we can toggle GPS.
		            if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
						return true;
		            }
		        }
		    }				
	    } catch (NameNotFoundException e) {
	        return false; //package not found
	    }   
	    return false;
	}
	
	static boolean canSetMobileData(Context context)
	{
		final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		try {
			final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
			final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
			getMobileDataEnabledMethod.setAccessible(true);
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	static private boolean rootChecking = false;
	static private boolean rootChecked = false;
	static private boolean rooted = false;
	static private boolean grantChecking = false;
	static private boolean grantChecked = false;
	static private boolean rootGranted = false;
	static private boolean settingsBinaryChecking = false;
	static private boolean settingsBinaryChecked = false;
	static private boolean settingsBinaryExists = false;
	static private boolean isSELinuxEnforcingChecked = false;
	static private boolean isSELinuxEnforcing = false;
	//static private String suVersion = null;
	//static private boolean suVersionChecked = false;
	
	static boolean isRooted(boolean onlyCheckFlags)
	{
		RootShell.debugMode = rootToolsDebug;

		if ((!rootChecked) && (!rootChecking))
		{
			settingsBinaryExists = false;
			settingsBinaryChecked = false;
			isSELinuxEnforcingChecked = false;
			isSELinuxEnforcing = false;
			//suVersionChecked = false;
			//suVersion = null;
			if (!onlyCheckFlags)
			{
				rootChecking = true;
				if (RootTools.isRootAvailable())
				{
					// zariadenie je rootnute
					rootChecked = true;
					rooted = true;
				}
				else
				{
					rootChecked = true;
					rooted = false;
				}
				/*try {
					RootTools.closeAllShells();
				} catch (IOException e) {
					e.printStackTrace();
				}*/
				rootChecking = false;
			}
			else
			{
				rootChecked = false;
				rooted = false;
			}
		}
		//if (rooted)
		//	getSUVersion();
		return rooted;
	}
	
	static boolean grantRoot(boolean force)
	{
		RootShell.debugMode = rootToolsDebug;
		
		GlobalData.logE("GlobalData.grantRoot", "grantChecked="+grantChecked);
		GlobalData.logE("GlobalData.grantRoot", "force="+force);
		

		if (((!grantChecked) || force) && (!grantChecking))
		{
			settingsBinaryExists = false;
			settingsBinaryChecked = false;
			isSELinuxEnforcingChecked = false;
			isSELinuxEnforcing = false;
			//suVersionChecked = false;
			//suVersion = null;
			GlobalData.logE("GlobalData.grantRoot", "start isAccessGiven");
			grantChecking = true;
			if (RootTools.isAccessGiven())
			{
				// root grantnuty
				GlobalData.logE("GlobalData.grantRoot", "root granted");
				rootChecked = true;
				rooted = true;
				grantChecked = true;
				rootGranted = true;
			}
			else
			{
				// grant odmietnuty
				GlobalData.logE("GlobalData.grantRoot", "root NOT granted");
				rootChecked = true;
				rooted = false;
				grantChecked = true;
				rootGranted = false;
			}
			/*try {
				RootTools.closeAllShells();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
			grantChecking = false;
		}
		//if (rooted)
		//	getSUVersion();
		return rootGranted;
	}
	
	static boolean settingsBinaryExists()
	{
		RootShell.debugMode = rootToolsDebug;
		
		if ((!settingsBinaryChecked) && (!settingsBinaryChecking))
		{
			settingsBinaryChecking = true;
			List<String> settingsPaths = RootTools.findBinary("settings"); 
			settingsBinaryExists = settingsPaths.size() > 0;
			/*try {
				RootTools.closeAllShells();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
			settingsBinaryChecking = false;
			settingsBinaryChecked = true;
		}
		GlobalData.logE("GlobalData.settingsBinaryExists", "settingsBinaryExists="+settingsBinaryExists);
		return settingsBinaryExists;
	}
	
    /**
     * Detect if SELinux is set to enforcing, caches result
     * 
     * @return true if SELinux set to enforcing, or false in the case of
     *         permissive or not present
     */
    public static boolean isSELinuxEnforcing()
    {
    	RootShell.debugMode = rootToolsDebug;
    	
        if (!isSELinuxEnforcingChecked)
        {
            boolean enforcing = false;

            // First known firmware with SELinux built-in was a 4.2 (17)
            // leak
            if (android.os.Build.VERSION.SDK_INT >= 17)
            {
                // Detect enforcing through sysfs, not always present
                File f = new File("/sys/fs/selinux/enforce");
                if (f.exists())
                {
                    try {
                        InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                        try {
                            enforcing = (is.read() == '1');
                        } finally {
                            is.close();
                        }
                    } catch (Exception e) {
                    }
                }

                /*
                // 4.4+ builds are enforcing by default, take the gamble
                if (!enforcing)
                {
                    enforcing = (android.os.Build.VERSION.SDK_INT >= 19);
                }
                */
            }

            isSELinuxEnforcing = enforcing;
            isSELinuxEnforcingChecked = true; 
            
        }
        
		GlobalData.logE("GlobalData.isSELinuxEnforcing", "isSELinuxEnforcing="+isSELinuxEnforcing);
        
        return isSELinuxEnforcing;
    }
    
    /*
    public static String getSELinuxEnforceCommand(String command, Shell.ShellContext context)
    {
    	if ((suVersion != null) && suVersion.contains("SUPERSU"))
    		return "su --context " + context.getValue() + " -c \"" + command + "\"  < /dev/null";
    	else
    		return command;
    }

    public static String getSUVersion()
    {
    	if (!suVersionChecked)
    	{
	    	Command command = new Command(0, false, "su -v")
	    	{
	    		@Override
	    		public void commandOutput(int id, String line) {
	    			Log.e("GlobalData.getSUVersion","version="+line);
	    			suVersion = line;
	    			
	    			super.commandOutput(id, line);
	    		}
	    	}
	    	;
			try {
				RootTools.getShell(false).add(command);
				commandWait(command);
				//RootTools.closeAllShells();
    			suVersionChecked = true;
			} catch (Exception e) {
				Log.e("GlobalData.getSUVersion", "Error on run su");
			}
    	}
    	return suVersion;
    }
    
	private static void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; // 6350 msec (3200 * 2 - 50)

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("GlobaData.commandWait", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }
    */    
    
	//------------------------------------------------------------
	
}
