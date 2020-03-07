package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

//import me.drakeet.support.toast.ToastCompat;

class TonesHandler {

    static final int TONE_ID = R.raw.phoneprofiles_silent;
    static final String TONE_NAME = "PhoneProfiles Silent";

    private static final String RINGING_TONE_URI_NONE = "content://settings/system/ringtone";
    static final String NOTIFICATION_TONE_URI_NONE = "content://settings/system/notification_sound";
    private static final String ALARM_TONE_URI_NONE = "content://settings/system/alarm_alert";

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

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("TonesHandler.getPhoneProfilesSilentNotificationUri", "title=" + title);
                    PPApplication.logE("TonesHandler.getPhoneProfilesSilentNotificationUri", "uriId=" + uriId);
                }*/

                if (title.equals(TONE_NAME) || title.equals("phoneprofiles_silent"))
                    return uriId;
            }
        } catch (Exception e) {
            // FC in manager.getCursor() for RingtoneManager.TYPE_NOTIFICATION.
            // Nokia 8 (HMD Global), hm
            //Log.e("TonesHandler.getPhoneProfilesSilentNotificationUri", Log.getStackTraceString(e));
            //Crashlytics.logException(e);
        }
        return "";
    }

    static String getToneName(Context context,
                              @SuppressWarnings("SameParameterValue") int type,
                              String _uri) {

        if ((type == RingtoneManager.TYPE_RINGTONE) && (_uri.isEmpty() || _uri.equals(RINGING_TONE_URI_NONE)))
            return context.getString(R.string.ringtone_preference_none);
        if ((type == RingtoneManager.TYPE_NOTIFICATION) && (_uri.isEmpty() || _uri.equals(NOTIFICATION_TONE_URI_NONE)))
            return context.getString(R.string.ringtone_preference_none);
        if ((type == RingtoneManager.TYPE_ALARM) && (_uri.isEmpty() || _uri.equals(ALARM_TONE_URI_NONE)))
            return context.getString(R.string.ringtone_preference_none);

        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(type);
        Cursor cursor = manager.getCursor();

        //PPApplication.logE("TonesHandler.getToneName", "_uri="+_uri);

        while (cursor.moveToNext()) {
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);

            String uriId = uri + "/" + id;

            //Log.d("TonesHandler.getToneName", "title="+title);
            //Log.d("TonesHandler.getToneName", "uriId="+uriId);

            if (uriId.equals(_uri))
                return title;
        }
        return "";
    }

    static boolean isPhoneProfilesSilent(Uri uri, Context appContext) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("TonesHandler.isPhoneProfilesSilent", "xxx");
            PPApplication.logE("TonesHandler.isPhoneProfilesSilent", "uri=" + uri);
        }*/
        String displayName = "";
        try {
            Cursor cursor = appContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1)
                        displayName = cursor.getString(nameIndex);
                }
                //PPApplication.logE("TonesHandler.isPhoneProfilesSilent", "displayName=" + displayName);

                cursor.close();
            }
        } catch (Exception ignored) {}
        String filename = appContext.getResources().getResourceEntryName(TonesHandler.TONE_ID) + ".ogg";
        //PPApplication.logE("TonesHandler.isPhoneProfilesSilent", "END");
        return (displayName != null) && displayName.equals(filename);
    }

    private static boolean  isToneInstalled(/*int resID, String directory,*/ int type, Context context) {
        // Make sure the shared storage is currently writable
        /*if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //Log.d("TonesHandler.isToneInstalled","not writable shared storage");
            return false;
        }*/
        /*
        //File path = Environment.getExternalStoragePublicDirectory(directory);
        File path = context.getFilesDir();
        // Make sure the directory exists
        //noinspection ResultOfMethodCallIgnored
        path.mkdirs();
        String filename = context.getResources().getResourceEntryName(resID) + ".ogg";
        File outFile = new File(path, filename);

        if (!outFile.exists()) {
            Log.e("TonesHandler.isToneInstalled","file not exists");
            return false;
        }
        */

        /*
        String outAbsPath = outFile.getAbsolutePath();

        //Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);
        Uri contentUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(contentUri,
                new String[]{MediaStore.MediaColumns.DATA},
                MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null, null);
        //Log.d("TonesHandler.isToneInstalled","cursor="+cursor);
        if ((cursor == null) || (!cursor.moveToFirst())) {
            if (cursor != null)
                cursor.close();
            Log.e("TonesHandler.isToneInstalled","empty cursor");
            return false;
        }
        else {
            //Log.d("TonesHandler.isToneInstalled","DATA="+cursor.getString(0));
            cursor.close();
        }
        */

        //noinspection RedundantIfStatement
        if (getPhoneProfilesSilentUri(context, type).isEmpty()) {
            //PPApplication.logE("TonesHandler.isToneInstalled","not in ringtone manager");
            return false;
        }

        //PPApplication.logE("TonesHandler.isToneInstalled","tone installed");

        return true;
    }

    static boolean isToneInstalled(/*@SuppressWarnings("SameParameterValue") int resID,*/
                                   Context context) {
        //if (Permissions.checkInstallTone(context, null)) {
            //boolean ringtone = isToneInstalled(resID, Environment.DIRECTORY_RINGTONES, context);
            //boolean notification = isToneInstalled(resID, Environment.DIRECTORY_NOTIFICATIONS, context);
            //boolean alarm = isToneInstalled(resID, Environment.DIRECTORY_ALARMS, context);
            boolean ringtone = isToneInstalled(/*resID,*/ RingtoneManager.TYPE_RINGTONE, context);
            boolean notification = isToneInstalled(/*resID,*/ RingtoneManager.TYPE_NOTIFICATION, context);
            boolean alarm = isToneInstalled(/*resID,*/ RingtoneManager.TYPE_ALARM, context);

            return ringtone && notification && alarm;
        //}
        //else {
        //    //Log.d("TonesHandler.isToneInstalled","not granted permission");
        //    return false;
        //}
    }

    @SuppressLint("SetWorldReadable")
    private static boolean _installTone(int resID, /*int type,*/ String title, Context context) {
        /*try {
            // Make sure the shared storage is currently writable
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                return false;
        } catch (Exception e) {
            return false;
        }*/

        //boolean isRingtone = true;
        //boolean isNotification = true;
        //boolean isAlarm = true;
        /*String directory;
        boolean isRingtone = false;
        boolean isNotification = false;
        boolean isAlarm = false;
        switch (type) {
            case RingtoneManager.TYPE_RINGTONE:
                //directory = Environment.DIRECTORY_RINGTONES;
                isRingtone = true;
                break;
            case RingtoneManager.TYPE_NOTIFICATION:
                //directory = Environment.DIRECTORY_NOTIFICATIONS;
                isNotification = true;
                break;
            case RingtoneManager.TYPE_ALARM:
                //directory = Environment.DIRECTORY_ALARMS;
                isAlarm = true;
                break;
            default:
                return false;
        }*/

        //File path = Environment.getExternalStoragePublicDirectory(directory);
        File path;
        if (Build.VERSION.SDK_INT < 29)
            path = context.getFilesDir();
        else
            path = context.getExternalFilesDir(null);
        //PPApplication.logE("TonesHandler._installTone", "path=" + path.getAbsolutePath());
        //noinspection ResultOfMethodCallIgnored
        path.mkdirs();
        String filename = context.getResources().getResourceEntryName(resID) + ".ogg";
        File outFile = new File(path, filename);

        boolean isError = false;

        //if (!outFile.exists()) {

            // Write the file
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            //noinspection TryFinallyCanBeTryWithResources
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
                Log.e("TonesHandler._installTone", "Error writing " + filename, e);
                //FirebaseCrashlytics.getInstance().recordException(e);
                Crashlytics.logException(e);
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
                Log.e("TonesHandler._installTone", "Error writing " + filename);
                //FirebaseCrashlytics.getInstance().log("TonesHandler._installTone - Error writing " + filename);
                Crashlytics.log("TonesHandler._installTone - Error writing " + filename);
                isError = true;
            }
            else {
                if (!outFile.setReadable(true, false)) {
                    Log.e("TonesHandler._installTone", "Error setting readable to all " + filename);
                    //FirebaseCrashlytics.getInstance().log("TonesHandler._installTone - Error setting readable to all " + filename);
                    Crashlytics.log("TonesHandler._installTone - Error setting readable to all " + filename);
                }
            }
        //}

        if (!isError) {

            try {
                String mimeType = "audio/ogg";

                // Set the file metadata
                String outAbsPath = outFile.getAbsolutePath();

                Uri contentUri;
                if (Build.VERSION.SDK_INT < 29)
                    contentUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
                else
                    contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

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

                try {
                    context.getContentResolver().delete(contentUri, MediaStore.MediaColumns.DATA + "='" + outAbsPath + "'", null);
                } catch (Exception eee) {
                    Crashlytics.logException(eee);
                }

                Uri newUri = context.getContentResolver().insert(contentUri, contentValues);

                if (newUri != null) {
                    //Log.d("TonesHandler","inserted to resolver");

                    // Tell the media scanner about the new ringtone
                    MediaScannerConnection.scanFile(
                            context,
                            new String[]{newUri.toString()},
                            new String[]{mimeType},
                            null
                            //new MediaScannerConnection.OnScanCompletedListener() {
                            //    @Override
                            //    public void onScanCompleted(String path, Uri uri) {
                            //        PPApplication.logE("TonesHandler._installTone","scanFile completed");
                            //        PPApplication.logE("TonesHandler._installTone","path="+path);
                            //        PPApplication.logE("TonesHandler._installTone","uri="+uri);
                            //    }
                            //}
                    );

                    //try { Thread.sleep(300); } catch (InterruptedException e) { }
                    //SystemClock.sleep(300);
                    PPApplication.sleep(500);
                }
                else {
                    Log.e("TonesHandler._installTone","newUri is empty");
                    //FirebaseCrashlytics.getInstance().log("TonesHandler._installTone - newUri is empty");
                    Crashlytics.log("TonesHandler._installTone - newUri is empty");
                    isError = true;
                }

                /*
                Cursor cursor = context.getContentResolver().query(contentUri,
                        new String[]{MediaStore.MediaColumns.DATA},
                        MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null, null);
                PPApplication.logE("TonesHandler._installTone", "cursor=" + cursor);
                if (cursor != null) {
                    if (!cursor.moveToFirst()) {
                        // not exists in content
                        PPApplication.logE("TonesHandler._installTone", "not exists in content resolver");

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
                            //Log.d("TonesHandler","inserted to resolver");

                            // Tell the media scanner about the new ringtone
                            MediaScannerConnection.scanFile(
                                    context,
                                    new String[]{newUri.toString()},
                                    new String[]{mimeType},
                                    null
                                    //new MediaScannerConnection.OnScanCompletedListener() {
                                    //    @Override
                                    //    public void onScanCompleted(String path, Uri uri) {
                                    //        PPApplication.logE("TonesHandler._installTone","scanFile completed");
                                    //        PPApplication.logE("TonesHandler._installTone","path="+path);
                                    //        PPApplication.logE("TonesHandler._installTone","uri="+uri);
                                    //    }
                                    //}
                            );

                            //try { Thread.sleep(300); } catch (InterruptedException e) { }
                            //SystemClock.sleep(300);
                            PPApplication.sleep(500);
                        }
                        else {
                            Log.e("TonesHandler._installTone","newUri is empty");
                            cursor.close();
                            isError = true;
                        }
                    } else {
                        Log.e("TonesHandler","exists in content resolver");
                        cursor.close();
                    }
                }
                */
            } catch (Exception e) {
                Log.e("TonesHandler._installTone", "Error installing tone " + filename, e);
                //FirebaseCrashlytics.getInstance().recordException(e);
                Crashlytics.logException(e);
                isError = true;
            }
        }

        //if (!isError)
        //    PPApplication.logE("TonesHandler._installTone", "Tone installed: " + filename);

        return !isError;
    }

    static void installTone(@SuppressWarnings("SameParameterValue") int resID,
                            @SuppressWarnings("SameParameterValue") String title,
                            Context context) {

        //boolean granted = Permissions.grantInstallTonePermissions(context.getApplicationContext());
        //if (granted) {
            boolean ringtone = _installTone(resID, /*RingtoneManager.TYPE_RINGTONE,*/ title, context.getApplicationContext());
            //boolean notification = _installTone(resID, RingtoneManager.TYPE_NOTIFICATION, title, context.getApplicationContext());
            //boolean alarm = _installTone(resID, RingtoneManager.TYPE_ALARM, title, context.getApplicationContext());
            int strId = R.string.toast_tone_installation_installed_ok;
            if (!(ringtone/* && notification && alarm*/))
                strId = R.string.toast_tone_installation_installed_error;

        GlobalGUIRoutines.showToast(context.getApplicationContext(),
                    context.getResources().getString(strId),
                    Toast.LENGTH_SHORT);
        //}
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

    /*
    static Uri uriFromRaw(String name, Context context) {
        int resId = context.getResources().getIdentifier(name, "raw", context.getPackageName());
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
    }
    */
}
