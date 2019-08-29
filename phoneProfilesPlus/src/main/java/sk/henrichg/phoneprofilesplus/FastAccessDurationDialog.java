package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

class FastAccessDurationDialog implements SeekBar.OnSeekBarChangeListener{

    private final int mMin, mMax;
    private final Profile mProfile;
    private int mAfterDo;
    private long mAfterDoProfile;

    private final DataWrapper mDataWrapper;
    //private final boolean mMonochrome;
    //private final int mMonochromeValue;
    private final int mStartupSource;
    //private final boolean mInteractive;
    private final Activity mActivity;
    //private boolean mLog;
    private final String[] afterDoValues;

    //Context mContext;

    private final AlertDialog mDialog;
    private final TextView mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;
    private final TextView mEnds;
    private final TimeDurationPickerDialog mValueDialog;
    private final AppCompatSpinner afterDoSpinner;
    private final TextView profileName;
    private final ImageView profileIcon;
    private final ImageView profileIndicators;

    private volatile Timer updateEndsTimer;

    //private int mColor = 0;

    @SuppressLint("SetTextI18n")
    FastAccessDurationDialog(Activity activity, Profile profile, DataWrapper dataWrapper,
                             /*boolean monochrome, int monochromeValue,*/
                             int startupSource/*, boolean interactive*/) {

        mMax = 86400;
        mMin = 0;
        mAfterDo = -1;
        mAfterDoProfile = Profile.PROFILE_NO_ACTIVATE;

        mActivity = activity;
        //mContext = activity.getBaseContext();
        mProfile = profile;
        mDataWrapper = dataWrapper;
        //mMonochrome = monochrome;
        //mMonochromeValue = monochromeValue;
        mStartupSource = startupSource;
        //mInteractive = true/*interactive*/;

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
            */

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(mActivity.getString(R.string.profile_preferences_duration) + " - " +
                               mActivity.getString(R.string.profile_string_0) + ": " + profile._name);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateEndsTimer = null;

                int hours = mSeekBarHours.getProgress();
                int minutes = mSeekBarMinutes.getProgress();
                int seconds = mSeekBarSeconds.getProgress();

                int iValue = (hours * 3600 + minutes * 60 + seconds);
                if (iValue < mMin) iValue = mMin;
                if (iValue > mMax) iValue = mMax;

                mProfile._duration = iValue;
                if (mAfterDo != -1)
                    mProfile._afterDurationDo = mAfterDo;
                DatabaseHandler.getInstance(mDataWrapper.context).updateProfile(mProfile);

                if (Permissions.grantProfilePermissions(mActivity, mProfile, false, false,
                        /*true, mMonochrome, mMonochromeValue,*/
                        mStartupSource, true, true, false))
                    mDataWrapper.activateProfileFromMainThread(mProfile, false, mStartupSource, true, mActivity);
                else
                    mDataWrapper.finishActivity(mStartupSource, true, mActivity);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateEndsTimer = null;
                mDataWrapper.finishActivity(mStartupSource, false, mActivity);
            }
        });
        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateEndsTimer = null;
                mDataWrapper.finishActivity(mStartupSource, false, mActivity);
            }
        });

        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_fast_access_duration_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        TextView afterDurationLabel = layout.findViewById(R.id.fast_access_duration_dlg_after_do_label);
        afterDurationLabel.setText(activity.getString(R.string.profile_preferences_afterDurationDo) + ":");

        TextView mTextViewRange = layout.findViewById(R.id.duration_pref_dlg_range);

        mValue = layout.findViewById(R.id.duration_pref_dlg_value);
        mSeekBarHours = layout.findViewById(R.id.duration_pref_dlg_hours);
        mSeekBarMinutes = layout.findViewById(R.id.duration_pref_dlg_minutes);
        mSeekBarSeconds = layout.findViewById(R.id.duration_pref_dlg_seconds);
        mEnds = layout.findViewById(R.id.duration_pref_dlg_ends);

        //mSeekBarHours.setRotation(180);
        //mSeekBarMinutes.setRotation(180);
        //mSeekBarSeconds.setRotation(180);

        // Initialize state
        int hours;
        int minutes;
        int seconds;
        hours = mMax / 3600;
        //minutes = (mMax % 3600) / 60;
        //seconds = mMax % 60;
        final String sMax = GlobalGUIRoutines.getDurationString(mMax);
        mSeekBarHours.setMax(hours);
        //if (hours == 0)
        //    mSeekBarMinutes.setMax(minutes);
        //else
            mSeekBarMinutes.setMax(59);
        //if ((hours == 0) && (minutes == 0))
        //    mSeekBarSeconds.setMax(seconds);
        //else
            mSeekBarSeconds.setMax(59);
        final String sMin = GlobalGUIRoutines.getDurationString(mMin);
        int iValue = mProfile._duration;
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);
        mSeekBarSeconds.setProgress(seconds);

        mValue.setText(GlobalGUIRoutines.getDurationString(iValue));
        mEnds.setText(GlobalGUIRoutines.getEndsAtString(iValue));

        mValueDialog = new TimeDurationPickerDialog(activity, new TimeDurationPickerDialog.OnDurationSetListener() {
            @Override
            public void onDurationSet(TimeDurationPicker view, long duration) {
                int iValue = (int) duration / 1000;

                if (iValue < mMin)
                    iValue = mMin;
                if (iValue > mMax)
                    iValue = mMax;

                mValue.setText(GlobalGUIRoutines.getDurationString(iValue));

                int hours = iValue / 3600;
                int minutes = (iValue % 3600) / 60;
                int seconds = iValue % 60;

                mSeekBarHours.setProgress(hours);
                mSeekBarMinutes.setProgress(minutes);
                mSeekBarSeconds.setProgress(seconds);

                updateTextFields(false);
            }
        }, iValue * 1000, TimeDurationPicker.HH_MM_SS);
        GlobalGUIRoutines.setThemeTimeDurationPickerDisplay(mValueDialog.getDurationInput(), activity);
        mValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int hours = mSeekBarHours.getProgress();
                    int minutes = mSeekBarMinutes.getProgress();
                    int seconds = mSeekBarSeconds.getProgress();

                    int iValue = (hours * 3600 + minutes * 60 + seconds);
                    if (iValue < mMin) iValue = mMin;
                    if (iValue > mMax) iValue = mMax;
                    mValueDialog.setDuration(iValue * 1000);
                    if (!mActivity.isFinishing())
                        mValueDialog.show();
                }
            }
        );

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

        afterDoSpinner = layout.findViewById(R.id.fast_access_duration_dlg_after_do_spinner);
        GlobalGUIRoutines.HighlightedSpinnerAdapter spinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                mActivity,
                R.layout.highlighted_spinner,
                mActivity.getResources().getStringArray(R.array.afterProfileDurationDoArray));
        spinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        afterDoSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        afterDoSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(mActivity.getBaseContext(), R.color.accent));
        /*switch (ApplicationPreferences.applicationTheme(mActivity, true)) {
            case "dark":
                afterDoSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dark);
                break;
            case "white":
                afterDoSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
//            case "dlight":
//                afterDoSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dlight);
//                break;
            default:
                afterDoSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
        }*/
        afterDoSpinner.setAdapter(spinnerAdapter);
        afterDoValues = mActivity.getResources().getStringArray(R.array.afterProfileDurationDoValues);
        afterDoSpinner.setSelection(Arrays.asList(afterDoValues).indexOf(String.valueOf(mProfile._afterDurationDo)));
        afterDoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)afterDoSpinner.getAdapter()).setSelection(position);
                mAfterDo = Integer.valueOf(afterDoValues[position]);

                updateProfileView();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        updateEndsTimer = new Timer();
        updateEndsTimer.schedule(new TimerTask() {
            private Activity activity;
            private TimerTask init(Activity a) {
                activity = a;
                return this;
            }

            @Override
            public void run() {
                if(updateEndsTimer != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(updateEndsTimer != null) {
                                updateTextFields(false);
                            }
                        }
                    });
                } else {
                    this.cancel();
                }
            }
        }.init(activity), 250, 250);

        TextView profileLabel = layout.findViewById(R.id.fast_access_duration_dlg_profile_label);
        profileLabel.setText(mDataWrapper.context.getString(R.string.profile_preferences_afterDurationProfile) + ":");

        profileName = layout.findViewById(R.id.fast_access_duration_dlg_profile_name);
        profileIcon = layout.findViewById(R.id.fast_access_duration_dlg_profile_icon);
        profileIndicators = layout.findViewById(R.id.fast_access_duration_dlg_profile_pref_indicator);
        if (!ApplicationPreferences.applicationActivatorPrefIndicator(mDataWrapper.context))
            profileIndicators.setVisibility(View.GONE);

        mAfterDoProfile = mProfile._afterDurationProfile;

        updateProfileView();

        final Button activateWithoutButton = layout.findViewById(R.id.fast_access_duration_dlg_activate_without);
        //activateWithoutButton.setAllCaps(false);
        activateWithoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEndsTimer = null;

                mProfile._duration = 0;
                DatabaseHandler.getInstance(mDataWrapper.context).updateProfile(mProfile);

                if (Permissions.grantProfilePermissions(mActivity, mProfile, false, false,
                        /*true, mMonochrome, mMonochromeValue,*/
                        mStartupSource, true, true, false))
                    mDataWrapper.activateProfileFromMainThread(mProfile, false, mStartupSource, true, mActivity);
                else
                    mDataWrapper.finishActivity(mStartupSource, true, mActivity);

                mDialog.dismiss();
            }
        });

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            updateTextFields(true);
        }
    }

    private void updateTextFields(boolean updateValueField) {
        int hours = mSeekBarHours.getProgress();
        int minutes = mSeekBarMinutes.getProgress();
        int seconds = mSeekBarSeconds.getProgress();

        int iValue = (hours * 3600 + minutes * 60 + seconds);
        if (iValue < mMin) iValue = mMin;
        if (iValue > mMax) iValue = mMax;

        if(mDialog!=null && mDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled()) {
            mEnds.setText(GlobalGUIRoutines.getEndsAtString(iValue));
        } else {
            mEnds.setText("--");
        }

        if(updateValueField) {
            mValue.setText(GlobalGUIRoutines.getDurationString(iValue));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void show() {
        if (!mActivity.isFinishing())
            mDialog.show();
    }

    private void updateProfileView()
    {
        PPApplication.logE("FastAccessDurationDialog.updateProfileView", "mProfile="+mProfile);

        if (mProfile == null)
        {
            profileName.setText(mDataWrapper.context.getString(R.string.profile_preference_profile_end_no_activate));
            profileIcon.setImageResource(R.drawable.ic_profile_default);
            profileIndicators.setImageResource(R.drawable.ic_empty);
        }
        else
        {
            if (mAfterDoProfile != Profile.PROFILE_NO_ACTIVATE) {

                boolean showIndicators = ApplicationPreferences.applicationActivatorPrefIndicator(mDataWrapper.context);

                Profile afterDoProfile = mDataWrapper.getProfileById(mAfterDoProfile, true, showIndicators, false);

                profileName.setText(afterDoProfile._name);
                if (afterDoProfile.getIsIconResourceID())
                {
                    if (afterDoProfile._iconBitmap != null)
                        profileIcon.setImageBitmap(afterDoProfile._iconBitmap);
                    else {
                        int res = Profile.getIconResource(afterDoProfile.getIconIdentifier());
                        profileIcon.setImageResource(res); // icon resource
                    }
                }
                else
                {
                    profileIcon.setImageBitmap(afterDoProfile._iconBitmap);
                }

                if (showIndicators)
                {
                    if (profileIndicators != null)
                    {
                        /*if (afterDoProfile == null)
                            profileIndicators.setImageResource(R.drawable.ic_empty);
                        else*/ {
                            if (afterDoProfile._preferencesIndicator != null)
                                profileIndicators.setImageBitmap(afterDoProfile._preferencesIndicator);
                            else
                                profileIndicators.setImageResource(R.drawable.ic_empty);
                        }
                    }
                }
            }
            else {
                profileName.setText(mDataWrapper.context.getString(R.string.profile_preference_profile_end_no_activate));
                profileIcon.setImageResource(R.drawable.ic_profile_default);
                profileIndicators.setImageResource(R.drawable.ic_empty);
            }
        }

        if (mAfterDo != 4) {
            int disabledColor = GlobalGUIRoutines.getThemeDisabledTextColor(mActivity);
            profileName.setTextColor(disabledColor);
            profileIcon.setColorFilter(disabledColor, android.graphics.PorterDuff.Mode.MULTIPLY);
            if (profileIndicators != null)
                profileIndicators.setColorFilter(disabledColor, android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else {
            profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(mActivity));
            profileIcon.setColorFilter(null);
            if (profileIndicators != null)
                profileIndicators.setColorFilter(null);
        }
    }

}
