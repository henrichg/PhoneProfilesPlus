package sk.henrichg.phoneprofilesplus;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class FirstStartJob extends Job {

    static final String JOB_TAG  = "FirstStartJob";

    static final int TONE_ID = R.raw.phoneprofiles_silent;
    static final String TONE_NAME = "PhoneProfiles Silent";
    
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "FirstStartJob.onRunJob", "FirstStartJob_onRunJob");

        PPApplication.initRoot();
        // grant root
        //if (PPApplication.isRooted(false))
        //{
        if (PPApplication.isRootGranted())
        {
            PPApplication.settingsBinaryExists();
            PPApplication.serviceBinaryExists();
            //PPApplication.getSUVersion();
        }
        //}

        GlobalGUIRoutines.setLanguage(appContext);

        if (PPApplication.getApplicationStarted(appContext, false)) {
            PPApplication.logE("$$$ FirstStartJob.onRunJob","application already started");
            return Result.SUCCESS;
        }

        PPApplication.logE("$$$ FirstStartJob.onRunJob","application not started, start it");

        Bundle bundle = params.getTransientExtras();
        boolean startOnBoot = bundle.getBoolean(PhoneProfilesService.EXTRA_START_ON_BOOT, false);

        Permissions.clearMergedPermissions(appContext);

        installTone(TONE_ID, TONE_NAME, appContext, false);

        ActivateProfileHelper.setLockScreenDisabled(appContext, false);

        AudioManager audioManager = (AudioManager)appContext.getSystemService(Context.AUDIO_SERVICE);
        ActivateProfileHelper.setRingerVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_RING));
        ActivateProfileHelper.setNotificationVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        RingerModeChangeReceiver.setRingerMode(appContext, audioManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            PPNotificationListenerService.setZenMode(appContext, audioManager);
        InterruptionFilterChangedBroadcastReceiver.setZenMode(appContext, audioManager);

        Profile.setActivatedProfileForDuration(appContext, 0);
        ForegroundApplicationChangedService.setApplicationInForeground(appContext, "");

        ApplicationPreferences.getSharedPreferences(appContext);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PhoneCallJob.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallJob.CALL_EVENT_UNDEFINED);
        editor.putString(PhoneCallJob.PREF_EVENT_CALL_PHONE_NUMBER, "");
        editor.apply();

        // show info notification
        ImportantInfoNotification.showInfoNotification(appContext);

        ProfileDurationAlarmBroadcastReceiver.removeAlarm(appContext);
        Profile.setActivatedProfileForDuration(appContext, 0);

        DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
        dataWrapper.getDatabaseHandler().deleteAllEventTimelines(true);

        MobileCellsRegistrationService.setMobileCellsAutoRegistration(appContext, true);

        PPApplication.setApplicationStarted(appContext, true);
        if (startOnBoot)
            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONSTARTONBOOT, null, null, null, 0);
        else
            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONSTART, null, null, null, 0);

        PPApplication.logE("$$$ FirstStartJob.onRunJob","application started");

        // startname eventy
        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("$$$ FirstStartJob.onRunJob","global event run is enabled, first start events");

            if (!dataWrapper.getIsManualProfileActivation()) {
                ////// unblock all events for first start
                //     that may be blocked in previous application run
                dataWrapper.pauseAllEvents(true, false/*, false*/);
            }

            dataWrapper.firstStartEvents(true);
        }
        else
        {
            PPApplication.logE("$$$ FirstStartJob.onRunJob","global event run is not enabled, manually activate profile");
            //PPApplication.setApplicationStarted(context, true);

            ////// unblock all events for first start
            //     that may be blocked in previous application run
            dataWrapper.pauseAllEvents(true, false/*, false*/);

            dataWrapper.activateProfileOnBoot();
        }

        dataWrapper.invalidateDataWrapper();
        
        return Result.SUCCESS;
    }

    static void start(boolean startOnBoot) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putBoolean(PhoneProfilesService.EXTRA_START_ON_BOOT, startOnBoot);

        try {
            jobBuilder
                    .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                    .setTransientExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } catch (Exception ignored) { }
    }

    static String getPhoneProfilesSilentUri(Context context, int type) {
        try {
            RingtoneManager manager = new RingtoneManager(context);
            manager.setType(type);
            Cursor cursor = manager.getCursor();

            while (cursor.moveToNext()) {
                String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
                String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);

                String uriId = uri + "/" + id;

                //Log.d("FirstStartJob.getPhoneProfilesSilentNotificationUri", "title="+title);
                //Log.d("FirstStartJob.getPhoneProfilesSilentNotificationUri", "uriId="+uriId);

                if (title.equals("PhoneProfiles Silent") || title.equals("phoneprofiles_silent"))
                    return uriId;
            }
        } catch (Exception ignored) {}
        return "";
    }

    static String getToneName(Context context, int type, String _uri) {
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(type);
        Cursor cursor = manager.getCursor();

        while (cursor.moveToNext()) {
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);

            String uriId = uri + "/" + id;

            //Log.d("FirstStartJob.getToneName", "title="+title);
            //Log.d("FirstStartJob.getToneName", "uriId="+uriId);

            if (uriId.equals(_uri))
                return title;
        }
        return "";
    }

    private static boolean  isToneInstalled(int resID, String directory, Context context) {
        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //Log.d("FirstStartJob.isToneInstalled","not writable shared storage");
            return false;
        }

        File path = Environment.
                getExternalStoragePublicDirectory(directory);
        // Make sure the directory exists
        //noinspection ResultOfMethodCallIgnored
        path.mkdirs();
        String filename = context.getResources().getResourceEntryName(resID) + ".ogg";
        File outFile = new File(path, filename);

        if (!outFile.exists()) {
            //Log.d("FirstStartJob.isToneInstalled","file not exists");
            return false;
        }

        String outAbsPath = outFile.getAbsolutePath();

        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

        Cursor cursor = context.getContentResolver().query(contentUri,
                new String[]{MediaStore.MediaColumns.DATA},
                MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null, null);
        //Log.d("FirstStartJob.isToneInstalled","cursor="+cursor);
        if ((cursor == null) || (!cursor.moveToFirst())) {
            if (cursor != null)
                cursor.close();
            //Log.d("FirstStartJob.isToneInstalled","empty cursor");
            return false;
        }
        else {
            //Log.d("FirstStartJob.isToneInstalled","DATA="+cursor.getString(0));
            cursor.close();
        }

            /*if (getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_RINGTONE).isEmpty()) {
                Log.d("FirstStartJob.isToneInstalled","not in ringtone manager");
                return false;
            }*/

        //Log.d("FirstStartJob.isToneInstalled","tone installed");

        return true;
    }

    static boolean isToneInstalled(int resID, Context context) {
        if (Permissions.checkInstallTone(context)) {
            boolean ringtone = isToneInstalled(resID, Environment.DIRECTORY_RINGTONES, context);
            boolean notification = isToneInstalled(resID, Environment.DIRECTORY_NOTIFICATIONS, context);
            boolean alarm = isToneInstalled(resID, Environment.DIRECTORY_ALARMS, context);
            return ringtone && notification && alarm;
        }
        else {
            //Log.d("FirstStartJob.isToneInstalled","not granted permission");
            return false;
        }
    }

    private static void installTone(int resID, int type, String title, Context context, boolean fromMenu) {
        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return;

        String directory;
        boolean isRingtone = false;
        boolean isNotification = false;
        boolean isAlarm = false;
        switch (type) {
            case RingtoneManager.TYPE_RINGTONE:
                directory = Environment.DIRECTORY_RINGTONES;
                isRingtone = true;
                break;
            case RingtoneManager.TYPE_NOTIFICATION:
                directory = Environment.DIRECTORY_NOTIFICATIONS;
                isNotification = true;
                break;
            case RingtoneManager.TYPE_ALARM:
                directory = Environment.DIRECTORY_ALARMS;
                isAlarm = true;
                break;
            default:
                return;
        }
        File path = Environment.
                getExternalStoragePublicDirectory(directory);
        // Make sure the directory exists
        //noinspection ResultOfMethodCallIgnored
        path.mkdirs();
        String filename = context.getResources().getResourceEntryName(resID) + ".ogg";
        File outFile = new File(path, filename);

        boolean isError = false;

        if (!outFile.exists()) {

            // Write the file
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = context.getResources().openRawResource(resID);
                outputStream = new FileOutputStream(outFile);


                // Write in 1024-byte chunks
                byte[] buffer = new byte[1024];
                int bytesRead;
                // Keep writing until `inputStream.read()` returns -1, which means we reached the
                //  end of the stream
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }

            } catch (Exception e) {
                Log.e("FirstStartJob", "installTone: Error writing " + filename, e);
                isError = true;
            } finally {
                // Close the streams
                try {
                    if (inputStream != null)
                        inputStream.close();
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    // Means there was an error trying to close the streams, so do nothing
                }
            }

            if (!outFile.exists()) {
                Log.e("FirstStartJob", "installTone: Error writing " + filename);
                isError = true;
            }
        }

        if (!isError) {

            try {
                String mimeType = "audio/ogg";

                // Set the file metadata
                String outAbsPath = outFile.getAbsolutePath();

                Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

                Cursor cursor = context.getContentResolver().query(contentUri,
                        new String[]{MediaStore.MediaColumns.DATA},
                        MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null, null);
                if (cursor != null) {
                    if (!cursor.moveToFirst()) {

                        //Log.e("FirstStartJob","not exists in resolver");

                        // not exists content

                        cursor.close();

                        //// If the ringtone already exists in the database, delete it first
                        //context.getContentResolver().delete(contentUri,
                        //        MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null);

                        // Add the metadata to the file in the database
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DATA, outAbsPath);
                        contentValues.put(MediaStore.MediaColumns.TITLE, title);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                        contentValues.put(MediaStore.MediaColumns.SIZE, outFile.length());

                        contentValues.put(MediaStore.Audio.Media.IS_ALARM, isAlarm);
                        contentValues.put(MediaStore.Audio.Media.IS_NOTIFICATION, isNotification);
                        contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, isRingtone);
                        contentValues.put(MediaStore.Audio.Media.IS_MUSIC, false);
                        Uri newUri = context.getContentResolver().insert(contentUri, contentValues);

                        if (newUri != null) {
                            //Log.d("FirstStartJob","inserted to resolver");

                            // Tell the media scanner about the new ringtone
                            MediaScannerConnection.scanFile(
                                    context,
                                    new String[]{newUri.toString()},
                                    new String[]{mimeType},
                                    null
                            );

                            //try { Thread.sleep(300); } catch (InterruptedException e) { }
                            //SystemClock.sleep(300);
                            PPApplication.sleep(300);
                        }
                        else {
                            Log.e("FirstStartJob","newUri is empty");
                            cursor.close();
                            isError = true;
                        }
                    } else {
                        //Log.d("FirstStartJob","exists in resolver");
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                Log.e("FirstStartJob", "installTone: Error installing tone " + filename);
                isError = true;
            }
        }

        if (fromMenu) {
            int strId = R.string.toast_tone_installation_installed_ok;
            if (isError)
                strId = R.string.toast_tone_installation_installed_error;

            Toast msg = Toast.makeText(context,
                    context.getResources().getString(strId),
                    Toast.LENGTH_SHORT);
            msg.show();
        }

        return;
    }

    static void installTone(int resID, String title, Context context, boolean fromMenu) {

        boolean granted;
        if (fromMenu)
            granted = Permissions.grantInstallTonePermissions(context, false);
        else
            granted = Permissions.grantInstallTonePermissions(context, true);
        if (granted) {
            installTone(resID, RingtoneManager.TYPE_RINGTONE, title, context, fromMenu);
            installTone(resID, RingtoneManager.TYPE_NOTIFICATION, title, context, fromMenu);
            installTone(resID, RingtoneManager.TYPE_ALARM, title, context, fromMenu);
        }
    }

    /*
    private void removeTone(String voiceFile, Context context) {

        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return;

        File path = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
        String filename = voiceFile;
        File outFile = new File(path, filename);

        String outAbsPath = outFile.getAbsolutePath();
        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

        // If the ringtone already exists in the database, delete it first
        context.getContentResolver().delete(contentUri,
                MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null);

        // delete the file
        outFile.delete();
    }
    */
    
}
