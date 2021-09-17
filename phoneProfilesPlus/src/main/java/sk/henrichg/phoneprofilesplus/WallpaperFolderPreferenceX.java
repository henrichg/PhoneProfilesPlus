package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.util.AttributeSet;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;
import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.List;

public class WallpaperFolderPreferenceX extends Preference {

    private String wallpaperFolder;
    //private Bitmap bitmap;

    private final Context prefContext;

    static final int RESULT_GET_FOLDER = 1980;

    public WallpaperFolderPreferenceX(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        wallpaperFolder = "-";

        prefContext = context;
    }

    @Override
    protected void onClick()
    {
        if (Permissions.grantWallpaperFolderPermissions(prefContext))
            startGallery();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        wallpaperFolder = getPersistedString((String)defaultValue);
        if ((wallpaperFolder != null) &&
                (!wallpaperFolder.isEmpty()) &&
                (!wallpaperFolder.equals("-"))) {
            Uri folderUri = Uri.parse(wallpaperFolder);

            try {
                String path;
                path = PPApplication.getRealPath(folderUri);
                setSummary(path);
            } catch (Exception e) {
                setSummary(R.string.preference_profile_no_change);
            }
        }
        else {
            setSummary(R.string.preference_profile_no_change);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.folder = wallpaperFolder;
        return myState;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        wallpaperFolder = myState.folder;
        if ((wallpaperFolder != null) &&
                (!wallpaperFolder.isEmpty()) &&
                (!wallpaperFolder.equals("-"))) {
            Uri folderUri = Uri.parse(wallpaperFolder);

            try {
                String path;
                path = PPApplication.getRealPath(folderUri);
                setSummary(path);
            } catch (Exception e) {
                setSummary(R.string.preference_profile_no_change);
            }
        }

        //notifyChanged();
    }

    void setWallpaperFolder(String newWallpaperFolder)
    {
        if (!callChangeListener(newWallpaperFolder)) {
            return;
        }

        wallpaperFolder = newWallpaperFolder;
        if ((wallpaperFolder != null) &&
                (!wallpaperFolder.isEmpty()) &&
                (!wallpaperFolder.equals("-"))) {
            Uri folderUri = Uri.parse(wallpaperFolder);
            try {
                String path;
                path = PPApplication.getRealPath(folderUri);
                setSummary(path);

                //----------
                // TODO move this to ActivateProfileHepler
                // test get list of files from folder

                prefContext.getApplicationContext().grantUriPermission(PPApplication.PACKAGE_NAME, folderUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION /* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                // persistent permissions
                final int takeFlags = //data.getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                prefContext.getApplicationContext().getContentResolver().takePersistableUriPermission(folderUri, takeFlags);


                List<Uri> uriList = new ArrayList<>();

                // the uri from which we query the files
                Uri uriFolder = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri, DocumentsContract.getTreeDocumentId(folderUri));

                Cursor cursor = null;
                try {
                    // let's query the files
                    ContentResolver contentResolver = prefContext.getApplicationContext().getContentResolver();
                    cursor = contentResolver.query(uriFolder,
                            new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                            null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            // build the uri for the file
                            Uri uriFile = DocumentsContract.buildDocumentUriUsingTree(folderUri, cursor.getString(0));
                            Log.e("WallpaperFolderPreferenceX.setWallpaperFolder", "mime type=" + contentResolver.getType(uriFile));
                            if (contentResolver.getType(uriFile).startsWith("image/")) {
                                //add to the list
                                uriList.add(uriFile);
                            }

                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e("WallpaperFolderPreferenceX.setWallpaperFolder", Log.getStackTraceString(e));
                } finally {
                    if (cursor!=null) cursor.close();
                }

                for (Uri fileUri : uriList) {
                    DocumentFile documentFile = DocumentFile.fromSingleUri(prefContext.getApplicationContext(), fileUri);
                    if (documentFile != null)
                        Log.e("WallpaperFolderPreferenceX.setWallpaperFolder", "documentFile="+documentFile.getName());
                }

                //----------------

            } catch (Exception e) {
                setSummary(R.string.preference_profile_no_change);
            }
        }

        // save to preferences
        persistString(newWallpaperFolder);

        // and notify
        notifyChanged();

    }

    void startGallery()
    {
        Intent intent;
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                StorageManager sm = (StorageManager) prefContext.getSystemService(Context.STORAGE_SERVICE);
                intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
            }
            else {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            }
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //intent.putExtra("android.content.extra.SHOW_ADVANCED",true);
            //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, PPApplication.backupFolderUri);*/

            //noinspection deprecation
            ((Activity)prefContext).startActivityForResult(intent, RESULT_GET_FOLDER);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }


    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String folder;

        SavedState(Parcel source)
        {
            super(source);

            // restore image identifier
            folder = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save image identifier and type
            dest.writeString(folder);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<WallpaperFolderPreferenceX.SavedState>() {
            public WallpaperFolderPreferenceX.SavedState createFromParcel(Parcel in)
            {
                return new WallpaperFolderPreferenceX.SavedState(in);
            }
            public WallpaperFolderPreferenceX.SavedState[] newArray(int size)
            {
                return new WallpaperFolderPreferenceX.SavedState[size];
            }

        };

    }

}
