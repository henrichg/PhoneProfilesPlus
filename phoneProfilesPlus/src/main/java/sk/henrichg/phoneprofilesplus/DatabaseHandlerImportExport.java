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

class DatabaseHandlerImportExport {

    static private boolean tableExists(String tableName, SQLiteDatabase db)
    {
        //boolean tableExists = false;

        /* get cursor on it */
        try
        {
            String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
            try (Cursor cursor = db.rawQuery(query, null)) {
                if(cursor!=null) {
                    if (cursor.getCount()>0) {
                        cursor.close();
                        return true;
                    }
                    cursor.close();
                }
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
            PPApplication.recordException(e);
        }

        return false;
    }

    static private void afterImportDb(DatabaseHandler instance, SQLiteDatabase db) {
        Cursor cursorImportDB = null;

        // update volumes by device max value
        try {
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
                SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(instance.context);
                int maximumVolumeRing = sharedPreferences.getInt("maximumVolume_ring", 0);
                int maximumVolumeNotification = sharedPreferences.getInt("maximumVolume_notification", 0);
                int maximumVolumeMusic = sharedPreferences.getInt("maximumVolume_music", 0);
                int maximumVolumeAlarm = sharedPreferences.getInt("maximumVolume_alarm", 0);
                int maximumVolumeSystem = sharedPreferences.getInt("maximumVolume_system", 0);
                int maximumVolumeVoiceCall = sharedPreferences.getInt("maximumVolume_voiceCall", 0);
                int maximumVolumeDTFM = sharedPreferences.getInt("maximumVolume_dtmf", 0);
                int maximumVolumeAccessibility = sharedPreferences.getInt("maximumVolume_accessibility", 0);
                int maximumVolumeBluetoothSCO = sharedPreferences.getInt("maximumVolume_bluetoothSCO", 0);

                if (cursorImportDB.moveToFirst()) {
                    do {

                        long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));

                        ContentValues values = new ContentValues();

                        String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGTONE));
                        try {
                            String[] splits = value.split("\\|");
                            int volume = Integer.parseInt(splits[0]);
                            float fVolume = volume;
                            float percentage;
                            if (maximumVolumeRing > 0)
                                percentage = fVolume / maximumVolumeRing * 100f;
                            else
                                percentage = fVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                            if (percentage > 100f)
                                percentage = 100f;
                            fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) / 100f * percentage;
                            volume = Math.round(fVolume);
                            if (splits.length == 3)
                                values.put(DatabaseHandler.KEY_VOLUME_RINGTONE, volume + "|" + splits[1] + "|" + splits[2]);
                            else
                                values.put(DatabaseHandler.KEY_VOLUME_RINGTONE, volume + "|" + splits[1]);
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Bad stream type X
                            //PPApplication.recordException(e);
                        } catch (Exception e) {
                            //Log.e("DatabaseHandler.importDB", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        }

                        value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_NOTIFICATION));
                        try {
                            String[] splits = value.split("\\|");
                            int volume = Integer.parseInt(splits[0]);
                            float fVolume = volume;
                            float percentage;
                            if (maximumVolumeNotification > 0)
                                percentage = fVolume / maximumVolumeNotification * 100f;
                            else
                                percentage = fVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) * 100f;
                            if (percentage > 100f)
                                percentage = 100f;
                            fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 100f * percentage;
                            volume = Math.round(fVolume);
                            if (splits.length == 3)
                                values.put(DatabaseHandler.KEY_VOLUME_NOTIFICATION, volume + "|" + splits[1] + "|" + splits[2]);
                            else
                                values.put(DatabaseHandler.KEY_VOLUME_NOTIFICATION, volume + "|" + splits[1]);
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Bad stream type X
                            //PPApplication.recordException(e);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MEDIA));
                        try {
                            String[] splits = value.split("\\|");
                            int volume = Integer.parseInt(splits[0]);
                            float fVolume = volume;
                            float percentage;
                            if (maximumVolumeMusic > 0)
                                percentage = fVolume / maximumVolumeMusic * 100f;
                            else
                                percentage = fVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 100f;
                            if (percentage > 100f)
                                percentage = 100f;
                            fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100f * percentage;
                            volume = Math.round(fVolume);
                            if (splits.length == 3)
                                values.put(DatabaseHandler.KEY_VOLUME_MEDIA, volume + "|" + splits[1] + "|" + splits[2]);
                            else
                                values.put(DatabaseHandler.KEY_VOLUME_MEDIA, volume + "|" + splits[1]);
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Bad stream type X
                            //PPApplication.recordException(e);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ALARM));
                        try {
                            String[] splits = value.split("\\|");
                            int volume = Integer.parseInt(splits[0]);
                            float fVolume = volume;
                            float percentage;
                            if (maximumVolumeAlarm > 0)
                                percentage = fVolume / maximumVolumeAlarm * 100f;
                            else
                                percentage = fVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * 100f;
                            if (percentage > 100f)
                                percentage = 100f;
                            fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) / 100f * percentage;
                            volume = Math.round(fVolume);
                            if (splits.length == 3)
                                values.put(DatabaseHandler.KEY_VOLUME_ALARM, volume + "|" + splits[1] + "|" + splits[2]);
                            else
                                values.put(DatabaseHandler.KEY_VOLUME_ALARM, volume + "|" + splits[1]);
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Bad stream type X
                            //PPApplication.recordException(e);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SYSTEM));
                        try {
                            String[] splits = value.split("\\|");
                            int volume = Integer.parseInt(splits[0]);
                            float fVolume = volume;
                            float percentage;
                            if (maximumVolumeSystem > 0)
                                percentage = fVolume / maximumVolumeSystem * 100f;
                            else
                                percentage = fVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM) * 100f;
                            if (percentage > 100f)
                                percentage = 100f;
                            fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM) / 100f * percentage;
                            volume = Math.round(fVolume);
                            if (splits.length == 3)
                                values.put(DatabaseHandler.KEY_VOLUME_SYSTEM, volume + "|" + splits[1] + "|" + splits[2]);
                            else
                                values.put(DatabaseHandler.KEY_VOLUME_SYSTEM, volume + "|" + splits[1]);
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Bad stream type X
                            //PPApplication.recordException(e);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_VOICE));
                        try {
                            String[] splits = value.split("\\|");
                            int volume = Integer.parseInt(splits[0]);
                            float fVolume = volume;
                            float percentage;
                            if (maximumVolumeVoiceCall > 0)
                                percentage = fVolume / maximumVolumeVoiceCall * 100f;
                            else
                                percentage = fVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) * 100f;
                            if (percentage > 100f)
                                percentage = 100f;
                            fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) / 100f * percentage;
                            volume = Math.round(fVolume);
                            if (splits.length == 3)
                                values.put(DatabaseHandler.KEY_VOLUME_VOICE, volume + "|" + splits[1] + "|" + splits[2]);
                            else
                                values.put(DatabaseHandler.KEY_VOLUME_VOICE, volume + "|" + splits[1]);
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Bad stream type X
                            //PPApplication.recordException(e);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_DTMF));
                        try {
                            String[] splits = value.split("\\|");
                            int volume = Integer.parseInt(splits[0]);
                            float fVolume = volume;
                            float percentage;
                            if (maximumVolumeDTFM > 0)
                                percentage = fVolume / maximumVolumeDTFM * 100f;
                            else
                                percentage = fVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF) * 100f;
                            if (percentage > 100f)
                                percentage = 100f;
                            fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF) / 100f * percentage;
                            volume = Math.round(fVolume);
                            if (splits.length == 3)
                                values.put(DatabaseHandler.KEY_VOLUME_DTMF, volume + "|" + splits[1] + "|" + splits[2]);
                            else
                                values.put(DatabaseHandler.KEY_VOLUME_DTMF, volume + "|" + splits[1]);
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Bad stream type X
                            //PPApplication.recordException(e);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        if (Build.VERSION.SDK_INT >= 26) {
                            value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ACCESSIBILITY));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                float fVolume = volume;
                                float percentage;
                                if (maximumVolumeAccessibility > 0)
                                    percentage = fVolume / maximumVolumeAccessibility * 100f;
                                else
                                    percentage = fVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY) * 100f;
                                if (percentage > 100f)
                                    percentage = 100f;
                                fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY) / 100f * percentage;
                                volume = Math.round(fVolume);
                                if (splits.length == 3)
                                    values.put(DatabaseHandler.KEY_VOLUME_ACCESSIBILITY, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(DatabaseHandler.KEY_VOLUME_ACCESSIBILITY, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type 10 - Android 6
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }

                        value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO));
                        try {
                            String[] splits = value.split("\\|");
                            int volume = Integer.parseInt(splits[0]);
                            float fVolume = volume;
                            float percentage;
                            if (maximumVolumeBluetoothSCO > 0)
                                percentage = fVolume / maximumVolumeBluetoothSCO * 100f;
                            else
                                percentage = fVolume / audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO) * 100f;
                            if (percentage > 100f)
                                percentage = 100f;
                            fVolume = audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO) / 100f * percentage;
                            volume = Math.round(fVolume);
                            if (splits.length == 3)
                                values.put(DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO, volume + "|" + splits[1] + "|" + splits[2]);
                            else
                                values.put(DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO, volume + "|" + splits[1]);
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Bad stream type X
                            //PPApplication.recordException(e);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        // updating row
                        if (values.size() > 0)
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                    } while (cursorImportDB.moveToNext());
                }
            }
            cursorImportDB.close();
        } finally {
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }

        // clear dual sim parameters for device without dual sim support
        int phoneCount = 1;
        if (Build.VERSION.SDK_INT >= 26) {
            TelephonyManager telephonyManager = (TelephonyManager) instance.context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phoneCount = telephonyManager.getPhoneCount();
            }
        }
        if (phoneCount < 2) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + "=\"0|0|0\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + "=\"0|0|0\"");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + "=\"\"");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + "=\"\"");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_FROM_SIM_SLOT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_FOR_SIM_CARD + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_FOR_SIM_CARD + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MOBILE_CELLS_FOR_SIM_CARD + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ROAMING_FOR_SIM_CARD + "=0");
        }

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
                    DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND +
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
                    String[] splits = tone.split("\\|");
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
                    splits = tone.split("\\|");
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
                    splits = tone.split("\\|");
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
                    splits = tone.split("\\|");
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
                    splits = tone.split("\\|");
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
                    splits = tone.split("\\|");
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
                    splits = tone.split("\\|");
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
                    String wallpaperFolder = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER));
                    if (!wallpaperFolder.isEmpty() && !wallpaperFolder.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER))) {
                        boolean isGranted = false;
                        Uri uri = Uri.parse(wallpaperFolder);
                        if (uri != null) {
                            try {
                                instance.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION /* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                                // persistent permissions
                                final int takeFlags = //data.getFlags() &
                                        (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                instance.context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
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
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_ID + " = ?",
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
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_ID + " = ?",
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
                                            String[] splits = fkProfiles.split("\\|");
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
                            PPApplication.recordException(ee);
                        }
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            exportedDB.setWritable(true, false);
                        } catch (Exception ee) {
                            PPApplication.recordException(ee);
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
                    //Log.e("DatabaseHandler.importDB", Log.getStackTraceString(e1));
                    //getVersion(): android.database.sqlite.SQLiteCantOpenDatabaseException: unable to open database file (Sqlite code 14), (OS error - 2:No such file or directory)
                    PPApplication.recordException(e1);
                    ret = DatabaseHandler.IMPORT_ERROR_BUG;
                }

            } catch (Exception e2) {
                //Log.e("DatabaseHandler.importDB", Log.getStackTraceString(e2));
                //getVersion(): android.database.sqlite.SQLiteCantOpenDatabaseException: unable to open database file (Sqlite code 14), (OS error - 2:No such file or directory)
                PPApplication.recordException(e2);
            }
            return ret;
        } finally {
            instance.stopRunningImportExport();
        }
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    static int exportDB(DatabaseHandler instance,
                        boolean deleteGeofences, boolean deleteWifiSSIDs,
                        boolean deleteBluetoothNames, boolean deleteMobileCells)
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
                                PPApplication.recordException(ee);
                            }
                            try {
                                //noinspection ResultOfMethodCallIgnored
                                exportedDB.setWritable(true, false);
                            } catch (Exception ee) {
                                PPApplication.recordException(ee);
                            }

                            if (ok) {
                                SQLiteDatabase exportedDBObj = null;
                                try {
                                    try {
                                        exportedDBObj = SQLiteDatabase.openDatabase(exportedDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

                                        if (deleteGeofences) {
                                            ContentValues values = new ContentValues();
                                            values.put(DatabaseHandler.KEY_E_LOCATION_GEOFENCES, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, values, null, null);
                                            exportedDBObj.delete(DatabaseHandler.TABLE_GEOFENCES, null, null);
                                        }
                                        if (deleteWifiSSIDs) {
                                            ContentValues values = new ContentValues();
                                            values.put(DatabaseHandler.KEY_E_WIFI_SSID, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, values, null, null);
                                        }
                                        if (deleteBluetoothNames) {
                                            ContentValues values = new ContentValues();
                                            values.put(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, values, null, null);
                                        }
                                        if (deleteMobileCells) {
                                            ContentValues values = new ContentValues();
                                            values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS, "");
                                            exportedDBObj.update(DatabaseHandler.TABLE_EVENTS, values, null, null);
                                            exportedDBObj.delete(DatabaseHandler.TABLE_MOBILE_CELLS, null, null);
                                        }

                                    } catch (Exception ee) {
                                        PPApplication.recordException(ee);
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
                        //Log.e("DatabaseHandler.exportDB", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    }
                } finally {
                    if (src != null)
                        src.close();
                    if (dst != null)
                        dst.close();
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return ret;
        } finally {
            instance.stopRunningImportExport();
        }
    }

}
