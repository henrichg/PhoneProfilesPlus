package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class ProfileIconPreference extends DialogPreference {

    private String imageIdentifier;
    private boolean isImageResourceID;
    private boolean useCustomColor;
    private int customColor;

    private MaterialDialog mDialog;

    private ImageView imageView;
    ProfileIconPreferenceAdapter adapter;
    private ImageView dialogIcon;
    private Context prefContext;
    private Button colorChooserButton;

    static int RESULT_LOAD_IMAGE = 1971;

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


        imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
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

        imageView = (ImageView)view.findViewById(R.id.profileicon_pref_imageview); // resource na Textview v custom preference layoute
        updateIcon(false);
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                        //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.imageview_resource_file_pref_dialog_gallery_btn)
                .autoDismiss(false)
                .content(getDialogMessage())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist()) {
                            setImageIdentifierAndType("", true, true);
                        }
                        mDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        // zavolat galeriu na vyzdvihnutie image
                        if (Permissions.grantCustomProfileIconPermissions(prefContext, ProfileIconPreference.this)) {
                            startGallery();
                            mDialog.dismiss();
                        }
                    }
                });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_profileicon_pref_dialog, null);
        onBindDialogView(layout);

        getValuePIDP();

        GridView gridView = (GridView)layout.findViewById(R.id.profileicon_pref_dlg_gridview);
        adapter = new ProfileIconPreferenceAdapter(prefContext, imageIdentifier, isImageResourceID, useCustomColor, customColor);
        gridView.setAdapter(adapter);
        gridView.setSelection(ProfileIconPreferenceAdapter.getImageResourcePosition(imageIdentifier));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                setImageIdentifierAndType(Profile.profileIconId[position], true, false);
                adapter.imageIdentifierAndTypeChanged(imageIdentifier, isImageResourceID);
                updateIcon(true);
                if (isImageResourceID)
                    colorChooserButton.setVisibility(View.VISIBLE);
                else
                    colorChooserButton.setVisibility(View.GONE);
            }
        });

        dialogIcon = (ImageView)layout.findViewById(R.id.profileicon_pref_dlg_icon);
        updateIcon(true);

        colorChooserButton = (Button)layout.findViewById(R.id.profileicon_pref_dlg_change_color);
        colorChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomColorChooser();
            }
        });
        if (isImageResourceID)
            colorChooserButton.setVisibility(View.VISIBLE);
        else
            colorChooserButton.setVisibility(View.GONE);

        mBuilder.customView(layout, false);

        final TextView helpText = (TextView)layout.findViewById(R.id.profileicon_pref_dlg_helpText);

        ImageView helpIcon = (ImageView)layout.findViewById(R.id.profileicon_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE)
                    visibility = View.GONE;
                else
                    visibility = View.VISIBLE;
                helpText.setVisibility(visibility);
            }
        });

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);  // ikona bude vratena ako retazec
    }

    private void getValuePIDP() {
        String value = getPersistedString(imageIdentifier+"|"+((isImageResourceID) ? "1" : "0")+"|"+((useCustomColor) ? "1" : "0")+"|"+customColor);
        String[] splits = value.split("\\|");
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
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
            customColor = ProfileIconPreferenceAdapter.getIconColor(imageIdentifier);
        }
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
                imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
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
                customColor = ProfileIconPreferenceAdapter.getIconColor(imageIdentifier);
            }
            persistString(value);
        }
    }

    /*
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
        String value = (String) myState.imageIdentifierAndType;
        String[] splits = value.split("\\|");
        try {
            imageIdentifier = splits[0];
        } catch (Exception e) {
            imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
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
                imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
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
            }
            newValue = imageIdentifier+"|"+((isImageResourceID) ? "1" : "0")+"|"+((useCustomColor) ? "1" : "0")+"|"+customColor;
            if (callChangeListener(newValue)) {
                persistString(newValue);
                // Data sa zmenili,notifikujeme
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
        //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // hm, neda sa ziskat aktivita z preference, tak vyuzivam static metodu
        ProfilePreferencesFragment.setChangedProfileIconPreference(this);
        ProfilePreferencesFragment.getPreferencesActivity().startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    private void showCustomColorChooser() {
        final ProfileIconColorChooserDialog dialog = new ProfileIconColorChooserDialog(prefContext, this, useCustomColor, customColor,
                                                            ProfileIconPreferenceAdapter.getIconColor(imageIdentifier));
        dialog.show();
    }

    private void updateIcon(boolean inDialog) {
        ImageView imageView;
        if (inDialog)
            imageView = this.dialogIcon;
        else
            imageView = this.imageView;

        if (imageView != null)
        {
            if (isImageResourceID)
            {
                // je to resource id
                int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.getPackageName());

                if (useCustomColor) {
                    Bitmap bitmap = BitmapFactory.decodeResource(prefContext.getResources(), res);
                    bitmap = BitmapManipulator.recolorBitmap(bitmap, customColor/*, prefContext*/);
                    imageView.setImageBitmap(bitmap);
                }
                else
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
                    imageView.setImageResource(R.drawable.ic_profile_default);
            }
        }
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
