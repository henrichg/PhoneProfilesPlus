package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.PreferenceDialogFragmentCompat;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

@SuppressWarnings("WeakerAccess")
public class BrightnessDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
                implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener{

    private Context context;
    private BrightnessDialogPreferenceX preference;

    // Layout widgets.
    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox automaticChBox = null;
    //private CheckBox sharedProfileChBox = null;
    private CheckBox changeLevelChBox = null;
    private TextView levelText = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        this.context = context;
        preference = (BrightnessDialogPreferenceX) getPreference();
        preference.fragment = this;

        ActivateProfileHelper.brightnessDialogInternalChange = true;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_brightness_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        seekBar = view.findViewById(R.id.brightnessPrefDialogSeekbar);
        valueText = view.findViewById(R.id.brightnessPrefDialogValueText);
        CheckBox noChangeChBox = view.findViewById(R.id.brightnessPrefDialogNoChange);
        automaticChBox = view.findViewById(R.id.brightnessPrefDialogAutomatic);
        //sharedProfileChBox = view.findViewById(R.id.brightnessPrefDialogSharedProfile);
        changeLevelChBox = view.findViewById(R.id.brightnessPrefDialogLevel);
        levelText = view.findViewById(R.id.brightnessPrefDialogAdaptiveLevelRoot);


        //if (android.os.Build.VERSION.SDK_INT >= 21) { // for Android 5.0: adaptive brightness
        automaticChBox.setText(R.string.preference_profile_adaptiveBrightness);
        //}

        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setKeyProgressIncrement(1/*preference.stepSize*/);
        seekBar.setMax(preference.maximumValue/* - preference.minimumValue*/);

        seekBar.setProgress(preference.value);

        noChangeChBox.setOnCheckedChangeListener(this);
        noChangeChBox.setChecked((preference.noChange == 1));

        automaticChBox.setOnCheckedChangeListener(this);
        automaticChBox.setChecked(preference.automatic == 1);

        //sharedProfileChBox.setOnCheckedChangeListener(this);
        //sharedProfileChBox.setChecked((preference.sharedProfile == 1));
        //sharedProfileChBox.setEnabled(preference.disableSharedProfile == 0);

        changeLevelChBox.setOnCheckedChangeListener(this);
        changeLevelChBox.setChecked(preference.changeLevel == 1);

        //if (preference.noChange == 1)
        //    sharedProfileChBox.setChecked(false);
        //if (preference.sharedProfile == 1)
        //    noChangeChBox.setChecked(false);

        if (Permissions.grantBrightnessDialogPermissions(context))
            enableViews();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        ActivateProfileHelper.brightnessDialogInternalChange = false;

        if (Permissions.checkScreenBrightness(context, null)) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, SettingsContentObserver.savedBrightnessMode);
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, SettingsContentObserver.savedBrightness);
            setAdaptiveBrightness(SettingsContentObserver.savedAdaptiveBrightness);
        }

        /*
        Window win = ((Activity)context).getWindow();
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        //if (preference.savedBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        //else
        //    layoutParams.screenBrightness = preference.savedLayoutParamsBrightness;
        win.setAttributes(layoutParams);
        */

        preference.fragment = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //PPApplication.logE("BrightnessDialogPreferenceFragmentX.onCheckedChanged", "xxx");

        if (buttonView.getId() == R.id.brightnessPrefDialogNoChange)
        {
            //PPApplication.logE("BrightnessDialogPreferenceFragmentX.onCheckedChanged", "brightnessPrefDialogNoChange");
            preference.noChange = (isChecked)? 1 : 0;

            enableViews();

            //if (isChecked)
            //    sharedProfileChBox.setChecked(false);
        }

        /*
        if (buttonView.getId() == R.id.brightnessPrefDialogSharedProfile)
        {
            preference.sharedProfile = (isChecked)? 1 : 0;

            enableViews();

            if (isChecked)
                noChangeChBox.setChecked(false);
        }
        */

        if (buttonView.getId() == R.id.brightnessPrefDialogAutomatic)
        {
            //PPApplication.logE("BrightnessDialogPreferenceFragmentX.onCheckedChanged", "brightnessPrefDialogAutomatic");
            preference.automatic = (isChecked)? 1 : 0;

            enableViews();
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogLevel)
        {
            //PPApplication.logE("BrightnessDialogPreferenceFragmentX.onCheckedChanged", "brightnessPrefDialogLevel");
            preference.changeLevel = (isChecked)? 1 : 0;

            enableViews();
        }

        // get values from sharedProfile when shared profile checkbox is checked
        int _automatic = preference.automatic;
        int _noChange = preference.noChange;
        int _value = preference.value;
        int _changeLevel = preference.changeLevel;
        /*if (preference.sharedProfile == 1)
        {
            _automatic = (preference._sharedProfile.getDeviceBrightnessAutomatic()) ? 1 : 0;
            _noChange = (preference._sharedProfile.getDeviceBrightnessChange()) ? 0 : 1;
            _value = preference._sharedProfile.getDeviceBrightnessValue();
            _changeLevel = (preference._sharedProfile.getDeviceBrightnessChangeLevel()) ? 1 : 0;
        }*/

        //PPApplication.logE("BrightnessDialogPreferenceFragmentX.onCheckedChanged", "_noChange="+_noChange);
        if (_noChange == 1)
        {
            if (Permissions.checkScreenBrightness(context, null)) {
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, SettingsContentObserver.savedBrightnessMode);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, SettingsContentObserver.savedBrightness);
                setAdaptiveBrightness(SettingsContentObserver.savedAdaptiveBrightness);
            }

            /*
            Window win = ((Activity)context).getWindow();
            WindowManager.LayoutParams layoutParams = win.getAttributes();
            //if (preference.savedBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            //else
            //    layoutParams.screenBrightness = preference.savedLayoutParamsBrightness;
            win.setAttributes(layoutParams);
            */
        }
        else
        {
            if (Permissions.checkScreenBrightness(context, null)) {
                if (_automatic == 1)
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                if (_changeLevel == 1) {
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("BrightnessDialogPreferenceFragmentX.onCheckedChanged", "_value=" + _value);
                        PPApplication.logE("BrightnessDialogPreferenceFragmentX.onCheckedChanged", "computed value=" +
                                                        Profile.convertPercentsToBrightnessManualValue(_value, context));
                    }*/
                    boolean allowed = true;
                    if (_automatic == 1)
                        allowed = preference.adaptiveAllowed;
                    if (allowed) {
                        int __value = Profile.convertPercentsToBrightnessManualValue(_value, context);
                        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, __value);
                        setAdaptiveBrightness(Profile.convertPercentsToBrightnessAdaptiveValue(_value, context));
                    }
                }
            }

            /*
            Window win = ((Activity)context).getWindow();
            WindowManager.LayoutParams layoutParams = win.getAttributes();
            //if (_automatic == 1)
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            //else {
            //    if (_changeLevel == 1)
            //        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            //        //layoutParams.screenBrightness = Profile.convertPercentsToBrightnessManualValue(_value, context) / (float) 255;
            //    else
            //        layoutParams.screenBrightness = preference.savedLayoutParamsBrightness;
            //}
            win.setAttributes(layoutParams);
            */
        }

        preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Round the value to the closest integer value.
        //if (preference.stepSize >= 1) {
        //    preference.value = Math.round((float)progress/preference.stepSize)*preference.stepSize;
        //}
        //else {
            preference.value = progress;
        //}

        // Set the valueText text.
        valueText.setText(String.valueOf(preference.value));

        if ((!fromUser) /*|| (android.os.Build.VERSION.SDK_INT < 23)*/){
            setBrightnessFromSeekBar(preference.value);
        }

        preference.callChangeListener(preference.getSValue());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //if ((android.os.Build.VERSION.SDK_INT >= 23))
            setBrightnessFromSeekBar(preference.value);
    }

    void enableViews() {
        if (Permissions.checkScreenBrightness(context, null)) {
            valueText.setEnabled((preference.adaptiveAllowed || preference.automatic == 0) && (preference.noChange == 0) && /*(preference.sharedProfile == 0) &&*/ (preference.changeLevel != 0));
            seekBar.setEnabled((preference.adaptiveAllowed || preference.automatic == 0) && (preference.noChange == 0) && /*(preference.sharedProfile == 0) &&*/ (preference.changeLevel != 0));
            automaticChBox.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
            changeLevelChBox.setEnabled((preference.adaptiveAllowed || preference.automatic == 0) && (preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
            if (preference.adaptiveAllowed) {
                //if (android.os.Build.VERSION.SDK_INT >= 21) { // for Android 5.0: adaptive brightness
                levelText.setText(R.string.brightness_pref_dialog_adaptive_level_may_not_working);
                //} else
                //    levelText.setVisibility(View.GONE);
            }
            levelText.setEnabled((preference.automatic != 0) && (preference.noChange == 0) && /*(preference.sharedProfile == 0) &&*/ (preference.changeLevel != 0));
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
            /*if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                Settings.System.putFloat(context.getContentResolver(),
                        ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, value);
            else*/ {
                try {
                    Settings.System.putFloat(context.getContentResolver(),
                            ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, value);
                } catch (Exception ee) {
                    PPApplication.startHandlerThread(/*"BrightnessDialogPreferenceFragmentX.setAdaptiveBrightness"*/);
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=BrightnessDialogPreferenceFragmentX.setAdaptiveBrightness");

                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME + " " + value;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command, "BrightnessDialogPreferenceFragmentX.setAdaptiveBrightness");
                                    } catch (Exception e) {
                                        // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("BrightnessDialogPreferenceFragmentX.setAdaptiveBrightness", Log.getStackTraceString(e));
                                        //PPApplication.recordException(e);
                                    }
                                }
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BrightnessDialogPreferenceFragmentX.setAdaptiveBrightness");
                        }
                    });
                }
            }
        }
    }

    private void setBrightnessFromSeekBar(int value) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("BrightnessDialogPreferenceFragmentX.setBrightnessFromSeekBar", "value=" + value);
            PPApplication.logE("BrightnessDialogPreferenceFragmentX.setBrightnessFromSeekBar", "computed value=" +
                    Profile.convertPercentsToBrightnessManualValue(value, context));
        }*/
        if (Permissions.checkScreenBrightness(context, null)) {
            if (preference.automatic == 1)
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            else
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            if (preference.changeLevel == 1) {
                boolean allowed = true;
                if (preference.automatic == 1)
                    allowed = preference.adaptiveAllowed;
                if (allowed) {
                    int __value = Profile.convertPercentsToBrightnessManualValue(value, context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, __value);
                    setAdaptiveBrightness(Profile.convertPercentsToBrightnessAdaptiveValue(value, context));
                }
            }
        }

        /*
        Window win = ((Activity)context).getWindow();
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        //if (preference.automatic == 1)
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        //else {
        //    if (preference.changeLevel == 1)
        //        //layoutParams.screenBrightness = Profile.convertPercentsToBrightnessManualValue(value, context) / (float) 255;
        //        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        //    else
        //        layoutParams.screenBrightness = preference.savedLayoutParamsBrightness;
        //}
        win.setAttributes(layoutParams);
        */
    }

}
