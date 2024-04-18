package sk.henrichg.phoneprofilesplus;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class DatabaseHandlerProfiles {

    // Adding new profile
    static void addProfile(DatabaseHandler instance, Profile profile, boolean merged) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                int porder = getMaxProfileOrder(instance) + 1;

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_NAME, profile._name); // Profile Name
                values.put(DatabaseHandler.KEY_ICON, profile._icon); // Icon
                values.put(DatabaseHandler.KEY_CHECKED, (profile._checked) ? 1 : 0); // Checked
                values.put(DatabaseHandler.KEY_PORDER, porder); // POrder
                //values.put(DatabaseHandler.KEY_PORDER, profile._porder); // POrder
                values.put(DatabaseHandler.KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
                values.put(DatabaseHandler.KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
                values.put(DatabaseHandler.KEY_VOLUME_RINGTONE, profile._volumeRingtone);
                values.put(DatabaseHandler.KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
                values.put(DatabaseHandler.KEY_VOLUME_MEDIA, profile._volumeMedia);
                values.put(DatabaseHandler.KEY_VOLUME_ALARM, profile._volumeAlarm);
                values.put(DatabaseHandler.KEY_VOLUME_SYSTEM, profile._volumeSystem);
                values.put(DatabaseHandler.KEY_VOLUME_VOICE, profile._volumeVoice);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE, profile._soundRingtone);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION, profile._soundNotification);
                values.put(DatabaseHandler.KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
                values.put(DatabaseHandler.KEY_SOUND_ALARM, profile._soundAlarm);
                values.put(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
                values.put(DatabaseHandler.KEY_DEVICE_WIFI, profile._deviceWiFi);
                values.put(DatabaseHandler.KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
                values.put(DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
                values.put(DatabaseHandler.KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
                values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
                values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
                values.put(DatabaseHandler.KEY_DEVICE_GPS, profile._deviceGPS);
                values.put(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
                values.put(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
                values.put(DatabaseHandler.KEY_DEVICE_AUTOSYNC, profile._deviceAutoSync);
                values.put(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
                values.put(DatabaseHandler.KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
                values.put(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
                values.put(DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
                values.put(DatabaseHandler.KEY_DEVICE_NFC, profile._deviceNFC);
                values.put(DatabaseHandler.KEY_DURATION, profile._duration);
                values.put(DatabaseHandler.KEY_AFTER_DURATION_DO, profile._afterDurationDo);
                values.put(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
                values.put(DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
                values.put(DatabaseHandler.KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
                values.put(DatabaseHandler.KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
                values.put(DatabaseHandler.KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
                values.put(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
                values.put(DatabaseHandler.KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
                values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
                values.put(DatabaseHandler.KEY_NOTIFICATION_LED, profile._notificationLed);
                values.put(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
                values.put(DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS, profile._vibrateNotifications);
                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
                values.put(DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
                values.put(DatabaseHandler.KEY_LOCK_DEVICE, profile._lockDevice);
                values.put(DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING, profile._applicationEnableWifiScanning);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING, profile._applicationEnableBluetoothScanning);
                values.put(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS, profile._deviceWiFiAPPrefs);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING, profile._applicationEnableLocationScanning);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, profile._applicationEnableMobileCellScanning);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING, profile._applicationEnableOrientationScanning);
                values.put(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS, profile._headsUpNotifications);
                values.put(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, profile._deviceForceStopApplicationChange);
                values.put(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
                values.put(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT, profile._activationByUserCount);
                values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS, profile._deviceNetworkTypePrefs);
                values.put(DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS, profile._deviceCloseAllApplications);
                values.put(DatabaseHandler.KEY_SCREEN_DARK_MODE, profile._screenDarkMode);
                values.put(DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING, profile._dtmfToneWhenDialing);
                values.put(DatabaseHandler.KEY_SOUND_ON_TOUCH, profile._soundOnTouch);
                values.put(DatabaseHandler.KEY_VOLUME_DTMF, profile._volumeDTMF);
                values.put(DatabaseHandler.KEY_VOLUME_ACCESSIBILITY, profile._volumeAccessibility);
                values.put(DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO, profile._volumeBluetoothSCO);
                values.put(DatabaseHandler.KEY_AFTER_DURATION_PROFILE, profile._afterDurationProfile);
                values.put(DatabaseHandler.KEY_ALWAYS_ON_DISPLAY, profile._alwaysOnDisplay);
                values.put(DatabaseHandler.KEY_SCREEN_ON_PERMANENT, profile._screenOnPermanent);
                values.put(DatabaseHandler.KEY_VOLUME_MUTE_SOUND, (profile._volumeMuteSound) ? 1 : 0);
                values.put(DatabaseHandler.KEY_DEVICE_LOCATION_MODE, profile._deviceLocationMode);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING, profile._applicationEnableNotificationScanning);
                values.put(DatabaseHandler.KEY_GENERATE_NOTIFICATION, profile._generateNotification);
                values.put(DatabaseHandler.KEY_CAMERA_FLASH, profile._cameraFlash);
                values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1, profile._deviceNetworkTypeSIM1);
                values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2, profile._deviceNetworkTypeSIM2);
                //values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1, profile._deviceMobileDataSIM1);
                //values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2, profile._deviceMobileDataSIM2);
                values.put(DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS, profile._deviceDefaultSIMCards);
                values.put(DatabaseHandler.KEY_DEVICE_ONOFF_SIM1, profile._deviceOnOffSIM1);
                values.put(DatabaseHandler.KEY_DEVICE_ONOFF_SIM2, profile._deviceOnOffSIM2);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1, profile._soundRingtoneChangeSIM1);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_SIM1, profile._soundRingtoneSIM1);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2, profile._soundRingtoneChangeSIM2);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_SIM2, profile._soundRingtoneSIM2);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1, profile._soundNotificationChangeSIM1);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1, profile._soundNotificationSIM1);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2, profile._soundNotificationChangeSIM2);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2, profile._soundNotificationSIM2);
                values.put(DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, profile._soundSameRingtoneForBothSIMCards);
                values.put(DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER, profile._deviceLiveWallpaper);
                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER, profile._deviceWallpaperFolder);
                values.put(DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, profile._applicationDisableGloabalEventsRun);
                values.put(DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS, profile._deviceVPNSettingsPrefs);
                values.put(DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE, profile._endOfActivationType);
                values.put(DatabaseHandler.KEY_END_OF_ACTIVATION_TIME, profile._endOfActivationTime);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING, profile._applicationEnablePeriodicScanning);
                values.put(DatabaseHandler.KEY_DEVICE_VPN, profile._deviceVPN);
                values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING, profile._vibrationIntensityRinging);
                values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS, profile._vibrationIntensityNotifications);
                values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION, profile._vibrationIntensityTouchInteraction);
                values.put(DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY, (profile._volumeMediaChangeDuringPlay) ? 1 : 0);
                values.put(DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL, profile._applicationWifiScanInterval);
                values.put(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL, profile._applicationBluetoothScanInterval);
                values.put(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION, profile._applicationBluetoothLEScanDuration);
                values.put(DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL, profile._applicationLocationScanInterval);
                values.put(DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL, profile._applicationOrientationScanInterval);
                values.put(DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL, profile._applicationPeriodicScanInterval);
                values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS, profile._phoneCallsContacts);
                values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS, profile._phoneCallsContactGroups);
                values.put(DatabaseHandler.KEY_PHONE_CALLS_BLOCK_CALLS, (profile._phoneCallsBlockCalls) ? 1 : 0);
                values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_LIST_TYPE, profile._phoneCallsContactListType);

                // Insert Row
                if (!merged) {
                    profile._id = db.insert(DatabaseHandler.TABLE_PROFILES, null, values);
                    profile._porder = porder;
                } else {
                    values.put(DatabaseHandler.KEY_ID, profile._id);
                    db.insert(DatabaseHandler.TABLE_MERGED_PROFILE, null, values);
                }
                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting single profile
    static Profile getProfile(DatabaseHandler instance, long profile_id, boolean merged) {
        instance.importExportLock.lock();
        try {

            Profile profile = null;

            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                String tableName = DatabaseHandler.TABLE_PROFILES;
                if (merged)
                    tableName = DatabaseHandler.TABLE_MERGED_PROFILE;
                Cursor cursor = db.query(tableName,
                        new String[]{DatabaseHandler.KEY_ID,
                                DatabaseHandler.KEY_NAME,
                                DatabaseHandler.KEY_ICON,
                                DatabaseHandler.KEY_CHECKED,
                                DatabaseHandler.KEY_PORDER,
                                DatabaseHandler.KEY_VOLUME_RINGER_MODE,
                                DatabaseHandler.KEY_VOLUME_RINGTONE,
                                DatabaseHandler.KEY_VOLUME_NOTIFICATION,
                                DatabaseHandler.KEY_VOLUME_MEDIA,
                                DatabaseHandler.KEY_VOLUME_ALARM,
                                DatabaseHandler.KEY_VOLUME_SYSTEM,
                                DatabaseHandler.KEY_VOLUME_VOICE,
                                DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE,
                                DatabaseHandler.KEY_SOUND_RINGTONE,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION,
                                DatabaseHandler.KEY_SOUND_ALARM_CHANGE,
                                DatabaseHandler.KEY_SOUND_ALARM,
                                DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE,
                                DatabaseHandler.KEY_DEVICE_WIFI,
                                DatabaseHandler.KEY_DEVICE_BLUETOOTH,
                                DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT,
                                DatabaseHandler.KEY_DEVICE_BRIGHTNESS,
                                DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE,
                                DatabaseHandler.KEY_DEVICE_WALLPAPER,
                                DatabaseHandler.KEY_DEVICE_MOBILE_DATA,
                                DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS,
                                DatabaseHandler.KEY_DEVICE_GPS,
                                DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE,
                                DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                                DatabaseHandler.KEY_DEVICE_AUTOSYNC,
                                DatabaseHandler.KEY_SHOW_IN_ACTIVATOR,
                                DatabaseHandler.KEY_DEVICE_AUTOROTATE,
                                DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS,
                                DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE,
                                DatabaseHandler.KEY_DEVICE_NFC,
                                DatabaseHandler.KEY_DURATION,
                                DatabaseHandler.KEY_AFTER_DURATION_DO,
                                DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND,
                                DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE,
                                DatabaseHandler.KEY_VOLUME_ZEN_MODE,
                                DatabaseHandler.KEY_DEVICE_KEYGUARD,
                                DatabaseHandler.KEY_VIBRATE_ON_TOUCH,
                                DatabaseHandler.KEY_DEVICE_WIFI_AP,
                                DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE,
                                DatabaseHandler.KEY_ASK_FOR_DURATION,
                                DatabaseHandler.KEY_DEVICE_NETWORK_TYPE,
                                DatabaseHandler.KEY_NOTIFICATION_LED,
                                DatabaseHandler.KEY_VIBRATE_WHEN_RINGING,
                                DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS,
                                DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR,
                                DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON,
                                DatabaseHandler.KEY_LOCK_DEVICE,
                                DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING,
                                DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING,
                                DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS,
                                DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE,
                                DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME,
                                DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT,
                                DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS,
                                DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS,
                                DatabaseHandler.KEY_SCREEN_DARK_MODE,
                                DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING,
                                DatabaseHandler.KEY_SOUND_ON_TOUCH,
                                DatabaseHandler.KEY_VOLUME_DTMF,
                                DatabaseHandler.KEY_VOLUME_ACCESSIBILITY,
                                DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO,
                                DatabaseHandler.KEY_AFTER_DURATION_PROFILE,
                                DatabaseHandler.KEY_ALWAYS_ON_DISPLAY,
                                DatabaseHandler.KEY_SCREEN_ON_PERMANENT,
                                DatabaseHandler.KEY_VOLUME_MUTE_SOUND,
                                DatabaseHandler.KEY_DEVICE_LOCATION_MODE,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING,
                                DatabaseHandler.KEY_GENERATE_NOTIFICATION,
                                DatabaseHandler.KEY_CAMERA_FLASH,
                                DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1,
                                DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2,
                                //DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1,
                                //DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2,
                                DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS,
                                DatabaseHandler.KEY_DEVICE_ONOFF_SIM1,
                                DatabaseHandler.KEY_DEVICE_ONOFF_SIM2,
                                DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1,
                                DatabaseHandler.KEY_SOUND_RINGTONE_SIM1,
                                DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2,
                                DatabaseHandler.KEY_SOUND_RINGTONE_SIM2,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2,
                                DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS,
                                DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER,
                                DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER,
                                DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN,
                                DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS,
                                DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE,
                                DatabaseHandler.KEY_END_OF_ACTIVATION_TIME,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING,
                                DatabaseHandler.KEY_DEVICE_VPN,
                                DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING,
                                DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS,
                                DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION,
                                DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY,
                                DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL,
                                DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL,
                                DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION,
                                DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL,
                                DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL,
                                DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL,
                                DatabaseHandler.KEY_PHONE_CALLS_CONTACTS,
                                DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS,
                                DatabaseHandler.KEY_PHONE_CALLS_BLOCK_CALLS,
                                DatabaseHandler.KEY_PHONE_CALLS_CONTACT_LIST_TYPE
                        },
                        DatabaseHandler.KEY_ID + "=?",
                        new String[]{String.valueOf(profile_id)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        profile = new Profile(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_CHECKED)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PORDER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGER_MODE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGTONE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_NOTIFICATION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MEDIA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ALARM)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SYSTEM)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_VOICE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BLUETOOTH)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BRIGHTNESS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_GPS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AUTOSYNC)),
                                (cursor.getColumnIndex(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR) != -1) && (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR)) == 1),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AUTOROTATE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NFC)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AFTER_DURATION_DO)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ZEN_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_KEYGUARD)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_ON_TOUCH)),
                                (cursor.getColumnIndex(DatabaseHandler.KEY_DEVICE_WIFI_AP) != -1) ? cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP)) : 0,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ASK_FOR_DURATION)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NOTIFICATION_LED)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_LOCK_DEVICE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SCREEN_DARK_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ON_TOUCH)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_DTMF)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ACCESSIBILITY)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AFTER_DURATION_PROFILE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ALWAYS_ON_DISPLAY)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SCREEN_ON_PERMANENT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MUTE_SOUND)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_GENERATE_NOTIFICATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_CAMERA_FLASH)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2)),
                                //cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1)),
                                //cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_ONOFF_SIM1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_ONOFF_SIM2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_SIM1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_SIM2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_END_OF_ACTIVATION_TIME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_VPN)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_LIST_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_BLOCK_CALLS)) == 1
                        );
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return profile;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting All Profiles
    static List<Profile> getAllProfiles(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {

            List<Profile> profileList = new ArrayList<>();

            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_NAME + "," +
                        DatabaseHandler.KEY_ICON + "," +
                        DatabaseHandler.KEY_CHECKED + "," +
                        DatabaseHandler.KEY_PORDER + "," +
                        DatabaseHandler.KEY_VOLUME_RINGER_MODE + "," +
                        DatabaseHandler.KEY_VOLUME_RINGTONE + "," +
                        DatabaseHandler.KEY_VOLUME_NOTIFICATION + "," +
                        DatabaseHandler.KEY_VOLUME_MEDIA + "," +
                        DatabaseHandler.KEY_VOLUME_ALARM + "," +
                        DatabaseHandler.KEY_VOLUME_SYSTEM + "," +
                        DatabaseHandler.KEY_VOLUME_VOICE + "," +
                        DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE + "," +
                        DatabaseHandler.KEY_SOUND_RINGTONE + "," +
                        DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE + "," +
                        DatabaseHandler.KEY_SOUND_NOTIFICATION + "," +
                        DatabaseHandler.KEY_SOUND_ALARM_CHANGE + "," +
                        DatabaseHandler.KEY_SOUND_ALARM + "," +
                        DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE + "," +
                        DatabaseHandler.KEY_DEVICE_WIFI + "," +
                        DatabaseHandler.KEY_DEVICE_BLUETOOTH + "," +
                        DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT + "," +
                        DatabaseHandler.KEY_DEVICE_BRIGHTNESS + "," +
                        DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE + "," +
                        DatabaseHandler.KEY_DEVICE_WALLPAPER + "," +
                        DatabaseHandler.KEY_DEVICE_MOBILE_DATA + "," +
                        DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS + "," +
                        DatabaseHandler.KEY_DEVICE_GPS + "," +
                        DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE + "," +
                        DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + "," +
                        DatabaseHandler.KEY_DEVICE_AUTOSYNC + "," +
                        DatabaseHandler.KEY_SHOW_IN_ACTIVATOR + "," +
                        DatabaseHandler.KEY_DEVICE_AUTOROTATE + "," +
                        DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS + "," +
                        DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE + "," +
                        DatabaseHandler.KEY_DEVICE_NFC + "," +
                        DatabaseHandler.KEY_DURATION + "," +
                        DatabaseHandler.KEY_AFTER_DURATION_DO + "," +
                        DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND + "," +
                        DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE + "," +
                        DatabaseHandler.KEY_VOLUME_ZEN_MODE + "," +
                        DatabaseHandler.KEY_DEVICE_KEYGUARD + "," +
                        DatabaseHandler.KEY_VIBRATE_ON_TOUCH + "," +
                        DatabaseHandler.KEY_DEVICE_WIFI_AP + "," +
                        DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE + "," +
                        DatabaseHandler.KEY_ASK_FOR_DURATION + "," +
                        DatabaseHandler.KEY_DEVICE_NETWORK_TYPE + "," +
                        DatabaseHandler.KEY_NOTIFICATION_LED + "," +
                        DatabaseHandler.KEY_VIBRATE_WHEN_RINGING + "," +
                        DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS + "," +
                        DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR + "," +
                        DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON + "," +
                        DatabaseHandler.KEY_LOCK_DEVICE + "," +
                        DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID + "," +
                        DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING + "," +
                        DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING + "," +
                        DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS + "," +
                        DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING + "," +
                        DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING + "," +
                        DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING + "," +
                        DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS + "," +
                        DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "," +
                        DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + "," +
                        DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + "," +
                        DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS + "," +
                        DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS + "," +
                        DatabaseHandler.KEY_SCREEN_DARK_MODE + "," +
                        DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING + "," +
                        DatabaseHandler.KEY_SOUND_ON_TOUCH + "," +
                        DatabaseHandler.KEY_VOLUME_DTMF + "," +
                        DatabaseHandler.KEY_VOLUME_ACCESSIBILITY + "," +
                        DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO + "," +
                        DatabaseHandler.KEY_AFTER_DURATION_PROFILE + "," +
                        DatabaseHandler.KEY_ALWAYS_ON_DISPLAY + "," +
                        DatabaseHandler.KEY_SCREEN_ON_PERMANENT + "," +
                        DatabaseHandler.KEY_VOLUME_MUTE_SOUND + "," +
                        DatabaseHandler.KEY_DEVICE_LOCATION_MODE + "," +
                        DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING + "," +
                        DatabaseHandler.KEY_GENERATE_NOTIFICATION + "," +
                        DatabaseHandler.KEY_CAMERA_FLASH + "," +
                        DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + "," +
                        DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + "," +
                        //DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + "," +
                        //DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + "," +
                        DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + "," +
                        DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + "," +
                        DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + "," +
                        DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + "," +
                        DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + "," +
                        DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + "," +
                        DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + "," +
                        DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "," +
                        DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + "," +
                        DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "," +
                        DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + "," +
                        DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "," +
                        DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER + "," +
                        DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER + "," +
                        DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN + "," +
                        DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS + "," +
                        DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE + "," +
                        DatabaseHandler.KEY_END_OF_ACTIVATION_TIME + "," +
                        DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING + "," +
                        DatabaseHandler.KEY_DEVICE_VPN + "," +
                        DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING + "," +
                        DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS + "," +
                        DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION + "," +
                        DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY + "," +
                        DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL + "," +
                        DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL + "," +
                        DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION + "," +
                        DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL + "," +
                        DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL + "," +
                        DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL + "," +
                        DatabaseHandler.KEY_PHONE_CALLS_CONTACTS + "," +
                        DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS + "," +
                        DatabaseHandler.KEY_PHONE_CALLS_CONTACT_LIST_TYPE + "," +
                        DatabaseHandler.KEY_PHONE_CALLS_BLOCK_CALLS +
                " FROM " + DatabaseHandler.TABLE_PROFILES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Profile profile = new Profile();
                        profile._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        profile._name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME));
                        profile._icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON));
                        profile._checked = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_CHECKED)) == 1;
                        profile._porder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PORDER));
                        profile._volumeRingerMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGER_MODE));
                        profile._volumeRingtone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGTONE));
                        profile._volumeNotification = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_NOTIFICATION));
                        profile._volumeMedia = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MEDIA));
                        profile._volumeAlarm = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ALARM));
                        profile._volumeSystem = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SYSTEM));
                        profile._volumeVoice = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_VOICE));
                        profile._soundRingtoneChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE));
                        profile._soundRingtone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE));
                        profile._soundNotificationChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE));
                        profile._soundNotification = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION));
                        profile._soundAlarmChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM_CHANGE));
                        profile._soundAlarm = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM));
                        profile._deviceAirplaneMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE));
                        profile._deviceWiFi = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI));
                        profile._deviceBluetooth = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BLUETOOTH));
                        profile._deviceScreenTimeout = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT));
                        profile._deviceBrightness = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BRIGHTNESS));
                        profile._deviceWallpaperChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE));
                        profile._deviceWallpaper = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER));
                        profile._deviceMobileData = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA));
                        profile._deviceMobileDataPrefs = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS));
                        profile._deviceGPS = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_GPS));
                        profile._deviceRunApplicationChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE));
                        profile._deviceRunApplicationPackageName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME));
                        profile._deviceAutoSync = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AUTOSYNC));
                        profile._showInActivator = (cursor.getColumnIndex(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR) != -1) && (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR)) == 1);
                        profile._deviceAutoRotate = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AUTOROTATE));
                        profile._deviceLocationServicePrefs = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS));
                        profile._volumeSpeakerPhone = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE));
                        profile._deviceNFC = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NFC));
                        profile._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION));
                        profile._afterDurationDo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AFTER_DURATION_DO));
                        profile._durationNotificationSound = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND));
                        profile._durationNotificationVibrate = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE)) == 1;
                        profile._volumeZenMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ZEN_MODE));
                        profile._deviceKeyguard = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_KEYGUARD));
                        profile._vibrationOnTouch = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_ON_TOUCH));
                        profile._deviceWiFiAP = (cursor.getColumnIndex(DatabaseHandler.KEY_DEVICE_WIFI_AP) != -1) ? cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP)) : 0;
                        profile._devicePowerSaveMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE));
                        profile._askForDuration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ASK_FOR_DURATION)) == 1;
                        profile._deviceNetworkType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE));
                        profile._notificationLed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NOTIFICATION_LED));
                        profile._vibrateWhenRinging = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING));
                        profile._vibrateNotifications = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS));
                        profile._deviceWallpaperFor = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR));
                        profile._hideStatusBarIcon = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON)) == 1;
                        profile._lockDevice = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_LOCK_DEVICE));
                        profile._deviceConnectToSSID = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID));
                        profile._applicationEnableWifiScanning = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING));
                        profile._applicationEnableBluetoothScanning = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING));
                        profile._deviceWiFiAPPrefs = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS));
                        profile._applicationEnableLocationScanning = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING));
                        profile._applicationEnableMobileCellScanning = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING));
                        profile._applicationEnableOrientationScanning = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING));
                        profile._headsUpNotifications = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS));
                        profile._deviceForceStopApplicationChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE));
                        profile._deviceForceStopApplicationPackageName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME));
                        profile._activationByUserCount = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT));
                        profile._deviceNetworkTypePrefs = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS));
                        profile._deviceCloseAllApplications = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS));
                        profile._screenDarkMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SCREEN_DARK_MODE));
                        profile._dtmfToneWhenDialing = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING));
                        profile._soundOnTouch = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ON_TOUCH));
                        profile._volumeDTMF = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_DTMF));
                        profile._volumeAccessibility = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ACCESSIBILITY));
                        profile._volumeBluetoothSCO = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO));
                        profile._afterDurationProfile = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AFTER_DURATION_PROFILE));
                        profile._alwaysOnDisplay = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ALWAYS_ON_DISPLAY));
                        profile._screenOnPermanent = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SCREEN_ON_PERMANENT));
                        profile._volumeMuteSound = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MUTE_SOUND)) == 1;
                        profile._deviceLocationMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_MODE));
                        profile._applicationEnableNotificationScanning = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING));
                        profile._generateNotification = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_GENERATE_NOTIFICATION));
                        profile._cameraFlash = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_CAMERA_FLASH));
                        profile._deviceNetworkTypeSIM1 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1));
                        profile._deviceNetworkTypeSIM2 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2));
                        //profile._deviceMobileDataSIM1 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1));
                        //profile._deviceMobileDataSIM2 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2));
                        profile._deviceDefaultSIMCards = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS));
                        profile._deviceOnOffSIM1 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_ONOFF_SIM1));
                        profile._deviceOnOffSIM2 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_ONOFF_SIM2));
                        profile._soundRingtoneChangeSIM1 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1));
                        profile._soundRingtoneSIM1 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_SIM1));
                        profile._soundRingtoneChangeSIM2 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2));
                        profile._soundRingtoneSIM2 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_SIM2));
                        profile._soundNotificationChangeSIM1 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1));
                        profile._soundNotificationSIM1 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1));
                        profile._soundNotificationChangeSIM2 = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2));
                        profile._soundNotificationSIM2 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2));
                        profile._soundSameRingtoneForBothSIMCards = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS));
                        profile._deviceLiveWallpaper = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER));
                        profile._deviceWallpaperFolder = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER));
                        profile._applicationDisableGloabalEventsRun = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN));
                        profile._deviceVPNSettingsPrefs = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS));
                        profile._endOfActivationType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE));
                        profile._endOfActivationTime = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_END_OF_ACTIVATION_TIME));
                        profile._applicationEnablePeriodicScanning = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING));
                        profile._deviceVPN = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_VPN));
                        profile._vibrationIntensityRinging = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING));
                        profile._vibrationIntensityNotifications = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS));
                        profile._vibrationIntensityTouchInteraction = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION));
                        profile._volumeMediaChangeDuringPlay = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY)) == 1;
                        profile._applicationWifiScanInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL));
                        profile._applicationBluetoothScanInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL));
                        profile._applicationBluetoothLEScanDuration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION));
                        profile._applicationLocationScanInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL));
                        profile._applicationOrientationScanInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL));
                        profile._applicationPeriodicScanInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL));
                        profile._phoneCallsContacts = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS));
                        profile._phoneCallsContactGroups = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS));
                        profile._phoneCallsBlockCalls = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_BLOCK_CALLS)) == 1;
                        profile._phoneCallsContactListType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_LIST_TYPE));
                        // Adding profile to list
                        profileList.add(profile);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

            return profileList;

        } finally {
            instance.stopRunningCommand();
        }
    }

    // Updating single profile
    static void updateProfile(DatabaseHandler instance, Profile profile) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_NAME, profile._name);
                values.put(DatabaseHandler.KEY_ICON, profile._icon);
                values.put(DatabaseHandler.KEY_CHECKED, (profile._checked) ? 1 : 0);
                values.put(DatabaseHandler.KEY_PORDER, profile._porder);
                values.put(DatabaseHandler.KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
                values.put(DatabaseHandler.KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
                values.put(DatabaseHandler.KEY_VOLUME_RINGTONE, profile._volumeRingtone);
                values.put(DatabaseHandler.KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
                values.put(DatabaseHandler.KEY_VOLUME_MEDIA, profile._volumeMedia);
                values.put(DatabaseHandler.KEY_VOLUME_ALARM, profile._volumeAlarm);
                values.put(DatabaseHandler.KEY_VOLUME_SYSTEM, profile._volumeSystem);
                values.put(DatabaseHandler.KEY_VOLUME_VOICE, profile._volumeVoice);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE, profile._soundRingtone);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION, profile._soundNotification);
                values.put(DatabaseHandler.KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
                values.put(DatabaseHandler.KEY_SOUND_ALARM, profile._soundAlarm);
                values.put(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
                values.put(DatabaseHandler.KEY_DEVICE_WIFI, profile._deviceWiFi);
                values.put(DatabaseHandler.KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
                values.put(DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
                values.put(DatabaseHandler.KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
                values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
                values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
                values.put(DatabaseHandler.KEY_DEVICE_GPS, profile._deviceGPS);
                values.put(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
                values.put(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
                values.put(DatabaseHandler.KEY_DEVICE_AUTOSYNC, profile._deviceAutoSync);
                values.put(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
                values.put(DatabaseHandler.KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
                values.put(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
                values.put(DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
                values.put(DatabaseHandler.KEY_DEVICE_NFC, profile._deviceNFC);
                values.put(DatabaseHandler.KEY_DURATION, profile._duration);
                values.put(DatabaseHandler.KEY_AFTER_DURATION_DO, profile._afterDurationDo);
                values.put(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
                values.put(DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
                values.put(DatabaseHandler.KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
                values.put(DatabaseHandler.KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
                values.put(DatabaseHandler.KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
                values.put(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
                values.put(DatabaseHandler.KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
                values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
                values.put(DatabaseHandler.KEY_NOTIFICATION_LED, profile._notificationLed);
                values.put(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
                values.put(DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS, profile._vibrateNotifications);
                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
                values.put(DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
                values.put(DatabaseHandler.KEY_LOCK_DEVICE, profile._lockDevice);
                values.put(DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING, profile._applicationEnableWifiScanning);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING, profile._applicationEnableBluetoothScanning);
                values.put(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS, profile._deviceWiFiAPPrefs);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING, profile._applicationEnableLocationScanning);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, profile._applicationEnableMobileCellScanning);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING, profile._applicationEnableOrientationScanning);
                values.put(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS, profile._headsUpNotifications);
                values.put(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, profile._deviceForceStopApplicationChange);
                values.put(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
                values.put(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT, profile._activationByUserCount);
                values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS, profile._deviceNetworkTypePrefs);
                values.put(DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS, profile._deviceCloseAllApplications);
                values.put(DatabaseHandler.KEY_SCREEN_DARK_MODE, profile._screenDarkMode);
                values.put(DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING, profile._dtmfToneWhenDialing);
                values.put(DatabaseHandler.KEY_SOUND_ON_TOUCH, profile._soundOnTouch);
                values.put(DatabaseHandler.KEY_VOLUME_DTMF, profile._volumeDTMF);
                values.put(DatabaseHandler.KEY_VOLUME_ACCESSIBILITY, profile._volumeAccessibility);
                values.put(DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO, profile._volumeBluetoothSCO);
                values.put(DatabaseHandler.KEY_AFTER_DURATION_PROFILE, profile._afterDurationProfile);
                values.put(DatabaseHandler.KEY_ALWAYS_ON_DISPLAY, profile._alwaysOnDisplay);
                values.put(DatabaseHandler.KEY_SCREEN_ON_PERMANENT, profile._screenOnPermanent);
                values.put(DatabaseHandler.KEY_VOLUME_MUTE_SOUND, (profile._volumeMuteSound) ? 1 : 0);
                values.put(DatabaseHandler.KEY_DEVICE_LOCATION_MODE, profile._deviceLocationMode);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING, profile._applicationEnableNotificationScanning);
                values.put(DatabaseHandler.KEY_GENERATE_NOTIFICATION, profile._generateNotification);
                values.put(DatabaseHandler.KEY_CAMERA_FLASH, profile._cameraFlash);
                values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1, profile._deviceNetworkTypeSIM1);
                values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2, profile._deviceNetworkTypeSIM2);
                //values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1, profile._deviceMobileDataSIM1);
                //values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2, profile._deviceMobileDataSIM2);
                values.put(DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS, profile._deviceDefaultSIMCards);
                values.put(DatabaseHandler.KEY_DEVICE_ONOFF_SIM1, profile._deviceOnOffSIM1);
                values.put(DatabaseHandler.KEY_DEVICE_ONOFF_SIM2, profile._deviceOnOffSIM2);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1, profile._soundRingtoneChangeSIM1);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_SIM1, profile._soundRingtoneSIM1);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2, profile._soundRingtoneChangeSIM2);
                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_SIM2, profile._soundRingtoneSIM2);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1, profile._soundNotificationChangeSIM1);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1, profile._soundNotificationSIM1);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2, profile._soundNotificationChangeSIM2);
                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2, profile._soundNotificationSIM2);
                values.put(DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, profile._soundSameRingtoneForBothSIMCards);
                values.put(DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER, profile._deviceLiveWallpaper);
                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER, profile._deviceWallpaperFolder);
                values.put(DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, profile._applicationDisableGloabalEventsRun);
                values.put(DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS, profile._deviceVPNSettingsPrefs);
                values.put(DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE, profile._endOfActivationType);
                values.put(DatabaseHandler.KEY_END_OF_ACTIVATION_TIME, profile._endOfActivationTime);
                values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING, profile._applicationEnablePeriodicScanning);
                values.put(DatabaseHandler.KEY_DEVICE_VPN, profile._deviceVPN);
                values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING, profile._vibrationIntensityRinging);
                values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS, profile._vibrationIntensityNotifications);
                values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION, profile._vibrationIntensityTouchInteraction);
                values.put(DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY, (profile._volumeMediaChangeDuringPlay) ? 1 : 0);
                values.put(DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL, profile._applicationWifiScanInterval);
                values.put(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL, profile._applicationBluetoothScanInterval);
                values.put(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION, profile._applicationBluetoothLEScanDuration);
                values.put(DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL, profile._applicationLocationScanInterval);
                values.put(DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL, profile._applicationOrientationScanInterval);
                values.put(DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL, profile._applicationPeriodicScanInterval);
                values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS, profile._phoneCallsContacts);
                values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS, profile._phoneCallsContactGroups);
                values.put(DatabaseHandler.KEY_PHONE_CALLS_BLOCK_CALLS, (profile._phoneCallsBlockCalls) ? 1 : 0);
                values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_LIST_TYPE, profile._phoneCallsContactListType);

                // updating row
                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                        new String[]{String.valueOf(profile._id)});
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting single profile
    static void deleteProfile(DatabaseHandler instance, Profile profile) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();
                try {

                    // unlink shortcuts from profile
                    String[] splits = profile._deviceRunApplicationPackageName.split(StringConstants.STR_SPLIT_REGEX);
                    for (String split : splits) {
                        boolean shortcut = Application.isShortcut(split);
                        if (shortcut) {
                            long shortcutId = Application.getShortcutId(split);
                            instance.deleteShortcut(shortcutId);
                        }
                    }

                    db.delete(DatabaseHandler.TABLE_PROFILES, DatabaseHandler.KEY_ID + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    // unlink profile from events
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_E_FK_PROFILE_START, 0);
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_FK_PROFILE_START + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values2 = new ContentValues();
                    values2.put(DatabaseHandler.KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                    db.update(DatabaseHandler.TABLE_EVENTS, values2, DatabaseHandler.KEY_E_FK_PROFILE_END + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values3 = new ContentValues();
                    values3.put(DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                    db.update(DatabaseHandler.TABLE_EVENTS, values3, DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                                                    DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE +
                                               " FROM " + DatabaseHandler.TABLE_EVENTS;
                    Cursor cursor = db.rawQuery(selectQuery, null);
                    if (cursor.moveToFirst()) {
                        do {
                            String oldFkProfiles = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE));
                            if (!oldFkProfiles.isEmpty()) {
                                splits = oldFkProfiles.split(StringConstants.STR_SPLIT_REGEX);
                                StringBuilder newFkProfiles = new StringBuilder();
                                for (String split : splits) {
                                    long fkProfile = Long.parseLong(split);
                                    if (fkProfile != profile._id) {
                                        if (newFkProfiles.length() > 0)
                                            newFkProfiles.append("|");
                                        newFkProfiles.append(split);
                                    }
                                }
                                values = new ContentValues();
                                values.put(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE, newFkProfiles.toString());
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting all profiles
    static void deleteAllProfiles(DatabaseHandler instance) {
        //boolean ok = false;
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();

                try {
                    db.delete(DatabaseHandler.TABLE_PROFILES, null, null);
                    //db.delete(TABLE_SHORTCUTS, null, null);
                    //db.delete(TABLE_INTENTS, null, null);

                    // unlink profiles from events
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_E_FK_PROFILE_START, 0);
                    values.put(DatabaseHandler.KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                    values.put(DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                    values.put(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE, "");
                    db.update(DatabaseHandler.TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                    //ok = true;
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
        //return ok;
    }

    // Getting max(porder)
    static private int getMaxProfileOrder(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                String countQuery = "SELECT MAX(" + DatabaseHandler.KEY_PORDER + ") FROM " + DatabaseHandler.TABLE_PROFILES;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static private void doActivateProfile(DatabaseHandler instance, Profile profile, boolean activate)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();
                try {
                    // update all profiles checked to false
                    ContentValues valuesAll = new ContentValues();
                    valuesAll.put(DatabaseHandler.KEY_CHECKED, 0);
                    db.update(DatabaseHandler.TABLE_PROFILES, valuesAll, null, null);

                    // updating checked = true for profile
                    //profile.setChecked(true);

                    if (activate && (profile != null)) {
                        ContentValues values = new ContentValues();
                        //values.put(KEY_CHECKED, (profile.getChecked()) ? 1 : 0);
                        values.put(DatabaseHandler.KEY_CHECKED, 1);

                        db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                new String[]{String.valueOf(profile._id)});
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void activateProfile(DatabaseHandler instance, Profile profile)
    {
        doActivateProfile(instance, profile, true);

        Intent sendIntent = new Intent(PhoneProfilesService.ACTION_ACTIVATED_PROFILE_EVENT_BROADCAST_RECEIVER);
        sendIntent.putExtra(ActivatedProfileEventBroadcastReceiver.EXTRA_ACTIVATED_PROFILE, profile._id);
        instance.context.sendBroadcast(sendIntent);
    }

    static void deactivateProfile(DatabaseHandler instance)
    {
        doActivateProfile(instance, null, false);
    }

    static Profile getActivatedProfile(DatabaseHandler instance)
    {
        instance.importExportLock.lock();
        try {
            Profile profile = null;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_PROFILES,
                        new String[]{DatabaseHandler.KEY_ID,
                                DatabaseHandler.KEY_NAME,
                                DatabaseHandler.KEY_ICON,
                                DatabaseHandler.KEY_CHECKED,
                                DatabaseHandler.KEY_PORDER,
                                DatabaseHandler.KEY_VOLUME_RINGER_MODE,
                                DatabaseHandler.KEY_VOLUME_RINGTONE,
                                DatabaseHandler.KEY_VOLUME_NOTIFICATION,
                                DatabaseHandler.KEY_VOLUME_MEDIA,
                                DatabaseHandler.KEY_VOLUME_ALARM,
                                DatabaseHandler.KEY_VOLUME_SYSTEM,
                                DatabaseHandler.KEY_VOLUME_VOICE,
                                DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE,
                                DatabaseHandler.KEY_SOUND_RINGTONE,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION,
                                DatabaseHandler.KEY_SOUND_ALARM_CHANGE,
                                DatabaseHandler.KEY_SOUND_ALARM,
                                DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE,
                                DatabaseHandler.KEY_DEVICE_WIFI,
                                DatabaseHandler.KEY_DEVICE_BLUETOOTH,
                                DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT,
                                DatabaseHandler.KEY_DEVICE_BRIGHTNESS,
                                DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE,
                                DatabaseHandler.KEY_DEVICE_WALLPAPER,
                                DatabaseHandler.KEY_DEVICE_MOBILE_DATA,
                                DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS,
                                DatabaseHandler.KEY_DEVICE_GPS,
                                DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE,
                                DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                                DatabaseHandler.KEY_DEVICE_AUTOSYNC,
                                DatabaseHandler.KEY_SHOW_IN_ACTIVATOR,
                                DatabaseHandler.KEY_DEVICE_AUTOROTATE,
                                DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS,
                                DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE,
                                DatabaseHandler.KEY_DEVICE_NFC,
                                DatabaseHandler.KEY_DURATION,
                                DatabaseHandler.KEY_AFTER_DURATION_DO,
                                DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND,
                                DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE,
                                DatabaseHandler.KEY_VOLUME_ZEN_MODE,
                                DatabaseHandler.KEY_DEVICE_KEYGUARD,
                                DatabaseHandler.KEY_VIBRATE_ON_TOUCH,
                                DatabaseHandler.KEY_DEVICE_WIFI_AP,
                                DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE,
                                DatabaseHandler.KEY_ASK_FOR_DURATION,
                                DatabaseHandler.KEY_DEVICE_NETWORK_TYPE,
                                DatabaseHandler.KEY_NOTIFICATION_LED,
                                DatabaseHandler.KEY_VIBRATE_WHEN_RINGING,
                                DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS,
                                DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR,
                                DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON,
                                DatabaseHandler.KEY_LOCK_DEVICE,
                                DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING,
                                DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING,
                                DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS,
                                DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE,
                                DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME,
                                DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT,
                                DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS,
                                DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS,
                                DatabaseHandler.KEY_SCREEN_DARK_MODE,
                                DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING,
                                DatabaseHandler.KEY_SOUND_ON_TOUCH,
                                DatabaseHandler.KEY_VOLUME_DTMF,
                                DatabaseHandler.KEY_VOLUME_ACCESSIBILITY,
                                DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO,
                                DatabaseHandler.KEY_AFTER_DURATION_PROFILE,
                                DatabaseHandler.KEY_ALWAYS_ON_DISPLAY,
                                DatabaseHandler.KEY_SCREEN_ON_PERMANENT,
                                DatabaseHandler.KEY_VOLUME_MUTE_SOUND,
                                DatabaseHandler.KEY_DEVICE_LOCATION_MODE,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING,
                                DatabaseHandler.KEY_GENERATE_NOTIFICATION,
                                DatabaseHandler.KEY_CAMERA_FLASH,
                                DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1,
                                DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2,
                                //DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1,
                                //DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2,
                                DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS,
                                DatabaseHandler.KEY_DEVICE_ONOFF_SIM1,
                                DatabaseHandler.KEY_DEVICE_ONOFF_SIM2,
                                DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1,
                                DatabaseHandler.KEY_SOUND_RINGTONE_SIM1,
                                DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2,
                                DatabaseHandler.KEY_SOUND_RINGTONE_SIM2,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2,
                                DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2,
                                DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS,
                                DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER,
                                DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER,
                                DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN,
                                DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS,
                                DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE,
                                DatabaseHandler.KEY_END_OF_ACTIVATION_TIME,
                                DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING,
                                DatabaseHandler.KEY_DEVICE_VPN,
                                DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING,
                                DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS,
                                DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION,
                                DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY,
                                DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL,
                                DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL,
                                DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION,
                                DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL,
                                DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL,
                                DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL,
                                DatabaseHandler.KEY_PHONE_CALLS_CONTACTS,
                                DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS,
                                DatabaseHandler.KEY_PHONE_CALLS_BLOCK_CALLS,
                                DatabaseHandler.KEY_PHONE_CALLS_CONTACT_LIST_TYPE
                        },
                        DatabaseHandler.KEY_CHECKED + "=?",
                        new String[]{"1"}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int rc = cursor.getCount();

                    if (rc == 1) {

                        profile = new Profile(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_CHECKED)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PORDER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGER_MODE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGTONE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_NOTIFICATION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MEDIA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ALARM)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SYSTEM)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_VOICE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BLUETOOTH)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BRIGHTNESS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_GPS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AUTOSYNC)),
                                (cursor.getColumnIndex(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR) != -1) && (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR)) == 1),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AUTOROTATE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NFC)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AFTER_DURATION_DO)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ZEN_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_KEYGUARD)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_ON_TOUCH)),
                                (cursor.getColumnIndex(DatabaseHandler.KEY_DEVICE_WIFI_AP) != -1) ? cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP)) : 0,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ASK_FOR_DURATION)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NOTIFICATION_LED)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_LOCK_DEVICE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SCREEN_DARK_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ON_TOUCH)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_DTMF)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ACCESSIBILITY)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AFTER_DURATION_PROFILE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ALWAYS_ON_DISPLAY)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SCREEN_ON_PERMANENT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MUTE_SOUND)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_GENERATE_NOTIFICATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_CAMERA_FLASH)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2)),
                                //cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1)),
                                //cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_ONOFF_SIM1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_ONOFF_SIM2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_SIM1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_SIM2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_END_OF_ACTIVATION_TIME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_VPN)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_LIST_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_BLOCK_CALLS)) == 1
                                );
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return profile;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static long getActivatedProfileId(DatabaseHandler instance)
    {
        instance.importExportLock.lock();
        try {
            long profileId = -1;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_PROFILES,
                        new String[]{DatabaseHandler.KEY_ID
                        },
                        DatabaseHandler.KEY_CHECKED + "=?",
                        new String[]{"1"}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int rc = cursor.getCount();

                    if (rc == 1) {
                        profileId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return profileId;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static long getProfileIdByName(DatabaseHandler instance, String name)
    {
        instance.importExportLock.lock();
        try {
            long id = 0;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_PROFILES,
                        new String[]{DatabaseHandler.KEY_ID},
                        "trim(" + DatabaseHandler.KEY_NAME + ")=?",
                        new String[]{name}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int rc = cursor.getCount();

                    if (rc == 1) {
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return id;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void setProfileOrder(DatabaseHandler instance, List<Profile> list)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();

                db.beginTransaction();
                try {

                    int size = list.size();
                    for (int i = 0; i < size; i++) {
                        Profile profile = list.get(i);
                        profile._porder = i + 1;

                        values.put(DatabaseHandler.KEY_PORDER, profile._porder);

                        db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                new String[]{String.valueOf(profile._id)});
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static String getProfileName(DatabaseHandler instance, long profile_id)
    {
        instance.importExportLock.lock();
        try {
            String name = "";
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_PROFILES,
                        new String[]{DatabaseHandler.KEY_NAME},
                        DatabaseHandler.KEY_ID + "=?",
                        new String[]{Long.toString(profile_id)}, null, null, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst())
                        name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME));
                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return name;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getProfileIcon(DatabaseHandler instance, Profile profile)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_PROFILES,
                        new String[]{DatabaseHandler.KEY_ICON},
                        DatabaseHandler.KEY_ID + "=?",
                        new String[]{Long.toString(profile._id)}, null, null, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst())
                        profile._icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON));
                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void saveMergedProfile(DatabaseHandler instance, Profile profile) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();

                try {
                    db.delete(DatabaseHandler.TABLE_MERGED_PROFILE, null, null);

                    addProfile(instance, profile, true);

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static long getActivationByUserCount(DatabaseHandler instance, long profileId) {
        instance.importExportLock.lock();
        try {
            long r = 0;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_PROFILES,
                        new String[]{DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT},
                        DatabaseHandler.KEY_ID + "=?",
                        new String[]{Long.toString(profileId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getLong(0);
                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static private void increaseActivationByUserCount(DatabaseHandler instance, long profileId) {
        long count = getActivationByUserCount(instance, profileId);
        ++count;

        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT, count);

                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                            new String[]{String.valueOf(profileId)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void increaseActivationByUserCount(DatabaseHandler instance, Profile profile) {
        if (profile != null) {
            long count = getActivationByUserCount(instance, profile._id);
            ++count;
            profile._activationByUserCount = count;
            increaseActivationByUserCount(instance, profile._id);
        }
    }

    static List<Profile> getProfilesForDynamicShortcuts(DatabaseHandler instance/*, boolean counted*/) {
        instance.importExportLock.lock();
        try {

            List<Profile> profileList = new ArrayList<>();

            try {
                instance.startRunningCommand();

                // Select All Query
                String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                                                DatabaseHandler.KEY_NAME + "," +
                                                DatabaseHandler.KEY_ICON + "," +
                                                DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT +
                                    " FROM " + DatabaseHandler.TABLE_PROFILES;

                /*if (counted) {
                    selectQuery = selectQuery +
                            " WHERE " + KEY_ACTIVATION_BY_USER_COUNT + "> 0" +
                            " ORDER BY " + KEY_ACTIVATION_BY_USER_COUNT + " DESC " +
                            " LIMIT " + "3"; // 3 shortcuts because first is restart events
                }
                else*/ {
                    selectQuery = selectQuery +
                            " WHERE " + DatabaseHandler.KEY_SHOW_IN_ACTIVATOR + "=1" +
                            //" AND " + DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + "= 0" +
                            " ORDER BY " + DatabaseHandler.KEY_PORDER +
                            " LIMIT " + "3"; // 3 shortcuts because first is restart events
                }

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Profile profile = new Profile();
                        profile._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        profile._name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME));
                        profile._icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON));
                        profile._activationByUserCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT));
                        boolean existsInTile = false;
                        for (int i = 0; i < PPApplication.quickTileProfileId.length; i++) {
                            if (ApplicationPreferences.getQuickTileProfileId(instance.context, i) == profile._id) {
                                existsInTile = true;
                                break;
                            }
                        }
                        if (!existsInTile) {
//                            PPApplicationStatic.logE("DatabaseHandlerProfiles.getProfilesForDynamicShortcuts", "profile._name="+profile._name);
                            profileList.add(profile);
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

            return profileList;

        } finally {
            instance.stopRunningCommand();
        }
    }

    static List<Profile> getProfilesInQuickTilesForDynamicShortcuts(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {

            List<Profile> profileList = new ArrayList<>();

            try {
                instance.startRunningCommand();

                // Select All Query
                String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_NAME + "," +
                        DatabaseHandler.KEY_ICON + "," +
                        DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                /*if (counted) {
                    selectQuery = selectQuery +
                            //" WHERE " + DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + "> 0" +
                            //" ORDER BY " + DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + " DESC " +
                            " LIMIT " + "3"; // 3 shortcuts because first is restart events
                }
                else {
                    selectQuery = selectQuery +
                            " WHERE " + DatabaseHandler.KEY_SHOW_IN_ACTIVATOR + "=1" +
                            " AND " + DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + "= 0" +
                            " ORDER BY " + DatabaseHandler.KEY_PORDER +
                            " LIMIT " + "3"; // 3 shortcuts because first is restart events
                }*/

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Profile profile = new Profile();
                        profile._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        profile._name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME));
                        profile._icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON));
                        profile._activationByUserCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT));
                        for (int i = 0; i < PPApplication.quickTileProfileId.length; i++) {
                            long tiledProfileId = ApplicationPreferences.getQuickTileProfileId(instance.context, i);
                            if (tiledProfileId == profile._id) {
//                                PPApplicationStatic.logE("DatabaseHandlerProfiles.getProfilesInQuickTilesForDynamicShortcuts", "profile._name="+profile._name);
                                profileList.add(profile);
                            }
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

            return profileList;

        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateProfileShowInActivator(DatabaseHandler instance, Profile profile) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR, profile._showInActivator);

                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

// SHORTCUTS ----------------------------------------------------------------------

    // Adding new shortcut
    static void addShortcut(DatabaseHandler instance, Shortcut shortcut) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_S_INTENT, shortcut._intent);
                values.put(DatabaseHandler.KEY_S_NAME, shortcut._name);

                db.beginTransaction();

                try {
                    // Inserting Row
                    shortcut._id = db.insert(DatabaseHandler.TABLE_SHORTCUTS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting single shortcut
    static Shortcut getShortcut(DatabaseHandler instance, long shortcutId) {
        instance.importExportLock.lock();
        try {
            Shortcut shortcut = null;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_SHORTCUTS,
                        new String[]{DatabaseHandler.KEY_S_ID,
                                DatabaseHandler.KEY_S_INTENT,
                                DatabaseHandler.KEY_S_NAME
                        },
                        DatabaseHandler.KEY_S_ID + "=?",
                        new String[]{String.valueOf(shortcutId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        shortcut = new Shortcut();
                        shortcut._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_S_ID));
                        shortcut._intent = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_S_INTENT));
                        shortcut._name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_S_NAME));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return shortcut;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting single shortcut
    static void deleteShortcut(DatabaseHandler instance, long shortcutId) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();

                try {

                    // delete geofence
                    db.delete(DatabaseHandler.TABLE_SHORTCUTS, DatabaseHandler.KEY_S_ID + " = ?",
                            new String[]{String.valueOf(shortcutId)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerProfiles.deleteShortcut", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

// INTENTS ----------------------------------------------------------------------

    // Adding new intent
    static void addIntent(DatabaseHandler instance, PPIntent intent) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_IN_NAME, intent._name);
                values.put(DatabaseHandler.KEY_IN_PACKAGE_NAME, intent._packageName);
                values.put(DatabaseHandler.KEY_IN_CLASS_NAME, intent._className);
                values.put(DatabaseHandler.KEY_IN_ACTION, intent._action);
                values.put(DatabaseHandler.KEY_IN_DATA, intent._data);
                values.put(DatabaseHandler.KEY_IN_MIME_TYPE, intent._mimeType);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_1, intent._extraKey1);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_1, intent._extraValue1);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_1, intent._extraType1);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_2, intent._extraKey2);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_2, intent._extraValue2);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_2, intent._extraType2);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_3, intent._extraKey3);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_3, intent._extraValue3);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_3, intent._extraType3);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_4, intent._extraKey4);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_4, intent._extraValue4);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_4, intent._extraType4);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_5, intent._extraKey5);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_5, intent._extraValue5);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_5, intent._extraType5);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_6, intent._extraKey6);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_6, intent._extraValue6);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_6, intent._extraType6);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_7, intent._extraKey7);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_7, intent._extraValue7);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_7, intent._extraType7);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_8, intent._extraKey8);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_8, intent._extraValue8);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_8, intent._extraType8);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_9, intent._extraKey9);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_9, intent._extraValue9);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_9, intent._extraType9);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_10, intent._extraKey10);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_10, intent._extraValue10);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_10, intent._extraType10);
                values.put(DatabaseHandler.KEY_IN_CATEGORIES, intent._categories);
                values.put(DatabaseHandler.KEY_IN_FLAGS, intent._flags);
                values.put(DatabaseHandler.KEY_IN_INTENT_TYPE, intent._intentType);

                //values.put(KEY_IN_USED_COUNT, intent._usedCount);
                values.put(DatabaseHandler.KEY_IN_DO_NOT_DELETE, intent._doNotDelete);

                db.beginTransaction();

                try {
                    // Inserting Row
                    intent._id = db.insert(DatabaseHandler.TABLE_INTENTS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting All intents
    static List<PPIntent> getAllIntents(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            List<PPIntent> intentList = new ArrayList<>();
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_IN_ID + "," +
                        DatabaseHandler.KEY_IN_NAME + ", " +
                        DatabaseHandler.KEY_IN_PACKAGE_NAME + ", " +
                        DatabaseHandler.KEY_IN_CLASS_NAME + ", " +
                        DatabaseHandler.KEY_IN_ACTION + ", " +
                        DatabaseHandler.KEY_IN_DATA + ", " +
                        DatabaseHandler.KEY_IN_MIME_TYPE + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_1 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_1 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_1 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_2 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_2 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_2 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_3 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_3 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_3 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_4 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_4 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_4 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_5 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_5 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_5 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_6 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_6 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_6 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_7 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_7 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_7 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_8 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_8 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_8 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_9 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_9 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_9 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_KEY_10 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_VALUE_10 + ", " +
                        DatabaseHandler.KEY_IN_EXTRA_TYPE_10 + ", " +
                        DatabaseHandler.KEY_IN_CATEGORIES + ", " +
                        DatabaseHandler.KEY_IN_FLAGS + ", " +
                        DatabaseHandler.KEY_IN_INTENT_TYPE + ", " +

                        //DatabaseHandler.KEY_IN_USED_COUNT + ", " +
                        DatabaseHandler.KEY_IN_DO_NOT_DELETE +

                        " FROM " + DatabaseHandler.TABLE_INTENTS +
                        " ORDER BY " + DatabaseHandler.KEY_IN_NAME;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        PPIntent ppIntent = new PPIntent(
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_PACKAGE_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_CLASS_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_ACTION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_DATA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_MIME_TYPE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_3)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_3)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_3)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_4)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_4)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_4)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_5)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_5)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_5)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_6)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_6)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_6)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_7)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_7)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_7)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_8)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_8)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_8)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_9)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_9)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_9)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_10)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_10)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_10)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_CATEGORIES)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_FLAGS)),
                                //cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_USED_COUNT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_INTENT_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_DO_NOT_DELETE))  == 1
                        );
                        intentList.add(ppIntent);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return intentList;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Updating single intent
    static void updateIntent(DatabaseHandler instance, PPIntent intent) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_IN_NAME, intent._name);
                values.put(DatabaseHandler.KEY_IN_PACKAGE_NAME, intent._packageName);
                values.put(DatabaseHandler.KEY_IN_CLASS_NAME, intent._className);
                values.put(DatabaseHandler.KEY_IN_ACTION, intent._action);
                values.put(DatabaseHandler.KEY_IN_DATA, intent._data);
                values.put(DatabaseHandler.KEY_IN_MIME_TYPE, intent._mimeType);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_1, intent._extraKey1);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_1, intent._extraValue1);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_1, intent._extraType1);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_2, intent._extraKey2);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_2, intent._extraValue2);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_2, intent._extraType2);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_3, intent._extraKey3);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_3, intent._extraValue3);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_3, intent._extraType3);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_4, intent._extraKey4);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_4, intent._extraValue4);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_4, intent._extraType4);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_5, intent._extraKey5);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_5, intent._extraValue5);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_5, intent._extraType5);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_6, intent._extraKey6);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_6, intent._extraValue6);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_6, intent._extraType6);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_7, intent._extraKey7);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_7, intent._extraValue7);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_7, intent._extraType7);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_8, intent._extraKey8);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_8, intent._extraValue8);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_8, intent._extraType8);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_9, intent._extraKey9);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_9, intent._extraValue9);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_9, intent._extraType9);
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_10, intent._extraKey10);
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_10, intent._extraValue10);
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_10, intent._extraType10);
                values.put(DatabaseHandler.KEY_IN_CATEGORIES, intent._categories);
                values.put(DatabaseHandler.KEY_IN_FLAGS, intent._flags);
                values.put(DatabaseHandler.KEY_IN_INTENT_TYPE, intent._intentType);

                //values.put(DatabaseHandler.KEY_IN_USED_COUNT, intent._usedCount);
                values.put(DatabaseHandler.KEY_IN_DO_NOT_DELETE, intent._doNotDelete);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_INTENTS, values, DatabaseHandler.KEY_IN_ID + " = ?",
                            new String[]{String.valueOf(intent._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerProfiles.updateIntent", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting single intent
    static PPIntent getIntent(DatabaseHandler instance, long intentId) {
        instance.importExportLock.lock();
        try {
            PPIntent intent = null;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_INTENTS,
                        new String[]{DatabaseHandler.KEY_IN_ID,
                                DatabaseHandler.KEY_IN_NAME,
                                DatabaseHandler.KEY_IN_PACKAGE_NAME,
                                DatabaseHandler.KEY_IN_CLASS_NAME,
                                DatabaseHandler.KEY_IN_ACTION,
                                DatabaseHandler.KEY_IN_DATA,
                                DatabaseHandler.KEY_IN_MIME_TYPE,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_1,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_1,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_1,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_2,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_2,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_2,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_3,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_3,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_3,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_4,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_4,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_4,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_5,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_5,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_5,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_6,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_6,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_6,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_7,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_7,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_7,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_8,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_8,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_8,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_9,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_9,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_9,
                                DatabaseHandler.KEY_IN_EXTRA_KEY_10,
                                DatabaseHandler.KEY_IN_EXTRA_VALUE_10,
                                DatabaseHandler.KEY_IN_EXTRA_TYPE_10,
                                DatabaseHandler.KEY_IN_CATEGORIES,
                                DatabaseHandler.KEY_IN_FLAGS,
                                DatabaseHandler.KEY_IN_INTENT_TYPE,

                                //DatabaseHandler.KEY_IN_USED_COUNT,
                                DatabaseHandler.KEY_IN_DO_NOT_DELETE
                        },
                        DatabaseHandler.KEY_IN_ID + "=?",
                        new String[]{String.valueOf(intentId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        intent = new PPIntent(
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_PACKAGE_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_CLASS_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_ACTION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_DATA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_MIME_TYPE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_3)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_3)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_3)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_4)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_4)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_4)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_5)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_5)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_5)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_6)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_6)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_6)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_7)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_7)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_7)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_8)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_8)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_8)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_9)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_9)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_9)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_KEY_10)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_VALUE_10)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_EXTRA_TYPE_10)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_CATEGORIES)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_FLAGS)),
                                //cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_USED_COUNT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_INTENT_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_DO_NOT_DELETE)) == 1
                        );
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return intent;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting single intent
    static void deleteIntent(DatabaseHandler instance, long intentId) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete geofence
                    db.delete(DatabaseHandler.TABLE_INTENTS, DatabaseHandler.KEY_IN_ID + " = ?",
                            new String[]{String.valueOf(intentId)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerProfiles.deleteIntent", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static boolean profileExists(DatabaseHandler instance, long profile_id) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + DatabaseHandler.TABLE_PROFILES +
                        " WHERE " + DatabaseHandler.KEY_ID + "=" + profile_id;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r > 0;
        } finally {
            instance.stopRunningCommand();
        }
    }

}
