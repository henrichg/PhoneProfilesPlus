package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ImageViewPreference extends Preference {

    private String imageIdentifier;
    private boolean isImageResourceID;

    private String imageSource;

    private Context prefContext;

    //private CharSequence preferenceTitle;

    static int RESULT_LOAD_IMAGE = 1970;

    public ImageViewPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ImageViewPreference);

        // resource, resource_file, file
        imageSource = typedArray.getString(
            R.styleable.ImageViewPreference_imageSource);


        //noinspection ConstantConditions
        if (imageSource.equals("file"))
        {
            imageIdentifier = "-";
            isImageResourceID = false;
        }
        else
        {
            imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
            isImageResourceID = true;
        }

        prefContext = context;

        //preferenceTitle = getTitle();

        setWidgetLayoutResource(R.layout.imageview_preference); // resource na layout custom preference - TextView-ImageView
        //setLayoutResource(R.layout.imageview_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        //imageTitle = (TextView)view.findViewById(R.id.imageview_pref_label);  // resource na image title
        //imageTitle.setText(preferenceTitle);

        ImageView imageView = (ImageView)view.findViewById(R.id.imageview_pref_imageview); // resource na Textview v custom preference layoute

        if (imageView != null)
        {
            if (isImageResourceID)
            {
                // je to resource id
                int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.getPackageName());
                imageView.setImageResource(res); // resource na ikonu
            }
            else
            {
                // je to file
                Resources resources = prefContext.getResources();
                int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
                int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
                Bitmap bitmap = BitmapManipulator.resampleBitmap(imageIdentifier, width, height, prefContext);

                if (bitmap != null)
                    imageView.setImageBitmap(bitmap);
                else
                    imageView.setImageResource(R.drawable.ic_empty);
            }
        }
    }

    @Override
    protected void onClick()
    {
        // klik na preference

        if (imageSource.equals("resource_file") || imageSource.equals("resource"))
        {
            final ImageViewPreferenceDialog dialog = new ImageViewPreferenceDialog(prefContext, this, imageSource,
                                                                                    imageIdentifier, isImageResourceID);
            dialog.show();
        }
        else
        {
            // zavolat galeriu na vyzdvihnutie image
            if (Permissions.grantWallpaperPermissions(prefContext, this))
                startGallery();
        }

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);  // ikona bude vratena ako retazec
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            String value = getPersistedString(imageIdentifier+"|"+((isImageResourceID) ? "1" : "0"));
            String[] splits = value.split("\\|");
            try {
                imageIdentifier = splits[0];
            } catch (Exception e) {
                imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
            }
            try {
                isImageResourceID = splits[1].equals("1");
            } catch (Exception e) {
                isImageResourceID = true;
            }
        }
        else {
            // set state
            String value = (String) defaultValue;
            String[] splits = value.split("\\|");
            try {
                imageIdentifier = splits[0];
            } catch (Exception e) {
                imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
            }
            try {
                isImageResourceID = splits[1].equals("1");
            } catch (Exception e) {
                isImageResourceID = true;
            }
            persistString(value);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        // ulozime instance state - napriklad kvoli zmene orientacie

        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // netreba ukladat, je ulozene persistentne
            return superState;
        }

        // ulozenie istance state
        final SavedState myState = new SavedState(superState);
        myState.imageIdentifierAndType = imageIdentifier+"|"+((isImageResourceID) ? "1" : "0");
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
        String value = myState.imageIdentifierAndType;
        String[] splits = value.split("\\|");
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
        }
        try {
            isImageResourceID = splits[1].equals("1");
        } catch (Exception e) {
            isImageResourceID = true;
        }
        notifyChanged();
    }

    /*
    public String getImageIdentifier()
    {
        return imageIdentifier;
    }

    public boolean getIsImageResourceID()
    {
        return isImageResourceID;
    }
    */

    void setImageIdentifierAndType(String newImageIdentifier, boolean newIsImageResourceID)
    {
        String newValue = newImageIdentifier+"|"+((newIsImageResourceID) ? "1" : "0");

        if (!callChangeListener(newValue)) {
            // nema sa nova hodnota zapisat
            return;
        }

        String[] splits = newValue.split("\\|");
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
        }
        try {
            isImageResourceID = splits[1].equals("1");
        } catch (Exception e) {
            isImageResourceID = true;
        }

        // zapis do preferences
        persistString(newValue);

        // Data sa zmenili,notifikujeme
        notifyChanged();

    }

    void startGallery()
    {
        //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // hm, neda sa ziskat aktivita z preference, tak vyuzivam static metodu
        ProfilePreferencesFragment.setChangedImageViewPreference(this);
        ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }


    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String imageIdentifierAndType;

        SavedState(Parcel source)
        {
            super(source);

            // restore image identifier and type
            imageIdentifierAndType = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save image identifier and type
            dest.writeString(imageIdentifierAndType);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }

        };

    }

//---------------------------------------------------------------------------------------------

    // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * author paulburke
     */
    @SuppressLint({"NewApi", "LongLogTag"})
    static String getPath(final Context context, final Uri uri) {

        /*Log.e("ImageViewPreference.getPath" + " File -",
                "Authority: " + uri.getAuthority() +
                        ", Fragment: " + uri.getFragment() +
                        ", Port: " + uri.getPort() +
                        ", Query: " + uri.getQuery() +
                        ", Scheme: " + uri.getScheme() +
                        ", Host: " + uri.getHost() +
                        ", Segments: " + uri.getPathSegments().toString()
        );*/

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
