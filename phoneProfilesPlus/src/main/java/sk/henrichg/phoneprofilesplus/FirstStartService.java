package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FirstStartService extends IntentService {

    public static final int TONE_ID = R.raw.phoneprofiles_silent;
    public static final String TONE_NAME = "PhoneProfiles Silent";

    public FirstStartService()
    {
        super("FirstStartService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Context context = getApplicationContext();

        GlobalData.logE("$$$ FirstStartService.onHandleIntent","--- START");

        GlobalData.initRoot();
        // grant root
        //if (GlobalData.isRooted(false))
        //{
            if (GlobalData.isRootGranted())
            {
                GlobalData.settingsBinaryExists();
                GlobalData.serviceBinaryExists();
                //GlobalData.getSUVersion();
            }
        //}

        if (GlobalData.getApplicationStarted(context))
            return;

        GlobalData.logE("$$$ FirstStartService.onHandleIntent","application not started");

        GlobalData.clearMergedPermissions(context);

        //int startType = intent.getStringExtra(GlobalData.EXTRA_FIRST_START_TYPE);

        GlobalData.loadPreferences(context);
        GUIData.setLanguage(context);

        installTone(TONE_ID, TONE_NAME, context, false);

        GlobalData.setLockscreenDisabled(context, false);

        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        GlobalData.setRingerVolume(context, audioManager.getStreamVolume(AudioManager.STREAM_RING));
        GlobalData.setNotificationVolume(context, audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        RingerModeChangeReceiver.setRingerMode(context, audioManager);
        PPNotificationListenerService.setZenMode(context, audioManager);
        InterruptionFilterChangedBroadcastReceiver.setZenMode(context, audioManager);

        GlobalData.setActivatedProfileForDuration(context, 0);
        GlobalData.setApplicationInForeground(context, "");

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(GlobalData.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallService.CALL_EVENT_UNDEFINED);
        editor.putString(GlobalData.PREF_EVENT_CALL_PHONE_NUMBER, "");
        editor.commit();

        // show info notification
        ImportantInfoNotification.showInfoNotification(context);

        // start ReceiverService
        context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));

        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        GlobalData.setActivatedProfileForDuration(context, 0);

        DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, context);
        dataWrapper.getDatabaseHandler().deleteAllEventTimelines(true);

        // create a handler to post messages to the main thread
        Handler toastHandler = new Handler(getMainLooper());
        dataWrapper.setToastHandler(toastHandler);
        Handler brightnessHandler = new Handler(getMainLooper());
        dataWrapper.getActivateProfileHelper().setBrightnessHandler(brightnessHandler);

        // zrusenie notifikacie
        dataWrapper.getActivateProfileHelper().removeNotification();

        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONSTART, null, null, null, 0);

        WifiScanAlarmBroadcastReceiver.initialize(context);
        BluetoothScanAlarmBroadcastReceiver.initialize(context);

        // startneme eventy
        if (GlobalData.getGlobalEventsRuning(context))
        {
            // must by false for avoiding starts/pause events from receivers before restart events
            GlobalData.setApplicationStarted(context, false);
            dataWrapper.firstStartEvents(true);
        }
        else
        {
            GlobalData.setApplicationStarted(context, true);
            dataWrapper.activateProfileOnBoot();
        }

        dataWrapper.invalidateDataWrapper();
    }

    public static boolean isToneInstalled(int resID, Context context) {
        if (Permissions.checkInstallTone(context)) {
            // Make sure the shared storage is currently writable
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                return false;

            File path = Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
            // Make sure the directory exists
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
            String filename = context.getResources().getResourceEntryName(resID) + ".ogg";
            File outFile = new File(path, filename);

            if (!outFile.exists())
                return false;

            String outAbsPath = outFile.getAbsolutePath();

            Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

            Cursor cursor = context.getContentResolver().query(contentUri,
                    new String[]{MediaStore.MediaColumns.DATA},
                    MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null, null);
            if ((cursor == null) || (!cursor.moveToFirst())) {
                if (cursor != null)
                    cursor.close();
                return false;
            }

            return true;
        }
        else
            return false;
    }

    public static boolean installTone(int resID, String title, Context context, boolean fromMenu) {

        boolean granted;
        if (fromMenu)
            granted = Permissions.grantInstallTonePermissions(context, false);
        else
            granted = Permissions.grantInstallTonePermissions(context, true);
        if (granted) {

            // Make sure the shared storage is currently writable
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                return false;

            File path = Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
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
                    Log.e("FirstStartService", "installTone: Error writing " + filename, e);
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
                    Log.e("FirstStartService", "installTone: Error writing " + filename);
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

                            //Log.e("FirstStartService","not exists in resolver");

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
                            contentValues.put(MediaStore.Audio.Media.IS_ALARM, true);
                            contentValues.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                            contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                            contentValues.put(MediaStore.Audio.Media.IS_MUSIC, false);
                            Uri newUri = context.getContentResolver().insert(contentUri, contentValues);

                            if (newUri != null) {
                                //Log.e("FirstStartService","inserted to resolver");

                                // Tell the media scanner about the new ringtone
                                MediaScannerConnection.scanFile(
                                        context,
                                        new String[]{newUri.toString()},
                                        new String[]{mimeType},
                                        null
                                );

                                //try { Thread.sleep(300); } catch (InterruptedException e) { }
                                //SystemClock.sleep(300);
                                GlobalData.sleep(300);
                            }
                        } else {
                            //    Log.e("FirstStartService","exists in resolver");
                            cursor.close();
                        }
                    }
                } catch (Exception e) {
                    Log.e("FirstStartService", "installTone: Error installing tone " + filename);
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

            return !isError;
        }
        else
            return false;
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
