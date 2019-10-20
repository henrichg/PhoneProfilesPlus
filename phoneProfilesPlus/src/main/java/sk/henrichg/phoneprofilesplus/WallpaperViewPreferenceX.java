package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.File;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class WallpaperViewPreferenceX extends Preference {

    private String imageIdentifier;
    //private Bitmap bitmap;

    private final Context prefContext;

    static final int RESULT_LOAD_IMAGE = 1970;

    public WallpaperViewPreferenceX(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        imageIdentifier = "-";

        prefContext = context;

        //preferenceTitle = getTitle();

        setWidgetLayoutResource(R.layout.imageview_preference);
        //setLayoutResource(R.layout.imageview_preference);
    }

    //@Override
    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        final ImageView imageView = (ImageView) holder.findViewById(R.id.imageview_pref_imageview);

        //imageTitle = view.findViewById(R.id.imageview_pref_label);
        //imageTitle.setText(preferenceTitle);

        new AsyncTask<Void, Integer, Void>() {
            Bitmap bitmap;

            @Override
            protected Void doInBackground(Void... params) {
                bitmap = getBitmap();
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
                if (imageView != null)
                {
                    if (bitmap != null)
                        imageView.setImageBitmap(bitmap);
                    else
                        imageView.setImageResource(R.drawable.ic_empty);
                }
            }

        }.execute();
    }

    @Override
    protected void onClick()
    {
        if (Permissions.grantWallpaperPermissions(prefContext))
            startGallery();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);  // icon is returned as string
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        imageIdentifier = getPersistedString((String)defaultValue);
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.imageIdentifier = imageIdentifier;
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
        imageIdentifier = myState.imageIdentifier;
        //notifyChanged();
    }

    private Bitmap getBitmap() {
        if (!imageIdentifier.startsWith("-")) {
            int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            return BitmapManipulator.resampleBitmapUri(imageIdentifier, width, height, false, true, prefContext);
        }
        else
            return null;
    }

    void setImageIdentifier(String newImageIdentifier)
    {
        if (!callChangeListener(newImageIdentifier)) {
            return;
        }

        imageIdentifier = newImageIdentifier;
        //Log.d("---- WallpaperViewPreference.setImageIdentifier","getBitmap");
        //getBitmap();

        // save to preferences
        persistString(newImageIdentifier);

        // and notify
        notifyChanged();

    }

    void startGallery()
    {
        Intent intent;
        try {
            //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            //}else
            //    intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");

            // is not possible to get activity from preference, used is static method
            //ProfilesPrefsFragment.setChangedWallpaperViewPreference(this);
            ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } catch (Exception ignored) {}
        /*} catch (ActivityNotFoundException e) {
            try {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");

                // is not possible to get activity from preference, used is static method
                ProfilesPrefsFragment.setChangedWallpaperViewPreference(this);
                ((Activity) prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
            } catch (Exception ignored) {}
        }*/
    }


    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String imageIdentifier;

        SavedState(Parcel source)
        {
            super(source);

            // restore image identifier
            imageIdentifier = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save image identifier and type
            dest.writeString(imageIdentifier);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<WallpaperViewPreferenceX.SavedState>() {
            public WallpaperViewPreferenceX.SavedState createFromParcel(Parcel in)
            {
                return new WallpaperViewPreferenceX.SavedState(in);
            }
            public WallpaperViewPreferenceX.SavedState[] newArray(int size)
            {
                return new WallpaperViewPreferenceX.SavedState[size];
            }

        };

    }

//---------------------------------------------------------------------------------------------

    static Uri getImageContentUri(Context context, String imageFile) {
        Cursor cursor = context.getApplicationContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { imageFile }, null);
        //PPApplication.logE("WallpaperViewPreferenceX.getImageContentUri","cursor="+cursor);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
            PPApplication.logE("WallpaperViewPreferenceX.getImageContentUri","uri1="+uri);
            return uri;
        } else {
            if (cursor != null)
                cursor.close();
            File file = new File(imageFile);
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, imageFile);
                Uri uri = context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    ContentResolver resolver = context.getApplicationContext().getContentResolver();
                    resolver.takePersistableUriPermission(uri, takeFlags);
                }*/
                PPApplication.logE("WallpaperViewPreferenceX.getImageContentUri","uri2="+uri);
                return uri;
            } else {
                return null;
            }
        }
    }

}
