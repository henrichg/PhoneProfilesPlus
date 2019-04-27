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
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import androidx.appcompat.app.AlertDialog;

public class BrightnessDialogPreference extends
        DialogPreference implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private final Context _context;
    private AlertDialog mDialog;

    // Layout widgets.
    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox noChangeChBox = null;
    private CheckBox automaticChBox = null;
    //private CheckBox sharedProfileChBox = null;
    private CheckBox changeLevelChBox = null;
    private TextView levelText = null;

    // Custom xml attributes.
    private int noChange;
    private int automatic;
    //private int sharedProfile;
    //private int disableSharedProfile;
    private int changeLevel;

    private final int defaultValue = 50;
    private final int maximumValue = 100;
    private final int minimumValue = 0;
    private final int stepSize = 1;

    private String sValue = "";
    private int value = 0;

    private final boolean adaptiveAllowed;
    //private final Profile _sharedProfile;

    private final int savedBrightness;
    private final float savedAdaptiveBrightness;
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
        /*sharedProfile = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bSharedProfile, 0);*/
        /*disableSharedProfile = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bDisableSharedProfile, 0);*/
        changeLevel = typedArray.getInteger(
                R.styleable.BrightnessDialogPreference_bChangeLevel, 1);

        typedArray.recycle();

        //_sharedProfile = Profile.getProfileFromSharedPreferences(_context, PPApplication.SHARED_PROFILE_PREFS_NAME);

        adaptiveAllowed = (android.os.Build.VERSION.SDK_INT <= 21) ||
                (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, true, _context).allowed
                        == PreferenceAllowed.PREFERENCE_ALLOWED);

        /*if (Build.VERSION.SDK_INT >= 28) {
            defaultValue = 24;
            maximumValue = 255;
        }*/
        savedBrightness = Settings.System.getInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                            Profile.convertPercentsToBrightnessManualValue(defaultValue, _context));
        savedBrightnessMode = Settings.System.getInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        //if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
            savedAdaptiveBrightness = Settings.System.getFloat(_context.getContentResolver(), ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, 0f);
    }

    @Override
    protected void showDialog(Bundle state) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist()) {
                    int _value = value + minimumValue;
                    persistString(_value
                            + "|" + noChange
                            + "|" + automatic
                            + "|" + "0"
                            + "|" + changeLevel);
                    setSummaryBDP();
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_brightness_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BrightnessDialogPreference.this.onShow(/*dialog*/);
            }
        });

        seekBar = layout.findViewById(R.id.brightnessPrefDialogSeekbar);
        valueText = layout.findViewById(R.id.brightnessPrefDialogValueText);
        noChangeChBox = layout.findViewById(R.id.brightnessPrefDialogNoChange);
        automaticChBox = layout.findViewById(R.id.brightnessPrefDialogAutomatic);
        //sharedProfileChBox = layout.findViewById(R.id.brightnessPrefDialogSharedProfile);
        changeLevelChBox = layout.findViewById(R.id.brightnessPrefDialogLevel);
        levelText = layout.findViewById(R.id.brightnessPrefDialogAdaptiveLevelRoot);


        //if (android.os.Build.VERSION.SDK_INT >= 21) { // for Android 5.0: adaptive brightness
            automaticChBox.setText(R.string.preference_profile_adaptiveBrightness);
        //}

        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setKeyProgressIncrement(stepSize);
        seekBar.setMax(maximumValue - minimumValue);

        getValueBDP();

        seekBar.setProgress(value);

        noChangeChBox.setOnCheckedChangeListener(this);
        noChangeChBox.setChecked((noChange == 1));

        automaticChBox.setOnCheckedChangeListener(this);
        automaticChBox.setChecked(automatic == 1);

        //sharedProfileChBox.setOnCheckedChangeListener(this);
        //sharedProfileChBox.setChecked((sharedProfile == 1));
        //sharedProfileChBox.setEnabled(disableSharedProfile == 0);

        changeLevelChBox.setOnCheckedChangeListener(this);
        changeLevelChBox.setChecked(changeLevel == 1);

        //if (noChange == 1)
        //    sharedProfileChBox.setChecked(false);
        //if (sharedProfile == 1)
        //    noChangeChBox.setChecked(false);

        //enableViews();

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);


        mDialog.setOnDismissListener(this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        if (!((Activity)_context).isFinishing())
            mDialog.show();
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    void enableViews() {
        if ((mDialog != null) && mDialog.isShowing()) {
            if (Permissions.checkScreenBrightness(_context, null)) {
                valueText.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && /*(sharedProfile == 0) &&*/ (changeLevel != 0));
                seekBar.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) && /*(sharedProfile == 0) &&*/ (changeLevel != 0));
                automaticChBox.setEnabled((noChange == 0) /*&& (sharedProfile == 0)*/);
                changeLevelChBox.setEnabled((adaptiveAllowed || automatic == 0) && (noChange == 0) /*&& (sharedProfile == 0)*/);
                if (adaptiveAllowed) {
                    //if (android.os.Build.VERSION.SDK_INT >= 21) { // for Android 5.0: adaptive brightness
                        levelText.setText(R.string.brightness_pref_dialog_adaptive_level_may_not_working);
                        levelText.setEnabled((automatic != 0) && (noChange == 0) && /*(sharedProfile == 0) &&*/ (changeLevel != 0));
                    //} else
                    //    levelText.setVisibility(View.GONE);
                } else {
                    levelText.setEnabled((automatic != 0) && (noChange == 0) && /*(sharedProfile == 0) &&*/ (changeLevel != 0));
                }
            } else {
                valueText.setEnabled(false);
                seekBar.setEnabled(false);
                automaticChBox.setEnabled(false);
                changeLevelChBox.setEnabled(false);
                levelText.setEnabled(false);
            }
        }
    }

    private void onShow(/*DialogInterface dialog*/) {
        if (Permissions.grantBrightnessDialogPermissions(_context))
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
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.brightnessPrefDialogNoChange)
        {
            noChange = (isChecked)? 1 : 0;

            enableViews();

            //if (isChecked)
            //    sharedProfileChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogSharedProfile)
        {
            //sharedProfile = (isChecked)? 1 : 0;

            enableViews();

            if (isChecked)
                noChangeChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogAutomatic)
        {
            automatic = (isChecked)? 1 : 0;

            enableViews();
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogLevel)
        {
            changeLevel = (isChecked)? 1 : 0;

            enableViews();
        }

        // get values from sharedProfile when shared profile checkbox is checked
        int _automatic = automatic;
        int _noChange = noChange;
        int _value = value;
        int _changeLevel = changeLevel;
        /*if (sharedProfile == 1)
        {
            _automatic = (_sharedProfile.getDeviceBrightnessAutomatic()) ? 1 : 0;
            _noChange = (_sharedProfile.getDeviceBrightnessChange()) ? 0 : 1;
            _value = _sharedProfile.getDeviceBrightnessValue();
            _changeLevel = (_sharedProfile.getDeviceBrightnessChangeLevel()) ? 1 : 0;
        }*/

        if (_noChange == 1)
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
                if (_changeLevel == 1) {
                    //PPApplication.logE("BrightnessDialogPreference.onCheckedChanged", "putInt value="+
                    //        Profile.convertPercentsToBrightnessManualValue(_value + minimumValue, _context));
                    Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                            Profile.convertPercentsToBrightnessManualValue(_value + minimumValue, _context));
                    setAdaptiveBrightness(Profile.convertPercentsToBrightnessAdaptiveValue(_value + minimumValue, _context));
                }
            }

            Window win = ((Activity)_context).getWindow();
            WindowManager.LayoutParams layoutParams = win.getAttributes();
            if (_automatic == 1)
                layoutParams.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            else {
                if (_changeLevel == 1)
                    layoutParams.screenBrightness = Profile.convertPercentsToBrightnessManualValue(_value + minimumValue, _context) / (float) 255;
                else
                    layoutParams.screenBrightness = savedBrightness / (float) 255;
            }
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
            if (changeLevel == 1) {
                Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                        Profile.convertPercentsToBrightnessManualValue(value + minimumValue, _context));
                setAdaptiveBrightness(Profile.convertPercentsToBrightnessAdaptiveValue(value + minimumValue, _context));
            }
        }

        Window win = ((Activity)_context).getWindow();
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        if (automatic == 1)
            layoutParams.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        else {
            if (changeLevel == 1)
                layoutParams.screenBrightness = Profile.convertPercentsToBrightnessManualValue(value + minimumValue, _context) / (float) 255;
            else
                layoutParams.screenBrightness = savedBrightness / (float) 255;
        }
        win.setAttributes(layoutParams);
    }

    public void onProgressChanged(SeekBar seek, int newValue,
                                  boolean fromUser) {
        // Round the value to the closest integer value.
        //noinspection ConstantConditions
        if (stepSize >= 1) {
            value = Math.round((float)newValue/stepSize)*stepSize;
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
            value = this.defaultValue;
            noChange = 1;
            automatic = 1;
            //sharedProfile = 0;
            changeLevel = 1;
            int _value = value + minimumValue;
            persistString(_value
                    + "|" + noChange
                    + "|" + automatic
                    + "|" + "0"
                    + "|" + changeLevel);
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
            if (value == Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET) {
                // brightness is not set, change it to default adaptive brightness value
                int halfValue = maximumValue / 2;
                value = Math.round(savedAdaptiveBrightness * halfValue + halfValue);
            }
            if ((value < 0) || (value > maximumValue)) {
                value = 50;
            }
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
        /*try {
            sharedProfile = Integer.parseInt(splits[3]);
        } catch (Exception e) {
            sharedProfile = 0;
        }*/
        try {
            changeLevel = Integer.parseInt(splits[4]);
        } catch (Exception e) {
            changeLevel = 1;
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
        /*else
        if (sharedProfile == 1)
            prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_default_profile);*/
        else
        {
            if (automatic == 1)
            {
                //if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                    prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_adaptiveBrightness);
                //else
                //    prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_autoBrightness);
            }
            else
                prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_manual_brightness);

            if ((changeLevel == 1) && (adaptiveAllowed || automatic == 0)) {
                String _value = value + " / " + maximumValue;
                prefVolumeDataSummary = prefVolumeDataSummary + "; " + _value;
            }
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
                    PPApplication.startHandlerThread("BrightnessDialogPreference.setAdaptiveBrightness");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BrightnessDialogPreference.setAdaptiveBrightness");

                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(_context)) &&
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME + " " + value;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command);
                                /*} catch (RootDeniedException e) {
                                    PPApplication.rootMutex.rootGranted = false;
                                    Log.e("BrightnessDialogPreference.setAdaptiveBrightness", Log.getStackTraceString(e));*/
                                    } catch (Exception e) {
                                        Log.e("BrightnessDialogPreference.setAdaptiveBrightness", Log.getStackTraceString(e));
                                    }
                                }
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BrightnessDialogPreference.setAdaptiveBrightness");
                        }
                    });
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
        if ((dialog == null) || !dialog.isShowing()) {
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
        //myState.sharedProfile = sharedProfile;
        //myState.disableSharedProfile = disableSharedProfile;
        myState.changeLevel = changeLevel;
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
        //sharedProfile = myState.sharedProfile;
        //disableSharedProfile = myState.disableSharedProfile;
        changeLevel = myState.changeLevel;

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
        //int sharedProfile = 0;
        //int disableSharedProfile = 0;
        int changeLevel = 0;

        @SuppressLint("ParcelClassLoader")
        SavedState(Parcel source)
        {
            super(source);

            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();

            value = source.readInt();
            noChange = source.readInt();
            automatic = source.readInt();
            //sharedProfile = source.readInt();
            //disableSharedProfile = source.readInt();
            changeLevel = source.readInt();
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
            //dest.writeInt(sharedProfile);
            //dest.writeInt(disableSharedProfile);
            dest.writeInt(changeLevel);
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
