package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
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
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

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

        if (GlobalData.firstStartServiceStarted)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
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

        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        GlobalData.setActivatedProfileForDuration(context, 0);

        DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
        dataWrapper.getDatabaseHandler().deleteAllEventTimelines(true);

        // create a handler to post messages to the main thread
        Handler toastHandler = new Handler(getMainLooper());
        dataWrapper.setToastHandler(toastHandler);
        Handler brightnessHandler = new Handler(getMainLooper());
        dataWrapper.getActivateProfileHelper().setBrightnessHandler(brightnessHandler);

        // zrusenie notifikacie
        //dataWrapper.getActivateProfileHelper().removeNotification();

        WifiScanAlarmBroadcastReceiver.initialize(context);
        BluetoothScanAlarmBroadcastReceiver.initialize(context);

        GlobalData.setMobileCellsAutoRegistration(context, true);

        GlobalData.firstStartServiceStarted = true;
        GlobalData.setApplicationStarted(context, true);
        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONSTART, null, null, null, 0);

        // startneme eventy
        if (GlobalData.getGlobalEventsRuning(context))
        {
            // must by false for avoiding starts/pause events from receivers before restart events
            //GlobalData.setApplicationStarted(context, false);
            dataWrapper.firstStartEvents(true);
        }
        else
        {
            //GlobalData.setApplicationStarted(context, true);
            // this scanner must be started, used is for mobile cells registration, events existence is not needed
            GlobalData.startPhoneStateScanner(context);
            dataWrapper.activateProfileOnBoot();
        }

        dataWrapper.invalidateDataWrapper();
    }

    static String getPhoneProfilesSilentUri(Context context, int type) {
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(type);
        Cursor cursor = manager.getCursor();

        while (cursor.moveToNext()) {
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);

            String uriId = uri + "/" + id;

            Log.d("FirstStartService.getPhoneProfilesSilentNotificationUri", "title="+title);
            Log.d("FirstStartService.getPhoneProfilesSilentNotificationUri", "uriId="+uriId);

            if (title.equals("PhoneProfiles Silent") || title.equals("phoneprofiles_silent"))
                return uriId;
        }
        return "";
    }

    private static boolean  isToneInstalled(int resID, String directory, Context context) {
        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("FirstStartService.isToneInstalled","not writable shared storage");
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
            Log.d("FirstStartService.isToneInstalled","file not exists");
            return false;
        }

        String outAbsPath = outFile.getAbsolutePath();

        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

        Cursor cursor = context.getContentResolver().query(contentUri,
                new String[]{MediaStore.MediaColumns.DATA},
                MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null, null);
        Log.d("FirstStartService.isToneInstalled","cursor="+cursor);
        if ((cursor == null) || (!cursor.moveToFirst())) {
            if (cursor != null)
                cursor.close();
            Log.d("FirstStartService.isToneInstalled","empty cursor");
            return false;
        }
        else {
            Log.d("FirstStartService.isToneInstalled","DATA="+cursor.getString(0));
            cursor.close();
        }

            /*if (getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_RINGTONE).isEmpty()) {
                Log.d("FirstStartService.isToneInstalled","not in ringtone manager");
                return false;
            }*/

        Log.d("FirstStartService.isToneInstalled","tone installed");

        return true;
    }

    public static boolean isToneInstalled(int resID, Context context) {
        if (Permissions.checkInstallTone(context)) {
            boolean ringtone = isToneInstalled(resID, Environment.DIRECTORY_RINGTONES, context);
            boolean notification = isToneInstalled(resID, Environment.DIRECTORY_NOTIFICATIONS, context);
            boolean alarm = isToneInstalled(resID, Environment.DIRECTORY_ALARMS, context);
            return ringtone && notification && alarm;
        }
        else {
            Log.d("FirstStartService.isToneInstalled","not granted permission");
            return false;
        }
    }

    private static boolean installTone(int resID, int type, String title, Context context, boolean fromMenu) {
        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return false;

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
                return false;
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

                        contentValues.put(MediaStore.Audio.Media.IS_ALARM, isAlarm);
                        contentValues.put(MediaStore.Audio.Media.IS_NOTIFICATION, isNotification);
                        contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, isRingtone);
                        contentValues.put(MediaStore.Audio.Media.IS_MUSIC, false);
                        Uri newUri = context.getContentResolver().insert(contentUri, contentValues);

                        if (newUri != null) {
                            Log.d("FirstStartService","inserted to resolver");

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
                        else {
                            Log.e("FirstStartService","newUri is emty");
                            cursor.close();
                            isError = true;
                        }
                    } else {
                        Log.d("FirstStartService","exists in resolver");
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

    public static boolean installTone(int resID, String title, Context context, boolean fromMenu) {

        boolean granted;
        if (fromMenu)
            granted = Permissions.grantInstallTonePermissions(context, false);
        else
            granted = Permissions.grantInstallTonePermissions(context, true);
        if (granted) {
            boolean ringtone = installTone(resID, RingtoneManager.TYPE_RINGTONE, title, context, fromMenu);
            boolean notification = installTone(resID, RingtoneManager.TYPE_NOTIFICATION, title, context, fromMenu);
            boolean alarm = installTone(resID, RingtoneManager.TYPE_ALARM, title, context, fromMenu);
            return ringtone && notification && alarm;
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
