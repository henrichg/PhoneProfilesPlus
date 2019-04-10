package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class ProfileIconPreferenceX extends DialogPreference {

    ProfileIconPreferenceFragmentX fragment;

    String imageIdentifier;
    boolean isImageResourceID;
    boolean useCustomColor;
    int customColor;
    //private Bitmap bitmap;

    private ImageView imageView;
    ProfileIconPreferenceAdapterX adapter;
    ImageView dialogIcon;
    private final Context prefContext;

    static final int RESULT_LOAD_IMAGE = 1971;

    //private static final String PREF_SHOW_HELP = "profile_icon_pref_show_help";

    public ProfileIconPreferenceX(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        /*
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ProfileIconPreference);

        // resource, resource_file, file
        imageSource = typedArray.getString(
            R.styleable.ProfileIconPreference_iconSource);
        */


        imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
        isImageResourceID = true;
        useCustomColor = false;
        customColor = 0;

        prefContext = context;

        setWidgetLayoutResource(R.layout.profileicon_preference); // resource na layout custom preference - TextView-ImageView

        //typedArray.recycle();

    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        imageView = (ImageView) holder.findViewById(R.id.profileicon_pref_imageview);
        updateIcon(false);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);
        return a.getString(index);  // icon is returned as string
    }

    private Bitmap getBitmap() {
        Resources resources = prefContext.getResources();
        int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
        int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
        return BitmapManipulator.resampleBitmapUri(imageIdentifier, width, height, true, false, prefContext);
    }

    private void getValuePIDP() {
        String value = getPersistedString(imageIdentifier+"|"+((isImageResourceID) ? "1" : "0")+"|"+((useCustomColor) ? "1" : "0")+"|"+customColor);
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
        try {
            useCustomColor = splits[2].equals("1");
        } catch (Exception e) {
            useCustomColor = false;
        }
        try {
            customColor = Integer.valueOf(splits[3]);
        } catch (Exception e) {
            customColor = ProfileIconPreferenceAdapterX.getIconColor(imageIdentifier/*, prefContext*/);
        }

        /*if (!isImageResourceID) {
            //Log.d("---- ProfileIconPreference.getValuePIDP","getBitmap");
            getBitmap();
        }*/
    }

    void persistIcon() {
        if (shouldPersist()) {
            setImageIdentifierAndType("", true, true);
        }
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        getValuePIDP();
    }

    /*
    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

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
        String value = (String) myState.imageIdentifierAndType;
        String[] splits = value.split("\\|");
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = PPApplication.PROFILE_ICON_DEFAULT;
        }
        try {
            isImageResourceID = splits[1].equals("1");
        } catch (Exception e) {
            isImageResourceID = true;
        }
        notifyChanged();
    }
    */

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

    void setImageIdentifierAndType(String newImageIdentifier, boolean newIsImageResourceID, boolean saveToPreference)
    {
        String newValue = newImageIdentifier+"|"+((newIsImageResourceID) ? "1" : "0");

        if (!saveToPreference) {
            if (!imageIdentifier.equals(newImageIdentifier)) {
                useCustomColor = false;
                customColor = 0;
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
        }

        if (saveToPreference) {
            if (!newIsImageResourceID) {
                imageIdentifier = newImageIdentifier;
                isImageResourceID = false;
                useCustomColor = false;
                customColor = 0;
                //Log.d("---- ProfileIconPreference.setImageIdentifierAndType","getBitmap");
                //getBitmap();
            }
            newValue = imageIdentifier+"|"+((isImageResourceID) ? "1" : "0")+"|"+((useCustomColor) ? "1" : "0")+"|"+customColor;
            if (callChangeListener(newValue)) {
                persistString(newValue);
                notifyChanged();
            }
        }

    }

    void setCustomColor(boolean newUseCustomColor, int newCustomColor) {
        useCustomColor = newUseCustomColor;
        customColor = newCustomColor;
        adapter.setCustomColor(useCustomColor, customColor);
        updateIcon(true);
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
            //ProfilePreferencesFragment.setChangedProfileIconPreference(this);
            ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } catch (Exception ignored) {}
        /*} catch (ActivityNotFoundException e) {
            try {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");

                // is not possible to get activity from preference, used is static method
                ProfilePreferencesFragment.setChangedProfileIconPreference(this);
                ((Activity) prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
            } catch (Exception ignored) {}
        }*/
    }

    @SuppressLint("StaticFieldLeak")
    void updateIcon(final boolean inDialog) {
        new AsyncTask<Void, Integer, Void>() {

            ImageView _imageView;
            Bitmap bitmap;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                if (inDialog)
                    _imageView = dialogIcon;
                else
                    _imageView = imageView;
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (isImageResourceID)
                {
                    // je to resource id
                    if (useCustomColor) {
                        //int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.getPackageName());
                        int res = Profile.getIconResource(imageIdentifier);
                        bitmap = BitmapFactory.decodeResource(prefContext.getResources(), res);
                        bitmap = BitmapManipulator.recolorBitmap(bitmap, customColor/*, prefContext*/);
                    }
                }
                else
                {
                    // je to file
                    bitmap = getBitmap();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
                if (_imageView != null)
                {
                    if (isImageResourceID)
                    {
                        // je to resource id
                        if (useCustomColor)
                            _imageView.setImageBitmap(bitmap);
                        else {
                            //int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.getPackageName());
                            int res = Profile.getIconResource(imageIdentifier);
                            _imageView.setImageResource(res); // icon resource
                        }
                    }
                    else
                    {
                        // je to file
                        if (bitmap != null)
                            _imageView.setImageBitmap(bitmap);
                        else
                            _imageView.setImageResource(R.drawable.ic_profile_default);
                    }
                }
            }

        }.execute();
    }

    void dismissDialog() {
        if (fragment != null)
            fragment.dismiss();
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final ProfileIconPreferenceX.SavedState myState = new ProfileIconPreferenceX.SavedState(superState);
        myState.imageIdentifier = imageIdentifier;
        myState.isImageResourceID = isImageResourceID;
        myState.useCustomColor = useCustomColor;
        myState.customColor = customColor;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(ProfileIconPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        ProfileIconPreferenceX.SavedState myState = (ProfileIconPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        imageIdentifier = myState.imageIdentifier;
        isImageResourceID = myState.isImageResourceID;
        useCustomColor = myState.useCustomColor;
        customColor = myState.customColor;
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String imageIdentifier;
        boolean isImageResourceID;
        boolean useCustomColor;
        int customColor;

        SavedState(Parcel source)
        {
            super(source);

            imageIdentifier = source.readString();
            isImageResourceID = source.readInt() == 1;
            useCustomColor = source.readInt() == 1;
            customColor = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(imageIdentifier);
            dest.writeInt(isImageResourceID ? 1 : 0);
            dest.writeInt(useCustomColor ? 1 : 0);
            dest.writeInt(customColor);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<ProfileIconPreferenceX.SavedState> CREATOR =
                new Creator<ProfileIconPreferenceX.SavedState>() {
                    public ProfileIconPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ProfileIconPreferenceX.SavedState(in);
                    }
                    public ProfileIconPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ProfileIconPreferenceX.SavedState[size];
                    }

                };

    }

}
