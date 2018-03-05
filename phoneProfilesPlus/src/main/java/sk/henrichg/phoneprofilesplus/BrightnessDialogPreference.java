package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

public class BrightnessDialogPreference extends
        DialogPreference implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private Context _context = null;
    private MaterialDialog mDialog;

    // Layout widgets.
    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox noChangeChBox = null;
    private CheckBox automaticChBox = null;
    private CheckBox defaultProfileChBox = null;

    // Custom xml attributes.
    private int noChange = 0;
    private int automatic = 0;
    private int defaultProfile = 0;
    private int disableDefaultProfile = 0;

    @SuppressWarnings("FieldCanBeLocal")
    private final int maximumValue = 100;
    private final int minimumValue = 0;
    private final int stepSize = 1;

    private String sValue = "";
    private int value = 0;

    private boolean adaptiveAllowed = true;
    private final Profile _defaultProfile;

    private final int savedBrightness;
    private float savedAdaptiveBrightness;
    private final int savedBrightnessMode;

    public BrightnessDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.BrightnessDialogPreference);

        noChange = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bNoChange, 1);
        automatic = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bAutomatic, 1);
        defaultProfile = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bDefaultProfile, 0);
        disableDefaultProfile = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bDisableDefaultProfile, 0);

        typedArray.recycle();

        _defaultProfile = Profile.getDefaultProfile(_context);

        adaptiveAllowed = (android.os.Build.VERSION.SDK_INT <= 21) ||
                (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, _context)
                        == PPApplication.PREFERENCE_ALLOWED);

        savedBrightness = Settings.System.getInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 128);
        savedBrightnessMode = Settings.System.getInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
            savedAdaptiveBrightness = Settings.System.getFloat(_context.getContentResolver(), ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, 0f);
    }

    @Override
    protected void showDialog(Bundle state) {

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                        //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .content(getDialogMessage())
                .customView(R.layout.activity_brightness_pref_dialog, true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist()) {
                            persistString(Integer.toString(value + minimumValue)
                                    + "|" + Integer.toString(noChange)
                                    + "|" + Integer.toString(automatic)
                                    + "|" + Integer.toString(defaultProfile));
                            setSummaryBDP();
                        }
                    }
                });

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BrightnessDialogPreference.this.onShow(/*dialog*/);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        //noinspection ConstantConditions
        seekBar = layout.findViewById(R.id.brightnessPrefDialogSeekbar);
        //noinspection ConstantConditions
        valueText = layout.findViewById(R.id.brightnessPrefDialogValueText);
        //noinspection ConstantConditions
        noChangeChBox = layout.findViewById(R.id.brightnessPrefDialogNoChange);
        //noinspection ConstantConditions
        automaticChBox = layout.findViewById(R.id.brightnessPrefDialogAutomatic);
        //noinspection ConstantConditions
        defaultProfileChBox = layout.findViewById(R.id.brightnessPrefDialogDefaultProfile);

        if (android.os.Build.VERSION.SDK_INT >= 21) { // for Android 5.0: adaptive brightness
            String text = _context.getString(R.string.preference_profile_adaptiveBrightness);
            automaticChBox.setText(text);
        }

        if (adaptiveAllowed) {
            layout.findViewById(R.id.brightnessPrefDialogAdaptiveLevelRoot).setVisibility(View.GONE);
        }

        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setKeyProgressIncrement(stepSize);
        seekBar.setMax(maximumValue - minimumValue);

        getValueBDP();

        seekBar.setProgress(value);

        noChangeChBox.setOnCheckedChangeListener(this);
        noChangeChBox.setChecked((noChange == 1));

        automaticChBox.setOnCheckedChangeListener(this);
        automaticChBox.setChecked(automatic == 1);

        defaultProfileChBox.setOnCheckedChangeListener(this);
        defaultProfileChBox.setChecked((defaultProfile == 1));
        defaultProfileChBox.setEnabled(disableDefaultProfile == 0);

        if (noChange == 1)
            defaultProfileChBox.setChecked(false);
        if (defaultProfile == 1)
            noChangeChBox.setChecked(false);

        enableViews();

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);


        mDialog.setOnDismissListener(this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.show();
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    void enableViews() {
        if (Permissions.checkScreenBrightness(_context, null)) {
            valueText.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && (defaultProfile == 0));
            seekBar.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && (defaultProfile == 0));
            automaticChBox.setEnabled((noChange == 0) && (defaultProfile == 0));
        }
        else {
            valueText.setEnabled(false);
            seekBar.setEnabled(false);
            automaticChBox.setEnabled(false);
        }
    }

    private void onShow(/*DialogInterface dialog*/) {
        if (Permissions.grantBrightnessDialogPermissions(_context, this))
            enableViews();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (Permissions.checkScreenBrightness(_context, null)) {
            Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, savedBrightnessMode);
            Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, savedBrightness);
            setAdaptiveBrightness(savedAdaptiveBrightness);
        }

        Window win = ((Activity)_context).getWindow();
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        if (savedBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            layoutParams.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        else
            layoutParams.screenBrightness = savedBrightness / (float) 255;
        win.setAttributes(layoutParams);

        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.brightnessPrefDialogNoChange)
        {
            noChange = (isChecked)? 1 : 0;

            enableViews();
            //valueText.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && (defaultProfile == 0));
            //seekBar.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && (defaultProfile == 0));
            //automaticChBox.setEnabled((noChange == 0) && (defaultProfile == 0));
            if (isChecked)
                defaultProfileChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogDefaultProfile)
        {
            defaultProfile = (isChecked)? 1 : 0;

            enableViews();
            //valueText.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && (defaultProfile == 0));
            //seekBar.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && (defaultProfile == 0));
            //automaticChBox.setEnabled((noChange == 0) && (defaultProfile == 0));
            if (isChecked)
                noChangeChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogAutomatic)
        {
            automatic = (isChecked)? 1 : 0;

            enableViews();
            //valueText.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && (defaultProfile == 0));
            //seekBar.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && (defaultProfile == 0));
        }

        // get values from defaultProfile when default profile checkbox is checked
        int _automatic = automatic;
        int _noChange = noChange;
        int _value = value;
        if (defaultProfile == 1)
        {
            _automatic = (_defaultProfile.getDeviceBrightnessAutomatic()) ? 1 : 0;
            _noChange = (_defaultProfile.getDeviceBrightnessChange()) ? 0 : 1;
            _value = _defaultProfile.getDeviceBrightnessValue();

            /*
            isAutomatic = (_automatic == 1);
            if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                isAutomatic = false;  // enable change value via seek bar
            */
        }

        if (/*(isAutomatic) || */(_noChange == 1))
        {
            if (Permissions.checkScreenBrightness(_context, null)) {
                Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, savedBrightnessMode);
                Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, savedBrightness);
                setAdaptiveBrightness(savedAdaptiveBrightness);
            }

            Window win = ((Activity)_context).getWindow();
            WindowManager.LayoutParams layoutParams = win.getAttributes();
            if (savedBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
                layoutParams.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            else
                layoutParams.screenBrightness = savedBrightness / (float) 255;
            win.setAttributes(layoutParams);
        }
        else
        {
            if (Permissions.checkScreenBrightness(_context, null)) {
                if (_automatic == 1)
                    Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                else
                    Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                        Profile.convertPercentsToBrightnessManualValue(_value + minimumValue, _context));
                setAdaptiveBrightness(Profile.convertPercentsToBrightnessAdaptiveValue(_value + minimumValue, _context));
            }

            Window win = ((Activity)_context).getWindow();
            WindowManager.LayoutParams layoutParams = win.getAttributes();
            if (_automatic == 1)
                layoutParams.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            else
                layoutParams.screenBrightness = Profile.convertPercentsToBrightnessManualValue(_value + minimumValue, _context) / (float) 255;
            win.setAttributes(layoutParams);
        }

        callChangeListener(noChange);
    }

    private void setBrightnessFromSeekBar(int value) {
        if (Permissions.checkScreenBrightness(_context, null)) {
            if (automatic == 1)
                Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            else
                Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                    Profile.convertPercentsToBrightnessManualValue(value + minimumValue, _context));
            setAdaptiveBrightness(Profile.convertPercentsToBrightnessAdaptiveValue(value + minimumValue, _context));
        }

        Window win = ((Activity)_context).getWindow();
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        if (automatic == 1)
            layoutParams.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        else
            layoutParams.screenBrightness = Profile.convertPercentsToBrightnessManualValue(value + minimumValue, _context) / (float) 255;
        win.setAttributes(layoutParams);
    }

    public void onProgressChanged(SeekBar seek, int newValue,
                                  boolean fromUser) {
        // Round the value to the closest integer value.
        //noinspection ConstantConditions
        if (stepSize >= 1) {
            value = Math.round(newValue/stepSize)*stepSize;
        }
        else {
            value = newValue;
        }

        // Set the valueText text.
        valueText.setText(String.valueOf(value));

        if ((!fromUser) || (android.os.Build.VERSION.SDK_INT < 23)){
            setBrightnessFromSeekBar(value);
        }

        callChangeListener(value);
    }

    public void onStartTrackingTouch(SeekBar seek) {
    }

    public void onStopTrackingTouch(SeekBar seek) {
        if ((android.os.Build.VERSION.SDK_INT >= 23))
            setBrightnessFromSeekBar(value);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueBDP();
        }
        else {
            // set state
            value = 50;
            noChange = 1;
            automatic = 1;
            defaultProfile = 0;
            persistString(Integer.toString(value + minimumValue)
                    + "|" + Integer.toString(noChange)
                    + "|" + Integer.toString(automatic)
                    + "|" + Integer.toString(defaultProfile));
        }
        setSummaryBDP();
    }

    private void getValueBDP()
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString(sValue);

        String[] splits = sValue.split("\\|");
        try {
            value = Integer.parseInt(splits[0]);
            if (value == -1)
                value = 50;
            if (value == Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
                // brightness is not set, change it to default adaptive brightness value
                value = Math.round(savedAdaptiveBrightness * 50 + 50);
        } catch (Exception e) {
            value = 50;
        }
        value = value - minimumValue;
        try {
            noChange = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            noChange = 1;
        }
        try {
            automatic = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            automatic = 1;
        }
        try {
            defaultProfile = Integer.parseInt(splits[3]);
        } catch (Exception e) {
            defaultProfile = 0;
        }

        //value = getPersistedInt(minimumValue) - minimumValue;

        // You're never know...
        if (value < 0) {
            value = 0;
        }
    }

    private void setSummaryBDP()
    {
        String prefVolumeDataSummary;
        if (noChange == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_no_change);
        else
        if (defaultProfile == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_default_profile);
        else
        {
            String sValue = String.valueOf(value) + " / 100";
            if (automatic == 1)
            {
                if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                    prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_adaptiveBrightness);
                else
                    prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_autobrightness);
                prefVolumeDataSummary = prefVolumeDataSummary + "; " + sValue;
            }
            else
                prefVolumeDataSummary = sValue;
        }
        setSummary(prefVolumeDataSummary);
    }

    private void setAdaptiveBrightness(final float value) {
        if (adaptiveAllowed) {
            if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                Settings.System.putFloat(_context.getContentResolver(),
                        ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, value);
            else {
                try {
                    Settings.System.putFloat(_context.getContentResolver(),
                            ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, value);
                } catch (Exception ee) {
                    if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
                        PPApplication.startHandlerThread();
                        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME + " " +
                                            Float.toString(value);
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command);
                                    } catch (Exception e) {
                                        Log.e("BrightnessDialogPreference.setAdaptiveBrightness", Log.getStackTraceString(e));
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split("\\|");
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final SavedState myState = new SavedState(superState);

        Dialog dialog = getDialog();
        //noinspection StatementWithEmptyBody
        if (dialog == null || !dialog.isShowing()) {
            //myState.isDialogShowing = superState.isDialogShowing;
            //myState.dialogBundle = dialogBundle;
        }
        else {
            myState.isDialogShowing = true;
            myState.dialogBundle = dialog.onSaveInstanceState();
        }

        // save instance state
        myState.value = value;
        myState.noChange = noChange;
        myState.automatic = automatic;
        myState.defaultProfile = defaultProfile;
        myState.disableDefaultProfile = disableDefaultProfile;
        return myState;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryBDP();
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        noChange = myState.noChange;
        automatic = myState.automatic;
        defaultProfile = myState.defaultProfile;
        disableDefaultProfile = myState.disableDefaultProfile;

        setSummaryBDP();
        notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        boolean isDialogShowing;
        Bundle dialogBundle;
        int value = 0;
        int noChange = 0;
        int automatic = 0;
        int defaultProfile = 0;
        int disableDefaultProfile = 0;

        @SuppressLint("ParcelClassLoader")
        SavedState(Parcel source)
        {
            super(source);

            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();

            value = source.readInt();
            noChange = source.readInt();
            automatic = source.readInt();
            defaultProfile = source.readInt();
            disableDefaultProfile = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeInt(isDialogShowing ? 1 : 0);
            dest.writeBundle(dialogBundle);

            dest.writeInt(value);
            dest.writeInt(noChange);
            dest.writeInt(automatic);
            dest.writeInt(defaultProfile);
            dest.writeInt(disableDefaultProfile);
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

}
