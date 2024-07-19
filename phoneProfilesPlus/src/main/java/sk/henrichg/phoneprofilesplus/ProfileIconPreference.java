package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import java.io.File;
import java.lang.ref.WeakReference;

public class ProfileIconPreference extends DialogPreference {

    ProfileIconPreferenceFragment fragment;

    private String value;
    private String defaultValue;
    private boolean savedInstanceState;

    String imageIdentifier;
    boolean isImageResourceID;
    boolean useCustomColor;
    int customColor;
    //private Bitmap bitmap;

    private ImageView imageView;
    final Context prefContext;

    private UpdateIconAsyncTask updateIconAsyncTask = null;

    static final int RESULT_LOAD_IMAGE = 1971;

    //private static final String PREF_SHOW_HELP = "profile_icon_pref_show_help";

    public ProfileIconPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        /*
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ProfileIconPreference);

        // resource, resource_file, file
        imageSource = typedArray.getString(
            R.styleable.ProfileIconPreference_iconSource);
        */

        imageIdentifier = StringConstants.PROFILE_ICON_DEFAULT;
        isImageResourceID = true;
        useCustomColor = false;
        customColor = 0;

        prefContext = context;

        setWidgetLayoutResource(R.layout.preference_widget_profileicon_preference); // resource na layout custom preference - TextView-ImageView

        //typedArray.recycle();

    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        imageView = (ImageView) holder.findViewById(R.id.profileicon_pref_imageview);
        updateIcon(false, null);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);
        return a.getString(index);  // icon is returned as string
    }

    @Override
    public void onDetached() {
        super.onDetached();
//        Log.e("ProfileIconPreference.onDetached", "xxxxxxxxxxxx");
        if ((updateIconAsyncTask != null) &&
                updateIconAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            updateIconAsyncTask.cancel(true);
        updateIconAsyncTask = null;
    }

    Bitmap getBitmap() {
        int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
        int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
        return BitmapManipulator.resampleBitmapUri(imageIdentifier, width, height, true, false, prefContext);
    }

    void getValuePIDP() {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        //noinspection UnreachableCode
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = StringConstants.PROFILE_ICON_DEFAULT;
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
            customColor = ProfileStatic.getIconDefaultColor(imageIdentifier/*, prefContext*/);
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
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            getValuePIDP();
            updateIcon(false, null);
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
        String[] splits = newValue.split(StringConstants.STR_SPLIT_REGEX);
        //noinspection UnreachableCode
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = StringConstants.PROFILE_ICON_DEFAULT;
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
        if (fragment != null) {
            fragment.setCustomColor(/*useCustomColor, customColor*/);
            updateIcon(true, fragment);
        }
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
            updateIcon(false, null);
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
            intent.setType(StringConstants.MIME_TYPE_IMAGE);

                boolean ok = false;
                if (!isImageResourceID) {
                    try {
                        Uri picturesUri = Uri.parse(imageIdentifier);
                        if (picturesUri != null) {
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, picturesUri);
                        }
                        ok = true;
                    } catch (Exception ignored) {
                    }
                }
                if (!ok) {
                    try {
                        File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        String fileName = pictures.getName();
                        Uri picturesUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:" + fileName);
                        if (picturesUri != null) {
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, picturesUri);
                        }
                    } catch (Exception ignored) {
                    }
                }

            // is not possible to get activity from preference, used is static method
            //ProfilesPrefsFragment.setChangedProfileIconPreference(this);
            ((Activity)prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        /*} catch (ActivityNotFoundException e) {
            try {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType(StringConstants.MIME_TYPE_IMAGE);

                // is not possible to get activity from preference, used is static method
                ProfilesPrefsFragment.setChangedProfileIconPreference(this);
                ((Activity) prefContext).startActivityForResult(intent, RESULT_LOAD_IMAGE);
            } catch (Exception ignored) {}
        }*/
    }

    void updateIcon(final boolean inDialog, final ProfileIconPreferenceFragment fragment) {
        updateIconAsyncTask = new UpdateIconAsyncTask(inDialog, this, fragment, prefContext);
        updateIconAsyncTask.execute();
    }

    void dismissDialog() {
        if (fragment != null)
            fragment.dismiss();
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final ProfileIconPreference.SavedState myState = new ProfileIconPreference.SavedState(superState);
        setValue(false);
        myState.value = value;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if ((state == null) || (!state.getClass().equals(ProfileIconPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // restore instance state
        ProfileIconPreference.SavedState myState = (ProfileIconPreference.SavedState)state;
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

        public static final Creator<ProfileIconPreference.SavedState> CREATOR =
                new Creator<ProfileIconPreference.SavedState>() {
                    public ProfileIconPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new ProfileIconPreference.SavedState(in);
                    }
                    public ProfileIconPreference.SavedState[] newArray(int size)
                    {
                        return new ProfileIconPreference.SavedState[size];
                    }

                };

    }

    private static class UpdateIconAsyncTask extends AsyncTask<Void, Integer, Void> {

        //ImageView _imageView;
        Bitmap bitmap;

        private final WeakReference<ProfileIconPreference> preferenceWeakRef;
        private final WeakReference<ProfileIconPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;
        final boolean inDialog;

        public UpdateIconAsyncTask(final boolean inDialog,
                ProfileIconPreference preference,
                ProfileIconPreferenceFragment fragment,
                Context prefContext) {
            this.inDialog = inDialog;
            this.preferenceWeakRef = new WeakReference<>(preference);
            if (fragment != null)
                this.fragmentWeakRef = new WeakReference<>(fragment);
            else
                this.fragmentWeakRef = null;
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        /*
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            //ProfileIconPreference preference = preferenceWeakRef.get();
            //if (preference != null) {
            //    if (inDialog)
            //        _imageView = preference.dialogIcon;
            //    else
            //        _imageView = preference.imageView;
            //}
        }
        */

        @Override
        protected Void doInBackground(Void... params) {
            ProfileIconPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {
                if (preference.isImageResourceID) {
                    // je to resource id

                    int res = ProfileStatic.getIconResource(preference.imageIdentifier);
                    bitmap = BitmapManipulator.getBitmapFromResource(res, true, prefContext);

                    if (preference.useCustomColor)
                        bitmap = BitmapManipulator.recolorBitmap(bitmap, preference.customColor/*, prefContext*/);

                    Bitmap _bitmap = increaseProfileIconBrightness(bitmap, preference);
                    if (_bitmap != null)
                        bitmap = _bitmap;
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

            ProfileIconPreference preference = preferenceWeakRef.get();
            ProfileIconPreferenceFragment fragment = null;
            if (fragmentWeakRef != null)
                fragment = fragmentWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((preference != null) && (prefContext != null)) {
                ImageView _imageView;
                if (inDialog && (fragment != null))
                    _imageView = fragment.dialogIcon;
                else
                    _imageView = preference.imageView;

                if (_imageView != null) {
                    if (bitmap != null)
                        _imageView.setImageBitmap(bitmap);
                    else {
                        _imageView.setImageResource(R.drawable.ic_profile_default);
                    }
                }
            }
        }

        Bitmap increaseProfileIconBrightness(Bitmap iconBitmap, ProfileIconPreference preference) {
            //if (ApplicationPreferences.applicationIncreaseBrightnessForProfileIcon) {
            try {
                if (preference != null) {
                    //boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(preference.prefContext.getApplicationContext());
                    //(preference.prefContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    //== Configuration.UI_MODE_NIGHT_YES;
                    String applicationTheme = ApplicationPreferences.applicationTheme(preference.prefContext, true);
                    boolean nightModeOn = !applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE);

                    if (nightModeOn) {
                        int iconColor;
                        if (preference.isImageResourceID) {
                            if (preference.useCustomColor)
                                iconColor = preference.customColor;
                            else
                                iconColor = ProfileStatic.getIconDefaultColor(preference.imageIdentifier);
                        } else {
                            //iconColor = BitmapManipulator.getDominantColor(_iconBitmap);
                            Palette palette = Palette.from(iconBitmap).generate();
                            iconColor = palette.getDominantColor(0xff1c9cd7);
                        }
                        if (ColorUtils.calculateLuminance(iconColor) < Profile.MIN_PROFILE_ICON_LUMINANCE) {
                            if (iconBitmap != null) {
                                return BitmapManipulator.setBitmapBrightness(iconBitmap, Profile.BRIGHTNESS_VALUE_FOR_DARK_MODE);
                            } else {
                                int iconResource = ProfileStatic.getIconResource(preference.imageIdentifier);
                                Bitmap bitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, preference.prefContext);
                                return BitmapManipulator.setBitmapBrightness(bitmap, Profile.BRIGHTNESS_VALUE_FOR_DARK_MODE);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            //}
            return null;
        }

    }

}
