package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.pinball83.maskededittext.MaskedEditText;

public class MobileCellsRegistrationDialogPreference extends DialogPreference
                                        implements SeekBar.OnSeekBarChangeListener {

    private String eventId;
    private String value;

    private int mMin, mMax;

    MaterialDialog mDialog;
    private MaskedEditText mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;

    private int mColor = 0;

    public MobileCellsRegistrationDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.DurationDialogPreference, 0, 0);

        mMax = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMax, 5);
        mMin = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMin, 0);

        durationDialogType.recycle();

        if (PhoneProfilesService.isPhoneStateStarted())
            value = Integer.toString(GlobalData.phoneProfilesService.phoneStateScanner.durationForAutoRegistration);
        else
            value = "0";

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                        //.disableDefaultFonts()
                .icon(getDialogIcon())
                .positiveText(R.string.mobile_cells_registration_pref_dlg_start_button)
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.mobile_cells_registration_pref_dlg_stop_button)
                .content(getDialogMessage())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        int hours = mSeekBarHours.getProgress();
                        int minutes = mSeekBarMinutes.getProgress();
                        int seconds = mSeekBarSeconds.getProgress();

                        int iValue = (hours * 3600 + minutes * 60 + seconds);
                        if (iValue < mMin) iValue = mMin;
                        if (iValue > mMax) iValue = mMax;

                        if (PhoneProfilesService.isPhoneStateStarted()) {
                            GlobalData.phoneProfilesService.phoneStateScanner.durationForAutoRegistration = Integer.parseInt(value);
                            GlobalData.phoneProfilesService.phoneStateScanner.eventIdForAutoRegistration = Long.parseLong(eventId);
                            GlobalData.phoneProfilesService.phoneStateScanner.enabledAutoRegistration = true;
                        }

                        /*
                        value = String.valueOf(iValue);

                        if (callChangeListener(value)) {
                            //persistInt(mNumberPicker.getValue());
                            persistString(value);
                            setSummaryDDP();
                        }
                        */
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (PhoneProfilesService.isPhoneStateStarted()) {
                            GlobalData.phoneProfilesService.phoneStateScanner.durationForAutoRegistration = 0;
                            GlobalData.phoneProfilesService.phoneStateScanner.eventIdForAutoRegistration = 0;
                            GlobalData.phoneProfilesService.phoneStateScanner.enabledAutoRegistration = false;
                        }
                    }
                })
                ;

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_duration_pref_dialog2, null);
        onBindDialogView(layout);

        TextView mTextViewRange = (TextView) layout.findViewById(R.id.duration_pref_dlg_range);
        mValue = (MaskedEditText) layout.findViewById(R.id.duration_pref_dlg_value);
        mSeekBarHours = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_hours);
        mSeekBarMinutes = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_minutes);
        mSeekBarSeconds = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_seconds);

        mValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = mValue.getText().toString();
                int hours = 0;
                int minutes = 0;
                int seconds = 0;
                String[] splits = value.split(":");
                try {
                    hours = Integer.parseInt(splits[0].replaceFirst("\\s+$", ""));
                } catch (Exception e) {
                }
                try {
                    minutes = Integer.parseInt(splits[1].replaceFirst("\\s+$", ""));
                } catch (Exception e) {
                }
                try {
                    seconds = Integer.parseInt(splits[2].replaceFirst("\\s+$", ""));
                } catch (Exception e) {
                }

                int iValue = (hours * 3600 + minutes * 60 + seconds);

                boolean badText = false;
                if (iValue < mMin) {
                    iValue = mMin;
                    badText = true;
                }
                if (iValue > mMax) {
                    iValue = mMax;
                    badText = true;
                }

                if (mDialog != null) {
                    MDButton button = mDialog.getActionButton(DialogAction.POSITIVE);
                    button.setEnabled(!badText);
                }

                hours = iValue / 3600;
                minutes = (iValue % 3600) / 60;
                seconds = iValue % 60;

                mSeekBarHours.setProgress(hours);
                mSeekBarMinutes.setProgress(minutes);
                mSeekBarSeconds.setProgress(seconds);
            }
        });

        //mSeekBarHours.setRotation(180);
        //mSeekBarMinutes.setRotation(180);
        //mSeekBarSeconds.setRotation(180);

        // Initialize state
        int hours;
        int minutes;
        int seconds;
        hours = mMax / 3600;
        minutes = (mMax % 3600) / 60;
        seconds = mMax % 60;
        final String sMax = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        mSeekBarHours.setMax(hours);
        if (hours == 0)
            mSeekBarMinutes.setMax(minutes);
        else
            mSeekBarMinutes.setMax(59);
        if ((hours == 0) && (minutes == 0))
            mSeekBarSeconds.setMax(seconds);
        else
            mSeekBarSeconds.setMax(59);
        hours = mMin / 3600;
        minutes = (mMin % 3600) / 60;
        seconds = mMin % 60;
        final String sMin = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        int iValue = Integer.valueOf(value);
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);
        mSeekBarSeconds.setProgress(seconds);

        mValue.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

        mBuilder.customView(layout, false);

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if(restoreValue)
        {
            eventId = getPersistedString(eventId);
        }
        else
        {
            eventId = (String)defaultValue;
            persistString(eventId);
        }
        setSummaryDDP();
    }

    private void setSummaryDDP()
    {
        int iValue = Integer.parseInt(value);
        int hours = iValue / 3600;
        int minutes = (iValue % 3600) / 60;
        int seconds = iValue % 60;
        setSummary(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int hours = mSeekBarHours.getProgress();
            int minutes = mSeekBarMinutes.getProgress();
            int seconds = mSeekBarSeconds.getProgress();

            int iValue = (hours * 3600 + minutes * 60 + seconds);
            if (iValue < mMin) iValue = mMin;
            if (iValue > mMax) iValue = mMax;

            hours = iValue / 3600;
            minutes = (iValue % 3600) / 60;
            seconds = iValue % 60;

            mValue.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}