package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import se.simbio.encryption.Encryption;

/** @noinspection ExtractMethodRecommender*/
class DatabaseHandlerImportExport {

    static final String PREF_MAXIMUM_VOLUME_RING = "maximumVolume_ring";
    static final String PREF_MAXIMUM_VOLUME_NOTIFICATION = "maximumVolume_notification";
    static final String PREF_MAXIMUM_VOLUME_MUSIC = "maximumVolume_music";
    static final String PREF_MAXIMUM_VOLUME_ALARM = "maximumVolume_alarm";
    static final String PREF_MAXIMUM_VOLUME_SYSTEM = "maximumVolume_system";
    static final String PREF_MAXIMUM_VOLUME_VOICE_CALL = "maximumVolume_voiceCall";
    static final String PREF_MAXIMUM_VOLUME_DTMF = "maximumVolume_dtmf";
    static final String PREF_MAXIMUM_VOLUME_ACCESSIBILITY = "maximumVolume_accessibility";
    static final String PREF_MAXIMUM_VOLUME_BLUETOOTH_SCO = "maximumVolume_bluetoothSCO";
    static final String PREF_MINIMUM_VIBRATION_INTENSITY_RINGING = "minimumVibrationIntensity_ringing";
    static final String PREF_MINIMUM_VIBRATION_INTENSITY_NOTIFICATION = "minimumVibrationIntensity_notificaitons";
    static final String PREF_MINIMUM_VIBRATION_INTENSITY_TOUCH_INTERACTION = "minimumVibrationIntensity_touchInteraction";
    static final String PREF_MAXIMUM_VIBRATION_INTENSITY_RINGING = "maximumVibrationIntensity_ringing";
    static final String PREF_MAXIMUM_VIBRATION_INTENSITY_NOTIFICATION = "maximumVibrationIntensity_notificaitons";
    static final String PREF_MAXIMUM_VIBRATION_INTENSITY_TOUCH_INTERACTION = "maximumVibrationIntensity_touchInteraction";

    static private boolean tableExists(String tableName, SQLiteDatabase db)
    {
        //boolean tableExists = false;

        /* get cursor on it */
        try
        {
            String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
            try (Cursor cursor = db.rawQuery(query, null)) {
                //if(cursor!=null) {
                    if (cursor.getCount()>0) {
                        cursor.close();
                        return true;
                    }
                    cursor.close();
                //}
                return false;
            }
            /*
            Cursor c = db.query(tableName, null,
                null, null, null, null, null);
            tableExists = true;
            c.close();*/
        }
        catch (Exception e) {
            /* not exists ? */
            PPApplicationStatic.recordException(e);
        }

        return false;
    }

    static private void recalculateVolume(Cursor cursorImportDB, String volumeField, ContentValues values,
                                          AudioManager audioManager, int volumeStream,
                                          int maximumVolumeFromSharedPrefs) {
        try {
            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(volumeField));
            if (value != null) {
                String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
                int volume = Integer.parseInt(splits[0]);
                float fVolume = volume;

                // get percentage of value from imported data
                float percentage;
                if (maximumVolumeFromSharedPrefs > 0)
                    percentage = fVolume / maximumVolumeFromSharedPrefs * 100f;
                else
                    percentage = fVolume / audioManager.getStreamMaxVolume(volumeStream) * 100f;
                if (percentage > 100f)
                    percentage = 100f;

                // get value from percentage for actual system max volume
                fVolume = audioManager.getStreamMaxVolume(volumeStream) / 100f * percentage;
                volume = Math.round(fVolume);

                if (splits.length == 3)
                    values.put(volumeField, volume + "|" + splits[1] + "|" + splits[2]);
                else
                    values.put(volumeField, volume + "|" + splits[1]);
            }
        } catch (IllegalArgumentException e) {
            // java.lang.IllegalArgumentException: Bad stream type X
            //PPApplicationStatic.recordException(e);
        } catch (Exception e) {
            //Log.e("DatabaseHandlerImportExport.afterImportDb", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static private void afterImportDbVolumes(DatabaseHandler instance, SQLiteDatabase db) {
        Cursor cursorImportDB = null;

        // update volumes by device max value
        try {
            // these shared preferences are put during export of data, values are from AudioManager
            // for import, these data are values from source of imported data (may be from another device)
            SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(instance.context);
            int maximumVolumeRing = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_RING, 0);
            int maximumVolumeNotification = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_NOTIFICATION, 0);
            int maximumVolumeMusic = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_MUSIC, 0);
            int maximumVolumeAlarm = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_ALARM, 0);
            int maximumVolumeSystem = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_SYSTEM, 0);
            int maximumVolumeVoiceCall = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_VOICE_CALL, 0);
            int maximumVolumeDTFM = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_DTMF, 0);
            int maximumVolumeAccessibility = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_ACCESSIBILITY, 0);
            int maximumVolumeBluetoothSCO = sharedPreferences.getInt(PREF_MAXIMUM_VOLUME_BLUETOOTH_SCO, 0);

            cursorImportDB = db.rawQuery("SELECT " +
                    DatabaseHandler.KEY_ID + ","+
                    DatabaseHandler.KEY_VOLUME_RINGTONE + ","+
                    DatabaseHandler.KEY_VOLUME_NOTIFICATION + ","+
                    DatabaseHandler.KEY_VOLUME_MEDIA + ","+
                    DatabaseHandler.KEY_VOLUME_ALARM + ","+
                    DatabaseHandler.KEY_VOLUME_SYSTEM + ","+
                    DatabaseHandler.KEY_VOLUME_VOICE + ","+
                    DatabaseHandler.KEY_VOLUME_DTMF + ","+
                    DatabaseHandler.KEY_VOLUME_ACCESSIBILITY + ","+
                    DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO +
                    " FROM " + DatabaseHandler.TABLE_PROFILES, null);

            AudioManager audioManager = (AudioManager) instance.context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                // these values are saved during export of PPP data

                if (cursorImportDB.moveToFirst()) {
                    do {

                        long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));

                        ContentValues values = new ContentValues();

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_RINGTONE, values,
                                audioManager, AudioManager.STREAM_RING, maximumVolumeRing);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_NOTIFICATION, values,
                                audioManager, AudioManager.STREAM_NOTIFICATION, maximumVolumeNotification);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_MEDIA, values,
                                audioManager, AudioManager.STREAM_MUSIC, maximumVolumeMusic);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_ALARM, values,
                                audioManager, AudioManager.STREAM_ALARM, maximumVolumeAlarm);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_SYSTEM, values,
                                audioManager, AudioManager.STREAM_SYSTEM, maximumVolumeSystem);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_VOICE, values,
                                audioManager, AudioManager.STREAM_VOICE_CALL, maximumVolumeVoiceCall);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_DTMF, values,
                                audioManager, AudioManager.STREAM_DTMF, maximumVolumeDTFM);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_ACCESSIBILITY, values,
                                audioManager, AudioManager.STREAM_ACCESSIBILITY, maximumVolumeAccessibility);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO, values,
                                audioManager, AudioManager.STREAM_BLUETOOTH_SCO, maximumVolumeBluetoothSCO);

                        // updating row
                        //noinspection SizeReplaceableByIsEmpty
                        if (values.size() > 0)
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                    } while (cursorImportDB.moveToNext());
                }
            }
            cursorImportDB.close();

            cursorImportDB = db.rawQuery("SELECT " +
                    DatabaseHandler.KEY_E_ID + ","+
                    DatabaseHandler.KEY_E_VOLUMES_RINGTONE_FROM + ","+
                    DatabaseHandler.KEY_E_VOLUMES_RINGTONE_TO + ","+
                    DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_FROM + ","+
                    DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_TO + ","+
                    DatabaseHandler.KEY_E_VOLUMES_MEDIA_FROM + ","+
                    DatabaseHandler.KEY_E_VOLUMES_MEDIA_TO + ","+
                    DatabaseHandler.KEY_E_VOLUMES_ALARM_FROM + ","+
                    DatabaseHandler.KEY_E_VOLUMES_ALARM_TO + ","+
                    DatabaseHandler.KEY_E_VOLUMES_SYSTEM_FROM + ","+
                    DatabaseHandler.KEY_E_VOLUMES_SYSTEM_TO + ","+
                    DatabaseHandler.KEY_E_VOLUMES_VOICE_FROM + ","+
                    DatabaseHandler.KEY_E_VOLUMES_VOICE_TO + ","+
                    DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_FROM + ","+
                    DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_TO + ","+
                    DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_FROM + ","+
                    DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_TO +
                    " FROM " + DatabaseHandler.TABLE_EVENTS, null);

            if (audioManager != null) {
                // these values are saved during export of PPP data

                if (cursorImportDB.moveToFirst()) {
                    do {

                        long eventId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));

                        ContentValues values = new ContentValues();

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_RINGTONE_FROM, values,
                                audioManager, AudioManager.STREAM_RING, maximumVolumeRing);
                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_RINGTONE_TO, values,
                                audioManager, AudioManager.STREAM_RING, maximumVolumeRing);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_FROM, values,
                                audioManager, AudioManager.STREAM_NOTIFICATION, maximumVolumeNotification);
                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_TO, values,
                                audioManager, AudioManager.STREAM_NOTIFICATION, maximumVolumeNotification);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_MEDIA_FROM, values,
                                audioManager, AudioManager.STREAM_MUSIC, maximumVolumeMusic);
                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_MEDIA_TO, values,
                                audioManager, AudioManager.STREAM_MUSIC, maximumVolumeMusic);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_ALARM_FROM, values,
                                audioManager, AudioManager.STREAM_ALARM, maximumVolumeAlarm);
                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_ALARM_TO, values,
                                audioManager, AudioManager.STREAM_ALARM, maximumVolumeAlarm);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_SYSTEM_FROM, values,
                                audioManager, AudioManager.STREAM_SYSTEM, maximumVolumeSystem);
                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_SYSTEM_TO, values,
                                audioManager, AudioManager.STREAM_SYSTEM, maximumVolumeSystem);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_VOICE_FROM, values,
                                audioManager, AudioManager.STREAM_VOICE_CALL, maximumVolumeVoiceCall);
                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_VOICE_TO, values,
                                audioManager, AudioManager.STREAM_VOICE_CALL, maximumVolumeVoiceCall);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_FROM, values,
                                audioManager, AudioManager.STREAM_ACCESSIBILITY, maximumVolumeAccessibility);
                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_TO, values,
                                audioManager, AudioManager.STREAM_ACCESSIBILITY, maximumVolumeAccessibility);

                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_FROM, values,
                                audioManager, AudioManager.STREAM_BLUETOOTH_SCO, maximumVolumeBluetoothSCO);
                        recalculateVolume(cursorImportDB, DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_TO, values,
                                audioManager, AudioManager.STREAM_BLUETOOTH_SCO, maximumVolumeBluetoothSCO);

                        // updating row
                        //noinspection SizeReplaceableByIsEmpty
                        if (values.size() > 0)
                            db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                    new String[]{String.valueOf(eventId)});
                    } while (cursorImportDB.moveToNext());
                }
            }

        } finally {
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void afterImportDbNonGrantedUri(DatabaseHandler instance, SQLiteDatabase db) {
        Cursor cursorImportDB = null;

        // set profile parameters to "Not used" for non-granted Uri premissions
        try {
            cursorImportDB = db.rawQuery("SELECT " +
                    DatabaseHandler.KEY_ID + ","+
                    DatabaseHandler.KEY_ICON + "," +
                    DatabaseHandler.KEY_SOUND_RINGTONE + "," +
                    DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + "," +
                    DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + "," +
                    DatabaseHandler.KEY_SOUND_NOTIFICATION + "," +
                    DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + "," +
                    DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + "," +
                    DatabaseHandler.KEY_SOUND_ALARM + "," +
                    DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE + "," +
                    DatabaseHandler.KEY_DEVICE_WALLPAPER + "," +
                    DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER + "," +
                    DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND + "," +
                    DatabaseHandler.KEY_DEVICE_WALLPAPER_LOCKSCREEN +
                    " FROM " + DatabaseHandler.TABLE_PROFILES, null);

            ContentResolver contentResolver = instance.context.getContentResolver();

            if (cursorImportDB.moveToFirst()) {
                do {
                    long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));

                    ContentValues values = new ContentValues();

                    String icon = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON));
                    if (!ProfileStatic.getIsIconResourceID(icon)) {
                        String iconIdentifier = ProfileStatic.getIconIdentifier(icon);
                        boolean isGranted = false;
                        Uri uri = Uri.parse(iconIdentifier);
                        if (uri != null) {
                            try {
                                instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                isGranted = true;
                            } catch (Exception e) {
                                //isGranted = false;
                            }
                        }
                        if (!isGranted) {
                            values.clear();
                            values.put(DatabaseHandler.KEY_ICON, Profile.defaultValuesString.get(Profile.PREF_PROFILE_ICON));
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                        }
                    }

                    String tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE));
                    String[] splits = tone.split(StringConstants.STR_SPLIT_REGEX);
                    String ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(instance.context, ringtone, RingtoneManager.TYPE_RINGTONE);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE));
                                values.put(DatabaseHandler.KEY_SOUND_RINGTONE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE));
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_SIM1));
                    splits = tone.split(StringConstants.STR_SPLIT_REGEX);
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(instance.context, ringtone, RingtoneManager.TYPE_RINGTONE);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1));
                                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_SIM1, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1));
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_SIM2));
                    splits = tone.split(StringConstants.STR_SPLIT_REGEX);
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(instance.context, ringtone, RingtoneManager.TYPE_RINGTONE);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2));
                                values.put(DatabaseHandler.KEY_SOUND_RINGTONE_SIM2, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2));
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION));
                    splits = tone.split(StringConstants.STR_SPLIT_REGEX);
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(instance.context, ringtone, RingtoneManager.TYPE_NOTIFICATION);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE));
                                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION));
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1));
                    splits = tone.split(StringConstants.STR_SPLIT_REGEX);
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(instance.context, ringtone, RingtoneManager.TYPE_NOTIFICATION);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1));
                                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1));
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2));
                    splits = tone.split(StringConstants.STR_SPLIT_REGEX);
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(instance.context, ringtone, RingtoneManager.TYPE_NOTIFICATION);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2));
                                values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2));
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM));
                    splits = tone.split(StringConstants.STR_SPLIT_REGEX);
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(instance.context, ringtone, RingtoneManager.TYPE_ALARM);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_SOUND_ALARM_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE));
                                values.put(DatabaseHandler.KEY_SOUND_ALARM, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM));
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }

                    String wallpaper = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER));
                    if (!wallpaper.isEmpty() && !wallpaper.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER))) {
                        boolean isGranted = false;
                        Uri uri = Uri.parse(wallpaper);
                        if (uri != null) {
                            try {
                                instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                isGranted = true;
                            } catch (Exception e) {
                                //isGranted = false;
                            }
                        }
                        if (!isGranted) {
                            values.clear();
                            String wallpaperChange = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE));
                            if (wallpaperChange.equals("1"))
                                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));
                            values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER));
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                        }
                    }
                    wallpaper = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_LOCKSCREEN));
                    if (!wallpaper.isEmpty() && !wallpaper.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN))) {
                        boolean isGranted = false;
                        Uri uri = Uri.parse(wallpaper);
                        if (uri != null) {
                            try {
                                instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                isGranted = true;
                            } catch (Exception e) {
                                //isGranted = false;
                            }
                        }
                        if (!isGranted) {
                            values.clear();
                            String wallpaperChange = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE));
                            if (wallpaperChange.equals("1"))
                                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));
                            values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_LOCKSCREEN, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN));
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                        }
                    }

                    String wallpaperFolder = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER));
                    if (!wallpaperFolder.isEmpty() && !wallpaperFolder.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER))) {
                        boolean isGranted = false;
                        Uri uri = Uri.parse(wallpaperFolder);
                        if (uri != null) {
                            try {
                                instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                /*
                                instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                // persistent permissions
                                final int takeFlags = //data.getFlags() &
                                        (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                instance.context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                */
                                isGranted = true;
                            } catch (Exception e) {
                                //isGranted = false;
                            }
                        }
                        if (!isGranted) {
                            values.clear();
                            String wallpaperChange = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE));
                            if (wallpaperChange.equals("3"))
                                values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));
                            values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER));
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                        }
                    }

                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND));
                    if (!tone.isEmpty()) {
                        if (tone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND));
                                db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }

                } while (cursorImportDB.moveToNext());
            }
            cursorImportDB.close();
        } finally {
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void afterImportDbContacts(DatabaseHandler instance, SQLiteDatabase db) {
        // convert contacts data to new format

        Cursor cursorImportDB = null;

        boolean contactsConverted = false;
//        Log.e("DatabaseHandlerImportExport.afterImportDb", "(1) convert contacts data to new format");
//        PPApplicationStatic.logE("[CONTACTS_CACHE] DatabaseHandlerImportExport.afterImportDbNonGrantedUri", "(1) PPApplicationStatic.getContactsCache()");
        ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
//        Log.e("DatabaseHandlerImportExport.afterImportDb", "(1.1) convert contacts data to new format");
        if (contactsCache == null) {
//            Log.e("DatabaseHandlerImportExport.afterImportDb", "(1.2) convert contacts data to new format");
//            PPApplicationStatic.logE("[CONTACTS_CACHE] DatabaseHandlerImportExport.afterImportDbNonGrantedUri", "PPApplicationStatic.createContactsCache()");
            PPApplicationStatic.createContactsCache(instance.context, false, false);
//            PPApplicationStatic.logE("[CONTACTS_CACHE] DatabaseHandlerImportExport.afterImportDbNonGrantedUri", "PPApplicationStatic.createContactGroupsCache()");
            PPApplicationStatic.createContactGroupsCache(instance.context, false/*, true*//*, true*/);
//            Log.e("DatabaseHandlerImportExport.afterImportDb", "(1.3) convert contacts data to new format");
//            PPApplicationStatic.logE("[CONTACTS_CACHE] DatabaseHandlerImportExport.afterImportDbNonGrantedUri", "(2) PPApplicationStatic.getContactsCache()");
            contactsCache = PPApplicationStatic.getContactsCache();
//            Log.e("DatabaseHandlerImportExport.afterImportDb", "(1.4) convert contacts data to new format");
        }
//        Log.e("DatabaseHandlerImportExport.afterImportDb", "(2) convert contacts data to new format");
//        PPApplicationStatic.logE("[CONTACTS_CACHE] DatabaseHandlerImportExport.afterImportDbNonGrantedUri", "contactsCache.getList()");
        List<Contact> contactList = contactsCache.getList(/*withoutNumbers*/);
//        Log.e("DatabaseHandlerImportExport.afterImportDb", "(3) convert contacts data to new format");
        if (contactList != null) {
            try {
                cursorImportDB = db.rawQuery("SELECT " +
                        DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_CALL_CONTACTS + "," +
                        DatabaseHandler.KEY_E_SMS_CONTACTS + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS +
                        " FROM " + DatabaseHandler.TABLE_EVENTS, null);

                if (cursorImportDB.moveToFirst()) {
                    do {
                        long eventId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "event="+eventId);

                        boolean dataChanged = false;
                        ContentValues values = new ContentValues();

                        String callContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTACTS));
                        String smsContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_CONTACTS));
                        String notificationContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS));

                        if (!callContacts.isEmpty()) {
                            String[] splits = callContacts.split(StringConstants.STR_SPLIT_REGEX);
                            String _split = splits[0];
                            String[] _splits2 = _split.split("#");
                            boolean oldData = false;
                            try {
                                //noinspection unused
                                long l = Long.parseLong(_splits2[0]);
                                oldData = true;
                            } catch (Exception ignored) {}
                            if (oldData) {
                                StringBuilder newContacts = new StringBuilder();
                                for (String split : splits) {
                                    String[] splits2 = split.split("#");
                                    long contactId = Long.parseLong(splits2[0]);
                                    long phoneId = Long.parseLong(splits2[1]);

                                    boolean found = false;
                                    for (Contact contact : contactList) {
                                        if (phoneId != 0) {
                                            if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                                                found = true;
                                        } else {
                                            if (contact.contactId == contactId)
                                                found = true;
                                        }
                                        if (found) {
                                            if (newContacts.length() > 0)
                                                newContacts.append("|");
                                            newContacts
                                                    .append(contact.name)
                                                    .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                    .append(contact.phoneNumber)
                                                    .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                    .append(contact.accountType);
                                            break;
                                        }
                                    }
                                }
                                callContacts = newContacts.toString();
                                values.put(DatabaseHandler.KEY_E_CALL_CONTACTS, callContacts);
                                dataChanged = true;
                            }
                        }

                        if (!smsContacts.isEmpty()) {
                            String[] splits = smsContacts.split(StringConstants.STR_SPLIT_REGEX);
                            String _split = splits[0];
                            String[] _splits2 = _split.split("#");
                            boolean oldData = false;
                            try {
                                //noinspection unused
                                long l = Long.parseLong(_splits2[0]);
                                oldData = true;
                            } catch (Exception ignored) {
                            }
                            if (oldData) {
                                StringBuilder newContacts = new StringBuilder();
                                for (String split : splits) {
                                    String[] splits2 = split.split("#");
                                    if (splits2.length != 3) {
                                        // old data
                                        splits2 = split.split("#");
                                        if (splits2.length != 2)
                                            continue;
                                        long contactId = Long.parseLong(splits2[0]);
                                        long phoneId = Long.parseLong(splits2[1]);

                                        boolean found = false;
                                        for (Contact contact : contactList) {
                                            if (phoneId != 0) {
                                                if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                                                    found = true;
                                            } else {
                                                if (contact.contactId == contactId)
                                                    found = true;
                                            }
                                            if (found) {
                                                if (newContacts.length() > 0)
                                                    newContacts.append("|");
                                                newContacts
                                                        .append(contact.name)
                                                        .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                        .append(contact.phoneNumber)
                                                        .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                        .append(contact.accountType);
                                                break;
                                            }
                                        }
                                    }
                                }
                                smsContacts = newContacts.toString();
                                values.put(DatabaseHandler.KEY_E_SMS_CONTACTS, smsContacts);
                                dataChanged = true;
                            }
                        }

                        if (!notificationContacts.isEmpty()) {
                            String[] splits = notificationContacts.split(StringConstants.STR_SPLIT_REGEX);
                            String _split = splits[0];
                            String[] _splits2 = _split.split("#");
                            boolean oldData = false;
                            try {
                                //noinspection unused
                                long l = Long.parseLong(_splits2[0]);
                                oldData = true;
                            } catch (Exception ignored) {
                            }
                            if (oldData) {
                                StringBuilder newContacts = new StringBuilder();
                                for (String split : splits) {
                                    String[] splits2 = split.split("#");
                                    if (splits2.length != 3) {
                                        // old data
                                        splits2 = split.split("#");
                                        if (splits2.length != 2)
                                            continue;
                                        long contactId = Long.parseLong(splits2[0]);
                                        long phoneId = Long.parseLong(splits2[1]);

                                        boolean found = false;
                                        for (Contact contact : contactList) {
                                            if (phoneId != 0) {
                                                if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
                                                    found = true;
                                            } else {
                                                if (contact.contactId == contactId)
                                                    found = true;
                                            }
                                            if (found) {
                                                if (newContacts.length() > 0)
                                                    newContacts.append("|");
                                                newContacts
                                                        .append(contact.name)
                                                        .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                        .append(contact.phoneNumber)
                                                        .append(StringConstants.STR_SPLIT_CONTACTS_REGEX)
                                                        .append(contact.accountType);
                                                break;
                                            }
                                        }
                                    }
                                }
                                notificationContacts = newContacts.toString();
                                values.put(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS, notificationContacts);
                                dataChanged = true;
                            }
                        }

                        if (dataChanged) {
                            db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(eventId)});
                            contactsConverted = true;
                        }

                    } while (cursorImportDB.moveToNext());
                }
                cursorImportDB.close();
            } finally {
                if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                    cursorImportDB.close();
            }
            contactList.clear();
        }

        if (!contactsConverted) {
            // decript contacts
            boolean applicationContactsInBackupEncripted =
                    ApplicationPreferences.getSharedPreferences(instance.context)
                            .getBoolean(ApplicationPreferences.PREF_APPLICATION_CONTACTS_IN_BACKUP_ENCRIPTED,
                                    false);
//        Log.e("DatabaseHandlerImportExport.afterImportDb", "applicationContactsInBackupEncripted="+applicationContactsInBackupEncripted);
            if (applicationContactsInBackupEncripted) {
                Encryption encryption = Encryption.getDefault(BuildConfig.encrypt_contacts_key, BuildConfig.encrypt_contacts_salt, new byte[16]);

                try {
                    cursorImportDB = db.rawQuery("SELECT " +
                            DatabaseHandler.KEY_E_ID + "," +
                            DatabaseHandler.KEY_E_CALL_CONTACTS + "," +
                            DatabaseHandler.KEY_E_SMS_CONTACTS + "," +
                            DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS + "," +
                            DatabaseHandler.KEY_E_CALL_SCREENING_CONTACTS +
                            " FROM " + DatabaseHandler.TABLE_EVENTS, null);

                    if (cursorImportDB.moveToFirst()) {
                        do {
                            long eventId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));

                            String callContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTACTS));
                            String smsContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_CONTACTS));
                            String notificationContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS));
                            String callScreeningContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_SCREENING_CONTACTS));

                            String decryptedCallContacts;
                            try {
                                decryptedCallContacts = encryption.decryptOrNull(callContacts);
                            } catch (Exception e) {
                                decryptedCallContacts = "";
                            }
                            String decryptedSMSContacts;
                            try {
                                decryptedSMSContacts = encryption.decryptOrNull(smsContacts);
                            } catch (Exception e) {
                                decryptedSMSContacts = "";
                            }
                            String decryptedNotificationContacts;
                            try {
                                decryptedNotificationContacts = encryption.decryptOrNull(notificationContacts);
                            } catch (Exception e) {
                                decryptedNotificationContacts = "";
                            }
                            String decryptedCallScreeningContacts;
                            try {
                                decryptedCallScreeningContacts = encryption.decryptOrNull(callScreeningContacts);
                            } catch (Exception e) {
                                decryptedCallScreeningContacts = "";
                            }
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedCallContacts="+decryptedCallContacts);
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedSMSContacts="+decryptedSMSContacts);
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedNotificationContacts="+decryptedNotificationContacts);
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedNotificationContacts="+decryptedCallScreeningContacts);
                            if (decryptedCallContacts == null) decryptedCallContacts = "";
                            if (decryptedSMSContacts == null) decryptedSMSContacts = "";
                            if (decryptedNotificationContacts == null) decryptedNotificationContacts = "";
                            if (decryptedCallScreeningContacts == null) decryptedCallScreeningContacts = "";

                            ContentValues values = new ContentValues();
                            values.put(DatabaseHandler.KEY_E_CALL_CONTACTS, decryptedCallContacts);
                            values.put(DatabaseHandler.KEY_E_SMS_CONTACTS, decryptedSMSContacts);
                            values.put(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS, decryptedNotificationContacts);
                            values.put(DatabaseHandler.KEY_E_CALL_SCREENING_CONTACTS, decryptedCallScreeningContacts);

                            db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                    new String[]{String.valueOf(eventId)});

                        } while (cursorImportDB.moveToNext());
                    }
                    cursorImportDB.close();
                } finally {
                    if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                        cursorImportDB.close();
                }

                /*
                try {
                    cursorImportDB = db.rawQuery("SELECT " +
                            DatabaseHandler.KEY_ID + "," +
                            DatabaseHandler.KEY_PHONE_CALLS_CONTACTS +
                            " FROM " + DatabaseHandler.TABLE_PROFILES, null);

                    if (cursorImportDB.moveToFirst()) {
                        do {
                            long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));

                            String phoneCallsContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS));

                            String decryptedPhoneCallsContacts;
                            try {
                                decryptedPhoneCallsContacts = encryption.decryptOrNull(phoneCallsContacts);
                            } catch (Exception e) {
                                decryptedPhoneCallsContacts = "";
                            }
                            //Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedPhoneCallsContacts="+decryptedPhoneCallsContacts);
                            if (decryptedPhoneCallsContacts == null) decryptedPhoneCallsContacts = "";

                            ContentValues values = new ContentValues();
                            values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS, decryptedPhoneCallsContacts);

                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});

                        } while (cursorImportDB.moveToNext());
                    }
                    cursorImportDB.close();
                } finally {
                    if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                        cursorImportDB.close();
                }
                */

                try {
                    cursorImportDB = db.rawQuery("SELECT " +
                            DatabaseHandler.KEY_ID + "," +
                            DatabaseHandler.KEY_SEND_SMS_CONTACTS + "," +
                            DatabaseHandler.KEY_CLEAR_NOTIFICATION_CONTACTS +
                            " FROM " + DatabaseHandler.TABLE_PROFILES, null);

                    if (cursorImportDB.moveToFirst()) {
                        do {
                            long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));

                            String sendSMSContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SEND_SMS_CONTACTS));
                            String clearNotificationContacts = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_CLEAR_NOTIFICATION_CONTACTS));

                            String decryptedSendSMSContacts;
                            try {
                                decryptedSendSMSContacts = encryption.decryptOrNull(sendSMSContacts);
                            } catch (Exception e) {
                                decryptedSendSMSContacts = "";
                            }
                            String decryptedClearNotificationContacts;
                            try {
                                decryptedClearNotificationContacts = encryption.decryptOrNull(clearNotificationContacts);
                            } catch (Exception e) {
                                decryptedClearNotificationContacts = "";
                            }
                            //Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedSendSMSContacts="+decryptedSendSMSContacts);
                            //Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedClearNotificationContacts="+decryptedClearNotificationContacts);
                            if (decryptedSendSMSContacts == null) decryptedSendSMSContacts = "";
                            if (decryptedClearNotificationContacts == null) decryptedClearNotificationContacts = "";

                            ContentValues values = new ContentValues();
                            values.put(DatabaseHandler.KEY_SEND_SMS_CONTACTS, decryptedSendSMSContacts);
                            values.put(DatabaseHandler.KEY_CLEAR_NOTIFICATION_CONTACTS, decryptedClearNotificationContacts);

                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});

                        } while (cursorImportDB.moveToNext());
                    }
                    cursorImportDB.close();
                } finally {
                    if (/*(cursorImportDB != null) &&*/ (!cursorImportDB.isClosed()))
                        cursorImportDB.close();
                }

            }
        }
    }

    static private void afterImportDbDecriptPrivateData(DatabaseHandler instance, SQLiteDatabase db) {
        Cursor cursorImportDB = null;

        boolean applicationLocationsInBackupEncripted =
                ApplicationPreferences.getSharedPreferences(instance.context)
                        .getBoolean(ApplicationPreferences.PREF_APPLICATION_LOCATIONS_IN_BACKUP_ENCRIPTED,
                                false);
//        Log.e("DatabaseHandlerImportExport.afterImportDb", "applicationLocationsInBackupEncripted="+applicationLocationsInBackupEncripted);
        if (applicationLocationsInBackupEncripted) {
            Encryption encryption = Encryption.getDefault(BuildConfig.encrypt_contacts_key, BuildConfig.encrypt_contacts_salt, new byte[16]);

            try {
                cursorImportDB = db.rawQuery("SELECT " +
                        DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_WIFI_SSID + "," +
                        DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME +
                        " FROM " + DatabaseHandler.TABLE_EVENTS, null);

                if (cursorImportDB.moveToFirst()) {
                    do {
                        long eventId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));

                        String wifiSSIDs = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_WIFI_SSID));
                        String bluetoothNames = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME));

                        String decryptedWifiSSIDs;
                        try {
                            decryptedWifiSSIDs = encryption.decryptOrNull(wifiSSIDs);
                        } catch (Exception e) {
                            decryptedWifiSSIDs = "";
                        }
                        String decryptedBluetoothNames;
                        try {
                            decryptedBluetoothNames = encryption.decryptOrNull(bluetoothNames);
                        } catch (Exception e) {
                            decryptedBluetoothNames = "";
                        }
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedWifiSSIDs="+decryptedWifiSSIDs);
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedBluetoothNames="+decryptedBluetoothNames);
                        if (decryptedWifiSSIDs == null) decryptedWifiSSIDs="";
                        if (decryptedBluetoothNames == null) decryptedBluetoothNames="";

                        ContentValues values = new ContentValues();
                        values.put(DatabaseHandler.KEY_E_WIFI_SSID, decryptedWifiSSIDs);
                        values.put(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME, decryptedBluetoothNames);

                        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                new String[]{String.valueOf(eventId)});

                    } while (cursorImportDB.moveToNext());
                }
                cursorImportDB.close();
            } finally {
                if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                    cursorImportDB.close();
            }
            cursorImportDB = null;
            try {
                cursorImportDB = db.rawQuery("SELECT " +
                        DatabaseHandler.KEY_G_ID + "," +
                        DatabaseHandler.KEY_G_NAME + "," +
                        DatabaseHandler.KEY_G_LATITUDE_T + "," +
                        DatabaseHandler.KEY_G_LONGITUDE_T +
                        " FROM " + DatabaseHandler.TABLE_GEOFENCES, null);

                if (cursorImportDB.moveToFirst()) {
                    do {
                        long geofenceId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_G_ID));

                        String geofenceName = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_G_NAME));
                        String latitudeT = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LATITUDE_T));
                        String longitudeT = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LONGITUDE_T));

                        String decryptedGeofenceName;
                        try {
                            decryptedGeofenceName = encryption.decryptOrNull(geofenceName);
                        } catch (Exception e) {
                            decryptedGeofenceName = "";
                        }
                        String decryptedLatitude;
                        try {
                            decryptedLatitude = encryption.decryptOrNull(latitudeT);
                        } catch (Exception e) {
                            decryptedLatitude = "";
                        }
                        String decryptedLongitude;
                        try {
                            decryptedLongitude = encryption.decryptOrNull(longitudeT);
                        } catch (Exception e) {
                            decryptedLongitude = "";
                        }
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedGeofenceName="+decryptedGeofenceName);
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedLatitude="+decryptedLatitude);
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedLongitude="+decryptedLongitude);
                        if (decryptedGeofenceName == null) decryptedGeofenceName="";
                        if (decryptedLatitude == null) decryptedLatitude="";
                        if (decryptedLongitude == null) decryptedLongitude="";

                        ContentValues values = new ContentValues();
                        values.put(DatabaseHandler.KEY_G_NAME, decryptedGeofenceName);
                        double latitude;
                        try {
                            latitude = Double.parseDouble(decryptedLatitude);
                        } catch (Exception e) {
                            latitude = 0;
                        }
                        values.put(DatabaseHandler.KEY_G_LATITUDE, latitude);
                        double longitude;
                        try {
                            longitude = Double.parseDouble(decryptedLongitude);
                        } catch (Exception e) {
                            longitude = 0;
                        }
                        values.put(DatabaseHandler.KEY_G_LONGITUDE, longitude);

                        db.update(DatabaseHandler.TABLE_GEOFENCES, values, DatabaseHandler.KEY_G_ID + " = ?",
                                new String[]{String.valueOf(geofenceId)});

                    } while (cursorImportDB.moveToNext());
                }
                cursorImportDB.close();
            } finally {
                if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                    cursorImportDB.close();
            }
            cursorImportDB = null;
            try {
                cursorImportDB = db.rawQuery("SELECT " +
                        DatabaseHandler.KEY_MC_ID + "," +
                        DatabaseHandler.KEY_MC_NAME + "," +
                        DatabaseHandler.KEY_MC_CELL_ID_T +
                        " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS, null);

                if (cursorImportDB.moveToFirst()) {
                    do {
                        long rCellId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_ID));

                        String cellName = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_NAME));
                        String cellIdT = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_CELL_ID_T));

                        String decryptedCellName;
                        try {
                            decryptedCellName = encryption.decryptOrNull(cellName);
                        } catch (Exception e) {
                            decryptedCellName = "";
                        }
                        String decryptedCellId;
                        try {
                            decryptedCellId = encryption.decryptOrNull(cellIdT);
                        } catch (Exception e) {
                            decryptedCellId = "";
                        }
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedCellName="+decryptedCellName);
//                        Log.e("DatabaseHandlerImportExport.afterImportDb", "decryptedCellId="+decryptedCellId);
                        if (decryptedCellName == null) decryptedCellName="";
                        if (decryptedCellId == null) decryptedCellId="";

                        ContentValues values = new ContentValues();
                        values.put(DatabaseHandler.KEY_MC_NAME, decryptedCellName);
                        int cellId;
                        try {
                            cellId = Integer.parseInt(decryptedCellId);
                        } catch (Exception e) {
                            cellId = 0;
                        }
                        values.put(DatabaseHandler.KEY_MC_CELL_ID, cellId);

                        db.update(DatabaseHandler.TABLE_MOBILE_CELLS, values, DatabaseHandler.KEY_MC_ID + " = ?",
                                new String[]{String.valueOf(rCellId)});

                    } while (cursorImportDB.moveToNext());
                }
                cursorImportDB.close();
            } finally {
                if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                    cursorImportDB.close();
            }
        }
    }

    static private void recalculateVibrationIntensity(Cursor cursorImportDB, String vibrationIntensityField, ContentValues values,
                                                      int minimumVibrationIntensityFromPref, int maximumVibrationIntensityFromPref,
                                                      int minimumVibrationIntensity, int maximumVibrationIntensity) {
        try {
            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(vibrationIntensityField));
            if (value != null) {
                String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
                int vibrationIntensity = Integer.parseInt(splits[0]);
                if (vibrationIntensity > 0) { // default value -1
                    float fVibrationIntensity = vibrationIntensity;

                    // get percentage of value from imported data
                    float percentage;
                    if ((maximumVibrationIntensityFromPref - minimumVibrationIntensityFromPref) > 0)
                        percentage = fVibrationIntensity / (maximumVibrationIntensityFromPref - minimumVibrationIntensityFromPref) * 100f;
                    else
                        percentage = fVibrationIntensity / (maximumVibrationIntensity - minimumVibrationIntensity) * 100f;
                    if (percentage > 100f)
                        percentage = 100f;

                    // get value from percentage for actual system max volume
                    fVibrationIntensity = (maximumVibrationIntensity - minimumVibrationIntensity) / 100f * percentage;
                    vibrationIntensity = Math.round(fVibrationIntensity);

                    if (splits.length == 3)
                        values.put(vibrationIntensityField, vibrationIntensity + "|" + splits[1] + "|" + splits[2]);
                    else
                        values.put(vibrationIntensityField, vibrationIntensity + "|" + splits[1]);
                }
            }
        } catch (IllegalArgumentException e) {
            // java.lang.IllegalArgumentException: Bad stream type X
            //PPApplicationStatic.recordException(e);
        } catch (Exception e) {
            //Log.e("DatabaseHandlerImportExport.afterImportDb", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static private void afterImportDbVibrationIntensity(DatabaseHandler instance, SQLiteDatabase db) {
        Cursor cursorImportDB = null;

        // update vibration intensity by device min, max value
        try {
            // these shared preferences are put during export of data, values are from:
            // - VibrationIntensityPreference.getMinValue()
            // - VibrationIntensityPreference.getMaxValue()
            // for import, these data are values from source of imported data (may be from another device)
            SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(instance.context);
            int maximumRingingVibrationIntensityFromSharedPref = sharedPreferences.getInt(PREF_MAXIMUM_VIBRATION_INTENSITY_RINGING, 0);
            int maximumNotificationsVibrationIntensityFromSharedPref = sharedPreferences.getInt(PREF_MAXIMUM_VIBRATION_INTENSITY_NOTIFICATION, 0);
            int maximumTouchInteractionVibrationIntensityFromSharedPref = sharedPreferences.getInt(PREF_MAXIMUM_VIBRATION_INTENSITY_TOUCH_INTERACTION, 0);
            int minimumRingingVibrationIntensityFromSharedPref = sharedPreferences.getInt(PREF_MINIMUM_VIBRATION_INTENSITY_RINGING, 0);
            int minimumNotificaitonsVibrationIntensityFromSharedPref = sharedPreferences.getInt(PREF_MINIMUM_VIBRATION_INTENSITY_NOTIFICATION, 0);
            int minimumTouchInteractionVibrationIntensityFromSharedPref = sharedPreferences.getInt(PREF_MINIMUM_VIBRATION_INTENSITY_TOUCH_INTERACTION, 0);

            int maximumRingingVibrationIntensity = VibrationIntensityPreference.getMaxValue(VibrationIntensityPreference.RINGING_VYBRATION_INTENSITY_TYPE);
            int maximumNotificationsVibrationIntensity = VibrationIntensityPreference.getMaxValue(VibrationIntensityPreference.NOTIFICATIONS_VYBRATION_INTENSITY_TYPE);
            int maximumTouchInteractionVibrationIntensity = VibrationIntensityPreference.getMaxValue(VibrationIntensityPreference.TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE);
            int minimumRingingVibrationIntensity = VibrationIntensityPreference.getMinValue(VibrationIntensityPreference.RINGING_VYBRATION_INTENSITY_TYPE);
            int minimumNotificaitonsVibrationIntensity = VibrationIntensityPreference.getMinValue(VibrationIntensityPreference.NOTIFICATIONS_VYBRATION_INTENSITY_TYPE);
            int minimumTouchInteractionVibrationIntensity = VibrationIntensityPreference.getMinValue(VibrationIntensityPreference.TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE);

            cursorImportDB = db.rawQuery("SELECT " +
                    DatabaseHandler.KEY_ID + ","+
                    DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING + ","+
                    DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS + ","+
                    DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION +
                    " FROM " + DatabaseHandler.TABLE_PROFILES, null);

            if (cursorImportDB.moveToFirst()) {
                do {

                    long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));

                    ContentValues values = new ContentValues();

                    recalculateVibrationIntensity(cursorImportDB, DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING, values,
                            minimumRingingVibrationIntensityFromSharedPref, maximumRingingVibrationIntensityFromSharedPref,
                            minimumRingingVibrationIntensity, maximumRingingVibrationIntensity);

                    recalculateVibrationIntensity(cursorImportDB, DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS, values,
                            minimumNotificaitonsVibrationIntensityFromSharedPref, maximumNotificationsVibrationIntensityFromSharedPref,
                            minimumNotificaitonsVibrationIntensity, maximumNotificationsVibrationIntensity);

                    recalculateVibrationIntensity(cursorImportDB, DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION, values,
                            minimumTouchInteractionVibrationIntensityFromSharedPref, maximumTouchInteractionVibrationIntensityFromSharedPref,
                            minimumTouchInteractionVibrationIntensity, maximumTouchInteractionVibrationIntensity);

                    // updating row
                    //noinspection SizeReplaceableByIsEmpty
                    if (values.size() > 0)
                        db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                new String[]{String.valueOf(profileId)});
                } while (cursorImportDB.moveToNext());
            }
            cursorImportDB.close();

        } finally {
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void afterImportDb(DatabaseHandler instance, SQLiteDatabase db) {
        afterImportDbVolumes(instance, db);

        Cursor cursorImportDB = null;

        // clear dual sim parameters for device without dual sim support
        int phoneCount = 1;
            TelephonyManager telephonyManager = (TelephonyManager) instance.context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phoneCount = telephonyManager.getPhoneCount();
            }
        if (phoneCount < 2) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");

            //db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            //db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");
            //db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            //db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + "='0|0|0'");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + "=''");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + "=''");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + "=''");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + "=''");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + "=''");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + "=''");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + "=''");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + "=''");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_FOR_SIM_CARD + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_FOR_SIM_CARD + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MOBILE_CELLS_FOR_SIM_CARD + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ROAMING_FOR_SIM_CARD + "=0");
        }

        afterImportDbNonGrantedUri(instance, db);

        // set event parameters to "Not used" for non-granted Uri premissions
        try {
            cursorImportDB = db.rawQuery("SELECT " +
                    DatabaseHandler.KEY_E_ID + ","+
                    DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START + "," +
                    DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END +
                    " FROM " + DatabaseHandler.TABLE_EVENTS, null);

            ContentResolver contentResolver = instance.context.getContentResolver();

            if (cursorImportDB.moveToFirst()) {
                do {
                    long eventId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));

                    ContentValues values = new ContentValues();

                    String tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START));
                    if (!tone.isEmpty()) {
                        if (tone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START, "");
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END));
                    if (!tone.isEmpty()) {
                        if (tone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END, "");
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventId)});
                            }
                        }
                    }

                } while (cursorImportDB.moveToNext());
            }
            cursorImportDB.close();
        } finally {
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }

        afterImportDbContacts(instance, db);
        afterImportDbDecriptPrivateData(instance, db);

        if (Build.VERSION.SDK_INT >= 28) {
            // for Android 9+ use Extender for lock device
            try {
                cursorImportDB = db.rawQuery("SELECT " +
                        DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_LOCK_DEVICE +
                        " FROM " + DatabaseHandler.TABLE_PROFILES, null);

                if (cursorImportDB.moveToFirst()) {
                    do {
                        long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int lockDevice = cursorImportDB.getInt(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_LOCK_DEVICE));

                        if (lockDevice == 2) {
                            ContentValues values = new ContentValues();

                            lockDevice = 3;

                            values.clear();
                            values.put(DatabaseHandler.KEY_LOCK_DEVICE, lockDevice);
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                        }

                    } while (cursorImportDB.moveToNext());
                }
                cursorImportDB.close();
            } finally {
                if (/*(cursorImportDB != null) &&*/ (!cursorImportDB.isClosed()))
                    cursorImportDB.close();
            }
        }

        afterImportDbVibrationIntensity(instance, db);

        // remove all not used non-named mobile cells
        DatabaseHandlerEvents.deleteNonNamedNotUsedCells(instance, true);

    }


    static private void importProfiles(SQLiteDatabase db, SQLiteDatabase exportedDBObj,
                                List<Long> exportedDBEventProfileIds, List<Long> importDBEventProfileIds) {

        long profileId;

        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_PROFILES);

            // cursor for profiles exportedDB
            cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_PROFILES, null);
            columnNamesExportedDB = cursorExportedDB.getColumnNames();

            // cursor for profiles of destination db
            cursorImportDB = db.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_PROFILES, null);

            if (cursorExportedDB.moveToFirst()) {
                do {
                    values.clear();
                    for (int i = 0; i < columnNamesExportedDB.length; i++) {
                        // put only when columnNamesExportedDB[i] exists in cursorImportDB
                        if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                            String value = cursorExportedDB.getString(i);
                            values.put(columnNamesExportedDB[i], value);
                        }
                    }

                    // Inserting Row do db z SQLiteOpenHelper
                    profileId = db.insert(DatabaseHandler.TABLE_PROFILES, null, values);
                    // save profile ids
                    exportedDBEventProfileIds.add(cursorExportedDB.getLong(cursorExportedDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)));
                    importDBEventProfileIds.add(profileId);

                } while (cursorExportedDB.moveToNext());
            }

            cursorExportedDB.close();
            cursorImportDB.close();
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void importEvents(SQLiteDatabase db, SQLiteDatabase exportedDBObj,
                              List<Long> exportedDBEventProfileIds, List<Long> importDBEventProfileIds) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_EVENTS);

            if (tableExists(DatabaseHandler.TABLE_EVENTS, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_EVENTS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_EVENTS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                if (columnNamesExportedDB[i].equals(DatabaseHandler.KEY_E_FK_PROFILE_START) ||
                                        columnNamesExportedDB[i].equals(DatabaseHandler.KEY_E_FK_PROFILE_END) ||
                                        columnNamesExportedDB[i].equals(DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED) ||
                                        columnNamesExportedDB[i].equals(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE)) {
                                    // imported profile has new id
                                    // map old profile id to new imported id
                                    if (columnNamesExportedDB[i].equals(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE)) {
                                        String fkProfiles = cursorExportedDB.getString(i);
                                        if (!fkProfiles.isEmpty()) {
                                            String[] splits = fkProfiles.split(StringConstants.STR_SPLIT_REGEX);
                                            StringBuilder newFkProfiles = new StringBuilder();
                                            for (String split : splits) {
                                                long fkProfile = Long.parseLong(split);
                                                int profileIdx = exportedDBEventProfileIds.indexOf(fkProfile);
                                                if (profileIdx != -1) {
                                                    if (newFkProfiles.length() > 0)
                                                        newFkProfiles.append("|");
                                                    newFkProfiles.append(importDBEventProfileIds.get(profileIdx));
                                                }
                                            }
                                            values.put(columnNamesExportedDB[i], newFkProfiles.toString());
                                        } else
                                            values.put(columnNamesExportedDB[i], "");
                                    } else {
                                        int profileIdx = exportedDBEventProfileIds.indexOf(cursorExportedDB.getLong(i));
                                        if (profileIdx != -1)
                                            values.put(columnNamesExportedDB[i], importDBEventProfileIds.get(profileIdx));
                                        else {
                                            if (columnNamesExportedDB[i].equals(DatabaseHandler.KEY_E_FK_PROFILE_END) &&
                                                    (cursorExportedDB.getLong(i) == Profile.PROFILE_NO_ACTIVATE))
                                                values.put(columnNamesExportedDB[i], Profile.PROFILE_NO_ACTIVATE);
                                            else if (columnNamesExportedDB[i].equals(DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED) &&
                                                    (cursorExportedDB.getLong(i) == Profile.PROFILE_NO_ACTIVATE))
                                                values.put(columnNamesExportedDB[i], Profile.PROFILE_NO_ACTIVATE);
                                            else
                                                values.put(columnNamesExportedDB[i], 0);
                                        }
                                    }
                                } else
                                    values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(DatabaseHandler.TABLE_EVENTS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();

            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void importActivityLog(/*String applicationDataPath, */SQLiteDatabase db, SQLiteDatabase exportedDBObj/*,
                                   List<Long> exportedDBEventProfileIds, List<Long> importDBEventProfileIds*/) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_ACTIVITY_LOG);

            if (tableExists(DatabaseHandler.TABLE_ACTIVITY_LOG, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_ACTIVITY_LOG, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_ACTIVITY_LOG, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // for non existent fields set default value
                        if (exportedDBObj.getVersion() < 2409) {
                            values.put(DatabaseHandler.KEY_AL_PROFILE_EVENT_COUNT, "1 [0]");
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(DatabaseHandler.TABLE_ACTIVITY_LOG, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void importGeofences(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_GEOFENCES);

            if (tableExists(DatabaseHandler.TABLE_GEOFENCES, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_GEOFENCES, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_GEOFENCES, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(DatabaseHandler.TABLE_GEOFENCES, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void importShortcuts(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_SHORTCUTS);

            if (tableExists(DatabaseHandler.TABLE_SHORTCUTS, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_SHORTCUTS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_SHORTCUTS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(DatabaseHandler.TABLE_SHORTCUTS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void importMobileCells(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_MOBILE_CELLS);

            if (tableExists(DatabaseHandler.TABLE_MOBILE_CELLS, exportedDBObj)) {
                // cursor for exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_MOBILE_CELLS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_MOBILE_CELLS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(DatabaseHandler.TABLE_MOBILE_CELLS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void importNFCTags(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_NFC_TAGS);

            if (tableExists(DatabaseHandler.TABLE_NFC_TAGS, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_NFC_TAGS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_NFC_TAGS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(DatabaseHandler.TABLE_NFC_TAGS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    static private void importIntents(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + DatabaseHandler.TABLE_INTENTS);

            if (tableExists(DatabaseHandler.TABLE_INTENTS, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_INTENTS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_INTENTS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(DatabaseHandler.TABLE_INTENTS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    static int importDB(DatabaseHandler instance/*String applicationDataPath*/) {
        instance.importExportLock.lock();
        try {
            int ret = DatabaseHandler.IMPORT_ERROR_BUG;
            try {
                instance.startRunningImportExport();

                List<Long> exportedDBEventProfileIds = new ArrayList<>();
                List<Long> importDBEventProfileIds = new ArrayList<>();

                // Close SQLiteOpenHelper so it will commit the created empty
                // database to internal storage
                //close();

                try {
                    //File sd = Environment.getExternalStorageDirectory();
                    File sd = instance.context.getExternalFilesDir(null);

                    //File exportedDB = new File(sd, applicationDataPath + "/" + EXPORT_DBFILENAME);
                    File exportedDB = new File(sd, DatabaseHandler.EXPORT_DBFILENAME);

                    if (exportedDB.exists()) {
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            exportedDB.setReadable(true, false);
                        } catch (Exception ee) {
                            PPApplicationStatic.recordException(ee);
                        }
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            exportedDB.setWritable(true, false);
                        } catch (Exception ee) {
                            PPApplicationStatic.recordException(ee);
                        }

                        SQLiteDatabase exportedDBObj;
                        //if (Build.VERSION.SDK_INT < 27)
                        exportedDBObj = SQLiteDatabase.openDatabase(exportedDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                        /*else {
                            SQLiteDatabase.OpenParams openParams = new SQLiteDatabase.OpenParams.Builder()
                                    .setOpenFlags(SQLiteDatabase.OPEN_READONLY)
                                    .build();
                            exportedDBObj = SQLiteDatabase.openDatabase(exportedDB, openParams);
                        }*/
                        int version;
                        //try {
                        // this will crash when PPP directory is not created by PPP :-(
                        version = exportedDBObj.getVersion();
                        //} catch (Exception ignored) {}

                        if (version <= DatabaseHandler.DATABASE_VERSION) {
                            SQLiteDatabase db = instance.getMyWritableDatabase();

                            try {
                                db.beginTransaction();

                                importProfiles(db, exportedDBObj, exportedDBEventProfileIds, importDBEventProfileIds);
                                importEvents(db, exportedDBObj, exportedDBEventProfileIds, importDBEventProfileIds);
                                importActivityLog(db, exportedDBObj);
                                importGeofences(db, exportedDBObj);
                                importShortcuts(db, exportedDBObj);
                                importMobileCells(db, exportedDBObj);
                                importNFCTags(db, exportedDBObj);
                                importIntents(db, exportedDBObj);

                                DatabaseHandlerCreateUpdateDB.updateDb(instance, db, version);

                                DatabaseHandlerCreateUpdateDB.afterUpdateDb(db);
                                afterImportDb(instance, db);

                                db.setTransactionSuccessful();

                                ret = DatabaseHandler.IMPORT_OK;
                            } finally {
                                db.endTransaction();
                                //db.close();
                            }
                        } else {
                            //    exportedDBObj.close();
                            ret = DatabaseHandler.IMPORT_ERROR_NEVER_VERSION;
                        }
                    }
                } catch (Exception e1) {
                    //Log.e("DatabaseHandlerImportExport.importDB", Log.getStackTraceString(e1));
                    //getVersion(): android.database.sqlite.SQLiteCantOpenDatabaseException: unable to open database file (Sqlite code 14), (OS error - 2:No such file or directory)
                    PPApplicationStatic.recordException(e1);
                    ret = DatabaseHandler.IMPORT_ERROR_BUG;
                }

            } catch (Exception e2) {
                //Log.e("DatabaseHandlerImportExport.importDB", Log.getStackTraceString(e2));
                //getVersion(): android.database.sqlite.SQLiteCantOpenDatabaseException: unable to open database file (Sqlite code 14), (OS error - 2:No such file or directory)
                PPApplicationStatic.recordException(e2);
            }
            return ret;
        } finally {
            instance.stopRunningImportExport();
        }
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    static int exportDB(DatabaseHandler instance,
                        boolean deleteGeofences, boolean deleteWifiSSIDs,
                        boolean deleteBluetoothNames, boolean deleteMobileCells,
                        boolean deleteCall, boolean deleteSMS, boolean deleteNotification,
                        boolean deletePhoneCalls, boolean deleteCallScreening,
                        boolean deleteClearNotifications)
    {
        instance.importExportLock.lock();
        try {
            int ret = 0;
            try {
                instance.startRunningImportExport();

                FileInputStream src = null;
                FileOutputStream dst = null;
                try {
                    try {
                        //File sd = Environment.getExternalStorageDirectory();
                        //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                        File sd = instance.context.getExternalFilesDir(null);

                        File data = Environment.getDataDirectory();

                        File dataDB = new File(data, PPApplication.DB_FILEPATH + "/" + DatabaseHandler.DATABASE_NAME);
                        //File exportedDB = new File(sd, PPApplication.EXPORT_PATH + "/" + EXPORT_DBFILENAME);
                        File exportedDB = new File(sd, DatabaseHandler.EXPORT_DBFILENAME);

                        if (dataDB.exists()) {
                            // close db
                            instance.close();

                            src = new FileInputStream(dataDB);
                            dst = new FileOutputStream(exportedDB);

                            //noinspection resource
                            FileChannel srcCh = new FileInputStream(dataDB).getChannel();
                            //noinspection resource
                            FileChannel dstCh = new FileOutputStream(exportedDB).getChannel();

                            srcCh.force(true);
                            dstCh.force(true);

                            boolean ok = false;
                            long transferredSize = dstCh.transferFrom(srcCh, 0, srcCh.size());
                            if (transferredSize == dataDB.length())
                                ok = true;

                            srcCh.close();
                            dstCh.close();

                            dst.flush();

                            src.close();
                            dst.close();

                            try {
                                //noinspection ResultOfMethodCallIgnored
                                exportedDB.setReadable(true, false);
                            } catch (Exception ee) {
                                PPApplicationStatic.recordException(ee);
                            }
                            try {
                                //noinspection ResultOfMethodCallIgnored
                                exportedDB.setWritable(true, false);
                            } catch (Exception ee) {
                                PPApplicationStatic.recordException(ee);
                            }

                            if (ok) {
                                SQLiteDatabase exportedDBObj = null;
                                try {
                                    try {
                                        exportedDBObj = SQLiteDatabase.openDatabase(exportedDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

                                        // encript contacts
                                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(instance.context);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_CONTACTS_IN_BACKUP_ENCRIPTED, true);
                                        editor.apply();

                                        Encryption encryption = Encryption.getDefault(BuildConfig.encrypt_contacts_key, BuildConfig.encrypt_contacts_salt, new byte[16]);

                                        Cursor cursorExportDB = null;
                                        try {
                                            cursorExportDB = exportedDBObj.rawQuery("SELECT " +
                                                    DatabaseHandler.KEY_E_ID + "," +
                                                    DatabaseHandler.KEY_E_CALL_CONTACTS + "," +
                                                    DatabaseHandler.KEY_E_SMS_CONTACTS + "," +
                                                    DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS + "," +
                                                    DatabaseHandler.KEY_E_CALL_SCREENING_CONTACTS +
                                                    " FROM " + DatabaseHandler.TABLE_EVENTS, null);

                                            if (cursorExportDB.moveToFirst()) {
                                                do {
                                                    long eventId = cursorExportDB.getLong(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));

                                                    String callContacts = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTACTS));
                                                    String smsContacts = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_CONTACTS));
                                                    String notificationContacts = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS));
                                                    String callScreeningContacts = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_SCREENING_CONTACTS));

                                                    String encryptedCallContacts = encryption.encryptOrNull(callContacts);
                                                    String encryptedSMSContacts = encryption.encryptOrNull(smsContacts);
                                                    String encryptedNotificationContacts = encryption.encryptOrNull(notificationContacts);
                                                    String encryptedCallScreeningContacts = encryption.encryptOrNull(callScreeningContacts);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedCallContacts="+encryptedCallContacts);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedSMSContacts="+encryptedSMSContacts);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedNotificationContacts="+encryptedNotificationContacts);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedCallScreeningContacts="+encryptedCallScreeningContacts);
                                                    if (encryptedCallContacts == null) encryptedCallContacts="";
                                                    if (encryptedSMSContacts == null) encryptedSMSContacts="";
                                                    if (encryptedNotificationContacts == null) encryptedNotificationContacts="";
                                                    if (encryptedCallScreeningContacts == null) encryptedCallScreeningContacts="";

                                                    ContentValues values = new ContentValues();
                                                    values.put(DatabaseHandler.KEY_E_CALL_CONTACTS, encryptedCallContacts);
                                                    values.put(DatabaseHandler.KEY_E_SMS_CONTACTS, encryptedSMSContacts);
                                                    values.put(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS, encryptedNotificationContacts);
                                                    values.put(DatabaseHandler.KEY_E_CALL_SCREENING_CONTACTS, encryptedCallScreeningContacts);

                                                    exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                                                new String[]{String.valueOf(eventId)});

                                                } while (cursorExportDB.moveToNext());
                                            }
                                            cursorExportDB.close();
                                        } finally {
                                            if ((cursorExportDB != null) && (!cursorExportDB.isClosed()))
                                                cursorExportDB.close();
                                        }

                                        // encript it, because must be encripted evem when is not used
                                        cursorExportDB = null;
                                        try {
                                            cursorExportDB = exportedDBObj.rawQuery("SELECT " +
                                                    DatabaseHandler.KEY_ID + "," +
                                                    DatabaseHandler.KEY_PHONE_CALLS_CONTACTS +
                                                    " FROM " + DatabaseHandler.TABLE_PROFILES, null);

                                            if (cursorExportDB.moveToFirst()) {
                                                do {
                                                    long profileId = cursorExportDB.getLong(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));

                                                    String phoneCallsContacts = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS));

                                                    String encryptedPhoneCallsContacts = encryption.encryptOrNull(phoneCallsContacts);
                                                    //Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedPhoneCallsContacts="+encryptedPhoneCallsContacts);
                                                    if (encryptedPhoneCallsContacts == null) encryptedPhoneCallsContacts="";

                                                    ContentValues values = new ContentValues();
                                                    values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS, encryptedPhoneCallsContacts);

                                                    exportedDBObj.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                                            new String[]{String.valueOf(profileId)});

                                                } while (cursorExportDB.moveToNext());
                                            }
                                            cursorExportDB.close();
                                        } finally {
                                            if ((cursorExportDB != null) && (!cursorExportDB.isClosed()))
                                                cursorExportDB.close();
                                        }

                                        cursorExportDB = null;
                                        try {
                                            cursorExportDB = exportedDBObj.rawQuery("SELECT " +
                                                    DatabaseHandler.KEY_ID + "," +
                                                    DatabaseHandler.KEY_SEND_SMS_CONTACTS + "," +
                                                    DatabaseHandler.KEY_CLEAR_NOTIFICATION_CONTACTS +
                                                    " FROM " + DatabaseHandler.TABLE_PROFILES, null);

                                            if (cursorExportDB.moveToFirst()) {
                                                do {
                                                    long profileId = cursorExportDB.getLong(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));

                                                    String sendSMSContacts = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_SEND_SMS_CONTACTS));
                                                    String clearNotificationContacts = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_CLEAR_NOTIFICATION_CONTACTS));

                                                    String encryptedSendSMSContacts = encryption.encryptOrNull(sendSMSContacts);
                                                    String encryptedClearNotificationContacts = encryption.encryptOrNull(clearNotificationContacts);
                                                    //Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedSendSMSContacts="+encryptedSendSMSContacts);
                                                    //Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedClearNotificationContacts="+encryptedClearNotificationContacts);
                                                    if (encryptedSendSMSContacts == null) encryptedSendSMSContacts="";
                                                    if (encryptedClearNotificationContacts == null) encryptedClearNotificationContacts="";

                                                    ContentValues values = new ContentValues();
                                                    values.put(DatabaseHandler.KEY_SEND_SMS_CONTACTS, encryptedSendSMSContacts);
                                                    values.put(DatabaseHandler.KEY_CLEAR_NOTIFICATION_CONTACTS, encryptedClearNotificationContacts);

                                                    exportedDBObj.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                                            new String[]{String.valueOf(profileId)});

                                                } while (cursorExportDB.moveToNext());
                                            }
                                            cursorExportDB.close();
                                        } finally {
                                            if ((cursorExportDB != null) && (!cursorExportDB.isClosed()))
                                                cursorExportDB.close();
                                        }

                                        editor = ApplicationPreferences.getEditor(instance.context);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_LOCATIONS_IN_BACKUP_ENCRIPTED, true);
                                        editor.apply();

                                        cursorExportDB = null;
                                        try {
                                            cursorExportDB = exportedDBObj.rawQuery("SELECT " +
                                                    DatabaseHandler.KEY_E_ID + "," +
                                                    DatabaseHandler.KEY_E_WIFI_SSID + "," +
                                                    DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME +
                                                    " FROM " + DatabaseHandler.TABLE_EVENTS, null);

                                            if (cursorExportDB.moveToFirst()) {
                                                do {
                                                    long eventId = cursorExportDB.getLong(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));

                                                    String wifiSSIDs = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_WIFI_SSID));
                                                    String bluetoothNames = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME));

                                                    String encryptedWifiSSIDs = encryption.encryptOrNull(wifiSSIDs);
                                                    String encryptedBluetoothNames = encryption.encryptOrNull(bluetoothNames);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedWifiSSIDs="+encryptedWifiSSIDs);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedBluetoothNames="+encryptedBluetoothNames);
                                                    if (encryptedWifiSSIDs == null) encryptedWifiSSIDs="";
                                                    if (encryptedBluetoothNames == null) encryptedBluetoothNames="";

                                                    ContentValues values = new ContentValues();
                                                    values.put(DatabaseHandler.KEY_E_WIFI_SSID, encryptedWifiSSIDs);
                                                    values.put(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME, encryptedBluetoothNames);

                                                    exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                                            new String[]{String.valueOf(eventId)});

                                                } while (cursorExportDB.moveToNext());
                                            }
                                            cursorExportDB.close();
                                        } finally {
                                            if ((cursorExportDB != null) && (!cursorExportDB.isClosed()))
                                                cursorExportDB.close();
                                        }
                                        cursorExportDB = null;
                                        try {
                                            cursorExportDB = exportedDBObj.rawQuery("SELECT " +
                                                    DatabaseHandler.KEY_G_ID + "," +
                                                    DatabaseHandler.KEY_G_NAME + "," +
                                                    DatabaseHandler.KEY_G_LATITUDE + "," +
                                                    DatabaseHandler.KEY_G_LONGITUDE +
                                                    " FROM " + DatabaseHandler.TABLE_GEOFENCES, null);

                                            if (cursorExportDB.moveToFirst()) {
                                                do {
                                                    long geofenceId = cursorExportDB.getLong(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_G_ID));

                                                    String geofenceName = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_G_NAME));
                                                    double latitude = cursorExportDB.getDouble(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LATITUDE));
                                                    double longitude = cursorExportDB.getDouble(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LONGITUDE));
                                                    String latitudeT = String.valueOf(latitude);
                                                    String longitudeT = String.valueOf(longitude);

                                                    String encryptedGeofenceName = encryption.encryptOrNull(geofenceName);
                                                    String encryptedLatitude = encryption.encryptOrNull(latitudeT);
                                                    String encryptedLongitude = encryption.encryptOrNull(longitudeT);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedGeofenceName="+encryptedGeofenceName);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedLatitude="+encryptedLatitude);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedLongitude="+encryptedLongitude);
                                                    if (encryptedGeofenceName == null) encryptedGeofenceName="";
                                                    if (encryptedLatitude == null) encryptedLatitude="";
                                                    if (encryptedLongitude == null) encryptedLongitude="";

                                                    ContentValues values = new ContentValues();
                                                    values.put(DatabaseHandler.KEY_G_NAME, encryptedGeofenceName);
                                                    values.put(DatabaseHandler.KEY_G_LATITUDE, 0);
                                                    values.put(DatabaseHandler.KEY_G_LATITUDE_T, encryptedLatitude);
                                                    values.put(DatabaseHandler.KEY_G_LONGITUDE, 0);
                                                    values.put(DatabaseHandler.KEY_G_LONGITUDE_T, encryptedLongitude);

                                                    exportedDBObj.update(DatabaseHandler.TABLE_GEOFENCES, values, DatabaseHandler.KEY_G_ID + " = ?",
                                                            new String[]{String.valueOf(geofenceId)});

                                                } while (cursorExportDB.moveToNext());
                                            }
                                            cursorExportDB.close();
                                        } finally {
                                            if ((cursorExportDB != null) && (!cursorExportDB.isClosed()))
                                                cursorExportDB.close();
                                        }
                                        cursorExportDB = null;
                                        try {
                                            cursorExportDB = exportedDBObj.rawQuery("SELECT " +
                                                    DatabaseHandler.KEY_MC_ID + "," +
                                                    DatabaseHandler.KEY_MC_NAME + "," +
                                                    DatabaseHandler.KEY_MC_CELL_ID +
                                                    " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS, null);

                                            if (cursorExportDB.moveToFirst()) {
                                                do {
                                                    long rCellId = cursorExportDB.getLong(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_ID));

                                                    String cellName = cursorExportDB.getString(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_NAME));
                                                    int cellId = cursorExportDB.getInt(cursorExportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_CELL_ID));
                                                    String cellIdT = String.valueOf(cellId);

                                                    String encryptedCellName = encryption.encryptOrNull(cellName);
                                                    String encryptedCelId = encryption.encryptOrNull(cellIdT);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedCellName="+encryptedCellName);
//                                                    Log.e("DatabaseHandlerImportExport.exportedDB", "encryptedCelId="+encryptedCelId);
                                                    if (encryptedCellName == null) encryptedCellName="";
                                                    if (encryptedCelId == null) encryptedCelId="";

                                                    ContentValues values = new ContentValues();
                                                    values.put(DatabaseHandler.KEY_MC_NAME, encryptedCellName);
                                                    values.put(DatabaseHandler.KEY_MC_CELL_ID, 0);
                                                    values.put(DatabaseHandler.KEY_MC_CELL_ID_T, encryptedCelId);

                                                    exportedDBObj.update(DatabaseHandler.TABLE_MOBILE_CELLS, values, DatabaseHandler.KEY_MC_ID + " = ?",
                                                            new String[]{String.valueOf(rCellId)});

                                                } while (cursorExportDB.moveToNext());
                                            }
                                            cursorExportDB.close();
                                        } finally {
                                            if ((cursorExportDB != null) && (!cursorExportDB.isClosed()))
                                                cursorExportDB.close();
                                        }

                                        if (deleteGeofences) {
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_E_LOCATION_GEOFENCES, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, _values, null, null);
                                            exportedDBObj.delete(DatabaseHandler.TABLE_GEOFENCES, null, null);
                                        }
                                        if (deleteWifiSSIDs) {
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_E_WIFI_SSID, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, _values, null, null);
                                        }
                                        if (deleteBluetoothNames) {
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, _values, null, null);
                                        }
                                        if (deleteMobileCells) {
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, _values, null, null);
                                            exportedDBObj.delete(DatabaseHandler.TABLE_MOBILE_CELLS, null, null);
                                        }

                                        String encriptedEmptyStr = encryption.encryptOrNull("");
                                        if (encriptedEmptyStr == null) encriptedEmptyStr = "";
                                        if (deleteCall) {
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_E_CALL_CONTACTS, encriptedEmptyStr);
                                            _values.put(DatabaseHandler.KEY_E_CALL_CONTACT_GROUPS, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, _values, null, null);
                                        }
                                        if (deleteSMS) {
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_E_SMS_CONTACTS, encriptedEmptyStr);
                                            _values.put(DatabaseHandler.KEY_E_SMS_CONTACT_GROUPS, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, _values, null, null);
                                        }
                                        if (deleteNotification) {
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS, encriptedEmptyStr);
                                            _values.put(DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_GROUPS, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, _values, null, null);
                                        }
                                        if (deletePhoneCalls) {
                                            //Log.e("DatabaseHandlerImportExport.exportedDB", "deletePhoneCalls");
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACTS, encriptedEmptyStr);
                                            _values.put(DatabaseHandler.KEY_PHONE_CALLS_CONTACT_GROUPS, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_PROFILES, _values, null, null);
                                        }
                                        if (deleteCallScreening) {
                                            //Log.e("DatabaseHandlerImportExport.exportedDB", "deletePhoneCalls");
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_E_CALL_SCREENING_CONTACTS, encriptedEmptyStr);
                                            _values.put(DatabaseHandler.KEY_E_CALL_SCREENING_CONTACT_GROUPS, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, _values, null, null);
                                        }
                                        if (deleteClearNotifications) {
                                            //Log.e("DatabaseHandlerImportExport.exportedDB", "deletePhoneCalls");
                                            ContentValues _values = new ContentValues();
                                            _values.put(DatabaseHandler.KEY_CLEAR_NOTIFICATION_CONTACTS, encriptedEmptyStr);
                                            _values.put(DatabaseHandler.KEY_CLEAR_NOTIFICATION_CONTACT_GROUPS, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_PROFILES, _values, null, null);
                                        }

                                    } catch (Exception ee) {
                                        PPApplicationStatic.recordException(ee);
                                        ok = false;
                                    }
                                } finally {
                                    if (exportedDBObj != null)
                                        exportedDBObj.close();
                                }
                            }
                            if (ok)
                                ret = 1;
                        }
                    } catch (Exception e) {
                        //Log.e("DatabaseHandlerImportExport.exportDB", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    }
                } finally {
                    if (src != null)
                        src.close();
                    if (dst != null)
                        dst.close();
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return ret;
        } finally {
            instance.stopRunningImportExport();
        }
    }

}
