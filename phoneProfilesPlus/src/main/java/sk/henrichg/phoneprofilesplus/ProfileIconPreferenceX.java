package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import java.lang.ref.WeakReference;

public class ProfileIconPreferenceX extends DialogPreference {

    ProfileIconPreferenceFragmentX fragment;

    private String value;
    private String defaultValue;
    private boolean savedInstanceState;

    String imageIdentifier;
    boolean isImageResourceID;
    boolean useCustomColor;
    int customColor;
    //private Bitmap bitmap;

    private ImageView imageView;
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

        setWidgetLayoutResource(R.layout.widget_profileicon_preference); // resource na layout custom preference - TextView-ImageView

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
        int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
        int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
        return BitmapManipulator.resampleBitmapUri(imageIdentifier, width, height, true, false, prefContext);
    }

    void getValuePIDP() {
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
            customColor = Integer.parseInt(splits[3]);
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
            //setImageIdentifierAndType("", true, true);
            setValue(true);
        }
    }

    void resetSummary() {
        //PPApplication.logE("ProfileIconPreferenceX.resetSummary", "savedInstanceState="+savedInstanceState);

        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            getValuePIDP();
            updateIcon(false);
        }
        savedInstanceState = false;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;

        getValuePIDP();
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

        setValue(false);
    }

    void setCustomColor(boolean newUseCustomColor, int newCustomColor) {
        useCustomColor = newUseCustomColor;
        customColor = newCustomColor;
        if (fragment != null)
            fragment.setCustomColor(/*useCustomColor, customColor*/);
        updateIcon(true);
        setValue(false);
    }

    void setValue(boolean saveToPreference) {
        /*if (!newIsImageResourceID) {
            imageIdentifier = newImageIdentifier;
            isImageResourceID = false;
            useCustomColor = false;
            customColor = 0;
            //Log.d("---- ProfileIconPreference.setImageIdentifierAndType","getBitmap");
            //getBitmap();
        }*/
        value = imageIdentifier+"|"+((isImageResourceID) ? "1" : "0")+"|"+((useCustomColor) ? "1" : "0")+"|"+customColor;
        if (saveToPreference) {
            updateIcon(false);
            if (callChangeListener(value)) {
                persistString(value);
                notifyChanged();
            }
        }
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
            //ProfilesPrefsFragment.setChangedProfileIconPreference(this);
            ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        /*} catch (ActivityNotFoundException e) {
            try {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");

                // is not possible to get activity from preference, used is static method
                ProfilesPrefsFragment.setChangedProfileIconPreference(this);
                ((Activity) prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
            } catch (Exception ignored) {}
        }*/
    }

    @SuppressLint("StaticFieldLeak")
    void updateIcon(final boolean inDialog) {
        new UpdateIconAsyncTask(inDialog, this, prefContext).execute();
    }

    void dismissDialog() {
        if (fragment != null)
            fragment.dismiss();
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        //PPApplication.logE("ProfileIconPreferenceX.onSaveInstanceState", "xxx");

        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final ProfileIconPreferenceX.SavedState myState = new ProfileIconPreferenceX.SavedState(superState);
        setValue(false);
        myState.value = value;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //PPApplication.logE("ProfileIconPreferenceX.onRestoreInstanceState", "xxx");

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
        value = myState.value;
        defaultValue = myState.defaultValue;

        getValuePIDP();
        //updateIcon(true);
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

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

    private static class UpdateIconAsyncTask extends AsyncTask<Void, Integer, Void> {

        ImageView _imageView;
        Bitmap bitmap;

        private final WeakReference<ProfileIconPreferenceX> preferenceWeakRef;
        private final WeakReference<Context> prefContextWeakRef;
        final boolean inDialog;

        public UpdateIconAsyncTask(final boolean inDialog,
                ProfileIconPreferenceX preference,
                Context prefContext) {
            this.inDialog = inDialog;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            ProfileIconPreferenceX preference = preferenceWeakRef.get();
            if (preference != null) {
                if (inDialog)
                    _imageView = preference.dialogIcon;
                else
                    _imageView = preference.imageView;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ProfileIconPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {
                if (preference.isImageResourceID) {
                    // je to resource id
                    if (preference.useCustomColor) {
                        //int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.PPApplication.PACKAGE_NAME);
                        int res = Profile.getIconResource(preference.imageIdentifier);
                        //bitmap = BitmapFactory.decodeResource(prefContext.getResources(), res);
                        bitmap = BitmapManipulator.getBitmapFromResource(res, true, prefContext);
                        bitmap = BitmapManipulator.recolorBitmap(bitmap, preference.customColor/*, prefContext*/);
                    }
                } else {
                    // je to file
                    bitmap = preference.getBitmap();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ProfileIconPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {
                if (_imageView != null) {
                    if (preference.isImageResourceID) {
                        // je to resource id
                        if (preference.useCustomColor)
                            _imageView.setImageBitmap(bitmap);
                        else {
                            //int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.PPApplication.PACKAGE_NAME);
                            int res = Profile.getIconResource(preference.imageIdentifier);
                            _imageView.setImageResource(res); // icon resource
                        }
                    } else {
                        // je to file
                        if (bitmap != null)
                            _imageView.setImageBitmap(bitmap);
                        else
                            _imageView.setImageResource(R.drawable.ic_profile_default);
                    }
                }
            }
        }

    }

}
