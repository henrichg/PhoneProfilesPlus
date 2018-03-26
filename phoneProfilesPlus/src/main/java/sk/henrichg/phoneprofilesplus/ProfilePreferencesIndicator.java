package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;


class ProfilePreferencesIndicator {

    private static Bitmap createIndicatorBitmap(Context context, int countDrawables)
    {
        // bitmap to get size
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_pref_volume_on);

        int width  = bmp.getWidth() * countDrawables;
        int height  = bmp.getHeight();

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    private static void addIndicator(int preferenceBitmapResourceID, int index, Context context, Canvas canvas)
    {
        Bitmap preferenceBitmap = BitmapFactory.decodeResource(context.getResources(), preferenceBitmapResourceID);

        if (preferenceBitmap != null)
            canvas.drawBitmap(preferenceBitmap, preferenceBitmap.getWidth() * index, 0, null);
        //canvas.save();

    }

    static Bitmap paint(Profile _profile, Context context)
    {

        int[] drawables = new int[30];
        int countDrawables = 0;

        Profile profile = Profile.getMappedProfile(_profile, context);

        if (profile != null)
        {
            if (profile._volumeRingerMode != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if (profile._volumeRingerMode == 5) {
                        // zen mode
                        if (profile._volumeZenMode == 1)
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
                        if (profile._volumeZenMode == 2)
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_priority;
                        if (profile._volumeZenMode == 3)
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_none;
                        if (profile._volumeZenMode == 4) {
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                        }
                        if (profile._volumeZenMode == 5) {
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_priority;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                        }
                        if (profile._volumeZenMode == 6)
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_alarms;
                    } else {
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
                }
            }
            /*// vibrate when ringing
            if (profile._vibrateWhenRinging != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_vibrate_when_ringing;
                    if (profile._vibrateWhenRinging == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_vibrate_when_ringing_off;
                }
            }*/
            // vibration on touch
            if (profile._vibrationOnTouch != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._vibrationOnTouch == 1) || (profile._vibrationOnTouch == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration_on_touch;
                    if (profile._vibrationOnTouch == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration_on_touch_off;
                }
            }
            // volume level
            if (profile.getVolumeAlarmChange() ||
                profile.getVolumeMediaChange() ||
                profile.getVolumeNotificationChange() ||
                profile.getVolumeRingtoneChange() ||
                profile.getVolumeSystemChange() ||
                profile.getVolumeVoiceChange()) {
                if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ALARM, context) == PPApplication.PREFERENCE_ALLOWED) ||
                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_MEDIA, context) == PPApplication.PREFERENCE_ALLOWED) ||
                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, context) == PPApplication.PREFERENCE_ALLOWED) ||
                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_RINGTONE, context) == PPApplication.PREFERENCE_ALLOWED) ||
                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_SYSTEM, context) == PPApplication.PREFERENCE_ALLOWED) ||
                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_VOICE, context) == PPApplication.PREFERENCE_ALLOWED))
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_level;
            }
            // speaker phone
            if (profile._volumeSpeakerPhone != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if (profile._volumeSpeakerPhone == 1)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone;
                    if (profile._volumeSpeakerPhone == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone_off;
                }
            }
            // sound
            if ((profile._soundRingtoneChange == 1) ||
                (profile._soundNotificationChange == 1) ||
                (profile._soundAlarmChange == 1)) {
                if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, context) == PPApplication.PREFERENCE_ALLOWED) ||
                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, context) == PPApplication.PREFERENCE_ALLOWED) ||
                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, context) == PPApplication.PREFERENCE_ALLOWED))
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_sound;
            }
            // airplane mode
            if (profile._deviceAirplaneMode != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceAirplaneMode == 1) || (profile._deviceAirplaneMode == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode;
                    if (profile._deviceAirplaneMode == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode_off;
                }
            }
            // auto-sync
            if (profile._deviceAutoSync != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceAutoSync == 1) || (profile._deviceAutoSync == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync;
                    if (profile._deviceAutoSync == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync_off;
                }
            }
            // network type
            if (profile._deviceNetworkType != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type;
            }
            // mobile data
            if (profile._deviceMobileData != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceMobileData == 1) || (profile._deviceMobileData == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata;
                    if (profile._deviceMobileData == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_off;
                }
            }
            // mobile data preferences
            if (profile._deviceMobileDataPrefs == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_pref;
            }
            // wifi
            if (profile._deviceWiFi != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceWiFi == 1) || (profile._deviceWiFi == 3) || (profile._deviceWiFi == 4) || (profile._deviceWiFi == 5))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi;
                    if (profile._deviceWiFi == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_off;
                }
            }
            // wifi AP
            if (profile._deviceWiFiAP != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceWiFiAP == 1) || (profile._deviceWiFiAP == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap;
                    if (profile._deviceWiFiAP == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap_off;
                }
            }
            // wifi AP preferences
            if (profile._deviceWiFiAPPrefs == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap_pref;
            }
            // bluetooth
            if (profile._deviceBluetooth != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceBluetooth == 1) || (profile._deviceBluetooth == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth;
                    if (profile._deviceBluetooth == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth_off;
                }
            }
            // gps
            if (profile._deviceGPS != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceGPS == 1) || (profile._deviceGPS == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_on;
                    if (profile._deviceGPS == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_off;
                }
            }
            // location settings preferences
            if (profile._deviceLocationServicePrefs == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_locationsettings_pref;
            }
            // nfc
            if (profile._deviceNFC != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceNFC == 1) || (profile._deviceNFC == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc;
                    if (profile._deviceNFC == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc_off;
                }
            }
            // screen timeout
            if (profile._deviceScreenTimeout != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_timeout;
            }
            // lock screen
            if (profile._deviceKeyguard != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_KEYGUARD, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._deviceKeyguard == 1) || (profile._deviceKeyguard == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen;
                    if (profile._deviceKeyguard == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen_off;
                }
            }
            // brightness/auto-brightness
            if (profile.getDeviceBrightnessChange()) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if (profile.getDeviceBrightnessAutomatic())
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_autobrightness;
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_brightness;
                }
            }
            // auto-rotate
            if (profile._deviceAutoRotate != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if (profile._deviceAutoRotate == 1)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate_off;
                    else if (profile._deviceAutoRotate != 0)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate;
                }
            }
            // notification led
            if (profile._notificationLed != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._notificationLed == 1) || (profile._notificationLed == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_notification_led;
                    if (profile._notificationLed == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_notification_led_off;
                }
            }
            // heads-up notifications
            if (profile._headsUpNotifications != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._headsUpNotifications == 1) || (profile._headsUpNotifications == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_heads_up_notifications;
                    if (profile._headsUpNotifications == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_heads_up_notifications_off;
                }
            }
            // power save mode
            if (profile._devicePowerSaveMode != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._devicePowerSaveMode == 1) || (profile._devicePowerSaveMode == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_power_save_mode;
                    if (profile._devicePowerSaveMode == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_power_save_mode_off;
                }
            }
            // run application
            if (profile._deviceRunApplicationChange == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_run_application;
            }
            // force stop application
            if (profile._deviceForceStopApplicationChange == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_force_stop_application;
            }
            // wallpaper
            if (profile._deviceWallpaperChange == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_wallpaper;
            }
            // lock device
            if (profile._lockDevice != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_LOCK_DEVICE, context) == PPApplication.PREFERENCE_ALLOWED)
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_lock;
            }
            // disable wifi scanning
            if (profile._applicationDisableWifiScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableWifiScanning == 1) || (profile._applicationDisableWifiScanning == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_wifi;
                    if (profile._applicationDisableWifiScanning == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_wifi_off;
                }
            }
            // disable bluetooth scanning
            if (profile._applicationDisableBluetoothScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableBluetoothScanning == 1) || (profile._applicationDisableBluetoothScanning == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_bluetooth;
                    if (profile._applicationDisableBluetoothScanning == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_bluetooth_off;
                }
            }
            // disable location scanning
            if (profile._applicationDisableLocationScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableLocationScanning == 1) || (profile._applicationDisableLocationScanning == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_location;
                    if (profile._applicationDisableLocationScanning == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_location_off;
                }
            }
            // disable mobile cell scanning
            if (profile._applicationDisableMobileCellScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableMobileCellScanning == 1) || (profile._applicationDisableMobileCellScanning == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_mobile_cell;
                    if (profile._applicationDisableMobileCellScanning == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_mobile_cell_off;
                }
            }
            // disable orientation scanning
            if (profile._applicationDisableOrientationScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING, context) == PPApplication.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableOrientationScanning == 1) || (profile._applicationDisableOrientationScanning == 3))
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_orientation;
                    if (profile._applicationDisableOrientationScanning == 2)
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_orientation_off;
                }
            }
        }
        else
            countDrawables = -1;

        Bitmap indicatorBitmap;
        if (countDrawables >= 0)
        {
            if (countDrawables > 0)
            {
                try {
                    indicatorBitmap = createIndicatorBitmap(context, countDrawables);
                    Canvas canvas = new Canvas(indicatorBitmap);
                    for (int i = 0; i < countDrawables; i++)
                        addIndicator(drawables[i], i, context, canvas);
                } catch (Exception e) {
                    indicatorBitmap = null;
                }
            }
            else
                indicatorBitmap = createIndicatorBitmap(context, 1);
        }
        else
            indicatorBitmap = null;

        return indicatorBitmap;

    }

}
