package sk.henrichg.phoneprofilesplus;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;

class DatabaseHandlerOthers {

// ACTIVITY LOG -------------------------------------------------------------------

    // Adding activity log
    static void addActivityLog(DatabaseHandler instance, int deleteOldActivityLogs,
                        int logType, String eventName, String profileName, String profileEventsCount) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_AL_LOG_TYPE, logType);
                values.put(DatabaseHandler.KEY_AL_EVENT_NAME, eventName);
                values.put(DatabaseHandler.KEY_AL_PROFILE_NAME, profileName);
                values.put(DatabaseHandler.KEY_AL_PROFILE_EVENT_COUNT, profileEventsCount);

                db.beginTransaction();

                try {
                    if (deleteOldActivityLogs > 0) {
                        // delete older than 7 days old records
                        db.delete(DatabaseHandler.TABLE_ACTIVITY_LOG, DatabaseHandler.KEY_AL_LOG_DATE_TIME +
                                " < date('now','-" + deleteOldActivityLogs + " days')", null);
                    }

                    // Inserting Row
                    db.insert(DatabaseHandler.TABLE_ACTIVITY_LOG, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void clearActivityLog(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    db.delete(DatabaseHandler.TABLE_ACTIVITY_LOG, null, null);

                    // db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    //} finally {
                    //db.endTransaction();
                    PPApplication.recordException(e);
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static Cursor getActivityLogCursor(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            Cursor cursor = null;
            try {
                instance.startRunningCommand();

                final String selectQuery = "SELECT " + DatabaseHandler.KEY_AL_ID + "," +
                        DatabaseHandler.KEY_AL_LOG_DATE_TIME + "," +
                        DatabaseHandler.KEY_AL_LOG_TYPE + "," +
                        DatabaseHandler.KEY_AL_EVENT_NAME + "," +
                        DatabaseHandler.KEY_AL_PROFILE_NAME + "," +
                        //DatabaseHandler.KEY_AL_PROFILE_ICON + "," +
                        //DatabaseHandler.KEY_AL_DURATION_DELAY + "," +
                        DatabaseHandler.KEY_AL_PROFILE_EVENT_COUNT +
                        " FROM " + DatabaseHandler.TABLE_ACTIVITY_LOG +
                        " ORDER BY " + DatabaseHandler.KEY_AL_ID + " DESC";

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                cursor = db.rawQuery(selectQuery, null);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return cursor;
        } finally {
            instance.stopRunningCommand();
        }
    }

// OTHERS -------------------------------------------------------------------------

    static void disableNotAllowedPreferences(DatabaseHandler instance)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                final String selectProfilesQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE + "," +
                        DatabaseHandler.KEY_DEVICE_WIFI + "," +
                        DatabaseHandler.KEY_DEVICE_BLUETOOTH + "," +
                        DatabaseHandler.KEY_DEVICE_MOBILE_DATA + "," +
                        DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS + "," +
                        DatabaseHandler.KEY_DEVICE_GPS + "," +
                        DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS + "," +
                        DatabaseHandler.KEY_DEVICE_NFC + "," +
                        DatabaseHandler.KEY_VOLUME_RINGER_MODE + "," +
                        DatabaseHandler.KEY_DEVICE_WIFI_AP + "," +
                        DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE + "," +
                        DatabaseHandler.KEY_VOLUME_ZEN_MODE + "," +
                        DatabaseHandler.KEY_DEVICE_NETWORK_TYPE + "," +
                        DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS + "," +
                        DatabaseHandler.KEY_NOTIFICATION_LED + "," +
                        DatabaseHandler.KEY_VIBRATE_WHEN_RINGING + "," +
                        DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS + "," +
                        DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID + "," +
                        DatabaseHandler.KEY_APPLICATION_DISABLE_WIFI_SCANNING + "," +
                        DatabaseHandler.KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + "," +
                        DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS + "," +
                        DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS + "," +
                        DatabaseHandler.KEY_ALWAYS_ON_DISPLAY + "," +
                        DatabaseHandler.KEY_DEVICE_LOCATION_MODE + "," +
                        DatabaseHandler.KEY_CAMERA_FLASH + "," +
                        DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + "," +
                        DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + "," +
                        DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + "," +
                        DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + "," +
                        DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + "," +
                        DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + "," +
                        DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + "," +
                        DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + "," +
                        DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + "," +
                        DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "," +
                        DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "," +
                        DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "," +
                        DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING + "," +
                        DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS + "," +
                        DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;
                final String selectEventsQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_WIFI_ENABLED + "," +
                        DatabaseHandler.KEY_E_BLUETOOTH_ENABLED + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_ENABLED + "," +
                        DatabaseHandler.KEY_E_ORIENTATION_ENABLED + "," +
                        DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED + "," +
                        DatabaseHandler.KEY_E_NFC_ENABLED + "," +
                        DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "," +
                        DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED + "," +
                        DatabaseHandler.KEY_E_VOLUMES_ENABLED + "," +
                        DatabaseHandler.KEY_E_VOLUMES_RINGTONE + "," +
                        DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION + "," +
                        DatabaseHandler.KEY_E_VOLUMES_MEDIA + "," +
                        DatabaseHandler.KEY_E_VOLUMES_ALARM + "," +
                        DatabaseHandler.KEY_E_VOLUMES_SYSTEM + "," +
                        DatabaseHandler.KEY_E_VOLUMES_VOICE + "," +
                        DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO + "," +
                        DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();

                Cursor profilesCursor = db.rawQuery(selectProfilesQuery, null);
                Cursor eventsCursor = db.rawQuery(selectEventsQuery, null);

                db.beginTransaction();
                //noinspection TryFinallyCanBeTryWithResources
                try {
                    SharedPreferences sharedPreferences = instance.context.getApplicationContext().getSharedPreferences("temp_disableNotAllowedPreferences", Context.MODE_PRIVATE);

                    if (profilesCursor.moveToFirst()) {
                        do {
                            Profile profile = DatabaseHandlerProfiles.getProfile(instance, profilesCursor.getLong(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)), false);
                            profile.saveProfileToSharedPreferences(sharedPreferences);

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_WIFI, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BLUETOOTH)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_BLUETOOTH, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, null, sharedPreferences, true, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_GPS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_GPS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NFC)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_NFC, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_WIFI_AP, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGER_MODE)) == 5) {
                                boolean notRemove = ActivateProfileHelper.canChangeZenMode(instance.context);
                                if (!notRemove) {
                                    int zenMode = profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ZEN_MODE));
                                    int ringerMode = 0;
                                    switch (zenMode) {
                                        case 1:
                                            ringerMode = 1;
                                            break;
                                        case 2:
                                        case 3:
                                        case 6:
                                            ringerMode = 4;
                                            break;
                                        case 4:
                                        case 5:
                                            ringerMode = 3;
                                            break;
                                    }
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_VOLUME_RINGER_MODE, ringerMode);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                                Vibrator vibrator = (Vibrator) instance.context.getSystemService(Context.VIBRATOR_SERVICE);
                                if (!((vibrator != null) && vibrator.hasVibrator())) {
                                    int ringerMode = profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGER_MODE));
                                    if (ringerMode == 3) {
                                        ringerMode = 1;

                                        values.clear();
                                        values.put(DatabaseHandler.KEY_VOLUME_RINGER_MODE, ringerMode);
                                        db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                                new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                    }
                                    else
                                    if (ringerMode == 5) {
                                        int zenMode = profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ZEN_MODE));
                                        if (zenMode == 4)
                                            zenMode = 1;
                                        else
                                        if (zenMode == 5)
                                            zenMode = 2;

                                        values.clear();
                                        values.put(DatabaseHandler.KEY_VOLUME_ZEN_MODE, zenMode);
                                        db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                                new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                    }
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NOTIFICATION_LED)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_NOTIFICATION_LED, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            PreferenceAllowed _preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, sharedPreferences, false, instance.context);
                            if ((_preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_DISABLE_WIFI_SCANNING)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_APPLICATION_DISABLE_WIFI_SCANNING, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ALWAYS_ON_DISPLAY)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_ALWAYS_ON_DISPLAY, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_MODE)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_LOCATION_MODE, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_CAMERA_FLASH)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_CAMERA_FLASH, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_CAMERA_FLASH, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_ONOFF_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_ONOFF_SIM1, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_ONOFF_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_DEVICE_ONOFF_SIM2, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION)) != 0) {
                                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, null, sharedPreferences, false, instance.context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION, 0);
                                    db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)))});
                                }
                            }

                        } while (profilesCursor.moveToNext());
                    }

                    //-----------------------

                    if (eventsCursor.moveToFirst()) {
                        do {
                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_WIFI_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, instance.context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_WIFI_ENABLED, 0);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }
                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, instance.context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_BLUETOOTH_ENABLED, 0);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }
                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, instance.context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_NOTIFICATION_ENABLED, 0);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }
                            if (eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_ENABLED)) != 0) {
                                boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
                                boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
                                boolean hasProximity = PPApplication.proximitySensor != null;
                                boolean hasLight = PPApplication.lightSensor != null;

                                boolean enabled = hasAccelerometer && hasMagneticField;
                                if (!enabled) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_ORIENTATION_DISPLAY, "");
                                    values.put(DatabaseHandler.KEY_E_ORIENTATION_SIDES, "");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                                enabled = hasAccelerometer;
                                if (!enabled) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_ORIENTATION_SIDES, "");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                                enabled = hasProximity;
                                if (!enabled) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_ORIENTATION_DISTANCE, 0);
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                                enabled = hasLight;
                                if (!enabled) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_ORIENTATION_CHECK_LIGHT, 0);
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }
                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED)) != 0) &&
                                    //(Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                    (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM, instance.context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED, 0);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }

                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NFC_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, instance.context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_NFC_ENABLED, 0);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }

                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, instance.context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED, 0);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }

                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED, instance.context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED, 0);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }

                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED, instance.context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_VOLUMES_ENABLED, 0);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }

                            String value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_RINGTONE));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_VOLUMES_RINGTONE, splits[0] + "|0|0");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION, splits[0] + "|0|0");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_MEDIA));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_VOLUMES_MEDIA, splits[0] + "|0|0");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_ALARM));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_VOLUMES_ALARM, splits[0] + "|0|0");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_SYSTEM));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_VOLUMES_SYSTEM, splits[0] + "|0|0");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_VOICE));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_VOLUMES_VOICE, splits[0] + "|0|0");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO, splits[0] + "|0|0");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY, splits[0] + "|0|0");
                                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                                }
                            }

                        } while (eventsCursor.moveToNext());
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                    profilesCursor.close();
                    eventsCursor.close();
                }

                //db.close();
            } catch (Exception e) {
//                Log.e("DatabaseHandler.disableNotAllowedPreferences", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

}
