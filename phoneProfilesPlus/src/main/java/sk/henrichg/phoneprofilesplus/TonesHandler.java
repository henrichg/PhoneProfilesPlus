package sk.henrichg.phoneprofilesplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.drakeet.support.toast.ToastCompat;

class TonesHandler {

    static final int TONE_ID = R.raw.phoneprofiles_silent;
    static final String TONE_NAME = "PhoneProfiles Silent";

    static String getPhoneProfilesSilentUri(Context context,
                                            @SuppressWarnings("SameParameterValue") int type) {
        try {
            RingtoneManager manager = new RingtoneManager(context);
            manager.setType(type);
            Cursor cursor = manager.getCursor();

            while (cursor.moveToNext()) {
                String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
                String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);

                String uriId = uri + "/" + id;

                //Log.d("TonesHandler.getPhoneProfilesSilentNotificationUri", "title="+title);
                //Log.d("TonesHandler.getPhoneProfilesSilentNotificationUri", "uriId="+uriId);

                if (title.equals("PhoneProfiles Silent") || title.equals("phoneprofiles_silent"))
                    return uriId;
            }
        } catch (Exception ignored) {}
        return "";
    }

    static String getToneName(Context context,
                              @SuppressWarnings("SameParameterValue") int type,
                              String _uri) {
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(type);
        Cursor cursor = manager.getCursor();

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

    private static boolean  isToneInstalled(int resID, String directory, Context context) {
        // Make sure the shared storage is currently writable
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //Log.d("TonesHandler.isToneInstalled","not writable shared storage");
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
            //Log.d("TonesHandler.isToneInstalled","file not exists");
            return false;
        }

        String outAbsPath = outFile.getAbsolutePath();

        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

        Cursor cursor = context.getContentResolver().query(contentUri,
                new String[]{MediaStore.MediaColumns.DATA},
                MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null, null);
        //Log.d("TonesHandler.isToneInstalled","cursor="+cursor);
        if ((cursor == null) || (!cursor.moveToFirst())) {
            if (cursor != null)
                cursor.close();
            //Log.d("TonesHandler.isToneInstalled","empty cursor");
            return false;
        }
        else {
            //Log.d("TonesHandler.isToneInstalled","DATA="+cursor.getString(0));
            cursor.close();
        }

            /*if (getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_RINGTONE).isEmpty()) {
                Log.d("TonesHandler.isToneInstalled","not in ringtone manager");
                return false;
            }*/

        //Log.d("TonesHandler.isToneInstalled","tone installed");

        return true;
    }

    static boolean isToneInstalled(@SuppressWarnings("SameParameterValue") int resID,
                                   Context context) {
        if (Permissions.checkInstallTone(context, null)) {
            boolean ringtone = isToneInstalled(resID, Environment.DIRECTORY_RINGTONES, context);
            boolean notification = isToneInstalled(resID, Environment.DIRECTORY_NOTIFICATIONS, context);
            boolean alarm = isToneInstalled(resID, Environment.DIRECTORY_ALARMS, context);
            return ringtone && notification && alarm;
        }
        else {
            //Log.d("TonesHandler.isToneInstalled","not granted permission");
            return false;
        }
    }

    private static boolean _installTone(int resID, int type, String title, Context context) {
        try {
            // Make sure the shared storage is currently writable
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                return false;
        } catch (Exception e) {
            return false;
        }

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
                            //Log.d("TonesHandler","inserted to resolver");

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
                            Log.e("TonesHandler._installTone","newUri is empty");
                            cursor.close();
                            isError = true;
                        }
                    } else {
                        //Log.d("TonesHandler","exists in resolver");
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                Log.e("TonesHandler._installTone", "Error installing tone " + filename, e);
                isError = true;
            }
        }

        return !isError;
    }

    static void installTone(@SuppressWarnings("SameParameterValue") int resID,
                            @SuppressWarnings("SameParameterValue") String title,
                            Context context) {

        boolean granted = Permissions.grantInstallTonePermissions(context);
        if (granted) {
            boolean ringtone = _installTone(resID, RingtoneManager.TYPE_RINGTONE, title, context);
            boolean notification = _installTone(resID, RingtoneManager.TYPE_NOTIFICATION, title, context);
            boolean alarm = _installTone(resID, RingtoneManager.TYPE_ALARM, title, context);
            int strId = R.string.toast_tone_installation_installed_ok;
            if (!(ringtone && notification && alarm))
                strId = R.string.toast_tone_installation_installed_error;

            Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                    context.getResources().getString(strId),
                    Toast.LENGTH_SHORT);
            msg.show();
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
