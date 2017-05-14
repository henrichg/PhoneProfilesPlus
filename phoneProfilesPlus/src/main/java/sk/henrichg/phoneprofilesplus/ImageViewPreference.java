package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class ImageViewPreference extends Preference {

    private String imageIdentifier;

    private Context prefContext;

    static int RESULT_LOAD_IMAGE = 1970;

    public ImageViewPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        imageIdentifier = "-";

        prefContext = context;

        //preferenceTitle = getTitle();

        setWidgetLayoutResource(R.layout.imageview_preference); // resource na layout custom preference - TextView-ImageView
        //setLayoutResource(R.layout.imageview_preference); // resource na layout custom preference - TextView-ImageView
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
            Resources resources = prefContext.getResources();
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            Bitmap bitmap = BitmapManipulator.resampleBitmapUri(imageIdentifier, width, height, prefContext);

            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
            else
                imageView.setImageResource(R.drawable.ic_empty);
        }
    }

    @Override
    protected void onClick()
    {
        if (Permissions.grantWallpaperPermissions(prefContext, this))
            startGallery();
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
            imageIdentifier = getPersistedString(imageIdentifier);
        }
        else {
            // set state
            imageIdentifier = (String) defaultValue;
            persistString(imageIdentifier);
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
        notifyChanged();
    }

    void setImageIdentifier(String newImageIdentifier)
    {
        String newValue = newImageIdentifier;

        if (!callChangeListener(newValue)) {
            // nema sa nova hodnota zapisat
            return;
        }

        imageIdentifier = newImageIdentifier;

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

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

}
