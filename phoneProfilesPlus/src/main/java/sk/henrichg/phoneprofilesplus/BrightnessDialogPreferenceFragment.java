package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class BrightnessDialogPreferenceFragment extends PreferenceDialogFragmentCompat
                implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener
{

    private Context context;
    private BrightnessDialogPreference preference;

    // Layout widgets.
    CheckBox noChangeChBox = null;
    private SeekBar seekBar = null;
    private TextView valueText = null;
    private CheckBox automaticChBox = null;
    //private CheckBox sharedProfileChBox = null;
    private CheckBox changeLevelChBox = null;
    private TextView levelText = null;
    private View checkBoxesDivider = null;
    private Button actualLevelBtn = null;

    private final Handler savedBrightnessHandler = new Handler(Looper.getMainLooper());
    private final Runnable savedBrightnessRunnable = this::setSavedBrightness;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        this.context = context;
        preference = (BrightnessDialogPreference) getPreference();
        preference.fragment = this;

        PPApplication.brightnessInternalChange = true;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_brightness_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        seekBar = view.findViewById(R.id.brightnessPrefDialogSeekbar);
        valueText = view.findViewById(R.id.brightnessPrefDialogValueText);
        noChangeChBox = view.findViewById(R.id.brightnessPrefDialogNoChange);
        automaticChBox = view.findViewById(R.id.brightnessPrefDialogAutomatic);
        //sharedProfileChBox = view.findViewById(R.id.brightnessPrefDialogSharedProfile);
        changeLevelChBox = view.findViewById(R.id.brightnessPrefDialogLevel);
        levelText = view.findViewById(R.id.brightnessPrefDialogAdaptiveLevelRoot);
        checkBoxesDivider = view.findViewById(R.id.brightnessPrefDialogCheckBoxesDivider);
        actualLevelBtn = view.findViewById(R.id.brightnessPrefDialogActualLevel);

        automaticChBox.setText(R.string.preference_profile_adaptiveBrightness);

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

        long actualLevel = ProfileStatic.convertBrightnessToPercents(PPApplication.savedBrightness);
        actualLevelBtn.setText(getString(R.string.brightness_pref_dialog_actual_level) +
                StringConstants.STR_COLON_WITH_SPACE + actualLevel);
        actualLevelBtn.setOnClickListener(v -> {
            preference.value = (int)ProfileStatic.convertBrightnessToPercents(PPApplication.savedBrightness);

            // Set the valueText text.
            valueText.setText(String.valueOf(preference.value));

            setBrightnessFromSeekBar(preference.value);
            seekBar.setProgress(preference.value);

            preference.callChangeListener(preference.getSValue());
        });

        if (Permissions.grantBrightnessDialogPermissions(context))
            enableViews();
    }

    private void setSavedBrightness() {
        if (Permissions.checkScreenBrightness(context, null)) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, PPApplication.savedBrightnessMode);
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, PPApplication.savedBrightness);
            //setAdaptiveBrightness(SettingsContentObserver.savedAdaptiveBrightness);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        savedBrightnessHandler.removeCallbacks(savedBrightnessRunnable);
        setSavedBrightness();

        PPApplication.brightnessInternalChange = false;
        PPExecutors.scheduleDisableBrightnessInternalChangeExecutor();

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
        if (buttonView.getId() == R.id.brightnessPrefDialogNoChange) {
            if (preference.forBrightnessSensor == 0)
                preference.noChange = (isChecked) ? 1 : 0;
            else
                preference.noChange = 0;

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

        if (buttonView.getId() == R.id.brightnessPrefDialogAutomatic) {
            if (preference.forBrightnessSensor == 0)
                preference.automatic = (isChecked) ? 1 : 0;
            else
                preference.automatic = 0;

            enableViews();
        }

        if (buttonView.getId() == R.id.brightnessPrefDialogLevel) {
            if (preference.forBrightnessSensor == 0)
                preference.changeLevel = (isChecked) ? 1 : 0;
            else
                preference.changeLevel = 1;

            enableViews();
        }

        // get values from sharedProfile when shared profile checkbox is checked
        int _automatic = preference.automatic;
        int _noChange = preference.noChange;
        final int _value = preference.value;
        int _changeLevel = preference.changeLevel;
        /*if (preference.sharedProfile == 1)
        {
            _automatic = (preference._sharedProfile.getDeviceBrightnessAutomatic()) ? 1 : 0;
            _noChange = (preference._sharedProfile.getDeviceBrightnessChange()) ? 0 : 1;
            _value = preference._sharedProfile.getDeviceBrightnessValue();
            _changeLevel = (preference._sharedProfile.getDeviceBrightnessChangeLevel()) ? 1 : 0;
        }*/

        if (_noChange == 1)
        {
            setSavedBrightness();

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
                    //boolean allowed = true;
                    //if (_automatic == 1)
                    //    allowed = preference.adaptiveAllowed;
                    //if (allowed) {
                    //    Handler handler = new Handler(context.getMainLooper());
                    //    handler.postDelayed(() -> {
                            int __value = ProfileStatic.convertPercentsToBrightnessManualValue(_value, context);
                            //Log.e("BrightnessDialogPreferenceFragment.onCheckedChanged", "__value="+__value);
                            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, __value);
                            //setAdaptiveBrightness(ProfileStatic.convertPercentsToBrightnessAdaptiveValue(_value, context));
                    //    }, 200);
                    //}
                }
                savedBrightnessHandler.removeCallbacks(savedBrightnessRunnable);
                savedBrightnessHandler.postDelayed(savedBrightnessRunnable, 5000);
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
        //Log.e("BrightnessDialogPreferenceFragment.onProgressChanged", "xxx");

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
        //Log.e("BrightnessDialogPreferenceFragment.onStopTrackingTouch", "xxx");
        //if ((android.os.Build.VERSION.SDK_INT >= 23))
            setBrightnessFromSeekBar(preference.value);
    }

    /*
    @Override
    public void onClick(View v) {
        Log.e("BrightnessDialogPreferenceFragment.onClick", "xxxxx");

        if (v.getId() == R.id.brightnessPrefDialogActualLevel) {
            preference.value = PPApplication.savedBrightness;

            // Set the valueText text.
            valueText.setText(String.valueOf(preference.value));

            setBrightnessFromSeekBar(preference.value);

            preference.callChangeListener(preference.getSValue());
        }
    }
    */

    void enableViews() {
        if (Permissions.checkScreenBrightness(context, null)) {
            if (preference.forBrightnessSensor == 0) {
                noChangeChBox.setVisibility(View.VISIBLE);
                changeLevelChBox.setVisibility(View.VISIBLE);
                automaticChBox.setVisibility(View.VISIBLE);
                changeLevelChBox.setVisibility(View.VISIBLE);
                levelText.setVisibility(View.VISIBLE);
                checkBoxesDivider.setVisibility(View.VISIBLE);

                valueText.setEnabled(/*(preference.adaptiveAllowed || preference.automatic == 0) &&*/ (preference.noChange == 0) && /*(preference.sharedProfile == 0) &&*/ (preference.changeLevel != 0));
                seekBar.setEnabled(/*(preference.adaptiveAllowed || preference.automatic == 0) &&*/ (preference.noChange == 0) && /*(preference.sharedProfile == 0) &&*/ (preference.changeLevel != 0));
                automaticChBox.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
                changeLevelChBox.setEnabled(/*(preference.adaptiveAllowed || preference.automatic == 0) &&*/ (preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
                //if (preference.adaptiveAllowed) {
                levelText.setText(R.string.brightness_pref_dialog_adaptive_level_may_not_working);
                levelText.setEnabled((preference.automatic != 0) && (preference.noChange == 0) && /*(preference.sharedProfile == 0) &&*/ (preference.changeLevel != 0));
                actualLevelBtn.setEnabled(/*(preference.adaptiveAllowed || preference.automatic == 0) &&*/ (preference.noChange == 0) && /*(preference.sharedProfile == 0) &&*/ (preference.changeLevel != 0));
            } else {
                noChangeChBox.setVisibility(View.GONE);
                changeLevelChBox.setVisibility(View.GONE);
                automaticChBox.setVisibility(View.GONE);
                changeLevelChBox.setVisibility(View.GONE);
                levelText.setVisibility(View.GONE);
                checkBoxesDivider.setVisibility(View.GONE);

                valueText.setEnabled(preference.changeLevel != 0);
                seekBar.setEnabled(preference.changeLevel != 0);
                automaticChBox.setEnabled(true);
                levelText.setText(R.string.brightness_pref_dialog_adaptive_level_may_not_working);
                levelText.setEnabled((preference.automatic != 0) && (preference.changeLevel != 0));
                actualLevelBtn.setEnabled(preference.changeLevel != 0);
            }
        } else {
            if (preference.forBrightnessSensor == 0) {
                noChangeChBox.setVisibility(View.VISIBLE);
                changeLevelChBox.setVisibility(View.VISIBLE);
                automaticChBox.setVisibility(View.VISIBLE);
                changeLevelChBox.setVisibility(View.VISIBLE);
                levelText.setVisibility(View.VISIBLE);
                checkBoxesDivider.setVisibility(View.VISIBLE);
            } else {
                noChangeChBox.setVisibility(View.GONE);
                changeLevelChBox.setVisibility(View.GONE);
                automaticChBox.setVisibility(View.GONE);
                changeLevelChBox.setVisibility(View.GONE);
                levelText.setVisibility(View.GONE);
                checkBoxesDivider.setVisibility(View.GONE);
            }

            valueText.setEnabled(false);
            seekBar.setEnabled(false);
            automaticChBox.setEnabled(false);
            changeLevelChBox.setEnabled(false);
            levelText.setEnabled(false);
            actualLevelBtn.setEnabled(false);
        }
    }

    /*
    private void setAdaptiveBrightness(final float value) {
*
        if (preference.adaptiveAllowed) {
            //if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
            //    Settings.System.putFloat(context.getContentResolver(),
            //            ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, value);
            //else
            {
                try {
                    Settings.System.putFloat(context.getContentResolver(),
                            Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, value);
                } catch (Exception ee) {
                    //PPApplication.startHandlerThread();
                    //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                    //__handler.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BrightnessDialogPreferenceFragment.setAdaptiveBrightness");

                        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                            synchronized (PPApplication.rootMutex) {
                                String command1 = "settings put system " + Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ + " " + value;
                                //if (PPApplication.isSELinuxEnforcing())
                                //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                Command command = new Command(0, command1); //, command2);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    RootUtils.commandWait(command, "BrightnessDialogPreferenceFragment.setAdaptiveBrightness");
                                } catch (Exception e) {
                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("BrightnessDialogPreferenceFragment.setAdaptiveBrightness", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                            }
                        }

                    }; //);
                    PPApplication.createBasicExecutorPool();
                    PPApplication.basicExecutorPool.submit(runnable);
                }
            }
        }
   }
   */

    private void setBrightnessFromSeekBar(int value) {
        if (Permissions.checkScreenBrightness(context, null)) {
            if (preference.automatic == 1)
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            else
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            if (preference.changeLevel == 1) {
                //boolean allowed = true;
                //if (preference.automatic == 1)
                //    allowed = preference.adaptiveAllowed;
                //if (allowed) {
                    int __value = ProfileStatic.convertPercentsToBrightnessManualValue(value, context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, __value);
                    //setAdaptiveBrightness(ProfileStatic.convertPercentsToBrightnessAdaptiveValue(value, context));
                //}
            }
            savedBrightnessHandler.removeCallbacks(savedBrightnessRunnable);
            savedBrightnessHandler.postDelayed(savedBrightnessRunnable, 5000);
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
