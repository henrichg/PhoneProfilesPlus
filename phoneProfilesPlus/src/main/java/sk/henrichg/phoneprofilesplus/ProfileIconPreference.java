package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

public class ProfileIconPreference extends DialogPreference {

    private String imageIdentifier;
    private boolean isImageResourceID;
    private boolean useCustomColor;
    private int customColor;
    //private Bitmap bitmap;

    private AlertDialog mDialog;
    private ProfileIconColorChooserDialog mColorDialog;
    //private ColorChooserDialog mColorDialog;

    private ImageView imageView;
    private ProfileIconPreferenceAdapter adapter;
    private ImageView dialogIcon;
    private final Context prefContext;
    private Button colorChooserButton;

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


        imageIdentifier = Profile.PROFILE_ICON_DEFAULT;
        isImageResourceID = true;
        useCustomColor = false;
        customColor = 0;

        prefContext = context;

        setWidgetLayoutResource(R.layout.profileicon_preference); // resource na layout custom preference - TextView-ImageView

        //typedArray.recycle();

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        imageView = view.findViewById(R.id.profileicon_pref_imageview);
        updateIcon(false);
    }

    @Override
    protected void showDialog(Bundle state) {
        getValuePIDP();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist()) {
                    setImageIdentifierAndType("", true, true);
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_profileicon_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        //noinspection ConstantConditions
        GridView gridView = layout.findViewById(R.id.profileicon_pref_dlg_gridview);
        adapter = new ProfileIconPreferenceAdapter(prefContext, imageIdentifier, isImageResourceID, useCustomColor, customColor);
        gridView.setAdapter(adapter);
        gridView.setSelection(ProfileIconPreferenceAdapter.getImageResourcePosition(imageIdentifier/*, prefContext*/));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                setImageIdentifierAndType(/*prefContext.getResources().getResourceEntryName(Profile.profileIconId[position]),*/
                        ProfileIconPreferenceAdapter.getImageResourceName(position),
                        true, false);
                adapter.imageIdentifierAndTypeChanged(imageIdentifier, isImageResourceID);
                updateIcon(true);
                colorChooserButton.setEnabled(isImageResourceID);
            }
        });

        dialogIcon = layout.findViewById(R.id.profileicon_pref_dlg_icon);
        updateIcon(true);

        colorChooserButton = layout.findViewById(R.id.profileicon_pref_dlg_change_color);
        //colorChooserButton.setAllCaps(false);
        colorChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomColorChooser();
            }
        });
        colorChooserButton.setEnabled(isImageResourceID);

        /*final ImageView helpIcon = layout.findViewById(R.id.profileicon_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelpPopupWindow.showPopup(helpIcon, prefContext, R.string.profileicon_pref_dialog_info_about_status_bar_icon);
            }
        });*/

        final Button customIconButton = layout.findViewById(R.id.profileicon_pref_dlg_custom_icon);
        //customIconButton.setAllCaps(false);
        customIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Permissions.grantCustomProfileIconPermissions(prefContext)) {
                    startGallery();
                    //mDialog.dismiss();
                }
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)prefContext).isFinishing())
            mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mColorDialog != null) && (mColorDialog.mDialog != null) && mColorDialog.mDialog.isShowing())
            mColorDialog.mDialog.dismiss();
        //if (mColorDialog != null && mColorDialog.isVisible())
        //    mColorDialog.dismiss();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
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
            customColor = ProfileIconPreferenceAdapter.getIconColor(imageIdentifier/*, prefContext*/);
        }

        /*if (!isImageResourceID) {
            //Log.d("---- ProfileIconPreference.getValuePIDP","getBitmap");
            getBitmap();
        }*/
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValuePIDP();
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
            try {
                useCustomColor = splits[2].equals("1");
            } catch (Exception e) {
                useCustomColor = false;
            }
            try {
                customColor = Integer.valueOf(splits[3]);
            } catch (Exception e) {
                customColor = ProfileIconPreferenceAdapter.getIconColor(imageIdentifier/*, prefContext*/);
            }

            //if (!isImageResourceID)
            //    getBitmap();

            persistString(value);
        }
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

    private void showCustomColorChooser() {
        mColorDialog = new ProfileIconColorChooserDialog((Activity)prefContext, this, useCustomColor, customColor,
                                       ProfileIconPreferenceAdapter.getIconColor(imageIdentifier/*, prefContext*/));
        mColorDialog.show();

        /*
        setAllCaps(false); ???
        mColorDialog  = new ColorChooserDialog.Builder(prefContext, R.string.colorChooser_pref_dialog_title)
                .titleSub(R.string.colorChooser_pref_dialog_title)  // title of dialog when viewing shades of a color
                .accentMode(false)  // when true, will display accent palette instead of primary palette
                .doneButton(android.R.string.ok)  // changes label of the done button
                .cancelButton(android.R.string.cancel)  // changes label of the cancel button
                .backButton(R.string.empty_string)  // changes label of the back button
                .preselect(customColor)  // optionally preselects a color
                .dynamicButtonColor(false)  // defaults to true, false will disable changing action buttons' color to currently selected color
                .build();
        mColorDialog.show(); // an AppCompatActivity which implements ColorCallback
        */
    }

    @SuppressLint("StaticFieldLeak")
    private void updateIcon(final boolean inDialog) {
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
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    /*
    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String imageIdentifierAndType;

        public SavedState(Parcel source)
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

        public SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
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
    */

}
