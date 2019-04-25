package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import androidx.preference.PreferenceDialogFragmentCompat;

public class BrightnessDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
                implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener{

    private Context context;
    private BrightnessDialogPreferenceX preference;

    // Layout widgets.
    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox noChangeChBox = null;
    private CheckBox automaticChBox = null;
    private CheckBox sharedProfileChBox = null;
    private CheckBox changeLevelChBox = null;
    private TextView levelText = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        this.context = context;
        preference = (BrightnessDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_brightness_pref_dialog, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        seekBar = view.findViewById(R.id.brightnessPrefDialogSeekbar);
        valueText = view.findViewById(R.id.brightnessPrefDialogValueText);
        noChangeChBox = view.findViewById(R.id.brightnessPrefDialogNoChange);
        automaticChBox = view.findViewById(R.id.brightnessPrefDialogAutomatic);
        sharedProfileChBox = view.findViewById(R.id.brightnessPrefDialogSharedProfile);
        changeLevelChBox = view.findViewById(R.id.brightnessPrefDialogLevel);
        levelText = view.findViewById(R.id.brightnessPrefDialogAdaptiveLevelRoot);


        //if (android.os.Build.VERSION.SDK_INT >= 21) { // for Android 5.0: adaptive brightness
        automaticChBox.setText(R.string.preference_profile_adaptiveBrightness);
        //}

        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setKeyProgressIncrement(preference.stepSize);
        seekBar.setMax(preference.maximumValue - preference.minimumValue);

        seekBar.setProgress(preference.value);

        noChangeChBox.setOnCheckedChangeListener(this);
        noChangeChBox.setChecked((preference.noChange == 1));

        automaticChBox.setOnCheckedChangeListener(this);
        automaticChBox.setChecked(preference.automatic == 1);

        sharedProfileChBox.setOnCheckedChangeListener(this);
        sharedProfileChBox.setChecked((preference.sharedProfile == 1));
        sharedProfileChBox.setEnabled(preference.disableSharedProfile == 0);

        changeLevelChBox.setOnCheckedChangeListener(this);
        changeLevelChBox.setChecked(preference.changeLevel == 1);

        if (preference.noChange == 1)
            sharedProfileChBox.setChecked(false);
        if (preference.sharedProfile == 1)
            noChangeChBox.setChecked(false);

        enableViews();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            preference.persistValue();
        }

        if (Permissions.checkScreenBrightness(context, null)) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, preference.savedBrightnessMode);
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, preference.savedBrightness);
            setAdaptiveBrightness(preference.savedAdaptiveBrightness);
        }

        Window win = ((Activity)context).getWindow();
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        if (preference.savedBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        else
            layoutParams.screenBrightness = preference.savedBrightness / (float) 255;
        win.setAttributes(layoutParams);


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.brightnessPrefDialogNoChange)
        {
            preference.noChange = (isChecked)? 1 : 0;

            enableViews();

            if (isChecked)
                sharedProfileChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogSharedProfile)
        {
            preference.sharedProfile = (isChecked)? 1 : 0;

            enableViews();

            if (isChecked)
                noChangeChBox.setChecked(false);
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogAutomatic)
        {
            preference.automatic = (isChecked)? 1 : 0;

            enableViews();
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogLevel)
        {
            preference.changeLevel = (isChecked)? 1 : 0;

            enableViews();
        }

        // get values from sharedProfile when shared profile checkbox is checked
        int _automatic = preference.automatic;
        int _noChange = preference.noChange;
        int _value = preference.value;
        int _changeLevel = preference.changeLevel;
        if (preference.sharedProfile == 1)
        {
            _automatic = (preference._sharedProfile.getDeviceBrightnessAutomatic()) ? 1 : 0;
            _noChange = (preference._sharedProfile.getDeviceBrightnessChange()) ? 0 : 1;
            _value = preference._sharedProfile.getDeviceBrightnessValue();
            _changeLevel = (preference._sharedProfile.getDeviceBrightnessChangeLevel()) ? 1 : 0;
        }

        if (_noChange == 1)
        {
            if (Permissions.checkScreenBrightness(context, null)) {
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, preference.savedBrightnessMode);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, preference.savedBrightness);
                setAdaptiveBrightness(preference.savedAdaptiveBrightness);
            }

            Window win = ((Activity)context).getWindow();
            WindowManager.LayoutParams layoutParams = win.getAttributes();
            if (preference.savedBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            else
                layoutParams.screenBrightness = preference.savedBrightness / (float) 255;
            win.setAttributes(layoutParams);
        }
        else
        {
            if (Permissions.checkScreenBrightness(context, null)) {
                if (_automatic == 1)
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                if (_changeLevel == 1) {
                    //PPApplication.logE("BrightnessDialogPreference.onCheckedChanged", "putInt value="+
                    //        Profile.convertPercentsToBrightnessManualValue(_value + minimumValue, _context));
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                            Profile.convertPercentsToBrightnessManualValue(_value + preference.minimumValue, context));
                    setAdaptiveBrightness(Profile.convertPercentsToBrightnessAdaptiveValue(_value + preference.minimumValue, context));
                }
            }

            Window win = ((Activity)context).getWindow();
            WindowManager.LayoutParams layoutParams = win.getAttributes();
            if (_automatic == 1)
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            else {
                if (_changeLevel == 1)
                    layoutParams.screenBrightness = Profile.convertPercentsToBrightnessManualValue(_value + preference.minimumValue, context) / (float) 255;
                else
                    layoutParams.screenBrightness = preference.savedBrightness / (float) 255;
            }
            win.setAttributes(layoutParams);
        }

        preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Round the value to the closest integer value.
        //noinspection ConstantConditions
        if (preference.stepSize >= 1) {
            preference.value = Math.round((float)progress/preference.stepSize)*preference.stepSize;
        }
        else {
            preference.value = progress;
        }

        // Set the valueText text.
        valueText.setText(String.valueOf(preference.value));

        if ((!fromUser) || (android.os.Build.VERSION.SDK_INT < 23)){
            setBrightnessFromSeekBar(preference.value);
        }

        preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if ((android.os.Build.VERSION.SDK_INT >= 23))
            setBrightnessFromSeekBar(preference.value);
    }

    void enableViews() {
        if (Permissions.checkScreenBrightness(context, null)) {
            valueText.setEnabled((preference.adaptiveAllowed || preference.automatic == 0) && (preference.noChange == 0) && (preference.sharedProfile == 0) && (preference.changeLevel != 0));
            seekBar.setEnabled((preference.adaptiveAllowed || preference.automatic == 0) && (preference.noChange == 0) && (preference.sharedProfile == 0) && (preference.changeLevel != 0));
            automaticChBox.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
            changeLevelChBox.setEnabled((preference.adaptiveAllowed || preference.automatic == 0) && (preference.noChange == 0) && (preference.sharedProfile == 0));
            if (preference.adaptiveAllowed) {
                //if (android.os.Build.VERSION.SDK_INT >= 21) { // for Android 5.0: adaptive brightness
                levelText.setText(R.string.brightness_pref_dialog_adaptive_level_may_not_working);
                levelText.setEnabled((preference.automatic != 0) && (preference.noChange == 0) && (preference.sharedProfile == 0) && (preference.changeLevel != 0));
                //} else
                //    levelText.setVisibility(View.GONE);
            } else {
                levelText.setEnabled((preference.automatic != 0) && (preference.noChange == 0) && (preference.sharedProfile == 0) && (preference.changeLevel != 0));
            }
        } else {
            valueText.setEnabled(false);
            seekBar.setEnabled(false);
            automaticChBox.setEnabled(false);
            changeLevelChBox.setEnabled(false);
            levelText.setEnabled(false);
        }
    }

    private void setAdaptiveBrightness(final float value) {
        if (preference.adaptiveAllowed) {
            if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                Settings.System.putFloat(context.getContentResolver(),
                        ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, value);
            else {
                try {
                    Settings.System.putFloat(context.getContentResolver(),
                            ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, value);
                } catch (Exception ee) {
                    PPApplication.startHandlerThread("BrightnessDialogPreference.setAdaptiveBrightness");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BrightnessDialogPreference.setAdaptiveBrightness");

                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
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

    private void setBrightnessFromSeekBar(int value) {
        if (Permissions.checkScreenBrightness(context, null)) {
            if (preference.automatic == 1)
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            else
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            if (preference.changeLevel == 1) {
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                        Profile.convertPercentsToBrightnessManualValue(value + preference.minimumValue, context));
                setAdaptiveBrightness(Profile.convertPercentsToBrightnessAdaptiveValue(value + preference.minimumValue, context));
            }
        }

        Window win = ((Activity)context).getWindow();
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        if (preference.automatic == 1)
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        else {
            if (preference.changeLevel == 1)
                layoutParams.screenBrightness = Profile.convertPercentsToBrightnessManualValue(value + preference.minimumValue, context) / (float) 255;
            else
                layoutParams.screenBrightness = preference.savedBrightness / (float) 255;
        }
        win.setAttributes(layoutParams);
    }

}
