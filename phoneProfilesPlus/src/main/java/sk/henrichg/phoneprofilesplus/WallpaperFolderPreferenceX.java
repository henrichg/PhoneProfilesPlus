package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.Preference;

public class WallpaperFolderPreferenceX extends Preference {

    private String folder;
    //private Bitmap bitmap;

    private final Context prefContext;

    static final int RESULT_GET_FOLDER = 1980;

    public WallpaperFolderPreferenceX(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        folder = "-";

        prefContext = context;

        //preferenceTitle = getTitle();

        setWidgetLayoutResource(R.layout.widget_imageview_preference);
        //setLayoutResource(R.layout.widget_imageview_preference);
    }

    /*
    //@Override
    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        imageView = (ImageView) holder.findViewById(R.id.imageview_pref_imageview);

        //imageTitle = view.findViewById(R.id.imageview_pref_label);
        //imageTitle.setText(preferenceTitle);

        new BindViewAsyncTask(this).execute();
    }
    */

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
        folder = getPersistedString((String)defaultValue);
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.folder = folder;
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
        folder = myState.folder;
        //notifyChanged();
    }

    void startGallery()
    {
        /*
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");

            // is not possible to get activity from preference, used is static method
            //ProfilesPrefsFragment.setChangedWallpaperViewPreference(this);
            ((Activity)prefContext).startActivityForResult(intent, RESULT_GET_FOLDER);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        */
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

    /*
    private static class BindViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        Bitmap bitmap;

        private final WeakReference<WallpaperFolderPreferenceX> preferenceWeakRef;

        public BindViewAsyncTask(WallpaperFolderPreferenceX preference) {
            this.preferenceWeakRef = new WeakReference<>(preference);
        }

        @Override
        protected Void doInBackground(Void... params) {
            WallpaperFolderPreferenceX preference = preferenceWeakRef.get();
            if (preference != null) {
                bitmap = preference.getBitmap();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            WallpaperFolderPreferenceX preference = preferenceWeakRef.get();
            if (preference != null) {
                if (preference.imageView != null) {
                    if (bitmap != null)
                        preference.imageView.setImageBitmap(bitmap);
                    else
                        preference.imageView.setImageResource(R.drawable.ic_empty);
                }
            }
        }

    }
    */

}
